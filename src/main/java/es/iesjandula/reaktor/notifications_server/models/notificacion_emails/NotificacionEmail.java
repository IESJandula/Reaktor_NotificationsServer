package es.iesjandula.reaktor.notifications_server.models.notificacion_emails;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notificacion_email")	
@NoArgsConstructor
@AllArgsConstructor
@Data
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class NotificacionEmail 
{
	/** Atributo - Id */
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private Long id ;

    @Column
	private LocalDate fechaCreacion ;
	
	@Column
	private String asunto ;
	
	@Column
	private String contenido ;
}
