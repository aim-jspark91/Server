package kr.co.aim.messolution.durable.event.CNX;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class AssignMask extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Assign", this.getEventUser(), this.getEventComment(), "", "");
		
		Element eleBody = SMessageUtil.getBodyElement(doc);
		
		if (eleBody != null)
		{
			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", false))
			{
				String sDurableName = SMessageUtil.getChildText(eledur, "DURABLENAME", true);
				String sDurableType = SMessageUtil.getChildText(eledur, "DURABLETYPE", true);
				String smachineName = SMessageUtil.getChildText(eledur, "MACHINENAME", true);
				String sUnitName    = SMessageUtil.getChildText(eledur, "UNITNAME", true);
				 
				//getMachineData 
				CommonUtil.getMachineInfo(smachineName).toString();
				
				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				Durable durableData = CommonUtil.getDurableInfo(sDurableName);
				Durable durableData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(sDurableName));
				
				//Set DurableState
				durableData.setDurableState("InUse");
				DurableServiceProxy.getDurableService().update(durableData);
				
				// Validation
				if(!StringUtils.equals(durableData.getUdfs().get("MACHINENAME"), ""))
				{
					throw new CustomException("MASK-0002"+"sDurableName is "+sDurableName);
				}
				
				Map<String, String> udfs = new HashMap<String, String>();
				 
				SetEventInfo assignMaskInfo =  
						MESDurableServiceProxy.getDurableInfoUtil().setAssignMaskInfo(sDurableType, udfs, smachineName, sUnitName, "", "" );

				MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, assignMaskInfo, eventInfo);
			}
		}

		return doc;
	}
}