package kr.co.aim.messolution.pms.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.pms.management.data.PMCode;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@CTORMHeader(tag = "PMS",divider = "_")
public class PMCodeService extends CTORMService<PMCode> {
	
	public static Log logger = LogFactory.getLog(PMCodeService.class);
	
	private final String historyEntity = "PMCODEHIST";
	
	public List<PMCode> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<PMCode> result = new ArrayList<PMCode>();
		
		try
		{
			result = super.select(condition, bindSet, PMCode.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public PMCode selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(PMCode.class, isLock, keySet);
	}
	
	public PMCode create(EventInfo eventInfo, PMCode dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, PMCode dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public PMCode modify(EventInfo eventInfo, PMCode dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
