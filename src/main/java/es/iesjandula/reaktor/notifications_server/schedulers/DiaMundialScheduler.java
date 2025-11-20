package es.iesjandula.reaktor.notifications_server.schedulers;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import es.iesjandula.reaktor.base.utils.FechasUtils;
import es.iesjandula.reaktor.base_client.dtos.NotificationWebDto;
import es.iesjandula.reaktor.base_client.requests.notificaciones.RequestNotificacionesEnviarWeb;
import es.iesjandula.reaktor.base_client.utils.BaseClientConstants;
import es.iesjandula.reaktor.base_client.utils.BaseClientException;
import es.iesjandula.reaktor.notifications_server.repository.IDiaMundialRepository;
import es.iesjandula.reaktor.notifications_server.utils.Constants;
import es.iesjandula.reaktor.notifications_server.models.DiaMundial;
import es.iesjandula.reaktor.notifications_server.models.ids.DiaMundialId;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DiaMundialScheduler
{
    @Autowired
    private IDiaMundialRepository diaMundialRepository;

    @Autowired
    private RequestNotificacionesEnviarWeb requestNotificacionesEnviarWeb;

    @Scheduled(cron = Constants.CRON_EXPRESSION, zone = "Europe/Madrid")
    public void notificarDiaMundial()
    {
        try
        {
            // Obtenemos la información de la fecha actual
            LocalDate fechaActual = LocalDate.now();
    
            int mesActual  = fechaActual.getMonthValue() ;
            int diaActual  = fechaActual.getDayOfMonth() ;
            int anioActual = fechaActual.getYear() ;
    
            // Creamos una instancia del ID del dia mundial
            DiaMundialId diaMundialId = new DiaMundialId();
            diaMundialId.setDia(diaActual);
            diaMundialId.setMes(mesActual);

            // Obtenemos el dia mundial
            Optional<DiaMundial> optionalDiaMundial = this.diaMundialRepository.findById(diaMundialId);
    
            // Si no hay dia mundial, logueamos
            if (!optionalDiaMundial.isPresent())
            {
                // Logueamos
                log.info("No hay dia mundial para notificar: " + diaActual + "/" + mesActual);
            }
            else
            {
                // Obtenemos el dia mundial
                DiaMundial diaMundial = optionalDiaMundial.get();

                // Si hay dia mundial, enviamos la notificación siempre que haya usuarios con ese nombre
                this.enviarNotificacionDiaMundial(diaActual, mesActual, anioActual, diaMundial) ;
            }
        }
        catch (BaseClientException baseClientException)
        {
            log.error("Error al enviar las notificaciones de dia mundial: " + baseClientException.getMessage(), baseClientException);
        }
    }

    /**
     * Método - Enviar notificación de dia mundial
     * @param diaActual - El día actual
     * @param mesActual - El mes actual
     * @param anioActual - El año actual
     * @param diaMundial - El dia mundial a notificar
     * @throws BaseClientException - Si hay un error al enviar la notificación
     */
    private void enviarNotificacionDiaMundial(int diaActual, int mesActual, int anioActual, DiaMundial diaMundial) throws BaseClientException
    {
        // Creamos el texto de dia mundial
        String diaMundialTexto = "Hoy es el " + diaMundial.getNombre() ;
        
        // Creamos el DTO de la notificación web
        NotificationWebDto notificationWebDto = new NotificationWebDto();

        // Seteamos el texto de la notificación web
        notificationWebDto.setTexto(diaMundialTexto);

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
