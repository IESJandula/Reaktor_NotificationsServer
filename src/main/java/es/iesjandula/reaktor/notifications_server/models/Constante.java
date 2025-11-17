package es.iesjandula.reaktor.notifications_server.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "constante")
public class Constante 
{
	/** Clave única que identifica la constante */
	@Id
	private String clave ; 
	
	/** Valor asociado a la constante */
	@Column
	private String valor ;
	
}
