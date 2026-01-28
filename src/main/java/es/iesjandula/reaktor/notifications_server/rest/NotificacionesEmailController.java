package es.iesjandula.reaktor.notifications_server.rest;

import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.Date;
import java.util.Optional;

import es.iesjandula.reaktor.notifications_server.models.Aplicacion;
import es.iesjandula.reaktor.notifications_server.models.Constante;
import es.iesjandula.reaktor.notifications_server.models.Usuario;
import es.iesjandula.reaktor.notifications_server.models.notificacion_emails.NotificacionEmailAplicacion;
import es.iesjandula.reaktor.notifications_server.models.notificacion_emails.NotificacionEmailCopiaOcultaUsuario;
import es.iesjandula.reaktor.notifications_server.models.notificacion_emails.NotificacionEmailCopiaUsuario;
import es.iesjandula.reaktor.notifications_server.models.notificacion_emails.NotificacionEmailParaUsuario;
import es.iesjandula.reaktor.notifications_server.repository.INotificacionEmailCopiaOcultaUsuarioRepository;
import es.iesjandula.reaktor.notifications_server.repository.INotificacionEmailCopiaUsuarioRepository;
import es.iesjandula.reaktor.notifications_server.repository.INotificacionEmailParaUsuarioRepository;
import es.iesjandula.reaktor.notifications_server.services.AplicacionesService;
import es.iesjandula.reaktor.notifications_server.services.EnvioCorreosGmailService;
import es.iesjandula.reaktor.notifications_server.repository.INotificacionEmailAplicacionRepository;
import es.iesjandula.reaktor.notifications_server.repository.IAplicacionRepository;
import es.iesjandula.reaktor.notifications_server.repository.IConstanteRepository;
import es.iesjandula.reaktor.notifications_server.services.UsersService;

@Slf4j
@RestController
@RequestMapping("/notifications/email")
public class NotificacionesEmailController
{    
    /* Atributo - Repositorio de notificaciones de emails para usuarios */
    @Autowired 
    private INotificacionEmailParaUsuarioRepository notificacionEmailParaUsuarioRepository;
    
    /* Atributo - Repositorio de notificaciones de emails de copias de usuarios */
    @Autowired 
    private INotificacionEmailCopiaUsuarioRepository notificacionEmailCopiaUsuarioRepository;
    
    /* Atributo - Repositorio de notificaciones de emails de copias ocultas de usuarios */
    @Autowired 
    private INotificacionEmailCopiaOcultaUsuarioRepository notificacionEmailCopiaOcultaUsuarioRepository;
    
    @Autowired
    private INotificacionEmailAplicacionRepository notificacionEmailAplicacionRepository;

    @Autowired
    private IAplicacionRepository aplicacionRepository;

    @Autowired
    private IConstanteRepository constanteRepository;

    @Autowired
    private AplicacionesService aplicacionesService;

    @Autowired
    private UsersService usersService;

    @Autowired
    private EnvioCorreosGmailService envioCorreosGmailService;

