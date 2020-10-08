package kr.co.aim.messolution.lot.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class EndSortJob extends SyncHandler{
	@Override
	public Object doWorks(Document doc) throws CustomException {

		String JobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("EndSortJob",getEventUser(), getEventComment(), "", "");
		
		try
		{
			SortJob sortJob = ExtendedObjectProxy.getSortJobService().selectByKey(false,new Object[] {JobName});
			sortJob.setJobState(GenericServiceProxy.getConstantMap().SORT_JOBSTATE_ENDED);
			sortJob.setEventTime(eventInfo.getEventTime());
			sortJob.setEventName(eventInfo.getEventName());
			sortJob.setEventUser(eventInfo.getEventUser());
			sortJob.setEventComment(eventInfo.getEventComment());
			ExtendedObjectProxy.getSortJobService().modify(eventInfo, sortJob);			
		}
		
		catch (Exception ex)
		{
			throw new CustomException("SYS-9999", "EndSortJob", ex.getMessage());
		}
		
		return doc;
	}
}
