package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.AfterAction;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
public class AfterActionService extends CTORMService<AfterAction>{
	public static Log logger = LogFactory.getLog(AfterAction.class);
	
	private final String historyEntity = "AfterActionHIST";
	
	public List<AfterAction> select(String condition, Object[] bindSet)
			throws CustomException
		{
			List<AfterAction> result = super.select(condition, bindSet, AfterAction.class);
			
			return result;
		}
		
		public AfterAction selectByKey(boolean isLock, Object[] keySet)
			throws CustomException
		{
			return super.selectByKey(AfterAction.class, isLock, keySet);
		}
		
		public AfterAction create(EventInfo eventInfo, AfterAction dataInfo)
			throws CustomException
		{
			super.insert(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		
		public void remove(EventInfo eventInfo, AfterAction dataInfo)
			throws CustomException
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			super.delete(dataInfo);
		}
		
		public AfterAction modify(EventInfo eventInfo, AfterAction dataInfo)
			throws CustomException
		{
			super.update(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
}


