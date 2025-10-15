package es.iesjandula.reaktor.notifications_server.models.notificacion_emails;

import java.time.LocalDate;
import java.util.List;

import es.iesjandula.reaktor.notifications_server.models.Aplicacion;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notificacion_email")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Inheritance(strategy = InheritanceType.JOINED)
public class NotificacionEmail 
{
    @Column
	private LocalDate fechaCreacion ;
	
	@Column
	private String asunto ;
	
	@Column
	private String contenido ;
}
