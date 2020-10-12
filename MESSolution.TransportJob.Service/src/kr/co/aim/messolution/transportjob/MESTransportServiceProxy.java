package kr.co.aim.messolution.transportjob;

import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.transportjob.service.TransportJobInfoUtil;
import kr.co.aim.messolution.transportjob.service.TransportJobServiceImpl;
import kr.co.aim.messolution.transportjob.service.TransportJobServiceUtil;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

import org.apache.commons.logging.Log;

public class MESTransportServiceProxy extends MESStackTrace {
	
	/**
	 * custom stack trace engine : must be implement in each proxy
	 * @author swcho
	 * @since 2014.02.19
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
	
	public static TransportJobInfoUtil getTransportJobInfoUtil()
	{
		return (TransportJobInfoUtil) BundleUtil.getServiceByBeanName(TransportJobInfoUtil.class.getSimpleName());
	}
	
	public static TransportJobServiceImpl getTransportJobServiceImpl()
	{
		return (TransportJobServiceImpl) BundleUtil.getServiceByBeanName(TransportJobServiceImpl.class.getSimpleName());
	} 

	public static TransportJobServiceUtil getTransportJobServiceUtil() 
	{
		return (TransportJobServiceUtil) BundleUtil.getServiceByBeanName(TransportJobServiceUtil.class.getSimpleName());
	} 
}
