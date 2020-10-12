package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.DefectResult;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
public class DefectResultService extends CTORMService<DefectResult>{
	public static Log logger = LogFactory.getLog(DefectResult.class);
	
	private final String historyEntity = "DefectResultHist";
	
	public List<DefectResult> select(String condition, Object[] bindSet)
			throws CustomException
		{
			List<DefectResult> result = super.select(condition, bindSet, DefectResult.class);
			
			return result;
		}
		
		public DefectResult selectByKey(boolean isLock, Object[] keySet)
			throws CustomException
		{
			return super.selectByKey(DefectResult.class, isLock, keySet);
		}
		
//		public DefectResult create(EventInfo eventInfo, DefectResult dataInfo)
//			throws CustomException
//		{
//			super.insert(dataInfo);
//			
//			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
//			
//			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
//		}
		
		public void create(EventInfo eventInfo, DefectResult dataInfo) throws CustomException
		{
			super.insert(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
		}
		
		public void remove(EventInfo eventInfo, DefectResult dataInfo)
			throws CustomException
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			super.delete(dataInfo);
		}
		
		public DefectResult modify(EventInfo eventInfo, DefectResult dataInfo)
			throws CustomException
		{
			super.update(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		
//		public DefectResult modify(EventInfo eventInfo, DefectResult dataInfo)
//				throws CustomException
//			{
//				super.update(dataInfo);
//				
//				super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
//				
//				return dataInfo;
//			}
}


