package kr.co.aim.messolution.dispatch.event;
import kr.co.aim.messolution.dispatch.MESDSPServiceProxy;
import kr.co.aim.messolution.dispatch.management.data.STKCstLimit;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class STKCstLimitSave extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("STKCstLimit", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
 
		
		String FactoryName         = SMessageUtil.getBodyItemValue(doc, "FactoryName", true);
		String MachineName    = SMessageUtil.getBodyItemValue(doc, "MachineName", true);
//		String ProductSpecVersion = SMessageUtil.getBodyItemValue(doc, "ProductSpecVersion", true);
		String DestMachineName = SMessageUtil.getBodyItemValue(doc, "DestMachineName", true);
		String Priority              = SMessageUtil.getBodyItemValue(doc, "Priority", true);
		String CommandQuantity              = SMessageUtil.getBodyItemValue(doc, "CommandQuantity", true);
		String RemainCSTQuantity              = SMessageUtil.getBodyItemValue(doc, "RemainCSTQuantity", true);
		String TransferFlag         = SMessageUtil.getBodyItemValue(doc, "TransferFlag", true);
		String Action         = SMessageUtil.getBodyItemValue(doc, "ACTION", true);
		
		int iCommandQuantity = Integer.parseInt(CommandQuantity);
		int iRemainCSTQuantity = Integer.parseInt(RemainCSTQuantity);
		int iPriority = Integer.parseInt(Priority);
		
		if(Action.equals("INSERT"))
		{
			try
			{
				STKCstLimit STKCstLimitData = MESDSPServiceProxy.getSTKCstLimitService().selectByKey(true, new Object[] {FactoryName,MachineName,DestMachineName});
			
				STKCstLimitData.setCommandQuantity(iCommandQuantity) ;
				STKCstLimitData.setRemainCSTQuantity(iRemainCSTQuantity) ;
				STKCstLimitData.setPriority(iPriority) ;
				STKCstLimitData.setTransferFlag(TransferFlag) ;
	
				try
				{
					eventInfo.setEventName("Modify");
					MESDSPServiceProxy.getSTKCstLimitService().modify(eventInfo, STKCstLimitData);
	
				}
				catch(Exception e)
				{
					throw new CustomException("JOB-8012", "MachineName : " + MachineName + ", " + "DestMachineName : " + DestMachineName);
				}		
			}
			catch(Exception e)
			{
	 
			 
				STKCstLimit STKCstLimitInfo = new STKCstLimit();
				
				STKCstLimitInfo.setFactoryName(FactoryName);
				STKCstLimitInfo.setMachineName(MachineName);
				STKCstLimitInfo.setDestMachineName(DestMachineName);
				STKCstLimitInfo.setCommandQuantity(iCommandQuantity) ;
				STKCstLimitInfo.setRemainCSTQuantity(iRemainCSTQuantity) ;
				STKCstLimitInfo.setPriority(iPriority) ;
				STKCstLimitInfo.setTransferFlag(TransferFlag) ;
					
				try
				{
					eventInfo.setEventName("Create");
					MESDSPServiceProxy.getSTKCstLimitService().create(eventInfo, STKCstLimitInfo);
				}
				catch(Exception ex)
				{
					throw new CustomException("JOB-8012", "MachineName : " + MachineName + ", " + "DestMachineName : " + DestMachineName);
				}
			}
					
		}
		else
		{
			try
			{
				STKCstLimit STKCstLimitData = MESDSPServiceProxy.getSTKCstLimitService().selectByKey(true, new Object[] {FactoryName,MachineName,DestMachineName});
	
				try
				{
					eventInfo.setEventName("Delete");
					MESDSPServiceProxy.getSTKCstLimitService().remove(eventInfo,STKCstLimitData);
				}
				catch(Exception e)
				{
					throw new CustomException("JOB-8012", "MachineName : " + MachineName + ", " + "DestMachineName : " + DestMachineName);
				}		
			}
			catch(Exception e)
			{				
				throw new CustomException("JOB-8012", "MachineName : " + MachineName + ", " + "DestMachineName : " + DestMachineName);
			}			
			
		}
		
		return doc;
 
	}
		
}