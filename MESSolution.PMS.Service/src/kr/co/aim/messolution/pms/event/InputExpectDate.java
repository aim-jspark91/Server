package kr.co.aim.messolution.pms.event;

import java.sql.Timestamp;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.Purchase;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class InputExpectDate extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String PoCode 		 = SMessageUtil.getBodyItemValue(doc, "POCODE", true);
		String PartID        = SMessageUtil.getBodyItemValue(doc, "PARTID", true);
		String InExpectDate  = SMessageUtil.getBodyItemValue(doc, "INEXPECTDATE", true);
	
		EventInfo eventInfo  = EventInfoUtil.makeEventInfo("InputExpectData", getEventUser(), getEventComment(), null, null);
		
		Purchase purchase = null;
		//get
		purchase = PMSServiceProxy.getPurchaseService().selectByKey(true, new Object[] {PoCode});
		String PurchaseType     = purchase.getPurchaseType();
		String AvailablePeriod  = purchase.getAvailablePeriod();
		Timestamp InRequestDate = purchase.getInRequestDate();
		Number PurchaseQuantity = purchase.getPurchaseQuantity();
		String PurchaseUnit     = purchase.getPurchaseUnit();
		String PurchaseReason   = purchase.getPurchaseReason();
		String PurcahseStatus   = "Purchasing";
		String CreateUser       = purchase.getCreateUser();
		Timestamp CreateTime    = purchase.getCreateTime();
		String CancelComment    = purchase.getCancelComment();
		String CancelFlag       = purchase.getCancelFlag();
		
		//set
		purchase = new Purchase(PoCode);
		purchase.setPartID(PartID);
		purchase.setPurchaseType(PurchaseType);
		purchase.setAvailablePeriod(AvailablePeriod);
		purchase.setInRequestDate(InRequestDate);
		purchase.setPurchaseQuantity(PurchaseQuantity);
		purchase.setPurchaseUnit(PurchaseUnit);
		purchase.setPurchaseReason(PurchaseReason);
		purchase.setPurchaseStatus(PurcahseStatus);
		purchase.setInExpectdate(TimeStampUtil.getTimestamp(InExpectDate));
		purchase.setCreateUser(CreateUser);
		purchase.setCreateTime(CreateTime);
		purchase.setCancelComment(CancelComment);
		purchase.setCancelFlag(CancelFlag);
		
		try
		{
			purchase = PMSServiceProxy.getPurchaseService().modify(eventInfo, purchase);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0059", PoCode);
		}
		
		return doc;
	}
}
