package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ProductQueueTime;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProductQueueTimeService extends CTORMService<ProductQueueTime> {
	
	public static Log logger = LogFactory.getLog(ProductQueueTimeService.class);
	
	private final String historyEntity = "ProductQueueTimeHist";
	
	public List<ProductQueueTime> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<ProductQueueTime> result = new ArrayList<ProductQueueTime>();
		
		try
		{
			result = super.select(condition, bindSet, ProductQueueTime.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public ProductQueueTime selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ProductQueueTime.class, isLock, keySet);
	}
	
	public ProductQueueTime create(EventInfo eventInfo, ProductQueueTime dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ProductQueueTime dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public ProductQueueTime modify(EventInfo eventInfo, ProductQueueTime dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
