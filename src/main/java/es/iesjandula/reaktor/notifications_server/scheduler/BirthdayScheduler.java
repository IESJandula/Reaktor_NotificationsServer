package es.iesjandula.reaktor.notifications_server.scheduler;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import es.iesjandula.reaktor.notifications_server.utils.Constants;
import es.iesjandula.reaktor.base.security.models.DtoUsuarioBase;
import es.iesjandula.reaktor.base.utils.FechasUtils;
import es.iesjandula.reaktor.base_client.dtos.NotificationEmailDto;
import es.iesjandula.reaktor.base_client.requests.notificationes.RequestNotificacionesEnviarEmail;
import es.iesjandula.reaktor.base_client.requests.notificationes.RequestNotificacionesEnviarWeb;
import es.iesjandula.reaktor.base_client.dtos.NotificationWebDto;
import es.iesjandula.reaktor.base_client.utils.BaseClientException;
import es.iesjandula.reaktor.base_client.utils.BaseClientConstants;
import es.iesjandula.reaktor.base_client.requests.firebase.RequestFirebaseObtenerUsuarios;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BirthdayScheduler 
{
    @Autowired
    private RequestFirebaseObtenerUsuarios requestFirebaseObtenerUsuarios;

    @Autowired
    private RequestNotificacionesEnviarEmail requestNotificacionesEnviarEmail;

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
    
            // Obtenemos la información de los usuarios
            List<DtoUsuarioBase> dtoUsuariosBase = this.requestFirebaseObtenerUsuarios.obtenerUsuarios();
    
            // Si hay usuarios, recorremos la lista y actualizamos el atributo greetings
            if (!dtoUsuariosBase.isEmpty())
            {
                // Recorremos la lista de usuarios
                for (DtoUsuarioBase dtoUsuarioBase : dtoUsuariosBase) 
                {
                    // Obtenemos la fecha de nacimiento del usuario
                    String fechaNacimiento = dtoUsuarioBase.getFechaNacimiento();
                    if (fechaNacimiento != null)
                    {
                        // Validar fecha de cumpleaños
                        boolean hoyEsCumple = this.validarFechaDeCumpleaños(mesActual, diaActual, fechaNacimiento) ;

                        if (hoyEsCumple)
                        {
                            this.enviarNotificacionCumple(diaActual, mesActual, anioActual, dtoUsuarioBase) ;
                        }
                    }
                }
    
                // Logueamos
                log.info("Notificaciones de cumpleaños enviadas correctamente");
            }
            else
            {
                log.info("No hay usuarios este día para enviar notificaciones de cumpleaños");
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
    private boolean validarFechaDeCumpleaños(int mesActual, int diaActual, String fechaNacimiento)
    {
        // Obtenemos la información de la fecha de nacimiento del usuario
        LocalDate fechaNacimientoLocal = LocalDate.parse(fechaNacimiento);

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
     * @param dtoUsuarioBase - El DTO del usuario que cumple años
     * @throws BaseClientException - Si hay un error al enviar la notificación
     */
    private void enviarNotificacionCumple(int diaActual, int mesActual, int anioActual, DtoUsuarioBase dtoUsuarioBase) throws BaseClientException
    {
        // Creamos el DTO de la notificación email
        NotificationEmailDto notificationEmailDto = new NotificationEmailDto();

        // Creamos el texto de cumpleaños
        String textoCumple = "¡Muchas felicidades, " + dtoUsuarioBase.getNombre() + " " + dtoUsuarioBase.getApellidos() + " por tu cumple!";

        // Seteamos los datos de la notificación email
        notificationEmailDto.setTo(List.of(dtoUsuarioBase.getEmail()));
        notificationEmailDto.setSubject("Felicitaciones por tu cumple");
        notificationEmailDto.setBody(textoCumple);

        // Lo notificamos por correo
        this.requestNotificacionesEnviarEmail.enviarNotificacionEmail(notificationEmailDto);

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
        this.requestNotificacionesEnviarWeb.enviarNotificacionWeb(notificationWebDto);   
    }
}
