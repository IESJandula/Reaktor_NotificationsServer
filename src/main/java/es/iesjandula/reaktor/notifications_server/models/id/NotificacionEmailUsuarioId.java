package es.iesjandula.reaktor.notifications_server.models.id;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionEmailUsuarioId implements Serializable
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Long notificacionId ;
	
	private String usuarioEmail ;
	
}
