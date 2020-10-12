package kr.co.aim.messolution.lot.event;

import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.VirtualGlass;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.port.management.data.Port;

import org.jdom.Document;
import org.jdom.Element;

public class UnpackerProcessAbort extends AsyncHandler {

	@Override
	public void doWorks(Document doc)
		throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Release", getEventUser(), getEventComment(), "", "");

		//Machine Data
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		
		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		Durable durableData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(carrierName));
		
		if (CommonUtil.getValue(portData.getUdfs(), "PORTUSETYPE").equals("NG"))
		{
			if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PL"))
			{
				eventLog.info("Unpacker MGV loader case");
				
				//MGV port unload scenario
				boolean doit = false;
				
				for (Element eProduct : productList)
				{
					String sProductName = SMessageUtil.getChildText(eProduct, "PRODUCTNAME", true);
					String sPosition = SMessageUtil.getChildText(eProduct, "POSITION", true);
					VirtualGlass productData = ExtendedObjectProxy.getVirtualGlassService().selectByKey(false, new Object[]{sProductName});
					
					productData.setCarrier(carrierName);
					productData.setPosition(Long.valueOf(sPosition));
					
					eventInfo.setEventName("Assign");
					productData = ExtendedObjectProxy.getVirtualGlassService().modify(eventInfo, productData);
					
					doit = true;
				}
				
				if (doit)
				{
					//deassign
					durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
					durableData.setLotQuantity(productList.size());
					
					eventInfo.setEventName("AssignCarrier");
					SetEventInfo setEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setEventInfo(durableData.getUdfs());
					
					DurableServiceProxy.getDurableService().update(durableData);
					MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
				}
			}
			else
			{
				eventLog.info("Unpacker MGV unloader case");
			}
		}
		else
		{
			eventLog.info("Unpacker AGV unloader case");
		}
	}
}
