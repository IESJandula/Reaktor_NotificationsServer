package es.iesjandula.reaktor.notifications_server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.iesjandula.reaktor.notifications_server.dtos.NotificacionesWebResponseDto;
import es.iesjandula.reaktor.notifications_server.models.notificacion_web.aplicacion.NotificacionWebAplicacion;

public interface INotificacionWebAplicacionRepository extends JpaRepository<NotificacionWebAplicacion, Long>
{
	/**
	 * Método para buscar todas las notificaciones de las aplicaciones
	 * @return List<NotificacionesWebResponseDto> con las notificaciones
	 */
	@Query(
	"""
		SELECT new es.iesjandula.reaktor.notifications_server.dtos.NotificacionesWebResponseDto(n.aplicacion.nombre,
																								n.texto,
																								CONCAT(FUNCTION('DATE_FORMAT', n.fechaInicio, '%d/%m/%Y'), ' ', FUNCTION('TIME_FORMAT', n.horaInicio, '%H:%i')),
																								CONCAT(FUNCTION('DATE_FORMAT', n.fechaFin, '%d/%m/%Y'), ' ', FUNCTION('TIME_FORMAT', n.horaFin, '%H:%i')),
																								n.rol, n.tipo)
		FROM NotificacionWebAplicacion n
	"""
	)
    List<NotificacionesWebResponseDto> buscarTodasLasNotificacionesAplicaciones();	

	/**
	 * Método para buscar todas las notificaciones vigentes de las aplicaciones
	 * @return List<NotificacionesWebResponseDto> con las notificaciones vigentes
	 */
	@Query(
	"""
		SELECT new es.iesjandula.reaktor.notifications_server.dtos.NotificacionesWebResponseDto(n.aplicacion.nombre,
																								n.texto,
																								CONCAT(FUNCTION('DATE_FORMAT', n.fechaInicio, '%d/%m/%Y'), ' ', FUNCTION('TIME_FORMAT', n.horaInicio, '%H:%i')),
																								CONCAT(FUNCTION('DATE_FORMAT', n.fechaFin, '%d/%m/%Y'), ' ', FUNCTION('TIME_FORMAT', n.horaFin, '%H:%i')), 
																								n.rol,
																								n.tipo)
		FROM NotificacionWebAplicacion n
		WHERE n.tipo = :tipo AND n.fechaInicio <= CURRENT_DATE AND n.fechaFin >= CURRENT_DATE AND n.rol IN :roles
	"""
	)
    List<NotificacionesWebResponseDto> buscarTodasLasNotificacionesAplicacionesVigentesPorTipo(String tipo, List<String> roles);
}
