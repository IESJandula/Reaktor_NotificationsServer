package es.iesjandula.reaktor.notifications_server.models.notificacion_emails.usuario;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "notificacion_email_copia_oculta_usuario")
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionEmailCopiaOcultaUsuario 
{
    @ManyToOne
    private NotificacionEmailUsuario notificacionEmailUsuario;
}
