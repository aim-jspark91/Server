package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.DailyCheck;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DailyCheckService extends CTORMService<DailyCheck>{
	public static Log logger = LogFactory.getLog(DailyCheckService.class);
	
	private final String historyEntity = "DailyCheckHist";
	public List<DailyCheck> select(String condition, Object[] bindSet)
			throws CustomException
		{
			List<DailyCheck> result = super.select(condition, bindSet, DailyCheck.class);
			
			return result;
		}
		
		public DailyCheck selectByKey(boolean isLock, Object[] keySet)
			throws CustomException
		{
			return super.selectByKey(DailyCheck.class, isLock, keySet);
		}
		
		public DailyCheck create(EventInfo eventInfo, DailyCheck dataInfo)
			throws CustomException
		{
			super.insert(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		
		public void remove(EventInfo eventInfo, DailyCheck dataInfo)
			throws CustomException
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			super.delete(dataInfo);
		}
		
		public DailyCheck modify(EventInfo eventInfo, DailyCheck dataInfo)
			throws CustomException
		{
			super.update(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
}
