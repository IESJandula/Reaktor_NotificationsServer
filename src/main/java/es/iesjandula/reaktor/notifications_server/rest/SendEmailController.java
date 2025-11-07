package es.iesjandula.reaktor.notifications_server.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.notifications_server.dtos.EmailRequestDto;
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

import es.iesjandula.reaktor.notifications_server.models.Usuario;
import es.iesjandula.reaktor.notifications_server.models.notificacion_emails.usuario.*;
import es.iesjandula.reaktor.notifications_server.repository.*;

@Slf4j
@RestController
public class SendEmailController
{
    /* Atributo - Credenciales de Gmail */
    @Autowired 
    private Credential gmailCredentials;
    
    /* Atributo - Repositorio de notificaciones de emails de usuarios */
    @Autowired 
    private INotificacionesEmailUsuarioRepository notificacionEmailUsuarioRepository;
    
    /* Atributo - Repositorio de notificaciones de emails para usuarios */
    @Autowired 
    private INotificacionesEmailParaUsuarioRepository paraUsuarioRepository;
    
    /* Atributo - Repositorio de notificaciones de emails de copias de usuarios */
    @Autowired 
    private INotificacionesEmailCopiaUsuarioRepository copiaUsuarioRepository;
    
    /* Atributo - Repositorio de notificaciones de emails de copias ocultas de usuarios */
    @Autowired 
    private INotificacionesEmailCopiaOcultaUsuarioRepository copiaOcultaUsuarioRepository;
    
    /* Atributo - Repositorio de usuarios */
    @Autowired 
    private IUsuarioRepository usuarioRepository;

    /* Atributo - Remitente del email */
    @Value("${reaktor.gmail.from}")
    private String from;


