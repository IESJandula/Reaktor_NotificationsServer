package es.iesjandula.reaktor.notifications_server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.iesjandula.reaktor.notifications_server.dtos.NotificacionesWebResponseDto;
import es.iesjandula.reaktor.notifications_server.models.notificacion_web.usuario.NotificacionWebUsuario;

public interface INotificacionWebUsuarioRepository extends JpaRepository<NotificacionWebUsuario, Long>
{
	/**
	 * Método para buscar todas las notificaciones de los usuarios
	 * @return List<NotificacionesWebResponseDto> con las notificaciones
	 */
	@Query(
	"""
		SELECT new es.iesjandula.reaktor.notifications_server.dtos.NotificacionesWebResponseDto(CONCAT(n.usuario.nombre, ' ', n.usuario.apellidos),
																								n.texto,
																								CONCAT(FUNCTION('DATE_FORMAT', n.fechaInicio, '%d/%m/%Y'), ' ', FUNCTION('TIME_FORMAT', n.horaInicio, '%H:%i')),
																								CONCAT(FUNCTION('DATE_FORMAT', n.fechaFin, '%d/%m/%Y'), ' ', FUNCTION('TIME_FORMAT', n.horaFin, '%H:%i')),
																								n.rol, n.tipo)
		FROM NotificacionWebUsuario n
	"""
	)
    List<NotificacionesWebResponseDto> buscarTodasLasNotificacionesUsuarios();
	
	/**
	 * Método para buscar las notificaciones vigentes de un usuario
	 * @param email Email del usuario
	 * @return List<NotificacionesWebResponseDto> con las notificaciones vigentes
	 */
	@Query(
	"""
		SELECT new es.iesjandula.reaktor.notifications_server.dtos.NotificacionesWebResponseDto(CONCAT(n.usuario.nombre, ' ', n.usuario.apellidos),
																								n.texto,
																								CONCAT(FUNCTION('DATE_FORMAT', n.fechaInicio, '%d/%m/%Y'), ' ', FUNCTION('TIME_FORMAT', n.horaInicio, '%H:%i')),
																								CONCAT(FUNCTION('DATE_FORMAT', n.fechaFin, '%d/%m/%Y'), ' ', FUNCTION('TIME_FORMAT', n.horaFin, '%H:%i')),
																								n.rol, n.tipo)
		FROM NotificacionWebUsuario n
		WHERE n.fechaInicio <= CURRENT_DATE AND n.fechaFin >= CURRENT_DATE AND n.usuario.email = :email
	"""
	)
	List<NotificacionesWebResponseDto> buscarNotificacionesUsuariosPorUsuario(String email);

	/**
	 * Método para buscar todas las notificaciones vigentes de los usuarios
	 * @param tipo Tipo de notificación
	 * @param roles Roles del usuario
	 * @return List<NotificacionesWebResponseDto> con las notificaciones vigentes
	 */
	@Query(
	"""
		SELECT new es.iesjandula.reaktor.notifications_server.dtos.NotificacionesWebResponseDto(CONCAT(n.usuario.nombre, ' ', n.usuario.apellidos),
																								n.texto,
																								CONCAT(FUNCTION('DATE_FORMAT', n.fechaInicio, '%d/%m/%Y'), ' ', FUNCTION('TIME_FORMAT', n.horaInicio, '%H:%i')),
																								CONCAT(FUNCTION('DATE_FORMAT', n.fechaFin, '%d/%m/%Y'), ' ', FUNCTION('TIME_FORMAT', n.horaFin, '%H:%i')),
																								n.rol, n.tipo)
		FROM NotificacionWebUsuario n
		WHERE n.tipo = :tipo AND n.fechaInicio <= CURRENT_DATE AND n.fechaFin >= CURRENT_DATE AND n.rol IN :roles
	"""
	)
    List<NotificacionesWebResponseDto> buscarTodasLasNotificacionesUsuariosVigentesPorTipo(String tipo, List<String> roles);
}
