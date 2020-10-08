package kr.co.aim.messolution.productrequest.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequest.management.info.MakeReleasedInfo;

import org.jdom.Document;

public class WorkOrderProcessStarted extends AsyncHandler {

	@Override
	public void doWorks(Document doc)
		throws CustomException
	{
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sProductSpec   = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String sWorkOrder   = SMessageUtil.getBodyItemValue(doc, "WORKORDERNAME", true);
		String sPlanQty   = SMessageUtil.getBodyItemValue(doc, "PLANQUANTITY", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackIn", getEventUser(), getEventComment(), "", "");
		
		//Get Product Request Data
		ProductRequestKey prdKey = new ProductRequestKey();
		prdKey.setProductRequestName(sWorkOrder);
		
		ProductRequest prdReq = ProductRequestServiceProxy.getProductRequestService().selectByKey(prdKey);
		
		//Change Product Request State
		if(prdReq.getProductRequestState().toString().equals("Created"))
		{
			MakeReleasedInfo pReleasedInfo = new MakeReleasedInfo();
			eventInfo.setEventName("Release");
			prdReq = ProductRequestServiceProxy.getProductRequestService().makeReleased(prdKey, eventInfo, pReleasedInfo);
			MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(prdReq, eventInfo);
		}
		
		//2016.03.30 hjung
		//Get Product Request PlanData		
		/*ProductRequestPlan pPlan = CommonUtil.getFirstPlanByMachine(sMachineName, false);
		
		ProductRequestPlanKey pKey = new ProductRequestPlanKey();
		pKey.setProductRequestName(pPlan.getKey().getProductRequestName());
		pKey.setAssignedMachineName(pPlan.getKey().getAssignedMachineName());
		pKey.setPlanReleasedTime(pPlan.getKey().getPlanReleasedTime());
		
		//Change Product Request Plan State
		kr.co.aim.greentrack.productrequestplan.management.info.MakeReleasedInfo planInfo = new kr.co.aim.greentrack.productrequestplan.management.info.MakeReleasedInfo();		
		pPlan = ProductRequestPlanServiceProxy.getProductRequestPlanService().makeReleased(pKey, eventInfo, planInfo);
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addPlanHistory(pPlan);*/
						
	}
}