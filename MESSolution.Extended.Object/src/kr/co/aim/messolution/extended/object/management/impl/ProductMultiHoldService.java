package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ProductMultiHold;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @since 2018.08.13
 * @author smkang
 * @see According to user's requirement, ProductName/ReasonCode/Department/EventComment are necessary to be keys.
 */
public class ProductMultiHoldService extends CTORMService<ProductMultiHold> {
	
	public static Log logger = LogFactory.getLog(ProductMultiHoldService.class);	
	
	public List<ProductMultiHold> select(String condition, Object[] bindSet) throws CustomException {
		return super.select(condition, bindSet, ProductMultiHold.class);
	}
		
	public ProductMultiHold selectByKey(boolean isLock, Object[] keySet) throws CustomException {
		return super.selectByKey(ProductMultiHold.class, isLock, keySet);
	}
		
	public ProductMultiHold create(EventInfo eventInfo, ProductMultiHold dataInfo) throws CustomException {
		super.insert(dataInfo);		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ProductMultiHold dataInfo) throws CustomException {
		super.delete(dataInfo);
	}
	
	public ProductMultiHold modify(EventInfo eventInfo, ProductMultiHold dataInfo) throws CustomException {
		super.update(dataInfo);		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}