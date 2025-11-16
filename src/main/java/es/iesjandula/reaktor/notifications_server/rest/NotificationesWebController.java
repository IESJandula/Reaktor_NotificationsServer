package es.iesjandula.reaktor.notifications_server.rest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.base.security.models.DtoAplicacion;
import es.iesjandula.reaktor.base.security.models.DtoUsuarioExtended;
import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.notifications_server.dtos.NotificacionesWebResponseDto;
import es.iesjandula.reaktor.notifications_server.models.Actor;
import es.iesjandula.reaktor.notifications_server.models.Aplicacion;
import es.iesjandula.reaktor.notifications_server.models.Usuario;
import es.iesjandula.reaktor.notifications_server.models.notificacion_web.NotificacionWeb;
import es.iesjandula.reaktor.notifications_server.models.notificacion_web.aplicacion.NotificacionWebAplicacion;
import es.iesjandula.reaktor.notifications_server.models.notificacion_web.usuario.NotificacionWebUsuario;
import es.iesjandula.reaktor.notifications_server.repository.IAplicacionRepository;
import es.iesjandula.reaktor.notifications_server.repository.INotificacionWebAplicacionRepository;
import es.iesjandula.reaktor.notifications_server.repository.INotificacionWebUsuarioRepository;
import es.iesjandula.reaktor.notifications_server.repository.IUsuarioRepository;
import es.iesjandula.reaktor.notifications_server.utils.Constants;
import es.iesjandula.reaktor.notifications_server.utils.NotificationsServerException;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/notifications/web")
@Slf4j
public class NotificationesWebController 
{
	@Autowired
	private IAplicacionRepository aplicacionRepository ;

	@Autowired
	private IUsuarioRepository usuarioRepository ;
	
	@Autowired
	private INotificacionWebUsuarioRepository notificacionWebUsuarioRepository ;

	@Autowired
	private INotificacionWebAplicacionRepository notificacionWebAplicacionRepository ;

	@Value("${" + Constants.REAKTOR_NOTIFICATIONES_MAX_CALENDAR + "}")
	private int notifMaxCalendar ;

	@Value("${" + Constants.REAKTOR_NOTIFICATIONES_MAX_EMAIL + "}")
	private int notifMaxEmail ;

	@Value("${" + Constants.REAKTOR_NOTIFICATIONS_MAX_WEB + "}")
	private int notifMaxWeb ;

	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/receptors")
	public ResponseEntity<?> obtenerReceptoresUsuario(@AuthenticationPrincipal DtoUsuarioExtended usuario)
	{
		return ResponseEntity.ok(this.obtenerReceptores(usuario.getRoles())) ;
	}

	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/types")
	public ResponseEntity<?> obtenerTiposNotificaciones()
	{
		return ResponseEntity.ok(Constants.TIPOS_NOTIFICACIONES) ;
	}

