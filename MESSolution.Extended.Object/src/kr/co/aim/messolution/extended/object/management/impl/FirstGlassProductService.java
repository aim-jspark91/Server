package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.FirstGlassProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FirstGlassProductService extends CTORMService<FirstGlassProduct> {
	
	public static Log logger = LogFactory.getLog(FirstGlassProductService.class);
	
	private final String historyEntity = "FirstGlassProductHist";
	
	public List<FirstGlassProduct> select(String condition, Object[] bindSet)
		throws CustomException, NotFoundSignal
	{
		try
		{
			List<FirstGlassProduct> result = super.select(condition, bindSet, FirstGlassProduct.class);
			
			return result;
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "FirstGlassProduct", ne.getMessage());
		}
	}
	
	public FirstGlassProduct selectByKey(boolean isLock, Object[] keySet)
		throws CustomException, NotFoundSignal
	{
		try
		{
			return super.selectByKey(FirstGlassProduct.class, isLock, keySet);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "FirstGlassProduct", ne.getMessage());
		}
	}
	
	public FirstGlassProduct create(EventInfo eventInfo, FirstGlassProduct dataInfo)
		throws CustomException, NotFoundSignal
	{
		try
		{
			super.insert(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "FirstGlassProduct", ne.getMessage());
		}
	}
	
	public void remove(EventInfo eventInfo, FirstGlassProduct dataInfo)
		throws CustomException, NotFoundSignal
	{
		try
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			super.delete(dataInfo);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "FirstGlassProduct", ne.getMessage());
		}
	}
	
	public FirstGlassProduct modify(EventInfo eventInfo, FirstGlassProduct dataInfo)
		throws CustomException, NotFoundSignal
	{
		try
		{
			super.update(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "FirstGlassProduct", ne.getMessage());
		}
	}
	
	/**
	 * create pilot product
	 * @author swcho
	 * @since 2016.09.01
	 * @param eventInfo
	 * @param jobName
	 * @param productName
	 * @param lotName
	 * @return
	 * @throws CustomException
	 */
	public FirstGlassProduct createFirstGlassProduct(EventInfo eventInfo, String jobName, String productName, String lotName)
		throws CustomException
	{
		FirstGlassProduct productData = new FirstGlassProduct(jobName, productName);
		{
			productData.setLotName(lotName);
			
			//history info
			productData.setLastEventName(eventInfo.getEventName());
			productData.setLastEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			productData.setLastEventTime(eventInfo.getEventTime());
			productData.setLastEventUser(eventInfo.getEventUser());
			productData.setLastEventComment(eventInfo.getEventComment());
		}
		
		productData = ExtendedObjectProxy.getFirstGlassProductService().create(eventInfo, productData);
		
		return productData;
	}
	
	/**
	 * modify pilot product
	 * @author swcho
	 * @since 2016.09.26
	 * @param eventInfo
	 * @param productData
	 * @param lotName
	 * @return
	 * @throws CustomException
	 */
	public FirstGlassProduct modifyFirstGlassProduct(EventInfo eventInfo, FirstGlassProduct productData, String lotName)
		throws CustomException
	{
		productData.setLotName(lotName);
		
		//history info
		productData.setLastEventName(eventInfo.getEventName());
		productData.setLastEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		productData.setLastEventTime(eventInfo.getEventTime());
		productData.setLastEventUser(eventInfo.getEventUser());
		productData.setLastEventComment(eventInfo.getEventComment());
		
		productData = ExtendedObjectProxy.getFirstGlassProductService().modify(eventInfo, productData);
		
		return productData;
	}
}
