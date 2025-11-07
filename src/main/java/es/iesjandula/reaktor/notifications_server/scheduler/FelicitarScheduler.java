package es.iesjandula.reaktor.notifications_server.scheduler;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import es.iesjandula.reaktor.notifications_server.models.Usuario;
import es.iesjandula.reaktor.notifications_server.repository.IUsuarioRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FelicitarScheduler 
{
    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Madrid")
    public void felicitar()
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

                    // Actualizamos el atributo felicitar del usuario
                    usuario.setFelicitar(mesNacimiento == mesActual && diaNacimiento == diaActual);
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
}
