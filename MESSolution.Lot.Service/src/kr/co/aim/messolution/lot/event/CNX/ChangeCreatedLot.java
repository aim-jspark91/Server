
package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequestplan.ProductRequestPlanServiceProxy;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlan;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlanKey;

import org.jdom.Document;
import org.jdom.Element;

public class ChangeCreatedLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String sProductRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "ASSIGNEDMACHINENAME", true);
		String sPlanReleasedTime = SMessageUtil.getBodyItemValue(doc, "PLANRELEASEDTIME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Change", getEventUser(), getEventComment(), "", "");

		//Product Request Key & Data
		ProductRequestKey pKey = new ProductRequestKey();
		pKey.setProductRequestName(sProductRequestName);
		
		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		ProductRequest pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
		ProductRequest pData = ProductRequestServiceProxy.getProductRequestService().selectByKeyForUpdate(pKey);
		
		//Product Request Plan Key & Data
		ProductRequestPlanKey pPlanKey = new ProductRequestPlanKey();
		pPlanKey.setProductRequestName(sProductRequestName);
		pPlanKey.setAssignedMachineName(sMachineName);
		pPlanKey.setPlanReleasedTime(TimeUtils.getTimestamp(sPlanReleasedTime));
		
		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		ProductRequestPlan pPlanData = ProductRequestPlanServiceProxy.getProductRequestPlanService().selectByKey(pPlanKey);
		ProductRequestPlan pPlanData = ProductRequestPlanServiceProxy.getProductRequestPlanService().selectByKeyForUpdate(pPlanKey);
				
		List<ReserveLot> reserveLotList = this.getReserveLotList(pPlanData);
		
		//1. Velidation
        //1) Check Current Time & Due Date
