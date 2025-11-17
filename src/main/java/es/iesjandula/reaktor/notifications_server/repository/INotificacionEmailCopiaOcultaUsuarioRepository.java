package es.iesjandula.reaktor.notifications_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.notifications_server.models.notificacion_emails.NotificacionEmailCopiaOcultaUsuario;

public interface INotificacionEmailCopiaOcultaUsuarioRepository extends JpaRepository<NotificacionEmailCopiaOcultaUsuario, Long>
{

}
