package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspAlternativeStocker;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;
import org.jdom.Element;

public class ModifyAlternativeStocker extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {

		String stockerName = SMessageUtil.getBodyItemValue(doc, "STOCKERNAME", true);
		
		List<Element> eleList = SMessageUtil.getBodySequenceItemList(doc, "STOCKERLIST", true);
		
		for(Element eleData : eleList)
		{
			String toStockerName = SMessageUtil.getChildText(eleData, "TOSTOCKERNAME", true);
			String position = SMessageUtil.getChildText(eleData, "POSITION", true);
			
		    Object[] keySet = new Object[]{stockerName, toStockerName};
			DspAlternativeStocker stockerData = ExtendedObjectProxy.getDspAlternativeStockerService().selectByKey(false, keySet);
			
			stockerData.setPosition(Long.parseLong(position.trim()));
			ExtendedObjectProxy.getDspAlternativeStockerService().update(stockerData);
		}
		
		return doc;
	}
}
