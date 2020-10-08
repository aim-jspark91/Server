package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowHistory;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowHistoryKey;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;

import org.jdom.Document;

public class MQCFlowSetting extends SyncHandler{
	@SuppressWarnings("unchecked")
	@Override
	public Object doWorks(Document doc)throws CustomException
	{ 
		String Factoryname = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String ProcessFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String RequiredSheetCount = SMessageUtil.getBodyItemValue(doc, "REQUIREDSHEETCOUNT", true);
		String ProcessFlowSettingCount = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWSETTINGCOUNT", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("MQCFlowSetting", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		ProcessFlowKey newProcessKeyInfo = new ProcessFlowKey(Factoryname, ProcessFlowName, "00001");
		
		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		ProcessFlow newProcessFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(newProcessKeyInfo);
		ProcessFlow newProcessFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKeyForUpdate(newProcessKeyInfo);

		Map<String,String> udfs = newProcessFlowData.getUdfs();
		udfs.put("MQCREQUIREDSHEETCOUNT", RequiredSheetCount);
		udfs.put("MQCFLOWSETTINGCOUNT", ProcessFlowSettingCount);	
		ProcessFlowServiceProxy.getProcessFlowService().update(newProcessFlowData);

		ProcessFlowHistory ProcessFlowHistoryData = new ProcessFlowHistory();
		
		ProcessFlowHistoryKey ProcessFlowHistoryKeyData = new ProcessFlowHistoryKey();
		ProcessFlowHistoryKeyData.setFactoryName(Factoryname);
		ProcessFlowHistoryKeyData.setProcessFlowName(ProcessFlowName);
		ProcessFlowHistoryKeyData.setProcessFlowVersion("00001");
		ProcessFlowHistoryKeyData.setTimeKey(eventInfo.getEventTimeKey());
		
		ProcessFlowHistoryData.setKey(ProcessFlowHistoryKeyData);
		ProcessFlowHistoryData.setEventComment("MQCFlowSetting");
		ProcessFlowHistoryData.setEventName(eventInfo.getEventName());
		ProcessFlowHistoryData.setEventTime(eventInfo.getEventTime());
		ProcessFlowHistoryData.setEventUser(eventInfo.getEventUser());
		ProcessFlowHistoryData.setProcessFlowType(newProcessFlowData.getProcessFlowType());
		Map<String,String> udfsHist = ProcessFlowHistoryData.getUdfs();
		udfsHist.put("MQCREQUIREDSHEETCOUNT", RequiredSheetCount);
		udfsHist.put("MQCFLOWSETTINGCOUNT", ProcessFlowSettingCount);	

		ProcessFlowServiceProxy.getProcessFlowHistoryService().insert(ProcessFlowHistoryData);
		
		String condition = "where MQCUSEPROCESSFLOW=? " ;
		Object[] bindSet = new Object[]{ProcessFlowName};
		List<Product> productList=null;
		try {
			productList =  ProductServiceProxy.getProductService().select(condition, bindSet);
			
			for(Product product : productList){
				// 2019.06.14_hsryu_Delete Logic. Mantis 0004194.
//				product.getUdfs().put("MQCLIMITCOUNT", ProcessFlowSettingCount);
//				product.setLastEventTime(eventInfo.getEventTime());
//				
//				// Modified by smkang on 2019.05.17 - EventInfo.LastEventTimekey is null.
////				product.setLastEventTimeKey(eventInfo.getLastEventTimekey());
//				product.setLastEventTimeKey(eventInfo.getEventTimeKey());
//				
//				ProductServiceProxy.getProductService().update(product);
				
				// 2019.06.14_hsryu_Insert Logic. Memory History!!!! Mantis 0004194.
                kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo1 = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
                
				setEventInfo1.getUdfs().put("MQCLIMITCOUNT", ProcessFlowSettingCount);
                ProductServiceProxy.getProductService().setEvent(product.getKey(), eventInfo, setEventInfo1);
			}
			
		} catch (Exception e) {
			if(productList!=null && productList.size()>0){
				throw new CustomException("COMMON-0001", "ProductList Update Error");	
			}
		}
		
		return doc;
	}
}