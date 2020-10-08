package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.PanelJudge;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ReviewStationReviewOperationEnd extends AsyncHandler { 
	
	@Override 
	public void doWorks(Document doc) throws CustomException {
		
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "ReviewOperationEndReply");	
		
		String processOperationGroup = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONGROUP", true);
		String lotName 	= SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String machineName 	= SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String lotJudge 	= SMessageUtil.getBodyItemValue(doc, "LOTJUDGE", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReviewOperationEnd", machineName, "ReviewOperationEndReply", null, null);
		
		List<Element> productListE = SMessageUtil.getBodySequenceItemList(doc, "DATALIST", true);
		
		try
		{
			MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			String lotNote ="";
			Product productData = null;
			for (Element productE : productListE)
			{
				String productName = SMessageUtil.getChildText(productE, "PRODUCTNAME", true);
				String productJudge = SMessageUtil.getChildText(productE, "PRODUCTJUDGE", false);
				String panelJudge = SMessageUtil.getChildText(productE, "PANELJUDGE", false);
				String productType = SMessageUtil.getChildText(productE, "PRODUCTTYPE", false);
				
				if(CheckEmptyAndCheckProductGrade(lotName,productName,machineName,productJudge,eventInfo)){
					throw new Exception();
				}
				// Product가 있는지 없는지 검사
				productData = null;
				// Lot이 있는지 없는지 검사
				try {
					productData= MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);
				} catch (Exception e) {
					throw new CustomException("COMMON-0001","Not Exist Product");
				}
				
				try {
					// 위에서 업데이트 한 PanelJudge 이외에 Panel에 대해서 machineName과 ProcessOperationName ProductSpecName, ProductRequestName을 업데이트 한다.
					String condition =  " where GLASSNAME= ? AND PROCESSOPERATIONNAME IS NULL AND MACHINENAME IS NULL";
					Object bindSet[] = new Object[]{ productName };

					List<PanelJudge> panelJudgeList = ExtendedObjectProxy.getPanelJudgeService().select(condition, bindSet);
					
					for(PanelJudge judge : panelJudgeList){
						judge.setMachineName(machineName);
						judge.setProcessOperationName(lotData.getUdfs().get("BEFOREOPERATIONNAME"));
						judge.setProductSpecName(lotData.getProductSpecName());
						judge.setProductRequestName(productData.getProductRequestName());
						judge.setLastEventComment(eventInfo.getEventComment());
						judge.setLastEventName(eventInfo.getEventName());
						judge.setLastEventTime(eventInfo.getEventTime());
						judge.setLastEventUser(eventInfo.getEventUser());
						ExtendedObjectProxy.getPanelJudgeService().modify(eventInfo, judge);
					}
				} catch (CustomException e) {
					eventLog.warn("Not found PanelJudge");
				} catch(Exception e){
					eventLog.warn("Not found PanelJudge");
				}
				
				
				try {
						
					String note = "Update Success!\nGLASS ID:["+productName+"] FileJude:["+productJudge+"]";
					lotNote = lotNote+note +"\n";
					kr.co.aim.greentrack.product.management.info.SetEventInfo setProductEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
					setProductEventInfo.getUdfs().put("FILEJUDGE", productJudge);
					setProductEventInfo.getUdfs().put("NOTE", note);
	
					ProductServiceProxy.getProductService().setEvent(productData.getKey(), eventInfo, setProductEventInfo);

				} catch (Exception e) {
					throw new CustomException("Ratio Error");
				}
				
				// Modified by smkang on 2019.05.28 - ProductServiceProxy.getProductService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//				ClearNoteProduct(productName);
				ClearNoteProduct(productData);
			}
			
			if(lotNote.length()>999){
				lotNote = lotNote.substring(0, 998);
			}
			kr.co.aim.greentrack.lot.management.info.SetEventInfo setLotEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
			setLotEventInfo.getUdfs().put("FILEJUDGE", "G");
			setLotEventInfo.getUdfs().put("NOTE", lotNote);
			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setLotEventInfo);
			
			// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//			ClearNoteLot(lotName);
			ClearNoteLot(lotData);
			
			this.generateBodyTemplate(doc, "OK", "");	
		}
		catch(Exception ex)
		{
			this.generateBodyTemplate(doc, "NG", "");	
		}
		try {
			GenericServiceProxy.getESBServive().sendReplyBySender(getOriginalSourceSubjectName(), doc, "OICSender");	
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	private boolean CheckEmptyAndCheckProductGrade(String lotName,String productName,String machineName,String productJudge,EventInfo eventInfo) throws CustomException{
		try {
	        @SuppressWarnings("unchecked")
	        List<Map<String, Object>> sqlResult =null;
			String note = "";
			String gradeDefinition="";
			boolean isExist = false;
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);
			
			if(StringUtils.isEmpty(machineName)){
        		if(StringUtils.isEmpty(note)){
        			note+="Update Fail!\n";
        		}
				note+=machineName+" is Empty\n";
			}

			if(StringUtils.isEmpty(productJudge)){
        		if(StringUtils.isEmpty(note)){
        			note+="Update Fail!\n";
        		}
				note+=productJudge + " is Empty\n";
			}
			
			String sql =  "select * from GRADEDEFINITION  A WHERE A.GRADETYPE=:GRADETYPE AND A.FACTORYNAME=:FACTORYNAME " ;
            Map<String, String> bindMap = new HashMap<String, String>();
            bindMap.put("GRADETYPE", "Product");
            //bindMap.put("GRADETYPE", "SubProduct");
            bindMap.put("FACTORYNAME", lotData.getFactoryName());

            sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
            
        	for(Map<String, Object> tmp : sqlResult){
        		gradeDefinition+= " " + (String) tmp.get("GRADE") +" ";
        		if(StringUtils.equals(productJudge, (String) tmp.get("GRADE")     ))
        		{
        			isExist=true;
        		}
        	}
            
        	if(!isExist){
        		if(StringUtils.isEmpty(note)){
        			note+="Update Fail!\n";
        		}
        		note += "ProductJudge Error DFS ProductJudge["+productJudge+"]" + "GradeDefinition["+gradeDefinition+"]";
        	}
			
			if(!StringUtils.isEmpty(note)){
				SetEventLotAndProduct(lotData,productData,note,eventInfo);
				
				// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//				ClearNoteLot(lotName);
				ClearNoteLot(lotData);
				
				// Modified by smkang on 2019.05.28 - ProductServiceProxy.getProductService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//				ClearNoteProduct(productName);
				ClearNoteProduct(productData);

				eventLog.error("CheckEmptyAndCheckProductGrade Error");
				return true;				
			}
			
			return false;
		} catch (Exception e) {
			eventLog.error("CheckEmptyAndCheckProductGrade Error");
			return true;
		}
	}
	
	// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//	private void ClearNoteLot(String lotName){
