package kr.co.aim.messolution.pms.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.pms.management.data.RequestSparePart;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@CTORMHeader(tag = "PMS",divider = "_")

public class RequestSparePartService extends CTORMService<RequestSparePart> {
	
	public static Log logger = LogFactory.getLog(RequestSparePartService.class);
	
	public List<RequestSparePart> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<RequestSparePart> result = new ArrayList<RequestSparePart>();
		
		try
		{
			result = super.select(condition, bindSet, RequestSparePart.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public RequestSparePart selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(RequestSparePart.class, isLock, keySet);
	}
	
	public RequestSparePart create(EventInfo eventInfo, RequestSparePart dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, "REQUESTSPAREPARTHIST", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, RequestSparePart dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, "REQUESTSPAREPARTHIST", dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public RequestSparePart modify(EventInfo eventInfo, RequestSparePart dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, "REQUESTSPAREPARTHIST", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
