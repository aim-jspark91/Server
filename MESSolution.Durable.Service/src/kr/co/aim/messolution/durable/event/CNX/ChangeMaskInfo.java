package kr.co.aim.messolution.durable.event.CNX;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ChangeMaskInfo extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeMaskInfo", this.getEventUser(), this.getEventComment(), "", "");
		
		String sMaskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String sTransportState = SMessageUtil.getBodyItemValue(doc, "TRANSPORTSTATE",true );
		String sTimeUsed = SMessageUtil.getBodyItemValue(doc, "TIMEUSED",false);
		String sMachineName= SMessageUtil.getBodyItemValue(doc, "MACHINENAME",false);
		String sMachineType= SMessageUtil.getBodyItemValue(doc, "MACHINETYPE",false);
		
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sMaskName);
		Durable durableData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(sMaskName));
				
		SetEventInfo setEventInfo = new SetEventInfo();
		// 2019.06.21_hsryu_Delete setUdfs.  other udfs must not be changed.
		//setEventInfo.setUdfs(udfs);
		setEventInfo.getUdfs().put("TRANSPORTSTATE", sTransportState);
		setEventInfo.getUdfs().put("MACHINENAME", sMachineName);

		if(StringUtils.isNotEmpty(sMachineName))
		{
			if(StringUtils.equals(sMachineType, "ProductionMachine"))
			durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
		}
		durableData.setTimeUsed(Double.parseDouble(sTimeUsed));
		
		DurableServiceProxy.getDurableService().update(durableData);
		
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
		
		return doc;
	}
}