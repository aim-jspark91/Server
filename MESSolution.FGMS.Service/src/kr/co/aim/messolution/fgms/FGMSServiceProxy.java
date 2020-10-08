package kr.co.aim.messolution.fgms;

import kr.co.aim.messolution.fgms.management.impl.CustomerService;
import kr.co.aim.messolution.fgms.management.impl.ERPINF_LG14Service;
import kr.co.aim.messolution.fgms.management.impl.FGMSProductRequestService;
import kr.co.aim.messolution.fgms.management.impl.PackingSTDDefService;
import kr.co.aim.messolution.fgms.management.impl.ProductService;
import kr.co.aim.messolution.fgms.management.impl.ShipRequestPSService;
import kr.co.aim.messolution.fgms.management.impl.ShipRequestService;
import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

import org.apache.commons.logging.Log;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class FGMSServiceProxy extends MESStackTrace implements ApplicationContextAware {

	private static ApplicationContext						ac;
	
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		// TODO Auto-generated method stub
		this.ac = arg0;
	}
	
	/**
	 * custom stack trace engine : must be implement in each proxy
	 * @author xzquan
	 * @since 2015.01.29
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
	
	public static PackingSTDDefService getPackingSTDDefService() throws CustomException
	{
		return (PackingSTDDefService) BundleUtil.getServiceByBeanName("PackingSTDService");
	}
	
	public static CustomerService getCustomerService() throws CustomException
	{
		return (CustomerService) BundleUtil.getServiceByBeanName("CustomerService");
	}
	
	public static ProductService getProductService() throws CustomException
	{
		return (ProductService) BundleUtil.getServiceByBeanName("ProductService");
	}
	
	public static ShipRequestService getShipRequestService() throws CustomException
	{
		return (ShipRequestService) BundleUtil.getServiceByBeanName("ShipRequestService");
	}
	
	public static ShipRequestPSService getShipRequestPSService() throws CustomException
	{
		return (ShipRequestPSService) BundleUtil.getServiceByBeanName("ShipRequestPSService");
	}
	
	public static ERPINF_LG14Service getERPINF_LG14Service() throws CustomException
	{
		return (ERPINF_LG14Service) BundleUtil.getServiceByBeanName("ERPINF_LG14Service");
	}
	public static FGMSProductRequestService getFGMSProductRequestService() throws CustomException
	{
		return (FGMSProductRequestService) BundleUtil.getServiceByBeanName("FGMSProductRequestService");
	}
}
 	