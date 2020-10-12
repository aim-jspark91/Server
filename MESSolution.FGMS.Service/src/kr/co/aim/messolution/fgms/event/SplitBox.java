package kr.co.aim.messolution.fgms.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.fgms.FGMSServiceProxy;
import kr.co.aim.messolution.fgms.management.data.Product;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.name.NameServiceProxy;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupKey;
import kr.co.aim.greentrack.processgroup.management.info.CreateInfo;
import kr.co.aim.greentrack.processgroup.management.info.SetEventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class SplitBox extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String boxName = SMessageUtil.getBodyItemValue(doc, "BOXNAME", true);
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		int sProductSize = 0;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Split", getEventUser(), getEventComment(), "", "");
		
		//Original Box
		ProcessGroupKey oBoxKey = new ProcessGroupKey(boxName);
		ProcessGroup oBoxData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(oBoxKey);
		
		//Check Invoice
		this.CheckInvoice(oBoxData);
		
		//Create Box Name
		List<String> argSeq = new ArrayList<String>();
		argSeq.add(oBoxData.getUdfs().get("PRODUCTSPECNAME"));
				
		List<String> palletList = NameServiceProxy.getNameGeneratorRuleDefService().generateName("BoxNaming", argSeq, Integer.parseInt("1"));
		String newBoxName = palletList.get(0) + "_FGI";
		
		sProductSize = (int)oBoxData.getMaterialQuantity()-productList.size();
		List<String> materialList = new ArrayList<String>();
		
		//update Original Box
		oBoxData.setMaterialQuantity(sProductSize);
				
		ProcessGroupServiceProxy.getProcessGroupService().update(oBoxData);
		SetEventInfo setEventInfo = new SetEventInfo();
		Map<String, String> udfs = oBoxData.getUdfs();
		setEventInfo.setUdfs(udfs);
						
		MESProcessGroupServiceProxy.getProcessGroupServiceImpl().setEvent(oBoxData, setEventInfo, eventInfo);
		
		//Make New Pallet
		ProcessGroup newBox = this.CreateNewBox(oBoxData, newBoxName, materialList);
		
		//Box Split, Assign
		for (Element eleProduct : productList) 
		{
			String productName = SMessageUtil.getChildText(eleProduct, "PRODUCTNAME", true);
						
			materialList.add(productName);
			
			Product productData = FGMSServiceProxy.getProductService().selectByKey(false, new Object[] {productName});
					
			this.SplitProduct(productData);
			this.AssignProduct(productData, newBoxName);
		}
		
		this.AssignBox(newBox, materialList, productList.size());
		
		//call by value so that reply would be modified
		XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "NEWBOXNAME", newBoxName);
		
		return doc;
	}
	
	private ProcessGroup CreateNewBox(ProcessGroup BoxData, String newBoxName,
			List<String> materialList)
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), "", "");
		
		HashMap<String, String> udfs = new HashMap<String, String>();
		
		CreateInfo createInfo = new CreateInfo();
		
		createInfo.setProcessGroupName(newBoxName);
		createInfo.setProcessGroupType(GenericServiceProxy.getConstantMap().TYPE_BOX);
		createInfo.setMaterialType(GenericServiceProxy.getConstantMap().TYPE_GLASS);
		createInfo.setDetailMaterialType(GenericServiceProxy.getConstantMap().TYPE_PANEL);
		createInfo.setMaterialQuantity(0);
		createInfo.setMaterialNames(materialList);
		createInfo.setMaterialUdfs(udfs);
		udfs.put("PRODUCTSPECNAME", BoxData.getUdfs().get("PRODUCTSPECNAME"));
		udfs.put("PRODUCTREQUESTNAME", BoxData.getUdfs().get("PRODUCTREQUESTNAME"));
		udfs.put("STOCKSTATE", GenericServiceProxy.getConstantMap().FGMS_STOCKSTATE_STOCKED);
		udfs.put("DOMESTICEXPORT", BoxData.getUdfs().get("DOMESTICEXPORT"));
		udfs.put("HOLDSTATE", "N");
		createInfo.setUdfs(udfs);		
		
		ProcessGroup newBox = ProcessGroupServiceProxy.getProcessGroupService().create(eventInfo, createInfo);
		
		return newBox;
	}
	
	private void SplitProduct(Product productData) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Split", getEventUser(), getEventComment(), "", "");
		
		productData.setProcessGroupName("");
		FGMSServiceProxy.getProductService().modify(eventInfo, productData);
	}
	
	private void AssignProduct(Product productData, String newBoxName) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Assign", getEventUser(), getEventComment(), "", "");
		
		productData.setProcessGroupName(newBoxName);
		FGMSServiceProxy.getProductService().modify(eventInfo, productData);
	}
	
	private void AssignBox(ProcessGroup BoxData, List<String> materialList, 
			int materialQuantity)
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Assign", getEventUser(), getEventComment(), "", "");
		
		BoxData.setMaterialQuantity(materialQuantity);
		
		ProcessGroupServiceProxy.getProcessGroupService().update(BoxData);
		SetEventInfo setEventInfo = new SetEventInfo();
		Map<String, String> udfs = BoxData.getUdfs();
		setEventInfo.setUdfs(udfs);
						
		MESProcessGroupServiceProxy.getProcessGroupServiceImpl().setEvent(BoxData, setEventInfo, eventInfo);
	}
	
	private void CheckInvoice(ProcessGroup pData) throws CustomException
	{
		if(StringUtils.isNotEmpty(pData.getUdfs().get("INVOICENO")))
		{
			throw new CustomException("PROCESSGROUP-0005", pData.getKey().getProcessGroupName());
		}
	}
}
