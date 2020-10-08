package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.CorresSampleLot;
import kr.co.aim.messolution.extended.object.management.data.LotAction;
import kr.co.aim.messolution.extended.object.management.data.LotMultiHold;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.object.ErrorDef;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ForceOutSampling extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String sReturnFlowName = SMessageUtil.getBodyItemValue(doc, "RETURNFLOWNAME", true);
		String sReturnOperName = SMessageUtil.getBodyItemValue(doc, "RETURNOPERATION", true);
		String sSampleOutHoldFlag = SMessageUtil.getBodyItemValue(doc, "SAMPLEOUTHOLDFLAG", true);
		String sBeforeNodeStack = SMessageUtil.getBodyItemValue(doc, "BEFORENODESTACK", false);
		String sSampleFlag = SMessageUtil.getBodyItemValue(doc, "SAMPLEFLAG", true);
		String note = SMessageUtil.getBodyItemValue(doc, "NOTE", true);
		String sNodeStack = SMessageUtil.getBodyItemValue(doc, "NODESTACK", false);// add by GJJ 20200220 mantis:5695

		
		// Modified by smkang on 2019.06.17 - According to Liu Hongwei's request, Lot should be locked to be prevented concurrent executing.
//		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		eventLog.debug("Lot will be locked to be prevented concurrent executing.");
		Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
		eventLog.debug("Lot is locked to be prevented concurrent executing.");
		
		// Added by smkang on 2019.05.21 - According to Jiang Haiying's request, after StartCSTInfoCheckRequest is succeeded, 
		//								   HoldLot/ForceSampling/StartBranch/StartRework/ChangeProductSpec/ChangeProcessFlow/ChangeProcessOperation
		//								   PrepareSort/CancelPrepareSort/ForceOutSampling/CancelBranch/CancelRework/CompleteRework should be rejected.
		if (StringUtils.equals(lotData.getUdfs().get("STARTCHECKRESULT"), "Y"))
			throw new CustomException("LOT-9050", lotName);
		
		// 2019.07.31 Park Jeong Su Mantis 4489
		// if Client Return Flow != GetReturn FlowName
		// Create Error
		if( StringUtils.equals(GenericServiceProxy.getConstantMap().Flag_Y, sSampleFlag) &&  !StringUtils.equals(sReturnFlowName, getReturnProcessFlowName(lotName))){
			throw new CustomException("LOT-9052");
		}
		
		// add by GJJ 20200220 mantis:5695 start
		if( !sNodeStack.isEmpty() &&  !StringUtils.equals(sNodeStack, lotData.getNodeStack())){
			setLanguage("Chinese");
			throw new CustomException("LOT-8002");
		}
		// add by GJJ 20200220 mantis:5695 end
		
		String logicalSlotMap = "";
		String departmentName = " ";
		String beforeReworkState = lotData.getReworkState();

		if(StringUtil.isNotEmpty(lotData.getCarrierName())){
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
			logicalSlotMap = MESProductServiceProxy.getProductServiceUtil().getSlotMapInfo(durableData);
		}
		
		
		// 2019.06.20_hsryu_Delete Logic. set ChangeSpecInfo note. 
