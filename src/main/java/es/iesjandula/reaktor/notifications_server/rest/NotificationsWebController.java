package es.iesjandula.reaktor.notifications_server.rest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
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
import es.iesjandula.reaktor.notifications_server.dtos.NotificacionesWebVigentesDto;
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
public class NotificationsWebController 
{
	@Autowired
	private IAplicacionRepository aplicacionRepository ;

	@Autowired
	private IUsuarioRepository usuarioRepository ;
	
	@Autowired
	private INotificacionWebUsuarioRepository notificacionWebUsuarioRepository ;

	@Autowired
	private INotificacionWebAplicacionRepository notificacionWebAplicacionRepository ;

	@Value("${" + Constants.NOTIFICATIONS_MAX_CALENDAR + "}")
	private int notifMaxCalendar ;

	@Value("${" + Constants.NOTIFICATIONS_MAX_EMAIL + "}")
	private int notifMaxEmail ;

	@Value("${" + Constants.NOTIFICATIONS_MAX_WEB + "}")
	private int notifMaxWeb ;

	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/roles")
	public ResponseEntity<?> obtenerRolesUsuario(@AuthenticationPrincipal DtoUsuarioExtended usuario)
	{
		List<String> roles = new ArrayList<String>();

		// Si el usuario es administrador...
		if (usuario.getRoles().contains(BaseConstants.ROLE_ADMINISTRADOR))
		{
			// Añadimos el role de administrador
			roles.add(BaseConstants.ROLE_ADMINISTRADOR);
		}

		// Si el usuario es dirección...
		if (usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION))
		{
			// Añadimos el role de dirección
			roles.add(BaseConstants.ROLE_DIRECCION);
		}

		// Si el usuario es profesor...
		if (usuario.getRoles().contains(BaseConstants.ROLE_PROFESOR))
		{
			// Añadimos el role de profesor
			roles.add(BaseConstants.ROLE_PROFESOR);
		}

