package es.iesjandula.reaktor.notifications_server.models.notificacion_emails.aplicacion;

import es.iesjandula.reaktor.notifications_server.models.Aplicacion;
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
@Data
@Table(name = "notificacion_email_para_aplicacion")
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionEmailParaAplicacion
{
	@EmbeddedId
	private NotificacionAplicacionId id ;
	
	@ManyToOne
    @MapsId("notificacionId")
    @JoinColumn(name = "notificacion_id")
    private NotificacionEmailAplicacion notificacionId;

    @ManyToOne
    @MapsId("aplicacionNombre")
    @JoinColumn(name = "aplicacion_nombre")
    private Aplicacion aplicacionNombre;
}
