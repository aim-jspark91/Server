package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.AutoMQCSetting;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
public class AutoMQCSettingService extends CTORMService<AutoMQCSetting>{
	public static Log logger = LogFactory.getLog(AutoMQCSetting.class);
	
	private final String historyEntity = "AutoMQCSettingHIST";
	
	public List<AutoMQCSetting> select(String condition, Object[] bindSet)
			throws CustomException
		{
			List<AutoMQCSetting> result = super.select(condition, bindSet, AutoMQCSetting.class);
			
			return result;
		}
		
		public AutoMQCSetting selectByKey(boolean isLock, Object[] keySet)
			throws CustomException
		{
			return super.selectByKey(AutoMQCSetting.class, isLock, keySet);
		}
		
		public AutoMQCSetting create(EventInfo eventInfo, AutoMQCSetting dataInfo)
			throws CustomException
		{
			super.insert(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		
		public void remove(EventInfo eventInfo, AutoMQCSetting dataInfo)
			throws CustomException
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			super.delete(dataInfo);
		}
		
		public AutoMQCSetting modify(EventInfo eventInfo, AutoMQCSetting dataInfo)
			throws CustomException
		{
			super.update(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
}


