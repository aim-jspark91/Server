package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.TimeSchedule;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TimeScheduleService extends CTORMService<TimeSchedule> {
	
	public static Log logger = LogFactory.getLog(TimeScheduleService.class);
	
	private final String historyEntity = "TimeScheduleHist";

	public List<TimeSchedule> select(String condition, Object[] bindSet)
		throws CustomException, NotFoundSignal
	{
		try
		{
			List<TimeSchedule> result = super.select(condition, bindSet, TimeSchedule.class);
			
			return result;
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "TimeSchedule", ne.getMessage());
		}
	}
	
	public TimeSchedule selectByKey(boolean isLock, Object[] keySet)
		throws CustomException, NotFoundSignal
	{
		try
		{
			return super.selectByKey(TimeSchedule.class, isLock, keySet);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "TimeSchedule", ne.getMessage());
		}
	}
	
	public TimeSchedule create(EventInfo eventInfo, TimeSchedule dataInfo)
		throws CustomException, NotFoundSignal
	{
		try
		{
			super.insert(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "TimeSchedule", ne.getMessage());
		}
	}
	
	public void remove(EventInfo eventInfo, TimeSchedule dataInfo)
		throws CustomException, NotFoundSignal
	{
		try
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			super.delete(dataInfo);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "TimeSchedule", ne.getMessage());
		}
	}
	
	public TimeSchedule modify(EventInfo eventInfo, TimeSchedule dataInfo)
		throws CustomException, NotFoundSignal
	{
		try
		{
			super.update(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "TimeSchedule", ne.getMessage());
		}
	}
	
}
