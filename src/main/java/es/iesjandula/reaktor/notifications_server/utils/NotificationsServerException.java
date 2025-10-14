package es.iesjandula.reaktor.notifications_server.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * @author Francisco Manuel Benítez Chico
 */
public class NotificationsServerException extends Exception
{
	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 8350802787825747372L ;
	
	/** Codigo de error para la excepcion */
	private int code ;
	
	/** Mensaje de error para la excepcion */
	private String message ;
	
	/** Excepcion de error para la excepcion */
	private Exception exception ;
	
	/**
	 * @param code código del error
	 * @param message mensaje del error
	 */
	public NotificationsServerException(int code, String message) 
	{
		super(message);
		
		this.code 	 = code ;
		this.message = message ;
	}
	/**
	 * 
	 * @param code código del error
	 * @param message mensaje del error
	 * @param exception excepcion del error
	 */
	public NotificationsServerException(int code, String message, Exception exception) 
	{
		super(message,exception) ;
		
		this.code 	   = code ;
		this.message   = message ;
		this.exception = exception ;
	}
	
	/**
	 * mapa para devolver la excepcion
	 * @return mapa con mensaje, error y en caso de tener excepcion tambien devuelve la excepcion
	 */
	public Map<String,String> getBodyExceptionMessage()
	{
		Map<String,String> messageMap = new HashMap<String,String>() ;
		
		messageMap.put("code", String.valueOf(this.code)) ;
		messageMap.put("message", this.message) ;
		
		if (this.exception != null)
		{
			String stackTrace = ExceptionUtils.getStackTrace(this.exception) ;
			messageMap.put("exception", stackTrace) ;
		}
		
		return messageMap ;
	}
}
