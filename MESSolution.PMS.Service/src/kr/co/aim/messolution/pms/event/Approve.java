package kr.co.aim.messolution.pms.event;

import java.sql.Timestamp;
import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
//import kr.co.aim.messolution.extended.object.management.data.Purchase;
import kr.co.aim.messolution.pms.management.data.Purchase;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class Approve extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String CancelApprove = SMessageUtil.getBodyItemValue(doc, "CANCELAPPROVE", false); //Distinguish Approve and Cancel Approve	
		
		String CancelComment  = SMessageUtil.getBodyItemValue(doc, "CANCELCOMMENT", false);
		
		String PoCode = "";//SMessageUtil.getBodyItemValue(doc, "POCODE", true);
		String PartID = "";//SMessageUtil.getBodyItemValue(doc, "PARTID", true);	
		
		EventInfo eventInfo = null ;
		
		if(CancelApprove.equals("CANCEL"))
		{
			PoCode = SMessageUtil.getBodyItemValue(doc, "POCODE", true);
			PartID = SMessageUtil.getBodyItemValue(doc, "PARTID", true);
			String cancelFlag    = SMessageUtil.getBodyItemValue(doc, "CANCELFLAG", true);
			
			eventInfo  = EventInfoUtil.makeEventInfo("CancelApprove", getEventUser(), getEventComment(), null, null);
			
			Purchase purchase = null;	
			//get
			purchase = PMSServiceProxy.getPurchaseService().selectByKey(true, new Object[] {PoCode});
			
			String PurchaseType     = purchase.getPurchaseType();
			String AvailablePeriod  = purchase.getAvailablePeriod();
			Timestamp InRequestDate = purchase.getInRequestDate();
			Number PurchaseQuantity = purchase.getPurchaseQuantity();
			String PurchaseUnit     = purchase.getPurchaseUnit();
			String PurchaseReason   = purchase.getPurchaseReason();
			String PurchaseStatus   = purchase.getPurchaseStatus();
			Timestamp InExpectDate  = purchase.getInExpectdate();	
			String PhoneNumber      = purchase.getPhoneNumber();	
	        String Status           = "";
			String CreateUser       = purchase.getCreateUser();
			Timestamp CreateTime    = purchase.getCreateTime();
			//String CancelComment    = purchase.getCancelComment();
			//String CancelFlag       = purchase.getCancelFlag();
	        
	        if(CancelApprove.equals("CANCEL"))
	        {
	        	if(PurchaseStatus.equals("WaitingDirector") || PurchaseStatus.equals("ItemManager") || PurchaseStatus.equals("WaitingPurchase")||
	        	   PurchaseStatus.equals("Purchasing") || PurchaseStatus.equals("Modified"))
	        	{
	        		Status = "Created";  
	        	}      		  		
	        }
	        else
	        {
	    		if(PurchaseStatus.equals("Created"))
	    			Status = "WaitingDirector";
	    		else if(PurchaseStatus.equals("WaitingDirector"))
	    			Status = "ItemManager";
	    		else if(PurchaseStatus.equals("ItemManager"))
	    			Status = "WaitingPurchase";
	    		else if(PurchaseStatus.equals("Modified"))
	    			Status = "Purchasing";
	        }
	        
			//set
			purchase = new Purchase(PoCode);
			purchase.setPartID(PartID);
			purchase.setPurchaseType(PurchaseType);
			purchase.setAvailablePeriod(AvailablePeriod);
			purchase.setInRequestDate(InRequestDate);
			purchase.setPurchaseQuantity(PurchaseQuantity);
			purchase.setPurchaseUnit(PurchaseUnit);
			purchase.setPurchaseReason(PurchaseReason);
			purchase.setInExpectdate(InExpectDate);
			purchase.setPurchaseStatus(Status);
			purchase.setPhoneNumber(PhoneNumber);
			purchase.setCreateUser(CreateUser);
			purchase.setCreateTime(CreateTime);
			purchase.setCancelComment(CancelComment);
			purchase.setCancelFlag(cancelFlag);
			
			try
			{
				purchase = PMSServiceProxy.getPurchaseService().modify(eventInfo, purchase);
			}
			catch(Exception ex)
			{
				throw new CustomException("PMS-0059", PoCode);
			}
		}
		else
		{
			List<Element> APPROVELIST = SMessageUtil.getBodySequenceItemList(doc, "APPROVELIST", true);
			
			eventInfo  = EventInfoUtil.makeEventInfo("Approve", getEventUser(), getEventComment(), null, null);
			
			for(Element approve:APPROVELIST)
			{
				PoCode = SMessageUtil.getChildText(approve, "POCODE", true);
				PartID = SMessageUtil.getChildText(approve, "PARTID", true);	
				
				Purchase purchase = null;	
				//get
				purchase = PMSServiceProxy.getPurchaseService().selectByKey(true, new Object[] {PoCode});
				
				String PurchaseType     = purchase.getPurchaseType();
				String AvailablePeriod  = purchase.getAvailablePeriod();
				Timestamp InRequestDate = purchase.getInRequestDate();
				Number PurchaseQuantity = purchase.getPurchaseQuantity();
				String PurchaseUnit     = purchase.getPurchaseUnit();
				String PurchaseReason   = purchase.getPurchaseReason();
				String PurchaseStatus   = purchase.getPurchaseStatus();
				Timestamp InExpectDate  = purchase.getInExpectdate();	
				String PhoneNumber      = purchase.getPhoneNumber();
		        String Status           = "";
		        String CreateUser       = purchase.getCreateUser();
				Timestamp CreateTime    = purchase.getCreateTime();
				String CancelFlag       = purchase.getCancelFlag();
				//String CancelComment    = purchase.getCancelComment();
				
				if(CancelFlag.equals("Y"))
				{
					CancelFlag = "N";
					CancelComment = "";
				}
										
		        if(CancelApprove.equals("CANCEL"))
		        {
		        	if(PurchaseStatus.equals("WaitingDirector") || PurchaseStatus.equals("ItemManager") || PurchaseStatus.equals("WaitingPurchase")||
		        	   PurchaseStatus.equals("Purchasing") || PurchaseStatus.equals("Modified"))
		        	{
		        		Status = "Created";  
		        	}      		  		
		        }
		        else
		        {
		    		if(PurchaseStatus.equals("Created"))
		    			Status = "WaitingDirector";
		    		else if(PurchaseStatus.equals("WaitingDirector"))
		    			Status = "ItemManager";
		    		else if(PurchaseStatus.equals("ItemManager"))
		    			Status = "WaitingPurchase";
		    		else if(PurchaseStatus.equals("Modified"))
		    			Status = "Purchasing";
		        }
		        
				//set
				purchase = new Purchase(PoCode);
				purchase.setPartID(PartID);
				purchase.setPurchaseType(PurchaseType);
				purchase.setAvailablePeriod(AvailablePeriod);
				purchase.setInRequestDate(InRequestDate);
				purchase.setPurchaseQuantity(PurchaseQuantity);
				purchase.setPurchaseUnit(PurchaseUnit);
				purchase.setPurchaseReason(PurchaseReason);
				purchase.setInExpectdate(InExpectDate);
				purchase.setPurchaseStatus(Status);
				purchase.setPhoneNumber(PhoneNumber);
				purchase.setCreateUser(CreateUser);
				purchase.setCreateTime(CreateTime);
				purchase.setCancelComment(CancelComment);
				purchase.setCancelFlag(CancelFlag);
				
				
				try
				{
					purchase = PMSServiceProxy.getPurchaseService().modify(eventInfo, purchase);
				}
				catch(Exception ex)
				{
					throw new CustomException("PMS-0059", PoCode);
				}
			}
			
		}				
		return doc;
	}
}
