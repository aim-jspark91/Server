package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ReserveDummyGlass;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReserveDummyGlassService extends CTORMService<ReserveDummyGlass> {
	
	public static Log logger = LogFactory.getLog(ReserveDummyGlassService.class);
	
	private final String historyEntity = "ReserveDummyGlassHist";
	
	public List<ReserveDummyGlass> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<ReserveDummyGlass> result = super.select(condition, bindSet, ReserveDummyGlass.class);
		
		return result;
	}
	
	public ReserveDummyGlass selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(ReserveDummyGlass.class, isLock, keySet);
	}
	
	public ReserveDummyGlass create(EventInfo eventInfo, ReserveDummyGlass dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ReserveDummyGlass dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public ReserveDummyGlass modify(EventInfo eventInfo, ReserveDummyGlass dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 		
}
