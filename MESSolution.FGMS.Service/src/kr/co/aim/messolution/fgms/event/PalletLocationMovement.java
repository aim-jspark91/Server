package kr.co.aim.messolution.fgms.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.greentrack.area.AreaServiceProxy;
import kr.co.aim.greentrack.area.management.data.Area;
import kr.co.aim.greentrack.area.management.data.AreaKey;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupKey;
import kr.co.aim.greentrack.processgroup.management.info.SetEventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class PalletLocationMovement extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String palletName = SMessageUtil.getBodyItemValue(doc, "PALLETNAME", true);		
		String areaName = SMessageUtil.getBodyItemValue(doc, "AREANAME", true);
		String location    = SMessageUtil.getBodyItemValue(doc,"LOCATION",true);
		List<Element> boxList = SMessageUtil.getBodySequenceItemList(doc, "BOXLIST", false);
		
		List<Element> elePalletList = new ArrayList<Element>();
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeLocation", getEventUser(), getEventComment(), "", "");
		
		//Pallet Data
		ProcessGroupKey palletKey = new ProcessGroupKey(palletName);
		ProcessGroup palletData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(palletKey);
		
		List<Area> areaList = new ArrayList<Area>(); 
		
		try
		{
			areaList = AreaServiceProxy.getAreaService().select("AREATYPE = ? AND PALLETNAME = ? ", new Object[] {"StorageArea", palletName});
		}		
		catch(Exception ex)
		{
			eventLog.error("Not Area List");
		}
		
		//if(areaList != null && areaList.size() > 0)
		//{
			//for (Area oldAreaData : areaList) 
			//{
				//update Old Area
				AreaKey locationKey = new AreaKey();
				locationKey.setAreaName(location);
				//Area oldAreaData = null;
				Area oldAreaData = AreaServiceProxy.getAreaService().selectByKey(locationKey);
				
				Map<String, String> oldAreaUdfs = oldAreaData.getUdfs();
				oldAreaUdfs.put("PALLETNAME", "");
				//oldAreaUdfs.put("LOCATIONSTATE", "Empty");
				oldLocationPalletQty(oldAreaData, 1);
				oldAreaData.setUdfs(oldAreaUdfs);
				
				AreaServiceProxy.getAreaService().update(oldAreaData);
			//}
		//}	
		
		//New Area Data
		AreaKey areaKey = new AreaKey();
		areaKey.setAreaName(areaName);
		Area areaData = AreaServiceProxy.getAreaService().selectByKey(areaKey);
		
		//update New Area
		Map<String, String> areaUdfs = palletData.getUdfs();
		areaUdfs.put("PALLETNAME", palletName);
		//areaUdfs.put("LOCATIONSTATE", "Full");
		incrementLocationPalletQty(areaData, 1);

		areaData.setUdfs(areaUdfs);
		AreaServiceProxy.getAreaService().update(areaData);
		
		//update Box
		for (Element elebox : boxList) 
		{
			String boxName = SMessageUtil.getChildText(elebox, "BOXNAME", false);
			
			//Box Data
			ProcessGroupKey boxKey = new ProcessGroupKey(boxName);
			ProcessGroup boxData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(boxKey);
			
			Map<String, String> boxUdfs = boxData.getUdfs();
			
			boxUdfs.put("LOCATION", areaName);		
			palletData.setUdfs(boxUdfs);
			
			ProcessGroupServiceProxy.getProcessGroupService().update(boxData);
			
		}
		
		//update Pallet
		Map<String, String> udfs = palletData.getUdfs();
		
		udfs.put("LOCATION", areaName);		
		palletData.setUdfs(udfs);		
		
		ProcessGroupServiceProxy.getProcessGroupService().update(palletData);
		
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs(udfs);
		
		MESProcessGroupServiceProxy.getProcessGroupServiceImpl().setEvent(palletData, setEventInfo, eventInfo);
				
		
		//call by value so that reply would be modified
		elePalletList.add(setCreatedPalletList(palletData));
		XmlUtil.setSubChildren(SMessageUtil.getBodyElement(doc), "PALLETLIST", elePalletList);
		
		return doc;
	}		
	
	private Element setCreatedPalletList(ProcessGroup palletData)
	{
		Element eleDurable = new Element("PALLET");
		 
		try 
		{
			XmlUtil.addElement(eleDurable, "PROCESSGROUPNAME", palletData.getKey().getProcessGroupName());
			XmlUtil.addElement(eleDurable, "LOCATION", palletData.getUdfs().get("LOCATION"));
			XmlUtil.addElement(eleDurable, "PRODUCTSPECNAME", palletData.getUdfs().get("PRODUCTSPECNAME"));
			XmlUtil.addElement(eleDurable, "PRODUCTREQUESTNAME", palletData.getUdfs().get("PRODUCTREQUESTNAME"));
			XmlUtil.addElement(eleDurable, "MATERIALQUANTITY", String.valueOf(palletData.getMaterialQuantity()));
		}
		catch (Exception ex)
		{
			eventLog.warn(String.format("Change Location Pallet[%s] is failed", palletData.getKey().getProcessGroupName()));
		}
		
		return eleDurable;
	}
	
	private void incrementLocationPalletQty(Area locationData, int palletQty) throws CustomException
	{
		// Update Location Info
		int iLocationCapa = Integer.parseInt(locationData.getUdfs().get("CAPACITY"));
		int iPalletQty = Integer.parseInt(locationData.getUdfs().get("PALLETQUANTITY")) + palletQty;
		
		Map<String, String> areaUdfs = new HashMap<String,String>();
		
		if(iLocationCapa <= iPalletQty)
		{
			areaUdfs.put("FULLSTATE", GenericServiceProxy.getConstantMap().FGMS_LOCATION_FULL);
		}
		else
		{
			areaUdfs.put("FULLSTATE", GenericServiceProxy.getConstantMap().FGMS_LOCATION_EMPTY);
		}
		
		areaUdfs.put("PALLETQUANTITY", String.valueOf(iPalletQty));
		locationData.setUdfs(areaUdfs);
		
		AreaServiceProxy.getAreaService().update(locationData);
	}
	
	private void oldLocationPalletQty(Area oldAreaData, int palletQty) throws CustomException
	{
		// Update Location Info
		int iLocationCapa = Integer.parseInt(oldAreaData.getUdfs().get("CAPACITY"));
		int iPalletQty = Integer.parseInt(oldAreaData.getUdfs().get("PALLETQUANTITY")) - palletQty;
		
		Map<String, String> areaUdfs = new HashMap<String,String>();
		
		if(iLocationCapa <= iPalletQty)
		{
			areaUdfs.put("FULLSTATE", GenericServiceProxy.getConstantMap().FGMS_LOCATION_FULL);
		}
		else
		{
			areaUdfs.put("FULLSTATE", GenericServiceProxy.getConstantMap().FGMS_LOCATION_EMPTY);
		}
		
		areaUdfs.put("PALLETQUANTITY", String.valueOf(iPalletQty));
		oldAreaData.setUdfs(areaUdfs);
		
		AreaServiceProxy.getAreaService().update(oldAreaData);
	}
	
}
