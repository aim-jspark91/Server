package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.LotService;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.impl.LotServiceImpl;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;

import org.jdom.Document;
import org.jdom.Element;

public class CommonBank extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("MoveBank", getEventUser(), getEventComment(), "", "");
		
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);		
		
		//get release qty
		for (Element eLotData : lotList) 
		{
			String lotName = eLotData.getChild("LOTNAME").getText();
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			
			CommonValidation.checkLotState(lotData);
			CommonValidation.checkLotProcessState(lotData);
			CommonValidation.checkLotHoldState(lotData);
			CommonValidation.checkProductGradeN(lotData);
			
			List<Product> productList = ProductServiceProxy.getProductService().select(" WHERE LOTNAME = ?  ", new Object[]{ lotData.getKey().getLotName() });
			for(Product product : productList){
				ProductServiceProxy.getProductService().selectByKeyForUpdate(product.getKey());
			}
			
			Map<String, String> lotUdfs = lotData.getUdfs();
			List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotName);
			
			productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productUdfs, "");
			
			String areaName = lotData.getAreaName();
			String lotHoldState = lotData.getLotHoldState();
			String lotProcessState = lotData.getLotProcessState();
			String lotState = lotData.getLotState();
			String processFlowName = lotData.getProcessFlowName();
			String processFlowVersion = lotData.getProcessFlowVersion();
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
					lotData.getProcessOperationName(), processOperationVersion, processFlowName, processOperationName, "", "", lotData.getNodeStack(), lotUdfs,
					productUdfs, true);
	       
			lotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo);	
			
		}
		return doc;
	}
	
}
