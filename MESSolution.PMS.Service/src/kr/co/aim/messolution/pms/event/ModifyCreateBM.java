package kr.co.aim.messolution.pms.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.BM;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class ModifyCreateBM extends SyncHandler{
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String BMName 		  = SMessageUtil.getBodyItemValue(doc, "BMNAME", true);
		String BMType 		  = SMessageUtil.getBodyItemValue(doc, "BMTYPE", false);	
		String MachineName 	  = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String UnitName 	  = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String ShiftName 	  = SMessageUtil.getBodyItemValue(doc, "SHIFTNAME", false);
		String BMDesc 	      = SMessageUtil.getBodyItemValue(doc, "BMDESC", false);
		String BreakDownTime  = SMessageUtil.getBodyItemValue(doc, "BREAKDOWNTIME", false);
		String BMState        = SMessageUtil.getBodyItemValue(doc, "BMSTATE", true);
		String FactoryName    = SMessageUtil.getBodyItemValue(doc,"FACTORYNAME",true);
		EventInfo eventInfo   = EventInfoUtil.makeEventInfo("ModifyCreateBM", getEventUser(), getEventComment(), null, null);
		
		BM bmData = null;
		
		try
		{
			bmData = PMSServiceProxy.getBMService().selectByKey(true, new Object[] {BMName});

			//Set
		//	bmData.setBmID(BMName);
			bmData.setBmType(BMType);
		//	bmData.setMachineGroupName(MachineGroupName);
			bmData.setMachineName(MachineName);
		//	bmData.setMachineDesc(MachineDesc);
			bmData.setUnitName(UnitName);
			bmData.setShift(ShiftName); 
			bmData.setDescription(BMDesc);
			bmData.setBmEndTime(TimeStampUtil.getTimestamp(BreakDownTime));
			bmData.setBmState(BMState);
			bmData.setFactoryName(FactoryName);
			bmData.setLastEventName(eventInfo.getEventName());
			bmData.setLastEventTime(eventInfo.getEventTime());

			bmData = PMSServiceProxy.getBMService().modify(eventInfo, bmData);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0064", BMName);
		}
		
		return doc;
	}
}
