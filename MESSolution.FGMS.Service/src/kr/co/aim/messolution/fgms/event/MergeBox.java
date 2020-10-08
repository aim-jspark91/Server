package kr.co.aim.messolution.fgms.event;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.fgms.FGMSServiceProxy;
import kr.co.aim.messolution.fgms.management.data.Product;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupKey;
import kr.co.aim.greentrack.processgroup.management.info.SetEventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class MergeBox extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {

		String boxName = SMessageUtil.getBodyItemValue(doc, "BOXNAME", true);
		String sourceBoxName = SMessageUtil.getBodyItemValue(doc, "SOURCEBOXNAME", true);
		String sBoxSize = SMessageUtil.getBodyItemValue(doc, "SOURCEQUANTITY", true);
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Split", getEventUser(), getEventComment(), "", "");
		
		//Original Box
		ProcessGroupKey oBoxKey = new ProcessGroupKey(sourceBoxName);
		ProcessGroup oBoxData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(oBoxKey);
		
		//Check Invoice
		this.CheckInvoice(oBoxData);
				
		//update Original Box
		oBoxData.setMaterialQuantity(oBoxData.getMaterialQuantity()-Integer.parseInt(sBoxSize));
								
		ProcessGroupServiceProxy.getProcessGroupService().update(oBoxData);
		SetEventInfo setEventInfo = new SetEventInfo();
		Map<String, String> udfs = oBoxData.getUdfs();
		setEventInfo.setUdfs(udfs);
				
		MESProcessGroupServiceProxy.getProcessGroupServiceImpl().setEvent(oBoxData, setEventInfo, eventInfo);
		
		//Box Split, Assign
		for (Element eleProduct : productList) 
		{
			String productName = SMessageUtil.getChildText(eleProduct, "PRODUCTNAME", true);
											
			Product productData = FGMSServiceProxy.getProductService().selectByKey(false, new Object[] {productName});
						
			this.SplitProduct(productData);
			this.AssignProduct(productData, boxName);
		}
		
		//Target Pallet
		ProcessGroupKey tBoxKey = new ProcessGroupKey(boxName);
		ProcessGroup tBoxData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(tBoxKey);
						
		//update Target Pallet
		tBoxData.setMaterialQuantity(productList.size());
										
		ProcessGroupServiceProxy.getProcessGroupService().update(tBoxData);
		setEventInfo = new SetEventInfo();
		Map<String, String> tudfs = tBoxData.getUdfs();
		setEventInfo.setUdfs(tudfs);
				
		eventInfo = EventInfoUtil.makeEventInfo("Merge", getEventUser(), getEventComment(), "", "");
					
		MESProcessGroupServiceProxy.getProcessGroupServiceImpl().setEvent(tBoxData, setEventInfo, eventInfo);

		return doc;
	}
	
	private void SplitProduct(Product productData) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Split", getEventUser(), getEventComment(), "", "");
		
		productData.setProcessGroupName("");
		FGMSServiceProxy.getProductService().modify(eventInfo, productData);
	}
	
	private void AssignProduct(Product productData, String boxName) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Merge", getEventUser(), getEventComment(), "", "");
		
		productData.setProcessGroupName(boxName);
		FGMSServiceProxy.getProductService().modify(eventInfo, productData);
	}
	
	private void CheckInvoice(ProcessGroup pData) throws CustomException
	{
		if(StringUtils.isNotEmpty(pData.getUdfs().get("INVOICENO")))
		{
			throw new CustomException("PROCESSGROUP-0005", pData.getKey().getProcessGroupName());
		}
	}
}
