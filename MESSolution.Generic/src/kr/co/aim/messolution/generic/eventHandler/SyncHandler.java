package kr.co.aim.messolution.generic.eventHandler;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public abstract class SyncHandler implements EventFactory {
	
	//transport parameter in form of String or Document
	private ThreadLocal<Object> replyDoc = new ThreadLocal<Object>();
	public Object getReplyDoc() {
		return replyDoc.get();
	}

	public void setReplyDoc(Object doc) {
		this.replyDoc.set(doc);
	}
	
	private ThreadLocal<String> replySubjectName = new ThreadLocal<String>();
	public String getReplySubjectName() {
		if (replySubjectName.get() != null)
			return replySubjectName.get();
		else
			return "";
	}

	public void setReplySubjectName(String replySubjectName) {
		this.replySubjectName.set(replySubjectName);
	}

	protected ThreadLocal<String> messageName = new ThreadLocal<String>();;
	public String getMessageName() {
		if (messageName.get() != null)
			return messageName.get();
		else
			return "";
	}
	public void setMessageName(String messageName) {
		this.messageName.set(messageName);
	}
	
	protected ThreadLocal<String> transactionId = new ThreadLocal<String>();;
	public String getTransactionId() {
		if (transactionId.get() != null)
			return transactionId.get();
		else
			return "";
	}
	public void setTransactionId(String transactionId) {
		this.transactionId.set(transactionId);
	}
	protected ThreadLocal<String> originalSourceSubjectName = new ThreadLocal<String>();;
	public String getOriginalSourceSubjectName() {
		if (originalSourceSubjectName.get() != null)
			return originalSourceSubjectName.get();
		else
			return "";
	}
	public void setOriginalSourceSubjectName(String originalSourceSubjectName) {
		this.originalSourceSubjectName.set(originalSourceSubjectName);
	}
	//protected ThreadLocal<String> sourceSubjectName = new ThreadLocal<String>();
//	public String getSourceSubjectName() {
//		if (sourceSubjectName.get() != null)
//			return sourceSubjectName.get();
//		else
//			return "";
//	}
//	public void setSourceSubjectName(String sourceSubjectName) {
//		this.sourceSubjectName.set(sourceSubjectName);
//	}
	//protected ThreadLocal<String> targetSubjectName = new ThreadLocal<String>();;
//	public String getTargetSubjectName() {
//		if (targetSubjectName.get() != null)
//			return targetSubjectName.get();
//		else
//			return "";
//	}
//	public void setTargetSubjectName(String targetSubjectName) {
//		this.targetSubjectName.set(targetSubjectName);
//	}
	protected ThreadLocal<String> eventUser = new ThreadLocal<String>();;
	public String getEventUser() {
		if (eventUser.get() != null)
			return eventUser.get();
		else
			return "";
	}
	public void setEventUser(String eventUser) {
		this.eventUser.set(eventUser);
	}
	protected ThreadLocal<String> eventComment = new ThreadLocal<String>();;
	public String getEventComment() {
		if (eventComment.get() != null)
			return eventComment.get();
		else
			return "";
	}
	public void setEventComment(String eventComment) {
		this.eventComment.set(eventComment);
	}
	protected ThreadLocal<String> language = new ThreadLocal<String>();;
	public String getLanguage() {
		if (language.get() != null)
			return language.get();
		else
			return "";
	}
	public void setLanguage(String language) {
		this.language.set(language);
	}

	public SyncHandler()
	{
		//constructor did once
		init();
	}
	
	/* (non-Javadoc)
	 * @purge thread garbage info
	 * @see kr.co.aim.messolution.generic.eventHandler.EventFactory#init()
	 */
	public void init()
	{
		//initialize for thread-safe
		setEventComment("");
		setEventUser("");
		setLanguage("");
		setMessageName("");
		setOriginalSourceSubjectName("");
		setReplyDoc(null);
		setReplySubjectName("");
		//setSourceSubjectName("");
		//setTargetSubjectName("");
		setTransactionId("");
	}

	@Override
	public void execute(Document doc) throws Exception
	{
		init();
		
		//setEventComment(SMessageUtil.getHeaderItemValue(doc, "EVENTCOMMENT", false));
		setEventUser(SMessageUtil.getHeaderItemValue(doc, "EVENTUSER", false));
		setLanguage(SMessageUtil.getHeaderItemValue(doc, "LANGUAGE", false));
		setMessageName(SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", false));
		setOriginalSourceSubjectName(SMessageUtil.getHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", false));
		//setSourceSubjectName(SMessageUtil.getHeaderItemValue(doc, "SOURCESUBJECTNAME", false));
		//setTargetSubjectName(SMessageUtil.getHeaderItemValue(doc, "TARGETSUBJECTNAME", false));
		setTransactionId(SMessageUtil.getHeaderItemValue(doc, "TRANSACTIONID", false));
		
		//default return to first sender
		//replySubjectName = originalSourceSubjectName;
		setReplySubjectName(getOriginalSourceSubjectName());
		
		//141103 by swcho : keep message name on communication
		String eventCommentTmp = SMessageUtil.getHeaderItemValue(doc, "EVENTCOMMENT", false);
		setEventComment(eventCommentTmp.isEmpty()?getMessageName():eventCommentTmp);
		
		try
		{
			setReplyDoc(doWorks(doc));			
		    handleSync(getReplyDoc(), getReplySubjectName());		
		}
		catch (Exception ex)
		{
			//give higher initiative to it in business implement
			//sync have to communicate with original stuff
			if (getReplyDoc() == null)
				setReplyDoc(doc);
			
			handleFault((Document) getReplyDoc(), getReplySubjectName(), ex);
			
			if (ex instanceof CustomException)
				throw ex;
			else
				throw new CustomException(ex);
		}
		finally
		{
			GenericServiceProxy.getMessageTraceService().recordMessageLog(doc, GenericServiceProxy.getConstantMap().INSERT_LOG_TYPE_SEND);
		}
	}
	
	public abstract Object doWorks(Document doc) throws CustomException;	
	
	@Override
	public void handleSync(Object doc, String sendSubjectName)
	{
		eventLog.debug("sync handler on start");
		
		if (doc != null && !StringUtil.isEmpty(sendSubjectName))
		{
			//send reply to destined subject
			if (doc instanceof Document)
				GenericServiceProxy.getESBServive().sendReply(sendSubjectName, (Document) doc);
			else if (doc instanceof String)
				GenericServiceProxy.getESBServive().sendReply(sendSubjectName, doc.toString());
			else
				return;
		}
		
		eventLog.debug("sync handler on end");
	}
	
	@Override
	public void handleFault(Document doc, String sendSubjectName, Exception ex)
	{
		eventLog.debug("fault handler on start");
		
		if (doc != null && !StringUtil.isEmpty(sendSubjectName) && ex != null)
		{
			GenericServiceProxy.getMessageTraceService().recordErrorMessageLog(doc, ex, StringUtils.EMPTY);
			
			//send reply to destined subject
			if (doc instanceof Document)
			{
				String a = getLanguage();
			System.out.println(a);
				
				GenericServiceProxy.getESBServive().sendError(sendSubjectName, doc, getLanguage(), ex);
			}
			else
				return;
		}
		
		eventLog.debug("fault handler on end");
	}
	
	/**
	 * 20180326 by hhlee : Send NG/OK results only in " Online Initial ".
	 * ====================================================================
	 */	
	/* (non-Javadoc)
	 * @see kr.co.aim.messolution.generic.eventHandler.EventFactory#handleSyncAsync(java.lang.Object, java.lang.String)
	 */
	@Override
	public void handleSyncAsync(Object doc, String sendSubjectName)
	{		
	}
	
	/**
	 * 20180326 by hhlee : Send NG/OK results only in " Online Initial ".
	 * ====================================================================
	 */
	/* (non-Javadoc)
	 * @see kr.co.aim.messolution.generic.eventHandler.EventFactory#handleSyncAsyncFault(org.jdom.Document, java.lang.String, java.lang.Exception)
	 */
	@Override
	public void handleSyncAsyncFault(Document doc, String sendSubjectName, Exception ex)
	{
		eventLog.debug("fault handler on start");
		
		if (doc != null && ex != null)
		{
			GenericServiceProxy.getMessageTraceService().recordErrorMessageLog(doc, ex, StringUtils.EMPTY);
		}
		
		eventLog.debug("fault handler on end");
	}
}
