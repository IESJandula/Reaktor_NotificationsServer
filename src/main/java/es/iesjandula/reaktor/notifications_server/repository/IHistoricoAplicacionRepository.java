package es.iesjandula.reaktor.notifications_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.notifications_server.models.HistoricoAplicacion;

public interface IHistoricoAplicacionRepository extends JpaRepository<HistoricoAplicacion, Long> {

}
