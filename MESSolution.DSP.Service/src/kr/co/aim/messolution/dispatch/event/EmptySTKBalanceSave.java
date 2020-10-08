package kr.co.aim.messolution.dispatch.event;
import kr.co.aim.messolution.dispatch.MESDSPServiceProxy;
import kr.co.aim.messolution.dispatch.management.data.EmptySTKBalance;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class EmptySTKBalanceSave extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
 
 
		String StockerName         = SMessageUtil.getBodyItemValue(doc, "STOCKERNAME", true);
		String EmptyMAXCST         = SMessageUtil.getBodyItemValue(doc, "EMPTYMAXCST", true);
		String EmptyMINCST         = SMessageUtil.getBodyItemValue(doc, "EMPTYMINCST", true);
		
		String Action         = SMessageUtil.getBodyItemValue(doc, "ACTION", true);
		
		int iEmptyMAXCST = Integer.parseInt(EmptyMAXCST);
		int iEmptyMINCST = Integer.parseInt(EmptyMINCST);
		
		if(Action.equals("INSERT"))
		{
			try
			{
				EmptySTKBalance EmptySTKBalanceData = MESDSPServiceProxy.getEmptySTKBalanceService().selectByKey(true, new Object[] {StockerName});
	
				EmptySTKBalanceData.setEmptyMAXCST(iEmptyMAXCST) ;
				EmptySTKBalanceData.setEmptyMINCST(iEmptyMINCST) ;
				
				try
				{
					eventInfo.setEventName("Modify");
					MESDSPServiceProxy.getEmptySTKBalanceService().modify(eventInfo, EmptySTKBalanceData);
	
				}
				catch(Exception e)
				{
					throw new CustomException("JOB-8012", "StockerName : " + StockerName  );
				}		
			}
			catch(Exception e)
			{
	 
			 
				EmptySTKBalance EmptySTKBalanceInfo = new EmptySTKBalance();
				
				EmptySTKBalanceInfo.setStockerName(StockerName);
				EmptySTKBalanceInfo.setEmptyMAXCST(iEmptyMAXCST) ;
				EmptySTKBalanceInfo.setEmptyMINCST(iEmptyMINCST) ;
					
				try
				{			
					eventInfo.setEventName("Create");
					MESDSPServiceProxy.getEmptySTKBalanceService().create(eventInfo, EmptySTKBalanceInfo);
				}
				catch(Exception ex)
				{
					throw new CustomException("JOB-8012", "StockerName : " + StockerName );
				}
			}
					
		}
		else
		{
			try
			{
				EmptySTKBalance EmptySTKBalanceData = MESDSPServiceProxy.getEmptySTKBalanceService().selectByKey(true, new Object[] {StockerName});
	
				try
				{
					eventInfo.setEventName("Delete");
					MESDSPServiceProxy.getEmptySTKBalanceService().remove(eventInfo,EmptySTKBalanceData);
	
				}
				catch(Exception e)
				{
					throw new CustomException("JOB-8012", "StockerName : " + StockerName );
				}		
			}
			catch(Exception e)
			{				
				throw new CustomException("JOB-8012", "StockerName : " + StockerName );
			}			
			
		}
 
		return doc;
	}
		
}