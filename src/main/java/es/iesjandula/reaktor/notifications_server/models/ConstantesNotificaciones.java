package es.iesjandula.reaktor.notifications_server.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "constantes_notificaciones")
public class ConstantesNotificaciones 
{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id ;
	
	@Column
	private String clave ; 
	
}
