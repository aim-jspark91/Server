package kr.co.aim.messolution.dispatch.event.CNX;

import kr.co.aim.messolution.dispatch.MESDSPServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspReserveProductNon;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class CreateDSPReserveProductNon extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		// ReserveState, SkipFlag, Position, Counting 컬럼 필요 없음
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String PortName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String productionType = SMessageUtil.getBodyItemValue(doc, "PRODUCTIONTYPE", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productRequestType = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTTYPE", true);
		String ecCode = SMessageUtil.getBodyItemValue(doc, "ECCODE", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String useFlag = SMessageUtil.getBodyItemValue(doc, "USEFLAG", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), null, null);
		
		String ReserveName = MESDSPServiceProxy.getDSPServiceUtil().getNextPositionForDSPReserveProductNon(machineName);
			
		try
		{
			DspReserveProductNon dspReserveProductNon = new DspReserveProductNon(machineName,unitName, PortName,ReserveName );
	
			dspReserveProductNon.setProductionType(productionType);
			dspReserveProductNon.setProductSpecName(productSpecName);
			dspReserveProductNon.setProductRequestType(productRequestType);
			dspReserveProductNon.setEcCode(ecCode);
			dspReserveProductNon.setProcessFlowName(processFlowName);
			dspReserveProductNon.setProcessOperationName(processOperationName);
			dspReserveProductNon.setUseFlag(useFlag);
				
			dspReserveProductNon.setLastEventUser(eventInfo.getEventUser());
			dspReserveProductNon.setLastEventComment(eventInfo.getEventComment());
			dspReserveProductNon.setLastEventTime(eventInfo.getEventTime());
			dspReserveProductNon.setLastEventTimekey(eventInfo.getEventTimeKey());
			dspReserveProductNon.setLastEventName(eventInfo.getEventName());
			
			ExtendedObjectProxy.getDspReserveProductNonService().create(eventInfo, dspReserveProductNon);
		}
		catch(Exception e){
			throw new CustomException("DSP-0000", productSpecName);
		}
		return doc;
	}

}
