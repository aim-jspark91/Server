package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.lot.service.LotServiceUtil;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.product.service.ProductServiceUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.LotService;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.impl.LotServiceImpl;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;

import org.jdom.Document;
import org.jdom.Element;

public class BankInOut extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("BankIn", getEventUser(), getEventComment(), "", "");
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);		
		
		//get release qty
		for (Element eLotData : lotList) 
		{
			String lotName = eLotData.getChild("LOTNAME").getText();
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			
			CommonValidation.checkLotState(lotData);
			CommonValidation.checkLotProcessState(lotData);
			CommonValidation.checkLotHoldState(lotData);
			CommonValidation.checkProductGradeN(lotData);
			
			eventInfo.setEventName( "BankIn" );
			MESLotServiceProxy.getLotServiceUtil().TrackInLotForBank(lotData, eventInfo);
		
			eventInfo.setEventName( "BankOut" );
			MESLotServiceProxy.getLotServiceUtil().TrackOutLotForBank(lotData, eventInfo);
			
		}
		return doc;
	}
	
}
