package kr.co.aim.messolution.pms.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.pms.management.data.ESDTestSet;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@CTORMHeader(tag = "PMS",divider = "_")
public class ESDTestSetService extends CTORMService<ESDTestSet> {
	
	public static Log logger = LogFactory.getLog(ESDTestSetService.class);
	
	//private final String historyEntity = "";
	
	public List<ESDTestSet> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<ESDTestSet> result = new ArrayList<ESDTestSet>();
		
		try
		{
			result = super.select(condition, bindSet, ESDTestSet.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public ESDTestSet selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ESDTestSet.class, isLock, keySet);
	}
	
	public ESDTestSet create(EventInfo eventInfo, ESDTestSet dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		//super.addHistory(eventInfo, "ESDTESTHISTORY", dataInfo, logger);	
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ESDTestSet dataInfo)
		throws greenFrameDBErrorSignal
	{
		//super.addHistory(eventInfo, "ESDTESTHISTORY", dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public ESDTestSet modify(EventInfo eventInfo, ESDTestSet dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		//super.addHistory(eventInfo, "ESDTESTHISTORY", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
