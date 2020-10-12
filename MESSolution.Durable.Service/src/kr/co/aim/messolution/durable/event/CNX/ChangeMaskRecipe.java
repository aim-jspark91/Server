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

public class ChangeMaskRecipe extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeMaskRecipe", this.getEventUser(), this.getEventComment(), "", "");
		
		String sDurableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		String sMachineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME",true);
		
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);
		
		//change Mask Recipe Name
		Map<String, String> udfs = new HashMap<String, String>();
		udfs.put("MACHINERECIPENAME", sMachineRecipeName);
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs(udfs);
		
		durableData.setUdfs(udfs);
		durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);
		
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
		
		return doc;
	}
}