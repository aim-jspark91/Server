package kr.co.aim.messolution.recipe.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.userprofile.MESUserServiceProxy;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.jdom.Document;
import org.jdom.Element;

public class RecipePermissionVerifyRequest extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {

        String machineName = StringUtil.EMPTY;
        String unitName = StringUtil.EMPTY;
        String subunitName = StringUtil.EMPTY;
        String userName = StringUtil.EMPTY;
        String userPassword = StringUtil.EMPTY;
		Machine machineData = null;
		Machine unitData = null;
		Machine subunitData = null;
		
		String factoryCode = StringUtil.EMPTY;

		/* 20181101, hhlee, add, Set Reply MessageName by DEFAULT_FACTORY ==>> */
		if(GenericServiceProxy.getConstantMap().DEFAULT_FACTORY.equals("MOD"))
        {
          factoryCode = "M_";
        }
        else if(GenericServiceProxy.getConstantMap().DEFAULT_FACTORY.equals("OLED"))
        {
          factoryCode = "E_";
        }
        else
        {
          factoryCode = "A_";
        }
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", factoryCode+"RecipePermissionVerifyReply");
        SMessageUtil.setHeaderItemValue(doc, "EVENTCOMMENT", "RecipePermissionVerifyReply");
        /* <<== 20181101, hhlee, add, Set Reply MessageName by DEFAULT_FACTORY */
		
		machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		subunitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		userName = SMessageUtil.getBodyItemValue(doc, "USERNAME", true);
		userPassword = SMessageUtil.getBodyItemValue(doc, "USERPASSWORD", true);
		
		/* Machine Validation */
		machineData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		/* 20181101, hhlee, delete, When SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true) is Error, send messagename is A_RecipePermissionVerifyRequest ==>> */
		//if(machineData.getFactoryName().equals("MOD"))
		//{
		//	factoryCode = "M_";
		//}
		//else if(machineData.getFactoryName().equals("OLED"))
		//{
		//	factoryCode = "E_";
		//}
		//else
		//{
		//	factoryCode = "A_";
		//}
        //
		//SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", factoryCode+"RecipePermissionVerifyReply");
		//SMessageUtil.setHeaderItemValue(doc, "EVENTCOMMENT", "RecipePermissionVerifyReply");
		/* <<== 20181101, hhlee, delete, When SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true) is Error, send messagename is A_RecipePermissionVerifyRequest */
		
		Element bodyElement = SMessageUtil.getBodyElement(doc);
		bodyElement.removeChild("USERPASSWORD");

		SMessageUtil.setBodyItemValue(doc, "RESULT", "OK",true);
		SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "",true);

		if(!StringUtil.isEmpty(unitName))
        {
		    /* Unit Validation */
		    unitData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
        }
		
		if(!StringUtil.isEmpty(subunitName))
		{
			/* SubUnit Validation */
			subunitData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(subunitName);
		}

		/* User Authentication */
		try
		{
			MESUserServiceProxy.getUserProfileServiceImpl().verifyUser(userName);
		}
		catch (Exception ex)
		{
			//throw new CustomException("USER-0001", userName);
			throw new CustomException("USER-0014", userName);
			//eventLog.warn(String.format("[%s]%s", ce.errorDef.getErrorCode(), ce.errorDef.getLoc_errorMessage()));

		}

		/* User, Password Authentication */
		/*if ( !(UserServiceProxy.getUserProfileService().verifyPassword(userId, password)))
        {
        	throw new CustomException("USER-0002", userId, password);
        }*/
		try
		{
			MESUserServiceProxy.getUserProfileServiceImpl().verifyPassword(userName, userPassword);
		}
		catch (Exception ex)
		{
			//throw new CustomException("USER-0002", userName, userPassword);
			throw new CustomException("USER-0010", userName, userPassword);
			//eventLog.warn(String.format("[%s]%s", ce.errorDef.getErrorCode(), ce.errorDef.getLoc_errorMessage()));
		}
		return doc;
	}
}
