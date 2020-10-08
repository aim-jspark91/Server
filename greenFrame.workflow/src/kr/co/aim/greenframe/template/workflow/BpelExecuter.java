package kr.co.aim.greenframe.template.workflow;

import java.util.Date;
import java.util.HashMap;

import javax.xml.namespace.QName;

import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenflow.core.BpelActivityListener;
import kr.co.aim.greenflow.core.BpelExecutionContext;
import kr.co.aim.greenflow.core.BpelExecutionContextAware;
import kr.co.aim.greenflow.core.BpelProcessManager;
import kr.co.aim.greenflow.core.activity.BpelProcess;
import kr.co.aim.greenframe.event.BundleMessageEventAdaptor;
import kr.co.aim.greenframe.fos.greenflow.BpelExecutionEventAdaptor;
import kr.co.aim.greenframe.fos.greenflow.BpelExecutorService;
import kr.co.aim.greenframe.util.bundle.BundleUtil;
import kr.co.aim.greenframe.util.msg.MessageUtil;
import kr.co.aim.greenframe.util.support.InvokeUtils;
import kr.co.aim.greentrack.generic.GenericServiceProxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


// BpelExecutionContextAware Interface
public class BpelExecuter extends BpelExecutorService implements ApplicationContextAware, BpelExecutionContextAware
{
	static Log log = LogFactory.getLog(BpelExecuter.class);
	
	/**
	 * @uml.property  name="context"
	 * @uml.associationEnd  
	 */
	private	ApplicationContext	context;
	
	// BpelExecutionContext save ThreadLocal add
	/**
	 * @uml.property  name="bpelExecutionContext"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="kr.co.aim.greenflow.core.BpelExecutionContext"
	 */
	private ThreadLocal<BpelExecutionContext> bpelExecutionContext = new ThreadLocal<BpelExecutionContext>();
	
	//business process handler repository
	private HashMap<String, Object> classInstanceMap = new HashMap<String, Object>();
	
	public void terminateImmediately()
	{
		log.info("***************************************************************************************");
		System.exit(0);
	}
	
	public void terminateAfterManagement(String serverName){
		
		String selfServerName = System.getProperty("Seq");
		log.info("***************************************************************************************");
		log.info("Receive ServerName = " + serverName);
		log.info("My ServerName = " + selfServerName);
		
		if(serverName.equals(selfServerName)){
		
			log.info("[1/2] Closeing Transport .....");
			BundleMessageEventAdaptor bundleMessageEventAdaptor = (BundleMessageEventAdaptor)BundleUtil.getBundleServiceClass(BundleMessageEventAdaptor.class);
			bundleMessageEventAdaptor.terminate();
			log.info("[2/2] Closeing Transport .....");
			
			log.info("[1/2] Checking alive bpel or running bpel .....");
			BpelProcessManager BpelProcessManager = (BpelProcessManager)BundleUtil.getBundleServiceClass(BpelProcessManager.class);
			boolean isrunningBpel = false;
			int bpelCheckCount = 2;
			
			String nameCheck = serverName.substring(0, 6);
			
			if(nameCheck.toUpperCase().equals("MSGSVR")){
				bpelCheckCount = 3; 
			}
			
			for (int i=0; i<3; i++)
			{
				log.info("Bpel Size = " + BpelProcessManager.getRunningBpelProcessSize());
				while (BpelProcessManager.getRunningBpelProcessSize() > bpelCheckCount)
				{
					if (!isrunningBpel) {
						log.info("Waiting for completing running bpel .....");
						isrunningBpel = true;
					}
					try {
						Thread.sleep(500);
					} catch (Exception e) {}
				}
				
				try {
					Thread.sleep(330);
				} catch (Exception e) {}
			}
			
			if (isrunningBpel){
				log.info("All running bpel are completed .....");
			}
			
			log.info("[2/2] Checking alive bpel or running bpel .....");
			log.info("***************************************************************************************");
			log.info("********************           Terminated Good bye           **************************");
			log.info("***************************************************************************************");
			System.exit(0);
		}else{
			log.warn("Receive ServerName is not same my Name");
		}
	}
	
	public void setApplicationContext(ApplicationContext ctx) throws BeansException
	{
		context = ctx;
	}
	
