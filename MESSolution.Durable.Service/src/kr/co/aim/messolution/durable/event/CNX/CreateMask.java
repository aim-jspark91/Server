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
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.durable.management.data.DurableHistoryKey;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.data.DurableSpecKey;
import kr.co.aim.greentrack.durable.management.info.CreateInfo;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.XmlUtil;

import org.jdom.Document;
import org.jdom.Element;

public class CreateMask extends SyncHandler {
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", this.getEventUser(), this.getEventComment(), "", "");
		
		List<Element> eleDurableList = new ArrayList<Element>();
		
		for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", true))
		{
			// Parsing
			String sFactoryName = SMessageUtil.getChildText(eledur, "FACTORYNAME", true);
			String sDurableSpecName = SMessageUtil.getChildText(eledur, "DURABLESPECNAME", true);
			String sMaskTitle = SMessageUtil.getChildText(eledur, "MASKNAME", true);
			//String sMaskBarcodeID = SMessageUtil.getChildText(eledur, "MASKBARCODEID", true);
			//String sManufactureDate = SMessageUtil.getChildText(eledur, "MANUFACTUREDATE", true);
			//String sMaskID = SMessageUtil.getChildText(eledur, "MASKID", false);
			
			// Validation
			List <Durable> sqlResult = new ArrayList <Durable>();
			try
			{
				sqlResult = DurableServiceProxy.getDurableService().select("DURABLENAME = ?", new Object[] {sMaskTitle});
				if (sqlResult.size() > 0)
					throw new CustomException("MASK-0021", sMaskTitle);
			}
			catch (NotFoundSignal ne)
			{
				sqlResult = new ArrayList <Durable>();
			}
			catch(Exception e)
			{
				throw new CustomException("MASK-0021", sMaskTitle);
			}
			
			// Preparation
			DurableSpecKey keyInfo = new DurableSpecKey();
			keyInfo.setDurableSpecName(sDurableSpecName);
			keyInfo.setFactoryName(sFactoryName);
			keyInfo.setDurableSpecVersion("00001");
			
			DurableSpec maskSpecInfo = DurableServiceProxy.getDurableSpecService().selectByKey(keyInfo);

			HashMap<String, String> udfs = new HashMap<String, String>();
			
			
			udfs.put("MACHINENAME", "");
			udfs.put("TOTALUSEDCOUNT","0");
			udfs.put("DURABLEHOLDSTATE", "N");
			udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
			//udfs.put("MASKTITLE", sMaskTitle);
			//udfs.put("INSPECTSTATE", "Inspected");
			//udfs.put("MANUFACTUREDATE", sManufactureDate);
			//udfs.put("JUDGE", "OK");
			//udfs.put("MASKID", sMaskID);
			//udfs.put("REWORKSTATE", "NotInRework");
			
			CreateInfo createInfo =  MESDurableServiceProxy.getDurableInfoUtil().createMaskInfo
											   (sMaskTitle, 
										        sFactoryName,
												maskSpecInfo.getDurableType(), 
												maskSpecInfo.getKey().getDurableSpecName(),
												maskSpecInfo.getDefaultCapacity(), 
												maskSpecInfo.getDurationUsedLimit(), 
												maskSpecInfo.getTimeUsedLimit(), 
												udfs);
			
			// Execution
			Durable newDurable = MESDurableServiceProxy.getDurableServiceImpl().create(sMaskTitle, createInfo, eventInfo);
			
			// Modified by smkang on 2018.09.24 - Available state is used instead of UnMount state.
//			newDurable.setDurableState(GenericServiceProxy.getConstantMap().Cons_Unmount);
			newDurable.setDurableState(GenericServiceProxy.getConstantMap().Cons_Available);

			DurableServiceProxy.getDurableService().update(newDurable);
			
			DurableHistoryKey DurableHistory = new DurableHistoryKey();
			DurableHistory.setDurableName(newDurable.getKey().getDurableName());
			DurableHistory.setTimeKey(eventInfo.getEventTimeKey());
			
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			DurableHistory durableHistory = DurableServiceProxy.getDurableHistoryService().selectByKey(DurableHistory);
			DurableHistory durableHistory = DurableServiceProxy.getDurableHistoryService().selectByKeyForUpdate(DurableHistory);
		        
			// Modified by smkang on 2018.09.24 - Available state is used instead of UnMount state.
//			durableHistory.setDurableState(GenericServiceProxy.getConstantMap().Cons_Unmount);
			durableHistory.setDurableState(GenericServiceProxy.getConstantMap().Cons_Available);
			
			DurableServiceProxy.getDurableHistoryService().update(durableHistory);
			
			eleDurableList.add(setCreatedDurableList(newDurable));
			
		}
		XmlUtil.setSubChildren(SMessageUtil.getBodyElement(doc), "MASKLIST", eleDurableList);
		
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
			//XmlUtil.addElement(eleDurable, "MASKBARCODEID", durableData.getKey().getDurableName());
			//XmlUtil.addElement(eleDurable, "MASKTITLE", CommonUtil.getValue(durableData.getUdfs(), "MASKTITLE"));
			//XmlUtil.addElement(eleDurable, "MANUFACTUREDATE", CommonUtil.getValue(durableData.getUdfs(), "MANUFACTUREDATE"));			
			/*
			if(durableData.getFactoryName().equalsIgnoreCase("LTPS"))
			{
				XmlUtil.addElement(eleDurable, "MASKID", CommonUtil.getValue(durableData.getUdfs(), "MASKID"));
			}
			*/
		}
		catch (Exception ex)
		{
			eventLog.warn(String.format("Send Mask[%s] grid info failed", durableData.getKey().getDurableName()));
		}
		
		return eleDurable;
	}
}

