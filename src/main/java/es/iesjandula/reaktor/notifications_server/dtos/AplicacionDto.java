package es.iesjandula.reaktor.notifications_server.dtos;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AplicacionDto implements Serializable
{
    /** Atributo - Nombre de la aplicación */
    private String nombre;
    
    /** Atributo - Fecha de la última notificación de calendario */
    private String fechaUltimaNotificacionCalendar;
    
    /** Atributo - Notificaciones de hoy de calendario */
    private Integer notifHoyCalendar;
    
    /** Atributo - Notificaciones máximas de calendario */
    private Integer notifMaxCalendar;

    /** Atributo - Fecha de la última notificación de email */
    private String fechaUltimaNotificacionEmail;
    
    /** Atributo - Notificaciones de hoy de email */
    private Integer notifHoyEmail;
    
    /** Atributo - Notificaciones máximas de email */
    private Integer notifMaxEmail;
    
    /** Atributo - Fecha de la última notificación de web */
    private String fechaUltimaNotificacionWeb;
    
    /** Atributo - Notificaciones de hoy de web */
    private Integer notifHoyWeb;
    
    /** Atributo - Notificaciones máximas de web */
    private Integer notifMaxWeb;
}