	public void setBpelExecutionContext(BpelExecutionContext bpelExecutionContext )
	{
		this.bpelExecutionContext.set( bpelExecutionContext );
	}
	public BpelExecutionContext getBpelExecutionContext()
	{
		return this.bpelExecutionContext.get();
	}
	
	public void executeWFListener(Document document)
	{
		Object[] arguments = new Object[]{document};		
        this.executeWF(arguments, false, SMessageUtil.getListenerBpelName(document));
	}
	
	public void executeWF(Document document)
	{
		Object[] arguments = new Object[]{document};		
        this.executeWF(arguments, false, SMessageUtil.getBpelName(document));
	}
	
	public void executeWF(Document document, String bpelName)
	{
		Object[] arguments = new Object[]{document};		
        this.executeWF(arguments, false, bpelName);
	}
	
	public void SyncExcuteWF(Document document, String bpelName)
	{
		Object[] arguments = new Object[]{document};		
        this.executeWF(arguments, false, bpelName);
	}
	
	public void executeProcess(Object[] arguments, boolean parallel, String bpelName)
	{
		BpelProcess process = null;
		try {
			if (bpelName == null) { 
				for (int i=0; i<arguments.length; i++)
				{
					if (arguments[i] instanceof Document)
					{
						String bpel = MessageUtil.getBpelName((Document)arguments[i]);
						if (bpel != null && bpel.length() > 0) {
							if (!bpel.toLowerCase().endsWith(".bpel"))
									bpelName = bpel + ".bpel";
							else
								bpelName = bpel;
							break;
						}
					}
				}
				if (bpelName == null)
					bpelName = this.getBpelRepository().getRootBpelName();
			}
			
			
			QName name = new QName(DEFAULT_TARGETNAMESPACE, bpelName);
			process = newBpelProcess(name);
			try {
				BpelActivityListener listener = (BpelActivityListener)context.getBean("Modeler");
				process.addBpelActivityListener(listener);
			} catch (Exception ex) {
				//log.error(ex);
				process.addBpelActivityListener((BpelExecutionEventAdaptor)context.getBean("BpelExecutionEventAdaptor"));
			}
			
		
			process.execute(arguments, parallel);

		} catch(Exception ex){
			log.error("Could not execute " + bpelName, ex);
			((BpelExecutionEventAdaptor)context.getBean("BpelExecutionEventAdaptor")).onException(process, ex);
		}
	}
	
