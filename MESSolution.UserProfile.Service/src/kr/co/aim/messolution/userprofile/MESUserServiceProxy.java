package kr.co.aim.messolution.userprofile;

import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.userprofile.service.UserProfileServiceImpl;
import kr.co.aim.messolution.userprofile.service.UserProfileServiceUtil;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

import org.apache.commons.logging.Log;

public class MESUserServiceProxy extends MESStackTrace {

	public static UserProfileServiceImpl getUserProfileServiceImpl()
	{
		return (UserProfileServiceImpl) BundleUtil.getServiceByBeanName(UserProfileServiceImpl.class.getSimpleName());
	}
	
	public static UserProfileServiceUtil getUserProfileServiceUtil()
	{
		return (UserProfileServiceUtil) BundleUtil.getServiceByBeanName(UserProfileServiceUtil.class.getSimpleName());
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
