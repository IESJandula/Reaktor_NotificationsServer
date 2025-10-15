package es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.usuario;

import java.util.List;

import es.iesjandula.reaktor.notifications_server.models.Usuario;
import es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.NotificacionCalendar;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class NotificacionCalendarUsuario extends NotificacionCalendar
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id ;

	@ManyToOne
	@JoinColumn(name = "usuario_email", nullable = false)
	private Usuario usuario ;
	
	@OneToMany(mappedBy = "notificacion")
	private List<NotificacionCalendarInvitadosUsuario> invitadosUsuarioUsuario ;

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
