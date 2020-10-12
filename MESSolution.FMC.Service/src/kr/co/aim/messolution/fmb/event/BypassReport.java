package kr.co.aim.messolution.fmb.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;

public class BypassReport extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		//String targetSubjectName = "CSW.CF.MES.DEV.GEN.FMB.*";
		
		String FMCSubjectName = GenericServiceProxy.getESBServive().getSendSubject("FMCsvr");
		FMCSubjectName = StringUtil.removeEndIgnoreCase(FMCSubjectName, "FMCsvr");
		
		StringBuilder subjectBuilder = new StringBuilder(FMCSubjectName).append("FMB").append(".").append("*");
		
		GenericServiceProxy.getESBServive().sendBySender(subjectBuilder.toString(), doc, "FMBSender");
	}

}
