/**
 * 
 */
package kr.co.aim.messolution.generic.errorHandler;

import java.text.MessageFormat;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.object.ErrorDef;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author sjlee
 *
 */
public class CustomException extends Exception 
{
	
	/**
	 * @uml.property  name="errorDef"
	 * @uml.associationEnd  
	 */
	public ErrorDef  errorDef;
	
	private static Log 	log = LogFactory.getLog(CustomException.class);

	/**
	 * 
	 */
	public CustomException() {
		// TODO Auto-generated constructor stub
	}

	public CustomException(String errorCode, Object... args)
	{
		ErrorDef tempErrorDef = GenericServiceProxy.getErrorDefMap().getErrorDef(errorCode);
		
		if ( tempErrorDef == null )
		{
			//tempErrorDef = GenericServiceProxy.getErrorDefMap().getErrorDef("UndefinedCode");
			
			tempErrorDef = new ErrorDef();
			tempErrorDef.setErrorCode("UndefinedCode");
			tempErrorDef.setCha_errorMessage("");
			tempErrorDef.setEng_errorMessage("");
			tempErrorDef.setKor_errorMessage("");
			tempErrorDef.setLoc_errorMessage("");
		}
		
		errorDef = new ErrorDef();
		{
			//initialize object
			errorDef.setErrorCode(tempErrorDef.getErrorCode());
			errorDef.setCha_errorMessage("");
			errorDef.setEng_errorMessage("");
			errorDef.setKor_errorMessage("");
			errorDef.setLoc_errorMessage("");
		}
		
		String korTempMsg = tempErrorDef.getKor_errorMessage();
		String engTempMsg = tempErrorDef.getEng_errorMessage();
		String chaTempMsg = tempErrorDef.getCha_errorMessage();
		String locTempMsg = tempErrorDef.getLoc_errorMessage();
		
		errorDef.setKor_errorMessage(
				MessageFormat.format(korTempMsg, args));
		errorDef.setEng_errorMessage(
				MessageFormat.format(engTempMsg, args));
		errorDef.setCha_errorMessage(
				MessageFormat.format(chaTempMsg, args));
		errorDef.setLoc_errorMessage(
				MessageFormat.format(locTempMsg, args));
		
		//log.error("errorDescription = " + errorDef.getEng_errorMessage());
		if(log.isErrorEnabled())
		{
			log.error(String.format("[%s]%s", errorDef.getErrorCode(), errorDef.getLoc_errorMessage()));
		}		
	}
	/**
	 * @param errorCode
	 */
	public CustomException(String errorCode) {
		
		//if(log.isInfoEnabled()){
		//	log.info("errorCode = " + errorCode);
		//}
		
		ErrorDef tempErrorDef = GenericServiceProxy.getErrorDefMap().getErrorDef(errorCode);
		
		if ( tempErrorDef == null )
		{
			//tempErrorDef = GenericServiceProxy.getErrorDefMap().getErrorDef("UndefinedCode");
			
			tempErrorDef = new ErrorDef();
			tempErrorDef.setErrorCode("UndefinedCode");
		}
		
		errorDef = new ErrorDef();
		errorDef.setErrorCode(tempErrorDef.getErrorCode());
		
		String korTempMsg = tempErrorDef.getKor_errorMessage();
		String engTempMsg = tempErrorDef.getEng_errorMessage();
		String chaTempMsg = tempErrorDef.getCha_errorMessage();
		String locTempMsg = tempErrorDef.getLoc_errorMessage();
		
		errorDef.setKor_errorMessage(korTempMsg);
		errorDef.setEng_errorMessage(engTempMsg);
		errorDef.setCha_errorMessage(chaTempMsg);
		errorDef.setLoc_errorMessage(locTempMsg);
		//super(message);
		
		if(log.isErrorEnabled())
		{
			log.error(String.format("[%s]%s", errorDef.getErrorCode(), errorDef.getLoc_errorMessage()));
		}
	}

	/**
	 * @param cause
	 */
	public CustomException(Throwable cause) {
		//super(cause);
		
		//if (log.isDebugEnabled())
		//	cause.printStackTrace();
		
		if (cause instanceof CustomException)
			return;
		
		errorDef = new ErrorDef();
		errorDef.setErrorCode("UndefinedCode");
		
		if (cause.getStackTrace() != null)
		{
			for (StackTraceElement element : cause.getStackTrace())
			{
				StringBuilder comment = new StringBuilder(cause.getMessage()).append(" ").append(element.toString());
				
				errorDef.setKor_errorMessage(comment.toString());
				errorDef.setEng_errorMessage(comment.toString());
				errorDef.setCha_errorMessage(comment.toString());
				errorDef.setLoc_errorMessage(comment.toString());

				break;
			}
		}
		else
		{
			errorDef.setKor_errorMessage("");
			errorDef.setEng_errorMessage("");
			errorDef.setCha_errorMessage("");
			errorDef.setLoc_errorMessage(cause.getMessage());
		}
		
		if(log.isErrorEnabled())
		{
			log.error(String.format("[%s]%s", errorDef.getErrorCode(), errorDef.getLoc_errorMessage()));
		}
	}

	/**
	 * no longer in use
	 * @param message
	 * @param cause
	 */
	//public CustomException(String message, Throwable cause) {
	//	super(message, cause);
	//	
	//	if (log.isDebugEnabled())
	//		cause.printStackTrace();
	//}

}
