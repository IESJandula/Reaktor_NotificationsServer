package es.iesjandula.reaktor.notifications_server.utils;

/**
 * @author Francisco Manuel Benítez Chico
 */
public class Constants
{
	public static final String STRING_COMA = "," ;
	
	public static final String NIVEL_GLOBAL = "GLOBAL" ;
	
	public static final String NIVEL_SECUNDARIO = "SECUNDARIO" ;
	
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
