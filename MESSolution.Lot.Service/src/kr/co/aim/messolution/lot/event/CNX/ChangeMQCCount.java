package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;

import org.jdom.Document;
import org.jdom.Element;

/**
 * @author ParkJeongSu
 * @see MQCCount를 수정하는 Class
 * Lot과 Product 모두 history를 남깁니다.
 * 그래서 같은 eventInfo를 사용합니다.
 */
public class ChangeMQCCount extends SyncHandler 
{
	
	public Object doWorks(Document doc) throws CustomException 
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String note = SMessageUtil.getBodyItemValue(doc, "NOTE", true);
		String mqcCount = SMessageUtil.getBodyItemValue(doc, "MQCCOUNT", true);
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UpdateUsedCnt", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
		
		kr.co.aim.greentrack.product.management.info.SetEventInfo setProductEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
		setProductEventInfo.getUdfs().put("MQCCOUNT", mqcCount);
		setProductEventInfo.getUdfs().put("NOTE", note);
		
		String tempProductlist = StringUtil.EMPTY;
		// productList setEvent
		for(Element element : productList){
			// mentis 3552 
			if(StringUtil.isEmpty(tempProductlist))
			{
				tempProductlist = "[UpdateCount Product] - " + SMessageUtil.getChildText(element, "PRODUCTNAME", true);
			}
			else
			{
				tempProductlist = tempProductlist + " , " + SMessageUtil.getChildText(element, "PRODUCTNAME", true);
			}
				
			String sProductName = SMessageUtil.getChildText(element, "PRODUCTNAME", true);
			Product product = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(sProductName);
			ProductServiceProxy.getProductService().setEvent(product.getKey(), eventInfo, setProductEventInfo);
		}

		kr.co.aim.greentrack.lot.management.info.SetEventInfo SetLotEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
		SetLotEventInfo.getUdfs().put("NOTE", note + "\n" + tempProductlist);
		
		// Lot setEvent 
		Lot lot= MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		LotServiceProxy.getLotService().setEvent(lot.getKey(), eventInfo, SetLotEventInfo);
		
		// Modified by smkang on 2019.05.24 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//		Lot lotData_Note = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//		Map<String, String> udfs_note = lotData_Note.getUdfs();
//		udfs_note.put("NOTE", "");
//		LotServiceProxy.getLotService().update(lotData_Note);
		Map<String, String> updateUdfs = new HashMap<String, String>();
		updateUdfs.put("NOTE", "");
		MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lot, updateUdfs);
		
		return doc;
	}
}