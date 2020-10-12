package kr.co.aim.messolution.lot.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.YieldInfo;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class DeleteYield extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		Element yieldList = SMessageUtil.getBodySequenceItem(doc, "YIELDLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteYield", this.getEventUser(), this.getEventComment(), "", "");
		
		if(yieldList != null)
		{
			for(Object obj : yieldList.getChildren())
			{
				Element element = (Element)obj;
				String productSpecName = SMessageUtil.getChildText(element, "PRODUCTSPECNAME", true);
				String ecCode = SMessageUtil.getChildText(element, "ECCODE", true);
				String processFlowName = SMessageUtil.getChildText(element, "PROCESSFLOWNAME", true);
				String processFlowVersion = SMessageUtil.getChildText(element, "PROCESSFLOWVERSION", true);
				String processOperationName = SMessageUtil.getChildText(element, "PROCESSOPERATIONNAME", true);
				YieldInfo yieldinfo =null;
				
				try
				{
					yieldinfo = ExtendedObjectProxy.getYieldInfoService().selectByKey(false, new Object[] {productSpecName, ecCode, processFlowName  , processFlowVersion  , processOperationName });
				}
				catch (Exception ex)
				{
					yieldinfo = null;
				}
				
				if(yieldinfo == null)
				{
					throw new CustomException("IDLE-0006", "");
				}
				
				ExtendedObjectProxy.getYieldInfoService().remove(eventInfo, yieldinfo);
			}
		}
		
		return doc;
	}
}
