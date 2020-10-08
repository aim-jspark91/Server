package kr.co.aim.messolution.durable.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskList;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class MaskProcessStarted extends AsyncHandler{
	@Override
	public void doWorks(Document doc) throws CustomException {
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackIn", getEventUser(), getEventComment(), "", "");
		
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sPortName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String sMaskCSTName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		
		//Port
		Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(sMachineName, sPortName);
		
		//Mask
		Durable maskData = new Durable();
		String maskName = "";
		
		List<Durable> maskList = null;
		List<ReserveMaskList> resvMaskList = null;
		
		if (portData.getUdfs().get("PORTTYPE").equals("PL"))
		{
			maskList = MESDurableServiceProxy.getDurableServiceUtil().getMaskListByCST(sMachineName, sPortName, sMaskCSTName);
			
			for (int i = 0; i < maskList.size(); i++)  
			{	
				maskName = maskList.get(i).getKey().getDurableName();
				
				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskName);
				maskData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(maskName));
				
				maskData.setDurableState("InUse");
				DurableServiceProxy.getDurableService().update(maskData);
				
				Map<String, String> udf = new HashMap<String, String>();
				udf.put("MACHINENAME", sMachineName);
				udf.put("PORTNAME", sPortName);
				
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.setUdfs(udf);
				
				//Excute
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, setEventInfo, eventInfo);
			}
		}
		else if (portData.getUdfs().get("PORTTYPE").equals("PU"))
		{
			resvMaskList = ExtendedObjectProxy.getReserveMaskService().getMaskInfoByCarreierName(sMachineName, sPortName, sMaskCSTName);
			
			for (int i = 0; i < resvMaskList.size(); i++)  
			{	
				maskName = resvMaskList.get(i).getMaskName();
				
				maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskName);
				
				SetEventInfo setEventInfo = new SetEventInfo();
				eventInfo.setEventName("TrackOut");
				
				//Excute
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, setEventInfo, eventInfo);
			}
		}
		else if (portData.getUdfs().get("PORTTYPE").equals("PB"))
		{
			maskList = MESDurableServiceProxy.getDurableServiceUtil().getMaskListByCST(sMachineName, sPortName, sMaskCSTName);
			resvMaskList = new ArrayList<ReserveMaskList>();
			
			for (Durable mskData : maskList) 
			{
				maskName = mskData.getKey().getDurableName();
				
				ReserveMaskList resvMask = ExtendedObjectProxy.getReserveMaskService().getReserveMaskInfoByPL(maskName);
				
				if(resvMask != null)
					resvMaskList.add(resvMask);
			}
			
			for (int i = 0; i < resvMaskList.size(); i++)  
			{	
				maskName = resvMaskList.get(i).getMaskName();
				
				maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskName);
				
				maskData.setDurableState("InUse");
				DurableServiceProxy.getDurableService().update(maskData);
				
				SetEventInfo setEventInfo = new SetEventInfo();
				eventInfo.setEventName("TrackOut");
				
				//Excute
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, setEventInfo, eventInfo);
			}
		}
		//make port working
		makePortWorking(eventInfo, portData);
	}
	
	/**
	 * @desc make port working
	 * @author jhlee
	 * @since 2016.05.04
	 * @param eventInfo
	 * @param portData
	 * @throws CustomException
	 */
	private void makePortWorking(EventInfo eventInfo, Port portData)
	{
		try
		{
			if( !StringUtils.equals(portData.getTransferState(), GenericServiceProxy.getConstantMap().Port_Processing) )
			{
				eventInfo.setEventName("ChangeTransferState");
				
				MakeTransferStateInfo makeTransferStateInfo = new MakeTransferStateInfo();
				makeTransferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_Processing);
				makeTransferStateInfo.setValidateEventFlag("N");
				makeTransferStateInfo.setUdfs(portData.getUdfs());
				
				MESPortServiceProxy.getPortServiceImpl().makeTransferState(portData, makeTransferStateInfo, eventInfo);
			}
		}
		catch (Exception ex)
		{
			eventLog.error("Port handling is failed");
		}
	}
}
