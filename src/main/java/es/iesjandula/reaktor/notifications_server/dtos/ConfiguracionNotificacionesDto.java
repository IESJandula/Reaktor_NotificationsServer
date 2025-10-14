package es.iesjandula.reaktor.notifications_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfiguracionNotificacionesDto 
{

	private String clientId;
    private String nombre;
    private int maxNotificacionesWeb;
    private int maxNotificacionesEmail;
    private int maxNotificacionesCalendar;
	
}
