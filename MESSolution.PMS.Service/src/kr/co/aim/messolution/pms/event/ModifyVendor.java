package kr.co.aim.messolution.pms.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.Vendor;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class ModifyVendor extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String vendorID   = SMessageUtil.getBodyItemValue(doc, "VENDORID", true);
		String vendorName = SMessageUtil.getBodyItemValue(doc, "VENDORNAME", false);
		String telephone  = SMessageUtil.getBodyItemValue(doc, "TELEPHONE", false);
		String mobile     = SMessageUtil.getBodyItemValue(doc, "MOBILE", false);	
			
		EventInfo eventInfo  = EventInfoUtil.makeEventInfo("ModifyVendor", getEventUser(), getEventComment(), null, null);
		
		Vendor vendorInfo = new Vendor(vendorID);
		
		vendorInfo.setVendorName(vendorName);
		vendorInfo.setTelephone(telephone);
		vendorInfo.setMobile(mobile);

		try
		{
			vendorInfo = PMSServiceProxy.getVendorService().modify(eventInfo, vendorInfo);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0075", vendorID);
		}	
		
		return doc;
	}
}
