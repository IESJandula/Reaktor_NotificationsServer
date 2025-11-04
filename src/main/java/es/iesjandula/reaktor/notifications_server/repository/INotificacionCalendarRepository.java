package es.iesjandula.reaktor.notifications_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.NotificacionCalendar;

public interface INotificacionCalendarRepository extends JpaRepository<NotificacionCalendar, Long>
{

}
