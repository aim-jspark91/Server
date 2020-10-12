package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
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
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.LotService;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.impl.LotServiceImpl;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.name.NameServiceProxy;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupKey;
import kr.co.aim.greentrack.processgroup.management.info.AssignMaterialsInfo;
import kr.co.aim.greentrack.processgroup.management.info.CreateInfo;
import kr.co.aim.greentrack.processgroup.management.info.ext.MaterialU;
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

public class Palletizing extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Palletizing", getEventUser(), getEventComment(), "", "");
		
		String palletSN = "";
		String saleDestination = "";
		int materialQuantity = 0;
		
		List<Element> eleLotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);	
		
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		
		List<String> argSeq = new ArrayList<String>();
		List<String> palletList = NameServiceProxy.getNameGeneratorRuleDefService().generateName("GeneratePallet", argSeq, 1);
	
		String palletId = palletList.get(0);
		
		// Create Pallet
		eventInfo.setEventName("CreatePallet");
		ProcessGroupKey processGroupKey = new ProcessGroupKey();		
		processGroupKey.setProcessGroupName(palletId);
				
		CreateInfo createInfo = new CreateInfo();		
		createInfo.setProcessGroupName( palletId );
		createInfo.setProcessGroupType( GenericServiceProxy.getConstantMap().TYPE_PALLET );
		createInfo.setMaterialType(GenericServiceProxy.getConstantMap().TYPE_LOT);
		createInfo.setDetailMaterialType(GenericServiceProxy.getConstantMap().TYPE_PPBOX);
		
		ProcessGroup palletData = ProcessGroupServiceProxy.getProcessGroupService().create(eventInfo, createInfo);

		// Assign DenseBox to Pallet
		List<MaterialU> materialList =  new ArrayList<MaterialU>(); 
		for (Element eLotData : eleLotList) 
		{
			String lotName = eLotData.getChild("LOTNAME").getText();
			MaterialU materialU = new MaterialU();
			materialU.setMaterialName( lotName );
			materialList.add(materialU);
		}
		materialQuantity = eleLotList.size();
		
		Map<String, String> userColumns = new HashMap<String, String>();
		userColumns.put("PRODUCTSPECNAME",productSpecName);
		
		eventInfo.setEventName("AssignPPBoxToPallet");
		AssignMaterialsInfo assignMaterialsInfo = new AssignMaterialsInfo();
		assignMaterialsInfo.setMaterialQuantity(materialQuantity);
		assignMaterialsInfo.setMaterialUSequence(materialList);		
		assignMaterialsInfo.setUdfs( userColumns );
		ProcessGroupServiceProxy.getProcessGroupService().assignMaterials(palletData.getKey(), eventInfo, assignMaterialsInfo );
		
		//New Batch- AssignBoxtoPallet for productList
		List<Product> productDataList =ProductServiceProxy.getProductService().select("WHERE LOTNAME IN(SELECT LOTNAME FROM LOT WHERE PROCESSGROUPNAME = ?)", new Object[] { palletId });
		
		if(productDataList.size()>0){
			Map<String, Object> changeColumns_assignBoxToPallet = new HashMap<String,Object>();
			changeColumns_assignBoxToPallet.put("processGroupName",palletId);
			
			try {
				ProductServiceUtil.productBatchSetEvent(productDataList, eventInfo, changeColumns_assignBoxToPallet);
			} catch (IllegalArgumentException | SecurityException
					| IllegalAccessException | NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		List<Lot> lotDataList =LotServiceProxy.getLotService().select("PROCESSGROUPNAME = ?", new Object[] { palletId });
		
		//Palletizing for lotData
		if(lotDataList.size()>0){
			eventInfo.setEventName( "Palletizing" );
			ProcessOperationSpec nextProcessOperationName = CommonUtil.getNextOperation(factoryName, processFlowName, processOperationName);
			String nextNodeStack = CommonUtil.getNodeStack(factoryName, processFlowName, nextProcessOperationName.getKey().getProcessOperationName());
			// New Batch-TrackIn
			Map<String, Object> changeColumns_Lot = new HashMap<String,Object>();
			changeColumns_Lot.put("processOperationName", nextProcessOperationName.getKey().getProcessOperationName());
			changeColumns_Lot.put("nodeStack", nextNodeStack);
			changeColumns_Lot.put("BEFORECHANGEOPERNAME", lotDataList.get( 0 ).getProcessOperationName());
	
			Map<String, Object> changeColumns_Product = new HashMap<String,Object>();
			changeColumns_Product.put("processOperationName", nextProcessOperationName.getKey().getProcessOperationName());
			changeColumns_Product.put("nodeStack", nextNodeStack);
			changeColumns_Product.put("BEFORECHANGEOPERNAME", lotDataList.get( 0 ).getProcessOperationName());
			
			try {
				LotServiceUtil.LotBatchSetEvent(lotDataList, eventInfo, changeColumns_Lot);
			} catch (IllegalArgumentException | SecurityException
					| IllegalAccessException | NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				ProductServiceUtil.productBatchSetEvent(productDataList, eventInfo, changeColumns_Product);
			} catch (IllegalArgumentException | SecurityException
					| IllegalAccessException | NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
						
		}

		XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "PALLETNAME", palletId);
		
		return doc;
	}

}
