package es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.aplicacion;

import es.iesjandula.reaktor.notifications_server.models.Usuario;
import es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.ids.NotificacionCalendarInvitadoId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notificacion_calendar_invitados_aplicacion")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionCalendarInvitadosAplicacion 
{
	@EmbeddedId
	private NotificacionCalendarInvitadoId id ;
	
	@ManyToOne
    @MapsId("notificacionId")
    @JoinColumn(name = "notificacion_id")
	private NotificacionCalendarAplicacion notificacionId ;
	
	@ManyToOne
    @MapsId("usuarioEmailInvitado")
    @JoinColumn(name = "usuario_email_invitado")
    private Usuario usuarioEmailInvitado;	
}
