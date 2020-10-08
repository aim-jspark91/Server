package kr.co.aim.messolution.dispatch.event.CNX;

import kr.co.aim.messolution.dispatch.MESDSPServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class DeleteMaxWip extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		Element connList = SMessageUtil.getBodySequenceItem(doc, "CONNLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Delete", this.getEventUser(), this.getEventComment(), "", "");
		
		if(connList != null)
		{
			for(Object obj : connList.getChildren())
			{
				Element element = (Element)obj;
								
				String machineName = SMessageUtil.getChildText(element,  "MACHINENAME", true);
				String productSpecName = SMessageUtil.getChildText(element,  "PRODUCTSPECNAME", true);
				String opeID = SMessageUtil.getChildText(element,  "OPEID", true);
				String validateFlag = SMessageUtil.getChildText(element,  "VALIDATEFLAG", true);
				String currentState = SMessageUtil.getChildText(element,  "CURRENTSTATE", true);
				String currentMode = SMessageUtil.getChildText(element,  "CURRENTMODE", true);
				String maxWip = SMessageUtil.getChildText(element,  "MAXWIP", true);
				String processFlowName = SMessageUtil.getChildText(element,  "PROCESSFLOWNAME", true);
				
				MESDSPServiceProxy.getDSPServiceImpl().deleteMaxWipSpec(eventInfo, machineName, productSpecName, opeID, processFlowName, validateFlag, currentState, currentMode, maxWip);
				
			}
		}
		
		return doc;
	}
}
