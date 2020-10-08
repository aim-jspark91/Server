package kr.co.aim.messolution.lot.event;
 
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;

import org.jdom.Document;

public class PanelScrap extends AsyncHandler {
 
	@Override
	public void doWorks(Document doc)
		throws CustomException  
	{
		/*
		String panel      = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "SCRAPCODE", true);
		 
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ScrapPanel", getEventUser(), getEventComment(), "", "");
		
		//validation.
		CommonValidation.checkExistPanelName(panel); 

		Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(panel);
		
		String currentProductState = productData.getProductState().toString();
		String boxName = productData.getProcessGroupName().toString();
		String lotName = productData.getLotName().toString();
		String isPanel = productData.getProductType().toString();
		double productQuantity = 1; 
		 
		List<ProductU> productUList = 
				MESProductServiceProxy.getProductServiceUtil().setProductUSequence(panel);

		MakeScrappedInfo makeScrappedInfo 
								= MESProductServiceProxy.getProductInfoUtil().makeScrappedInfo(productData, productQuantity, productUList);
		  
		//Set EventInfo
		EventInfo tmpEventInfo = new EventInfo();
		tmpEventInfo.setEventName(eventInfo.getEventName());
		tmpEventInfo.setEventComment(eventInfo.getEventComment());
		tmpEventInfo.setEventUser(eventInfo.getEventUser());
		tmpEventInfo.setReasonCode(reasonCode);
		
		if("Panel".equals(isPanel)){
			if(!StringUtil.isEmpty(boxName) || !StringUtil.isEmpty(lotName)){
				throw new CustomException("PANEL-9006", panel, currentProductState);
			}
			//API Call 
			MESProductServiceProxy.getProductServiceImpl().makeScrapped(productData, makeScrappedInfo, tmpEventInfo);  
		
	
*/
	}
}
