package kr.co.aim.messolution.datacollection;

import kr.co.aim.messolution.datacollection.service.DataCollectionInfoUtil;
import kr.co.aim.messolution.datacollection.service.DataCollectionServiceImpl;
import kr.co.aim.messolution.datacollection.service.DataCollectionServiceUtil;
import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

import org.apache.commons.logging.Log;

public class MESEDCServiceProxy extends MESStackTrace {

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
	
	/**
	 * 
	 * @author aim System
	 * @since 2015.05.01
	 * @return
	 */
	public static DataCollectionServiceUtil getDataCollectionServiceUtil()
	{
		return (DataCollectionServiceUtil) BundleUtil.getServiceByBeanName(DataCollectionServiceUtil.class.getSimpleName());
	}
	
	/**
	 * 
	 * @author aim System
	 * @since 2015.05.01
	 * @return
	 */
	public static DataCollectionServiceImpl getDataCollectionServiceImpl()
	{
		return (DataCollectionServiceImpl) BundleUtil.getServiceByBeanName("DataCollectionServiceImpl");
	}
	
	/**
	 * 
	 * @author aim System
	 * @since 2015.05.01
	 * @return
	 */
	public static DataCollectionInfoUtil getDataCollectionInfoUtil()
	{
		return (DataCollectionInfoUtil) BundleUtil.getServiceByBeanName("DataCollectionInfoUtil");
	}
}
