package es.iesjandula.reaktor.notifications_server.models;

import es.iesjandula.reaktor.notifications_server.models.id.NotificacionCalendarInvitadoId;
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
@Table(name = "notificacion_calendar_invitado")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionCalendarInvitado 
{

	@EmbeddedId
	private NotificacionCalendarInvitadoId id ;
	
	@ManyToOne
    @MapsId("notificacionId")
    @JoinColumn(name = "notificacion_id")
	private NotificacionCalendar notificacion ;
	
	@ManyToOne
    @MapsId("usuarioEmail")
    @JoinColumn(name = "usuario_email")
    private Usuario usuario;
	
}
