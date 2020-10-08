package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.AlarmMailTemplate;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AlarmMailTemplateService extends CTORMService<AlarmMailTemplate> {
	
	public static Log logger = LogFactory.getLog(AlarmMailTemplateService.class);
	
	private final String historyEntity = "";
	
	public List<AlarmMailTemplate> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<AlarmMailTemplate> result = super.select(condition, bindSet, AlarmMailTemplate.class);
		
		return result;
	}
	
	public AlarmMailTemplate selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(AlarmMailTemplate.class, isLock, keySet);
	}
	
	public AlarmMailTemplate create(EventInfo eventInfo, AlarmMailTemplate dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, AlarmMailTemplate dataInfo)
		throws CustomException
	{
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public AlarmMailTemplate modify(EventInfo eventInfo, AlarmMailTemplate dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
