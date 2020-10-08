package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.NonSortJobProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NonSortJobProductService extends CTORMService<NonSortJobProduct> {
	
	public static Log logger = LogFactory.getLog(NonSortJobProductService.class);
	
	private final String historyEntity = "NonSortJobProductHist";
	
	public List<NonSortJobProduct> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<NonSortJobProduct> result = super.select(condition, bindSet, NonSortJobProduct.class);
		
		return result;
	}
	
	public NonSortJobProduct selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(NonSortJobProduct.class, isLock, keySet);
	}
	
	public NonSortJobProduct create(EventInfo eventInfo, NonSortJobProduct dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, NonSortJobProduct dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public NonSortJobProduct modify(EventInfo eventInfo, NonSortJobProduct dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
	

}
