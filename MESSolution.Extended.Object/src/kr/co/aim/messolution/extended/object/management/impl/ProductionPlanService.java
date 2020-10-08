package kr.co.aim.messolution.extended.object.management.impl;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ProductionPlan;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProductionPlanService extends CTORMService<ProductionPlan> {
	
	public static Log logger = LogFactory.getLog(ProductionPlanService.class);
	
	private final String historyEntity = "";
	
	public List<ProductionPlan> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<ProductionPlan> result = super.select(condition, bindSet, ProductionPlan.class);
		
		return result;
	}
	
	public ProductionPlan selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(ProductionPlan.class, isLock, keySet);
	}
	
	public ProductionPlan create(EventInfo eventInfo, ProductionPlan dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ProductionPlan dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public ProductionPlan modify(EventInfo eventInfo, ProductionPlan dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
