package kr.co.aim.messolution.dispatch.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspEQPMaxWip;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class ModifyMaxWip extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String operationName = SMessageUtil.getBodyItemValue(doc, "OPEID", true);
		String validFlag = SMessageUtil.getBodyItemValue(doc, "VALIDATEFLAG", true);
		String currentState = SMessageUtil.getBodyItemValue(doc, "CURRENTSTATE", true);
		String currentRunMode = SMessageUtil.getBodyItemValue(doc, "CURRENTMODE", true);
		String maxWip = SMessageUtil.getBodyItemValue(doc, "MAXWIP", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
 
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Modify", this.getEventUser(), this.getEventComment(), "", "");
			 
		
		DspEQPMaxWip dspEQPMaxWip = null;
		
		try
		{
			dspEQPMaxWip = ExtendedObjectProxy.getDspEQPMaxWipService().selectByKey(false, new Object[] {machineName, productSpecName, processFlowName,operationName  });
					
		}
		catch (Exception ex)
		{
			dspEQPMaxWip = null;
			
		}
		
		if(dspEQPMaxWip == null)
		{
			throw new CustomException("DspEQPMaxWip Modify Error");
		}

		
		dspEQPMaxWip.setMaxWipCount(maxWip);
		dspEQPMaxWip.setValidFlag(validFlag);
		dspEQPMaxWip.setCurrentState(currentState);
		dspEQPMaxWip.setCurrentRunMode(currentRunMode);
		dspEQPMaxWip.setCreateUser(eventInfo.getEventUser());
		dspEQPMaxWip.setCreateTime(eventInfo.getEventTime());
		
		dspEQPMaxWip.setLastEventUser(eventInfo.getEventUser());
		dspEQPMaxWip.setLastEventComment(eventInfo.getEventComment());
		dspEQPMaxWip.setLastEventTime(eventInfo.getEventTime());
		dspEQPMaxWip.setLastEventName(eventInfo.getEventName());
		dspEQPMaxWip.setLastEventTimekey(eventInfo.getEventTimeKey());
		
		ExtendedObjectProxy.getDspEQPMaxWipService().modify(eventInfo, dspEQPMaxWip);
		
		//MESDSPServiceProxy.getDSPServiceImpl().updateMaxWipSpec(eventInfo, machineName, productSpecName, opeID, validateFlag, currentState, currentMode, maxWip);
		
		return doc;
	}
}
