package kr.co.aim.messolution.dispatch.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.dispatch.management.data.MAXWIP;
import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@CTORMHeader(tag = "CT",divider = "_")
public class MAXWIPService extends CTORMService<MAXWIP> {
	
	public static Log logger = LogFactory.getLog(MAXWIPService.class);
	
	private final String historyEntity = "MAXWIPHistory";
	
	public List<MAXWIP> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<MAXWIP> result = new ArrayList<MAXWIP>();
		
		try
		{
			result = super.select(condition, bindSet, MAXWIP.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public MAXWIP selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MAXWIP.class, isLock, keySet);
	}
	
	public MAXWIP create(EventInfo eventInfo, MAXWIP dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, MAXWIP dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public MAXWIP modify(EventInfo eventInfo, MAXWIP dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
