package es.iesjandula.reaktor.notifications_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.notifications_server.models.notificacion_emails.usuario.NotificacionEmailCopiaOcultaUsuario;

public interface INotificacionesEmailCopiaOcultaUsuarioRepository extends JpaRepository<NotificacionEmailCopiaOcultaUsuario, Long>
{

}
