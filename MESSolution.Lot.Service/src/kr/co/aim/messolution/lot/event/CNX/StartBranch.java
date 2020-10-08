package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class StartBranch extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String branchFlowName = SMessageUtil.getBodyItemValue(doc, "BRANCHFLOWNAME", true);
		String branchFlowVersion = SMessageUtil.getBodyItemValue(doc, "BRANCHFLOWVERSION", true);
		String branchOperationName = SMessageUtil.getBodyItemValue(doc, "BRANCHOPERATIONNAME", true);
		String xNodeStack = SMessageUtil.getBodyItemValue(doc, "NODESTACK", false);// 20200323 add by GJJ mantis :5852 卡控nodestack与Server一致

		
		// Modified by smkang on 2019.06.17 - According to Liu Hongwei's request, Lot should be locked to be prevented concurrent executing.
//		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		eventLog.debug("Lot will be locked to be prevented concurrent executing.");
		Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
		eventLog.debug("Lot is locked to be prevented concurrent executing.");
		
		String beforeOper = "";
		String beforeNodeId = "";

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Branch", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		eventInfo.setBehaviorName("ARRAY");
		
		
//		//2019.09.02 Add By Park Jeong Su Mantis 4691
//		CommonValidation.checkFirstCheckResultIsY(lotData.getKey().getLotName());
		
		// 20200323 add by GJJ mantis :5852 卡控nodestack与Server一致
		if( !xNodeStack.isEmpty() &&  !StringUtils.equals(xNodeStack, lotData.getNodeStack())){
			setLanguage("Chinese");
			throw new CustomException("LOT-8002");
		}
		// 20200323 add by GJJ mantis :5852 卡控nodestack与Server一致
		
		
		// 2019.06.03_hsryu_Add Validation. if FirstOperation, Can't Proceed.
		if(CommonValidation.checkFirstOperation(lotData)){
			throw new CustomException("PROCESSOPERATION-0005", lotData.getProcessOperationName(), "Branch");
		}
		
		// 2019.06.05_hsryu_Add Validation. if already Lot in Same BranchFlow, Can't Proceed. 
		if(StringUtils.equals(lotData.getProcessFlowName(), branchFlowName )){
			throw new CustomException("PROCESSOPERATION-0006", lotData.getKey().getLotName(), branchFlowName);
		}
		
		// 2019.06.05_hsryu_Add Validation. Recheck AlterProcessOperation.
		this.checkRegisterModeling(lotData, branchFlowName);
		
        // Added by smkang on 2019.05.21 - According to Jiang Haiying's request, after StartCSTInfoCheckRequest is succeeded, 
		//								   HoldLot/ForceSampling/StartBranch/StartRework/ChangeProductSpec/ChangeProcessFlow/ChangeProcessOperation
		//								   PrepareSort/CancelPrepareSort/ForceOutSampling/CancelBranch/CancelRework/CompleteRework should be rejected.
		if (StringUtils.equals(lotData.getUdfs().get("STARTCHECKRESULT"), "Y"))
			throw new CustomException("LOT-9050", lotName);
		
			
		
		//MODIFY BY JHIYING START ON20190907 MANTIS:4691
		  String durableName = lotData.getCarrierName();
		  Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		if(StringUtils.equals(carrierData.getUdfs().get("TRANSPORTSTATE"), "ONEQP"))
			throw new  CustomException("LOT-9050", lotName);
		//MODIFY BY JHIYING END ON20190907 MANTIS:4691
		
		//validation 
		CommonValidation.checkAlreadyLotProcessStateTrackIn(lotData);
		
		// 2019.05.14_hsryu_Add Validation. if MQCValidationFlag is 'N', Not Proceed!
		CommonValidation.checkMQCValidationFlag(lotData);
		
		String beforeProcessFlowName = lotData.getProcessFlowName();
		String beforeProcessOperationName = lotData.getProcessOperationName();
		
		/***** Add Logic. 2019.05.13_hsryu_if Start Branch, Memory Before NodeStack. *****/
		Map<String, String> returnBeforeInfo = MESLotServiceProxy.getLotServiceUtil().getBeforeOperName(lotData.getFactoryName(), beforeProcessFlowName, "00001", beforeProcessOperationName, "00001");
		beforeOper = returnBeforeInfo.get("PROCESSOPERATIONNAME");
		beforeNodeId = returnBeforeInfo.get("NODEID");
		
		// 2019.03.28_hsryu_First Oper, Can't ForceSample. 
		if(StringUtils.isEmpty(beforeOper)){
			throw new CustomException("PROCESSOPERATION-0004", beforeProcessOperationName);
		}

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
		/********************************************************************************/
		
		List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotData.getKey().getLotName());
		productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productUdfs, "");

		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();
		
		String branchNodeStack = "";
		
		if(StringUtil.equals(lotData.getLotProcessState(), "WAIT"))
		{
			changeSpecInfo.setAreaName(lotData.getAreaName());
			changeSpecInfo.setDueDate(lotData.getDueDate());
			changeSpecInfo.setFactoryName(lotData.getFactoryName());
			changeSpecInfo.setLotHoldState(lotData.getLotHoldState());
			changeSpecInfo.setLotProcessState(lotData.getLotProcessState());
			changeSpecInfo.setLotState(lotData.getLotState());
			changeSpecInfo.setPriority(lotData.getPriority());
			changeSpecInfo.setProcessFlowName(branchFlowName);
			changeSpecInfo.setProcessFlowVersion(StringUtil.equals(branchFlowVersion, "*")?"00001":branchFlowVersion);
			changeSpecInfo.setProcessOperationName(branchOperationName);
			changeSpecInfo.setProcessOperationVersion("00001");
			changeSpecInfo.setProductionType(lotData.getProductionType());
			//2018.11.01 Modify, hsryu, lotData.getProductRequestName -> "", Because WorkOrder of Product must not be changed.
			//changeSpecInfo.setProductRequestName(lotData.getProductRequestName());
			changeSpecInfo.setProductSpec2Name(lotData.getProductSpec2Name());
			changeSpecInfo.setProductSpec2Version(lotData.getProductSpec2Version());
			changeSpecInfo.setProductSpecName(lotData.getProductSpecName());
			changeSpecInfo.setProductSpecVersion(lotData.getProductSpecVersion());
			//changeSpecInfo.setProductUdfs(productUdfs);
			changeSpecInfo.setSubProductUnitQuantity1(lotData.getSubProductUnitQuantity1());
			changeSpecInfo.setSubProductUnitQuantity2(lotData.getSubProductUnitQuantity2());
			
			// Get SampleFlow 1st ProcessOperation's NodeStack
			branchNodeStack = MESLotServiceProxy.getLotServiceUtil().getOperFirstNodeStack(branchFlowName);
			// 2019.05.13_hsryu_Delete Logic. set NodeStack is changed.
			//			List<Map<String, Object>> mainNodeMap = MESLotServiceProxy.getLotServiceUtil().getNodeStackByLot(lotName);
//	
//			if ( branchNodeStack.isEmpty() || branchNodeStack.equals("") ) 
//			{
//				throw new CustomException("Node-0001", 
//						lotData.getProductSpecName(), branchFlowName, branchOperationName);
//			} 
//			else
//			{
//				if ( mainNodeMap != null )
//				{
//					branchNodeStack = mainNodeMap.get(0).get("NODESTACK") + "." + branchNodeStack;
//					changeSpecInfo.setNodeStack(branchNodeStack);
//				}
//				else
//				{
//					// Error
//					throw new CustomException("Node-0001", 
//							lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName());
//				}
//			}
			
			if ( branchNodeStack.isEmpty() || branchNodeStack.equals("") ) 
			{
				throw new CustomException("Node-0001", 
						lotData.getProductSpecName(), branchFlowName, branchOperationName);
			} 
			else
			{
				if ( tempNodeStack != null )
				{
					tempNodeStack = tempNodeStack + "." + branchNodeStack;
					changeSpecInfo.setNodeStack(tempNodeStack);
					//sampleNodeStack = mainNodeMap.get(0).get("NODESTACK") + "." + sampleNodeStack;
				}
				else
				{
					// Error
					throw new CustomException("Node-0001", 
							lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName());
				}
			}
		}
		else
		{
			throw new CustomException("LOT-9003", lotName+"(LotProcessState:"+lotData.getLotProcessState()+")");
		}
		
		lotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
		
		Lot afterLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());

		if(!MESLotServiceProxy.getLotServiceUtil().isExistBholdorOperHold(lotName, eventInfo))
		{
			//Reserve Change
			afterLotData = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionChange(afterLotData.getKey().getLotName(), eventInfo);
		}

		MESLotServiceProxy.getLotServiceUtil().deletePermanentHoldInfo(afterLotData.getKey().getLotName(), afterLotData.getFactoryName(), beforeProcessFlowName,beforeProcessOperationName, eventInfo);

		return doc;
	}

	// 2019.06.05_hsryu_Check Resiger Modeleing. Requsted by CIM. 
	private void checkRegisterModeling(Lot lotData, String branchFlowName) throws CustomException {
		StringBuffer queryBuffer = new StringBuffer()
		.append(" SELECT A.TOPROCESSFLOWNAME, NVL(A.TOPROCESSFLOWVERSION,'00001') AS TOPROCESSFLOWVERSION, A.DESCRIPTION \n")
		.append("  FROM ( \n")
		.append(" SELECT PA.TOPROCESSFLOWNAME, PA.TOPROCESSFLOWVERSION, \n")
		.append("       ( SELECT DESCRIPTION \n")
		.append("       FROM PROCESSFLOW \n")
		.append("       WHERE PROCESSFLOWNAME = PA.TOPROCESSFLOWNAME) DESCRIPTION   \n")
		.append("       FROM TPEFOPOLICY TPEFO2, POSALTERPROCESSOPERATION PA, \n")
		.append("       ( SELECT N.FACTORYNAME, N.PROCESSFLOWNAME, N.PROCESSFLOWVERSION,  \n")
		.append("       N.NODEATTRIBUTE1 AS PROCESSOPERATIONNAME, N.NODEATTRIBUTE2 AS PROCESSOPERATIONVERSION \n")               
		.append("       FROM (SELECT DISTINCT FROMNODEID \n")
		.append("       FROM ARC A, NODE N, ( \n")
		.append("       SELECT N.NODEID \n")
		.append("       FROM NODE N \n")
		.append("       WHERE 1=1 \n")
		.append("       AND N.FACTORYNAME =:FACTORYNAME  \n")
		.append("       AND N.PROCESSFLOWNAME = :PROCESSFLOWNAME \n")
		.append("       AND N.PROCESSFLOWVERSION = :PROCESSFLOWVERSION \n")
		.append("       AND N.NODEATTRIBUTE1 = :PROCESSOPERATIONNAME \n")         
		.append("       AND N.NODEATTRIBUTE2 = :PROCESSOPERATIONVERSION \n")         
		.append("       ) NL \n")        
		.append("       WHERE 1=1 \n")        
		.append("       AND A.TONODEID = NL.NODEID \n")         
		.append("       ) NA, NODE N \n")         
		.append("       WHERE NA.FROMNODEID = N.NODEID \n")         
		.append("       ) BEFOREOPER \n")         
		.append("       WHERE 1=1 \n")         
		.append("       AND ((TPEFO2.FACTORYNAME = BEFOREOPER.FACTORYNAME) OR (TPEFO2.FACTORYNAME = '*')) \n")      
		.append("       AND ((TPEFO2.PRODUCTSPECNAME = :PRODUCTSPECNAME) OR (TPEFO2.PRODUCTSPECNAME = '*')) \n")         
		.append("       AND ((TPEFO2.PRODUCTSPECVERSION = :PRODUCTSPECVERSION) OR (TPEFO2.PRODUCTSPECVERSION = '*')) \n")         
		.append("       AND ((TPEFO2.ECCODE = :ECCODE) OR (TPEFO2.ECCODE = '*')) \n")         
		.append("       AND ((TPEFO2.PROCESSFLOWNAME = BEFOREOPER.PROCESSFLOWNAME) OR (TPEFO2.PROCESSFLOWNAME = '*')) \n")         
		.append("       AND ((TPEFO2.PROCESSFLOWVERSION = BEFOREOPER.PROCESSFLOWVERSION) OR (TPEFO2.PROCESSFLOWVERSION = '*')) \n")         
		.append("       AND ((TPEFO2.PROCESSOPERATIONNAME = BEFOREOPER.PROCESSOPERATIONNAME) OR (TPEFO2.PROCESSOPERATIONNAME = '*')) \n")         
		.append("       AND ((TPEFO2.PROCESSOPERATIONVERSION = BEFOREOPER.PROCESSOPERATIONVERSION) OR (TPEFO2.PROCESSOPERATIONVERSION = '*')) \n")        
		.append("       AND PA.TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME \n")         
		.append("       AND TPEFO2.CONDITIONID = PA.CONDITIONID \n")         
		.append("       AND UPPER(PA.CONDITIONNAME) = :CONDITIONNAME \n")         
		.append("       AND NVL(PA.VALIDFLAG, :N) = :Y \n")         
		.append("       ) A \n");

		HashMap<String, Object> bindMap = new HashMap<String, Object>();

		bindMap.put("FACTORYNAME", lotData.getFactoryName());
		bindMap.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
		bindMap.put("PROCESSFLOWVERSION", lotData.getProcessFlowVersion());
		bindMap.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
		bindMap.put("PROCESSOPERATIONVERSION", lotData.getProcessOperationVersion());
		bindMap.put("PRODUCTSPECNAME", lotData.getProductSpecName());
		bindMap.put("PRODUCTSPECVERSION", lotData.getProductSpecVersion());
		bindMap.put("ECCODE", lotData.getUdfs().get("ECCODE"));
		bindMap.put("TOPROCESSFLOWNAME", branchFlowName);
		bindMap.put("CONDITIONNAME", "BRANCH");
		bindMap.put("Y", "Y");
		bindMap.put("N", "N");

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);

		if(sqlResult == null || sqlResult.size() == 0){
			throw new CustomException("MODELER-0001", lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"), lotData.getProcessFlowName(), lotData.getProcessOperationName(), branchFlowName);
		}
	}
}
