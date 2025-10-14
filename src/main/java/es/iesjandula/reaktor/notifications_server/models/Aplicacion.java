package es.iesjandula.reaktor.notifications_server.models;

import java.util.Arrays;
import java.util.List;

import es.iesjandula.reaktor.notifications_server.utils.Constants;
import jakarta.persistence.Column;
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
public class Aplicacion
{
    /** Atributo - Nombre descriptivo de la aplicación */
    @Id
    private String nombre ;

    /** Atributo - Roles asociados a la aplicación */
    @Column
    private String roles ;
    
    private int notifHoyCalendar ;
    
    private int notifMaxCalendar ;
    
    private int notifHoyEmail ;
    
    private int notifMaxEmail ;
    
    private int notifHoyWeb ;
    
    private int notifMaxWeb ;
    
    @OneToMany(mappedBy = "aplicacion")
    private List<NotificacionCalendar> notificacionesCalendars ;
    
    @OneToMany(mappedBy = "aplicacion")
    private List<NotificacionEmail> notificacionesEmails ;
    
    @OneToMany(mappedBy = "aplicacion")
    private List<NotificacionWebUsuario> notificacionesWebs ;

    /**
     * @return lista de roles deserializada
     */
    public List<String> getRolesList()
    {
        return Arrays.asList(this.roles.split(Constants.STRING_COMA)) ;
    }

    /**
     * Setter para establecer los roles desde una lista.
     * 
     * @param rolesList lista de roles
     */
    public void setRolesList(List<String> rolesList) {
        this.roles = null;

        if (rolesList != null && !rolesList.isEmpty()) {
            StringBuilder rolesStringBuilder = new StringBuilder();

            for (int i = 0; i < rolesList.size(); i++) {
                // Añadimos el rol
                rolesStringBuilder.append(rolesList.get(i));

                // Si no es el último, añadimos una coma
                if (i < rolesList.size() - 1) {
                    rolesStringBuilder.append(Constants.STRING_COMA);
                }
            }

            // Convierte el StringBuilder a cadena
            this.roles = rolesStringBuilder.toString();
        }
    }
}
