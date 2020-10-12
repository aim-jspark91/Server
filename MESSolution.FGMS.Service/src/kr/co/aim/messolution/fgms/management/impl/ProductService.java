package kr.co.aim.messolution.fgms.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.fgms.management.data.Product;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProductService extends CTORMService<Product> {
	
	public static Log logger = LogFactory.getLog(ProductService.class);
	
	private final String historyEntity = "";
	
	public List<Product> select(String condition, Object[] bindSet)
		throws CustomException
	{
		try
		{
			List<Product> result = super.select(condition, bindSet, Product.class);
			
			return result;
		}
		catch (greenFrameErrorSignal ne)
		{
			throw new CustomException("SYS-9999", "Product", ne.getMessage());
		}
	}
	
	public Product selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(Product.class, isLock, keySet);
	}
	
	public Product create(EventInfo eventInfo, Product dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, "PRODUCTHISTORY", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, Product dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public Product modify(EventInfo eventInfo, Product dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, "PRODUCTHISTORY", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
