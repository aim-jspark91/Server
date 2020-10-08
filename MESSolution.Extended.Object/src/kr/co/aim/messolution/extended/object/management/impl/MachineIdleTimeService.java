package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MachineIdleTime;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @since 2018.08.11
 * @author smkang
 * @see For management of equipment idle time.
 */
public class MachineIdleTimeService extends CTORMService<MachineIdleTime> {
	
	public static Log logger = LogFactory.getLog(MachineIdleTimeService.class);	
	private final String historyEntity = "MachineIdleTimeHist";
	
	public List<MachineIdleTime> select(String condition, Object[] bindSet) throws CustomException {
		return super.select(condition, bindSet, MachineIdleTime.class);
	}
		
	public MachineIdleTime selectByKey(boolean isLock, Object[] keySet) throws CustomException {
		return super.selectByKey(MachineIdleTime.class, isLock, keySet);
	}
		
	public MachineIdleTime create(EventInfo eventInfo, MachineIdleTime dataInfo) throws CustomException {
		super.insert(dataInfo);		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, MachineIdleTime dataInfo) throws CustomException {
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);		
		super.delete(dataInfo);
	}
	
	public MachineIdleTime modify(EventInfo eventInfo, MachineIdleTime dataInfo) throws CustomException {
		super.update(dataInfo);		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}