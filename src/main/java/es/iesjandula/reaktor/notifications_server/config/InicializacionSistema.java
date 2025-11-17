package es.iesjandula.reaktor.notifications_server.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import es.iesjandula.reaktor.base.resources_handler.ResourcesHandler;
import es.iesjandula.reaktor.base.resources_handler.ResourcesHandlerFile;
import es.iesjandula.reaktor.base.resources_handler.ResourcesHandlerJar;
import es.iesjandula.reaktor.base.utils.BaseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import es.iesjandula.reaktor.notifications_server.models.Constante;
import es.iesjandula.reaktor.notifications_server.models.Santoral;
import es.iesjandula.reaktor.notifications_server.models.ids.SantoralId;
import es.iesjandula.reaktor.notifications_server.repository.IConstanteRepository;
import es.iesjandula.reaktor.notifications_server.repository.ISantoralRepository;
import es.iesjandula.reaktor.notifications_server.utils.Constants;
import es.iesjandula.reaktor.notifications_server.utils.NotificationsServerException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class InicializacionSistema
{
	@Autowired
	private IConstanteRepository constanteRepository;

    @Autowired
    private ISantoralRepository santoralRepository;

    @Value("${reaktor.reiniciarParametros}")
	private boolean reiniciarParametros;

	@Value("${" + Constants.PARAM_YAML_NOTIFICACIONES_MAX_EMAILS + "}")
	private String maxNotificacionesEmailDiaria;

	@Value("${" + Constants.PARAM_YAML_NOTIFICACIONES_MAX_WEB + "}")
	private String maxNotificacionesWebDiaria;

	@Value("${" + Constants.PARAM_YAML_NOTIFICACIONES_MAX_CALENDAR + "}")
	private String maxNotificacionesCalendarDiaria  ;

	/**
	 * Este método se encarga de inicializar el sistema ya sea en el entorno de
	 * desarrollo o ejecutando JAR
	 * 
	 * @throws BaseException con un error
	 * @throws NotificationsServerException excepción mientras se inicializaba el santoral
	 */
	@PostConstruct
	public void inicializarSistema() throws BaseException, NotificationsServerException
	{
		// Esta es la carpeta con las subcarpetas y configuraciones
		ResourcesHandler notificationsServerConfig = this.getResourcesHandler(Constants.NOTIFICATIONS_SERVER_CONFIG);

		if (notificationsServerConfig != null)
		{
			// Nombre de la carpeta destino
			File notificationsServerConfigExec = new File(Constants.NOTIFICATIONS_SERVER_CONFIG_EXEC);

			// Copiamos las plantillas (origen) al destino
			notificationsServerConfig.copyToDirectory(notificationsServerConfigExec);
		}

        if (this.reiniciarParametros)
		{
            // Inicializamos el santoral
            this.inicializarSantoral();

            // Inicializamos el sistema con las constantes
            this.inicializarSistemaConConstantes();
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
	 * Este método se encarga de inicializar el sistema con las constantes siempre
	 * que estemos creando la base de datos ya sea en el entorno de desarrollo o
	 * ejecutando JAR
	 */
	private void inicializarSistemaConConstantes()
	{
		// Borramos las constantes
		this.constanteRepository.deleteAll();

		// Cargamos las constantes
		this.cargarPropiedad(Constants.TABLA_CONST_NOTIFICACIONES_MAX_EMAILS, this.maxNotificacionesEmailDiaria);
		this.cargarPropiedad(Constants.TABLA_CONST_NOTIFICACIONES_MAX_WEB, this.maxNotificacionesWebDiaria);
		this.cargarPropiedad(Constants.TABLA_CONST_NOTIFICACIONES_MAX_CALENDAR, this.maxNotificacionesCalendarDiaria);
	}

	/**
	 * Este método se encarga de inicializar el sistema con los santorales siempre
	 * que estemos creando la base de datos ya sea en el entorno de desarrollo o
	 * ejecutando JAR
	 * 
	 * @throws NotificationsServerException excepción mientras se inicializaba el santoral
	 */
	private void inicializarSantoral() throws NotificationsServerException
	{
		// Borramos los santorales
		this.santoralRepository.deleteAll();

        // Cargamos los santorales desde el CSV
        this.cargarSantoralesDesdeCSVInternal();
	}

	/**
	 * Carga santorales desde CSV - Internal
	 * 
	 * @throws NotificationsServerException excepción mientras se leían los santorales
	 */
	private void cargarSantoralesDesdeCSVInternal() throws NotificationsServerException
	{
		// Inicializamos la lista de santorales
		List<Santoral> santorales = new ArrayList<Santoral>();

		BufferedReader bufferedReader = null;

		try
		{
			// Leer el archivo CSV desde la carpeta de recursos
			bufferedReader = new BufferedReader(new FileReader(ResourceUtils.getFile(Constants.FICHERO_SANTORALES), Charset.forName("UTF-8")));

			// Nos saltamos la primera línea
			bufferedReader.readLine();

			// Leemos la segunda línea que ya tiene datos
			String linea = bufferedReader.readLine();

			while (linea != null)
			{
				// Leemos la línea y la spliteamos
				String[] valores = linea.split(",");

                // Obtenemos los valores
                Integer dia   = Integer.parseInt(valores[0]);
                Integer mes   = Integer.parseInt(valores[1]);
                String nombre = valores[2];

				Santoral santoral = new Santoral();

                // Creamos el ID del santoral
                SantoralId santoralId = new SantoralId();
                santoralId.setDia(dia);
                santoralId.setMes(mes);

                // Seteamos el ID y el nombre del santoral
				santoral.setSantoralId(santoralId);
				santoral.setNombre(nombre);

				// Añadimos a la lista
				santorales.add(santoral);

                // Leemos la siguiente línea
                linea = bufferedReader.readLine();
			}
		}
		catch (IOException ioException)
		{
			String errorString = "IOException mientras se leía línea de santoral";
			log.error(errorString, ioException);
			throw new NotificationsServerException(Constants.ERR_CODE_PROCESANDO_SANTORAL, errorString, ioException);
		}
		finally
		{
			this.cerrarFlujo(bufferedReader);
		}

		// Guardamos los santorales en la base de datos
		if (!santorales.isEmpty())
		{
			this.santoralRepository.saveAll(santorales);
		}   
	}

	/**
	 * @param bufferedReader bufferedReader
	 * @throws NotificationsServerException excepción mientras se cerraba el santoral
	 */
	private void cerrarFlujo(BufferedReader bufferedReader) throws NotificationsServerException
	{
		if (bufferedReader != null)
		{
			try
			{
				// Cierre del bufferedReader
				bufferedReader.close();
			}
			catch (IOException ioException)
			{
				String errorString = "IOException mientras se cerraba el santoral";

				log.error(errorString, ioException);
				throw new NotificationsServerException(Constants.ERR_CODE_CIERRE_SANTORAL, errorString, ioException);
			}
		}
	}

	/**
	 * Método auxiliar para cargar una propiedad en la base de datos
	 * @param key   clave
	 * @param value valor
	 */
	private void cargarPropiedad(String key, String value)
	{
		// Verificamos si tiene algún valor
		Optional<Constante> property = this.constanteRepository.findById(key);

		// Si está vacío, lo seteamos con el valor del YAML
		if (property.isEmpty())
		{
			Constante constante = new Constante();

			constante.setClave(key);
			constante.setValor(value);

			// Almacenamos la constante en BBDD
			this.constanteRepository.save(constante);
		}
	}
}
