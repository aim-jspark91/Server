package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;

import org.jdom.Document;
import org.jdom.Element;

public class AssignWorkOrderForBank extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		Element lotList = SMessageUtil.getBodySequenceItem(doc, "LOTLIST", true);
		String sProductRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		String sProductSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String sFactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		// 2019.06.04_hsryu_Delete Receive ECCode & ProcessFlow. after AssignWorkOrderForBank, Same ECCode and Flow. 
		//String sECCode = SMessageUtil.getBodyItemValue(doc, "ECCODE", true);
		//String sProcessFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);

		ProductRequestKey pKey = new ProductRequestKey();
		pKey.setProductRequestName(sProductRequestName);
		
		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		ProductRequest pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
		ProductRequest pData = ProductRequestServiceProxy.getProductRequestService().selectByKeyForUpdate(pKey);		
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("MoveFromEndBank", this.getEventUser(), this.getEventComment(), "", "");
		
		int totalReleaseQty = 0;
		
		if (lotList != null)
		{
			for ( @SuppressWarnings("rawtypes")
			Iterator iteratorLotList = lotList.getChildren().iterator(); iteratorLotList.hasNext();)
			{
				Element lotE = (Element) iteratorLotList.next();
				String lotName        = SMessageUtil.getChildText(lotE, "LOTNAME", true);
				eventInfo = EventInfoUtil.makeEventInfo("MoveFromEndBank", this.getEventUser(), this.getEventComment(), "", "");
				
				// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
				Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
				
				// 2019.06.04_hsryu_Missd Logic. Add Validation. 
				CommonValidation.checkLotHoldState(lotData);
				
				if(!StringUtil.equals(lotData.getProductSpecName(), pData.getUdfs().get("crateSpecName")))
				{
					throw new CustomException("BRANCH-0002",lotData.getProductSpecName(), pData.getKey().getProductRequestName(), pData.getUdfs().get("crateSpecName"));
				}
				
				// 2019.06.04_hsryu_Momory ProcessFlow and ECCode. 
				String processFlowName = lotData.getProcessFlowName();
				String ecCode = lotData.getUdfs().get("ECCODE");
				
				this.checkIsProductPossiblePF(sFactoryName, sProductSpecName, ecCode, processFlowName);
				
				String lastOperForToFlow = CommonUtil.getLastOperation(sFactoryName, processFlowName);
				String nodeStack = CommonUtil.getNodeStack(sFactoryName, processFlowName, lastOperForToFlow);	
				ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(sFactoryName, lastOperForToFlow);

				// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				List<Product> pProductList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
				List<Product> pProductList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());
				
				totalReleaseQty += pProductList.size();

				List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotName);
				
				//Operation Changed, Update Product ProcessingInfo to N
				productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productUdfs, "");

				// MBEP..
				lotData.setLotState(GenericServiceProxy.getConstantMap().Lot_Released);
				lotData.setLotHoldState(GenericServiceProxy.getConstantMap().Prq_NotOnHold);
				lotData.setLotProcessState(GenericServiceProxy.getConstantMap().Lot_Wait);
				
				SetEventInfo setEventInfo = new SetEventInfo();
				
				LotServiceProxy.getLotService().update(lotData);
				LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
				
				for(Product product : pProductList)
				{
					product.setProductState(GenericServiceProxy.getConstantMap().Prod_InProduction);
					product.setProductHoldState(GenericServiceProxy.getConstantMap().Prq_NotOnHold);
					product.setProductProcessState(GenericServiceProxy.getConstantMap().Prod_Idle);
					
					ProductServiceProxy.getProductService().update(product);
					
					kr.co.aim.greentrack.product.management.info.SetEventInfo setProductEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
					ProductServiceProxy.getProductService().setEvent(product.getKey(), eventInfo, setProductEventInfo);
				}
				
				// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
				lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
				
				//Assign WO For EndBank
				eventInfo = EventInfoUtil.makeEventInfo("AssignWorkOrderForBank", this.getEventUser(), this.getEventComment(), "", "");

				lotData.setProductRequestName(sProductRequestName);
				lotData.setProductSpecName(sProductSpecName);
				// 2019.06.21_hsryu_Delete setUdfs.  other udfs must not be changed.
