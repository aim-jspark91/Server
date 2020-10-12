package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.DefectRuleSetting;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
public class DefectRuleSettingService extends CTORMService<DefectRuleSetting>{
	public static Log logger = LogFactory.getLog(DefectRuleSetting.class);
	
	private final String historyEntity = "DefectRuleSettingHist";
	
	public List<DefectRuleSetting> select(String condition, Object[] bindSet)
			throws CustomException
		{
			List<DefectRuleSetting> result = super.select(condition, bindSet, DefectRuleSetting.class);
			
			return result;
		}
		
		public DefectRuleSetting selectByKey(boolean isLock, Object[] keySet)
			throws CustomException
		{
			return super.selectByKey(DefectRuleSetting.class, isLock, keySet);
		}
		
		public DefectRuleSetting create(EventInfo eventInfo, DefectRuleSetting dataInfo)
			throws CustomException
		{
			super.insert(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		
		public void remove(EventInfo eventInfo, DefectRuleSetting dataInfo)
			throws CustomException
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			super.delete(dataInfo);
		}
		
		public DefectRuleSetting modify(EventInfo eventInfo, DefectRuleSetting dataInfo)
			throws CustomException
		{
			super.update(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
}


