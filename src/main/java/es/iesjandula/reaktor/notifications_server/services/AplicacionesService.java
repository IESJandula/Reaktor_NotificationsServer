package es.iesjandula.reaktor.notifications_server.services;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.base.security.models.DtoAplicacion;
import es.iesjandula.reaktor.notifications_server.repository.IAplicacionRepository;
import es.iesjandula.reaktor.notifications_server.utils.NotificationsServerException;
import es.iesjandula.reaktor.notifications_server.models.Aplicacion;
import es.iesjandula.reaktor.notifications_server.utils.Constants;
import es.iesjandula.reaktor.notifications_server.models.Constante;

/**
 * Clase que gestiona las aplicaciones de la aplicación
 */
@Service
public class AplicacionesService
{
	/** Repositorio de aplicaciones de la base de datos */
	@Autowired
	private IAplicacionRepository aplicacionRepository ;

	/** Servicio de constantes de la aplicación */
	@Autowired
	private ConstantesService constantesService ;

	/**
	 * Método auxiliar para obtener la aplicación de la base de datos
	 * @param aplicacion Aplicación
	 * @return Aplicación de la base de datos
	 * @throws NotificationsServerException Excepción de notificaciones web
	 */
	public Aplicacion obtenerAplicacion(DtoAplicacion aplicacion) throws NotificationsServerException
	{
        // Creamos variable de aplicación
        Aplicacion aplicacionDatabase = null;

		// Buscamos si existe la aplicación, sino lo creamos
		Optional<Aplicacion> aplicacionDatabaseOptional = this.aplicacionRepository.findById(aplicacion.getNombre());

		// Si no existe la aplicación ...
		if (aplicacionDatabaseOptional.isEmpty())
		{
			// Creamos la aplicación en la base de datos
			this.crearAplicacionEnBBDD(aplicacion);
		}
        else
        {
            // ... obtenemos la aplicación de la base de datos
            aplicacionDatabase = aplicacionDatabaseOptional.get();
        }

		return aplicacionDatabase;
	}
    
    /**
     * Método auxiliar para crear un usuario en la base de datos
     * @param aplicacionDto DTO de aplicación
     * @throws NotificationsServerException Excepción de notificaciones web
     */
    private void crearAplicacionEnBBDD(DtoAplicacion aplicacionDto) throws NotificationsServerException
    {
        // Creamos una nueva instancia de aplicación
        Aplicacion aplicacionDatabase = new Aplicacion() ;

        // Seteamos los atributos del usuario
        aplicacionDatabase.setNombre(aplicacionDto.getNombre());

        // Seteamos los receptores de la aplicación
        aplicacionDatabase.setRolesList(aplicacionDto.getRoles());

        // Asociamos los contadores de notificaciones
        this.asociarContadoresNotificaciones(aplicacionDatabase);

        // Guardamos la aplicación en la base de datos
        this.aplicacionRepository.saveAndFlush(aplicacionDatabase);
    }

    /**
     * Método auxiliar para asociar los contadores de notificaciones a la aplicación
     * @param aplicacionDatabase Aplicación en la base de datos
     * @throws NotificationsServerException Excepción de notificaciones web
     */
    private void asociarContadoresNotificaciones(Aplicacion aplicacionDatabase) throws NotificationsServerException
    {
        // Seteamos el número de notificaciones de hoy
        aplicacionDatabase.setNotifMaxWeb(0);
        aplicacionDatabase.setNotifHoyCalendar(0) ;
        aplicacionDatabase.setNotifHoyEmail(0) ;

        // Obtenemos la constante de notificaciones máximas de tipo web
        Constante constanteNotificacionesMaxWeb = this.constantesService.obtenerConstante(Constants.TABLA_CONST_NOTIFICACIONES_MAX_WEB) ;

        // Seteamos el número de notificaciones máximas de tipo web
        aplicacionDatabase.setNotifMaxWeb(Integer.parseInt(constanteNotificacionesMaxWeb.getValor())) ;

        // Obtenemos la constante de notificaciones máximas de tipo email
        Constante constanteNotificacionesMaxEmail = this.constantesService.obtenerConstante(Constants.TABLA_CONST_NOTIFICACIONES_MAX_EMAILS) ;

        // Seteamos el número de notificaciones máximas de tipo email
        aplicacionDatabase.setNotifMaxEmail(Integer.parseInt(constanteNotificacionesMaxEmail.getValor())) ;

        // Obtenemos la constante de notificaciones máximas de tipo calendar
        Constante constanteNotificacionesMaxCalendar = this.constantesService.obtenerConstante(Constants.TABLA_CONST_NOTIFICACIONES_MAX_CALENDAR) ;

        // Seteamos el número de notificaciones máximas de tipo calendar
        aplicacionDatabase.setNotifMaxCalendar(Integer.parseInt(constanteNotificacionesMaxCalendar.getValor())) ;
    }

    /**
     * Método auxiliar para actualizar una aplicación al enviar una notificación web
     * @param aplicacion Aplicación
     * @throws NotificationsServerException Excepción de notificaciones web
     */
    public void aplicacionHaEnviadoNotificacionWeb(Aplicacion aplicacion) throws NotificationsServerException
    {
        // Incrementamos el número de notificaciones web de la aplicación
        aplicacion.setNotifHoyWeb(aplicacion.getNotifHoyWeb() + 1);

        // Seteamos la fecha de la última notificación web
        aplicacion.setFechaUltimaNotificacionWeb(LocalDateTime.now()) ;

        // Actualizamos la aplicación en la base de datos
        this.aplicacionRepository.saveAndFlush(aplicacion);
    }

    /**
     * Método auxiliar para actualizar una aplicación al eliminar una notificación web
     * @param aplicacion Aplicación
     * @throws NotificationsServerException Excepción de notificaciones web
     */
    public void aplicacionHaEliminadoNotificacionWeb(Aplicacion aplicacion) throws NotificationsServerException
    {
        // Decrementamos el número de notificaciones web de la aplicación
        aplicacion.setNotifHoyWeb(aplicacion.getNotifHoyWeb() - 1);

        // Actualizamos la aplicación en la base de datos
        this.aplicacionRepository.saveAndFlush(aplicacion);
    }

    /**
     * Método auxiliar para actualizar una aplicación al enviar una notificación de email
     * @param aplicacion Aplicación
     * @throws NotificationsServerException Excepción de notificaciones web
     */
    public void aplicacionHaEnviadoNotificacionEmail(Aplicacion aplicacion) throws NotificationsServerException
    {
        // Incrementamos el número de notificaciones de email de la aplicación
        aplicacion.setNotifHoyEmail(aplicacion.getNotifHoyEmail() + 1);

        // Seteamos la fecha de la última notificación de email
        aplicacion.setFechaUltimaNotificacionEmail(LocalDateTime.now()) ;

        // Actualizamos la aplicación en la base de datos
        this.aplicacionRepository.saveAndFlush(aplicacion);
    }
}
