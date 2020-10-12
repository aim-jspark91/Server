package kr.co.aim.messolution.lot.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
//import kr.co.aim.messolution.generic.GenericServiceProxy;

public class SorterJobListReport extends AsyncHandler {
 
	@Override
	public void doWorks(Document doc)
		throws CustomException  
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("SortJobListReport", getEventUser(), getEventComment(), null, null);
 
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String jobName    = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);
		String jobState    = SMessageUtil.getBodyItemValue(doc, "JOBSTATE", true);
		
		SortJob sortJob = ExtendedObjectProxy.getSortJobService().selectByKey(false, new Object[] {jobName});
		
		sortJob.setJobState(jobState);
		sortJob.setEventComment(eventInfo.getEventComment());
        sortJob.setEventName(eventInfo.getEventName());
        sortJob.setEventTime(eventInfo.getEventTime());
        sortJob.setEventUser(eventInfo.getEventUser());     
        
		ExtendedObjectProxy.getSortJobService().modify(eventInfo, sortJob);
		

	}
	
}
