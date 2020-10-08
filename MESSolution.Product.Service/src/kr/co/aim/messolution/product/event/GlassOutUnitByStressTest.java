package kr.co.aim.messolution.product.event;

import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;
import kr.co.aim.greentrack.product.management.data.Product;

import org.jdom.Document;

public class GlassOutUnitByStressTest extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		/*
		MACHINENAME 
		UNITNAME
		SUBUNITNAME
		LOTNAME
		PRODUCTNAME
		PRODUCTJUDGE
		PRODUCTGRADE
		FROMSLOTID
		TOSLOTID
		*/
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String productGrade = SMessageUtil.getBodyItemValue(doc, "PRODUCTJUDGE", false);
		//String fromSlot = SMessageUtil.getBodyItemValue(doc, "FROMSLOTID", false);
		//String toSlot = SMessageUtil.getBodyItemValue(doc, "TOSLOTID", false);
		
		Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnitOut", getEventUser(), getEventComment(), null, null);
		
		Map<String, String> udfs = CommonUtil.setNamedValueSequence(SMessageUtil.getBodyElement(doc), Product.class.getSimpleName());
		
		SetMaterialLocationInfo setMaterialLocationInfo;
		
		if(subUnitName.isEmpty())
		{
			unitName = "";
			setMaterialLocationInfo = MESProductServiceProxy.getProductInfoUtil().setMaterialLocationInfo(productData, unitName, udfs);
		}
		else
		{
			setMaterialLocationInfo = MESProductServiceProxy.getProductInfoUtil().setMaterialLocationInfo(productData, subUnitName, udfs);
		}
		
		MESProductServiceProxy.getProductServiceImpl().setMaterialLocation(eventInfo, productData, setMaterialLocationInfo);
		
		return doc;
	}
}
