package es.iesjandula.reaktor.notifications_server.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
	
import es.iesjandula.reaktor.notifications_server.dtos.NotificacionesWebVigentesDto;
import es.iesjandula.reaktor.notifications_server.models.notificacion_web.usuario.NotificacionWebUsuario;

public interface INotificacionWebUsuarioRepository extends JpaRepository<NotificacionWebUsuario, Long>
{
	/**
	 * Método para contar las notificaciones vigentes de un usuario
	 * @param email Email del usuario
	 * @param fechaCreacion Fecha de creación
	 * @return int con el número de notificaciones vigentes
	 */
	int countByEmailAndFechaCreacion(String email, LocalDate fechaCreacion);
	
	/**
	 * Método para buscar todas las notificaciones vigentes de los usuarios
	 * @return List<NotificacionesWebVigentesDto> con las notificaciones vigentes
	 */
	@Query("SELECT new es.iesjandula.reaktor.notifications_server.dtos.NotificacionesWebVigentesDto(n.texto, n.fechaInicio, n.horaInicio, n.fechaFin, n.horaFin, n.roles, n.nivel) " +
		   "FROM NotificacionWebUsuario n " + 
		   "WHERE n.fechaInicio <= CURRENT_DATE AND n.fechaFin >= CURRENT_DATE")
    List<NotificacionesWebVigentesDto> buscarTodasLasNotificacionesUsuariosVigentes();
	
	/**
	 * Método para buscar las notificaciones vigentes de un usuario
	 * @param email Email del usuario
	 * @return List<NotificacionesWebVigentesDto> con las notificaciones vigentes
	 */
	@Query("SELECT new es.iesjandula.reaktor.notifications_server.dtos.NotificacionesWebVigentesDto(n.texto, n.fechaInicio, n.horaInicio, n.fechaFin, n.horaFin, n.roles, n.nivel) " +
		   "FROM NotificacionWebUsuario n " + 
		   "WHERE n.fechaInicio <= CURRENT_DATE AND n.fechaFin >= CURRENT_DATE AND n.usuario.email = :email")
	List<NotificacionesWebVigentesDto> buscarNotificacionesVigentesUsuariosPorUsuario(String email);
}
