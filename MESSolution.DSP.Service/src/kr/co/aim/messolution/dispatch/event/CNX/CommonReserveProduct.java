package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CommonReserveProduct extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String processOperationGroupName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONGROUPNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String reserveState = SMessageUtil.getBodyItemValue(doc, "RESERVESTATE", true);
		String reserveQuantity = SMessageUtil.getBodyItemValue(doc, "RESERVEDQUANTITY", true);
		String completeQuantity = SMessageUtil.getBodyItemValue(doc, "COMPLETEQUANTITY", true);
		String position = SMessageUtil.getBodyItemValue(doc, "POSITION", false);
		String eventName = SMessageUtil.getBodyItemValue(doc, "EVENTNAME", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		if(StringUtils.equals(eventName, "CreateReserveProduct"))
		{
			eventInfo.setEventName("Create");
			
			List<ReserveProduct> reserveProductList = null;
			try
			{
				String condition = " WHERE machineName = ? AND processOperationGroupName = ? AND processOperationName = ?"
						+ " AND productSpecName = ? ORDER BY TO_NUMBER(position) DESC";
				Object[] bindSet = new Object[]{machineName, processOperationGroupName, processOperationName, productSpecName};
				reserveProductList = ExtendedObjectProxy.getReserveProductService().select(condition, bindSet);
				
				position = reserveProductList.get(0).getPosition();
			}
			catch(Exception ex)
			{
				
			}
			
			if(reserveProductList == null)
			{
				position = "0";
			}
			
			ReserveProduct reserveProductData = new ReserveProduct();
			reserveProductData.setMachineName(machineName);
			reserveProductData.setProcessOperationGroupName(processOperationGroupName);
			reserveProductData.setProcessOperationName(processOperationName);
			reserveProductData.setProductSpecName(productSpecName);
			reserveProductData.setPosition(position);
			reserveProductData.setReserveState("Reserved");
			reserveProductData.setReservedQuantity(reserveQuantity);
			reserveProductData.setCompleteQuantity(completeQuantity);
			
			ExtendedObjectProxy.getReserveProductService().create(eventInfo, reserveProductData);
		}
		if(StringUtils.equals(eventName, "DeleteReserveProduct"))
		{
			ReserveProduct reserveProductData = ExtendedObjectProxy.getReserveProductService().selectByKey(false,
					new Object[] {machineName, processOperationGroupName, processOperationName, productSpecName});
			
			ExtendedObjectProxy.getReserveProductService().remove(eventInfo, reserveProductData);
		}
		if(StringUtils.equals(eventName, "CompleteReserveProduct"))
		{
			ReserveProduct reserveProductData = ExtendedObjectProxy.getReserveProductService().selectByKey(false,
					new Object[] {machineName, processOperationGroupName, processOperationName, productSpecName});
			
			reserveProductData.setReserveState("Completed");
			
			ExtendedObjectProxy.getReserveProductService().modify(eventInfo, reserveProductData);
		}
		
		
		return doc;
	}

}
