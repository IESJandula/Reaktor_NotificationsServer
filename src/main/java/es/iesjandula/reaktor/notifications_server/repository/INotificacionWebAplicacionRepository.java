package es.iesjandula.reaktor.notifications_server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.iesjandula.reaktor.notifications_server.dtos.NotificacionesWebVigentesDto;
import es.iesjandula.reaktor.notifications_server.models.notificacion_web.aplicacion.NotificacionWebAplicacion;

public interface INotificacionWebAplicacionRepository extends JpaRepository<NotificacionWebAplicacion, Long>
{
	/**
	 * Método para buscar todas las notificaciones vigentes de las aplicaciones
	 * @return List<NotificacionesWebVigentesDto> con las notificaciones vigentes
	 */
	@Query("SELECT new es.iesjandula.reaktor.notifications_server.dtos.NotificacionesWebVigentesDto(n.texto, n.fechaInicio, n.horaInicio, n.fechaFin, n.horaFin, n.roles, n.nivel) " +
		   "FROM NotificacionWebAplicacion n " + 
		   "WHERE n.fechaInicio <= CURRENT_DATE AND n.fechaFin >= CURRENT_DATE")
    List<NotificacionesWebVigentesDto> buscarTodasLasNotificacionesAplicacionesVigentes();	
}
