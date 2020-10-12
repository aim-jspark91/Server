package kr.co.aim.messolution.pms.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.pms.management.data.MaintenanceCheck;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@CTORMHeader(tag = "PMS",divider = "_")
public class MaintenanceCheckService extends CTORMService<MaintenanceCheck> {
	
	public static Log logger = LogFactory.getLog(MaintenanceCheckService.class);
	
	private final String historyEntity = "";
	
	public List<MaintenanceCheck> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<MaintenanceCheck> result = new ArrayList<MaintenanceCheck>();
		
		try
		{
			result = super.select(condition, bindSet, MaintenanceCheck.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public MaintenanceCheck selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MaintenanceCheck.class, isLock, keySet);
	}
	
	public MaintenanceCheck create(EventInfo eventInfo, MaintenanceCheck dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, "MAINTENANCEHIST", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, MaintenanceCheck dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public MaintenanceCheck modify(EventInfo eventInfo, MaintenanceCheck dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, "MAINTENANCEHIST", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
