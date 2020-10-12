package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCProductRelation;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class DeleteMQCProductSpecRelation extends SyncHandler{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		List<Element> DeleteList = SMessageUtil.getBodySequenceItemList(doc, "MQCPRODUCTSPECLIST", true);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteMQCProductSpecRelation", this.getEventUser(), this.getEventComment(), "", "");
		
		if(DeleteList != null)
		{
			for(Element Delete : DeleteList)
			{
				String From = SMessageUtil.getChildText(Delete, "FROMPRODUCTSPEC", true);
				String To = SMessageUtil.getChildText(Delete, "TOPRODUCTSPEC", true);
				
				MQCProductRelation MQCProductSpecRelationData = null;
				
				try
				{
					MQCProductSpecRelationData = ExtendedObjectProxy.getMQCProductRelationService().selectByKey(false, new Object[] {From, To});
				}
				catch (Exception ex)
				{
					MQCProductSpecRelationData = null;
				}
				
				if(MQCProductSpecRelationData == null)
				{
					
				}
				
				ExtendedObjectProxy.getMQCProductRelationService().remove(eventInfo, MQCProductSpecRelationData);
			}
		}
		
		return doc;
	}
}
