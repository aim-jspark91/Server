package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ProductSpecIdleTime;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @since 2018.08.11
 * @author smkang
 * @see For management of ProductSpec idle time.
 */
public class ProductSpecIdleTimeService extends CTORMService<ProductSpecIdleTime> {
	
	public static Log logger = LogFactory.getLog(ProductSpecIdleTimeService.class);	
	private final String historyEntity = "ProductSpecIdleTimeHist";
	
	public List<ProductSpecIdleTime> select(String condition, Object[] bindSet) throws CustomException {
		return super.select(condition, bindSet, ProductSpecIdleTime.class);
	}
		
	public ProductSpecIdleTime selectByKey(boolean isLock, Object[] keySet) throws CustomException {
		return super.selectByKey(ProductSpecIdleTime.class, isLock, keySet);
	}
		
	public ProductSpecIdleTime create(EventInfo eventInfo, ProductSpecIdleTime dataInfo) throws CustomException {
		super.insert(dataInfo);		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ProductSpecIdleTime dataInfo) throws CustomException {
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);		
		super.delete(dataInfo);
	}
	
	public void forceResetProductSpecIdleTime(String ProductSpec,EventInfo eventInfo) 
	{
		
		
		
			try {
			//	log.info("Reset ProductSpec Idle Time! ProductSpec : " + ProductSpec );
				
				List<ProductSpecIdleTime> ProductSpecIdleTimeList = null;
				String condition = "PRODUCTSPECNAME=? ";
				Object[] bindSet = new Object[] {ProductSpec};

				ProductSpecIdleTimeList = ExtendedObjectProxy.getProductSpecIdleTimeService().select(condition, bindSet);

				for (ProductSpecIdleTime ProductSpecIdleTime : ProductSpecIdleTimeList) {
					ProductSpecIdleTime.setLastruntime(eventInfo.getEventTime());
					ProductSpecIdleTime.setLasteventname(eventInfo.getEventName());
					ProductSpecIdleTime.setLasteventtime(eventInfo.getEventTime());
					ProductSpecIdleTime.setLasteventtimekey(eventInfo.getEventTimeKey());
					
					ProductSpecIdleTime.setLasteventuser(eventInfo.getEventUser());
					ProductSpecIdleTime.setLasteventcomment(eventInfo.getEventComment());
					
					ExtendedObjectProxy.getProductSpecIdleTimeService().modify(eventInfo,ProductSpecIdleTime );
					
				}
				
				
				
			} catch (Exception e) {
			//	log.warn(e);
			}
		
		
		
		
	}
	
	
	public ProductSpecIdleTime modify(EventInfo eventInfo, ProductSpecIdleTime dataInfo) throws CustomException {
		super.update(dataInfo);		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}