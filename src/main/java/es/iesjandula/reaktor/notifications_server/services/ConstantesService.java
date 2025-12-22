package es.iesjandula.reaktor.notifications_server.services;


import org.springframework.beans.factory.annotation.Autowired;

import es.iesjandula.reaktor.notifications_server.repository.IConstanteRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Optional;

import es.iesjandula.reaktor.notifications_server.models.Constante;
import es.iesjandula.reaktor.notifications_server.utils.NotificationsServerException;
import es.iesjandula.reaktor.notifications_server.utils.Constants;

/**
 * Clase que gestiona las constantes de la aplicación
 */
@Service
@Slf4j
public class ConstantesService
{
	/** Repositorio de constantes de la base de datos */
    @Autowired
    private IConstanteRepository constanteRepository ;

    /**
	 * Método auxiliar para obtener una constante de la base de datos
	 * @param clave Clave de la constante
	 * @return Constante de la base de datos
	 * @throws NotificationsServerException Excepción de notificaciones web
	 */
	public Constante obtenerConstante(String clave) throws NotificationsServerException
	{
		// Obtenemos la constante
		Optional<Constante> optionalConstante = this.constanteRepository.findById(clave) ;

		// Si no existe la constante, lanzamos una excepción
		if (!optionalConstante.isPresent())
		{
			String errorMessage = "Constante no encontrada: " + clave;

			log.error(errorMessage);
			throw new NotificationsServerException(Constants.ERR_CONSTANTE_NO_ENCONTRADA, errorMessage);
		}

		// Obtenemos la constante
		return optionalConstante.get() ;
	}
}