	/**
	 * Método para crear una notificación web de usuario
	 * @param usuario Usuario extendido
	 * @param texto Texto de la notificación web
	 * @param fechaInicio Fecha de inicio de la notificación web
	 * @param horaInicio Hora de inicio de la notificación web
	 * @param fechaFin Fecha de fin de la notificación web
	 * @param horaFin Hora de fin de la notificación web
	 * @param receptor Receptor de la notificación web
	 * @param tipo Tipo de notificación web
	 * @return ResponseEntity con el resultado de la creación de la notificación web
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/users")
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	public ResponseEntity<?> crearNotificacionWebUser(@AuthenticationPrincipal DtoUsuarioExtended usuario,
	                                                  @RequestHeader("texto") String texto,
	                                                  @RequestHeader("fechaInicio") String fechaInicio,
	                                                  @RequestHeader("horaInicio") String horaInicio,
	                                                  @RequestHeader("fechaFin") String fechaFin,
	                                                  @RequestHeader("horaFin") String horaFin,
	                                                  @RequestHeader("receptor") String receptor,
	                                                  @RequestHeader("tipo") String tipo)
	{
		try 
	    {
			// Obtenemos el usuario de la base de datos
			Usuario usuarioDatabase = this.obtenerUsuario(usuario) ;

			if (usuarioDatabase.getNotifHoyWeb() >= usuarioDatabase.getNotifMaxWeb())
			{
				String errorMessage = "Has alcanzado el límite diario de notificaciones web: " + usuarioDatabase.getNotifMaxWeb();
				
				log.error(errorMessage);
				throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_CREATION, errorMessage);
			}

			// Realizamos validaciones previas
			this.validacionesPrevias(texto, fechaInicio, horaInicio, fechaFin, horaFin, receptor, tipo) ;

			// Creamos la notificación web de usuario
			this.crearNotificacionWebUsuario(usuarioDatabase, texto, fechaInicio, horaInicio, fechaFin, horaFin, receptor, tipo);

			// Devolvemos la respuesta
	        return ResponseEntity.status(200).build() ;

	    }
        catch (NotificationsServerException firebaseServerServerException)
        {
			return ResponseEntity.status(400).body(firebaseServerServerException.getBodyExceptionMessage()) ;
        }
	    catch (Exception exception) 
	    {
	        String errorMessage = "Error al crear la notificación web";
	        log.error(errorMessage, exception);
	        
			NotificationsServerException notificationsServerException =  new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception);
	        return ResponseEntity.status(500).body(notificationsServerException.getBodyExceptionMessage());
	    }
	}

	/**
	 * Método auxiliar para obtener el usuario de la base de datos
	 * @param usuario Usuario extendido
	 * @return Usuario de la base de datos
	 */
	private Usuario obtenerUsuario(DtoUsuarioExtended usuario)
	{
		// Creamos variable de usuario
		Usuario usuarioDatabase = null;

		// Buscamos si existe el usuario, sino lo creamos
		Optional<Usuario> usuarioDatabaseOptional = this.usuarioRepository.findById(usuario.getEmail()) ;

		// Si no existe el usuario ...
		if (usuarioDatabaseOptional.isEmpty())
		{
			// ... creamos una nueva instancia de usuario
			usuarioDatabase = new Usuario() ;

			// Seteamos los atributos del usuario
			usuarioDatabase.setEmail(usuario.getEmail());
			usuarioDatabase.setNombre(usuario.getNombre());
			usuarioDatabase.setApellidos(usuario.getApellidos());
			usuarioDatabase.setDepartamento(usuario.getDepartamento());
			usuarioDatabase.setRoles(String.join(",", usuario.getRoles()));

			// Seteamos los campos comunes de la aplicación
			this.setearCamposComunesActores(usuarioDatabase);

			// Guardamos el usuario en la base de datos
			this.usuarioRepository.saveAndFlush(usuarioDatabase);
		}
		else
		{
			// ... obtenemos el usuario de la base de datos
			usuarioDatabase = usuarioDatabaseOptional.get();
		}

		return usuarioDatabase;
	}

	/**
	 * Método auxiliar para crear una notificación web
	 * @param usuario Usuario
	 * @param texto Texto de la notificación web
	 * @param fechaInicio Fecha de inicio de la notificación web
	 * @param horaInicio Hora de inicio de la notificación web
	 * @param fechaFin Fecha de fin de la notificación web
	 * @param horaFin Hora de fin de la notificación web
	 * @param receptor Receptor de la notificación web
	 * @param tipo Tipo de notificación web
	 * @throws NotificationsServerException Excepción de notificaciones web
	 */
	private void crearNotificacionWebUsuario(Usuario usuario,
	                                         String texto,
											 String fechaInicio, String horaInicio,
											 String fechaFin,    String horaFin,
											 String receptor,    String tipo) throws NotificationsServerException
	{
		// Creamos una instancia de notificación web de usuario
		NotificacionWebUsuario notificacionWebUsuario = new NotificacionWebUsuario();

		// Creamos una instancia de notificación web
		this.crearNotificacionWebCamposComunes(notificacionWebUsuario, texto, fechaInicio, horaInicio, fechaFin, horaFin, receptor, tipo);

		// Seteamos el usuario
		notificacionWebUsuario.setUsuario(usuario);

		// Guardamos la notificación web de usuario en la base de datos
		this.notificacionWebUsuarioRepository.saveAndFlush(notificacionWebUsuario);

		// Incrementamos el número de notificaciones web del usuario
		usuario.setNotifHoyWeb(usuario.getNotifHoyWeb() + 1);

		// Guardamos el usuario en la base de datos
		this.usuarioRepository.saveAndFlush(usuario);
	}

