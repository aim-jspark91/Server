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
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequestplan.ProductRequestPlanServiceProxy;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlan;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlanKey;
import kr.co.aim.greentrack.productrequestplan.management.info.MakeOnHoldInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class HoldProductRequestPlan extends SyncHandler {
	
	public Object doWorks(Document doc) throws CustomException
	{	
		// get Message Item
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		
		// EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Hold", this.getEventUser(), this.getEventComment(), "", "");
		
		// select productRequest
		ProductRequestKey productRequestKey = new ProductRequestKey(productRequestName);
		
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		ProductRequest productRequestData = ProductRequestServiceProxy.getProductRequestService().selectByKey(productRequestKey);
		ProductRequest productRequestData = ProductRequestServiceProxy.getProductRequestService().selectByKeyForUpdate(productRequestKey);
		
		// if ProductRequest == Finished Throw
		if(StringUtils.equals(productRequestData.getProductRequestState(), GenericServiceProxy.getConstantMap().Prq_Finished))
		{
			throw new CustomException("PRODUCTREQUEST-0009", productRequestName, productRequestData.getProductRequestState());
		}
		
		if(StringUtils.equals(productRequestData.getProductRequestHoldState(), GenericServiceProxy.getConstantMap().Prq_OnHold))
		{
			throw new CustomException("PRODUCTREQUEST-0001", productRequestName);
		}
		
		// if ProductRequsetHoldState != "Y"
		if(!StringUtils.equals(productRequestData.getProductRequestHoldState(), GenericServiceProxy.getConstantMap().Prq_OnHold))
		{

			if( StringUtils.equals(productRequestData.getProductRequestState(), GenericServiceProxy.getConstantMap().Prq_Completed) )
			{
				productRequestData.setProductRequestState(GenericServiceProxy.getConstantMap().Prq_Released);
				ProductRequestServiceProxy.getProductRequestService().update(productRequestData);
				
				productRequestData = ProductRequestServiceProxy.getProductRequestService().selectByKey(productRequestKey);
				
				kr.co.aim.greentrack.productrequest.management.info.MakeOnHoldInfo makeOnHoldInfo = new kr.co.aim.greentrack.productrequest.management.info.MakeOnHoldInfo();
				makeOnHoldInfo.setUdfs(productRequestData.getUdfs());
				
				ProductRequestServiceProxy.getProductRequestService().makeOnHold(productRequestData.getKey(), eventInfo, makeOnHoldInfo );

				productRequestData = ProductRequestServiceProxy.getProductRequestService().selectByKey(productRequestKey);
				
				productRequestData.setProductRequestState(GenericServiceProxy.getConstantMap().Prq_Completed);
				ProductRequestServiceProxy.getProductRequestService().update(productRequestData);
			}
			else if( StringUtils.equals(productRequestData.getProductRequestState(), GenericServiceProxy.getConstantMap().Prq_Created) || 
					StringUtils.equals(productRequestData.getProductRequestState(), GenericServiceProxy.getConstantMap().Prq_Planned) )
			{
				productRequestData.setProductRequestHoldState(GenericServiceProxy.getConstantMap().Prq_OnHold);
				productRequestData.setLastEventName(eventInfo.getEventName());
				productRequestData.setLastEventTime(eventInfo.getEventTime());
				productRequestData.setLastEventTimeKey(eventInfo.getEventTimeKey());
				productRequestData.setLastEventUser(eventInfo.getEventUser());
				productRequestData.setLastEventComment(eventInfo.getEventComment());
				
				ProductRequestServiceProxy.getProductRequestService().update(productRequestData);
			}			
			else
			{
				kr.co.aim.greentrack.productrequest.management.info.MakeOnHoldInfo makeOnHoldInfo = new kr.co.aim.greentrack.productrequest.management.info.MakeOnHoldInfo();
				makeOnHoldInfo.setUdfs(productRequestData.getUdfs());				
				ProductRequestServiceProxy.getProductRequestService().makeOnHold(productRequestData.getKey(), eventInfo, makeOnHoldInfo );
			}	
			productRequestData = ProductRequestServiceProxy.getProductRequestService().selectByKey(productRequestKey);		
			// Add History
			MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(productRequestData, eventInfo);
		}
		
		//2. WorkOrder Plan Hold
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
				
				if(!objPlanData.getProductRequestPlanHoldState().equals(GenericServiceProxy.getConstantMap().Prq_OnHold))
				{
					//Make on Hold (Plan)
					MakeOnHoldInfo makeOnHoldPlanInfo = new MakeOnHoldInfo();
					makeOnHoldPlanInfo.setUdfs(objPlanData.getUdfs());
					
					MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeOnHoldPlan(objPlanData, makeOnHoldPlanInfo, eventInfo);
				}
				
			}
		}
		
		
		return doc;
	}
}
