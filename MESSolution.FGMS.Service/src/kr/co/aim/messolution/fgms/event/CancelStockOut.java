package kr.co.aim.messolution.fgms.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.fgms.FGMSServiceProxy;
import kr.co.aim.messolution.fgms.management.data.Product;
import kr.co.aim.messolution.fgms.management.data.ShipRequest;
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

public class CancelStockOut extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String invoiceNo = SMessageUtil.getBodyItemValue(doc, "INVOICENO", true);
		List<Element> palletList = SMessageUtil.getBodySequenceItemList(doc, "PALLETLIST", true);
		List<Element> eleInvoiceList = new ArrayList<Element>();		
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelStockOut", getEventUser(), getEventComment(), "", "");
		
		ShipRequest shipRequestData = FGMSServiceProxy.getShipRequestService().selectByKey(false,  new Object[] {invoiceNo});
		
		shipRequestData.setShipRequestState("Confirmed");
		shipRequestData.setLastEventComment(eventInfo.getEventComment());
		shipRequestData.setLastEventName(eventInfo.getEventName());
		shipRequestData.setLastEventTime(eventInfo.getEventTime());
		shipRequestData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		shipRequestData.setLastEventUser(eventInfo.getEventUser());
		
		shipRequestData = FGMSServiceProxy.getShipRequestService().modify(eventInfo, shipRequestData);
		
		for (Element elePallet : palletList) 
		{
			String processGroupName = SMessageUtil.getChildText(elePallet, "PROCESSGROUPNAME", true);
			
			List<ProcessGroup> boxList = ProcessGroupServiceProxy.getProcessGroupService().select("SUPERPROCESSGROUPNAME = ? ", new Object[] {processGroupName});
			
			//Pallet Data
			ProcessGroupKey pKey = new ProcessGroupKey(processGroupName);
			ProcessGroup processGroupData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(pKey);
			ProcessGroup oldProcessGroupData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(pKey);
			
			Map<String, String> udfs = processGroupData.getUdfs();
			udfs.put("STOCKSTATE", "Stocked");
			udfs.put("LOCATION",oldProcessGroupData.getUdfs().get("OLDAREA"));
			udfs.put("OLDAREA", "");
			processGroupData.setUdfs(udfs);
			
			ProcessGroupServiceProxy.getProcessGroupService().update(processGroupData);
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.setUdfs(udfs);
			
			MESProcessGroupServiceProxy.getProcessGroupServiceImpl().setEvent(processGroupData, setEventInfo, eventInfo);
			
			for (ProcessGroup boxData : boxList) 
			{				
				Map<String, String> boxUdfs = boxData.getUdfs();
				boxUdfs.put("STOCKSTATE", "Stocked");
				udfs.put("LOCATION",boxData.getUdfs().get("OLDAREA"));
				udfs.put("OLDAREA", "");
				boxData.setUdfs(boxUdfs);
				boxData.setLastEventComment(eventInfo.getEventComment());
				boxData.setLastEventName(eventInfo.getEventName());
				boxData.setLastEventTime(eventInfo.getEventTime());
				boxData.setLastEventTimeKey(eventInfo.getEventTimeKey());
				boxData.setLastEventUser(eventInfo.getEventUser());
				
				ProcessGroupServiceProxy.getProcessGroupService().update(boxData);
				
				List<Product> prdList = FGMSServiceProxy.getProductService().select("DURABLENAME = ? ", new Object[] {boxData.getUdfs().get("DURABLENAME")});
				
				for (Product prdData : prdList) 
				{
					prdData.setStockState("Stocked");
					
					FGMSServiceProxy.getProductService().modify(eventInfo, prdData);
				}
			}
			
			//ADD Area pallet
			String location = oldProcessGroupData.getUdfs().get("OLDAREA");
			AreaKey lKey = new AreaKey();
			lKey.setAreaName(location);
			Area locationData = AreaServiceProxy.getAreaService().selectByKey(lKey);
			incrementLocationPalletQty(locationData, 1);
		}
		
		//call by value so that reply would be modified
		eleInvoiceList.add(setCreatedInvoiceList(shipRequestData));
		XmlUtil.setSubChildren(SMessageUtil.getBodyElement(doc), "INVOICELIST", eleInvoiceList);
		
		return doc;
	}
	
	private Element setCreatedInvoiceList(ShipRequest shipRequestData)
	{
		Element eleDurable = new Element("INVOICE");
		 
		try 
		{
			XmlUtil.addElement(eleDurable, "INVOICENO", shipRequestData.getInVoiceNo());
			XmlUtil.addElement(eleDurable, "INVOICETYPE", shipRequestData.getInVoiceType());
			XmlUtil.addElement(eleDurable, "SHIPREQUESTSTATE", shipRequestData.getShipRequestState());
			XmlUtil.addElement(eleDurable, "CUSTOMERNO", shipRequestData.getCustomerNo());
			XmlUtil.addElement(eleDurable, "PLANSHIPDATE", String.valueOf(shipRequestData.getPlanShipDate()));
			XmlUtil.addElement(eleDurable, "DOMESTICEXPORT", String.valueOf(shipRequestData.getDomesticExport()));
		}
		catch (Exception ex)
		{
			eventLog.warn(String.format("Invoice[%s] Stock Out failed", shipRequestData.getInVoiceNo()));
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
}
