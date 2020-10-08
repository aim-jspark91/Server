package kr.co.aim.messolution.pms.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.pms.management.data.SparePartInOut;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@CTORMHeader(tag = "PMS",divider = "_")
public class SparePartInOutService extends CTORMService<SparePartInOut> {
	
	public static Log logger = LogFactory.getLog(SparePartInOutService.class);
	
	public List<SparePartInOut> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<SparePartInOut> result = new ArrayList<SparePartInOut>();
		
		try
		{
			result = super.select(condition, bindSet, SparePartInOut.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public SparePartInOut selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(SparePartInOut.class, isLock, keySet);
	}
	
	public SparePartInOut create(EventInfo eventInfo, SparePartInOut dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, "SPAREPARTINOUTHIST", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, SparePartInOut dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, "SPAREPARTINOUTHIST", dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public SparePartInOut modify(EventInfo eventInfo, SparePartInOut dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, "SPAREPARTINOUTHIST", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
