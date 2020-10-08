package kr.co.aim.messolution.durable.event;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class MaskProcessData extends AsyncHandler{

	private static Log log = LogFactory.getLog(MaskProcessData.class);
	
	@Override
	public void doWorks(Document doc) throws CustomException {

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("MaskProcessData", this.getEventUser(), this.getEventComment(), null, null);
		
        String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String machineReicpeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);
		String processOperation = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", false);
		String productSpec = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", false);
		
		String maskName = "";
		
		for (Element eleMask : SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", false))
		{
			maskName = SMessageUtil.getChildText(eleMask, "MASKNAME", false);
			
			Durable maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskName);
			
			SetEventInfo setEventInfo = new SetEventInfo();
			DurableServiceProxy.getDurableService().setEvent(maskData.getKey(), eventInfo, setEventInfo);
		}
	}
}