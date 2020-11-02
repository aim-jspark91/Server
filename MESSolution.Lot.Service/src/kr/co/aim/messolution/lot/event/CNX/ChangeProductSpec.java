package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.PanelJudge;
import kr.co.aim.messolution.extended.object.management.data.VirtualGlass;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.GradeDefUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.MakeReleasedInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.ProductRequestService;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequestplan.ProductRequestPlanServiceProxy;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

//import sun.org.mozilla.javascript.internal.regexp.SubString;

public class ChangeProductSpec extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String newProcessFlowName = SMessageUtil.getBodyItemValue(doc, "NEWPROCESSFLOWNAME", true);
		String newProcessOperationName = SMessageUtil.getBodyItemValue(doc, "NEWPROCESSOPERATIONNAME", true);
		String eccode = SMessageUtil.getBodyItemValue(doc, "ECCODE", true);
		String newwo = SMessageUtil.getBodyItemValue(doc, "NEWWORKORDER", true);
		String note = SMessageUtil.getBodyItemValue(doc, "NOTE", true);
		List<Element> eleLotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		
		String oldProductSpecName = SMessageUtil.getBodyItemValue(doc, "OLDPRODUCTSPECNAME", true);//add by GJJ 20200101 mantis:5532
		
		// Added by smkang on 2019.06.17 - According to Liu Hongwei's request, Lot should be locked to be prevented concurrent executing.
		eventLog.debug("Lot will be locked to be prevented concurrent executing.");
		List<Lot> lotDataList = new ArrayList<Lot>();
		Map<String, String> lotMap = new HashMap<String, String>();
		for (Element eleLot : eleLotList) {
			
			// 20200323 add by GJJ mantis :5852 卡控nodestack与Server一致
			Lot lotData =  LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(SMessageUtil.getChildText(eleLot, "LOTNAME", true)));
			String xNodeStack = SMessageUtil.getChildText(eleLot, "NODESTACK", false);
			if( xNodeStack.isEmpty() || xNodeStack.equals(lotData.getNodeStack()))
			{
				lotDataList.add(lotData);
			}
			else {
				setLanguage("Chinese");
				throw new CustomException("LOT-8002");
			}
				
			lotMap.put(lotData.getKey().getLotName(), 
					SMessageUtil.getChildText(eleLot, "ISLAST", false));
			// 20200323 add by GJJ mantis :5852 卡控nodestack与Server一致
			
			
			//lotDataList.add(LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(SMessageUtil.getChildText(eleLot, "LOTNAME", true))));modefiy by GJJ 20200323
			//lotMap.put(SMessageUtil.getChildText(eleLot, "LOTNAME", true), 
					//SMessageUtil.getChildText(eleLot, "ISLAST", false));//add by GJJ 20200101 mantis:5532
		
		}
		eventLog.debug("Lot is locked to be prevented concurrent executing.");
				
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeWO", getEventUser(), "ChangeWorkOrder", null, null);
		
		// newProcessFlowInfo
		ProcessFlowKey newProcessKeyInfo = new ProcessFlowKey(factoryName, newProcessFlowName, "00001");
		ProcessFlow newProcessFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(newProcessKeyInfo);
		
		// Modified by smkang on 2019.06.17 - According to Liu Hongwei's request, Lot should be locked to be prevented concurrent executing.
