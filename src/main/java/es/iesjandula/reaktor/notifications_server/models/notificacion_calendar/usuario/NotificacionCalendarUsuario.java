package es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.usuario;

import java.util.List;

import es.iesjandula.reaktor.notifications_server.models.Usuario;
import es.iesjandula.reaktor.notifications_server.models.ids.NotificacionUsuarioId;
import es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.NotificacionCalendar;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notificacion_calendar_usuario")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NotificacionCalendarUsuario
{
	@EmbeddedId
	private NotificacionUsuarioId id ;

	@ManyToOne
    @MapsId("usuarioEmail")
    @JoinColumn(name = "usuario_email")
	private Usuario usuarioEmail;

	@ManyToOne
    @MapsId("notificacionId")
    @JoinColumn(name = "notificacion_id")
	private NotificacionCalendar notificacionId;

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof NotificacionCalendarUsuario))
		{
			return false;
		}

		return this.id.equals(((NotificacionCalendarUsuario) obj).id);
	}

	@Override
	public int hashCode()
	{
		return this.id.hashCode();
	}
}
