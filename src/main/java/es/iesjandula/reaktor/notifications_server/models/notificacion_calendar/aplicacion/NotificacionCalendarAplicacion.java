package es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.aplicacion;

import es.iesjandula.reaktor.notifications_server.models.Aplicacion;
import es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.NotificacionCalendar;
import es.iesjandula.reaktor.notifications_server.models.ids.NotificacionAplicacionId;
import jakarta.persistence.Entity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.MapsId;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notificacion_calendar_aplicacion")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NotificacionCalendarAplicacion
{
	@EmbeddedId
	private NotificacionAplicacionId id ;

	@ManyToOne
    @MapsId("aplicacionNombre")
    @JoinColumn(name = "aplicacion_nombre")
	private Aplicacion aplicacionNombre;
	
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
