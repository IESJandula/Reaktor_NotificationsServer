package es.iesjandula.reaktor.notifications_server.scheduler;

import java.util.Date;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import es.iesjandula.reaktor.notifications_server.models.Usuario;
import es.iesjandula.reaktor.notifications_server.repository.IUsuarioRepository;
import es.iesjandula.reaktor.notifications_server.utils.Constants;
import es.iesjandula.reaktor.base.utils.FechasUtils;
import es.iesjandula.reaktor.base_client.dtos.NotificationEmailDto;
import es.iesjandula.reaktor.base_client.requests.NotificationEmailSender;
import es.iesjandula.reaktor.base_client.requests.NotificationWebSender;
import es.iesjandula.reaktor.base_client.dtos.NotificationWebDto;
import es.iesjandula.reaktor.base_client.utils.BaseClientException;
import es.iesjandula.reaktor.base_client.utils.BaseClientConstants;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BirthdayScheduler 
{
    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private NotificationEmailSender notificationEmailSender;

    @Autowired
    private NotificationWebSender notificationWebSender;

    @Scheduled(cron = Constants.CRON_FELICITACION, zone = "Europe/Madrid")
    public void felicitar()
    {
        try
        {
            // Obtenemos la información de la fecha actual
            LocalDate fechaActual = LocalDate.now();

            int mesActual  = fechaActual.getMonthValue() ;
            int diaActual  = fechaActual.getDayOfMonth() ;
            int anioActual = fechaActual.getYear() ;
    
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
                        // Validar fecha de cumpleaños
                        boolean hoyEsCumple = this.validarFechaDeCumpleaños(mesActual, diaActual, fechaNacimiento) ;

                        if (hoyEsCumple)
                        {
                            this.enviarNotificacionCumple(diaActual, mesActual, anioActual, usuario) ;
                        }
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
            log.error("Error al enviar las notificaciones de cumpleaños: " + baseClientException.getMessage(), baseClientException);
        }
    }

    /**
     * Método - Validar fecha de cumpleaños
     * @param mesActual - El mes actual
     * @param diaActual - El día actual
     * @param fechaNacimiento - La fecha de nacimiento del usuario
     * @return boolean - true si es cumpleaños, false en caso contrario
     */
    private boolean validarFechaDeCumpleaños(int mesActual, int diaActual, Date fechaNacimiento)
    {
        // Obtenemos la información de la fecha de nacimiento del usuario
        LocalDate fechaNacimientoLocal = fechaNacimiento.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        int mesNacimiento = fechaNacimientoLocal.getMonthValue() ;
        int diaNacimiento = fechaNacimientoLocal.getDayOfMonth() ;

        // Si el mes y el día de nacimiento son iguales al mes y día actual, es cumpleaños
        return mesNacimiento == mesActual && diaNacimiento == diaActual;
    }

    /**
     * Método - Enviar notificación de cumpleaños
     * @param diaActual - El día actual
     * @param mesActual - El mes actual
     * @param anioActual - El año actual
     * @param usuario - El usuario que cumple años
     * @throws BaseClientException - Si hay un error al enviar la notificación
     */
    private void enviarNotificacionCumple(int diaActual, int mesActual, int anioActual, Usuario usuario) throws BaseClientException
    {
        // Creamos el DTO de la notificación email
        NotificationEmailDto notificationEmailDto = new NotificationEmailDto();

        // Creamos el texto de cumpleaños
        String textoCumple = "¡Muchas felicidades, " + usuario.getNombre() + " " + usuario.getApellidos() + " por tu cumple!";

        // Seteamos los datos de la notificación email
        notificationEmailDto.setTo(List.of(usuario.getEmail()));
        notificationEmailDto.setSubject("Felicitaciones por tu cumple");
        notificationEmailDto.setBody(textoCumple);

        // Lo notificamos por correo
        this.notificationEmailSender.enviarNotificacionEmail(notificationEmailDto);

        // Creamos el DTO de la notificación web
        NotificationWebDto notificationWebDto = new NotificationWebDto();

        // Seteamos el texto de la notificación web
        notificationWebDto.setTexto(textoCumple);

        // Seteamos la fecha de inicio y fin de la notificación web

        // La fecha de inicio es hoy a las 00:00:00
        LocalDateTime fechaInicio = LocalDateTime.of(anioActual, mesActual, diaActual, 0, 0, 0);
        // La fecha de fin es hoy a las 23:59:59
        LocalDateTime fechaFin    = LocalDateTime.of(anioActual, mesActual, diaActual, 23, 59, 59);

        notificationWebDto.setFechaInicio(FechasUtils.convertirFecha(fechaInicio));
        notificationWebDto.setHoraInicio(FechasUtils.convertirHora(fechaInicio));
        notificationWebDto.setFechaFin(FechasUtils.convertirFecha(fechaFin));
        notificationWebDto.setHoraFin(FechasUtils.convertirHora(fechaFin));

        // Seteamos el receptor de la notificación web
        notificationWebDto.setReceptor(BaseClientConstants.RECEPTOR_NOTIFICACION_CLAUSTRO);

        // Seteamos el tipo de notificación web
        notificationWebDto.setTipo(BaseClientConstants.TIPO_NOTIFICACION_SOLO_TEXTO);

        // Lo notificamos por web
        this.notificationWebSender.enviarNotificacionWeb(notificationWebDto);   
    }
}
