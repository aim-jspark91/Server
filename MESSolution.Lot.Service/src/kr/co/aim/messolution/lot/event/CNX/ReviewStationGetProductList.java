package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;

import org.jdom.Document;
import org.jdom.Element;

public class ReviewStationGetProductList extends AsyncHandler {
	@Override
	public void doWorks(Document doc) throws CustomException {
		
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "GetProductListReply");
		
		String factoryName 			   = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String lotName    			   = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);

		try
		{
			this.generateBodyTemplate(doc, factoryName, lotName);	
		}
		catch(Exception ex)
		{
			this.generateBodyTemplateNoData(doc, factoryName, lotName);	
		}
		
		GenericServiceProxy.getESBServive().sendReplyBySender(getOriginalSourceSubjectName(), doc, "OICSender");
	}
	
	private Element generateBodyTemplate(Document doc, String factoryName, String lotName) throws CustomException
	{
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		
		Element bodyElement = null;
		bodyElement = new Element("Body");
		
		List<Lot> lotList = LotServiceProxy.getLotService().select("WHERE LOTSTATE = ? AND FACTORYNAME = ? AND LOTNAME = ?", new Object[]{GenericServiceProxy.getConstantMap().Lot_Released, factoryName, lotName});
		Lot lotData = lotList.get(0);
		
		Element productSpecNameElement = new Element("PRODUCTSPECNAME");
		productSpecNameElement.setText(lotData.getProductSpecName());
		bodyElement.addContent(productSpecNameElement);
		
		Element productSpecVersionElement = new Element("PRODUCTSPECVERSION");
		productSpecVersionElement.setText(lotData.getProductSpecVersion());
		bodyElement.addContent(productSpecVersionElement);
		
		Element processOperationNameElement = new Element("PROCESSOPERATIONNAME");
		processOperationNameElement.setText(lotData.getProcessOperationName());
		bodyElement.addContent(processOperationNameElement);
		
		Element processOperationVersionElement = new Element("PROCESSOPERATIONVERSION");
		processOperationVersionElement.setText(lotData.getProcessOperationVersion());
		bodyElement.addContent(processOperationVersionElement);
		
		Element dataList = new Element("DATALIST");
		List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
		
		for(Product productData : productList)
		{
			Element data = new Element("DATA");
			
			Element productNameElement = new Element("PRODUCTNAME");
			productNameElement.setText(productData.getKey().getProductName());
			data.addContent(productNameElement);
			
			Element positionElement = new Element("POSITION");
			positionElement.setText(String.valueOf(productData.getPosition()));
			data.addContent(positionElement);
			
			Element processingResultElement = new Element("PROCESSINGRESULT");
			processingResultElement.setText(productData.getUdfs().get("PROCESSINGINFO"));
			data.addContent(processingResultElement);
			
			dataList.addContent(data);
		}
		
		bodyElement.addContent(dataList);
		doc.getRootElement().addContent(2, bodyElement);
		
		return bodyElement;
	}
	
	
	private Element generateBodyTemplateNoData(Document doc, String factoryName, String lotName) throws CustomException
	{
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		
		Element bodyElement = null;
		bodyElement = new Element("Body");
		
		Element productSpecNameElement = new Element("PRODUCTSPECNAME");
		productSpecNameElement.setText("");
		bodyElement.addContent(productSpecNameElement);
		
		Element productSpecVersionElement = new Element("PRODUCTSPECVERSION");
		productSpecVersionElement.setText("");
		bodyElement.addContent(productSpecVersionElement);
		
		Element processOperationNameElement = new Element("PROCESSOPERATIONNAME");
		processOperationNameElement.setText("");
		bodyElement.addContent(processOperationNameElement);
		
		Element processOperationVersionElement = new Element("PROCESSOPERATIONVERSION");
		processOperationVersionElement.setText("");
		bodyElement.addContent(processOperationVersionElement);
		
		Element dataList = new Element("DATALIST");
		

		Element data = new Element("DATA");
		
		Element productNameElement = new Element("PRODUCTNAME");
		productNameElement.setText("");
		data.addContent(productNameElement);
		
		Element positionElement = new Element("POSITION");
		positionElement.setText("");
		data.addContent(positionElement);
		
		Element processingResultElement = new Element("PROCESSINGRESULT");
		processingResultElement.setText("");
		data.addContent(processingResultElement);
		
		dataList.addContent(data);
		
		
		bodyElement.addContent(dataList);
		doc.getRootElement().addContent(2, bodyElement);
		
		return bodyElement;
	}
	
	
	

}
