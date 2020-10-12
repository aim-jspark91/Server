package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class ReserveSortJob extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReserveSortPriority",getEventUser(), getEventComment(), "", "");
		SortJob sortJob = new SortJob("");
		List<Element> reserveList = SMessageUtil.getBodySequenceItemList(doc,"RESERVELIST", false);
		String MACHINENAME = SMessageUtil.getBodyItemValue(doc, "MACHINENAME",true);
		// Wait List
		for (Element reserveE : reserveList) {
			String eventComment = "";
			sortJob = ExtendedObjectProxy.getSortJobService().selectByKey(false,new Object[] {reserveE.getChild("JOBNAME").getText()});		
			// Mentis 3385
			eventComment += "[";
			eventComment += reserveE.getChild("JOBNAME").getText();
			eventComment += "]";
			eventComment += " From Seq[";
			eventComment += sortJob.getSeq();
			eventComment += "] To Seq[";
			eventComment += reserveE.getChild("NO").getText();
			eventComment += "]";
			
			eventInfo.setEventComment(eventComment);
			
			sortJob.setSeq(Integer.valueOf(reserveE.getChild("NO").getText()));
			sortJob.setJobState(GenericServiceProxy.getConstantMap().SORT_JOBSTATE_WAIT);
			sortJob.setEventTime(eventInfo.getEventTime());
			sortJob.setEventName(eventInfo.getEventName());
			sortJob.setEventUser(eventInfo.getEventUser());
			sortJob.setEventComment(eventInfo.getEventComment());

			ExtendedObjectProxy.getSortJobService().modify(eventInfo, sortJob);
		}
		return doc;
	}
}
