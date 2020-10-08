package kr.co.aim.messolution.dispatch.event;
import kr.co.aim.messolution.dispatch.MESDSPServiceProxy;
import kr.co.aim.messolution.dispatch.management.data.KANBAN;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class KANBANSave extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("KANBAN", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
 
		
		String StockrName         = SMessageUtil.getBodyItemValue(doc, "StockrName", true);
		String ProductSpecName    = SMessageUtil.getBodyItemValue(doc, "ProductSpecName", true);
//		String ProductSpecVersion = SMessageUtil.getBodyItemValue(doc, "ProductSpecVersion", true);
		String ProcessOperationName = SMessageUtil.getBodyItemValue(doc, "ProcessOperationName", true);
		String KanBan              = SMessageUtil.getBodyItemValue(doc, "KanBan", true);
		String MinKanBan              = SMessageUtil.getBodyItemValue(doc, "MinKanBan", true);
		String KanbanFlag         = SMessageUtil.getBodyItemValue(doc, "KanbanFlag", true);

		String Action         = SMessageUtil.getBodyItemValue(doc, "ACTION", true);
		
		int iKanBan = Integer.parseInt(KanBan);
		int iMinKanBan = Integer.parseInt(MinKanBan);
		
		if(Action.equals("INSERT"))
		{
			try
			{
				KANBAN KanBanData = MESDSPServiceProxy.getKANBANService().selectByKey(true, new Object[] {StockrName,ProductSpecName,ProcessOperationName});
			
				KanBanData.setKanBan(iKanBan) ;
				KanBanData.setMinKanBan(iMinKanBan) ;
				KanBanData.setKanbanFlag(KanbanFlag) ;
	
				try
				{
					eventInfo.setEventName("Modify");
					MESDSPServiceProxy.getKANBANService().modify(eventInfo, KanBanData);
	
				}
				catch(Exception e)
				{
					throw new CustomException("JOB-8012", "StockrName : " + StockrName + ", " + "ProductSpecName : " + ProductSpecName);
				}		
			}
			catch(Exception e)
			{
	 
			 
				KANBAN KANBANInfo = new KANBAN();
				
				KANBANInfo.setStockrName(StockrName);
				KANBANInfo.setProductSpecName(ProductSpecName);
				KANBANInfo.setProcessOperationName(ProcessOperationName);
				KANBANInfo.setKanBan(iKanBan);
				KANBANInfo.setMinKanBan(iMinKanBan);
				KANBANInfo.setKanbanFlag(KanbanFlag);
					
				try
				{
					eventInfo.setEventName("Create");
					MESDSPServiceProxy.getKANBANService().create(eventInfo, KANBANInfo);
				}
				catch(Exception ex)
				{
					throw new CustomException("JOB-8012", "StockrName : " + StockrName + ", " + "ProductSpecName : " + ProductSpecName);
				}
			}
					
		}
		else
		{
			try
			{
				KANBAN KanBanData = MESDSPServiceProxy.getKANBANService().selectByKey(true, new Object[] {StockrName,ProductSpecName,ProcessOperationName});
	
				try
				{
					eventInfo.setEventName("Delete");
					MESDSPServiceProxy.getKANBANService().remove(eventInfo,KanBanData);
				}
				catch(Exception e)
				{
					throw new CustomException("JOB-8012", "StockrName : " + StockrName + ", " + "ProductSpecName : " + ProductSpecName);
				}		
			}
			catch(Exception e)
			{				
				throw new CustomException("JOB-8012", "StockrName : " + StockrName + ", " + "ProductSpecName : " + ProductSpecName);
			}			
			
		}
		
		return doc;
 
	}
		
}