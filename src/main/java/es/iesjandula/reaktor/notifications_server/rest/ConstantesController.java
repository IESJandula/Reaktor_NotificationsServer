package es.iesjandula.reaktor.notifications_server.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.notifications_server.dtos.DtoConstante;
import es.iesjandula.reaktor.notifications_server.models.Constante;
import es.iesjandula.reaktor.notifications_server.repository.IConstanteRepository;
import es.iesjandula.reaktor.notifications_server.utils.Constants;
import es.iesjandula.reaktor.notifications_server.utils.NotificationsServerException;
import lombok.extern.log4j.Log4j2;

@RequestMapping(value = "/notifications/constants")
@RestController
@Log4j2
public class ConstantesController 
{
	@Autowired
	private IConstanteRepository constanteRepository;

	@PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> actualizarConstantes()
	{
		try
		{
			List<DtoConstante> dtoConstanteList = this.constanteRepository.encontrarTodoComoDto();

			return ResponseEntity.ok(dtoConstanteList);
		}
		catch (Exception exception)
		{

			NotificationsServerException notificationsServerException = 
			        new NotificationsServerException(Constants.ERR_CONSTANTE_NO_ENCONTRADA,
											"Excepción genérica al obtener las constantes", exception);

			log.error("Excepción genérica al obtener las constantes", notificationsServerException);
			return ResponseEntity.status(500).body(notificationsServerException.getBodyExceptionMessage());
		}
	}

	@PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> actualizarConstantes(@RequestBody(required = true) List<DtoConstante> dtoConstanteList)
	{
		try
		{
			for (DtoConstante dtoConstante : dtoConstanteList)
			{
				Constante constantes = new Constante(dtoConstante.getClave(), dtoConstante.getValor());

				this.constanteRepository.saveAndFlush(constantes);
			}

			return ResponseEntity.ok().build();
		}
		catch (Exception exception)
		{
			NotificationsServerException notificationsServerException = 
					new NotificationsServerException(Constants.ERR_CONSTANTE_NO_ENCONTRADA,
											"Excepción genérica al actualizar las constantes", exception);

			log.error("Excepción genérica al actualizar las constantes", notificationsServerException);
			return ResponseEntity.status(500).body(notificationsServerException.getBodyExceptionMessage());
		}
	}
	
}
