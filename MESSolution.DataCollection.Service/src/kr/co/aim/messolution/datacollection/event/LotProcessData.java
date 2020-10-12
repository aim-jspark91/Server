package kr.co.aim.messolution.datacollection.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import kr.co.aim.messolution.datacollection.MESEDCServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.datacollection.DataCollectionServiceProxy;
import kr.co.aim.greentrack.datacollection.management.data.DCSpec;
import kr.co.aim.greentrack.datacollection.management.data.DCSpecItem;
import kr.co.aim.greentrack.datacollection.management.data.DCSpecKey;
import kr.co.aim.greentrack.datacollection.management.info.ext.SampleData;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.collections.map.ListOrderedMap;
import org.jdom.Document;
import org.jdom.Element;

public class LotProcessData extends AsyncHandler {
 
	@Override
	public void doWorks(Document doc)
		throws CustomException  
	{
		String machineName 			= SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String lotName		 		= SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String carrierName		 	= SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String machineRecipeName 	= SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", false);
		String productSpecName 		= SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", false);
		
		Element eUnitList = SMessageUtil.getBodySequenceItem(doc, "UNITLIST", true);
		
		for (@SuppressWarnings("rawtypes")
		Iterator iUnit = eUnitList.getChildren().iterator(); iUnit.hasNext();)
		{
			Element eUnit = (Element) iUnit.next();
			String unitName 			= SMessageUtil.getChildText(eUnit, "UNITNAME", true);
			String subUnitName 			= SMessageUtil.getChildText(eUnit, "SUBUNITNAME", false);
			
			//overriding target tool
			if(!subUnitName.isEmpty())
			{
				unitName = subUnitName;
			}

			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			
			DCSpec DCSpecData = this.getDCSpec(lotData, machineName, unitName);
			
			List<Element> itemList = eUnit.getChildren("ITEMLIST");
			
			List<DCSpecItem> DCSpecItemList = MESEDCServiceProxy.getDataCollectionServiceUtil().getDCSpecItem(DCSpecData, itemList);
			
			//sample data per material
			List<SampleData> sds = new ArrayList<SampleData>();
			SampleData sd = MESEDCServiceProxy.getDataCollectionInfoUtil().getSampleData(lotName, DCSpecData.getMaterialType(), itemList);
			sds.add(sd);
			
			long dataId = MESEDCServiceProxy.getDataCollectionServiceImpl().collectData(DCSpecData, lotData.getFactoryName(), machineName, machineRecipeName,
																		lotData.getKey().getLotName(),
																		lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(),
																		sds, DCSpecItemList,
																		DCSpecData.getUdfs(), getEventUser(), getEventComment());
			
			//Create CheckSPC Message
			Document spcDoc = writeCheckSPCMessage(doc, unitName, String.valueOf(dataId));
			
			//Send EDC Server : Create Alarm
			String replySubject = GenericServiceProxy.getESBServive().getSendSubject("SPCsvr");
			GenericServiceProxy.getESBServive().sendBySender(replySubject, spcDoc, "EDCSender");
		}
	}
	
	@SuppressWarnings("unused")
	private Document writeCheckSPCMessage(Document doc, String unitName, String dcDataId)
			throws CustomException
		{
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "CheckSPC");

			Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);
			
			Element element1 = new Element("MACHINENAME");
			element1.setText(SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true));
			eleBodyTemp.addContent(element1);
			
			Element element2 = new Element("UNITNAME");
			element2.setText(unitName);
			eleBodyTemp.addContent(element2);
			
			Element element3 = new Element("LOTNAME");
			element3.setText(SMessageUtil.getBodyItemValue(doc, "LOTNAME", true));
			eleBodyTemp.addContent(element3);
			
			Element element4 = new Element("CARRIERNAME");
			element4.setText(SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false));
			eleBodyTemp.addContent(element4);
			
			Element element5 = new Element("DCDATAID");
			element5.setText(dcDataId);
			eleBodyTemp.addContent(element5);

			boolean result = doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
			
			//overwrite
			doc.getRootElement().addContent(eleBodyTemp);
			
			return doc;
		}
	
	@SuppressWarnings("unchecked")
	private DCSpec getDCSpec(Lot lotData, String machineName, String unitName)
		throws CustomException
	{
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT P.unitName, P.DCSpecName " + "\n")
					.append("    FROM TMPolicy C, POSDCSpec P " + "\n")
					.append("WHERE C.conditionId = P.conditionId " + "\n")
					.append("    AND C.factoryName = ? " + "\n")
					.append("    AND C.machineName = ? " + "\n")
					.append("    AND P.unitName = ? " + "\n");
		
		Object[] bindArray = new Object[] {lotData.getFactoryName(), machineName, unitName};
		
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
			throw new CustomException("SYS-9999", "POSPolicy", String.format("[%s, %s, %s] is not defined in POS DC spec policy", lotData.getFactoryName(), machineName, unitName));
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
