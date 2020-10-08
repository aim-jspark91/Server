package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AutoMQCSetting;
import kr.co.aim.messolution.extended.object.management.data.LocalRunException;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.util.sys.SystemPropHelper;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class TrackOutLotPU extends SyncHandler {

	@SuppressWarnings("unchecked")
	@Override
	public Object doWorks(Document doc)throws CustomException
	{ 
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String lotJudge = SMessageUtil.getBodyItemValue(doc, "LOTJUDGE", false);		
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		String sampleFlowName = "";
		
		//for common
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		
		//Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);//modfiy by GJJ 20200218 Trackout  ÐèÒªËø±í mantis£º5676
		Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));//add by GJJ 20200218 Trackout  ÐèÒªËø±í mantis£º5676

		// -------------------------------------------------------------------------------------------------------
		// Added by smkang on 2019.05.30 - Need to check LotProcessState and LotHoldState.
		if (!StringUtil.equals(lotData.getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Run))
			throw new CustomException("LOT-9003", lotName +". Current State is " + lotData.getLotProcessState());
	    
		CommonValidation.checkLotHoldState(lotData);
		// -------------------------------------------------------------------------------------------------------
		
		ProductKey productkey = new ProductKey();
		SetMaterialLocationInfo MaterialLocationInfo=new SetMaterialLocationInfo();
		EventInfo setMaterialLocationeventInfo = EventInfoUtil.makeEventInfo("SetMaterialLocation", getEventUser(), getEventComment(), null, null);
		
		for (Element product : productList )
		{   
			productkey.setProductName(SMessageUtil.getChildText(product, "PRODUCTNAME", true));
			
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			Product productData = ProductServiceProxy.getProductService().selectByKey(productkey);
			Product productData = ProductServiceProxy.getProductService().selectByKeyForUpdate(productkey);
			
			if(!productData.getMaterialLocationName().equals(""))
			{
				MaterialLocationInfo.setMaterialLocationName("");
				ProductServiceProxy.getProductService().update(productData);
				MESProductServiceProxy.getProductServiceImpl().setMaterialLocation(setMaterialLocationeventInfo, productData, MaterialLocationInfo);
			}
			
			/* 20180921, hhlee, Modify, ==>> */
            //MESProductServiceProxy.getProductServiceImpl().setProcessedOperationBeforeAOI(product, machineName);
            MESProductServiceProxy.getProductServiceImpl().setProcessedOperationBeforeAOI(eventInfo, product, machineName);
            /* <<== 20180921, hhlee, Modify, */
		}

		MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		
		// Validation CST Hold
		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(carrierName);
		Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
		CommonValidation.CheckDurableHoldState(durableData);
        
		String logicalSlotMap = "";
		
		try
		{
	        logicalSlotMap = MESProductServiceProxy.getProductServiceUtil().getSlotMapInfo(durableData);
		}
		catch(Throwable e)
		{
			eventLog.warn("Fail LogicalSlotMap!");
		}
        
		if(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PS")
				&& CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("SORT"))
		{	
			String sortjob = this.getSortJobName(lotName);
		    if(sortjob != null && !sortjob.isEmpty())
		    {
				SortJob sortJob = ExtendedObjectProxy.getSortJobService().selectByKey(false, new Object[] {sortjob});
				eventInfo = EventInfoUtil.makeEventInfo("SorterJobEnded", getEventUser(), getEventComment(), null, null);
				sortJob.setJobState(GenericServiceProxy.getConstantMap().SORT_JOBSTATE_ENDED);			
				ExtendedObjectProxy.getSortJobService().modify(eventInfo, sortJob);
			}		
		}
		
		// -----------------------------------------------------------------------------------------------------------------------------------------------------------
		// Added by hsryu on 2018.09.12 - Hold check

		// check yield. if yield is lack, reserve AHold in FutureAction.
		MESLotServiceProxy.getLotServiceImpl().checkYield(lotData.getKey().getLotName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), eventInfo);
	
		// if currently in the sampleFlow & lastOperation, check SampleOutHoldFlag and reserve AHold in FutureAction. 
		MESLotServiceProxy.getLotServiceImpl().checkSampleOutHoldFlag(lotData, eventInfo);

		// check releaseLot Hold(after UPK, hold or notHold)
		MESLotServiceProxy.getLotServiceUtil().checkReserveLotHold(machineName, lotData, eventInfo);
		
		String machineRecipeName = null;
			
		try
		{
			machineRecipeName = lotData.getMachineRecipeName();
		}
		catch (Exception ex)
		{
		}

		if(!StringUtil.isEmpty(machineRecipeName))
		{
		MESProductServiceProxy.getProductServiceImpl().checkRecipeIdleTimeFirstFlag(lotData,
																					carrierName,
																					machineName, 
																					machineRecipeName, 
																					machineSpecData.getAreaName(), 
																					lotData.getFactoryName(), 
																					eventInfo);
		}
		
		// -----------------------------------------------------------------------------------------------------------------------------------------------------------

		Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
		Map<String, String> assignCarrierUdfs = new HashMap<String, String>();
		
 		Lot trackOutLot = MESLotServiceProxy.getLotServiceUtil().getTrackOutLot(eventInfo, carrierName, lotName, productList);
		
		if (trackOutLot == null)
		{
			throw new CustomException("LOT-XXXX", carrierName);
		}
				
		//2019.01.31_hsryu_Check PanelJudge Definition. Mantis 0002640.
		MESLotServiceProxy.getLotServiceUtil().checkPanelJudgeInDummyOperation(trackOutLot);
		
		MESLotServiceProxy.getLotServiceUtil().checkTrackFlag(productList, machineName, true, eventInfo);
		
		List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotServiceUtil().setProductPGSRCForTrackOutForOPI(lotData, productList);
		
		String beforeProductSpecName = trackOutLot.getProductSpecName();
		String beforeECCode = trackOutLot.getUdfs().get("ECCODE");
		String beforeProcessFlowName = trackOutLot.getProcessFlowName();
		String beforeProcessFlowVersion = trackOutLot.getProcessFlowVersion();
		String beforeProcessOperationName = trackOutLot.getProcessOperationName();
		String beforeProcessOperationVersion = trackOutLot.getProcessOperationVersion();
		
   		String decideSampleNodeStack = "";
		
   		boolean aHoldFlag = false;
   		
   		try {
   	   		// MQC Job¿¡ »ý¼ºµÈ °øÁ¤µé°ú Lot ÀÇ ProcessFlow ÀÇ °øÁ¤µéÀÌ ´Ù¸£´Ù¸é aHoldFlag ¸¦ true ·Î ¼³Á¤
   			aHoldFlag = MESLotServiceProxy.getLotServiceImpl().getAholdFlagMQCOperationListAndProcessFlowOperationList(trackOutLot.getKey().getLotName(),beforeProcessFlowName);
		} catch (Exception e) {
			
		}
   		if(!aHoldFlag){
   			aHoldFlag = MESLotServiceProxy.getLotServiceUtil().isExistAhold(trackOutLot.getKey().getLotName(), beforeProcessFlowName, beforeProcessOperationName);
   		}
   		
   		
   		String superLotFlag = trackOutLot.getUdfs().get("SUPERLOTFLAG");
   		if (StringUtils.isEmpty(superLotFlag) || !superLotFlag.equals("Y"))
   		{
			EventInfo eventInfoForDecideSample = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
			eventInfoForDecideSample.setEventTime(eventInfo.getEventTime());
			eventInfoForDecideSample.setEventTimeKey(eventInfo.getEventTimeKey());
			
   			MESLotServiceProxy.getLotServiceUtil().getDecideSample(doc, eventInfoForDecideSample, trackOutLot);
   		}
   		
   		// aHold is exist, not go to SampleFlow.
   		if(!aHoldFlag)
   		{
   			sampleFlowName = MESLotServiceProxy.getLotServiceUtil().checkReservedSamplingInfo(trackOutLot.getKey().getLotName(), beforeProcessFlowName, beforeProcessFlowVersion, beforeProcessOperationName, eventInfo);
   			
   	   		if (StringUtils.isEmpty(sampleFlowName) && (StringUtils.isEmpty(superLotFlag) || superLotFlag.equals("N")))
   	   			sampleFlowName = MESLotServiceProxy.getLotServiceUtil().checkNormalSamplingInfo(trackOutLot.getKey().getLotName(), beforeProcessOperationName, eventInfo);
   			
   			if (StringUtil.isNotEmpty(sampleFlowName))
   				decideSampleNodeStack = MESLotServiceProxy.getLotServiceUtil().getOperFirstNodeStack(sampleFlowName);
   		}
   		// ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		
		//20180504, kyjung, QTime
		eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		EventInfo eventInfo1 = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		List<Map<String, Object>> queuePolicyData = null;
		
		Map<String, Object> qTimeTPEFOPolicyData = MESProductServiceProxy.getProductServiceImpl().checkPriorityPolicy(trackOutLot);

		if(qTimeTPEFOPolicyData != null)
		{
			queuePolicyData = MESProductServiceProxy.getProductServiceImpl().checkQTimePolicy(qTimeTPEFOPolicyData);
			
			if(queuePolicyData != null)
			{
				for (Element product : productList )
				{   
					String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", true);
					
					MESProductServiceProxy.getProductServiceImpl().EnteredQTime(eventInfo, eventInfo1, productName, queuePolicyData);
				}
			}
		}
		
		List<Product> productDataList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);

		// 20180504, kyjung, QTime
		for (Product productData : productDataList) {
			MESProductServiceProxy.getProductServiceImpl().closeQTime(eventInfo, productData, "TrackIn");
		}

		//TK OUT
 		Lot afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().trackOutLot(eventInfo, trackOutLot, portData, 
												carrierName, lotJudge, machineName, "",
												productPGSRCSequence, assignCarrierUdfs, deassignCarrierUdfs, decideSampleNodeStack, aHoldFlag, null);
 				
        /* Array 20180807, Add [Process Flag Update] ==>> */            
        MESProductServiceProxy.getProductServiceUtil().setProdutProcessFlag(eventInfo, afterTrackOutLot, logicalSlotMap, true);
        /* <<== Array 20180807, Add [Process Flag Update] */
 		
		//20180604, kyjung, MQC
		ProcessFlowKey processFlowKey = new ProcessFlowKey();
		processFlowKey.setFactoryName(afterTrackOutLot.getFactoryName());
		processFlowKey.setProcessFlowName(beforeProcessFlowName);
		processFlowKey.setProcessFlowVersion(afterTrackOutLot.getProcessFlowVersion());
		ProcessFlow processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
		
		if(processFlowData != null)
		{
			if(StringUtils.equals(processFlowData.getProcessFlowType(), "MQC"))
			{
				MESLotServiceProxy.getLotServiceImpl().checkMQCOperationListAndProcessFlowOperationListHoldLot(afterTrackOutLot.getKey().getLotName(),beforeProcessFlowName);
				
				// MODIFY 2019.04.24
				EventInfo eventInfoMQC = EventInfoUtil.makeEventInfo("UpdateMQCCount", getEventUser(), getEventComment(), null, null);
				MESProductServiceProxy.getProductServiceImpl().updateMQCCountToProduct(afterTrackOutLot, eventInfoMQC, processFlowData, beforeProcessOperationName);
				
				if(StringUtil.equals(afterTrackOutLot.getLotState(), "Completed"))
				{
					eventInfoMQC.setEventName("FinishMQCJob");
					eventInfoMQC.setCheckTimekeyValidation(false);
					eventInfoMQC.setEventTime( TimeStampUtil.getCurrentTimestamp());
					eventInfoMQC.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
					MESProductServiceProxy.getProductServiceImpl().checkFinishMQCJob(afterTrackOutLot, eventInfoMQC, processFlowData);
				}
			}
		}
		
		if(!StringUtils.equals(processFlowData.getProcessFlowType(), "MQC"))
		{
			MESLotServiceProxy.getLotServiceImpl().chkAfterCompleteLot(afterTrackOutLot,eventInfo);
		}
		else {
			// 2018.10.18 Mentis 682
			MESLotServiceProxy.getLotServiceImpl().chkAfterCompleteLotMQC(afterTrackOutLot,eventInfo);
		}
        
		// 2019.05.31_hsryu_Add Logic. Check BHold ! 
        afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().checkBHoldAndOperHold(afterTrackOutLot.getKey().getLotName(), eventInfo);
        
        //start by jhying on20200303 mantis:5435
        List<LocalRunException> localRunExceptionList = ExtendedObjectProxy.getLocalRunExceptionService().select("WHERE LOTNAME = ? AND PROCESSFLOWNAME = ? AND  PROCESSOPERATIONNAME = ? AND  MACHINENAME = ?"
                ,new Object[]{lotData.getKey().getLotName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(),machineName});
        if(localRunExceptionList != null && localRunExceptionList.size()>0 ){
      
             if(MESLotServiceProxy.getLotServiceImpl().isExistLocalRunException(lotData.getKey().getLotName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(),machineName ))
             {
            	 localRunExceptionList.get(0).setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_EXECUTED);

			   ExtendedObjectProxy.getLocalRunExceptionService().remove(eventInfo, localRunExceptionList.get(0));
            }
        }
        
        	
      //end by jhying on20200303 mantis:5435
        
		setNextInfo(doc, afterTrackOutLot);
			
		try
		{
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
		}
		
		catch(Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}

		// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//		//Note clear - YJYU
