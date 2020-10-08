package kr.co.aim.messolution.pms.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.pms.management.data.BM;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@CTORMHeader(tag = "PMS",divider = "_")
public class BMService extends CTORMService<BM> {
	
	public static Log logger = LogFactory.getLog(BMService.class);
	
	private final String historyEntity = "";
	
	public List<BM> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<BM> result = new ArrayList<BM>();
		
		try
		{
			result = super.select(condition, bindSet, BM.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public BM selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(BM.class, isLock, keySet);
	}
	
	public BM create(EventInfo eventInfo, BM dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, "BMHISTORY", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, BM dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, "BMHISTORY", dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public BM modify(EventInfo eventInfo, BM dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, "BMHISTORY", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
