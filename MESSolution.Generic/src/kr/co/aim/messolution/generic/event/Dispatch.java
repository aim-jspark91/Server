package kr.co.aim.messolution.generic.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;

public class Dispatch extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		eventLog.debug("XML message in");
		
		//151231 by swcho : determined by configuration
		/*if (System.getProperty("svr") != null)
		{
			String svrName = System.getProperty("svr").toString();
			
			if (svrName.equals("PEMsvr"))
				dispatch2PEX(doc);
			else if (svrName.equals("TEMsvr"))
				dispatch2TEX(doc);
			else if (svrName.equals("CNMsvr"))
				dispatch2CNX(doc);
		}*/
		
		//default sender for message server is following
		String defaultSender = "LocalSender";
		
		String senderName = this.getSender(doc, defaultSender);
		
		try
		{
			GenericServiceProxy.getESBServive().sendBySender(doc, senderName);
		}
		catch (Exception ex)
		{
			eventLog.error(ex);
			eventLog.warn(String.format("retry with default sender[%s]", defaultSender));
			GenericServiceProxy.getESBServive().sendBySender(doc, defaultSender);
		}
		
		eventLog.debug("XML message out");
	}
	
	private String getSender(Document doc, String defaultSender)
	{
		try
		{
			String sender = SMessageUtil.getHeaderItemValue(doc, "SENDER", false);
			
			if (sender.isEmpty())
			{
				eventLog.debug(String.format("Sender is not specified so that redirect to default sender[%s]", defaultSender));
				sender = defaultSender;
			}
			else
			{
				eventLog.debug(String.format("Sender is specified with [%s]", sender));
			}
			
			return sender;
		}
		catch (Exception ex)
		{
			return defaultSender;
		}
	}
}
