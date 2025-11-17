package es.iesjandula.reaktor.notifications_server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.notifications_server.dtos.DtoConstante;
import es.iesjandula.reaktor.notifications_server.models.Constante;

@Repository
public interface IConstanteRepository extends JpaRepository<Constante, String>
{

	/**
	 * Busca una constante según su clave.
	 * 
	 * @param clave la clave de la constante que quieres encontrar
	 * @return un Optional con la constante si existe
	 */
	Optional<Constante> findByClave(String clave);
	
	/**
     * Obtiene todas las constantes como lista de objetos DTO.
     * 
     * @return lista de constantes en formato DtoConstantesNotificaciones
     */
    @Query("SELECT new es.iesjandula.reaktor.notifications_server.dtos.DtoConstante(c.clave, c.valor) FROM Constante c")
    List<DtoConstante> encontrarTodoComoDto();
}
