package kr.co.aim.messolution.fgms.event;

import java.sql.Timestamp;

import kr.co.aim.messolution.fgms.FGMSServiceProxy;
import kr.co.aim.messolution.fgms.management.data.Customer;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class RemoveCustomer extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {

		String customerNo    = SMessageUtil.getBodyItemValue(doc, "CUSTOMERNO", true);
		String lastEventUser = getEventUser();
		
		Timestamp lastEventTime = new Timestamp(System.currentTimeMillis());

		Customer customer = null;
		
		customer = FGMSServiceProxy.getCustomerService().selectByKey(true, new Object[] {customerNo});
		customer = new Customer(customerNo);

		String customerName  = customer.getCustomerName();
		String destination   = customer.getDestination();
		String telePhone     = customer.getTelePhone();
		String fax           = customer.getFax();
		String address1      = customer.getAddress1();
		String address2      = customer.getAddress2();
		String address3      = customer.getAddress3();
		
		customer.setCustomerNo(customerNo);
		customer.setCustomerName(customerName);
		customer.setDestination(destination);
		customer.setTelePhone(telePhone);
		customer.setFax(fax);
		customer.setAddress1(address1);
		customer.setAddress2(address2);
		customer.setAddress3(address3);
		customer.setLastEventTime(lastEventTime);
		customer.setLastEventUser(lastEventUser);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("RemoveCustomer", getEventUser(), getEventComment(), "", "");
		
		FGMSServiceProxy.getCustomerService().remove(eventInfo, customer);
	
		return doc;
	}
}
