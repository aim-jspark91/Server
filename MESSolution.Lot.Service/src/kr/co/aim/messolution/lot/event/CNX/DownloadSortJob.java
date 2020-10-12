package kr.co.aim.messolution.lot.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;
import org.jdom.Element;

public class DownloadSortJob extends AsyncHandler {

	@Override
	public void doWorks(Document doc)
		throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String jobName    = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);
		
		SortJob sortJob = ExtendedObjectProxy.getSortJobService().selectByKey(false, new Object[] {jobName});
		
		if (sortJob == null)
			throw new CustomException("SYS-1051", "Sorter Job already confirmed");
		
		Element bodyElement = ExtendedObjectProxy.getSortJobProductService().createSortJobBodyElement(machineName, jobName);
		
		//first removal of existing node would be duplicated
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		//index of Body node is static
		doc.getRootElement().addContent(2, bodyElement);
		
		//from CNX to PEX
		String targetSubjectName = GenericServiceProxy.getESBServive().getSendSubject("PEXsvr");
		
		GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "PEXSender");
	}

}
