package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.HQGlassJudge;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HQGlassJudgeService extends CTORMService<HQGlassJudge> {
	
	public static Log logger = LogFactory.getLog(HQGlassJudgeService.class);
	
	private final String historyEntity = "HQGlassJudgeHist";
	
	public List<HQGlassJudge> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<HQGlassJudge> result = super.select(condition, bindSet, HQGlassJudge.class);
		
		return result;
	}
	
	public HQGlassJudge selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(HQGlassJudge.class, isLock, keySet);
	}
	
	public HQGlassJudge create(EventInfo eventInfo, HQGlassJudge dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, HQGlassJudge dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public HQGlassJudge modify(EventInfo eventInfo, HQGlassJudge dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	

}
