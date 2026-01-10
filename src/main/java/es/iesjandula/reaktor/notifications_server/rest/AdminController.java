package es.iesjandula.reaktor.notifications_server.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.notifications_server.models.Aplicacion;
import es.iesjandula.reaktor.notifications_server.repository.IAplicacionRepository;
import es.iesjandula.reaktor.notifications_server.utils.Constants;
import es.iesjandula.reaktor.notifications_server.utils.NotificationsServerException;
import lombok.extern.log4j.Log4j2;

@RequestMapping(value = "/notifications/admin")
@RestController
@Log4j2
public class AdminController
{
    @Autowired
    private IAplicacionRepository aplicacionRepository;
    
	/**
	 * Lista las incidencias ordenadas por fecha.
	 * 
	 * Este método devuelve una lista de incidencias ordenadas por fecha, tanto para el profesor como para el administrador.
	 * 
	 * @param usuario El usuario que lista las incidencias (profesor o administrador).
	 * @param pageable La página de incidencias a listar (paginación).
	 * @return Un objeto {@link ResponseEntity} que puede contener:
	 *         <ul>
	 *         <li>Un código de estado 200 (OK) si la lista de incidencias se devuelve correctamente.</li>
	 *         <li>Un código de estado 500 (Internal Server Error) si ocurre un error inesperado.</li>
	 *         </ul>
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
	@GetMapping("/") 	
	public ResponseEntity<?> listarAplicaciones()
	{ 	   
		try
		{    
			// Devolvemos la respuesta
			return ResponseEntity.ok().body(this.aplicacionRepository.buscarTodasLasAplicaciones());
		}
		catch (Exception exception)
		{
            // Creamos una excepción genérica para devolver al cliente
			NotificationsServerException notificationsServerException =  new NotificationsServerException(BaseConstants.ERR_GENERIC_EXCEPTION_CODE, BaseConstants.ERR_GENERIC_EXCEPTION_MSG, exception);

			// Log de la excepción
			log.error("Excepción genérica al listar las aplicaciones", notificationsServerException);

			// Devolvemos la respuesta
			return ResponseEntity.status(500).body(notificationsServerException.getBodyExceptionMessage());
		}
	}

	/**
	 * Crea una nueva aplicación.
	 * 
	 * @param aplicacionData Los datos de la aplicación a crear (nombre, notifMaxCalendar, notifMaxEmail, notifMaxWeb).
	 * @return Un objeto {@link ResponseEntity} que puede contener:
	 *         <ul>
	 *         <li>Un código de estado 200 (OK) si la aplicación se crea correctamente.</li>
	 *         <li>Un código de estado 400 (Bad Request) si la aplicación ya existe o hay datos inválidos.</li>
	 *         <li>Un código de estado 500 (Internal Server Error) si ocurre un error inesperado.</li>
	 *         </ul>
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
	@Transactional
	@PostMapping("/")
	public ResponseEntity<?> crearAplicacion(@RequestHeader("aplicacion") String aplicacion, @RequestHeader("notificacionesMaximasCalendar") int notificacionesMaximasCalendar, @RequestHeader("notificacionesMaximasEmail") int notificacionesMaximasEmail, @RequestHeader("notificacionesMaximasWeb") int notificacionesMaximasWeb)
	{
		try
		{
			// Validamos que el nombre no esté vacío
			if (aplicacion == null || aplicacion.trim().isEmpty())
			{
				String message = "El nombre de la aplicación es obligatorio";
				
				log.error(message);
				throw new NotificationsServerException(Constants.ERR_CODE_APLICACION_NO_ENCONTRADA, message, null);
			}

			// Le quitamos los espacios en blanco
			aplicacion = aplicacion.trim();

			// Verificamos si la aplicación ya existe
			if (this.aplicacionRepository.existsById(aplicacion))
			{
				// Creamos el mensaje de error
				String message = "La aplicación ya existe";
				
				// Logueamos el error
				log.error(message);
				
				// Lanzamos la excepción
				throw new NotificationsServerException(Constants.ERR_CODE_APLICACION_YA_EXISTE, message, null);
			}

			// Creamos la nueva aplicación
			Aplicacion nuevaAplicacion = new Aplicacion();
			nuevaAplicacion.setNombre(aplicacion);
			nuevaAplicacion.setNotifMaxCalendar(notificacionesMaximasCalendar);
			nuevaAplicacion.setNotifMaxEmail(notificacionesMaximasEmail);
			nuevaAplicacion.setNotifMaxWeb(notificacionesMaximasWeb);
			nuevaAplicacion.setNotifHoyCalendar(0);
			nuevaAplicacion.setNotifHoyEmail(0);
			nuevaAplicacion.setNotifHoyWeb(0);
			nuevaAplicacion.setFechaUltimaNotificacionCalendar(null);
			nuevaAplicacion.setFechaUltimaNotificacionEmail(null);
			nuevaAplicacion.setFechaUltimaNotificacionWeb(null);

			// Guardamos la aplicación
			this.aplicacionRepository.save(nuevaAplicacion);

			// Devolvemos la respuesta
			return ResponseEntity.ok().build();
		}
		catch (NotificationsServerException notificationsServerException)
		{
			// Devolvemos la respuesta
			return ResponseEntity.status(400).body(notificationsServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			// Creamos una excepción genérica para devolver al cliente
			NotificationsServerException notificationsServerException = new NotificationsServerException(BaseConstants.ERR_GENERIC_EXCEPTION_CODE, 
																										 BaseConstants.ERR_GENERIC_EXCEPTION_MSG, 
																										 exception);

			// Log de la excepción
			log.error("Excepción genérica al crear la aplicación", notificationsServerException);

			// Devolvemos la respuesta
			return ResponseEntity.status(500).body(notificationsServerException.getBodyExceptionMessage());
		}
	}

	/**
	 * Actualiza las notificaciones máximas de una aplicación.
	 * 
	 * @param aplicacion La aplicación a actualizar.
	 * @param notificacionesMaximas Las notificaciones máximas a actualizar.
	 * @return Un objeto {@link ResponseEntity} que puede contener:
	 *         <ul>
	 *         <li>Un código de estado 200 (OK) si la notificación máxima se actualiza correctamente.</li>
	 *         <li>Un código de estado 500 (Internal Server Error) si ocurre un error inesperado.</li>
	 *         </ul>
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
	@Transactional
	@PutMapping("/calendar")
	public ResponseEntity<?> actualizarNotificacionesMaximasCalendar(@RequestHeader("aplicacion") String aplicacion, @RequestHeader("notificacionesMaximas") int notificacionesMaximas)
	{
		try
		{
			// Actualizamos las notificaciones máximas de la aplicación
			this.aplicacionRepository.actualizarNotificacionesMaximasCalendar(aplicacion, notificacionesMaximas);

			// Devolvemos la respuesta
			return ResponseEntity.ok().build();
		}
		catch (Exception exception)
		{
			// Creamos una excepción genérica para devolver al cliente
			NotificationsServerException notificationsServerException =  new NotificationsServerException(BaseConstants.ERR_GENERIC_EXCEPTION_CODE, BaseConstants.ERR_GENERIC_EXCEPTION_MSG, exception);

			// Log de la excepción
			log.error("Excepción genérica al actualizar las notificaciones máximas de la aplicación", notificationsServerException);

			// Devolvemos la respuesta
			return ResponseEntity.status(500).body(notificationsServerException.getBodyExceptionMessage());	
		}
	}

	/**
	 * Actualiza las notificaciones máximas de una aplicación.
	 * 
	 * @param aplicacion La aplicación a actualizar.
	 * @param notificacionesMaximas Las notificaciones máximas a actualizar.
	 * @return Un objeto {@link ResponseEntity} que puede contener:
	 *         <ul>
	 *         <li>Un código de estado 200 (OK) si la notificación máxima se actualiza correctamente.</li>
	 *         <li>Un código de estado 500 (Internal Server Error) si ocurre un error inesperado.</li>
	 *         </ul>
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
	@Transactional
	@PutMapping("/email")
	public ResponseEntity<?> actualizarNotificacionesMaximasEmail(@RequestHeader("aplicacion") String aplicacion, @RequestHeader("notificacionesMaximas") int notificacionesMaximas)
	{
		try
		{
			// Actualizamos las notificaciones máximas de la aplicación
			this.aplicacionRepository.actualizarNotificacionesMaximasEmail(aplicacion, notificacionesMaximas);

			// Devolvemos la respuesta
			return ResponseEntity.ok().build();
		}
		catch (Exception exception)
		{
			// Creamos una excepción genérica para devolver al cliente
			NotificationsServerException notificationsServerException =  new NotificationsServerException(BaseConstants.ERR_GENERIC_EXCEPTION_CODE, BaseConstants.ERR_GENERIC_EXCEPTION_MSG, exception);

			// Log de la excepción
			log.error("Excepción genérica al actualizar las notificaciones máximas de la aplicación", notificationsServerException);

			// Devolvemos la respuesta
			return ResponseEntity.status(500).body(notificationsServerException.getBodyExceptionMessage());
		}
	}

	/**
	 * Actualiza las notificaciones máximas de una aplicación.
	 * 
	 * @param aplicacion La aplicación a actualizar.
	 * @param notificacionesMaximas Las notificaciones máximas a actualizar.
	 * @return Un objeto {@link ResponseEntity} que puede contener:
	 *         <ul>
	 *         <li>Un código de estado 200 (OK) si la notificación máxima se actualiza correctamente.</li>
	 *         <li>Un código de estado 500 (Internal Server Error) si ocurre un error inesperado.</li>
	 *         </ul>
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
	@Transactional
	@PutMapping("/web")
	public ResponseEntity<?> actualizarNotificacionesMaximasWeb(@RequestHeader("aplicacion") String aplicacion, @RequestHeader("notificacionesMaximas") int notificacionesMaximas)
	{
		try
		{
			// Actualizamos las notificaciones máximas de la aplicación
			this.aplicacionRepository.actualizarNotificacionesMaximasWeb(aplicacion, notificacionesMaximas);

			// Devolvemos la respuesta
			return ResponseEntity.ok().build();
		}
		catch (Exception exception)
		{
			// Creamos una excepción genérica para devolver al cliente
			NotificationsServerException notificationsServerException =  new NotificationsServerException(BaseConstants.ERR_GENERIC_EXCEPTION_CODE, BaseConstants.ERR_GENERIC_EXCEPTION_MSG, exception);

			// Log de la excepción
			log.error("Excepción genérica al actualizar las notificaciones máximas de la aplicación", notificationsServerException);

			// Devolvemos la respuesta
			return ResponseEntity.status(500).body(notificationsServerException.getBodyExceptionMessage());
		}
	}

	/**
	 * Borra una aplicación por su nombre.
	 * 
	 * @param nombreAplicacion El nombre de la aplicación a borrar.
	 * @return Un objeto {@link ResponseEntity} que puede contener:
	 *         <ul>
	 *         <li>Un código de estado 200 (OK) si la aplicación se borra correctamente.</li>
	 *         <li>Un código de estado 404 (Not Found) si la aplicación no existe.</li>
	 *         <li>Un código de estado 500 (Internal Server Error) si ocurre un error inesperado.</li>
	 *         </ul>
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
	@Transactional
	@DeleteMapping("/")
	public ResponseEntity<?> borrarAplicacion(@RequestHeader("aplicacion") String aplicacion)
	{
		try
		{
			// Verificamos si la aplicación existe
			if (!this.aplicacionRepository.existsById(aplicacion))
			{
				// Creamos el mensaje de error
				String message = "La aplicación no existe";

				// Logueamos el error
				log.error(message);

				// Lanzamos la excepción
				throw new NotificationsServerException(Constants.ERR_CODE_APLICACION_NO_ENCONTRADA, message, null);
			}

			// Borramos la aplicación
			this.aplicacionRepository.deleteById(aplicacion);

			// Devolvemos la respuesta
			return ResponseEntity.ok().build();
		}
		catch (NotificationsServerException notificationsServerException)
		{
			// Devolvemos la respuesta
			return ResponseEntity.status(400).body(notificationsServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			// Creamos una excepción genérica para devolver al cliente
			NotificationsServerException notificationsServerException = new NotificationsServerException(BaseConstants.ERR_GENERIC_EXCEPTION_CODE, 
																										 BaseConstants.ERR_GENERIC_EXCEPTION_MSG, 
																										 exception);

			// Log de la excepción
			log.error("Excepción genérica al borrar la aplicación", notificationsServerException);

			// Devolvemos la respuesta
			return ResponseEntity.status(500).body(notificationsServerException.getBodyExceptionMessage());
		}
	}
}