package kr.co.aim.messolution.recipe;

import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.recipe.service.RecipeServiceImpl;
import kr.co.aim.messolution.recipe.service.RecipeServiceUtil;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

import org.apache.commons.logging.Log;

public class MESRecipeServiceProxy extends MESStackTrace {

	/**
	 * custom stack trace engine : must be implement in each proxy
	 * @author swcho
	 * @since 2014.02.11
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
	
	public static RecipeServiceImpl getRecipeServiceImpl()
	{
		return (RecipeServiceImpl) BundleUtil.getServiceByBeanName(RecipeServiceImpl.class.getSimpleName());
	}
	
	public static RecipeServiceUtil getRecipeServiceUtil()
	{
		return (RecipeServiceUtil) BundleUtil.getServiceByBeanName(RecipeServiceUtil.class.getSimpleName());
	}
}
