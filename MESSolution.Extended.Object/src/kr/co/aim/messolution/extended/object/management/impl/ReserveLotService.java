package kr.co.aim.messolution.extended.object.management.impl;

import java.sql.Timestamp;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReserveLotService extends CTORMService<ReserveLot> {
	
	public static Log logger = LogFactory.getLog(ReserveLotService.class);
	
	private final String historyEntity = "RESERVELOTHISTORY";
	
	public List<ReserveLot> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<ReserveLot> result = super.select(condition, bindSet, ReserveLot.class);
		
		return result;
	}
	
	public ReserveLot selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ReserveLot.class, isLock, keySet);
	}
	
	public ReserveLot create(EventInfo eventInfo, ReserveLot dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ReserveLot dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public ReserveLot modify(EventInfo eventInfo, ReserveLot dataInfo)
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	/**
	 * getFirstPlanByMachine
	 * 160329 by hjung : service object changed
	 * @author hjung
	 * @since 2016.03.29
	 * @param machineName
	 * @param productRequestName
	 * @throws CustomException
	 */
	public ReserveLot getFirstReserveLot(String machineName, String productRequest, Timestamp planReleasedTime)
		throws CustomException
	{
		try
		{
			String condition = "machineName = ? and productRequestName =? and planReleasedTime =? and reserveState = ? order by position, reserveTimekey";
			
			Object bindSet[] = new Object[]{machineName, productRequest, planReleasedTime, "Reserved"};
			List<ReserveLot> pLotList = ExtendedObjectProxy.getReserveLotService().select(condition, bindSet);
					
			return pLotList.get(0);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			throw new CustomException("LOT-0207", machineName, productRequest, planReleasedTime);
		}
	}
}