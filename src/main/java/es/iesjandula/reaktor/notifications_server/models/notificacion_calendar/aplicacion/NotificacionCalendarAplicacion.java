package es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.aplicacion;

import java.util.List;

import es.iesjandula.reaktor.notifications_server.models.Aplicacion;
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
@Table(name = "notificacion_calendar_aplicacion")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NotificacionCalendarAplicacion extends NotificacionCalendar
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id ;

	@ManyToOne
	@JoinColumn(name = "aplicacion_nombre", nullable = false)
	private Aplicacion aplicacion ;
	
	@OneToMany(mappedBy = "notificacion")
	private List<NotificacionCalendarInvitadosAplicacion> invitadosAplicacion ;

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
		if (!(obj instanceof NotificacionCalendarAplicacion))
		{
			return false;
		}

		return this.id.equals(((NotificacionCalendarAplicacion) obj).id);
	}

	@Override
	public int hashCode()
	{
		return this.id.hashCode();
	}
}