//				lotUdfs.put("ECCODE", ecCode);
//				lotData.setUdfs(lotUdfs);
				
				LotServiceProxy.getLotService().update(lotData);

				SetEventInfo setEventInfo2 = new SetEventInfo();
				// 2019.06.21_hsryu_Delete setUdfs.  other udfs must not be changed.
				//setEventInfo2.setUdfs(lotUdfs);
				setEventInfo2.getUdfs().put("ECCODE", ecCode);
				LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo2);
				
				for(Product product : pProductList)
				{
					ProductKey productKey = new ProductKey(product.getKey().getProductName());
					
					// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					product = ProductServiceProxy.getProductService().selectByKey(productKey);
					product = ProductServiceProxy.getProductService().selectByKeyForUpdate(productKey);
					
					product.setProductRequestName(sProductRequestName);
					product.setProductSpecName(sProductSpecName);
					product.setProductProcessState(GenericServiceProxy.getConstantMap().Prod_Idle);
					
					// 2019.06.21_hsryu_Delete setUdfs.  other udfs must not be changed.
					//prdUdfs.put("ECCODE", ecCode);
					
					ProductServiceProxy.getProductService().update(product);
					
					kr.co.aim.greentrack.product.management.info.SetEventInfo setProductEventInfo2 = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
					//setProductEventInfo2.setUdfs(prdUdfs);
					setProductEventInfo2.getUdfs().put("ECCODE", ecCode);

					ProductServiceProxy.getProductService().setEvent(product.getKey(), eventInfo, setProductEventInfo2);
				}
				
				// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
				lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));

				//Move to Flow and First Operation
				eventInfo = EventInfoUtil.makeEventInfo("OperationLocate", this.getEventUser(), this.getEventComment(), "", "");

				lotData.setAreaName(operationData.getDefaultAreaName());
				lotData.setNodeStack(nodeStack);
				lotData.setProcessFlowName(processFlowName);
				lotData.setProcessFlowVersion("00001");
				lotData.setProcessOperationName(lastOperForToFlow);
				lotData.setProcessOperationVersion("00001");
				
				// 2019.06.21_hsryu_Delete setUdfs.  other udfs must not be changed.
//				lotUdfs.put("ENDBANK", "");
//				lotData.setUdfs(lotUdfs);

				LotServiceProxy.getLotService().update(lotData);

				SetEventInfo setEventInfo3 = new SetEventInfo();
				setEventInfo3.getUdfs().put("ENDBANK", "");
				// 2019.06.21_hsryu_Delete setUdfs.  other udfs must not be changed.
				//setEventInfo3.setUdfs(lotUdfs);

				LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo3);
				
				for(Product product : pProductList)
				{
					ProductKey productKey = new ProductKey(product.getKey().getProductName());
					
					// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					product = ProductServiceProxy.getProductService().selectByKey(productKey);
					product = ProductServiceProxy.getProductService().selectByKeyForUpdate(productKey);

					product.setAreaName(operationData.getDefaultAreaName());
					product.setNodeStack(nodeStack);
					product.setProcessFlowName(processFlowName);
					product.setProcessFlowVersion("00001");
					product.setProcessOperationName(lastOperForToFlow);
					product.setProcessOperationVersion("00001");
					
					ProductServiceProxy.getProductService().update(product);
					
					kr.co.aim.greentrack.product.management.info.SetEventInfo setProductEventInfo3 = new kr.co.aim.greentrack.product.management.info.SetEventInfo();

					ProductServiceProxy.getProductService().setEvent(product.getKey(), eventInfo, setProductEventInfo3);
				}
				
				lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
				
				
				// HOLD...
				EventInfo holdEventInfo = EventInfoUtil.makeEventInfo("Hold", eventInfo.getEventUser(), "Hold by AssignWOForEndBank", GenericServiceProxy.getConstantMap().REASONCODETYPE_HOLDLOT, "RWHL");

				List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
				MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo(productUSequence);
//				String holdDepartment = MESLotServiceProxy.getLotServiceImpl().getHoldDepartment(lotData.getKey().getLotName());
//				makeOnHoldInfo.getUdfs().put("HOLDDEPARTMENT", holdDepartment);
				LotServiceProxy.getLotService().makeOnHold(lotData.getKey(), holdEventInfo, makeOnHoldInfo);

				// -------------------------------------------------------------------------------------------------------------------------------------------
				// Modified by smkang on 2018.08.13 - According to user's requirement, LotName/ReasonCode/Department/EventComment are necessary to be keys.
