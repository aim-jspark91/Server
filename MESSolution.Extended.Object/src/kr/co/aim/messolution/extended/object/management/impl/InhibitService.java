package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.Inhibit;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InhibitService extends CTORMService<Inhibit> {
	
	public static Log logger = LogFactory.getLog(InhibitService.class);
	
	private final String historyEntity = "InhibitHist";
	
	public List<Inhibit> select(String condition, Object[] bindSet)
			throws greenFrameDBErrorSignal
	{
		List<Inhibit> result = new ArrayList<Inhibit>();
		
		try
		{
			result = super.select(condition, bindSet, Inhibit.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public Inhibit selectByKey(boolean isLock, Object[] keySet)
			throws greenFrameDBErrorSignal
	{
		return super.selectByKey(Inhibit.class, isLock, keySet);
	}
	
	public Inhibit create(EventInfo eventInfo, Inhibit dataInfo)
			throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, Inhibit dataInfo)
			throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public Inhibit modify(EventInfo eventInfo, Inhibit dataInfo)
			throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

}
