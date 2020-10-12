package kr.co.aim.messolution.pms.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.pms.management.data.UserGrade;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@CTORMHeader(tag = "PMS",divider = "_")
public class UserGradeService extends CTORMService<UserGrade> {
	
	public static Log logger = LogFactory.getLog(UserGradeService.class);
	
	private final String historyEntity = "";
	
	public List<UserGrade> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<UserGrade> result = new ArrayList<UserGrade>();
		
		try
		{
			result = super.select(condition, bindSet, UserGrade.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public UserGrade selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(UserGrade.class, isLock, keySet);
	}
	
	public UserGrade create(EventInfo eventInfo, UserGrade dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, "historyEntity", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, UserGrade dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, "historyEntity", dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public UserGrade modify(EventInfo eventInfo, UserGrade dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, "historyEntity", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
