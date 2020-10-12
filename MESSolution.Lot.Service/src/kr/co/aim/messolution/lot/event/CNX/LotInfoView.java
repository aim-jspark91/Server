package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class LotInfoView extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String lotNote = SMessageUtil.getBodyItemValue(doc, "LOTNOTE", false);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", false);
		String prdNote = SMessageUtil.getBodyItemValue(doc, "PRODUCTNOTE", false);
		String lotFlag = SMessageUtil.getBodyItemValue(doc, "LOTFLAG", false);
		String productFlag = SMessageUtil.getBodyItemValue(doc, "PRODUCTFLAG", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Note", getEventUser(), "Note", null, null);
				
		if(StringUtil.equals(lotFlag, "Y"))
		{
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);		
			
			//CommonValidation.checkLotState(lotData);
			
			SetEventInfo setEventInfo = new SetEventInfo();
			// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//			setEventInfo.setUdfs(lotData.getUdfs());
			setEventInfo.getUdfs().put("NOTE", lotNote);
			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
			
			// Modified by smkang on 2019.05.24 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//		    lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
//
//			lotData.getUdfs().put("NOTE", "");
//			LotServiceProxy.getLotService().update(lotData);
			Map<String, String> updateUdfs = new HashMap<String, String>();
			updateUdfs.put("NOTE", "");
			MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
		}
		
		if(StringUtil.equals(productFlag, "Y"))
		{
			Product prdData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);
			
			kr.co.aim.greentrack.product.management.info.SetEventInfo prdSetEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
			// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//			prdSetEventInfo.setUdfs(prdData.getUdfs());
			prdSetEventInfo.getUdfs().put("NOTE", prdNote);
			MESProductServiceProxy.getProductServiceImpl().setEvent(prdData, prdSetEventInfo, eventInfo);
			
			// Modified by smkang on 2019.05.28 - ProductServiceProxy.getProductService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//			prdData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(prdData.getKey().getProductName());
//
//			prdData.getUdfs().put("NOTE", "");
//			ProductServiceProxy.getProductService().update(prdData);
			Map<String, String> updateUdfs = new HashMap<String, String>();
			updateUdfs.put("NOTE", "");
			MESProductServiceProxy.getProductServiceImpl().updateProductWithoutHistory(prdData, updateUdfs);
			
			// Glass Note 남길시 Lot에도 History 남겨달라는 요구사항
			// EventName = [GlassID]&Note
			Lot lotData=null;
			eventInfo.setEventName("ProductNote");
			if(!StringUtils.isEmpty(lotName)){
				lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			}else{
				lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(prdData.getLotName());
			}
			
			SetEventInfo setEventInfo = new SetEventInfo();
			// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//			setEventInfo.setUdfs(lotData.getUdfs());
			setEventInfo.getUdfs().put("NOTE", "["+prdData.getKey().getProductName()+"]"+prdNote);
			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
			
			// Modified by smkang on 2019.05.24 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//		    lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
//
//			lotData.getUdfs().put("NOTE", "");
//			LotServiceProxy.getLotService().update(lotData);
			Map<String, String> updateUdfs2 = new HashMap<String, String>();
			updateUdfs2.put("NOTE", "");
			MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs2);
		}
		
		return doc;
	}
}