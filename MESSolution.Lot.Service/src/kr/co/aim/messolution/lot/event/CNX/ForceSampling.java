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
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ForceSampling extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
 		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String mainFlowName = SMessageUtil.getBodyItemValue(doc, "MAINFLOWNAME", true);
		String mainOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String sampleFlowName = SMessageUtil.getBodyItemValue(doc, "SAMPLEFLOWNAME", true);
		String sampleOutHoldFlag = SMessageUtil.getBodyItemValue(doc, "SAMPLEOUTHOLDFLAG", true);
		String note = SMessageUtil.getBodyItemValue(doc, "NOTE", true);
		List<Element> productElement = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);
		String xNodeStack = SMessageUtil.getBodyItemValue(doc, "NODESTACK", false);// 20200323 add by GJJ mantis :5852 卡控nodestack与Server一致

		
		// Modified by smkang on 2019.06.17 - According to Liu Hongwei's request, Lot should be locked to be prevented concurrent executing.
//		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		eventLog.debug("Lot will be locked to be prevented concurrent executing.");
		Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
		eventLog.debug("Lot is locked to be prevented concurrent executing.");

        //modify by JHIYING on2019.07.12 0004289 start
		String sampleDepartmentName = SMessageUtil.getBodyItemValue(doc, "SAMPLEDEPARTMENTNAME", true);
		String holdDepartmentName = SMessageUtil.getBodyItemValue(doc, "HOLDDEPARTMENT", false);
		String reasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", false);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", true);
		//modify by JHIYING on2019.07.12 0004289 end
		
		String samplePosition = "";
		String logicalSlotMap = "";
		ArrayList<String> sampleProductSlot = new ArrayList<String>();
		String beforeFlow = "";
		String beforeOper = "";
		String beforeNodeId = "";
		boolean repairFlag = false;
		
