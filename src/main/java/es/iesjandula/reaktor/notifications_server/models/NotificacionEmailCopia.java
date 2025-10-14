package es.iesjandula.reaktor.notifications_server.models;

import es.iesjandula.reaktor.notifications_server.models.id.NotificacionEmailUsuarioId;
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
@Table(name = "notificacion_email_copia")
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionEmailCopia 
{

	@EmbeddedId
	private NotificacionEmailUsuarioId id ;
	
	@ManyToOne
    @MapsId("notificacionId")
    @JoinColumn(name = "notificacion_id")
    private NotificacionEmail notificacion;

    @ManyToOne
    @MapsId("usuarioEmail")
    @JoinColumn(name = "usuario_email")
    private Usuario usuario;
	
}
