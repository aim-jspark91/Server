package kr.co.aim.messolution.generic.timer;

import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.template.workflow.WorkflowServiceProxy;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class MessageExcuteJob implements Job{

	private static Log log = LogFactory.getLog(MessageExcuteJob.class);
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		// TODO Auto-generated method stub
		
		String message = (String) context.getJobDetail().getJobDataMap().get("MessageBody");
		Document document = null;
		try {
			document = JdomUtils.loadText(message);
			
			String time = TimeUtils.getCurrentEventTimeKey();
			document.getRootElement().getChild(SMessageUtil.Header_Tag).getChild(SMessageUtil.TransactionId_Tag).setText(time);
			document.getRootElement().getChild(SMessageUtil.Body_Tag).getChild("TIME").setText(time);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String bpel = "dispatching.bpel";
		
		Object[] object = {document};
		WorkflowServiceProxy.getBpelExecuter().executeProcess( object, false, bpel);
		
	}
}










