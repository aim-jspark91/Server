package kr.co.aim.messolution.fgms.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.fgms.FGMSServiceProxy;
import kr.co.aim.messolution.fgms.management.data.ShipRequestPS;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ShipRequestPSService extends CTORMService<ShipRequestPS> {
	
	public static Log logger = LogFactory.getLog(ShipRequestPSService.class);
	
	private final String historyEntity = "";
	
	public List<ShipRequestPS> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<ShipRequestPS> result = super.select(condition, bindSet, ShipRequestPS.class);
		
		return result;
	}
	
	public ShipRequestPS selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(ShipRequestPS.class, isLock, keySet);
	}
	
	public ShipRequestPS create(EventInfo eventInfo, ShipRequestPS dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ShipRequestPS dataInfo)
		throws CustomException
	{
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public ShipRequestPS modify(EventInfo eventInfo, ShipRequestPS dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void insertShipRequestPS(EventInfo eventInfo, String invoiceDetailNo, String invoiceNo, long boxQuantity,
			long palletQuantity, String productSpecName, long productQuantity, String createUser, long requestPanelQuantity)
	{
		try
		{
			ShipRequestPS shipRequestPSData = new ShipRequestPS();
			shipRequestPSData.setInvoiceDetailNo(invoiceDetailNo);
			shipRequestPSData.setInvoiceNo(invoiceNo);
			shipRequestPSData.setBoxQuantity(boxQuantity);
			shipRequestPSData.setPalletQuantity(palletQuantity);
			shipRequestPSData.setProductSpecName(productSpecName);
			shipRequestPSData.setProductQuantity(productQuantity);
			shipRequestPSData.setCreateUser(createUser);
			shipRequestPSData.setRequestPanelQuantity(requestPanelQuantity);
			
			FGMSServiceProxy.getShipRequestPSService().create(eventInfo, shipRequestPSData);
		}
		catch(Exception ex)
		{
			logger.info(ex.getMessage());
		}
	}
	
	public void updateShipRequestPS(EventInfo eventInfo, String invoiceDetailNo, String invoiceNo, long boxQuantity, long palletQuantity,
			String productSpecName, long productQuantity, String createUser, long requestPanelQuantity)
	{
		try
		{
			ShipRequestPS shipRequestPSData = new ShipRequestPS();
			shipRequestPSData.setInvoiceDetailNo(invoiceDetailNo);
			shipRequestPSData.setInvoiceNo(invoiceNo);
			shipRequestPSData.setBoxQuantity(boxQuantity);
			shipRequestPSData.setPalletQuantity(palletQuantity);
			shipRequestPSData.setProductSpecName(productSpecName);
			shipRequestPSData.setProductQuantity(productQuantity);
			shipRequestPSData.setCreateUser(createUser);
			shipRequestPSData.setRequestPanelQuantity(requestPanelQuantity);
			
			FGMSServiceProxy.getShipRequestPSService().modify(eventInfo, shipRequestPSData);
		}
		catch(Exception ex)
		{
			logger.info(ex.getMessage());
		}
	}
}