	public void executeWF(Object[] arguments, boolean parallelMode, String bpelName)
	{
		BpelProcess process = null;
		
		try
		{
			if (bpelName == null) { 
				for (int i=0; i<arguments.length; i++)
				{
					if (arguments[i] instanceof Document)
					{
						String bpel = MessageUtil.getBpelName((Document)arguments[i]);
						if (bpel != null && bpel.length() > 0) {
							if (!bpel.toLowerCase().endsWith(".bpel"))
									bpelName = bpel + ".bpel";
							else
								bpelName = bpel;
							break;
						}
					}
				}
				if (bpelName == null)
					bpelName = this.getBpelRepository().getRootBpelName();
			}
			QName name = new QName(DEFAULT_TARGETNAMESPACE, bpelName);
			
			//no need to gain BPEL context on engine
			try
			{
				process = newBpelProcess(name);
				
				//2020.01.09 dmlee : Record Running Bpel Process Size
				log.info("************** Running Bpel Process Size : "+this.bpelRepository.getRunningBpelProcessSize()+" **************");
				
				try
				{
					BpelActivityListener listener = (BpelActivityListener)context.getBean("Modeler");
					process.addBpelActivityListener(listener);
				}
				catch (Exception ex)
				{
					//log.error(ex);
				}

				process.setParent( getBpelExecutionContext().getRunningActivity());
				process.setSuperProcess( getBpelExecutionContext().getBpelProcess());
				process.addBpelActivityListener( getBpelExecutionContext().getRunningActivity());
				
				//open arch transaction
				//GenericServiceProxy.getTxDataSourceManager().beginTransaction();
				{
					//BPEL has internal exception handling
					process.execute(arguments, parallelMode, false, getBpelExecutionContext().getBpelProcess().getOriginatorBpelProcess());
				}
			}
			catch (Exception ex)
			{//only considering that BPEL not found
				//160830 by swcho : by CIM request
				if (bpelName.contains(".bpel"))
					bpelName = bpelName.replaceFirst(".bpel", "");
				
				if (log.isDebugEnabled())
					log.debug(String.format("Could not find [%s], then do class invocation", bpelName));
				
				//2020.01.09 dmlee : Record Running Bpel Process Size
				log.info("************** Running Bpel Process Size : "+this.bpelRepository.getRunningBpelProcessSize()+" **************");
				
				log.info("===============================================================================================");
				log.info(new StringBuffer("***** EVENT STARTED :: ").append(bpelName).append("@").append(getBpelExecutionContext().getBpelProcess().getId()).toString());
				log.info("===============================================================================================");
				
				//150909 by swcho : by task
				if (System.getProperty("svrType") != null && System.getProperty("svrType").toString().equals("M"))
				{
					//message delivery service
					invokeDispatchService(arguments);
				}
				else
				{
					//direct event loading
					invokeEventService(bpelName, arguments); 
				}
				
				long elapse = calculateElapsed();
				
				log.info("===============================================================================================");
				log.info("***** EVENT COMPLETED :: " + summaryData(log.isInfoEnabled(), true, bpelName, elapse));
				log.info("===============================================================================================");
				
				//record result on independent thread
				if (true && arguments.length > 0 && arguments[0] instanceof Document)
				 	kr.co.aim.messolution.generic.GenericServiceProxy.getMessageTraceService().
				 		recordTranscationLog((Document) arguments[0], elapse, true);
				
				log.info("pararell end");
			}
			//catch (Exception ex)
			//{
			//	log.error("Could not execute " + bpelName, ex);
			//	((BpelExecutionEventAdaptor)context.getBean("BpelExecutionEventAdaptor")).onException(process, ex);
			//}
		}
		catch (Exception ex)
		{	
			//rollback on BPEL exception
			//GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			
			//log.error("Could not execute " + bpelName, ex);
			//((BpelExecutionEventAdaptor)context.getBean("BpelExecutionEventAdaptor")).onException(process, ex);
			
			//for uncontrollable error not expected reply
			log.error(ex);
			
			
			//2019.10.08 dmlee : Request By CIM, If OutOfMemory Error Record CT_ERRORMESSAGELOG
			try
			{
				if(ex.toString().contains("OutOfMemoryError"))
				{
					kr.co.aim.messolution.generic.GenericServiceProxy.getMessageTraceService().recordErrorMessageLog((Document) arguments[0], ex, "");
				}
			}
			catch(Exception ex2)
			{
				
			}
			
			
			long elapse = calculateElapsed();
			
			log.info("===============================================================================================");
			log.info("***** EVENT COMPLETED :: " + summaryData(log.isInfoEnabled(), false, bpelName, elapse));
			log.info("===============================================================================================");
			
			//record result on independent thread
			if (true && arguments.length > 0 && arguments[0] instanceof Document)
			 	kr.co.aim.messolution.generic.GenericServiceProxy.getMessageTraceService().
			 		recordTranscationLog((Document) arguments[0], elapse, false);
			
			//cut transaction manager
			GenericServiceProxy.getTxDataSourceManager().rollbackAllTransactions();
			
			log.info("pararell end");
		}
		finally
		{
			//commit message request
			//GenericServiceProxy.getTxDataSourceManager().commitTransaction();
			
			//doAlarmRequest();
			
			//executeAction();
			
			this.bpelExecutionContext.remove();
		}
	}
	
