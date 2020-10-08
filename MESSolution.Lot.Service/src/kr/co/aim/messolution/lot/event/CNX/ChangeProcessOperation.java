package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ChangeProcessOperation extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
 		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String targetOperationName = SMessageUtil.getBodyItemValue(doc, "NEWPROCESSOPERATIONNAME", true);
		String note = SMessageUtil.getBodyItemValue(doc, "NOTE", true);
		String flag = SMessageUtil.getBodyItemValue(doc, "FLAG", true);
		String eventFlag = SMessageUtil.getBodyItemValue(doc, "EVENTFLAG", false);
		
		String nowOperationName = SMessageUtil.getBodyItemValue(doc, "NOWOPERATIONNAME", false);

		
		// Modified by smkang on 2019.06.17 - According to Liu Hongwei's request, Lot should be locked to be prevented concurrent executing.
//		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		eventLog.debug("Lot will be locked to be prevented concurrent executing.");
		Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
		eventLog.debug("Lot is locked to be prevented concurrent executing.");
		
		// Start 2019.10.09 Add By Park Jeong Su Mantis 4953
		List<Product> productList = ProductServiceProxy.getProductService().select(" WHERE LOTNAME = ?  ", new Object[]{ lotData.getKey().getLotName() });
		for(Product product : productList){
			ProductServiceProxy.getProductService().selectByKeyForUpdate(product.getKey());
		}
		// End 2019.10.09 Add By Park Jeong Su Mantis 4953
		
        // Added by smkang on 2019.05.21 - According to Jiang Haiying's request, after StartCSTInfoCheckRequest is succeeded, 
		//								   HoldLot/ForceSampling/StartBranch/StartRework/ChangeProductSpec/ChangeProcessFlow/ChangeProcessOperation
		//								   PrepareSort/CancelPrepareSort/ForceOutSampling/CancelBranch/CancelRework/CompleteRework should be rejected.
		if (StringUtils.equals(lotData.getUdfs().get("STARTCHECKRESULT"), "Y"))
			throw new CustomException("LOT-9050", lotName);
		
		String beforeProcessFlowName = lotData.getProcessFlowName();
		String beforeProcessOperationName = lotData.getProcessOperationName();
		
		String reasonCode = "";
		String reasonCodeType = "";
		if ( StringUtil.isNotEmpty(lotData.getReasonCode()) || StringUtil.isNotEmpty(lotData.getReasonCodeType()) )
		{
			reasonCode = lotData.getReasonCode();
			reasonCodeType = lotData.getReasonCodeType();
		}
		// modify by jhiying on20190830 mantis:4646 start
		//EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), reasonCode, reasonCodeType);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), reasonCodeType, reasonCode);	
		if (StringUtil.equals(lotData.getLotProcessState(), "RUN"))
			throw new CustomException("LOT-0008", lotName);

		//20170206 zhongsl validate targetOperationName dismatched with processOperationName
		if(lotData.getProcessOperationName().equals(targetOperationName))
		{
			throw new CustomException("LOT-0217", lotName);
		}
		//20200214 GJJ validate  processOperationName must be nowoperationname
		if(!nowOperationName.isEmpty() && !lotData.getProcessOperationName().equals(nowOperationName))
		{
			setLanguage("Chinese");
			throw new CustomException("OPERACTION-801",lotData.getProcessOperationName() ,nowOperationName);
		}
		
		
		ProcessOperationSpec processOperationSpecData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), targetOperationName);

		eventInfo.setEventName("ChangeOperation");
		Map<String, String> lotUdfs = lotData.getUdfs();
		List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotName);
		//modify by jhiying on20190814 mantis:4566 start
		lotUdfs.put("NOTE", note);
		if(StringUtil.equals(flag, "TRUE")){
		//	lotUdfs.put("NOTE", note);  //modify by jhiying on20190814 mantis:4566 end
			if(StringUtil.equals(eventFlag, "SKIP")){
				eventInfo.setEventName("OperationSkip");
			}else{
				eventInfo.setEventName("OperationBack");
			}
			
		}
		//Operation Changed, Update Product ProcessingInfo to N
		productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productUdfs, "");
		
		String areaName = lotData.getAreaName();
		String factoryName = lotData.getFactoryName();
		String lotHoldState = lotData.getLotHoldState();
		String lotProcessState = lotData.getLotProcessState();
		String lotState = lotData.getLotState();
		String processFlowName = lotData.getProcessFlowName();
		String processFlowVersion = lotData.getProcessFlowVersion();
		String processOperationName = lotData.getProcessOperationName();
		String processOperationVersion = lotData.getProcessOperationVersion();
		String productionType = lotData.getProductionType();
		String productSpec2Name = lotData.getProductSpec2Name();
		String productSpec2Version = lotData.getProductSpec2Version();
		String productSpecName = lotData.getProductSpecName();
		String productSpecVersion = lotData.getProductSpecVersion();
		long priority = lotData.getPriority();
		Timestamp dueDate = lotData.getDueDate();
		double subProductUnitQuantity1 = lotData.getSubProductUnitQuantity1();
		double subProductUnitQuantity2 = lotData.getSubProductUnitQuantity2();
		
		
		// Start 2019.10.09 Add By Park Jeong Su Mantis 4953
		for(Product product : productList){
			if(StringUtils.equals(GenericServiceProxy.getConstantMap().ProductGrade_P, product.getProductGrade())){
				
				product.setProductGrade(GenericServiceProxy.getConstantMap().ProductGrade_G);
				ProductServiceProxy.getProductService().update(product);
				
			}
		}
		// End 2019.10.09 Add By Park Jeong Su Mantis 4953
		
		
		// 151228 by swcho : only single interval tracing from flow to flow
		//2018.11.01 Modify, hsryu, lotData.getProductRequestName -> "", Because WorkOrder of Product must not be changed.
 		ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotName, productionType, productSpecName,
				productSpecVersion, productSpec2Name, productSpec2Version, "", subProductUnitQuantity1, subProductUnitQuantity2,
				dueDate, priority, factoryName, areaName, lotState, lotProcessState, lotHoldState, processFlowName, processFlowVersion,
				processOperationName, processOperationVersion, processFlowName, targetOperationName, "", "", lotData.getNodeStack(), lotUdfs,
				productUdfs, true);
		
		eventInfo.setBehaviorName("ARRAY");

		lotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
		
		eventInfo.setBehaviorName("greenTrack");
		//modify byJHIYING ON20190830 MANTIS:4646 STATR
		lotData.getUdfs().put("NOTE", "Hold by ChangeOperation");
		LotServiceProxy.getLotService().update(lotData);
		//modify byJHIYING ON20190830 MANTIS:4646 STATR
		
		try
		{
			EventInfo eventInfo1 = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
			
			eventInfo1.setEventTime(TimeStampUtil.getCurrentTimestamp());
			eventInfo1.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfo1.setReasonCode("OPER-01");
			eventInfo1.setReasonCodeType(GenericServiceProxy.getConstantMap().REASONCODETYPE_HOLDLOT);
			eventInfo1.setEventName("Hold");
			eventInfo1.setEventComment("Hold by ChangeOperation");
			
			if(!lotData.getLotHoldState().equals("Y"))
			{
				List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
				MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo(productUSequence);
				
				LotServiceProxy.getLotService().makeOnHold(lotData.getKey(), eventInfo1, makeOnHoldInfo);
				
				// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				String condition = "where lotname=?" + " and timekey= ?  " ;
				String condition = "WHERE LOTNAME = ? AND TIMEKEY = ? FOR UPDATE";

				Object[] bindSet = new Object[]{lotData.getKey().getLotName(),eventInfo1.getEventTimeKey()};
				List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
				LotHistory lotHistory = arrayList.get(0);
				
				if(StringUtil.equals(flag, "TRUE")){
					if(StringUtil.equals(eventFlag, "SKIP")){
						lotHistory.getUdfs().put("NOTE", "Hold by OperationSkip");
					}
					else
					{
						lotHistory.getUdfs().put("NOTE", "Hold by OperationBack");
					}
				}
				
				LotServiceProxy.getLotHistoryService().update(lotHistory);

				try {
					lotData = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(lotData.getKey().getLotName(), "OPER-01", " ","AHOLD", eventInfo1);
				} catch (Exception e) {
					eventLog.warn(e);
				}
			}
			else
			{	
				SetEventInfo setEventInfo = new SetEventInfo();
				LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo1, setEventInfo);
				
				if(StringUtil.equals(flag, "TRUE")){
					// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					String condition = "where lotname=?" + " and timekey = ?  " ;
					String condition = "WHERE LOTNAME = ? AND TIMEKEY = ? FOR UPDATE";

					Object[] bindSet = new Object[]{lotData.getKey().getLotName(), eventInfo1.getEventTimeKey()};
					List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
					LotHistory lotHistory = arrayList.get(0);
					
					lotHistory.getUdfs().put("NOTE", "Hold by ChangeOperation");
					if(StringUtil.equals(eventFlag, "SKIP")){
						lotHistory.getUdfs().put("NOTE", "Hold by OperationSkip");
					}
					else
					{
						lotHistory.getUdfs().put("NOTE", "Hold by OperationBack");
					}
					
					LotServiceProxy.getLotHistoryService().update(lotHistory);
				}
				
				try {
					lotData = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(lotData.getKey().getLotName(), "OPER-01", " ","AHOLD", eventInfo1);
				} catch (Exception e) {
					eventLog.warn(e);
				}
			}
		}
		catch (Throwable ex)
		{
			eventLog.error(ex.getMessage());
		}
		
		//Note clear - YJYU
		Lot lotData_Note = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		// Modified by smkang on 2019.05.24 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//		Map<String, String> udfs_note = lotData_Note.getUdfs();
//		udfs_note.put("NOTE", "");
//		LotServiceProxy.getLotService().update(lotData_Note);
		Map<String, String> updateUdfs = new HashMap<String, String>();
		updateUdfs.put("NOTE", "");
		MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData_Note, updateUdfs);
		
		MESLotServiceProxy.getLotServiceUtil().deletePermanentHoldInfo(lotData_Note.getKey().getLotName(), lotData_Note.getFactoryName(), beforeProcessFlowName,beforeProcessOperationName, eventInfo);
		
		return doc;
	}
}