package es.iesjandula.reaktor.notifications_server.models;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notificacion_email")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NotificacionEmail 
{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id ;
	
	private LocalDate fechaCreacion ;
	
	private String asunto ;
	
	private String contenido ;
	
	@ManyToOne
	@JoinColumn(name = "aplicacion_client_id", nullable = false)
	private Aplicacion aplicacion ;
	
	@OneToMany(mappedBy = "notificacion")
    private List<NotificacionEmailPara> paraUsuarios;

    @OneToMany(mappedBy = "notificacion")
    private List<NotificacionEmailCopia> copiaUsuarios;

    @OneToMany(mappedBy = "notificacion")
    private List<NotificacionEmailCopiaOculta> copiaOcultaUsuarios;
	
}
