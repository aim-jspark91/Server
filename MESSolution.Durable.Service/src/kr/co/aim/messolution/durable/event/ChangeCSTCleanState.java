package kr.co.aim.messolution.durable.event;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.CleanInfo;
import kr.co.aim.greentrack.durable.management.info.DirtyInfo;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ChangeCSTCleanState extends SyncHandler {

	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		String sDurableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		String sCleanState = SMessageUtil.getBodyItemValue(doc,"DURABLECLEANSTATE", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc,"MACHINENAME", false);
		String sDryFlag = SMessageUtil.getBodyItemValue(doc,"DURABLEDRYFLAG", false);
		
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		
		if(!StringUtils.equals(durableData.getFactoryName(), System.getProperty("shop")))
		{
			if(!StringUtil.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_Available))
			{
				throw new CustomException("CST-0039", sDurableName, durableData.getFactoryName());
			}
		}
		
		if(durableData.getDurableCleanState().equals(sCleanState))
		{
			if(durableData.getUdfs().get("DRYFLAG").equals(sDryFlag))
			{
				throw new CustomException("CST-0010", SMessageUtil.Body_Tag);
			}
			else
			{
				//Change Dry Flag
				eventInfo.setEventName("ChangeDryFlag");
				
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("DRYFLAG", sDryFlag);
				
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
			}
		}
		else
		{
			if(sCleanState.equals(GenericServiceProxy.getConstantMap().Dur_Clean))
			{
				eventInfo.setEventName("Clean");
				
				//Clean
				CleanInfo cleanInfo = new CleanInfo();
				cleanInfo.getUdfs().put("DRYFLAG", sDryFlag);
				
				if (StringUtils.isNotEmpty(sMachineName))
					cleanInfo.getUdfs().put("MACHINENAME", sMachineName);
				
				cleanInfo.getUdfs().put("LASTCLEANTIME", TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
				
				durableData = MESDurableServiceProxy.getDurableServiceImpl().clean(durableData, cleanInfo, eventInfo);
			}
			else if (sCleanState.equals(GenericServiceProxy.getConstantMap().Dur_Dirty))
			{	
				eventInfo.setEventName("Dirty");
				
				//Dirty
				DirtyInfo dirtyInfo = new DirtyInfo();
				dirtyInfo.getUdfs().put("DRYFLAG", sDryFlag);
	
				MESDurableServiceProxy.getDurableServiceImpl().dirty(durableData, dirtyInfo, eventInfo);
			}
		}
		
		// Added by smkang on 2018.10.02 - According to EDO's request, carrier data should be synchronized with shared factory.
		//MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(doc, sDurableName);
		
		return doc;
	}
}