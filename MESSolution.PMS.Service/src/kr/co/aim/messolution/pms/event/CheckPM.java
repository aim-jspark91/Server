package kr.co.aim.messolution.pms.event;

import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.MaintenanceCheck;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class CheckPM extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String maintenanceID     = SMessageUtil.getBodyItemValue(doc, "MAINTENANCEID", true);
		//String maintStatus 		 = SMessageUtil.getBodyItemValue(doc, "MAINTSTATUS", true);		
		//String checkUser 	 	 = SMessageUtil.getBodyItemValue(doc, "CHECKUSER", true);
		String checkResult 	 	 = SMessageUtil.getBodyItemValue(doc, "CHECKRESULT", true);
	
		List<Element> CheckItemList = SMessageUtil.getBodySequenceItemList(doc, "CHECKITEMLIST", true);
		
		EventInfo eventInfo      = EventInfoUtil.makeEventInfo("CheckPM", getEventUser(), getEventComment(), null, null);
		/*	
		//Delete Info
		List<CompleteCheckPM> CompleteCheckPMData = null;
		try
		{
			CompleteCheckPMData = PMSServiceProxy.getCompleteCheckPMService().select("MAINTENANCEID = ? ", new Object[] {maintenanceID});			
		}
		catch (Exception ex)
		{
			eventLog.error(String.format("<<<<<<<<<<<<<<< No Data this maintenanceID = %s", maintenanceID));
		}
		
		if(CompleteCheckPMData != null)
		{
			for(CompleteCheckPM completeCheckPMData : CompleteCheckPMData)
			{
				String checkID    = completeCheckPMData.getCheckID();
				String checkNo    = completeCheckPMData.getCheckNo();
				
				CompleteCheckPM completeCheckPM = new CompleteCheckPM(maintenanceID,checkID,checkNo);
				
				try
				{				
					
				    PMSServiceProxy.getCompleteCheckPMService().delete(completeCheckPM);
				}
				catch(Exception ex)
				{
					throw new CustomException("PMS-0083", maintenanceID,checkID);
				}
			}							
		}
		*/
		
		//Modify
		for(Element checkItemList : CheckItemList)
		{
			
			String CheckNo	  = SMessageUtil.getChildText(checkItemList, "NO", true);
			String CheckID	  = SMessageUtil.getChildText(checkItemList, "CHECKID", true);
			String ItemType	  = SMessageUtil.getChildText(checkItemList, "ITEMTYPE", true);
			String ItemName	  = SMessageUtil.getChildText(checkItemList, "ITEMNAME", true);
			//String CheckDesc	  = SMessageUtil.getChildText(checkItemList, "CHECKDESC", true);
			
			String PartID	  = SMessageUtil.getChildText(checkItemList, "PARTID", false);
			String PartName	  = SMessageUtil.getChildText(checkItemList, "PARTNAME", false);
			String UseQty	  = SMessageUtil.getChildText(checkItemList, "USEQUANTITY", false);
			
			
			//CompleteCheckPM completeCheckPMData = new CompleteCheckPM(maintenanceID,CheckID,CheckNo);
			MaintenanceCheck completeCheckPMData = 
					PMSServiceProxy.getCompleteCheckPMService().selectByKey(true, new Object[] {maintenanceID,CheckID,CheckNo});
			
			completeCheckPMData.setItemType(ItemType);
			completeCheckPMData.setItemName(ItemName);
			//completeCheckPMData.setCheckDesc(CheckDesc);
			completeCheckPMData.setPartID(PartID);
			completeCheckPMData.setPartName(PartName);
			completeCheckPMData.setUseQuantity(UseQty);
			completeCheckPMData.setCheckResult(checkResult);
					
			try
			{						
			    PMSServiceProxy.getCompleteCheckPMService().modify(eventInfo,completeCheckPMData);
			}
			catch(Exception ex)
			{
				throw new CustomException("PMS-0086", CheckID);
			}								
		}
		return doc;
	}
}
