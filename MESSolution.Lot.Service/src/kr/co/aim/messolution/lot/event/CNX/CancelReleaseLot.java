package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.data.LotHistoryKey;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.processoperationspec.ProcessOperationSpecServiceProxy;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpecKey;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductHistory;
import kr.co.aim.greentrack.product.management.data.ProductHistoryKey;

import org.jdom.Document;
import org.jdom.Element;

public class CancelReleaseLot extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		Element lotList = SMessageUtil.getBodySequenceItem(doc, "LOTLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelRelease", getEventUser(), getEventComment(), null, null);
		
		for (@SuppressWarnings("rawtypes")
		Iterator iLot = lotList.getChildren().iterator(); iLot.hasNext();)
		{
			Element eLot = (Element) iLot.next();
			String sLotName = SMessageUtil.getChildText(eLot, "LOTNAME", true);
			
			Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(sLotName);
			
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			List<Product> prdList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(sLotName);
			List<Product> prdList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(sLotName);
			
			Lot bankLotData = new Lot();
			Map<String, String> histUdfs;
			
			//Validation
			ProcessOperationSpec oSpec = CommonUtil.getFirstOperation(lotData.getFactoryName(), lotData.getProcessFlowName());
			
			if(!StringUtil.equals(lotData.getProcessOperationName(), oSpec.getKey().getProcessOperationName()))
			{
				throw new CustomException("LOT-0084", sLotName);
			}
			
			if ("BANK".equals(oSpec.getDetailProcessOperationType())) 
			{
				throw new CustomException("LOT-0094",sLotName);
			}
			
			//Cst multi Lot Case
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			List<Lot> cstLotList = CommonUtil.getLotListByCarrier(lotData.getCarrierName(), false);
			List<Lot> cstLotList = CommonUtil.getLotListByCarrierForUpdate(lotData.getCarrierName(), false);
			
			if(cstLotList.size() > 1)
				bankLotData = this.findBankLot(cstLotList);
			
			String oldWoName = lotData.getProductRequestName();
			
			//Multi Lot Case(only two Lot)
			if(bankLotData != null && StringUtil.isNotEmpty(bankLotData.getKey().getLotName()))
			{
				histUdfs = this.findLotHist(bankLotData.getKey().getLotName());				
				Document cloneDoc = (Document) doc.clone();				
				
				this.setLotInfo(lotData, histUdfs, eventInfo);
				this.setPrdInfo(prdList, histUdfs, eventInfo);
				
				this.writeProductList(prdList, cloneDoc);				
				List<Element> productElementList = SMessageUtil.getBodySequenceItemList(cloneDoc, "PRODUCTLIST", true);
				
				eventInfo.setEventName("Merge");
				List<Lot> mergeList = new ArrayList<Lot>();
				
				mergeList.add(lotData);
				
				MESLotServiceProxy.getLotServiceUtil().mergeLot(eventInfo, mergeList, bankLotData);
			}		
			//one Lot Case
			else
			{
				histUdfs = this.findLotHist(sLotName);
				
				this.setLotInfo(lotData, histUdfs, eventInfo);
				this.setPrdInfo(prdList, histUdfs, eventInfo);
			}
			
			//Decrement Product Request
			try
			{	
				MESWorkOrderServiceProxy.getProductRequestServiceUtil().calculateProductRequestQty(oldWoName, null, "R", -prdList.size(), eventInfo);
				
			}
			catch(Exception e)
			{
				eventLog.error("decrementWorkOrderReleaseQty Failed");
			}
			
		}

		return doc;
	}
	
	
	private Map<String, String> findLotHist(String lotName)throws CustomException
	{
		String sql = "SELECT PRODUCTIONTYPE, PRODUCTSPECNAME, PRODUCTREQUESTNAME, PROCESSFLOWNAME, PROCESSOPERATIONNAME, "
				+ " NODESTACK, DESTINATIONFACTORYNAME, LASTFACTORYNAME, BEFOREOPERATIONNAME FROM LOTHISTORY WHERE LOTNAME = :lotName AND TIMEKEY = "
				+ "(SELECT MAX(TIMEKEY) FROM LOTHISTORY WHERE LOTNAME = :lotName AND EVENTNAME In (:eventName1, :eventName2)) ";
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("lotName", lotName);
		bindMap.put("eventName1", "Receive");
		bindMap.put("eventName2", "Recreate");
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = 
				GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		Map<String, String> udfs = new HashMap<String, String>();
		
		try
		{
			if(sqlResult.size() > 0)
			{
				udfs.put("PRODUCTIONTYPE", sqlResult.get(0).get("PRODUCTIONTYPE").toString());
				udfs.put("PRODUCTSPECNAME", sqlResult.get(0).get("PRODUCTSPECNAME").toString());
				udfs.put("PRODUCTREQUESTNAME", sqlResult.get(0).get("PRODUCTREQUESTNAME").toString());
				udfs.put("PROCESSFLOWNAME", sqlResult.get(0).get("PROCESSFLOWNAME").toString());
				udfs.put("PROCESSOPERATIONNAME", sqlResult.get(0).get("PROCESSOPERATIONNAME").toString());
				udfs.put("NODESTACK", sqlResult.get(0).get("NODESTACK").toString());
				//udfs.put("DESTINATIONFACTORYNAME", sqlResult.get(0).get("DESTINATIONFACTORYNAME").toString());
				udfs.put("LASTFACTORYNAME", sqlResult.get(0).get("LASTFACTORYNAME").toString());
				udfs.put("BEFOREOPERATIONNAME", sqlResult.get(0).get("BEFOREOPERATIONNAME").toString());
			}
			else 
			{			
				throw new CustomException("Not found data");	
			}
		}
		
		catch (Exception ex)
		{
			throw new CustomException("LOT-0211", lotName);
		}
			
		return udfs;
	}
	
	private Lot findBankLot(List<Lot> lotList)
	{		
		for (Lot lotData : lotList) 
		{
			ProcessOperationSpecKey poKey = new ProcessOperationSpecKey(lotData.getFactoryName(), lotData.getProcessOperationName(),lotData.getProcessOperationVersion());
			ProcessOperationSpec poSpec = ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(poKey);
			
			if(poSpec.getDetailProcessOperationType().equals("BANK"))
			{
				return lotData;
			}
		}
		
		return null;
	}
	
	private void setLotInfo(Lot lotData, Map<String, String> histUdfs, EventInfo eventInfo)
	{
		String temptimekey = eventInfo.getEventTimeKey();
		
		Map<String, String> udfs = lotData.getUdfs();
		String oldProductSpecName = lotData.getProductSpecName();
		String oldProcessFlowName = lotData.getProcessFlowName();
		String oldProcessOperationName = lotData.getProcessOperationName();
		String oldProductionType = lotData.getProductionType();
		
		lotData.setProductionType(histUdfs.get("PRODUCTIONTYPE"));
		lotData.setProductSpecName(histUdfs.get("PRODUCTSPECNAME"));
		lotData.setProductRequestName(histUdfs.get("PRODUCTREQUESTNAME"));
		lotData.setProcessFlowName(histUdfs.get("PROCESSFLOWNAME"));
		lotData.setProcessOperationName(histUdfs.get("PROCESSOPERATIONNAME"));
		lotData.setNodeStack(histUdfs.get("NODESTACK"));
		//lotData.setDestinationFactoryName(histUdfs.get("DESTINATIONFACTORYNAME"));
		lotData.setLotState("Released");
		lotData.setLotProcessState("WAIT");
		lotData.setLotHoldState("N");
		
		udfs.put("BEFOREOPERATIONNAME", histUdfs.get("BEFOREOPERATIONNAME"));
		udfs.put("LASTFACTORYNAME", histUdfs.get("LASTFACTORYNAME"));
		
		LotServiceProxy.getLotService().update(lotData);
		
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs(udfs);
		
		LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
		
		LotHistoryKey LotHistoryKey = new LotHistoryKey();
	    LotHistoryKey.setLotName(lotData.getKey().getLotName());
	    LotHistoryKey.setTimeKey(temptimekey);
	    
	    // Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//	    LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKey(LotHistoryKey);
	    LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(LotHistoryKey);

	    lotHistory.setOldProductSpecName(oldProductSpecName);
		lotHistory.setOldProcessFlowName(oldProcessFlowName);
		lotHistory.setOldProcessOperationName(oldProcessOperationName);
		lotHistory.setOldProductionType(oldProductionType);
		
		LotServiceProxy.getLotHistoryService().update(lotHistory);
		
		
	}
	
	private void setPrdInfo(List<Product> prdList, Map<String, String> histUdfs, EventInfo eventInfo)
	{
		for (Product prdData : prdList)
		{
			Map<String, String> prdUdfs = prdData.getUdfs();
			String factoryName = prdData.getFactoryName();
			String oldProductSpecName = prdData.getProductSpecName();
			String oldProcessFlowName = prdData.getProcessFlowName();
			String oldProcessOperationName = prdData.getProcessOperationName();
			String oldProductionType = prdData.getProductionType();
			
			prdData.setProductionType(histUdfs.get("PRODUCTIONTYPE"));
			prdData.setProductSpecName(histUdfs.get("PRODUCTSPECNAME"));
			prdData.setProductRequestName(histUdfs.get("PRODUCTREQUESTNAME"));
			prdData.setProcessFlowName(histUdfs.get("PROCESSFLOWNAME"));
			prdData.setProcessOperationName(histUdfs.get("PROCESSOPERATIONNAME"));
			prdData.setNodeStack(histUdfs.get("NODESTACK"));
			//prdData.setDestinationFactoryName(histUdfs.get("DESTINATIONFACTORYNAME"));
			prdData.setProductState("InProduction");
			prdData.setProductProcessState("Idle");
			prdData.setProductHoldState("N");
			//prdData.setFactoryName(histUdfs.get("LASTFACTORYNAME"));
			
			ProductServiceProxy.getProductService().update(prdData);
			
			kr.co.aim.greentrack.product.management.info.SetEventInfo prdSetEvInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
			prdSetEvInfo.setUdfs(prdUdfs);
			
			ProductServiceProxy.getProductService().setEvent(prdData.getKey(), eventInfo, prdSetEvInfo);
			
			ProductHistoryKey productHistoryKey = new ProductHistoryKey();
            productHistoryKey.setProductName(prdData.getKey().getProductName());
            productHistoryKey.setTimeKey(prdData.getLastEventTimeKey());

            // Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//            ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKey(productHistoryKey);
            ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKeyForUpdate(productHistoryKey);
            
			productHistory.setFactoryName(factoryName);
			productHistory.setOldProductSpecName(oldProductSpecName);
			productHistory.setOldProcessFlowName(oldProcessFlowName);
			productHistory.setOldProcessOperationName(oldProcessOperationName);
			productHistory.setOldFactoryName(factoryName);
			productHistory.setOldProductionType(oldProductionType);
			
			ProductServiceProxy.getProductHistoryService().update(productHistory);
		}
	}
	
	private Document writeProductList(List<Product> productList, Document doc)
	{	
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);

		Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);		
		
		Element element = new Element("PRODUCTLIST");
		for (Product prdData : productList) 
		{
			Element eleProduct = new Element("PRODUCT");
			
			Element elePrdName = new Element("PRODUCTNAME");
			elePrdName.setText(prdData.getKey().getProductName());
			eleProduct.addContent(elePrdName);
			
			Element elePosition = new Element("POSITION");
			elePosition.setText(String.valueOf(prdData.getPosition()));
			eleProduct.addContent(elePosition);
			
			element.addContent(eleProduct);
			
		}
		
		eleBodyTemp.addContent(element);
		
		// overwrite
		doc.getRootElement().addContent(eleBodyTemp);
		
		return doc;
	}
}
