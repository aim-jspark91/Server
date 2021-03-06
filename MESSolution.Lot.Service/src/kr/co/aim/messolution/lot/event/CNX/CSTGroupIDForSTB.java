package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LotAction;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.processgroup.service.ProcessGroupInfoUtil;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedInInfo;
import kr.co.aim.greentrack.lot.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.lot.management.info.MakeReleasedInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupKey;
import kr.co.aim.greentrack.processgroup.management.info.CreateInfo;
import kr.co.aim.greentrack.processoperationspec.ProcessOperationSpecServiceProxy;
import kr.co.aim.greentrack.processoperationspec.management.ProcessOperationSpecService;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductC;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.ProductRequestService;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequestplan.ProductRequestPlanServiceProxy;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CSTGroupIDForSTB extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		/* 확인해봐야 할 사항들. (확인 후 해당 주석 지우겠습니다)
		 * PI STB
		   1. TFT 의 Product , CF Product 개수 같아야 Group 가능한지 확인
			-> 가능하다면, 이미 GroupID 가 존재하는 랏은 Split, Merge, (Scrap?) 불가능하게 Validation? 아니면 Cancel STB 후 진행하라는 메시지?
		   2. ProductGrade 섞여있어도 Group 가능한지 확인
		   3. TFT, CF Lot 의 Flow, Operation 무조건 같아야 Group 가능한지 확인.
		   5. Cancel STB 시 이전 공정으로 Change 맞는지? 
		   6. 밸리데이션 중 Cell Stocker 에 있을 경우에만 STB 가능하도록 요구하였는데, 오프라인의 경우?
 		 */
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Release", getEventUser(), "STB", "", "");
		
		List<Element> eleArrayLotList = SMessageUtil.getBodySequenceItemList(doc, "ARRAYLOTLIST", false);
		List<Element> eleCFLotList = SMessageUtil.getBodySequenceItemList(doc, "CFLOTLIST", false);
		String note = SMessageUtil.getBodyItemValue(doc, "NOTE", true);
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		String CreateIDFlag = SMessageUtil.getBodyItemValue(doc, "CREATEIDFLAG", true);
		String groupID = SMessageUtil.getBodyItemValue(doc, "GROUPID", false);
		
		// Validation. GroupID Create or Assigned.
		if (groupID == null || StringUtils.isEmpty(groupID))
		{
			if (StringUtils.equals(CreateIDFlag, "N"))
			{
				throw new CustomException("LOT-0239");
			}
		}
		
		ProcessGroup processGroupData = new ProcessGroup();
		
		if (StringUtils.equals(CreateIDFlag, "Y"))
		{
			processGroupData = this.CreateProcessGroupName("CELL", productRequestName, eventInfo);
		}
		else
		{
			ProcessGroupKey processGroupKey = new ProcessGroupKey(groupID);
			processGroupData = ProcessGroupServiceProxy.getProcessGroupService().selectByKeyForUpdate(processGroupKey);
		}
		
		ProductRequestKey arrayProductRequestKey = new ProductRequestKey();
		arrayProductRequestKey.setProductRequestName(productRequestName);
		ProductRequest arrayProductRequestData = ProductRequestServiceProxy.getProductRequestService().selectByKey(arrayProductRequestKey);

		ProductRequest cfProductRequestData = getCFProductRequestName(productRequestName);

		// Validation. Array WO State.
		if (arrayProductRequestData.getProductRequestState().equals(GenericServiceProxy.getConstantMap().Prq_Completed))
		{
			throw new CustomException("PRODUCTREQUEST-0042", arrayProductRequestData.getKey().getProductRequestName());
		}

		// Validation. CF WO State.
		if (cfProductRequestData.getProductRequestState().equals(GenericServiceProxy.getConstantMap().Prq_Completed))
		{
			throw new CustomException("PRODUCTREQUEST-0042", cfProductRequestData.getKey().getProductRequestName());
		}

		eventLog.debug("Lot will be locked to be prevented concurrent executing.");
		Map<String, Lot> arrayLotDataMap = new ConcurrentHashMap<String, Lot>();
		for (Element eleLot : eleArrayLotList) {
			String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
			arrayLotDataMap.put(lotName, LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName)));
		}
		
		Map<String, Lot> cfLotDataMap = new ConcurrentHashMap<String, Lot>();
		for (Element eleLot : eleCFLotList) {
			String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
			cfLotDataMap.put(lotName, LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName)));
		}
		eventLog.debug("Lot is locked to be prevented concurrent executing.");
		
		int sumArrayProductQuantity = 0;
		int sumCfProductQuantity = 0;
		
		//------ Start Update Lot & Product [PROCESSGROUPNAME Column] ------//
		for (Element eleLot : eleArrayLotList)
		{
			String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);

			Lot lotData = arrayLotDataMap.get(lotName);

			ProcessOperationSpec processOperationSpec = CommonUtil.getProcessOperationSpec(lotData);

			if (!StringUtils.equals(processOperationSpec.getDetailProcessOperationType(), "STB"))
			{
				throw new CustomException("LOT-0245", lotData.getKey().getLotName(), lotData.getProcessOperationName());
			}

			if (StringUtils.isNotEmpty(lotData.getProcessGroupName()))
			{
				throw new CustomException("LOT-0241", lotData.getKey().getLotName(), lotData.getProcessGroupName());
			}

			if (this.checkCellStocker(lotData))
			{
				throw new CustomException("LOT-0246", lotData.getCarrierName(), lotData.getKey().getLotName());
			}

			lotData.setProcessGroupName(processGroupData.getKey().getProcessGroupName());
			LotServiceProxy.getLotService().update(lotData);

			// Releasd For STB
			lotData = ReleaseForSTB(lotData, eventInfo);

			// TrackIn For STB
			lotData = TrackInForSTB(lotData, eventInfo);

			// TrackOut For STB
			lotData = TrackOutForSTB(lotData, eventInfo);

			List<Product> ProductList = new ArrayList<Product>();

			try
			{
				ProductList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());
				sumArrayProductQuantity += ProductList.size();
			}
			catch (Exception e)
			{
				throw new CustomException("LOT-0238", lotData.getKey().getLotName());
			}

			// for(Product product : ProductList) {
			// product.setProcessGroupName(processGroupData.getKey().getProcessGroupName());
			//
			// ProductServiceProxy.getProductService().update(product);
			//
			// kr.co.aim.greentrack.product.management.info.SetEventInfo
			// productSetEventInfo = new
			// kr.co.aim.greentrack.product.management.info.SetEventInfo();
			// ProductServiceProxy.getProductService().setEvent(product.getKey(),
			// eventInfo, productSetEventInfo);
			// }
		}
		
		for (Element eleLot : eleCFLotList)
		{
			String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);

			Lot lotData = cfLotDataMap.get(lotName);

			ProcessOperationSpec processOperationSpec = CommonUtil.getProcessOperationSpec(lotData);

			if (!StringUtils.equals(processOperationSpec.getDetailProcessOperationType(), "STB"))
			{
				throw new CustomException("LOT-0245", lotData.getKey().getLotName(), lotData.getProcessOperationName());
			}

			if (StringUtils.isNotEmpty(lotData.getProcessGroupName()))
			{
				throw new CustomException("LOT-0241", lotData.getKey().getLotName(), lotData.getProcessGroupName());
			}

			if (this.checkCellStocker(lotData))
			{
				throw new CustomException("LOT-0246", lotData.getCarrierName(), lotData.getKey().getLotName());
			}

			lotData.setProcessGroupName(processGroupData.getKey().getProcessGroupName());
			LotServiceProxy.getLotService().update(lotData);

			// Releasd For STB
			lotData = ReleaseForSTB(lotData, eventInfo);

			// TrackIn For STB
			lotData = TrackInForSTB(lotData, eventInfo);

			// TrackOut For STB
			lotData = TrackOutForSTB(lotData, eventInfo);

			List<Product> ProductList = new ArrayList<Product>();

			try
			{
				ProductList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());
				sumCfProductQuantity += ProductList.size();
			}
			catch (Exception e)
			{
				throw new CustomException("LOT-0238", lotData.getKey().getLotName());
			}
			//
			// for(Product product : ProductList) {
			// product.setProcessGroupName(processGroupData.getKey().getProcessGroupName());
			//
			// ProductServiceProxy.getProductService().update(product);
			//
			// kr.co.aim.greentrack.product.management.info.SetEventInfo
			// productSetEventInfo = new
			// kr.co.aim.greentrack.product.management.info.SetEventInfo();
			// ProductServiceProxy.getProductService().setEvent(product.getKey(),
			// eventInfo, productSetEventInfo);
			// }
		}
		// ------ End Update Lot & Product [PROCESSGROUPNAME Column]------//
		
		
		
		//------ Start Calculate WorkOrder ReleasedQuantity ------//
		//ARRAY
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().calculateProductRequestQtyForSTB(arrayProductRequestData.getKey().getProductRequestName(), "R", (long)sumArrayProductQuantity, eventInfo);
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().calculateProductRequestQtyForSTB(arrayProductRequestData.getKey().getProductRequestName(), "F", (long)sumArrayProductQuantity, eventInfo);
		//CF
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().calculateProductRequestQtyForSTB(cfProductRequestData.getKey().getProductRequestName(), "R", (long)sumCfProductQuantity, eventInfo);
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().calculateProductRequestQtyForSTB(cfProductRequestData.getKey().getProductRequestName(), "F", (long)sumCfProductQuantity, eventInfo);
		//------ End Calculate WorkOrder ReleasedQuantity ------//
		
		arrayProductRequestData = ProductRequestServiceProxy.getProductRequestService().selectByKey(arrayProductRequestData.getKey());
		cfProductRequestData = ProductRequestServiceProxy.getProductRequestService().selectByKey(cfProductRequestData.getKey());
		
		if (arrayProductRequestData.getPlanQuantity() < arrayProductRequestData.getFinishedQuantity())
		{
			throw new CustomException("PRODUCTREQUEST-0059", arrayProductRequestData.getKey().getProductRequestName());
		}
		
		if (cfProductRequestData.getPlanQuantity() < cfProductRequestData.getFinishedQuantity())
		{
			throw new CustomException("PRODUCTREQUEST-0059", cfProductRequestData.getKey().getProductRequestName());
		}
		
		//this.checkSameProductQuantity(productRequestName, processGroupData.getKey().getProcessGroupName());
		
