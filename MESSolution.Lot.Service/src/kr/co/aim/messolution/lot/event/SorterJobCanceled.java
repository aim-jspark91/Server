package kr.co.aim.messolution.lot.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class SorterJobCanceled extends AsyncHandler {
 
	@Override
	public void doWorks(Document doc)
		throws CustomException  
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelSortJob", getEventUser(), getEventComment(), null, null);
 
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String jobName    = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);
		
		/* 20190409, hhlee, Modify, add CancelJob Update Function ==>> */
		ExtendedObjectProxy.getSortJobService().sortJobCanceled(eventInfo, machineName, jobName, SMessageUtil.getReturnItemValue(doc, SMessageUtil.Result_ErrorMessage, false));
		//int seq = ExtendedObjectProxy.getSortJobService().getMaxSequence(machineName);
		//
		//SortJob sortJob = ExtendedObjectProxy.getSortJobService().selectByKey(false, new Object[] {jobName});
		//
		//sortJob.setSeq(seq);
		//sortJob.setJobState(GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CANCELED);
		//
		///* 20181214, hhlee, add, Record Sorter Job Cancel Note ==>> */
		//sortJob.setNote(SMessageUtil.getReturnItemValue(doc, SMessageUtil.Result_ErrorMessage, false));
		///* 20181214, hhlee, add, Record Sorter Job Cancel Note ==>> */
		//
		//sortJob.setEventComment(eventInfo.getEventComment());
        //sortJob.setEventName(eventInfo.getEventName());
        //sortJob.setEventTime(eventInfo.getEventTime());
        //sortJob.setEventUser(eventInfo.getEventUser());     
        //
		//ExtendedObjectProxy.getSortJobService().modify(eventInfo, sortJob);
		/* <<== 20190409, hhlee, Modify, add CancelJob Update Function */
		
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
        
		//AutoDownloadSortJob after canceled
		/*try
		{
			String condition = " WHERE 1=1 AND MACHINENAME = ? AND JOBSTATE IN ('RESERVED', 'CANCELED') ORDER BY SEQ ";
			Object[] bindSet = new Object[]{machineName};
			
			List<SortJob> reservedSortJoblist = ExtendedObjectProxy.getSortJobService().select(condition, bindSet);
			
			if(reservedSortJoblist.size() > 1)
			{
				if( !StringUtils.equals(reservedSortJoblist.get(0).getJobName() , jobName) )
				{
					ExtendedObjectProxy.getSortJobService().AutoDownloadSortJob(eventInfo, doc, machineName);
				}
			}
		}
		catch(Exception ex)
		{
			eventLog.error("Sorter job auto download failed");
		}*/
	}
	
}
