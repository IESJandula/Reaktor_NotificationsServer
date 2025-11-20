package es.iesjandula.reaktor.notifications_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;   
import org.springframework.stereotype.Repository;
import es.iesjandula.reaktor.notifications_server.models.DiaMundial;
import es.iesjandula.reaktor.notifications_server.models.ids.DiaMundialId;

@Repository
public interface IDiaMundialRepository extends JpaRepository<DiaMundial, DiaMundialId>
{

}