//		if(!this.checkMixGlassSize("CELL", processGroupData.getKey().getProcessGroupName())) {
//			throw new CustomException("LOT-0236");
//		}
		
//		if(!this.checkMixGlassGrade(processGroupData.getKey().getProcessGroupName())) {
//			throw new CustomException("LOT-0235");
//		}
		
		this.setReturnGroupInfo(doc, processGroupData.getKey().getProcessGroupName());
		
		return doc;
	}

	private void setReturnGroupInfo(Document doc, String groupID)
	{
		try
		{
			StringBuilder strComment = new StringBuilder();

			strComment.append(groupID).append("\n");

			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, strComment.toString());
		}
		catch (Exception ex)
		{
			eventLog.warn("Warning setReturnGroupInfo..");
		}
	}

	private ProcessGroup CreateProcessGroupName(String factoryName, String productRequestName, EventInfo eventInfo) throws CustomException
	{

		EventInfo eventInfoForPG = EventInfoUtil.makeEventInfo("Create", eventInfo.getEventUser(), "Create", "", "");
		eventInfoForPG.setEventTime(eventInfo.getEventTime());
		eventInfoForPG.setEventTimeKey(eventInfo.getEventTimeKey());

		HashMap<String, String> udfs = new HashMap<String, String>();

		ProductRequest productRequestData = CommonUtil.getProductRequestData(productRequestName);
		ProductSpec productSpecData = CommonUtil.getProductSpecByProductSpecName(factoryName, productRequestData.getProductSpecName(), "00001");

		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("PRODUCTSPECNAME", productSpecData.getKey().getProductSpecName());
		nameRuleAttrMap.put("OWNER", productSpecData.getProductionType());

		List<String> cstGroupId = CommonUtil.generateNameByNamingRule("CSTGroupIdNaming", nameRuleAttrMap, 1);

		if (StringUtils.isEmpty(cstGroupId.get(0).toString()))
		{
			throw new CustomException(""); // Fail generate GroupID!
		}

		CreateInfo createInfo = new CreateInfo();

		createInfo.setProcessGroupName(cstGroupId.get(0));
		createInfo.setProcessGroupType("GROUPID");
		createInfo.setMaterialType(GenericServiceProxy.getConstantMap().TYPE_LOT);

		createInfo.setMaterialUdfs(udfs);
		createInfo.setUdfs(udfs);

		ProcessGroup newGroup = ProcessGroupServiceProxy.getProcessGroupService().create(eventInfoForPG, createInfo);

		return newGroup;
	}

	private boolean checkMixGlassSize(String factoryName, String groupID) {
		try {
			String sql = "SELECT DISTINCT SUBSTR(PS.PRODUCTSPECNAME,3,3) AS GLASSSIZE "
					+ "  FROM LOT L "
					+ " INNER JOIN PRODUCTSPEC PS ON L.PRODUCTSPECNAME = PS.PRODUCTSPECNAME "
					+ " INNER JOIN PROCESSGROUP PG ON L.PROCESSGROUPNAME = PG.PROCESSGROUPNAME  "
					+ " WHERE L.PROCESSGROUPNAME = :PROCESSGROUPNAME "
					+ " AND PS.FACTORYNAME = :FACTORYNAME "
					+ " AND PG.PROCESSGROUPTYPE = :PROCESSGROUPTYPE "
					+ " AND PG.MATERIALTYPE = :MATERIALTYPE ";

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PROCESSGROUPNAME", groupID);
			bindMap.put("FACTORYNAME", factoryName);
			bindMap.put("PROCESSGROUPTYPE", "GROUPID");
			bindMap.put("MATERIALTYPE", "Lot");

			List<Map<String, Object>> sqlResult = GenericServiceProxy
					.getSqlMesTemplate().queryForList(sql, bindMap);

			if (sqlResult.size() == 0 || sqlResult.size() > 1) {
				return false;
			} else {
				return true;
			}
		} catch (Exception ex) {
			return false;
		}
	}

	private boolean checkMixGlassGrade(String groupID) {
		try {
			String sql = "SELECT DISTINCT P.PRODUCTGRADE "
					+ "  FROM PRODUCT P "
					+ " INNER JOIN PROCESSGROUP PG ON P.PROCESSGROUPNAME = PG.PROCESSGROUPNAME  "
					+ " WHERE P.PROCESSGROUPNAME = :PROCESSGROUPNAME  "
					+ " AND PG.PROCESSGROUPTYPE = :PROCESSGROUPTYPE "
					+ " AND PG.MATERIALTYPE = :MATERIALTYPE ";

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PROCESSGROUPNAME", groupID);
			bindMap.put("PROCESSGROUPTYPE", "GROUPID");
			bindMap.put("MATERIALTYPE", "Lot");

			List<Map<String, Object>> sqlResult = GenericServiceProxy
					.getSqlMesTemplate().queryForList(sql, bindMap);

			if (sqlResult.size() == 0 || sqlResult.size() > 1) {
				return false;
			} else {
				return true;
			}
		} catch (Exception ex) {
			return false;
		}
	}

	private boolean checkCellStocker(Lot lotData) {

		String sql = "SELECT COUNT(L.LOTNAME)  "
				+ " FROM LOT L "
				+ "   INNER JOIN DURABLE D ON L.CARRIERNAME = D.DURABLENAME "
				+ "   INNER JOIN MACHINESPEC MS ON D.MACHINENAME = MS.MACHINENAME "
				+ " WHERE MS.FACTORYNAME = :FACTORYNAME  "
				+ "     AND MS.MACHINETYPE = :MACHINETYPE "
				+ "     AND L.LOTNAME = :LOTNAME ";

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", "CELL");
		bindMap.put("MACHINETYPE", "TransportMachine");
		bindMap.put("LOTNAME", lotData.getKey().getLotName());

		List<Map<String, Object>> sqlResult = GenericServiceProxy
				.getSqlMesTemplate().queryForList(sql, bindMap);

		if (sqlResult.size() <= 0) {
			return true;
		}

		return false;
	}

	private void checkSameProductQuantity(String productRequestName,
			String groupID) throws CustomException {

		String sql = "WITH ARRAY_CST AS  "
				+ " ( "
				+ " SELECT COUNT(P.PRODUCTNAME) AS PRODUCTQUANTITY "
				+ " 	  FROM PRODUCT P "
				+ "     INNER JOIN PRODUCTREQUEST PR ON P.PRODUCTREQUESTNAME = PR.PRODUCTREQUESTNAME  "
				+ "     WHERE P.PROCESSGROUPNAME = :PROCESSGROUPNAME "
				+ "      AND PR.PRODUCTREQUESTNAME = :PRODUCTREQUESTNAME "
				+ "      AND PR.MAINPRODUCTREQUESTNAME IS NULL "
				+ " ),"
				+ " CF_CST AS  "
				+ " ( "
				+ "     SELECT COUNT(P.PRODUCTNAME) AS PRODUCTQUANTITY "
				+ "     FROM PRODUCT P "
				+ "       INNER JOIN PRODUCTREQUEST PR ON P.PRODUCTREQUESTNAME = PR.PRODUCTREQUESTNAME "
				+ "     WHERE P.PROCESSGROUPNAME = :PROCESSGROUPNAME "
				+ "     AND PR.MAINPRODUCTREQUESTNAME = :PRODUCTREQUESTNAME "
				+ " ) "
				+ " SELECT 'ARRAY' AS GUBUN, PRODUCTQUANTITY FROM ARRAY_CST "
				+ " UNION "
				+ " SELECT 'CF' AS GUBUN, PRODUCTQUANTITY FROM CF_CST ";

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PROCESSGROUPNAME", groupID);
		bindMap.put("PRODUCTREQUESTNAME", productRequestName);

		List<Map<String, Object>> sqlResult = GenericServiceProxy
				.getSqlMesTemplate().queryForList(sql, bindMap);

		if (sqlResult.size() == 2) {
			int arrayProductQuantity = Integer.valueOf(sqlResult.get(0).get("PRODUCTQUANTITY").toString());
			int cfProductQuantity = Integer.valueOf(sqlResult.get(1).get("PRODUCTQUANTITY").toString());

			if (arrayProductQuantity != cfProductQuantity) {
				throw new CustomException("LOT-0237", arrayProductQuantity, cfProductQuantity);
			}
		} else {
			throw new CustomException("LOT-0240");
		}
	}
	
	private ProductRequest getCFProductRequestName(String arrayProductRequestName) throws CustomException
	{
		try
		{
			String sql = " SELECT PRODUCTREQUESTNAME "
					+ " FROM PRODUCTREQUEST "
					+ " WHERE 1=1 "
					+ " 	  AND MAINPRODUCTREQUESTNAME = :MAINPRODUCTREQUESTNAME "
					+ " 	  AND FACTORYNAME = :FACTORYNAME ";

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("MAINPRODUCTREQUESTNAME", arrayProductRequestName);
			bindMap.put("FACTORYNAME", "CELL");

			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			
			if(sqlResult.size() <= 0){
				throw new CustomException("PRODUCTREQUEST-0058", arrayProductRequestName); 
			}
			
			String cfProductReuqestName = sqlResult.get(0).get("PRODUCTREQUESTNAME").toString();

			ProductRequestKey prKey = new ProductRequestKey();
			prKey.setProductRequestName(cfProductReuqestName);

			ProductRequest prData = ProductRequestServiceProxy.getProductRequestService().selectByKey(prKey);

			return prData;
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-9001", "ProductRequest");
		}
	}
	
	private Lot ReleaseForSTB(Lot lotData, EventInfo eventInfo) throws CustomException
	{
//		List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();
//		List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotNameForUpdate(lotData.getKey().getLotName());
//		
//		for(Product product : productList)
//		{
//			ProductPGS productPGS = new ProductPGS();
//
//			productPGS.setProductName(product.getKey().getProductName());
//			
//			productPGS.setPosition(product.getPosition());
//			productPGS.setProductGrade(product.getProductGrade());
//			productPGS.setSubProductGrades1(product.getSubProductGrades1());
//			productPGS.setSubProductGrades2(product.getSubProductGrades2());
//			
//			productPGS.setSubProductQuantity1(product.getSubProductQuantity1());
//			productPGS.setSubProductQuantity2(product.getSubProductQuantity2());
//
//			productPGSSequence.add(productPGS);
//		}
//		
//		MakeReleasedInfo releaseInfo = MESLotServiceProxy.getLotInfoUtil()
//				.makeReleasedInfo(lotData, lotData.getAreaName(), lotData.getNodeStack(), lotData.getProcessFlowName(),
//						lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
//						lotData.getProductionType(), lotData.getUdfs(), "", lotData.getDueDate(), lotData.getPriority());
//
//		lotData = MESLotServiceProxy.getLotServiceImpl().releaseLot(eventInfo, lotData, releaseInfo, productPGSSequence);
		
		eventLog.info("Start Release");
		
		EventInfo eventInfoForSTB = EventInfoUtil.makeEventInfo("Release", eventInfo.getEventUser(),eventInfo.getEventComment(), null, null);
		eventInfoForSTB.setEventTime(eventInfo.getEventTime());
		eventInfoForSTB.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotData.getKey().getLotName());
		productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productUdfs, "");
		
		ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotData.getKey().getLotName(),
				lotData.getProductionType(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProductSpec2Name(),
				lotData.getProductSpec2Version(), lotData.getProductRequestName(), lotData.getSubProductUnitQuantity1(),
				lotData.getSubProductUnitQuantity2(), lotData.getDueDate(), lotData.getPriority(), lotData.getFactoryName(), lotData.getAreaName(),
				GenericServiceProxy.getConstantMap().Lot_Released, GenericServiceProxy.getConstantMap().Lot_Wait,
				GenericServiceProxy.getConstantMap().Prq_NotOnHold, lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(),
				lotData.getProcessOperationVersion(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), "", "", lotData.getNodeStack(),
				lotData.getUdfs(), productUdfs, true);
		
		lotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfoForSTB, lotData, changeSpecInfo);
		
		return lotData;
	}

	private Lot TrackInForSTB(Lot lotData, EventInfo eventInfo) throws CustomException
	{
		eventLog.info("Start TrackInForSTB");
		
		EventInfo eventInfoForSTB = EventInfoUtil.makeEventInfo("TrackIn", eventInfo.getEventUser(),eventInfo.getEventComment(), null, null);
		eventInfoForSTB.setEventTime(eventInfo.getEventTime());
		eventInfoForSTB.setEventTimeKey(eventInfo.getEventTimeKey());
    	
    	List<ProductC> productCSequence = MESLotServiceProxy.getLotInfoUtil().setProductCSequence(lotData.getKey().getLotName());
		
    	Map<String, String> lotUdfs = new HashMap();

    	lotUdfs.put("NOTE", "");
		lotUdfs.put("HOLDDURATION", "");
		lotUdfs.put("HOLDRELEASETIME", "");

		MakeLoggedInInfo makeLoggedInInfo = MESLotServiceProxy.getLotInfoUtil().makeLoggedInInfoExceptMac(productCSequence,lotUdfs);
		Lot afterTrackInLot;
		
		try 
		{
			afterTrackInLot = MESLotServiceProxy.getLotServiceImpl().trackInLotForOPI(eventInfoForSTB, lotData, makeLoggedInInfo);
		} 
		catch (CustomException e) 
		{
			throw new CustomException("COMMON-0001",e.toString());
		}
		
		eventLog.info("End TrackInForSTB");

    	return afterTrackInLot;
    }
	
	private Lot TrackOutForSTB(Lot lotData, EventInfo eventInfo) throws CustomException
	{
		eventLog.info("Start TrackOutForSTB");
		    	
		List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(lotData.getKey().getLotName());
		Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
		Map<String, String> assignCarrierUdfs = new HashMap<String, String>();
		
   		boolean aHoldFlag = false;
   		
   		aHoldFlag = MESLotServiceProxy.getLotServiceUtil().isExistAhold(lotData.getKey().getLotName(), lotData.getProcessFlowName(), lotData.getProcessOperationName());
		
		Lot afterTrackOutLot = null;
		
		try 
		{
			afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().trackOutLot(eventInfo, lotData, null, lotData.getCarrierName(), "", lotData.getMachineName(), "",productPGSRCSequence, assignCarrierUdfs, deassignCarrierUdfs, "", aHoldFlag,null);	
		} 
		catch (Exception e) 
		{
			eventLog.error( lotData.getKey().getLotName() +" TrackOut Error ");
			throw new CustomException("COMMON-0001",e.toString());
		}
		
		eventLog.info("End TrackOutForSTB");

    	return afterTrackOutLot;
    }
}
