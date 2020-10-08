package kr.co.aim.messolution.pms.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.pms.management.data.Purchase;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@CTORMHeader(tag = "PMS",divider = "_")
public class PurchaseService extends CTORMService<Purchase> {
	
	public static Log logger = LogFactory.getLog(PurchaseService.class);
	
	private final String historyEntity = "";
	
	public List<Purchase> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<Purchase> result = super.select(condition, bindSet, Purchase.class);
		
		return result;
	}
	
	public Purchase selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(Purchase.class, isLock, keySet);
	}
	
	public Purchase create(EventInfo eventInfo, Purchase dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, "PURCHASEHIST", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, Purchase dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, "PURCHASEHIST", dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public Purchase modify(EventInfo eventInfo, Purchase dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, "PURCHASEHIST", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
