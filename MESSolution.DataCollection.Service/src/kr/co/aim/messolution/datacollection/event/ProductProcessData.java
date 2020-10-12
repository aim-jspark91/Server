package kr.co.aim.messolution.datacollection.event;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.datacollection.MESEDCServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.datacollection.DataCollectionServiceProxy;
import kr.co.aim.greentrack.datacollection.management.data.DCSpec;
import kr.co.aim.greentrack.datacollection.management.data.DCSpecItem;
import kr.co.aim.greentrack.datacollection.management.data.DCSpecKey;
import kr.co.aim.greentrack.datacollection.management.info.ext.SampleData;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.collections.map.ListOrderedMap;
import org.jdom.Document;
import org.jdom.Element;

public class ProductProcessData extends AsyncHandler {
 
	@Override
	public void doWorks(Document doc)
		throws CustomException  
	{
		String machineName 			= SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String lotName		 		= SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String carrierName		 	= SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String machineRecipeName 	= SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", false);
		String productSpecName 		= SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", false);
		String productName	 		= SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String unitName 			= SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String subUnitName 			= SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		
		//overriding target tool
		if(!subUnitName.isEmpty())
		{
			unitName = subUnitName;
		}

		Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
		
		DCSpec DCSpecData = this.getDCSpec(productData, machineName, unitName);
		
		List<Element> itemList = SMessageUtil.getBodySequenceItemList(doc, "ITEMLIST", true);
		
		List<DCSpecItem> DCSpecItemList = MESEDCServiceProxy.getDataCollectionServiceUtil().getDCSpecItem(DCSpecData, itemList);
		
		//sample data per material
		List<SampleData> sds = new ArrayList<SampleData>();
		SampleData sd = MESEDCServiceProxy.getDataCollectionInfoUtil().getSampleData(productName, DCSpecData.getMaterialType(), itemList);
		sds.add(sd);
		
		long dataId = MESEDCServiceProxy.getDataCollectionServiceImpl().collectData(DCSpecData, productData.getFactoryName(), machineName, machineRecipeName,
																	productData.getKey().getProductName(),
																	productData.getProductSpecName(), productData.getProcessFlowName(), productData.getProcessOperationName(),
																	sds, DCSpecItemList,
																	DCSpecData.getUdfs(), getEventUser(), getEventComment());
		
		//Send EDC Server : Check SPC
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "CheckSPC");
		SMessageUtil.addItemToBody(doc, "DCDATAID", String.valueOf(dataId));
		String replySubject = GenericServiceProxy.getESBServive().getSendSubject("SPCsvr");
		GenericServiceProxy.getESBServive().sendBySender(replySubject, doc, "EDCSender");
	}
	
	private DCSpec getDCSpec(Product productData, String machineName, String unitName)
		throws CustomException
	{
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT P.unitName, P.DCSpecName " + "\n")
					.append("    FROM TMPolicy C, POSDCSpec P " + "\n")
					.append("WHERE C.conditionId = P.conditionId " + "\n")
					.append("    AND C.factoryName = ? " + "\n")
					.append("    AND C.machineName = ? " + "\n")
					.append("    AND P.unitName = ? " + "\n");
		
		Object[] bindArray = new Object[] {productData.getFactoryName(), machineName, unitName};
		
		List<ListOrderedMap> result;
		try
		{
			result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuilder.toString(), bindArray);
		}
		catch (FrameworkErrorSignal fe)
		{
			result = new ArrayList<ListOrderedMap>();
		}
		
		if (result.size() < 1)
		{
			throw new CustomException("SYS-9999", "POSPolicy", String.format("[%s, %s, %s] is not defined in POS DC spec policy", productData.getFactoryName(), machineName, unitName));
		}
			
		
		String dcSpecName = CommonUtil.getValue(result.get(0), "DCSPECNAME");
		String dcSpecVersion = GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION;
		
		DCSpecKey keyInfo = new DCSpecKey();
		keyInfo.setDCSpecName(dcSpecName);
		keyInfo.setDCSpecVersion(dcSpecVersion);
		
		try
		{
			DCSpec dcSpecData = DataCollectionServiceProxy.getDCSpecService().selectByKey(keyInfo);
			
			return dcSpecData;
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-9001", "DCSpec");
		}
	}
}
