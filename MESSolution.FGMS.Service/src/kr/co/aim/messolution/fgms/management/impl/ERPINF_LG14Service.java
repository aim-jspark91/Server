package kr.co.aim.messolution.fgms.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.fgms.management.data.ERPINF_LG14;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ERPINF_LG14Service extends CTORMService<ERPINF_LG14> {
	
	public static Log logger = LogFactory.getLog(ERPINF_LG14Service.class);
	
	private final String historyEntity = "";
	
	public List<ERPINF_LG14> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<ERPINF_LG14> result = super.select(condition, bindSet, ERPINF_LG14.class);
		
		return result;
	}
	
	public ERPINF_LG14 selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(ERPINF_LG14.class, isLock, keySet);
	}
	
	public ERPINF_LG14 create(EventInfo eventInfo, ERPINF_LG14 dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ERPINF_LG14 dataInfo)
		throws CustomException
	{
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public ERPINF_LG14 modify(EventInfo eventInfo, ERPINF_LG14 dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
