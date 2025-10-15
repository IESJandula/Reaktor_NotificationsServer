package es.iesjandula.reaktor.notifications_server.models.ids;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionUsuarioId implements Serializable
{
    /**
     * Serialización de la clase para persistencia.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Identificador de la notificación.
     */
    private Long notificacionId ;

    /**
     * Email del usuario.
     */
    private String usuarioEmail ;
}
