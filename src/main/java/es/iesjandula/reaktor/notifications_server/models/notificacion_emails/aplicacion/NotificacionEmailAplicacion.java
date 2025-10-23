package es.iesjandula.reaktor.notifications_server.models.notificacion_emails.aplicacion;

import java.util.List;
import java.util.Objects;

import es.iesjandula.reaktor.notifications_server.models.Aplicacion;
import es.iesjandula.reaktor.notifications_server.models.notificacion_emails.NotificacionEmail;
import es.iesjandula.reaktor.notifications_server.models.ids.NotificacionAplicacionId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
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
public class NotificacionEmailAplicacion
{
	@EmbeddedId
	private NotificacionAplicacionId id ;
	
	@ManyToOne
    @MapsId("aplicacionNombre")
    @JoinColumn(name = "aplicacion_nombre")
	private Aplicacion aplicacionNombre;
	
	@OneToMany(mappedBy = "notificacionId")
    private List<NotificacionEmailParaAplicacion> paraUsuariosAplicacion;

    @OneToMany(mappedBy = "notificacionId")
    private List<NotificacionEmailCopiaAplicacion> copiaUsuariosAplicacion;

    @OneToMany(mappedBy = "notificacionId")
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
