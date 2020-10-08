package kr.co.aim.messolution.fgms.event;

import java.util.ArrayList;
import java.util.HashMap;
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
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.info.CreateInfo;

import org.apache.commons.collections.map.ListOrderedMap;
import org.jdom.Document;

public class StockInPallet extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {

		String palletName = SMessageUtil.getBodyItemValue(doc, "PALLETNAME", true);
		String location = SMessageUtil.getBodyItemValue(doc, "LOCATIONNAME", true);
		
		String productSpecName = "";
		List<String> boxList = new ArrayList<String>();
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("StockIn", getEventUser(), getEventComment(), "", "");
		
		//2. Get MES Product List by Pallet Name(DBLINK)	
		List<ListOrderedMap> productListByPallet = getProductListByPallet(palletName);
		List<ListOrderedMap> boxListByPallet = getBoxListByPallet(palletName);
		List<ListOrderedMap> palletList = getPalletInfo(palletName);
		
		//1. Get Location Info & Area Info
		AreaKey lKey = new AreaKey();
		lKey.setAreaName(location);
		Area locationData = AreaServiceProxy.getAreaService().selectByKey(lKey);

        //Check Location Resource State
		if(locationData.getResourceState().equals("OutOfService"))
		{
			throw new CustomException("FGMS-0002", location);
		}
		// Check Full State 
		 
		if(locationData.getUdfs().get("FULLSTATE").equals("Y"))
		{
			throw new CustomException("FGMS-0003", location);
		}
		//Check Location & Pallet Domastic Export
		if(!locationData.getUdfs().get("OUTSOURCE").equals(palletList.get(0).get("OUTSOURCE").toString())
				                       ||!locationData.getUdfs().get("ORIGINALFACTORY").equals(palletList.get(0).get("ORIGINALFACTORY").toString())
				                       ||!locationData.getUdfs().get("DOMESTICEXPORT").equals(palletList.get(0).get("DOMESTICEXPORT").toString()))
		{
			throw new CustomException("FGMS-0004", location);
		}

		//3. Create FGMS_Product / CT_FGMS_Product
		
		
		for(ListOrderedMap eProduct: productListByPallet)
		{
			String sProductName = CommonUtil.getValue(eProduct, "PRODUCTNAME");
			String sLotName = CommonUtil.getValue(eProduct, "LOTNAME");
			String sBoxName = CommonUtil.getValue(eProduct, "BOXNAME");
			String sProductSpecName = CommonUtil.getValue(eProduct, "PRODUCTSPECNAME");
			String sProductionType = CommonUtil.getValue(eProduct, "PRODUCTIONTYPE");
			String sProductRequestName = CommonUtil.getValue(eProduct, "PRODUCTREQUESTNAME");
			String sProductType = CommonUtil.getValue(eProduct, "PRODUCTTYPE");
			String sSubProductType = CommonUtil.getValue(eProduct, "SUBPRODUCTTYPE");
			String sProductGrade = CommonUtil.getValue(eProduct, "PRODUCTGRADE");
			String sCrateName = CommonUtil.getValue(eProduct, "CRATENAME");
			String sDomesticExport = CommonUtil.getValue(eProduct, "DOMESTICEXPORT");
			String sOutSource = CommonUtil.getValue(eProduct, "OUTSOURCE");
			String sOriginalFactory = CommonUtil.getValue(eProduct, "ORIGINALFACTORY");
			
			Product product = new Product(sProductName);
			product.setLotName(sLotName);
			product.setProcessGroupName("");
			product.setProductSpecName(sProductSpecName);
			product.setProductionType(sProductionType);
			product.setProductRequestName(sProductRequestName);
			product.setProductType(sProductType);
			product.setSubProductType(sSubProductType);
			product.setProductGrade(sProductGrade);
			product.setCrateName(sCrateName);
			product.setStockState(GenericServiceProxy.getConstantMap().FGMS_STOCKSTATE_STOCKED);
			product.setDomesticExport(sDomesticExport);
			product.setOutSource(sOutSource);
			product.setOriginalFactory(sOriginalFactory);
			product.setDurableName(sBoxName);

			product.setProductionType(sProductionType);

			try{
				FGMSServiceProxy.getProductService().create(eventInfo, product);
			}
			catch (FrameworkErrorSignal fe)
			{
				throw new CustomException("PROCESSGROUP-9999", fe.getMessage());
			}
			catch (DuplicateNameSignal de)
			{
				throw new CustomException("PROCESSGROUP-9002", "Product", sProductName);
			}
			catch (NotFoundSignal ne)
			{
				throw new CustomException("PROCESSGROUP-9003", "Product", sProductName);
			}
		}
		
