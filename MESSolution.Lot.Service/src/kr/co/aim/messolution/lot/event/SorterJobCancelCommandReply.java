package kr.co.aim.messolution.lot.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;
import org.jdom.Element;

public class SorterJobCancelCommandReply extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
	    EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelSortJob", getEventUser(), getEventComment(), null, null);
	    
	    /* 20190409, hhlee, add, if RESULT = 'NG', SortJobState is 'CANCELED' update. ==>> */
		try
		{		    
		    String commandResult = SMessageUtil.getBodyItemValue(doc, "RESULT", true);
		    String commandResultDescription = SMessageUtil.getBodyItemValue(doc, "RESULTDESCRIPTION", false);
		    if(StringUtil.equals(commandResult, GenericServiceProxy.getConstantMap().RESULT_NG))
		    {
		        String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		        List<Element> sorterJobList = SMessageUtil.getBodySequenceItemList(doc, "JOBLIST", false);
		        for(Element sorterJob : sorterJobList)
		        {
		            String sortJobName = SMessageUtil.getChildText(sorterJob, "JOBNAME", false);
		            if(StringUtil.isNotEmpty(sortJobName))
		            {
		                ExtendedObjectProxy.getSortJobService().sortJobCanceled(eventInfo, machineName, sortJobName, "RESULT= " + commandResult + ", DESCRIPTION= " + commandResultDescription);
		            }
		        }
		    }
		}
		catch (Exception ex)
        {
            eventLog.error("[SorterJobCancelCommandReply] - JobCancel Update. " + ex);
        }
		/* <<== 20190409, hhlee, add, if RESULT = 'NG', SortJobState is 'CANCELED' update. */
	    
		try
        {	    
			//Option
			String originalSourceSubjectName = getOriginalSourceSubjectName();
			if (StringUtil.indexOf(StringUtil.upperCase(originalSourceSubjectName), "_INBOX") > -1 )
			{
				String commandResult = SMessageUtil.getBodyItemValue(doc, "RESULT", true);
				if(StringUtil.equals(commandResult, GenericServiceProxy.getConstantMap().RESULT_NG))
				{
					SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "0");
				}
				GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");
			}	
		}
		catch (Exception ex)
		{
			eventLog.error("[SorterJobCancelCommandReply] - OICSender. " + ex );
		}
	}
}
