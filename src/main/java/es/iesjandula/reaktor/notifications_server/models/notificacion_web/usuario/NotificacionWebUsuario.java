package es.iesjandula.reaktor.notifications_server.models.notificacion_web.usuario;

import es.iesjandula.reaktor.notifications_server.models.Usuario;
import es.iesjandula.reaktor.notifications_server.models.notificacion_web.NotificacionWeb;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
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
	@ManyToOne
	private Usuario usuario ;
}
