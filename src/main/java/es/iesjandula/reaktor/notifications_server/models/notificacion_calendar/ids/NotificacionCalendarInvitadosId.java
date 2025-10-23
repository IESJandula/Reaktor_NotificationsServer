package es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.ids;

import es.iesjandula.reaktor.notifications_server.models.ids.NotificacionAplicacionId;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class NotificacionCalendarInvitadosId implements Serializable
{
    /**
     * Serialización de la clase para persistencia.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Identificador de la notificación de la aplicación.
     */
    private NotificacionAplicacionId notificacionAplicacionId ;

    /**
     * Email del usuario invitado a la notificación de la aplicación.
     */
    private String usuarioEmailInvitado ;
}
