package es.iesjandula.reaktor.notifications_server.models.notificacion_web.aplicacion;

import es.iesjandula.reaktor.notifications_server.models.Aplicacion;
import es.iesjandula.reaktor.notifications_server.models.notificacion_web.NotificacionWeb;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notificacion_web_aplicacion")
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class NotificacionWebAplicacion extends NotificacionWeb
{	
	@ManyToOne
	private Aplicacion aplicacion;
}
