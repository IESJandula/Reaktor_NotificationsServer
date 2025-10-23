package es.iesjandula.reaktor.notifications_server.models.notificacion_calendar;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.aplicacion.NotificacionCalendarInvitadosAplicacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notificacion_calendar")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NotificacionCalendar 
{
	/** Atributo - Id */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id ;

	@Column
	private LocalDate fechaCreacion ;
	
	@Column
	private String titulo ;
	
	@Column
	private Date fechaInicio ;
	
	@Column
	private Date fechaFin ;
	
	@OneToMany(mappedBy = "notificacionId")
	private List<NotificacionCalendarInvitadosAplicacion> invitadosAplicacion ;
}
