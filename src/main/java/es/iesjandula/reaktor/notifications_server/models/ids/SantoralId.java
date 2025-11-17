package es.iesjandula.reaktor.notifications_server.models.ids;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class SantoralId implements Serializable
{
    /** Día del santoral */
    private Integer dia ;
    /** Mes del santoral */
    private Integer mes ;
}
