package es.iesjandula.reaktor.notifications_server.models.notificacion_web;

import java.time.LocalTime;
import java.util.Date;

import jakarta.persistence.Id;
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
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class NotificacionWeb 
{
	/** Atributo - Id */
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private Long id ;
	
	/** Atributo - Texto */
	@Column
	private String texto ;

	/** Atributo - Fecha de creación */
	@Column
	private Date fechaCreacion ;

	/** Atributo - Fecha de inicio */
	@Column
	private Date fechaInicio ;
	
	/** Atributo - Hora de inicio */
	@Column
	private LocalTime horaInicio ;
	
	/** Atributo - Fecha de fin */
	@Column
	private Date fechaFin ;
	
	/** Atributo - Hora de fin */
	@Column
	private LocalTime horaFin ;
	
	/** Atributo - Receptor */
	@Column
	private String receptor ;
	
	/** Atributo - Tipo */
	@Column
	private String tipo ;
}
