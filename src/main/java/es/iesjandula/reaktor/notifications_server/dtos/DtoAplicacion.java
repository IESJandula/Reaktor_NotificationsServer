package es.iesjandula.reaktor.notifications_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de aplicación emisora para notificaciones
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtoAplicacion 
{

	/** Nombre de la aplicación (clave primaria en base de datos) */
    private String nombre;

    /** Roles asignados a la aplicación (opcional) */
    private String roles;
	
}