		//3. Create Box (ProcessGroup)
		for(ListOrderedMap eBox: boxListByPallet)
		{
			String sBoxName = CommonUtil.getValue(eBox, "BOXNAME");
			String sProductSpecName = CommonUtil.getValue(eBox, "PRODUCTSPECNAME");
			String sProductRequestName = CommonUtil.getValue(eBox, "PRODUCTREQUESTNAME");
			String sDomesticExport = CommonUtil.getValue(eBox, "DOMESTICEXPORT");
			String sOutSource = CommonUtil.getValue(eBox,"OUTSOURCE");
			String sOriginalFactory = CommonUtil.getValue(eBox, "ORIGINALFACTORY");
			
			//Get Product Data List By Box Name
			String condition = "WHERE DURABLENAME = ?";
			Object[] bindSet = new Object[] {sBoxName};
			
			List<Product> productListByBox = FGMSServiceProxy.getProductService().select(condition, bindSet);
			
			//Get material List By Product Data List
			List<String> materialList = new ArrayList<String>();
			
			for(Product product : productListByBox)
			{
				materialList.add(product.getProductName());
			}

			//Create
			HashMap<String, String> udfs = new HashMap<String, String>();
			String processGroupName1 = createProcessGroupNaming();
			CreateInfo createInfo = new CreateInfo();
			createInfo.setProcessGroupName(processGroupName1);
			createInfo.setProcessGroupType(GenericServiceProxy.getConstantMap().TYPE_BOX);
			createInfo.setMaterialType(GenericServiceProxy.getConstantMap().TYPE_GLASS);
			createInfo.setDetailMaterialType(GenericServiceProxy.getConstantMap().TYPE_PANEL);
			createInfo.setMaterialNames(materialList);
			createInfo.setMaterialQuantity(materialList.size());
			createInfo.setMaterialUdfs(udfs);
			udfs.put("DURABLENAME", sBoxName);
			udfs.put("PRODUCTREQUESTNAME", sProductRequestName);
			udfs.put("PRODUCTSPECNAME", sProductSpecName);
			udfs.put("LOCATION", location);
			udfs.put("DOMESTICEXPORT", sDomesticExport);
			udfs.put("OUTSOURCE", sOutSource);
			udfs.put("ORIGINALFACTORY", sOriginalFactory);
			udfs.put("STOCKSTATE", GenericServiceProxy.getConstantMap().FGMS_STOCKSTATE_STOCKED);
			udfs.put("STOCKINTIME", eventInfo.getEventTime().toString());
			udfs.put("STOCKINUSER", eventInfo.getEventUser());
			createInfo.setUdfs(udfs);
			
			ProcessGroupServiceProxy.getProcessGroupService().create(eventInfo, createInfo);
			
			//Set Box List
			if(!boxList.contains(processGroupName1))
			{
				boxList.add(processGroupName1);
			}
			
			//Set Product Spec
			productSpecName = sProductSpecName;
		}
		
