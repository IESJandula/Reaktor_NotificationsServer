package es.iesjandula.reaktor.notifications_server.models.notificacion_emails.usuario;

import java.util.List;

import es.iesjandula.reaktor.notifications_server.models.Usuario;
import es.iesjandula.reaktor.notifications_server.models.notificacion_emails.NotificacionEmail;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notificacion_email_usuario")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class NotificacionEmailUsuario extends NotificacionEmail
{	
	@ManyToOne
	private Usuario usuario;
	
	@OneToMany(mappedBy = "notificacionEmailUsuario")
    private List<NotificacionEmailParaUsuario> paraUsuariosUsuario;

	@OneToMany(mappedBy = "notificacionEmailUsuario")
    private List<NotificacionEmailCopiaUsuario> copiaUsuariosUsuario;

    @OneToMany(mappedBy = "notificacionEmailUsuario")
    private List<NotificacionEmailCopiaOcultaUsuario> copiaOcultaUsuariosUsuario;
}
