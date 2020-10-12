package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MachineAlarmList;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MachineAlarmListService extends CTORMService<MachineAlarmList> {
	
	public static Log logger = LogFactory.getLog(MachineAlarmListService.class);
	
	private final String historyEntity = "MachineAlarmHist";
	
	public List<MachineAlarmList> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<MachineAlarmList> result = super.select(condition, bindSet, MachineAlarmList.class);
		
		return result;
	}
	
	public MachineAlarmList selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(MachineAlarmList.class, isLock, keySet);
	}
	
	public MachineAlarmList create(EventInfo eventInfo, MachineAlarmList dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, MachineAlarmList dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public MachineAlarmList modify(EventInfo eventInfo, MachineAlarmList dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
