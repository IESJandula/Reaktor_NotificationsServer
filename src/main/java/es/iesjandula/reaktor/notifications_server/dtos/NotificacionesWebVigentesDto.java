package es.iesjandula.reaktor.notifications_server.dtos;

import java.time.LocalTime;
import java.util.Date;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionesWebVigentesDto 
{
	/** Atributo - Texto */
	private String texto ;

	/** Atributo - Fecha de inicio */
	private Date fechaInicio ;
	
	/** Atributo - Hora de inicio */
	private LocalTime horaInicio ;
	
	/** Atributo - Fecha de fin */
	private Date fechaFin ;
	
	/** Atributo - Hora de fin */
	private LocalTime horaFin ;
	
	/** Atributo - Roles */
	private String roles ;
	
	/** Atributo - Nivel */
	private String nivel ;
}
