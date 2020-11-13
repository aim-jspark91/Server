package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.PanelJudge;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.MakeShippedInfo;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ShipLot extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		Element lotList = SMessageUtil.getBodySequenceItem(doc, "LOTLIST", true);
		List<Product> productList = null;
		
		if (lotList != null)
		{
			for ( @SuppressWarnings("rawtypes")
			Iterator iteratorLotList = lotList.getChildren().iterator(); iteratorLotList.hasNext();)
			{
				Element lotE = (Element) iteratorLotList.next();
				
				String factoryName = SMessageUtil.getChildText(lotE, "FACTORYNAME", true);
				String processFlowName = SMessageUtil.getChildText(lotE, "PROCESSFLOWNAME", true);
				String processOperationName = SMessageUtil.getChildText(lotE, "PROCESSOPERATIONNAME", true);
				String lotName        = SMessageUtil.getChildText(lotE, "LOTNAME", true);
				String destFactoryName = SMessageUtil.getChildText(lotE, "DESTINATIONNFACTORY", true);
				String destAreaName = SMessageUtil.getChildText(lotE, "DESTINATIONAREANAME", false);
				//Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
				
				// Modified by GJJ mantis:5725 20200309  Lot should be locked to be prevented concurrent executing.
				eventLog.debug("Lot will be locked to be prevented concurrent executing.");
				Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
				eventLog.debug("Lot is locked to be prevented concurrent executing.");
				
				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());

				if(StringUtil.equals(lotData.getLotHoldState(), "Y"))
				{
					throw new CustomException("LOT-9048");
				}

				if(StringUtil.equals(durableData.getUdfs().get("DURABLEHOLDSTATE").toString(),"Y"))
				{
					throw new CustomException("DURABLE-9001");
				}
				//not use completed State
				//CommonValidation.checkLotCompletedState(lotData);
				CommonValidation.checkProcessFlowTypeIsSort(lotData);
				
			//	MESLotServiceProxy.getLotServiceUtil().checkThresHoldRatio(lotData);
				
				// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotName);
				productList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotName);
				
				String sGradeproductList = "";
				for(Product product : productList){
					if( StringUtil.equals(GenericServiceProxy.getConstantMap().ProductGrade_S, product.getProductGrade())){
						sGradeproductList+=product.getKey().getProductName() + " ";
					}
				}
				
				String nGradeproductList = "";
				for(Product product : productList){
					if( StringUtil.equals(GenericServiceProxy.getConstantMap().ProductGrade_N, product.getProductGrade()) ){
						nGradeproductList+=product.getKey().getProductName() + " ";
					}
				}

				for (Product productData : productList )
				{
					if(StringUtils.isNotEmpty(productData.getCarrierName()))
					{
						if(StringUtils.isNotEmpty(sGradeproductList.trim()))
						     throw new CustomException("PRODUCT-9005", sGradeproductList, GenericServiceProxy.getConstantMap().ProductGrade_S);

						if(StringUtils.isNotEmpty(nGradeproductList.trim()))
						     throw new CustomException("PRODUCT-9005", nGradeproductList, GenericServiceProxy.getConstantMap().ProductGrade_N);
					}
					
				  
					List<PanelJudge> panelJudgeList = new ArrayList<PanelJudge>();
					
	                //String conditionForPanelJudge = "WHERE glassName = ? "; modfiy by GJJ 20200309 mantis:5725
	                String conditionForPanelJudge = "WHERE glassName = ? and PANELJUDGE='R'  ";
	                Object[] bindSet = new Object[]{ productData.getKey().getProductName() };
	                
	                try
	                {
	                	panelJudgeList = ExtendedObjectProxy.getPanelJudgeService().select(conditionForPanelJudge, bindSet);
	                }
	                catch(Throwable e)
	                {
//	                	eventLog.warn("Not PanelList - GlassName [" + productData.getKey().getProductName() + "]");
	                	eventLog.info(" PanelJudge 'R' Number is zero. ");
	                }
	                
//	                if(panelJudgeList.size()>0) modfiy by GJJ 20200309 mantis:5725
//	                {
//	                    for(PanelJudge panelJudge : panelJudgeList)
//	                    {
//	                    	if(StringUtils.equals(panelJudge.getPanelJudge(), "R"))
//	                    	{
//	                    		throw new CustomException("PANEL-0002", panelJudge.getPanelName(), productData.getKey().getProductName());
//	                    	}
//	                    }
//	                }
					if (panelJudgeList.size() > 0) //modfiy by GJJ 20200309 mantis:5725 start 
					{
						throw new CustomException("PANEL-0002", panelJudgeList.get(0).getPanelName(), productData.getKey().getProductName());
					}
					//modfiy by GJJ 20200309 mantis:5725 end 
				}	
				
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
				
				//20180504, kyjung, QTime
				MESProductServiceProxy.getProductServiceImpl().checkQTime(lotName);
				
				//2019.01.31_hsryu_Check PanelJudge Definition. Mantis 0002640.
				MESLotServiceProxy.getLotServiceUtil().checkPanelJudgeInDummyOperation(lotData);
				
				//input Bank Operation
				ProcessOperationSpec nextProcessOperation = new ProcessOperationSpec();
                try
                {
                	nextProcessOperation = CommonUtil.getNextOperation(factoryName, processFlowName, processOperationName);
                }
                catch(Throwable e)
                {
                	nextProcessOperation = CommonUtil.getProcessOperationSpec(lotData);
                }
				
				String nextNodeStack = CommonUtil.getNodeStack(factoryName, processFlowName, nextProcessOperation.getKey().getProcessOperationName());
				String bankName = nextProcessOperation.getKey().getProcessOperationName();
				lotData.setProcessOperationName(bankName);
				lotData.setNodeStack(nextNodeStack);
				LotServiceProxy.getLotService().update(lotData);
				
				//20180504, kyjung, QTime
				for (Product productData : productList )
				{   
/*					//2018-09-13, PARK JEONG SU, Modify OperationName and OperationVersion
					productData.setProcessOperationName("-");
					productData.setProcessOperationVersion("-");
					MESProductServiceProxy.getProductServiceImpl().ExitedQTime(eventInfo, productData, "Ship");
					
					//2019.05.06 dmlee : Close All Qtime Data (Mantis 3759)
					MESProductServiceProxy.getProductServiceImpl().closeAllQTime(eventInfo, productData);*/
					
					//input Bank Operation
					productData.setProcessOperationName(bankName);
					productData.setProcessOperationVersion("0001");
					productData.setNodeStack(nextNodeStack);
					ProductServiceProxy.getProductService().update(productData);
				}	
				
				CheckTurnFlag(productList);
				
			
			
				List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotName);
			
				MakeShippedInfo makeShippedInfo = MESLotServiceProxy.getLotInfoUtil().makeShippedInfo(lotData, destAreaName, "", bankName, destFactoryName, productUdfs );
				eventInfo = EventInfoUtil.makeEventInfo("Ship", getEventUser(), getEventComment(), "", "");

				Lot aLot = MESLotServiceProxy.getLotServiceImpl().shipLot(eventInfo, lotData, makeShippedInfo);
				
				//Increment Work Order Finished Quantity
				//MESWorkOrderServiceProxy.getProductRequestServiceUtil().calculateProductRequestQty(aLot.getProductRequestName(), null, "F", (long)aLot.getProductQuantity(), eventInfo);
				
				//20180504, kyjung, QTime
				eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
				EventInfo eventInfo1 = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
				List<Map<String, Object>> queuePolicyData = null;
				aLot.setProcessOperationName("-");
				aLot.setProcessOperationVersion("-");
				Map<String, Object> qTimeTPEFOPolicyData = MESProductServiceProxy.getProductServiceImpl().checkPriorityPolicy(aLot);
				
				if(qTimeTPEFOPolicyData != null)
				{
					queuePolicyData = MESProductServiceProxy.getProductServiceImpl().checkQTimePolicy(qTimeTPEFOPolicyData);
					
					if(queuePolicyData != null)
					{
						for (Product productData : productList )
						{   
							MESProductServiceProxy.getProductServiceImpl().EnteredQTime(eventInfo, eventInfo1, productData.getKey().getProductName(), queuePolicyData);
						}
					}
				}
				
				Map<String, String> updateUdfs = new HashMap<String, String>();
				updateUdfs.put("NOTE", "");
				MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
			}
		} 
		
		return doc;
	}
	
	//table  
	public static void CheckQtime(Lot lotData,String factoryName, String toFactoryName) throws CustomException
	{
		
			String sql = "SELECT QUEUETIMESTATE FROM CT_LOTQUEUETIME "
					+ "WHERE LOTNAME = :lotname AND FACTORYNAME = :factoryname AND TOFACTORYNAME = :tofactoryname";
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("lotname", lotData.getKey().getLotName());
			bindMap.put("factoryname", factoryName);
			bindMap.put("tofactoryname", toFactoryName);
			
			List<Map<String, Object>> sqlResult = 
					GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

			if ( sqlResult.size() > 0)
			{
				String queuetimeState = (String)sqlResult.get(0).get("QUEUETIMESTATE");	
				if(queuetimeState.equals("Interlocked"))
				{
					//will insert errorcode in ct_nlsdata 
					throw new CustomException("LOT-9017", lotData.getKey().getLotName()); 
				}
			}
	}
	
	public static void CheckTurnFlag(List<Product> productList) throws CustomException
	{
		ArrayList<String> arrPrdName = new ArrayList<String>();
		
		for(Product productName : productList)
		{
				// TurnFlag column is not exist. will insert col.
				// No mentis list , 2019.03.12 by shkim , PRODUCT >> CT_PRODUCTFLAG
				String sql = "SELECT PROCESSTURNFLAG TURNSIDEFLAG, TURNOVERFLAG FROM CT_PRODUCTFLAG "
						+ "WHERE PRODUCTNAME = :productname ";
				
				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("productname", productName.getKey().getProductName());
				
				List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
				
				if(sqlResult.size() > 0)
				{
					String turnSideFlag = (String)sqlResult.get(0).get("TURNSIDEFLAG");	
					String turnOverFlag = (String)sqlResult.get(0).get("TURNOVERFLAG");
					
					if(!StringUtil.isEmpty(turnSideFlag)||!StringUtil.isEmpty(turnOverFlag))
					{
						if(StringUtil.equals(turnSideFlag, "Y")||StringUtil.equals(turnOverFlag, "Y"))
						{
							arrPrdName.add(productName.getKey().getProductName());
						}

					}
				}
		}
		
		if(arrPrdName.size()>0)
		{
			String existTurnFlagProductList = "";
			
			for(int i=0;i<arrPrdName.size();i++)
			{
				existTurnFlagProductList += "[" + arrPrdName.get(i).toString() + "]";
			}
		
			throw new CustomException("TURN-0001", existTurnFlagProductList); 
		}
	}
	
}
