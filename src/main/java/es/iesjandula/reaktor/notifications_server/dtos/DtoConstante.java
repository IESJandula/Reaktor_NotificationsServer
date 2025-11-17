package es.iesjandula.reaktor.notifications_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Objeto de Transferencia de Datos) utilizado para representar constantes
 * del sistema.

 * Este objeto se emplea en operaciones donde se requiere transferir información
 * sobre constantes entre diferentes capas de la aplicación, como por ejemplo
 * entre el backend y el frontend.
 * 
 * Cada constante está compuesta por una clave identificadora y su
 * correspondiente valor.
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DtoConstante 
{

	/** Clave identificadora de la constante */
	private String clave;

	/** Valor asociado a la constante */
	private String valor;
	
}
