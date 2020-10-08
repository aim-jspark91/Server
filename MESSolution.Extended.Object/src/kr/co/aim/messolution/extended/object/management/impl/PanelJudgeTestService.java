package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.PanelJudgeTest;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PanelJudgeTestService extends CTORMService<PanelJudgeTest>{
	
	public static Log logger = LogFactory.getLog(PanelJudgeTest.class);
	
	private final String historyEntity = "";
	
	public List<PanelJudgeTest> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<PanelJudgeTest> result = super.select(condition, bindSet, PanelJudgeTest.class);
		
		return result;
	}
	
	public PanelJudgeTest selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(PanelJudgeTest.class, isLock, keySet);
	}
	
	public PanelJudgeTest create(EventInfo eventInfo, PanelJudgeTest dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, PanelJudgeTest dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public PanelJudgeTest modify(EventInfo eventInfo, PanelJudgeTest dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
