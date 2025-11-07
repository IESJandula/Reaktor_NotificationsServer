package es.iesjandula.reaktor.notifications_server.models;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "birthday")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Birthday 
{

	@Id
	private Long id ;
	
	private String nombre ;
	
	private String apellidos ;
	
	private LocalDate fechaNacimiento ;
	
	private boolean greetings ;
	
}
