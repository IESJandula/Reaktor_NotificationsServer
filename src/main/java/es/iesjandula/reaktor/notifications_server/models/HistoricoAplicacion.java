package es.iesjandula.reaktor.notifications_server.models;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "historico_aplicacion")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class HistoricoAplicacion 
{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id ;
	
	private String nombre ;
	
	private LocalDate fecha ;
	
	private int notificaciones ;
	
	private String tipo ;	
	
}
