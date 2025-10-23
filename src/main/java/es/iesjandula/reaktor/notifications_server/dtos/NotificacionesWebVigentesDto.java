package es.iesjandula.reaktor.notifications_server.dtos;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotificacionesWebVigentesDto 
{
	/** Atributo - Texto */
	private String texto ;

	/** Atributo - Fecha de inicio */
	private LocalDate fechaInicio ;
	
	/** Atributo - Hora de inicio */
	private LocalTime horaInicio ;
	
	/** Atributo - Fecha de fin */
	private LocalDate fechaFin ;
	
	/** Atributo - Hora de fin */
	private LocalTime horaFin ;
	
	/** Atributo - Roles */
	private String roles ;
	
	/** Atributo - Nivel */
	private String nivel ;
}
