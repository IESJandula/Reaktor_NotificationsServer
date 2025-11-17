package es.iesjandula.reaktor.notifications_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;   
import org.springframework.stereotype.Repository;
import es.iesjandula.reaktor.notifications_server.models.Santoral;
import es.iesjandula.reaktor.notifications_server.models.ids.SantoralId;

@Repository
public interface ISantoralRepository extends JpaRepository<Santoral, SantoralId>
{

}
