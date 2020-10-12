package kr.co.aim.messolution.dispatch.event;
import kr.co.aim.messolution.dispatch.MESDSPServiceProxy;
import kr.co.aim.messolution.dispatch.management.data.MAXWIP;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class MAXWIPSave extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("MAXWIP", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
 
		String ProductSpecName    = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String ProcessOperationName              = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String MaxWIP         = SMessageUtil.getBodyItemValue(doc, "MAXWIP", true);

		String Action         = SMessageUtil.getBodyItemValue(doc, "ACTION", true);
		
		int iMaxWIP = Integer.parseInt(MaxWIP);
		
		if(Action.equals("INSERT"))
		{
			try
			{
				MAXWIP MaxWipData = MESDSPServiceProxy.getMAXWIPService().selectByKey(true, new Object[] {ProductSpecName,ProcessOperationName});
			
				MaxWipData.setMaxWIP(iMaxWIP) ; 
	
				try
				{
					MESDSPServiceProxy.getMAXWIPService().modify(eventInfo, MaxWipData);
	
				}
				catch(Exception e)
				{
					throw new CustomException("JOB-8012", "ProductSpecName : " + ProductSpecName + ", " + "ProcessOperationName : " + ProcessOperationName);
				}		
			}
			catch(Exception e)
			{
	 
			 
				MAXWIP MaxWipInfo = new MAXWIP();
				
 
				MaxWipInfo.setProductSpecName(ProductSpecName);
				MaxWipInfo.setProcessOperationName(ProcessOperationName);
				MaxWipInfo.setMaxWIP(iMaxWIP);
					
				try
				{
					
					MESDSPServiceProxy.getMAXWIPService().create(eventInfo, MaxWipInfo);
				}
				catch(Exception ex)
				{
					throw new CustomException("JOB-8012", "ProductSpecName : " + ProductSpecName + ", " + "ProcessOperationName : " + ProcessOperationName);
				}
			}
					
		}
		else
		{
			try
			{
				MAXWIP MaxWipData = MESDSPServiceProxy.getMAXWIPService().selectByKey(true, new Object[] {ProductSpecName, ProcessOperationName});

	
				try
				{
					MESDSPServiceProxy.getMAXWIPService().remove(eventInfo,MaxWipData);
	
				}
				catch(Exception e)
				{
					throw new CustomException("JOB-8012", "ProductSpecName : " + ProductSpecName + ", " + "ProcessOperationName : " + ProcessOperationName);
				}		
			}
			catch(Exception e)
			{				
				throw new CustomException("JOB-8012", "ProductSpecName : " + ProductSpecName + ", " + "ProcessOperationName : " + ProcessOperationName);
			}			
			
		}
	
		return doc ;
	}		
}