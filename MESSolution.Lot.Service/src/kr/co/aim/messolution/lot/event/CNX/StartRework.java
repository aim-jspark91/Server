package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
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
import kr.co.aim.greentrack.lot.management.info.MakeInReworkInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductRU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class StartRework extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "REWORKFLOWNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "REWORKOPERATIONNAME", true);
		String returnProcessFlowName = SMessageUtil.getBodyItemValue(doc, "RETURNFLOWNAME", true); 
		String returnProcessFlowVersion = SMessageUtil.getBodyItemValue(doc, "RETURNFLOWVERSION", true); 
		String returnProcessOperationName = SMessageUtil.getBodyItemValue(doc, "RETURNOPERATIONNAME", true); 
		String returnProcessOperationVersion = SMessageUtil.getBodyItemValue(doc, "RETURNOPERATIONVERSION", true); 
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", false);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", true);
		String departmentName = SMessageUtil.getBodyItemValue(doc, "DEPARTMENTNAME", true);
		String nodeStack = SMessageUtil.getBodyItemValue(doc, "NODESTACK", false);//add by jhying on20200324 mantis:5852
		
		// Added by smkang on 2019.06.17 - According to Liu Hongwei's request, Lot should be locked to be prevented concurrent executing.
		eventLog.debug("Lot will be locked to be prevented concurrent executing.");
		List<Lot> lotDataList = new ArrayList<Lot>();
		for (Element eleLot : lotList) {
			lotDataList.add(LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(SMessageUtil.getChildText(eleLot, "LOTNAME", true))));
		}
		eventLog.debug("Lot is locked to be prevented concurrent executing.");
		
		Element element = doc.getDocument().getRootElement(); 

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Rework", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		eventInfo.setBehaviorName("ARRAY");
		eventInfo.setReasonCode(reasonCode);

		// Modified by smkang on 2019.06.17 - According to Liu Hongwei's request, Lot should be locked to be prevented concurrent executing.
