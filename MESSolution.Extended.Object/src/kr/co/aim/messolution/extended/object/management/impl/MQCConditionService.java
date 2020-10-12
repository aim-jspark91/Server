package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MQCCondition;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @since 2018.07.12
 * @author smkang
 * @see For management of equipment idle time.
 */
public class MQCConditionService extends CTORMService<MQCCondition> {
	
	public static Log logger = LogFactory.getLog(MQCConditionService.class);	
	private final String historyEntity = "MQCConditionHist";
	
	public List<MQCCondition> select(String condition, Object[] bindSet) throws CustomException {
		return super.select(condition, bindSet, MQCCondition.class);
	}
		
	public MQCCondition selectByKey(boolean isLock, Object[] keySet) throws CustomException {
		return super.selectByKey(MQCCondition.class, isLock, keySet);
	}
		
	public MQCCondition create(EventInfo eventInfo, MQCCondition dataInfo) throws CustomException {
		super.insert(dataInfo);		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, MQCCondition dataInfo) throws CustomException {
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);		
		super.delete(dataInfo);
	}
	
	public MQCCondition modify(EventInfo eventInfo, MQCCondition dataInfo) throws CustomException {
		super.update(dataInfo);		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}