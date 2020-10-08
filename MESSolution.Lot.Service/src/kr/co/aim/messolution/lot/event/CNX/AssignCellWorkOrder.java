package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.List;

import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;

import org.jdom.Document;
import org.jdom.Element;

public class AssignCellWorkOrder extends SyncHandler 
{
	public Object doWorks(Document doc) throws CustomException 
	{
		// parsing variable
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Release", getEventUser(), getEventComment(), "", "");
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		List<Element> eleLotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		int releaseQuantity = 0;
		ProductRequest productRequest = MESWorkOrderServiceProxy.getProductRequestInfoUtil().getProductRequest(productRequestName);
		int i=0;
		// validation
		
		for (Element eleLot : eleLotList) 
		{
			
			String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			releaseQuantity += (int)lotData.getProductQuantity();
		}
		
		if(releaseQuantity > productRequest.getPlanQuantity()-productRequest.getReleasedQuantity())
		{
			throw new CustomException("PRODUCTREQUEST-0049", productRequest.getKey().getProductRequestName());
		}
		
		
		// Before ChangeSpec Lot & Assign WorkOrder
		
		
		
		// ChangeSpec Lot & Assign WorkOrder
		
		for (Element eleLot : eleLotList) 
		{
			
			String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			
			ProcessOperationSpec nextOperationData = CommonUtil.getNextOperation(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessOperationName());
			
			List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotName);
			productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productUdfs, "");
			
			String areaName = lotData.getAreaName();
			String factoryName = lotData.getFactoryName();
			String lotHoldState = lotData.getLotHoldState();
			String lotProcessState = GenericServiceProxy.getConstantMap().Lot_Wait;
			String lotState = GenericServiceProxy.getConstantMap().Lot_Released;
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
			
	 		ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotName, productionType, productSpecName,
					productSpecVersion, productSpec2Name, productSpec2Version, "", subProductUnitQuantity1, subProductUnitQuantity2,
					dueDate, priority, factoryName, areaName, lotState, lotProcessState, lotHoldState, processFlowName, processFlowVersion,
					processOperationName, processOperationVersion, processFlowName, nextOperationData.getKey().getProcessOperationName(), "", "", lotData.getNodeStack(), lotData.getUdfs(),
					productUdfs, true);
			
			

			lotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
			
		}
		
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().calculateProductRequestQty(productRequestName, null, "R", releaseQuantity, eventInfo);
		
		// After ChangeSpec Lot & Assign WorkOrder
		
		
		
		return doc;
		
	}
}
