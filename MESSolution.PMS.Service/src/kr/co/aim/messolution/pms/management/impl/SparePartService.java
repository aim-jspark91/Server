package kr.co.aim.messolution.pms.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.pms.management.data.SparePart;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@CTORMHeader(tag = "PMS",divider = "_")
public class SparePartService extends CTORMService<SparePart> {
	
	public static Log logger = LogFactory.getLog(SparePartService.class);
	
	private final String historyEntity = "";
	
	public List<SparePart> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<SparePart> result = new ArrayList<SparePart>();
		
		try
		{
			result = super.select(condition, bindSet, SparePart.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public SparePart selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(SparePart.class, isLock, keySet);
	}
	
	public SparePart create(EventInfo eventInfo, SparePart dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, "SPAREPARTHIST", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, SparePart dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, "SPAREPARTHIST", dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public SparePart modify(EventInfo eventInfo, SparePart dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, "SPAREPARTHIST", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
