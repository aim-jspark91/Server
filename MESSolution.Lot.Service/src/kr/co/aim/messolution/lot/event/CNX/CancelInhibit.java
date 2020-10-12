package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Inhibit;
import kr.co.aim.messolution.extended.object.management.data.InhibitException;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;
import org.jdom.Element;

public class CancelInhibit extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Cancel", this.getEventUser(), this.getEventComment(), "", "");
		
		String userDepartment = SMessageUtil.getBodyItemValue(doc, "USERDEPARTMENT", true);
		List<Element> inhibitList = SMessageUtil.getBodySequenceItemList(doc, "INHIBITLIST", true);
		
		for(Element eleInhibit : inhibitList)
		{
			String inhibitID = SMessageUtil.getChildText(eleInhibit, "INHIBITID", true);
			String department = SMessageUtil.getChildText(eleInhibit, "DEPARTMENT", false);
			
			try
			{
				Inhibit inhibitData = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[] {inhibitID});
				
				if(department == "" || StringUtil.equals(userDepartment, department))
				{
					try {
						List<InhibitException> inhibitExceptionList 
							= ExtendedObjectProxy.getInhibitExceptionService().select("WHERE INHIBITID = ?", new Object[]{inhibitID});
						
						if(inhibitExceptionList != null)
						{
							for(InhibitException inhibitExceptionData : inhibitExceptionList)
							{
								EventInfo inhibitExceptionEventInfo = EventInfoUtil.makeEventInfo("CancelByInhibitCancel", this.getEventUser(), this.getEventComment(), "", "");
								ExtendedObjectProxy.getInhibitExceptionService().remove(inhibitExceptionEventInfo, inhibitExceptionData);	
							}
						}
					} catch(NotFoundSignal ex) {						
					}
					
					ExtendedObjectProxy.getInhibitService().remove(eventInfo, inhibitData);
				}
				else
				{
					throw new CustomException("INHIBIT-0002", "");
				}
			}
			catch(NotFoundSignal ex)
			{
				throw new CustomException("INHIBIT-0001", "");
			}
		}
		return doc;
	}
}
