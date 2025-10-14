package es.iesjandula.reaktor.notifications_server.models;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notificacion_web")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NotificacionWeb 
{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id ;
	
	private LocalDate fechaCreacion ;
	
	private String texto ;
	
	private LocalTime horaInicio ;
	
	private LocalDate fechaFin ;
	
	private LocalTime horaFin ;
	
	private String roles ;
	
	private String nivel ;
	
	private LocalDate fechaInicio ;
	
	@ManyToOne
	@JoinColumn(name = "aplicacion_client_id", nullable = false)
	private Aplicacion aplicacion ;
	
}
