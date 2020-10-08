package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.service.LotServiceUtil;
import kr.co.aim.messolution.product.service.ProductServiceUtil;
import kr.co.aim.greentrack.generic.GenericServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupHistory;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupHistoryKey;
import kr.co.aim.greentrack.processgroup.management.info.DeassignMaterialsInfo;
import kr.co.aim.greentrack.processgroup.management.info.SetEventInfo;
import kr.co.aim.greentrack.processgroup.management.info.ext.MaterialU;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;







import org.jdom.Document;
import org.jdom.Element;

public class PalletShipping extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("PalletShipping", getEventUser(), getEventComment(), "", "");
		
		String sql = "";
		
		List<Map<String, Object>> sqlResult = new ArrayList<Map<String, Object>>();
		Map<String, Object> bindMap = new HashMap<String, Object>();
		
		String palletName = SMessageUtil.getBodyItemValue(doc, "PALLETNAME", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		
		List<Lot> lotList = CommonUtil.getLotListByPalletID( palletName );
		List<Product> productDataList =ProductServiceProxy.getProductService().select("WHERE PROCESSGROUPNAME = ? ", new Object[] { palletName });
		
		// PalletShipping		
		if(lotList.size()>0){
			// New Batch-changeOperation
			Map<String, Object> changeColumns_Lot= new HashMap<String,Object>();
			changeColumns_Lot.put("areaName",  "");
			changeColumns_Lot.put("lotState",  GenericServiceProxy.getConstantMap().Lot_Shipped);
			changeColumns_Lot.put("lotProcessState",  "");
			changeColumns_Lot.put("lotHoldState",  "");
			
			Map<String, Object> changeColumns_Product = new HashMap<String,Object>();
			changeColumns_Product.put("areaName",  "");
			changeColumns_Product.put("productState",  GenericServiceProxy.getConstantMap().Lot_Shipped);
			changeColumns_Product.put("productProcessState",  "");
			changeColumns_Product.put("productHoldState",  "");
						
			try {
				LotServiceUtil.LotBatchSetEvent(lotList, eventInfo, changeColumns_Lot);
			} catch (IllegalArgumentException | SecurityException
					| IllegalAccessException | NoSuchFieldException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				ProductServiceUtil.productBatchSetEvent(productDataList, eventInfo, changeColumns_Product);
			} catch (IllegalArgumentException | SecurityException
					| IllegalAccessException | NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//Update ProcessGroup		
		ProcessGroup processGroupData = CommonUtil.getProcessGroupByProcesessGroupName( palletName );
		Map<String, String> palletUdf = new HashMap<String, String>();
		palletUdf.put("shipFlag", "Y");
		processGroupData.setUdfs(palletUdf);
		processGroupData.setLastEventName(eventInfo.getEventName());
		processGroupData.setLastEventTime(eventInfo.getEventTime());
		processGroupData.setLastEventUser(eventInfo.getEventUser());
		processGroupData.setLastEventComment(eventInfo.getEventComment());
		processGroupData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		ProcessGroupServiceProxy.getProcessGroupService().update(processGroupData);
		
		ProcessGroupHistoryKey phk = new ProcessGroupHistoryKey();
		phk.setProcessGroupName(processGroupData.getKey().getProcessGroupName());
		phk.setTimeKey(ConvertUtil.getCurrTimeKey());
		
		ProcessGroupHistory ph = new ProcessGroupHistory();
		ph.setKey(phk);
		ph.setEventComment(eventInfo.getEventComment());
		ph.setEventName(eventInfo.getEventName());
		ph.setEventTime(eventInfo.getEventTime());
		ph.setEventUser(eventInfo.getEventUser());
		
		ph.setUdfs(palletUdf);
		
		ProcessGroupServiceProxy.getProcessGroupHistoryService().insert(ph);	
		
		return doc;
	}

}
