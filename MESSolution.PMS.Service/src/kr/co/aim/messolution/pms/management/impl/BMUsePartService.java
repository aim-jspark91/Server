package kr.co.aim.messolution.pms.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.pms.management.data.BMUsePart;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@CTORMHeader(tag = "PMS",divider = "_")
public class BMUsePartService extends CTORMService<BMUsePart> {
	
	public static Log logger = LogFactory.getLog(BMUsePartService.class);
	
	private final String historyEntity = "";
	
	public List<BMUsePart> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<BMUsePart> result = new ArrayList<BMUsePart>();
		
		try
		{
			result = super.select(condition, bindSet, BMUsePart.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public BMUsePart selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(BMUsePart.class, isLock, keySet);
	}
	
	public BMUsePart create(EventInfo eventInfo, BMUsePart dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, "historyEntity", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, BMUsePart dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, "historyEntity", dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public BMUsePart modify(EventInfo eventInfo, BMUsePart dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, "historyEntity", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
