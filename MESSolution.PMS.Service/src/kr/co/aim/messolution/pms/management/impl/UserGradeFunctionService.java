package kr.co.aim.messolution.pms.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.pms.management.data.UserGradeFunction;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@CTORMHeader(tag = "PMS",divider = "_")
public class UserGradeFunctionService extends CTORMService<UserGradeFunction> {
	
	public static Log logger = LogFactory.getLog(UserGradeFunctionService.class);
	
	private final String historyEntity = "";
	
	public List<UserGradeFunction> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<UserGradeFunction> result = new ArrayList<UserGradeFunction>();
		
		try
		{
			result = super.select(condition, bindSet, UserGradeFunction.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public UserGradeFunction selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(UserGradeFunction.class, isLock, keySet);
	}
	
	public UserGradeFunction create(EventInfo eventInfo, UserGradeFunction dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, "historyEntity", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, UserGradeFunction dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, "historyEntity", dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public UserGradeFunction modify(EventInfo eventInfo, UserGradeFunction dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, "historyEntity", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
