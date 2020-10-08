package kr.co.aim.messolution.extended.object;

import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.*;
import kr.co.aim.messolution.extended.object.management.impl.*;
import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;

import org.apache.commons.logging.Log;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ExtendedObjectProxy extends MESStackTrace implements ApplicationContextAware {

	private static ApplicationContext						ac;

	@Override
    public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		// TODO Auto-generated method stub
		this.ac = arg0;
	}

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

	//141103 by swcho : Lot reservation service changed
	public static ReserveLotService getReserveLotService()
		throws CustomException
	{
		return (ReserveLotService) CTORMUtil.loadServiceProxy(ReserveLot.class);
	}

	public static TransportJobCommandService getTransportJobCommandService()
		throws CustomException
	{
		return (TransportJobCommandService) CTORMUtil.loadServiceProxy(TransportJobCommand.class);
	}

	// Deleted by smkang on 2018.09.26 - CT_DSPSTOCKERZONE is used instead of CT_STOCKERZONEINFO. 
//	public static StockerZoneInfoService getStockerZoneInfo()
//		throws CustomException
//	{
//		return (StockerZoneInfoService) CTORMUtil.loadServiceProxy(StockerZoneInfo.class);
//	}

	public static LotQueueTimeService getQTimeService()
		throws CustomException
	{
		return (LotQueueTimeService) CTORMUtil.loadServiceProxy(LotQueueTime.class);
	}

	public static ReserveMaskListService getReserveMaskService()throws CustomException

	{
		return (ReserveMaskListService) CTORMUtil.loadServiceProxy(ReserveMaskList.class);
	}

	//141106 by swcho : added service
	public static RecipeService getRecipeService()
		throws CustomException
	{
		return (RecipeService) CTORMUtil.loadServiceProxy(Recipe.class);
	}

	public static RecipeParameterService getRecipeParamService()
		throws CustomException
	{
		return (RecipeParameterService) CTORMUtil.loadServiceProxy(RecipeParameter.class);
	}
	
	public static MaterialConsumedService getMaterialConsumedService() throws CustomException
	{
		return (MaterialConsumedService) CTORMUtil.loadServiceProxy(MaterialConsumed.class);
	}
	
	//141213 by swcho : added service
	public static AlarmService getAlarmService() throws CustomException
	{
		return (AlarmService) CTORMUtil.loadServiceProxy(Alarm.class);
	}

	public static AlarmDefinitionService getAlarmDefinitionService() throws CustomException
	{
		return (AlarmDefinitionService) CTORMUtil.loadServiceProxy(AlarmDefinition.class);
	}

	public static AlarmActionDefService getAlarmActionDefService() throws CustomException
	{
		return (AlarmActionDefService) CTORMUtil.loadServiceProxy(AlarmActionDef.class);
	}

	public static PanelInspDataService getPanelInspDataService()throws CustomException

	{
		return (PanelInspDataService) CTORMUtil.loadServiceProxy(PanelInspData.class);
	}

	public static PanelJudgeService getPanelJudgeService()throws CustomException

	{
		return (PanelJudgeService) CTORMUtil.loadServiceProxy(PanelJudge.class);
	}
	public static PanelDefectService getPanelDefectService()throws CustomException

	{
		return (PanelDefectService) CTORMUtil.loadServiceProxy(PanelDefect.class);
	}

	public static GlassJudgeService getGlassJudgeService() throws CustomException

	{
		return (GlassJudgeService) CTORMUtil.loadServiceProxy(GlassJudge.class);
	}

	//150906 by hwlee : added Service
	public static SampleLotService getSampleLotService() throws CustomException
	{
		return (SampleLotService) CTORMUtil.loadServiceProxy(SampleLot.class);
	}

	public static SampleLotCountService getSampleLotCountService() throws CustomException
	{
		return (SampleLotCountService) CTORMUtil.loadServiceProxy(SampleLotCount.class);
	}

	public static SampleProductService getSampleProductService() throws CustomException
	{
		return (SampleProductService) CTORMUtil.loadServiceProxy(SampleProduct.class);
	}

	public static SampleLotStateService getSampleLotStateService() throws CustomException
	{
		return (SampleLotStateService) CTORMUtil.loadServiceProxy(SampleLotState.class);
	}

	public static SampleReserveService getSampleReserveService() throws CustomException
	{
		return (SampleReserveService) CTORMUtil.loadServiceProxy(SampleReserve.class);
	}

	//151012 by hjung : added Service
	public static MachineAlarmListService getMachineAlarmListService() throws CustomException
	{
		return (MachineAlarmListService) CTORMUtil.loadServiceProxy(MachineAlarmList.class);
	}

	//151026 by hjung : added Service
	public static SortJobService getSortJobService() throws CustomException
	{
		return (SortJobService) CTORMUtil.loadServiceProxy(SortJob.class);
	}

	public static SortJobCarrierService getSortJobCarrierService() throws CustomException
	{
		return (SortJobCarrierService) CTORMUtil.loadServiceProxy(SortJobCarrier.class);
	}

	public static SortJobProductService getSortJobProductService() throws CustomException
	{
		return (SortJobProductService) CTORMUtil.loadServiceProxy(SortJobProduct.class);
	}

	public static VirtualGlassService getVirtualGlassService() throws CustomException
	{
		return (VirtualGlassService) CTORMUtil.loadServiceProxy(VirtualGlass.class);
	}

	public static ProductRequestHistoryService getProductRequestHistoryService() throws CustomException
	{
		return (ProductRequestHistoryService) CTORMUtil.loadServiceProxy(ProductRequestHistory.class);
	}

	public static ProductRequestPlanHistoryService getProductRequestPlanHistoryService() throws CustomException
	{
		return (ProductRequestPlanHistoryService) CTORMUtil.loadServiceProxy(ProductRequestPlanHistory.class);
	}
	
	public static ShipProductService getShipProductService() throws CustomException
	{
		return (ShipProductService) CTORMUtil.loadServiceProxy(ShipProduct.class);
	}

	public static ShipBoxService getShipBoxService() throws CustomException
	{
		return (ShipBoxService) CTORMUtil.loadServiceProxy(ShipBox.class);
	}

	public static ShipPalletService getShipPalletService() throws CustomException
	{
		return (ShipPalletService) CTORMUtil.loadServiceProxy(ShipPallet.class);
	}

	public static HelpDeskService getHelpDeskService() throws CustomException
	{
		return (HelpDeskService) CTORMUtil.loadServiceProxy(HelpDesk.class);
	}

	public static BulletinBoardService getBulletinBoardService() throws CustomException
	{
		return (BulletinBoardService) CTORMUtil.loadServiceProxy(BulletinBoard.class);
	}

	public static BulletinBoardAreaService getBulletinBoardAreaService() throws CustomException
	{
		return (BulletinBoardAreaService) CTORMUtil.loadServiceProxy(BulletinBoardArea.class);
	}

	public static FirstGlassLogService getFirstGlassService() throws CustomException
	{
		return (FirstGlassLogService) CTORMUtil.loadServiceProxy(FirstGlassLog.class);
	}

	public static FirstGlassLogMService getFirstGlassLogMService() throws CustomException
	{
		return (FirstGlassLogMService) CTORMUtil.loadServiceProxy(FirstGlassLogM.class);
	}

	public static FirstGlassLogSService getFirstGlassLogSService() throws CustomException
	{
		return (FirstGlassLogSService) CTORMUtil.loadServiceProxy(FirstGlassLogS.class);
	}

	public static FirstGlassLogDetailService getFirstGlassLogDetailService() throws CustomException
	{
		return (FirstGlassLogDetailService) CTORMUtil.loadServiceProxy(FirstGlassLogDetail.class);
	}

	public static WorkOrderPriorityService getWorkOrderPriorityService() throws CustomException
	{
		return (WorkOrderPriorityService) CTORMUtil.loadServiceProxy(WorkOrderPriority.class);
	}

	//151026 by hjung : added Service
	public static ReserveProductService getReserveProductService() throws CustomException
	{
		return (ReserveProductService) CTORMUtil.loadServiceProxy(ReserveProduct.class);
	}
	public static ReserveProductFixService getReserveProductFixService() throws CustomException
	{
		return (ReserveProductFixService) CTORMUtil.loadServiceProxy(ReserveProductFix.class);
	}

	public static FirstGlassJobService getFirstGlassJobService() throws CustomException
	{
		return (FirstGlassJobService) CTORMUtil.loadServiceProxy(FirstGlassJob.class);
	}

	public static FirstGlassLotService getFirstGlassLotService() throws CustomException
	{
		return (FirstGlassLotService) CTORMUtil.loadServiceProxy(FirstGlassLot.class);
	}

	public static FirstGlassProductService getFirstGlassProductService() throws CustomException
	{
		return (FirstGlassProductService) CTORMUtil.loadServiceProxy(FirstGlassProduct.class);
	}

	public static ReserveDummyGlassService getReserveDummyGlassService() throws CustomException
	{
		return (ReserveDummyGlassService) CTORMUtil.loadServiceProxy(ReserveDummyGlass.class);
	}

	//150906 by hwlee : added Service
	public static FlowSampleLotService getFlowSampleLotService() throws CustomException
	{
		return (FlowSampleLotService) CTORMUtil.loadServiceProxy(FlowSampleLot.class);
	}

	public static FlowSampleLotCountService getFlowSampleLotCountService() throws CustomException
	{
		return (FlowSampleLotCountService) CTORMUtil.loadServiceProxy(FlowSampleLotCount.class);
	}

	public static FlowSampleProductService getFlowSampleProductService() throws CustomException
	{
		return (FlowSampleProductService) CTORMUtil.loadServiceProxy(FlowSampleProduct.class);
	}

	public static MailingService getMailingService() throws CustomException
	{
		return (MailingService) CTORMUtil.loadServiceProxy(Mailing.class);
	}

	public static MailingUserService getMailingUserService() throws CustomException
	{
		return (MailingUserService) CTORMUtil.loadServiceProxy(MailingUser.class);
	}
	//170327 by yuhonghao : production plan
	public static ProductionPlanService getProductionPlanService()throws CustomException
	{
		return (ProductionPlanService) CTORMUtil.loadServiceProxy(ProductionPlan.class);
	}

	//170427 by yuhonghao : Plan Quantity
		public static PlanQuantityService getPlanQuantityService()throws CustomException
		{
			return (PlanQuantityService) CTORMUtil.loadServiceProxy(PlanQuantity.class);
		}

	public static DurableMultiHoldService getDurableMultiHoldService()throws CustomException
	{
	 return (DurableMultiHoldService) CTORMUtil.loadServiceProxy(DurableMultiHold.class);
	}

	//2018.02.22 huyeo - add
	public static PanelJudgeTestService getPanelJudgeTestService() throws CustomException
	{
		return (PanelJudgeTestService) CTORMUtil.loadServiceProxy(PanelJudgeTest.class);
	}
	// 2018.02.19
	public static OperationModeService getOperationModeService() throws CustomException
	{
	return (OperationModeService) CTORMUtil.loadServiceProxy(OperationMode.class);
	}

	//2018.02.20 hsryu - add
	public static ReworkLotService getreworkLotService() throws CustomException
	{
	return (ReworkLotService) CTORMUtil.loadServiceProxy(ReworkLot.class);
	}

	//2018.02.20 hsryu - add
	public static ReworkProductService getReworkProductService() throws CustomException
	{
		return (ReworkProductService) CTORMUtil.loadServiceProxy(ReworkProduct.class);
	}

	public static HQGlassJudgeService getHQGlassJudgeService() throws CustomException
	{
		return (HQGlassJudgeService) CTORMUtil.loadServiceProxy(HQGlassJudge.class);
	}

	public static LotActionService getLotActionService() throws CustomException
	{
		return (LotActionService) CTORMUtil.loadServiceProxy(LotAction.class);
	}

	public static PhtMaskStockerService getMaskStockerService() throws CustomException
	{
		return (PhtMaskStockerService) CTORMUtil.loadServiceProxy(PhtMaskStocker.class);
	}

	//2018.03.31, hhlee, add
	public static MachineGroupMachineService getMachineGroupMachineService() throws CustomException
	{
		return (MachineGroupMachineService) CTORMUtil.loadServiceProxy(MachineGroupMachine.class);
	}

	public static MaskSpecService getMaskSpecService() throws CustomException
	{
		return (MaskSpecService) CTORMUtil.loadServiceProxy(MaskSpec.class);
	}

	public static MaskService getMaskService() throws CustomException
	{
		return (MaskService) CTORMUtil.loadServiceProxy(Mask.class);
	}

	public static MaskSheetService getMaskSheetService() throws CustomException
	{
		return (MaskSheetService) CTORMUtil.loadServiceProxy(MaskSheet.class);
	}

	public static MaskSheetTrayService getMaskSheetTrayService() throws CustomException
	{
		return (MaskSheetTrayService) CTORMUtil.loadServiceProxy(MaskSheetTray.class);
	}

	public static OperActionService getOperActionService() throws CustomException
	{
		return (OperActionService) CTORMUtil.loadServiceProxy(OperAction.class);
	}

	public static PhtMaskStockerService getPhtMaskStockerService() throws CustomException
	{
		return (PhtMaskStockerService) CTORMUtil.loadServiceProxy(PhtMaskStocker.class);
	}

	public static ProductQueueTimeService getProductQueueTimeService() throws CustomException
	{
		return (ProductQueueTimeService) CTORMUtil.loadServiceProxy(ProductQueueTime.class);
	}

	public static ProductFlagService getProductFlagService() throws CustomException
	{
		return (ProductFlagService) CTORMUtil.loadServiceProxy(ProductFlag.class);
	}

	public static ExposureFeedBackService getExposureFeedBackService() throws CustomException
    {
        return (ExposureFeedBackService) CTORMUtil.loadServiceProxy(ExposureFeedBack.class);
    }

	public static MQCTemplateService getMQCTemplateService() throws CustomException
	{
		return (MQCTemplateService) CTORMUtil.loadServiceProxy(MQCTemplate.class);
	}

	public static CorresSampleLotService getCorresSampleLotService() throws CustomException
	{
		return (CorresSampleLotService) CTORMUtil.loadServiceProxy(CorresSampleLot.class);
	}
	public static MQCTemplatePositionService getMQCTemplatePositionService() throws CustomException
	{
		return (MQCTemplatePositionService) CTORMUtil.loadServiceProxy(MQCTemplatePosition.class);
	}
	
	public static MQCJobService getMQCJobService() throws CustomException
	{
		return (MQCJobService) CTORMUtil.loadServiceProxy(MQCJob.class);
	}
	
	public static MQCJobOperService getMQCJobOperService() throws CustomException
	{
		return (MQCJobOperService) CTORMUtil.loadServiceProxy(MQCJobOper.class);
	}
	
	public static MQCJobPositionService getMQCJobPositionService() throws CustomException
	{
		return (MQCJobPositionService) CTORMUtil.loadServiceProxy(MQCJobPosition.class);
	}
	public static ProductInUnitOrSubUnitService getProductInUnitOrSubUnitService() throws CustomException
    {
        return (ProductInUnitOrSubUnitService) CTORMUtil.loadServiceProxy(ProductInUnitOrSubUnit.class);
    }
	
	public static RecipeIdleTimeService getRecipeIdleTimeService() throws CustomException
    {
        return (RecipeIdleTimeService) CTORMUtil.loadServiceProxy(RecipeIdleTime.class);
    }
	
	public static RecipeIdleTimeLotService getRecipeIdleTimeLotService() throws CustomException
    {
        return (RecipeIdleTimeLotService) CTORMUtil.loadServiceProxy(RecipeIdleTimeLot.class);
    }
	
	/* 20180620, hhlee, Add Champber Group Info ==>> */
    public static ChamberGroupInfoService getChamberGroupInfoService() throws CustomException
    {
        return (ChamberGroupInfoService) CTORMUtil.loadServiceProxy(ChamberGroupInfo.class);
    }

    // Added by smkang on 2018.08.11 - For management of equipment idle time.
    public static MachineIdleTimeService getMachineIdleTimeService() throws CustomException {
        return (MachineIdleTimeService) CTORMUtil.loadServiceProxy(MachineIdleTime.class);
    }
    
    
    //2019.08.19 BY YU.CUI
    public static ProductSpecIdleTimeService getProductSpecIdleTimeService() throws CustomException {
        return (ProductSpecIdleTimeService) CTORMUtil.loadServiceProxy(ProductSpecIdleTime.class);
    }
    //2019.08.19 BY YU.CUI
    
    // Added by smkang on 2018.07.12 - For management of equipment idle time.
    public static MQCConditionService getMQCConditionService() throws CustomException {
        return (MQCConditionService) CTORMUtil.loadServiceProxy(MQCCondition.class);
    }
    
    // Added by jspark on 2018.07.24 - For management of Yield.
    public static YieldInfoService getYieldInfoService() throws CustomException
    {
        return (YieldInfoService) CTORMUtil.loadServiceProxy(YieldInfo.class);
    }
    
    public static YieldInfoLotService getYieldInfoLotService() throws CustomException
    {
        return (YieldInfoLotService) CTORMUtil.loadServiceProxy(YieldInfo.class);
    }
    
    public static DailyCheckService getDailyCheckService() throws CustomException
    {
        return (DailyCheckService) CTORMUtil.loadServiceProxy(DailyCheck.class);
    }
     
    public static MQCProductRelationService getMQCProductRelationService() throws CustomException
    {
        return (MQCProductRelationService) CTORMUtil.loadServiceProxy(MQCProductRelation.class);
    }
    
    // Added by smkang on 2018.08.13 - According to user's requirement, LotName/ReasonCode/Department/EventComment are necessary to be keys.
    public static LotMultiHoldService getLotMultiHoldService() throws CustomException {
    	return (LotMultiHoldService) CTORMUtil.loadServiceProxy(LotMultiHold.class);
    }
    // Added by smkang on 2018.08.13 - According to user's requirement, ProductName/ReasonCode/Department/EventComment are necessary to be keys.
    public static ProductMultiHoldService getProductMultiHoldService() throws CustomException {
    	return (ProductMultiHoldService) CTORMUtil.loadServiceProxy(ProductMultiHold.class);
    }
    
    public static RecipeRelationService getRecipeRelationService() throws CustomException
    {
        return (RecipeRelationService) CTORMUtil.loadServiceProxy(RecipeRelation.class);
    }
    
    public static DspAlternativeStockerService getDspAlternativeStockerService() throws CustomException
	{
		return (DspAlternativeStockerService) CTORMUtil.loadServiceProxy(DspAlternativeStocker.class);
	}
    
    public static DspConnectedStockerService getDspConnectedStockerService() throws CustomException
	{
		return (DspConnectedStockerService) CTORMUtil.loadServiceProxy(DspConnectedStocker.class);
	}
    
    public static DspReserveLotService getDspReserveLotService() throws CustomException
	{
		return (DspReserveLotService) CTORMUtil.loadServiceProxy(DspReserveLot.class);
	}
    
    public static DspReserveProductService getDspReserveProductService() throws CustomException
	{
		return (DspReserveProductService) CTORMUtil.loadServiceProxy(DspReserveProduct.class);
	}
    
    public static DspStockerKanbanService getDspStockerKanbanService() throws CustomException
	{
		return (DspStockerKanbanService) CTORMUtil.loadServiceProxy(DspStockerKanban.class);
	}
    
    public static DspStockerRegionService getDspStockerRegionService() throws CustomException
	{
		return (DspStockerRegionService) CTORMUtil.loadServiceProxy(DspStockerRegion.class);
	}
    
    public static DspStockerZoneEmptyCSTService getDspStockerZoneEmptyCSTService() throws CustomException
	{
		return (DspStockerZoneEmptyCSTService) CTORMUtil.loadServiceProxy(DspStockerZoneEmptyCST.class);
	}
    
    public static DspStockerZoneService getDspStockerZoneService() throws CustomException
	{
		return (DspStockerZoneService) CTORMUtil.loadServiceProxy(DspStockerZone.class);
	}
    
	public static DspMachineDispatchService getDspMachineDispatchService() throws CustomException
    {
        return (DspMachineDispatchService) CTORMUtil.loadServiceProxy(DspMachineDispatch.class);
    }
	
	// Added by jjyoo on 2018.09.16
    public static InhibitService getInhibitService() throws CustomException
    {
        return (InhibitService) CTORMUtil.loadServiceProxy(Inhibit.class);
    }
    // Added by jjyoo on 2018.09.16
    public static InhibitExceptionService getInhibitExceptionService() throws CustomException
    {
        return (InhibitExceptionService) CTORMUtil.loadServiceProxy(InhibitException.class);
    }
    
    /* 20180922, hhlee, add, ProcessedOperation ==>> */
    public static ProcessedOperationService getProcessedOperationService() throws CustomException
    {
        return (ProcessedOperationService) CTORMUtil.loadServiceProxy(ProcessedOperation.class);
    }   
    /* <<== 20180922, hhlee, add, ProcessedOperation */
    
    // Added by hsryu on 2018.09.20
    public static PermanentHoldInfoService getPermanentHoldInfoService() throws CustomException
    {
        return (PermanentHoldInfoService) CTORMUtil.loadServiceProxy(PermanentHoldInfo.class);
    }
    
    // Added by ParkJeongSu on 2018.09.26
    public static FileJudgeSettingService getFileJudgeSettingService() throws CustomException
    {
        return (FileJudgeSettingService) CTORMUtil.loadServiceProxy(FileJudgeSetting.class);
    }
    
    public static DefectResultService getDefectResultService() throws CustomException
    {
        return (DefectResultService) CTORMUtil.loadServiceProxy(DefectResult.class);
    }
    
    public static DefectRuleSettingService getDefectRuleSettingService() throws CustomException
    {
        return (DefectRuleSettingService) CTORMUtil.loadServiceProxy(DefectRuleSetting.class);
    }
    
    /* 20180930, hhlee, add, Chamber Idle Time ==>> */
    public static ChamberIdleTimeService getChamberIdleTimeService() throws CustomException {
        return (ChamberIdleTimeService) CTORMUtil.loadServiceProxy(ChamberIdleTime.class);
    }
    /* <<== 20180930, hhlee, add, Chamber Idle Time */
    
    /* 2018 10 18 jspark add */
    public static AfterActionService getAfterActionService() throws CustomException {
        return (AfterActionService) CTORMUtil.loadServiceProxy(AfterAction.class);
    }

    public static AutoMQCSettingService getAutoMQCSettingService() throws CustomException{
    	return (AutoMQCSettingService) CTORMUtil.loadServiceProxy(AutoMQCSetting.class);
    }
    
    /* 20181031, hsryu, add, ScrapProduct ==>> */
    public static ScrapProductService getScrapProductService() throws CustomException{
    	return (ScrapProductService) CTORMUtil.loadServiceProxy(ScrapProduct.class);
    }
    
    /* 20181113, ParkJeongSu, add, DSPEQPPMaxWip ==>> */
    public static DspEQPMaxWipService getDspEQPMaxWipService() throws CustomException{
    	return (DspEQPMaxWipService) CTORMUtil.loadServiceProxy(DspEQPMaxWip.class);
    }
    
    /* 20181113, ParkJeongSu, add, DspReserveProductNon ==>> */
    public static DspReserveProductNonService getDspReserveProductNonService() throws CustomException{
    	return (DspReserveProductNonService) CTORMUtil.loadServiceProxy(DspReserveProductNon.class);
    }
     
    //2019.01.16 dmlee
    public static AlarmMailTemplateService getAlarmMailTemplateService() throws CustomException{
    	return (AlarmMailTemplateService) CTORMUtil.loadServiceProxy(AlarmMailTemplate.class);
    }
    
    //2019.01.17_hsryu
	public static NonSortJobProductService getNonSortJobProductService() throws CustomException
	{
		return (NonSortJobProductService) CTORMUtil.loadServiceProxy(NonSortJobProduct.class);
	}

	/* 20190221, hhlee, add, SPC send Data */
	public static SpcProcessedOperationService getSpcProcessedOperationService() throws CustomException
    {
        return (SpcProcessedOperationService) CTORMUtil.loadServiceProxy(SpcProcessedOperation.class);
    }
	
	// Added by smkang on 2019.04.23 - According to Liu Hongwei's request, SCHsvr should be executed AP1 and AP2.
	//								   For avoid duplication of schedule job, running information should be recorded.
	public static ScheduleJobService getScheduleJobService() throws CustomException {
		return (ScheduleJobService) CTORMUtil.loadServiceProxy(ScheduleJob.class);
	}
	
	// 2019.06.10 dmlee
	public static RecipeRelationLastActiveVerService getRecipeRelationLastActiveVerService() throws CustomException
    {
        return (RecipeRelationLastActiveVerService) CTORMUtil.loadServiceProxy(RecipeRelationLastActiveVer.class);
    }
	
	// 2019.06.10 dmlee
	public static RecipeParamLastActiveVerService getRecipeParamLastActiveVerService() throws CustomException
    {
        return (RecipeParamLastActiveVerService) CTORMUtil.loadServiceProxy(RecipeParamLastActiveVer.class);
    }
	
	//2019.07.18 nskim
	public static UserTableAccessService getUserTableAccessService() throws CustomException
	{
		return (UserTableAccessService) CTORMUtil.loadServiceProxy(UserTableAccess.class);
	}
	
	//2019.07.18 nskim
	public static UserGroupTableAccessService getUserGroupTableAccessService() throws CustomException
	{
		return (UserGroupTableAccessService) CTORMUtil.loadServiceProxy(UserGroupTableAccess.class);
	}
	
	
	//2020.01.22 jhiying
	
	public static LocalRunExceptionService getLocalRunExceptionService() throws CustomException
	{
		return (LocalRunExceptionService) CTORMUtil.loadServiceProxy(LocalRunException.class);
	}
	
	public static TimeScheduleService getTimeScheduleService() throws CustomException
	{
		return (TimeScheduleService) CTORMUtil.loadServiceProxy(TimeSchedule.class); 
	}
}