//		Map<String, String> udfs = lotData.getUdfs();
//		udfs.put("NOTE", note);
//		lotData.setUdfs(udfs);

		// Check LotProcessState
		if(StringUtil.equals(lotData.getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Run))
		{
			throw new CustomException("LOT-0008", lotData.getKey().getLotName());
		}
		
		if(isExistGHLDHoldCode(lotName))
		{
			throw new CustomException("COMMON-0001","LotHoldCode is GHLD");
		}

		// Added by smkang on 2018.07.02 - According to EDO's request, if the first process operation is not executed yet, event name should be recorded as 'CancelForceSampling'.
		EventInfo eventInfo = null;
		if (lotData.getProcessOperationName().equals(CommonUtil.getFirstOperation(lotData.getFactoryName(), lotData.getProcessFlowName()).getKey().getProcessOperationName()))
			eventInfo = EventInfoUtil.makeEventInfo("CancelForceSampling", getEventUser(), "ForceOutSampling", null, null);
		else
			eventInfo = EventInfoUtil.makeEventInfo("ForceOutSampling", getEventUser(), "ForceOutSampling", null, null);

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();
		List<CorresSampleLot> corresSampleLotList = new ArrayList<CorresSampleLot>();

		SampleLot sampleLot = new SampleLot();
		CorresSampleLot corresSampleLot = new CorresSampleLot();
		LotAction lotAction = new LotAction();

		String tempNodeStack = lotData.getNodeStack();
		String[] arrNodeStack = StringUtil.split(tempNodeStack, ".");
		int count = arrNodeStack.length;

		Map<String, String> flowMap = MESLotServiceProxy.getLotServiceUtil().getProcessFlowInfo(arrNodeStack[count-1]);

		String flowName = flowMap.get("PROCESSFLOWNAME");
		String flowVersion = flowMap.get("PROCESSFLOWVERSION");
		String operationName = flowMap.get("PROCESSOPERATIONNAME");

		Map<String, String> beforeFlowMap = MESLotServiceProxy.getLotServiceUtil().getProcessFlowInfo(arrNodeStack[count-2]);

		String beforeFlowName = beforeFlowMap.get("PROCESSFLOWNAME");
		String beforeFlowVersion = beforeFlowMap.get("PROCESSFLOWVERSION");
		String beforeOperationName = beforeFlowMap.get("PROCESSOPERATIONNAME");

		//2019.02.27_hsryu_Move Logic location. For SampleOutHold eventUser. Mantis 0002723.
		EventInfo holdEventInfo = EventInfoUtil.makeEventInfo("Hold", eventInfo.getEventUser(), "Hold by SampleOutHoldFlag", "HoldLot", "HD-0001");
		holdEventInfo.setEventTime(eventInfo.getEventTime());

		if (count > 1)
		{
			try {
				corresSampleLotList = ExtendedObjectProxy.getCorresSampleLotService().select(" lotName = ? and factoryName = ? and productSpecName = ? and ecCode = ? and processFlowName = ? and processFlowVersion = ?"
						+ " and fromProcessOperationName = ? and (fromProcessOperationVersion = ? or fromProcessOperationVersion = ?) and sampleProcessFlowName = ? and (sampleProcessFlowVersion = ? or sampleProcessFlowVersion = ?) ",
						new Object[] {lotData.getKey().getLotName(), 
								lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"), beforeFlowName, beforeFlowVersion,
								beforeOperationName, "00001", "*", flowName, flowVersion, "*"});

				for(int i=0; i<corresSampleLotList.size(); i++){
					corresSampleLot = corresSampleLotList.get(0);

					//2019.02.27_hsryu_Insert Logic. For SampleOutHold eventUser. Mantis 0002723.
					holdEventInfo.setEventUser(corresSampleLot.getLastEventUser());

					corresSampleLot.setSampleState("Force Completed");
					corresSampleLot.setLastEventName(eventInfo.getEventName());
					corresSampleLot.setLastEventComment(eventInfo.getEventComment());
					corresSampleLot.setLastEventTime(eventInfo.getEventTime());
					corresSampleLot.setLastEventTimekey(eventInfo.getEventTimeKey());
					corresSampleLot = ExtendedObjectProxy.getCorresSampleLotService().modify(eventInfo, corresSampleLot);

					ExtendedObjectProxy.getCorresSampleLotService().delete(corresSampleLot);
				}
			} catch(Throwable e) {
				eventLog.info("not corresSample currently..");
			}

			try {
				sampleLotList = ExtendedObjectProxy.getSampleLotService().select(" lotName = ? and factoryName = ? and productSpecName = ? and ecCode = ? and processFlowName = ? "
						+ "and processFlowVersion = ? and fromProcessOperationName = ? and (fromProcessOperationVersion = ? or fromProcessOperationVersion = ?) and sampleProcessFlowName = ? and (sampleProcessFlowVersion = ? or sampleProcessFlowVersion= ?) ",
						new Object[] {lotData.getKey().getLotName(), 
								lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"), beforeFlowName, beforeFlowVersion,
								beforeOperationName, "00001", "*", flowName, flowVersion, "*"});

				for (int i=0; i<sampleLotList.size(); i++) {
					sampleLot = sampleLotList.get(i);

					//2019.02.27_hsryu_Insert Logic. For SampleOutHold eventUser. Mantis 0002723.
					holdEventInfo.setEventUser(sampleLot.getLastEventUser());

					sampleLot.setSampleState("Force Completed");
					sampleLot.setLastEventName(eventInfo.getEventName());
					//2019.02.27_hsryu_Mantis 0002723. remain EventUser 
					//sampleLot.setLastEventUser(eventInfo.getEventUser());
					sampleLot.setLastEventComment(eventInfo.getEventComment());
					sampleLot.setLastEventTime(eventInfo.getEventTime());
					sampleLot.setLastEventTimekey(eventInfo.getEventTimeKey());
					sampleLot = ExtendedObjectProxy.getSampleLotService().modify(eventInfo, sampleLot);

					ExtendedObjectProxy.getSampleLotService().delete(sampleLot);
				}
			} catch(Throwable e) {
				eventLog.info("not normalSample currently..");
			}

			//ReserveSample Completing..
			try
			{
				List<LotAction> sampleActionList = new ArrayList<LotAction>();

				String condition = " WHERE 1=1 AND lotName = ? AND factoryName = ? AND processFlowName = ? AND processFlowVersion = ? AND processOperationName = ?"
						+ " AND processOperationVersion = ? AND sampleProcessFlowName = ? AND sampleProcessFlowVersion = ? AND actionName = ? AND actionState = ? ";
				Object[] bindSet = new Object[]{ lotData.getKey().getLotName(), lotData.getFactoryName(), beforeFlowName, beforeFlowVersion,
						beforeOperationName, "00001", flowName, flowVersion, "Sampling", "Created" };

				sampleActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);

				if(sampleActionList.size() == 1)
				{
					lotAction = sampleActionList.get(0);

					//2019.02.27_hsryu_Insert Logic. For SampleOutHold eventUser. Mantis 0002723.
					holdEventInfo.setEventUser(lotAction.getLastEventUser());

					//2019.03.05_hsryu_Mantis 0002944.
					if(StringUtils.equals(lotAction.getSampleOutHoldFlag(), "Y")){
						departmentName = StringUtils.isNotEmpty(lotAction.getDepartment())?lotAction.getDepartment():" ";
					}

					lotAction.setActionState("Force Completed");
					lotAction.setLastEventTime(eventInfo.getEventTime());
					lotAction.setLastEventTimeKey(eventInfo.getEventTimeKey());
					lotAction.setLastEventUser(eventInfo.getEventUser());
					lotAction.setLastEventComment(eventInfo.getEventComment());
					ExtendedObjectProxy.getLotActionService().modify(eventInfo, lotAction);

					ExtendedObjectProxy.getLotActionService().delete(lotAction);
				}
			}
			catch(Throwable e)
			{
				eventLog.info("not ReserveSampling currently..");
			}
		}

		boolean chkReworkFlow = false;
		String nextNodeID = "";
		int nodeNum = 0;
		String nodeStack = "";

		for ( int i = count-2; i >=0; i-- ){
			Map<String, String> flowMap2 = MESLotServiceProxy.getLotServiceUtil().getProcessFlowInfo(arrNodeStack[i]);

			String flowName2 = flowMap2.get("PROCESSFLOWNAME");
			String flowVersion2 = flowMap2.get("PROCESSFLOWVERSION");
			String operationName2 = flowMap2.get("PROCESSOPERATIONNAME");

			if(!chkReworkFlow){
				boolean EndFlagForBeforeFlow = MESLotServiceProxy.getLotServiceUtil().checkEndOperation(flowName2, flowVersion2, arrNodeStack[i]);

				//if not end Operation
				if(!EndFlagForBeforeFlow){
					nextNodeID = MESLotServiceProxy.getLotServiceUtil().GetReturnAfterNodeStackForSampling(lotData.getFactoryName(), arrNodeStack[i]);
					nodeNum = i;
					break;
				}
			}
			else{
				nextNodeID = CommonUtil.getNodeStack(lotData.getFactoryName(),flowName2, operationName2);		
				nodeNum = i;
				break;
			}
		}

		if(nodeNum == 0){
			nodeStack = nextNodeID;
		}
		else{
			for(int i=0; i<=nodeNum-1; i++){
				nodeStack += arrNodeStack[i] + ".";
			}
			nodeStack += nextNodeID;
		}
		
		/****** 2019.03.06_hsryu_Insert Logic. if productGrade is 'P', update 'G'. requested by Hongwei ******/
		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		List<Product> pProductList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
		List<Product> pProductList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());

		for (Product product : pProductList)
		{
			if(StringUtils.equals(product.getProductGrade(), GenericServiceProxy.getConstantMap().ProductGrade_P))
			{
				product.setProductGrade(GenericServiceProxy.getConstantMap().ProductGrade_G);
				ProductServiceProxy.getProductService().update(product);
			}
		}
		/*************************************************************/
		
		ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().getCurrentChangSpectInfo(lotData);
		changeSpecInfo.setProcessFlowName("");
		changeSpecInfo.setProcessFlowVersion("");
		changeSpecInfo.setProcessOperationName("");
		changeSpecInfo.setProcessOperationVersion("");
		changeSpecInfo.setNodeStack(nodeStack);
		// 2019.06.20_hsryu_Add Logic. set ChangeSpecInfo note.
		changeSpecInfo.getUdfs().put("NOTE", note);

		eventInfo.setBehaviorName("ARRAY");

		lotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo);

		// Modified by smkang on 2019.05.24 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//		//2019.02.13_hsryu_Mantis 2723.
