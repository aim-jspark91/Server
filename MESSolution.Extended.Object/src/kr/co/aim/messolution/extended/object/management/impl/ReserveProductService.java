package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ReserveProduct;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReserveProductService extends CTORMService<ReserveProduct> {
public static Log logger = LogFactory.getLog(ReserveProductService.class);
	
	private final String historyEntity = "RESERVEPRODUCTHIST";
	
	public List<ReserveProduct> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<ReserveProduct> result = super.select(condition, bindSet, ReserveProduct.class);
		
		return result;
	}
	
	public ReserveProduct selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ReserveProduct.class, isLock, keySet);
	}
	
	public ReserveProduct create(EventInfo eventInfo, ReserveProduct dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ReserveProduct dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public ReserveProduct modify(EventInfo eventInfo, ReserveProduct dataInfo)
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
