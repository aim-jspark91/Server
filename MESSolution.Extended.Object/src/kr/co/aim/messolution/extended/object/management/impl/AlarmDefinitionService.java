package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.AlarmDefinition;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AlarmDefinitionService extends CTORMService<AlarmDefinition> {
	
	public static Log logger = LogFactory.getLog(AlarmDefinitionService.class);
	
	private final String historyEntity = "";
	
	public List<AlarmDefinition> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<AlarmDefinition> result = super.select(condition, bindSet, AlarmDefinition.class);
		
		return result;
	}
	
	public AlarmDefinition selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(AlarmDefinition.class, isLock, keySet);
	}
	
	public AlarmDefinition create(EventInfo eventInfo, AlarmDefinition dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, AlarmDefinition dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public AlarmDefinition modify(EventInfo eventInfo, AlarmDefinition dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
