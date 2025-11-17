package es.iesjandula.reaktor.notifications_server.config;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.notifications_server.models.Aplicacion;
import es.iesjandula.reaktor.notifications_server.models.Usuario;
import es.iesjandula.reaktor.notifications_server.repository.IAplicacionRepository;
import es.iesjandula.reaktor.notifications_server.repository.IUsuarioRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class InicializacionNotificationsServer
{
    @Autowired
    private IAplicacionRepository aplicacionRepository;

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Value("${reaktor.notifications.max.email:10}")
    private int maxEmails;

    @Value("${reaktor.notifications.max.web:10}")
    private int maxWeb;

    @Value("${reaktor.notifications.max.calendar:10}")
    private int maxCalendar;

    @PostConstruct
    public void inicializarSistema()
    {
        log.info("Inicialización del sistema de NotificationsServer iniciada...");

        this.inicializarAplicacionesPorDefecto();
        this.inicializarUsuariosPorDefecto();

        log.info("Inicialización del sistema de NotificationsServer completada.");
    }

    private void inicializarAplicacionesPorDefecto()
    {
        // Aquí puedes agregar las aplicaciones que quieras tener por defecto
        String[] appsPorDefecto = { "AppNotificaciones", "AppCalendario", "AppWeb" };

        for (String appNombre : appsPorDefecto)
        {
            Optional<Aplicacion> appOptional = this.aplicacionRepository.findById(appNombre);
            if (appOptional.isEmpty())
            {
                Aplicacion aplicacion = new Aplicacion();
                aplicacion.setNombre(appNombre);

                // Inicializamos contadores y límites desde el YAML
                aplicacion.setNotifMaxEmail(this.maxEmails);
                aplicacion.setNotifMaxWeb(this.maxWeb);
                aplicacion.setNotifMaxCalendar(this.maxCalendar);

                aplicacion.setNotifHoyEmail(0);
                aplicacion.setNotifHoyWeb(0);
                aplicacion.setNotifHoyCalendar(0);

                this.aplicacionRepository.save(aplicacion);
                log.info("Aplicación '{}' creada por defecto", appNombre);
            }
        }
    }

    private void inicializarUsuariosPorDefecto()
    {
        // Puedes inicializar usuarios de prueba o de administración por defecto
        String[][] usuariosPorDefecto = {
            { "admin@reaktor.com", "Admin", "Reaktor" },
            { "usuario@reaktor.com", "Usuario", "Test" }
        };

        for (String[] usuarioData : usuariosPorDefecto)
        {
            String email = usuarioData[0];
            Optional<Usuario> usuarioOptional = this.usuarioRepository.findById(email);
            if (usuarioOptional.isEmpty())
            {
                Usuario usuario = new Usuario();
                usuario.setEmail(email);
                usuario.setNombre(usuarioData[1]);
                usuario.setApellidos(usuarioData[2]);

                // Valores opcionales
                usuario.setDepartamento("Administración");

                this.usuarioRepository.save(usuario);
                log.info("Usuario '{}' creado por defecto", email);
            }
        }
    }
}
