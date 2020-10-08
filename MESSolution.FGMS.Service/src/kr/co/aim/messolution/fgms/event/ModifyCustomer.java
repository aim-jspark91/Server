package kr.co.aim.messolution.fgms.event;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.fgms.FGMSServiceProxy;
import kr.co.aim.messolution.fgms.management.data.Customer;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class ModifyCustomer extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {

		String customerNo    = SMessageUtil.getBodyItemValue(doc, "CUSTOMERNO", true);
		String customerName  = SMessageUtil.getBodyItemValue(doc, "CUSTOMERNAME", true);
		String destination   = SMessageUtil.getBodyItemValue(doc, "DESTINATION", true);
		String telePhone     = SMessageUtil.getBodyItemValue(doc, "TELEPHONE", true);
		String fax           = SMessageUtil.getBodyItemValue(doc, "FAX", false);
		String mail           = SMessageUtil.getBodyItemValue(doc, "MAIL", false);
		String address1      = SMessageUtil.getBodyItemValue(doc, "ADDRESS1", true);
		String address2      = SMessageUtil.getBodyItemValue(doc, "ADDRESS2", false);
		String address3      = SMessageUtil.getBodyItemValue(doc, "ADDRESS3", false);
		String lastEventUser = getEventUser();
		
		//check existence
		List <Customer> sqlResult = new ArrayList <Customer>();
		try
		{
			sqlResult = FGMSServiceProxy.getCustomerService().select("CUSTOMERNO = ?", new Object[] { customerNo });
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("CUSTOMER-9000", customerNo);
		}
		catch(greenFrameDBErrorSignal de)
		{
			if(de.getErrorCode().equals("NotFoundSignal"))
			{
				throw new CustomException("CUSTOMER-9000", customerNo);
			}else 
			{
				throw new CustomException("SYS-8001",de.getSql()); 
			}
		}
		
		//check tel/fax format
//		if (telePhone.indexOf("-") < 1 || 
//			(telePhone.lastIndexOf("-") == (telePhone.length() + 1)))
//				throw new CustomException("CUSTOMER-0001", telePhone);
//			
//		if (fax.indexOf("-") < 1 ||
//			(fax.lastIndexOf("-") == (fax.length() + 1)))
//				throw new CustomException("CUSTOMER-0001", fax);
		
		//prepare
		Timestamp lastEventTime = new Timestamp(System.currentTimeMillis());

		Customer customer = new Customer(customerNo);

		customer.setCustomerNo(customerNo);
		customer.setCustomerName(customerName);
		customer.setDestination(destination);
		customer.setTelePhone(telePhone);
		customer.setFax(fax);
		customer.setMail(mail);
		customer.setAddress1(address1);
		customer.setAddress2(address2);
		customer.setAddress3(address3);
		customer.setLastEventTime(lastEventTime);
		customer.setLastEventUser(lastEventUser);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyCustomer", getEventUser(), getEventComment(), "", "");
		
		//execute
		FGMSServiceProxy.getCustomerService().modify(eventInfo, customer);
	
		return doc;
	}
}
