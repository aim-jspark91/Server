package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.productspec.management.info.ChangeSpecInfo;

import org.jdom.Document;
import org.jdom.Element;

public class SetProductSpec extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		List<Element> productSpecList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTSPECLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Change", getEventUser(), getEventComment(), null, null);
		
		for(Element eleProductSpecData : productSpecList)
		{
			String factoryName = SMessageUtil.getChildText(eleProductSpecData, "FACTORYNAME", true);
			String productSpecName = SMessageUtil.getChildText(eleProductSpecData, "PRODUCTSPECNAME", true);
			String productSpecVersion = SMessageUtil.getChildText(eleProductSpecData, "PRODUCTSPECVERSION", true);
			String dspFlag = SMessageUtil.getChildText(eleProductSpecData, "DSPFLAG", false);

			ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, productSpecName, productSpecVersion);
			
			productSpecData.getUdfs().put("DSPFLAG", dspFlag);
			
			ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();
			changeSpecInfo.setDescription(productSpecData.getDescription());
			changeSpecInfo.setProductionType(productSpecData.getProductionType());
			changeSpecInfo.setProductType(productSpecData.getProductType());
			changeSpecInfo.setProductQuantity(productSpecData.getProductQuantity());
			changeSpecInfo.setSubProductType(productSpecData.getSubProductType());
			changeSpecInfo.setSubProductUnitQuantity1(productSpecData.getSubProductUnitQuantity1());
			changeSpecInfo.setSubProductUnitQuantity2(productSpecData.getSubProductUnitQuantity2());
			changeSpecInfo.setProcessFlowName(productSpecData.getProcessFlowName());
			changeSpecInfo.setProcessFlowVersion(productSpecData.getProcessFlowVersion());
			changeSpecInfo.setEstimatedCycleTime(productSpecData.getEstimatedCycleTime());
			changeSpecInfo.setMultiProductSpecType(productSpecData.getMultiProductSpecType());
			changeSpecInfo.setProductSpec2Name(productSpecData.getProductSpec2Name());
			changeSpecInfo.setProductSpec2Version(productSpecData.getProductSpec2Version());
			
			changeSpecInfo.setUdfs(productSpecData.getUdfs());
			
			ProductServiceProxy.getProductSpecService().checkOut(productSpecData.getKey(), getEventUser(), getEventComment());
			ProductServiceProxy.getProductSpecService().changeSpec(productSpecData.getKey(), changeSpecInfo, getEventUser(), getEventComment());
			ProductServiceProxy.getProductSpecService().checkIn(productSpecData.getKey(), getEventUser(), getEventComment());
		}
		return doc;
	}

}
