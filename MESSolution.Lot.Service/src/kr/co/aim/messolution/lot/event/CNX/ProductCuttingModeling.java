package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ProductCutModeling;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;
import org.jdom.Element;

public class ProductCuttingModeling extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		List<Element> positionElement = SMessageUtil.getBodySequenceItemList(doc, "POSITIONLIST", true);
		
		//EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", this.getEventUser(), this.getEventComment(), "", "");
		
		if(positionElement.size() > 0)
		{
			for (Element positionInfo : positionElement)
			{
				String no = positionInfo.getChild("NO").getText();
				String xPosition = positionInfo.getChild("XPOSITION").getText();
				String yPosition = positionInfo.getChild("YPOSITION").getText();
				String productSpecAC = positionInfo.getChild("PRODUCTSPECNAMEAC").getText();
				String ecCodeAC = positionInfo.getChild("ECCODEAC").getText();
				String processFlowAC = positionInfo.getChild("PROCESSFLOWNAMEAC").getText();
				
				try
				{
					ProductCutModeling productCutModeling = ExtendedObjectProxy.getProductCutModelingService().selectByKey(false, new Object[] {factoryName, productSpecName, "00001", no});
					
					productCutModeling.setxPosition(xPosition);
					productCutModeling.setyPosition(yPosition);
					productCutModeling.setProductSpecNameAC(productSpecAC);
					productCutModeling.setEcCodeAC(ecCodeAC);
					productCutModeling.setProcessFlowNameAC(processFlowAC);
					productCutModeling.setLastEventTime(eventInfo.getEventTime());
					productCutModeling.setLastEventTimeKey(eventInfo.getEventTimeKey());
					productCutModeling.setLastEventName(eventInfo.getEventName());
					productCutModeling.setLastEventUser(eventInfo.getEventUser());
					productCutModeling.setLastEventComment(eventInfo.getEventComment());
					
					eventInfo.setEventName("Change");
					ExtendedObjectProxy.getProductCutModelingService().modify(eventInfo, productCutModeling);
				}
				catch(NotFoundSignal nf)
				{
					ProductCutModeling newData = new ProductCutModeling(factoryName, productSpecName, "00001", no);
					newData.setxPosition(xPosition);
					newData.setyPosition(yPosition);
					newData.setProductSpecNameAC(productSpecAC);
					newData.setEcCodeAC(ecCodeAC);
					newData.setProcessFlowNameAC(processFlowAC);
					newData.setLastEventTime(eventInfo.getEventTime());
					newData.setLastEventTimeKey(eventInfo.getEventTimeKey());
					newData.setLastEventName(eventInfo.getEventName());
					newData.setLastEventUser(eventInfo.getEventUser());
					newData.setLastEventComment(eventInfo.getEventComment());
					
					eventInfo.setEventName("Create");
					ExtendedObjectProxy.getProductCutModelingService().create(eventInfo, newData);
				}	
			}
		}
		
		return doc;
	}

}
