package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.WorkOrderPriority;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class ReserveWorkOrderForDSP extends SyncHandler {   // Modify  By Hu Haifeng 20170122

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		List<Element> eleReserveList = SMessageUtil.getBodySequenceItemList(doc, "RESERVELIST", false);
		
		Set<WorkOrderPriority> addList = new HashSet<>() ;
		Set<WorkOrderPriority> updateList = new HashSet<>() ;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		
		List<WorkOrderPriority> reservedWorkOrderList = null ;
		List<WorkOrderPriority> alreadyreserveWorkOrderList = null ;
		try
		{
			
			String condition = " WHERE factoryName = ? ";
			Object[] bindSet = new Object[]{factoryName};
			
			reservedWorkOrderList = ExtendedObjectProxy.getWorkOrderPriorityService().select(condition, bindSet);
			alreadyreserveWorkOrderList = ExtendedObjectProxy.getWorkOrderPriorityService().select(condition, bindSet);
			
		/*	List<WorkOrderPriority> workOrderPriorityList = ExtendedObjectProxy.getWorkOrderPriorityService().select(condition, bindSet);
			
			for(WorkOrderPriority workOrderPriorityData : workOrderPriorityList)
			{
				eventInfo.setEventName("Delete");
				ExtendedObjectProxy.getWorkOrderPriorityService().remove(eventInfo, workOrderPriorityData);
			}
			*/
		}
		catch(Exception ex)
		{
			
		}
		
		label1:for(Element elementReserveData : eleReserveList)
		{
			
			long seq = Long.parseLong(SMessageUtil.getChildText(elementReserveData, "SEQ", true));
			String productRequestName = SMessageUtil.getChildText(elementReserveData, "PRODUCTREQUESTNAME", true);
	   //   String factoryName = SMessageUtil.getChildText(elementReserveData, "FACTORYNAME", true);
			String productSpecName = SMessageUtil.getChildText(elementReserveData, "PRODUCTSPECNAME", false);
			String dspFlag = SMessageUtil.getChildText(elementReserveData, "DSPFLAG", false);
			String processFlowName = SMessageUtil.getChildText(elementReserveData, "PROCESSFLOWNAME", false);
			
			try
			{
				WorkOrderPriority clientReserveworkOrderPriorityData = new WorkOrderPriority(); 
				clientReserveworkOrderPriorityData.setPriority(seq);
				clientReserveworkOrderPriorityData.setWorkName(productRequestName);
				clientReserveworkOrderPriorityData.setFactoryName(factoryName);				
				
				//ExtendedObjectProxy.getWorkOrderPriorityService().create(eventInfo, workOrderPriorityData);
				String OperationclientData = clientReserveworkOrderPriorityData.getFactoryName()+clientReserveworkOrderPriorityData.getWorkName();
	            		
				long a=clientReserveworkOrderPriorityData.getPriority();   
	              
	            if (alreadyreserveWorkOrderList !=null && !alreadyreserveWorkOrderList.isEmpty())
	            {
	            	for(WorkOrderPriority reserveProoperationData:alreadyreserveWorkOrderList)
		            {
	            		String	operationdbList = reserveProoperationData.getFactoryName()+reserveProoperationData.getWorkName() ;
			          		
	            		long b=reserveProoperationData.getPriority();
	        
		            	if(OperationclientData.equals(operationdbList) && (a!=b))
		            	{ 
		            		updateList.add(clientReserveworkOrderPriorityData) ; 
		            		continue label1 ;
		            	}
		            }
		       
		        	
			    }
	            
			}
			catch(Exception ex)
			{
				
			}			
		}
				
			label:for(Element eleReserveData : eleReserveList)
			{
				long seq = Long.parseLong(SMessageUtil.getChildText(eleReserveData, "SEQ", true));
				String productRequestName = SMessageUtil.getChildText(eleReserveData, "PRODUCTREQUESTNAME", true);
//				String factoryName = SMessageUtil.getChildText(eleReserveData, "FACTORYNAME", true);
				String productSpecName = SMessageUtil.getChildText(eleReserveData, "PRODUCTSPECNAME", false);
				String dspFlag = SMessageUtil.getChildText(eleReserveData, "DSPFLAG", false);
				String processFlowName = SMessageUtil.getChildText(eleReserveData, "PROCESSFLOWNAME", false);
				
				try
				{			
					WorkOrderPriority reserveworkOrderData = new WorkOrderPriority(); 
					reserveworkOrderData.setPriority(seq);
					reserveworkOrderData.setWorkName(productRequestName);
					reserveworkOrderData.setFactoryName(factoryName);
		            //huhaifeng
		            String clientData = reserveworkOrderData.getFactoryName()+reserveworkOrderData.getWorkName() ;
		 
		            String dbList = "" ;
		            //boolean mark = false ;
		        	if (reservedWorkOrderList!=null && !reservedWorkOrderList.isEmpty())
		          {	
		            for(WorkOrderPriority reserveProData:reservedWorkOrderList)
		            {
		            	dbList = reserveProData.getFactoryName()+reserveProData.getWorkName() ;
		            	if(clientData.equals(dbList))
		            	{
		            		reservedWorkOrderList.remove(reserveProData) ; 
		            		continue label ;
		            	}
		            }
		            addList.add(reserveworkOrderData) ;
				  }
		        	else addList.add(reserveworkOrderData) ;
			    }
				catch(Exception ex)
				{
					
				}			
			}
			
			if (reservedWorkOrderList!=null && !reservedWorkOrderList.isEmpty())
			{
			for(WorkOrderPriority reservedWorkOrderData : reservedWorkOrderList) 
			  {
				eventInfo.setEventName("Delete"); 
				ExtendedObjectProxy.getWorkOrderPriorityService().remove(eventInfo, reservedWorkOrderData);
			  }
			}
			
			for(WorkOrderPriority clientAddData:addList){
				eventInfo.setEventName("Create");
				ExtendedObjectProxy.getWorkOrderPriorityService().create(eventInfo, clientAddData);
			}
			
			for(WorkOrderPriority clientUpdateData:updateList){
				eventInfo.setEventName("Change Position");
				ExtendedObjectProxy.getWorkOrderPriorityService().modify(eventInfo, clientUpdateData);
			}	
			
		return doc;
	}

}
