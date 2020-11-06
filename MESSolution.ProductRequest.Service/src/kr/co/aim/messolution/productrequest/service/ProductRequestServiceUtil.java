package kr.co.aim.messolution.productrequest.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.management.data.ProductRequestHistory;
import kr.co.aim.messolution.extended.object.management.data.ProductRequestPlanHistory;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequest.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.productrequest.management.info.IncrementFinishedQuantityByInfo;
import kr.co.aim.greentrack.productrequest.management.info.IncrementReleasedQuantityByInfo;
import kr.co.aim.greentrack.productrequest.management.info.IncrementScrappedQuantityByInfo;
import kr.co.aim.greentrack.productrequest.management.info.MakeReleasedInfo;
import kr.co.aim.greentrack.productrequestplan.ProductRequestPlanServiceProxy;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlan;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlanKey;
import kr.co.aim.greentrack.productrequestplan.management.info.IncrementQuantityByInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class ProductRequestServiceUtil implements ApplicationContextAware
{
	
	/**
	 * @uml.property  name="applicationContext"
	 * @uml.associationEnd  
	 */
	private ApplicationContext		applicationContext;
	private static Log				log = LogFactory.getLog("ProductRequestServiceUtil");
	
	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	
	/**
	 * @param arg0
	 * @throws BeansException
	 * @uml.property  name="applicationContext"
	 */
	@Override
    public void setApplicationContext( ApplicationContext arg0 ) throws BeansException
	{
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}
	
	/**
	 * to post input plan reservation
	 * 151105 by xzquan : service object changed
	 * @author xzquan
	 * @since 2015.11.08
	 * @param eventInfo
	 * @param planData
	 * @throws CustomException
	 */
	public void syncPlanByProductRequest(EventInfo eventInfo, String sProductRequestName)
		throws CustomException
	{
		try
		{
			ProductRequestKey pKey = new ProductRequestKey(sProductRequestName);
			
			//Get Data
			ProductRequest pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
			
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			String condition = "productRequestName = ?";
			String condition = "productRequestName = ? for update";

			Object bindSet[] = new Object[]{pKey.getProductRequestName()};
			List<ProductRequestPlan> pPlanList = ProductRequestPlanServiceProxy.getProductRequestPlanService().select(condition, bindSet);
			
			if(pPlanList.size() > 0)
			{
				eventInfo.setEventName("Sync");
				for(ProductRequestPlan pPlanData : pPlanList)
				{
					pPlanData.setReleasedQuantity(pData.getReleasedQuantity());
					pPlanData.setFinishedQuantity(pData.getFinishedQuantity());
					pPlanData.setPlanFinishedTime(pData.getPlanFinishedTime());
					pPlanData.setPlanQuantity(pData.getPlanQuantity());
					pPlanData.setProductRequestPlanHoldState(pPlanData.getProductRequestPlanHoldState());
					pPlanData.setProductRequestPlanState(pPlanData.getProductRequestPlanState());
					pPlanData.setReleasedQuantity(pData.getReleasedQuantity());
					pPlanData.setLastEventName(eventInfo.getEventName());
					
					// Modified by smkang on 2019.05.17 - EventInfo.LastEventTimekey is null.
//					pPlanData.setLastEventTimeKey(eventInfo.getLastEventTimekey());
					pPlanData.setLastEventTimeKey(eventInfo.getEventTimeKey());
					
					pPlanData.setLastEventTime(eventInfo.getEventTime());
					pPlanData.setLastEventUser(eventInfo.getEventUser());
					pPlanData.setLastEventComment(eventInfo.getEventComment());
					
					ProductRequestPlanServiceProxy.getProductRequestPlanService().update(pPlanData);
				}
			}
		}
		catch (greenFrameDBErrorSignal ne)
		{
			throw new CustomException("", ne.getMessage());
		}
	}
	
	
	public void addHistory(ProductRequest dataInfo, EventInfo eventInfo) throws CustomException
	{
		//If WorkOrderPlan Data Update, using this Function
		
		ProductRequestHistory historyData = new ProductRequestHistory(dataInfo.getKey().getProductRequestName(), eventInfo.getEventTimeKey());
		
		historyData.setProductRequestType(dataInfo.getProductRequestType());
		historyData.setFactoryName(dataInfo.getFactoryName());
		historyData.setProductSpecName(dataInfo.getProductSpecName());
		historyData.setPlanReleasedTime(dataInfo.getPlanReleasedTime());
		historyData.setPlanFinishedTime(dataInfo.getPlanFinishedTime());
		historyData.setPlanQuantity(dataInfo.getPlanQuantity());
		historyData.setReleasedQuantity(dataInfo.getReleasedQuantity());
		historyData.setFinishedQuantity(dataInfo.getFinishedQuantity());
		historyData.setScrappedQuantity(dataInfo.getScrappedQuantity());
		historyData.setProductRequestState(dataInfo.getProductRequestState());
		historyData.setProductRequestHoldState(dataInfo.getProductRequestHoldState());
		historyData.setEventName(eventInfo.getEventName());
		historyData.setEventTime(eventInfo.getEventTime());
		historyData.setEventUser(eventInfo.getEventUser());
		historyData.setEventComment(eventInfo.getEventComment());
		historyData.setCreateTime(dataInfo.getCreateTime());
		historyData.setCreateUser(dataInfo.getCreateUser());
		historyData.setReleaseTime(dataInfo.getReleaseTime());
		historyData.setReleaseUser(dataInfo.getReleaseUser());
		historyData.setCompleteTime(dataInfo.getCompleteTime());
		historyData.setCompleteUser(dataInfo.getCompleteUser());
		historyData.setCrateSpecName(dataInfo.getUdfs().get("CrateSpecName"));
		historyData.setChangeInQuantity(dataInfo.getUdfs().get("CHANGEINQUANTITY"));
		historyData.setChangeOutQuantity(dataInfo.getUdfs().get("CHANGEOUTQUANTITY"));
		historyData.setdescription(dataInfo.getUdfs().get("DESCRIPTION"));
		historyData.setUnit(dataInfo.getUdfs().get("UNIT"));
		historyData.setSales_Order(dataInfo.getUdfs().get("SALES_ORDER"));
		historyData.setEng_Name(dataInfo.getUdfs().get("ENG_NAME"));
		historyData.setExp_No(dataInfo.getUdfs().get("EXP_NO"));
		
		String panelSheetCount = dataInfo.getUdfs().get("PLN_SHT_CNT");
		historyData.setPln_Sht_Cnt((StringUtils.isNotEmpty(panelSheetCount) && StringUtils.isNumeric(panelSheetCount)) ? Long.parseLong(panelSheetCount) : 0);
		
		historyData.setClm_User(dataInfo.getUdfs().get("CLM_USER"));
		historyData.setPln_Stb_Date(TimeUtils.getTimestamp(dataInfo.getUdfs().get("PLN_STB_DATE")));
		historyData.setSelf_Kind_Name1(dataInfo.getUdfs().get("SELF_KIND_NAME1"));
		historyData.setSelf_Kind_Name2(dataInfo.getUdfs().get("SELF_KIND_NAME2"));
		historyData.setSelf_Kind_Name3(dataInfo.getUdfs().get("SELF_KIND_NAME3"));
		historyData.setSelf_Kind_Name4(dataInfo.getUdfs().get("SELF_KIND_NAME4"));
		historyData.setSelf_Kind_Name5(dataInfo.getUdfs().get("SELF_KIND_NAME5"));
		historyData.setCust_Prod_Id(dataInfo.getUdfs().get("CUST_PROD_ID"));
		historyData.setCust_Name(dataInfo.getUdfs().get("CUST_NAME"));
		historyData.setGrade(dataInfo.getUdfs().get("GRADE"));
		historyData.setMakt_Maktx(dataInfo.getUdfs().get("MAKT_MAKTX"));
		historyData.setMkal_Text1(dataInfo.getUdfs().get("MKAL_TEXT1"));
		historyData.setAfpo_Dwerk(dataInfo.getUdfs().get("AFPO_DWERK"));
		historyData.setKnmt_Postx(dataInfo.getUdfs().get("KNMT_POSTX"));
		historyData.setVbkd_Bstkd(dataInfo.getUdfs().get("VBKD_BSTKD"));
		historyData.setVbak_Kunnr(dataInfo.getUdfs().get("VBAK_KUNNR"));
		historyData.setEndBank(dataInfo.getUdfs().get("ENDBANK"));
		
		MESWorkOrderServiceProxy.getProductRequestServiceImpl().addHistory(historyData);
	}
	
	public void addPlanHistory(ProductRequestPlan dataInfo, EventInfo eventInfo) throws CustomException
	{
		//If WorkOrderPlan Data Update, using this Function
		
		ProductRequestPlanKey key = dataInfo.getKey();
		
		ProductRequestPlanHistory historyData = new ProductRequestPlanHistory(key.getProductRequestName(), key.getAssignedMachineName(), key.getPlanReleasedTime(), eventInfo.getEventTimeKey()); 
		
		/*hhlee, 20180404, Add TimeKey ==>> */
		historyData.setTimekey(eventInfo.getEventTimeKey());
		/*<<== hhlee, 20180404, Add TimeKey */
		
		historyData.setPlanFinishedTime(dataInfo.getPlanFinishedTime());
		historyData.setPlanQuantity(dataInfo.getPlanQuantity());
		historyData.setReleasedQuantity(dataInfo.getReleasedQuantity());
		historyData.setFinishedQuantity(dataInfo.getFinishedQuantity());
		historyData.setProductRequestPlanState(dataInfo.getProductRequestPlanState());
		historyData.setProductRequestPlanHoldState(dataInfo.getProductRequestPlanHoldState());
		historyData.setEventName(eventInfo.getEventName());
		historyData.setEventTime(eventInfo.getEventTime());
		historyData.setEventUser(eventInfo.getEventUser());
		historyData.setEventComment(eventInfo.getEventComment());
		historyData.setEventFlag(dataInfo.getLastEventFlag());
		historyData.setPosition(Integer.parseInt(dataInfo.getUdfs().get("position")));
		historyData.setProcessFlowName(dataInfo.getUdfs().get("processFlowName"));
		historyData.setECCode(dataInfo.getUdfs().get("ECCode"));
		historyData.setECCode(dataInfo.getUdfs().get("departmentName"));
		historyData.setECCode(dataInfo.getUdfs().get("priority"));
		
		MESWorkOrderServiceProxy.getProductRequestServiceImpl().addPlanHistory(historyData);
	}
	
	/*
	* Name : CheckProductRequestReserved
	* Desc : This function is CheckProductRequestReserved
	* Author : AIM Systems, Inc
	* Date : 2011.02.18
	*/
	public static boolean CheckProductRequestReserved(String productRequestName) throws CustomException
	{
		try
		{		
			String condition = "ProductRequestName = ?";
			Object bindSet[] = new Object[]{productRequestName};
			
			List<ProductRequestPlan> productRequestPlanList = ProductRequestPlanServiceProxy.getProductRequestPlanService().select(condition, bindSet);
			
			if(productRequestPlanList.size() > 0)
			{
				return true;
			}
			
		}
		catch (greenFrameDBErrorSignal ex)
		{
			//throw new CustomException();
		}
		catch (Exception ex)
		{
			//throw new CustomException();
		}
		
		return false;
	}
	
	public static boolean CheckERPWorkOrder(String productRequestName) throws CustomException
	{
		try
		{		
			String sql = "SELECT PRODUCTREQUESTNAME FROM CT_ERPPRODUCTREQUEST WHERE PRODUCTREQUESTNAME = :productrequestname";
			
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("productrequestname", productRequestName);
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult = 
					GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			
			if(sqlResult.size() > 0)
			{
				return true;
			}
			
		}
		catch (greenFrameDBErrorSignal ex)
		{
			//throw new CustomException();
		}
		catch (Exception ex)
		{
			//throw new CustomException();
		}
		
		return false;
	}
	
	public static void WriteMESProductRequest(String productRequestName) throws CustomException
	{
		//Get ProductRequest Data
		ProductRequestKey pKey = new ProductRequestKey();
		pKey.setProductRequestName(productRequestName);
		
		ProductRequest pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
		
		//Check ProductRequest Reserved
		String isInput = "N";
		boolean chkProductRequest = ProductRequestServiceUtil.CheckProductRequestReserved(productRequestName);
		if(chkProductRequest) isInput = "Y";
		
		//Insert
		StringBuilder sql = new StringBuilder();
		sql.append(" INSERT INTO CT_MESPRODUCTREQUEST (PRODUCTREQUESTNAME, ");
		sql.append("                                   PRODUCTSPECNAME, ");
		sql.append("                                   FACTORYNAME, ");
		//sql.append("                                   PRODUCTTYPE, ");
		sql.append("                                   PRODUCTIONTYPE, ");
		sql.append("                                   PRODUCTREQUESTSTATE, ");
		sql.append("                                   PLANRELEASEDTIME, ");
		sql.append("                                   PLANFINISHEDTIME, ");
		sql.append("                                   TRADETYPE, ");
		//sql.append("                                   PRIORITY, ");
		sql.append("                                   PLANQUANTITY, ");
		sql.append("                                   RELEASEDQUANTITY, ");
		//sql.append("                                   PLANLIMITQUANTITY, ");
		sql.append("                                   MTO, ");
		sql.append("                                   ROHS, ");
		sql.append("                                   SALEORDER, ");
		sql.append("                                   ISINPUT, ");
		sql.append("                                   SEQ, ");
		//sql.append("                                   INFORESULTSTATUS, ");
		//sql.append("                                   INFORESULTMESSAGE, ");
		sql.append("                                   WRITEDATE, ");
		//sql.append("                                   READDATE, ");
		sql.append("                                   RECEIVEFLAG) ");
		sql.append("      VALUES ( :PRODUCTREQUESTNAME, ");
		sql.append("              :PRODUCTSPECNAME, ");
		sql.append("              :FACTORYNAME, ");
		//sql.append("              :PRODUCTTYPE, ");
		sql.append("              :PRODUCTIONTYPE, ");
		sql.append("              :PRODUCTREQUESTSTATE, ");
		sql.append("              :PLANRELEASEDTIME, ");
		sql.append("              :PLANFINISHEDTIME, ");
		sql.append("              :TRADETYPE, ");
		//sql.append("              :PRIORITY, ");
		sql.append("              :PLANQUANTITY, ");
		sql.append("              :RELEASEDQUANTITY, ");
		//sql.append("              :PLANLIMITQUANTITY, ");
		sql.append("              :MTO, ");
		sql.append("              :ROHS, ");
		sql.append("              :SALEORDER, ");
		sql.append("              :ISINPUT, ");
		sql.append("              :SEQ, ");
		//sql.append("              :INFORESULTSTATUS, ");
		//sql.append("              :INFORESULTMESSAGE, ");
		sql.append("              :WRITEDATE, ");
		//sql.append("              :READDATE, ");
		sql.append("              :RECEIVEFLAG) ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PRODUCTREQUESTNAME", pData.getKey().getProductRequestName());
		bindMap.put("PRODUCTSPECNAME", pData.getProductSpecName());
		bindMap.put("FACTORYNAME", pData.getFactoryName());
		//bindMap.put("PRODUCTTYPE", "");
		bindMap.put("PRODUCTIONTYPE", pData.getProductRequestType());
		bindMap.put("PRODUCTREQUESTSTATE", pData.getProductRequestState());
		bindMap.put("PLANRELEASEDTIME", pData.getPlanReleasedTime());
		bindMap.put("PLANFINISHEDTIME", pData.getPlanFinishedTime());
		bindMap.put("TRADETYPE", pData.getUdfs().get("tradeType"));
		//bindMap.put("PRIORITY", "");
		bindMap.put("PLANQUANTITY", String.valueOf(pData.getPlanQuantity()));
		bindMap.put("RELEASEDQUANTITY", String.valueOf(pData.getReleasedQuantity()));
		//bindMap.put("PLANLIMITQUANTITY", "");
		bindMap.put("MTO", pData.getUdfs().get("mto"));
		bindMap.put("ROHS", pData.getUdfs().get("rohs"));
		bindMap.put("SALEORDER", pData.getUdfs().get("saleOrder"));
		bindMap.put("ISINPUT", isInput);
		bindMap.put("SEQ", TimeUtils.getCurrentEventTimeKey());
		//bindMap.put("INFORESULTSTATUS", "");
		//bindMap.put("INFORESULTMESSAGE", "");
		bindMap.put("WRITEDATE", TimeStampUtil.getCurrentTimestamp());
		//bindMap.put("READDATE", "");
		bindMap.put("RECEIVEFLAG", "N");

		try
		{
			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		}
		catch (FrameworkErrorSignal fe)
		{
		}
	}
	
	public static void WriteMESProductRequestScrap(String productRequestName,int scrapQty) throws CustomException
	{
		//Get ProductRequest Data
		ProductRequestKey pKey = new ProductRequestKey();
		pKey.setProductRequestName(productRequestName);
		
		ProductRequest pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
		
		//Insert
		StringBuilder sql = new StringBuilder();
		sql.append(" INSERT INTO CT_MESPRODUCTREQUESTSCRAP (PRODUCTREQUESTNAME, ");
		sql.append("                                   PRODUCTSPECNAME, ");
		sql.append("                                   FACTORYNAME, ");
		//sql.append("                                   PRODUCTTYPE, ");
		sql.append("                                   SCRAPPEDQUANTITY, ");
		sql.append("                                   FINISHEDQUANTITY, ");
		sql.append("                                   MTO, ");
		sql.append("                                   TRADETYPE, ");
		sql.append("                                   SEQ, ");
		//sql.append("                                   INFORESULTSTATUS, ");
		//sql.append("                                   INFORESULTMESSAGE, ");
		sql.append("                                   WRITEDATE, ");
		//sql.append("                                   READDATE, ");
		sql.append("                                   RECEIVEFLAG) ");
		sql.append("      VALUES ( :PRODUCTREQUESTNAME, ");
		sql.append("              :PRODUCTSPECNAME, ");
		sql.append("              :FACTORYNAME, ");
		//sql.append("              :PRODUCTTYPE, ");
		sql.append("              :SCRAPPEDQUANTITY, ");
		sql.append("              :FINISHEDQUANTITY, ");
		sql.append("              :MTO, ");
		sql.append("              :TRADETYPE, ");
		sql.append("              :SEQ, ");
		//sql.append("              :INFORESULTSTATUS, ");
		//sql.append("              :INFORESULTMESSAGE, ");
		sql.append("              :WRITEDATE, ");
		//sql.append("              :READDATE, ");
		sql.append("              :RECEIVEFLAG) ");


		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PRODUCTREQUESTNAME", pData.getKey().getProductRequestName());
		bindMap.put("PRODUCTSPECNAME", pData.getProductSpecName());
		bindMap.put("FACTORYNAME", pData.getFactoryName());
		//bindMap.put("PRODUCTTYPE", "");
		bindMap.put("SCRAPPEDQUANTITY", scrapQty);
		bindMap.put("FINISHEDQUANTITY", "0");
		bindMap.put("MTO", pData.getUdfs().get("mto"));
		bindMap.put("TRADETYPE", pData.getUdfs().get("tradeType"));
		bindMap.put("SEQ", TimeUtils.getCurrentEventTimeKey());
		//bindMap.put("INFORESULTSTATUS", "");
		//bindMap.put("INFORESULTMESSAGE", "");
		bindMap.put("WRITEDATE", TimeStampUtil.getCurrentTimestamp());
		//bindMap.put("READDATE", "");
		bindMap.put("RECEIVEFLAG", "N");

		try
		{
			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		}
		catch (FrameworkErrorSignal fe)
		{
		}
	}
	
	//20170325 Add by yudan
	public static void WriteMESProductRequestFinish(String productRequestName,int finishQty) throws CustomException
	{
		//Get ProductRequest Data
		ProductRequestKey pKey = new ProductRequestKey();
		pKey.setProductRequestName(productRequestName);
		
		ProductRequest pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
		
		//Insert
		StringBuilder sql = new StringBuilder();
		sql.append(" INSERT INTO CT_MESPRODUCTREQUESTSCRAP (PRODUCTREQUESTNAME, ");
		sql.append("                                   PRODUCTSPECNAME, ");
		sql.append("                                   FACTORYNAME, ");
		//sql.append("                                   PRODUCTTYPE, ");
		sql.append("                                   SCRAPPEDQUANTITY, ");
		sql.append("                                   FINISHEDQUANTITY, ");
		sql.append("                                   MTO, ");
		sql.append("                                   TRADETYPE, ");
		sql.append("                                   SEQ, ");
		//sql.append("                                   INFORESULTSTATUS, ");
		//sql.append("                                   INFORESULTMESSAGE, ");
		sql.append("                                   WRITEDATE, ");
		//sql.append("                                   READDATE, ");
		sql.append("                                   RECEIVEFLAG) ");
		sql.append("      VALUES ( :PRODUCTREQUESTNAME, ");
		sql.append("              :PRODUCTSPECNAME, ");
		sql.append("              :FACTORYNAME, ");
		//sql.append("              :PRODUCTTYPE, ");
		sql.append("              :SCRAPPEDQUANTITY, ");
		sql.append("              :FINISHEDQUANTITY, ");
		sql.append("              :MTO, ");
		sql.append("              :TRADETYPE, ");
		sql.append("              :SEQ, ");
		//sql.append("              :INFORESULTSTATUS, ");
		//sql.append("              :INFORESULTMESSAGE, ");
		sql.append("              :WRITEDATE, ");
		//sql.append("              :READDATE, ");
		sql.append("              :RECEIVEFLAG) ");


		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PRODUCTREQUESTNAME", pData.getKey().getProductRequestName());
		bindMap.put("PRODUCTSPECNAME", pData.getProductSpecName());
		bindMap.put("FACTORYNAME", pData.getFactoryName());
		//bindMap.put("PRODUCTTYPE", "");
		bindMap.put("SCRAPPEDQUANTITY", "0");
		bindMap.put("FINISHEDQUANTITY", finishQty);
		bindMap.put("MTO", pData.getUdfs().get("mto"));
		bindMap.put("TRADETYPE", pData.getUdfs().get("tradeType"));
		bindMap.put("SEQ", TimeUtils.getCurrentEventTimeKey());
		//bindMap.put("INFORESULTSTATUS", "");
		//bindMap.put("INFORESULTMESSAGE", "");
		bindMap.put("WRITEDATE", TimeStampUtil.getCurrentTimestamp());
		//bindMap.put("READDATE", "");
		bindMap.put("RECEIVEFLAG", "N");

		try
		{
			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		}
		catch (FrameworkErrorSignal fe)
		{
		}
	}

	public static void SetERPReceiveFlag(String seq, String productRequestName) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append(" UPDATE CT_ERPPRODUCTREQUEST A ");
		sql.append("    SET ");
		sql.append("    A.RECEIVEFLAG = 'Y', ");
		sql.append("    A.READDATE = :READDATE ");
		sql.append("  WHERE 1 = 1 ");
		sql.append("    AND A.PRODUCTREQUESTNAME = :PRODUCTREQUESTNAME ");
		sql.append("    AND A.SEQ = :SEQ ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("SEQ", seq);
		bindMap.put("PRODUCTREQUESTNAME", productRequestName);
		bindMap.put("READDATE", TimeStampUtil.getCurrentTimestamp());

		try
		{
			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		}
		catch (FrameworkErrorSignal fe)
		{
		}
	}
	
	/*
	* Name : calculateProductRequestQty
	* Desc : This function is calculateProductRequestQty (Request EDO)
	* Author : dmlee
	* Date : 2018.03.30
	*/
	public void calculateProductRequestQty(String productRequestName, ProductRequestPlan productRequestPlanData,
							  String calculateType, long calculateQty, EventInfo eventInfo)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{
		try
		{
			/*
			 * -String calculateType-
			 * R : ReleasedQty calculate
			 * F : FinishedQty calculate
			 * S : ScrappedQty calculate
			 */
			
			ProductRequestKey productRequestKey;
			
			//If Don't know ProductRequestPlan Info
			if(productRequestPlanData == null)
			{
				productRequestKey = new ProductRequestKey(productRequestName);
			}
			else
			{
				productRequestKey = new ProductRequestKey(productRequestPlanData.getKey().getProductRequestName());
			}
			
			ProductRequest resultData;
			
			if(calculateType.equals("R"))
			{
				if(calculateQty > 0)
				{
					eventInfo.setEventName("IncrementReleasedQuantity");
				}
				else
				{
					eventInfo.setEventName("DecrementReleasedQuantity");
				}
				
				//Increment Released Qty
				IncrementReleasedQuantityByInfo incrementReleasedInfo = new IncrementReleasedQuantityByInfo();
				incrementReleasedInfo.setQuantity(calculateQty);

				ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestInfoUtil().getProductRequest(productRequestKey.getProductRequestName());
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementReleasedQuantityBy(productRequestData, incrementReleasedInfo, eventInfo);
				
				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				resultData = MESWorkOrderServiceProxy.getProductRequestInfoUtil().getProductRequest(productRequestKey.getProductRequestName());
				resultData = ProductRequestServiceProxy.getProductRequestService().selectByKeyForUpdate(productRequestKey);
				
				///////////////////////////////////////////////
				//Plan Calculate///////////////////////////////
				if(calculateQty > 0)
				{
					if(productRequestPlanData == null)
					{
						throw new CustomException("productRequestPlanData is null ! ");
					}
				
					ProductRequestPlan resultPlanData;	
	
					//Increment Released Qty
					IncrementQuantityByInfo incrementInfo = new IncrementQuantityByInfo();
					incrementInfo.setQuantity(calculateQty);
					
					eventInfo.setEventName("IncrementReleasedQuantity");
					MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementPlanQuantityBy(productRequestPlanData, incrementInfo, eventInfo);
					
					productRequestPlanData = ProductRequestPlanServiceProxy.getProductRequestPlanService().selectByKey(productRequestPlanData.getKey());
					
					// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					resultPlanData = ProductRequestPlanServiceProxy.getProductRequestPlanService().selectByKey(productRequestPlanData.getKey());
					resultPlanData = ProductRequestPlanServiceProxy.getProductRequestPlanService().selectByKeyForUpdate(productRequestPlanData.getKey());
					
					//Set Product Request State
					if(resultPlanData.getProductRequestPlanState().equals(GenericServiceProxy.getConstantMap().Prq_Planned)
							&& resultPlanData.getReleasedQuantity() > 0)
					{
						//WO State : Planned -> Released
						eventInfo.setEventName("Release");
						eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
						eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
						
						kr.co.aim.greentrack.productrequestplan.management.info.MakeReleasedInfo makeReleasedInfo = new kr.co.aim.greentrack.productrequestplan.management.info.MakeReleasedInfo();
						makeReleasedInfo.setUdfs(resultPlanData.getUdfs());
						
						MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeReleasedPlan(resultPlanData, makeReleasedInfo, eventInfo);
						
						// 2019.04.15_hsryu_Add Complete Logic. 
						if(resultPlanData.getProductRequestPlanState().equals(GenericServiceProxy.getConstantMap().Prq_Released)
								&& resultPlanData.getPlanQuantity() <= (resultPlanData.getReleasedQuantity()))
						{
							//WO State : Released -> Complete
							eventInfo.setEventName("Complete");
							eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
							eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
							
							kr.co.aim.greentrack.productrequestplan.management.info.MakeCompletedInfo makeCompletedInfo = new kr.co.aim.greentrack.productrequestplan.management.info.MakeCompletedInfo();
							makeCompletedInfo.setUdfs(resultPlanData.getUdfs());
							MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompletedPlan(resultPlanData, makeCompletedInfo, eventInfo);
						}
					}
					else if(resultPlanData.getProductRequestPlanState().equals(GenericServiceProxy.getConstantMap().Prq_Released)
							&& resultPlanData.getPlanQuantity() <= (resultPlanData.getReleasedQuantity()))
					{
						//WO State : Released -> Complete
						eventInfo.setEventName("Complete");
						eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
						eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
						
						kr.co.aim.greentrack.productrequestplan.management.info.MakeCompletedInfo makeCompletedInfo = new kr.co.aim.greentrack.productrequestplan.management.info.MakeCompletedInfo();
						makeCompletedInfo.setUdfs(resultPlanData.getUdfs());
						MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompletedPlan(resultPlanData, makeCompletedInfo, eventInfo);
					}
					///////////////////////////////////////////////
					//Plan Calculate End///////////////////////////////
				}
			}
			else if(calculateType.equals("F"))
			{
				if(calculateQty > 0)
				{
					eventInfo.setEventName("IncrementFinishedQuantity");
				}
				else
				{
					eventInfo.setEventName("DecrementFinishedQuantity");
				}
				
				//Increment Finished Qty
				IncrementFinishedQuantityByInfo incrementFinishedInfo = new IncrementFinishedQuantityByInfo();
				incrementFinishedInfo.setQuantity(calculateQty);
				
				ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestInfoUtil().getProductRequest(productRequestKey.getProductRequestName());
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementFinishedQuantityBy(productRequestData, incrementFinishedInfo, eventInfo);
				
				resultData = MESWorkOrderServiceProxy.getProductRequestInfoUtil().getProductRequest(productRequestKey.getProductRequestName());

			}
			else if(calculateType.equals("S"))
			{	
				//Increment Scrapped Qty
				IncrementScrappedQuantityByInfo incrementScrappedInfo = new IncrementScrappedQuantityByInfo();
				incrementScrappedInfo.setQuantity(calculateQty);
				
				if(calculateQty > 0)
				{
					eventInfo.setEventName("IncrementScrappedQuantity");
				}
				else
				{
					eventInfo.setEventName("DecrementScrappedQuantity");
				}
				
				
				ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestInfoUtil().getProductRequest(productRequestKey.getProductRequestName());
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementScrappedQuantityBy(productRequestData, incrementScrappedInfo, eventInfo);
				
				resultData = MESWorkOrderServiceProxy.getProductRequestInfoUtil().getProductRequest(productRequestKey.getProductRequestName());
			}
			else
			{
				throw new CustomException("calcultate Type Error !");
			}
			
			
			//Set Product Request State
			if(resultData.getProductRequestState().equals(GenericServiceProxy.getConstantMap().Prq_Planned)
					&& resultData.getReleasedQuantity() > 0)
			{
				//WO State : Planned -> Released
				eventInfo.setEventName("Release");
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
				
				MakeReleasedInfo makeReleasedInfo = new MakeReleasedInfo();
				makeReleasedInfo.setUdfs(resultData.getUdfs());
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeReleased(resultData, makeReleasedInfo, eventInfo);
			}
			/*
			else if(resultData.getProductRequestState().equals(GenericServiceProxy.getConstantMap().Prq_Released)
					&& resultData.getPlanQuantity() <= (resultData.getReleasedQuantity()))
			{
				//WO State : Released -> Complete
				eventInfo.setEventName("Complete");
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
				
				MakeCompletedInfo makeCompletedInfo = new MakeCompletedInfo();
				makeCompletedInfo.setUdfs(resultData.getUdfs());
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(resultData, makeCompletedInfo, eventInfo);
			}
			else if(resultData.getProductRequestState().equals(GenericServiceProxy.getConstantMap().Prq_Completed)
					&& resultData.getPlanQuantity() <= (resultData.getFinishedQuantity() + resultData.getScrappedQuantity()))
			{
				//WO State : Complete -> Finished
				eventInfo.setEventName("Finish");
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
				
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeFinished(resultData, eventInfo);
			}
			*/
		}
		catch (greenFrameDBErrorSignal ne)
		{
			throw new CustomException("", ne.getMessage());
		}
		catch(Exception ex)
		{
			throw new CustomException(ex);
		}
	}
	
	// 2019.04.25_hsryu_Insert Logic. if WorkOrderName of Lot is 'MIXED', Error...
	public void adjustWorkOrder(Lot lotData, String newWorkOrder, EventInfo eventInfo) throws CustomException
	{
		if(!StringUtils.equals(lotData.getProductionType(), "MQCA") && !StringUtils.equals(lotData.getProductRequestName(), newWorkOrder)){
			
			// 2019.05.22_hsryu_Insert Logic. not be Changed 'EventName'..
			EventInfo eventInfoForChangeWO = EventInfoUtil.makeEventInfo("", eventInfo.getEventUser(), "ChangeWorkOrder", null, null);
			eventInfoForChangeWO.setEventTime(eventInfo.getEventTime());
			eventInfoForChangeWO.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			
			//----Check New WorkOrder Quantity && Valid WorkOrder ----//
			ProductRequestKey pKey = new ProductRequestKey();
			pKey.setProductRequestName(newWorkOrder);
			
			ProductRequest pData = null;
			
			try{
				pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
			}
			catch(Throwable e){
				throw new CustomException("PRODUCTREQUEST-0055");
			}
			
			if(pData != null){
				if((pData.getPlanQuantity() - pData.getReleasedQuantity()) < lotData.getProductQuantity()){
					throw new CustomException("PRODUCTREQUEST-0037", pData.getKey().getProductRequestName());
				}
			}
			
			//2019.06.14_hsryu_Add Logic. if RemainQty < Lot.getProductQuantity, Error ! Mantis 0004188.
			this.checkNewWorkOrderRemainQty(pData, lotData.getProductQuantity());
			//------------------------------------------------------//
			
			Map<String ,Integer> woHashMap = new HashMap<String, Integer>();
			
			// Check all products will be scrapped or not.
			List<Product> unscrappedProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());
			
			/********* 2019.04.25_hsryu_adjust Old WO Quantity **********/
			for (Product productData : unscrappedProductList) {
				if(StringUtils.isNotEmpty(productData.getProductRequestName())){
					if(!woHashMap.containsKey(productData.getProductRequestName()))
						woHashMap.put(productData.getProductRequestName(), 1);
					else
						woHashMap.put(productData.getProductRequestName(), (woHashMap.get(productData.getProductRequestName()))+1);
				}
			}
			
			// Adjust WorkOrder Quantity
			for(String key : woHashMap.keySet()){
				MESWorkOrderServiceProxy.getProductRequestServiceUtil().calculateProductRequestQty(key, null, "R", -(woHashMap.get(key)), eventInfoForChangeWO);
			}
			/********************************************************/
			
			/********* 2019.04.25_hsryu_adjust New WO Quantity **********/
			MESWorkOrderServiceProxy.getProductRequestServiceUtil().calculateProductRequestQty(pData.getKey().getProductRequestName(), "R", (long)unscrappedProductList.size(), eventInfoForChangeWO);
			/********************************************************/
		}
	}
	
	/*
	* Name : calculateProductRequestQty
	* Desc : This function is calculateProductRequestQty (Request EDO)
	* Author : dmlee
	* Date : 2018.03.30
	*/
	public void calculateProductRequestQty(String productRequestName, 
							  String calculateType, long calculateQty, EventInfo eventInfo)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{
		try
		{
			/*
			 * -String calculateType-
			 * R : ReleasedQty calculate
			 * F : FinishedQty calculate
			 * S : ScrappedQty calculate
			 */
			
			ProductRequestKey productRequestKey;
			
			//If Don't know ProductRequestPlan Info
			
			productRequestKey = new ProductRequestKey(productRequestName);
			
			ProductRequest resultData;
			
			if(calculateType.equals("R"))
			{
				if(calculateQty > 0)
				{
					eventInfo.setEventName("IncrementReleasedQuantity");
				}
				else
				{
					eventInfo.setEventName("DecrementReleasedQuantity");
				}
				
				//Increment Released Qty
				IncrementReleasedQuantityByInfo incrementReleasedInfo = new IncrementReleasedQuantityByInfo();
				incrementReleasedInfo.setQuantity(calculateQty);

				ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestInfoUtil().getProductRequest(productRequestKey.getProductRequestName());
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementReleasedQuantityBy(productRequestData, incrementReleasedInfo, eventInfo);
				
				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				resultData = MESWorkOrderServiceProxy.getProductRequestInfoUtil().getProductRequest(productRequestKey.getProductRequestName());	
				resultData = ProductRequestServiceProxy.getProductRequestService().selectByKeyForUpdate(productRequestKey);
			}
			else if(calculateType.equals("F"))
			{
				if(calculateQty > 0)
				{
					eventInfo.setEventName("IncrementFinishedQuantity");
				}
				else
				{
					eventInfo.setEventName("DecrementFinishedQuantity");
				}
				
				//Increment Finished Qty
				IncrementFinishedQuantityByInfo incrementFinishedInfo = new IncrementFinishedQuantityByInfo();
				incrementFinishedInfo.setQuantity(calculateQty);
				
				ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestInfoUtil().getProductRequest(productRequestKey.getProductRequestName());
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementFinishedQuantityBy(productRequestData, incrementFinishedInfo, eventInfo);
				
				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				resultData = MESWorkOrderServiceProxy.getProductRequestInfoUtil().getProductRequest(productRequestKey.getProductRequestName());
				resultData = ProductRequestServiceProxy.getProductRequestService().selectByKeyForUpdate(productRequestKey);
			}
			else if(calculateType.equals("S"))
			{	
				//Increment Scrapped Qty
				IncrementScrappedQuantityByInfo incrementScrappedInfo = new IncrementScrappedQuantityByInfo();
				incrementScrappedInfo.setQuantity(calculateQty);
				
				if(calculateQty > 0)
				{
					eventInfo.setEventName("IncrementScrappedQuantity");
				}
				else
				{
					eventInfo.setEventName("DecrementScrappedQuantity");
				}
				
				ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestInfoUtil().getProductRequest(productRequestKey.getProductRequestName());
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementScrappedQuantityBy(productRequestData, incrementScrappedInfo, eventInfo);
				
				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				resultData = MESWorkOrderServiceProxy.getProductRequestInfoUtil().getProductRequest(productRequestKey.getProductRequestName());
				resultData = ProductRequestServiceProxy.getProductRequestService().selectByKeyForUpdate(productRequestKey);
			}
			else if(calculateType.equals("A"))
			{

				if(calculateQty > 0)
				{
					eventInfo.setEventName("IncrementAssignQuantity");
				}
				else
				{
					eventInfo.setEventName("DecrementAssignQuantity");
				}
				
				ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestInfoUtil().getProductRequest(productRequestKey.getProductRequestName());
				ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();
				changeSpecInfo.setProductRequestType(productRequestData.getProductRequestType());
				changeSpecInfo.setFactoryName(productRequestData.getFactoryName());
				changeSpecInfo.setProductSpecName(productRequestData.getProductSpecName());
				changeSpecInfo.setProductSpecVersion(productRequestData.getProductSpecVersion());
				changeSpecInfo.setPlanReleasedTime(productRequestData.getPlanReleasedTime());
				changeSpecInfo.setPlanFinishedTime(productRequestData.getPlanFinishedTime());
				changeSpecInfo.setPlanQuantity(productRequestData.getPlanQuantity());
				changeSpecInfo.setReleasedQuantity(productRequestData.getReleasedQuantity());
				changeSpecInfo.setFinishedQuantity(productRequestData.getFinishedQuantity());
				changeSpecInfo.setScrappedQuantity(productRequestData.getScrappedQuantity());
				changeSpecInfo.setProductRequestState("Assigned");
				changeSpecInfo.setProductRequestHoldState(productRequestData.getProductRequestHoldState());
				productRequestData.getUdfs().put("ASSIGNQUANTITY", Long.toString(calculateQty));
				changeSpecInfo.setUdfs(productRequestData.getUdfs());
				
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().changeSpec(productRequestData, changeSpecInfo, eventInfo, productRequestData.getKey().getProductRequestName());
				
				resultData = ProductRequestServiceProxy.getProductRequestService().selectByKeyForUpdate(productRequestKey);
			}
			else
			{
				throw new CustomException("calcultate Type Error !");
			}
			
			//Set Product Request State
			if(resultData.getProductRequestState().equals(GenericServiceProxy.getConstantMap().Prq_Planned) && resultData.getReleasedQuantity() > 0)
			{
				//WO State : Planned -> Released
				eventInfo.setEventName("Release");
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
				
				MakeReleasedInfo makeReleasedInfo = new MakeReleasedInfo();
				makeReleasedInfo.setUdfs(resultData.getUdfs());
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeReleased(resultData, makeReleasedInfo, eventInfo);
			}
			/*
			else if(resultData.getProductRequestState().equals(GenericServiceProxy.getConstantMap().Prq_Released)
					&& resultData.getPlanQuantity() <= (resultData.getReleasedQuantity()))
			{
				//WO State : Released -> Complete
				eventInfo.setEventName("Complete");
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
				
				MakeCompletedInfo makeCompletedInfo = new MakeCompletedInfo();
				makeCompletedInfo.setUdfs(resultData.getUdfs());
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(resultData, makeCompletedInfo, eventInfo);
			}
			else if(resultData.getProductRequestState().equals(GenericServiceProxy.getConstantMap().Prq_Completed)
					&& resultData.getPlanQuantity() <= (resultData.getFinishedQuantity() + resultData.getScrappedQuantity()))
			{
				//WO State : Complete -> Finished
				eventInfo.setEventName("Finish");
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
				
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeFinished(resultData, eventInfo);
			}
			*/
		}
		catch (greenFrameDBErrorSignal ne)
		{
			throw new CustomException("", ne.getMessage());
		}
		catch(Exception ex)
		{
			throw new CustomException(ex);
		}
	}
	
	//2019.06.14_hsryu_Add Logic. if RemainQty < Lot.getProductQuantity, Error ! 
	private void checkNewWorkOrderRemainQty(ProductRequest woData, Double productQuantity) throws CustomException
	{
		StringBuffer queryBuffer = new StringBuffer()
		.append("SELECT P.PLANQUANTITY - NVL(NRQ.QUANTITY,0) - P.RELEASEDQUANTITY AS REMAINQUANTITY \n")
		.append("  FROM PRODUCTREQUEST P, \n")
		.append(" (SELECT PL.PRODUCTREQUESTNAME,  \n")
		.append(" SUM (PL.PLANQUANTITY - PL.RELEASEDQUANTITY) AS QUANTITY \n")
		.append(" FROM PRODUCTREQUESTPLAN PL \n")
		.append(" WHERE 1=1 \n")
		.append(" GROUP BY PRODUCTREQUESTNAME) NRQ \n")
		.append(" WHERE P.FACTORYNAME = :FACTORYNAME \n")
		.append("       AND P.PRODUCTREQUESTNAME = NRQ.PRODUCTREQUESTNAME(+)  \n")
		.append("       AND P.PRODUCTREQUESTNAME = :PRODUCTREQUESTNAME \n")
		.append("       ORDER BY P.PRODUCTREQUESTNAME \n");

		HashMap<String, Object> bindMap = new HashMap<String, Object>();

		bindMap.put("FACTORYNAME", woData.getFactoryName());
		bindMap.put("PRODUCTREQUESTNAME", woData.getKey().getProductRequestName());

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);

		if(sqlResult.size()!=0)
		{
			int remainQty = Integer.parseInt(sqlResult.get(0).get("REMAINQUANTITY").toString());

			if(remainQty < productQuantity)
			{
				throw new CustomException("PRODUCTREQUEST-0057", woData.getKey().getProductRequestName(), String.valueOf(productQuantity), String.valueOf(remainQty));
			}
		}
	}
 	/*
	 * Name : calculateProductRequestQtyForSTB 
	 * Desc : This function is calculateProductRequestQtyForSTB 
	 * Author : hsryu 
	 * Date : 2020.11.05
	 */
	public void calculateProductRequestQtyForSTB(String productRequestName, String calculateType, long calculateQty, EventInfo eventInfo)
			throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{
		try
		{
			EventInfo eventInfoForWO = EventInfoUtil.makeEventInfo("", eventInfo.getEventUser(), eventInfo.getEventComment(), "", "");
			eventInfoForWO.setEventTime(eventInfo.getEventTime());
			eventInfoForWO.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			ProductRequestKey productRequestKey;
			productRequestKey = new ProductRequestKey(productRequestName);

			ProductRequest resultData;

			if (calculateType.equals("R"))
			{
				if (calculateQty > 0)
				{
					eventInfoForWO.setEventName("IncrementReleasedQuantity");
				}
				else
				{
					eventInfoForWO.setEventName("DecrementReleasedQuantity");
				}

				// Increment Released Qty
				IncrementReleasedQuantityByInfo incrementReleasedInfo = new IncrementReleasedQuantityByInfo();
				incrementReleasedInfo.setQuantity(calculateQty);

				ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestInfoUtil().getProductRequest(
						productRequestKey.getProductRequestName());
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementReleasedQuantityBy(productRequestData, incrementReleasedInfo,
						eventInfoForWO);

				resultData = ProductRequestServiceProxy.getProductRequestService().selectByKeyForUpdate(productRequestKey);
			}
			else if (calculateType.equals("F"))
			{
				if (calculateQty > 0)
				{
					eventInfoForWO.setEventName("IncrementFinishedQuantity");
				}
				else
				{
					eventInfoForWO.setEventName("DecrementFinishedQuantity");
				}

				// Increment Finished Qty
				IncrementFinishedQuantityByInfo incrementFinishedInfo = new IncrementFinishedQuantityByInfo();
				incrementFinishedInfo.setQuantity(calculateQty);

				ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestInfoUtil().getProductRequest(
						productRequestKey.getProductRequestName());
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementFinishedQuantityBy(productRequestData, incrementFinishedInfo,
						eventInfoForWO);

				resultData = ProductRequestServiceProxy.getProductRequestService().selectByKeyForUpdate(productRequestKey);
			}
			else if (calculateType.equals("S"))
			{
				IncrementScrappedQuantityByInfo incrementScrappedInfo = new IncrementScrappedQuantityByInfo();
				incrementScrappedInfo.setQuantity(calculateQty);

				if (calculateQty > 0)
				{
					eventInfoForWO.setEventName("IncrementScrappedQuantity");
				}
				else
				{
					eventInfoForWO.setEventName("DecrementScrappedQuantity");
				}

				ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestInfoUtil().getProductRequest(
						productRequestKey.getProductRequestName());
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementScrappedQuantityBy(productRequestData, incrementScrappedInfo,
						eventInfoForWO);

				resultData = ProductRequestServiceProxy.getProductRequestService().selectByKeyForUpdate(productRequestKey);
			}
			else
			{
				throw new CustomException("calcultate Type Error !");
			}

			if (calculateType.equals("R"))
			{
				if ((resultData.getProductRequestState().equals(GenericServiceProxy.getConstantMap().Prq_Planned) || resultData
						.getProductRequestState().equals(GenericServiceProxy.getConstantMap().Prq_Assigned)))
				{
					if (calculateQty > 0)
					{
						if (resultData.getReleasedQuantity() >= resultData.getPlanQuantity())
						{
							// WO State : Assigned -> Complete
							eventInfoForWO.setEventName("Complete");
							eventInfoForWO.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
							eventInfoForWO.setEventTime(TimeStampUtil.getCurrentTimestamp());

							resultData.setProductRequestState(GenericServiceProxy.getConstantMap().Prq_Completed);
							resultData.setCompleteTime(eventInfoForWO.getEventTime());
							resultData.setCompleteUser(TimeUtils.getCurrentEventTimeKey());

							resultData.setLastEventComment(eventInfoForWO.getEventComment());
							resultData.setLastEventName(eventInfoForWO.getEventName());
							resultData.setLastEventTime(eventInfoForWO.getEventTime());
							resultData.setLastEventTimeKey(eventInfoForWO.getEventTimeKey());
							resultData.setLastEventUser(eventInfoForWO.getEventUser());

							ProductRequestServiceProxy.getProductRequestService().update(resultData);

							MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(resultData, eventInfoForWO);
						}
						else if (resultData.getReleasedQuantity() > 0)
						{
							// WO State : Assigned -> Released
							eventInfoForWO.setEventName("Release");
							eventInfoForWO.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
							eventInfoForWO.setEventTime(TimeStampUtil.getCurrentTimestamp());

							MakeReleasedInfo makeReleasedInfo = new MakeReleasedInfo();
							makeReleasedInfo.setUdfs(resultData.getUdfs());
							MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeReleased(resultData, makeReleasedInfo, eventInfoForWO);
						}
					}
				}
				else if ((resultData.getProductRequestState().equals(GenericServiceProxy.getConstantMap().Prq_Released)))
				{
					if (calculateQty > 0)
					{
						// STB
						if (resultData.getReleasedQuantity() >= resultData.getPlanQuantity())
						{
							// WO State : Released -> Complete
							eventInfoForWO.setEventName("Complete");
							eventInfoForWO.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
							eventInfoForWO.setEventTime(TimeStampUtil.getCurrentTimestamp());

							kr.co.aim.greentrack.productrequest.management.info.MakeCompletedInfo makeCompletedInfo = new kr.co.aim.greentrack.productrequest.management.info.MakeCompletedInfo();
							makeCompletedInfo.setUdfs(resultData.getUdfs());
							MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(resultData, makeCompletedInfo, eventInfoForWO);
						}
					}
					else
					{
						// WO State : Released -> Planned.(To do : Change state
						// 'Planned' -> Assigned')
						if (resultData.getReleasedQuantity() <= 0)
						{
							eventInfoForWO.setEventName("Assign");
							eventInfoForWO.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
							eventInfoForWO.setEventTime(TimeStampUtil.getCurrentTimestamp());

							resultData.setProductRequestState(GenericServiceProxy.getConstantMap().Prq_Assigned);
							resultData.setReleaseTime(null);
							resultData.setReleaseUser(StringUtil.EMPTY);
							resultData.setCompleteTime(null);
							resultData.setCompleteUser(StringUtil.EMPTY);

							resultData.setLastEventComment(eventInfoForWO.getEventComment());
							resultData.setLastEventName(eventInfoForWO.getEventName());
							resultData.setLastEventTime(eventInfoForWO.getEventTime());
							resultData.setLastEventTimeKey(eventInfoForWO.getEventTimeKey());
							resultData.setLastEventUser(eventInfoForWO.getEventUser());

							ProductRequestServiceProxy.getProductRequestService().update(resultData);

							MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(resultData, eventInfoForWO);
						}
					}
				}
				else if ((resultData.getProductRequestState().equals(GenericServiceProxy.getConstantMap().Prq_Completed)))
				{
					if (calculateQty < 0)
					{
						// WO State : Completed -> Assigned.
						if (resultData.getReleasedQuantity() <= 0)
						{
							eventInfoForWO.setEventName("Assign");
							eventInfoForWO.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
							eventInfoForWO.setEventTime(TimeStampUtil.getCurrentTimestamp());

							resultData.setProductRequestState(GenericServiceProxy.getConstantMap().Prq_Assigned);
							resultData.setReleaseTime(null);
							resultData.setReleaseUser(StringUtil.EMPTY);
							resultData.setCompleteTime(null);
							resultData.setCompleteUser(StringUtil.EMPTY);

							resultData.setLastEventComment(eventInfoForWO.getEventComment());
							resultData.setLastEventName(eventInfoForWO.getEventName());
							resultData.setLastEventTime(eventInfoForWO.getEventTime());
							resultData.setLastEventTimeKey(eventInfoForWO.getEventTimeKey());
							resultData.setLastEventUser(eventInfoForWO.getEventUser());

							ProductRequestServiceProxy.getProductRequestService().update(resultData);

							MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(resultData, eventInfoForWO);
						}
						// WO State : Completed -> Release.
						if (resultData.getReleasedQuantity() < resultData.getPlanQuantity())
						{
							// WO State : Assigned -> Released
							eventInfoForWO.setEventName("Release");
							eventInfoForWO.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
							eventInfoForWO.setEventTime(TimeStampUtil.getCurrentTimestamp());

							MakeReleasedInfo makeReleasedInfo = new MakeReleasedInfo();
							makeReleasedInfo.setUdfs(resultData.getUdfs());
							MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeReleased(resultData, makeReleasedInfo, eventInfoForWO);
						}
					}
				}
			}
		}
		catch (greenFrameDBErrorSignal ne)
		{
			throw new CustomException("", ne.getMessage());
		}
		catch (Exception ex)
		{
			throw new CustomException(ex);
		}
	}

}
