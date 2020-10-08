package kr.co.aim.messolution.lot.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCProductRelation;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class CreateMQCProductSpecRelation extends SyncHandler{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String From = SMessageUtil.getBodyItemValue(doc, "FROMPRODUCTSPECNAME", true);
		String To = SMessageUtil.getBodyItemValue(doc, "TOPRODUCTSPECNAME", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateMQCProductSpecRelation", this.getEventUser(), this.getEventComment(), "", "");
		
		MQCProductRelation MQCProductSpecRelationData = null;
		
		try
		{
			MQCProductSpecRelationData = ExtendedObjectProxy.getMQCProductRelationService().selectByKey(false, new Object[] {From, To});
		}
		catch (Exception ex)
		{
			MQCProductSpecRelationData = null;				
		}
	
		if(MQCProductSpecRelationData != null)
		{
			throw new CustomException("MQC-0046", "");
		}
		
		MQCProductSpecRelationData = new MQCProductRelation(From, To);
		MQCProductSpecRelationData.setFACTORYNAME(factoryName);
		MQCProductSpecRelationData.setLASTEVENTNAME(eventInfo.getEventName());		
		MQCProductSpecRelationData.setLASTEVENTTIMEKEY(eventInfo.getEventTimeKey());		
		MQCProductSpecRelationData.setLASTEVENTUSER(eventInfo.getEventUser());
		MQCProductSpecRelationData.setCREATETIME(eventInfo.getEventTime());
		MQCProductSpecRelationData.setLASTEVENTTIME(eventInfo.getEventTime());

		ExtendedObjectProxy.getMQCProductRelationService().create(eventInfo, MQCProductSpecRelationData);
		return doc;
	}
}
