package kr.co.aim.messolution.fgms.management.impl;

import java.sql.Timestamp;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.fgms.FGMSServiceProxy;
import kr.co.aim.messolution.fgms.management.data.ShipRequest;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ShipRequestService extends CTORMService<ShipRequest> {
	
	public static Log logger = LogFactory.getLog(ShipRequestService.class);
	
	public List<ShipRequest> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<ShipRequest> result = super.select(condition, bindSet, ShipRequest.class);
		
		return result;
	}
	
	public ShipRequest selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(ShipRequest.class, isLock, keySet);
	}
	
	public ShipRequest create(EventInfo eventInfo, ShipRequest dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ShipRequest dataInfo)
		throws CustomException
	{
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public ShipRequest modify(EventInfo eventInfo, ShipRequest dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void insertShipRequest(EventInfo eventInfo, String invoiceNo, String invoiceType, String shipRequestState, Timestamp createTime,
			String createUser, Timestamp reserveTime, String reserveUser, Timestamp confirmTime, String confirmUser, 
			Timestamp completeTime, String completeUser, Timestamp cancelTime, String cancelUser, String customerNo, Timestamp planShipDate,
			String domesticExport)
	{
		try
		{
			ShipRequest shipRequestData = new ShipRequest();
			shipRequestData.setInVoiceNo(invoiceNo);
			shipRequestData.setInVoiceType(invoiceType);
			shipRequestData.setShipRequestState(shipRequestState);
			shipRequestData.setCreateTime(eventInfo.getEventTime());
			shipRequestData.setCreateUser(eventInfo.getEventUser());
			shipRequestData.setReserveTime(reserveTime);
			shipRequestData.setReserveUser(reserveUser);
			shipRequestData.setConfirmTime(confirmTime);
			shipRequestData.setConfirmUser(confirmUser);
			shipRequestData.setCompleteTime(completeTime);
			shipRequestData.setCompleteUser(completeUser);
			shipRequestData.setCancelTime(cancelTime);
			shipRequestData.setCancelUser(cancelUser);
			shipRequestData.setLastEventName(eventInfo.getEventName());
			shipRequestData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			shipRequestData.setLastEventTime(eventInfo.getEventTime());
			shipRequestData.setLastEventUser(eventInfo.getEventUser());
			shipRequestData.setLastEventComment(eventInfo.getEventComment());
			shipRequestData.setCustomerNo(customerNo);
			shipRequestData.setPlanShipDate(planShipDate);
			shipRequestData.setDomesticExport(domesticExport);
			
			FGMSServiceProxy.getShipRequestService().create(eventInfo, shipRequestData);
		}
		catch(Exception ex)
		{
			logger.info(ex.getMessage());
		}
	}
	
	public void updateShipRequest(EventInfo eventInfo, String invoiceNo, String invoiceType, String shipRequestState, Timestamp createTime,
			String createUser, Timestamp reserveTime, String reserveUser, Timestamp confirmTime, String confirmUser, 
			Timestamp completeTime, String completeUser, Timestamp cancelTime, String cancelUser, String customerNo, Timestamp planShipDate,
			String domesticExport)
	{
		try
		{
			ShipRequest shipRequestData = new ShipRequest(invoiceNo);
			shipRequestData.setInVoiceNo(invoiceNo);
			shipRequestData.setInVoiceType(invoiceType);
			shipRequestData.setShipRequestState(shipRequestState);
			shipRequestData.setCreateTime(eventInfo.getEventTime());
			shipRequestData.setCreateUser(eventInfo.getEventUser());
			shipRequestData.setReserveTime(reserveTime);
			shipRequestData.setReserveUser(reserveUser);
			shipRequestData.setConfirmTime(confirmTime);
			shipRequestData.setConfirmUser(confirmUser);
			shipRequestData.setCompleteTime(completeTime);
			shipRequestData.setCompleteUser(completeUser);
			shipRequestData.setCancelTime(cancelTime);
			shipRequestData.setCancelUser(cancelUser);
			shipRequestData.setLastEventName(eventInfo.getEventName());
			shipRequestData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			shipRequestData.setLastEventTime(eventInfo.getEventTime());
			shipRequestData.setLastEventUser(eventInfo.getEventUser());
			shipRequestData.setLastEventComment(eventInfo.getEventComment());
			shipRequestData.setCustomerNo(customerNo);
			shipRequestData.setPlanShipDate(planShipDate);
			shipRequestData.setDomesticExport(domesticExport);
				
			FGMSServiceProxy.getShipRequestService().modify(eventInfo, shipRequestData);
		}
		catch(Exception ex)
		{
			logger.info(ex.getMessage());
		}
	}
	
	public void removeShipRequest(EventInfo eventInfo, ShipRequest shipRequestData)
	{
		try
		{			
			FGMSServiceProxy.getShipRequestService().remove(eventInfo, shipRequestData);
		}
		catch(Exception ex)
		{
			logger.info(ex.getMessage());
		}
	}
	
	
	public ShipRequest getShipRequestData(String invoiceNo) throws CustomException
	{
		Object[] bindSet = new Object[]{invoiceNo};
		
		ShipRequest shipRequestData = FGMSServiceProxy.getShipRequestService().selectByKey(false, bindSet);
		
		return shipRequestData;
	}
	
	
}
