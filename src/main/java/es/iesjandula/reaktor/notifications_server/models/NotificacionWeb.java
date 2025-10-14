package es.iesjandula.reaktor.notifications_server.models;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notificacion_web")
@Inheritance(strategy = InheritanceType.JOINED)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificacionWeb 
{
	/** Atributo - Fecha de creación */
	private LocalDate fechaCreacion ;
	
	/** Atributo - Texto */
	private String texto ;
	
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
	
	/** Atributo - Fecha de inicio */
	private LocalDate fechaInicio ;
}
