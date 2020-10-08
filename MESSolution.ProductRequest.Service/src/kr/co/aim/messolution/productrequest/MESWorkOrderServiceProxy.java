package kr.co.aim.messolution.productrequest;

import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.productrequest.service.ProductRequestInfoUtil;
import kr.co.aim.messolution.productrequest.service.ProductRequestServiceImpl;
import kr.co.aim.messolution.productrequest.service.ProductRequestServiceUtil;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

import org.apache.commons.logging.Log;

public class MESWorkOrderServiceProxy extends MESStackTrace {


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
	
	public static ProductRequestInfoUtil getProductRequestInfoUtil()
	{
		return (ProductRequestInfoUtil) BundleUtil.getServiceByBeanName(ProductRequestInfoUtil.class.getSimpleName());
	}
	
	public static ProductRequestServiceImpl getProductRequestServiceImpl()
	{
		return (ProductRequestServiceImpl) BundleUtil.getServiceByBeanName(ProductRequestServiceImpl.class.getSimpleName());
	}
	
	public static ProductRequestServiceUtil getProductRequestServiceUtil()
	{
		return (ProductRequestServiceUtil) BundleUtil.getServiceByBeanName(ProductRequestServiceUtil.class.getSimpleName());
	}
}
