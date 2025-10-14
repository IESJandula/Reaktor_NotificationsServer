package es.iesjandula.reaktor.notifications_server.rest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.notifications_server.dtos.NotificacionesWebHoyDto;
import es.iesjandula.reaktor.notifications_server.models.Aplicacion;
import es.iesjandula.reaktor.notifications_server.models.NotificacionWeb;
import es.iesjandula.reaktor.notifications_server.repository.IAplicacionRepository;
import es.iesjandula.reaktor.notifications_server.repository.INotificacionWebRepository;
import es.iesjandula.reaktor.notifications_server.utils.Constants;
import es.iesjandula.reaktor.notifications_server.utils.NotificationsServerException;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/notifications_web")
@Slf4j
public class NotificationsWebController 
{
	
	@Autowired
	private IAplicacionRepository aplicacionRepository ;
	
	@Autowired
	private INotificacionWebRepository notificacionWebRepository ;

	@RequestMapping(method = RequestMethod.POST, value = "/crearNotificacionWeb")
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
	public ResponseEntity<?> crearNotificacionWeb(
	        @RequestHeader("client_id") String clientId,
	        @RequestHeader("nombre") String nombre,
	        @RequestHeader("texto") String texto,
	        @RequestHeader("fecha_inicio") String fechaInicio,
	        @RequestHeader("hora_inicio") String horaInicio,
	        @RequestHeader("fecha_fin") String fechaFin,
	        @RequestHeader("hora_fin") String horaFin,
	        @RequestHeader("roles") String roles,
	        @RequestHeader("nivel") String nivel)
	{

	    try 
	    {
	        Aplicacion aplicacion = aplicacionRepository.findByClientIdAndNombre(clientId, nombre);
	        if (aplicacion == null) 
	        {
	            String errorMessage = "Aplicación no encontrada con ese client_id y nombre";
	            log.error(errorMessage);
	            throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_CREATION, errorMessage);
	        }

	        // Validar nivel
	        if (!nivel.equalsIgnoreCase(Constants.NIVEL_GLOBAL) && !nivel.equalsIgnoreCase(Constants.NIVEL_SECUNDARIO)) 
	        {
	            String errorMessage = "Nivel inválido. Solo se permiten: " 
	                                  + Constants.NIVEL_GLOBAL + " o " 
	                                  + Constants.NIVEL_SECUNDARIO;
	            
				log.error(errorMessage);
	            throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_CREATION, errorMessage);
	        }
	        
