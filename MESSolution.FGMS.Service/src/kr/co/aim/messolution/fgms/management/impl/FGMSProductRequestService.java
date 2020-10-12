package kr.co.aim.messolution.fgms.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.fgms.management.data.FGMSProductRequest;
import kr.co.aim.messolution.fgms.management.data.Product;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FGMSProductRequestService extends CTORMService<FGMSProductRequest> {
public static Log logger = LogFactory.getLog(FGMSProductRequestService.class);
	
	private final String historyEntity = "";
	
	public List<FGMSProductRequest> select(String condition, Object[] bindSet)
		throws CustomException
	{
		try
		{
			List<FGMSProductRequest> result = super.select(condition, bindSet, Product.class);
			
			return result;
		}
		catch (greenFrameErrorSignal ne)
		{
			throw new CustomException("SYS-9999", "Product", ne.getMessage());
		}
	}
	
	public FGMSProductRequest selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(FGMSProductRequest.class, isLock, keySet);
	}
	
	public FGMSProductRequest create(EventInfo eventInfo, FGMSProductRequest dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		//super.addHistory(eventInfo, "PRODUCTHISTORY", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, FGMSProductRequest dataInfo)
		throws CustomException
	{
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public FGMSProductRequest modify(EventInfo eventInfo, FGMSProductRequest dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		//super.addHistory(eventInfo, "PRODUCTHISTORY", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 

}
