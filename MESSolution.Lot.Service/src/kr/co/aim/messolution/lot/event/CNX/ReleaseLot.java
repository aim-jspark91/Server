package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.GradeDefUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpecKey;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.MakeReleasedInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequestplan.ProductRequestPlanServiceProxy;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlan;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlanKey;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ReleaseLot extends SyncHandler {
	
	/**
	 * 151106 by xzquan : Create ReleaseProductRequest
	 */
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String sFactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String sProductRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "ASSIGNEDMACHINENAME", true);
		String sLoadPort = SMessageUtil.getBodyItemValue(doc, "LOADPORT", true);
		String sUnLoadPort = SMessageUtil.getBodyItemValue(doc, "UNLOADPORT", true);
		String sPlanReleasedTime = SMessageUtil.getBodyItemValue(doc, "PLANRELEASEDTIME", true);
		String sPriority = SMessageUtil.getBodyItemValue(doc, "PRIORITY", true);
		Element eLotList = SMessageUtil.getBodySequenceItem(doc, "LOTLIST", true);

		List<Lot> successLotList = new ArrayList<Lot>();
		int LotNum=0;
		
		//Product Request Key & Data
		ProductRequestKey pKey = new ProductRequestKey();
		pKey.setProductRequestName(sProductRequestName);
		ProductRequest pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
		
		//Product Request Plan Key & Data
		ProductRequestPlanKey pPlanKey = new ProductRequestPlanKey();
		pPlanKey.setProductRequestName(sProductRequestName);
		pPlanKey.setAssignedMachineName(sMachineName);
		pPlanKey.setPlanReleasedTime(TimeUtils.getTimestamp(sPlanReleasedTime));
		ProductRequestPlan pPlanData = ProductRequestPlanServiceProxy.getProductRequestPlanService().selectByKey(pPlanKey);
		
		//2019.01.07_hsryu_add Validation. Mantis 2263
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT (TO_CHAR(A.PLANRELEASEDTIME, :DATEFORMAT)||:TIME) INPUTTIME, TO_CHAR (SYSDATE, :DATETIMEFORMAT) CURRENTTIME, A.* FROM PRODUCTREQUESTPLAN A");
		sql.append(" WHERE PRODUCTREQUESTNAME = :PRODUCTREQUESTNAME");
		sql.append("   AND ASSIGNEDMACHINENAME = :ASSIGNEDMACHINENAME");
		sql.append("   AND PLANRELEASEDTIME = :PLANRELEASEDTIME");
		sql.append("   AND (TO_CHAR(A.PLANRELEASEDTIME, :DATEFORMAT)||:TIME) > TO_CHAR (SYSDATE, :DATETIMEFORMAT)");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PRODUCTREQUESTNAME", sProductRequestName);
		bindMap.put("ASSIGNEDMACHINENAME", sMachineName);
		bindMap.put("PLANRELEASEDTIME", TimeUtils.getTimestamp(sPlanReleasedTime));
		bindMap.put("DATEFORMAT", "YYYYMMDD");
		bindMap.put("DATETIMEFORMAT", "YYYYMMDDHH24MISS");
		bindMap.put("TIME", "070000");

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		
		if ( sqlResult.size() > 0 )
		{
			String inputTime = sqlResult.get(0).get("INPUTTIME").toString();
			throw new CustomException("PLAN-0001",inputTime);
		}
		
		//2019.01.07_hsryu_Delete Logic. 