		//4. Create Pallet
		HashMap<String, String> udfs = new HashMap<String, String>();
		String processGroupName2 = createProcessGroupNaming();
		CreateInfo createInfo = new CreateInfo();
		createInfo.setProcessGroupName(processGroupName2);
		createInfo.setProcessGroupType(GenericServiceProxy.getConstantMap().TYPE_PALLET);
		createInfo.setMaterialType(GenericServiceProxy.getConstantMap().TYPE_PROCESSGROUP);
		createInfo.setDetailMaterialType(GenericServiceProxy.getConstantMap().TYPE_BOX);
		createInfo.setMaterialNames(boxList);
		createInfo.setMaterialQuantity(boxList.size());
		//createInfo.setMaterialUdfs(udfs);
		udfs.put("DURABLENAME", palletName);
		udfs.put("PRODUCTREQUESTNAME", palletList.get(0).get("PRODUCTREQUESTNAME").toString());
		udfs.put("PRODUCTSPECNAME", palletList.get(0).get("PRODUCTSPECNAME").toString());
		udfs.put("LOCATION", location);
		udfs.put("DOMESTICEXPORT", palletList.get(0).get("DOMESTICEXPORT").toString());
		udfs.put("STOCKSTATE", GenericServiceProxy.getConstantMap().FGMS_STOCKSTATE_STOCKED);
		udfs.put("STOCKINTIME", eventInfo.getEventTime().toString());
		udfs.put("STOCKINUSER", eventInfo.getEventUser());
		udfs.put("HOLDSTATE", "N");
		udfs.put("OUTSOURCE", palletList.get(0).get("OUTSOURCE").toString());
		udfs.put("ORIGINALFACTORY", palletList.get(0).get("ORIGINALFACTORY").toString());
		createInfo.setUdfs(udfs);
		
		ProcessGroup pData = ProcessGroupServiceProxy.getProcessGroupService().create(eventInfo, createInfo);
		
		//UPDATE PALLET
		//updateDurable(processData,palletName);
		
		// Increment Location Pallet Quantity
		incrementLocationPalletQty(locationData, 1);
		
		// Set MES Receive Flag
		SetMESReceiveFlag(palletName, "Y");
		
		// Add New Work Order to Body Message
		SMessageUtil.addItemToBody(doc, "STOCKINPALLETNAME", palletName);
		
		
		//send wo Info to ERP
		int productQty = productListByPallet.size();
		String pQty = String.valueOf(productQty);
		updateFGMSProductRequestInfo(pData,eventInfo,locationData,pQty);
		
