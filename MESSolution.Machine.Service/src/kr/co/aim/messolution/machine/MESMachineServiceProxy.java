package kr.co.aim.messolution.machine;

import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.machine.service.MachineInfoUtil;
import kr.co.aim.messolution.machine.service.MachineServiceImpl;
import kr.co.aim.messolution.machine.service.MachineServiceUtil;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

import org.apache.commons.logging.Log;

public class MESMachineServiceProxy extends MESStackTrace {

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
	 * MachineInfoUtil service proxy
	 * @author swcho
	 * @since 2014.04.21
	 * @return
	 */
	public static MachineInfoUtil getMachineInfoUtil()
	{
		return (MachineInfoUtil) BundleUtil.getBundleServiceClass(MachineInfoUtil.class);
	}
	
	public static MachineServiceImpl getMachineServiceImpl() 
	{
		return (MachineServiceImpl) BundleUtil.getBundleServiceClass(MachineServiceImpl.class);
	}
	
	// Added by smkang on 201804.28 - getMachineServiceUtil method is missing.
	public static MachineServiceUtil getMachineServiceUtil() 
	{
		return (MachineServiceUtil) BundleUtil.getBundleServiceClass(MachineServiceUtil.class);
	}
}