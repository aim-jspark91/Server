package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.FileJudgeSetting;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
public class FileJudgeSettingService extends CTORMService<FileJudgeSetting>{
	public static Log logger = LogFactory.getLog(FileJudgeSetting.class);
	
	private final String historyEntity = "FileJudgeSettingHist";
	
	public List<FileJudgeSetting> select(String condition, Object[] bindSet)
			throws CustomException
		{
			List<FileJudgeSetting> result = super.select(condition, bindSet, FileJudgeSetting.class);
			
			return result;
		}
		
		public FileJudgeSetting selectByKey(boolean isLock, Object[] keySet)
			throws CustomException
		{
			return super.selectByKey(FileJudgeSetting.class, isLock, keySet);
		}
		
		public FileJudgeSetting create(EventInfo eventInfo, FileJudgeSetting dataInfo)
			throws CustomException
		{
			super.insert(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		
		public void remove(EventInfo eventInfo, FileJudgeSetting dataInfo)
			throws CustomException
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			super.delete(dataInfo);
		}
		
		public FileJudgeSetting modify(EventInfo eventInfo, FileJudgeSetting dataInfo)
			throws CustomException
		{
			super.update(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
}


