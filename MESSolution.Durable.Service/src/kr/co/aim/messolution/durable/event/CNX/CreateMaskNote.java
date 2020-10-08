package kr.co.aim.messolution.durable.event.CNX;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class CreateMaskNote extends SyncHandler {
	public Object doWorks(Document doc) throws CustomException
	{
		String messageName = SMessageUtil.getMessageName(doc);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, this.getEventUser(), this.getEventComment(), "", "");
		
		//condition
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String maskNote = SMessageUtil.getBodyItemValue(doc, "MASKNOTE", true);

		try {
			Durable newDurable = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskName);

			if(newDurable != null) {
				SetEventInfo setMaskEventInfo = new SetEventInfo();
				setMaskEventInfo.getUdfs().put("NOTE", maskNote);

				//set event
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(newDurable, setMaskEventInfo, eventInfo);
				
				// Modified by smkang on 2019.05.28 - DurableServiceProxy.getDurableService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//				newDurable = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(newDurable.getKey().getDurableName());
//				
//				// For Clear Note, Add By Park Jeong Su
//				newDurable.getUdfs().put("NOTE", "");
//				DurableServiceProxy.getDurableService().update(newDurable);
				Map<String, String> updateUdfs = new HashMap<String, String>();
				updateUdfs.put("NOTE", "");
				MESDurableServiceProxy.getDurableServiceImpl().updateDurableWithoutHistory(newDurable, updateUdfs);
			}
		} catch(Exception ex) {
			throw ex;
		}
		
		return doc;
	}
}