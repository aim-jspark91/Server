package kr.co.aim.messolution.pms.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.pms.management.data.CheckID;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@CTORMHeader(tag = "PMS",divider = "_")
public class CheckIDService extends CTORMService<CheckID> {
	
	public static Log logger = LogFactory.getLog(CheckIDService.class);
	
	private final String historyEntity = "";
	
	public List<CheckID> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<CheckID> result = new ArrayList<CheckID>();
		
		try
		{
			result = super.select(condition, bindSet, CheckID.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public CheckID selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(CheckID.class, isLock, keySet);
	}
	
	public CheckID create(EventInfo eventInfo, CheckID dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, "historyEntity", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, CheckID dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, "historyEntity", dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public CheckID modify(EventInfo eventInfo, CheckID dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, "historyEntity", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
