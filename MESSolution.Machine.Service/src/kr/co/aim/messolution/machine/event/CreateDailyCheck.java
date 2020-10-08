package kr.co.aim.messolution.machine.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DailyCheck;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class CreateDailyCheck extends SyncHandler{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String timekey = SMessageUtil.getBodyItemValue(doc, "TIMEKEY", true);
		String detailmachinetype = SMessageUtil.getBodyItemValue(doc, "DETAILMACHINETYPE", true);
		String areaName = SMessageUtil.getBodyItemValue(doc, "AREANAME", true);
		List<Element> machineList = SMessageUtil.getBodySequenceItemList(doc, "MACHINELIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateDailyCheck", this.getEventUser(), this.getEventComment(), "", "");
		
		for(Element machine : machineList)
		{
			String machinename = SMessageUtil.getChildText(machine, "MACHINENAME", true);
			String supermachinename = SMessageUtil.getChildText(machine, "SUPERMACHINENAME", false);
			
			DailyCheck DailyCheckData = null;
			
			try
			{
				DailyCheckData = ExtendedObjectProxy.getDailyCheckService().selectByKey(false, new Object[] {machinename});
			}
			catch (Exception ex)
			{
				DailyCheckData = null;				
			}
		
			if(DailyCheckData != null)
			{
				throw new CustomException("IDLE-0005", "");
			}
			
			DailyCheckData = new DailyCheck(machinename);
			DailyCheckData.setFactoryName(factoryName);
			DailyCheckData.settime(Integer.parseInt(timekey));
			DailyCheckData.setsupermachinename(supermachinename);
			DailyCheckData.setlasteventuser(eventInfo.getEventUser());
			DailyCheckData.setlasteventcomment(eventInfo.getEventComment());
			DailyCheckData.setlasteventtime(eventInfo.getEventTime());
			DailyCheckData.setlasteventtimekey(eventInfo.getEventTimeKey());
			DailyCheckData.setlasteventname(eventInfo.getEventName());
			DailyCheckData.setcreateuser(eventInfo.getEventUser());
			DailyCheckData.setcreatetime(eventInfo.getEventTime());
			DailyCheckData.setdetailmachinetype(detailmachinetype);
			DailyCheckData.setAreaName(areaName);
			
			ExtendedObjectProxy.getDailyCheckService().create(eventInfo, DailyCheckData);
		}		
		return doc;
	}
}