	/**
	 * Método para eliminar una notificación web de usuario
	 * @param usuario Usuario extendido
	 * @param id Id de la notificación web
	 * @return ResponseEntity con el resultado de la eliminación de la notificación web
	 */
    @RequestMapping(method = RequestMethod.DELETE, value = "/users/{id}")
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
    public ResponseEntity<?> eliminarNotificacionWebUsuario(@AuthenticationPrincipal DtoUsuarioExtended usuarioDto,
		                                                    @PathVariable("id") Long id)
    {
		NotificacionWebUsuario notificacionUsuario = null ;

        try 
        {
			// Buscamos la notificación web de usuario por el id
			notificacionUsuario = this.buscarNotificacionWebUsuarioPorId(id);

			// Si el usuario es solo profesor y no es el propietario de la notificación...
			if (!usuarioDto.getRoles().contains(BaseConstants.ROLE_DIRECCION) && !usuarioDto.getRoles().contains(BaseConstants.ROLE_ADMINISTRADOR) && 
			    !notificacionUsuario.getUsuario().getEmail().equalsIgnoreCase(usuarioDto.getEmail()))
			{
				String errorMessage = "No tienes permisos para eliminar esta notificación";

				log.error(errorMessage);
				throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_CHANGE_STATE, errorMessage);
			}

			// Obtenemos la información del usuario
			Usuario usuarioDatabase = notificacionUsuario.getUsuario();

			// Incrementamos el número de notificaciones web del usuario
			usuarioDatabase.setNotifHoyWeb(usuarioDatabase.getNotifHoyWeb() + 1);
			
			// Actualizamos el usuario en la base de datos
			this.usuarioRepository.saveAndFlush(usuarioDatabase);

			// Eliminamos la notificación web de usuario en la base de datos
			this.notificacionWebUsuarioRepository.delete(notificacionUsuario);

			return ResponseEntity.status(200).build() ;
        } 
		catch (NotificationsServerException notificationsServerException)
		{
			return ResponseEntity.status(400).body(notificationsServerException.getBodyExceptionMessage()) ;
		}
        catch (Exception exception) 
        {
            String errorMessage = "Error al eliminar la notificación con id: " + id;
            log.error(errorMessage, exception);

            NotificationsServerException notificationsServerException = new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception);
            return ResponseEntity.status(500).body(notificationsServerException.getBodyExceptionMessage());
        }
    }

	/**
	 * Método auxiliar para buscar una notificación web de usuario por el id
	 * @param id Id de la notificación web
	 * @return la notificación web de usuario
	 * @throws NotificationsServerException Excepción de notificaciones web
	 */
	private NotificacionWebUsuario buscarNotificacionWebUsuarioPorId(Long id) throws NotificationsServerException
	{
		// Buscamos la notificación por el id
		Optional<NotificacionWebUsuario> optionalNotificacion = this.notificacionWebUsuarioRepository.findById(id) ;

		// Verificamos si existe la notificación
		if (!optionalNotificacion.isPresent())
		{
			String errorMessage = "No se encontró la notificación con id: " + id;
			
			log.error(errorMessage);
			throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_CHANGE_STATE, errorMessage);
		}

		// Obtenemos la notificación
		return optionalNotificacion.get() ;
	}

	/**
	 * Método para crear una notificación web de aplicación
	 * @param aplicacion Aplicación
	 * @param nombre Nombre de la aplicación
	 * @param texto Texto de la notificación web
	 * @param fechaInicio Fecha de inicio de la notificación web
	 * @param horaInicio Hora de inicio de la notificación web
	 * @param fechaFin Fecha de fin de la notificación web
	 * @param horaFin Hora de fin de la notificación web
	 * @param receptor Receptor de la notificación web
	 * @param tipo Tipo de notificación web
	 * @return ResponseEntity con el resultado de la creación de la notificación web
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/apps")
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_APLICACION_NOTIFICACIONES + "')")
	public ResponseEntity<?> crearNotificacionWebApp(@AuthenticationPrincipal DtoAplicacion aplicacion,
			                                         @RequestHeader("texto") String texto,
			                                         @RequestHeader("fecha_inicio") String fechaInicio,
			                                         @RequestHeader("hora_inicio") String horaInicio,
			                                         @RequestHeader("fecha_fin") String fechaFin,
			                                         @RequestHeader("hora_fin") String horaFin,
			                                         @RequestHeader("receptor") String receptor,
			                                         @RequestHeader("tipo") String tipo)
	{
	    try 
	    {
			// Obtenemos la aplicación de la base de datos
			Aplicacion aplicacionDatabase = this.obtenerAplicacion(aplicacion);

			if (aplicacionDatabase.getNotifHoyWeb() >= aplicacionDatabase.getNotifMaxWeb())
			{
				String errorMessage = "Has alcanzado el límite diario de notificaciones web: " + aplicacionDatabase.getNotifMaxWeb();
				
				log.error(errorMessage);
				throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_CREATION, errorMessage);
			}

			// Realizamos validaciones previas
			this.validacionesPrevias(texto, fechaInicio, horaInicio, fechaFin, horaFin, receptor, tipo);

			// Creamos la notificación web de aplicación
			this.crearNotificacionWebApp(aplicacionDatabase, texto, fechaInicio, horaInicio, fechaFin, horaFin, receptor, tipo);

			// Devolvemos la respuesta
	        return ResponseEntity.status(200).build() ;

	    }
        catch (NotificationsServerException firebaseServerServerException)
        {
			return ResponseEntity.status(400).body(firebaseServerServerException.getBodyExceptionMessage()) ;
        }
	    catch (Exception exception) 
	    {
	        String errorMessage = "Error al crear la notificación web";
	        log.error(errorMessage, exception);
	        
			NotificationsServerException notificationsServerException =  new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception);
	        return ResponseEntity.status(500).body(notificationsServerException.getBodyExceptionMessage());
	    }
	}

	/**
	 * Método auxiliar para obtener la aplicación de la base de datos
	 * @param aplicacion Aplicación
	 * @return Aplicación de la base de datos
	 */
	private Aplicacion obtenerAplicacion(DtoAplicacion aplicacion)
	{
		// Buscamos si existe la aplicación, sino lo creamos
		Aplicacion aplicacionDatabase = this.aplicacionRepository.findByNombre(aplicacion.getNombre());

		// Si no existe la aplicación ...
		if (aplicacionDatabase == null)
		{
			// ... creamos una nueva instancia de aplicación
			aplicacionDatabase = new Aplicacion() ;

			// Seteamos los receptores de la aplicación
			aplicacionDatabase.setRoles(String.join(",", aplicacion.getRoles()));

			// Seteamos los atributos del usuario
			aplicacionDatabase.setNombre(aplicacion.getNombre());

			// Seteamos los campos comunes de la aplicación
			this.setearCamposComunesActores(aplicacionDatabase);

			// Guardamos la aplicación en la base de datos
			this.aplicacionRepository.saveAndFlush(aplicacionDatabase);
		}

		return aplicacionDatabase;
	}

	/**
	 * Método auxiliar para setear los campos comunes de un actor
	 * @param actor Actor
	 */
	private void setearCamposComunesActores(Actor actor)
	{
		// Seteamos la fecha de la última notificación
		LocalDateTime ahora = LocalDateTime.now() ;
		actor.setFechaUltimaNotificacionCalendar(ahora) ;
		actor.setFechaUltimaNotificacionEmail(ahora) ;
		actor.setFechaUltimaNotificacionWeb(ahora) ;

		// Inicializamos el número de notificaciones hoy
		actor.setNotifHoyCalendar(0) ;
		actor.setNotifHoyEmail(0) ;
		actor.setNotifHoyWeb(0) ;

		// Inicializamos el número de notificaciones máximas
		actor.setNotifMaxCalendar(this.notifMaxCalendar) ;
		actor.setNotifMaxEmail(this.notifMaxEmail) ;
		actor.setNotifMaxWeb(this.notifMaxWeb) ;
	}

	/**
	 * Método auxiliar para realizar validaciones previas a la creación de una notificación web
	 * @param texto Texto de la notificación web
	 * @param fechaInicio Fecha de inicio de la notificación web
	 * @param horaInicio Hora de inicio de la notificación web
	 * @param fechaFin Fecha de fin de la notificación web
	 * @param horaFin Hora de fin de la notificación web
	 * @param receptor Receptor de la notificación web
	 * @param tipo Tipo de notificación web
	 * @throws NotificationsServerException Excepción de notificaciones web
	 */
	private void validacionesPrevias(String texto, String fechaInicio, String horaInicio, String fechaFin, String horaFin, String receptor, String tipo) throws NotificationsServerException
	{
		// Validamos si los campos son nulos o vacíos
		if (texto == null       || texto.isEmpty()       || 
		    fechaInicio == null || fechaInicio.isEmpty() || horaInicio == null || horaInicio.isEmpty() || 
			fechaFin == null    || fechaFin.isEmpty()    || horaFin == null    || horaFin.isEmpty()    || 
			receptor == null   	|| receptor.isEmpty()    || 
			tipo == null 		|| tipo.isEmpty())
		{
			String errorMessage = "Campos inválidos. Nulos o vacíos" ;
			log.error(errorMessage);
			throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_CREATION, errorMessage);
		}

		// Validamos el tipo de notificación, solo se permiten los tipos de notificaciones de solo texto y texto e imagen
		if (!tipo.equalsIgnoreCase(Constants.TIPO_NOTIFICACION_SOLO_TEXTO) && !tipo.equalsIgnoreCase(Constants.TIPO_NOTIFICACION_TEXTO_E_IMAGEN)) 
		{
			String errorMessage = "Tipo de notificación inválido. Solo se permiten: "  + Constants.TIPO_NOTIFICACION_SOLO_TEXTO + " o "  + Constants.TIPO_NOTIFICACION_TEXTO_E_IMAGEN;
			
			log.error(errorMessage);
			throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_CREATION, errorMessage);
		}

		// Validamos si se permite adjuntar imagen en notificaciones de nivel SECUNDARIO, no se permite
		if (tipo.equalsIgnoreCase(Constants.TIPO_NOTIFICACION_TEXTO_E_IMAGEN) && texto != null && texto.contains("[Imagen:")) 
		{
			String errorMessage = "No se permite adjuntar imagen en notificaciones de tipo de notificación " + Constants.TIPO_NOTIFICACION_TEXTO_E_IMAGEN ;
			log.error(errorMessage) ;
			throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_CREATION, errorMessage) ;
		}
		
		// Validamos el receptor, solo se permiten los receptores de solo administradores, solo equipo directivo y todo el claustro
		boolean receptorEncontrado = receptor.equals(Constants.RECEPTOR_NOTIFICACION_ADMINISTRADORES) ||
								     receptor.equals(Constants.RECEPTOR_NOTIFICACION_EQUIPO_DIRECTIVO) ||
								     receptor.equals(Constants.RECEPTOR_NOTIFICACION_CLAUSTRO) ;

		if (!receptorEncontrado)
		{
			String errorMessage = "Receptor inválido. Solo se permiten: " + Constants.RECEPTOR_NOTIFICACION_ADMINISTRADORES + " o "  +
																	        Constants.RECEPTOR_NOTIFICACION_EQUIPO_DIRECTIVO     + " o "  +
																	        Constants.RECEPTOR_NOTIFICACION_CLAUSTRO ;

			log.error(errorMessage);
			throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_CREATION, errorMessage);
		}
	}

	/**
	 * Método auxiliar para crear una notificación web de aplicación
	 * @param aplicacion Aplicación
	 * @param texto Texto de la notificación web
	 * @param fechaInicio Fecha de inicio de la notificación web
	 * @param horaInicio Hora de inicio de la notificación web
	 * @param fechaFin Fecha de fin de la notificación web
	 * @param horaFin Hora de fin de la notificación web
	 * @param receptor Receptor de la notificación web
	 * @param tipo Tipo de notificación web
	 * @throws NotificationsServerException Excepción de notificaciones web
	 */
	private void crearNotificacionWebApp(Aplicacion aplicacion, 
	                                     String texto,
										 String fechaInicio, String horaInicio,
										 String fechaFin,    String horaFin,
										 String receptor,    String tipo) throws NotificationsServerException
	{
		// Creamos una instancia de notificación web de aplicación
		NotificacionWebAplicacion notificacionWebAplicacion = new NotificacionWebAplicacion();

		// Creamos los campos comunes de la notificación web
		this.crearNotificacionWebCamposComunes(notificacionWebAplicacion, texto, fechaInicio, horaInicio, fechaFin, horaFin, receptor, tipo);

		// Seteamos la aplicación
		notificacionWebAplicacion.setAplicacion(aplicacion);

		// Guardamos la notificación web de aplicación en la base de datos
		this.notificacionWebAplicacionRepository.saveAndFlush(notificacionWebAplicacion);
	}

	/**
	 * Método auxiliar para crear una notificación web
	 * @param notificacionWeb Notificación web
	 * @param texto Texto de la notificación web
	 * @param fechaInicio Fecha de inicio de la notificación web
	 * @param horaInicio Hora de inicio de la notificación web
	 * @param fechaFin Fecha de fin de la notificación web
	 * @param horaFin Hora de fin de la notificación web
	 * @param receptor Receptor de la notificación web
	 * @param tipo Tipo de notificación web
	 * @throws NotificationsServerException Excepción de notificaciones web
	 */
	private void crearNotificacionWebCamposComunes(NotificacionWeb notificacionWeb, 
	                                               String texto,
												   String fechaInicio, String horaInicio,
												   String fechaFin,    String horaFin,
												   String receptor,    String tipo) throws NotificationsServerException
	{
		// Seteamos la fecha de creación
		notificacionWeb.setFechaCreacion(new Date());

		// Seteamos el texto
		notificacionWeb.setTexto(texto);

		// Seteamos el nivel
		notificacionWeb.setTipo(tipo.toUpperCase());

		// Convertimos las fechas a Date
		Date fechaInicioDate = this.convertirFecha(fechaInicio);
		Date fechaFinDate    = this.convertirFecha(fechaFin);

		// Validamos si la fecha de fin es anterior a la fecha de inicio
		if (fechaFinDate.before(fechaInicioDate))
		{
			String errorMessage = "La fecha de fin no puede ser anterior a la fecha de inicio";

			log.error(errorMessage);
			throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_CREATION, errorMessage);
		}

		// Convertimos la hora a LocalTime
		LocalTime horaInicioLocalTime = this.convertirHora(horaInicio);
		LocalTime horaFinLocalTime    = this.convertirHora(horaFin);

		// Seteamos la fecha y hora de inicio
		notificacionWeb.setFechaInicio(fechaInicioDate);
		notificacionWeb.setHoraInicio(horaInicioLocalTime);

		// Seteamos la fecha y hora de fin
		notificacionWeb.setFechaFin(fechaFinDate);
		notificacionWeb.setHoraFin(horaFinLocalTime);
		
		// Seteamos el receptor
		notificacionWeb.setReceptor(receptor);
	}

	/**
	 * Método auxiliar para convertir una fecha a Date
	 * @param fecha Fecha
	 * @return Date
	 * @throws NotificationsServerException Excepción de notificaciones web
	 */
	private Date convertirFecha(String fecha) throws NotificationsServerException
	{
		try
		{
			return new SimpleDateFormat("dd/MM/yyyy").parse(fecha);
		}
		catch (ParseException parseException)
		{
			String errorMessage = "Error al convertir la fecha: " + fecha;
			log.error(errorMessage, parseException);
			throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_CREATION, errorMessage, parseException);
		}
	}

	/**
	 * Método auxiliar para convertir una hora a LocalTime
	 * @param hora Hora
	 * @return LocalTime
	 * @throws NotificationsServerException Excepción de notificaciones web
	 */
	private LocalTime convertirHora(String hora) throws NotificationsServerException
	{
		try
		{
			return LocalTime.parse(hora);
		}
		catch (DateTimeParseException dateTimeParseException)
		{
			String errorMessage = "Error al convertir la hora: " + hora;
			log.error(errorMessage, dateTimeParseException);
			throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_CREATION, errorMessage, dateTimeParseException);
		}
	}

	/**
	 * Método para obtener las notificaciones vigentes por tipo de notificación
	 * @param usuario Usuario extendido
	 * @param tipo Tipo de notificación de las notificaciones
	 * @return ResponseEntity con el resultado de las notificaciones vigentes
	 * @throws NotificationsServerException Excepción de notificaciones web
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/search_by_type")
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	public ResponseEntity<?> obtenerNotificacionesVigentesPorTipo(@AuthenticationPrincipal DtoUsuarioExtended usuario,
																  @RequestHeader("tipo") String tipo)
	{
	    List<NotificacionesWebResponseDto> resultado = new ArrayList<NotificacionesWebResponseDto>();

		try 
		{

			// Obtenemos los receptores de notificaciones
			List<String> receptores = this.obtenerReceptores(usuario.getRoles());

			// Obtenemos todas las notificaciones vigentes de los usuarios
			resultado.addAll(this.notificacionWebUsuarioRepository.buscarTodasLasNotificacionesUsuariosVigentesPorTipo(tipo, receptores)) ;

			// Obtenemos todas las notificaciones vigentes de la aplicación
			resultado.addAll(this.notificacionWebAplicacionRepository.buscarTodasLasNotificacionesAplicacionesVigentesPorTipo(tipo, receptores)) ;

			return ResponseEntity.ok(resultado) ;			
		}
		catch (Exception exception) 
		{			
			String errorMessage = "Error inesperado al obtener las notificaciones" ;
			log.error(errorMessage, exception) ;

			NotificationsServerException notificationsServerException = new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception) ;
			return ResponseEntity.internalServerError().body(notificationsServerException.getBodyExceptionMessage()) ;
		
		}
	}

    /**
	 * Método para obtener los receptores de notificaciones
	 * @param roles List<String> con los roles del usuario
	 * @return List<String> con los receptores de notificaciones
	 */
	private List<String> obtenerReceptores(List<String> roles)
	{
		List<String> receptores = new ArrayList<String>();

		// Si el usuario es administrador...
		if (roles.contains(BaseConstants.ROLE_ADMINISTRADOR))
		{
			// Añadimos el receptor de notificación de administradores
			receptores.add(Constants.RECEPTOR_NOTIFICACION_ADMINISTRADORES);
		}

		// Si el usuario es dirección...
		if (roles.contains(BaseConstants.ROLE_DIRECCION))
		{
			// Añadimos el receptor de notificación de equipo directivo
			receptores.add(Constants.RECEPTOR_NOTIFICACION_EQUIPO_DIRECTIVO);
		}

		// Si el usuario es profesor...
		if (roles.contains(BaseConstants.ROLE_PROFESOR))
		{
			// Añadimos el receptor de notificación de claustro
			receptores.add(Constants.RECEPTOR_NOTIFICACION_CLAUSTRO);
		}

		return receptores;
	}

	/**
	 * Método para obtener las notificaciones vigentes por usuario
	 * @param usuario Usuario extendido
	 * @return ResponseEntity con el resultado de las notificaciones vigentes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/search_by_user")
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	public ResponseEntity<?> obtenerNotificacionesPorUsuario(@AuthenticationPrincipal DtoUsuarioExtended usuario)
	{
	    List<NotificacionesWebResponseDto> resultado = new ArrayList<NotificacionesWebResponseDto>();

		try 
		{
			// Si el usuario es un administrador, obtenemos todas las notificaciones vigentes de aplicaciones
			if (usuario.getRoles().contains(BaseConstants.ROLE_ADMINISTRADOR))
			{
				resultado.addAll(this.notificacionWebAplicacionRepository.buscarTodasLasNotificacionesAplicaciones()) ;
			}

			// Si además es dirección, obtenemos todas las notificaciones vigentes de los usuarios
			if (usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION))
			{
				resultado.addAll(this.notificacionWebUsuarioRepository.buscarTodasLasNotificacionesUsuarios()) ;
			}
			// Si el usuario es profesor, obtenemos las notificaciones vigentes de este usuario
			else if (usuario.getRoles().contains(BaseConstants.ROLE_PROFESOR))
			{
				resultado.addAll(this.notificacionWebUsuarioRepository.buscarNotificacionesUsuariosPorUsuario(usuario.getEmail())) ;
			}

			return ResponseEntity.ok(resultado) ;			
		}
		catch (Exception exception) 
		{			
			String errorMessage = "Error inesperado al obtener las notificaciones por usuario" ;
			log.error(errorMessage, exception) ;

			NotificationsServerException notificationsServerException = new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception) ;
			return ResponseEntity.internalServerError().body(notificationsServerException.getBodyExceptionMessage()) ;
		}
	}

	/**
	 * Método para eliminar una notificación web de usuario
	 * @param usuario Usuario extendido
	 * @param id Id de la notificación web
	 * @return ResponseEntity con el resultado de la eliminación de la notificación web
	 */
    @RequestMapping(method = RequestMethod.DELETE, value = "/apps/{id}")
    @PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
	public ResponseEntity<?> eliminarNotificacionWebAplicacion(@AuthenticationPrincipal DtoAplicacion aplicacionDto,
	                                                           @PathVariable("id") Long id)
	{
		try
		{
			// Buscamos la notificación web de aplicación por el id
			NotificacionWebAplicacion notificacionWebAplicacion = this.buscarNotificacionWebAplicacionPorId(id);

			// Obtenemos la información de la aplicación
			Aplicacion aplicacionDatabase = notificacionWebAplicacion.getAplicacion();

			// Incrementamos el número de notificaciones web de la aplicación
			aplicacionDatabase.setNotifHoyWeb(aplicacionDatabase.getNotifHoyWeb() + 1);
			
			// Actualizamos la aplicación en la base de datos
			this.aplicacionRepository.saveAndFlush(aplicacionDatabase);

			// Eliminamos la notificación web de aplicación en la base de datos
			this.notificacionWebAplicacionRepository.delete(notificacionWebAplicacion);

			// Devolvemos la respuesta
			return ResponseEntity.ok().build() ;
		}
		catch (Exception exception) 
		{
			String errorMessage = "Error al eliminar la notificación web de aplicación";
			log.error(errorMessage, exception);

			NotificationsServerException notificationsServerException = new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception);
			return ResponseEntity.status(500).body(notificationsServerException.getBodyExceptionMessage()) ;
		}
	}

	/**
	 * Método auxiliar para buscar una notificación web de aplicación por el id
	 * @param id Id de la notificación web
	 * @return la notificación web de aplicación
	 * @throws NotificationsServerException Excepción de notificaciones web
	 */
	private NotificacionWebAplicacion buscarNotificacionWebAplicacionPorId(Long id) throws NotificationsServerException
	{
		// Buscamos la notificación web de aplicación por el id
		Optional<NotificacionWebAplicacion> optionalNotificacionWebAplicacion = this.notificacionWebAplicacionRepository.findById(id) ;

		// Verificamos si existe la notificación web de aplicación
		if (!optionalNotificacionWebAplicacion.isPresent())
		{
			String errorMessage = "No se encontró la notificación web de aplicación con id: " + id;
			log.error(errorMessage);
			throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_CHANGE_STATE, errorMessage);
		}

		// Obtenemos la notificación web de aplicación
		return optionalNotificacionWebAplicacion.get() ;
	}
}
