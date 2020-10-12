package kr.co.aim.messolution.product.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ProductFlag;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.jdom.Document;
import org.jdom.Element;
 
public class ProductFlagManual extends SyncHandler {

	@Override
	public Object doWorks(Document doc)
		throws CustomException 
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UpdateProductFlagByManual", getEventUser(), getEventComment(), null, null);
		
		List<Element> productElement = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);
		String note = SMessageUtil.getBodyItemValue(doc, "NOTE", true);
		
		String changeGlassList = "";
		boolean changeFlag = false;
		
		Lot lotData = null;
		for (Element product : productElement)
		{
			String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", true);
			
			ProductFlag oldProductFlagData = ExtendedObjectProxy.getProductFlagService().selectByKey(false, new Object[]{productName});
			
			changeGlassList += "\n[" + productName+ "] ";
			
			ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGMANUAL);
			
			ProductFlag newProductFlagData = ExtendedObjectProxy.getProductFlagService().selectByKey(false, new Object[]{productName});
			
			if(!oldProductFlagData.getelaQTimeFlag().equals(newProductFlagData.getelaQTimeFlag()))
			{
				changeGlassList += "[ELAQTimeFlag : "+newProductFlagData.getelaQTimeFlag()+"] ";
				
				changeFlag = true;
			}
			
			if(!oldProductFlagData.getProcessTurnFlag().equals(newProductFlagData.getProcessTurnFlag()))
			{
				changeGlassList += "[ProcessTurnFlag : "+ newProductFlagData.getProcessTurnFlag() +"] ";
				
				changeFlag = true;
			}
			
			if(!oldProductFlagData.getTrackFlag().equals(newProductFlagData.getTrackFlag()))
			{
				changeGlassList += "[TrackFlag : "+ newProductFlagData.getTrackFlag() +"]";
				
				changeFlag = true;
			}
			
			if(!oldProductFlagData.getTurnOverFlag().equals(newProductFlagData.getTurnOverFlag()))
			{
				changeGlassList += "[TurnOverFlag : "+ newProductFlagData.getTurnOverFlag() +"]";
				
				changeFlag = true;
			}
			
			
			Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);
			
			lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(productData.getLotName());
			
		}
		
		if(!changeFlag)
		{
			throw new CustomException("PRODUCTFLAG-0001");
		}
		else
		{
			// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//			Map<String, String> udfs = lotData.getUdfs();
//			udfs.put("NOTE", note+"\n Change Glass : "+changeGlassList);
			
			List<ProductU> productUList = new ArrayList<ProductU>();
			
			SetEventInfo setEventInfo = new SetEventInfo(0, productUList);
//			setEventInfo.setUdfs(udfs);
			setEventInfo.getUdfs().put("NOTE", note+"\n Change Glass : "+changeGlassList);
			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
			
			// Modified by smkang on 2019.05.24 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//		    lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
//
//			udfs.put("NOTE", "");
//			LotServiceProxy.getLotService().update(lotData);
			Map<String, String> updateUdfs = new HashMap<String, String>();
			updateUdfs.put("NOTE", "");
			MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
		}
		
		return doc;
	}
}