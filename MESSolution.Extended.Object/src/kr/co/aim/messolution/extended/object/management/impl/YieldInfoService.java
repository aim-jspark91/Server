package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.YieldInfo;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
public class YieldInfoService extends CTORMService<YieldInfo>{
	public static Log logger = LogFactory.getLog(YieldInfo.class);
	
	private final String historyEntity = "YieldInfoHist";
	
	public List<YieldInfo> select(String condition, Object[] bindSet)
			throws CustomException
		{
			List<YieldInfo> result = super.select(condition, bindSet, YieldInfo.class);
			
			return result;
		}
		
		public YieldInfo selectByKey(boolean isLock, Object[] keySet)
			throws CustomException
		{
			return super.selectByKey(YieldInfo.class, isLock, keySet);
		}
		
		public YieldInfo create(EventInfo eventInfo, YieldInfo dataInfo)
			throws CustomException
		{
			super.insert(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		
		public void remove(EventInfo eventInfo, YieldInfo dataInfo)
			throws CustomException
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			super.delete(dataInfo);
		}
		
		public YieldInfo modify(EventInfo eventInfo, YieldInfo dataInfo)
			throws CustomException
		{
			super.update(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
}


