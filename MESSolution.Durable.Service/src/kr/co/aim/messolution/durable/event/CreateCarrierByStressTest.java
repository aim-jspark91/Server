package kr.co.aim.messolution.durable.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.CreateInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.XmlUtil;

import org.jdom.Document;
import org.jdom.Element;

public class CreateCarrierByStressTest extends SyncHandler {

	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", this.getEventUser(), this.getEventComment(), "", "");
		
		List<Element> eleDurableList = new ArrayList<Element>();
		
		String sFactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String sDurableSpec = SMessageUtil.getBodyItemValue(doc, "DURABLESPECNAME", true);
		String sCapacity = SMessageUtil.getBodyItemValue(doc, "CAPACITY", true);
		String sShop = SMessageUtil.getBodyItemValue(doc, "FACTORYCODE", true);
		String sCapaCode = SMessageUtil.getBodyItemValue(doc, "DURABLETYPECODE", true);
		
		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("CARRIERID", "C");
		nameRuleAttrMap.put("SHOP", sShop);
		nameRuleAttrMap.put("LINENO", "1");
		nameRuleAttrMap.put("CARRIERTYPECODE", sCapaCode);
		
		//if Count Exsist, Case 1, else Case 2.
		//Case 1:Nomal Create Carrier. Case 2:Create Carrier By Stress Test
		try{
			String sQuantity = SMessageUtil.getBodyItemValue(doc, "COUNT", true);
			
			List<String> durableList = CommonUtil.generateNameByNamingRule("CarrierNaming", nameRuleAttrMap, Integer.parseInt(sQuantity));
			
			for (String durableName : durableList)
			{
				String strDurableName = durableName;
				
				//Check DurableName Exist
				MESDurableServiceProxy.getDurableServiceImpl().checkExistDurable(strDurableName);
		
				CreateInfo createInfo =  MESDurableServiceProxy.getDurableInfoUtil().createInfo(strDurableName, sDurableSpec, sCapacity, sFactoryName);
				
				Durable newDurable = MESDurableServiceProxy.getDurableServiceImpl().create(strDurableName, createInfo, eventInfo);
				 
				eleDurableList.add(setCreatedDurableList(newDurable));
			}
		}
		catch(Exception ex)
		{
			Element eCarrierList = SMessageUtil.getBodySequenceItem(doc, "DURABLELIST", true);
			
			for (@SuppressWarnings("rawtypes")
			Iterator itLot = eCarrierList.getChildren().iterator(); itLot.hasNext();)
			{
				Element eLot = (Element) itLot.next();
				String strDurableName = SMessageUtil.getChildText(eLot, "DURABLENAME", true);

				//Check DurableName Exist
				MESDurableServiceProxy.getDurableServiceImpl().checkExistDurable(strDurableName);
		
				CreateInfo createInfo =  MESDurableServiceProxy.getDurableInfoUtil().createInfo(strDurableName, sDurableSpec, sCapacity, sFactoryName);
				
				Durable newDurable = MESDurableServiceProxy.getDurableServiceImpl().create(strDurableName, createInfo, eventInfo);
				 
				eleDurableList.add(setCreatedDurableList(newDurable));
			}
		}
	    
	    //call by value so that reply would be modified
		XmlUtil.setSubChildren(SMessageUtil.getBodyElement(doc), "DURABLELIST", eleDurableList);
	    
		return doc;
	}
	
	/**
	 * scribe Durable as form of Element type
	 * @author JHYEOM
	 * @since 2015.01.05
	 * @param durableData
	 * @return
	 */
	private Element setCreatedDurableList(Durable durableData)
	{
		Element eleDurable = new Element("DURABLE");
		 
		try 
		{
			XmlUtil.addElement(eleDurable, "FACTORYNAME", durableData.getFactoryName());
			XmlUtil.addElement(eleDurable, "DURABLENAME", durableData.getKey().getDurableName());
			XmlUtil.addElement(eleDurable, "DURABLESPECNAME", durableData.getDurableSpecName());
			XmlUtil.addElement(eleDurable, "CAPACITY", String.valueOf((long)durableData.getCapacity()));
			XmlUtil.addElement(eleDurable, "TIMEUSEDLIMIT", String.valueOf((double)durableData.getTimeUsedLimit()));
		}
		catch (Exception ex)
		{
			eventLog.warn(String.format("Scribing Lot[%s] is failed so that skip", durableData.getKey().getDurableName()));
		}
		
		return eleDurable;
	}

}
