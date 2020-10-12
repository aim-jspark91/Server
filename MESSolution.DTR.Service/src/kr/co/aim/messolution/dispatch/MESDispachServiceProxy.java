package kr.co.aim.messolution.dispatch;

import org.apache.commons.logging.Log;

import kr.co.aim.messolution.dispatch.management.impl.STKPriorityService;
import kr.co.aim.messolution.dispatch.service.DSPInfoUtil;
import kr.co.aim.messolution.dispatch.service.DSPServiceImpl;
import kr.co.aim.messolution.dispatch.service.DSPServiceUtil;
import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

public class MESDispachServiceProxy extends MESStackTrace {
	
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
	
	public static DSPInfoUtil getDSPInfoUtil()
	{
		return (DSPInfoUtil) BundleUtil.getServiceByBeanName(DSPInfoUtil.class.getSimpleName());
	}
	
	public static DSPServiceImpl getDSPServiceImpl()
	{
		return (DSPServiceImpl) BundleUtil.getServiceByBeanName(DSPServiceImpl.class.getSimpleName());
	} 

	public static DSPServiceUtil getDSPServiceUtil() 
	{
		return (DSPServiceUtil) BundleUtil.getServiceByBeanName(DSPServiceUtil.class.getSimpleName());
	} 
	
	public static STKPriorityService getSTKPriorityService() throws CustomException
	{
		return (STKPriorityService)BundleUtil.getServiceByBeanName("STKPriorityService");
	}	
}
