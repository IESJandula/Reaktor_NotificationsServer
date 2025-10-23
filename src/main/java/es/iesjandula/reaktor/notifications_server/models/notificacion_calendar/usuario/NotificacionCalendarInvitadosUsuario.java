package es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.usuario;

import es.iesjandula.reaktor.notifications_server.models.Usuario;
import es.iesjandula.reaktor.notifications_server.models.ids.NotificacionUsuarioId;
import es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.aplicacion.NotificacionCalendarAplicacion;
import es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.ids.NotificacionCalendarInvitadosId;
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
	private NotificacionCalendarInvitadosId id ;
	
	@ManyToOne
    @MapsId("notificacionAplicacionId")
    @JoinColumn(name = "notificacion_aplicacion_id")
	private NotificacionCalendarAplicacion notificacionAplicacion ;
	
	@ManyToOne
    @MapsId("usuarioEmailInvitado")
    @JoinColumn(name = "usuario_email_invitado")
    private Usuario usuarioEmailInvitado;
}
