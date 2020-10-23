package kr.co.aim.messolution.durable.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.CreateInfo;
import kr.co.aim.greentrack.durable.management.info.DirtyInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.name.NameServiceProxy;
import kr.co.aim.greentrack.name.management.data.NameGeneratorRuleAttrDef;
import kr.co.aim.greentrack.name.management.data.NameGeneratorRuleAttrDefKey;

import org.jdom.Document;
import org.jdom.Element;

public class CreateCarrier extends SyncHandler {

	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", this.getEventUser(), this.getEventComment(), "", "");
		
		List<Element> eleDurableList = new ArrayList<Element>();
		
		String sFactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String sdurableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", false);
		String sDurableSpec = SMessageUtil.getBodyItemValue(doc, "DURABLESPECNAME", true);
		String sCapacity = SMessageUtil.getBodyItemValue(doc, "CAPACITY", true);
		String sNamingRule = SMessageUtil.getBodyItemValue(doc, "NAMINGRULE", true);
		String sQuantity = SMessageUtil.getBodyItemValue(doc, "COUNT", true);
		String sSize = SMessageUtil.getBodyItemValue(doc, "SIZE", false);
		Boolean checkname;
		
		if(!sdurableName.isEmpty())
		{
			checkname = MESDurableServiceProxy.getDurableServiceImpl().checkExistDurable(sdurableName);	
			if(checkname){
				throw new CustomException("MASK-0001",sdurableName);
			}
			
			//2018.10.16 dmlee : Check Naming Rule
			if(!this.checkCSTNamingRule(sdurableName, sNamingRule))
			{
				throw new CustomException("CSTNAMING-0001",sdurableName);
			}
			//2018.10.16 dmlee : ------------------
			
			CreateInfo createInfo =  MESDurableServiceProxy.getDurableInfoUtil().createInfo(sdurableName, sDurableSpec, sCapacity, sFactoryName);
			
			Durable newDurable = MESDurableServiceProxy.getDurableServiceImpl().create(sdurableName, createInfo, eventInfo);
			
			//Change Default CST State : Dirty
			setDirtyDurable(newDurable, eventInfo);
			
			eleDurableList.add(setCreatedDurableList(newDurable));
		}
		else{
			List<String> argSeq = new ArrayList<String>();
			if(!sSize.isEmpty()){
				argSeq.add("D5");
				argSeq.add(sSize);
			}
			List<String> durableList = NameServiceProxy.getNameGeneratorRuleDefService().generateName(sNamingRule, argSeq, Integer.parseInt(sQuantity));

			for (String durableName : durableList)
			{
				String strDurableName = durableName;
				
				try {
					//Check DurableName Exist
					MESDurableServiceProxy.getDurableServiceImpl().checkExistDurable(strDurableName);	
				} catch (Exception e) {
					continue;
				}

				CreateInfo createInfo =  MESDurableServiceProxy.getDurableInfoUtil().createInfo(strDurableName, sDurableSpec, sCapacity, sFactoryName);
				
				// Added by smkang on 2018.11.01 - EventName is recorded with 'Dirty' from 2nd carrier, it is a problem.
				eventInfo.setEventName("Create");
				
				Durable newDurable = MESDurableServiceProxy.getDurableServiceImpl().create(strDurableName, createInfo, eventInfo);
				
				//Change Default CST State : Dirty
				setDirtyDurable(newDurable, eventInfo);
				
				eleDurableList.add(setCreatedDurableList(newDurable));
			}
		    
		}
		
		//call by value so that reply would be modified
		XmlUtil.setSubChildren(SMessageUtil.getBodyElement(doc), "DURABLELIST", eleDurableList);
		
/*		// Added by smkang on 2018.10.02 - According to EDO's request, carrier data should be synchronized with shared factory.
		List<Element> durableElementList = SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", false);
		if (durableElementList != null && durableElementList.size() > 0)
			MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(doc, durableElementList.get(0).getChildText("DURABLENAME"));
	    */
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
			XmlUtil.addElement(eleDurable, "TIMEUSEDLIMIT", String.valueOf((long)durableData.getTimeUsedLimit()));
		}
		catch (Exception ex)
		{
			eventLog.warn(String.format("Scribing Lot[%s] is failed so that skip", durableData.getKey().getDurableName()));
		}
		
		return eleDurable;
	}
	
	private void setDirtyDurable(Durable durableData, EventInfo eventInfo)
	{
		eventInfo.setEventName("Dirty");
		
		DirtyInfo dirtyInfo = new DirtyInfo();
		dirtyInfo.setUdfs(durableData.getUdfs());
		MESDurableServiceProxy.getDurableServiceImpl().dirty(durableData, dirtyInfo, eventInfo);
	}
	
	private boolean checkCSTNamingRule(String cstName, String namingRule)
	{
		NameGeneratorRuleAttrDefKey nameGeneratorRuleAttrDefKey = new NameGeneratorRuleAttrDefKey(namingRule, 0);
		NameGeneratorRuleAttrDef nameGeneratorRuleAttrDef = NameServiceProxy.getNameGeneratorRuleAttrDefService().selectByKey(nameGeneratorRuleAttrDefKey);
		
		String prefixValue = nameGeneratorRuleAttrDef.getSectionValue();
		
		String pattern = "^"+prefixValue+"[0-9]{4}$";
		
		if(!Pattern.matches(pattern, cstName))
		{
			return false;
		}
		
		return true;
	}

}
