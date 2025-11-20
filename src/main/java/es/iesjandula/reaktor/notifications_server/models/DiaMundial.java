package es.iesjandula.reaktor.notifications_server.models;

import jakarta.persistence.Entity;
import es.iesjandula.reaktor.notifications_server.models.ids.DiaMundialId;
import jakarta.persistence.EmbeddedId;
import lombok.Data;

@Entity
@Data
public class DiaMundial
{
    @EmbeddedId
    private DiaMundialId diaMundialId;

    /** Nombre del dia mundial */
    private String nombre;
}
