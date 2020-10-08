package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ProductRequestPlanHistory;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProductRequestPlanHistoryService extends CTORMService<ProductRequestPlanHistory> {
	
	public static Log logger = LogFactory.getLog(ProductRequestPlanHistoryService.class);
	
	public List<ProductRequestPlanHistory> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<ProductRequestPlanHistory> result = super.select(condition, bindSet, ProductRequestPlanHistory.class);
		
		return result;
	}
	
	public ProductRequestPlanHistory selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(ProductRequestPlanHistory.class, isLock, keySet);
	}
	
	public ProductRequestPlanHistory create(EventInfo eventInfo, ProductRequestPlanHistory dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
			
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ProductRequestPlanHistory dataInfo)
		throws CustomException
	{

		super.delete(dataInfo);
	}
	
	public ProductRequestPlanHistory modify(EventInfo eventInfo, ProductRequestPlanHistory dataInfo)
		throws CustomException
	{
		super.update(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