		return ResponseEntity.ok(roles) ;
	}

	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/levels")
	public ResponseEntity<?> obtenerNivelesNotificaciones()
	{
		return ResponseEntity.ok(Arrays.asList(Constants.NIVEL_GLOBAL, Constants.NIVEL_SECUNDARIO)) ;
	}

	/**
	 * Método para crear una notificación web de usuario
	 * @param usuario Usuario extendido
	 * @param texto Texto de la notificación web
	 * @param fechaInicio Fecha de inicio de la notificación web
	 * @param horaInicio Hora de inicio de la notificación web
	 * @param fechaFin Fecha de fin de la notificación web
	 * @param horaFin Hora de fin de la notificación web
	 * @param roles Roles de la notificación web
	 * @param nivel Nivel de la notificación web
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
	                                                  @RequestHeader("roles") String roles,
	                                                  @RequestHeader("nivel") String nivel)
	{
		try 
	    {
			// Obtenemos el usuario de la base de datos
			Usuario usuarioDatabase = this.obtenerUsuario(usuario) ;

			if (usuarioDatabase.getNotifHoyWeb() >= usuarioDatabase.getNotifMaxWeb())
			{
				String errorMessage = "No se puede crear la notificación web porque se ha alcanzado el límite de notificaciones web";
				
				log.error(errorMessage);
				throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_CREATION, errorMessage);
			}

			// Realizamos validaciones previas
			this.validacionesPreviasNotificacionWebUsuario(texto, fechaInicio, horaInicio, fechaFin, horaFin, roles, nivel) ;

			// Creamos la notificación web de usuario
			this.crearNotificacionWebUsuario(usuarioDatabase, texto, fechaInicio, horaInicio, fechaFin, horaFin, roles, nivel);

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
	        
			NotificationsServerException NotificationsServerException =  new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception);
	        return ResponseEntity.status(500).body(NotificationsServerException.getBodyExceptionMessage());
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
	 * Método auxiliar para realizar validaciones previas a la creación de una notificación web
	 * @param texto Texto de la notificación web
	 * @param fechaInicio Fecha de inicio de la notificación web
	 * @param horaInicio Hora de inicio de la notificación web
	 * @param fechaFin Fecha de fin de la notificación web
	 * @param horaFin Hora de fin de la notificación web
	 * @param roles Roles de la notificación web
	 * @param nivel Nivel de la notificación web
	 * @throws NotificationsServerException Excepción de notificaciones web
	 */
	private void validacionesPreviasNotificacionWebUsuario(String texto, String fechaInicio, String horaInicio, String fechaFin, String horaFin, String roles, String nivel) throws NotificationsServerException
	{
		// Validamos los campos comunes
		this.validacionesPreviasNotificacionWebComunes(texto, fechaInicio, horaInicio, fechaFin, horaFin, roles, nivel);

		boolean roleEncontrado = roles.contains(BaseConstants.ROLE_ADMINISTRADOR) ||
								 roles.contains(BaseConstants.ROLE_DIRECCION)     ||
								 roles.contains(BaseConstants.ROLE_PROFESOR) ;

		if (!roleEncontrado)
		{
			String errorMessage = "Roles inválidos. Solo se permiten: " + BaseConstants.ROLE_ADMINISTRADOR + " o "  +
																		  BaseConstants.ROLE_DIRECCION     + " o "  +
																		  BaseConstants.ROLE_PROFESOR ;

			log.error(errorMessage);
			throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_CREATION, errorMessage);
		}
	}

	/**
	 * Método auxiliar para crear una notificación web
	 * @param usuario Usuario
	 * @param texto Texto de la notificación web
	 * @param fechaInicio Fecha de inicio de la notificación web
	 * @param horaInicio Hora de inicio de la notificación web
	 * @param fechaFin Fecha de fin de la notificación web
	 * @param horaFin Hora de fin de la notificación web
	 * @param roles Roles de la notificación web
	 * @param nivel Nivel de la notificación web
	 * @throws NotificationsServerException Excepción de notificaciones web
	 */
	private void crearNotificacionWebUsuario(Usuario usuario,
	                                         String texto,
											 String fechaInicio, String horaInicio,
											 String fechaFin,    String horaFin,
											 String roles,       String nivel) throws NotificationsServerException
	{
		// Creamos una instancia de notificación web de usuario
		NotificacionWebUsuario notificacionWebUsuario = new NotificacionWebUsuario();

		// Creamos una instancia de notificación web
		this.crearNotificacionWebCamposComunes(notificacionWebUsuario, texto, fechaInicio, horaInicio, fechaFin, horaFin, roles, nivel);

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
    @RequestMapping(method = RequestMethod.PUT, value = "/users/{id}")
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
    public ResponseEntity<?> cambiarEstadoNotificacionWebUsuario(@AuthenticationPrincipal DtoUsuarioExtended usuario,
		                                                         @PathVariable("id") Long id)
    {
        try 
        {
			// Si el usuario es Dirección o Administrador, puede cambiar el estado de cualquier notificación
			if (usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION) || usuario.getRoles().contains(BaseConstants.ROLE_ADMINISTRADOR))
			{
				// Cambiamos el estado de la notificación web de usuario y obtenemos la notificación web de usuario
				NotificacionWebUsuario notificacionUsuario = this.cambiarEstadoNotificacionWebUsuarioInternal(id);

				// Actualizar el número de notificaciones web del usuario
				this.cambiarEstadoNotificacionWebUsuarioActualizarNumeroNotificaciones(notificacionUsuario);
			}
			else
			{
                // Buscamos la notificación web de usuario por el id
				NotificacionWebUsuario notificacionUsuario = this.buscarNotificacionWebUsuarioPorId(id);

				// Verificamos si el usuario es el propietario de la notificación
				if (!notificacionUsuario.getUsuario().getEmail().equals(usuario.getEmail()))
				{
					String errorMessage = "No tienes permisos para cambiar el estado de esta notificación";

					log.error(errorMessage);
					throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_CHANGE_STATE, errorMessage);
				}

				// Actualizar el número de notificaciones web del usuario
				this.cambiarEstadoNotificacionWebUsuarioActualizarNumeroNotificaciones(notificacionUsuario);
			}

            return ResponseEntity.status(200).build() ;
        } 
		catch (NotificationsServerException NotificationsServerException)
		{
			return ResponseEntity.status(400).body(NotificationsServerException.getBodyExceptionMessage()) ;
		}
        catch (Exception exception) 
        {
            String errorMessage = "Error al eliminar la notificación con id: " + id;
            log.error(errorMessage, exception);

            NotificationsServerException NotificationsServerException = new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception);
            return ResponseEntity.status(500).body(NotificationsServerException.getBodyExceptionMessage());
        }
    }

	/**
	 * Método auxiliar para cambiar el estado de una notificación web de usuario
	 * @param id Id de la notificación web
	 * @return la notificación web
	 * @throws NotificationsServerException Excepción de notificaciones web
	 */
	private NotificacionWebUsuario cambiarEstadoNotificacionWebUsuarioInternal(Long id) throws NotificationsServerException
	{
		// Buscamos la notificación web de usuario por el id
		NotificacionWebUsuario notificacionUsuario = this.buscarNotificacionWebUsuarioPorId(id);

		// Seteamos el estado de la notificación al contrario del que está
		notificacionUsuario.setActivo(!notificacionUsuario.isActivo());

		// Guardamos la notificación en la base de datos
		this.notificacionWebUsuarioRepository.saveAndFlush(notificacionUsuario);

		// Devolvemos la notificación web
		return notificacionUsuario;
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
	 * Método auxiliar para actualizar el número de notificaciones web del usuario
	 * @param notificacionUsuario Notificación web de usuario
	 */
	private void cambiarEstadoNotificacionWebUsuarioActualizarNumeroNotificaciones(NotificacionWebUsuario notificacionUsuario)
	{
		// Si la notificación está activa, decrementamos el número de notificaciones web del usuario, si no, incrementamos
		if (notificacionUsuario.isActivo())
		{
			// Decrementamos el número de notificaciones web del usuario
			notificacionUsuario.getUsuario().setNotifHoyWeb(notificacionUsuario.getUsuario().getNotifHoyWeb() - 1);
		}
		else
		{
			// Incrementamos el número de notificaciones web del usuario
			notificacionUsuario.getUsuario().setNotifHoyWeb(notificacionUsuario.getUsuario().getNotifHoyWeb() + 1);
		}

		// Guardamos el usuario en la base de datos
		this.usuarioRepository.saveAndFlush(notificacionUsuario.getUsuario());
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
	 * @param roles Roles de la notificación web
	 * @param nivel Nivel de la notificación web
	 * @return ResponseEntity con el resultado de la creación de la notificación web
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/apps")
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_APLICACION_NOTIFICACIONES + "')")
	public ResponseEntity<?> crearNotificacionWebApp(@AuthenticationPrincipal DtoAplicacion aplicacion,
			                                         @RequestHeader("nombre") String nombre,
			                                         @RequestHeader("texto") String texto,
			                                         @RequestHeader("fecha_inicio") String fechaInicio,
			                                         @RequestHeader("hora_inicio") String horaInicio,
			                                         @RequestHeader("fecha_fin") String fechaFin,
			                                         @RequestHeader("hora_fin") String horaFin,
			                                         @RequestHeader("roles") String roles,
			                                         @RequestHeader("nivel") String nivel)
	{
	    try 
	    {
			// Obtenemos la aplicación de la base de datos
			Aplicacion aplicacionDatabase = this.obtenerAplicacion(aplicacion);

			if (aplicacionDatabase.getNotifHoyWeb() >= aplicacionDatabase.getNotifMaxWeb())
			{
				String errorMessage = "No se puede crear la notificación web porque se ha alcanzado el límite de notificaciones web";
				
				log.error(errorMessage);
				throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_CREATION, errorMessage);
			}

			// Validamos los campos de la aplicación
			this.validacionesPreviasNotificacionWebApp(texto, fechaInicio, horaInicio, fechaFin, horaFin, roles, nivel);

			// Creamos la notificación web de aplicación
			this.crearNotificacionWebApp(aplicacionDatabase, texto, fechaInicio, horaInicio, fechaFin, horaFin, roles, nivel);

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
	        
			NotificationsServerException NotificationsServerException =  new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception);
	        return ResponseEntity.status(500).body(NotificationsServerException.getBodyExceptionMessage());
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

			// Seteamos los roles de la aplicación
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
	 * @param roles Roles de la notificación web
	 * @param nivel Nivel de la notificación web
	 * @throws NotificationsServerException Excepción de notificaciones web
	 */
	private void validacionesPreviasNotificacionWebApp(String texto, String fechaInicio, String horaInicio, String fechaFin, String horaFin, String roles, String nivel) throws NotificationsServerException
	{
		// Validamos los campos comunes
		this.validacionesPreviasNotificacionWebComunes(texto, fechaInicio, horaInicio, fechaFin, horaFin, roles, nivel);

		if (!roles.contains(BaseConstants.ROLE_APLICACION_NOTIFICACIONES))
		{
			String errorMessage = "Roles inválidos. Solo se permiten: " + BaseConstants.ROLE_APLICACION_NOTIFICACIONES ;

			log.error(errorMessage);
			throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_CREATION, errorMessage);
		}
	}

	/**
	 * Método auxiliar para realizar validaciones previas a la creación de una notificación web
	 * @param texto Texto de la notificación web
	 * @param fechaInicio Fecha de inicio de la notificación web
	 * @param horaInicio Hora de inicio de la notificación web
	 * @param fechaFin Fecha de fin de la notificación web
	 * @param horaFin Hora de fin de la notificación web
	 * @param roles Roles de la notificación web
	 * @param nivel Nivel de la notificación web
	 * @throws NotificationsServerException Excepción de notificaciones web
	 */
	private void validacionesPreviasNotificacionWebComunes(String texto, String fechaInicio, String horaInicio, String fechaFin, String horaFin, String roles, String nivel) throws NotificationsServerException
	{
		// Validamos si los campos son nulos o vacíos
		if (texto == null       || texto.isEmpty()       || 
		    fechaInicio == null || fechaInicio.isEmpty() || horaInicio == null || horaInicio.isEmpty() || 
			fechaFin == null    || fechaFin.isEmpty()    || horaFin == null    || horaFin.isEmpty()    || 
			roles == null       || roles.isEmpty()       || 
			nivel == null       || nivel.isEmpty())
		{
			String errorMessage = "Campos inválidos. Nulos o vacíos" ;
			log.error(errorMessage);
			throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_CREATION, errorMessage);
		}

		// Validamos el nivel, solo se permiten los niveles globales y secundarios
		if (!nivel.equalsIgnoreCase(Constants.NIVEL_GLOBAL) && !nivel.equalsIgnoreCase(Constants.NIVEL_SECUNDARIO)) 
		{
			String errorMessage = "Nivel inválido. Solo se permiten: "  + Constants.NIVEL_GLOBAL + " o "  + Constants.NIVEL_SECUNDARIO;
			
			log.error(errorMessage);
			throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_CREATION, errorMessage);
		}

		// Validamos si se permite adjuntar imagen en notificaciones de nivel SECUNDARIO, no se permite
		if (nivel.equalsIgnoreCase(Constants.NIVEL_SECUNDARIO) && texto != null && texto.contains("[Imagen:")) 
		{
			String errorMessage = "No se permite adjuntar imagen en notificaciones de nivel SECUNDARIO" ;
			log.error(errorMessage) ;
			throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_CREATION, errorMessage) ;
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
	 * @param roles Roles de la notificación web
	 * @param nivel Nivel de la notificación web
	 * @throws NotificationsServerException Excepción de notificaciones web
	 */
	private void crearNotificacionWebApp(Aplicacion aplicacion, 
	                                     String texto,
										 String fechaInicio, String horaInicio,
										 String fechaFin,    String horaFin,
										 String roles,       String nivel) throws NotificationsServerException
	{
		// Creamos una instancia de notificación web de aplicación
		NotificacionWebAplicacion notificacionWebAplicacion = new NotificacionWebAplicacion();

		// Creamos los campos comunes de la notificación web
		this.crearNotificacionWebCamposComunes(notificacionWebAplicacion, texto, fechaInicio, horaInicio, fechaFin, horaFin, roles, nivel);

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
	 * @param roles Roles de la notificación web
	 * @param nivel Nivel de la notificación web
	 * @throws NotificationsServerException Excepción de notificaciones web
	 */
	private void crearNotificacionWebCamposComunes(NotificacionWeb notificacionWeb, 
	                                               String texto,
												   String fechaInicio, String horaInicio,
												   String fechaFin,    String horaFin,
												   String roles,       String nivel) throws NotificationsServerException
	{
		// Seteamos la fecha de creación
		notificacionWeb.setFechaCreacion(new Date());

		// Seteamos el texto
		notificacionWeb.setTexto(texto);

		// Seteamos el nivel
		notificacionWeb.setNivel(nivel.toUpperCase());

		// Convertimos las fechas a Date
		Date fechaInicioDate = this.convertirFecha(fechaInicio);
		Date fechaFinDate    = this.convertirFecha(fechaFin);

		// Convertimos la hora a LocalTime
		LocalTime horaInicioLocalTime = this.convertirHora(horaInicio);
		LocalTime horaFinLocalTime    = this.convertirHora(horaFin);

		// Seteamos la fecha y hora de inicio
		notificacionWeb.setFechaInicio(fechaInicioDate);
		notificacionWeb.setHoraInicio(horaInicioLocalTime);

		// Seteamos la fecha y hora de fin
		notificacionWeb.setFechaFin(fechaFinDate);
		notificacionWeb.setHoraFin(horaFinLocalTime);
		
		// Seteamos los roles
		notificacionWeb.setRoles(roles);

		// Seteamos el activo a true
		notificacionWeb.setActivo(true) ;
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
			return new SimpleDateFormat("yyyy-MM-dd").parse(fecha);
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
	 * Método para obtener las notificaciones vigentes por nivel
	 * @param usuario Usuario extendido
	 * @param nivel Nivel de las notificaciones
	 * @return ResponseEntity con el resultado de las notificaciones vigentes
	 * @throws NotificationsServerException Excepción de notificaciones web
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/level")
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	public ResponseEntity<?> obtenerNotificacionesVigentesPorNivel(@AuthenticationPrincipal DtoUsuarioExtended usuario,
																   @RequestHeader("nivel") String nivel)
	{
	    List<NotificacionesWebVigentesDto> resultado = new ArrayList<NotificacionesWebVigentesDto>();

		try 
		{
			// Obtenemos todas las notificaciones vigentes de los usuarios
			resultado.addAll(this.notificacionWebUsuarioRepository.buscarTodasLasNotificacionesUsuariosVigentesPorNivel(nivel)) ;

			// Obtenemos todas las notificaciones vigentes de la aplicación
			resultado.addAll(this.notificacionWebAplicacionRepository.buscarTodasLasNotificacionesAplicacionesVigentesPorNivel(nivel)) ;

			return ResponseEntity.ok(resultado) ;			
		}
		catch (Exception exception) 
		{			
			String errorMessage = "Error inesperado al obtener las notificaciones" ;
			log.error(errorMessage, exception) ;

			NotificationsServerException NotificationsServerException = new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception) ;
			return ResponseEntity.internalServerError().body(NotificationsServerException.getBodyExceptionMessage()) ;
		
		}
	}

	/**
	 * Método para obtener las notificaciones vigentes por usuario
	 * @param usuario Usuario extendido
	 * @return ResponseEntity con el resultado de las notificaciones vigentes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/users")
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	public ResponseEntity<?> obtenerNotificacionesVigentesPorUsuario(@AuthenticationPrincipal DtoUsuarioExtended usuario)
	{
	    List<NotificacionesWebVigentesDto> resultado = new ArrayList<NotificacionesWebVigentesDto>();

		try 
		{
			// Si el usuario es un administrador, obtenemos todas las notificaciones vigentes de aplicaciones
			if (usuario.getRoles().contains(BaseConstants.ROLE_ADMINISTRADOR))
			{
				resultado.addAll(this.notificacionWebAplicacionRepository.buscarTodasLasNotificacionesAplicacionesVigentes()) ;
			}

			// Si además es dirección, obtenemos todas las notificaciones vigentes de los usuarios
			if (usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION))
			{
				resultado.addAll(this.notificacionWebUsuarioRepository.buscarTodasLasNotificacionesUsuariosVigentes()) ;
			}
			// Si el usuario es profesor, obtenemos las notificaciones vigentes de este usuario
			else if (usuario.getRoles().contains(BaseConstants.ROLE_PROFESOR))
			{
				resultado.addAll(this.notificacionWebUsuarioRepository.buscarNotificacionesVigentesUsuariosPorUsuario(usuario.getEmail())) ;
			}

			return ResponseEntity.ok(resultado) ;			
		}
		catch (Exception exception) 
		{			
			String errorMessage = "Error inesperado al obtener las notificaciones por usuario" ;
			log.error(errorMessage, exception) ;

			NotificationsServerException NotificationsServerException = new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception) ;
			return ResponseEntity.internalServerError().body(NotificationsServerException.getBodyExceptionMessage()) ;
		}
	}

	/**
	 * Método para eliminar una notificación web de usuario
	 * @param usuario Usuario extendido
	 * @param id Id de la notificación web
	 * @return ResponseEntity con el resultado de la eliminación de la notificación web
	 */
    @RequestMapping(method = RequestMethod.PUT, value = "/apps/{id}")
    @PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
	public ResponseEntity<?> cambiarEstadoNotificacionWebAplicacion(@AuthenticationPrincipal DtoAplicacion aplicacion,
	                                                                @PathVariable("id") Long id)
	{
		try
		{
			// Cambiamos el estado de la notificación web de usuario y obtenemos la notificación web de usuario
			NotificacionWebAplicacion notificacionWebAplicacion = this.cambiarEstadoNotificacionWebAplicacionInternal(id);

			// Actualizar el número de notificaciones web del usuario
			this.cambiarEstadoNotificacionWebAplicacionActualizarNumeroNotificaciones(notificacionWebAplicacion);

			// Devolvemos la respuesta
			return ResponseEntity.status(200).build() ;
		}
		catch (NotificationsServerException firebaseServerServerException)
		{
			return ResponseEntity.status(400).body(firebaseServerServerException.getBodyExceptionMessage()) ;
		}
		catch (Exception exception) 
		{
			String errorMessage = "Error al cambiar el estado de la notificación web";
			log.error(errorMessage, exception);

			NotificationsServerException NotificationsServerException = new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception);
			return ResponseEntity.status(500).body(NotificationsServerException.getBodyExceptionMessage()) ;
		}
	}

	/**
	 * Método auxiliar para cambiar el estado de una notificación web de aplicación
	 * @param id Id de la notificación web
	 * @return la notificación web de aplicación
	 * @throws NotificationsServerException Excepción de notificaciones web
	 */
	private NotificacionWebAplicacion cambiarEstadoNotificacionWebAplicacionInternal(Long id) throws NotificationsServerException
	{
		// Buscamos la notificación web de aplicación por el id
		NotificacionWebAplicacion notificacionWebAplicacion = this.buscarNotificacionWebAplicacionPorId(id);

		// Seteamos el estado de la notificación al contrario del que está
		notificacionWebAplicacion.setActivo(!notificacionWebAplicacion.isActivo());

		// Guardamos la notificación en la base de datos
		this.notificacionWebAplicacionRepository.saveAndFlush(notificacionWebAplicacion);

		// Devolvemos la notificación web
		return notificacionWebAplicacion;
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

	/**
	 * Método auxiliar para actualizar el número de notificaciones web de la aplicación
	 * @param notificacionWebAplicacion Notificación web de aplicación
	 */
	private void cambiarEstadoNotificacionWebAplicacionActualizarNumeroNotificaciones(NotificacionWebAplicacion notificacionWebAplicacion)
	{
		// Si la notificación está activa, decrementamos el número de notificaciones web de la aplicación, si no, incrementamos
		if (notificacionWebAplicacion.isActivo())
		{
			// Decrementamos el número de notificaciones web de la aplicación
			notificacionWebAplicacion.getAplicacion().setNotifHoyWeb(notificacionWebAplicacion.getAplicacion().getNotifHoyWeb() - 1);
		}
		else
		{
			// Incrementamos el número de notificaciones web de la aplicación
			notificacionWebAplicacion.getAplicacion().setNotifHoyWeb(notificacionWebAplicacion.getAplicacion().getNotifHoyWeb() + 1);
		}

		// Guardamos la aplicación en la base de datos
		this.aplicacionRepository.saveAndFlush(notificacionWebAplicacion.getAplicacion());
	}
}
