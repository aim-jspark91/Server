package kr.co.aim.messolution.lot.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class SorterJobCancelCommand extends AsyncHandler {

	@Override
	public void doWorks(Document doc)
		throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Cancel", getEventUser(), getEventComment(), null, null);

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);

		//get machineData
	    Machine machineData	= MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

	    String targetSubjectName = CommonUtil.getValue(machineData.getUdfs(), "MCSUBJECTNAME");

	    if(StringUtils.isEmpty(targetSubjectName))
	    	throw new CustomException("LOT-9006", machineName);

		List<Element> jobList = SMessageUtil.getBodySequenceItemList(doc, "JOBLIST", true);

		String note = SMessageUtil.getBodyItemValue(doc, "NOTE", false);
		for(Element joblist:jobList)
		{
			String jobName    = SMessageUtil.getChildText(joblist, "JOBNAME", true);
			int seq = ExtendedObjectProxy.getSortJobService().getMaxSequence(machineName);

			SortJob sortJob = ExtendedObjectProxy.getSortJobService().selectByKey(false, new Object[] {jobName});

			String jobState = sortJob.getJobState();
			sortJob.setSeq(seq);
			sortJob.setJobState(GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CANCELED);
			sortJob.setEventComment(eventInfo.getEventComment());
	        sortJob.setEventName(eventInfo.getEventName());
	        sortJob.setEventTime(eventInfo.getEventTime());
	        sortJob.setEventUser(eventInfo.getEventUser());
	        sortJob.setNote(note);
	        doc.getRootElement().getChild(SMessageUtil.Body_Tag).removeChild("NOTE");
	        //eventLog.info(JdomUtils.toString(doc));
			if(StringUtils.equals(jobState, GenericServiceProxy.getConstantMap().SORT_JOBSTATE_WAIT))
			{
				ExtendedObjectProxy.getSortJobService().modify(eventInfo, sortJob);
				GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");
				//only send CONFIRMED StateJob
				//joblist.removeChild("JOBNAME");
			}
			else
			{
			    /* 20180914, hhlee, modify, Because I go to the case where the log record is in trouble ==>> */
		        //GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "EISSender");
		        GenericServiceProxy.getESBServive().recordMessagelogAftersendBySender(targetSubjectName, doc, "EISSender");
		        /* <<== 20180914, hhlee, modify, Because I go to the case where the log record is in trouble */
			}
		}
	}

}
