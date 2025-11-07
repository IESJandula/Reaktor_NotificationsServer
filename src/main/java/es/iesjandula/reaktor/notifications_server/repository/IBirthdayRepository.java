package es.iesjandula.reaktor.notifications_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.notifications_server.models.Birthday;

public interface IBirthdayRepository extends JpaRepository<Birthday, Long>
{

}