//		String checkDateSql = "SELECT PLANRELEASEDTIME, SYSDATE FROM PRODUCTREQUESTPLAN "
//				+ " WHERE PRODUCTREQUESTNAME = :PRODUCTREQUESTNAME "
//				+ " AND ASSIGNEDMACHINENAME = :ASSIGNEDMACHINENAME "
//				+ " AND PLANRELEASEDTIME = :PLANRELEASEDTIME "
//				+ " AND PLANRELEASEDTIME > SYSDATE ";
//
//		Map<String, Object> bindSet = new HashMap<String, Object>();
//		bindSet.put("PRODUCTREQUESTNAME", sProductRequestName);
//		bindSet.put("ASSIGNEDMACHINENAME", sMachineName);
//		bindSet.put("PLANRELEASEDTIME", TimeUtils.getTimestamp(sPlanReleasedTime));
//
//		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(checkDateSql, bindSet);
//
//		if ( sqlResult.size() > 0 )
//		{
//			throw new CustomException("PRODUCTREQUEST-0050");
//		}
		
		//Product Spec Data
		ProductSpec specData = GenericServiceProxy.getSpecUtil().getProductSpec(sFactoryName, pData.getProductSpecName(),
									GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
		
		String cutType = specData.getUdfs().get("CUTTYPE");
		
		//Machine Data
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sMachineName);

		//1. Velidation
		//1) Check Product Request Plan Hold State
		if(pPlanData.getProductRequestPlanHoldState().equals(GenericServiceProxy.getConstantMap().Prq_OnHold))
		{
			throw new CustomException("PRODUCTREQUEST-0001", sProductRequestName);
		}
		
		//2) Chek Product Request Plan State
		if(pPlanData.getProductRequestPlanState().equals(GenericServiceProxy.getConstantMap().PrqPlan_Completed))
		{
			throw new CustomException("PRODUCTREQUEST-0007", sProductRequestName, sMachineName, sPlanReleasedTime);
		}
		
		String insertSql = " INSERT INTO CT_PANELJUDGE "
				+ " (PANELNAME, PANELJUDGE, PANELGRADE, XAXIS1, YAXIS1, XAXIS2, YAXIS2, "
				+ " GLASSNAME, HQGLASSNAME, CUTTYPE, LASTEVENTNAME, LASTEVENTUSER, LASTEVENTTIME, LASTEVENTCOMMENT, PRODUCTSPECTYPE)"
				+ " VALUES "
				+ " (:PANELNAME, :PANELJUDGE, :PANELGRADE, NVL(:XAXIS1,0), NVL(:YAXIS1,0), NVL(:XAXIS2,0), NVL(:YAXIS2,0), "
				+ "  :GLASSNAME, :HQGLASSNAME, :CUTTYPE, :LASTEVENTNAME, :LASTEVENTUSER, TO_DATE(:LASTEVENTTIME,'yyyy-MM-dd HH24:mi:ss'), :LASTEVENTCOMMENT, :PRODUCTSPECTYPE)";
		
		String insertSqlForHQGlass = " INSERT INTO CT_HQGLASSJUDGE "
				+ " (HQGLASSNAME, HQGLASSJUDGE, XAXIS, YAXIS, GLASSNAME, "
				+ " LASTEVENTNAME, LASTEVENTUSER, LASTEVENTTIME, LASTEVENTCOMMENT)"
				+ " VALUES "
				+ " (:HQGLASSNAME, :HQGLASSJUDGE, NVL(:XAXIS,0), NVL(:YAXIS,0), :GLASSNAME, "
				+ " :LASTEVENTNAME, :LASTEVENTUSER, TO_DATE(:LASTEVENTTIME,'yyyy-MM-dd HH24:mi:ss'), :LASTEVENTCOMMENT)";
		
		for (@SuppressWarnings("rawtypes")
		Iterator iLot = eLotList.getChildren().iterator(); iLot.hasNext();)
		{
			double originalCreateQty = 0;
			
			Element eLot = (Element) iLot.next();
			String sLotName = SMessageUtil.getChildText(eLot, "LOTNAME", true);
			String sCarrierName = SMessageUtil.getChildText(eLot, "DURABLENAME", true);
			String crateName = SMessageUtil.getChildText(eLot, "CONSUMABLENAME", true);
			
			//1. Check Exist Carrier
			CommonValidation.checkExistCarrier(sCarrierName);
			
			//2. Check Empty Carrier
			CommonValidation.checkEmptyCst(sCarrierName);
			
			//20170121 Add by yudan
			Consumable crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(crateName);
			if (crateData.getUdfs().get("CONSUMABLEHOLDSTATE").equals("Y"))
			{
				throw new CustomException("CRATE-0008", crateName);
			}
			
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Release", getEventUser(), getEventComment(), "", "");
			
			//1. Release Lot
			//1.1) get lot Data by Lot Name
			LotKey lotKey = new LotKey(sLotName);
			
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			Lot lotData = LotServiceProxy.getLotService().selectByKey(lotKey);
			Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(lotKey);
			
			//1.2) get PGSSequance
			List<Element> eProductListElement = eLot.getChildren("PRODUCTLIST");
			Element eProductList = eProductListElement.get(0);
			List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();
			List<Object[]> insertArgList = new ArrayList<Object[]>();
			List<Object[]> insertArgListForHQGlass = new ArrayList<Object[]>();
			
			for (@SuppressWarnings("rawtypes")
			Iterator iProduct = eProductList.getChildren().iterator(); iProduct.hasNext();)
			{
				Element eProduct = (Element) iProduct.next();
				
				String sProductName = SMessageUtil.getChildText(eProduct, "PRODUCTNAME", true);
				String sPosition = SMessageUtil.getChildText(eProduct, "POSITION", true);
				String sCrateName = SMessageUtil.getChildText(eProduct, "CONSUMABLENAME", true);
										
				ProductPGS productInfo = new ProductPGS();
				productInfo.setPosition(Long.parseLong(sPosition));
				productInfo.setProductGrade(GradeDefUtil.getGrade(GenericServiceProxy.getConstantMap().DEFAULT_FACTORY,
																	GenericServiceProxy.getConstantMap().GradeType_Product, true).getGrade());
				productInfo.setProductName(sProductName);
				productInfo.setSubProductGrades1("");
				productInfo.setSubProductQuantity1(specData.getSubProductUnitQuantity1());
				productInfo.getUdfs().put("CRATENAME", sCrateName);
    			// Start 2019.09.11 Modfiy By Park Jeong Su Mantis 4706
    			try {
    				crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(sCrateName);
    				productInfo.getUdfs().put("CONSUMABLESPECNAME", crateData.getConsumableSpecName());
				} catch (Exception e) {
					eventLog.info("crateData is Not Found");
				}
    			// End 2019.09.11 Modfiy By Park Jeong Su Mantis 4706
				productPGSSequence.add(productInfo);
				
				try
				{
					//2019.08.01 dmlee : Half and Quarter Same logic
					if ( StringUtil.equals(cutType, GenericServiceProxy.getConstantMap().CUTTYPE_HALF) || StringUtil.equals(cutType, GenericServiceProxy.getConstantMap().CUTTYPE_QUARTER) )
					{
						int cut1XaxisCount = StringUtils.isNumeric(specData.getUdfs().get("CUT1XAXISCOUNT")) ? Integer.parseInt(specData.getUdfs().get("CUT1XAXISCOUNT")) : 0;
						int cut2XaxisCount = StringUtils.isNumeric(specData.getUdfs().get("CUT2XAXISCOUNT")) ? Integer.parseInt(specData.getUdfs().get("CUT2XAXISCOUNT")) : 0;
						int cut1YaxisCount = StringUtils.isNumeric(specData.getUdfs().get("CUT1YAXISCOUNT")) ? Integer.parseInt(specData.getUdfs().get("CUT1YAXISCOUNT")) : 0;
						int cut2YaxisCount = StringUtils.isNumeric(specData.getUdfs().get("CUT2YAXISCOUNT")) ? Integer.parseInt(specData.getUdfs().get("CUT2YAXISCOUNT")) : 0;
						
						for ( int i = 1; i < 3; i++ )
						{
							if(i==1)
							{
								// for PanelJudge
								for(int x=0; x<cut1XaxisCount; x++)
								{
									for(int y=0; y< cut1YaxisCount; y++)
									{
										Object[] inbindSet = new Object[15];

										inbindSet[0] = sProductName + Integer.toString(i) + StringUtils.leftPad(Integer.toString(x+1), 2, "0") + StringUtils.leftPad(Integer.toString(y+1), 2, "0");
										inbindSet[1] = "G";
										inbindSet[2] = "G";
										inbindSet[3] = String.valueOf(cut1XaxisCount);
										inbindSet[4] = String.valueOf(cut1YaxisCount);
										inbindSet[5] = String.valueOf(cut2XaxisCount);
										inbindSet[6] = String.valueOf(cut2YaxisCount);
										inbindSet[7] = sProductName;
										inbindSet[8] = sProductName + Integer.toString(i);
										inbindSet[9] = cutType;
										inbindSet[10] = "Created";
										inbindSet[11] = eventInfo.getEventUser();
										inbindSet[12] = ConvertUtil.getCurrTime();
										inbindSet[13] = "Auto Create PanelJudge";
										inbindSet[14] = specData.getUdfs().get("PRODUCTSPECTYPE");
										
										insertArgList.add(inbindSet);
									}
								}
							}
							else if(i==2)
							{
								for(int x=0; x<cut2XaxisCount; x++)
								{
									for(int y=cut1YaxisCount; y<cut1YaxisCount+cut2YaxisCount; y++)
									{
										Object[] inbindSet = new Object[15];

										inbindSet[0] = sProductName + Integer.toString(i) + StringUtils.leftPad(Integer.toString(x+1), 2, "0") + StringUtils.leftPad(Integer.toString(y+1), 2, "0");
										inbindSet[1] = "G";
										inbindSet[2] = "G";
										inbindSet[3] = String.valueOf(cut1XaxisCount);
										inbindSet[4] = String.valueOf(cut1YaxisCount);
										inbindSet[5] = String.valueOf(cut2XaxisCount);
										inbindSet[6] = String.valueOf(cut2YaxisCount);
										inbindSet[7] = sProductName;
										inbindSet[8] = sProductName + Integer.toString(i);
										inbindSet[9] = cutType;
										inbindSet[10] = "Created";
										inbindSet[11] = eventInfo.getEventUser();
										inbindSet[12] = ConvertUtil.getCurrTime();
										inbindSet[13] = "Auto Create PanelJudge";
										inbindSet[14] = specData.getUdfs().get("PRODUCTSPECTYPE");
										
										insertArgList.add(inbindSet);
									}
								}
							}
							
							
							// for HQGlassJudge
							Object[] inbindSetForHQGlass = new Object[9];
							
							inbindSetForHQGlass[0] = sProductName + Integer.toString(i);
							inbindSetForHQGlass[1] = "G";
							if(i==1)
							{
								inbindSetForHQGlass[2] = String.valueOf(cut1XaxisCount);
								inbindSetForHQGlass[3] = String.valueOf(cut1YaxisCount);
							}
							else if(i==2)
							{
								inbindSetForHQGlass[2] = String.valueOf(cut2XaxisCount);
								inbindSetForHQGlass[3] = String.valueOf(cut2YaxisCount);
							}
							inbindSetForHQGlass[4] = sProductName;
							inbindSetForHQGlass[5] = "Created";
							inbindSetForHQGlass[6] = eventInfo.getEventUser();
							inbindSetForHQGlass[7] = ConvertUtil.getCurrTime();
							inbindSetForHQGlass[8] = "Auto Create HQGlassJudge";
							
							insertArgListForHQGlass.add(inbindSetForHQGlass);
						}
					}
					
					//2019.08.01 dmlee : Half and Quarter Same logic Start ---
					/*
					else if ( StringUtil.equals(cutType, GenericServiceProxy.getConstantMap().CUTTYPE_QUARTER) )
					{
						for ( int i = 1; i < 5; i++ )
						{
							String character = "";
							if (i == 1)
							{
								character = "A";
							}
							else if (i == 2)
							{
								character = "B";
							}
							else if (i == 3)
							{
								character = "C";
							}
							else if (i == 4)
							{
								character = "D";
							}
							
							Object[] inbindSet = new Object[15];
							inbindSet[0] = sProductName + Integer.toString(i) + character;
							inbindSet[1] = "G";
							inbindSet[2] = "G";
							inbindSet[3] = specData.getUdfs().get("CUT1XAXISCOUNT");
							inbindSet[4] = specData.getUdfs().get("CUT1YAXISCOUNT");
							inbindSet[5] = specData.getUdfs().get("CUT2XAXISCOUNT");
							inbindSet[6] = specData.getUdfs().get("CUT2YAXISCOUNT");
							inbindSet[7] = sProductName;
							inbindSet[8] = sProductName + Integer.toString(i);
							inbindSet[9] = cutType;
							inbindSet[10] = "Created";
							inbindSet[11] = eventInfo.getEventUser();
							inbindSet[12] = ConvertUtil.getCurrTime();
							inbindSet[13] = "Auto Create PanelJudge";
							inbindSet[14] = specData.getUdfs().get("PRODUCTSPECTYPE");
							
							insertArgList.add(inbindSet);
						}
					}
					*/
					//2019.08.01 dmlee : Half and Quarter Same logic End ---
				}
				catch (Throwable e)
				{
					eventLog.warn(String.format("BindSet Fail! CT_PANELJUDGE"));
				}
			}
			
			//1.3)Release Lot
			MakeReleasedInfo releaseInfo = MESLotServiceProxy.getLotInfoUtil().makeReleasedInfo(
					lotData, machineData.getAreaName(), lotData.getNodeStack(),
					lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
					lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
					lotData.getProductionType(),
					lotData.getUdfs(), "",
					lotData.getDueDate(), lotData.getPriority());			
			
			originalCreateQty = lotData.getCreateProductQuantity();
			lotData.setCreateProductQuantity(productPGSSequence.size());
			lotData.setCreateSubProductQuantity(lotData.getSubProductUnitQuantity1() * productPGSSequence.size());
			lotData.setCreateSubProductQuantity1(lotData.getSubProductUnitQuantity1() * productPGSSequence.size());
			LotServiceProxy.getLotService().update(lotData);
			
			lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
			lotData = MESLotServiceProxy.getLotServiceImpl().releaseLot(eventInfo, lotData, releaseInfo, productPGSSequence);
			
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotData.getKey().getLotName());
			List<Product> productList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());
			
			if(productList != null)
			{
				ExtendedObjectProxy.getProductFlagService().setCreateProductFlagForUPK(eventInfo, productList);
			}
			
			//3. increment Product Request(OIC Release Qty)
			/*
			try
			{
				MESLotServiceProxy.getLotServiceUtil().incrementWorkOrderOicReleaseQty(eventInfo, pPlanData.getKey(), (int) productPGSSequence.size());		
			}
			catch(Exception e)
			{
				eventLog.error("incrementWorkOrderOicReleaseQty Failed");
			}*/
			
			//4. increment Product Request
			try
			{
				// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				pPlanData = ProductRequestPlanServiceProxy.getProductRequestPlanService().selectByKey(pPlanKey);
				pPlanData = ProductRequestPlanServiceProxy.getProductRequestPlanService().selectByKeyForUpdate(pPlanKey);
				
				pPlanData.setPlanQuantity(pPlanData.getPlanQuantity() - ((long)originalCreateQty - productPGSSequence.size()));
				ProductRequestPlanServiceProxy.getProductRequestPlanService().update(pPlanData);
				
				pPlanData = ProductRequestPlanServiceProxy.getProductRequestPlanService().selectByKey(pPlanKey);
				MESWorkOrderServiceProxy.getProductRequestServiceUtil().calculateProductRequestQty(null, pPlanData, "R", productPGSSequence.size(), eventInfo);
			}
			catch(Exception e)
			{
				eventLog.error("incrementWorkOrderReleaseQty Failed");
			}
			
			//4.1 Check WO, WO Plan Released Qty & Plan Qty
			//Product Request Key & Data
			ProductRequestKey pNewKey = new ProductRequestKey();
			pNewKey.setProductRequestName(sProductRequestName);
			ProductRequest pNewData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pNewKey);
			
			//Product Request Plan Key & Data
			ProductRequestPlanKey pNewPlanKey = new ProductRequestPlanKey();
			pNewPlanKey.setProductRequestName(sProductRequestName);
			pNewPlanKey.setAssignedMachineName(sMachineName);
			pNewPlanKey.setPlanReleasedTime(TimeUtils.getTimestamp(sPlanReleasedTime));
			ProductRequestPlan pNewPlanData = ProductRequestPlanServiceProxy.getProductRequestPlanService().selectByKey(pNewPlanKey);
			
			if(pNewData.getPlanQuantity() < pNewData.getReleasedQuantity())
				throw new CustomException("PRODUCTREQUEST-0037", sProductRequestName);
			
			if(pNewPlanData.getPlanQuantity() < pNewPlanData.getReleasedQuantity())
				throw new CustomException("PRODUCTREQUEST-0037", sProductRequestName);
			
			//5. consume DP box
			try
			{
				decreaseCrateQuantity(eventInfo, lotData, productPGSSequence);				
			}
			catch(Exception e)
			{
				eventLog.error("decreaseCrateQuantity Failed");
			}			
			
			//6. Matching Lot Name & W/O Plan(CT_ReserveLot) to Released
			try
			{	
				//Reserve Lot Data
				ReserveLot reserveLot = ExtendedObjectProxy.getReserveLotService().selectByKey(false, new Object[] {sMachineName, sLotName});
				reserveLot.setReserveState(GenericServiceProxy.getConstantMap().RESV_STATE_END);
				
				ExtendedObjectProxy.getReserveLotService().modify(eventInfo, reserveLot);
			}
			catch (Exception ex)
			{
				eventLog.warn(String.format("Lot[%s] is failed to Macthing Plan", sLotName));
			}
			
			try
			{
				GenericServiceProxy.getSqlMesTemplate().updateBatch(insertSql, insertArgList);
				
				String insHistSql = " INSERT INTO CT_PANELJUDGEHISTORY "
						+ " (TIMEKEY, PANELNAME, PANELJUDGE, PANELGRADE, XAXIS1, YAXIS1, XAXIS2, YAXIS2, " 
						+ " GLASSNAME, HQGLASSNAME, CUTTYPE, EVENTNAME, EVENTUSER, EVENTTIME, EVENTCOMMENT, PRODUCTSPECTYPE) "
						+ " SELECT TO_CHAR(SYSDATE, 'YYYYMMDDHH24MISSsss'), PANELNAME, PANELJUDGE, PANELGRADE, XAXIS1, YAXIS1, XAXIS2, YAXIS2, " 
						+ " GLASSNAME, HQGLASSNAME, CUTTYPE, LASTEVENTNAME, LASTEVENTUSER, LASTEVENTTIME, LASTEVENTCOMMENT, PRODUCTSPECTYPE "
						+ " FROM CT_PANELJUDGE "
						+ " WHERE GLASSNAME IN (SELECT PRODUCTNAME FROM PRODUCT WHERE LOTNAME = :LOTNAME) ";
				
				Map<String, Object> insHistbindSet = new HashMap<String, Object>();
				insHistbindSet.put("LOTNAME", lotData.getKey().getLotName());
				
				GenericServiceProxy.getSqlMesTemplate().update(insHistSql, insHistbindSet);
			}
			catch (Exception ex)
			{
				eventLog.warn(String.format("Update Fail! CT_PANELJUDGE"));
			}
			
			try
			{
				GenericServiceProxy.getSqlMesTemplate().updateBatch(insertSqlForHQGlass, insertArgListForHQGlass);
				
				String insHistSqlForHQGlass = " INSERT INTO CT_HQGLASSJUDGEHIST "
						+ " (TIMEKEY, HQGLASSNAME, HQGLASSJUDGE, XAXIS, YAXIS, " 
						+ " GLASSNAME, EVENTNAME, EVENTUSER, EVENTTIME, EVENTCOMMENT) "
						+ " SELECT TO_CHAR(SYSDATE, 'YYYYMMDDHH24MISSsss'), HQGLASSNAME, HQGLASSJUDGE, XAXIS, YAXIS, " 
						+ " GLASSNAME, LASTEVENTNAME, LASTEVENTUSER, LASTEVENTTIME, LASTEVENTCOMMENT "
						+ " FROM CT_HQGLASSJUDGE "
						+ " WHERE GLASSNAME IN (SELECT PRODUCTNAME FROM PRODUCT WHERE LOTNAME = :LOTNAME) ";
				
				Map<String, Object> insHistbindSetForHQGlass = new HashMap<String, Object>();
				insHistbindSetForHQGlass.put("LOTNAME", lotData.getKey().getLotName());
				
				GenericServiceProxy.getSqlMesTemplate().update(insHistSqlForHQGlass, insHistbindSetForHQGlass);
			}
			catch (Exception ex)
			{
				eventLog.warn(String.format("Update Fail! CT_HQGLASSJUDGE"));
			}
			
			//7. Auto Track In / Out Unpacker
			//Document trackInOutDoc = writeTrackInOutRequest(doc, sLotName, sMachineName, sCarrierName, sLoadPort, sUnLoadPort, crateName);
			
			Port loader = MESLotServiceProxy.getLotServiceUtil().searchLoaderPort(sMachineName);
			
