package es.iesjandula.reaktor.notifications_server.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.notifications_server.models.NotificacionWebAplicacion;

public interface INotificacionWebAplicacionRepository extends JpaRepository<NotificacionWebAplicacion, Long>
{

	int countByNombreAndFechaCreacion(String nombre, LocalDate fechaCreacion);
	
	List<NotificacionWebAplicacion> findByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(LocalDate fechaInicio, LocalDate fechaFin) ;
	
}
