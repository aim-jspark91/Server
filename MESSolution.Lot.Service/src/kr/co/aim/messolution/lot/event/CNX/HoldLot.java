package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LotAction;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class HoldLot extends SyncHandler 
{
	public Object doWorks(Document doc) throws CustomException 
	{
		// ----------------------------------------------------------------------------------------------------------------------------------
		// Added by smkang on 2019.07.11 - According to Liu Hongwei's request, Lot should be locked to be prevented concurrent executing.
		List<Element> eleLotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);		
		
		eventLog.debug("Lot will be locked to be prevented concurrent executing.");
		Map<String, Lot> lotDataMap = new ConcurrentHashMap<String, Lot>();
		for (Element eleLot : eleLotList) {
			String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
			lotDataMap.put(lotName, LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName)));
		}
		eventLog.debug("Lot is locked to be prevented concurrent executing.");
		// ----------------------------------------------------------------------------------------------------------------------------------
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Hold", getEventUser(), "Hold Lot", "", "");

		String department = SMessageUtil.getBodyItemValue(doc, "USERDEPARTMENT", true);
		String note = SMessageUtil.getBodyItemValue(doc, "NOTE", true);

		for (Element eleLot : eleLotList) {
			String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
			String reasonCode = SMessageUtil.getChildText(eleLot, "REASONCODE", true);		
			String reasonCodeType = SMessageUtil.getChildText(eleLot, "REASONCODETYPE", true);	
			eventInfo.setReasonCode(reasonCode);
			eventInfo.setReasonCodeType(reasonCodeType);
			
			// Modified by smkang on 2019.07.11 - According to Liu Hongwei's request, Lot should be locked to be prevented concurrent executing.
//			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			Lot lotData = lotDataMap.get(lotName);
			
            // Added by smkang on 2019.05.21 - According to Jiang Haiying's request, after StartCSTInfoCheckRequest is succeeded, 
			//								   HoldLot/ForceSampling/StartBranch/StartRework/ChangeProductSpec/ChangeProcessFlow/ChangeProcessOperation
			//								   PrepareSort/CancelPrepareSort/ForceOutSampling/CancelBranch/CancelRework/CompleteRework should be rejected.
			if (StringUtils.equals(lotData.getUdfs().get("STARTCHECKRESULT"), "Y"))
				throw new CustomException("LOT-9050", lotName);
			
			// if Lot In Sort ProcessFlow Throw Error Add By Park Jeong su
			ProcessFlowKey processFlowKey = new ProcessFlowKey(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion());
			ProcessFlow processFlow = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
			if (StringUtils.equals("Sort", processFlow.getProcessFlowType()))
			{
				throw new CustomException("COMMON-0001", "it is not possible to do lot hold or reserve lot hold in sorter flow.");
			}
			
			//2019.01.28_hsryu_Modify Logic. Mantis 0002608.
//				if (!lotData.getLotState().equals(GenericServiceProxy.getConstantMap().Lot_Released))
//				{
//					throw new CustomException("LOT-0016", lotName, lotData.getLotState());
//				}
			
			List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
			
			if (StringUtil.equals(lotData.getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Run)) 
			{
				eventInfo.setEventName("FutureAHold");
				
				long lastPosition = 0;
				List<LotAction> lotActionList = new ArrayList<LotAction>();
				
				try
				{
					String condition = "WHERE LOTNAME = ?";
					condition += " AND PROCESSFLOWNAME = ?";
					condition += " AND PROCESSFLOWVERSION = ?";
					condition += " AND PROCESSOPERATIONNAME = ?";
					condition += " AND PROCESSOPERATIONVERSION = ?";
					condition += " ORDER BY POSITION DESC";
					
					Object[] bindSet = new Object[5];
					bindSet[0] = lotName;
					bindSet[1] = lotData.getProcessFlowName();
					bindSet[2] = lotData.getProcessFlowVersion();
					bindSet[3] = lotData.getProcessOperationName();
					bindSet[4] = lotData.getProcessOperationVersion();
					
					lotActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);
				}
				catch(greenFrameDBErrorSignal ne)
				{
					//not exist.. start Create AHOLD.s
				}			
				
				if(lotActionList.size()>0)
				{
					lastPosition = lotActionList.get(0).getPosition();
				}
				
				LotAction lotAction = new LotAction();
				//PK
				lotAction.setLotName(lotData.getKey().getLotName());
				lotAction.setFactoryName(lotData.getFactoryName());
				lotAction.setProcessFlowName(lotData.getProcessFlowName());
				lotAction.setProcessFlowVersion(lotData.getProcessFlowVersion());
				lotAction.setProcessOperationName(lotData.getProcessOperationName());
				lotAction.setProcessOperationVersion(lotData.getProcessOperationVersion());
				lotAction.setPosition(lastPosition+1);
				//Columns
				lotAction.setActionName(GenericServiceProxy.getConstantMap().ACTIONNAME_HOLD);
				lotAction.setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_CREATED);
				lotAction.setHoldCode(reasonCode);
				lotAction.setHoldType(GenericServiceProxy.getConstantMap().HOLDTYPE_AHOLD);
				lotAction.setHoldPermanentFlag("N");
				lotAction.setLastEventName(eventInfo.getEventName());
				lotAction.setLastEventTime(eventInfo.getEventTime());
				lotAction.setLastEventTimeKey(eventInfo.getEventTimeKey());
				lotAction.setLastEventUser(eventInfo.getEventUser());
				lotAction.setLastEventComment(note);
				lotAction.setDepartment(department);
				
				EventInfo eventInfoLotAction = EventInfoUtil.makeEventInfo("Hold", getEventUser(), getEventComment(), "", "");
				ExtendedObjectProxy.getLotActionService().create(eventInfoLotAction, lotAction);
			}
			else
			{
				// -------------------------------------------------------------------------------------------------------------------------------------------
				// Modified by smkang on 2018.08.13 - According to user's requirement, LotName/ReasonCode/Department/EventComment are necessary to be keys.
//					Map<String,String> multiHoldudfs = new HashMap<String, String>();
//					multiHoldudfs.put("eventuserdep", department);
//					
//					LotMultiHoldKey multiholdkey = new LotMultiHoldKey();
//					multiholdkey.setLotName(lotName);
//					multiholdkey.setReasonCode(reasonCode);
//					
//					LotMultiHold multihold = LotServiceProxy.getLotMultiHoldService().selectByKey(multiholdkey);
//					multihold.setUdfs(multiHoldudfs);
//							
//					LotServiceProxy.getLotMultiHoldService().update(multihold);
				try {
					EventInfo holdEventInfo = EventInfoUtil.makeEventInfo("Hold", eventInfo.getEventUser(), "Hold Lot", 
							GenericServiceProxy.getConstantMap().REASONCODETYPE_HOLDLOT, "HD-0001");
					holdEventInfo.setEventTime(eventInfo.getEventTime());
					holdEventInfo.setEventTimeKey(eventInfo.getEventTimeKey());
					holdEventInfo.setEventComment(note);
					
					lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
					
//						String holdDepartment = MESLotServiceProxy.getLotServiceImpl().getHoldDepartment(lotData.getKey().getLotName());
//						udfs.put("HOLDDEPARTMENT", holdDepartment);
					
					Map<String, String> udfs = new HashMap<String, String>();
					udfs.put("NOTE", note);
					
					MakeOnHoldInfo makeOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeOnHoldInfo(productUSequence, udfs);
					LotServiceProxy.getLotService().makeOnHold(lotData.getKey(), holdEventInfo, makeOnHoldInfo);
					
					lotData = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(lotName, reasonCode, department,"BHOLD", holdEventInfo);
					
					// Modified by smkang on 2019.05.24 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//						//Note clear - YJYU
//						Lot lotData_Note = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//						Map<String, String> udfs_note = lotData_Note.getUdfs();
//						udfs_note.put("NOTE", "");
//						LotServiceProxy.getLotService().update(lotData_Note);
					Map<String, String> updateUdfs = new HashMap<String, String>();
					updateUdfs.put("NOTE", "");
					MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
				} catch (Exception e) {
					eventLog.warn(e);
				}
				// -------------------------------------------------------------------------------------------------------------------------------------------
			}	
		}

		return doc;
	}
}