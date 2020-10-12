package kr.co.aim.messolution.dispatch.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.dispatch.management.data.STKCstLimit;
import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@CTORMHeader(tag = "CT",divider = "_")
public class STKCstLimitService extends CTORMService<STKCstLimit> {
	
	public static Log logger = LogFactory.getLog(STKCstLimitService.class);
	
	private final String historyEntity = "STKCSTLIMITHISTORY";
	
	public List<STKCstLimit> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<STKCstLimit> result = new ArrayList<STKCstLimit>();
		
		try
		{
			result = super.select(condition, bindSet, STKCstLimit.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public STKCstLimit selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(STKCstLimit.class, isLock, keySet);
	}
	
	public STKCstLimit create(EventInfo eventInfo, STKCstLimit dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, STKCstLimit dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo,this.historyEntity, dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public STKCstLimit modify(EventInfo eventInfo, STKCstLimit dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
