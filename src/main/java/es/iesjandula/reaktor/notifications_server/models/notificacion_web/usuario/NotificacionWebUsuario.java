package es.iesjandula.reaktor.notifications_server.models.notificacion_web.usuario;

import es.iesjandula.reaktor.notifications_server.models.Usuario;
import es.iesjandula.reaktor.notifications_server.models.notificacion_web.NotificacionWeb;
import es.iesjandula.reaktor.notifications_server.models.ids.NotificacionUsuarioId;
import jakarta.persistence.Entity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notificacion_web_usuario")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class NotificacionWebUsuario extends NotificacionWeb
{
	@EmbeddedId
	private NotificacionUsuarioId id ;

	@ManyToOne
    @MapsId("notificacionId")
    @JoinColumn(name = "notificacion_id")
	private NotificacionWeb notificacion ;
	
	@ManyToOne
    @MapsId("usuarioEmail")
    @JoinColumn(name = "usuario_email")
	private Usuario usuario ;
}
