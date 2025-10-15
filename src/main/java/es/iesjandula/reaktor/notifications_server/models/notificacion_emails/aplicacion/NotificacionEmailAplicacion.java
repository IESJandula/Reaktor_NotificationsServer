package es.iesjandula.reaktor.notifications_server.models.notificacion_emails.aplicacion;

import java.util.List;
import java.util.Objects;

import es.iesjandula.reaktor.notifications_server.models.Aplicacion;
import es.iesjandula.reaktor.notifications_server.models.notificacion_emails.NotificacionEmail;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notificacion_email_aplicacion")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NotificacionEmailAplicacion extends NotificacionEmail
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id ;
	
	@ManyToOne
	@JoinColumn(name = "aplicacion_client_id", nullable = false)
	private Aplicacion aplicacion ;
	
	@OneToMany(mappedBy = "notificacion")
    private List<NotificacionEmailParaAplicacion> paraUsuariosAplicacion;

    @OneToMany(mappedBy = "notificacion")
    private List<NotificacionEmailCopiaAplicacion> copiaUsuariosAplicacion;

    @OneToMany(mappedBy = "notificacion")
    private List<NotificacionEmailCopiaOcultaAplicacion> copiaOcultaUsuariosAplicacion;

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
		if (!(obj instanceof NotificacionEmailAplicacion))
		{
			return false;
		}

		NotificacionEmailAplicacion other = (NotificacionEmailAplicacion) obj;

		return Objects.equals(id, other.id);
	}	

	@Override
	public int hashCode()
	{
		return Objects.hash(id);
	}
}
