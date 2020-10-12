package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.Inhibit;
import kr.co.aim.messolution.extended.object.management.data.LocalRunException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LocalRunExceptionService extends CTORMService<LocalRunException> {
	
	public static Log logger = LogFactory.getLog(LocalRunExceptionService.class);
	
	private final String historyEntity = "LocalRunExceptionHist";
	
	public List<LocalRunException> select(String condition, Object[] bindSet)
			throws greenFrameDBErrorSignal
	{
		List<LocalRunException> result = new ArrayList<LocalRunException>();
		
		try
		{
			result = super.select(condition, bindSet, LocalRunException.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public LocalRunException selectByKey(boolean isLock, Object[] keySet)
			throws greenFrameDBErrorSignal
	{
		return super.selectByKey(LocalRunException.class, isLock, keySet);
	}
	
	public LocalRunException create(EventInfo eventInfo, LocalRunException dataInfo)
			throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, LocalRunException dataInfo)
			throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public LocalRunException modify(EventInfo eventInfo, LocalRunException dataInfo)
			throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

}
