package es.iesjandula.reaktor.notifications_server.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.notifications_server.models.Aplicacion;
import es.iesjandula.reaktor.notifications_server.models.NotificacionCalendar;

public interface INotificacionCalendarRepository extends JpaRepository<NotificacionCalendar, Long>
{

	int countByAplicacionAndFechaCreacion(Aplicacion aplicacion, LocalDate fechaCreacion);

}
