package kr.co.aim.messolution.product;

import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.product.service.ProductInfoUtil;
import kr.co.aim.messolution.product.service.ProductServiceImpl;
import kr.co.aim.messolution.product.service.ProductServiceUtil;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

import org.apache.commons.logging.Log;

public class MESProductServiceProxy extends MESStackTrace {

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
	
	public static ProductInfoUtil getProductInfoUtil()
	{
		return (ProductInfoUtil) BundleUtil.getServiceByBeanName(ProductInfoUtil.class.getSimpleName());
	}
	
	public static ProductServiceImpl getProductServiceImpl()
	{
		return (ProductServiceImpl) BundleUtil.getServiceByBeanName(ProductServiceImpl.class.getSimpleName());
	}
	
	public static ProductServiceUtil getProductServiceUtil() 
	{
		return (ProductServiceUtil) BundleUtil.getServiceByBeanName(ProductServiceUtil.class.getSimpleName());
	}
}
