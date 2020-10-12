package kr.co.aim.messolution.dispatch.event;
import kr.co.aim.messolution.dispatch.MESDSPServiceProxy;
import kr.co.aim.messolution.dispatch.management.data.STKOperationPriority;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class STKOperationPrioritySave extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
 
		String MachineName    = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);		
		String StockerName         = SMessageUtil.getBodyItemValue(doc, "STOCKERNAME", true);
		String ProcessOperationName    = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String Priority         = SMessageUtil.getBodyItemValue(doc, "PRIORITY", true);
		String RequestName        = SMessageUtil.getBodyItemValue(doc, "REQUESTNAME", true);
		String Action         = SMessageUtil.getBodyItemValue(doc, "ACTION", true);
		
		int iPriority = Integer.parseInt(Priority);
		
		if(Action.equals("INSERT"))
		{
			try
			{
				STKOperationPriority STKPriorityData = MESDSPServiceProxy.getSTKOperationPriorityService().selectByKey(true, new Object[] {MachineName,RequestName,ProcessOperationName,StockerName});
			
				STKPriorityData.setPriority(iPriority) ;
				STKPriorityData.setRequestName(RequestName);
 	
				try
				{
					eventInfo.setEventName("Modify");
					MESDSPServiceProxy.getSTKOperationPriorityService().modify(eventInfo, STKPriorityData);
	
				}
				catch(Exception e)
				{
					throw new CustomException("JOB-8012", "StockerName : " + StockerName + ", " + "MachineName : " + MachineName);
				}		
			}
			catch(Exception e)
			{
	 
			 
				STKOperationPriority STKPriorityInfo = new STKOperationPriority();
				
				STKPriorityInfo.setStockerName(StockerName);
				STKPriorityInfo.setMachineName(MachineName);
				STKPriorityInfo.setProcessOperationName(ProcessOperationName);
				STKPriorityInfo.setPriority(iPriority) ;			
				STKPriorityInfo.setRequestName(RequestName);
					
				try
				{
					eventInfo.setEventName("Create");
					MESDSPServiceProxy.getSTKOperationPriorityService().create(eventInfo, STKPriorityInfo);
				}
				catch(Exception ex) 
				{
					throw new CustomException("JOB-8012", "StockerName : " + StockerName + ", " + "MachineName : " + MachineName);
				}
			}
					
		}
		else
		{
			try
			{
				STKOperationPriority STKPriorityData = MESDSPServiceProxy.getSTKOperationPriorityService().selectByKey(true, new Object[] {MachineName,RequestName,ProcessOperationName,StockerName});
	
				try
				{
					eventInfo.setEventName("Delete");
					MESDSPServiceProxy.getSTKOperationPriorityService().remove(eventInfo,STKPriorityData);
	
				}
				catch(Exception e)
				{
					throw new CustomException("JOB-8012", "StockrName : " + StockerName + ", " + "MachineName : " + MachineName);
				}		
			}
			catch(Exception e)
			{				
				throw new CustomException("JOB-8012", "StockrName : " + StockerName + ", " + "MachineName : " + MachineName);
			}			
			
		}
		
		return doc;
	}
		
}