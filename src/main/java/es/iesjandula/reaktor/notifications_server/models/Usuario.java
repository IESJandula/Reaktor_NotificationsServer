package es.iesjandula.reaktor.notifications_server.models;

import java.sql.Date;
import java.util.Arrays;
import java.util.List;

import es.iesjandula.reaktor.notifications_server.utils.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Francisco Manuel Benítez Chico
 */
@Entity
@Table(name = "usuario")
@NoArgsConstructor
@AllArgsConstructor
@Data
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
	
	/** Atributo - Departamento */
	@Column
	private String departamento ;
	
	/** Atributo - Fecha de nacimiento */
	@Column
	private Date fechaNacimiento ;
}

