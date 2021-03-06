package kr.co.aim.messolution.userprofile.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.userprofile.MESUserServiceProxy;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.ExceptionKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;
import org.jdom.Element;

public class CheckPassword extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String userId = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag + "/", doc, "USERID");
		String password = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag + "/", doc, "PASSWORD");
		String workStationName = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag + "/", doc, "WORKSTATIONNAME");
		

		try
		{
			MESUserServiceProxy.getUserProfileServiceImpl().verifyPassword(userId, password);
		}
		catch (CustomException ce )
		{
			throw ce;
		}
		catch (Exception e) 
		{
			throw new CustomException("COMMON-0001");
		}
		
		//update version

		
		Element returnElement = MESUserServiceProxy.getUserProfileServiceUtil().createUserProfileElement(userId, password);
		
		//replace for reply
		doc.getRootElement().getChild(SMessageUtil.Body_Tag).setContent(returnElement);
		
		return doc;
	}
	
	/**
	 * after log-in record UI version
	 * @author swcho
	 * @since 2015.05.08
	 * @param userId
	 * @param uiName
	 * @param workStationName
	 * @param version
	 * @throws CustomException
	 */
	private void executePostAction(String userId, String uiName, String workStationName, String version)
		throws CustomException
	{
		try
		{
			String sql = "UPDATE UserLoggedIn SET LOGGEDINUIVERSION = ? WHERE userId = ? AND uiName = ? AND workStationName = ?";
			
			Object[] bindList = new Object[] {version, userId, uiName, workStationName};
			
//			kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().update(sql, bindList);
			kr.co.aim.messolution.generic.GenericServiceProxy.getSqlMesTemplate().update(sql, bindList);
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("SYS-9999", "LogIn", de.getMessage());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "LogIn", fe.getMessage());
		}
	}
	
	/**
	 * old log-in information is not important
	 * @author swcho
	 * @since 2015.05.14
	 * @param userName
	 * @param uiName
	 * @throws CustomException
	 */
	private void purgeGarbageLoggedIn(String userName, String uiName)
		throws CustomException
	{
		String sql = "DELETE UserLoggedIn WHERE userId = ? AND uiName = ?";
		
		Object[] bindArray = new Object[] {userName, uiName};
		
		try
		{
//			GenericServiceProxy.getSqlMesTemplate().update(sql, bindArray);
			kr.co.aim.messolution.generic.GenericServiceProxy.getSqlMesTemplate().update(sql, bindArray);
		}
		catch (Exception ex)
		{
			eventLog.error(ex);
		}
	}
}
