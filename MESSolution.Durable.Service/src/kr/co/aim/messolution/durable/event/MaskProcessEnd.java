package kr.co.aim.messolution.durable.event;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskList;
import kr.co.aim.messolution.extended.object.management.impl.ReserveMaskListService;
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

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class MaskProcessEnd extends AsyncHandler{	
	@Override
	public void doWorks(Document doc) throws CustomException 
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ProcessEnd", this.getEventUser(), this.getEventComment(), null, null);
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
		double timeUsed = 0;
		
		List <Element> maskEle = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", false);
		
		//prepare MaskProcessEnd Data
		portType = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName).getUdfs().get("PORTTYPE");

		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Durable maskCSTData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		Durable maskCSTData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(carrierName));

		SetEventInfo setEventInfo = new SetEventInfo();
		
		if (portType.equals("PB"))
		{
			eventInfo.setEventName("Unload");
			
			List <ReserveMaskList> reserveMaskList = new ArrayList<ReserveMaskList>();
			
			for (Element eleRsvMask : maskEle) 
			{
				String maskName = SMessageUtil.getChildText(eleRsvMask, "MASKNAME", true);
				
				ReserveMaskList resvMask = ExtendedObjectProxy.getReserveMaskService().getReserveMaskInfoByPB(maskName, unitName);
				
				if(resvMask != null)
					reserveMaskList.add(resvMask);				
			}

			if (reserveMaskList != null)
			{
				String offset_xyt [] = new String[3];
				
				for (ReserveMaskList resvMask : reserveMaskList)
				{
					// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					Durable maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(resvMask.getMaskName());
					Durable maskData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(resvMask.getMaskName()));
					
					String [] splitPre = StringUtils.split(resvMask.getOffSetPre(), "^");
					
					if (splitPre != null && splitPre.length == 3)
					{
						for(int j = 0; j < offset_xyt.length; j++)
							offset_xyt[j]  = splitPre[j];
					}
					else
					{
						for(int j = 0; j < offset_xyt.length; j++)
							offset_xyt[j]  = "";
					}
					
					StringBuffer maskOffSetBuffer = new StringBuffer();
					maskOffSetBuffer.append(offset_xyt[0]);
					maskOffSetBuffer.append('^');
					maskOffSetBuffer.append(offset_xyt[1]);
					maskOffSetBuffer.append('^');
					maskOffSetBuffer.append(offset_xyt[2]);
					
					//set mask Udf
					maskData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
					DurableServiceProxy.getDurableService().update(maskData);
					
					setEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setMaskProcessEndInfo(maskData.getUdfs(),machineName,unitName ,carrierName,maskOffSetBuffer.toString());
					
					//Unload
					MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, setEventInfo, eventInfo); 
					
					//MaskCST
					maskCSTData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
					DurableServiceProxy.getDurableService().update(maskCSTData);
				}
				
				//removeReserveData(reserveMaskList, eventInfo);
			}
		}
		else if (portType.equals("PU") && maskEle.size() > 0)
		{
			eventInfo.setEventName("Unload");
			
			List <ReserveMaskList> reserveMaskList = ReserveMaskListService.getMaskInfoByCarreierName(machineName, portName, carrierName);
			
			if (reserveMaskList != null)
			{	
				String offset_xyt [] = new String[3];
				
				for (ReserveMaskList resvMask : reserveMaskList)
				{
					// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					Durable maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(resvMask.getMaskName());
					Durable maskData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(resvMask.getMaskName()));
					
					String [] splitPre = StringUtils.split(resvMask.getOffSetPre(), "^");
					
					if (splitPre != null && splitPre.length == 3)
					{
						for(int j = 0; j < offset_xyt.length; j++)
							offset_xyt[j]  = splitPre[j];
					}
					else
					{
						for(int j = 0; j < offset_xyt.length; j++)
							offset_xyt[j]  = "";
					}
					
					StringBuffer maskOffSetBuffer = new StringBuffer();
					maskOffSetBuffer.append(offset_xyt[0]);
					maskOffSetBuffer.append('^');
					maskOffSetBuffer.append(offset_xyt[1]);
					maskOffSetBuffer.append('^');
					maskOffSetBuffer.append(offset_xyt[2]);
					timeUsed=maskData.getTimeUsed();
						
					
					//set mask Udf
					if (machineName.equalsIgnoreCase("O1EVA100"))
					{
						timeUsed+=1;
						maskData.setDurableState(GenericServiceProxy.getConstantMap().Dur_NotAvailable);
						maskData.setDurableCleanState(GenericServiceProxy.getConstantMap().Dur_Dirty);
						maskData.setTimeUsed(timeUsed);
					}
					else
					{
						maskData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
					}
					
					DurableServiceProxy.getDurableService().update(maskData);
					
					setEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setMaskProcessEndInfo(maskData.getUdfs(),machineName,unitName ,carrierName,maskOffSetBuffer.toString());
					
					//Unload
					MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, setEventInfo, eventInfo); 
					
					//MaskCST
					maskCSTData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
					DurableServiceProxy.getDurableService().update(maskCSTData);
				}
				
				//removeReserveData(reserveMaskList, eventInfo);
			}
			
		}
		
		//2017.6.1 zhongsl delete Cleaner reserveData 
