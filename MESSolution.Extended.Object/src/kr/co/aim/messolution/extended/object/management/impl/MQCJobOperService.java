package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MQCJobOper;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MQCJobOperService extends CTORMService<MQCJobOper> {
	
	public static Log logger = LogFactory.getLog(MQCJobOperService.class);
	
	private final String historyEntity = "MQCJobOperHist";
	
	public List<MQCJobOper> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<MQCJobOper> result = new ArrayList<MQCJobOper>();
		
		try
		{
			result = super.select(condition, bindSet, MQCJobOper.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public MQCJobOper selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MQCJobOper.class, isLock, keySet);
	}
	
	public MQCJobOper create(EventInfo eventInfo, MQCJobOper dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, MQCJobOper dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public MQCJobOper modify(EventInfo eventInfo, MQCJobOper dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
