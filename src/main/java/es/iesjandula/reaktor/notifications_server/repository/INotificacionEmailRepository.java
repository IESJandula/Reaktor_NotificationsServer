package es.iesjandula.reaktor.notifications_server.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.notifications_server.models.Aplicacion;
import es.iesjandula.reaktor.notifications_server.models.NotificacionEmail;

public interface INotificacionEmailRepository extends JpaRepository<NotificacionEmail, Long>
{

	int countByAplicacionAndFechaCreacion(Aplicacion aplicacion, LocalDate fechaCreacion);
	
}
