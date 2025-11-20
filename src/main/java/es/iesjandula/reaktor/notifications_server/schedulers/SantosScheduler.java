package es.iesjandula.reaktor.notifications_server.schedulers;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import es.iesjandula.reaktor.base.utils.FechasUtils;
import es.iesjandula.reaktor.base.security.models;
import es.iesjandula.reaktor.base_client.dtos.NotificationWebDto;
import es.iesjandula.reaktor.base_client.requests.notificaciones.RequestNotificacionesEnviarWeb;
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

    @Autowired
    private RequestFirebaseObtenerUsuarios requestFirebaseObtenerUsuarios;

    @Scheduled(cron = Constants.CRON_EXPRESSION, zone = "Europe/Madrid")
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
    
            // Si no hay santoral, logueamos
            if (!optionalSantoral.isPresent())
            {
                // Logueamos
                log.info("No hay santoral para notificar: " + diaActual + "/" + mesActual);
            }
            else
            {
                // Si hay santoral, enviamos la notificación siempre que haya usuarios con ese nombre
                
                // Obtenemos el santoral
                Santoral santoral = optionalSantoral.get();

                // Consultamos los usuarios del sistema
                List<DtoUsuarioBase> dtoUsuariosBase = this.requestFirebaseObtenerUsuarios.obtenerUsuarios();

                // Si no hay usuarios, logueamos
                if (dtoUsuariosBase.isEmpty())
                {
                    // Logueamos
                    log.info("No hay usuarios para enviar notificaciones de santos: " + santoral.getNombre());
                }
                // Si al menos hay un usuario que tenga ese nombre, enviamos la notificación
                else if (this.algunUsuarioConEseNombre(santoral.getNombre(), dtoUsuariosBase))
                {
                    // Enviamos la notificación
                    this.enviarNotificacionSantoral(diaActual, mesActual, anioActual, santoral) ;
                }
                else
                {
                    // Logueamos
                    log.info("No se encontró ningún usuario para enviar notificaciones de santos: " + santoral.getNombre());
                }
            }
        }
        catch (BaseClientException baseClientException)
        {
            log.error("Error al enviar las notificaciones de santos: " + baseClientException.getMessage(), baseClientException);
        }
    }

    /**
     * Método - Comprueba si al menos hay un usuario que tenga ese nombre
     * @param nombre - El nombre a buscar
     * @param dtoUsuariosBase - La lista de usuarios
     * @return - True si hay al menos un usuario que tenga ese nombre, false en caso contrario
     */
    private boolean algunUsuarioConEseNombre(String nombre, List<DtoUsuarioBase> dtoUsuariosBase)
    {
        // Inicializamos el resultado a false
        boolean encontrado = false;

        // Recorremos la lista
        int indice = 0;
        while (indice < dtoUsuariosBase.size() && !encontrado)
        {
            // Comprobamos
            encontrado = dtoUsuariosBase.get(indice).getNombre().contains(nombre);

            // Incrementamos el índice
            indice++;
        }

        // Devolvemos el resultado
        return encontrado;
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
        String felicitacionSanto = "¡Hoy es " + santoral.getMasculinoFemenino() + " " + santoral.getNombre() + "! ¡Muchas felicidades a  quien le toque!";
        
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
