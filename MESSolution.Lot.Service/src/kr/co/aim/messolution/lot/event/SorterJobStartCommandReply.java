package kr.co.aim.messolution.lot.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.support.InvokeUtils;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;

public class SorterJobStartCommandReply extends AsyncHandler {
 
	@Override
	public void doWorks(Document doc)
		throws CustomException  
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Confirm", getEventUser(), getEventComment(), null, null);
 
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String jobName    = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);
		String Result    = SMessageUtil.getBodyItemValue(doc, "RESULT", true);
		String ResultDescription    = SMessageUtil.getBodyItemValue(doc, "RESULTDESCRIPTION", false);
		
		if(StringUtil.equals(Result, "OK"))
		{
			SortJob sortJob = ExtendedObjectProxy.getSortJobService().selectByKey(false, new Object[] {jobName});
			
			sortJob.setJobState(GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CONFIRMED);
			sortJob.setEventComment(eventInfo.getEventComment());
	        sortJob.setEventName(eventInfo.getEventName());
	        sortJob.setEventTime(eventInfo.getEventTime());
	        sortJob.setEventUser(eventInfo.getEventUser());     
	        
			ExtendedObjectProxy.getSortJobService().modify(eventInfo, sortJob);
		}
		else
		{
		    //previous logic[Truly]
			//this.SorterJobCanceled(doc);
		}
		
		//return doc : reply is option
/*		try
		{
			GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");
		}
		catch (Exception ex)
		{
			eventLog.error(ex);
		}*/
	}  
	
	private void SorterJobCanceled(Document doc) throws CustomException
	{
		Document cloneDoc = (Document) doc.clone();
		
		SMessageUtil.setHeaderItemValue(cloneDoc, "MESSAGENAME", "SorterJobCanceled");
		
		try 
		{
			InvokeUtils.invokeMethod(InvokeUtils.newInstance(SorterJobCanceled.class.getName(), null, null), "execute", new Object[] {cloneDoc});
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-9999", "System", "Invocation failed");
		}
	}
	
}
