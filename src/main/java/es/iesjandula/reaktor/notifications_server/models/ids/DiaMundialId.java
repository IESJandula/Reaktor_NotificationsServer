package es.iesjandula.reaktor.notifications_server.models.ids;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class DiaMundialId implements Serializable
{
    /** Día del dia mundial */
    private Integer dia ;
    /** Mes del dia mundial */
    private Integer mes ;
}
