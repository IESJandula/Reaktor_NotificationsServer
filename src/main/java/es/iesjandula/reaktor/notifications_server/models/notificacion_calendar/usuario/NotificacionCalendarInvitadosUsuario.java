package es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.usuario;

import es.iesjandula.reaktor.notifications_server.models.Usuario;
import es.iesjandula.reaktor.notifications_server.models.ids.NotificacionUsuarioId;
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
@Table(name = "notificacion_calendar_invitados_usuario")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionCalendarInvitadosUsuario 
{
	@EmbeddedId
	private NotificacionUsuarioId id ;
	
	@ManyToOne
    @MapsId("notificacionId")
    @JoinColumn(name = "notificacion_usuario_id")
	private NotificacionCalendarUsuario notificacion ;
	
	@ManyToOne
    @MapsId("usuarioEmail")
    @JoinColumn(name = "usuario_email")
    private Usuario usuario;
}
