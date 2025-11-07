package es.iesjandula.reaktor.notifications_server.scheduler;

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
public class BirthdayScheduler 
{
    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Madrid")
    public void greetings()
    {
        Date fechaActual = new Date();

        List<Usuario> usuarios = this.usuarioRepository.findAll();

        for (Usuario u : usuarios) 
        {
            Date fechaNac = u.getFechaNacimiento();
            if (fechaNac != null)
            {
                if (fechaNac.getMonth() == fechaActual.getMonth() &&
                    fechaNac.getDate() == fechaActual.getDate())
                {
                    u.setGreetings(true);
                } 
                else 
                {
                    u.setGreetings(false);
                }
            }
        }

        this.usuarioRepository.saveAll(usuarios);
        log.info("Greetings actualizados correctamente");
    }
}
