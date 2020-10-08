package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ShipBox;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ShipBoxService extends CTORMService<ShipBox> {
	
	public static Log logger = LogFactory.getLog(ShipBoxService.class);
	
	private final String historyEntity = "";
	
	public List<ShipBox> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<ShipBox> result = super.select(condition, bindSet, ShipBox.class);
		
		return result;
	}
	
	public ShipBox selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(ShipBox.class, isLock, keySet);
	}
	
	public ShipBox create(EventInfo eventInfo, ShipBox dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ShipBox dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public ShipBox modify(EventInfo eventInfo, ShipBox dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
