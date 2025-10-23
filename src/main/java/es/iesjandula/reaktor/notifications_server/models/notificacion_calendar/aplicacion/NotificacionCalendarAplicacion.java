package es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.aplicacion;

import es.iesjandula.reaktor.notifications_server.models.Aplicacion;
import es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.NotificacionCalendar;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notificacion_calendar_aplicacion")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class NotificacionCalendarAplicacion extends NotificacionCalendar
{
	@ManyToOne
	private Aplicacion aplicacion;
}