//		Map<String, String> udfs_lotNote = lotData.getUdfs();
//		udfs_lotNote.put("NOTE", "");
//		LotServiceProxy.getLotService().update(lotData);
		Map<String, String> updateUdfs = new HashMap<String, String>();
		updateUdfs.put("NOTE", "");
		MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);

		lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());

		if(StringUtil.isNotEmpty(sSampleOutHoldFlag)&&StringUtil.equals(sSampleOutHoldFlag, "Y"))
		{
			List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
			MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo(productUSequence);
			LotServiceProxy.getLotService().makeOnHold(lotData.getKey(), holdEventInfo, makeOnHoldInfo);

			try {
				lotData = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(lotData.getKey().getLotName(), "HD-0001", departmentName, "AHOLD", holdEventInfo);
			} catch (Exception e) {
				eventLog.warn(e);
			}
			// -------------------------------------------------------------------------------------------------------------------------------------------
		}

		lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);

		if(StringUtil.isNotEmpty(sSampleOutHoldFlag)&&StringUtil.equals(sSampleOutHoldFlag, "N"))
		{
			if(StringUtil.equals(sSampleFlag, "Y"))
			{
				//2019.02.04_hsryu_add Validation.	
				if(!StringUtil.equals(MESLotServiceProxy.getLotServiceUtil().returnFlowType(lotData.getFactoryName(), sReturnFlowName,"00001").toUpperCase(), "SAMPLING"))
				{
					throw new CustomException("SAMPLE-0008", sReturnFlowName); 
				}

				String originalEventUser = eventInfo.getEventUser();

				eventInfo.setEventName("Sampling");

				Map<String, String> lotUdfs = lotData.getUdfs();
				List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotName);

				//Operation Changed, Update Product ProcessingInfo to N
				productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productUdfs, "");

				boolean checkEventInfo = false;

				for ( int i = count-2; i >=0; i-- )
				{
					Map<String, String> flow = MESLotServiceProxy.getLotServiceUtil().getProcessFlowInfo(arrNodeStack[i]);

					String decideFlowName = flow.get("PROCESSFLOWNAME");
					String decideFlowVersion = flow.get("PROCESSFLOWVERSION");
					String decideOperationName = flow.get("PROCESSOPERATIONNAME");

					checkEventInfo = MESLotServiceProxy.getLotServiceUtil().setEventInfoForSampling(lotData,decideFlowName,decideFlowVersion,decideOperationName,sReturnFlowName,"00001",eventInfo);

					if(checkEventInfo)
					{
						break;
					}
				}

				if(checkEventInfo)
				{
					ChangeSpecInfo changeSpecInfo2;

					//2019.02.22_hsryu_ProductRequestName "". Mantis 0002757.
					changeSpecInfo2 = MESLotServiceProxy.getLotInfoUtil().changeSpecInfoForForceOutSampling(lotName,
							lotData.getProductionType(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProductSpec2Name(), lotData.getProductSpec2Version(),
							"", lotData.getSubProductUnitQuantity1(), lotData.getSubProductQuantity2(), lotData.getDueDate(), lotData.getPriority(),
							lotData.getFactoryName(), lotData.getAreaName(), lotData.getLotState(), lotData.getLotProcessState(), lotData.getLotHoldState(),
							lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
							sReturnFlowName, sReturnOperName, lotData.getProcessFlowName(), lotData.getProcessOperationName(), sBeforeNodeStack,
							lotData.getUdfs(), productUdfs,
							true, false);

					lotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo2);

					eventInfo.setEventUser(originalEventUser);

				}
				else
				{
					throw new CustomException("COMMON-0001","Reserved Sample [" + sReturnFlowName + "] is not exist. Please Check."); 
				}
			}

		}

		/* Array 20180829, Add [Process Flag Update] ==>> */            
		if(StringUtil.isNotEmpty(logicalSlotMap))
		{
			MESProductServiceProxy.getProductServiceUtil().setProdutProcessFlag(eventInfo, lotData, logicalSlotMap, false);
		}
		/* <<== Array 20180829, Add [Process Flag Update] */

		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));

		if(!MESLotServiceProxy.getLotServiceUtil().checkReworkState(lotData))
		{
			if(StringUtils.equals(beforeReworkState, GenericServiceProxy.getConstantMap().Lot_InRework))
				lotData = MESLotServiceProxy.getLotServiceUtil().endReworkState(eventInfo, lotData);
		}

		if(StringUtil.isNotEmpty(sSampleOutHoldFlag) && StringUtil.equals(sSampleOutHoldFlag, "N"))
		{
			if(!MESLotServiceProxy.getLotServiceUtil().isExistBholdorOperHold(lotName, eventInfo))
			{
				//Reserve Change
				lotData = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionChange(lotData.getKey().getLotName(), eventInfo);
			}
		}

		MESLotServiceProxy.getLotServiceUtil().deletePermanentHoldInfo(lotData.getKey().getLotName(), lotData.getFactoryName(), flowName, operationName, eventInfo);

		// Modified by smkang on 2019.05.24 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//		//Note clear - YJYU
