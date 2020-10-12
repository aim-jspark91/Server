package kr.co.aim.messolution.lot.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class CancelSortJob extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException {

		String JobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);
		String Machinename = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String note = SMessageUtil.getBodyItemValue(doc, "NOTE", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelSortJob",getEventUser(), getEventComment(), "", "");
		
		try
		{
			SortJob sortJob = new SortJob("");
			sortJob = ExtendedObjectProxy.getSortJobService().selectByKey(false,new Object[] {JobName});
			sortJob.setJobState(GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CANCELED);
			sortJob.setEventTime(eventInfo.getEventTime());
			sortJob.setEventName(eventInfo.getEventName());
			sortJob.setEventUser(eventInfo.getEventUser());
			sortJob.setEventComment(eventInfo.getEventComment());
			sortJob.setNote(note);
			ExtendedObjectProxy.getSortJobService().modify(eventInfo, sortJob);

			eventInfo = EventInfoUtil.makeEventInfo("AssignPort",getEventUser(), getEventComment(), "", "");
			MESLotServiceProxy.getLotInfoUtil().rearrangeSorterPort(JobName, Machinename, eventInfo);
			
		}
		
		catch (Exception ex)
		{
			throw new CustomException("SYS-9999", "CancelSortJob", ex.getMessage());
		}
		
		return doc;
	}
}
