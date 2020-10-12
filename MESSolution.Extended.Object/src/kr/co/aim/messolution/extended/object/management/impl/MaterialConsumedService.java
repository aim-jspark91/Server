package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MaterialConsumed;
import kr.co.aim.messolution.generic.errorHandler.CustomException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class MaterialConsumedService extends CTORMService<MaterialConsumed> {
	
	public static Log logger = LogFactory.getLog(MaterialConsumedService.class);
	
	private final String historyEntity = "MaterialConsumedHistory";
	
	public List<MaterialConsumed> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<MaterialConsumed> result = super.select(condition, bindSet, MaterialConsumed.class);
		
		return result;
	}
	
	public MaterialConsumed selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(MaterialConsumed.class, isLock, keySet);
	}
	
	public MaterialConsumed create(EventInfo eventInfo, MaterialConsumed dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, MaterialConsumed dataInfo)
		throws CustomException
	{
//		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public MaterialConsumed modify(EventInfo eventInfo, MaterialConsumed dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	
	
}
