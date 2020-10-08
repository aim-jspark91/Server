package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.data.DurableSpecKey;
import kr.co.aim.greentrack.durable.management.info.CreateInfo;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.XmlUtil;

import org.jdom.Document;
import org.jdom.Element;

public class CreateProbe extends SyncHandler 
{
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", this.getEventUser(), this.getEventComment(), "", "");
		
		List<Element> eleDurableList = new ArrayList<Element>();
		
		for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "PROBELIST", true))
		{
			String sFactoryName = SMessageUtil.getChildText(eledur, "FACTORYNAME", true);
			String sDurableSpecName = SMessageUtil.getChildText(eledur, "DURABLESPECNAME", true);
			String probeName = SMessageUtil.getChildText(eledur, "PROBENAME", true);
			
			List <Durable> sqlResult = new ArrayList <Durable>();
			try
			{
				sqlResult = DurableServiceProxy.getDurableService().select("DURABLENAME = ?", new Object[] {probeName});
				
				if (sqlResult != null && sqlResult.size() > 0)
				{
					throw new CustomException("PROBE-0001", probeName);
				}
					
			}
			catch (NotFoundSignal ne)
			{
				sqlResult = new ArrayList <Durable>();
			}
			catch(Exception e)
			{
				throw new CustomException("PROBE-0001", probeName);
			}
			
			DurableSpecKey keyInfo = new DurableSpecKey();
			keyInfo.setDurableSpecName(sDurableSpecName);
			keyInfo.setFactoryName(sFactoryName);
			keyInfo.setDurableSpecVersion("00001");
			
			DurableSpec probeSpecInfo = DurableServiceProxy.getDurableSpecService().selectByKey(keyInfo);

			HashMap<String, String> udfs = new HashMap<String, String>();
			udfs.put("MACHINENAME", "");
			udfs.put("TOTALUSEDCOUNT","0");
			udfs.put("DURABLEHOLDSTATE", "N");
			udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);

			CreateInfo createInfo =  MESDurableServiceProxy.getDurableInfoUtil().createMaskInfo
											   (probeName, 
										        sFactoryName,
										        probeSpecInfo.getDurableType(), 
										        probeSpecInfo.getKey().getDurableSpecName(),
										        probeSpecInfo.getDefaultCapacity(), 
										        probeSpecInfo.getDurationUsedLimit(), 
										        probeSpecInfo.getTimeUsedLimit(), 
												udfs);
			
			Durable newDurable = MESDurableServiceProxy.getDurableServiceImpl().create(probeName, createInfo, eventInfo);
			
			eleDurableList.add(setCreatedDurableList(newDurable));
			
		}
		XmlUtil.setSubChildren(SMessageUtil.getBodyElement(doc), "PROBELIST", eleDurableList);
		
		return doc;
	}
	
	private Element setCreatedDurableList(Durable durableData)
	{
		Element eleDurable = new Element("DURABLE");
		 
		try 
		{
			XmlUtil.addElement(eleDurable, "DURABLENAME", durableData.getKey().getDurableName());
			XmlUtil.addElement(eleDurable, "DURABLETYPE", durableData.getDurableType());
			XmlUtil.addElement(eleDurable, "DURABLESPECNAME", durableData.getDurableSpecName());
			XmlUtil.addElement(eleDurable, "FACTORYNAME", durableData.getFactoryName());
			XmlUtil.addElement(eleDurable, "DURABLESTATE", durableData.getDurableState());
			XmlUtil.addElement(eleDurable, "TRANSPORTSTATE", CommonUtil.getValue(durableData.getUdfs(), "TRANSPORTSTATE"));
			XmlUtil.addElement(eleDurable, "DURABLECLEANSTATE", durableData.getDurableCleanState());
		}
		catch (Exception ex)
		{
			eventLog.warn(String.format("Send Probe[%s] grid info failed", durableData.getKey().getDurableName()));
		}
		
		return eleDurable;
	}
}

