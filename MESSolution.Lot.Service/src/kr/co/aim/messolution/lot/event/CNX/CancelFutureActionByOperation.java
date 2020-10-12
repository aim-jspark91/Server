package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.OperAction;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class CancelFutureActionByOperation extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		List<Element> futureActionElementList = SMessageUtil.getBodySequenceItemList(doc, "FUTUREACTIONLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelFutureActionByOperation", this.getEventUser(), this.getEventComment(), "", "");
		
		for (Element eledur : futureActionElementList)
		{
			String productSpecName = SMessageUtil.getChildText(eledur, "PRODUCTSPECNAME", true);
			String productSpecVersion = SMessageUtil.getChildText(eledur, "PRODUCTSPECVERSION", true);
			String ecCode = SMessageUtil.getChildText(eledur, "ECCODE", true);
			String processFlowName = SMessageUtil.getChildText(eledur, "PROCESSFLOWNAME", true);
			String processFlowVersion = SMessageUtil.getChildText(eledur, "PROCESSFLOWVERSION", true);
			String processOperationName = SMessageUtil.getChildText(eledur, "PROCESSOPERATIONNAME", true);
			String processOperationVersion = SMessageUtil.getChildText(eledur, "PROCESSOPERATIONVERSION", true);
			String position = SMessageUtil.getChildText(eledur, "POSITION", true);
			
			
			try
			{
				
				String[] keySet={factoryName,productSpecName,productSpecVersion,ecCode,processFlowName,processFlowVersion,processOperationName,processOperationVersion,position};
				OperAction operAction = ExtendedObjectProxy.getOperActionService().selectByKey(false, keySet);

				ExtendedObjectProxy.getOperActionService().remove(eventInfo, operAction);
			}
			catch(Exception ex)
			{
				throw new CustomException("ACTION-0001"); 
			}
		}
		
		return doc;
	}

}
