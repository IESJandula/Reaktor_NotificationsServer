package es.iesjandula.reaktor.notifications_server.models.notificacion_emails.usuario;

import java.util.List;

import es.iesjandula.reaktor.notifications_server.models.Usuario;
import es.iesjandula.reaktor.notifications_server.models.notificacion_emails.NotificacionEmail;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "notificacion_email_usuario")
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class NotificacionEmailUsuario extends NotificacionEmail
{	
    /* Atributo - Indicador de si el email ha sido enviado */
    @Column(name = "enviado")
    private Boolean enviado;

    /* Atributo - Usuario emisor */
	@ManyToOne
	private Usuario usuario;

    /** Constructor - Constructor por defecto
     *
     * @param enviado - Indicador de si el email ha sido enviado
     */
    public NotificacionEmailUsuario()
    {
        this.enviado = false;
    }
	
	@OneToMany(mappedBy = "notificacionEmailUsuario")
    private List<NotificacionEmailParaUsuario> paraUsuariosUsuario;

	@OneToMany(mappedBy = "notificacionEmailUsuario")
    private List<NotificacionEmailCopiaUsuario> copiaUsuariosUsuario;

    @OneToMany(mappedBy = "notificacionEmailUsuario")
    private List<NotificacionEmailCopiaOcultaUsuario> copiaOcultaUsuariosUsuario;
}