//		else if (portType.equals("PL") && StringUtil.equals(machineName, "O1MSK200"))
//	    {
//			List <ReserveMaskList> reserveMaskListPL = ReserveMaskListService.getMaskInfoByCarreierName(machineName,carrierName);
//			if(reserveMaskListPL.size() > 0)
//			{
//				removeReserveData(reserveMaskListPL, eventInfo);
//			}						
//	    }
		
		//2017.5.30 zhongsl  handle MaskCSTData
//		else if (portType.equals("PL") && maskEle.size() < 1 && StringUtil.equals(machineName, "O1MSK200"))
//		{
//			List <ReserveMaskList> reserveMaskListPL = ReserveMaskListService.getMaskInfoByCarreierName(machineName,carrierName);
//			
//			maskCSTData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
//			maskCSTData.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
//			maskCSTData.getUdfs().put("MACHINENAME", "");
//			maskCSTData.getUdfs().put("UNITNAME", "");
//			maskCSTData.getUdfs().put("POSITIONTYPE", "");
//			maskCSTData.getUdfs().put("PORTNAME", "");
//			maskCSTData.getUdfs().put("POSITIONNAME", "");
//			maskCSTData.setLotQuantity(0);
//		   																
//			for (Durable maskData : maskList)
//			{
//				Map<String, String> maskUdfs = maskData.getUdfs();
//				maskUdfs.put("MACHINENAME", machineName);
//				maskUdfs.put("UNITNAME", unitName);
//				maskUdfs.put("POSITIONNAME", unitName);
//				maskUdfs.put("POSITIONTYPE", "");
//				maskUdfs.put("PORTNAME", "");
//				maskUdfs.put("TRANSPORTSTATE", "INLINE");
//				maskData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
//				
//				DurableServiceProxy.getDurableService().update(maskData);
//				
//				carrierName = "";
//			    String maskPosition = "";
//					    
//			    //deassign mask
//			    setEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setDeassignEVAMaskCSTInfo(maskUdfs, carrierName, maskPosition);
//				setEventInfo.setUdfs(maskUdfs);																
//				
//				MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, setEventInfo, eventInfo);										
//			}
//			
//			removeReserveData(reserveMaskListPL, eventInfo);
//			
//			DurableServiceProxy.getDurableService().update(maskCSTData);
//			
//			//MASKCST
//			SetEventInfo setCSTEvent = new SetEventInfo();
//			setCSTEvent.setUdfs(maskCSTData.getUdfs()); 
//			
//			eventInfo.setEventName("Desassign");
//			
//			MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskCSTData, setCSTEvent, eventInfo);		
//		}
		
	}
	
	/**
	* Name : removeReserveData
	* Desc : remove mask reserve data
	* Author : jhlee
	* Date : 2016.07.04
	*/
	private void removeReserveData(List <ReserveMaskList> reserveMaskList, EventInfo eventInfo) 
			throws CustomException
	{
		//remove Mask reserve Data
		if (reserveMaskList != null)
		{	
			for (int i = 0; i < reserveMaskList.size(); i++)
			{
				String reserveConditionID = reserveMaskList.get(i).getConditionID();
				
				ReserveMaskList reserveMaskdataInfo = new ReserveMaskList(reserveConditionID);
				
				EventInfo eventRemoveMaskInfo = EventInfoUtil.makeEventInfo("Remove", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
				
				ExtendedObjectProxy.getReserveMaskService().remove(eventRemoveMaskInfo, reserveMaskdataInfo);
			}
		}
	}

}
