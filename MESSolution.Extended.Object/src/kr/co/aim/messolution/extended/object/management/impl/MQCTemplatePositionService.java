package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MQCTemplatePosition;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MQCTemplatePositionService extends CTORMService<MQCTemplatePosition> {
	
	public static Log logger = LogFactory.getLog(MQCTemplatePositionService.class);
	
	private final String historyEntity = "MQCTemplatePositionHist";
	
	public List<MQCTemplatePosition> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<MQCTemplatePosition> result = new ArrayList<MQCTemplatePosition>();
		
		try
		{
			result = super.select(condition, bindSet, MQCTemplatePosition.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public MQCTemplatePosition selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MQCTemplatePosition.class, isLock, keySet);
	}
	
	public MQCTemplatePosition create(EventInfo eventInfo, MQCTemplatePosition dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, MQCTemplatePosition dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public MQCTemplatePosition modify(EventInfo eventInfo, MQCTemplatePosition dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
