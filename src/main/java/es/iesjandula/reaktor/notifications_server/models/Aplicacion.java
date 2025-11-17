package es.iesjandula.reaktor.notifications_server.models;

import java.util.List;

import es.iesjandula.reaktor.notifications_server.models.notificacion_calendar.aplicacion.NotificacionCalendarAplicacion;
import es.iesjandula.reaktor.notifications_server.models.notificacion_emails.NotificacionEmailAplicacion;
import es.iesjandula.reaktor.notifications_server.models.notificacion_web.aplicacion.NotificacionWebAplicacion;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad para las aplicaciones/servicios clientes (como PrintersClient)
 * que interactúan con FirebaseServer.
 */
@Entity
@Table(name = "aplicacion")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Aplicacion extends Actor
{
    /** Atributo - Nombre descriptivo de la aplicación */
    @Id
    private String nombre ;
    
    /** Atributo - Notificaciones de calendario */
    @OneToMany(mappedBy = "aplicacion")
    private List<NotificacionCalendarAplicacion> notificacionesCalendarsAplicacion ;
    
    /** Atributo - Notificaciones de email */
    @OneToMany(mappedBy = "aplicacion")
    private List<NotificacionEmailAplicacion> notificacionesEmailsAplicacion ;
    
    /** Atributo - Notificaciones de web */
    @OneToMany(mappedBy = "aplicacion")
    private List<NotificacionWebAplicacion> notificacionesWebsAplicacion ;

    /**
     * @return true si las aplicaciones son iguales, false en caso contrario
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof Aplicacion))
        {
            return false;
        }

        return this.nombre.equals(((Aplicacion) obj).nombre);
    }

    /**
     * @return hashcode de la aplicación
     */
    @Override
    public int hashCode()
    {
        return this.nombre.hashCode();
    }
}
