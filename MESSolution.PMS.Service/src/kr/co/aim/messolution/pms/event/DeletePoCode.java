package kr.co.aim.messolution.pms.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.BulletinBoard;
import kr.co.aim.messolution.pms.management.data.BulletinBoardArea;
import kr.co.aim.messolution.pms.management.data.Purchase;
import kr.co.aim.messolution.pms.management.data.SparePart;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class DeletePoCode extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String PoCode 		 = SMessageUtil.getBodyItemValue(doc, "POCODE", true);
		String PartID        = SMessageUtil.getBodyItemValue(doc, "PARTID", true);
		String PurchaseQty   = SMessageUtil.getBodyItemValue(doc, "PURCHASEQUANTITY", true);

		EventInfo eventInfo  = EventInfoUtil.makeEventInfo("DeletePurchase", getEventUser(), getEventComment(), null, null);
								
												
		try
		{						
			Purchase purchaseData = PMSServiceProxy.getPurchaseService().selectByKey(true, new Object[] {PoCode});
			PMSServiceProxy.getPurchaseService().remove(eventInfo, purchaseData);		
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0061", PoCode);
		}	
		
		//SparePart	
	    try
		{
		//Get
		SparePart sparePartData = PMSServiceProxy.getSparePartService().selectByKey(true, new Object[] {PartID});
						
		String partName            = sparePartData.getPartName();
		String groupName           = sparePartData.getGroupName();
		String partSpec            = sparePartData.getPartSpec(); 
		String location            = sparePartData.getLocation(); 
		Number quantity            = sparePartData.getQuantity(); 
		Number safeQuantity        = sparePartData.getSafeQuantity(); 
		Number notInQuantity       = sparePartData.getNotInQuantity(); 
		Number purchaseCompleteQty = sparePartData.getPurchaseCompleteQty(); 
		String vendorId            = sparePartData.getVendorID(); 
		String unit                = sparePartData.getUnit(); 
		Number unitPrice           = sparePartData.getUnitPrice(); 
		Number useFrequency        = sparePartData.getUseFrequency(); 
		String purchaseCycle       = sparePartData.getPurchaseCycle(); 
		String partDesc            = sparePartData.getPartDescription(); 
		String useDesc             = sparePartData.getUseDescription(); 
		String partType            = sparePartData.getPartType(); 
		String partAttribute       = sparePartData.getPartAttribute(); 
		String partGroup           = sparePartData.getPartGroup(); 
		Number orderQty            = sparePartData.getOrderQuantity();
		Number warningQty          = sparePartData.getWarningQuantity();
		String materialCode        = sparePartData.getMaterialCode(); 

		int realNotInQty = notInQuantity.intValue() - Integer.parseInt(PurchaseQty);
		//Set
		sparePartData = new SparePart(PartID);
		sparePartData.setPartID(PartID);
		sparePartData.setPartName(partName);
		sparePartData.setGroupName(groupName);
		sparePartData.setPartSpec(partSpec);
		sparePartData.setLocation(location);
		sparePartData.setQuantity(quantity);
		sparePartData.setSafeQuantity(safeQuantity);
		sparePartData.setNotInQuantity(realNotInQty);
		sparePartData.setPurchaseCompleteQty(purchaseCompleteQty);
		sparePartData.setVendorID(vendorId);
		sparePartData.setUnit(unit);
		sparePartData.setUnitPrice(unitPrice);
		sparePartData.setUseFrequency(useFrequency);
		sparePartData.setPurchaseCycle(purchaseCycle);
		sparePartData.setPartDescription(partDesc);
		sparePartData.setUseDescription(useDesc);
		sparePartData.setPartType(partType);
		sparePartData.setPartAttribute(partAttribute);
		sparePartData.setPartGroup(partGroup);
		sparePartData.setOrderQuantity(orderQty);
		sparePartData.setWarningQuantity(warningQty);
		sparePartData.setMaterialCode(materialCode);
							
		eventInfo.setEventName("");
							
		sparePartData = PMSServiceProxy.getSparePartService().modify(eventInfo, sparePartData); 
		eventLog.error(String.format( "<<<<<<<<<<<<<<<<< Success SparePart modify >>>>>>>>>>>>>>>>>>>>>>"));			
	}
	catch (Exception ex)
	{
		throw new CustomException("PMS-0057", PartID);
	}    
				
	    InsertBoard(PoCode,getEventUser()); // Inser Modify data to Board.
	    
		return doc;		
	}
	
	public void InsertBoard(String POCode, String User) throws CustomException
	{
		String [] scopeShopList = new String[]{"Administrator","AMOLED General Manager","AM EQP","AMOLED Design Department","AMOLED EN Production","AMOLED EV Production","AMOLED LTPS Production","MaterialControl","Purchase"
                ,"Facility Department","TFT General Manager","TFT EQP","TFT Array Production","TFT CELL Production","TFT CF Production","TFT 1st technical Department","TFT 2st technical Department","AMOLED 1st technical Department","AMOLED 2st technical Department"};
		
		String no;
		try
		{
			String currentTime = TimeUtils.getCurrentEventTimeKey();		
		    no = currentTime.substring(2, 14) + currentTime.substring(17);			
		}
		catch(Exception ex)
		{
			throw new CustomException("MOD-0002");
		}	
		
		try
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyBoardPMS", getEventUser(), getEventComment(), null, null);
			
			String title = "PoCode Delete";
			String comments = "Pocode: " + " [" + POCode +"] " + " was deleted by " + " [ " + User + " ] " +"." + "\n" + "You can check detail info with PurchaseHistory Function.";
			
			//Create
			BulletinBoard boardData = new BulletinBoard("Administrator", no);
			boardData.setTitle(title);
			boardData.setCreateTime(eventInfo.getEventTime());
			boardData.setCreateUser(eventInfo.getEventUser());
			boardData.setComments(comments);
			
			boardData = PMSServiceProxy.getBulletinBoardService().create(eventInfo, boardData);
			
			for(String eleshop : scopeShopList)
			{
				String scopeShopName = eleshop.toString();
				
				BulletinBoardArea boardAreaData = new BulletinBoardArea(scopeShopName, no);
				boardAreaData = PMSServiceProxy.getBulletinBoardAreaService().create(eventInfo, boardAreaData);
			}
		}
		catch(Exception ex)
		{
			throw new CustomException("MOD-0002");
		}		
	}
}