//		//2019.09.02 Add By Park Jeong Su Mantis 4691
//		CommonValidation.checkFirstCheckResultIsY(lotData.getKey().getLotName());
		
        // Added by smkang on 2019.05.21 - According to Jiang Haiying's request, after StartCSTInfoCheckRequest is succeeded, 
		//								   HoldLot/ForceSampling/StartBranch/StartRework/ChangeProductSpec/ChangeProcessFlow/ChangeProcessOperation
		//								   PrepareSort/CancelPrepareSort/ForceOutSampling/CancelBranch/CancelRework/CompleteRework should be rejected.
		if (StringUtils.equals(lotData.getUdfs().get("STARTCHECKRESULT"), "Y"))
			throw new CustomException("LOT-9050", lotName);
		
		// 20200323 add by GJJ mantis :5852 卡控nodestack与Server一致
		if( !xNodeStack.isEmpty() &&  !StringUtils.equals(xNodeStack, lotData.getNodeStack())){
			setLanguage("Chinese");
			throw new CustomException("LOT-8002");
		}
		// 20200323 add by GJJ mantis :5852 卡控nodestack与Server一致
		
		
		//MODIFY BY JHIYING START ON20190907 MANTIS:4691
		  String durableName = lotData.getCarrierName();
		  Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		if(StringUtils.equals(carrierData.getUdfs().get("TRANSPORTSTATE"), "ONEQP"))
			throw new  CustomException("LOT-9050", lotName);
		//MODIFY BY JHIYING END ON20190907 MANTIS:4691
		
		// 2019.06.05_hsryu_Add Validation. if already Lot in Same SampleFlow, Can't Proceed. 
		if(StringUtils.equals(lotData.getProcessFlowName(), sampleFlowName )){
			throw new CustomException("PROCESSOPERATION-0006", lotData.getKey().getLotName(), sampleFlowName);
		}
		
		// 2019.06.03_hsryu_Add Validation. if FirstOperation, Can't Proceed.
		if(CommonValidation.checkFirstOperation(lotData)){
			throw new CustomException("PROCESSOPERATION-0005", lotData.getProcessOperationName(), "ForceSampling");
		}
		
		if(isExistGHLDHoldCode(lotData.getKey().getLotName())) {
			throw new CustomException("COMMON-0001","LotHoldCode is GHLD");
		}
		
		// 2019.04.08_hsryu_Mantis 0003412.
		if(this.checkFirstOperRepair(factoryName, sampleFlowName))	
			repairFlag = true;

		for ( int i = 0; i < productElement.size(); i++)
		{
			Element elementProduct = productElement.get(i);
			String position = SMessageUtil.getChildText(elementProduct, "POSITION", true);
			String productName = SMessageUtil.getChildText(elementProduct, "PRODUCTNAME", true);

			sampleProductSlot.add(position);
			
			if ( i == productElement.size() - 1 )
			{
				samplePosition = samplePosition + position;
			}
			else
			{
				samplePosition = samplePosition + position + ",";
			}
			
			/**** 2019.04.04_hsryu_when DetailProcessOperationType of First Operation is 'REP' in SampleFlow, Change ProductGrade. Mantis 0003412. *****/
			if(repairFlag)
			{
				// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				Product productData = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);
				Product productData = ProductServiceProxy.getProductService().selectByKeyForUpdate(new ProductKey(productName));

				productData.setProductGrade(GenericServiceProxy.getConstantMap().ProductGrade_P);
				ProductServiceProxy.getProductService().update(productData);
			}
		}
		
		List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotName);
		
		//Operation Changed, Update Product ProcessingInfo to null
		productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productUdfs, "");
		
		//String beforeTrackOutOperName = lotData.getUdfs().get("BEFOREOPERATIONNAME");//MODFIY  BY gjj 20200315 MANTIS:5811
		String beforeTrackOutOperName = getBeforeOperation(lotData);//ADD BY gjj 20200315 MANTIS:5811
		
		
		String beforeProcessFlowName = lotData.getProcessFlowName();
		String beforeProcessOperationName = lotData.getProcessOperationName();
		
		Map<String, String> returnBeforeInfo = MESLotServiceProxy.getLotServiceUtil().getBeforeOperName(lotData.getFactoryName(), beforeProcessFlowName, "00001", beforeProcessOperationName, "00001");
		beforeFlow = returnBeforeInfo.get("PROCESSFLOWNAME");
		beforeOper = returnBeforeInfo.get("PROCESSOPERATIONNAME");
		beforeNodeId = returnBeforeInfo.get("NODEID");
		
		// 2019.03.28_hsryu_First Oper, Can't ForceSample. 
		if(StringUtils.isEmpty(beforeOper)){
			throw new CustomException("PROCESSOPERATION-0004", beforeProcessOperationName);
		}
		
		// 2019.06.05_hsryu_Add Validation. Recheck TPEFO2POlicy & TPEFOM Sampling.
		this.checkRegisterModeling(lotData, beforeFlow, beforeOper, sampleFlowName);
		
		String[] nodeStackArray = StringUtil.split(lotData.getNodeStack(), ".");
		String tempNodeStack = "";
		
		if(StringUtils.isNotEmpty(beforeNodeId))
		{
			for(int i=0; i<nodeStackArray.length-1; i++)
			{
				tempNodeStack += nodeStackArray[i] + "."; 
			}
			
			tempNodeStack += beforeNodeId;
		}
		else
		{
			tempNodeStack = lotData.getNodeStack();
		}

		if(StringUtil.isNotEmpty(lotData.getCarrierName()))
		{
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
	        logicalSlotMap = MESProductServiceProxy.getProductServiceUtil().getSlotMapInfo(durableData);
		}

		
		//EventInfo eventInfo = EventInfoUtil.makeEventInfo("ForceSampling", getEventUser(), note, "", "");
		//MODIFY BY JHIYING ON2019.07.12 0004289 START
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ForceSampling", getEventUser(), note, "Sample", reasonCode);
		//MODIFY BY JHIYING ON2019.07.12 0004289 END
		eventInfo.setBehaviorName("ARRAY");
		
		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();

			
		String sampleNodeStack = "";
		String sampleProcessOperationName = "";
		
		if(StringUtil.equals(lotData.getLotProcessState(), "WAIT"))
		{
			changeSpecInfo.setAreaName(lotData.getAreaName());
			changeSpecInfo.setDueDate(lotData.getDueDate());
			changeSpecInfo.setFactoryName(lotData.getFactoryName());
			changeSpecInfo.setLotHoldState(lotData.getLotHoldState());
			changeSpecInfo.setLotProcessState(lotData.getLotProcessState());
			changeSpecInfo.setLotState(lotData.getLotState());
			changeSpecInfo.setPriority(lotData.getPriority());
			changeSpecInfo.setProcessFlowName(sampleFlowName);
			changeSpecInfo.setProcessFlowVersion("00001");
			changeSpecInfo.setProcessOperationName(sampleProcessOperationName);
			changeSpecInfo.setProcessOperationVersion("00001");
			changeSpecInfo.setProductionType(lotData.getProductionType());
			//2018.11.01 Modify, hsryu, lotData.getProductRequestName -> ""
			//changeSpecInfo.setProductRequestName(lotData.getProductRequestName());
			changeSpecInfo.setProductSpec2Name(lotData.getProductSpec2Name());
			changeSpecInfo.setProductSpec2Version(lotData.getProductSpec2Version());
			changeSpecInfo.setProductSpecName(lotData.getProductSpecName());
			changeSpecInfo.setProductSpecVersion(lotData.getProductSpecVersion());
			//changeSpecInfo.setProductUdfs(productUdfs);
			changeSpecInfo.setSubProductUnitQuantity1(lotData.getSubProductUnitQuantity1());
			changeSpecInfo.setSubProductUnitQuantity2(lotData.getSubProductUnitQuantity2());
			//changeSpecInfo.getUdfs().put("SAMPLEDEPARTMENTNAME", sampleDepartmentName);
			
			// Get SampleFlow 1st ProcessOperation's NodeStack
			sampleNodeStack = MESLotServiceProxy.getLotServiceUtil().getOperFirstNodeStack(sampleFlowName);
			//List<Map<String, Object>> mainNodeMap = MESLotServiceProxy.getLotServiceUtil().getNodeStackByLot(lotName);
	
			if ( sampleNodeStack.isEmpty() || sampleNodeStack.equals("") ) 
			{
				throw new CustomException("Node-0001", 
						lotData.getProductSpecName(), sampleFlowName, sampleProcessOperationName);
			} 
			else
			{
				if ( tempNodeStack != null )
				{
					tempNodeStack = tempNodeStack + "." + sampleNodeStack;
					changeSpecInfo.setNodeStack(tempNodeStack);
					//sampleNodeStack = mainNodeMap.get(0).get("NODESTACK") + "." + sampleNodeStack;
				}
				else
				{
					// Error
					throw new CustomException("Node-0001", 
							lotData.getProductSpecName(), mainFlowName, mainOperationName);
				}
			}
		}
		else
		{
			throw new CustomException("LOT-9003", lotName+"(LotProcessState:"+lotData.getLotProcessState()+")");
		}

		MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
		
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Lot afterLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
		Lot afterLotData = LotServiceProxy.getLotService().selectByKeyForUpdate(lotData.getKey());


		
		//2019.02.13_hsryu_Mantis 2723.
 		afterLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
 		
        /*** 2019.04.08_hsryu_Insert Logic. Mantis 0003412. if only Repair Sampling, memory Note. ***/

        List<Product> productList = null;
        try{
            productList = LotServiceProxy.getLotService().allUnScrappedProducts(afterLotData.getKey().getLotName());
        }
        catch (Exception ex){
        	eventLog.warn("ProductList is not exist. Error!");
        }
        
        
        // Update SampleLot data (CT_SMAPLELOT),
     	// 2019.07.25 ParkJeongSu hold Add
        boolean sampleLotExist = false;
        boolean corSampleLotExist = false;
        
        List<SampleLot> sampleLotList = null;
        List<CorresSampleLot> corresSampleLotList = null;
        
        try
		{
			
			//CT_SAMPLELOT Merge
			try
			{
				sampleLotList = ExtendedObjectProxy.getSampleLotService().select(" lotName = ? and factoryName = ? and productSpecName = ? and ecCode = ? and sampleProcessFlowName = ? and sampleProcessFlowVersion = ? and fromProcessOperationName = ? and fromProcessOperationVersion = ? "
						, new Object[] {lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"),
								sampleFlowName, "00001", beforeTrackOutOperName, "00001" });	
				
			}
			catch(greenFrameDBErrorSignal ne)
			{
				eventLog.info("Already Decide Sampling Data Non Exist (CT_SAMPLELOT)");
			}
			
			if(sampleLotList != null)
			{
				eventLog.info("Already Decide Sampling Data Exist, SUM Actual Sample Position ... (CT_SAMPLELOT)");
				
				sampleLotExist = true;
			}
		}
		catch(Exception ex)
		{
			eventLog.error("SUM Actual Sample Position Fail ! (CT_SAMPLELOT)");
		}
        
        try
  		{
  			//CT_CORRESSAMPLELOT
  			try
  			{
  				corresSampleLotList = ExtendedObjectProxy.getCorresSampleLotService().select(" lotName = ? and factoryName = ? and productSpecName = ? and ecCode = ? and sampleProcessFlowName = ? and sampleProcessFlowVersion = ? and fromProcessOperationName = ? and fromProcessOperationVersion = ? "
  						, new Object[] {lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"),
  								sampleFlowName, "00001", beforeTrackOutOperName, "00001" });		
  				
  			}
  			catch(greenFrameDBErrorSignal ne)
  			{
  				eventLog.info("Already Decide Sampling Data Non Exist (CT_CORRESSAMPLELOT)");
  			}
  			
  			if(corresSampleLotList != null)
  			{
  				eventLog.info("Already Decide Sampling Data Exist, SUM Actual Sample Position ... (CT_CORRESSAMPLELOT)");
  				
  				corSampleLotExist = true;
  			}
  		}
  		catch(Exception ex)
  		{
  			eventLog.error("SUM Actual Sample Position Fail ! (CT_CORRESSAMPLELOT)");
  		}
        
        
        if(sampleLotExist || corSampleLotExist )
        {
        	if(sampleLotList != null)
        	{
        		sampleLotList.get(0).setSampleDepartmentName(sampleDepartmentName);
        		sampleLotList.get(0).setReasonCodeType(reasonCodeType);
        		sampleLotList.get(0).setReasonCode(reasonCode);
				
        		ExtendedObjectProxy.getSampleLotService().modify(eventInfo, sampleLotList.get(0));
        	}
        	else if(corresSampleLotList != null)
        	{
        		//Cores Sample why no Record Sample Deparment?
        	}
        }
        else
        {
        	MESLotServiceProxy.getLotServiceUtil().setSampleLotDataByOPI("Y", eventInfo, lotData, "-", beforeFlow, beforeOper, sampleFlowName, "00001", samplePosition,sampleOutHoldFlag,sampleDepartmentName,reasonCodeType,reasonCode,holdDepartmentName);
        }
        
        if(productList.size() > 0){
            String actualSamplePosition = StringUtil.EMPTY;
            actualSamplePosition = ExtendedObjectProxy.getSampleLotService().getActualSamplePosition(eventInfo, afterLotData, logicalSlotMap, true);
    		MESProductServiceProxy.getProductServiceUtil().setNoteSampleInfo(productList, actualSamplePosition, afterLotData, eventInfo, null, null, null);
        }
		/*******************************************************************************************/
        
        
        //2019.08.16 dmlee : SUM Actual SamplePosition [Reserve Sampling] - [System Sampling]
		try
		{
			//CT_SAMPLELOT Merge
			if(sampleLotExist)
			{
				eventLog.info("Already Decide Sampling Data Exist, SUM Actual Sample Position ... (CT_SAMPLELOT)");
				
				EventInfo eventInfo2 = EventInfoUtil.makeEventInfo("MergeActualPosition", getEventUser(), note, "Sample", reasonCode);
				eventInfo.setEventName("SamplePositionMerge");
				
				//2019.09.09 dmlee : Mantis 4696 Start
				eventInfo.setEventComment(getEventComment());
				//2019.09.09 dmlee : Mantis 4696 End
				
				sumActualSamplePosition(eventInfo2, samplePosition, sampleLotList, sampleOutHoldFlag,sampleDepartmentName,reasonCodeType,reasonCode,holdDepartmentName, productList, afterLotData, eventInfo);	
			}
		}
		catch(Exception ex)
		{
			eventLog.error("SUM Actual Sample Position Fail ! (CT_SAMPLELOT)");
		}
        
        try
  		{
        	if(corSampleLotExist)
        	{
        		//CT_CORRESSAMPLELOT
        		eventLog.info("Already Decide Sampling Data Exist, SUM Actual Sample Position ... (CT_CORRESSAMPLELOT)");
  				
        		EventInfo eventInfo2 = EventInfoUtil.makeEventInfo("MergeActualPosition", getEventUser(), note, "Sample", reasonCode);
        		eventInfo.setEventName("SamplePositionMerge");
        		
        		//2019.09.09 dmlee : Mantis 4696 Start
				eventInfo.setEventComment(getEventComment());
				//2019.09.09 dmlee : Mantis 4696 End
        		
  				sumActualSamplePositionCorres(eventInfo2, samplePosition, corresSampleLotList, sampleOutHoldFlag,sampleDepartmentName,reasonCodeType,reasonCode,holdDepartmentName, productList, afterLotData, eventInfo);	
        	}
  		}
  		catch(Exception ex)
  		{
  			eventLog.error("SUM Actual Sample Position Fail ! (CT_CORRESSAMPLELOT)");
  		}
  		//2019.08.16 dmlee : SUM Actual SamplePosition End-----------------------------------
        
        // Start 2019.09.17 Move By Park Jeong Su 258Line -> 412 Line Mantis 4825
        /* Array 20180829, Add [Process Flag Update] ==>> */            
		if(StringUtil.isNotEmpty(logicalSlotMap))
		{
			//2019.04.08_hsryu_Change NoteFlag. true -> false. For Repair Sampling.
	        MESProductServiceProxy.getProductServiceUtil().setProdutProcessFlag(eventInfo, afterLotData, logicalSlotMap, false);
		}
        /* <<== Array 20180829, Add [Process Flag Update] */
		// End 2019.09.17 Move By Park Jeong Su 258Line -> 412 Line Mantis 4825
        
        
 		// 2019.04.08_hsryu_Delete Logic. 