//		Lot lotData_Note = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//		Map<String, String> udfs_note = lotData_Note.getUdfs();
//		udfs_note.put("NOTE", "");
//		LotServiceProxy.getLotService().update(lotData_Note);
		Map<String, String> updateUdfs2 = new HashMap<String, String>();
		updateUdfs2.put("NOTE", "");
		MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs2);

		return doc;
	}
	private String getQuery(){
		
		StringBuilder selectProcessFlowQuery = new StringBuilder();
		
		selectProcessFlowQuery.append("	SELECT SAMPLEPROCESSFLOWNAME, POSITION, TYPE  	");
		selectProcessFlowQuery.append("	                  FROM (SELECT LA.SAMPLEPROCESSFLOWNAME, LA.POSITION, 'RESERVE' TYPE  	");
		selectProcessFlowQuery.append("	                          FROM CT_LOTACTION LA , LOT L	");
		selectProcessFlowQuery.append("	                         WHERE 1 = 1	");
		selectProcessFlowQuery.append("	                           AND LA.LOTNAME = L.LOTNAME	");
		selectProcessFlowQuery.append("	                           AND NOT LA.SAMPLEPROCESSFLOWNAME = L.PROCESSFLOWNAME  	");
		selectProcessFlowQuery.append("	                           AND LA.LOTNAME = :LOTNAME  	");
		selectProcessFlowQuery.append("	                           AND LA.PROCESSFLOWNAME = :PROCESSFLOWNAME  	");
		selectProcessFlowQuery.append("	                           AND LA.PROCESSFLOWVERSION = :PROCESSFLOWVERSION  	");
		selectProcessFlowQuery.append("	                           AND LA.PROCESSOPERATIONNAME = :FROMPROCESSOPERATIONNAME  	");
		selectProcessFlowQuery.append("	                           AND LA.PROCESSOPERATIONVERSION = :FROMPROCESSOPERATIONVERSION  	");
		selectProcessFlowQuery.append("	                           AND LA.ACTIONSTATE = :ACTIONSTATE  	");
		selectProcessFlowQuery.append("	                           AND LA.ACTIONNAME = :ACTIONNAME  	");
		selectProcessFlowQuery.append("	                        UNION  	");
		selectProcessFlowQuery.append("	                        SELECT SL.SAMPLEPROCESSFLOWNAME, SLC.SAMPLEPRIORITY, 'AUTO' TYPE  	");
		selectProcessFlowQuery.append("	                          FROM CT_SAMPLELOTCOUNT SLC, CT_SAMPLELOT SL , LOT L 	");
		selectProcessFlowQuery.append("	                         WHERE 1 = 1  	");
		selectProcessFlowQuery.append("	                           AND SLC.FACTORYNAME = SL.FACTORYNAME  	");
		selectProcessFlowQuery.append("	                           AND SLC.PRODUCTSPECNAME = SL.PRODUCTSPECNAME  	");
		selectProcessFlowQuery.append("	                           AND SLC.ECCODE = SL.ECCODE  	");
		selectProcessFlowQuery.append("	                           AND SLC.PROCESSFLOWNAME = SL.PROCESSFLOWNAME  	");
		selectProcessFlowQuery.append("	                           AND SLC.PROCESSFLOWVERSION = SL.PROCESSFLOWVERSION  	");
		selectProcessFlowQuery.append("	                           AND SLC.SAMPLEPROCESSFLOWNAME = SL.SAMPLEPROCESSFLOWNAME  	");
		selectProcessFlowQuery.append("	                           AND SLC.SAMPLEPROCESSFLOWVERSION = SL.SAMPLEPROCESSFLOWVERSION  	");
		selectProcessFlowQuery.append("	                           AND SLC.FROMPROCESSOPERATIONNAME = SL.FROMPROCESSOPERATIONNAME  	");
		selectProcessFlowQuery.append("	                           AND SL.LOTNAME = L.LOTNAME	");
		selectProcessFlowQuery.append("	                           AND NOT SL.SAMPLEPROCESSFLOWNAME = L.PROCESSFLOWNAME	");
		selectProcessFlowQuery.append("	                           AND SL.LOTNAME = :LOTNAME  	");
		selectProcessFlowQuery.append("	                           AND SL.FROMPROCESSOPERATIONNAME = :FROMPROCESSOPERATIONNAME  	");
		selectProcessFlowQuery.append("	                           AND SL.FROMPROCESSOPERATIONVERSION = :FROMPROCESSOPERATIONVERSION  	");
		selectProcessFlowQuery.append("	                           AND SL.SAMPLEFLAG = :SAMPLEFLAG  	");
		selectProcessFlowQuery.append("	                           AND SL.SAMPLESTATE = :SAMPLESTATE	");
		selectProcessFlowQuery.append("	                           AND SLC.ECCODE = :ECCODE	");
		selectProcessFlowQuery.append("	                           AND SLC.PRODUCTSPECNAME=:PRODUCTSPECNAME	");
		selectProcessFlowQuery.append("	                        UNION  	");
		selectProcessFlowQuery.append("	                        SELECT CSL.SAMPLEPROCESSFLOWNAME, SLC.CORRESSAMPLEPRIORITY POSITION, 'CORRES' TYPE  	");
		selectProcessFlowQuery.append("	                          FROM CT_SAMPLELOTCOUNT SLC, CT_CORRESSAMPLELOT CSL, LOT L  	");
		selectProcessFlowQuery.append("	                         WHERE 1 = 1  	");
		selectProcessFlowQuery.append("	                           AND SLC.FACTORYNAME = CSL.FACTORYNAME  	");
		selectProcessFlowQuery.append("	                           AND SLC.PRODUCTSPECNAME = CSL.PRODUCTSPECNAME  	");
		selectProcessFlowQuery.append("	                           AND SLC.ECCODE = CSL.ECCODE  	");
		selectProcessFlowQuery.append("	                           AND SLC.PROCESSFLOWNAME = CSL.PROCESSFLOWNAME  	");
		selectProcessFlowQuery.append("	                           AND SLC.PROCESSFLOWVERSION = CSL.PROCESSFLOWVERSION  	");
		selectProcessFlowQuery.append("	                           AND SLC.CORRESSAMPLEPROCESSFLOWNAME = CSL.SAMPLEPROCESSFLOWNAME  	");
		selectProcessFlowQuery.append("	                           AND SLC.CORRESSAMPLEPROCESSFLOWVERSION = CSL.SAMPLEPROCESSFLOWVERSION  	");
		selectProcessFlowQuery.append("	                           AND SLC.CORRESPROCESSOPERATIONNAME = CSL.FROMPROCESSOPERATIONNAME  	");
		selectProcessFlowQuery.append("	                           AND SLC.CORRESPROCESSOPERATIONVERSION = CSL.FROMPROCESSOPERATIONVERSION	");
		selectProcessFlowQuery.append("	                           AND CSL.LOTNAME = L.LOTNAME	");
		selectProcessFlowQuery.append("	                           AND NOT CSL.SAMPLEPROCESSFLOWNAME = L.PROCESSFLOWNAME	");
		selectProcessFlowQuery.append("	                           AND CSL.LOTNAME = :LOTNAME  	");
		selectProcessFlowQuery.append("	                           AND CSL.FROMPROCESSOPERATIONNAME = :FROMPROCESSOPERATIONNAME  	");
		selectProcessFlowQuery.append("	                           AND CSL.FROMPROCESSOPERATIONVERSION = :FROMPROCESSOPERATIONVERSION  	");
		selectProcessFlowQuery.append("	                           AND CSL.SAMPLEFLAG = :SAMPLEFLAG  	");
		selectProcessFlowQuery.append("	                           AND CSL.SAMPLESTATE = :SAMPLESTATE	");
		selectProcessFlowQuery.append("	                           AND SLC.ECCODE = :ECCODE	");
		selectProcessFlowQuery.append("	                           AND SLC.PRODUCTSPECNAME=:PRODUCTSPECNAME	");
		selectProcessFlowQuery.append("	                           )  	");
		selectProcessFlowQuery.append("	                   ORDER BY DECODE(TYPE, 'RESERVE', 0, 'AUTO', 1,2), POSITION ASC	");
		
		return selectProcessFlowQuery.toString();
	}
	
	private String getReturnProcessFlowName(String lotName){
		try {
			Lot lotData = LotServiceProxy.getLotService().selectByKey(new LotKey(lotName));
			
			String nodeStack = lotData.getNodeStack();
			String[] arrNodeStack = StringUtils.split(nodeStack, ".");
			String query = this.getQuery();
			
			for(int i=arrNodeStack.length-2;i>=0;i--){
				Map<String, String> returnFlowInfo = CommonUtil.getNodeInfo(arrNodeStack[i]);
				
				String returnFlowName = returnFlowInfo.get("PROCESSFLOWNAME").toString();
				String returnFlowVersion = returnFlowInfo.get("PROCESSFLOWVERSION").toString();
				String returnOperationName = returnFlowInfo.get("PROCESSOPERATIONNAME").toString();
				String returnOperationVersion = returnFlowInfo.get("PROCESSOPERATIONVERSION").toString();
				
		        Map<String, Object> bindMap = new HashMap<String, Object>();
	            bindMap.put("LOTNAME", lotName);
	            bindMap.put("FACTORYNAME", lotData.getFactoryName());
	            bindMap.put("PROCESSFLOWNAME", returnFlowName);
	            bindMap.put("PROCESSFLOWVERSION", returnFlowVersion);
	            bindMap.put("PRODUCTSPECNAME", lotData.getProductSpecName());
	            bindMap.put("ECCODE", lotData.getUdfs().get("ECCODE"));
	            bindMap.put("FROMPROCESSOPERATIONNAME", returnOperationName);
	            bindMap.put("FROMPROCESSOPERATIONVERSION", returnOperationVersion);
	            bindMap.put("ACTIONSTATE", "Created");
	            bindMap.put("ACTIONNAME", "Sampling");
	            bindMap.put("SAMPLEFLAG", "Y");
	            bindMap.put("SAMPLESTATE", "Decided");
				
				List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(query.toString(), bindMap);

				if(sqlResult != null && sqlResult.size() > 0){
					return (String)sqlResult.get(0).get("SAMPLEPROCESSFLOWNAME");
				}
				
			}
			
		} catch (Exception e) {
			return "";
		}
		return "";
		
	}
	

	private boolean isExistGHLDHoldCode(String lotName)
	{
		try {
			List<LotMultiHold> lotMultiHoldList = ExtendedObjectProxy.getLotMultiHoldService().select(" WHERE LOTNAME = ? AND REASONCODE = ?", new Object[]{ lotName, "GHLD" });
			return (lotMultiHoldList != null && lotMultiHoldList.size() > 0);
		} catch (Exception e) {
			return false;
		}
	}
}