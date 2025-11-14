package es.iesjandula.reaktor.notifications_server.models.notificacion_emails;

import java.util.List;

import es.iesjandula.reaktor.notifications_server.models.Aplicacion;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notificacion_email_aplicacion")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class NotificacionEmailAplicacion extends NotificacionEmail
{
	@ManyToOne
	private Aplicacion aplicacion;
	
	@OneToMany(mappedBy = "notificacionEmailAplicacion")
    private List<NotificacionEmailParaUsuario> paraUsuariosUsuario;

	@OneToMany(mappedBy = "notificacionEmailAplicacion")
    private List<NotificacionEmailCopiaUsuario> copiaUsuariosUsuario;

    @OneToMany(mappedBy = "notificacionEmailAplicacion")
    private List<NotificacionEmailCopiaOcultaUsuario> copiaOcultaUsuariosUsuario;
}
