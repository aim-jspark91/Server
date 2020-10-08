package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.PanelInspData;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PanelInspDataService extends CTORMService<PanelInspData> {
	
	public static Log logger = LogFactory.getLog(PanelInspData.class);
	
	private final String historyEntity = "";
	
	public List<PanelInspData> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<PanelInspData> result = super.select(condition, bindSet, PanelInspData.class);
		
		return result;
	}
	
	public PanelInspData selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(PanelInspData.class, isLock, keySet);
	}
	
	public PanelInspData create(EventInfo eventInfo, PanelInspData dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, PanelInspData dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public PanelInspData modify(EventInfo eventInfo, PanelInspData dataInfo)
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}