	        if (nivel.equalsIgnoreCase(Constants.NIVEL_SECUNDARIO) && texto != null && texto.contains("[Imagen:")) 
	        {
	        	String errorMessage = "No se permite adjuntar imagen en notificaciones de nivel SECUNDARIO" ;
	        	log.error(errorMessage) ;
	        	throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_CREATION, errorMessage) ;
			}

			// Validar los roles
			if (roles == null || roles.isEmpty())
			{
				String errorMessage = "Roles inválidos. Nulos o vacíos" ;
				
				log.error(errorMessage);
				throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_CREATION, errorMessage);
			}
			else
			{
				boolean roleEncontrado = roles.contains(BaseConstants.ROLE_ADMINISTRADOR) ||
				                         roles.contains(BaseConstants.ROLE_DIRECCION)     ||
										 roles.contains(BaseConstants.ROLE_PROFESOR) ;

				if (!roleEncontrado)
				{
					String errorMessage = "Roles inválidos. Solo se permiten: " 
	                                  + BaseConstants.ROLE_ADMINISTRADOR + " o " 
	                                  + BaseConstants.ROLE_DIRECCION     + " o " 
	                                  + BaseConstants.ROLE_PROFESOR ;

					log.error(errorMessage);
					throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_CREATION, errorMessage);
				}
			}

	        NotificacionWeb notificacionWeb = new NotificacionWeb();
	        notificacionWeb.setAplicacion(aplicacion);
	        notificacionWeb.setFechaCreacion(LocalDate.now());
	        notificacionWeb.setFechaInicio(LocalDate.parse(fechaInicio));
	        notificacionWeb.setHoraInicio(LocalTime.parse(horaInicio));
	        notificacionWeb.setFechaFin(LocalDate.parse(fechaFin));
	        notificacionWeb.setHoraFin(LocalTime.parse(horaFin));
	        notificacionWeb.setRoles(roles);
	        notificacionWeb.setTexto(texto);
	        notificacionWeb.setNivel(nivel.toUpperCase()); 


	        notificacionWebRepository.saveAndFlush(notificacionWeb);

	        return ResponseEntity.status(200).build() ;

	    }
        catch (NotificationsServerException firebaseServerServerException)
        {
			return ResponseEntity.status(400).body(firebaseServerServerException.getBodyExceptionMessage()) ;
        }
	    catch (Exception exception) 
	    {
	        String errorMessage = "Error al crear la notificación web";
	        log.error(errorMessage, exception);
	        
			NotificationsServerException NotificationsServerException =  new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception);
	        return ResponseEntity.status(500).body(NotificationsServerException.getBodyExceptionMessage());
	    }
	}

	
	@RequestMapping(method = RequestMethod.GET, value = "/obtenerNotificacionesHoy")
	public ResponseEntity<?> obtenerNotificacionHoy(@RequestHeader("usuario") String usuario, @RequestHeader("nivel") String nivel)
	{
	    List<NotificacionesWebHoyDto> resultado = new ArrayList<NotificacionesWebHoyDto>();
		try 
		{
			LocalDate hoy = LocalDate.now() ;
			
			List<NotificacionWeb> notificaciones = notificacionWebRepository.findByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(hoy, hoy) ;
			
			if (notificaciones != null && !notificaciones.isEmpty())
			{
	        	resultado = notificaciones.stream()
	        						      .filter(n -> n.getNivel().equalsIgnoreCase(nivel))
					                      .map(n -> new NotificacionesWebHoyDto(
											   n.getId(),
											   n.getTexto(),
											   n.getNivel(),
											   n.getFechaInicio(),
											   n.getHoraInicio(),
											   n.getFechaFin(),
											   n.getHoraFin(),
											   String.join(",", n.getRoles())
							))
							.collect(Collectors.toList()) ;
			}

			return ResponseEntity.status(200).body(resultado) ;			
		}
		catch (Exception exception) 
		{			
			String errorMessage = "Error inesperado al obtener las notificaciones" ;
			log.error(errorMessage, exception) ;

			NotificationsServerException NotificationsServerException = new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception) ;
			return ResponseEntity.status(500).body(NotificationsServerException.getBodyExceptionMessage()) ;
		
		}
		
	}
	
	// Método auxiliar para obtener nombre de imagen si está dentro del texto
    private String extraerNombreImagen(String texto) 
    {
        if (texto != null && texto.contains("[Imagen:")) 
        {
            return texto.substring(texto.indexOf("[Imagen:") + 8, texto.indexOf("]")).trim();
        }
        return null;
    }
    
    @RequestMapping(method = RequestMethod.DELETE, value = "/eliminarNotificacionWeb/{id}")
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
    public ResponseEntity<?> eliminarNotificacionWeb(@PathVariable("id") Long id) 
    {
        try 
        {
            // Buscar la notificación
            NotificacionWeb notificacion = notificacionWebRepository.findById(id).orElse(null);

            if (notificacion == null) 
            {
                String errorMessage = "No se encontró la notificación con id: " + id;
                log.error(errorMessage);

                throw new NotificationsServerException(Constants.ERR_NOTIFICATIONS_WEB_DELETION, errorMessage);
            }

            // Eliminar
            notificacionWebRepository.delete(notificacion);

            return ResponseEntity.status(200).build() ;
        } 
		catch (NotificationsServerException NotificationsServerException)
		{
			return ResponseEntity.status(400).body(NotificationsServerException.getBodyExceptionMessage()) ;
		}
        catch (Exception exception) 
        {
            String errorMessage = "Error al eliminar la notificación con id: " + id;
            log.error(errorMessage, exception);

            NotificationsServerException NotificationsServerException = new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception);
            return ResponseEntity.status(500).body(NotificationsServerException.getBodyExceptionMessage());
        }
    }
}
