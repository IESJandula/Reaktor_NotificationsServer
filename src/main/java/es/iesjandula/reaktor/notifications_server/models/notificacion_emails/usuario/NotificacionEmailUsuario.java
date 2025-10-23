package es.iesjandula.reaktor.notifications_server.models.notificacion_emails.usuario;

import java.util.List;
import java.util.Objects;

import es.iesjandula.reaktor.notifications_server.models.Usuario;
import es.iesjandula.reaktor.notifications_server.models.ids.NotificacionUsuarioId;
import jakarta.persistence.Entity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notificacion_email_usuario")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NotificacionEmailUsuario
{
	@EmbeddedId
	private NotificacionUsuarioId id ;
	
	@ManyToOne
	@MapsId("usuarioEmail")
    @JoinColumn(name = "usuario_email")
	private Usuario usuarioEmail;
	
	@OneToMany(mappedBy = "notificacionId")
    private List<NotificacionEmailParaUsuario> paraUsuariosUsuario;

	@OneToMany(mappedBy = "notificacionId")
    private List<NotificacionEmailCopiaUsuario> copiaUsuariosUsuario;

    @OneToMany(mappedBy = "notificacionId")
    private List<NotificacionEmailCopiaOcultaUsuario> copiaOcultaUsuariosUsuario;

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof NotificacionEmailUsuario))
		{
			return false;
		}

		NotificacionEmailUsuario other = (NotificacionEmailUsuario) obj;

		return Objects.equals(id, other.id);
	}	

	@Override
	public int hashCode()
	{
		return Objects.hash(id);
	}
}
