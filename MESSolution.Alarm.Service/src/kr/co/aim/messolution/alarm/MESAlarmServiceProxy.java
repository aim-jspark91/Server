package kr.co.aim.messolution.alarm;

import kr.co.aim.messolution.alarm.service.AlarmInfoUtil;
import kr.co.aim.messolution.alarm.service.AlarmServiceImpl;
import kr.co.aim.messolution.alarm.service.AlarmServiceUtil;
import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

import org.apache.commons.logging.Log;

public class MESAlarmServiceProxy extends MESStackTrace {
	//kkdd
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
	
	// Added by smkang on 2018.05.07 - Allow to access AlarmInfoUtil.
	public static AlarmInfoUtil getAlarmInfoUtil()
	{
		return (AlarmInfoUtil) BundleUtil.getServiceByBeanName(AlarmInfoUtil.class.getSimpleName());
	}
	
	// Added by smkang on 2018.05.07 - Allow to access AlarmServiceImpl.
	public static AlarmServiceImpl getAlarmServiceImpl()
	{
		return (AlarmServiceImpl) BundleUtil.getServiceByBeanName(AlarmServiceImpl.class.getSimpleName());
	}
	
	// Added by smkang on 2018.05.07 - Allow to access AlarmServiceUtil.
	public static AlarmServiceUtil getAlarmServiceUtil()
	{
		return (AlarmServiceUtil) BundleUtil.getBundleServiceClass(AlarmServiceUtil.class);
	}
}