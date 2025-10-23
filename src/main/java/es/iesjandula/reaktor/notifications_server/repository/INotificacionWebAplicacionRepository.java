package es.iesjandula.reaktor.notifications_server.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.iesjandula.reaktor.notifications_server.dtos.NotificacionesWebVigentesDto;
import es.iesjandula.reaktor.notifications_server.models.notificacion_web.aplicacion.NotificacionWebAplicacion;
import es.iesjandula.reaktor.notifications_server.models.ids.NotificacionAplicacionId;

public interface INotificacionWebAplicacionRepository extends JpaRepository<NotificacionWebAplicacion, NotificacionAplicacionId>
{
	/**
	 * Método para contar las notificaciones vigentes de una aplicación
	 * @param nombre Nombre de la aplicación
	 * @param fechaCreacion Fecha de creación
	 * @return int con el número de notificaciones vigentes
	 */
	int countByNombreAndFechaCreacion(String nombre, LocalDate fechaCreacion);

	/**
	 * Método para buscar todas las notificaciones vigentes de las aplicaciones
	 * @return List<NotificacionesWebVigentesDto> con las notificaciones vigentes
	 */
	@Query("SELECT new es.iesjandula.reaktor.notifications_server.dtos.NotificacionesWebVigentesDto(n.texto, n.fechaInicio, n.horaInicio, n.fechaFin, n.horaFin, n.roles, n.nivel) " +
		   "FROM NotificacionWebAplicacion n " + 
		   "WHERE n.fechaInicio <= CURRENT_DATE AND n.fechaFin >= CURRENT_DATE")
    List<NotificacionesWebVigentesDto> buscarTodasLasNotificacionesAplicacionesVigentes();	
}