    @RequestMapping(method = RequestMethod.POST, value = "/")
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_APLICACION_NOTIFICACIONES + "')")
    public ResponseEntity<?> crearNotificacionEmail(@AuthenticationPrincipal DtoAplicacion aplicacion,
                                                    @RequestBody NotificationEmailDto notificationEmailDto)
    {
        try
        {
            // Primero creamos los objetos en BBDD (notificación asociada a la aplicación que la envía)
            NotificacionEmailAplicacion notificacionEmailAplicacion = this.crearNotificacionEmailBBDD(aplicacion, notificationEmailDto);

            // Enviar el correo a través de Gmail API
            this.envioCorreosGmailService.enviarCorreoGmailAPI(notificationEmailDto);

            // Actualizamos el indicador de envío de la notificación de email
            notificacionEmailAplicacion.setEnviado(true);

            // Guardamos la notificación de email en la base de datos
            this.notificacionEmailAplicacionRepository.save(notificacionEmailAplicacion);

            // Obtenemos la aplicación emisora
            Aplicacion aplicacionEmisora = notificacionEmailAplicacion.getAplicacion();

            // Actualizamos la aplicación al enviar la notificación de email
            this.aplicacionesService.aplicacionHaEnviadoNotificacionEmail(aplicacionEmisora);

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
            this.verificarEnviosEmail(aplicacion);

            // Creamos la notificación de email para la aplicación
            NotificacionEmailAplicacion notificacionAplicacion = new NotificacionEmailAplicacion();

            // Asignar la aplicación emisora
            notificacionAplicacion.setAplicacion(aplicacion);

            // Asignar asunto, contenido y fecha de creación
            notificacionAplicacion.setAsunto(notificationEmailDto.getSubject());
            notificacionAplicacion.setContenido(notificationEmailDto.getBody());
            notificacionAplicacion.setFechaCreacion(new Date());

            // Guardar la notificación principal
            NotificacionEmailAplicacion notificacionEmailAplicacion = this.notificacionEmailAplicacionRepository.save(notificacionAplicacion);

            // Guardamos los destinatarios del email (TO)
            if (notificationEmailDto.getTo() != null)
            {
                for (String correo : notificationEmailDto.getTo())
                {
                    Usuario usuarioPara = this.usersService.obtenerUsuarioPorEmail(correo);

                    NotificacionEmailParaUsuario notificacionPara = new NotificacionEmailParaUsuario();
                    notificacionPara.setUsuario(usuarioPara);
                    notificacionPara.setNotificacionEmailAplicacion(notificacionEmailAplicacion);

                    this.notificacionEmailParaUsuarioRepository.save(notificacionPara);
                }
            }

            // Guardamos los destinatarios en copia (CC)
            if (notificationEmailDto.getCc() != null)
            {
                for (String correo : notificationEmailDto.getCc())
                {
                    Usuario usuarioCopia = this.usersService.obtenerUsuarioPorEmail(correo);

                    NotificacionEmailCopiaUsuario notificacionCopia = new NotificacionEmailCopiaUsuario();
                    notificacionCopia.setUsuario(usuarioCopia);
                    notificacionCopia.setNotificacionEmailAplicacion(notificacionEmailAplicacion);

                    this.notificacionEmailCopiaUsuarioRepository.save(notificacionCopia);
                }
            }

            // Guardamos los destinatarios en copia oculta (BCC)
            if (notificationEmailDto.getBcc() != null)
            {
                for (String correo : notificationEmailDto.getBcc())
                {
                    Usuario usuarioCopiaOculta = this.usersService.obtenerUsuarioPorEmail(correo);

                    NotificacionEmailCopiaOcultaUsuario notificacionCopiaOculta = new NotificacionEmailCopiaOcultaUsuario();
                    notificacionCopiaOculta.setUsuario(usuarioCopiaOculta);
                    notificacionCopiaOculta.setNotificacionEmailAplicacion(notificacionEmailAplicacion);

                    this.notificacionEmailCopiaOcultaUsuarioRepository.save(notificacionCopiaOculta);
                }
            }

            // Logueamos el registro completo
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
     * @throws NotificationsServerException - Si hay un error al buscar o crear la aplicación
     */
    private Aplicacion buscarOCrearAplicacion(DtoAplicacion dtoAplicacion) throws NotificationsServerException
    {
        Aplicacion aplicacion = this.aplicacionRepository.findByNombre(dtoAplicacion.getNombre());
        if (aplicacion == null)
        {
            aplicacion = new Aplicacion();
            aplicacion.setNombre(dtoAplicacion.getNombre());

            // Obtenemos las notificaciones máximas según el tipo
            aplicacion.setNotifMaxWeb(this.obtenerNotificacionesMaximasSegunTipo(Constants.TABLA_CONST_NOTIFICACIONES_MAX_WEB));
            aplicacion.setNotifMaxEmail(this.obtenerNotificacionesMaximasSegunTipo(Constants.TABLA_CONST_NOTIFICACIONES_MAX_EMAILS));
            aplicacion.setNotifMaxCalendar(this.obtenerNotificacionesMaximasSegunTipo(Constants.TABLA_CONST_NOTIFICACIONES_MAX_CALENDAR));

            // Almacenamos la aplicación en la base de datos
            aplicacion = this.aplicacionRepository.save(aplicacion);
        }

        return aplicacion;
    }

    /**
     * Método - Obtener las notificaciones máximas según el tipo
     *
     * @param tipo - El tipo de notificación
     * @return int - Las notificaciones máximas
     * @throws NotificationsServerException - Si hay un error al obtener las notificaciones máximas
     */
    private int obtenerNotificacionesMaximasSegunTipo(String tipo) throws NotificationsServerException
    {
        Optional<Constante> optionalConstante = this.constanteRepository.findById(tipo);
        if (optionalConstante.isEmpty())
        {
            // Construimos el mensaje de error
            String errorMessage = "La constante " + tipo + " no está configurada";

            // Logueamos
            log.error(errorMessage);

            // Lanzamos una excepción
            throw new NotificationsServerException(Constants.ERR_CONSTANTE_NO_ENCONTRADA, errorMessage);
        }

        // Obtenemos la constante
        Constante constante = optionalConstante.get();

        // Devolvemos el valor de la constante
        return Integer.parseInt(constante.getValor());
    }

    /**
     * Verifica si el usuario puede enviar otro email y actualiza sus contadores.
     *
     * @param aplicacion Aplicación que intenta enviar un correo
     * @throws NotificationsServerException Si supera el máximo diario permitido
     */
    private void verificarEnviosEmail(Aplicacion aplicacion) throws NotificationsServerException
    {
        // Si la fecha de la última notificación no es de hoy, reiniciamos contador
        if (aplicacion.getFechaUltimaNotificacionEmail() == null ||
            !aplicacion.getFechaUltimaNotificacionEmail().toLocalDate().equals(java.time.LocalDate.now()))
        {
            // Reiniciamos el contador de emails diarios
            aplicacion.setNotifHoyEmail(0);
    
            // Guardamos los cambios en la base de datos
            this.aplicacionRepository.save(aplicacion);
        }

        // Comprobar límite diario
        if (aplicacion.getNotifHoyEmail() > aplicacion.getNotifMaxEmail())
        {
            String errorMessage = "La aplicación " + aplicacion.getNombre() + " ha superado el máximo de emails diarios permitidos.";
            log.warn(errorMessage);

            throw new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage);
        }
    }
}
