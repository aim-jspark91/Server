package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.service.LotServiceUtil;
import kr.co.aim.messolution.product.service.ProductServiceUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.info.DeassignMaterialsInfo;
import kr.co.aim.greentrack.processgroup.management.info.ext.MaterialU;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;


import org.jdom.Document;
import org.jdom.Element;

public class CancelPalletizing extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelPalletizing", getEventUser(), getEventComment(), "", "");
		
		String sql = "";
		
		List<Map<String, Object>> sqlResult = new ArrayList<Map<String, Object>>();
		Map<String, Object> bindMap = new HashMap<String, Object>();
		
		String palletName = SMessageUtil.getBodyItemValue(doc, "PALLETNAME", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		
		List<Lot> lotList = CommonUtil.getLotListByPalletID( palletName );
		List<Product> productDataList =ProductServiceProxy.getProductService().select("WHERE PROCESSGROUPNAME = ? ", new Object[] { palletName });
		
		String PackingProcessOperationName = CommonUtil.getProcessOperationNameByDetailProcessOperationType(factoryName, processFlowName, "PACKING");
		
		String nodeStack = CommonUtil.getNodeStack(factoryName, processFlowName, PackingProcessOperationName);
		
		// Cancel Palletizing		
		if(lotList.size()>0){
			// New Batch-changeOperation
			Map<String, Object> changeColumns_Lot= new HashMap<String,Object>();
			changeColumns_Lot.put("nodeStack", nodeStack);
			changeColumns_Lot.put("processOperationName", PackingProcessOperationName);
			changeColumns_Lot.put("BEFORECHANGEOPERNAME", lotList.get( 0 ).getProcessOperationName());
			
			Map<String, Object> changeColumns_Product = new HashMap<String,Object>();
			changeColumns_Product.put("nodeStack", nodeStack);
			changeColumns_Product.put("processOperationName", PackingProcessOperationName);
			changeColumns_Product.put("BEFORECHANGEOPERNAME", lotList.get( 0 ).getProcessOperationName());
						
			try {
				LotServiceUtil.LotBatchSetEvent(lotList, eventInfo, changeColumns_Lot);
			} catch (IllegalArgumentException | SecurityException
					| IllegalAccessException | NoSuchFieldException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				ProductServiceUtil.productBatchSetEvent(productDataList, eventInfo, changeColumns_Product);
			} catch (IllegalArgumentException | SecurityException
					| IllegalAccessException | NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//Update ProcessGroup		
		List<MaterialU> materialUSequence = new ArrayList<MaterialU>();
		DeassignMaterialsInfo deassignMaterialsInfo = new DeassignMaterialsInfo();	
		ProcessGroup processGroupData = CommonUtil.getProcessGroupByProcesessGroupName( palletName );
		eventInfo.setEventName("DeassignPPBoxToPallet");
		for(Lot lotData : lotList)
		{
			//check LotState
			CommonValidation.checkLotState(lotData);
			CommonValidation.checkLotHoldState(lotData);
							
			MaterialU mu = new MaterialU();
			mu.setMaterialName(lotData.getKey().getLotName());
			materialUSequence.add(mu);
		}
		
		deassignMaterialsInfo.setMaterialUSequence(materialUSequence);
		deassignMaterialsInfo.setMaterialQuantity(materialUSequence.size());
		ProcessGroupServiceProxy.getProcessGroupService().deassignMaterials( processGroupData.getKey(), eventInfo, deassignMaterialsInfo );
				
		//New Batch- DeassignBoxtoPallet for productList		
		if(productDataList.size()>0){
			Map<String, Object> changeColumns_deassignBoxToPallet = new HashMap<String,Object>();
			changeColumns_deassignBoxToPallet.put("processGroupName","");
			
			try {
				ProductServiceUtil.productBatchSetEvent(productDataList, eventInfo, changeColumns_deassignBoxToPallet);
			} catch (IllegalArgumentException | SecurityException
					| IllegalAccessException | NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
		return doc;
	}

}
