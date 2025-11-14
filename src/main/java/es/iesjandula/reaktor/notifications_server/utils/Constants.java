package es.iesjandula.reaktor.notifications_server.utils;

/**
 * @author Francisco Manuel Benítez Chico
 */
public class Constants
{
	public static final String STRING_COMA = "," ;
	
	public static final String NIVEL_GLOBAL = "Global" ;
	
	public static final String NIVEL_SECUNDARIO = "Secundario" ;
	
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
	
	public final static int CONSTANTE_NO_ENCONTRADA = 50;

	/*********************************************************/
	/****************** Propiedades YAML *********************/
	/*********************************************************/

	/** Propiedades YAML - Notificaciones - Calendar */
	public static final String NOTIFICATIONS_MAX_CALENDAR = "reaktor.constantes.notificationsMaxCalendar" ;

	/** Propiedades YAML - Notificaciones - Email */
	public static final String NOTIFICATIONS_MAX_EMAIL    = "reaktor.constantes.notificationsMaxEmail" ;

	/** Propiedades YAML - Notificaciones - Web */
	public static final String NOTIFICATIONS_MAX_WEB      = "reaktor.constantes.notificationsMaxWeb" ;


	/*********************************************************/
	/**************** Propiedades Gmail **********************/
	/*********************************************************/

	/** Propiedades Gmail - Application Name */
	public static final String GMAIL_APPLICATION_NAME = "Reaktor-FirebaseServer" ;
}
