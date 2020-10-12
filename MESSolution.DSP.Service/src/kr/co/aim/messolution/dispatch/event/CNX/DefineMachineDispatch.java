package kr.co.aim.messolution.dispatch.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspMachineDispatch;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class DefineMachineDispatch extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String plDispatchFlagLR = SMessageUtil.getBodyItemValue(doc, "PLDISPATCHFLAGLR", true);
		String plDispatchFlagUR = SMessageUtil.getBodyItemValue(doc, "PLDISPATCHFLAGUR", true);
		String plWaitTimee2e = SMessageUtil.getBodyItemValue(doc, "PLWAITTIMEE2E", true);
		String plWaitTimePush = SMessageUtil.getBodyItemValue(doc, "PLWAITTIMEPUSH", true);
		String plPullFlag = SMessageUtil.getBodyItemValue(doc, "PLPULLFLAG", true);
		String puDispatchFlagLR = SMessageUtil.getBodyItemValue(doc, "PUDISPATCHFLAGLR", true);
		String puDispatchFlagUR = SMessageUtil.getBodyItemValue(doc, "PUDISPATCHFLAGUR", true);
		String puWaitTimee2e = SMessageUtil.getBodyItemValue(doc, "PUWAITTIMEE2E", true);
		String puWaitTimePush = SMessageUtil.getBodyItemValue(doc, "PUWAITTIMEPUSH", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", this.getEventUser(), this.getEventComment(), "", "");
		
		DspMachineDispatch dspMachineDispatchData = null;
		
		boolean noDataFlag = false;
		
		try
		{
			dspMachineDispatchData = ExtendedObjectProxy.getDspMachineDispatchService().selectByKey(false, new Object[] {machineName});
		}
		catch (Exception ex)
		{
			//dspMachineDispatchData = null;
			noDataFlag = true;
		}
		
		//if(dspMachineDispatchData != null)
		//{
			//throw new CustomException("RECIPE-0009", "");
		//}
		
		if (noDataFlag == true) {
			dspMachineDispatchData = new DspMachineDispatch(machineName);
			dspMachineDispatchData.setPlDispatchFlagLR(plDispatchFlagLR);
			dspMachineDispatchData.setPlDispatchFlagUR(plDispatchFlagUR);
			
			if (plDispatchFlagUR.equals("Y")) {
				dspMachineDispatchData.setPlWaitTimee2e(Long.valueOf(plWaitTimee2e));
			}
			else {
				dspMachineDispatchData.setPlWaitTimee2e(0);
			}
			
			dspMachineDispatchData.setPlWaitTimePush(Long.valueOf(plWaitTimePush));
			dspMachineDispatchData.setPlPullFlag(plPullFlag);
			dspMachineDispatchData.setPuDispatchFlagLR(puDispatchFlagLR);
			dspMachineDispatchData.setPuDispatchFlagUR(puDispatchFlagUR);
			
			if (puDispatchFlagUR.equals("Y")) {
				dspMachineDispatchData.setPuWaitTimee2e(Long.valueOf(puWaitTimee2e));
			}
			else {
				dspMachineDispatchData.setPuWaitTimee2e(0);
			}

			dspMachineDispatchData.setPuWaitTimePush(Long.valueOf(puWaitTimePush));
			dspMachineDispatchData.setLastEventUser(eventInfo.getEventUser());
			//dspMachineDispatchData.setlastEventComment(eventInfo.getEventComment());
			dspMachineDispatchData.setLastEventTime(eventInfo.getEventTime());
			dspMachineDispatchData.setLastEventTimekey(eventInfo.getEventTimeKey());
			dspMachineDispatchData.setLastEventName(eventInfo.getEventName());
			
			ExtendedObjectProxy.getDspMachineDispatchService().create(eventInfo, dspMachineDispatchData);
		}
		else {
			
			dspMachineDispatchData.setPlDispatchFlagLR(plDispatchFlagLR);
			dspMachineDispatchData.setPlDispatchFlagUR(plDispatchFlagUR);
			
			if (plDispatchFlagUR.equals("Y")) {
				dspMachineDispatchData.setPlWaitTimee2e(Long.valueOf(plWaitTimee2e));
			}
			else {
				dspMachineDispatchData.setPlWaitTimee2e(dspMachineDispatchData.getPlWaitTimee2e());
			}
			
			dspMachineDispatchData.setPlWaitTimePush(Long.valueOf(plWaitTimePush));
			dspMachineDispatchData.setPlPullFlag(plPullFlag);
			dspMachineDispatchData.setPuDispatchFlagLR(puDispatchFlagLR);
			dspMachineDispatchData.setPuDispatchFlagUR(puDispatchFlagUR);
			
			if (puDispatchFlagUR.equals("Y")) {
				dspMachineDispatchData.setPuWaitTimee2e(Long.valueOf(puWaitTimee2e));
			}
			else {
				dspMachineDispatchData.setPuWaitTimee2e(dspMachineDispatchData.getPuWaitTimee2e());
			}
			eventInfo.setEventName("Modify");
			dspMachineDispatchData.setPuWaitTimePush(Long.valueOf(puWaitTimePush));
			dspMachineDispatchData.setLastEventUser(eventInfo.getEventUser());
			//dspMachineDispatchData.setlastEventComment(eventInfo.getEventComment());
			dspMachineDispatchData.setLastEventTime(eventInfo.getEventTime());
			dspMachineDispatchData.setLastEventTimekey(eventInfo.getEventTimeKey());
			dspMachineDispatchData.setLastEventName(eventInfo.getEventName());			
			
			ExtendedObjectProxy.getDspMachineDispatchService().modify(eventInfo, dspMachineDispatchData);
		}
		
		return doc;
	}
}
