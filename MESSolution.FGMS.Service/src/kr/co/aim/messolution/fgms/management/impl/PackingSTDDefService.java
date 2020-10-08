package kr.co.aim.messolution.fgms.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.fgms.management.data.PackingSTDDef;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PackingSTDDefService extends CTORMService<PackingSTDDef> {
	
	public static Log logger = LogFactory.getLog(PackingSTDDefService.class);
	
	public List<PackingSTDDef> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<PackingSTDDef> result = super.select(condition, bindSet, PackingSTDDef.class);
		
		return result;
	}
	
	public PackingSTDDef selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(PackingSTDDef.class, isLock, keySet);
	}
	
	public PackingSTDDef create(EventInfo eventInfo, PackingSTDDef dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, PackingSTDDef dataInfo)
		throws CustomException
	{
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public PackingSTDDef modify(EventInfo eventInfo, PackingSTDDef dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
