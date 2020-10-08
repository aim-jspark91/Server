package kr.co.aim.messolution.userprofile.event;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.PlanQuantity;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class CreatePlanQuantity extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String OperationName = SMessageUtil.getBodyItemValue(doc, "OPERATIONNAME", true);
		String PlanQuantity = SMessageUtil.getBodyItemValue(doc, "PLANQUANTITY", false);
		String DayQuantity = SMessageUtil.getBodyItemValue(doc, "DAYQUANTITY", false);
		String LightQuantity = SMessageUtil.getBodyItemValue(doc, "LIGHTQUANTITY", false);
	
		try
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreatePlanQuantity", getEventUser(), getEventComment(), null, null);
			
			long 	planQuantity=Long.parseLong(PlanQuantity);
			Long 	dayQuantity=Long.parseLong(DayQuantity);
			Long 	lightQuantity=Long.parseLong(LightQuantity);
			//Create
			PlanQuantity quantityPlan = new PlanQuantity();
			quantityPlan.setOperationName(OperationName);
			quantityPlan.setPlanQuantity(planQuantity);
			quantityPlan.setDayQuantity(dayQuantity);
			quantityPlan.setLightQuantity(lightQuantity);
			quantityPlan.setCreateTime(eventInfo.getEventTime());
			quantityPlan.setCreateUser(eventInfo.getEventUser());
			
			ExtendedObjectProxy.getPlanQuantityService().create(eventInfo,quantityPlan);
				
		}
		catch(Exception ex)
		{
			throw new CustomException("MOD-0002");
		}
		
		//return No
		Document rtnDoc = new Document();
		rtnDoc = (Document)doc.clone();
		rtnDoc = SMessageUtil.addItemToBody(rtnDoc, "OPERATIONNAME", OperationName);

		return rtnDoc;
	}
	
	 
}
