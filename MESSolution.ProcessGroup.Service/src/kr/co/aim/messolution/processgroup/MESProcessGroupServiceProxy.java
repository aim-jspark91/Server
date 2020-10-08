package kr.co.aim.messolution.processgroup;

import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.processgroup.service.ProcessGroupInfoUtil;
import kr.co.aim.messolution.processgroup.service.ProcessGroupServiceImpl;
import kr.co.aim.messolution.processgroup.service.ProcessGroupServiceUtil;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

import org.apache.commons.logging.Log;

public class MESProcessGroupServiceProxy extends MESStackTrace {
	
	public static ProcessGroupServiceImpl getProcessGroupServiceImpl()
	{
		return (ProcessGroupServiceImpl) BundleUtil.getServiceByBeanName(ProcessGroupServiceImpl.class.getSimpleName());
	}
	
	public static ProcessGroupServiceUtil getProcessGroupServiceUtil()
	{
		return (ProcessGroupServiceUtil) BundleUtil.getBundleServiceClass(ProcessGroupServiceUtil.class);
	}
	
	public static ProcessGroupInfoUtil getProcessGroupInfoUtil()
	{
		return (ProcessGroupInfoUtil) BundleUtil.getServiceByBeanName(ProcessGroupInfoUtil.class.getSimpleName());
	}
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
}
