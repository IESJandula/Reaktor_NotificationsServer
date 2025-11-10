package es.iesjandula.reaktor.notifications_server.dtos;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionesWebResponseDto 
{
	/** Atributo - Identificador de la notificación */
	private Long id ;

	/** Atributo - Creador de la notificación */
	private String creador ;

	/** Atributo - Texto */
	private String texto ;

	/** Atributo - Fecha y hora de inicio */
	private String fechaHoraInicio ;

	/** Atributo - Fecha y hora de fin */
	private String fechaHoraFin ;
	
	/** Atributo - Receptor */
	private String receptor ;
	
	/** Atributo - Tipo */
	private String tipo ;
}
