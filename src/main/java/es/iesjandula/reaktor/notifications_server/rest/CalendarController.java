package es.iesjandula.reaktor.notifications_server.rest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventAttendee;

import es.iesjandula.reaktor.notifications_server.dtos.GoogleCalendarDto;
import es.iesjandula.reaktor.notifications_server.dtos.DtoAplicacion;
import es.iesjandula.reaktor.notifications_server.models.Aplicacion;
import es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.aplicacion.NotificacionCalendarAplicacion;
import es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.aplicacion.NotificacionCalendarInvitadosAplicacion;
import es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.ids.NotificacionCalendarInvitadoId;
import es.iesjandula.reaktor.notifications_server.repository.IAplicacionRepository;
import es.iesjandula.reaktor.notifications_server.repository.IUsuarioRepository;
import es.iesjandula.reaktor.notifications_server.repository.INotificacionCalendarAplicacionRepository;
import es.iesjandula.reaktor.notifications_server.repository.INotificacionCalendarInvitadosAplicacionRepository;
import es.iesjandula.reaktor.notifications_server.utils.Constants;
import es.iesjandula.reaktor.notifications_server.utils.NotificationsServerException;

@RestController
@RequestMapping("/calendar")
public class CalendarController {

    /** Repositorios */
    @Autowired 
    private IAplicacionRepository aplicacionRepository;
    
    @Autowired 
    private IUsuarioRepository usuarioRepository;
    
    @Autowired 
    private INotificacionCalendarAplicacionRepository notificacionCalendarAplicacionRepository;
    
    @Autowired 
    private INotificacionCalendarInvitadosAplicacionRepository invitadosAplicacionRepository;
    
    @Autowired
    private Credential calendarCredentials;

    /** Constantes para Google Calendar */
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "Reaktor Notifications Calendar";

    // =========================================================================================================
    //                                           ENDPOINT PRINCIPAL
    // =========================================================================================================

