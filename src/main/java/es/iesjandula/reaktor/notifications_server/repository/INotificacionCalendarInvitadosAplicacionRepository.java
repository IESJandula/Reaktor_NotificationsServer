package es.iesjandula.reaktor.notifications_server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.aplicacion.NotificacionCalendarInvitadosAplicacion;
import es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.ids.NotificacionCalendarInvitadoId;

public interface INotificacionCalendarInvitadosAplicacionRepository extends JpaRepository<NotificacionCalendarInvitadosAplicacion, NotificacionCalendarInvitadoId>
{

	/**
     * Buscar todos los invitados asociados a una notificación Calendar
     *
     * @param notificacionId Id de la notificación
     * @return Lista de invitados
     */
    List<NotificacionCalendarInvitadosAplicacion> findByNotificacionId_Id(Long notificacionId);
	
}
