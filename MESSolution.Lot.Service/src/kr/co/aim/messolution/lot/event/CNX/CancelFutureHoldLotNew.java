package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

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

public class CancelFutureHoldLotNew extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub		
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);		
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		
		Element elebody = SMessageUtil.getBodyElement(doc);
		
		//Validation
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		if(elebody != null)
		{
			for(Element eleLot : SMessageUtil.getBodySequenceItemList(doc, "PROCESSOPERATIONLIST", false))
			{
				String factoryName = SMessageUtil.getChildText(eleLot, "FACTORYNAME", true);
				String processFlowName = SMessageUtil.getChildText(eleLot, "PROCESSFLOWNAME", true);
				String processOperationName = SMessageUtil.getChildText(eleLot, "PROCESSOPERATIONNAME", true);
				String lotSampleFlag = SMessageUtil.getChildText(eleLot, "LOTSAMPLEFLAG", true);
				String reasonCode = SMessageUtil.getChildText(eleLot, "REASONCODE", true);
				
				eventInfo = EventInfoUtil.makeEventInfo("CancelReserveHold", getEventUser(), getEventComment(), "", reasonCode);
				
				List<FlowSampleLot> flowSampleLot = null;
				try
				{
					String condition = "WHERE FACTORYNAME = ? AND LOTNAME = ? AND PROCESSFLOWNAME = ?"
							+ " AND PROCESSOPERATIONNAME = ? AND REASONCODE = ? AND LOTSAMPLEFLAG = ? ";
					Object[] bindSet = new Object[] {factoryName, lotName, processFlowName, processOperationName, reasonCode, lotSampleFlag};
					
					flowSampleLot = ExtendedObjectProxy.getFlowSampleLotService().select(condition, bindSet);
				}
				catch(Exception ex)
				{
					throw new CustomException("LOT-0018", flowSampleLot.get(0).getLOTNAME());
				}
				
				ExtendedObjectProxy.getFlowSampleLotService().remove(eventInfo, flowSampleLot.get(0));
			}
			
			LotKey lotKey = new LotKey(lotData.getKey().getLotName());
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.setUdfs(lotData.getUdfs());
			
			LotServiceProxy.getLotService().setEvent(lotKey, eventInfo, setEventInfo);
		}
		
		return doc;
	}

}
