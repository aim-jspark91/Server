package kr.co.aim.messolution.generic.eventHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public interface EventFactory {
	
	public Log eventLog = LogFactory.getLog(EventFactory.class);
	
	public void execute(Document doc) throws Exception;
	
	public void handleSync(Object doc, String sendSubjectName);
	
	public void handleFault(Document doc, String sendSubjectName, Exception ex);
	
	//thread-safe handler
	public void init();
	
	/**
	 * 20180326 by hhlee : Send NG/OK results only in " Online Initial ".
	 * ====================================================================
	 */		
    public void handleSyncAsync(Object doc, String sendSubjectName);
    /**
	 * 20180326 by hhlee : Send NG/OK results only in " Online Initial ".
	 * ====================================================================
	 */		
	public void handleSyncAsyncFault(Document doc, String sendSubjectName, Exception ex);
}
