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
import kr.co.aim.messolution.pms.management.data.BM;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class CreateBM extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		//String BMName 		 = SMessageUtil.getBodyItemValue(doc, "BMNAME", true);
		String BMType 		 = SMessageUtil.getBodyItemValue(doc, "BMTYPE", true);
		String MachineName 	 = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String UnitName 	 = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String SubUnitName   = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String ShiftName  	 = SMessageUtil.getBodyItemValue(doc, "SHIFTNAME", false);
		String BMDesc  		 = SMessageUtil.getBodyItemValue(doc, "BMDESC", false);
		String BreakDownTime = SMessageUtil.getBodyItemValue(doc, "BREAKDOWNTIME", true);
		String BMState       = SMessageUtil.getBodyItemValue(doc, "BMSTATE", true);
		String FactoryName   = SMessageUtil.getBodyItemValue(doc,"FACTORYNAME",true);
		String BreakDownConfirmer   = SMessageUtil.getBodyItemValue(doc,"BREAKDOWNCONFIRMER",true);
		EventInfo eventInfo  = EventInfoUtil.makeEventInfo("CreateBM", getEventUser(), getEventComment(), null, null);
		
		String BMName = this.createBMName(MachineName);
		
		BM bmData = null;
		 
		bmData = new BM(BMName);
		bmData.setBmID(BMName);
		bmData.setBmType(BMType);
		bmData.setMachineName(MachineName);
		bmData.setUnitName(UnitName);
		bmData.setSubUnitName(SubUnitName);
		bmData.setShift(ShiftName); 
		bmData.setDescription(BMDesc);
		bmData.setBmEndTime(TimeStampUtil.getTimestamp(BreakDownTime));
		bmData.setBmState(BMState);
		bmData.setFactoryName(FactoryName);
		bmData.setLastEventName(eventInfo.getEventName());
		bmData.setLastEventTime(eventInfo.getEventTime());
		bmData.setLastEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		bmData.setBreakDownConfirmer(BreakDownConfirmer);

		try
		{
			bmData = PMSServiceProxy.getBMService().create(eventInfo, bmData);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0060", BMName);
		}	

		//return 
		Document rtnDoc = new Document();
		rtnDoc = (Document)doc.clone();
		rtnDoc = SMessageUtil.addItemToBody(rtnDoc, "BMID", BMName);
		return rtnDoc;
	}
	
	
	public String createBMName(String MachineName)  throws CustomException
	{
		String newBMID = "";
		String currentDate = TimeUtils.getCurrentEventTimeKey();
		String bmDate = currentDate.substring(2, 8);
		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("MACHINENAME", MachineName);
		nameRuleAttrMap.put("HYPHEN", "-");
		nameRuleAttrMap.put("BMDATE", bmDate);
		
		
		//LotID Generate
		try
		{
			int createQty = 1;
			List<String> lstName = CommonUtil.generateNameByNamingRule("BMNaming", nameRuleAttrMap, createQty);
			newBMID = lstName.get(0);
		}
		catch(Exception ex)
		{
			new CustomException("LOT-9011", ex.getMessage());
		}
		
		return newBMID;
	}
}
