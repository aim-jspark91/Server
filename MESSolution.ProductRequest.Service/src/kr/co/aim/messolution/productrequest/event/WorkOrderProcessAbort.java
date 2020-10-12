package kr.co.aim.messolution.productrequest.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.productrequestplan.ProductRequestPlanServiceProxy;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlan;

import org.jdom.Document;

public class WorkOrderProcessAbort extends AsyncHandler {

	@Override
	public void doWorks(Document doc)
		throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String workOrderName = SMessageUtil.getBodyItemValue(doc, "WORKORDERNAME", true);
				
		ProductRequestPlan pPlan = CommonUtil.getFirstPlanByMachine(machineName, false);
		
		//Check Work Order Name
		if(!workOrderName.equals(pPlan.getKey().getProductRequestName()))
		{
			return;
		}
		
		//1. Increment Work Order Plan Release Qty
		//MESLotServiceProxy.getLotServiceUtil().incrementWorkOrderReleaseQty(eventInfo, pPlan.getKey(),  Integer.valueOf(inputGlassQty));
				
		//2. Change Product Request State
		//MakeCompletedInfo pCompletedInfo = new MakeCompletedInfo();
		//eventInfo.setEventName("Complete");
		//ProductRequestServiceProxy.getProductRequestService().makeCompleted(pKey, eventInfo, pCompletedInfo);
		
		//3. Change Product Request Plan State
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Abort", getEventUser(), getEventComment(), null, null);
		pPlan.setProductRequestPlanState("Aborted");
		pPlan.setLastEventName(eventInfo.getEventName());
		pPlan.setLastEventTime(eventInfo.getEventTime());
		pPlan.setLastEventUser(eventInfo.getEventUser());
		pPlan.setLastEventComment(eventInfo.getEventComment());
		
		ProductRequestPlanServiceProxy.getProductRequestPlanService().update(pPlan);
		
		//addHistory
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addPlanHistory(pPlan, eventInfo);
	}
}