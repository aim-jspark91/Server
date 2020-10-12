package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.FirstGlassLogS;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FirstGlassLogSService extends CTORMService<FirstGlassLogS> {
	public static Log logger = LogFactory.getLog(FirstGlassLogSService.class);
	
	private final String historyEntity = "";
	
	public List<FirstGlassLogS> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<FirstGlassLogS> result = super.select(condition, bindSet, FirstGlassLogS.class);
		
		return result;
	}
	
	public FirstGlassLogS selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(FirstGlassLogS.class, isLock, keySet);
	}
	
	public FirstGlassLogS create(EventInfo eventInfo, FirstGlassLogS dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, FirstGlassLogS dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public FirstGlassLogS modify(EventInfo eventInfo, FirstGlassLogS dataInfo)
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
