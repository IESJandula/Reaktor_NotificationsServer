package es.iesjandula.reaktor.notifications_server.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.base.security.models.DtoUsuarioBase;
import es.iesjandula.reaktor.base.security.models.DtoUsuarioExtended;
import es.iesjandula.reaktor.base_client.requests.firebase.RequestFirebaseObtenerUsuarios;
import es.iesjandula.reaktor.base_client.utils.BaseClientException;
import es.iesjandula.reaktor.notifications_server.repository.IUsuarioRepository;
import es.iesjandula.reaktor.notifications_server.utils.Constants;
import es.iesjandula.reaktor.notifications_server.utils.NotificationsServerException;
import jakarta.annotation.PostConstruct;
import es.iesjandula.reaktor.notifications_server.models.Constante;
import es.iesjandula.reaktor.notifications_server.models.Usuario;
import lombok.extern.slf4j.Slf4j;

/**
 * Clase que gestiona los usuarios de la aplicación
 */
@Service
@Slf4j
public class UsersService
{
	/** Servicio de constantes de la aplicación */
	@Autowired
	private ConstantesService constantesService ;

	/** Servicio de obtención de usuarios de FirebaseServer */
	@Autowired
	private RequestFirebaseObtenerUsuarios requestFirebaseObtenerUsuarios ;

	/** Repositorio de usuarios de la base de datos */
	@Autowired
	private IUsuarioRepository usuarioRepository ;

	/**
	 * Método para inicializar la base de datos con los usuarios de FirebaseServer
	 * @throws BaseClientException Excepción de base client
	 * @throws NotificationsServerException Excepción mientras consulta las constantes de la base de datos
	 */
	@PostConstruct
	public void init() throws BaseClientException, NotificationsServerException
	{
		// Nos traemos de FirebaseServer todos los usuarios
		List<DtoUsuarioBase> usuarios = this.requestFirebaseObtenerUsuarios.obtenerUsuarios();

		// Para cada usuario ...
		for (DtoUsuarioBase usuario : usuarios)
		{
			// ... creamos el usuario en la base de datos
			this.crearUsuarioEnBBDD(usuario);
		}
	}

    /**
	 * Método auxiliar para obtener el usuario de la base de datos
	 * @param usuario Usuario extendido
	 * @return Usuario de la base de datos
	 * @throws NotificationsServerException Excepción de notificaciones web
	 */
	public Usuario obtenerUsuario(DtoUsuarioExtended usuario) throws NotificationsServerException
	{
		// Creamos variable de usuario
		Usuario usuarioDatabase = null;

		// Buscamos si existe el usuario, sino lo creamos
		Optional<Usuario> usuarioDatabaseOptional = this.usuarioRepository.findById(usuario.getEmail()) ;

		// Si no existe el usuario ...
		if (usuarioDatabaseOptional.isEmpty())
		{
            // ... creamos el usuario en la base de datos
            this.crearUsuarioEnBBDD(usuario);
		}
		else
		{
			// ... obtenemos el usuario de la base de datos
			usuarioDatabase = usuarioDatabaseOptional.get();
		}

		return usuarioDatabase;
	}

    /**
     * Método auxiliar para crear un usuario en la base de datos
     * @param usuarioDto DTO de usuario base
     * @throws NotificationsServerException Excepción de notificaciones web
     */
    private void crearUsuarioEnBBDD(DtoUsuarioBase usuarioDto) throws NotificationsServerException
    {
        // Creamos una nueva instancia de usuario
        Usuario usuarioDatabase = new Usuario() ;

        // Seteamos los atributos del usuario
        usuarioDatabase.setEmail(usuarioDto.getEmail());
        usuarioDatabase.setNombre(usuarioDto.getNombre());
        usuarioDatabase.setApellidos(usuarioDto.getApellidos());
        usuarioDatabase.setRolesList(usuarioDto.getRoles());

        // Asociamos los máximos de notificaciones
        this.asociarMaximosNotificaciones(usuarioDatabase);

        // Guardamos el usuario en la base de datos
        this.usuarioRepository.saveAndFlush(usuarioDatabase);
    }

	/**
	 * Método auxiliar para asociar los máximos de notificaciones a un usuario
	 * @param usuario Usuario
	 * @throws NotificationsServerException Excepción de notificaciones web
	 */
	private void asociarMaximosNotificaciones(Usuario usuario) throws NotificationsServerException
	{
		// Inicializamos el número de notificaciones web hoy
		usuario.setNotifMaxWeb(0);

		// Obtenemos la constante de notificaciones máximas de tipo web
		Constante constanteNotificacionesMaxWeb = this.constantesService.obtenerConstante(Constants.TABLA_CONST_NOTIFICACIONES_MAX_WEB) ;
		
		// Inicializamos el número de notificaciones máximas
		usuario.setNotifMaxWeb(Integer.parseInt(constanteNotificacionesMaxWeb.getValor())) ;
	}

	/**
	 * Método auxiliar para actualizar un usuario al enviar una notificación web
	 * @param usuario Usuario
	 * @throws NotificationsServerException Excepción de notificaciones web
	 */
	public void usuarioHaEnviadoNotificacionWeb(Usuario usuario) throws NotificationsServerException
	{
		// Incrementamos el número de notificaciones web del usuario
		usuario.setNotifHoyWeb(usuario.getNotifHoyWeb() + 1);

		// Seteamos la fecha de la última notificación web
		usuario.setFechaUltimaNotificacionWeb(LocalDateTime.now()) ;

		// Guardamos el usuario en la base de datos
		this.usuarioRepository.saveAndFlush(usuario);
	}

	/**
	 * Método auxiliar para actualizar un usuario al eliminar una notificación web
	 * @param usuario Usuario
	 * @throws NotificationsServerException Excepción de notificaciones web
	 */
	public void usuarioHaEliminadoNotificacionWeb(Usuario usuario) throws NotificationsServerException
	{
		// Decrementamos el número de notificaciones web del usuario
		usuario.setNotifHoyWeb(usuario.getNotifHoyWeb() - 1);

		// Guardamos el usuario en la base de datos
		this.usuarioRepository.saveAndFlush(usuario);
	}

    /**
     * Método auxiliar para obtener un usuario por email
     *
     * @param email - Email del usuario
     * @return Usuario
     * @throws NotificationsServerException - Si el usuario no existe
     */
    public Usuario obtenerUsuarioPorEmail(String email) throws NotificationsServerException
    {
        // Buscamos el usuario por email
        Optional<Usuario> usuarioOptional = this.usuarioRepository.findByEmail(email);

        // Si no existe el usuario ...
        if (usuarioOptional.isEmpty())
        {
            String errorMessage = "Usuario no encontrado: " + email;
            log.error(errorMessage);
            throw new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, null);
        }

        // ... obtenemos el usuario de la base de datos
        return usuarioOptional.get();
    }
}
