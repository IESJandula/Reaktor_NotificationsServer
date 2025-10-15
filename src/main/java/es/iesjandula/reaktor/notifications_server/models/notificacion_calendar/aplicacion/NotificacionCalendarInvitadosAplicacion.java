package es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.aplicacion;

import es.iesjandula.reaktor.notifications_server.models.Aplicacion;
import es.iesjandula.reaktor.notifications_server.models.Usuario;
import es.iesjandula.reaktor.notifications_server.models.ids.NotificacionAplicacionId;
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
@Table(name = "notificacion_calendar_invitados_aplicacion")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionCalendarInvitadosAplicacion 
{
	@EmbeddedId
	private NotificacionAplicacionId id ;
	
	@ManyToOne
    @MapsId("notificacionId")
    @JoinColumn(name = "notificacion_aplicacion_id")
	private NotificacionCalendarAplicacion notificacion ;
	
	@ManyToOne
    @MapsId("aplicacionNombre")
    @JoinColumn(name = "aplicacion_nombre")
    private Aplicacion aplicacion;
	
}
