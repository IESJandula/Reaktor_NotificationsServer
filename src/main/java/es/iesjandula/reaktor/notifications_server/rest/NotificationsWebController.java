package es.iesjandula.reaktor.notifications_server.rest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
import es.iesjandula.reaktor.notifications_server.dtos.NotificacionesWebHoyDto;
import es.iesjandula.reaktor.notifications_server.models.Aplicacion;
import es.iesjandula.reaktor.notifications_server.models.NotificacionWebAplicacion;
import es.iesjandula.reaktor.notifications_server.models.Usuario;
import es.iesjandula.reaktor.notifications_server.models.NotificacionWeb;
import es.iesjandula.reaktor.notifications_server.models.NotificacionWebUsuario;
import es.iesjandula.reaktor.notifications_server.repository.IAplicacionRepository;
import es.iesjandula.reaktor.notifications_server.repository.INotificacionWebAplicacionRepository;
import es.iesjandula.reaktor.notifications_server.repository.INotificacionWebUsuarioRepository;
import es.iesjandula.reaktor.notifications_server.repository.IUsuarioRepository;
import es.iesjandula.reaktor.notifications_server.utils.Constants;
import es.iesjandula.reaktor.notifications_server.utils.NotificationsServerException;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/notifications_web")
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

	@RequestMapping(method = RequestMethod.POST, value = "/user")
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	public ResponseEntity<?> crearNotificacionWebUser(@AuthenticationPrincipal DtoUsuarioExtended usuario,
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
			// Obtenemos el usuario de la base de datos
			Usuario usuarioDatabase = this.obtenerUsuario(usuario);

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

		// Verificamos si existe el usuario
		usuarioDatabase = usuarioDatabaseOptional.get() ;

		// Si no existe el usuario ...
		if (usuarioDatabase == null)
		{
			// ... creamos una nueva instancia de usuario
			usuarioDatabase = new Usuario() ;

			// Seteamos los atributos del usuario
			usuarioDatabase.setEmail(usuario.getEmail());
			usuarioDatabase.setNombre(usuario.getNombre());
			usuarioDatabase.setApellidos(usuario.getApellidos());
			usuarioDatabase.setDepartamento(usuario.getDepartamento());
			usuarioDatabase.setRoles(String.join(",", usuario.getRoles()));

			// Guardamos el usuario en la base de datos
			this.usuarioRepository.saveAndFlush(usuarioDatabase);
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
	 * @param roles Roles de la notificación web
	 * @param nivel Nivel de la notificación web
	 */
	private void crearNotificacionWebUsuario(Usuario usuario, String texto, String fechaInicio, String horaInicio, String fechaFin, String horaFin, String roles, String nivel)
	{
		// Creamos una instancia de notificación web
		NotificacionWeb notificacionWebUsuario = new NotificacionWebUsuario();

		// Seteamos los campos comunes
		this.crearNotificacionWebComunes(notificacionWebUsuario, texto, fechaInicio, horaInicio, fechaFin, horaFin, roles, nivel);

		// Seteamos el usuario
		((NotificacionWebUsuario) notificacionWebUsuario).setUsuario(usuario);

		// Guardamos la notificación web de usuario en la base de datos
		this.notificacionWebUsuarioRepository.saveAndFlush((NotificacionWebUsuario) notificacionWebUsuario);
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
			String errorMessage = "Roles inválidos. Solo se permiten: " 
							+ BaseConstants.ROLE_ADMINISTRADOR + " o " 
							+ BaseConstants.ROLE_DIRECCION     + " o " 
							+ BaseConstants.ROLE_PROFESOR ;

			log.error(errorMessage);
			throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_CREATION, errorMessage);
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/user")
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

			// Seteamos los atributos del usuario
			aplicacionDatabase.setNombre(aplicacion.getNombre());
			aplicacionDatabase.setRoles(String.join(",", aplicacion.getRoles()));

			// Guardamos la aplicación en la base de datos
			this.aplicacionRepository.saveAndFlush(aplicacionDatabase);
		}

		return aplicacionDatabase;
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
	 */
	private void crearNotificacionWebApp(Aplicacion aplicacion, String texto, String fechaInicio, String horaInicio, String fechaFin, String horaFin, String roles, String nivel)
	{
		// Creamos una instancia de notificación web de aplicación
		NotificacionWeb notificacionWeb = new NotificacionWebAplicacion();

		// Seteamos los campos comunes
		this.crearNotificacionWebComunes(notificacionWeb, texto, fechaInicio, horaInicio, fechaFin, horaFin, roles, nivel);

		// Seteamos la aplicación
		((NotificacionWebAplicacion) notificacionWeb).setAplicacion(aplicacion);

		// Guardamos la notificación web de aplicación en la base de datos
		this.notificacionWebAplicacionRepository.saveAndFlush((NotificacionWebAplicacion) notificacionWeb);
	}

	/**
	 * Método auxiliar para crear los campos comunes de una notificación web
	 * @param notificacionWeb Notificación web
	 * @param texto Texto de la notificación web
	 * @param fechaInicio Fecha de inicio de la notificación web
	 * @param horaInicio Hora de inicio de la notificación web
	 * @param fechaFin Fecha de fin de la notificación web
	 * @param horaFin Hora de fin de la notificación web
	 * @param roles Roles de la notificación web
	 * @param nivel Nivel de la notificación web
	 */
	private void crearNotificacionWebComunes(NotificacionWeb notificacionWeb, String texto, String fechaInicio, String horaInicio, String fechaFin, String horaFin, String roles, String nivel)
	{
		// Seteamos la fecha de creación
		notificacionWeb.setFechaCreacion(LocalDate.now());

		// Seteamos el texto
		notificacionWeb.setTexto(texto);

		// Seteamos el nivel
		notificacionWeb.setNivel(nivel.toUpperCase());

		// Seteamos la fecha y hora de inicio
		notificacionWeb.setFechaInicio(LocalDate.parse(fechaInicio));
		notificacionWeb.setHoraInicio(LocalTime.parse(horaInicio));

		// Seteamos la fecha y hora de fin
		notificacionWeb.setFechaFin(LocalDate.parse(fechaFin));
		notificacionWeb.setHoraFin(LocalTime.parse(horaFin));
		
		// Seteamos los roles
		notificacionWeb.setRoles(roles);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/")
	public ResponseEntity<?> obtenerNotificacionHoy(@RequestHeader("usuario") String usuario, @RequestHeader("nivel") String nivel)
	{
	    List<NotificacionesWebHoyDto> resultado = new ArrayList<NotificacionesWebHoyDto>();
		try 
		{
			LocalDate hoy = LocalDate.now() ;
			
			List<NotificacionWebUsuario> notificaciones = notificacionWebRepository.findByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(hoy, hoy) ;
			
			if (notificaciones != null && !notificaciones.isEmpty())
			{
	        	resultado = notificaciones.stream()
	        						      .filter(n -> n.getNivel().equalsIgnoreCase(nivel))
					                      .map(n -> new NotificacionesWebHoyDto(
											   n.getId(),
											   n.getTexto(),
											   n.getNivel(),
											   n.getFechaInicio(),
											   n.getHoraInicio(),
											   n.getFechaFin(),
											   n.getHoraFin(),
											   String.join(",", n.getRoles())
							))
							.collect(Collectors.toList()) ;
			}

			return ResponseEntity.status(200).body(resultado) ;			
		}
		catch (Exception exception) 
		{			
			String errorMessage = "Error inesperado al obtener las notificaciones" ;
			log.error(errorMessage, exception) ;

			NotificationsServerException NotificationsServerException = new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception) ;
			return ResponseEntity.status(500).body(NotificationsServerException.getBodyExceptionMessage()) ;
		
		}
		
	}
    
    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
    public ResponseEntity<?> eliminarNotificacionWeb(@PathVariable("id") Long id) 
    {
        try 
        {
            // Buscar la notificación
            NotificacionWebUsuario notificacion = notificacionWebRepository.findById(id).orElse(null);

            if (notificacion == null) 
            {
                String errorMessage = "No se encontró la notificación con id: " + id;
                log.error(errorMessage);

                throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_DELETION, errorMessage);
            }

            // Eliminar
            notificacionWebRepository.delete(notificacion);

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
}
