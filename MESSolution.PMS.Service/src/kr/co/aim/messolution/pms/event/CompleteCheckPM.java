package kr.co.aim.messolution.pms.event;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.Maintenance;
import kr.co.aim.messolution.pms.management.data.SparePartInOut;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class CompleteCheckPM extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String partResult        = SMessageUtil.getBodyItemValue(doc, "PARTRESULT", true);
		String maintenanceID     = SMessageUtil.getBodyItemValue(doc, "MAINTENANCEID", true);
		String maintStatus 		 = SMessageUtil.getBodyItemValue(doc, "MAINTSTATUS", true);		
		String checkUser 	 	 = SMessageUtil.getBodyItemValue(doc, "CHECKUSER", true);
		String checkResult 	 	 = SMessageUtil.getBodyItemValue(doc, "CHECKRESULT", true);
		List<Element> partList = null ;
		if(partResult.equals("Y"))
		{			
			partList = SMessageUtil.getBodySequenceItemList(doc, "PARTLIST", true);
		}
					
		EventInfo eventInfo      = EventInfoUtil.makeEventInfo("CompleteCheckPM", getEventUser(), getEventComment(), null, null);
		
		//PM Part
		Maintenance pmData = null;
		try
		{
			//Get
			pmData = PMSServiceProxy.getMaintenanceService().selectByKey(true, new Object[] {maintenanceID});
			String pmCode      = pmData.getPmCode();
			String groupName   = pmData.getGroupName();
			String className   = pmData.getClassName();
			String machineName = pmData.getMachineName();
			String machineType = pmData.getMachineType();
			String unitName    = pmData.getUnitName();
			String maintName   = pmData.getMaintName();		
			String maintType   = pmData.getMaintType();
			String maintDesc   = pmData.getMaintDesc();
			String maintControlDesc = pmData.getMaintControlDesc();
			String maintPurposeDesc = pmData.getMaintPurposeDesc();
			Timestamp maintPlandate = pmData.getMaintPlanDate();
			//maintStatus
			Timestamp maintStartDate = pmData.getMaintStartDate();
			Timestamp maintEnddate   = pmData.getMaintEndDate();
			Timestamp maintEarlydate = pmData.getMaintEarlyDate();
			Timestamp maintLimitDate = pmData.getMaintLimitDate();
			String maintElapsedDate  = pmData.getMaintElapsedDate();
			String maintDoDesc       = pmData.getMaintDesc();
			String executeUser       = pmData.getExecuteUser();
			String maintHelper       = pmData.getMaintHelper();
			//String checkResult       = pmData.getMaintHelper();
			String checkAction       = pmData.getCheckAction();
			String evaluationUser    = pmData.getEvaluationUser();
			String evaluationTime    = pmData.getEvaluationTime();
			String evaluationDesc    = pmData.getEvaluationDesc();
			String cancelFlag = pmData.getCancelFlag();
			String cancelUser = pmData.getCancelUser();
			String cancelTime = pmData.getCancelTime();
			String remark     = pmData.getRemark();
					
			//Set
			pmData = new Maintenance(maintenanceID);
			pmData.setPmCode(pmCode);
			pmData.setGroupName(groupName);
			pmData.setClassName(className);
			pmData.setMachineName(machineName);
			pmData.setMachineType(machineType);
			pmData.setUnitName(unitName);
			pmData.setMaintName(maintName);			
			pmData.setMaintType(maintType);
			pmData.setMaintDesc(maintDesc);
			pmData.setMaintControlDesc(maintControlDesc);
			pmData.setMaintPurposeDesc(maintPurposeDesc);
			pmData.setMaintPlanDate(maintPlandate);
			pmData.setMaintStatus(maintStatus);
			pmData.setMaintStartDate(maintStartDate);
			pmData.setMaintEndDate(maintEnddate);
			pmData.setMaintEarlyDate(maintEarlydate);
			pmData.setMaintLimitDate(maintLimitDate);
			pmData.setMaintElapsedDate(maintElapsedDate);
			pmData.setMaintDoDesc(maintDoDesc);
			pmData.setExecuteUser(executeUser);
			pmData.setMaintHelper(maintHelper);
			pmData.setCheckUser(checkUser);
			pmData.setCheckResult(checkResult);
			pmData.setCheckAction(checkAction);
			pmData.setEvaluationUser(evaluationUser);
			pmData.setEvaluationTime(evaluationTime);
			pmData.setEvaluationDesc(evaluationDesc);
			pmData.setCancelFlag(cancelFlag);
			pmData.setCancelUser(cancelUser);
			pmData.setCancelTime(cancelTime);
			pmData.setRemark(remark);

			pmData = PMSServiceProxy.getMaintenanceService().modify(eventInfo, pmData);
		}
		catch (Exception ex)
		{
			throw new CustomException("PMS-0064", pmData);
		}	
		
		//WHEN PART IS SELECTED
		if(partResult.equals("Y"))
		{
			//SparepartInout Part
			for(Element PartInfo : partList)
			{
				String partID = SMessageUtil.getChildText(PartInfo, "PARTID", true);
				String useQuantity = SMessageUtil.getChildText(PartInfo, "USEQUANTITY", true);
				
				long LuseQuantity = Long.parseLong(useQuantity);
				
				//Modify SparepartInout 
				List<SparePartInOut>SparepartInoutList = null;
				try
				{
					SparepartInoutList = PMSServiceProxy.getSparePartInOutService().
							select("PARTID = ? AND AVAILABLEORDERQTY IS NOT NULL", new Object []{partID});
				}
				catch (Exception ex)
				{
					eventLog.error(String.format("<<<<<<<<<<<<<<< No Data this PARTID = %s", partID));
					SparepartInoutList = new ArrayList<SparePartInOut>();
				}
				
				for(SparePartInOut sparePartInOut:SparepartInoutList)
				{
					try{
						//Get ResultQuantity
						String sAvailableOrderQty = sparePartInOut.getAvailableOrderQty().toString(); 
						String sOrderQuantity   = sparePartInOut.getOrderQuantity().toString();
						
						long availableOrderQty = Long.parseLong(sAvailableOrderQty);
						long orderQuantity   = Long.parseLong(sOrderQuantity);
						long decreaseResult  = availableOrderQty - LuseQuantity ;
		
						String orderNo     = sparePartInOut.getOrderNo();
						String orderType   = sparePartInOut.getOrderType();
						String orderReason = sparePartInOut.getOrderReason();
						String orderStatus = sparePartInOut.getOrderStatus();
						String orderUser   = sparePartInOut.getOrderUser();
						String orderDate   = sparePartInOut.getOrderDate().toString();
						String approveUser = sparePartInOut.getApproveUser();
						String approveDate = sparePartInOut.getApproveDate().toString();
						String PartID      = sparePartInOut.getPartID();
						String requestID   = sparePartInOut.getRequestID();
		
						//Set 
						sparePartInOut = new SparePartInOut(orderNo,PartID);
						
						sparePartInOut.setOrderNo(orderNo);
						sparePartInOut.setOrderType(orderType);
						sparePartInOut.setOrderReason(orderReason);
						sparePartInOut.setOrderStatus(orderStatus);
						sparePartInOut.setOrderUser(orderUser);
						sparePartInOut.setOrderDate(TimeStampUtil.getTimestamp(orderDate));
						sparePartInOut.setApproveUser(approveUser);
						sparePartInOut.setApproveDate(TimeStampUtil.getTimestamp(approveDate));
						sparePartInOut.setPartID(PartID);
						sparePartInOut.setOrderQuantity(orderQuantity);
						sparePartInOut.setAvailableOrderQty(decreaseResult);
						sparePartInOut.setRequestID(requestID);
						
						eventInfo.setEventName("decreaseByCheckPM"); 
						
						sparePartInOut = PMSServiceProxy.getSparePartInOutService().modify(eventInfo, sparePartInOut);
						
					}catch (Exception ex)
					{
						throw new CustomException("PMS-0064", maintenanceID);
					}
				}
			}
		}
			
		
		return doc;
	}
}
