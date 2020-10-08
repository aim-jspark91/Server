package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.VirtualGlass;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VirtualGlassService extends CTORMService<VirtualGlass> {
	
	public static Log logger = LogFactory.getLog(VirtualGlassService.class);
	
	private final String historyEntity = "VirtualGlassHistory";
	
	public List<VirtualGlass> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<VirtualGlass> result = super.select(condition, bindSet, VirtualGlass.class);
		
		return result;
	}
	
	public VirtualGlass selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(VirtualGlass.class, isLock, keySet);
	}
	
	public VirtualGlass create(EventInfo eventInfo, VirtualGlass dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, VirtualGlass dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public VirtualGlass modify(EventInfo eventInfo, VirtualGlass dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public List<VirtualGlass> getProductByCarrier(String carrierName) throws CustomException
	{
		List<VirtualGlass> result;
		
		try
		{
			result = ExtendedObjectProxy.getVirtualGlassService().select("carrier = ? order by position ", new Object[] {carrierName});
		}
		catch (Exception ex)
		{
			logger.warn("Nothing in carrier");
			result = new ArrayList<VirtualGlass>();
		}
		
		return result;
	}
	
	public String getSlotMapInfo(Durable durableData) throws CustomException
	{
		StringBuffer normalSlotInfoBuffer = new StringBuffer();
		
		// Get Durable's Capacity
		long iCapacity = durableData.getCapacity(); 
		
		// Get Product's Slot , These are not Scrapped Product.
		List<VirtualGlass> productList = new ArrayList<VirtualGlass>();
		
		try
		{
			productList = ExtendedObjectProxy.getVirtualGlassService().select("carrier = ? ", new Object[] {durableData.getKey().getDurableName()});
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-9999", "VirtualGlass", "No avaliable Product");
		}
		
		// Make Durable Normal SlotMapInfo
		for(int i = 0; i < iCapacity; i++)
		{
			normalSlotInfoBuffer.append(GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT);
		}
		
		logger.debug("Default Slot Map : " + normalSlotInfoBuffer);
		
		for(int i = 0; i < productList.size(); i++)
		{
			try
			{
				int index = (int)productList.get(i).getPosition() - 1;
				
				normalSlotInfoBuffer.replace(index, index+1, GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT);
			}
			catch (Exception ex)
			{
				logger.error("Position conversion failed");
				normalSlotInfoBuffer.replace(i, i+1, GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT);
			}
		}
		
		logger.info("Current Slot Map : " + normalSlotInfoBuffer);
		
		return normalSlotInfoBuffer.toString();
	}
}
