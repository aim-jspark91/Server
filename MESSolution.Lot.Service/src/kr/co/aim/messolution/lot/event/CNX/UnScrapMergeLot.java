package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ChangeGradeInfo;

import org.jdom.Document;
import org.jdom.Element;

public class UnScrapMergeLot extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String durableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		String lotJudge = SMessageUtil.getBodyItemValue(doc, "LOTJUDGE", false);
		
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Merge", getEventUser(), getEventComment(), null, null);
		
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		
		//Check Vaildation CST
		CommonValidation.CheckDurableHoldState(durableData);
		
		Lot oldLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		CommonValidation.checkLotHoldState(oldLotData);
				
		EventInfo eventInfo1 = EventInfoUtil.makeEventInfo("ChangeGrade", getEventUser(), getEventComment(), null, null);
		
		for (Element eleProduct : productList) 
		{
			String productName = SMessageUtil.getChildText(eleProduct, "PRODUCTNAME", true);			
			Product prdData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);
			
			if(!StringUtil.equals(prdData.getCarrierName(), durableName) || prdData.getCarrierName() == null)
			{
				//judge
				ChangeGradeInfo changeGradeInfo = MESProductServiceProxy.getProductInfoUtil().changeGradeInfo(prdData, 
						prdData.getPosition(), "G", prdData.getProductProcessState(),
						prdData.getSubProductGrades1(), prdData.getSubProductGrades2(), prdData.getSubProductQuantity1(), prdData.getSubProductQuantity2());
				
				prdData = MESProductServiceProxy.getProductServiceImpl().changeGrade(prdData, changeGradeInfo, eventInfo1);
			}
		}
		
		Lot lotData = MESLotServiceProxy.getLotServiceUtil().composeLot(eventInfo, durableName, productList);
		
		lotData.setLotGrade(lotJudge);
		
		LotServiceProxy.getLotService().update(lotData);
		
		try
		{
			lotData = MESLotServiceProxy.getLotServiceUtil().arrangeCarrier(eventInfo, lotData, durableData, productList);
		}
		catch (Exception ex)
		{
			eventLog.error("CST arrange failed");
		}
		
		setNextInfo(doc, lotData);
		
		return doc;
	}
	
	
	private void setNextInfo(Document doc, Lot lotData)
	{
		try
		{
			StringBuilder strComment = new StringBuilder();
			
			strComment.append(String.format("Lot[%s] is merged in CST[%s]", lotData.getKey().getLotName(), lotData.getCarrierName()));
			
			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, strComment.toString());
		}
		catch (Exception ex)
		{
			eventLog.warn("Note after TK OUT is nothing");
		}
	}
}