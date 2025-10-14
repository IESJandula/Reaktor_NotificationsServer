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
public class Usuario
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
	
	/** Atributo - Lista de roles */
	@Column
	private String roles ;

	/** Atributo - Departamento */
	@Column
	private String departamento ;
	
	private Date fechaNacimiento ;
	
    /**
     * @return lista de roles deserializada
     */
    public List<String> getRolesList()
    {
        return Arrays.asList(this.roles.split(Constants.STRING_COMA)) ;
    }

    /**
     * Setter para establecer los roles desde una lista
     * 
     * @param rolesList lista de roles
     */
    public void setRolesList(List<String> rolesList)
    {
    	this.roles = null ;
    	
        if (rolesList != null && !rolesList.isEmpty())
        {

	        StringBuilder rolesStringBuilder = new StringBuilder();
	
	        for (int i = 0 ; i < rolesList.size() ; i++)
	        {
	        	// Añadimos el role 
	            rolesStringBuilder.append(rolesList.get(i)) ;
	            
	            // Si no es el último, añadimos una coma
	            if (i < rolesList.size() - 1)
	            {
	                rolesStringBuilder.append(Constants.STRING_COMA) ;
	            }
	        }
	
	        // Convierte el StringBuilder a cadena
	        this.roles = rolesStringBuilder.toString() ;
        }
    }
}

