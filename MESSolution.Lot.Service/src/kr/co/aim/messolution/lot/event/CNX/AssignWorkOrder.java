package kr.co.aim.messolution.lot.event.CNX;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.ProcessOperationSpecServiceProxy;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpecKey;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequestplan.ProductRequestPlanServiceProxy;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlan;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlanKey;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class AssignWorkOrder extends SyncHandler {

	/**
	 * 151106 by xzquan : Create AssignWorkOrder
	 */
	@Override
	public Object doWorks(Document doc) throws CustomException {
		String sFactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String sProductRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "ASSIGNEDMACHINENAME", true);
		String sPlanReleasedTime = SMessageUtil.getBodyItemValue(doc, "PLANRELEASEDTIME", true);
		//String sPortName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		//String sChkAuto = SMessageUtil.getBodyItemValue(doc, "CHKAUTO", true);
		// String sDurableSpec = SMessageUtil.getBodyItemValue(doc,
		// "DURABLESPECNAME", true);
		Element eLotList = SMessageUtil.getBodySequenceItem(doc, "LOTLIST", true);
		ProductSpec mainProductSpec ;

		// Product Request Key & Data
		ProductRequestKey pKey = new ProductRequestKey();
		pKey.setProductRequestName(sProductRequestName);
		ProductRequest pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);			

		// Get Process Flow From Work Order Data
		String sProcessFlowName = pData.getUdfs().get("processFlowName");
		
		// Product Request Plan Key & Data
		ProductRequestPlanKey pPlanKey = new ProductRequestPlanKey();
		pPlanKey.setProductRequestName(sProductRequestName);
		pPlanKey.setAssignedMachineName(sMachineName);
		pPlanKey.setPlanReleasedTime(TimeUtils.getTimestamp(sPlanReleasedTime));
		ProductRequestPlan pPlanData = ProductRequestPlanServiceProxy.getProductRequestPlanService()
				.selectByKey(pPlanKey);

		// Product Spec Data
		ProductSpec specData = GenericServiceProxy.getSpecUtil().getProductSpec(sFactoryName,
				pData.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
		
		try 
		{
			ProductRequest mainPData = CommonUtil.getProductRequestData(pData.getUdfs().get("mainWorkOrderName"));
			
			mainProductSpec = GenericServiceProxy.getSpecUtil().getProductSpec(sFactoryName, 
					mainPData.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
			
		} catch (Exception e) 
		{
			mainProductSpec = specData;
		}		

		// Machine Data
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sMachineName);

		// 1. Velidation
		// 1) Check Product Request Plan Hold State
		if (pPlanData.getProductRequestPlanHoldState().equals(GenericServiceProxy.getConstantMap().Prq_OnHold)) {
			throw new CustomException("PRODUCTREQUEST-0001", sProductRequestName);
		}

		// 2) Chek Product Request Plan State
		if (pPlanData.getProductRequestPlanState().equals(GenericServiceProxy.getConstantMap().PrqPlan_Completed)) {
			throw new CustomException("PRODUCTREQUEST-0007", sProductRequestName, sMachineName, sPlanReleasedTime);
		}
		
		// 3) Check Product Qty & Plan Remain Qty
		int lotProductQty = 0;
		for (@SuppressWarnings("rawtypes")
		Iterator iLot = eLotList.getChildren().iterator(); iLot.hasNext();) {

			Element eLot = (Element) iLot.next();
			String sLotName = SMessageUtil.getChildText(eLot, "LOTNAME", true);
			
			LotKey lotKey = new LotKey(sLotName);
			Lot lotData = LotServiceProxy.getLotService().selectByKey(lotKey);
			
			//2017-01-07 by zhanghao  Check Lot DetailProcessOperationType 
			ProcessOperationSpecKey poKey = new ProcessOperationSpecKey(lotData.getFactoryName(), lotData.getProcessOperationName(),lotData.getProcessOperationVersion());
			ProcessOperationSpec poSpec = ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(poKey);
			
			if(!poSpec.getDetailProcessOperationType().equals("BANK"))
			{
				throw new CustomException("PRODUCTREQUEST-0039", "");
			}
			
			lotProductQty += lotData.getProductQuantity();
		}
		
		if(pPlanData.getPlanQuantity() - pPlanData.getReleasedQuantity() < lotProductQty)
		{
			throw new CustomException("PRODUCTREQUEST-0014", "");
		}

		// Event Info
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Release", getEventUser(), getEventComment(), "", "");

		// Common Info
		String sProductSpec = specData.getKey().getProductSpecName();
		ProcessFlowKey pfKey = new ProcessFlowKey(sFactoryName, sProcessFlowName, "00001");

		String currentNodeStack = ProcessFlowServiceProxy.getProcessFlowService().getStartNode(pfKey).getKey()
				.getNodeId(); // start
		Node targetNode = ProcessFlowServiceProxy.getProcessFlowService()
				.getNextNode(currentNodeStack, "Normal", ""); 
		String nodeId =  targetNode.getKey().getNodeId();//firstNode
		String processOperationName = targetNode.getNodeAttribute1(); // firstOper

		for (@SuppressWarnings("rawtypes")
		Iterator iLot = eLotList.getChildren().iterator(); iLot.hasNext();) {

			Element eLot = (Element) iLot.next();
			String sLotName = SMessageUtil.getChildText(eLot, "LOTNAME", true);	
			
			LotKey lotKey = new LotKey(sLotName);
			Lot lotData = LotServiceProxy.getLotService().selectByKey(lotKey);
									
			if (StringUtils.equals(sFactoryName, "OLED") && StringUtils.equals(specData.getMultiProductSpecType(), "Material") 
					&& !StringUtils.equals(mainProductSpec.getUdfs().get("PRODUCTCODE"), sLotName.substring(1, 4))) 
			{
				lotData = MESLotServiceProxy.getLotServiceUtil().recreateAndCreateAllProducts(sLotName,specData, mainProductSpec, eventInfo);
				sLotName = lotData.getKey().getLotName();
			}
			
			// Revert Event Info
			eventInfo.setEventName("Release");

			//GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);

			// 1. Release Lot
			// 1.1) get lot Data by Lot Name
			//LotKey lotKey = new LotKey(sLotName);
			//Lot lotData = LotServiceProxy.getLotService().selectByKey(lotKey);
			
			// Assigned Lot can not Assign Again
			if(sProductRequestName.equals(lotData.getProductRequestName()))
			{
				throw new CustomException("PRODUCTREQUEST-0028", sProductRequestName);
			}

			//2. Release Lot
			List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(sLotName);
			
			ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(sLotName,pData.getProductRequestType(),
												 sProductSpec, "00001", "", "",
												sProductRequestName, lotData.getSubProductUnitQuantity1(), lotData.getSubProductQuantity2(), lotData.getDueDate(), lotData.getPriority(),
												sFactoryName, lotData.getAreaName(), lotData.getLotState(),
												lotData.getLotProcessState(), lotData.getLotHoldState(),
												sProcessFlowName, "00001", processOperationName, "00001",
												"", "", "", "", "",
												lotData.getUdfs(), productUdfs,
												false);
			
			//changeSpecInfo.setProcessOperationName(targetOperationData.getKey().getProcessOperationName());
			
			//Lot aLot = MESLotServiceProxy.getLotServiceImpl().receiveLot(eventInfo, lotData, changeSpecInfo);
			Lot aLot = MESLotServiceProxy.getLotServiceImpl().changeProductSpec(eventInfo, lotData, changeSpecInfo);
			// Release Lot
//			List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(sLotName);
//
//			MakeReceivedInfo makeReceivedInfo = MESLotServiceProxy.getLotInfoUtil().makeReceivedInfo(lotData,
//					lotData.getAreaName(), nodeId, sProcessFlowName, "00001", processOperationName, "00001",
//					pData.getProductRequestType(), sProductRequestName, "", "", sProductSpec, "00001",
//					lotData.getProductType(), productUdfs, lotData.getSubProductType());
//
//			Lot aLot = MESLotServiceProxy.getLotServiceImpl().receiveLot(eventInfo, lotData, makeReceivedInfo);

			// 2. Change Durable Spec
			String carrierName = aLot.getCarrierName();
			DurableKey cKey = new DurableKey(carrierName);
			
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			Durable carrierData = DurableServiceProxy.getDurableService().selectByKey(cKey);
			Durable carrierData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(cKey);
			
//			// 2.1 Check Exsist Durable Spec
//			DurableSpecKey cSpecKey = new DurableSpecKey();
//			cSpecKey.setFactoryName(sFactoryName);
//			cSpecKey.setDurableSpecName(carrierData.getDurableSpecName());
//			cSpecKey.setDurableSpecVersion("00001");
//			
//			try {
//				DurableServiceProxy.getDurableSpecService().selectByKey(cSpecKey);
//			} catch (Exception ex) {
//				throw new CustomException("CST-8001", cSpecKey.getDurableSpecName(), cSpecKey.getDurableSpecVersion(), cSpecKey.getFactoryName());
//			}
			
			// 2.2 Update New Durable Spec
			carrierData.setFactoryName(sFactoryName);
			DurableServiceProxy.getDurableService().update(carrierData);
			
			// 2.3 Set Event
			SetEventInfo setEventInfo = new SetEventInfo();
			Map<String, String> udfs = carrierData.getUdfs();
			setEventInfo.setUdfs(udfs);
			eventInfo.setEventName("ChangeSpec");
			DurableServiceProxy.getDurableService().setEvent(cKey, eventInfo, setEventInfo);

			// Revert Event Info
			eventInfo.setEventName("Release");

			// 3. increment Product Request
			try
			{
				ProductRequestPlan planData = ProductRequestPlanServiceProxy.getProductRequestPlanService().selectByKey(pPlanKey); 
				MESWorkOrderServiceProxy.getProductRequestServiceUtil().calculateProductRequestQty(null, planData, "R", (long)lotData.getProductQuantity(), eventInfo);
			}
			catch(Exception e)
			{
				eventLog.error("incrementWorkOrderReleaseQty Failed");
			}

			// 4. Matching Lot Name & W/O Plan(CT_LotPlan) to Released
			try {
				ReserveLot reserveData = new ReserveLot(sMachineName, sLotName);
				reserveData.setReserveState(GenericServiceProxy.getConstantMap().RESV_STATE_END);
				reserveData.setMachineName(sMachineName);
				reserveData.setProductSpecName(specData.getKey().getProductSpecName());
				reserveData.setReserveTimeKey(eventInfo.getEventTimeKey());
				reserveData.setProductRequestName(sProductRequestName);
				reserveData.setPlanReleasedTime(pPlanKey.getPlanReleasedTime());
				reserveData.setReserveUser(eventInfo.getEventUser());

				reserveData = ExtendedObjectProxy.getReserveLotService().create(eventInfo, reserveData);

				//GenericServiceProxy.getTxDataSourceManager().commitTransaction();
			} catch (Exception ex) {
				eventLog.warn(String.format("Lot[%s] is failed to Macthing Plan", sLotName));
				//2017-03-09 wuzhiming add：添加事务处理，发生异常后一定要rollBack
				//GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			}

			// 5. Auto Track In PI
//			if(StringUtil.equals(sChkAuto, "Y") && sChkAuto != null)
//			{
//				Document AutoTrackInDoc = writeTrackInRequest(doc, sLotName, sMachineName, sPortName);
//				String replySubject = GenericServiceProxy.getESBServive().getSendSubject("CNXsvr");
//				GenericServiceProxy.getESBServive().sendBySender(replySubject, AutoTrackInDoc, "LocalSender");
//			}
		}

		return doc;
	}

	private Document writeTrackInRequest(Document doc, String lotName, String machineName, String portName)
			throws CustomException {
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "AutoTrackInLot");
		boolean result = doc.getRootElement().removeChild(SMessageUtil.Body_Tag);

		Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);

		Element element1 = new Element("LOTNAME");
		element1.setText(lotName);
		eleBodyTemp.addContent(element1);

		Element element2 = new Element("MACHINENAME");
		element2.setText(machineName);
		eleBodyTemp.addContent(element2);

		Element element3 = new Element("PORTNAME");
		element3.setText(portName);
		eleBodyTemp.addContent(element3);

		// overwrite
		doc.getRootElement().addContent(eleBodyTemp);

		return doc;
	}		
}
