package es.iesjandula.reaktor.notifications_server.models.notificacion_web.aplicacion;

import es.iesjandula.reaktor.notifications_server.models.Aplicacion;
import es.iesjandula.reaktor.notifications_server.models.ids.NotificacionAplicacionId;
import es.iesjandula.reaktor.notifications_server.models.notificacion_web.NotificacionWeb;
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
@Table(name = "notificacion_web_aplicacion")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificacionWebAplicacion
{
	@EmbeddedId
	private NotificacionAplicacionId id ;

	@ManyToOne
    @MapsId("notificacionId")
    @JoinColumn(name = "notificacion_id")
	private NotificacionWeb notificacionId ;
	
	@ManyToOne
    @MapsId("aplicacionNombre")
    @JoinColumn(name = "aplicacion_nombre")
	private Aplicacion aplicacionNombre;	
}
