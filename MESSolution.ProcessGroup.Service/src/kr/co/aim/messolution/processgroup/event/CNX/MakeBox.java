package kr.co.aim.messolution.processgroup.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.name.NameServiceProxy;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupKey;
import kr.co.aim.greentrack.processgroup.management.info.AssignMaterialsInfo;
import kr.co.aim.greentrack.processgroup.management.info.CreateInfo;
import kr.co.aim.greentrack.processgroup.management.info.ext.MaterialU;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class MakeBox extends SyncHandler {
	public Object doWorks(Document doc) throws CustomException
	{		
		Element lotElementList = SMessageUtil.getBodySequenceItem(doc, "LOTLIST", true);
		//String productQty = SMessageUtil.getBodyItemValue(doc, "PRODUCTQUANTITY", false);
		String workOrder = SMessageUtil.getBodyItemValue(doc, "WORKORDER", false);
		//String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		
		List<MaterialU> materialUList = new ArrayList<MaterialU>();
		List<String> specList = new ArrayList<String>();
		
		//Validation
		for (Iterator<?> iteratorLotList = lotElementList.getChildren().iterator(); iteratorLotList.hasNext();)
		{
			Element lotE = (Element) iteratorLotList.next();
			String sLotName = SMessageUtil.getChildText(lotE, "LOTNAME", true);
			
			//GetLotData
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(sLotName);
			
			//1. CommonValidation
			CommonValidation.checkLotState(lotData);
			CommonValidation.checkLotHoldState(lotData);
			CommonValidation.checkLotShippedState(lotData);

			//2. Check Same ProductSpec
			if(specList.size() <= 0) specList.add(lotData.getProductSpecName());
			if(!specList.contains(lotData.getProductSpecName()))
			{
				throw new CustomException("LOT-0055"); 
			}
			
			//3. Check ProcessGroup
			if(!StringUtils.isEmpty(lotData.getProcessGroupName()))
			{
				throw new CustomException("PROCESSGROUP-0002", lotData.getKey().getLotName(), lotData.getProcessGroupName()); 
			}
			
		}
		
		//DeassignCarrierFromLot
		for ( Iterator<?> iteratorLotList = lotElementList.getChildren().iterator(); iteratorLotList.hasNext();)
		{
			Element lotE = (Element) iteratorLotList.next();
			String sLotName = SMessageUtil.getChildText(lotE, "LOTNAME", true);
			
			//GetMaterialU
			MaterialU materialU = new MaterialU();
			materialU.setMaterialName(sLotName);
			materialUList.add(materialU);
			
			//GetLotData
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(sLotName);
			
			if(!StringUtils.isEmpty(lotData.getCarrierName()))
			{
				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
				
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeassignCarrier", this.getEventUser(), this.getEventComment(), "", "");
				
				List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);

				DeassignCarrierInfo createInfo =  MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, durableData, productUSequence);
		
				MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, createInfo, eventInfo);
			}
			
		}
		
		//Split
		List<Element> LotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		
		for(Element lotE : LotList)
		{
			String sLotName = SMessageUtil.getChildText(lotE, "LOTNAME", true);
			String sCstName = SMessageUtil.getChildText(lotE, "CSTNAME", false);
			
			List<Element> productList = SMessageUtil.getSubSequenceItemList(lotE, "PRODUCTLIST", true);
			
			Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(sLotName);
			
			if((int)lotData.getProductQuantity() != productList.size())
			{
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("Split", getEventUser(), getEventComment(), null, null);
				
				Lot childLot = MESLotServiceProxy.getLotServiceUtil().composeLot(eventInfo, null, productList);
			}
		}
		
		//CreateBoxName
		List<String> argSeq = new ArrayList<String>();
		argSeq.add(specList.get(0));

		List<String> boxList = NameServiceProxy.getNameGeneratorRuleDefService().generateName("BoxNaming", argSeq, Integer.parseInt("1"));
		String boxName = boxList.get(0);
		
		//CheckExist BoxName
		MESProcessGroupServiceProxy.getProcessGroupServiceUtil().checkExistProcessGroup(boxName);

		//CreateBox
		List<String> materialList = new ArrayList<String>();
		HashMap<String, String> udfs = new HashMap<String, String>();
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", this.getEventUser(), this.getEventComment(), "", "");
		
		CreateInfo createInfo = new CreateInfo();

		createInfo.setProcessGroupName(boxName);
		createInfo.setProcessGroupType(GenericServiceProxy.getConstantMap().TYPE_BOX);
		createInfo.setMaterialType(GenericServiceProxy.getConstantMap().TYPE_LOT);
		createInfo.setDetailMaterialType(GenericServiceProxy.getConstantMap().TYPE_PANEL);
		createInfo.setMaterialNames(materialList);
		createInfo.setMaterialQuantity(materialList.size());
		createInfo.setMaterialUdfs(udfs);
		createInfo.setUdfs(udfs);
		
		ProcessGroupServiceProxy.getProcessGroupService().create(eventInfo, createInfo);

		//AssignLotToBox
		ProcessGroupKey processGroupKey = new ProcessGroupKey();
		processGroupKey.setProcessGroupName(boxName);
		
		AssignMaterialsInfo assignMaterialInfo = new AssignMaterialsInfo();
		assignMaterialInfo.setMaterialQuantity(materialUList.size());
		assignMaterialInfo.setMaterialUSequence(materialUList);
		
		eventInfo.setEventName("AssignToBox");
		ProcessGroupServiceProxy.getProcessGroupService().assignMaterials(processGroupKey, eventInfo, assignMaterialInfo);
		
		//Track Out
		eventInfo.setEventName("TrackOut");
		
		//return NewBox
		Document rtnDoc = new Document();
		rtnDoc = (Document)doc.clone();
		rtnDoc = SMessageUtil.addItemToBody(rtnDoc, "BOXNAME", boxName);
		return rtnDoc;
	}
}