//		if(eventInfo.getEventTime().after(pData.getPlanFinishedTime()))
//		{
//			throw new CustomException("PRODUCTREQUEST-0019", eventInfo.getEventTime().toString(), pData.getPlanFinishedTime().toString());
//		}
		
		//Get Created Lot Product Quantity Sum
		int createProductQty = this.getCreateQty(pPlanKey) + (int)pPlanData.getReleasedQuantity();
		int createProductQty2 = this.getAllQty(pPlanKey);
		
		for(Element element : SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", false))
		{
			String sPosition = SMessageUtil.getChildText(element, "POSITION", false);
			String sLotName = SMessageUtil.getChildText(element, "LOTNAME", false);
			String sProductQuantity = SMessageUtil.getChildText(element, "PRODUCTQUANTITY", false);
			
			//Lot Data
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(sLotName);
			Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(sLotName));
			
			if(reserveLotList != null && reserveLotList.size() > 0)
			{
				for (int idx=0; idx < reserveLotList.size(); idx++)
				{
					ReserveLot reserveData = reserveLotList.get(idx);
					
					if (pPlanData != null 
							&& reserveData.getLotName().equals(sLotName)
							&& reserveData.getMachineName().equals(sMachineName))
					{				
						reserveLotList.remove(idx);
					}
				}
			}
			
			//Get Product Quantity
			int iProductQuantity = Integer.parseInt(sProductQuantity) - (int)lotData.getCreateProductQuantity();
			
			createProductQty = createProductQty + iProductQuantity;
			
			
			if(pPlanData.getPlanQuantity() < createProductQty)
			{
				throw new CustomException("PRODUCTREQUEST-0014", "");
			}
			
			
			//Change Lot Created Qty
			if(!StringUtil.equals(String.valueOf(lotData.getCreateProductQuantity()), sProductQuantity))
			{
				//int multipleSubProductQty = 60;
				
				lotData.setCreateProductQuantity(Integer.parseInt(sProductQuantity));
				//lotData.setCreateSubProductQuantity(Integer.parseInt(sProductQuantity)*multipleSubProductQty);
				//lotData.setCreateSubProductQuantity1(Integer.parseInt(sProductQuantity)*multipleSubProductQty);
				LotServiceProxy.getLotService().update(lotData);
				
				LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, new SetEventInfo());
			}
			
			//Change ReserveLot Position
			ReserveLot reserveLot = ExtendedObjectProxy.getReserveLotService().selectByKey(false, new Object[] {sMachineName, sLotName});
			
			if(!StringUtil.equals(String.valueOf(reserveLot.getPosition()), sPosition) && reserveLot != null)
			{
				reserveLot.setPosition(Integer.parseInt(sPosition));
				
				ExtendedObjectProxy.getReserveLotService().modify(eventInfo, reserveLot);
			}
		}
		
		if(reserveLotList != null && reserveLotList.size() > 0)
		{
			int deleteCreateQuantity = 0;
			
			eventInfo.setEventName("CancelCreate");
			
			for (ReserveLot reserveLot : reserveLotList) 
			{			
				
				ExtendedObjectProxy.getReserveLotService().remove(eventInfo, reserveLot);
				
				LotKey lkey = new LotKey();
				lkey.setLotName(reserveLot.getLotName());
				
				Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(reserveLot.getLotName());
				
				deleteCreateQuantity += lotData.getCreateProductQuantity();
				
				SetEventInfo setEventInfo = new SetEventInfo();
				// 2019.06.21_hsryu_Delete setUdfs.  other udfs must not be changed.
//				Map<String, String> udfs = lotData.getUdfs();
//				setEventInfo.setUdfs(udfs);				
				LotServiceProxy.getLotService().setEvent(lkey, eventInfo, setEventInfo);				
				
				LotServiceProxy.getLotService().delete(lkey);
			}
			
			int allplanQty = createProductQty2 - deleteCreateQuantity;
			
			if(allplanQty==0)
			{
				//remove Plan
				eventInfo.setEventName("RemovePlan");
				ProductRequestPlanServiceProxy.getProductRequestPlanService().remove(pPlanData.getKey());
				MESWorkOrderServiceProxy.getProductRequestServiceUtil().addPlanHistory(pPlanData, eventInfo);
				
				//select exist Other Plan
				StringBuffer queryBuffer = new StringBuffer()
				.append("SELECT PRODUCTREQUESTSTATE FROM PRODUCTREQUESTPLAN  \n")
				.append("  WHERE 1=1 \n")
				.append("  AND PRODUCTREQUESTNAME = :PRODUCTREQUESTNAME \n");
				
				HashMap<String, Object> bindMap = new HashMap<String, Object>();

				bindMap.put("PRODUCTREQUESTNAME", sProductRequestName);

				List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);

				// not exist other plan, WO state 'Planned' -> 'Created'
				if(sqlResult.size()==0)
				{
					
					pData.setProductRequestState(GenericServiceProxy.getConstantMap().Prq_Created);
					
					eventInfo.setEventName("CancelReserve");

					pData.setLastEventName(eventInfo.getEventName());
					pData.setLastEventTimeKey(eventInfo.getEventTimeKey());
					pData.setLastEventTime(eventInfo.getEventTime());
					pData.setLastEventUser(eventInfo.getEventUser());
					pData.setLastEventComment(eventInfo.getEventComment());
					
					ProductRequestServiceProxy.getProductRequestService().update(pData);

					//  Add History
					MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(pData, eventInfo);
					
					/*String productRequestState = sqlResult.get(0).get("PRODUCTREQUESTSTATE").toString(); 
					
					if(StringUtil.equals(productRequestState, GenericServiceProxy.getConstantMap().Prq_Planned))
					{
						
					}*/
				}
			}
			else
			{
				eventInfo.setEventName("DecreasePlanQuantity");
				
				pPlanData.setPlanQuantity(allplanQty);
				pPlanData.setLastEventName(eventInfo.getEventName());
				pPlanData.setLastEventTimeKey(eventInfo.getEventTimeKey());
				pPlanData.setLastEventTime(eventInfo.getEventTime());
				pPlanData.setLastEventUser(eventInfo.getEventUser());
				pPlanData.setLastEventComment(eventInfo.getEventComment());
				

				ProductRequestPlanServiceProxy.getProductRequestPlanService().update(pPlanData);
				MESWorkOrderServiceProxy.getProductRequestServiceUtil().addPlanHistory(pPlanData, eventInfo);

				if(allplanQty==pPlanData.getReleasedQuantity())
				{
					eventInfo.setEventName("Completed");
					
					pPlanData.setProductRequestPlanState(GenericServiceProxy.getConstantMap().Prq_Completed);
					pPlanData.setLastEventName(eventInfo.getEventName());
					
					eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					
					ProductRequestPlanServiceProxy.getProductRequestPlanService().update(pPlanData);
					MESWorkOrderServiceProxy.getProductRequestServiceUtil().addPlanHistory(pPlanData, eventInfo);
				}
			}
		}
		
		return doc;
	}
	
	private int getCreateQty(ProductRequestPlanKey pPlanKey)
	{
		int createProductQty = 0;
		
		try
		{
			String condition = "WHERE productRequestName = ? AND machineName = ? AND planReleasedTime = ? AND reserveState = ?";
			Object[] bindSet = new Object[] {pPlanKey.getProductRequestName(), pPlanKey.getAssignedMachineName(), 
					pPlanKey.getPlanReleasedTime(), GenericServiceProxy.getConstantMap().RESV_STATE_RESV};
			
			List<ReserveLot> lotList = new ArrayList<ReserveLot>();
			
			lotList = ExtendedObjectProxy.getReserveLotService().select(condition, bindSet);
			for(ReserveLot dLot : lotList)
			{
				LotKey lKey = new LotKey(dLot.getLotName());
				createProductQty += LotServiceProxy.getLotService().selectByKey(lKey).getCreateProductQuantity();
			}
		}
		catch(Exception e)
		{
			eventLog.warn(String.format("Created Product Quantity is 0.", ""));
			createProductQty = 0;
		}
		
		return createProductQty;
	}
	
	private int getAllQty(ProductRequestPlanKey pPlanKey)
	{
		int createProductQty = 0;
		
		try
		{
			//2019.02.14_hsryu_delete condition 'RESERVESTATE'. Becasue planQty of ProductrequestPlan is Error!
			String condition = "WHERE productRequestName = ? AND machineName = ? AND planReleasedTime = ? ";
			Object[] bindSet = new Object[] {pPlanKey.getProductRequestName(), pPlanKey.getAssignedMachineName(), 
					pPlanKey.getPlanReleasedTime()};
			
			List<ReserveLot> lotList = new ArrayList<ReserveLot>();
			
			lotList = ExtendedObjectProxy.getReserveLotService().select(condition, bindSet);
			
			for(ReserveLot dLot : lotList)
			{
				LotKey lKey = new LotKey(dLot.getLotName());
				createProductQty += LotServiceProxy.getLotService().selectByKey(lKey).getCreateProductQuantity();
			}
			
			
		}
		catch(Exception e)
		{
			eventLog.warn(String.format("Created Product Quantity is 0.", ""));
			createProductQty = 0;
		}
		
		return createProductQty;
	}
	
	private List<ReserveLot> getReserveLotList(ProductRequestPlan pPlanData) throws CustomException 
	{
		String condition = "Where productRequestName = ? and machineName = ? and planReleasedTime = ? and ReserveState = ?";

		Object bindSet[] = new Object[] { pPlanData.getKey().getProductRequestName(), pPlanData.getKey().getAssignedMachineName(), 
				pPlanData.getKey().getPlanReleasedTime() , "Reserved" };

		List<ReserveLot> reserveLotList = ExtendedObjectProxy.getReserveLotService().select(condition, bindSet);

		return reserveLotList;
	}
}
