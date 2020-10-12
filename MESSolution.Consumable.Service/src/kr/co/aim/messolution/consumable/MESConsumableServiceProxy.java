package kr.co.aim.messolution.consumable;

import kr.co.aim.messolution.consumable.service.ConsumableInfoUtil;
import kr.co.aim.messolution.consumable.service.ConsumableServiceImpl;
import kr.co.aim.messolution.consumable.service.ConsumableServiceUtil;
import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

import org.apache.commons.logging.Log;

public class MESConsumableServiceProxy extends MESStackTrace {
	//commit test
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
	
	public static ConsumableServiceImpl getConsumableServiceImpl()
	{
		return (ConsumableServiceImpl) BundleUtil.getServiceByBeanName(ConsumableServiceImpl.class.getSimpleName());
	}
	
	public static ConsumableInfoUtil getConsumableInfoUtil()
	{
		return (ConsumableInfoUtil) BundleUtil.getServiceByBeanName(ConsumableInfoUtil.class.getSimpleName());
	}
	
	public static ConsumableServiceUtil getConsumableServiceUtil()
	{
		return (ConsumableServiceUtil) BundleUtil.getServiceByBeanName(ConsumableServiceUtil.class.getSimpleName());
	}
}
