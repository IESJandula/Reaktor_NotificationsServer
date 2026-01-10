package es.iesjandula.reaktor.notifications_server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.iesjandula.reaktor.notifications_server.models.Aplicacion;
import es.iesjandula.reaktor.notifications_server.dtos.AplicacionDto;

/**
 * Repositorio para acceder a las aplicaciones/servicios clientes.
 */
public interface IAplicacionRepository extends JpaRepository<Aplicacion, String>
{
	Aplicacion findByNombre(String nombre) ;

	/**
	 * Busca todas las aplicaciones.
	 * 
	 * @param pageable La página de aplicaciones a buscar.
	 * @return La página de aplicaciones encontradas.
	 */
	@Query(value = 
		      """
	          SELECT new es.iesjandula.reaktor.notifications_server.dtos.AplicacionDto(
			         a.nombre, 
  					 CAST(FUNCTION('DATE_FORMAT', a.fechaUltimaNotificacionCalendar, '%d/%m/%Y %H:%i:%s') AS string) AS fechaUltimaNotificacionCalendar,
	                 a.notifHoyCalendar,
	                 a.notifMaxCalendar, 
	                 CAST(FUNCTION('DATE_FORMAT', a.fechaUltimaNotificacionEmail, '%d/%m/%Y %H:%i:%s') AS string) AS fechaUltimaNotificacionEmail,
	                 a.notifHoyEmail,
	                 a.notifMaxEmail, 
	                 CAST(FUNCTION('DATE_FORMAT', a.fechaUltimaNotificacionWeb, '%d/%m/%Y %H:%i:%s') AS string) AS fechaUltimaNotificacionWeb,
	                 a.notifHoyWeb,
	                 a.notifMaxWeb)
	          FROM Aplicacion a
	          ORDER BY a.nombre ASC
	          """)
    List<AplicacionDto> buscarTodasLasAplicaciones();	

	/**
	 * Actualiza las notificaciones máximas de una aplicación.
	 * 
	 * @param aplicacion La aplicación a actualizar.
	 * @param notificacionesMaximas Las notificaciones máximas a actualizar.
	 */
	@Modifying
	@Query(value = "UPDATE Aplicacion SET notifMaxCalendar = :notificacionesMaximas WHERE nombre = :aplicacion")
	void actualizarNotificacionesMaximasCalendar(@Param("aplicacion") String aplicacion, @Param("notificacionesMaximas") int notificacionesMaximas);

	/**
	 * Actualiza las notificaciones máximas de una aplicación.
	 * 
	 * @param aplicacion La aplicación a actualizar.
	 * @param notificacionesMaximas Las notificaciones máximas a actualizar.
	 */
	@Modifying
	@Query(value = "UPDATE Aplicacion SET notifMaxEmail = :notificacionesMaximas WHERE nombre = :aplicacion")
	void actualizarNotificacionesMaximasEmail(@Param("aplicacion") String aplicacion, @Param("notificacionesMaximas") int notificacionesMaximas);

	/**
	 * Actualiza las notificaciones máximas de una aplicación.
	 * 
	 * @param aplicacion La aplicación a actualizar.
	 * @param notificacionesMaximas Las notificaciones máximas a actualizar.
	 */
	@Modifying
	@Query(value = "UPDATE Aplicacion SET notifMaxWeb = :notificacionesMaximas WHERE nombre = :aplicacion")
	void actualizarNotificacionesMaximasWeb(@Param("aplicacion") String aplicacion, @Param("notificacionesMaximas") int notificacionesMaximas);
}

