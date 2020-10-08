package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.FirstGlassLogDetail;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FirstGlassLogDetailService extends CTORMService<FirstGlassLogDetail> {
	public static Log logger = LogFactory.getLog(FirstGlassLogDetailService.class);
	
	private final String historyEntity = "";
	
	public List<FirstGlassLogDetail> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<FirstGlassLogDetail> result = super.select(condition, bindSet, FirstGlassLogDetail.class);
		
		return result;
	}
	
	public FirstGlassLogDetail selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(FirstGlassLogDetail.class, isLock, keySet);
	}
	
	public FirstGlassLogDetail create(EventInfo eventInfo, FirstGlassLogDetail dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, FirstGlassLogDetail dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public FirstGlassLogDetail modify(EventInfo eventInfo, FirstGlassLogDetail dataInfo)
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
