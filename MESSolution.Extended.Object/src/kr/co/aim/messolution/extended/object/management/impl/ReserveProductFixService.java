package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ReserveProductFix;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReserveProductFixService extends CTORMService<ReserveProductFix> {
public static Log logger = LogFactory.getLog(ReserveProductFixService.class);
	
	private final String historyEntity = "RESERVEPRODUCTFIXHIST";
	
	public List<ReserveProductFix> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<ReserveProductFix> result = super.select(condition, bindSet, ReserveProductFix.class);
		
		return result;
	}
	
	public ReserveProductFix selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ReserveProductFix.class, isLock, keySet);
	}
	
	public ReserveProductFix create(EventInfo eventInfo, ReserveProductFix dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ReserveProductFix dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}  
	
	/* public ReserveProductFix remove(EventInfo eventInfo, ReserveProductFix dataInfo)  // Add by Hu Haifeng 2016/12/22 10:34
			throws greenFrameDBErrorSignal
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			super.delete(dataInfo);
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
	*/
	public ReserveProductFix modify(EventInfo eventInfo, ReserveProductFix dataInfo)
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
