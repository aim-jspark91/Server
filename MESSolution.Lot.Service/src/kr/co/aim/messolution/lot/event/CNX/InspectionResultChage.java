package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.FileJudgeSetting;
import kr.co.aim.messolution.extended.object.management.data.HQGlassJudge;
import kr.co.aim.messolution.extended.object.management.data.PanelJudge;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.SetEventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class InspectionResultChage extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", false);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", false); 

		/*
		 * productJudge 설비에서 전송해주는 GlassJudge
		 * 이 값은 Product Table 에 FileJudge에 저장한다.
		 * 만일 하나의 값이라도  RepairGrade와 같다면, 
		 * Lot의 FileJudge 값을 R로 변경한다.
		 * */
		String productJudge = SMessageUtil.getBodyItemValue(doc, "PRODUCTJUDGE", false);
		/*
		 * panelJudge
		 * ex) PanelID=G,PanelID=X 식으로 받는다. 모든 Panel이 전송되진 않는다.
		 * ex) 공백으로 올수 있으며, <PANELJUDGE>PanelID=G</PANELJUDGE> 와 같이 하나의 값만 전송받을 수 있다.
		 * 각각의 값을 CT_PanelJudge테이블의 PanelJudge 컬럼에서 key값으로 찾은 후 업데이트를 진행한다.
		 * */
		String panelJudge = SMessageUtil.getBodyItemValue(doc, "PANELJUDGE", false);
		String eventUser = StringUtils.isEmpty(this.getEventUser()) ? machineName:this.getEventUser();
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("updatePanelJudgebyReviewAp", eventUser, this.getEventComment(), "", "");
		
		if(StringUtils.isEmpty(eventInfo.getEventTimeKey())){
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		}

		/*
		 * 어느 Grade가 리페어 공정으로 갈것인가를 설정하는 Flag
		 * */
		String RepairGradeFlag="";
		String productSpecName="";
		String productRequestName="";
		Lot lotData = null;
		// Lot이 있는지 없는지 검사
		try {
			lotData= MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			productSpecName = lotData.getProductSpecName();
			productRequestName = lotData.getProductRequestName();
		} catch (Exception e) {
			throw new CustomException("COMMON-0001","Not Exist Lot");
		}
		
		// Product가 있는지 없는지 검사
		Product productData = null;
		// Lot이 있는지 없는지 검사
		try {
			productData= MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);
		} catch (Exception e) {
			throw new CustomException("COMMON-0001","Not Exist Product");
		}
		
		
		// check Empty machineName, lotName, productName, processOperationName, productJudge And CheckGrade productJudge
		if(CheckEmptyAndCheckProductGrade(lotName,productName,machineName,processOperationName,productJudge,eventInfo)){
			return false;
		}
		
		if(CompareOperationAndMachine(lotName,productName,processOperationName,machineName,eventInfo)){
			return false;
		}
		
		
		try {
			// 다음 공정의 RepairGrade를 가져온다.
			// 없으면 "";
			RepairGradeFlag =  getForceRepairFlag(lotData);
		} catch (Exception e) {
			RepairGradeFlag="";
		}
		
		String wrongNote = UpdatePanelJudge(lotName,panelJudge,machineName,processOperationName,eventInfo);
		try {
			// 위에서 업데이트 한 PanelJudge 이외에 Panel에 대해서 machineName과 ProcessOperationName ProductSpecName, ProductRequestName을 업데이트 한다.
			String condition =  " where GLASSNAME= ? AND PROCESSOPERATIONNAME IS NULL AND MACHINENAME IS NULL";
			Object bindSet[] = new Object[]{ productName };

			List<PanelJudge> panelJudgeList = ExtendedObjectProxy.getPanelJudgeService().select(condition, bindSet);
			
			for(PanelJudge judge : panelJudgeList){
				judge.setMachineName(machineName);
				judge.setProcessOperationName(processOperationName);
				judge.setProductSpecName(productSpecName);
				judge.setProductRequestName(productRequestName);
				judge.setLastEventComment(eventInfo.getEventComment());
				judge.setLastEventName(eventInfo.getEventName());
				judge.setLastEventTime(eventInfo.getEventTime());
				judge.setLastEventUser(eventInfo.getEventUser());
				ExtendedObjectProxy.getPanelJudgeService().modify(eventInfo, judge);
			}
		} catch (Exception e) {
			throw new CustomException("COMMON-0001","Extra panel Update Error");
		}
		
		
		try {
			
			// 프로덕트의 FileJudge를 업데이트 한다.
			// 만일 하나라도 RepairGrade와 같다면, Lot의 FileJudge를 변경한다.
			// 아니라면, Lot의 FileJudge를 G로 변경
			
			String note = "Update Success!\nGLASS ID:["+productName+"] FileJude:["+productJudge+"] WrongPanelValue:["+wrongNote+"]";
			kr.co.aim.greentrack.product.management.info.SetEventInfo setProductEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
			setProductEventInfo.getUdfs().put("FILEJUDGE", productJudge);
			setProductEventInfo.getUdfs().put("NOTE", note);
			ProductServiceProxy.getProductService().setEvent(productData.getKey(), eventInfo, setProductEventInfo);
			
			kr.co.aim.greentrack.lot.management.info.SetEventInfo setLotEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
			if(StringUtils.equals(RepairGradeFlag, productJudge))
			{
				setLotEventInfo.getUdfs().put("FILEJUDGE", RepairGradeFlag);
			}
			else
			{
				if(StringUtils.isEmpty(lotData.getUdfs().get("FILEJUDGE"))){
					setLotEventInfo.getUdfs().put("FILEJUDGE", "G");
				}
			}
			setLotEventInfo.getUdfs().put("NOTE", note);
			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setLotEventInfo);
			
			checkThresHoldRatio(lotData.getProductSpecName(),lotData.getFactoryName(),productName,eventInfo);
		} catch (Exception e) {
			throw new CustomException("Ratio Error");
		}
		
		// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
		//									  ProductServiceProxy.getProductService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//		ClearNote(lotName, productName);
		ClearNote(lotData, productData);
		
		return doc;
	}
	private String  UpdatePanelJudge(String lotName,String panelJudge,String machineName,String processOperationName,EventInfo eventInfo) throws CustomException{
		String note = "";
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult =null;
		Lot lotData = null;
		try {
			lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		} catch (Exception e) {
			throw new CustomException("COMMON-0001","Not Fount Lot Data");
		}
		MachineSpec machineSepc =null;
		try {
			machineSepc = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);
		} catch (Exception e) {
			throw new CustomException("COMMON-0001","Not Fount machineSepc Data");
		}
		try {
			String sql =  "select * from GRADEDEFINITION  A WHERE A.GRADETYPE=:GRADETYPE AND A.FACTORYNAME=:FACTORYNAME " ;
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("GRADETYPE", "SubProduct");
			bindMap.put("FACTORYNAME", lotData.getFactoryName());
			sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		} catch (Exception e) {
			return "GradeDefinition Not Found!";
		}
		
		try {
			String[] splitComma = StringUtils.split(panelJudge, ',');
			
			for(String split : splitComma){
				// wrongFlag -> PanelId 혹은 PanelJudge 값이 하나라도 잘못됐다면, false
				// 만일 wrongFlag가 false라면 wrongNote에 PanelId와 PanelJudge값을 추가한다.
				boolean wrongFlag = false;
				String[] splitEqual = StringUtils.split(split, '=');
				
		      	for(Map<String, Object> tmp : sqlResult){
		      		if(StringUtils.equals(splitEqual[1], (String) tmp.get("GRADE"))){
		      			wrongFlag=true;
		      		}
		      	}

		      	if(wrongFlag == true){      		
					try {
						PanelJudge panelJude = ExtendedObjectProxy.getPanelJudgeService().selectByKey(false, new Object[] {splitEqual[0]});
						// "-" 의 의미 어떠한 경우든
			      		if( StringUtils.equals("ATST", machineSepc.getUdfs().get("CONSTRUCTTYPE")) ){
			      			// ATST인 경우는
			      			// G * O = O , - * X = X , - * R  = R
			      			panelJude.setPanelJudge(splitEqual[1]);
			      		}else{
			      			// ATST가 아닌경우
			      			// - * X = X, G * O = G , R * O = O
			      			if(StringUtils.equals("X", splitEqual[1])){
			      				panelJude.setPanelJudge(splitEqual[1]);
			      			}
			      			else if(StringUtils.equals("O", splitEqual[1])){
			      				if(StringUtils.equals("G", panelJude.getPanelJudge())){
			      					//panelJude.setPanelJudge(panelJudge);
			      				}
			      				else if(StringUtils.equals("R", panelJude.getPanelJudge())){
			      					panelJude.setPanelJudge(splitEqual[1]);
			      				}
			      			}

			      		}
						
						panelJude.setMachineName(machineName);
						panelJude.setProcessOperationName(processOperationName);
						panelJude.setProductSpecName(lotData.getProductSpecName());
						panelJude.setProductRequestName(lotData.getProductRequestName());
						panelJude.setLastEventComment(eventInfo.getEventComment());
						panelJude.setLastEventName(eventInfo.getEventName());
						panelJude.setLastEventTime(eventInfo.getEventTime());
						panelJude.setLastEventUser(eventInfo.getEventUser());
						
						ExtendedObjectProxy.getPanelJudgeService().modify(eventInfo, panelJude);	
					} catch (Exception e) {
						wrongFlag=false;
					}
		      	}

				if(wrongFlag==false){
					note += split+" ";
				}
				
			}
			
			return note;
		} catch (Exception e) {
			return note;
		}
	}
	
	// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
	//									  ProductServiceProxy.getProductService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//	private void ClearNote(String lotName, String productName){
	private void ClearNote(Lot lotData, Product productData) {
		// Clear Note
		try {
			// Modified by smkang on 2019.05.24 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//			lotData.getUdfs().put("NOTE", "");
//			LotServiceProxy.getLotService().update(lotData);
			Map<String, String> updateUdfs = new HashMap<String, String>();
			updateUdfs.put("NOTE", "");
			MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
			
			// Modified by smkang on 2019.05.28 - ProductServiceProxy.getProductService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//			Product product = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);
//			product.getUdfs().put("NOTE", "");
//			ProductServiceProxy.getProductService().update(product);
			Map<String, String> updateUdfs2 = new HashMap<String, String>();
			updateUdfs2.put("NOTE", "");
			MESProductServiceProxy.getProductServiceImpl().updateProductWithoutHistory(productData, updateUdfs2);
		} catch (Exception e) {
			// TODO: handle exception
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
	private boolean CheckEmptyAndCheckProductGrade(String lotName,String productName,String machineName,String processOperationName,String productJudge,EventInfo eventInfo) throws CustomException{
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
			if(StringUtils.isEmpty(processOperationName)){
        		if(StringUtils.isEmpty(note)){
        			note+="Update Fail!\n";
        		}
				note+=processOperationName + " is Empty\n";
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
				
				// Modified by smkang on 2019.05.24 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
				//									  ProductServiceProxy.getProductService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//				ClearNote(lotName, productName);
				ClearNote(lotData, productData);
				
				eventLog.error("CheckEmptyAndCheckProductGrade Error");
				return true;				
			}
			
			return false;
		} catch (Exception e) {
			eventLog.error("CheckEmptyAndCheckProductGrade Error");
			return true;
		}
	}
	
	private boolean CompareOperationAndMachine(String lotName,String productName,String processOperationName,String machineName,EventInfo eventInfo) throws CustomException{
		try {
			// 만일 검사 공정의 ProcessOperationName과 MachineName을 DFS로 부터 받았다면,
			// 에러 발생하고 Note 컬럼에 에러 원인 작성
			
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);
			String note="Update Fail!\n";
			
			ProcessOperationSpec processOperation = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
			// 현재 공정이 Dummy 공정인 경우와
			if( StringUtils.equalsIgnoreCase("DUMMY", processOperation.getDetailProcessOperationType()) ){
				// 만일 Lot의 BeforeOperationName != processOperationName && LastLogedMachine != machineName
				if(!StringUtils.equals(processOperationName, lotData.getUdfs().get("BEFOREOPERATIONNAME")) && !StringUtils.equals(machineName, lotData.getUdfs().get("LASTLOGGEDOUTMACHINE")) ){
					note = "BeforeOperationName != processOperationName && LastLogedMachine != machineName";
					note +="\nDFSOperationName["+processOperationName +"], DFSMachineName["+machineName +"]";
					SetEventLotAndProduct(lotData,productData,note,eventInfo);
					
					// Modified by smkang on 2019.05.24 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
					//									  ProductServiceProxy.getProductService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//					ClearNote(lotName, productName);
					ClearNote(lotData, productData);
					
					throw new CustomException("COMMON-0001","BeforeOperationName != processOperationName && LastLogedMachine != machineName");
				}
				// Lot의 BeforeOperationName != processOperationName
				else if(!StringUtils.equals(processOperationName, lotData.getUdfs().get("BEFOREOPERATIONNAME"))){
					note = "BeforeOperationName != processOperationName";
					note +="\nDFSOperationName["+processOperationName +"], DFSMachineName["+machineName +"]";
					SetEventLotAndProduct(lotData,productData,note,eventInfo);
					
					// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
					//									  ProductServiceProxy.getProductService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//					ClearNote(lotName, productName);
					ClearNote(lotData, productData);
					
					throw new CustomException("COMMON-0001","BeforeOperationName != processOperationName");
				}
				// LastLogedMachine != machineName
				else if(!StringUtils.equals(machineName, lotData.getUdfs().get("LASTLOGGEDOUTMACHINE"))){
					note = "LastLogedMachine != machineName";
					note +="\nDFSOperationName["+processOperationName +"], DFSMachineName["+machineName +"]";
					SetEventLotAndProduct(lotData,productData,note,eventInfo);
					
					// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
					//									  ProductServiceProxy.getProductService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//					ClearNote(lotName, productName);
					ClearNote(lotData, productData);
					
					throw new CustomException("COMMON-0001","LastLogedMachine != machineName");
				}
			}
			// 그렇지 않은 경우
			else
			{
				// 만일 Lot의 CurrentOperationName != processOperationName && CurrentMachine != machineName
				if(!StringUtils.equals(processOperationName, lotData.getProcessOperationName()) && !StringUtils.equals(machineName, lotData.getMachineName()) ){
					note = "CurrentOperationName != processOperationName && CurrentMachine != machineName";
					note +="\nDFSOperationName["+processOperationName +"], DFSMachineName["+machineName +"]";
					SetEventLotAndProduct(lotData,productData,note,eventInfo);
					
					// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
					//									  ProductServiceProxy.getProductService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//					ClearNote(lotName, productName);
					ClearNote(lotData, productData);
					
					throw new CustomException("COMMON-0001","BeforeOperationName != processOperationName && LastLogedMachine != machineName");
				}
				// Lot의 CurrentOperationName != processOperationName
				else if(!StringUtils.equals(processOperationName, lotData.getProcessOperationName()  )){
					note = "CurrentOperationName != processOperationName";
					note +="\nDFSOperationName["+processOperationName +"], DFSMachineName["+machineName +"]";
					SetEventLotAndProduct(lotData,productData,note,eventInfo);
					
					// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
					//									  ProductServiceProxy.getProductService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//					ClearNote(lotName, productName);
					ClearNote(lotData, productData);
					
					throw new CustomException("COMMON-0001","BeforeOperationName != processOperationName");
				}
				// CurrentMachine != machineName
				else if(!StringUtils.equals(machineName, lotData.getMachineName()  )){
					note = "CurrentMachine != machineName";
					note +="\nDFSOperationName["+processOperationName +"], DFSMachineName["+machineName +"]";
					SetEventLotAndProduct(lotData,productData,note,eventInfo);
					
					// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
					//									  ProductServiceProxy.getProductService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//					ClearNote(lotName, productName);
					ClearNote(lotData, productData);
					
					throw new CustomException("COMMON-0001","LastLogedMachine != machineName");
				}
			}
			
			return false;
		} catch (Exception e) {
			eventLog.error("CompareOperationAndMachine Error");
			return true;
		}
		
	}
	private String getForceRepairFlag(Lot lotData){
    	// 다음 공정 정보를 가져와서
    	// FileJudgeSetting 기준 정보 찾고
    	// 다음공정의 RepariGradeFlag == lotData의 FileJudge와 같다면 true 틀리면 false
    	try {
    		
    		ProcessOperationSpec operationData = null;
    		
    		if(StringUtils.equals("RUN", lotData.getLotProcessState())){
    			operationData = getNextNextOperation(lotData);
    		}
    		else if(StringUtils.equals("WAIT", lotData.getLotProcessState())){
    			operationData = CommonUtil.getNextOperation(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessOperationName());
    		}
			
			
			FileJudgeSetting fileJudgeSetting = ExtendedObjectProxy.getFileJudgeSettingService().selectByKey(false, new Object[] {lotData.getFactoryName() ,lotData.getProcessFlowName(),lotData.getProcessFlowVersion(), operationData.getKey().getProcessOperationName(),operationData.getKey().getProcessOperationVersion() });
			
			return fileJudgeSetting.getRepairGradeFlag();

		} catch (CustomException e) {
			e.printStackTrace();
			eventLog.error("getForceRepairFlag Error");
			return "";
		}

    }
    private ProcessOperationSpec getNextNextOperation( Lot lotData ) throws CustomException
    {	
    	try {
    		ProcessOperationSpec nextOperationData = CommonUtil.getNextOperation(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessOperationName());
    		nextOperationData = CommonUtil.getNextOperation(lotData.getFactoryName(), lotData.getProcessFlowName(), nextOperationData.getKey().getProcessOperationName());
    		return nextOperationData;
		} catch (Exception e) {
			throw new CustomException("COMMON-0001","Error getNextNextOperationName");
		}
    }
	private void checkThresHoldRatio(String productSpecName, String factoryName, String glassName, EventInfo eventInfo) throws CustomException 
	{
 		Double cut1ThresHoldRatio = 0.0;
		Double cut2ThresHoldRatio = 0.0;
		Double cut3ThresHoldRatio = 0.0;
		Double cut4ThresHoldRatio = 0.0;

		ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, productSpecName, "00001");
		
		int cutType = StringUtil.equals(productSpecData.getUdfs().get("CUTTYPE").toUpperCase(), "HALF") ? 2 : 4 ;
		
		if(Double.valueOf(productSpecData.getUdfs().get("CUT1THRESHOLDRATIO"))!=null)
		{
			cut1ThresHoldRatio = Double.valueOf(productSpecData.getUdfs().get("CUT1THRESHOLDRATIO"));
		}
		
		if(Double.valueOf(productSpecData.getUdfs().get("CUT2THRESHOLDRATIO"))!=null)
		{
			cut2ThresHoldRatio = Double.valueOf(productSpecData.getUdfs().get("CUT2THRESHOLDRATIO"));
		}

		if(StringUtil.equals(String.valueOf(cutType), "4"))
		{
			if(Double.valueOf(productSpecData.getUdfs().get("CUT3THRESHOLDRATIO"))!=null)
			{
				cut3ThresHoldRatio = Double.valueOf(productSpecData.getUdfs().get("CUT3THRESHOLDRATIO"));
			}
			
			if(Double.valueOf(productSpecData.getUdfs().get("CUT4THRESHOLDRATIO"))!=null)
			{
				cut4ThresHoldRatio = Double.valueOf(productSpecData.getUdfs().get("CUT4THRESHOLDRATIO"));
			}
		}

		for(int i=1; i< cutType+1; i++)
		{
			StringBuilder sql = new StringBuilder();

			sql.append(" select  PJ.PANELNAME, SUBSTR(PJ.PANELNAME,-5,1) CUTPOSITION, PJ.PANELJUDGE ");
			sql.append("   from CT_PANELJUDGE PJ ");
			sql.append("  WHERE     PJ.GLASSNAME = :GLASSNAME ");
			sql.append("        AND SUBSTR(PJ.PANELNAME,-5,1) = :CUTNUM ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("GLASSNAME", glassName);
			bindMap.put("CUTNUM", i);

			List<Map<String, Object>> sqlResult = new ArrayList<Map<String, Object>>();

			try
			{
				sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

				if(sqlResult.size()>0)
				{
					double oNum = 0;
					double xNum = 0;
					double gNum = 0;
					double otherGradeNum = 0;

					for(int j=0; j<sqlResult.size(); j++)
					{
						String panelJudge = sqlResult.get(j).get("PANELJUDGE").toString();

						if(StringUtil.equals(panelJudge, "O"))
						{
							oNum++;
						}
						else if(StringUtil.equals(panelJudge, "X"))
						{
							xNum++;
						}
						else if(StringUtil.equals(panelJudge, "G"))
						{
							gNum++;
						}
						else if((!StringUtil.equals(panelJudge, "G"))&&(!StringUtil.equals(panelJudge, "O")&&(!StringUtil.equals(panelJudge, "X"))))
						{
							otherGradeNum ++;
						}
					}

					Double cutXThresHoldRatio = 0.0;
					
					if(i==1) cutXThresHoldRatio = cut1ThresHoldRatio;
					else if(i==2) cutXThresHoldRatio = cut2ThresHoldRatio;
					else if(i==3) cutXThresHoldRatio = cut3ThresHoldRatio;
					else if(i==4) cutXThresHoldRatio = cut4ThresHoldRatio;

					HQGlassJudge hqGlassJudge = new HQGlassJudge();

					try
					{
						hqGlassJudge = ExtendedObjectProxy.getHQGlassJudgeService().selectByKey(false, new Object[] {glassName+String.valueOf(i)});
					}
					catch(Throwable e)
					{
						//hqGlass is not exist..
					}

					if(hqGlassJudge!=null)
					{
						if(oNum!=0)
						{
							if(cutXThresHoldRatio!= 0.0 && oNum/(oNum+xNum)<cutXThresHoldRatio)
							{
								if(!StringUtil.equals(hqGlassJudge.getHQGlassJudge(), "N"))
								{
									hqGlassJudge.setHQGlassJudge("N");
									hqGlassJudge.setLastEventName(eventInfo.getEventName());
									hqGlassJudge.setLastEventUser(eventInfo.getEventUser());
									hqGlassJudge.setLastEventTime(eventInfo.getEventTime());
									hqGlassJudge.setLastEventComment(eventInfo.getEventComment());

									ExtendedObjectProxy.getHQGlassJudgeService().modify(eventInfo, hqGlassJudge);
								}
							}
						}
						else
						{
							if(cutXThresHoldRatio!= 0.0 && gNum/(gNum+xNum)<cutXThresHoldRatio)
							{
								if(!StringUtil.equals(hqGlassJudge.getHQGlassJudge(), "N"))
								{
									hqGlassJudge.setHQGlassJudge("N");
									hqGlassJudge.setLastEventName(eventInfo.getEventName());
									hqGlassJudge.setLastEventUser(eventInfo.getEventUser());
									hqGlassJudge.setLastEventTime(eventInfo.getEventTime());
									hqGlassJudge.setLastEventComment(eventInfo.getEventComment());

									ExtendedObjectProxy.getHQGlassJudgeService().modify(eventInfo, hqGlassJudge);
								}
							}
						}
					}
				}
			}
			catch (Exception ex)
			{
				throw new CustomException("CRATE-0003", factoryName, productSpecName);
			}
		}
		
		List<HQGlassJudge> hqGlassJudgeList = new ArrayList<HQGlassJudge>();
		
		try
		{
			hqGlassJudgeList = ExtendedObjectProxy.getHQGlassJudgeService().select("where glassname = ? ", new Object[]{glassName});
		}
		catch(Throwable e)
		{
			//HQGlassJudge List is not exist..
		}
		
		if(hqGlassJudgeList.size()>0)
		{
			boolean ngFlag = true;
			
			for(int i=0; i<hqGlassJudgeList.size(); i++)
			{
				HQGlassJudge hqGlassJudge = hqGlassJudgeList.get(i);
				
				if(!StringUtil.equals(hqGlassJudge.getHQGlassJudge(), "N"))
				{
					ngFlag = false;
				}
			}
			
			if(ngFlag)
			{
				// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(glassName);
				Product productData = ProductServiceProxy.getProductService().selectByKeyForUpdate(new ProductKey(glassName));
				
				if(!StringUtil.equals(productData.getProductGrade(), "N"))
				{
					productData.setProductGrade("N");
					
					ProductServiceProxy.getProductService().update(productData);
					
					SetEventInfo setEventInfo = new SetEventInfo();
					ProductServiceProxy.getProductService().setEvent(productData.getKey(), eventInfo, setEventInfo);
				}
				
//				try
//				{
//					Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(productData.getLotName());
//					
//					EventInfo eventInfo1 = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
//					
//					eventInfo1.setEventTime(TimeStampUtil.getCurrentTimestamp());
//					eventInfo1.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
//					eventInfo1.setReasonCode("HD-0001");
//					eventInfo1.setReasonCodeType("HoldLot");
//					eventInfo1.setEventName("cutThresHold");
//					eventInfo1.setEventComment("Hold by CutThresHoldRatio");
//					
//					if(!lotData.getLotHoldState().equals("Y"))
//					{
//						List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
//						MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo(productUSequence);
//						LotServiceProxy.getLotService().makeOnHold(lotData.getKey(), eventInfo1, makeOnHoldInfo);
//						
//						try {
//							MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(lotData.getKey().getLotName(), "HD-0001", " ","AHOLD", eventInfo1);
//						} catch (Exception e) {
//							eventLog.warn(e);
//						}
//					}
//					else
//					{	
//						kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
//						LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo1, setEventInfo);
//						
//						try {
//							MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(lotData.getKey().getLotName(), "HD-0001", " ","AHOLD", eventInfo1);
//						} catch (Exception e) {
//							eventLog.warn(e);
//						}
//						// -------------------------------------------------------------------------------------------------------------------------------------------
//					}
//				}
//				catch (Throwable ex)
//				{
//					eventLog.error(ex.getMessage());
//				}
			}
		}
	}
}
