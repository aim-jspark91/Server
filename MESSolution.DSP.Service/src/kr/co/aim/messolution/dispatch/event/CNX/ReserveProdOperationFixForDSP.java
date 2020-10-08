package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveProductFix;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class ReserveProdOperationFixForDSP extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		List<Element> eleReserveList = SMessageUtil.getBodySequenceItemList(doc, "RESERVELIST", false);
		//huhaifeng
		Set<ReserveProductFix> addList = new HashSet<>() ;
		Set<ReserveProductFix> updateList = new HashSet<>() ;
		
		/*	EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", ""); */  //  annotate  by Hu Haifeng 2016/12/21 17:00
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		//huhaifeng
		List<ReserveProductFix> reservedProductFixList = null ;
		List<ReserveProductFix> alreadyreserveProductList = null ;
		try           
		{
			
			String condition = " WHERE machineName = ? ";
			Object[] bindSet = new Object[]{machineName};
			
			reservedProductFixList = ExtendedObjectProxy.getReserveProductFixService().select(condition, bindSet);
		    alreadyreserveProductList = ExtendedObjectProxy.getReserveProductFixService().select(condition, bindSet);
		//	
			/*		for(ReserveProductFix reservedProductData : reservedProductFixList)   //  annotate  by Hu Haifeng 2016/12/21 17:00
			{
				eventInfo.setEventName("Delete");  // Modify Delete to CancelProOperationFix by Hu Haifeng 2016/12/22 10:34
				ExtendedObjectProxy.getReserveProductFixService().remove(eventInfo, reservedProductData);
			}
		//		*/
		}
		catch(Exception ex)
		{
			
		}
	//  add by Huhaifeng 20170103
		label1:for(Element elementReserveData : eleReserveList)
		{
		//List<ReserveProductFix> clientreserveProductFixList = null ;  annotate by huhaifeng 20170103
		          
			String position = SMessageUtil.getChildText(elementReserveData, "POSITION", true);
//			String factoryName = SMessageUtil.getChildText(elementReserveData, "FACTORYNAME", true);
			String productSpecName = SMessageUtil.getChildText(elementReserveData, "PRODUCTSPECNAME", true);
			String processFlowName = SMessageUtil.getChildText(elementReserveData, "PROCESSFLOWNAME", true);
			String dspFlag = SMessageUtil.getChildText(elementReserveData, "DSPFLAG", false);
			String processOperationName = SMessageUtil.getChildText(elementReserveData, "PROCESSOPERATIONNAME", true);
			String subMachineName = SMessageUtil.getChildText(elementReserveData, "MACHINENAME", true);
			String processOperationGroupName = SMessageUtil.getChildText(elementReserveData, "PROCESSOPERATIONGROUPNAME", false);
			
			if(processOperationGroupName.isEmpty() || processOperationGroupName == null)
			{
				processOperationGroupName = "NONE";
			}
			
			try
			{
				ReserveProductFix clientreserveProductData = new ReserveProductFix(); 
				clientreserveProductData.setPosition(position);
				clientreserveProductData.setProductSpecName(productSpecName);
				clientreserveProductData.setProcessFlowName(processFlowName);
				clientreserveProductData.setProcessOperationName(processOperationName);
				clientreserveProductData.setMachineName(subMachineName);
				clientreserveProductData.setProcessOperationGroupName(processOperationGroupName);
				clientreserveProductData.setReserveState("Executing");
				clientreserveProductData.setReservedQuantity("0");
				clientreserveProductData.setCompleteQuantity("0");
	            //eventInfo.setEventName("ReserveProOperationFix");  // Add by Hu Haifeng 2016/12/22 10:34
				//ExtendedObjectProxy.getReserveProductFixService().create(eventInfo, reserveProductData);
	            //huhaifeng
	            //boolean mark = false ;
	        	
				String OperationclientData = clientreserveProductData.getMachineName()+clientreserveProductData.getProductSpecName()
	            		+clientreserveProductData.getProcessOperationGroupName()
	            		+clientreserveProductData.getProcessOperationName()  + clientreserveProductData.getProcessFlowName(); ;
	            String positionData=clientreserveProductData.getPosition();
	            
	            if (alreadyreserveProductList !=null && !alreadyreserveProductList.isEmpty())
	            {
	            	for(ReserveProductFix reserveProoperationData:alreadyreserveProductList)
		            {
	            		String	operationdbList = reserveProoperationData.getMachineName()+reserveProoperationData.getProductSpecName()
			            		+reserveProoperationData.getProcessOperationGroupName()
			            		+reserveProoperationData.getProcessOperationName() +reserveProoperationData.getProcessFlowName() ;
	            		String operationposition=reserveProoperationData.getPosition();
		            	if(OperationclientData.equals(operationdbList) && !positionData.equals(operationposition))
		            	{ 
		            		updateList.add(clientreserveProductData) ; 
		            		continue label1 ;
		            	}
		            }
		       
		        	
			    }
	          
			  }
		    
			catch(Exception ex)
			{
				
			}			
		}
		
		//  add by huhaifeng 20161229
		label:for(Element eleReserveData : eleReserveList)
		{
			String position = SMessageUtil.getChildText(eleReserveData, "POSITION", true);
//			String factoryName = SMessageUtil.getChildText(eleReserveData, "FACTORYNAME", true);
			String productSpecName = SMessageUtil.getChildText(eleReserveData, "PRODUCTSPECNAME", true);
			String processFlowName = SMessageUtil.getChildText(eleReserveData, "PROCESSFLOWNAME", true);
			String dspFlag = SMessageUtil.getChildText(eleReserveData, "DSPFLAG", false);
			String processOperationName = SMessageUtil.getChildText(eleReserveData, "PROCESSOPERATIONNAME", true);
			String subMachineName = SMessageUtil.getChildText(eleReserveData, "MACHINENAME", true);
			String processOperationGroupName = SMessageUtil.getChildText(eleReserveData, "PROCESSOPERATIONGROUPNAME", false);
			
			if(processOperationGroupName.isEmpty() || processOperationGroupName == null)
			{
				processOperationGroupName = "NONE";
			}
			
          	//	ReserveProductFix reserveData = null;  annotate by huhaifeng 20170103
			try
			{
				ReserveProductFix reserveProductData = new ReserveProductFix(); 
				reserveProductData.setPosition(position);
				reserveProductData.setProductSpecName(productSpecName);
				reserveProductData.setProcessFlowName(processFlowName);
				reserveProductData.setProcessOperationName(processOperationName);
				reserveProductData.setMachineName(subMachineName);
				reserveProductData.setProcessOperationGroupName(processOperationGroupName);
				reserveProductData.setReserveState("Executing");
				reserveProductData.setReservedQuantity("0");
				reserveProductData.setCompleteQuantity("0");
	            //eventInfo.setEventName("ReserveProOperationFix");  // Add by Hu Haifeng 2016/12/22 10:34
				//ExtendedObjectProxy.getReserveProductFixService().create(eventInfo, reserveProductData);
	            //huhaifeng
	            String clientData = reserveProductData.getMachineName()+reserveProductData.getProductSpecName()
	            		+reserveProductData.getProcessOperationGroupName()
	            		+reserveProductData.getProcessOperationName()  + reserveProductData.getProcessFlowName() ;
	            String dbList = "" ;
	            //boolean mark = false ;
	        	if (reservedProductFixList!=null && !reservedProductFixList.isEmpty())
	          {	
	            for(ReserveProductFix reserveProData:reservedProductFixList)
	            {
	            	dbList = reserveProData.getMachineName()+reserveProData.getProductSpecName()
		            		+reserveProData.getProcessOperationGroupName()
		            		+reserveProData.getProcessOperationName() + reserveProData.getProcessFlowName() ;
	            	if(clientData.equals(dbList))
	            	{
	            		reservedProductFixList.remove(reserveProData) ; 
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
		//huhaifeng
		
		if (reservedProductFixList!=null && !reservedProductFixList.isEmpty())
		{
		for(ReserveProductFix reservedProductData : reservedProductFixList)   //  annotate  by Hu Haifeng 2016/12/21 17:00
		  {
			eventInfo.setEventName("Delete");  // Modify Delete to CancelProOperationFix by Hu Haifeng 2016/12/22 10:34
			ExtendedObjectProxy.getReserveProductFixService().remove(eventInfo, reservedProductData);
		  }
		}
		
		for(ReserveProductFix clientAddData:addList){
			eventInfo.setEventName("Create");
			ExtendedObjectProxy.getReserveProductFixService().create(eventInfo, clientAddData);
		}
		
		for(ReserveProductFix clientUpdateData:updateList){
			eventInfo.setEventName("Change Position");
			ExtendedObjectProxy.getReserveProductFixService().modify(eventInfo, clientUpdateData);
		}
		
		return doc;
	}

}
