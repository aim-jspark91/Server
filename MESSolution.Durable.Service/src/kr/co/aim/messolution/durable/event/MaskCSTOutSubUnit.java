package kr.co.aim.messolution.durable.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetAreaInfo;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.port.management.data.Port;

import org.jdom.Document;

public class MaskCSTOutSubUnit extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Unload", this.getEventUser(), this.getEventComment(), null, null);
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String subUnitName  = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", true);
		String carrierName  = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String slotMap = SMessageUtil.getBodyItemValue(doc, "SLOTMAP", false);
		
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		Port portData = CommonUtil.getPortInfo(machineName, subUnitName);
		List<Durable> maskData = null;
		String maskName = null;
		Durable durableMaskData  =null;
		
		// makeTransportStateOutSTK
		SetAreaInfo setAreaInfo  = MESDurableServiceProxy.getDurableServiceImpl().makeTransportStateOutSTK(eventInfo, carrierName, machineName, subUnitName);
		
		// Execute setArea
		DurableServiceProxy.getDurableService().setArea(durableData.getKey(),eventInfo, setAreaInfo);
		
		// change portInfo
		//MESPortServiceProxy.getPortServiceUtil().unLoadComplete(eventInfo, machineName, subUnitName);
		
		// change portInfo
		kr.co.aim.greentrack.port.management.info.SetEventInfo setEventInfo = 
			MESPortServiceProxy.getPortServiceUtil().MaskUnloadComplete(eventInfo, machineName, subUnitName,"MB"); //Port Type MB Used MSK Silo port;
		
		//  Execute
		 MESPortServiceProxy.getPortServiceImpl().setEvent(portData, setEventInfo, eventInfo);
		
		
		try{
			maskData = (List<Durable>) CommonUtil.getMaskInfoBydurableName(carrierName,"EVAMask");
			
			for (int i = 0; i < maskData.size(); i++)  
			{
				maskName = maskData.get(i).getKey().getDurableName();
				
			    durableMaskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskName);
				
				Map<String, String> udfsMask = new HashMap<String, String>();
				udfsMask.put("TRANSPORTSTATE", "OUTSTK");
				udfsMask.put("POSITIONTYPE", "PORT");
				udfsMask.put("PORTNAME", subUnitName);
				SetEventInfo setMaskEventInfo = new SetEventInfo();
				setMaskEventInfo.setUdfs(udfsMask);
				//Excute
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableMaskData, setMaskEventInfo, eventInfo);
			}
		}
		catch(Exception e){
			maskData = null;
			throw new CustomException("MASK-0009",carrierName);
		}		
	}

}
