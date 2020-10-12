package kr.co.aim.messolution.durable.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.RelocateProductsInfo;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ChangeCSTMap extends SyncHandler {

	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		String messageName = SMessageUtil.getMessageName(doc);
		String sFactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String sDurableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		String sLotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		
		//GetDurableData
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);
		

/*		if(!StringUtils.equals(durableData.getFactoryName(), System.getProperty("shop")))
		{
			throw new CustomException("CST-0039", sDurableName, durableData.getFactoryName());
		}*/
		
		//2019.01.08_hsryu_Check SortJob!
		CommonValidation.checkExistSortJob(durableData.getFactoryName(),sDurableName);
		

		Element eleBody;
		try {
			eleBody = XmlUtil.getNode(doc, new StringBuilder("//").append(SMessageUtil.Message_Tag)
													.append("/").append(SMessageUtil.Body_Tag)
													.toString());
		} catch (Exception e) {
			throw new CustomException("SYS-0001", SMessageUtil.Body_Tag);
		}
		
		if (eleBody != null)
		{
			List<ProductP> productPSequence = new ArrayList<ProductP>();
			
			for (Element eleCarrier : XmlUtil.getChildren(eleBody, "PRODUCTLIST", false))
			{
				String sProductName = SMessageUtil.getChildText(eleCarrier, "PRODUCTNAME", true);
				String sPosition = SMessageUtil.getChildText(eleCarrier, "POSITION", true);				
				
				ProductP productP = new ProductP();
				productP.setProductName(sProductName);
				productP.setPosition(Long.valueOf(sPosition));

				Product productData = CommonUtil.getProductData(sProductName);
				
				Map<String, String> productUdfs = productData.getUdfs();
				productP.setUdfs(productUdfs);

				productPSequence.add(productP);
			}
			
			Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(sLotName);
			
			RelocateProductsInfo relocateInfo = 
				MESLotServiceProxy.getLotInfoUtil().relocateProductsInfo(lotData, productPSequence, lotData.getProductQuantity());
			
			EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, getEventUser(), getEventComment(), "", "");
			
			MESLotServiceProxy.getLotServiceImpl().relocateProducts(lotData, relocateInfo, eventInfo);
		}

		return doc;
	}

}
