package kr.co.aim.messolution.pms.event;

import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.BulletinBoard;
import kr.co.aim.messolution.pms.management.data.BulletinBoardArea;
import kr.co.aim.messolution.pms.management.data.Purchase;
import kr.co.aim.messolution.pms.management.data.RequestSparePart;
import kr.co.aim.messolution.pms.management.data.SparePart;
import kr.co.aim.messolution.pms.management.data.SparePartInOut;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class DeleteSparePart extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String PartID 		 = SMessageUtil.getBodyItemValue(doc, "PARTID", true);
		String CheckInfo     = SMessageUtil.getBodyItemValue(doc, "CHECKINFO", true);
		EventInfo eventInfo  = EventInfoUtil.makeEventInfo("DeleteSparePart", getEventUser(), getEventComment(), null, null);
		
		//PurchasePart
		if(CheckInfo.equals("YES"))
		{
			String CheckPurchase = SMessageUtil.getBodyItemValue(doc, "RESULTPURCHASE", true);
			String CheckRequest = SMessageUtil.getBodyItemValue(doc, "RESULTREQUEST", true);
			String CheckSparepartInOut = SMessageUtil.getBodyItemValue(doc, "RESULTSPAREPARTINOUT", true);
						
			if(CheckPurchase.equals("YES"))
			{
				try
				{
					List<Purchase> PurcahseList = PMSServiceProxy.getPurchaseService().select("PARTID = ?", new Object[] {PartID});
					
					for(Purchase purchaseList: PurcahseList)
					{										
						try
						{						
							PMSServiceProxy.getPurchaseService().remove(eventInfo, purchaseList);
						}
						catch(Exception ex)
						{
							throw new CustomException("PMS-0061", purchaseList.getPoCode().toString());
						}
					}
				}
				catch (Exception ex)
				{
					eventLog.error(String.format("<<<<<<<<<<<<<<< No Data this PARTID = %s", PartID));
				}
			}
			
			//RequestPart
			if(CheckRequest.equals("YES"))
			{
				try
				{
					List<RequestSparePart> RequestList = PMSServiceProxy.getRequestSparePartService().select("PARTID = ?", new Object[] {PartID});
					
					for(RequestSparePart requestList: RequestList)
					{										
						try
						{						
							PMSServiceProxy.getRequestSparePartService().remove(eventInfo, requestList);
						}
						catch(Exception ex)
						{
							throw new CustomException("PMS-0061", requestList.getRequestId().toString());
						}
					}
				}
				catch (Exception ex)
				{
					eventLog.error(String.format("<<<<<<<<<<<<<<< No Data this PARTID = %s", PartID));
				}
			}
			
			//SparePartInOutPart
			if(CheckSparepartInOut.equals("YES"))
			{
				try
				{
					List<SparePartInOut> SparePartInOutList = PMSServiceProxy.getSparePartInOutService().select("PARTID = ?", new Object[] {PartID});
					
					for(SparePartInOut sparePartInOutList: SparePartInOutList)
					{										
						try
						{						
							PMSServiceProxy.getSparePartInOutService().remove(eventInfo, sparePartInOutList);
						}
						catch(Exception ex)
						{
							throw new CustomException("PMS-0061", sparePartInOutList.getOrderNo().toString());
						}
					}
				}
				catch (Exception ex)
				{
					eventLog.error(String.format("<<<<<<<<<<<<<<< No Data this PARTID = %s", PartID));
				}
			}
			
			//SparePart
			try
			{
				SparePart sparepartData = PMSServiceProxy.getSparePartService().selectByKey(true, new Object[] {PartID});
				PMSServiceProxy.getSparePartService().remove(eventInfo, sparepartData);		
			}
			catch(Exception ex)
			{
				throw new CustomException("PMS-0063", PartID);
			}			
		}			
		else
		{
			//SparePart 
			try
			{
				SparePart sparepartData = PMSServiceProxy.getSparePartService().selectByKey(true, new Object[] {PartID});
				PMSServiceProxy.getSparePartService().remove(eventInfo, sparepartData);		
			}
			catch(Exception ex)
			{
				throw new CustomException("PMS-0063", PartID);
			}	
		}
		
		InsertBoard(PartID,getEventUser());  //Inser Modify data to Board.
		
		return doc;		
	}
	
	public void InsertBoard(String PartID, String User) throws CustomException
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
			
			String title = "SparePart Delete";
			String comments = "SparePart: " + " [" + PartID + "] " + " was deleted by " + " [ " + User + " ] " +"." + "\n" + "You can check detail info with SparePartHistory Function.";
			
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