//		for(Element eleLot : eleLotList)
//		{
//			String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
//			
//			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//			
//            // Added by smkang on 2019.05.21 - According to Jiang Haiying's request, after StartCSTInfoCheckRequest is succeeded, 
//			//								   HoldLot/ForceSampling/StartBranch/StartRework/ChangeProductSpec/ChangeProcessFlow/ChangeProcessOperation
//			//								   PrepareSort/CancelPrepareSort/ForceOutSampling/CancelBranch/CancelRework/CompleteRework should be rejected.
//			if (StringUtils.equals(lotData.getUdfs().get("STARTCHECKRESULT"), "Y"))
//				throw new CustomException("LOT-9050", lotName);
//			
//	   		String beforeProcessFlowName = lotData.getProcessFlowName();
//	   		String beforeProcessOperationName = lotData.getProcessOperationName();
//	   		
//	   		String flowType = MESLotServiceProxy.getLotServiceUtil().returnFlowType(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion());
//	   		
//			if(!StringUtils.equals(flowType.toUpperCase(), "MAIN"))
//			{
//				throw new CustomException("FLOW-0001", flowType); 
//			}
//			
//			String oldWO = lotData.getProductRequestName();
//			
//			//oldProcessFlowInfo
//			ProcessFlowKey oldProcessKeyInfo = new ProcessFlowKey(lotData.getFactoryName(), lotData.getProcessFlowName(), "00001");
//			ProcessFlow oldProcessFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(oldProcessKeyInfo);
//			
//			// 2018.02 Modify
//			//check Same FlowType, Same Operation. 
//			this.validation(lotData, newProcessOperationName, newProcessFlowData, oldProcessFlowData); 
//			//check All Operation.
//			this.validation_Operation(newProcessOperationName, newProcessFlowName, factoryName);
//			// ********** 2018.02.23 **********
//			String oldoperationData = lotData.getProcessOperationName();
//			String oldprocessflowData = lotData.getProcessFlowName();					
//			this.validation_Flow(oldoperationData, oldprocessflowData, newProcessOperationName, newProcessFlowName, factoryName);
//			// ********** 2018.02.23 **********
//			
//			ProductSpec produtSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, productSpecName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
//			
//			List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotName);
//			
//			//zhongsl 2017.7.20 when Operation Changed, Update Product ProcessingInfo to N
//			productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productUdfs, "");
//			
//			ChangeSpecInfo changeSpecInfo = null;
//			
//			// ********** 2018.02.23 **********
//			Map<String, String> udfs = lotData.getUdfs();
//			udfs.put("ECCODE", eccode);
//			//2018.09.18 dmlee : old Product Request
//			udfs.put("OLDPRODUCTREQUESTNAME", oldWO);
//			//2018.09.18 dmlee ---------------------
//			
//			//2018.10.11 hsryu : note
//			udfs.put("NOTE", note);
//			
//			for (ProductU product : productUdfs) {
//				Map<String, String> productsUdfs = new HashMap<String, String>();
//				productsUdfs = product.getUdfs();
//				productsUdfs.put("ECCODE", eccode);
//			}
//			// ********** 2018.02.23 **********
//			
//			// 2019.04.25_hsryu_Delete Logic. if WorkOrder of Lot is 'MIXED', Error.
//			//this.validation_Amount_Of_WorkOrder(lotData, newwo,oldWO,factoryName);
//			// 2019.04.25_hsryu_Insert Logic. 
//			MESWorkOrderServiceProxy.getProductRequestServiceUtil().adjustWorkOrder(lotData, newwo, eventInfo);
//			
//			eventInfo.setBehaviorName("ARRAY");
//			
//			changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotName,
//					produtSpecData.getProductionType(), produtSpecData.getKey().getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION,
//					lotData.getProductSpec2Name(), lotData.getProductSpec2Version(),
//					newwo,
//					lotData.getSubProductUnitQuantity1(), lotData.getSubProductQuantity2(), lotData.getDueDate(), lotData.getPriority(),
//					factoryName, lotData.getAreaName(), lotData.getLotState(), lotData.getLotProcessState(), lotData.getLotHoldState(),
//					newProcessFlowName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION, newProcessOperationName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION,
//					"", "", "", "", "",
//					udfs, productUdfs,
//					false);
//
//			lotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
//			
//			// Modified by smkang on 2019.05.24 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
////			//Note clear - YJYU
////			Lot lotData_Note = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
////			Map<String, String> udfs_note = lotData_Note.getUdfs();
////			udfs_note.put("NOTE", "");
////			LotServiceProxy.getLotService().update(lotData_Note);
//			Map<String, String> updateUdfs = new HashMap<String, String>();
//			updateUdfs.put("NOTE", "");
//			MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
//
//			if(!MESLotServiceProxy.getLotServiceUtil().isExistBholdorOperHold(lotName, eventInfo))
//			{
//				//Reserve Change
//				lotData = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionChange(lotData.getKey().getLotName(), eventInfo);
//			}
//
//			MESLotServiceProxy.getLotServiceUtil().deletePermanentHoldInfo(lotData.getKey().getLotName(), lotData.getFactoryName(), beforeProcessFlowName,beforeProcessOperationName, eventInfo);
//		}
		for (Lot lotData : lotDataList) {						
			
			eventInfo.setEventName("ChangeWO");// add by GJJ 20200131 mantis:5612
			eventInfo.setEventComment("ChangeWorkOrder");// add by GJJ 20200131 mantis:5612
			
            // Added by smkang on 2019.05.21 - According to Jiang Haiying's request, after StartCSTInfoCheckRequest is succeeded, 
			//								   HoldLot/ForceSampling/StartBranch/StartRework/ChangeProductSpec/ChangeProcessFlow/ChangeProcessOperation
			//								   PrepareSort/CancelPrepareSort/ForceOutSampling/CancelBranch/CancelRework/CompleteRework should be rejected.
			if (StringUtils.equals(lotData.getUdfs().get("STARTCHECKRESULT"), "Y"))
				throw new CustomException("LOT-9050", lotData.getKey().getLotName());
			
	   		String beforeProcessFlowName = lotData.getProcessFlowName();
	   		String beforeProcessOperationName = lotData.getProcessOperationName();
	   		
	   		String flowType = MESLotServiceProxy.getLotServiceUtil().returnFlowType(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion());
	   		
			if(!StringUtils.equals(flowType.toUpperCase(), "MAIN"))
			{
				throw new CustomException("FLOW-0001", flowType); 
			}
			
			String oldWO = lotData.getProductRequestName();
			
			//oldProcessFlowInfo
			ProcessFlowKey oldProcessKeyInfo = new ProcessFlowKey(lotData.getFactoryName(), lotData.getProcessFlowName(), "00001");
			ProcessFlow oldProcessFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(oldProcessKeyInfo);
			
			// 2018.02 Modify
			//check Same FlowType, Same Operation. 
			this.validation(lotData, newProcessOperationName, newProcessFlowData, oldProcessFlowData); 
			//check All Operation.
			this.validation_Operation(newProcessOperationName, newProcessFlowName, factoryName);
			// ********** 2018.02.23 **********
			String oldoperationData = lotData.getProcessOperationName();
			String oldprocessflowData = lotData.getProcessFlowName();					
			this.validation_Flow(oldoperationData, oldprocessflowData, newProcessOperationName, newProcessFlowName, factoryName);
			// ********** 2018.02.23 **********
			
			ProductSpec produtSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, productSpecName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
			
			List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotData.getKey().getLotName());
			
			//zhongsl 2017.7.20 when Operation Changed, Update Product ProcessingInfo to N
			productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productUdfs, "");
			
			ChangeSpecInfo changeSpecInfo = null;
			
			// ********** 2018.02.23 **********
			Map<String, String> udfs = lotData.getUdfs();
			udfs.put("ECCODE", eccode);
			//2018.09.18 dmlee : old Product Request
			udfs.put("OLDPRODUCTREQUESTNAME", oldWO);
			//2018.09.18 dmlee ---------------------
			
			//2018.10.11 hsryu : note
			udfs.put("NOTE", note);
			
			for (ProductU product : productUdfs) {
				Map<String, String> productsUdfs = new HashMap<String, String>();
				productsUdfs = product.getUdfs();
				productsUdfs.put("ECCODE", eccode);
			}
			// ********** 2018.02.23 **********
			
			// 2019.04.25_hsryu_Delete Logic. if WorkOrder of Lot is 'MIXED', Error.
			//this.validation_Amount_Of_WorkOrder(lotData, newwo,oldWO,factoryName);
			// 2019.04.25_hsryu_Insert Logic. 
			MESWorkOrderServiceProxy.getProductRequestServiceUtil().adjustWorkOrder(lotData, newwo, eventInfo);
			
			eventInfo.setBehaviorName("ARRAY");
			
			changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotData.getKey().getLotName(),
					produtSpecData.getProductionType(), produtSpecData.getKey().getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION,
					lotData.getProductSpec2Name(), lotData.getProductSpec2Version(),
					newwo,
					lotData.getSubProductUnitQuantity1(), lotData.getSubProductQuantity2(), lotData.getDueDate(), lotData.getPriority(),
					factoryName, lotData.getAreaName(), lotData.getLotState(), lotData.getLotProcessState(), lotData.getLotHoldState(),
					newProcessFlowName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION, newProcessOperationName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION,
					"", "", "", "", "",
					udfs, productUdfs,
					false);

