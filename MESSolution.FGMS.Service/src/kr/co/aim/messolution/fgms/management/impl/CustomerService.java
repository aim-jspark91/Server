package kr.co.aim.messolution.fgms.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.fgms.management.data.Customer;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CustomerService extends CTORMService<Customer> {
	
	public static Log logger = LogFactory.getLog(CustomerService.class);
	
	private final String historyEntity = "";
	
	public List<Customer> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<Customer> result = super.select(condition, bindSet, Customer.class);
		
		return result;
	}
	
	public Customer selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(Customer.class, isLock, keySet);
	}
	
	public Customer create(EventInfo eventInfo, Customer dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, Customer dataInfo)
		throws CustomException
	{
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public Customer modify(EventInfo eventInfo, Customer dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
