package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ShipProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ShipProductService extends CTORMService<ShipProduct> {
	
	public static Log logger = LogFactory.getLog(ShipProductService.class);
	
	private final String historyEntity = "";
	
	public List<ShipProduct> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<ShipProduct> result = super.select(condition, bindSet, ShipProduct.class);
		
		return result;
	}
	
	public ShipProduct selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(ShipProduct.class, isLock, keySet);
	}
	
	public ShipProduct create(EventInfo eventInfo, ShipProduct dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ShipProduct dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public ShipProduct modify(EventInfo eventInfo, ShipProduct dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