// 		String originalNote = afterLotData.getUdfs().get("NOTE");
 		
// 		if(repairFlag){
// 		}
// 		else{
// 			if(StringUtils.isNotEmpty(note))
// 			{
// 				originalNote += " & Sampling Desc :" + note;
// 			}
// 		}
//		
//		//2019.02.27_hsryu_clear Note.
//		afterLotData.getUdfs().put("NOTE", "");
//		LotServiceProxy.getLotService().update(afterLotData);
//		
//		//2019.01.28_hsryu_eventInfo.getEventTimeKey -> lotData.getLastEventTimeKey.
//		String cond = "where lotname=?" + " and timekey= ? " ;
//		Object[] bind = new Object[]{afterLotData.getKey().getLotName(),afterLotData.getLastEventTimeKey()};
//		List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(cond, bind);
//		LotHistory lotHistory = arrayList.get(0);
//		lotHistory.getUdfs().put("NOTE", originalNote);
//		LotServiceProxy.getLotHistoryService().update(lotHistory);
				
		if(!MESLotServiceProxy.getLotServiceUtil().isExistBholdorOperHold(lotName, eventInfo))
		{
			//Reserve Change
			afterLotData = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionChange(afterLotData.getKey().getLotName(), eventInfo);
		}

		MESLotServiceProxy.getLotServiceUtil().deletePermanentHoldInfo(afterLotData.getKey().getLotName(), afterLotData.getFactoryName(), beforeProcessFlowName,beforeProcessOperationName, eventInfo);
		
		// Modified by smkang on 2019.05.24 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//		//Note clear - YJYU
