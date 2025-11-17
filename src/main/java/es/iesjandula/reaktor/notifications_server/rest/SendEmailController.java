package es.iesjandula.reaktor.notifications_server.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.base.security.models.DtoAplicacion;

import es.iesjandula.reaktor.base_client.dtos.NotificationEmailDto;

import es.iesjandula.reaktor.notifications_server.utils.Constants;
import es.iesjandula.reaktor.notifications_server.utils.NotificationsServerException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import es.iesjandula.reaktor.notifications_server.models.Aplicacion;
import es.iesjandula.reaktor.notifications_server.models.Usuario;
import es.iesjandula.reaktor.notifications_server.models.notificacion_emails.NotificacionEmailAplicacion;
import es.iesjandula.reaktor.notifications_server.models.notificacion_emails.NotificacionEmailCopiaOcultaUsuario;
import es.iesjandula.reaktor.notifications_server.models.notificacion_emails.NotificacionEmailCopiaUsuario;
import es.iesjandula.reaktor.notifications_server.models.notificacion_emails.NotificacionEmailParaUsuario;
import es.iesjandula.reaktor.notifications_server.repository.INotificacionEmailCopiaOcultaUsuarioRepository;
import es.iesjandula.reaktor.notifications_server.repository.INotificacionEmailCopiaUsuarioRepository;
import es.iesjandula.reaktor.notifications_server.repository.INotificacionEmailParaUsuarioRepository;
import es.iesjandula.reaktor.notifications_server.repository.IUsuarioRepository;
import es.iesjandula.reaktor.notifications_server.repository.INotificacionEmailAplicacionRepository;
import es.iesjandula.reaktor.notifications_server.repository.IAplicacionRepository;

@Slf4j
@RestController
@RequestMapping("/notifications/email")
public class SendEmailController
{
    /* Atributo - Credenciales de Gmail */
    @Autowired 
    private Credential gmailCredentials;
    
    /* Atributo - Repositorio de notificaciones de emails para usuarios */
    @Autowired 
    private INotificacionEmailParaUsuarioRepository notificacionEmailParaUsuarioRepository;
    
    /* Atributo - Repositorio de notificaciones de emails de copias de usuarios */
    @Autowired 
    private INotificacionEmailCopiaUsuarioRepository notificacionEmailCopiaUsuarioRepository;
    
    /* Atributo - Repositorio de notificaciones de emails de copias ocultas de usuarios */
    @Autowired 
    private INotificacionEmailCopiaOcultaUsuarioRepository notificacionEmailCopiaOcultaUsuarioRepository;
    
    /* Atributo - Repositorio de usuarios */
    @Autowired 
    private IUsuarioRepository usuarioRepository;
    
    @Autowired
    private INotificacionEmailAplicacionRepository notificacionEmailAplicacionRepository;

    @Autowired
    private IAplicacionRepository aplicacionRepository;

    /* Atributo - Remitente del email */
    @Value("${reaktor.gmail.from}")
    private String from;