//		for (Element eleLot : lotList) 
//		{
//			lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
//			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//			
//			// 2019.06.03_hsryu_Add Validation. if FirstOperation, Can't Proceed.
//			if(CommonValidation.checkFirstOperation(lotData)){
//				throw new CustomException("PROCESSOPERATION-0005", lotData.getProcessOperationName(), "Rework");
//			}
//			
//			// 2019.06.05_hsryu_Add Validation. if already Lot in Same ReworkFlow, Can't Proceed. 
//			if(StringUtils.equals(lotData.getProcessFlowName(), processFlowName )){
//				throw new CustomException("PROCESSOPERATION-0006", lotData.getKey().getLotName(), processFlowName);
//			}
//			
//			// 2019.06.05_hsryu_Add Validation. Recheck AlterProcessOperation.
//			this.checkRegisterModeling(lotData ,processFlowName);
//			
//            // Added by smkang on 2019.05.21 - According to Jiang Haiying's request, after StartCSTInfoCheckRequest is succeeded, 
//			//								   HoldLot/ForceSampling/StartBranch/StartRework/ChangeProductSpec/ChangeProcessFlow/ChangeProcessOperation
//			//								   PrepareSort/CancelPrepareSort/ForceOutSampling/CancelBranch/CancelRework/CompleteRework should be rejected.
//			if (StringUtils.equals(lotData.getUdfs().get("STARTCHECKRESULT"), "Y"))
//				throw new CustomException("LOT-9050", lotName);
//
//			//validation 
//			CommonValidation.checkAlreadyLotProcessStateTrackIn(lotData);
//			
//			// 2019.05.14_hsryu_Add Validation. if MQCValidationFlag is 'N', Not Proceed!
//			CommonValidation.checkMQCValidationFlag(lotData);
//
//			if(StringUtils.equals(lotData.getLotGrade(), GenericServiceProxy.getConstantMap().LotGrade_S))
//			{
//				throw new CustomException("LOT-0224", lotData.getKey().getLotName(), lotData.getLotGrade());
//			}
//
//			String beforeProcessFlowName = lotData.getProcessFlowName();
//			String beforeProcessOperationName = lotData.getProcessOperationName();
//
//			Map<String, String> returnBeforeInfo = MESLotServiceProxy.getLotServiceUtil().getBeforeOperName(lotData.getFactoryName(), beforeProcessFlowName, "00001", beforeProcessOperationName, "00001");
//			beforeNodeStack = returnBeforeInfo.get("NODEID");
//
//			// makeInRework
//			Map<String, String> udfs = MESLotServiceProxy.getLotServiceUtil().setNamedValueSequence(lotName, element);								
//
//			List<Product> pProductList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
//
//			ExtendedObjectProxy.getreworkLotService().increaseReworkCount(pProductList, lotData, returnProcessFlowName, returnProcessOperationName, processFlowName, processOperationName, departmentName , eventInfo);
//
//			CommonValidation.checkOverReworkLimitCount2(pProductList,lotName,factoryName,returnProcessFlowName,returnProcessFlowVersion, returnProcessOperationName,returnProcessOperationVersion,processFlowName,processOperationName);
//
//			List<ProductRU> productRUdfs = new ArrayList<ProductRU>();
//
//			//2018.02.08 hsryu - Change LotGrade = "R"
//			lotData.setLotGrade(GenericServiceProxy.getConstantMap().LotGrade_R);	
//			LotServiceProxy.getLotService().update(lotData);
//
//			for (Product product : pProductList)
//			{
//				if(!StringUtils.equals(product.getProductGrade(), GenericServiceProxy.getConstantMap().ProductGrade_S))
//				{
//					product.setProductGrade(GenericServiceProxy.getConstantMap().ProductGrade_R);
//					ProductServiceProxy.getProductService().update(product);
//					
//					ProductRU productRU = new ProductRU();
//					productRU.setProductName(product.getKey().getProductName());						
//					productRU.setUdfs(product.getUdfs());
//					productRU.setReworkFlag("Y");
//
//					productRUdfs.add(productRU);
//				}
//			}
//
//			//2017.7.20 zhongsl  when Operation Changed, Update Product ProcessingInfo to N
//			productRUdfs = MESLotServiceProxy.getLotInfoUtil().setProductRUdfsProcessingInfo(productRUdfs,"");
//
//			//For ReworkToRework
//			Map<String, String> lotUdfs = lotData.getUdfs();
//			//lotUdfs.put("REWORKGRADE", "R");
//			lotUdfs.put("BEFOREFLOWNAME", lotData.getProcessFlowName());
//			lotUdfs.put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
//			//lotUdfs.put("REWORKDEPARTMENTNAME", departmentName);
//			lotData.setUdfs(lotUdfs);
//
//			String[] nodeStackArray = StringUtil.split(lotData.getNodeStack(), ".");
//			String tempNodeStack = "";
//
//			if(StringUtils.isNotEmpty(beforeNodeStack))
//			{
//				for(int i=0; i<nodeStackArray.length-1; i++)
//				{
//					tempNodeStack += nodeStackArray[i] + "."; 
//				}
//
//				tempNodeStack += beforeNodeStack;
//			}
//			else
//			{
//				tempNodeStack = lotData.getNodeStack();
//			}
//
//			MakeInReworkInfo makeInReworkInfo =  MESLotServiceProxy.getLotInfoUtil().makeInReworkInfo(lotData, 
//					eventInfo, 
//					lotName, 
//					processFlowName, 
//					processOperationName, 
//					returnProcessFlowName, 
//					returnProcessOperationName,
//					udfs, 
//					productRUdfs,
//					tempNodeStack);
//
//			MESLotServiceProxy.getLotServiceImpl().startRework(eventInfo, lotData, makeInReworkInfo);
//
//			Lot afterLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
//
//			if(!MESLotServiceProxy.getLotServiceUtil().isExistBholdorOperHold(lotName, eventInfo))
//			{
//				//Reserve Change
//				afterLotData = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionChange(afterLotData.getKey().getLotName(), eventInfo);
//			}
//
//			MESLotServiceProxy.getLotServiceUtil().deletePermanentHoldInfo(afterLotData.getKey().getLotName(), afterLotData.getFactoryName(), beforeProcessFlowName,beforeProcessOperationName, eventInfo);
//		}
		for (Lot lotData : lotDataList) {

//			//2019.09.02 Add By Park Jeong Su Mantis 4691
//			CommonValidation.checkFirstCheckResultIsY(lotData.getKey().getLotName());
			
			// 2019.06.03_hsryu_Add Validation. if FirstOperation, Can't Proceed.
			if(CommonValidation.checkFirstOperation(lotData)){
				throw new CustomException("PROCESSOPERATION-0005", lotData.getProcessOperationName(), "Rework");
			}
			
			// 2019.06.05_hsryu_Add Validation. if already Lot in Same ReworkFlow, Can't Proceed. 
			if(StringUtils.equals(lotData.getProcessFlowName(), processFlowName )){
				throw new CustomException("PROCESSOPERATION-0006", lotData.getKey().getLotName(), processFlowName);
			}
			
			// 2019.06.05_hsryu_Add Validation. Recheck AlterProcessOperation.
			this.checkRegisterModeling(lotData ,processFlowName);
			
            // Added by smkang on 2019.05.21 - According to Jiang Haiying's request, after StartCSTInfoCheckRequest is succeeded, 
			//								   HoldLot/ForceSampling/StartBranch/StartRework/ChangeProductSpec/ChangeProcessFlow/ChangeProcessOperation
			//								   PrepareSort/CancelPrepareSort/ForceOutSampling/CancelBranch/CancelRework/CompleteRework should be rejected.
			if (StringUtils.equals(lotData.getUdfs().get("STARTCHECKRESULT"), "Y"))
				throw new CustomException("LOT-9050", lotData.getKey().getLotName());
			
			if(!StringUtils.equals(lotData.getNodeStack(), nodeStack)){
				
				throw new CustomException("LOT-8002","");
			}//add by jhying on20200324 mantis:5852

			//MODIFY BY JHIYING START ON20190907 MANTIS:4691
			  String durableName = lotData.getCarrierName();
			  Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
			if(StringUtils.equals(carrierData.getUdfs().get("TRANSPORTSTATE"), "ONEQP"))
				throw new  CustomException("LOT-9053", durableName);
			
			//MODIFY BY JHIYING END ON20190907 MANTIS:4691
			
			//validation 
			CommonValidation.checkAlreadyLotProcessStateTrackIn(lotData);
			
			// 2019.05.14_hsryu_Add Validation. if MQCValidationFlag is 'N', Not Proceed!
			CommonValidation.checkMQCValidationFlag(lotData);

			if(StringUtils.equals(lotData.getLotGrade(), GenericServiceProxy.getConstantMap().LotGrade_S))
			{
				throw new CustomException("LOT-0224", lotData.getKey().getLotName(), lotData.getLotGrade());
			}

			String beforeProcessFlowName = lotData.getProcessFlowName();
			String beforeProcessOperationName = lotData.getProcessOperationName();

			Map<String, String> returnBeforeInfo = MESLotServiceProxy.getLotServiceUtil().getBeforeOperName(lotData.getFactoryName(), beforeProcessFlowName, "00001", beforeProcessOperationName, "00001");
			String beforeNodeStack = returnBeforeInfo.get("NODEID");

			// makeInRework
			Map<String, String> udfs = MESLotServiceProxy.getLotServiceUtil().setNamedValueSequence(lotData.getKey().getLotName(), element);								

			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			List<Product> pProductList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
			List<Product> pProductList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());

			ExtendedObjectProxy.getreworkLotService().increaseReworkCount(pProductList, lotData, returnProcessFlowName, returnProcessOperationName, processFlowName, processOperationName, departmentName , eventInfo);

			CommonValidation.checkOverReworkLimitCount2(pProductList, lotData.getKey().getLotName(), "", returnProcessFlowName, returnProcessFlowVersion, returnProcessOperationName, returnProcessOperationVersion, processFlowName, processOperationName);

			List<ProductRU> productRUdfs = new ArrayList<ProductRU>();

			//2018.02.08 hsryu - Change LotGrade = "R"
			lotData.setLotGrade(GenericServiceProxy.getConstantMap().LotGrade_R);	
			LotServiceProxy.getLotService().update(lotData);

			for (Product product : pProductList)
			{
				if(!StringUtils.equals(product.getProductGrade(), GenericServiceProxy.getConstantMap().ProductGrade_S))
				{
					product.setProductGrade(GenericServiceProxy.getConstantMap().ProductGrade_R);
					ProductServiceProxy.getProductService().update(product);
					
					ProductRU productRU = new ProductRU();
					productRU.setProductName(product.getKey().getProductName());						
					productRU.setUdfs(product.getUdfs());
					productRU.setReworkFlag("Y");

					productRUdfs.add(productRU);
				}
			}

			//2017.7.20 zhongsl  when Operation Changed, Update Product ProcessingInfo to N
			productRUdfs = MESLotServiceProxy.getLotInfoUtil().setProductRUdfsProcessingInfo(productRUdfs,"");

			//For ReworkToRework
			Map<String, String> lotUdfs = lotData.getUdfs();
			//lotUdfs.put("REWORKGRADE", "R");
			lotUdfs.put("BEFOREFLOWNAME", lotData.getProcessFlowName());
			lotUdfs.put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
			//lotUdfs.put("REWORKDEPARTMENTNAME", departmentName);
			lotData.setUdfs(lotUdfs);

			String[] nodeStackArray = StringUtil.split(lotData.getNodeStack(), ".");
			String tempNodeStack = "";

			if(StringUtils.isNotEmpty(beforeNodeStack))
			{
				for(int i=0; i<nodeStackArray.length-1; i++)
				{
					tempNodeStack += nodeStackArray[i] + "."; 
				}

				tempNodeStack += beforeNodeStack;
			}
			else
			{
				tempNodeStack = lotData.getNodeStack();
			}

			MakeInReworkInfo makeInReworkInfo =  MESLotServiceProxy.getLotInfoUtil().makeInReworkInfo(lotData, 
																										eventInfo, 
																										lotData.getKey().getLotName(), 
																										processFlowName, 
																										processOperationName, 
																										returnProcessFlowName, 
																										returnProcessOperationName,
																										udfs, 
																										productRUdfs,
																										tempNodeStack);

			MESLotServiceProxy.getLotServiceImpl().startRework(eventInfo, lotData, makeInReworkInfo);

			Lot afterLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());

			if(!MESLotServiceProxy.getLotServiceUtil().isExistBholdorOperHold(lotData.getKey().getLotName(), eventInfo))
			{
				//Reserve Change
				afterLotData = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionChange(afterLotData.getKey().getLotName(), eventInfo);
			}

			MESLotServiceProxy.getLotServiceUtil().deletePermanentHoldInfo(afterLotData.getKey().getLotName(), afterLotData.getFactoryName(), beforeProcessFlowName,beforeProcessOperationName, eventInfo);
		}

		return doc;
	}
	
	// 2019.06.05_hsryu_Check Resiger Modeleing. Requsted by CIM. 
	private void checkRegisterModeling(Lot lotData, String reworkFlowName) throws CustomException {
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
		.append("       AND UPPER(PA.CONDITIONVALUE) = :CONDITIONVALUE \n")        
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
        bindMap.put("TOPROCESSFLOWNAME", reworkFlowName);
        bindMap.put("CONDITIONNAME", "REWORK");
        bindMap.put("CONDITIONVALUE", "MANUAL");
        bindMap.put("Y", "Y");
        bindMap.put("N", "N");

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
		
        if(sqlResult == null || sqlResult.size() == 0){
        	throw new CustomException("MODELER-0001", lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"), lotData.getProcessFlowName(), lotData.getProcessOperationName(), reworkFlowName);
        }
	}
}
