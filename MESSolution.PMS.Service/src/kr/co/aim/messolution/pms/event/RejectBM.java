package kr.co.aim.messolution.pms.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.BM;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class RejectBM extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String sRejectType = SMessageUtil.getBodyItemValue(doc, "REJECTTYPE", true);
		String BMName      = SMessageUtil.getBodyItemValue(doc, "BMNAME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("RejectBM", getEventUser(), getEventComment(), null, null);
		
		BM bmData = null;
		try
		{ 
			bmData = PMSServiceProxy.getBMService().selectByKey(true, new Object[] {BMName});
		}
		catch (Exception ex)
		{
			eventLog.error(String.format("Select PMS_BM Fail [ BMName = %s] ", BMName));
		}
		
		if( sRejectType.equals("Execute"))
		{
            bmData.setBmState("Verified");
            bmData.setRepairTime(null);
            bmData.setLastEventName("RejectExecuteBM");
			eventInfo.setEventName("RejectExecute");
			eventLog.error(String.format( "<<<<<<<<<<<<<<<<<<<<<<< Reject Execute BM >>>>>>>>>>>>>>>>>>>>>>>>>>"));
			try
			{
				bmData = PMSServiceProxy.getBMService().modify(eventInfo, bmData);
			}
			catch(Exception ex)
			{
				throw new CustomException("PMS-0064", BMName);
			}
			
		}
			
			else if( sRejectType.equals("Verify"))
			{
                bmData.setRepairTime(null);
                bmData.setExecuteResult(null);
                bmData.setBmState("Created");
                bmData.setBmGrade(null);
                bmData.setLastEventName("RejectVerifyBM");
	
				eventInfo.setEventName("RejectVerify");
				eventLog.error(String.format( "<<<<<<<<<<<<<<<<<<<<<<< Reject Verify BM >>>>>>>>>>>>>>>>>>>>>>>>>>"));
				try
				{
					bmData = PMSServiceProxy.getBMService().modify(eventInfo, bmData);
				}
				catch(Exception ex)
				{
					throw new CustomException("PMS-0064", BMName);
				}

		     }

		return doc;
	}
 }
