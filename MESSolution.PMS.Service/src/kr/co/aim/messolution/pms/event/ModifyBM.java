package kr.co.aim.messolution.pms.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.BM;
import kr.co.aim.messolution.pms.management.data.BMUser;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class ModifyBM extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String BMName 		  = SMessageUtil.getBodyItemValue(doc, "BMNAME", true);
		String BMType 		  = SMessageUtil.getBodyItemValue(doc, "BMTYPE", true);
		String BMGrade        = SMessageUtil.getBodyItemValue(doc, "BMGRADE", true);
		String MachineName 	  = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String UnitName 	  = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String CumulativeTime = SMessageUtil.getBodyItemValue(doc, "CUMULATIVETIME", true);

		EventInfo eventInfo  = EventInfoUtil.makeEventInfo("ModifyBM", getEventUser(), getEventComment(), null, null);
		
		BM bmData = null;
		
		try
		{
			bmData = PMSServiceProxy.getBMService().selectByKey(true, new Object[] {BMName});
			
			//Get
			String ExecuteResult = bmData.getExecuteResult();
			String RepairTime    = bmData.getRepairTime().toString();
			String ShiftName     = bmData.getShift();
			String BMEndTime     = bmData.getBmEndTime().toString();
			String BMDesc        = bmData.getDescription();
			String BMCause       = bmData.getBmCause();
			String BMSolution    = bmData.getBmSolution();
			String BMState       = bmData.getBmState();
			String FactoryName   =bmData.getFactoryName();
			
			//Set
			bmData = new BM(BMName);
			bmData.setBmID(BMName);
			bmData.setBmCause(BMCause);
			bmData.setBmSolution(BMSolution);
			bmData.setBmType(BMType);
			bmData.setMachineName(MachineName);
			bmData.setUnitName(UnitName);
			bmData.setShift(ShiftName); 
			bmData.setDescription(BMDesc);
			bmData.setBmEndTime(TimeStampUtil.getTimestamp(BMEndTime));
			bmData.setExecuteResult(ExecuteResult);
			bmData.setBmGrade(BMGrade);
			bmData.setRepairTime(TimeStampUtil.getTimestamp(RepairTime));
			bmData.setBmState(BMState);
			bmData.setLastEventName(eventInfo.getEventName());
			bmData.setLastEventTime(eventInfo.getEventTime()); 
			bmData.setCumulativeTime(CumulativeTime);
			bmData.setFactoryName(FactoryName);

			bmData = PMSServiceProxy.getBMService().modify(eventInfo, bmData);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0064", BMName);
		}
		
		//Set User Info 
		BMUser userData = null;
		String BMUserType = "Modify";
						 
		userData = new BMUser(eventInfo.getEventUser(), BMName, BMUserType);
		userData.setBmID(BMName);
		userData.setBmUser(eventInfo.getEventUser());
		userData.setUserType(BMUserType);
		userData.setTimeKey(StringUtil.isEmpty(eventInfo.getEventTimeKey())?TimeUtils.getCurrentEventTimeKey():eventInfo.getEventTimeKey());

		eventInfo.setEventName("");
						
		try
		{
			userData = PMSServiceProxy.getBMUserService().create(eventInfo, userData);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0065", BMName);
		}
		
		return doc;
	}
}
