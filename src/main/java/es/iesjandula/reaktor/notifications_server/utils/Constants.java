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
	
	// Carga de datos
	public static final int ERR_CODE_CIERRE_READER = 202;
	
	/*********************************************************/
	/******************* Ficheros y carpetas *****************/
	/*********************************************************/
	
	/** Nombre de la carpeta de configuracion */
	public static final String NOTFICATIONS_SERVER_CONFIG = "notifications_server_config";
	
	/** Nombre de la carpeta de configuracion al ejecutarse */
	public static final String NOTIFICATIONS_SERVER_CONFIG_EXEC = "notifications_server_config_exec";


	/*********************************************************/
	/**************** Propiedades Gmail **********************/
	/*********************************************************/

	/** Propiedades Gmail - Application Name */
	public static final String GMAIL_APPLICATION_NAME = "Reaktor-FirebaseServer" ;
	
	/*********************************************************/
	/******************** Tabla Constantes *******************/
	/*********************************************************/

	/** Constante - NotificacionesMax email */
	public static final String TABLA_CONST_NOTIFICATIONS_MAX_EMAIL = "Notificaciones máximas email";

	/** Constante - NotificacionesMax web */
	public static final String TABLA_CONST_NOTIFICATIONS_MAX_WEB = "Notificaciones máximas web";

	/** Constante - NotificacionesMax calendar */
	public static final String TABLA_CONST_NOTIFICATIONS_MAX_CALENDAR = "Notificaciones máximas calendar";
	
	/*********************************************************/
	/******************* Parámetros YAML *********************/
	/*********************************************************/
	
	/** Constante - Parámetros YAML - NotificacionesMax Email */
	public static final String PARAM_YAML_MAX_EMAIL = "reaktor.constantes.notificationsMaxEmail";
	
	/** Constante - Parámetros YAML - NotificacionesMax Web */
	public static final String PARAM_YAML_MAX_WEB = "reaktor.constantes.notificationsMaxWeb";
	
	/** Constante - Parámetros YAML - NotificacionesMax Web */
	public static final String PARAM_YAML_MAX_CALENDAR = "reaktor.constantes.notificationsMaxCalendar";
}
