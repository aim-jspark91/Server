package kr.co.aim.messolution.lot.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.extended.object.management.data.SortJobCarrier;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;

public class SorterJobEnded extends AsyncHandler {
 
	@Override
	public void doWorks(Document doc)
		throws CustomException  
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("End", getEventUser(), getEventComment(), null, null);
 
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String jobName    = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);
		
		SortJob sortJob = ExtendedObjectProxy.getSortJobService().selectByKey(false, new Object[] {jobName});
		
		/* 20181120, hhlee, add, This SorterJob checks whether all jobs ended or not. ==>> */
		List<SortJobCarrier> sortJobCarrier = null;
		try
        {
		    sortJobCarrier = ExtendedObjectProxy.getSortJobCarrierService().select(" JOBNAME = ? AND TRACKFLAG <> ? ", 
		            new Object[] {jobName, GenericServiceProxy.getConstantMap().SORT_JOBCARRIER_TRACKFLAG_OUT});
		    eventLog.warn(String.format("This sorterjob[%s] is not ended.!", jobName));
        }
		catch (Exception ce)
        {
            //eventLog.error(String.format("AlarmCode[%s] action set have not removed", alarmCode));
		    eventLog.info(String.format("This sorterjob[%s] is ended.!", jobName)); 
        }
		/* <<== 20181120, hhlee, add, This SorterJob checks whether all jobs ended or not. */
		
		/* 20181120, hhlee, add, This SorterJob checks whether all jobs ended or not. */
		if(sortJobCarrier == null)
		{
		    /* 20190130, hhlee, add, check logic */
		    if(!StringUtil.equals(sortJob.getJobState(), GenericServiceProxy.getConstantMap().SORT_JOBSTATE_ENDED))
		    {
        		sortJob.setJobState(GenericServiceProxy.getConstantMap().SORT_JOBSTATE_ENDED);
        		sortJob.setEventComment(eventInfo.getEventComment());
                sortJob.setEventName(eventInfo.getEventName());
                sortJob.setEventTime(eventInfo.getEventTime());
                sortJob.setEventUser(eventInfo.getEventUser());
                
        		ExtendedObjectProxy.getSortJobService().modify(eventInfo, sortJob);
		    }
		}
		
		/* 20180929, hhlee, Add SortJob Port ReAssign(delete not used -> Cron Job(Timer)) ==>> */
        //try
        //{
        //    /* 20180929, hhlee, Add SortJob Port ReAssign(delete not used -> Cron Job(Timer)) ==>> */
        //    //MESLotServiceProxy.getLotInfoUtil().RearrangeSorterPort(jobName, machineName, eventInfo);
        //    /* <<== 20180929, hhlee, Add SortJob Port ReAssign(delete not used -> Cron Job(Timer)) */
        //}
        //catch(Exception ex)
        //{
        //    eventLog.error(ex.getStackTrace());
        //}
        /* <<== 20180929, hhlee, Add SortJob Port ReAssign(delete not used -> Cron Job(Timer)) */
		
		//AutoDownloadSortJob
        /*try
		{
			ExtendedObjectProxy.getSortJobService().AutoDownloadSortJob(eventInfo, doc, machineName);
		}
		catch(Exception ex)
		{
			eventLog.error("Sorter job auto download failed");
		}*/
	}
}