//		// Clear Note
//		try {
//			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//			lotData.getUdfs().put("NOTE", "");
//			LotServiceProxy.getLotService().update(lotData);			
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//	}
	private void ClearNoteLot(Lot lotData){
		try {
			Map<String, String> updateUdfs = new HashMap<String, String>();
			updateUdfs.put("NOTE", "");
			MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
		} catch (Exception e) {
		}
	}
	
	// Modified by smkang on 2019.05.28 - ProductServiceProxy.getProductService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//	private void ClearNoteProduct(String productName){
//		// Clear Note
//		try {
//			
//			Product product = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);
//			product.getUdfs().put("NOTE", "");
//			ProductServiceProxy.getProductService().update(product);		
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//	}
	private void ClearNoteProduct(Product productData) {
		try {
			Map<String, String> updateUdfs = new HashMap<String, String>();
			updateUdfs.put("NOTE", "");
			MESProductServiceProxy.getProductServiceImpl().updateProductWithoutHistory(productData, updateUdfs);
		} catch (Exception e) {
		}
	}

	private void SetEventLotAndProduct(Lot lotData,Product product,String note,EventInfo eventInfo){
		kr.co.aim.greentrack.product.management.info.SetEventInfo setProductEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
		setProductEventInfo.getUdfs().put("NOTE", note);
		kr.co.aim.greentrack.lot.management.info.SetEventInfo setLotEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
		setLotEventInfo.getUdfs().put("NOTE", note);
		
		LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setLotEventInfo);
		ProductServiceProxy.getProductService().setEvent(product.getKey(), eventInfo, setProductEventInfo);
		
	}
	private Element generateBodyTemplate(Document doc, String result, String description) throws CustomException
	{
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		
		Element bodyElement = null;
		bodyElement = new Element("Body");
		
		Element resultE = new Element("RESULT");
		resultE.setText(result);
		bodyElement.addContent(resultE);
		
		Element descE = new Element("DESCRIPTION");
		descE.setText(description);
		bodyElement.addContent(descE);
		
		doc.getRootElement().addContent(2, bodyElement);
		
		return bodyElement;
	}

	
	
	
	
}
