package es.iesjandula.reaktor.notifications_server.models;

import jakarta.persistence.Column;
import java.util.Arrays;
import java.util.List;
import es.iesjandula.reaktor.notifications_server.utils.Constants;

public class Actor
{
    /** Atributo - Roles asociados a la aplicación */
    @Column
    private String roles ;
    
    /** Atributo - Notificaciones de hoy de calendario */
    @Column 
    private int notifHoyCalendar ;
    
    /** Atributo - Notificaciones máximas de calendario */
    @Column
    private int notifMaxCalendar ;
    
    /** Atributo - Notificaciones de hoy de email */
    @Column
    private int notifHoyEmail ;
    
    /** Atributo - Notificaciones máximas de email */
    @Column
    private int notifMaxEmail ;
    
    /** Atributo - Notificaciones de hoy de web */
    @Column
    private int notifHoyWeb ;
    
    /** Atributo - Notificaciones máximas de web */
    @Column
    private int notifMaxWeb ;

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
    public void setRolesList(List<String> rolesList)
    {
        this.roles = null;

        if (rolesList != null && !rolesList.isEmpty())
        {
            StringBuilder rolesStringBuilder = new StringBuilder();

            for (int i = 0; i < rolesList.size(); i++)
            {
                // Añadimos el rol
                rolesStringBuilder.append(rolesList.get(i));

                // Si no es el último, añadimos una coma
                if (i < rolesList.size() - 1)
                {
                    rolesStringBuilder.append(Constants.STRING_COMA);
                }
            }

            // Convierte el StringBuilder a cadena
            this.roles = rolesStringBuilder.toString();
        }
    }
}