	public void invokeEventService(String eventServiceName, Object[] argumemts) throws Exception
	{
		//parsing event name
		if (eventServiceName.contains(".bpel"))
			eventServiceName = eventServiceName.replaceFirst(".bpel", "");
		
		try 
		{
			//Object classNameObject = kr.co.aim.messolution.generic.GenericServiceProxy.getConstantMap().getClassDefsMap().get(eventServiceName);
			//141126 by swcho : improved stability
			Object classNameObject = kr.co.aim.messolution.generic.GenericServiceProxy.getEventClassMap().get(eventServiceName);
			
			String className = "";
			
			if (classNameObject != null)
				className = classNameObject.toString();
			
			Object instance = classInstanceMap.get(eventServiceName);
			
			//prepared to GC
			//150309 by swcho : improved class binding
			if (instance == null)
			{
				log.warn(String.format("class [%s] is required to load", eventServiceName));
				
				Object loadObject = null;
				
				try
				{
					loadObject = InvokeUtils.newInstance(className, null, null);
					
					if (loadObject == null)
						throw new Exception(String.format("class [%s] not found", className));
				}
				catch (Exception ex)
				{
					log.error(ex);
					
					log.warn(String.format("try to reload class [%s]", className));
					//reload
					loadObject = InvokeUtils.newInstance(className, null, null);
					
					//record Error Message & set EmptyFlag : Y
					kr.co.aim.messolution.generic.GenericServiceProxy.getMessageTraceService().recordErrorMessageLog((Document) argumemts[0], ex, "Y");
				}
				finally
				{
					classInstanceMap.put(eventServiceName, loadObject);
				}
			}
			
			//if (greenFrameServiceProxy.getTxDataSourceManager().isAutoManaged())
			//{
			//	greenFrameServiceProxy.getTxDataSourceManager().beginTransaction();
			//	
			//	//setApplicationInfo(getProcessName(), eventServiceName);
			//}
			
			if (argumemts.length > 0 && argumemts[0] instanceof Document)
			{
				InvokeUtils.invokeMethod(classInstanceMap.get(eventServiceName), "execute", new Object[] {(Document) argumemts[0]});
			}
			else
			{
				//log.error(String.format("Class[%s] is not defined", className));
				throw new Exception(String.format("Class[%s] is not defined", className));
			}
							
			//if (greenFrameServiceProxy.getTxDataSourceManager().isAutoManaged())
			//{
			//	//setApplicationInfo(getProcessName() , "");
			//	greenFrameServiceProxy.getTxDataSourceManager().commitTransaction();
			//}
		}
		catch (Exception e)
		{
			//setApplicationInfo(getProcessName(), "");
			//error in logic in form of standardized exceptiopn
			log.error(e);
			
			throw (Exception) e.getCause();
		}
		
		//log.info(JdomUtils.toString(replyDoc));
	}
	
	public void invokeDispatchService(Object[] argumemts) throws Exception
	{
		try 
		{
			String eventServiceName = "dispatching";
			String className = "kr.co.aim.messolution.generic.event.Dispatch";
			
			Object instance = classInstanceMap.get(eventServiceName);
			
			//prepared to GC
			if (instance == null)
			{
				log.warn(String.format("class [%s] is required to load", eventServiceName));
				
				Object loadObject = null;
				
				try
				{
					loadObject = InvokeUtils.newInstance(className, null, null);
					
					if (loadObject == null)
						throw new Exception(String.format("class [%s] not found", className));
				}
				catch (Exception ex)
				{
					log.error(ex);
					
					log.warn(String.format("try to reload class [%s]", className));
					//reload
					loadObject = InvokeUtils.newInstance(className, null, null);
				}
				finally
				{
					classInstanceMap.put(eventServiceName, loadObject);
				}
			}
			
			if (argumemts.length > 0 && argumemts[0] instanceof Document)
			{
				InvokeUtils.invokeMethod(classInstanceMap.get(eventServiceName), "execute", new Object[] {(Document) argumemts[0]});
			}
			else
			{
				//log.error(String.format("Class[%s] is not defined", className));
				throw new Exception(String.format("Class[%s] is not defined", className));
			}
		}
		catch (Exception e)
		{
			log.error(e);
			
			throw (Exception) e.getCause();
		}
	}
	
	private String summaryData(boolean isDebug, boolean result, String eventName, long time)
	{
		StringBuffer stringbuffer = new StringBuffer();
		if (isDebug && result)
		{
			stringbuffer.append(eventName).append("@")
						.append(getBpelExecutionContext().getBpelProcess().getId()).append(" <SUCCESS> ");
			stringbuffer.append(" Elapsed Time = ").append(time).append(" miliseconds");
		}
		else if (isDebug)
		{
			stringbuffer.append(eventName).append("@")
						.append(getBpelExecutionContext().getBpelProcess().getId()).append(" <FAIL> ");
			stringbuffer.append(" Elapsed Time = ").append(time).append(" miliseconds");
		}
		
		return stringbuffer.toString();
	}
	
	private long calculateElapsed()
	{
		return new Date(System.currentTimeMillis()).getTime()
				- getBpelExecutionContext().getBpelProcess().getStartTime().getTime();
	}
}