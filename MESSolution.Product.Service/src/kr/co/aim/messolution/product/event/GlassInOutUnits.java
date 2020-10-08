package kr.co.aim.messolution.product.event;

import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;
import kr.co.aim.greentrack.product.management.data.Product;

import org.jdom.Document;
import org.jdom.Element;

public class GlassInOutUnits extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		/*
		MACHINENAME 
		UNITLIST
		UNIT
		UNITNAME
		SUBUNITNAME
		LOTNAME
		PRODUCTNAME
		PRODUCTJUDGE
		PRODUCTGRADE
		PRODUCTRECIPE
		UNITINTIME
		UNITOUTTIME
		*/
		
		//String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		
		Element eleBody = SMessageUtil.getBodyElement(doc);
		if (eleBody != null) {
			for (Element eleUnit : SMessageUtil.getBodySequenceItemList(doc, "UNITLIST", false))

			{
				String unitName = SMessageUtil.getChildText(eleUnit, "UNITNAME", true);
				String subUnitName = SMessageUtil.getChildText(eleUnit, "SUBUNITNAME", false);
				String productName = SMessageUtil.getChildText(eleUnit, "PRODUCTNAME", true);
				String unitInTime = SMessageUtil.getChildText(eleUnit, "UNITINTIME", true);
				String unitOutTime = SMessageUtil.getChildText(eleUnit, "UNITOUTTIME", true);

				Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
				
				EventInfo eventInfo = new EventInfo();
				SetMaterialLocationInfo setMaterialLocationInfo;
				
				if(!unitInTime.isEmpty())
				{
					String eventComment = "UnitInTime : [" + unitInTime + "]";
					eventInfo = EventInfoUtil.makeEventInfo("UnitIn", getEventUser(), eventComment, null, null);
					
					Map<String, String> udfs = CommonUtil.setNamedValueSequence(SMessageUtil.getBodyElement(doc), Product.class.getSimpleName());
					
					if(subUnitName.isEmpty())
					{
						setMaterialLocationInfo = MESProductServiceProxy.getProductInfoUtil().setMaterialLocationInfo(productData, unitName, udfs);
					}
					else
					{
						setMaterialLocationInfo = MESProductServiceProxy.getProductInfoUtil().setMaterialLocationInfo(productData, subUnitName, udfs);
					}
					
					MESProductServiceProxy.getProductServiceImpl().setMaterialLocation(eventInfo, productData, setMaterialLocationInfo);
				}

				if(!unitOutTime.isEmpty())
				{
					String eventComment = "UnitOutTime : [" + unitOutTime + "]";
					eventInfo = EventInfoUtil.makeEventInfo("UnitOut", getEventUser(), eventComment, null, null);

					Map<String, String> udfs = CommonUtil.setNamedValueSequence(SMessageUtil.getBodyElement(doc), Product.class.getSimpleName());
					
					if(subUnitName.isEmpty())
					{
						setMaterialLocationInfo = MESProductServiceProxy.getProductInfoUtil().setMaterialLocationInfo(productData, unitName, udfs);
					}
					else
					{
						setMaterialLocationInfo = MESProductServiceProxy.getProductInfoUtil().setMaterialLocationInfo(productData, subUnitName, udfs);
					}
					
					MESProductServiceProxy.getProductServiceImpl().setMaterialLocation(eventInfo, productData, setMaterialLocationInfo);
				}
			}
		}
	}
}
