package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.CreateInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.XmlUtil;

import org.jdom.Document;
import org.jdom.Element;

public class CreateProbeID extends SyncHandler{

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String sFactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String sDurableSpec = SMessageUtil.getBodyItemValue(doc, "DURABLESPECNAME", true);
	    String sDurableName = SMessageUtil.getBodyItemValue(doc, "PROBEID", true);
	    
	  //Check DurableName Exist
	    boolean existProbeData =  MESDurableServiceProxy.getDurableServiceImpl().checkExistDurable(sDurableName);
	    
	    if(existProbeData == true)
		{
	    	throw new CustomException("MASK-0001");
			
		}else 
		{
			List<Element> eleDurableList = new ArrayList<Element>();
			
			CreateInfo createInfo =  MESDurableServiceProxy.getDurableInfoUtil().createInfo(sDurableName, sDurableSpec, "1", sFactoryName);
			
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", this.getEventUser(), this.getEventComment(), "", "");
			
			Durable newDurable = MESDurableServiceProxy.getDurableServiceImpl().create(sDurableName, createInfo, eventInfo);
			 
			eleDurableList.add(setCreatedDurableList(newDurable,sFactoryName));
			
			//call by value so that reply would be modified
			XmlUtil.setSubChildren(SMessageUtil.getBodyElement(doc), "DURABLELIST", eleDurableList);
		}

		return doc;
	}
	
	/**
	 * @author Lhkim
	 * @since 2015.02.14
	 * @param durableData
	 * @return  Element
	 */
	private Element setCreatedDurableList(Durable durableData,String factoryName)
	{
		Element eleDurable = new Element("DURABLE");
		 
		try 
		{
			XmlUtil.addElement(eleDurable, "FACTORYNAME", factoryName);
			XmlUtil.addElement(eleDurable, "PROBESPECNAME", durableData.getDurableSpecName());
			XmlUtil.addElement(eleDurable, "PROBEID", durableData.getKey().getDurableName());
			
		}
		catch (Exception ex)
		{
			eventLog.warn(String.format("Scribing Lot[%s] is failed so that skip", durableData.getKey().getDurableName()));
		}
		
		return eleDurable;
	}

}
