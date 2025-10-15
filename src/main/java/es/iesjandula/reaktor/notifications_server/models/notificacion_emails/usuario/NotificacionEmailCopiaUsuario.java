package es.iesjandula.reaktor.notifications_server.models.notificacion_emails.usuario;

import es.iesjandula.reaktor.notifications_server.models.Usuario;
import es.iesjandula.reaktor.notifications_server.models.ids.NotificacionUsuarioId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "notificacion_email_copia_usuario")
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionEmailCopiaUsuario 
{
	@EmbeddedId
	private NotificacionUsuarioId id ;
	
	@ManyToOne
    @MapsId("notificacionId")
    @JoinColumn(name = "notificacion_id")
    private NotificacionEmailUsuario notificacionUsuario;

    @ManyToOne
    @MapsId("usuarioEmail")
    @JoinColumn(name = "usuario_email")
    private Usuario usuario;
}
