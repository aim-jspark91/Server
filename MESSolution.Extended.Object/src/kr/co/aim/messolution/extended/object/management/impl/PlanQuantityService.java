package kr.co.aim.messolution.extended.object.management.impl;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.PlanQuantity;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PlanQuantityService extends CTORMService<PlanQuantity>
{
public static Log logger = LogFactory.getLog(PlanQuantityService.class);
	
	private final String historyEntity = "";
	
	public List<PlanQuantity> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<PlanQuantity> result = super.select(condition, bindSet, PlanQuantity.class);
		
		return result;
	}
	
	public PlanQuantity selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(PlanQuantity.class, isLock, keySet);
	}
	
	public PlanQuantity create(EventInfo eventInfo, PlanQuantity dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, PlanQuantity dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public PlanQuantity modify(EventInfo eventInfo, PlanQuantity dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
}
