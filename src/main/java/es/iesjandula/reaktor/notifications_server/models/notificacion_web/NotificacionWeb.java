package es.iesjandula.reaktor.notifications_server.models.notificacion_web;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.annotation.Id;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notificacion_web")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Inheritance(strategy = InheritanceType.JOINED)
public class NotificacionWeb 
{
	/** Atributo - Id */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id ;

	/** Atributo - Fecha de creación */
	@Column
	private LocalDate fechaCreacion ;
	
	/** Atributo - Texto */
	@Column
	private String texto ;

	/** Atributo - Fecha de inicio */
	@Column
	private LocalDate fechaInicio ;
	
	/** Atributo - Hora de inicio */
	@Column
	private LocalTime horaInicio ;
	
	/** Atributo - Fecha de fin */
	@Column
	private LocalDate fechaFin ;
	
	/** Atributo - Hora de fin */
	@Column
	private LocalTime horaFin ;
	
	/** Atributo - Roles */
	@Column
	private String roles ;
	
	/** Atributo - Nivel */
	@Column
	private String nivel ;

	/** Atributo - Activo */
	@Column
	private boolean activo ;
}
