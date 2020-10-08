package kr.co.aim.messolution.durable.event.CNX;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;

public class MountMaskProcessing extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{	
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("MaskProcessing", this.getEventUser(), this.getEventComment(), "", "");
		
		String sDurableName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sMaskPosition = SMessageUtil.getBodyItemValue(doc, "POSITION", true);
		String sTransportState = SMessageUtil.getBodyItemValue(doc, "TRANSPORTSTATE", true);
		String sProcessingFlag = SMessageUtil.getBodyItemValue(doc, "PROCESSINGFLAG", true);

		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Durable maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);
		Durable maskData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(sDurableName));
		
		CommonValidation.checkExistMountMask(maskData,sMaskPosition,sMachineName);
		
		if(StringUtil.equals(sProcessingFlag, "Y"))
		{
			if(StringUtil.equals(sTransportState, GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INLINE))
			{
				maskData.setDurableState(GenericServiceProxy.getConstantMap().Cons_InUse);
				DurableServiceProxy.getDurableService().update(maskData);
		
				// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
				//udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_PROCESSING);
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_PROCESSING);
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, setEventInfo, eventInfo);
			}
		}
		else if(StringUtil.equals(sProcessingFlag, "N"))
		{
			if(StringUtil.equals(sTransportState, GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_PROCESSING))
			{
				maskData.setDurableState(GenericServiceProxy.getConstantMap().Cons_Mount);
				DurableServiceProxy.getDurableService().update(maskData);
				
				// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
				//udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INLINE);
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INLINE);
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, setEventInfo, eventInfo);
			}
		}
		
		return doc;	
	}
}