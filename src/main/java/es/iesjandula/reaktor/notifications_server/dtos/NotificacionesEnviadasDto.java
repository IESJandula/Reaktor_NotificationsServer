package es.iesjandula.reaktor.notifications_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionesEnviadasDto 
{

	private String client ;
	private String nombre ;
	
	private int calendarEnviadasHoy;
    private int emailEnviadasHoy;
    private int webEnviadasHoy;

    private int maxPorDiaCalendar;
    private int maxPorDiaEmail;
    private int maxPorDiaWeb;
	
}
