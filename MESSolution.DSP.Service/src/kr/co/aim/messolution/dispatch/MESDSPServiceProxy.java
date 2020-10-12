package kr.co.aim.messolution.dispatch;

import kr.co.aim.messolution.dispatch.management.impl.EmptySTKBalanceService;
import kr.co.aim.messolution.dispatch.management.impl.EmptySTKPriorityService;
import kr.co.aim.messolution.dispatch.management.impl.KANBANService;
import kr.co.aim.messolution.dispatch.management.impl.MAXWIPService;
import kr.co.aim.messolution.dispatch.management.impl.MaxQTimeService;
import kr.co.aim.messolution.dispatch.management.impl.MaxQTime_SubOperationListService;
import kr.co.aim.messolution.dispatch.management.impl.MaxQTime_ToProductSpecListService;
import kr.co.aim.messolution.dispatch.management.impl.ProductSpecPriorityService;
import kr.co.aim.messolution.dispatch.management.impl.STKCstLimitService;
import kr.co.aim.messolution.dispatch.management.impl.STKLimitService;
import kr.co.aim.messolution.dispatch.management.impl.STKOperationPriorityService;
import kr.co.aim.messolution.dispatch.management.impl.STKPriorityService;
import kr.co.aim.messolution.dispatch.service.DSPInfoUtil;
import kr.co.aim.messolution.dispatch.service.DSPServiceImpl;
import kr.co.aim.messolution.dispatch.service.DSPServiceUtil;
import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

import org.apache.commons.logging.Log;

public class MESDSPServiceProxy extends MESStackTrace {
	
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

	public static KANBANService getKANBANService() throws CustomException
	{
		return (KANBANService)BundleUtil.getServiceByBeanName("KANBANService");
	}
	
	public static STKCstLimitService getSTKCstLimitService() throws CustomException
	{
		return (STKCstLimitService)BundleUtil.getServiceByBeanName("STKCstLimitService") ;
	}	
	
	public static MAXWIPService getMAXWIPService() throws CustomException
	{
		return (MAXWIPService)BundleUtil.getServiceByBeanName("MAXWIPService");
	}
	
	public static EmptySTKBalanceService getEmptySTKBalanceService() throws CustomException
	{
		return (EmptySTKBalanceService)BundleUtil.getServiceByBeanName("EmptySTKBalanceService");
	}
	
	public static EmptySTKPriorityService getEmptySTKPriorityService() throws CustomException
	{
		return (EmptySTKPriorityService)BundleUtil.getServiceByBeanName("EmptySTKPriorityService");
	}
	
	public static STKPriorityService getSTKPriorityService() throws CustomException
	{
		return (STKPriorityService)BundleUtil.getServiceByBeanName("STKPriorityService");
	}	
	
	public static STKOperationPriorityService getSTKOperationPriorityService() throws CustomException
	{
		return (STKOperationPriorityService)BundleUtil.getServiceByBeanName("STKOperationPriorityService");
	}	
	
	public static STKLimitService getSTKLimitService() throws CustomException
	{
		return (STKLimitService)BundleUtil.getServiceByBeanName("STKLimitService");
	}
	
	public static ProductSpecPriorityService getProductSpecPriorityService() throws CustomException
	{
		return (ProductSpecPriorityService)BundleUtil.getServiceByBeanName("ProductSpecPriorityService");
	}
	
	public static MaxQTimeService getMaxQTimeService() throws CustomException
	{
		return (MaxQTimeService)BundleUtil.getServiceByBeanName("MaxQTimeService");
	}
	
	public static MaxQTime_ToProductSpecListService getMaxQTime_ToProductSpecListService() throws CustomException
	{
		return (MaxQTime_ToProductSpecListService)BundleUtil.getServiceByBeanName("MaxQTime_ToProductSpecListService");
	}
	
	public static MaxQTime_SubOperationListService getMaxQTime_SubOperationListService() throws CustomException
	{
		return (MaxQTime_SubOperationListService)BundleUtil.getServiceByBeanName("MaxQTime_SubOperationListService");
	}

}
