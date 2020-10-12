package kr.co.aim.messolution.lot.event.CNX;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greenframe.util.support.InvokeUtils;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpecKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;

import org.jdom.Document;
import org.jdom.Element;

public class TrackInOutLot extends AsyncHandler 
{
	@Override
	public void doWorks(Document doc) throws CustomException 
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String loadPort = SMessageUtil.getBodyItemValue(doc, "LOADPORT", true);
		String unLoadPort = SMessageUtil.getBodyItemValue(doc, "UNLOADPORT", true);
		String crateName = SMessageUtil.getBodyItemValue(doc, "CRATENAME", true);
		
		Object result = null;
		
		Lot lotData =  MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		try
		{
			Port loader = MESPortServiceProxy.getPortServiceUtil().searchLoaderPort(machineName);
			
//			String machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(),
//					lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, false, lotData.getUdfs().get("ECCODE"));

			ConsumableKey consumableKey = new ConsumableKey(crateName);
			Consumable con = ConsumableServiceProxy.getConsumableService().selectByKey(consumableKey);
			String consumableSpec = con.getConsumableSpecName();

			ConsumableSpecKey consumableSpecKey = new ConsumableSpecKey();
			consumableSpecKey.setConsumableSpecName(consumableSpec);
			consumableSpecKey.setConsumableSpecVersion("00001");
			consumableSpecKey.setFactoryName(con.getFactoryName());
			ConsumableSpec conSpec = ConsumableServiceProxy.getConsumableSpecService().selectByKey(consumableSpecKey);
			
			String machineRecipeName = conSpec.getUdfs().get("MACHINERECIPENAME");
			
			doc = this.writeTrackInRequest(doc, lotData.getKey().getLotName(), loader.getKey().getMachineName(), loadPort, machineRecipeName);
			
			result = InvokeUtils.invokeMethod(InvokeUtils.newInstance(TrackInLot.class.getName(), null, null), "execute", new Object[] {doc});
			
			Port unloader = MESPortServiceProxy.getPortServiceUtil().searchUnloaderPort(loader);
			
			doc = this.writeTrackOutRequest(doc, lotData.getKey().getLotName(), unloader.getKey().getMachineName(), unLoadPort, carrierName);
			
			result = InvokeUtils.invokeMethod(InvokeUtils.newInstance(TrackOutLotPU.class.getName(), null, null), "execute", new Object[] {doc});
		}
		catch (NoSuchMethodException e)
		{
			eventLog.error(e);
		}
		catch (IllegalAccessException e)
		{
			eventLog.error(e);
		}
		catch (InvocationTargetException e)
		{
			eventLog.error(e);
		}
		
		eventLog.debug("end");
	}
	
	private Document writeTrackInRequest(Document doc, String lotName, String machineName, String portName, String recipeName)
		throws CustomException
	{
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "TrackInLot");
		//SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", GenericServiceProxy.getESBServive().getSendSubject("CNXsvr"));
		
		//Element eleBody = SMessageUtil.getBodyElement(doc);

		boolean result = doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		
		Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);
		
		Element element1 = new Element("LOTNAME");
		element1.setText(lotName);
		eleBodyTemp.addContent(element1);
		
		Element element2 = new Element("MACHINENAME");
		element2.setText(machineName);
		eleBodyTemp.addContent(element2);
		
		Element element3 = new Element("PORTNAME");
		element3.setText(portName);
		eleBodyTemp.addContent(element3);
		
		Element element4 = new Element("RECIPENAME");
		element4.setText(recipeName);
		eleBodyTemp.addContent(element4);
		
		Element element5 = new Element("AUTOFLAG");
		element5.setText("Y");
		eleBodyTemp.addContent(element5);
		
		//overwrite
		doc.getRootElement().addContent(eleBodyTemp);
		
		return doc;
	}

	private Document writeTrackOutRequest(Document doc, String lotName, String machineName, String portName, String carrierName)
			throws CustomException
	{
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "TrackOutLot");
		//SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", GenericServiceProxy.getESBServive().getSendSubject("CNXsvr"));
		
		boolean result = doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		
		Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);
		
		Element element1 = new Element("LOTNAME");
		element1.setText(lotName);
		eleBodyTemp.addContent(element1);
		
		Element element2 = new Element("MACHINENAME");
		element2.setText(machineName);
		eleBodyTemp.addContent(element2);
		
		Element element3 = new Element("PORTNAME");
		element3.setText(portName);
		eleBodyTemp.addContent(element3);
		
		Element element4 = new Element("CARRIERNAME");
		element4.setText(carrierName);
		eleBodyTemp.addContent(element4);
		
		Element elementPL = new Element("PRODUCTLIST");
		try
		{
			List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
			
			for (Product productData : productList)
			{
				Element elementP = new Element("PRODUCT");
				{
					Element elementS1 = new Element("PRODUCTNAME");
					elementS1.setText(productData.getKey().getProductName());
					elementP.addContent(elementS1);
					
					Element elementS2 = new Element("POSITION");
					elementS2.setText(String.valueOf(productData.getPosition()));
					elementP.addContent(elementS2);
					
					Element elementS3 = new Element("PRODUCTJUDGE");
					elementS3.setText(productData.getProductGrade());
					elementP.addContent(elementS3);
				}
				elementPL.addContent(elementP);
			}
		}
		catch (NotFoundSignal e) 
	    {
	    	throw new CustomException("PRODUCT-9001", "");	
		}
	    catch (FrameworkErrorSignal fe)
	    {
	    	throw new CustomException("PRODUCT-9999", fe.getMessage());	
	    }
		eleBodyTemp.addContent(elementPL);
		
		//overwrite
		doc.getRootElement().addContent(eleBodyTemp);
		
		return doc;
	}
	
}