//				Map<String,String> multiHoldudfs = new HashMap<String, String>();
//				//2018.05.09 dmlee : To Be Modify EventUserDep
//				multiHoldudfs.put("eventuserdep", "MFG");
//
//				LotMultiHoldKey multiholdkey = new LotMultiHoldKey();
//				multiholdkey.setLotName(lotData.getKey().getLotName());
//				multiholdkey.setReasonCode("RWHL");
//
//				LotMultiHold multihold = LotServiceProxy.getLotMultiHoldService().selectByKey(multiholdkey);
//				multihold.setUdfs(multiHoldudfs);
//				
//				LotServiceProxy.getLotMultiHoldService().update(multihold);
				try {
					lotData = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(lotData.getKey().getLotName(), "RWHL", "MFG","AHOLD", holdEventInfo);
				} catch (Exception e) {
					eventLog.warn(e);
				}
				// -------------------------------------------------------------------------------------------------------------------------------------------
			}
			
			eventInfo.setEventName("Increment");

			pData.setProductRequestState(GenericServiceProxy.getConstantMap().Prq_Released);

			if(pData.getReleasedQuantity()+totalReleaseQty == pData.getPlanQuantity())
			{
				pData.setProductRequestState(GenericServiceProxy.getConstantMap().Prq_Completed);
			}
			pData.setReleasedQuantity(pData.getReleasedQuantity()+totalReleaseQty);
			pData.setLastEventName(eventInfo.getEventName());
			pData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			pData.setLastEventTime(eventInfo.getEventTime());
			pData.setLastEventUser(eventInfo.getEventUser());
			pData.setLastEventComment(eventInfo.getEventComment());
			
			ProductRequestServiceProxy.getProductRequestService().update(pData);

			//  Add History
			MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(pData, eventInfo);
			
		} 

		return doc;

	}

	private String insertEndBank(Lot trackOutLot, String productRequestName, String ProductionType)
	{
		
		//ProductRequest Key & Data
//		ProductRequestKey pKey = new ProductRequestKey();
//		pKey.setProductRequestName(productRequestName);
//		ProductRequest pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
//		
//		String sql = "SELECT ENUMVALUE FROM ENUMDEFVALUE "
//				+ "WHERE ENUMNAME = :ENUMNAME ";
//		
//		Map<String, Object> bindMap = new HashMap<String, Object>();
//		bindMap.put("ENUMNAME", pData.getProductRequestType());
//		
//		List<Map<String, Object>> sqlResult = 
//				GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
//		
//		return sqlResult.get(0).get("ENUMVALUE").toString();
		
		//ProductRequest Key & Data
		ProductRequestKey pKey = new ProductRequestKey();
		pKey.setProductRequestName(productRequestName);
		ProductRequest pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
		String sql = "";
		Map<String, Object> bindMap = new HashMap<String, Object>();
		if(StringUtil.equals(ProductionType, "DMQC"))
		{
			sql = "SELECT ENUMVALUE FROM ENUMDEFVALUE "
					+ "WHERE ENUMNAME = :ENUMNAME AND TAG = :TAG";	
			bindMap.put("ENUMNAME", pData.getProductRequestType());
			bindMap.put("TAG", ProductionType);
		}
		else {
			sql = "SELECT ENUMVALUE FROM ENUMDEFVALUE "
					+ "WHERE ENUMNAME = :ENUMNAME ";	
			bindMap.put("ENUMNAME", pData.getProductRequestType());
		}
		
		List<Map<String, Object>> sqlResult = 
				GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return sqlResult.get(0).get("ENUMVALUE").toString();
	}
	
	private void checkIsProductPossiblePF(String factoryName, String productSpecName, String ecCode, String processFlowName )  throws CustomException
	{
	    String Sql = StringUtil.EMPTY;
        Sql = Sql + " SELECT PSPF.*                                        \n"
                  + "   FROM CT_PRODUCTSPECPOSSIBLEPF PSPF                 \n"
                  + "  WHERE 1=1                                           \n"
                  + "    AND PSPF.FACTORYNAME = :FACTORYNAME               \n"
                  + "    AND PSPF.PRODUCTSPECNAME =:PRODUCTSPECNAME        \n"
                  + "    AND PSPF.ECCODE = :ECCODE                         \n"
        		  + "    AND PSPF.PROCESSFLOWNAME = :PROCESSFLOWNAME       ";

        Map<String, Object> bindMap = new HashMap<String, Object>();
        bindMap.put("FACTORYNAME", factoryName);
        bindMap.put("PRODUCTSPECNAME", productSpecName);
        bindMap.put("ECCODE", ecCode);
        bindMap.put("PROCESSFLOWNAME", processFlowName);

        List<Map<String, Object>> productSpecPossiblePfList = GenericServiceProxy.getSqlMesTemplate().queryForList(Sql, bindMap);
        
        if ( productSpecPossiblePfList == null || productSpecPossiblePfList.size() <= 0 )
        {
            //throw new CustomException("POLICY-0022",GenericServiceProxy.getConstantMap().DEFAULT_FACTORY,unitName,operationMode);
            throw new CustomException("CRATE-9005",factoryName, productSpecName, ecCode, processFlowName);
        }
	}
}
