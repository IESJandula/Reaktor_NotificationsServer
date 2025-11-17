package es.iesjandula.reaktor.notifications_server.scheduler;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import es.iesjandula.reaktor.notifications_server.models.Usuario;
import es.iesjandula.reaktor.notifications_server.repository.IUsuarioRepository;
import es.iesjandula.reaktor.base_client.requests.NotificationEmailSender;
import es.iesjandula.reaktor.base_client.utils.BaseClientException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FelicitarScheduler 
{
    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private NotificationEmailSender notificationEmailSender;

    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Madrid")
    public void felicitar()
    {
        try
        {

            // Obtenemos la información de la fecha actual
            Calendar calendarActual = Calendar.getInstance();
            calendarActual.setTime(new Date());
    
            int mesActual = calendarActual.get(Calendar.MONTH);
            int diaActual = calendarActual.get(Calendar.DAY_OF_MONTH);
    
            // Obtenemos la información de los usuarios
            List<Usuario> usuarios = this.usuarioRepository.findAll();
    
            // Si hay usuarios, recorremos la lista y actualizamos el atributo greetings
            if (!usuarios.isEmpty())
            {
                // Recorremos la lista de usuarios
                for (Usuario usuario : usuarios) 
                {
                    // Obtenemos la fecha de nacimiento del usuario
                    Date fechaNacimiento = usuario.getFechaNacimiento();
                    if (fechaNacimiento != null)
                    {
                        // Obtenemos la información de la fecha de nacimiento del usuario
                        Calendar calendarNacimiento = Calendar.getInstance();
                        calendarNacimiento.setTime(fechaNacimiento);
    
                        int mesNacimiento = calendarNacimiento.get(Calendar.MONTH);
                        int diaNacimiento = calendarNacimiento.get(Calendar.DAY_OF_MONTH);
    
                        // Lo notificamos al endpoint de SendEmailController
                        this.notificationEmailSender.send(usuario.getEmail(), "Felicitaciones por tu cumple", "Hoy es el cumpleaños de: " + usuario.getNombre() + " " + usuario.getApellidos());
                    }
                }
    
                // Guardamos los usuarios actualizados
                this.usuarioRepository.saveAll(usuarios);
    
                // Logueamos
                log.info("Felicitaciones actualizadas correctamente");
            }
            else
            {
                log.info("No hay usuarios este día para felicitar");
            }
        }
        catch (BaseClientException baseClientException)
        {
            log.error("Error al enviar el email: " + baseClientException.getMessage(), baseClientException);
        }
    }
}
