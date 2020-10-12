package kr.co.aim.messolution.durable.event;

import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.AssignCarrierInfo;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;

import org.jdom.Document;
import org.jdom.Element;

public class AssignCarrier extends SyncHandler {

	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{		
		String sDurableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		List<Element> allProductList = SMessageUtil.getBodySequenceItemList(doc, "ALLPRODUCTLIST", true);
		
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);
		
		//2019.01.08_hsryu_Check SortJob!
		CommonValidation.checkExistSortJob(durableData.getFactoryName(), durableData.getKey().getDurableName());
		
		CommonValidation.validateProductMix(allProductList, false, false, false, false);		
		
		for (Element eleLot : lotList) 
		{
			String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
			List<Element> productList = SMessageUtil.getSubSequenceItemList(eleLot, "PRODUCTLIST", true);
			
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			
			if(StringUtil.equals(lotData.getLotProcessState(), "RUN"))
				throw new CustomException("LOT-0013", lotName);
			
			if((int)lotData.getProductQuantity() != productList.size())
				throw new CustomException("PRODUCT-0027", lotName);
			
			List<ProductP> productPSequence = MESLotServiceProxy.getLotInfoUtil().setProductPSequence(productList);
			
			AssignCarrierInfo assignInfo =  MESLotServiceProxy.getLotInfoUtil().assignCarrierInfo(lotData, durableData, productPSequence);
			
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("AssignCarrier", getEventUser(), getEventComment(), "", "");
			
			MESLotServiceProxy.getLotServiceImpl().assignCarrier(lotData, assignInfo, eventInfo);
			
		}
		
		return doc;
	}
}
