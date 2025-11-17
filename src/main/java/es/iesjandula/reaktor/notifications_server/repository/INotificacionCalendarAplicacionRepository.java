package es.iesjandula.reaktor.notifications_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.aplicacion.NotificacionCalendarAplicacion;

public interface INotificacionCalendarAplicacionRepository extends JpaRepository<NotificacionCalendarAplicacion, Long>
{

}
