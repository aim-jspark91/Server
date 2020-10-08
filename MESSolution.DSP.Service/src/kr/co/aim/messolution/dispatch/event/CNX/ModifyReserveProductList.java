package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspReserveProduct;
import kr.co.aim.messolution.extended.object.management.data.DspReserveProductNon;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;
import org.jdom.Element;

public class ModifyReserveProductList extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		try
		{	
			List<Element> eleproductList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
			String reserveFlag = SMessageUtil.getBodyItemValue(doc, "RESERVEFLAG", true);
			
			for(Element eleData : eleproductList)
			{
				String machineName = SMessageUtil.getChildText(eleData, "MACHINENAME", true);
				String unitName = SMessageUtil.getChildText(eleData, "UNITNAME", true);
				String portName = SMessageUtil.getChildText(eleData, "PORTNAME", true);
				String reserveName = SMessageUtil.getChildText(eleData, "RESERVENAME", true);
				String positionName = SMessageUtil.getChildText(eleData, "POSITION", true);
				
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("Modify", this.getEventUser(), this.getEventComment(), "", "");

				if(StringUtil.equals("TRUE", reserveFlag.toUpperCase())){
					DspReserveProduct reserveProductData = ExtendedObjectProxy.getDspReserveProductService().selectByKey(false, new Object[] {machineName, unitName,portName, reserveName});
					
					reserveProductData.setPositionName(positionName);
					reserveProductData.setLastEventUser(eventInfo.getEventUser());
					reserveProductData.setLastEventComment(eventInfo.getEventComment());
					reserveProductData.setLastEventTime(eventInfo.getEventTime());
					reserveProductData.setLastEventTimekey(eventInfo.getEventTimeKey());
					reserveProductData.setLastEventName(eventInfo.getEventName());
					
					ExtendedObjectProxy.getDspReserveProductService().modify(eventInfo, reserveProductData); 
				}
				else{
					DspReserveProductNon dspReserveProductNon = ExtendedObjectProxy.getDspReserveProductNonService().selectByKey(false, new Object[] {machineName, unitName,portName, reserveName});
					
//					dspReserveProductNon.setPositionName(positionName);
					dspReserveProductNon.setLastEventUser(eventInfo.getEventUser());
					dspReserveProductNon.setLastEventComment(eventInfo.getEventComment());
					dspReserveProductNon.setLastEventTime(eventInfo.getEventTime());
					dspReserveProductNon.setLastEventTimekey(eventInfo.getEventTimeKey());
					dspReserveProductNon.setLastEventName(eventInfo.getEventName());
					
					ExtendedObjectProxy.getDspReserveProductNonService().modify(eventInfo, dspReserveProductNon); 
				}

			}
		}
		catch(Exception ex)
		{
			throw new CustomException("UpdateFail!");
		}
		
		return doc;
			
			
//			List<DspAlternativeStocker> stockerList = null;
//			long position = 0;
//			
//			try
//			{			
//				String condition = "WHERE 1=1 AND STOCKERNAME = ? ORDER BY POSITION DESC ";
//
//				Object[] bindSet = new Object[]{stockerName};
//				
//				stockerList = ExtendedObjectProxy.getDspAlternativeStockerService().select(condition, bindSet);
//				
//				if(stockerList != null)
//				{
//					position = stockerList.get(0).getPosition();
//					position++;
//				}
//			}
//			catch(Exception ex)
//			{
//				
//			}
//			
//			DspAlternativeStocker alternativeStockerData = new DspAlternativeStocker();
//			alternativeStockerData.setStockerName(stockerName);
//			alternativeStockerData.setToStockerName(toStockerName);
//			alternativeStockerData.setPosition(position);
//			
//			alternativeStockerData.setLastEventUser(eventInfo.getEventUser());
//			alternativeStockerData.setLastEventComment(eventInfo.getEventComment());
//			alternativeStockerData.setLastEventTime(eventInfo.getEventTime());
//			alternativeStockerData.setLastEventTimekey(eventInfo.getEventTimeKey());
//			alternativeStockerData.setLastEventName(eventInfo.getEventName());	
//			
//			try
//			{
//				ExtendedObjectProxy.getDspAlternativeStockerService().create(eventInfo, alternativeStockerData);
//			}
//			catch(Exception ex)
//			{
//				throw new CustomException("", "");
//			}
		
	
	}
}
