package kr.co.aim.messolution.durable.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class MaskOutShelf extends AsyncHandler {
	
	private static Log log = LogFactory.getLog(MaskOutShelf.class);

	@Override
	public void doWorks(Document doc) throws CustomException {
	
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ShelfOut", this.getEventUser(), this.getEventComment(), null, null);
		
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String vcrMaskName = SMessageUtil.getBodyItemValue(doc, "VCRMASKNAME", false);
		String maskCleanState = SMessageUtil.getBodyItemValue(doc, "MASKCLEANSTATE", false);//DURABLECLEANSTATE
		String maskAoiState  = SMessageUtil.getBodyItemValue(doc, "MASKAOISTATE", false);
		String maskInspState = SMessageUtil.getBodyItemValue(doc, "MASKINSPSTATE", false);//INSPECTSTATE
		String maskType = SMessageUtil.getBodyItemValue(doc, "MASKTYPE", false);
		String maskRepairCount = SMessageUtil.getBodyItemValue(doc, "MASKREPAIRCOUNT", false);
		String maskThickness = SMessageUtil.getBodyItemValue(doc, "MASKTHICKNESS", false);
		String maskngCode = SMessageUtil.getBodyItemValue(doc, "MASKNGCODE", false);
		String maskAmhsZone = SMessageUtil.getBodyItemValue(doc, "MASKAMHSZONE", false);
		String maskMagnet = SMessageUtil.getBodyItemValue(doc, "MASKMAGNET", false);
		String maskOffSetX = SMessageUtil.getBodyItemValue(doc, "MASK_OFFSET_X", false);
		String maskOffSetY = SMessageUtil.getBodyItemValue(doc, "MASK_OFFSET_Y", false);
		String maskOffSetT = SMessageUtil.getBodyItemValue(doc, "MASK_OFFSET_T", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String positionName = SMessageUtil.getBodyItemValue(doc, "POSITIONNAME", false);
		String workflowSkip = SMessageUtil.getBodyItemValue(doc, "WORKFLOWSKIP", false);
		
		Durable durMaskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskName);
		
		// Put data into UDF
		Map<String, String> udfs = new HashMap<String, String>();
		udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_MOVING);
		udfs.put("MASKPOSITION", "");
		udfs.put("INSPECTSTATE", maskInspState);
		udfs.put("POSITIONTYPE", "");
		udfs.put("POSITIONNAME", "");
		
		//set event
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs(udfs);
		 
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durMaskData, setEventInfo, eventInfo);
	}
}
