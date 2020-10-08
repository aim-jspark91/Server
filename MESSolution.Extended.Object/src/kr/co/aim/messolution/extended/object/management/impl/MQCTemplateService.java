package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MQCTemplate;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MQCTemplateService extends CTORMService<MQCTemplate> {
	
	public static Log logger = LogFactory.getLog(MQCTemplateService.class);
	
	private final String historyEntity = "MQCTemplateHist";
	
	public List<MQCTemplate> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<MQCTemplate> result = new ArrayList<MQCTemplate>();
		
		try
		{
			result = super.select(condition, bindSet, MQCTemplate.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public MQCTemplate selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MQCTemplate.class, isLock, keySet);
	}
	
	public MQCTemplate create(EventInfo eventInfo, MQCTemplate dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, MQCTemplate dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public MQCTemplate modify(EventInfo eventInfo, MQCTemplate dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
