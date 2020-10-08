package kr.co.aim.messolution.durable;

import kr.co.aim.messolution.durable.service.DurableInfoUtil;
import kr.co.aim.messolution.durable.service.DurableServiceImpl;
import kr.co.aim.messolution.durable.service.DurableServiceUtil;
import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

import org.apache.commons.logging.Log;

public class MESDurableServiceProxy extends MESStackTrace {

	public static DurableServiceImpl getDurableServiceImpl()
	{
		return (DurableServiceImpl) BundleUtil.getServiceByBeanName(DurableServiceImpl.class.getSimpleName());
	}
	
	public static DurableServiceUtil getDurableServiceUtil()
	{
		return (DurableServiceUtil) BundleUtil.getBundleServiceClass(DurableServiceUtil.class);
	}
	
	public static DurableInfoUtil getDurableInfoUtil()
	{
		return (DurableInfoUtil) BundleUtil.getServiceByBeanName(DurableInfoUtil.class.getSimpleName());
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
}
