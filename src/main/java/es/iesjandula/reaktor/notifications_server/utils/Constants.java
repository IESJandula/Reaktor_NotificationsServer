package es.iesjandula.reaktor.notifications_server.utils;

import java.util.Arrays;
import java.util.List;

/**
 * @author Francisco Manuel Benítez Chico
 */
public class Constants
{
	public static final String STRING_COMA = "," ;
	
	/** Tipo de notificación - Solo texto */
	public static final String TIPO_NOTIFICACION_SOLO_TEXTO     = "Solo texto" ;
	
	/** Tipo de notificación - Texto e imagen */
	public static final String TIPO_NOTIFICACION_TEXTO_E_IMAGEN = "Texto e imagen" ;

	/** Lista de tipos de notificación */
	public static final List<String> TIPOS_NOTIFICACIONES = Arrays.asList(TIPO_NOTIFICACION_SOLO_TEXTO) ; //, TIPO_NOTIFICACION_TEXTO_E_IMAGEN) ;
	
	/*********************************************************/
	/*********************** Errores *************************/
	/*********************************************************/
	
	/** Error - Excepción genérica - Código */
	public static final int ERR_GENERIC_EXCEPTION_CODE 			  = 100 ;
	
	/** Error - Excepción genérica - Mensaje */
	public static final String ERR_GENERIC_EXCEPTION_MSG 		  = "Excepción genérica en " ;
	
	/** Error - Error en la creación de la notificación web */
	public static final int ERR_NOTIFICATIONS_WEB_CREATION		  = 101 ;
	
	/** Error - Error en el cambio de estado de la notificación web */
	public static final int ERR_NOTIFICATIONS_WEB_CHANGE_STATE	  = 102 ;

	/*********************************************************/
	/****************** Propiedades YAML *********************/
	/*********************************************************/

	/** Propiedades YAML - Notificaciones - Calendar */
	public static final String REAKTOR_NOTIFICATIONES_MAX_CALENDAR = "reaktor.notifications.max.calendar" ;

	/** Propiedades YAML - Notificaciones - Email */
	public static final String REAKTOR_NOTIFICATIONES_MAX_EMAIL    = "reaktor.notifications.max.email" ;

	/** Propiedades YAML - Notificaciones - Web */
	public static final String REAKTOR_NOTIFICATIONS_MAX_WEB       = "reaktor.notifications.max.web" ;
}
