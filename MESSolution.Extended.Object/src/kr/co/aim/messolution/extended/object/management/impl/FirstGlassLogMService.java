package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.FirstGlassLogM;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FirstGlassLogMService extends CTORMService<FirstGlassLogM> {
	public static Log logger = LogFactory.getLog(FirstGlassLogMService.class);
	
	private final String historyEntity = "";
	
	public List<FirstGlassLogM> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<FirstGlassLogM> result = super.select(condition, bindSet, FirstGlassLogM.class);
		
		return result;
	}
	
	public FirstGlassLogM selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(FirstGlassLogM.class, isLock, keySet);
	}
	
	public FirstGlassLogM create(EventInfo eventInfo, FirstGlassLogM dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, FirstGlassLogM dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public FirstGlassLogM modify(EventInfo eventInfo, FirstGlassLogM dataInfo)
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
