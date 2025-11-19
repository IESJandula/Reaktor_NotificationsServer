package es.iesjandula.reaktor.notifications_server.utils;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * @author Francisco Manuel Benítez Chico
 */
public class Constants
{
	public static final String STRING_COMA = "," ;
	
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
	
	/** Error - Constante no encontrada */
	public final static int ERR_CONSTANTE_NO_ENCONTRADA           = 103 ;

	/** Error - Error en el procesamiento del santoral */
	public static final int ERR_CODE_PROCESANDO_SANTORAL          = 104 ;

	/** Error - Error en el cierre del santoral */	
	public static final int ERR_CODE_CIERRE_SANTORAL              = 105 ;

	/*********************************************************/
	/****************** Propiedades YAML *********************/
	/*********************************************************/

	/** Propiedades YAML - Constantes - Max Emails */
	public static final String PARAM_YAML_NOTIFICACIONES_MAX_EMAILS    = "reaktor.constantes.notificationsMaxEmail" ;

	/** Propiedades YAML - Constantes - Max Web */
	public static final String PARAM_YAML_NOTIFICACIONES_MAX_WEB       = "reaktor.constantes.notificationsMaxWeb" ;

	/** Propiedades YAML - Constantes - Max Calendar */
	public static final String PARAM_YAML_NOTIFICACIONES_MAX_CALENDAR  = "reaktor.constantes.notificationsMaxCalendar" ;


	/*********************************************************/
	/******************** Tabla Constantes *******************/
	/*********************************************************/

	/** Constante - Max Emails */
	public static final String TABLA_CONST_NOTIFICACIONES_MAX_EMAILS   = "Máximo número de notificaciones por email al día";

	/** Constante - Max Web */
	public static final String TABLA_CONST_NOTIFICACIONES_MAX_WEB      = "Máximo número de notificaciones por web al día";

	/** Constante - Max Calendar */
	public static final String TABLA_CONST_NOTIFICACIONES_MAX_CALENDAR = "Máximo número de notificaciones por calendar al día";

	/*********************************************************/
	/******************* Ficheros y carpetas *****************/
	/*********************************************************/

	/** Nombre de la carpeta de configuracion */
	public static final String NOTIFICATIONS_SERVER_CONFIG      = "notifications_server_config";

	/** Nombre de la carpeta de configuracion al ejecutarse */
	public static final String NOTIFICATIONS_SERVER_CONFIG_EXEC = "notifications_server_config_exec";

	/*********************************************************/
	/******************* Ficheros CSV ***********************/
	/*********************************************************/

	/** Fichero CSV - Santorales */
	public static final String FICHERO_SANTORALES = NOTIFICATIONS_SERVER_CONFIG_EXEC + File.separator + "santoral.csv";

	/*********************************************************/
	/******************* Cronos ******************************/
	/*********************************************************/
	
	/** Crono - Felicitacion */
	public static final String CRON_FELICITACION = "10 27 21 * * *";


	/*********************************************************/
	/******************* Google ******************************/
	/*********************************************************/

	/** Google - Directorio de tokens */
	public static final String GOOGLE_TOKEN_DIRECTORY_PATH = NOTIFICATIONS_SERVER_CONFIG_EXEC + File.separator + "tokens";

	/** Google - ID del usuario */
	public static final String GOOGLE_USER_ID = "user";

	/** Google - Scopes */
	public static final List<String> GOOGLE_SCOPES = Collections.singletonList("https://www.googleapis.com/auth/gmail.send");

}
