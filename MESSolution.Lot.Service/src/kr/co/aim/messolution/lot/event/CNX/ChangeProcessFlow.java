package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ChangeProcessFlow extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String newProcessFlowName = SMessageUtil.getBodyItemValue(doc, "NEWPROCESSFLOWNAME", true);
		String newProcessOperationName = SMessageUtil.getBodyItemValue(doc, "NEWPROCESSOPERATIONNAME", true);
		String eccode = SMessageUtil.getBodyItemValue(doc, "ECCODE", true);
		String note = SMessageUtil.getBodyItemValue(doc, "NOTE", true);
		List<Element> eleLotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		
		// Added by smkang on 2019.06.17 - According to Liu Hongwei's request, Lot should be locked to be prevented concurrent executing.
		eventLog.debug("Lot will be locked to be prevented concurrent executing.");
		List<Lot> lotDataList = new ArrayList<Lot>();
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
			// 20200323 add by GJJ mantis :5852 卡控nodestack与Server一致	
			
			
			//lotDataList.add(LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(SMessageUtil.getChildText(eleLot, "LOTNAME", true))));modefiy by GJJ 20200323
		}
		eventLog.debug("Lot is locked to be prevented concurrent executing.");
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeFlow", getEventUser(), "ChangeProcessFlow", null, null);
		eventInfo.setBehaviorName("ARRAY");
		
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
//			String beforeProcessFlowName = lotData.getProcessFlowName();
//			String beforeProcessOperationName = lotData.getProcessOperationName();
//			
//			//oldProcessFlowInfo
//			ProcessFlowKey oldProcessKeyInfo = new ProcessFlowKey(lotData.getFactoryName(), lotData.getProcessFlowName(), "00001");
//			ProcessFlow oldProcessFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(oldProcessKeyInfo);
//			
//			// 2018.02 Modify
//			this.validation(lotData, newProcessOperationName, newProcessFlowData, oldProcessFlowData); 
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
//			udfs.put("NOTE", note);
//			
//			for (ProductU product : productUdfs) {
//				Map<String, String> productsUdfs = new HashMap<String, String>();
//				productsUdfs = product.getUdfs();
//				productsUdfs.put("ECCODE", eccode);
//			}
//			// ********** 2018.02.23 **********
//			
//			//2018.11.01 Modify, hsryu, lotData.getProductRequestName -> "", Because WorkOrder of Product must not be changed.
//			changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotName,
//					lotData.getProductionType(), produtSpecData.getKey().getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION,
//					lotData.getProductSpec2Name(), lotData.getProductSpec2Version(),
//					"",
//					lotData.getSubProductUnitQuantity1(), lotData.getSubProductQuantity2(), lotData.getDueDate(), lotData.getPriority(),
//					factoryName, lotData.getAreaName(), lotData.getLotState(), lotData.getLotProcessState(), lotData.getLotHoldState(),
//					newProcessFlowName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION, newProcessOperationName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION,
//					"", "", "", "", "",
//					lotData.getUdfs(), productUdfs,
//					false);
//					
//			lotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
//			
//			//Note clear - YJYU
//			// Modified by smkang on 2019.05.24 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
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
            // Added by smkang on 2019.05.21 - According to Jiang Haiying's request, after StartCSTInfoCheckRequest is succeeded, 
			//								   HoldLot/ForceSampling/StartBranch/StartRework/ChangeProductSpec/ChangeProcessFlow/ChangeProcessOperation
			//								   PrepareSort/CancelPrepareSort/ForceOutSampling/CancelBranch/CancelRework/CompleteRework should be rejected.
			if (StringUtils.equals(lotData.getUdfs().get("STARTCHECKRESULT"), "Y"))
				throw new CustomException("LOT-9050", lotData.getKey().getLotName());
			
			String beforeProcessFlowName = lotData.getProcessFlowName();
			String beforeProcessOperationName = lotData.getProcessOperationName();
			
			//oldProcessFlowInfo
			ProcessFlowKey oldProcessKeyInfo = new ProcessFlowKey(lotData.getFactoryName(), lotData.getProcessFlowName(), "00001");
			ProcessFlow oldProcessFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(oldProcessKeyInfo);
			
			// 2018.02 Modify
			this.validation(lotData, newProcessOperationName, newProcessFlowData, oldProcessFlowData); 
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
			udfs.put("NOTE", note);
			
			for (ProductU product : productUdfs) {
				Map<String, String> productsUdfs = new HashMap<String, String>();
				productsUdfs = product.getUdfs();
				productsUdfs.put("ECCODE", eccode);
			}
			// ********** 2018.02.23 **********
			
			//2018.11.01 Modify, hsryu, lotData.getProductRequestName -> "", Because WorkOrder of Product must not be changed.
			changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotData.getKey().getLotName(),
					lotData.getProductionType(), produtSpecData.getKey().getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION,
					lotData.getProductSpec2Name(), lotData.getProductSpec2Version(),
					"",
					lotData.getSubProductUnitQuantity1(), lotData.getSubProductQuantity2(), lotData.getDueDate(), lotData.getPriority(),
					factoryName, lotData.getAreaName(), lotData.getLotState(), lotData.getLotProcessState(), lotData.getLotHoldState(),
					newProcessFlowName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION, newProcessOperationName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION,
					"", "", "", "", "",
					lotData.getUdfs(), productUdfs,
					false);
					
			lotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
			
			//Note clear - YJYU
			// Modified by smkang on 2019.05.24 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
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
				if(newProcessOperationName.equals(newsqlResult.get(k).get("processoperationname").toString()))
				{
					checkOperation = !checkOperation;
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
			// 2019.04.13_hsryu_Insert Logic. 
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
}