    @RequestMapping(method = RequestMethod.POST, value = "/send")
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_APLICACION_NOTIFICACIONES + "')")
    public ResponseEntity<?> crearNotificacionEmail(@AuthenticationPrincipal DtoAplicacion aplicacion,
                                                    @RequestBody NotificationEmailDto notificationEmailDto)
    {
        try
        {
            // Primero creamos los objetos en BBDD (notificación asociada a la aplicación que la envía)
            NotificacionEmailAplicacion notificacionEmailAplicacion = this.crearNotificacionEmailBBDD(aplicacion, notificationEmailDto);

            // Enviar el correo a través de Gmail API
            this.enviarCorreoGmailAPI(notificationEmailDto);

            // Actualizamos el indicador de envío de la notificación de email
            notificacionEmailAplicacion.setEnviado(true);

            // Guardamos la notificación de email en la base de datos
            this.notificacionEmailAplicacionRepository.save(notificacionEmailAplicacion);

            log.info("Correo enviado con éxito por la aplicación {}", 
                     notificacionEmailAplicacion.getAplicacion().getNombre());

            return ResponseEntity.ok().build();
        }
        catch (NotificationsServerException exception)
        {
            return ResponseEntity.status(400).body(exception.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String errorMessage = "Error inesperado al enviar el correo electrónico";
            log.error(errorMessage, exception);

            NotificationsServerException notificationsServerException =
                new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception);

            return ResponseEntity.status(500).body(notificationsServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Método - Crear notificación de email en BBDD (para aplicación emisora)
     *
     * @param dtoAplicacion - La aplicación emisora
     * @param notificationEmailDto - DTO de la petición de email
     * @return NotificacionEmailAplicacion - La notificación de email creada
     * @throws NotificationsServerException - Si hay un error al crear la notificación de email en la base de datos
     */
    private NotificacionEmailAplicacion crearNotificacionEmailBBDD(DtoAplicacion dtoAplicacion, NotificationEmailDto notificationEmailDto) throws NotificationsServerException
    {
        try
        {
            // Buscamos o creamos la aplicación emisora
            Aplicacion aplicacion = this.buscarOCrearAplicacion(dtoAplicacion);

            // Verificamos si la aplicación puede enviar otro email
            this.verificarYActualizarEnviosEmail(aplicacion);

            // ==========================================================
            // 2️⃣ Crear la notificación de email para la aplicación
            // ==========================================================
            NotificacionEmailAplicacion notificacionAplicacion = new NotificacionEmailAplicacion();

            // Asignar la aplicación emisora
            notificacionAplicacion.setAplicacion(aplicacion);

            // Asignar asunto, contenido y fecha de creación
            notificacionAplicacion.setAsunto(notificationEmailDto.getSubject());
            notificacionAplicacion.setContenido(notificationEmailDto.getBody());
            notificacionAplicacion.setFechaCreacion(new Date());

            // Guardar la notificación principal
            NotificacionEmailAplicacion notificacionEmailAplicacion = this.notificacionEmailAplicacionRepository.save(notificacionAplicacion);

            // ==========================================================
            // 3️⃣ Guardar los destinatarios del email (TO)
            // ==========================================================
            if (notificationEmailDto.getTo() != null)
            {
                for (String correo : notificationEmailDto.getTo())
                {
                    Usuario usuarioPara = this.obtenerUsuarioPorEmail(correo);

                    NotificacionEmailParaUsuario notificacionPara = new NotificacionEmailParaUsuario();
                    notificacionPara.setUsuario(usuarioPara);
                    notificacionPara.setNotificacionEmailAplicacion(notificacionEmailAplicacion);

                    this.notificacionEmailParaUsuarioRepository.save(notificacionPara);
                }
            }

            // ==========================================================
            // 4️⃣ Guardar los destinatarios en copia (CC)
            // ==========================================================
            if (notificationEmailDto.getCc() != null)
            {
                for (String correo : notificationEmailDto.getCc())
                {
                    Usuario usuarioCopia = this.obtenerUsuarioPorEmail(correo);

                    NotificacionEmailCopiaUsuario notificacionCopia = new NotificacionEmailCopiaUsuario();
                    notificacionCopia.setUsuario(usuarioCopia);
                    notificacionCopia.setNotificacionEmailAplicacion(notificacionEmailAplicacion);

                    this.notificacionEmailCopiaUsuarioRepository.save(notificacionCopia);
                }
            }

            // ==========================================================
            // 5️⃣ Guardar los destinatarios en copia oculta (BCC)
            // ==========================================================
            if (notificationEmailDto.getBcc() != null)
            {
                for (String correo : notificationEmailDto.getBcc())
                {
                    Usuario usuarioCopiaOculta = this.obtenerUsuarioPorEmail(correo);

                    NotificacionEmailCopiaOcultaUsuario notificacionCopiaOculta = new NotificacionEmailCopiaOcultaUsuario();
                    notificacionCopiaOculta.setUsuario(usuarioCopiaOculta);
                    notificacionCopiaOculta.setNotificacionEmailAplicacion(notificacionEmailAplicacion);

                    this.notificacionEmailCopiaOcultaUsuarioRepository.save(notificacionCopiaOculta);
                }
            }

            // ==========================================================
            // 6️⃣ Registro completo
            // ==========================================================
            log.info("Notificación creada correctamente para la aplicación '{}'", aplicacion.getNombre());

            return notificacionEmailAplicacion;
        }
        catch (Exception exception)
        {
            String errorMessage = "Error al crear la notificación de email de aplicación en base de datos";
            log.error(errorMessage, exception);
            throw new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception);
        }
    }

    /**
     * Método - Buscar o crear la aplicación emisora
     *
     * @param dtoAplicacion - La aplicación emisora
     * @return Aplicacion - La aplicación emisora creada
     */
    private Aplicacion buscarOCrearAplicacion(DtoAplicacion dtoAplicacion)
    {
        Aplicacion aplicacion = this.aplicacionRepository.findByNombre(dtoAplicacion.getNombre());
        if (aplicacion == null)
        {
            aplicacion = new Aplicacion();
            aplicacion.setNombre(dtoAplicacion.getNombre());

            // Cuando tengas hecho la tabla de constantes, deberás devolver aquí los valores almacenados
            aplicacion.setNotifMaxWeb(10);
            aplicacion.setNotifMaxEmail(10);
            aplicacion.setNotifMaxCalendar(10);

            // Almacenamos la aplicación en la base de datos
            aplicacion = this.aplicacionRepository.save(aplicacion);
        }

        return aplicacion;
    }

    /**
     * Verifica si el usuario puede enviar otro email y actualiza sus contadores.
     *
     * @param aplicacion Aplicación que intenta enviar un correo
     * @throws NotificationsServerException Si supera el máximo diario permitido
     */
    private void verificarYActualizarEnviosEmail(Aplicacion aplicacion) throws NotificationsServerException
    {
        // Si la fecha de la última notificación no es de hoy, reiniciamos contador
        if (aplicacion.getFechaUltimaNotificacionEmail() == null ||
            !aplicacion.getFechaUltimaNotificacionEmail().toLocalDate().equals(java.time.LocalDate.now()))
        {
            aplicacion.setNotifHoyEmail(0);
        }

        // Comprobar límite diario
        if (aplicacion.getNotifHoyEmail() > aplicacion.getNotifMaxEmail())
        {
            String errorMessage = "La aplicación " + aplicacion.getNombre() + " ha superado el máximo de emails diarios permitidos.";
            log.warn(errorMessage);

            throw new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage);
        }

        // Actualizar fecha y contador
        aplicacion.setFechaUltimaNotificacionEmail(java.time.LocalDateTime.now());
        aplicacion.setNotifHoyEmail(aplicacion.getNotifHoyEmail() + 1);

        // Guardar cambios
        this.aplicacionRepository.save(aplicacion);
    }


    /** Método - Obtener usuario por email
     *
     * @param email - Email del usuario
     * @return Usuario
     * @throws NotificationsServerException - Si el usuario no existe
     */
    private Usuario obtenerUsuarioPorEmail(String email) throws NotificationsServerException
    {
        // Buscamos el usuario por email
        Optional<Usuario> usuarioOptional = this.usuarioRepository.findByEmail(email);
        if (usuarioOptional.isEmpty())
        {
            String errorMessage = "Usuario no encontrado: " + email;
            log.error(errorMessage);
            throw new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, null);
        }

        return usuarioOptional.get();
    }

    /**
     * Método - Enviar correo a través de Gmail API
     *
     * @param notificationEmailDto - La petición de email a enviar
     * @throws NotificationsServerException - Si hay un error al enviar el correo electrónico
     */
    private void enviarCorreoGmailAPI(NotificationEmailDto notificationEmailDto) throws NotificationsServerException
    {
        try
        {
            // Configuro el servicio de Gmail
            Gmail service = this.enviarCorreGmailAPICrearServicioGmail();

            // Configuro el mensaje de email
            MimeMessage email = this.enviarCorreoGmailAPICrearMensajeEmail();

            // Añado el remitente, subject y contenido del email
            this.enviarCorreoGmailAPIConfigurarRemitenteSubjectContenido(email, notificationEmailDto);

            // Configuro los destinatarios del email
            this.enviarCorreoGmailAPIConfigurarDestinatarios(email, notificationEmailDto);

            // Configuro el envío del email y obtengo el mensaje final a enviar
            Message message = this.enviarCorreoGmailAPIConfigurarEnvio(email, notificationEmailDto);

            // Envío el email mediante Gmail API
            service.users().messages().send("me", message).execute();

            log.info("Email enviado correctamente mediante Gmail API");
        }
        catch (NotificationsServerException exception)
        {
            // Propago la excepción tal cual si ya es controlada
            throw exception;
        }
        catch (Exception exception)
        {
            // Error inesperado al enviar el correo
            String errorMessage = "Error al enviar el email";
            log.error(errorMessage, exception);
            throw new NotificationsServerException(
                    Constants.ERR_GENERIC_EXCEPTION_CODE,
                    errorMessage,
                    exception
            );
        }
    }


    /** 
     * Método - Crear servicio de Gmail
     *
     * @return Gmail - El servicio de Gmail creado
     * @throws NotificationsServerException - Si hay un error al crear el servicio de Gmail
     */
    private Gmail enviarCorreGmailAPICrearServicioGmail() throws NotificationsServerException
    {
        try
        { 
            // Creo el servicio de Gmail
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            // Creo el servicio de Gmail
            return new Gmail.Builder(httpTransport, GsonFactory.getDefaultInstance(), this.gmailCredentials)
                            .setApplicationName(Constants.GMAIL_APPLICATION_NAME)
                            .build() ;
        }
        catch (Exception exception)
        {
            String errorMessage = "Error al crear el servicio de Gmail";
            log.error(errorMessage, exception);
            throw new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception);
        }
        
    }

    /** Método - Crear mensaje de email
     *
     * @return MimeMessage - El mensaje de email creado
     */
    private MimeMessage enviarCorreoGmailAPICrearMensajeEmail()
    {
        // Añado las propiedades de la sesión
        Properties properties = new Properties();

        // Creo la sesión
        Session session = Session.getDefaultInstance(properties, null);

        // Devuelvo el mensaje de email
        return new MimeMessage(session);
    }

    /** Método - Configurar remitente, subject y contenido del email
     *
     * @param email - El mensaje de email a configurar
     * @param notificationEmailDto - La petición de email a configurar
     * @throws NotificationsServerException - Si hay un error al configurar el remitente, subject y contenido del email
     */
    private void enviarCorreoGmailAPIConfigurarRemitenteSubjectContenido(MimeMessage email, NotificationEmailDto notificationEmailDto) throws NotificationsServerException
    {
        try
        {
            // Añado el remitente del email
            email.setFrom(new InternetAddress(this.from));

            // Añado el asunto y el cuerpo del email
            email.setSubject(notificationEmailDto.getSubject());
            email.setText(notificationEmailDto.getBody());
        }
        catch (Exception exception)
        {
            String errorMessage = "Error al configurar el remitente, subject y contenido del email";
            log.error(errorMessage, exception);
            throw new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception);
        }
    }

    /** Método - Configurar destinatarios del email
     *
     * @param email - El mensaje de email a configurar
     * @param notificationEmailDto - La petición de email a configurar
     * @throws NotificationsServerException - Si hay un error al configurar los destinatarios del email
     */
    private void enviarCorreoGmailAPIConfigurarDestinatarios(MimeMessage email, NotificationEmailDto notificationEmailDto) throws NotificationsServerException
    {
        try
        {
            // Añado los destinatarios del email
            if (notificationEmailDto.getTo() != null)
            {
                for (String recipient : notificationEmailDto.getTo())
                {
                    email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(recipient));
                }
            }

            // Añado los copias del email
            if (notificationEmailDto.getCc() != null)
            {
                for (String recipient : notificationEmailDto.getCc())
                {
                    email.addRecipient(javax.mail.Message.RecipientType.CC, new InternetAddress(recipient));
                }
            }

            // Añado los copias ocultas del email
            if (notificationEmailDto.getBcc() != null)
            {
                for (String recipient : notificationEmailDto.getBcc())
                {
                    email.addRecipient(javax.mail.Message.RecipientType.BCC, new InternetAddress(recipient));
                }
            }
        }
        catch (Exception exception)
        {
            String errorMessage = "Error al configurar los destinatarios del email";
            log.error(errorMessage, exception);
            throw new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception);
        }
    }

    /** Método - Configurar envío del email
     *
     * @param email - El mensaje de email a configurar
     * @param notificationEmailDto - La petición de email a configurar
     * @return Message - El mensaje de email configurado
     * @throws NotificationsServerException - Si hay un error al configurar el envío del email
     */
    private Message enviarCorreoGmailAPIConfigurarEnvio(MimeMessage email, NotificationEmailDto notificationEmailDto) throws NotificationsServerException
    {
        try
        {
            // Convierto el email a un array de bytes
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            email.writeTo(buffer);

            // Convierto el array de bytes a una cadena de base64
            byte[] rawMessageBytes = buffer.toByteArray();
            String encodedEmail = Base64.getUrlEncoder().withoutPadding().encodeToString(rawMessageBytes);

            // Creo el mensaje de email
            Message message = new Message();

            // Añado el mensaje de email a la solicitud
            message.setRaw(encodedEmail);

            return message;
        }
        catch (Exception exception)
        {
            String errorMessage = "Error al configurar el envío del email";
            log.error(errorMessage, exception);
            throw new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception);
        }
    }
}
