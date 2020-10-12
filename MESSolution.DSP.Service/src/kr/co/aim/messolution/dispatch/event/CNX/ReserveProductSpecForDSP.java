package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kr.co.aim.messolution.dispatch.MESDSPServiceProxy;
import kr.co.aim.messolution.dispatch.management.data.ProductSpecPriority;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class ReserveProductSpecForDSP extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		List<Element> eleReserveList = SMessageUtil.getBodySequenceItemList(doc, "RESERVELIST", false);
		
		Set<ProductSpecPriority> addList = new HashSet<>() ;
		Set<ProductSpecPriority> updateList = new HashSet<>() ;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		List<ProductSpecPriority> reservedProductSpecList = null ;
		List<ProductSpecPriority> alreadyreserveProductSpecList = null ;
		
		try
		{
			
			String condition = " WHERE factoryName = ? ";
			Object[] bindSet = new Object[]{factoryName};
			
			reservedProductSpecList = MESDSPServiceProxy.getProductSpecPriorityService().select(condition, bindSet);
			alreadyreserveProductSpecList = MESDSPServiceProxy.getProductSpecPriorityService().select(condition, bindSet);
			
		/*	List<ProductSpecPriority> reserveProductSpecPriorityList = MESDSPServiceProxy.getProductSpecPriorityService().select(condition, bindSet);
			for(ProductSpecPriority reserveProductSpecData : reserveProductSpecPriorityList)
			{
				eventInfo.setEventName("Delete");
				MESDSPServiceProxy.getProductSpecPriorityService().delete(reserveProductSpecData);
			}
			*/
		}
		catch(Exception ex)
		{
			
		}
		
		label1:for(Element elementReserveData : eleReserveList)
		{
			String position = SMessageUtil.getChildText(elementReserveData, "POSITION", true);
//			String factoryName = SMessageUtil.getChildText(eleReserveData, "FACTORYNAME", true);
			String productSpecName = SMessageUtil.getChildText(elementReserveData, "PRODUCTSPECNAME", true);
			String dspFlag = SMessageUtil.getChildText(elementReserveData, "DSPFLAG", false);
			String productionType = SMessageUtil.getChildText(elementReserveData, "PRODUCTIONTYPE", true);
			String productType = SMessageUtil.getChildText(elementReserveData, "PRODUCTTYPE", true);
			String productQuantity = SMessageUtil.getChildText(elementReserveData, "PRODUCTQUANTITY", false);
			
		//	ProductSpecPriority reserveData = null;
			try
			{
				ProductSpecPriority clientreserveProductSpecData = new ProductSpecPriority(); 
				clientreserveProductSpecData.setPriority(Long.parseLong(position));
				clientreserveProductSpecData.setProductSpecName(productSpecName);
				clientreserveProductSpecData.setFactoryName(factoryName);
				
				
				String OperationclientData = clientreserveProductSpecData.getFactoryName()+clientreserveProductSpecData.getProductSpecName();
	            		
	            Number positionData=clientreserveProductSpecData.getPriority();   
	               int a= positionData.intValue();
	            if (alreadyreserveProductSpecList !=null && !alreadyreserveProductSpecList.isEmpty())
	            {
	            	for(ProductSpecPriority reserveProoperationData:alreadyreserveProductSpecList)
		            {
	            		String	operationdbList = reserveProoperationData.getFactoryName()+reserveProoperationData.getProductSpecName() ;
			          		
	            		Number operationposition=reserveProoperationData.getPriority();
	            		int b= operationposition.intValue();
		            	if(OperationclientData.equals(operationdbList) && (a!=b))
		            	{ 
		            		updateList.add(clientreserveProductSpecData) ; 
		            		continue label1 ;
		            	}
		            }
		       
		        	
			    }
	            
				/* eventInfo.setEventName("Create");
				
				MESDSPServiceProxy.getProductSpecPriorityService().create(eventInfo, reserveProductData); */ // annotate by Huhaifeng 20170118
			}
			catch(Exception ex)
			{
				
			}			
		}
		
		//  add by huhaifeng 20170118
		label:for(Element eleReserveData : eleReserveList)
		{
			String position = SMessageUtil.getChildText(eleReserveData, "POSITION", true);
//			String factoryName = SMessageUtil.getChildText(eleReserveData, "FACTORYNAME", true);
			String productSpecName = SMessageUtil.getChildText(eleReserveData, "PRODUCTSPECNAME", true);
			String dspFlag = SMessageUtil.getChildText(eleReserveData, "DSPFLAG", false);
			String productionType = SMessageUtil.getChildText(eleReserveData, "PRODUCTIONTYPE", true);
			String productType = SMessageUtil.getChildText(eleReserveData, "PRODUCTTYPE", true);
			String productQuantity = SMessageUtil.getChildText(eleReserveData, "PRODUCTQUANTITY", false);
			
			try
			{			
				ProductSpecPriority reserveProductData = new ProductSpecPriority(); 
				reserveProductData.setPriority(Long.parseLong(position));
				reserveProductData.setProductSpecName(productSpecName);
				reserveProductData.setFactoryName(factoryName);
	            //huhaifeng
	            String clientData = reserveProductData.getFactoryName()+reserveProductData.getProductSpecName() ;
	 
	            String dbList = "" ;
	            //boolean mark = false ;
	        	if (reservedProductSpecList!=null && !reservedProductSpecList.isEmpty())
	          {	
	            for(ProductSpecPriority reserveProData:reservedProductSpecList)
	            {
	            	dbList = reserveProData.getFactoryName()+reserveProData.getProductSpecName() ;
	            	if(clientData.equals(dbList))
	            	{
	            		reservedProductSpecList.remove(reserveProData) ; 
	            		continue label ;
	            	}
	            }
	            addList.add(reserveProductData) ;
			  }
	        	else addList.add(reserveProductData) ;
		    }
			catch(Exception ex)
			{
				
			}			
		}
		
		if (reservedProductSpecList!=null && !reservedProductSpecList.isEmpty())
		{
		for(ProductSpecPriority reservedProductData : reservedProductSpecList) 
		  {
			eventInfo.setEventName("Delete"); 
			MESDSPServiceProxy.getProductSpecPriorityService().remove(eventInfo, reservedProductData);
		  }
		}
		
		for(ProductSpecPriority clientAddData:addList){
			eventInfo.setEventName("Create");
			MESDSPServiceProxy.getProductSpecPriorityService().create(eventInfo, clientAddData);
		}
		
		for(ProductSpecPriority clientUpdateData:updateList){
			eventInfo.setEventName("Change Position");
			MESDSPServiceProxy.getProductSpecPriorityService().modify(eventInfo, clientUpdateData);
		}	
		
		return doc;
	}

}
