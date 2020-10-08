package kr.co.aim.messolution.lot.event.CNX;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class FutureHoldLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);		
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
//		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", true);
		String reasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", true);
//		String holdType = SMessageUtil.getBodyItemValue(doc, "HOLDTYPE", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReserveHold", getEventUser(), getEventComment(), "", "");

		/*2018.02.22 dmlee : arrange For EDO		
		//Validation
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		Element eleBody = SMessageUtil.getBodyElement(doc);
		if(eleBody != null)
		{
			for (Element eleLot : SMessageUtil.getBodySequenceItemList(doc, "PROCESSOPERATIONLIST", false))
			{
				String factoryName = SMessageUtil.getChildText(eleLot, "FACTORYNAME", true);
				String processOperationName = SMessageUtil.getChildText(eleLot, "PROCESSOPERATIONNAME", true);
				String toProcessOperationName = SMessageUtil.getChildText(eleLot, "TOPROCESSOPERATIONNAME", false);
				String reasonCode = SMessageUtil.getChildText(eleLot, "REASONCODE", true);
				String holdType = SMessageUtil.getChildText(eleLot, "HOLDTYPE", true);
				
				eventInfo = EventInfoUtil.makeEventInfo("ReserveHold", getEventUser(), getEventComment(), reasonCodeType, reasonCode);
				
				SampleLot sampleLotInfo = new SampleLot();
				sampleLotInfo.setLOTNAME(lotName);
				sampleLotInfo.setFACTORYNAME(factoryName);
				sampleLotInfo.setPRODUCTSPECNAME(lotData.getProductSpecName());
				sampleLotInfo.setPROCESSFLOWNAME(processFlowName);
				sampleLotInfo.setPROCESSOPERATIONNAME(processOperationName);
				sampleLotInfo.setMACHINENAME(machineName);
				sampleLotInfo.setTOPROCESSOPERATIONNAME(processOperationName);
				sampleLotInfo.setLOTSAMPLEFLAG("BHOLD");
				 2018.02.07 hsryu - remove 
				sampleLotInfo.setREASONCODETYPE(reasonCodeType);
				sampleLotInfo.setREASONCODE(reasonCode);
				
				if(holdType.equalsIgnoreCase("AHOLD"))
				{
					sampleLotInfo.setLOTSAMPLEFLAG("AHOLD");
					sampleLotInfo.setTOPROCESSOPERATIONNAME(reasonCode);
				}
				
				sampleLotInfo.setLASTEVENTNAME(eventInfo.getEventName());
				sampleLotInfo.setLASTEVENTTIMEKEY(TimeUtils.getCurrentEventTimeKey());
				sampleLotInfo.setLASTEVENTTIME(eventInfo.getEventTime());
				sampleLotInfo.setLASTEVENTUSER(eventInfo.getEventUser());
				sampleLotInfo.setLASTEVENTCOMMENT(eventInfo.getEventComment());
				
				ExtendedObjectProxy.getSampleLotService().create(eventInfo, sampleLotInfo);
			}
			
			LotKey lotKey = new LotKey(lotData.getKey().getLotName());
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.setUdfs(lotData.getUdfs());
			
			LotServiceProxy.getLotService().setEvent(lotKey, eventInfo, setEventInfo);
		}

		 dmlee */
		
		
		return doc;
	}
}
