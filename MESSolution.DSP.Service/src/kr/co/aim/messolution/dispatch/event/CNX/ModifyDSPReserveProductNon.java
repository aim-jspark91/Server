package kr.co.aim.messolution.dispatch.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspReserveProductNon;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class ModifyDSPReserveProductNon extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		// ReserveState, SkipFlag, Position, Counting 컬럼 필요 없음
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String reserveName = SMessageUtil.getBodyItemValue(doc, "RESERVENAME", true);
		
		String productionType = SMessageUtil.getBodyItemValue(doc, "PRODUCTIONTYPE", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productRequestType = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTTYPE", true);
		String ecCode = SMessageUtil.getBodyItemValue(doc, "ECCODE", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String useFlag = SMessageUtil.getBodyItemValue(doc, "USEFLAG", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Modify", this.getEventUser(), this.getEventComment(), "", "");
		
		try
		{
			DspReserveProductNon reserveProductDataNon = ExtendedObjectProxy.getDspReserveProductNonService().selectByKey(false, new Object[] {machineName,unitName, portName, reserveName});
		
			reserveProductDataNon.setProductionType(productionType);
			reserveProductDataNon.setProductSpecName(productSpecName);
			reserveProductDataNon.setProductRequestType(productRequestType);
			reserveProductDataNon.setEcCode(ecCode);
			reserveProductDataNon.setProcessFlowName(processFlowName);
			reserveProductDataNon.setProcessOperationName(processOperationName);
			reserveProductDataNon.setUseFlag(useFlag);
			reserveProductDataNon.setLastEventUser(eventInfo.getEventUser());
			reserveProductDataNon.setLastEventComment(eventInfo.getEventComment());
			reserveProductDataNon.setLastEventTime(eventInfo.getEventTime());
			reserveProductDataNon.setLastEventTimekey(eventInfo.getEventTimeKey());
			reserveProductDataNon.setLastEventName(eventInfo.getEventName());
			
			ExtendedObjectProxy.getDspReserveProductNonService().modify(eventInfo, reserveProductDataNon);
		}
		catch (Exception ex)
		{
			throw new CustomException("RECIPE-0009", "");
		}
		return doc;
	}
}
