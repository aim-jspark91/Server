package kr.co.aim.messolution.dispatch.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.dispatch.management.data.KANBAN;
import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@CTORMHeader(tag = "CT",divider = "_")
public class KANBANService extends CTORMService<KANBAN> {
	
	public static Log logger = LogFactory.getLog(KANBANService.class);
	
	private final String historyEntity = "KANBANHistory";
	
	public List<KANBAN> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<KANBAN> result = new ArrayList<KANBAN>();
		
		try
		{
			result = super.select(condition, bindSet, KANBAN.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public KANBAN selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(KANBAN.class, isLock, keySet);
	}
	
	public KANBAN create(EventInfo eventInfo, KANBAN dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, KANBAN dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public KANBAN modify(EventInfo eventInfo, KANBAN dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
