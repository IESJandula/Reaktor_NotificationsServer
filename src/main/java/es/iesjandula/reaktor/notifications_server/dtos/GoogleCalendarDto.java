package es.iesjandula.reaktor.notifications_server.dtos;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleCalendarDto 
{

	/** Título del evento */
    private String titulo;

    /** Descripción o contenido del evento */
    private String descripcion;

    /** Fecha y hora de inicio del evento */
    private LocalDateTime fechaInicio;

    /** Fecha y hora de fin del evento */
    private LocalDateTime fechaFin;

    /** Lista de emails de los invitados */
    private List<String> invitados;

    /** DTO de la aplicación emisora */
    private DtoAplicacion dtoAplicacion;
	
}