    @RequestMapping(method = RequestMethod.POST, value = "/")
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
    public ResponseEntity<?> crearNotificacionEmail(@RequestBody EmailRequestDto emailRequestDto)
    {
        try
        {
            // Primero creamos los objetos en BBDD
            NotificacionEmailUsuario notificacionEmailUsuario = this.crearNotificacionEmailBBDD(emailRequestDto) ;

            // Enviar el correo a través de Gmail API
            this.enviarCorreoGmailAPI(emailRequestDto, notificacionEmailUsuario);

            // Actualizamos el indicador de envío de la notificación de email
            notificacionEmailUsuario.setEnviado(true);

            // Guardamos la notificación de email en la base de datos
            this.notificacionEmailUsuarioRepository.save(notificacionEmailUsuario);

            log.info("Correo enviado con éxito");
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

            NotificationsServerException notificationsServerException = new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception);
            return ResponseEntity.status(500).body(notificationsServerException.getBodyExceptionMessage());
        }
    }

    /** Método - Crear notificación de email en BBDD 
     *
     * @param emailRequestDto - DTO de la petición de email
     * @return NotificacionEmailUsuario - La notificación de email creada
     * @throws NotificationsServerException - Si hay un error al crear la notificación de email en la base de datos
    */
    private NotificacionEmailUsuario crearNotificacionEmailBBDD(EmailRequestDto emailRequestDto) throws NotificationsServerException
    {
        // Creamos la notificación de email
        NotificacionEmailUsuario notificacion = new NotificacionEmailUsuario();

        // Añadimos el asunto y el contenido de la notificación
        notificacion.setAsunto(emailRequestDto.getSubject());
        notificacion.setContenido(emailRequestDto.getBody());

        // Añadimos la fecha de creación de la notificación
        notificacion.setFechaCreacion(new Date());

        // Añadimos el usuario emisor a la notificación
        notificacion.setUsuario(this.obtenerUsuarioPorEmail(this.from));

        // Guardamos la notificación en la base de datos
        NotificacionEmailUsuario notificacionEmailUsuario = this.notificacionEmailUsuarioRepository.save(notificacion);

        // Añadimos los destinatarios de la notificación
        if (emailRequestDto.getTo() != null)
        {
            for (String correo : emailRequestDto.getTo())
            {
                // Obtenemos el usuario destinatario
                Usuario usuarioPara = this.obtenerUsuarioPorEmail(correo);

                // Creamos la notificación de email para el usuario destinatario
                NotificacionEmailParaUsuario notificacionEmailParaUsuario = new NotificacionEmailParaUsuario();
                notificacionEmailParaUsuario.setUsuario(usuarioPara);
                notificacionEmailParaUsuario.setNotificacionEmailUsuario(notificacionEmailUsuario);

                // Guardamos la notificación de email para el usuario destinatario en la base de datos
                this.paraUsuarioRepository.save(notificacionEmailParaUsuario);
            }
        }

        // Añadimos las copias de la notificación
        if (emailRequestDto.getCc() != null)
        {
            for (String correo : emailRequestDto.getCc())
            {
                // Obtenemos el usuario copia
                Usuario usuarioCopia = this.obtenerUsuarioPorEmail(correo);

                // Creamos la notificación de email para el usuario copia
                NotificacionEmailCopiaUsuario notificacionEmailCopiaUsuario = new NotificacionEmailCopiaUsuario();
                notificacionEmailCopiaUsuario.setUsuario(usuarioCopia);
                notificacionEmailCopiaUsuario.setNotificacionEmailUsuario(notificacionEmailUsuario);

                // Guardamos la notificación de email para el usuario copia en la base de datos
                this.copiaUsuarioRepository.save(notificacionEmailCopiaUsuario);
            }
        }

        // Añadimos las copias ocultas de la notificación
        if (emailRequestDto.getBcc() != null)
        {
            for (String correo : emailRequestDto.getBcc())
            {
                // Obtenemos el usuario copia oculta
                Usuario usuarioCopiaOculta = this.obtenerUsuarioPorEmail(correo);

                // Creamos la notificación de email para el usuario copia oculta
                NotificacionEmailCopiaOcultaUsuario notificacionEmailCopiaOcultaUsuario = new NotificacionEmailCopiaOcultaUsuario();
                notificacionEmailCopiaOcultaUsuario.setUsuario(usuarioCopiaOculta);
                notificacionEmailCopiaOcultaUsuario.setNotificacionEmailUsuario(notificacionEmailUsuario);

                // Guardamos la notificación de email para el usuario copia oculta en la base de datos
                this.copiaOcultaUsuarioRepository.save(notificacionEmailCopiaOcultaUsuario);
            }
        }

        return notificacionEmailUsuario;
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

    /** Método - Enviar correo a través de Gmail API
     *
     * @param emailRequestDto - La petición de email a enviar
     * @param notificacionEmailUsuario - La notificación de email a enviar
     * @throws NotificationsServerException - Si hay un error al enviar el correo electrónico
     */
    private void enviarCorreoGmailAPI(EmailRequestDto emailRequestDto, NotificacionEmailUsuario notificacionEmailUsuario) throws NotificationsServerException
    {
        // Configuro el servicio de Gmail
        Gmail service = this.enviarCorreGmailAPICrearServicioGmail();

        // Configuro el mensaje de email
        MimeMessage email = this.enviarCorreoGmailAPICrearMensajeEmail();

        // Añado el remitente, subject y contenido del email
        this.enviarCorreoGmailAPIConfigurarRemitenteSubjectContenido(email, emailRequestDto);
        
        // Configuro los destinatarios del email
        this.enviarCorreoGmailAPIConfigurarDestinatarios(email, emailRequestDto);

        // Configuro el envío del email y obtengo el mensaje de email
        Message message = this.enviarCorreoGmailAPIConfigurarEnvio(email, emailRequestDto);

        try
        {
            // Envío el email
            service.users().messages().send("me", message).execute();
        }
        catch (Exception exception)
        {
            String errorMessage = "Error al enviar el email";
            log.error(errorMessage, exception);
            throw new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception);
        }
    }

    /** Método - Crear servicio de Gmail
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
     * @param emailRequestDto - La petición de email a configurar
     * @throws NotificationsServerException - Si hay un error al configurar el remitente, subject y contenido del email
     */
    private void enviarCorreoGmailAPIConfigurarRemitenteSubjectContenido(MimeMessage email, EmailRequestDto emailRequestDto) throws NotificationsServerException
    {
        try
        {
            // Añado el remitente del email
            email.setFrom(new InternetAddress(this.from));

            // Añado el asunto y el cuerpo del email
            email.setSubject(emailRequestDto.getSubject());
            email.setText(emailRequestDto.getBody());
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
     * @param emailRequestDto - La petición de email a configurar
     * @throws NotificationsServerException - Si hay un error al configurar los destinatarios del email
     */
    private void enviarCorreoGmailAPIConfigurarDestinatarios(MimeMessage email, EmailRequestDto emailRequestDto) throws NotificationsServerException
    {
        try
        {
            // Añado los destinatarios del email
            if (emailRequestDto.getTo() != null)
            {
                for (String recipient : emailRequestDto.getTo())
                {
                    email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(recipient));
                }
            }

            // Añado los copias del email
            if (emailRequestDto.getCc() != null)
            {
                for (String recipient : emailRequestDto.getCc())
                {
                    email.addRecipient(javax.mail.Message.RecipientType.CC, new InternetAddress(recipient));
                }
            }

            // Añado los copias ocultas del email
            if (emailRequestDto.getBcc() != null)
            {
                for (String recipient : emailRequestDto.getBcc())
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
     * @param emailRequestDto - La petición de email a configurar
     * @return Message - El mensaje de email configurado
     * @throws NotificationsServerException - Si hay un error al configurar el envío del email
     */
    private Message enviarCorreoGmailAPIConfigurarEnvio(MimeMessage email, EmailRequestDto emailRequestDto) throws NotificationsServerException
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
