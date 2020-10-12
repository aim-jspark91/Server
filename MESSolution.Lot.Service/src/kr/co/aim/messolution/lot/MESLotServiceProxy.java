package kr.co.aim.messolution.lot;

import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.lot.service.LotInfoUtil;
import kr.co.aim.messolution.lot.service.LotServiceImpl;
import kr.co.aim.messolution.lot.service.LotServiceUtil;
import kr.co.aim.messolution.lot.service.SendServiceImpl;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

import org.apache.commons.logging.Log;

public class MESLotServiceProxy extends MESStackTrace {

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
	
	public static LotInfoUtil getLotInfoUtil()
	{
		return (LotInfoUtil) BundleUtil.getServiceByBeanName(LotInfoUtil.class.getSimpleName());
	}
	
	public static LotServiceImpl getLotServiceImpl()
	{
		return (LotServiceImpl) BundleUtil.getServiceByBeanName(LotServiceImpl.class.getSimpleName());
	} 

	public static LotServiceUtil getLotServiceUtil() 
	{
		return (LotServiceUtil) BundleUtil.getServiceByBeanName(LotServiceUtil.class.getSimpleName());
	}
	public static SendServiceImpl getSendServiceImpl() 
	{
		return (SendServiceImpl) BundleUtil.getServiceByBeanName(SendServiceImpl.class.getSimpleName());
	} 
}
