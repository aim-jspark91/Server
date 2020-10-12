package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MQCProductRelation;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MQCProductRelationService extends CTORMService<MQCProductRelation>{
	public static Log logger = LogFactory.getLog(MQCProductRelation.class);
	
	private final String historyEntity = "MQCProductRelationHist";
	public List<MQCProductRelation> select(String condition, Object[] bindSet)
			throws CustomException
		{
			List<MQCProductRelation> result = super.select(condition, bindSet, MQCProductRelation.class);
			
			return result;
		}
		
		public MQCProductRelation selectByKey(boolean isLock, Object[] keySet)
			throws CustomException
		{
			return super.selectByKey(MQCProductRelation.class, isLock, keySet);
		}
		
		public MQCProductRelation create(EventInfo eventInfo, MQCProductRelation dataInfo)
			throws CustomException
		{
			super.insert(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		
		public void remove(EventInfo eventInfo, MQCProductRelation dataInfo)
			throws CustomException
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			super.delete(dataInfo);
		}
		
		public MQCProductRelation modify(EventInfo eventInfo, MQCProductRelation dataInfo)
			throws CustomException
		{
			super.update(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
}