//			String machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(),
//					lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, false, lotData.getUdfs().get("ECCODE"));

			ConsumableKey consumableKey = new ConsumableKey(crateName);
			Consumable con = ConsumableServiceProxy.getConsumableService().selectByKey(consumableKey);
			String consumableSpec = con.getConsumableSpecName();

			ConsumableSpecKey consumableSpecKey = new ConsumableSpecKey();
			consumableSpecKey.setConsumableSpecName(consumableSpec);
			consumableSpecKey.setConsumableSpecVersion("00001");
			consumableSpecKey.setFactoryName(con.getFactoryName());
			ConsumableSpec conSpec = ConsumableServiceProxy.getConsumableSpecService().selectByKey(consumableSpecKey);
			
			String machineRecipeName = conSpec.getUdfs().get("MACHINERECIPENAME");
			
			doc = MESLotServiceProxy.getLotServiceUtil().writeTrackInRequest(doc, lotData.getKey().getLotName(), loader.getKey().getMachineName(), sLoadPort, machineRecipeName);

			MESLotServiceProxy.getLotServiceUtil().TrackInLotForUPK(doc, eventInfo);
			
			Port unloader = MESLotServiceProxy.getLotServiceUtil().searchUnloaderPort(loader);

			doc = MESLotServiceProxy.getLotServiceUtil().writeTrackOutRequest(doc, lotData.getKey().getLotName(), unloader.getKey().getMachineName(), sUnLoadPort, sCarrierName);

			MESLotServiceProxy.getLotServiceUtil().TrackOutLotForUPK(doc, eventInfo);
			
			successLotList.add(LotNum, lotData);
			
			LotNum++;
			// 2019.08.23 Add By Park Jeong Su
			try {
				eventLog.info("ProductSpecIdle Time Reset Start ProductSpecName : " + lotData.getProductSpecName());
				ExtendedObjectProxy.getProductSpecIdleTimeService().forceResetProductSpecIdleTime(lotData.getProductSpecName(), eventInfo);
				eventLog.info("ProductSpecIdle Time Reset End");
			} catch (Exception e) {
				eventLog.info("ProductSpecIdle Time Reset Error");
			}
			// 2019.08.23 Add By Park Jeong Su
		}
		
		MESLotServiceProxy.getLotServiceUtil().setNextInfoForUPK(doc, successLotList);

		return doc;
	}
	
	/**
	 * logic for decrement with different DP box
	 * @author swcho
	 * @since 2014.05.08
	 * @param eventInfo
	 * @param lotData
	 * @param productPGSSequence
	 * @throws CustomException
	 */
	private void decreaseCrateQuantity(EventInfo eventInfo, Lot lotData, List<ProductPGS> productPGSSequence)
		throws CustomException
	{
		Map<String, Object> crateMap = new HashMap<String, Object>();
		
		String oldCrateName = "";
		
		int count = 0;
		
		for (ProductPGS productPGS : productPGSSequence)
		{
			if (!oldCrateName.equals(CommonUtil.getValue(productPGS.getUdfs(), "CRATENAME")))
			{
				//initialize
				count = 0;
				oldCrateName = CommonUtil.getValue(productPGS.getUdfs(), "CRATENAME");
			}
			
			count++;
			crateMap.put(oldCrateName, count);
		}
		
		for (String crateName : crateMap.keySet())
		{
			if (crateMap.get(crateName) != null)
			{
				int quantity = Integer.parseInt(crateMap.get(crateName).toString());
				
				eventInfo = EventInfoUtil.makeEventInfo("AdjustQty", getEventUser(), getEventComment(), null, null);
				decreaseCrateQuantity(eventInfo, lotData, crateName, quantity);
				
				//makeNotAvailable
				Consumable crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(crateName);
				if(crateData.getQuantity() == 0 && StringUtil.equals(crateData.getConsumableState(), "Available"))
				{
					eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), null, null);
					
					MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();
					makeNotAvailableInfo.setUdfs(crateData.getUdfs());
					MESConsumableServiceProxy.getConsumableServiceImpl().makeNotAvailable(crateData, makeNotAvailableInfo, eventInfo);
				}
			}
		}
	}
	
	/**
	 * to consume Crate
	 * @author swcho
	 * @since 2014.05.08
	 * @param eventInfo
	 * @param lotData
	 * @param consumableName
	 * @throws CustomException
	 */
	private void decreaseCrateQuantity(EventInfo eventInfo, Lot lotData, String consumableName, double quantity)
		throws CustomException
	{
		eventInfo.setEventName("Consume");
		
		Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(consumableName);
		
		DecrementQuantityInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().decrementQuantityInfo(lotData.getKey().getLotName(),
												lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
												"", TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()),
												quantity, lotData.getUdfs());
		
		MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(consumableData, transitionInfo, eventInfo);
	}
}