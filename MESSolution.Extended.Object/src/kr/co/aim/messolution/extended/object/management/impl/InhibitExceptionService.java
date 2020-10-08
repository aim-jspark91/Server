package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.InhibitException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InhibitExceptionService extends CTORMService<InhibitException> {

	public static Log logger = LogFactory.getLog(InhibitExceptionService.class);
	
	private final String historyEntity = "InhibitExceptionHist";
	
	public List<InhibitException> select(String condition, Object[] bindSet)
			throws greenFrameDBErrorSignal
	{
		List<InhibitException> result = new ArrayList<InhibitException>();
		
		try
		{
			result = super.select(condition, bindSet, InhibitException.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public InhibitException selectByKey(boolean isLock, Object[] keySet)
			throws greenFrameDBErrorSignal
	{
		return super.selectByKey(InhibitException.class, isLock, keySet);
	}
	
	public InhibitException create(EventInfo eventInfo, InhibitException dataInfo)
			throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, InhibitException dataInfo)
			throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public InhibitException modify(EventInfo eventInfo, InhibitException dataInfo)
			throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}	
}
