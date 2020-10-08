package kr.co.aim.messolution.pms.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.ESDTest;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class CreateESDTest extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String UserID 		     = SMessageUtil.getBodyItemValue(doc, "USERID", true);
		String ESDTestResult 	 = SMessageUtil.getBodyItemValue(doc, "ESDTESTRESULT", true);
		
		EventInfo eventInfo  = EventInfoUtil.makeEventInfo("CreateESDTest", getEventUser(), getEventComment(), null, null);
		
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		String ESDTestID = this.createESDTestID();
		
		ESDTest ESDTestData = null;
		 
		ESDTestData = new ESDTest(ESDTestID);
		ESDTestData.setESDTestID(ESDTestID);
		ESDTestData.setUserID(UserID);
		ESDTestData.setLastEventName(eventInfo.getEventName());
		ESDTestData.setLastEventTime(eventInfo.getEventTime());
		ESDTestData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		ESDTestData.setESDTestResult(ESDTestResult);

		try
		{
			ESDTestData = PMSServiceProxy.getESDTestService().create(eventInfo, ESDTestData);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0100", ESDTestID);
		}	

		//return 
		Document rtnDoc = new Document();
		rtnDoc = (Document)doc.clone();
		rtnDoc = SMessageUtil.addItemToBody(rtnDoc, "ESDTestID", ESDTestID);
		return rtnDoc;
	}
	
	
	public String createESDTestID()  throws CustomException
	{
		String newESDTestID = "";
		String currentDate = TimeUtils.getCurrentEventTimeKey();
		String ESDTestDate = currentDate.substring(0, 8);
		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("ESDTESTDATE", ESDTestDate);
		nameRuleAttrMap.put("HYPHEN", "-");
		
		
		//ESDTestID Generate
		try
		{
			int createQty = 1;
			List<String> lstName = CommonUtil.generateNameByNamingRule("ESDTestIDNaming", nameRuleAttrMap, createQty);
			newESDTestID = lstName.get(0);
		}
		catch(Exception ex)
		{
			new CustomException("PMS-0101", ex.getMessage());
		}
		
		return newESDTestID;
	}
}
