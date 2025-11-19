package es.iesjandula.reaktor.notifications_server.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.notifications_server.models.Usuario;

/**
 * @author Francisco Manuel Benítez Chico
 */
@Repository
public interface IUsuarioRepository extends JpaRepository<Usuario, String>
{
    /**
     * Busca un usuario por su email
     * @param email el email del usuario
     * @return el usuario encontrado
     */
    @Query("SELECT u FROM Usuario u WHERE u.email = :email")
    Optional<Usuario> findByEmail(@Param("email") String email);
}
