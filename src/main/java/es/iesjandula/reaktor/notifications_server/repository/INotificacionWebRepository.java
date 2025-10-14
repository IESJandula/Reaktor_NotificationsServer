package es.iesjandula.reaktor.notifications_server.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.notifications_server.models.Aplicacion;
import es.iesjandula.reaktor.notifications_server.models.NotificacionWeb;

public interface INotificacionWebRepository extends JpaRepository<NotificacionWeb, Long>
{

	int countByAplicacionAndFechaCreacion(Aplicacion aplicacion, LocalDate fechaCreacion);
	
	List<NotificacionWeb> findByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(LocalDate fechaInicio, LocalDate fechaFin) ;
	
}