//		Lot lotData_Note = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//		Map<String, String> udfs_note = lotData_Note.getUdfs();
//		udfs_note.put("NOTE", "");
//		LotServiceProxy.getLotService().update(lotData_Note);
		Map<String, String> updateUdfs = new HashMap<String, String>();
		updateUdfs.put("NOTE", "");
		MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
		
		return doc;
	}
	
	private boolean checkFirstOperRepair(String factoryName, String toProcessFlowName) throws CustomException
	{
		try {
			ProcessOperationSpec operationData = CommonUtil.getFirstOperation(factoryName, toProcessFlowName);
			if(StringUtils.equals(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP)){
				return true;
			}
			
			return false;
		}
		catch(Throwable e){
			eventLog.warn("CheckFirstOperRepair Error!!! ");
			return false;
		}
	}
	
	private boolean isExistGHLDHoldCode(String lotName)
	{
		String condition = " WHERE LOTNAME = ? AND REASONCODE = ? ";
		Object[] bindSet = new Object[]{ lotName, "GHLD" };
		try {
			List<LotMultiHold> lotMultiHoldList = ExtendedObjectProxy.getLotMultiHoldService().select(condition, bindSet);
			if(lotMultiHoldList!=null && lotMultiHoldList.size()>0){
				return true;
			}else{
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		
	}

	// 2019.06.05_hsryu_Check Resiger Modeleing. Requsted by CIM. 
	private void checkRegisterModeling(Lot lotData, String beforeProcessFlowName, String beforeProcessOperation, String sampleProcessFlowName ) throws CustomException {
		StringBuffer queryBuffer = new StringBuffer()
		.append(" SELECT DISTINCT A.SAMPLEPROCESSFLOWNAME,  \n")
		.append("  A.SAMPLEPROCESSFLOWVERSION, \n")
		.append(" DECODE(A.DEFAULTFLAG,'AUTO','AUTO','Y','Y','N') DEFAULTFLAG \n")
		.append("       FROM ( \n")
		.append("       SELECT PS.SAMPLEPROCESSFLOWNAME, \n")
		.append("       PS.SAMPLEPROCESSFLOWVERSION, \n")
		.append("       PS.DEFAULTFLAG, TPEFO.PROCESSFLOWNAME \n")
		.append("       FROM TPEFOPOLICY TPEFO, POSPOSSIBLESAMPLE PS  \n")
		.append("       WHERE TPEFO.CONDITIONID = PS.CONDITIONID \n")               
		.append("       AND ((TPEFO.FACTORYNAME = :FACTORYNAME) OR (TPEFO.FACTORYNAME = '*')) \n")
		.append("       AND ((TPEFO.PRODUCTSPECNAME = :PRODUCTSPECNAME) OR (TPEFO.PRODUCTSPECNAME = '*')) \n")
		.append("       AND ((TPEFO.PRODUCTSPECVERSION = :PRODUCTSPECVERSION) OR (TPEFO.PRODUCTSPECVERSION = '*')) \n")
		.append("       AND ((TPEFO.ECCODE = :ECCODE) OR (TPEFO.ECCODE = '*')) \n")
		.append("       AND ((TPEFO.PROCESSFLOWNAME = :PROCESSFLOWNAME) OR (TPEFO.PROCESSFLOWNAME = '*')) \n")
		.append("       AND ((TPEFO.PROCESSFLOWVERSION = :PROCESSFLOWVERSION) OR (TPEFO.PROCESSFLOWVERSION = '*'))  \n")
		.append("       AND (TPEFO.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME OR (TPEFO.PROCESSOPERATIONNAME='*')) \n")
		.append("       AND PS.SAMPLEPROCESSFLOWNAME = :SAMPLEPROCESSFLOWNAME \n")
		.append("       AND NVL(PS.VALIDFLAG,'N') = 'Y' \n")
		.append("       UNION \n")         
		.append("       SELECT SAMPLEPROCESSFLOWNAME, \n")         
		.append("       SAMPLEPROCESSFLOWVERSION, \n")        
		.append("       'AUTO' DEFAULTFLAG, TPEFOM.PROCESSFLOWNAME \n")        
		.append("       FROM TPEFOMPOLICY TPEFOM, POSSAMPLE PS \n")         
		.append("       WHERE TPEFOM.CONDITIONID = PS.CONDITIONID \n")         
		.append("       AND ((TPEFOM.FACTORYNAME = :FACTORYNAME) OR (TPEFOM.FACTORYNAME = '*')) \n")         
		.append("       AND ((TPEFOM.PRODUCTSPECNAME = :PRODUCTSPECNAME) OR (TPEFOM.PRODUCTSPECNAME = '*')) \n")         
		.append("       AND ((TPEFOM.PRODUCTSPECVERSION = :PRODUCTSPECVERSION) OR (TPEFOM.PRODUCTSPECVERSION = '*')) \n")         
		.append("       AND ((TPEFOM.ECCODE = :ECCODE) OR (TPEFOM.ECCODE = '*')) \n")      
		.append("       AND ((TPEFOM.PROCESSFLOWNAME = :PROCESSFLOWNAME) OR (TPEFOM.PROCESSFLOWNAME = '*')) \n")         
		.append("       AND ((TPEFOM.PROCESSFLOWVERSION = :PROCESSFLOWVERSION) OR (TPEFOM.PROCESSFLOWVERSION = '*')) \n")         
		.append("       AND (TPEFOM.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME) \n")
		.append("       AND ((TPEFOM.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION) OR (TPEFOM.PROCESSOPERATIONVERSION = '*')) \n")
		.append("       AND PS.SAMPLEPROCESSFLOWNAME = :SAMPLEPROCESSFLOWNAME \n")
		.append("       ) A  \n");         

		HashMap<String, Object> bindMap = new HashMap<String, Object>();

		bindMap.put("FACTORYNAME", lotData.getFactoryName());
		bindMap.put("PROCESSFLOWNAME", beforeProcessFlowName);
		bindMap.put("PROCESSFLOWVERSION", lotData.getProcessFlowVersion());
		bindMap.put("PROCESSOPERATIONNAME", beforeProcessOperation);
		bindMap.put("PROCESSOPERATIONVERSION", lotData.getProcessOperationVersion());
		bindMap.put("PRODUCTSPECNAME", lotData.getProductSpecName());
		bindMap.put("PRODUCTSPECVERSION", lotData.getProductSpecVersion());
		bindMap.put("ECCODE", lotData.getUdfs().get("ECCODE"));
		bindMap.put("SAMPLEPROCESSFLOWNAME", sampleProcessFlowName);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);

		if(sqlResult == null || sqlResult.size() == 0){
			throw new CustomException("MODELER-0001", lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"), lotData.getProcessFlowName(), lotData.getProcessOperationName(), sampleProcessFlowName);
		}
	}
	
	
	private void sumActualSamplePosition(EventInfo eventInfo,
			String samplePosition, List<SampleLot> sampleLotList, String sampleOutHoldFlag,String sampleDepartmentName,String ReasonCodeType,String ReasonCode , String holdDepartmentName, List<Product> productList, Lot afterLotData, EventInfo eventInfoOld)
			throws CustomException {
		for(SampleLot sampleLotData : sampleLotList)
		{		
			String oldMSamplePosition = sampleLotData.getManualSamplePosition();
			
			String positionSUM = MESLotServiceProxy.getLotServiceUtil().sumPositionAndSort(sampleLotData.getActualSamplePosition(), samplePosition);
			
			ArrayList<String> manualPositionArr = new ArrayList<String>();
			ArrayList<String> actualPositionArr = new ArrayList<String>();
			ArrayList<String> resultPositionArr = new ArrayList<String>();
			
			if(samplePosition.contains(","))
			{
				String[] fromPositionArr = samplePosition.split(",");

				for(int i=0;i<fromPositionArr.length;i++)
				{
					manualPositionArr.add(fromPositionArr[i]);
				}
			}
			else
			{
				manualPositionArr.add(samplePosition);
			}
			
			
			if(sampleLotData.getActualSamplePosition().contains(","))
			{
				String[] toPositionArr = sampleLotData.getActualSamplePosition().split(",");

				for(int i=0; i<toPositionArr.length;i++)
				{
					actualPositionArr.add(toPositionArr[i]);
				}
			}
			else
			{
				actualPositionArr.add(sampleLotData.getActualSamplePosition());
			}
			
			for(String maPosition : manualPositionArr)
			{
				resultPositionArr.add(maPosition);

				for(String acPosition : actualPositionArr)
				{
					if(StringUtils.equals(acPosition, maPosition))
					{
						resultPositionArr.remove(maPosition);
						break;
					}
				}
			}
			
			String strManualPosition = MESLotServiceProxy.getLotServiceUtil().ConvertArrayPositionToString(resultPositionArr);

			sampleLotData.setActualSamplePosition(positionSUM);
			sampleLotData.setManualSamplePosition(strManualPosition);
			//sampleLotData.setSampleOutHoldFlag(sampleOutHoldFlag); modfiy by gjj 20200320
			if (StringUtils.isEmpty(sampleLotData.getSampleOutHoldFlag())) {
				sampleLotData.setSampleOutHoldFlag(sampleOutHoldFlag);
			}
			else {
				sampleLotData.setSampleOutHoldFlag(sampleLotData.getSampleOutHoldFlag().equals(sampleOutHoldFlag)?sampleOutHoldFlag:"Y");//add by GJJ 20200320 mantis:5851
			}
			
			sampleLotData.setSampleDepartmentName(sampleDepartmentName);
			sampleLotData.setReasonCodeType(ReasonCodeType);
			sampleLotData.setReasonCode(ReasonCode);
			//sampleLotData.setHoldDepartmentName(holdDepartmentName);
			sampleLotData.setHoldDepartmentName(StringUtils.isNotEmpty(holdDepartmentName)?holdDepartmentName:"");//add by GJJ 20200320 mantis:5851

			sampleLotData.setLastEventComment(eventInfo.getEventComment());
			sampleLotData.setLastEventName(eventInfo.getEventName());
			sampleLotData.setLastEventTime(eventInfo.getEventTime());
			sampleLotData.setLastEventTimekey(eventInfo.getEventTimeKey());
			sampleLotData.setLastEventUser(eventInfo.getEventUser());
			
			ExtendedObjectProxy.getSampleLotService().modify(eventInfo, sampleLotData);
			
			MESProductServiceProxy.getProductServiceUtil().setNoteSampleInfo(productList, positionSUM, afterLotData, eventInfoOld, sampleLotData, oldMSamplePosition, null);
		}
	}
	
	
	private void sumActualSamplePositionCorres(EventInfo eventInfo,
			String samplePosition, List<CorresSampleLot> corresSampleLotList, String sampleOutHoldFlag,String sampleDepartmentName,String ReasonCodeType,String ReasonCode , String holdDepartmentName,List<Product> productList, Lot afterLotData, EventInfo eventInfoOld)
			throws CustomException {
		for(CorresSampleLot corresSampleLotData : corresSampleLotList)
		{		
			String oldMSamplePosition = corresSampleLotData.getManualSamplePosition();
			
			String positionSUM = MESLotServiceProxy.getLotServiceUtil().sumPositionAndSort(corresSampleLotData.getActualSamplePosition(), samplePosition);
			
			ArrayList<String> manualPositionArr = new ArrayList<String>();
			ArrayList<String> actualPositionArr = new ArrayList<String>();
			ArrayList<String> resultPositionArr = new ArrayList<String>();
			
			if(samplePosition.contains(","))
			{
				String[] fromPositionArr = samplePosition.split(",");

				for(int i=0;i<fromPositionArr.length;i++)
				{
					manualPositionArr.add(fromPositionArr[i]);
				}
			}
			else
			{
				manualPositionArr.add(samplePosition);
			}
			
			
			if(corresSampleLotData.getActualSamplePosition().contains(","))
			{
				String[] toPositionArr = corresSampleLotData.getActualSamplePosition().split(",");

				for(int i=0; i<toPositionArr.length;i++)
				{
					actualPositionArr.add(toPositionArr[i]);
				}
			}
			else
			{
				actualPositionArr.add(corresSampleLotData.getActualSamplePosition());
			}
			
			for(String maPosition : manualPositionArr)
			{
				resultPositionArr.add(maPosition);

				for(String acPosition : actualPositionArr)
				{
					if(StringUtils.equals(acPosition, maPosition))
					{
						resultPositionArr.remove(maPosition);
						break;
					}
				}
			}
			
			String strManualPosition = MESLotServiceProxy.getLotServiceUtil().ConvertArrayPositionToString(resultPositionArr);

			corresSampleLotData.setActualSamplePosition(positionSUM);
			corresSampleLotData.setManualSamplePosition(strManualPosition);
			//corresSampleLotData.setSampleOutHoldFlag(sampleOutHoldFlag);	modfiy by GJJ 20200320
			
			if (StringUtils.isEmpty(corresSampleLotData.getSampleOutHoldFlag())) {
				corresSampleLotData.setSampleOutHoldFlag(sampleOutHoldFlag);		
			}    
			else {
				corresSampleLotData.setSampleOutHoldFlag(corresSampleLotData.getSampleOutHoldFlag().equals(sampleOutHoldFlag)?sampleOutHoldFlag:"Y");//add by GJJ 20200320 mantis:5851
			}
									
			
			//corresSampleLotData.setSampleDepartmentName(sampleDepartmentName);
			corresSampleLotData.setReasonCodeType(ReasonCodeType);
			corresSampleLotData.setReasonCode(ReasonCode);
			//corresSampleLotData.setHoldDepartmentName(holdDepartmentName);
			
			corresSampleLotData.setLastEventComment(eventInfo.getEventComment());
			corresSampleLotData.setLastEventName(eventInfo.getEventName());
			corresSampleLotData.setLastEventTime(eventInfo.getEventTime());
			corresSampleLotData.setLastEventTimekey(eventInfo.getEventTimeKey());
			corresSampleLotData.setLastEventUser(eventInfo.getEventUser());
			
			ExtendedObjectProxy.getCorresSampleLotService().modify(eventInfo, corresSampleLotData);
			
			MESProductServiceProxy.getProductServiceUtil().setNoteSampleInfo(productList, positionSUM, afterLotData, eventInfoOld, null, oldMSamplePosition, corresSampleLotData);
		}
	}
	
	
	//ADD BY gjj 20200315 MANTIS:5811
	private String getBeforeOperation(Lot lotData ) throws CustomException {
		StringBuffer queryBuffer = new StringBuffer();
		queryBuffer.append("	SELECT N2.NODEATTRIBUTE1 BEFOREOPERATIONNAME"); 
		queryBuffer.append("	FROM NODE N,"); 
		queryBuffer.append("	  ARC a,"); 
		queryBuffer.append("	  NODE N2"); 
		queryBuffer.append("	WHERE a.TONODEID= N.NODEID"); 
		queryBuffer.append("	AND N.NODEID    ="); 
		queryBuffer.append("	  (SELECT REGEXP_SUBSTR(L.NODESTACK,'[^.]+',1,REGEXP_COUNT(L.NODESTACK, '[^.]+'))"); 
		queryBuffer.append("	  FROM LOT L"); 
		queryBuffer.append("	  WHERE LOTNAME=:LOTNAME"); 
		queryBuffer.append("	  )"); 
		queryBuffer.append("	AND N2.NODEID        =a.FROMNODEID"); 
		queryBuffer.append("	AND N.PROCESSFLOWNAME=N2.PROCESSFLOWNAME");     

		HashMap<String, Object> bindMap = new HashMap<String, Object>();

		bindMap.put("LOTNAME", lotData.getKey().getLotName());

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);

		if(sqlResult != null && sqlResult.size() > 0){
			
			if(!StringUtil.isEmpty((String)sqlResult.get(0).get("BEFOREOPERATIONNAME")))
			{
				return (String)sqlResult.get(0).get("BEFOREOPERATIONNAME");
			}
			else {
				throw new CustomException("PROCESSOPERATION-0005", lotData.getProcessOperationName(), "ForceSampling");
			}
		}
		throw new CustomException("PROCESSOPERATION-0005", lotData.getProcessOperationName(), "ForceSampling");

	}
	
	
}
