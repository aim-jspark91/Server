package kr.co.aim.messolution.durable.event;

import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.CleanInfo;
import kr.co.aim.greentrack.durable.management.info.DirtyInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class CancelClean extends SyncHandler {

	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		String sDurableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		String sCleanState = SMessageUtil.getBodyItemValue(doc,"DURABLECLEANSTATE", true);

		//GetDurableData
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");

		if(sCleanState.equals(GenericServiceProxy.getConstantMap().Dur_Clean))
		{
			eventInfo.setEventName("CancelClean");

			Map<String, String> udfs = durableData.getUdfs();

			//Clean
			CleanInfo cleanInfo = new CleanInfo();
//			udfs.put("MACHINENAME", "");
			udfs.put("LASTCLEANTIME", TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
			cleanInfo.setUdfs(durableData.getUdfs());

			durableData = MESDurableServiceProxy.getDurableServiceImpl().clean(durableData, cleanInfo, eventInfo);
			
			// Deleted by smkang on 2018.10.23 - This function is not applied in OLED shop.
//			if(!StringUtils.equals(durableData.getFactoryName(), System.getProperty("shop")))
//			{
//				if(!StringUtil.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_Available))
//				{
//					throw new CustomException("CST-0039", sDurableName, durableData.getFactoryName());
//				}
//			}

//			try
//			{
//				durableData.getUdfs().put("MACHINENAME", "");
//				DurableServiceProxy.getDurableService().update(durableData);
//			}
//			catch (Exception ex)
//			{
//				eventLog.error("Location purge failed");
//			}
		}
		else if (sCleanState.equals(GenericServiceProxy.getConstantMap().Dur_Dirty))
		{	
			eventInfo.setEventName("CancelDirty");

			//Dirty
			DirtyInfo dirtyInfo = new DirtyInfo();
			dirtyInfo.setUdfs(durableData.getUdfs());

			MESDurableServiceProxy.getDurableServiceImpl().dirty(durableData, dirtyInfo, eventInfo);
		}
		
		/*// Added by smkang on 2018.10.02 - According to EDO's request, carrier data should be synchronized with shared factory.
		MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(doc, sDurableName);*/

		return doc;
	}
}