// Delete by mgkang 20200901			
//			//add by GJJ 20200101 mantis:5696 start
//			ProductRequest  newRequestData = ProductRequestServiceProxy.getProductRequestService().selectByKey(new ProductRequestKey(newwo));
//			ProductRequest  oldRequestData = ProductRequestServiceProxy.getProductRequestService().selectByKey(new ProductRequestKey(oldWO));
//			
//			if((newwo.length()>11 && !newRequestData.getUdfs().get("ENDBANK").isEmpty()&&oldRequestData.getUdfs().get("ENDBANK").isEmpty())|| 
//					(oldWO.length()>11 && !oldRequestData.getUdfs().get("ENDBANK").isEmpty())&&newRequestData.getUdfs().get("ENDBANK").isEmpty())
//			{
//				eventInfo.setEventName("CHANGEWOSCARP");
//			}
			//add by GJJ 20200101 mantis:5696 end 
			
			lotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
			
			// Modified by smkang on 2019.05.24 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//			//Note clear - YJYU
//			Lot lotData_Note = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//			Map<String, String> udfs_note = lotData_Note.getUdfs();
//			udfs_note.put("NOTE", "");
//			LotServiceProxy.getLotService().update(lotData_Note);
			Map<String, String> updateUdfs = new HashMap<String, String>();
			updateUdfs.put("NOTE", "");
			MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);

			if(!MESLotServiceProxy.getLotServiceUtil().isExistBholdorOperHold(lotData.getKey().getLotName(), eventInfo))
			{
				//Reserve Change
				lotData = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionChange(lotData.getKey().getLotName(), eventInfo);
			}

			MESLotServiceProxy.getLotServiceUtil().deletePermanentHoldInfo(lotData.getKey().getLotName(), lotData.getFactoryName(), beforeProcessFlowName,beforeProcessOperationName, eventInfo);

