package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ShipPallet;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ShipPalletService extends CTORMService<ShipPallet> {
	
	public static Log logger = LogFactory.getLog(ShipPalletService.class);
	
	private final String historyEntity = "";
	
	public List<ShipPallet> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<ShipPallet> result = super.select(condition, bindSet, ShipPallet.class);
		
		return result;
	}
	
	public ShipPallet selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(ShipPallet.class, isLock, keySet);
	}
	
	public ShipPallet create(EventInfo eventInfo, ShipPallet dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ShipPallet dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public ShipPallet modify(EventInfo eventInfo, ShipPallet dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
