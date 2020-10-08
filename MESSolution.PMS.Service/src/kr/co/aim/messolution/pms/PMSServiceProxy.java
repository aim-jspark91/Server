package kr.co.aim.messolution.pms;

import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.pms.management.impl.BMService;
import kr.co.aim.messolution.pms.management.impl.BMUsePartService;
import kr.co.aim.messolution.pms.management.impl.BMUserService;
import kr.co.aim.messolution.pms.management.impl.BulletinBoardAreaService;
import kr.co.aim.messolution.pms.management.impl.BulletinBoardService;
import kr.co.aim.messolution.pms.management.impl.CheckIDService;
import kr.co.aim.messolution.pms.management.impl.CheckListService;
import kr.co.aim.messolution.pms.management.impl.CreateClothesService;
import kr.co.aim.messolution.pms.management.impl.ESDTestService;
import kr.co.aim.messolution.pms.management.impl.ESDTestSetService;
import kr.co.aim.messolution.pms.management.impl.MaintenanceCheckService;
import kr.co.aim.messolution.pms.management.impl.MaintenanceService;
import kr.co.aim.messolution.pms.management.impl.MappingItemService;
import kr.co.aim.messolution.pms.management.impl.PMCodeService;
import kr.co.aim.messolution.pms.management.impl.PurchaseService;
import kr.co.aim.messolution.pms.management.impl.RequestSparePartService;
import kr.co.aim.messolution.pms.management.impl.SparePartGroupService;
import kr.co.aim.messolution.pms.management.impl.SparePartInOutService;
import kr.co.aim.messolution.pms.management.impl.SparePartService;
import kr.co.aim.messolution.pms.management.impl.UserGradeFunctionService;
import kr.co.aim.messolution.pms.management.impl.UserGradeService;
import kr.co.aim.messolution.pms.management.impl.VendorService;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

import org.apache.commons.logging.Log;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class PMSServiceProxy extends MESStackTrace implements ApplicationContextAware {

	private static ApplicationContext						ac;
	
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		// TODO Auto-generated method stub
		this.ac = arg0;
	}
	
	public static Object executeMethod(Log eventLogger, String beanName, String methodName, Object... args)
		throws CustomException
	{
		return executeMethodMonitor(eventLogger, beanName, methodName, args);
	}	
	public static BMService getBMService() throws CustomException
	{
		return (BMService)BundleUtil.getServiceByBeanName("BMService");
	}
	public static BMUsePartService getBMUsePartService() throws CustomException
	{
		return (BMUsePartService)BundleUtil.getServiceByBeanName("BMUsePartService");
	}
	public static BMUserService getBMUserService() throws CustomException
	{
		return (BMUserService)BundleUtil.getServiceByBeanName("BMUserService");
	}
	public static PurchaseService getPurchaseService() throws CustomException
	{
		return (PurchaseService)BundleUtil.getServiceByBeanName("PurchaseService");
	}
	public static RequestSparePartService getRequestSparePartService() throws CustomException
	{
		return (RequestSparePartService)BundleUtil.getServiceByBeanName("RequestSparePartService");
	}
	public static SparePartService getSparePartService() throws CustomException
	{
		return (SparePartService)BundleUtil.getServiceByBeanName("SparePartService");
	}
	public static SparePartGroupService getSparePartGroupService() throws CustomException
	{
		return (SparePartGroupService)BundleUtil.getServiceByBeanName("SparePartGroupService");
	}
	public static SparePartInOutService getSparePartInOutService() throws CustomException
	{
		return (SparePartInOutService)BundleUtil.getServiceByBeanName("SparePartInOutService");
	}
	public static VendorService getVendorService() throws CustomException
	{
		return (VendorService)BundleUtil.getServiceByBeanName("VendorService");
	}
	public static PMCodeService getPMCodeService() throws CustomException
	{
		return (PMCodeService)BundleUtil.getServiceByBeanName("PMCodeService");
	}
	public static MappingItemService getMappingItemService() throws CustomException
	{
		return (MappingItemService)BundleUtil.getServiceByBeanName("MappingItemService");
	}
	public static CheckListService getCheckListService() throws CustomException
	{
		return (CheckListService)BundleUtil.getServiceByBeanName("CheckListService");
	}
	public static MaintenanceService getMaintenanceService() throws CustomException
	{
		return (MaintenanceService)BundleUtil.getServiceByBeanName("MaintenanceService");
	}
	public static CheckIDService getCheckIDService() throws CustomException
	{
		return (CheckIDService)BundleUtil.getServiceByBeanName("CheckIDService");
	}
	public static MaintenanceCheckService getCompleteCheckPMService() throws CustomException
	{
		return (MaintenanceCheckService)BundleUtil.getServiceByBeanName("MaintenanceCheckService");
	}
	public static BulletinBoardService getBulletinBoardService() throws CustomException
	{
		return (BulletinBoardService)BundleUtil.getServiceByBeanName("BulletinBoardService");
	}
	public static BulletinBoardAreaService getBulletinBoardAreaService() throws CustomException
	{
		return (BulletinBoardAreaService)BundleUtil.getServiceByBeanName("BulletinBoardAreaService");
	}
	
	public static CreateClothesService getCreateClothesService() throws CustomException
	{
		return (CreateClothesService)BundleUtil.getServiceByBeanName("CreateClothesService");
	}
	
	public static ESDTestService getESDTestService() throws CustomException
	{
		return (ESDTestService)BundleUtil.getServiceByBeanName("ESDTestService");
	}
	
	public static ESDTestSetService getESDTestSetService() throws CustomException
	{
		return (ESDTestSetService)BundleUtil.getServiceByBeanName("ESDTestSetService");
	}
	
	public static UserGradeService getUserGradeService() throws CustomException
	{
		return (UserGradeService)BundleUtil.getServiceByBeanName("UserGradeService");
	}
	
	public static UserGradeFunctionService getUserGradeFunctionService() throws CustomException
	{
		return (UserGradeFunctionService)BundleUtil.getServiceByBeanName("UserGradeFunctionService");
	}
	
}
 	