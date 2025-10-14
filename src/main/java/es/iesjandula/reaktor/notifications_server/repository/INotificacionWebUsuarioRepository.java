package es.iesjandula.reaktor.notifications_server.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.notifications_server.models.NotificacionWebUsuario;

public interface INotificacionWebUsuarioRepository extends JpaRepository<NotificacionWebUsuario, Long>
{

	int countByEmailAndFechaCreacion(String email, LocalDate fechaCreacion);
	
	List<NotificacionWebUsuario> findByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(LocalDate fechaInicio, LocalDate fechaFin) ;
	
}
