package kr.co.aim.messolution.lot.event.CNX;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.productrequest.service.ProductRequestInfoUtil;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.data.ProductSpecKey;

import org.jdom.Document;

public class CreateECCode extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String ecCode = SMessageUtil.getBodyItemValue(doc, "ECCODE", true);
		
		//GetProductSpecData
		ProductSpecKey prdSpecKey = new ProductSpecKey();
		prdSpecKey.setFactoryName(factoryName);
		prdSpecKey.setProductSpecName(productSpecName);
		prdSpecKey.setProductSpecVersion(productSpecVersion);
						
		ProductSpec prdSpecData = ProductServiceProxy.getProductSpecService().selectByKey(prdSpecKey);
		
		ProductRequestInfoUtil.insertECCode(factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, ecCode);
		
		return doc;
	}

}
