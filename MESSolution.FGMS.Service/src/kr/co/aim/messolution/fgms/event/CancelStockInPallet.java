package kr.co.aim.messolution.fgms.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.fgms.FGMSServiceProxy;
import kr.co.aim.messolution.fgms.management.data.FGMSProductRequest;
import kr.co.aim.messolution.fgms.management.data.Product;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.area.AreaServiceProxy;
import kr.co.aim.greentrack.area.management.data.Area;
import kr.co.aim.greentrack.area.management.data.AreaKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupHistory;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupHistoryKey;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupKey;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CancelStockInPallet extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {

		Element processGroupElementList = SMessageUtil.getBodySequenceItem(doc, "PROCESSGROUPLIST", true);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelStockIn", getEventUser(), getEventComment(), "", "");
		
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		for ( @SuppressWarnings("rawtypes")
		Iterator iPalletList = processGroupElementList.getChildren().iterator(); iPalletList.hasNext();)
		{

			Element processGroupE = (Element) iPalletList.next();
			String palletName = SMessageUtil.getChildText(processGroupE, "PALLETNAME", true);
			String processGroupName = SMessageUtil.getChildText(processGroupE, "PROCESSGROUPNAME", true);
			
			//Get processGroup Data
			ProcessGroupKey pKey = new ProcessGroupKey(processGroupName);
			ProcessGroup processGroupData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(pKey);
			
			//1. Check Pallet Invoice
			if(!StringUtils.isEmpty(processGroupData.getUdfs().get("INVOICENO")) || !StringUtils.isEmpty(processGroupData.getUdfs().get("INVOICEDETAILNO")))
				throw new CustomException("PROCESSGROUP-0005", palletName);

			//2. Check Pallet Hold State
			if(processGroupData.getUdfs().get("HOLDSTATE").equals("Y"))
				throw new CustomException("PROCESSGROUP-0001", palletName);
			
			//Get Location
			String location = processGroupData.getUdfs().get("LOCATION");
			
			//1. Remove Product(FGMS_Product)
			//Get Product Data List By Pallet Name
			List<ListOrderedMap> productListByPallet = getProductListByPallet(processGroupName);

			//Remove Product Data
			for(ListOrderedMap eProduct : productListByPallet)
			{
				String sProductName = CommonUtil.getValue(eProduct, "PRODUCTNAME");
				
				Product objProduct = FGMSServiceProxy.getProductService().selectByKey(false, new Object[] {sProductName});
				FGMSServiceProxy.getProductService().remove(eventInfo, objProduct);
			}
			
			//3. Remove Box
			//Get Box By Pallet Name
			String condition = "WHERE superProcessGroupName = ?";
			Object[] bindSet = new Object[] {processGroupName};
			List<ProcessGroup> boxListByPallet = ProcessGroupServiceProxy.getProcessGroupService().select(condition, bindSet);
			
			//Remove Box
			for(ProcessGroup objBox : boxListByPallet)
			{
				ProcessGroupKey boxKey = objBox.getKey();
				ProcessGroupServiceProxy.getProcessGroupService().remove(boxKey);
			}
			
			//4. Remove Pallet  modify update processGroup Info
			//ProcessGroupServiceProxy.getProcessGroupService().remove(pKey);
			ProcessGroup processGroupInfo = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(pKey);
			HashMap<String, String> udfs = new HashMap<String, String>();
			
			processGroupInfo.setMaterialQuantity(0);
			processGroupInfo.setLastEventName(eventInfo.getEventName());
			processGroupInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
			processGroupInfo.setLastEventTime(eventInfo.getEventTime());
			processGroupInfo.setLastEventUser(eventInfo.getEventUser());
			processGroupInfo.setLastEventComment(eventInfo.getEventName());
			udfs.put("LOCATION", "");
			udfs.put("STOCKSTATE", "CancelStocked");
			processGroupInfo.setUdfs(udfs);
			
			ProcessGroupServiceProxy.getProcessGroupService().update(processGroupInfo);
			
			ProcessGroup newProductGroupData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(pKey);
			
			addProdGroupHistory(newProductGroupData,eventInfo);
			
			
			//5.Area Move Increment Location Pallet Quantity
			
            String areaName = location;
			
			AreaKey areaKey = new AreaKey();
			areaKey.setAreaName(areaName);
			
			Area areaData = AreaServiceProxy.getAreaService().selectByKey(areaKey);
			incrementLocationPalletQty(areaData, -1);
			
			// Set MES Receive Flag
			SetMESReceiveFlag(palletName, "N");
			
			//send wo Info to ERP
			int productQty = -productListByPallet.size();
			String pQty = String.valueOf(productQty);
			updateFGMSProductRequestInfo(newProductGroupData,eventInfo,areaData,pQty);
		}
		//String palletName = SMessageUtil.getBodyItemValue(doc, "PALLETNAME", true);

		return doc;
	}
	
	private List<ListOrderedMap> getProductListByPallet(String processGroupName) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT F.PRODUCTNAME ");
		sql.append("   FROM FGMS_PRODUCT F, PROCESSGROUP P ");
		sql.append("  WHERE     1 = 1 ");
		sql.append("        AND F.DURABLENAME = P.DURABLENAME ");
		sql.append("        AND P.SUPERPROCESSGROUPNAME = :PROCESSGROUPNAME ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PROCESSGROUPNAME", processGroupName);
		
		List<ListOrderedMap> result;
		try
		{
			result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		}
		catch (FrameworkErrorSignal fe)
		{
			result = new ArrayList<ListOrderedMap>();
		}
		
		if (result.size() < 1)
			throw new CustomException("SYS-9001", "");
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private void SetMESReceiveFlag(String palletName, String receiveFlag) throws CustomException
	{
		StringBuilder sqlPallet = new StringBuilder();
		sqlPallet.append(" UPDATE CT_SHIPPALLET@MES ");
		sqlPallet.append("    SET RECEIVEFLAG = :RECEIVEFLAG ");
		sqlPallet.append("  WHERE 1 = 1 ");
		sqlPallet.append("    AND DESTINATIONFACTORYNAME = 'FGI' ");
		sqlPallet.append("    AND PALLETNAME = :PALLETNAME ");
		
		StringBuilder sqlBox = new StringBuilder();
		sqlBox.append(" UPDATE CT_SHIPBOX@MES ");
		sqlBox.append("    SET RECEIVEFLAG = :RECEIVEFLAG ");
		sqlBox.append("  WHERE 1 = 1 ");
		sqlBox.append("    AND PALLETNAME = :PALLETNAME ");
		
		StringBuilder sqlProduct = new StringBuilder();
		sqlProduct.append(" UPDATE CT_SHIPPRODUCT@MES ");
		sqlProduct.append("    SET RECEIVEFLAG = :RECEIVEFLAG ");
		sqlProduct.append("  WHERE 1 = 1 ");
		sqlProduct.append("    AND PALLETNAME = :PALLETNAME ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PALLETNAME", palletName);
		bindMap.put("RECEIVEFLAG", receiveFlag);

		try
		{
			GenericServiceProxy.getSqlMesTemplate().update(sqlPallet.toString(), bindMap);
			GenericServiceProxy.getSqlMesTemplate().update(sqlBox.toString(), bindMap);
			GenericServiceProxy.getSqlMesTemplate().update(sqlProduct.toString(), bindMap);
		}
		catch (FrameworkErrorSignal fe)
		{
			
		}
	}
	private void incrementLocationPalletQty(Area locationData, int palletQty) throws CustomException
	{
		// Update Location Info
		int iLocationCapa = Integer.parseInt(locationData.getUdfs().get("CAPACITY"));
		int iPalletQty = Integer.parseInt(locationData.getUdfs().get("PALLETQUANTITY")) + palletQty;
		
		Map<String, String> areaUdfs = new HashMap<String,String>();
		
		if(iLocationCapa <= iPalletQty)
		{
			areaUdfs.put("FULLSTATE", "Y");
		}
		else
		{
			areaUdfs.put("FULLSTATE", "N");
		}
		
		areaUdfs.put("PALLETQUANTITY", String.valueOf(iPalletQty));
		locationData.setUdfs(areaUdfs);
		
		AreaServiceProxy.getAreaService().update(locationData);
	}
	public void addProdGroupHistory(ProcessGroup processGroupData, EventInfo eventInfo) throws CustomException
	{
		ProcessGroupHistory processGroupHistory = new ProcessGroupHistory();
		
		ProcessGroupHistoryKey key = new ProcessGroupHistoryKey();
		
		key.setProcessGroupName(processGroupData.getKey().getProcessGroupName());
		key.setTimeKey(eventInfo.getEventTimeKey());
		
		Map<String,String> groupUdfs = processGroupData.getUdfs();
		
		groupUdfs.put("OUTSOURCE", processGroupData.getUdfs().get("OUTSOURCE"));
		groupUdfs.put("ORIGINALFACTORY", processGroupData.getUdfs().get("ORIGINALFACTORY"));
		
		
		processGroupHistory.setKey(key);
		processGroupHistory.setEventTime(eventInfo.getEventTime());
		processGroupHistory.setEventName(eventInfo.getEventName());
		processGroupHistory.setSuperProcessGroupName(processGroupData.getSuperProcessGroupName());
		processGroupHistory.setOldMaterialQuantity(processGroupData.getMaterialQuantity());
		processGroupHistory.setMaterialQuantity(processGroupData.getMaterialQuantity());
		processGroupHistory.setEventUser(eventInfo.getEventUser());
		processGroupHistory.setEventComment(eventInfo.getEventComment());
		processGroupHistory.setEventFlag("N");
		processGroupHistory.setReasonCode(eventInfo.getReasonCode());
		processGroupHistory.setReasonCodeType(eventInfo.getReasonCodeType());
		processGroupHistory.setSystemTime(TimeUtils.getCurrentTimestamp());
		processGroupHistory.setCancelFlag("A");
		processGroupHistory.setCancelTimeKey(null);
		processGroupHistory.setUdfs(groupUdfs);
		ProcessGroupServiceProxy.getProcessGroupHistoryService().insert(processGroupHistory);
	}
	
	public void  updateFGMSProductRequestInfo(ProcessGroup processData,EventInfo eventInfo,Area locationData,String productQty) throws CustomException
	{
		try
		{
			
			//checkExit
			List<ListOrderedMap> WorkOrderList = this.getMESWorkOrderInfo(processData.getUdfs().get("PRODUCTREQUESTNAME"));
			
			if(WorkOrderList.size() > 0){
			
			for(ListOrderedMap workOrder : WorkOrderList )
			{
			//Get WO info
			String productRequestName	= workOrder.get("PRODUCTREQUESTNAME").toString();
			//String seq	= workOrder.get("SEQ").toString();
			String mto	= workOrder.get("MTO").toString();
			
            //Create FGMS stock info to WO
			FGMSProductRequest createInfo = new FGMSProductRequest();
			createInfo.setProductRequestName(productRequestName);
			createInfo.setSEQ(eventInfo.getEventTimeKey());
			createInfo.setPlanQty(productQty);
			createInfo.setSupperAreaName(locationData.getSuperAreaName());
			createInfo.setAreaName(locationData.getKey().getAreaName());
			createInfo.setMTO(mto);
			createInfo.setInfoResultStatus("CancelStocked");
			createInfo.setInfoResultMessage("");
			createInfo.setWriteDate(eventInfo.getEventTime());
			//createInfo.setReadDate(0);
			createInfo.setReceiveflag("N");
			
			//createInfo.setMaterialUdfs(udfs);

			FGMSServiceProxy.getFGMSProductRequestService().create(eventInfo, createInfo);
		
			}
		  }
			
		}
		catch(Exception ex)
		{
			throw new CustomException("SYS-9001");
		}
	}
	public List<ListOrderedMap> getMESWorkOrderInfo(String ProductRequestName) throws CustomException
	{                                                     
		StringBuffer sql = new StringBuffer();
		sql.append("    SELECT P.PRODUCTREQUESTNAME,  ");
		sql.append("           P.PRODUCTSPECNAME,  ");
		sql.append("           P.FACTORYNAME,  ");
		sql.append("           P.PRODUCTREQUESTSTATE,  ");
		sql.append("           P.PLANRELEASEDTIME,  ");
		sql.append("           P.PLANFINISHEDTIME,  ");
		sql.append("           P.PLANQUANTITY,  ");
		sql.append("           P.MTO ");
		//sql.append("           P.SEQ, ");
		sql.append("      FROM PRODUCTREQUEST@MES P  ");
		sql.append("     WHERE 1 = 1   ");
		sql.append("           AND P.PRODUCTREQUESTNAME = :PRODUCTREQUESTNAME ");

		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PRODUCTREQUESTNAME", ProductRequestName);
		
		try
		{
			@SuppressWarnings("unchecked")
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(),bindMap);
			
			return result;
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "MESWorkOrder", fe.getMessage());
		}
	}
}
