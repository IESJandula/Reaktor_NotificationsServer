package es.iesjandula.reaktor.notifications_server.scheduler;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import es.iesjandula.reaktor.base.utils.FechasUtils;
import es.iesjandula.reaktor.base_client.dtos.NotificationWebDto;
import es.iesjandula.reaktor.base_client.requests.notificationes.RequestNotificacionesEnviarWeb;
import es.iesjandula.reaktor.base_client.utils.BaseClientConstants;
import es.iesjandula.reaktor.base_client.utils.BaseClientException;
import es.iesjandula.reaktor.notifications_server.repository.ISantoralRepository;
import es.iesjandula.reaktor.notifications_server.utils.Constants;
import es.iesjandula.reaktor.notifications_server.models.Santoral;
import es.iesjandula.reaktor.notifications_server.models.ids.SantoralId;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SantosScheduler
{
    @Autowired
    private ISantoralRepository santoralRepository;

    @Autowired
    private RequestNotificacionesEnviarWeb requestNotificacionesEnviarWeb;

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
    
            // Creamos una instancia del ID del santoral
            SantoralId santoralId = new SantoralId();
            santoralId.setDia(diaActual);
            santoralId.setMes(mesActual);

            // Obtenemos el santoral
            Optional<Santoral> optionalSantoral = this.santoralRepository.findById(santoralId);
    
            // Si hay santoral, enviamos la notificación
            if (optionalSantoral.isPresent())
            {
                // Obtenemos el santoral
                Santoral santoral = optionalSantoral.get();

                // Enviamos la notificación
                this.enviarNotificacionSantoral(diaActual, mesActual, anioActual, santoral) ;
            }
        }
        catch (BaseClientException baseClientException)
        {
            log.error("Error al enviar las notificaciones de santos: " + baseClientException.getMessage(), baseClientException);
        }
    }

    /**
     * Método - Enviar notificación de felicitación por el santoral
     * @param diaActual - El día actual
     * @param mesActual - El mes actual
     * @param anioActual - El año actual
     * @param santoral - El santoral a notificar
     * @throws BaseClientException - Si hay un error al enviar la notificación
     */
    private void enviarNotificacionSantoral(int diaActual, int mesActual, int anioActual, Santoral santoral) throws BaseClientException
    {
        // Creamos el texto de felicitación por el santoral
        String felicitacionSanto = "¡Hoy es el santo de " + santoral.getNombre() + "! ¡Muchas felicidades!";
        
        // Creamos el DTO de la notificación web
        NotificationWebDto notificationWebDto = new NotificationWebDto();

        // Seteamos el texto de la notificación web
        notificationWebDto.setTexto(felicitacionSanto);

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
        this.requestNotificacionesEnviarWeb.enviarNotificacionWeb(notificationWebDto);   
    }
}
