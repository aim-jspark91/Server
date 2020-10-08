package kr.co.aim.messolution.consumable.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.info.CreateInfo;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CreateCrate extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		
		EventInfo deleteEventInfo = EventInfoUtil.makeEventInfo("DeleteCrate", getEventUser(), getEventComment(), null, null);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventName("Create");
		StringBuilder strComment = new StringBuilder();
		
		List<Element> eleCrateList = SMessageUtil.getBodySequenceItemList(doc, "CRATELIST", true);
		List<Element> crateNameList = new ArrayList<Element>();
		
		for (Element eleCrate : eleCrateList)
		{
			String sFactoryName = SMessageUtil.getChildText(eleCrate, "FACTORYNAME", true);
			String sConsumableSpecName = SMessageUtil.getChildText(eleCrate, "CONSUMABLESPECNAME", true);
			String sGlassDefaultQuantity = SMessageUtil.getChildText(eleCrate, "CREATEQUANTITY", true);
			String sConsoumableName = SMessageUtil.getChildText(eleCrate, "CONSUMABLENAME", false);
			String expirationDate= SMessageUtil.getChildText(eleCrate, "EXPIRATIONDATE", true);
			
			if(sGlassDefaultQuantity.length() == 1)
			{
				sGlassDefaultQuantity="00"+sGlassDefaultQuantity;
			}
			else if(sGlassDefaultQuantity.length() == 2)
			{
				sGlassDefaultQuantity="0"+sGlassDefaultQuantity;
			}
			
			if(sConsoumableName.isEmpty()) 
			{
				//Auto Naming
				ConsumableSpec crateSpec = GenericServiceProxy.getSpecUtil().getConsumableSpec(sFactoryName, sConsumableSpecName, "");
				
				Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
				nameRuleAttrMap.put("CRATESPEC", crateSpec.getKey().getConsumableSpecName());
				nameRuleAttrMap.put("GLASSQUANTITY", sGlassDefaultQuantity);
				
				List<String> crateName=CommonUtil.generateNameByNamingRule("CrateNaming", nameRuleAttrMap, 1);
				
				Map<String, String> udfs = CommonUtil.setNamedValueSequence(eleCrate, Consumable.class.getSimpleName());
				udfs.put("EXPIRATIONDATE", expirationDate);
				
				CreateInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().createInfo(sFactoryName, "", crateName.get(0),
																			crateSpec.getKey().getConsumableSpecName(), crateSpec.getKey().getConsumableSpecVersion(),
																			crateSpec.getConsumableType(), Long.parseLong(sGlassDefaultQuantity),
																			udfs);
				
				MESConsumableServiceProxy.getConsumableServiceImpl().createCrate(eventInfo, crateName.get(0), transitionInfo);
										
				Consumable crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(crateName.get(0));
				
				strComment.append("ConsumableName").append("[").append(crateData.getKey().getConsumableName()).append("]").append("\n");
				

				Element crate =  new Element("CRATE");
				XmlUtil.addElement(crate, "CRATENAME", crateName.get(0));
				crateNameList.add(crate);
			}
			else {
				
				// Start 2019.09.11 Modfiy By Park Jeong Su Mantis 4706
				
				Consumable crateData = null;
				try {
					crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(sConsoumableName);
					
					if(crateData!=null && !StringUtils.equals(GenericServiceProxy.getConstantMap().Cons_NotAvailable, crateData.getConsumableState())){
						throw new CustomException("CONS-0001", crateData.getKey().getConsumableName(), crateData.getConsumableState());
					}
					
					
					SetEventInfo setEventInfo = MESConsumableServiceProxy.getConsumableInfoUtil().setEventInfo(crateData, crateData.getAreaName()); 

					MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(crateData.getKey().getConsumableName(), setEventInfo, deleteEventInfo);
					
					ConsumableServiceProxy.getConsumableService().delete(crateData.getKey());
					
				} catch (Exception e) {
					if(crateData!=null && !StringUtils.equals(GenericServiceProxy.getConstantMap().Cons_NotAvailable, crateData.getConsumableState())){
						throw new CustomException("CONS-0001", crateData.getKey().getConsumableName(), crateData.getConsumableState());
					}
				}
				
				// End 2019.09.11 Modfiy By Park Jeong Su Mantis 4706
				
				//Manual Naming
				ConsumableSpec crateSpec = GenericServiceProxy.getSpecUtil().getConsumableSpec(sFactoryName, sConsumableSpecName, "");
				
				Map<String, String> udfs = CommonUtil.setNamedValueSequence(eleCrate, Consumable.class.getSimpleName());
				udfs.put("EXPIRATIONDATE", expirationDate);
				
				CreateInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().createInfo(sFactoryName, "", sConsoumableName,
																			crateSpec.getKey().getConsumableSpecName(), crateSpec.getKey().getConsumableSpecVersion(),
																			crateSpec.getConsumableType(), Long.parseLong(sGlassDefaultQuantity),
																			udfs);
				
				MESConsumableServiceProxy.getConsumableServiceImpl().createCrate(eventInfo, sConsoumableName, transitionInfo);
										
				crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(sConsoumableName);
				
				strComment.append("ConsumableName").append("[").append(crateData.getKey().getConsumableName()).append("]").append("\n");
			}				
		}
		
		if(crateNameList.size() > 0)
		{
			XmlUtil.setSubChildren(SMessageUtil.getBodyElement(doc), "CRATENAMELIST", crateNameList);
		}
		setNextInfo(doc, strComment);
		
		return doc;
	}
	
	private void setNextInfo(Document doc, StringBuilder strComment)
	{
		try
		{			
			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, strComment.toString());
		}
		catch (Exception ex)
		{
			eventLog.warn("Note after Crate is nothing");
		}
	}

}
