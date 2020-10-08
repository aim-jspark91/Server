package kr.co.aim.messolution.generic;

import java.util.HashMap;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.esb.ESBService;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.master.ErrorDefMap;
import kr.co.aim.messolution.generic.master.MessageLogger;
import kr.co.aim.messolution.generic.util.MessageHistoryUtil;
import kr.co.aim.messolution.generic.util.SpecUtil;
import kr.co.aim.messolution.generic.util.dblog.DBLogWriterManager;
import kr.co.aim.greenframe.esb.GenericSender;
import kr.co.aim.greenframe.infra.EventConfigurator;
import kr.co.aim.greenframe.infra.InfraServiceProxy;
import kr.co.aim.greenframe.transaction.TxDataSourceManager;
import kr.co.aim.greenframe.util.bundle.BundleUtil;
import kr.co.aim.greentrack.generic.master.AbstractConstantMap;
import kr.co.aim.greentrack.generic.orm.SqlMesTemplate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class GenericServiceProxy extends MESStackTrace implements ApplicationContextAware {
	private static Log log = LogFactory.getLog(GenericServiceProxy.class);
	private static ApplicationContext ac;
	
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		// TODO Auto-generated method stub
		log.debug("Success Get AC");
		ac = arg0;
	}

	public static ApplicationContext getApplicationContext()
	{
		return ac;
	}
	
	public static TxDataSourceManager getTxDataSourceManager()
	{
		return (TxDataSourceManager)BundleUtil.getBundleServiceClass(TxDataSourceManager.class);
	}

	public static ConstantMap getConstantMap()
	{
		
		return (ConstantMap)BundleUtil.getBundleServiceClass(AbstractConstantMap.class);
	}
	
	public static ErrorDefMap getErrorDefMap()
	{
		
		return (ErrorDefMap)BundleUtil.getBundleServiceClass(ErrorDefMap.class);	
	}
	
	public static GenericSender getGenericSender(String senderName)
	{
		
		if(log.isInfoEnabled()){
			log.info("senderName = " + senderName);
		}
		
		BundleUtil.getBundleServiceClass(InfraServiceProxy.class);
		return (GenericSender) InfraServiceProxy.getBeanService(senderName);
	}
	
	public static ESBService getESBServive()
	{
		
		return (ESBService)BundleUtil.getBundleServiceClass(ESBService.class);	
	}
	
//	public static JobTimer getJobTimer()
//	{
//		return (JobTimer)BundleUtil.getBundleServiceClass(JobTimer.class);	
//	}
	
	public static MessageLogger getMessageLogService()
	{
		
		return (MessageLogger) BundleUtil.getBundleServiceClass(MessageLogger.class);	
	}
	
	public static MessageHistoryUtil getMessageTraceService()
	{
		return (MessageHistoryUtil) BundleUtil.getBundleServiceClass(MessageHistoryUtil.class);
	}
	
	/**
	 * common GreenTrack spec service
	 * @author swcho
	 * @since 2014.04.14
	 * @return
	 */
	public static SpecUtil getSpecUtil()
	{
		return (SpecUtil) BundleUtil.getBundleServiceClass(SpecUtil.class);
	}
	
	/**
	 * custom stack trace engine : must be implement in each proxy
	 * @author swcho
	 * @since 2014.02.11
	 * @param eventLogger
	 * @param beanName
	 * @param methodName
	 * @param args
	 * @return
	 * @throws CustomException
	 */
	public static Object executeMethod(Log eventLogger, String beanName, String methodName, Object... args)
		throws CustomException
	{
		return executeMethodMonitor(eventLogger, beanName, methodName, args);
	}
	
	/**
	 * alternatives for BPEL
	 * @author swcho
	 * @since 2014.11.26
	 * @return
	 */
	public static HashMap<String, String> getEventClassMap()
	{
		//return (EventClassMap)BundleUtil.getBundleServiceClass(EventClassMap.class);
		//if(log.isInfoEnabled()){
		//	log.info("EventClassMap proxy");
		//}
		
		//for pre-load?
		BundleUtil.getBundleServiceClass(InfraServiceProxy.class);
		
		EventConfigurator config = ((EventConfigurator) InfraServiceProxy.getBeanService("EventMapConfig"));
		
		return (HashMap<String, String>) config.getClassMap();
	}
	
	/**
	 * DCOL processor query unit
	 * @author swcho
	 * @since 2014.12.18
	 * @return
	 */
	public static SqlMesTemplate getDcolQueryTemplate()
	{
		return (SqlMesTemplate)BundleUtil.getServiceByBeanName("DColQueryTemplate");
	}
	
	/**
	 * 
	 * @author swcho
	 * @since 2015.09.02
	 * @return
	 */
	public static QueryTemplate getSqlMesTemplate()
	{
		return (QueryTemplate) ac.getBean("QueryTemplate");
	}
	
	public static DBLogWriterManager getDBLogWriter()
	{
		return (DBLogWriterManager) ac.getBean("DBLogWriterManager");
	}
	
	
}
