package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ProductCutModeling;
import kr.co.aim.messolution.generic.errorHandler.CustomException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProductCutModelingService extends CTORMService<ProductCutModeling> {
	
	public static Log logger = LogFactory.getLog(ProductCutModelingService.class);
	
	private final String historyEntity = "ProductCutModelingHist";
	
	public List<ProductCutModeling> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<ProductCutModeling> result = super.select(condition, bindSet, ProductCutModeling.class);
		
		return result;
	}
	
	public ProductCutModeling selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(ProductCutModeling.class, isLock, keySet);
	}
	
	public ProductCutModeling create(EventInfo eventInfo, ProductCutModeling dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ProductCutModeling dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public ProductCutModeling modify(EventInfo eventInfo, ProductCutModeling dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
}
