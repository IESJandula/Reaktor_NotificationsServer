package es.iesjandula.reaktor.notifications_server.models.notificacion_emails.aplicacion;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "notificacion_email_copia_oculta_aplicacion")
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionEmailCopiaOcultaAplicacion 
{
    /** Atributo - Identificador único */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private NotificacionEmailAplicacion notificacionEmailAplicacion;
}