		return doc;
	}
	
	@SuppressWarnings("unchecked")
	private List<ListOrderedMap> getProductListByPallet(String palletName) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT P.PRODUCTNAME, ");
		sql.append("        P.LOTNAME, ");
		sql.append("        P.DURABLENAME, ");
		sql.append("        P.BOXNAME, ");
		sql.append("        P.PALLETNAME, ");
		sql.append("        P.PRODUCTREQUESTNAME, ");
		sql.append("        P.PRODUCTSPECNAME, ");
		sql.append("        P.PRODUCTTYPE, ");
		sql.append("        P.SUBPRODUCTTYPE, ");
		sql.append("        P.PRODUCTGRADE, ");
		sql.append("        P.CRATENAME, ");
		sql.append("        P.RECEIVEFLAG, ");
		sql.append("        P.DOMESTICEXPORT, ");
		sql.append("        P.PRODUCTIONTYPE, ");
		sql.append("        p.OUTSOURCE,");
		sql.append("        P.ORIGINALFACTORY");
		sql.append("   FROM CT_SHIPPRODUCT@MES P ");
		sql.append("  WHERE 1 = 1 AND P.PALLETNAME = :PALLETNAME");
		sql.append("  ORDER BY P.PRODUCTNAME ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PALLETNAME", palletName);
		
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
	private List<ListOrderedMap> getBoxListByPallet(String palletName) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT B.BOXNAME, ");
		sql.append("        B.DURABLENAME, ");
		sql.append("        B.PALLETNAME, ");
		sql.append("        B.PRODUCTREQUESTNAME, ");
		sql.append("        B.MATERIALTYPE, ");
		sql.append("        B.DETAILMATERIALTYPE, ");
		sql.append("        B.MATERIALQUANTITY, ");
		sql.append("        B.PRODUCTSPECNAME, ");
		sql.append("        B.RECEIVEFLAG, ");
		sql.append("        B.DOMESTICEXPORT, ");
		sql.append("        B.OUTSOURCE,");
		sql.append("        B.ORIGINALFACTORY,");
		sql.append("        B.PRODUCTIONTYPE ");
		sql.append("   FROM CT_SHIPBOX@MES B ");
		sql.append("  WHERE B.PALLETNAME = :PALLETNAME ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PALLETNAME", palletName);
		
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
	private List<ListOrderedMap> getPalletInfo(String palletName) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT B.PALLETNAME, ");
		sql.append("        B.PRODUCTREQUESTNAME, ");
		sql.append("        B.MATERIALTYPE, ");
		sql.append("        B.DETAILMATERIALTYPE, ");
		sql.append("        B.MATERIALQUANTITY, ");
		sql.append("        B.PRODUCTSPECNAME, ");
		sql.append("        B.RECEIVEFLAG, ");
		sql.append("        B.DOMESTICEXPORT, ");
		sql.append("        B.OUTSOURCE,");
		sql.append("        B.ORIGINALFACTORY,");
		sql.append("        B.PRODUCTIONTYPE, ");
		sql.append("        B.DESTINATIONFACTORYNAME ");
		sql.append("   FROM CT_SHIPPALLET@MES B ");
		sql.append("  WHERE B.PALLETNAME = :PALLETNAME ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PALLETNAME", palletName);
		
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
		sqlPallet.append("    SET RECEIVEFLAG = 'Y' ");
		sqlPallet.append("  WHERE 1 = 1 ");
		sqlPallet.append("    AND DESTINATIONFACTORYNAME = 'FGI' ");
		sqlPallet.append("    AND PALLETNAME = :PALLETNAME ");
		
		StringBuilder sqlBox = new StringBuilder();
		sqlBox.append(" UPDATE CT_SHIPBOX@MES ");
		sqlBox.append("    SET RECEIVEFLAG = 'Y' ");
		sqlBox.append("  WHERE 1 = 1 ");
		sqlBox.append("    AND PALLETNAME = :PALLETNAME ");
		
		StringBuilder sqlProduct = new StringBuilder();
		sqlProduct.append(" UPDATE CT_SHIPPRODUCT@MES ");
		sqlProduct.append("    SET RECEIVEFLAG = 'Y' ");
		sqlProduct.append("  WHERE 1 = 1 ");
		sqlProduct.append("    AND PALLETNAME = :PALLETNAME ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PALLETNAME", palletName);

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
	
	@SuppressWarnings("unchecked")
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
	public static String createProcessGroupNaming() throws CustomException
	{
		try
		{
			Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
			
			nameRuleAttrMap.put("PG", "PG");
			
			long quantity = 1;
			List<String> GroupList = CommonUtil.generateNameByNamingRule("ProcessGroupNaming", nameRuleAttrMap, quantity);
			String processGroupName = GroupList.get(0);
			
			return processGroupName;
		}
		catch(Exception ex)
		{
			throw new CustomException("SYS-9001");
		}
	}
	
	public void  updateDurable(ProcessGroup processData,String durableName) throws CustomException
	{
		try
		{
			String queryString = " UPDATE PROCESSGROUP SET DURABLENAME = ? WHERE PROCESSGROUPNAME = ? ";
			
			
			
			List<Object[]> UpdateArgList = new ArrayList<Object[]>();
			List<Object> bindList = new ArrayList<Object>();
			
			bindList.add(durableName);
			bindList.add(processData.getKey().getProcessGroupName());
			UpdateArgList.add(bindList.toArray());
			
			GenericServiceProxy.getSqlMesTemplate().update(queryString, UpdateArgList);
			
			
		}
		catch(Exception ex)
		{
			throw new CustomException("SYS-9001");
		}
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
			createInfo.setInfoResultStatus("Stocked");
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
