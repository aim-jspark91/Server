package kr.co.aim.messolution.userprofile.event;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ProductionPlan;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class DeleteProductionPlan extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String no = SMessageUtil.getBodyItemValue(doc, "NO", true);


		try
		{	
			ProductionPlan productionPlanData=new  ProductionPlan(no);
			//Delete
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteProductionPlan", getEventUser(), getEventComment(), null, null);
			 productionPlanData = ExtendedObjectProxy.getProductionPlanService().selectByKey(false, new Object[] {no});
			ExtendedObjectProxy.getProductionPlanService().remove(eventInfo, productionPlanData);
		}
		catch(Exception ex)
		{
			throw new CustomException("MOD-0002");
		}

		return doc;
	}
}