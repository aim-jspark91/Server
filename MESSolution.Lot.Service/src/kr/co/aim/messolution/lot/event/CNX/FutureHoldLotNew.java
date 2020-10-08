package kr.co.aim.messolution.lot.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.FlowSampleLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class FutureHoldLotNew extends SyncHandler {

	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String reasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReserveHold", getEventUser(), getEventComment(), "", "");
		
		//Validation
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		Element eleBody = SMessageUtil.getBodyElement(doc);
		if(eleBody != null)
		{
			for (Element eleLot : SMessageUtil.getBodySequenceItemList(doc, "PROCESSOPERATIONLIST", false))
			{
				String factoryName = SMessageUtil.getChildText(eleLot, "FACTORYNAME", true);
				String dProcessFlowName = SMessageUtil.getChildText(eleLot, "PROCESSFLOWNAME", true);
				String dProcessOperationName = SMessageUtil.getChildText(eleLot, "PROCESSOPERATIONNAME", true);								
				String reasonCode = SMessageUtil.getChildText(eleLot, "REASONCODE", true);
				String holdType = SMessageUtil.getChildText(eleLot, "HOLDTYPE", true);
				
				eventInfo = EventInfoUtil.makeEventInfo("ReserveHold", getEventUser(), getEventComment(), reasonCodeType, reasonCode);
				
				FlowSampleLot sampleLotInfo = new FlowSampleLot();
				sampleLotInfo.setLOTNAME(lotName);
				sampleLotInfo.setFACTORYNAME(factoryName);
				sampleLotInfo.setPRODUCTSPECNAME(lotData.getProductSpecName());
				sampleLotInfo.setPROCESSFLOWNAME(dProcessFlowName);
				sampleLotInfo.setPROCESSOPERATIONNAME(dProcessOperationName);
				sampleLotInfo.setMACHINENAME(machineName);
				sampleLotInfo.setTOPROCESSFLOWNAME(dProcessFlowName);
				sampleLotInfo.setTOPROCESSOPERATIONNAME(dProcessOperationName);
				sampleLotInfo.setLOTSAMPLEFLAG("BHOLD");
				sampleLotInfo.setREASONCODETYPE(reasonCodeType);
				sampleLotInfo.setREASONCODE(reasonCode);
				
				if(holdType.equalsIgnoreCase("AHOLD"))
				{
					sampleLotInfo.setLOTSAMPLEFLAG("AHOLD");
					sampleLotInfo.setTOPROCESSFLOWNAME("NA");
					sampleLotInfo.setTOPROCESSOPERATIONNAME("NA");
				}
				
				sampleLotInfo.setLASTEVENTNAME(eventInfo.getEventName());
				sampleLotInfo.setLASTEVENTTIME(eventInfo.getEventTime());
				sampleLotInfo.setLASTEVENTUSER(eventInfo.getEventUser());
				sampleLotInfo.setLASTEVENTCOMMENT(eventInfo.getEventComment());
				
				ExtendedObjectProxy.getFlowSampleLotService().create(eventInfo, sampleLotInfo);
			}
			
			LotKey lotKey = new LotKey(lotData.getKey().getLotName());
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.setUdfs(lotData.getUdfs());
			
			LotServiceProxy.getLotService().setEvent(lotKey, eventInfo, setEventInfo);
		}
		
		return doc;
	}
}
