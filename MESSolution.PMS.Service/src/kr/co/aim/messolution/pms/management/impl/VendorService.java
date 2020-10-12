package kr.co.aim.messolution.pms.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.pms.management.data.Vendor;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@CTORMHeader(tag = "PMS",divider = "_")
public class VendorService extends CTORMService<Vendor> {
	
	public static Log logger = LogFactory.getLog(VendorService.class);
	
	private final String historyEntity = "PMS_CreateBMHist";
	
	public List<Vendor> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<Vendor> result = new ArrayList<Vendor>();
		
		try
		{
			result = super.select(condition, bindSet, Vendor.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public Vendor selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(Vendor.class, isLock, keySet);
	}
	
	public Vendor create(EventInfo eventInfo, Vendor dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		//super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, Vendor dataInfo)
		throws greenFrameDBErrorSignal
	{
		//super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public Vendor modify(EventInfo eventInfo, Vendor dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		//super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
