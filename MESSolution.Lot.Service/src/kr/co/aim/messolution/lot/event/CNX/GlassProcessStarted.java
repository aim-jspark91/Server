package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedInInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductC;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;

import org.jdom.Document;
import org.jdom.Element;

public class GlassProcessStarted extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		//additional info initialized
		setEventComment("");
		
		//String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		
		//optional
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		
		//Q-time
		/*ExtendedObjectProxy.getQTimeService().monitorQTime(eventInfo, lotName);
		ExtendedObjectProxy.getQTimeService().validateQTime(eventInfo, lotName);
		ExtendedObjectProxy.getQTimeService().exitQTime(eventInfo, lotName, lotData.getProcessOperationName());*/
		
		//transaction per Lot
		for (Element elementLot : SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true))
		{
			String lotName = SMessageUtil.getChildText(elementLot, "LOTNAME", true);
			
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			
			Map<String, String> lotUdfs = lotData.getUdfs();
			
			MESLotServiceProxy.getLotServiceUtil().validationLotGrade(lotData);

			//TrackIn
			List<ProductC> productCSequence = MESLotServiceProxy.getLotInfoUtil().setProductCSequence(lotName);
			
			lotUdfs.put("PORTNAME", portData.getKey().getPortName());
			lotUdfs.put("PORTTYPE", portData.getUdfs().get("PORTTYPE"));
			lotUdfs.put("PORTUSETYPE", portData.getUdfs().get("PORTUSETYPE"));
			
			String machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(),
										lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, lotData.getUdfs().get("ECCODE"));
			
			MakeLoggedInInfo makeLoggedInInfo = MESLotServiceProxy.getLotInfoUtil().makeLoggedInInfo(machineName, machineRecipeName, productCSequence, lotUdfs);
			
			//inline tracking special
			//makeLoggedInInfo.getUdfs().put("UNITNAME", unitName);
			
			eventInfo.setEventName("TrackIn");
			Lot trackInLot = MESLotServiceProxy.getLotServiceImpl().trackInLot(eventInfo, lotData, makeLoggedInInfo);
			
			splitGlass(eventInfo, trackInLot);
		}

		sendToFMC(doc);
		SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, getEventComment().toString());
	}
	
	private void splitGlass(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		List<Product> productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
		
		if (productList.size() > 1)
		{
			eventLog.info("Split Glass as Lot for inline");
			
			for (Product productData : productList)
			{
				eventLog.info(String.format("Product[%s] would be separated", productData.getKey().getProductName()));
				
				eventInfo.setEventName("Create");
				Lot garbageLot = MESLotServiceProxy.getLotServiceUtil().createWithParentLot(eventInfo, productData.getKey().getProductName(), lotData, lotData.getCarrierName(), false, new HashMap<String, String>(), lotData.getUdfs());
				
				List<ProductP> productPSequence = new ArrayList<ProductP>();
				ProductP productP = new ProductP();
				productP.setProductName(productData.getKey().getProductName());
				productP.setPosition(productData.getPosition());
				productP.setUdfs(productData.getUdfs());
				productPSequence.add(productP);
				
				TransferProductsToLotInfo  transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(
																garbageLot.getKey().getLotName(), 1, productPSequence, lotData.getUdfs(), new HashMap<String, String>());
				
				//do split
				eventInfo.setEventName("Split");
				lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);
				
				garbageLot = MESLotServiceProxy.getLotInfoUtil().getLotData(garbageLot.getKey().getLotName());
				
				setNextInfo(garbageLot);
				
				eventLog.info(String.format("Lot[%s] is separated", productData.getKey().getProductName()));
			}
		}
	}
	
	private void setNextInfo(Lot lotData)
	{
		try
		{
			StringBuilder strComment = new StringBuilder(getEventComment());
			strComment.append("LotName").append("[").append(lotData.getKey().getLotName()).append("]")
						.append("LotGrade").append("[").append(lotData.getLotGrade()).append("]")
						.append("NextFlow").append("[").append(lotData.getProcessFlowName()).append("]")
						.append("NextOperation").append("[").append(lotData.getProcessOperationName()).append("]").append("\n");
			
			setEventComment(strComment.toString());
		}
		catch (Exception ex)
		{
			eventLog.warn("Note after TK In is nothing");
		}
	}
	
	private void sendToFMC(Document doc)
	{
		//150117 by swcho : success then report to FMC
		try
		{
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
		}
		catch(Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}	
	}
}