    /**
     * Método - Crear un evento en Google Calendar
     *
     * Este método recibe los datos de un evento, registra la notificación en la base
     * de datos, genera los invitados y finalmente crea el evento en el Calendar del usuario.
     *
     * @param dto - Datos del evento a crear
     * @param aplicacionAutenticada - Aplicación autenticada que ejecuta la acción
     * @return ResponseEntity - Resultado del proceso
     * @throws NotificationsServerException - Error genérico controlado
     */
    @PostMapping("/crear")
    @PreAuthorize("hasRole('" + es.iesjandula.reaktor.base.utils.BaseConstants.ROLE_APLICACION_NOTIFICACIONES + "')")
    public ResponseEntity<?> crearEventoCalendar(
            @RequestBody GoogleCalendarDto dto,
            @AuthenticationPrincipal DtoAplicacion aplicacionAutenticada)  
            throws NotificationsServerException {
        try {
            // -------------------------------------------------------------------------------------------------
            // 1️⃣ Buscar o crear la aplicación emisora asociada a la notificación
            // -------------------------------------------------------------------------------------------------
            Aplicacion aplicacion = this.buscarOCrearAplicacion(dto.getDtoAplicacion());

            NotificacionCalendarAplicacion notificacion = new NotificacionCalendarAplicacion();
            notificacion.setAplicacion(aplicacion);
            notificacion.setTitulo(dto.getTitulo());
            notificacion.setFechaCreacion(LocalDateTime.now().toLocalDate());
            notificacion.setFechaInicio(Date.from(dto.getFechaInicio().atZone(java.time.ZoneId.systemDefault()).toInstant()));
            notificacion.setFechaFin(Date.from(dto.getFechaFin().atZone(java.time.ZoneId.systemDefault()).toInstant()));

            // Guardamos la notificación en BBDD
            notificacion = this.notificacionCalendarAplicacionRepository.save(notificacion);

            // -------------------------------------------------------------------------------------------------
            // 2️⃣ Construir el servicio de Google Calendar usando OAuth2
            // -------------------------------------------------------------------------------------------------
            Credential credential = this.obtenerCredenciales();
            Calendar calendarService = this.construirCalendarService(credential);

            // -------------------------------------------------------------------------------------------------
            // 3️⃣ Crear un objeto Event que representará al evento en Google Calendar
            // -------------------------------------------------------------------------------------------------
            Event event = new Event();
            event.setSummary(dto.getTitulo());
            event.setDescription(dto.getDescripcion());

            EventDateTime eventStart = new EventDateTime()
                    .setDateTime(convertirAEventDateTime(dto.getFechaInicio()))
                    .setTimeZone("Europe/Madrid");
            event.setStart(eventStart);

            EventDateTime eventEnd = new EventDateTime()
                    .setDateTime(convertirAEventDateTime(dto.getFechaFin()))
                    .setTimeZone("Europe/Madrid");
            event.setEnd(eventEnd);

            // -------------------------------------------------------------------------------------------------
            // 4️⃣ Procesar los invitados del evento
            // -------------------------------------------------------------------------------------------------
            List<EventAttendee> attendees = new ArrayList<>();
            for (String emailInvitado : dto.getInvitados()) {
                EventAttendee attendee = new EventAttendee().setEmail(emailInvitado);
                attendees.add(attendee);

                NotificacionCalendarInvitadoId id = new NotificacionCalendarInvitadoId();
                id.setNotificacionId(notificacion.getId());
                id.setUsuarioEmailInvitado(emailInvitado);

                NotificacionCalendarInvitadosAplicacion invitado = new NotificacionCalendarInvitadosAplicacion();
                invitado.setId(id);
                invitado.setNotificacionId(notificacion);

                this.invitadosAplicacionRepository.save(invitado);
            }
            event.setAttendees(attendees);

            // -------------------------------------------------------------------------------------------------
            // 5️⃣ Enviar el evento a Google Calendar
            // -------------------------------------------------------------------------------------------------
            event = calendarService.events().insert("primary", event).execute();

            return ResponseEntity.ok("Evento creado correctamente: " + event.getHtmlLink());

        } catch (Exception exception) {
        	String errorMessage = "Error creando evento en Calendar";
        	
        	NotificationsServerException notificationsServerException =
                    new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception);
        	
        	return ResponseEntity.status(500).body(notificationsServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Método - Buscar o crear la aplicación emisora de la notificación
     *
     * Este método garantiza que la aplicación emisora exista en la base de datos.
     * Si no existe, se crea automáticamente con valores por defecto.
     *
     * @param dtoAplicacion - Información de la aplicación emisora
     * @return Aplicacion - La aplicación existente o recién creada
     */
    private Aplicacion buscarOCrearAplicacion(DtoAplicacion dtoAplicacion) {
        Aplicacion aplicacion = this.aplicacionRepository.findByNombre(dtoAplicacion.getNombre());
        if (aplicacion == null) {
            aplicacion = new Aplicacion();
            aplicacion.setNombre(dtoAplicacion.getNombre());
            aplicacion.setNotifMaxCalendar(10);
            aplicacion.setNotifHoyCalendar(0);
            aplicacion = this.aplicacionRepository.save(aplicacion);
        }
        return aplicacion;
    }

    /**
     * Método - Convertir LocalDateTime a DateTime de Google Calendar
     *
     * @param ldt - Fecha/hora a convertir
     * @return DateTime - Objeto compatible con Google Calendar
     */
    private DateTime convertirAEventDateTime(LocalDateTime ldt) {
        return new DateTime(Date.from(ldt.atZone(java.time.ZoneId.systemDefault()).toInstant()));
    }

    /** 
     * Método - Obtener credenciales OAuth2 del usuario
     *
     * Este método devuelve las credenciales OAuth2 necesarias para utilizar
     * la API de Google Calendar. Las credenciales son inyectadas desde un 
     * bean de Spring configurado previamente (similar a gmailCredentials).
     *
     * @return Credential - Token válido
     * @throws Exception - Si ocurre algún problema obteniendo las credenciales
     */
    private Credential obtenerCredenciales() throws Exception 
    {
        try 
        {
            return this.calendarCredentials;
        }
        catch (Exception exception) 
        {
            String errorMessage = "Error al obtener las credenciales de Google Calendar";
            throw new NotificationsServerException(
                    Constants.ERR_GENERIC_EXCEPTION_CODE,
                    errorMessage,
                    exception
            );
        }
    }


    /**
     * Método - Construir servicio Google Calendar
     *
     * @param credential - Credenciales OAuth2 válidas
     * @return Calendar - Servicio listo para usar
     * @throws Exception - Error al construir el servicio
     */
    private Calendar construirCalendarService(Credential credential) throws Exception {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
