package kr.co.aim.messolution.lot.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DefectRuleSetting;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class ModifyDefectRuleSetting extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);
		String defectCode = SMessageUtil.getBodyItemValue(doc, "DEFECTCODE", true);
		String size = SMessageUtil.getBodyItemValue(doc, "SIZE", true);
		String count = SMessageUtil.getBodyItemValue(doc, "COUNT", true);
		
		String holdFlag = SMessageUtil.getBodyItemValue(doc, "HOLDFLAG", true);
		String mailFlag = SMessageUtil.getBodyItemValue(doc, "MAILFLAG", true);
		String userGroupName = SMessageUtil.getBodyItemValue(doc, "USERGROUPNAME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyDefectRuleSetting", this.getEventUser(), this.getEventComment(), "", "");
		
			
		DefectRuleSetting defectRuleSetting = null;
		
		try
		{
			defectRuleSetting = ExtendedObjectProxy.getDefectRuleSettingService().selectByKey(false, new Object[] {factoryName,productSpecName, productSpecVersion, processOperationName  , processOperationVersion  , defectCode });
		}
		catch (Exception ex)
		{
			defectRuleSetting = null;
		}
		
		if(defectRuleSetting == null)
		{
			throw new CustomException("IDLE-0006", "");
		}
		

		try{
			defectRuleSetting.setDefectSize(Integer.parseInt(size));
			defectRuleSetting.setDefectCount(Integer.parseInt(count));
		}catch(Exception e){
			throw new CustomException("IDLE-0005", "");
		}
		defectRuleSetting.setHoldFlag(holdFlag);
		defectRuleSetting.setMailFlag(mailFlag);
		defectRuleSetting.setUserGroupName(userGroupName);
		defectRuleSetting.setLastEventComment(eventInfo.getEventComment());
		defectRuleSetting.setLastEventName(eventInfo.getEventName());
		defectRuleSetting.setLastEventTime(eventInfo.getEventTime());
		defectRuleSetting.setLastEventTimeKey(eventInfo.getEventTimeKey());
		defectRuleSetting.setLastEventUser(eventInfo.getEventUser());
		
		
		ExtendedObjectProxy.getDefectRuleSettingService().modify(eventInfo, defectRuleSetting);
		
		return doc;
	}
}
