package es.iesjandula.reaktor.notifications_server.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id ;

	@Column(name = "nombre", nullable = false)
	private String nombre ;
	
	@ManyToOne
	@JoinColumn(name = "aplicacion_nombre", nullable = false)
	private Aplicacion aplicacion ;	
}
