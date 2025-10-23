package es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.usuario;

import es.iesjandula.reaktor.notifications_server.models.Usuario;
import es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.NotificacionCalendar;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notificacion_calendar_usuario")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class NotificacionCalendarUsuario extends NotificacionCalendar
{
	@ManyToOne
	private Usuario usuario;
}
