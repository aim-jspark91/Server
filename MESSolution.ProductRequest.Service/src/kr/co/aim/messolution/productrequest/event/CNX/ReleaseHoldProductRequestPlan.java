package kr.co.aim.messolution.productrequest.event.CNX;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequestplan.ProductRequestPlanServiceProxy;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlan;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlanKey;
import kr.co.aim.greentrack.productrequestplan.management.info.MakeNotOnHoldInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ReleaseHoldProductRequestPlan extends SyncHandler {
	
	public Object doWorks(Document doc) throws CustomException
	{	
		
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		
		// EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReleaseHold", this.getEventUser(), this.getEventComment(), "", "");
		
		// select productRequest
		ProductRequestKey productRequestKey = new ProductRequestKey(productRequestName);
		
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		ProductRequest productRequestData = ProductRequestServiceProxy.getProductRequestService().selectByKey(productRequestKey);
		ProductRequest productRequestData = ProductRequestServiceProxy.getProductRequestService().selectByKeyForUpdate(productRequestKey);
		
		//ProductRequsetHoldState Validation
		if(StringUtil.equals(productRequestData.getProductRequestHoldState(), GenericServiceProxy.getConstantMap().Prq_NotOnHold))
		{
			throw new CustomException("WORKORDER-0008", productRequestName);
		}

		// ProductRequsetState Validation
		if(StringUtil.equals(productRequestData.getProductRequestState(), GenericServiceProxy.getConstantMap().Prq_Finished))
		{
			throw new CustomException("WORKORDER-0009", productRequestName, productRequestData.getProductRequestState());
		}
		
		if(StringUtil.equals(productRequestData.getProductRequestHoldState(), GenericServiceProxy.getConstantMap().Prq_OnHold))
		{
			if(StringUtils.equals(productRequestData.getProductRequestState(), GenericServiceProxy.getConstantMap().Prq_Completed))
			{
				productRequestData.setProductRequestState(GenericServiceProxy.getConstantMap().Prq_Released);
				ProductRequestServiceProxy.getProductRequestService().update(productRequestData);
				
				productRequestData = ProductRequestServiceProxy.getProductRequestService().selectByKey(productRequestKey);
				
				kr.co.aim.greentrack.productrequest.management.info.MakeNotOnHoldInfo makeNotOnHoldInfo = new kr.co.aim.greentrack.productrequest.management.info.MakeNotOnHoldInfo();
				makeNotOnHoldInfo.setUdfs(productRequestData.getUdfs());
				
				ProductRequestServiceProxy.getProductRequestService().makeNotOnHold(productRequestData.getKey(), eventInfo, makeNotOnHoldInfo );

				productRequestData = ProductRequestServiceProxy.getProductRequestService().selectByKey(productRequestKey);
				
				productRequestData.setProductRequestState(GenericServiceProxy.getConstantMap().Prq_Completed);
				ProductRequestServiceProxy.getProductRequestService().update(productRequestData);
				
				productRequestData = ProductRequestServiceProxy.getProductRequestService().selectByKey(productRequestKey);		
				// Add History
				MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(productRequestData, eventInfo);
			}
			else
			{
				//Make Not on Hold (Work Order)
				kr.co.aim.greentrack.productrequest.management.info.MakeNotOnHoldInfo makeNotOnHoldInfo = new kr.co.aim.greentrack.productrequest.management.info.MakeNotOnHoldInfo();
				makeNotOnHoldInfo.setUdfs(productRequestData.getUdfs());
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeNotOnHold(productRequestData, makeNotOnHoldInfo, eventInfo);
			}
			
		}
		
		//2. WorkOrder Plan Release Hold
		Element ProductRequestList = SMessageUtil.getBodySequenceItem(doc, "PRODUCTREQUESTLIST", false);

		if (ProductRequestList != null)
		{
			for ( Iterator<?> iteratorLotList = ProductRequestList.getChildren().iterator(); iteratorLotList.hasNext();)					
			{
				Element productRequestE = (Element) iteratorLotList.next();
				
				String productRequestPlanName = SMessageUtil.getChildText(productRequestE, "PRODUCTREQUESTNAME", true);
				String sPlanReleasedTime = SMessageUtil.getChildText(productRequestE, "PLANRELEASEDTIME", true);
				String sAssignedMachineName = SMessageUtil.getChildText(productRequestE, "ASSIGNEDMACHINENAME", true);
				
				Date date = new Date();
				DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
				try {
					date = sdf.parse(sPlanReleasedTime);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				Timestamp tPlanReleasedTime = new Timestamp(date.getTime());
				
				ProductRequestPlanKey pPlanKey = new ProductRequestPlanKey(productRequestPlanName, sAssignedMachineName, tPlanReleasedTime);
				
				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				ProductRequestPlan objPlanData = ProductRequestPlanServiceProxy.getProductRequestPlanService().selectByKey(pPlanKey);
				ProductRequestPlan objPlanData = ProductRequestPlanServiceProxy.getProductRequestPlanService().selectByKeyForUpdate(pPlanKey);
				
				if(objPlanData.getProductRequestPlanHoldState().equals(GenericServiceProxy.getConstantMap().Prq_OnHold))
				{
					//Make Not on Hold (Plan)
					MakeNotOnHoldInfo makeNotOnHoldPlanInfo = new MakeNotOnHoldInfo();
					makeNotOnHoldPlanInfo.setUdfs(objPlanData.getUdfs());
					
					MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeNotOnHoldPlan(objPlanData, makeNotOnHoldPlanInfo, eventInfo);
				}
				
			}
		}
		
		return doc;
	}
}
