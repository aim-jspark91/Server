package kr.co.aim.messolution.pms.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.pms.management.data.ESDTest;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@CTORMHeader(tag = "PMS",divider = "_")
public class ESDTestService extends CTORMService<ESDTest> {
	
	public static Log logger = LogFactory.getLog(ESDTestService.class);
	
	private final String historyEntity = "";
	
	public List<ESDTest> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<ESDTest> result = new ArrayList<ESDTest>();
		
		try
		{
			result = super.select(condition, bindSet, ESDTest.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public ESDTest selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ESDTest.class, isLock, keySet);
	}
	
	public ESDTest create(EventInfo eventInfo, ESDTest dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, "ESDTESTHISTORY", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ESDTest dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, "ESDTESTHISTORY", dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public ESDTest modify(EventInfo eventInfo, ESDTest dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, "ESDTESTHISTORY", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
