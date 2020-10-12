package kr.co.aim.messolution.durable.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class MaskInShelf extends AsyncHandler{
	
	private static Log log = LogFactory.getLog(MaskInShelf.class);

	@Override
	public void doWorks(Document doc) throws CustomException {

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ShelfIn", this.getEventUser(), this.getEventComment(), null, null);
		
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String maskCleanState = SMessageUtil.getBodyItemValue(doc, "MASKCLEANSTATE", false);
		String maskAoiState  = SMessageUtil.getBodyItemValue(doc, "MASKAOISTATE", false);
		String maskInspState = SMessageUtil.getBodyItemValue(doc, "MASKINSPSTATE", false);
		String maskType = SMessageUtil.getBodyItemValue(doc, "MASKTYPE", false);
		String maskRepairCount = SMessageUtil.getBodyItemValue(doc, "MASKREPAIRCOUNT", false);
		String maskNGCode = SMessageUtil.getBodyItemValue(doc, "MASKNGCODE", false);
		String maskAmhsZone = SMessageUtil.getBodyItemValue(doc, "MASKAMHSZONE", false);
		String maskOffSetX = SMessageUtil.getBodyItemValue(doc, "MASK_OFFSET_X", false);
		String maskOffSetY = SMessageUtil.getBodyItemValue(doc, "MASK_OFFSET_Y", false);
		String maskOffSetT = SMessageUtil.getBodyItemValue(doc, "MASK_OFFSET_T", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String maskPosition = SMessageUtil.getBodyItemValue(doc, "POSITIONNAME", false);//SHELFNO from EAP
		String vcrMaskName = SMessageUtil.getBodyItemValue(doc, "VCRMASKNAME", false);
		String maskPPAState = SMessageUtil.getBodyItemValue(doc, "MASKPPASTATE", false);
		String maskJudge = SMessageUtil.getBodyItemValue(doc, "MASKJUDGE", false);
		String maskThickness = SMessageUtil.getBodyItemValue(doc, "MASKTHICKNESS", false);
		String maskMagnet = SMessageUtil.getBodyItemValue(doc, "MASKMAGNET", false);
		String workFlowSkip = SMessageUtil.getBodyItemValue(doc, "WORKFLOWSKIP", false);
		
		/**************************************************************************************
		 maskNGCode 						1: Clean NG | 2:AOI NG | 3 : INSP NG |4 : Qtime NG
		 maskAoiState; (MaskRepairState) 	R : Repair  | G : Good
		 maskInspState; (InspectState)  	N : No Good | G : Good
		***************************************************************************************/
		
		//Mask
		Durable durMaskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskName);
		String sCleanCount = CommonUtil.getValue(durMaskData.getUdfs(), "CLEANUSED");
		// offsetPre
		StringBuffer maskOffSetBuffer = new StringBuffer();
		
		if(StringUtils.isNotEmpty(maskOffSetX))
		{
			maskOffSetBuffer.append(maskOffSetX);
			maskOffSetBuffer.append('^');
		}
		if(StringUtils.isNotEmpty(maskOffSetY))
		{
			maskOffSetBuffer.append(maskOffSetY);
			maskOffSetBuffer.append('^');
		}
		if(StringUtils.isNotEmpty(maskOffSetT))
		{
			maskOffSetBuffer.append(maskOffSetT);
		}
		int cleanCount = Integer.parseInt(sCleanCount);
		cleanCount++;
		
		Map<String, String> udfs = new HashMap<String, String>();
		udfs.put("REASONCODE", maskNGCode);
		udfs.put("OFFSETPRE", maskOffSetBuffer.toString());
		udfs.put("POSITIONNAME", subUnitName);
		udfs.put("MASKPOSITION", maskPosition);
		udfs.put("REPAIRCOUNT", maskRepairCount);
		udfs.put("DURABLECLEANSTATE", maskCleanState);
		udfs.put("TRANSPORTSTATE", "STORED");
		udfs.put("INSPECTSTATE", maskInspState);
		udfs.put("POSITIONTYPE", "SHELF");
		udfs.put("AOISTATE", maskAoiState);
		udfs.put("PPASTATE",maskInspState);
		udfs.put("JUDGE", maskJudge);
		udfs.put("CLEANUSED", String.valueOf(cleanCount));
		udfs.put("LASTCLEANTIME", eventInfo.getEventTime().toString());
		if(StringUtils.isNotEmpty(maskAoiState) || StringUtils.isNotEmpty(maskPPAState))
		{
			udfs.put("INSPECTSTATE", "Inspected");
		}
		
		//set event
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs(udfs);
		 
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durMaskData, setEventInfo, eventInfo);
	}
}