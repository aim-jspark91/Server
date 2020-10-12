package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MQCJobPosition;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MQCJobPositionService extends CTORMService<MQCJobPosition> {
	
	public static Log logger = LogFactory.getLog(MQCJobPositionService.class);
	
	private final String historyEntity = "MQCJobPositionHist";
	
	public List<MQCJobPosition> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<MQCJobPosition> result = new ArrayList<MQCJobPosition>();
		
		try
		{
			result = super.select(condition, bindSet, MQCJobPosition.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public MQCJobPosition selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MQCJobPosition.class, isLock, keySet);
	}
	
	public MQCJobPosition create(EventInfo eventInfo, MQCJobPosition dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, MQCJobPosition dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public MQCJobPosition modify(EventInfo eventInfo, MQCJobPosition dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
