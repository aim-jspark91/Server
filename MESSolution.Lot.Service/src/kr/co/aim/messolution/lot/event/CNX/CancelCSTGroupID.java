package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.info.CreateInfo;
import kr.co.aim.greentrack.processoperationspec.ProcessOperationSpecServiceProxy;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpecKey;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;

public class CancelCSTGroupID extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelSTB", getEventUser(), "CancelSTB", "", "");
		
		List<Element> eleArrayLotList = SMessageUtil.getBodySequenceItemList(doc, "ARRAYLOTLIST", false);
		List<Element> eleCFLotList = SMessageUtil.getBodySequenceItemList(doc, "CFLOTLIST", false);
		String note = SMessageUtil.getBodyItemValue(doc, "NOTE", true);
		
		eventLog.debug("Lot will be locked to be prevented concurrent executing.");
		Map<String, Lot> arrayLotDataMap = new ConcurrentHashMap<String, Lot>();
		for (Element eleLot : eleArrayLotList) {
			String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
			arrayLotDataMap.put(lotName, LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName)));
		}
		
		Map<String, Lot> cfLotDataMap = new ConcurrentHashMap<String, Lot>();
		for (Element eleLot : eleCFLotList) {
			String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
			cfLotDataMap.put(lotName, LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName)));
		}
		eventLog.debug("Lot is locked to be prevented concurrent executing.");
		
		for (Element eleLot : eleArrayLotList) {

			String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
						
			Lot lotData = arrayLotDataMap.get(lotName);
			ProcessOperationSpec processOperationSpec = CommonUtil.getProcessOperationSpec(lotData);
			
			if(!StringUtils.equals(processOperationSpec.getDetailProcessOperationType(), "STB")) {
				throw new CustomException("LOT-0244", lotData.getKey().getLotName(), lotData.getProcessOperationName());
			}
			
			if(StringUtils.isEmpty(lotData.getProcessGroupName())) {
				throw new CustomException("LOT-0243", lotData.getKey().getLotName());
			}
			
			if(this.checkCellStocker(lotData)) {
				throw new CustomException("LOT-0246", lotData.getCarrierName(), lotData.getKey().getLotName());
			}
			
			int sumArrayProductQuantity = 0;			
			
			lotData.setProcessGroupName(StringUtils.EMPTY);
			LotServiceProxy.getLotService().update(lotData);
			
			SetEventInfo setEventInfo = new SetEventInfo();
			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
			
			List<Product> ProductList = new ArrayList<Product>();
			
			try
			{
				ProductList = MESLotServiceProxy.getLotServiceUtil().getProductListByLotName(lotData.getKey().getLotName());
				sumArrayProductQuantity += ProductList.size();
			}
			catch(Exception e)
			{
				throw new CustomException("LOT-0238", lotData.getKey().getLotName());
			}
			
			for(Product product : ProductList) {
				product.setProcessGroupName(StringUtils.EMPTY);
				
				ProductServiceProxy.getProductService().update(product);
				
				kr.co.aim.greentrack.product.management.info.SetEventInfo productSetEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
				ProductServiceProxy.getProductService().setEvent(product.getKey(), eventInfo, productSetEventInfo);
			}
			
			ProductRequestKey arrayProductRequestKey = new ProductRequestKey();
			arrayProductRequestKey.setProductRequestName(lotData.getProductRequestName());
			ProductRequest arrayProductRequestData = ProductRequestServiceProxy.getProductRequestService().selectByKey(arrayProductRequestKey);
			
			MESWorkOrderServiceProxy.getProductRequestServiceUtil().calculateProductRequestQty(arrayProductRequestData.getKey().getProductRequestName(), "R", -(sumArrayProductQuantity), eventInfo);
		}
		
		for (Element eleLot : eleCFLotList) {
			String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
						
			Lot lotData = cfLotDataMap.get(lotName);
			ProcessOperationSpec processOperationSpec = CommonUtil.getProcessOperationSpec(lotData);
			
			if(!StringUtils.equals(processOperationSpec.getDetailProcessOperationType(), "STB")) {
				throw new CustomException("LOT-0244", lotData.getKey().getLotName(), lotData.getProcessOperationName());
			}
			
			if(StringUtils.isEmpty(lotData.getProcessGroupName())) {
				throw new CustomException("LOT-0243", lotData.getKey().getLotName());
			}
			
			if(this.checkCellStocker(lotData)) {
				throw new CustomException("LOT-0246", lotData.getCarrierName(), lotData.getKey().getLotName());
			}
			
			int sumCfProductQuantity = 0;
						
			lotData.setProcessGroupName(StringUtils.EMPTY);
			LotServiceProxy.getLotService().update(lotData);
			
			SetEventInfo setEventInfo = new SetEventInfo();
			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
			
			List<Product> ProductList = new ArrayList<Product>();
			
			try
			{
				ProductList = MESLotServiceProxy.getLotServiceUtil().getProductListByLotName(lotData.getKey().getLotName());
				sumCfProductQuantity += ProductList.size();
			}
			catch(Exception e)
			{
				throw new CustomException("LOT-0238", lotData.getKey().getLotName());
			}
			
			for(Product product : ProductList) {
				product.setProcessGroupName(StringUtils.EMPTY);
				
				ProductServiceProxy.getProductService().update(product);
				
				kr.co.aim.greentrack.product.management.info.SetEventInfo productSetEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
				ProductServiceProxy.getProductService().setEvent(product.getKey(), eventInfo, productSetEventInfo);
			}
			
			ProductRequestKey cfProductRequestKey = new ProductRequestKey();
			cfProductRequestKey.setProductRequestName(lotData.getProductRequestName());
			ProductRequest cfProductRequestData = ProductRequestServiceProxy.getProductRequestService().selectByKey(cfProductRequestKey);
			
			MESWorkOrderServiceProxy.getProductRequestServiceUtil().calculateProductRequestQty(cfProductRequestData.getKey().getProductRequestName(), "R", -(sumCfProductQuantity), eventInfo);
		}
		
		return doc;
	}
	
	private ProductRequest getCFProductRequestName(String arrayProductRequestName) throws CustomException
	{
		try
		{
			String sql = " SELECT PRODUCTREQUESTNAME "
					+ " FROM PRODUCTREQUEST "
					+ " WHERE 1=1 "
					+ " 	  AND MAINPRODUCTREQUESTNAME = :MAINPRODUCTREQUESTNAME "
					+ " 	  AND FACTORYNAME = :FACTORYNAME ";

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("MAINPRODUCTREQUESTNAME", arrayProductRequestName);
			bindMap.put("FACTORYNAME", "CELL");

			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			
			if(sqlResult.size() <= 0){
				throw new CustomException("PRODUCTREQUEST-0058", arrayProductRequestName); 
			}
			
			String cfProductReuqestName = sqlResult.get(0).get("PRODUCTREQUESTNAME").toString();

			ProductRequestKey prKey = new ProductRequestKey();
			prKey.setProductRequestName(cfProductReuqestName);

			ProductRequest prData = ProductRequestServiceProxy.getProductRequestService().selectByKey(prKey);

			return prData;
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-9001", "ProductRequest");
		}
	}
	
	private boolean checkCellStocker(Lot lotData) {

		String sql = "SELECT COUNT(L.LOTNAME)  "
				+ " FROM LOT L "
				+ "   INNER JOIN DURABLE D ON L.CARRIERNAME = D.DURABLENAME "
				+ "   INNER JOIN MACHINESPEC MS ON D.MACHINENAME = MS.MACHINENAME "
				+ " WHERE MS.FACTORYNAME = :FACTORYNAME  "
				+ "     AND MS.MACHINETYPE = :MACHINETYPE "
				+ "     AND L.LOTNAME = :LOTNAME ";

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", "CELL");
		bindMap.put("MACHINETYPE", "TransportMachine");
		bindMap.put("LOTNAME", lotData.getKey().getLotName());

		List<Map<String, Object>> sqlResult = GenericServiceProxy
				.getSqlMesTemplate().queryForList(sql, bindMap);

		if (sqlResult.size() <= 0) {
			return true;
		}

		return false;
	}
	
}
