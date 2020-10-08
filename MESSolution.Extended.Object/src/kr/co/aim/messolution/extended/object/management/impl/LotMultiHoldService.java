package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.LotMultiHold;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @since 2018.08.13
 * @author smkang
 * @see According to user's requirement, LotName/ReasonCode/Department/EventComment are necessary to be keys.
 */
public class LotMultiHoldService extends CTORMService<LotMultiHold> {
	
	public static Log logger = LogFactory.getLog(LotMultiHoldService.class);	
	
	public List<LotMultiHold> select(String condition, Object[] bindSet) throws CustomException {
		return super.select(condition, bindSet, LotMultiHold.class);
	}
		
	public LotMultiHold selectByKey(boolean isLock, Object[] keySet) throws CustomException {
		return super.selectByKey(LotMultiHold.class, isLock, keySet);
	}
		
	public LotMultiHold create(EventInfo eventInfo, LotMultiHold dataInfo) throws CustomException {
		super.insert(dataInfo);		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, LotMultiHold dataInfo) throws CustomException {
		super.delete(dataInfo);
	}
	
	public LotMultiHold modify(EventInfo eventInfo, LotMultiHold dataInfo) throws CustomException {
		super.update(dataInfo);		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}