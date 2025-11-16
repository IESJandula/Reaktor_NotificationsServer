package es.iesjandula.reaktor.notifications_server.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.base.resources_handler.ResourcesHandler;
import es.iesjandula.reaktor.base.resources_handler.ResourcesHandlerFile;
import es.iesjandula.reaktor.base.resources_handler.ResourcesHandlerJar;
import es.iesjandula.reaktor.base.utils.BaseException;
import es.iesjandula.reaktor.notifications_server.models.Aplicacion;
import es.iesjandula.reaktor.notifications_server.models.ConstantesNotificaciones;
import es.iesjandula.reaktor.notifications_server.models.Usuario;
import es.iesjandula.reaktor.notifications_server.repository.IAplicacionRepository;
import es.iesjandula.reaktor.notifications_server.repository.IConstantesRepository;
import es.iesjandula.reaktor.notifications_server.repository.IUsuarioRepository;
import es.iesjandula.reaktor.notifications_server.utils.Constants;
import es.iesjandula.reaktor.notifications_server.utils.NotificationsServerException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class InicializacionNotificationsServer
{
    
	private IConstantesRepository constantesRepository;

    @Value("${" + Constants.PARAM_YAML_MAX_EMAIL + "}")
    private int maxEmails;

    @Value("${" + Constants.PARAM_YAML_MAX_WEB + "}")
    private int maxWeb;

    @Value("${" + Constants.PARAM_YAML_MAX_CALENDAR + "}")
    private int maxCalendar;

    @PostConstruct
	public void inicializarSistema() throws BaseException, NotificationsServerException
	{
		// Esta es la carpeta con las subcarpetas y configuraciones
		ResourcesHandler bookingServerConfig = this.getResourcesHandler(Constants.NOTFICATIONS_SERVER_CONFIG);

		if (bookingServerConfig != null)
		{
			// Nombre de la carpeta destino
			File bookingServerConfigExec = new File(Constants.NOTIFICATIONS_SERVER_CONFIG_EXEC);

			// Copiamos las plantillas (origen) al destino
			bookingServerConfig.copyToDirectory(bookingServerConfigExec);
		}

	}
    
    /**
	 * 
	 * @param resourceFilePath con la carpeta origen que tiene las plantillas
	 * @return el manejador que crea la estructura
	 */
	private ResourcesHandler getResourcesHandler(String resourceFilePath)
	{
		ResourcesHandler outcome = null;

		URL baseDirSubfolderUrl = Thread.currentThread().getContextClassLoader().getResource(resourceFilePath);
		if (baseDirSubfolderUrl != null)
		{
			if (baseDirSubfolderUrl.getProtocol().equalsIgnoreCase("file"))
			{
				outcome = new ResourcesHandlerFile(baseDirSubfolderUrl);
			}
			else
			{
				outcome = new ResourcesHandlerJar(baseDirSubfolderUrl);
			}
		}

		return outcome;
	}
	
	/**
	 * @param reader reader
	 * @throws BookingError excepción mientras se cerraba el reader
	 */
	private void cerrarFlujo(BufferedReader reader) throws NotificationsServerException
	{
		if (reader != null)
		{
			try
			{
				// Cierre del reader
				reader.close();
			}
			catch (IOException ioException)
			{
				String errorString = "IOException mientras se cerraba el reader";

				log.error(errorString, ioException);
				throw new NotificationsServerException(Constants.ERR_CODE_CIERRE_READER, errorString, ioException);
			}
		}
	}
	
	/**
	 * Este método se encarga de inicializar el sistema con las constantes siempre
	 * que estemos creando la base de datos ya sea en el entorno de desarrollo o
	 * ejecutando JAR
	 */
	private void inicializarSistemaConConstantes()
	{
		// Borramos las constantes
		this.constantesRepository.deleteAll();

		// Cargamos las constantes
		this.cargarPropiedad(Constants.TABLA_CONST_NOTIFICATIONS_MAX_EMAIL, this.maxEmails);
		this.cargarPropiedad(Constants.TABLA_CONST_NOTIFICATIONS_MAX_WEB, this.maxWeb);
		this.cargarPropiedad(Constants.TABLA_CONST_NOTIFICATIONS_MAX_CALENDAR, this.maxCalendar);
	}
	
	/**
	 * @param key   clave
	 * @param value valor
	 */
	private void cargarPropiedad(String key, int value)
	{
		// Verificamos si tiene algún valor
		Optional<ConstantesNotificaciones> property = this.constantesRepository.findById(key);

		// Si está vacío, lo seteamos con el valor del YAML
		if (property.isEmpty())
		{
			ConstantesNotificaciones constante = new ConstantesNotificaciones();

			constante.setClave(key);
			constante.setValor(value);

			// Almacenamos la constante en BBDD
			this.constantesRepository.save(constante);
		}
	}
}