//		Lot lotData_Note = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//		Map<String, String> udfs_note = lotData_Note.getUdfs();
//		udfs_note.put("NOTE", "");
//		LotServiceProxy.getLotService().update(lotData_Note);
		Map<String, String> updateUdfs = new HashMap<String, String>();
		updateUdfs.put("NOTE", "");
		MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
		
		// 2018-11-21 Park Jeong Su Add for ClearName And Update LastRuntime
		clearCarrerNameOnAutoMQCSetting(carrierName,machineName);
		
		
		return doc;
	}

	/**
	 * must be in here at last point of event
	 * @since 2015.08.21
	 * @author swcho
	 * @param doc
	 * @param lotData
	 */
	private void setNextInfo(Document doc, Lot lotData)
	{
		try
		{
			StringBuilder strComment = new StringBuilder();
			strComment.append("LotName").append("[").append(lotData.getKey().getLotName()).append("]").append("\n")
						.append("LotGrade").append("[").append(lotData.getLotGrade()).append("]").append("\n")
						.append("NextFlow").append("[").append(lotData.getProcessFlowName()).append("]").append("\n")
						.append("NextOperation").append("[").append(lotData.getProcessOperationName()).append("]").append("\n");
			
			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, strComment.toString());
		}
		catch (Exception ex)
		{
			eventLog.warn("Note after TK OUT is nothing");
		}
	}
	
	/**
	 * get sortJob
	 * @since 2017.06.15
	 * @author zhongsl
	 * @param doc
	 * @param lotName
	 */
	private String getSortJobName(String lotName)
			throws CustomException
	{
		String NEWLINE = SystemPropHelper.CR;
		
		StringBuffer sqlBuffer = new StringBuffer("")
			.append("SELECT C.JOBNAME").append(NEWLINE)
			.append("  FROM CT_SORTJOB C,CT_SORTJOBCARRIER CS").append(NEWLINE)
			.append(" WHERE C.JOBNAME = CS.JOBNAME(+)").append(NEWLINE)
			.append("   AND CS.LOTNAME = ?").append(NEWLINE)
			.append("   AND CS.TRANSFERDIRECTION = ?").append(NEWLINE)
			.append("   AND C.JOBSTATE NOT IN (?)").append(NEWLINE)
			.append(" ORDER BY C.TIMEKEY DESC").append(NEWLINE);
		
		String sqlStmt = sqlBuffer.toString();
		
		Object[] bindSet = new String[]{lotName, "SOURCE", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_ENDED};
		try
		{
			List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt, bindSet);
		
			if(sqlResult.size() > 0)
			{
				return sqlResult.get(0).get("JOBNAME").toString();
			}
			else
			{
				return null;
			}
		}
		catch (FrameworkErrorSignal de)
		{
			throw new CustomException("SYS-9999", "Error", de.getMessage());
		}
	}
	
	private void clearCarrerNameOnAutoMQCSetting(String carrerName,String machineName )
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOut", getEventUser(), getEventComment(), null, null);
		String condition = " WHERE MACHINENAME = ? AND CARRIERNAME = ? ";
		Object[] bindSet = new Object[] { machineName ,  carrerName};
		
		try {
			List<AutoMQCSetting> autoMQCSettingList = ExtendedObjectProxy.getAutoMQCSettingService().select(condition, bindSet);
			for(AutoMQCSetting autoMQCSetting : autoMQCSettingList)
			{
				autoMQCSetting.setCarrierName("");
				autoMQCSetting.setLastRunTime(eventInfo.getEventTime());
				
				ExtendedObjectProxy.getAutoMQCSettingService().modify(eventInfo, autoMQCSetting);
			}
		} catch (CustomException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Throwable e)
		{
			eventLog.warn("Not AutoMQC Data!");
		}
	}
}
