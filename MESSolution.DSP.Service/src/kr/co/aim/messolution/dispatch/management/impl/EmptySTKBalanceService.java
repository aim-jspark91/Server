package kr.co.aim.messolution.dispatch.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.dispatch.management.data.EmptySTKBalance;
import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@CTORMHeader(tag = "CT",divider = "_")
public class EmptySTKBalanceService extends CTORMService<EmptySTKBalance> {
	
	public static Log logger = LogFactory.getLog(EmptySTKBalanceService.class);
	
	private final String historyEntity = "EmptySTKBalanceHistory";
	
	public List<EmptySTKBalance> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<EmptySTKBalance> result = new ArrayList<EmptySTKBalance>();
		
		try
		{
			result = super.select(condition, bindSet, EmptySTKBalance.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public EmptySTKBalance selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(EmptySTKBalance.class, isLock, keySet);
	}
	
	public EmptySTKBalance create(EventInfo eventInfo, EmptySTKBalance dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, EmptySTKBalance dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public EmptySTKBalance modify(EventInfo eventInfo, EmptySTKBalance dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
