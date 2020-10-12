package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.extended.object.management.data.SortJobCarrier;
import kr.co.aim.messolution.extended.object.management.data.SortJobProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class RemoveSortJob extends SyncHandler {

	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		String Machinename = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("RemoveSortJob", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		//Generate JobName
		String jobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", false);
		
		try
		{
			//Get Sort List
			SortJob sortJob = ExtendedObjectProxy.getSortJobService().selectByKey(false, new Object[] {jobName});

			List<SortJobCarrier> sortCstList = ExtendedObjectProxy.getSortJobCarrierService().select(" jobName = ?", new Object[] {jobName});
			List<SortJobProduct> sortPrdList = ExtendedObjectProxy.getSortJobProductService().select(" jobName = ?", new Object[] {jobName});
				
				//remove sortJobProduct
			for (SortJobProduct sortJobProduct : sortPrdList) 
			{
				ExtendedObjectProxy.getSortJobProductService().remove(eventInfo, sortJobProduct);
			}
				
				//remove SortJob Carrier
			for (SortJobCarrier sortJobCarrier : sortCstList) 
			{
				ExtendedObjectProxy.getSortJobCarrierService().remove(eventInfo, sortJobCarrier);
			}
				
			ExtendedObjectProxy.getSortJobService().remove(eventInfo, sortJob);
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-9999", "RemoveSortJob", ex.getMessage());
		}
		
				
		return doc;
	}

}
