package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ScheduleJob;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @since 2019.04.23
 * @author smkang
 * @see According to Liu Hongwei's request, SCHsvr should be executed AP1 and AP2.
 *      For avoid duplication of schedule job, running information should be recorded.
 */
public class ScheduleJobService extends CTORMService<ScheduleJob> {
	
	public static Log logger = LogFactory.getLog(ScheduleJobService.class);	
	private final String historyEntity = "ScheduleJobHist";
	
	public List<ScheduleJob> select(String condition, Object[] bindSet) throws CustomException {
		return super.select(condition, bindSet, ScheduleJob.class);
	}
		
	public ScheduleJob selectByKey(boolean isLock, Object[] keySet) throws CustomException {
		return super.selectByKey(ScheduleJob.class, isLock, keySet);
	}
		
	public ScheduleJob create(EventInfo eventInfo, ScheduleJob dataInfo) throws CustomException {
		super.insert(dataInfo);		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ScheduleJob dataInfo) throws CustomException {
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);		
		super.delete(dataInfo);
	}
	
	public ScheduleJob modify(EventInfo eventInfo, ScheduleJob dataInfo) throws CustomException {
		super.update(dataInfo);		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}