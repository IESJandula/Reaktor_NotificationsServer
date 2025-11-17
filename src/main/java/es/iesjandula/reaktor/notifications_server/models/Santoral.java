package es.iesjandula.reaktor.notifications_server.models;

import jakarta.persistence.Entity;
import es.iesjandula.reaktor.notifications_server.models.ids.SantoralId;
import jakarta.persistence.EmbeddedId;
import lombok.Data;

@Entity
@Data
public class Santoral
{
    @EmbeddedId
    private SantoralId santoralId;

    /** Nombre del santoral */
    private String nombre;
}
