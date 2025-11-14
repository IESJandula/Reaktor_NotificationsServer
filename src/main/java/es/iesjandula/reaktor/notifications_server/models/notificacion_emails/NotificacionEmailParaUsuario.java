package es.iesjandula.reaktor.notifications_server.models.notificacion_emails;

import es.iesjandula.reaktor.notifications_server.models.Usuario;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "notificacion_email_para_usuario")
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionEmailParaUsuario
{
	/** Atributo - Id */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id ;

	@ManyToOne
    private NotificacionEmailAplicacion notificacionEmailAplicacion;

	@ManyToOne
	private Usuario usuario;
}
