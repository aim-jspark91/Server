package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MaskSheetTray;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MaskSheetTrayService extends CTORMService<MaskSheetTray> {
	
	public static Log logger = LogFactory.getLog(MaskSheetTrayService.class);
	
	private final String historyEntity = "MaskSheetTrayHist";
	
	public List<MaskSheetTray> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<MaskSheetTray> result = new ArrayList<MaskSheetTray>();
		
		try
		{
			result = super.select(condition, bindSet, MaskSheetTray.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public MaskSheetTray selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MaskSheetTray.class, isLock, keySet);
	}
	
	public MaskSheetTray create(EventInfo eventInfo, MaskSheetTray dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, MaskSheetTray dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public MaskSheetTray modify(EventInfo eventInfo, MaskSheetTray dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
