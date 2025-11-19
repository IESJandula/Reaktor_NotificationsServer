package es.iesjandula.reaktor.notifications_server.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Francisco Manuel Benítez Chico
 */
@Entity
@Table(name = "usuario")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Usuario extends Actor
{
	/** Atributo - Email */
	@Id
	private String email ;
	
	/** Atributo - Nombre */
	@Column
	private String nombre ;
	
	/** Atributo - Apellidos */
	@Column
	private String apellidos ;
}

