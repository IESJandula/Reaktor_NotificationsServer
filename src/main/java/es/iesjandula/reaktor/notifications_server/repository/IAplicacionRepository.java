package es.iesjandula.reaktor.notifications_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.notifications_server.models.Aplicacion;

/**
 * Repositorio para acceder a las aplicaciones/servicios clientes.
 */
public interface IAplicacionRepository extends JpaRepository<Aplicacion, String>
{

	Aplicacion findByClientIdAndNombre(String clientId, String nombre) ;
	
}

