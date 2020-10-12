package kr.co.aim.messolution.lot.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.YieldInfo;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class ModifyYield extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String ecCode = SMessageUtil.getBodyItemValue(doc, "ECCODE", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String sheetYield = SMessageUtil.getBodyItemValue(doc, "SHEETYIELD", true);
		String sheetFlag = SMessageUtil.getBodyItemValue(doc, "SHEETFLAG", true);
		String lotYield = SMessageUtil.getBodyItemValue(doc, "LOTYIELD", true);
		String lotFlag = SMessageUtil.getBodyItemValue(doc, "LOTFLAG", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyYield", this.getEventUser(), this.getEventComment(), "", "");
		
			
		YieldInfo yieldInfo = null;
		
		try
		{
			yieldInfo = ExtendedObjectProxy.getYieldInfoService().selectByKey(false, new Object[] {productSpecName, ecCode, processFlowName  , processFlowVersion  , processOperationName });
		}
		catch (Exception ex)
		{
			yieldInfo = null;
		}
		
		if(yieldInfo == null)
		{
			throw new CustomException("IDLE-0006", "");
		}
		yieldInfo.setSheetYield(Double.parseDouble(sheetYield));
		yieldInfo.setSheetFlag(sheetFlag);
		yieldInfo.setLotYield(Double.parseDouble(lotYield));
		yieldInfo.setLotFlag(lotFlag);

		yieldInfo.setLastEventUser(eventInfo.getEventUser());
		yieldInfo.setLastEventComment(eventInfo.getEventComment());
		yieldInfo.setLastEventTime(eventInfo.getEventTime());
		yieldInfo.setLastEventName(eventInfo.getEventName());
		yieldInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
		
		ExtendedObjectProxy.getYieldInfoService().modify(eventInfo, yieldInfo);
		
		return doc;
	}
}
