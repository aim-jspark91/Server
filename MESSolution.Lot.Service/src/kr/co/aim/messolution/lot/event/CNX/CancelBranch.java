package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CancelBranch extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String lotName 			   = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String returnFlowName 	   = SMessageUtil.getBodyItemValue(doc, "RETURNFLOWNAME", true);
		String returnOperationName = SMessageUtil.getBodyItemValue(doc, "RETURNOPERATION", true);
		String xNodeStack = SMessageUtil.getBodyItemValue(doc, "NODESTACK", false);// 20200323 add by GJJ mantis :5852 卡控nodestack与Server一致

		
		// Modified by smkang on 2019.06.17 - According to Liu Hongwei's request, Lot should be locked to be prevented concurrent executing.
//		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		eventLog.debug("Lot will be locked to be prevented concurrent executing.");
		Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
		eventLog.debug("Lot is locked to be prevented concurrent executing.");
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelBranch", getEventUser(), getEventComment(), "", "");
		
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
		
		
		
   		String beforeProcessFlowName = lotData.getProcessFlowName();
   		String beforeProcessOperationName = lotData.getProcessOperationName();

		if(!StringUtil.equals(MESLotServiceProxy.getLotServiceUtil().returnFlowType(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion()).toUpperCase(), "BRANCH"))
		{
			throw new CustomException("BRANCH-0001", lotData.getKey().getLotName()); 
		}
		
		// 2018.02.19 hsryu - Check First Operation in ReworkFlow
		if(CommonValidation.checkFirstOperation(lotData, "BRANCH"))
		{
			throw new CustomException("LOT-9012", lotData.getProcessOperationName()); 
		}
		
		// Check LotProcessState
		if(StringUtil.equals(lotData.getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Run))
		{
			throw new CustomException("LOT-0008", lotData.getKey().getLotName());
		}
		

		
		String tempNodeStack = lotData.getNodeStack();
		String[] arrNodeStack = StringUtil.split(tempNodeStack, ".");
		int count = arrNodeStack.length;
		String nextNodeID = "";
		String nodeStack = "";

		nextNodeID = MESLotServiceProxy.getLotServiceUtil().GetReturnAfterNodeStackForSampling(lotData.getFactoryName(), arrNodeStack[count-2]);

		for(int i=0; i<arrNodeStack.length-2; i++)
		{
			nodeStack += arrNodeStack[i] + "."; 
		}
		
		nodeStack += nextNodeID;

		List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotData.getKey().getLotName());
		productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productUdfs, "");

		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();

		if(StringUtil.equals(lotData.getLotProcessState(), "WAIT"))
		{
			changeSpecInfo.setAreaName(lotData.getAreaName());
			changeSpecInfo.setDueDate(lotData.getDueDate());
			changeSpecInfo.setFactoryName(lotData.getFactoryName());
			changeSpecInfo.setLotHoldState(lotData.getLotHoldState());
			changeSpecInfo.setLotProcessState(lotData.getLotProcessState());
			changeSpecInfo.setLotState(lotData.getLotState());
			changeSpecInfo.setPriority(lotData.getPriority());
			changeSpecInfo.setProcessFlowName(returnFlowName);
			changeSpecInfo.setProcessFlowVersion("00001");
			changeSpecInfo.setProcessOperationName(returnOperationName);
			changeSpecInfo.setProcessOperationVersion("00001");
			changeSpecInfo.setProductionType(lotData.getProductionType());
			changeSpecInfo.setProductSpec2Name(lotData.getProductSpec2Name());
			changeSpecInfo.setProductSpec2Version(lotData.getProductSpec2Version());
			changeSpecInfo.setProductSpecName(lotData.getProductSpecName());
			changeSpecInfo.setProductSpecVersion(lotData.getProductSpecVersion());
			//changeSpecInfo.setProductUdfs(productUdfs);
			changeSpecInfo.setSubProductUnitQuantity1(lotData.getSubProductUnitQuantity1());
			changeSpecInfo.setSubProductUnitQuantity2(lotData.getSubProductUnitQuantity2());
			changeSpecInfo.setNodeStack(nodeStack);
		}
		else
		{
			throw new CustomException("LOT-9003", lotName+"(LotProcessState:"+lotData.getLotProcessState()+")");
		}

		eventInfo.setBehaviorName("ARRAY");

		lotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo);

		Lot afterLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
		
		// 2019.05.30_hsryu_Modify Logic. if TrackOut and Sampling, Not Execute BHold. So, Check Sampling and BHold at the same time.
		boolean isCheckSampling = false;
		isCheckSampling = MESLotServiceProxy.getLotServiceUtil().checkSampling(lotName,eventInfo);
		
		if (!MESLotServiceProxy.getLotServiceUtil().isExistBholdorOperHold(lotName, eventInfo) && !isCheckSampling)
			afterLotData = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionChange(afterLotData.getKey().getLotName(), eventInfo);

		MESLotServiceProxy.getLotServiceUtil().deletePermanentHoldInfo(afterLotData.getKey().getLotName(), afterLotData.getFactoryName(), beforeProcessFlowName,beforeProcessOperationName, eventInfo);

		return doc;
	}
}