// Delete by mgkang 20200901
//			//add by GJJ 20200101 mantis:5532 start
//			if(lotMap.containsKey(lotData.getKey().getLotName()))
//			{
//				String isLast =	lotMap.get(lotData.getKey().getLotName());
//				
//				if(!StringUtil.isEmpty(isLast)&& isLast.equals("TRUE"))
//				{
//					PanelJudge paneljudgeInfo = ExtendedObjectProxy.getPanelJudgeService()
//							.select("WHERE GLASSNAME IN ( SELECT PRODUCTNAME FROM PRODUCT WHERE LOTNAME=:LOTNAME ) and ROWNUM=1 ", new Object[] { lotData.getKey().getLotName() }).get(0);
//						
//					if (Long.valueOf(produtSpecData.getUdfs().get("CUT1XAXISCOUNT")) != paneljudgeInfo.getxAxis1()
//					|| Long.valueOf(produtSpecData.getUdfs().get("CUT2XAXISCOUNT")) != paneljudgeInfo.getxAxis2()
//					|| Long.valueOf(produtSpecData.getUdfs().get("CUT1YAXISCOUNT")) != paneljudgeInfo.getyAxis1()
//					|| Long.valueOf(produtSpecData.getUdfs().get("CUT2YAXISCOUNT")) != paneljudgeInfo.getyAxis2())// add by GJJ 20200131 mantis:5612
//					// if (validationXY(lotData, produtSpecData))
//					{
//
//						// if(!productSpecName.substring(3,5).equals(oldProductSpecName.substring(3,5)) && isLast.equals("TRUE") )
//						// {
//						EventInfo eventInfo1 = EventInfoUtil.makeEventInfo("RegeneratePanelID", getEventUser(), "RegeneratePanelID", null, null);
//
//						modfiyPanelJudge(lotData, eventInfo1);
//						// }
//						eventInfo1.setEventName("RegeneratePanelID");
//						eventInfo1.setEventComment("新料号LayOut与初始化下线的LayOut不一致,重新生成PanelID并初始化为G");
//						SetEventInfo setEventInfo = new SetEventInfo();
//						//setEventInfo.getUdfs().put("NOTE", "新料号LayOut与初始化下线的LayOut不一致重新生成PanelID并初始化为G");
//						LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo1, setEventInfo);
//						//MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
//						
//					}
//				}
//			}
//			//add by GJJ 20200101 mantis:5532 end
			
		}
		
		return doc;
	}
	
	private void validation_Operation(String newProcessOperationName, String newProcessFlowName, String factoryName)
			throws CustomException
	{
		String sql = "SELECT LEVEL LV,FACTORYNAME,PROCESSOPERATIONNAME,PROCESSFLOWNAME,PROCESSFLOWVERSION,NODEID,NODETYPE";
		sql += " FROM (SELECT N.FACTORYNAME,N.NODEATTRIBUTE1 PROCESSOPERATIONNAME,N.PROCESSFLOWNAME,N.PROCESSFLOWVERSION,N.NODEID,N.NODETYPE,A.FROMNODEID,A.TONODEID";
		sql += " FROM ARC A,";
		sql += " NODE N,";
		sql += " PROCESSFLOW PF";
		sql += " WHERE 1 = 1";
		sql += " AND PF.PROCESSFLOWNAME = :PROCESSFLOWNAME";
		sql += " AND N.FACTORYNAME = :FACTORYNAME";
		sql += " AND N.PROCESSFLOWNAME = PF.PROCESSFLOWNAME";
		sql += " AND N.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION";
		sql += " AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME";
		sql += " AND N.FACTORYNAME = PF.FACTORYNAME";
		sql += " AND A.FROMNODEID = N.NODEID)";
		sql += " START WITH NODETYPE = :NODETYPE";
		sql += " CONNECT BY NOCYCLE FROMNODEID = PRIOR TONODEID";
		Map<String, String> newbindMap = new HashMap<String, String>();
		newbindMap.put("PROCESSFLOWNAME", newProcessFlowName);
		newbindMap.put("FACTORYNAME", factoryName);
		newbindMap.put("PROCESSOPERATIONNAME", newProcessOperationName);
		newbindMap.put("NODETYPE", GenericServiceProxy.getConstantMap().Node_Start);
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> newsqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, newbindMap);
		if (newsqlResult.size() > 0) 
		{
			// Check new Operation
			boolean checkOperation = false;			
			for( int k=1; k< newsqlResult.size(); k++)
			{
				if(!StringUtil.equals(newsqlResult.get(k).get("NODETYPE").toString(), "End"))
				{
					if(newProcessOperationName.equals(newsqlResult.get(k).get("processoperationname").toString()))
					{
						checkOperation = !checkOperation;
					}
				}
			}
			
			if(!checkOperation)
			{
				throw new CustomException("PROCESSOPERATION-0003", newProcessOperationName);
			}
		}
		else {
			throw new CustomException("PROCESSOPERATION-0002");
		}
	}
	
	private void validation_Flow(String oldoperationData, String oldprocessflowData, String newProcessOperationName, String newProcessFlowName, String factoryName)
		throws CustomException
	{
		String sql = "SELECT LEVEL LV,FACTORYNAME,PROCESSOPERATIONNAME,PROCESSFLOWNAME,PROCESSFLOWVERSION,NODEID,NODETYPE";
		sql += " FROM (SELECT N.FACTORYNAME,N.NODEATTRIBUTE1 PROCESSOPERATIONNAME,N.PROCESSFLOWNAME,N.PROCESSFLOWVERSION,N.NODEID,N.NODETYPE,A.FROMNODEID,A.TONODEID";
		sql += " FROM ARC A,";
		sql += " NODE N,";
		sql += " PROCESSFLOW PF";
		sql += " WHERE 1 = 1";
		sql += " AND PF.PROCESSFLOWNAME = :PROCESSFLOWNAME";
		sql += " AND N.FACTORYNAME = :FACTORYNAME";
		sql += " AND N.PROCESSFLOWNAME = PF.PROCESSFLOWNAME";
		sql += " AND N.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION";
		sql += " AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME";
		sql += " AND N.FACTORYNAME = PF.FACTORYNAME";
		sql += " AND A.FROMNODEID = N.NODEID)";
		sql += " START WITH NODETYPE = :NODETYPE";
		sql += " CONNECT BY NOCYCLE FROMNODEID = PRIOR TONODEID";
		sql += " AND PROCESSOPERATIONNAME <> :PROCESSOPERATIONNAME";
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("PROCESSFLOWNAME", oldprocessflowData);
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PROCESSOPERATIONNAME", oldoperationData);
		bindMap.put("NODETYPE", GenericServiceProxy.getConstantMap().Node_Start);
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> OldsqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		if (OldsqlResult.size() > 0) 
		{
			Map<String, String> newbindMap = new HashMap<String, String>();
			newbindMap.put("PROCESSFLOWNAME", newProcessFlowName);
			newbindMap.put("FACTORYNAME", factoryName);
			newbindMap.put("PROCESSOPERATIONNAME", newProcessOperationName);
			newbindMap.put("NODETYPE", GenericServiceProxy.getConstantMap().Node_Start);
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> newsqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, newbindMap);
			
			if (newsqlResult.size() > 0) 
			{				
				// Compare between old flow and new flow before new operation
				String oldname = "";
				String Newname = "";							
				for( int i=1; i<OldsqlResult.size(); i++)
				{
					if(!StringUtil.equals(OldsqlResult.get(i).get("NODETYPE").toString(), "Start"))
					{
						boolean check = false;					
						oldname = OldsqlResult.get(i).get("processoperationname").toString();
						for( int j=1; j<newsqlResult.size(); j++)
						{
							Newname = newsqlResult.get(j).get("processoperationname").toString();						
							if(oldname.equals(Newname))
							{
								check = !check;
							}
						}
						if(!check)
						{
							throw new CustomException("PROCESSOPERATION-0002");
						}
					}
				}			
			}
			else {
				throw new CustomException("PROCESSOPERATION-0002");
			}			
		}
		else {
			throw new CustomException("PROCESSOPERATION-0002");
		}		
	}
		
	private void validation(Lot lotData, String newProcessOperationName, ProcessFlow newProcessFlowData, ProcessFlow oldProcessFlowData)
		throws CustomException
	{
		// Check ProcessOperation 2018.02.20
		if(!StringUtil.equals(lotData.getProcessOperationName(),newProcessOperationName))
		{
			throw new CustomException("PROCESSOPERATION-0001", lotData.getKey().getLotName());
		}
		
		// Check ProcessFlowType 2018.02.22
		if(!StringUtil.equals(newProcessFlowData.getProcessFlowType() ,oldProcessFlowData.getProcessFlowType()))
		{
			throw new CustomException("PROCESSFlOWTYPE-0001", lotData.getKey().getLotName());
		}
	}
	
	private Boolean validationXY(Lot lotData , ProductSpec produtSpecData)
			throws CustomException
		{
		String sql = "SELECT ";
		sql += "	CASE ";
		sql += "		WHEN COUNT(*) = :COUNT ";
		sql += "			THEN 'FALSE' ";
		sql += "		WHEN COUNT(*)= 0 ";
		sql += "			THEN 'TRUE' ";
		sql += "			ELSE 'N' ";
		sql += "	END AS RESULT  ";
		sql += "FROM ";
		sql += "	( ";
		sql += "		SELECT DISTINCT ";
		sql += "			GLASSNAME , XAXIS1 ";
		sql += "		  , XAXIS2    , YAXIS1 ";
		sql += "		  , YAXIS2 ";
		sql += "		FROM ";
		sql += "			CT_PANELJUDGE ";
		sql += "		WHERE ";
		sql += "			GLASSNAME IN ";
		sql += "			( ";
		sql += "				SELECT ";
		sql += "					PRODUCTNAME ";
		sql += "				FROM ";
		sql += "					PRODUCT ";
		sql += "				WHERE ";
		sql += "					LOTNAME=:LOTNAME ";
		sql += "			) ";
		sql += "	) ";
		sql += "WHERE ";
		sql += "	XAXIS1     =:XAXIS1 ";
		sql += "	AND XAXIS2 =:XAXIS2 ";
		sql += "	AND YAXIS1 =:YAXIS1 ";
		sql += "	AND YAXIS2 =:YAXIS2 ";
		
		Map<Object, Object> bindMap = new HashMap<Object, Object>();
		bindMap.put("COUNT",lotData.getProductQuantity());
		bindMap.put("LOTNAME", lotData.getKey().getLotName());
		bindMap.put("XAXIS1", produtSpecData.getUdfs().get("XAXIS1"));
		bindMap.put("XAXIS2", produtSpecData.getUdfs().get("XAXIS2"));
		bindMap.put("YAXIS1", produtSpecData.getUdfs().get("YAXIS1"));
		bindMap.put("YAXIS2", produtSpecData.getUdfs().get("YAXIS2"));
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		if (sqlResult.size() > 0) 
		{
			if(StringUtil.equals(sqlResult.get(0).get("RESULT").toString(), "TRUE"))
					{
				return true;
				
					}
			if(StringUtil.equals(sqlResult.get(0).get("RESULT").toString(), "FALSE"))
			{
		return false;
		
			}
			
			if(StringUtil.equals(sqlResult.get(0).get("RESULT").toString(), "N"))
			{
				throw new CustomException("PROCESSOPERATION-0003", " ");
		
			}
			
		
		}
		return true;
		
		}
	
	
	private void modfiyPanelJudge(Lot lotData,EventInfo  eventInfo)
			throws CustomException
		{
		ProductSpec specData = GenericServiceProxy.getSpecUtil().getProductSpec(lotData.getFactoryName(), lotData.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
	    
		List<Product> productList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());

		String cutType = specData.getUdfs().get("CUTTYPE");
		List<Object[]> insertArgList = new ArrayList<Object[]>();
		String insertSql = " INSERT INTO CT_PANELJUDGE "
				+ " (PANELNAME, PANELJUDGE, PANELGRADE, XAXIS1, YAXIS1, XAXIS2, YAXIS2, "
				+ " GLASSNAME, HQGLASSNAME, CUTTYPE, LASTEVENTNAME, LASTEVENTUSER, LASTEVENTTIME, LASTEVENTCOMMENT, PRODUCTSPECTYPE)"
				+ " VALUES "
				+ " (:PANELNAME, :PANELJUDGE, :PANELGRADE, NVL(:XAXIS1,0), NVL(:YAXIS1,0), NVL(:XAXIS2,0), NVL(:YAXIS2,0), "
				+ "  :GLASSNAME, :HQGLASSNAME, :CUTTYPE, :LASTEVENTNAME, :LASTEVENTUSER, TO_DATE(:LASTEVENTTIME,'yyyy-MM-dd HH24:mi:ss'), :LASTEVENTCOMMENT, :PRODUCTSPECTYPE)";
				
		
		for(Product ePruduct : productList)
		{			                
			try
			{
				
				try {
					List<PanelJudge> panelJudgeList = ExtendedObjectProxy.getPanelJudgeService().select("WHERE GLASSNAME = ?", new Object[] {ePruduct.getKey().getProductName()});
										
					for( PanelJudge judge  :panelJudgeList)
					{
						ExtendedObjectProxy.getPanelJudgeService().delete(judge);
					}
					
				} catch (Exception e) {
					
				}
				
				//2019.08.01 dmlee : Half and Quarter Same logic
				if ( StringUtil.equals(cutType, GenericServiceProxy.getConstantMap().CUTTYPE_HALF) || StringUtil.equals(cutType, GenericServiceProxy.getConstantMap().CUTTYPE_QUARTER) )
				{
					int cut1XaxisCount = StringUtils.isNumeric(specData.getUdfs().get("CUT1XAXISCOUNT")) ? Integer.parseInt(specData.getUdfs().get("CUT1XAXISCOUNT")) : 0;
					int cut2XaxisCount = StringUtils.isNumeric(specData.getUdfs().get("CUT2XAXISCOUNT")) ? Integer.parseInt(specData.getUdfs().get("CUT2XAXISCOUNT")) : 0;
					int cut1YaxisCount = StringUtils.isNumeric(specData.getUdfs().get("CUT1YAXISCOUNT")) ? Integer.parseInt(specData.getUdfs().get("CUT1YAXISCOUNT")) : 0;
					int cut2YaxisCount = StringUtils.isNumeric(specData.getUdfs().get("CUT2YAXISCOUNT")) ? Integer.parseInt(specData.getUdfs().get("CUT2YAXISCOUNT")) : 0;
					
					for ( int i = 1; i < 3; i++ )
					{
						if(i==1)
						{
							for(int x=0; x<cut1XaxisCount; x++)
							{
								for(int y=0; y< cut1YaxisCount; y++)
								{
									Object[] inbindSet = new Object[15];

									inbindSet[0] = ePruduct.getKey().getProductName() + Integer.toString(i) + StringUtils.leftPad(Integer.toString(x+1), 2, "0") + StringUtils.leftPad(Integer.toString(y+1), 2, "0");
									inbindSet[1] = "G";
									inbindSet[2] = "G";
									inbindSet[3] = String.valueOf(cut1XaxisCount);
									inbindSet[4] = String.valueOf(cut1YaxisCount);
									inbindSet[5] = String.valueOf(cut2XaxisCount);
									inbindSet[6] = String.valueOf(cut2YaxisCount);
									inbindSet[7] = ePruduct.getKey().getProductName();
									inbindSet[8] = ePruduct.getKey().getProductName() + Integer.toString(i);
									inbindSet[9] = cutType;
									inbindSet[10] = "ChangeProductSpec";
									inbindSet[11] = eventInfo.getEventUser();
									inbindSet[12] = ConvertUtil.getCurrTime();
									inbindSet[13] = "Auto Create PanelJudge";
									inbindSet[14] = specData.getUdfs().get("PRODUCTSPECTYPE");
									
									insertArgList.add(inbindSet);
								}
							}
						}
						
						else if(i==2)
						{
							for(int x=0; x<cut2XaxisCount; x++)
							{
								for(int y=cut1YaxisCount; y<cut1YaxisCount+cut2YaxisCount; y++)
								{
									Object[] inbindSet = new Object[15];

									inbindSet[0] = ePruduct.getKey().getProductName() + Integer.toString(i) + StringUtils.leftPad(Integer.toString(x+1), 2, "0") + StringUtils.leftPad(Integer.toString(y+1), 2, "0");
									inbindSet[1] = "G";
									inbindSet[2] = "G";
									inbindSet[3] = String.valueOf(cut1XaxisCount);
									inbindSet[4] = String.valueOf(cut1YaxisCount);
									inbindSet[5] = String.valueOf(cut2XaxisCount);
									inbindSet[6] = String.valueOf(cut2YaxisCount);
									inbindSet[7] = ePruduct.getKey().getProductName();
									inbindSet[8] = ePruduct.getKey().getProductName() + Integer.toString(i);
									inbindSet[9] = cutType;
									inbindSet[10] = "ChangeProductSpec";
									inbindSet[11] = eventInfo.getEventUser();
									inbindSet[12] = ConvertUtil.getCurrTime();
									inbindSet[13] = "Auto Create PanelJudge";
									inbindSet[14] = specData.getUdfs().get("PRODUCTSPECTYPE");
									
									insertArgList.add(inbindSet);
								}
							}
						}
					}
				}
			}
			catch (Throwable e)
			{
				eventLog.warn(String.format("BindSet Fail! CT_PANELJUDGE"));
			}
		}
		
		eventInfo.setEventName("ChangeState");
		
		try
		{
			GenericServiceProxy.getSqlMesTemplate().updateBatch(insertSql, insertArgList);
			
			String insHistSql = " INSERT INTO CT_PANELJUDGEHISTORY "
					+ " (TIMEKEY, PANELNAME, PANELJUDGE, PANELGRADE, XAXIS1, YAXIS1, XAXIS2, YAXIS2, " 
					+ " GLASSNAME, HQGLASSNAME, CUTTYPE, EVENTNAME, EVENTUSER, EVENTTIME, EVENTCOMMENT, PRODUCTSPECTYPE) "
					+ " SELECT TO_CHAR(SYSDATE, 'YYYYMMDDHH24MISSsss'), PANELNAME, PANELJUDGE, PANELGRADE, XAXIS1, YAXIS1, XAXIS2, YAXIS2, " 
					+ " GLASSNAME, HQGLASSNAME, CUTTYPE, LASTEVENTNAME, LASTEVENTUSER, LASTEVENTTIME, LASTEVENTCOMMENT, PRODUCTSPECTYPE "
					+ " FROM CT_PANELJUDGE "
					+ " WHERE GLASSNAME IN (SELECT PRODUCTNAME FROM PRODUCT WHERE LOTNAME = :LOTNAME) ";
			
			Map<String, Object> insHistbindSet = new HashMap<String, Object>();
			insHistbindSet.put("LOTNAME", lotData.getKey().getLotName());
			
			GenericServiceProxy.getSqlMesTemplate().update(insHistSql, insHistbindSet);
		}
		catch (Exception ex)
		{
			eventLog.warn(String.format("Update Fail! CT_PANELJUDGE"));
		}		
		
		}
}