package kr.co.aim.messolution.pms.event;

import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.CheckID;
import kr.co.aim.messolution.pms.management.data.CheckList;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class DeleteCheckID extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), "", "");
		
		String CheckIDName 		 = SMessageUtil.getBodyItemValue(doc, "CHECKID", true);
		
	  	eventInfo.setEventName("DeleteCheckID");
		
		//EventInfo eventInfo  = EventInfoUtil.makeEventInfo("DeleteCheckID", getEventUser(), getEventComment(), null, null);
		try
		{						
			CheckID CheckIDData = PMSServiceProxy.getCheckIDService().selectByKey(true, new Object[] {CheckIDName});
			PMSServiceProxy.getCheckIDService().remove(eventInfo, CheckIDData);	
			String condition="WHERE CHECKID=?";
		    List<CheckList> checkLists=PMSServiceProxy.getCheckListService().select(condition,new Object[]{CheckIDName});
		    for(int i=0;i<checkLists.size();i++)
		    {
		    	CheckList checkListData = checkLists.get(i);
		    	eventInfo.setEventName("DeleteCheckList");
		    	PMSServiceProxy.getCheckListService().remove(eventInfo, checkListData);
		    }
			
			
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0091", CheckIDName);
		}	
		
		return doc;
	}
}
