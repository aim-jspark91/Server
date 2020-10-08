package kr.co.aim.messolution.productrequest.event.CNX;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ProductSpecIdleTime;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequest.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.productrequestplan.ProductRequestPlanServiceProxy;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlan;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlanKey;
import kr.co.aim.greentrack.productrequestplan.management.info.CreateInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ReserveProductRequest extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{	
		String sTimeKey = TimeUtils.getCurrentEventTimeKey();
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Reserve", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(sTimeKey);

		String assignedMachineName = SMessageUtil.getBodyItemValue(doc, "ASSIGNEDMACHINENAME", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);

		//Get Current Reserved Product Request List
		List<ProductRequestPlan> currentBook = getPlanListByAssignedMachineNameByWO(assignedMachineName,productRequestName, factoryName);

		ProductRequestPlan PlanOldData = this.getFirstPlanByMachine(assignedMachineName);

		ProductRequestKey pKey = new ProductRequestKey(productRequestName);
		ProductRequest pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
		
		//ADD BY JHYING ON2020.03.02

		ProductSpec prdSpec = GenericServiceProxy.getSpecUtil().getProductSpec(pData.getFactoryName(), pData.getProductSpecName(),GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
		 
		/*ProductSpec prdSpec1 = ProductServiceProxy.getProductSpecService().select("WHERE FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND  PRODUCTSPECVERSION = ? "
                ,new Object[]{pData.getFactoryName(),pData.getProductSpecName(),prdSpec.getKey().getProductSpecVersion()}).get(0);*/
		if (prdSpec.getUdfs().get("CUT1XAXISCOUNT").equals("0") || prdSpec.getUdfs().get("CUT1YAXISCOUNT").equals("0") || prdSpec.getUdfs().get("CUT2YAXISCOUNT").equals("0")
				|| prdSpec.getUdfs().get("CUT2XAXISCOUNT").equals("0"))
			throw new CustomException("PRODUCTSPEC-9003", pData.getProductSpecName());

		//ADD BY JHYING ON2020.03.02
		
		// 2019.08.23 Add By Park Jeong Su
		ProductSpecIdleTime productSpecIdleTime =null;
		try {
			productSpecIdleTime = ExtendedObjectProxy.getProductSpecIdleTimeService().selectByKey(false, new Object[]{pData.getProductSpecName()});
		} catch (Exception e) {
			eventLog.info("productSpecIdleTime Not Exist");
		}
		
		try {
			if(StringUtils.equals("Y", productSpecIdleTime.getValidflag())){
				Date lastRunTime = new Date(productSpecIdleTime.getLastruntime().getTime()); // get LastRunTime
				eventLog.info("lastRunTime : "+lastRunTime.toString());
				Date currentTime = new Date(); // get Current Time
				eventLog.info("lastRunTime : "+currentTime.toString());
				long IdleTime = Long.parseLong(productSpecIdleTime.getIdletime());
				eventLog.info("Diff minute :"+(currentTime.getTime() - lastRunTime.getTime()) / ( 60 * 1000)+" IdleTime : "+IdleTime);
				if(productSpecIdleTime!=null && (currentTime.getTime() - lastRunTime.getTime()) / ( 60 * 1000) > IdleTime ){
					throw new CustomException("PRODUCTSPECIDLETIME-0001", pData.getProductSpecName());
				}
			}
		} catch (CustomException ce) {
			if(StringUtil.equals(ce.errorDef.getErrorCode(), "PRODUCTSPECIDLETIME-0001")){
				throw new CustomException("PRODUCTSPECIDLETIME-0001", pData.getProductSpecName());
			}
			eventLog.info("Another PRODUCTSPECIDLETIME-0001");
		}
		catch (Exception e){
			eventLog.info("Another Error!");
		}
		// 2019.08.23 Add By Park Jeong Su
		
		for(Element element : SMessageUtil.getBodySequenceItemList(doc, "PRODUCTREQUESTPLANLIST", false))
		{
			//String sPosition = SMessageUtil.getChildText(element, "POSITION", false);
			String sProductRequestName = SMessageUtil.getChildText(element, "PRODUCTREQUESTNAME", false);
			String sPlanReleasedTime = SMessageUtil.getChildText(element, "PLANRELEASEDTIME", false);
			String sPlanQuantity = SMessageUtil.getChildText(element, "PLANQUANTITY", false);
			String sProcessFlowName = SMessageUtil.getChildText(element, "PROCESSFLOWNAME", false);
			String sECCode = SMessageUtil.getChildText(element, "ECCODE", false);
			String sDepartmentName = SMessageUtil.getChildText(element, "DEPARTMENTNAME", false);
			String sPriority = SMessageUtil.getChildText(element, "PRIORITY", false);

			// String => Timestemp
			Date date = new Date();
			DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				date = sdf.parse(sPlanReleasedTime);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Timestamp tPlanReleasedTime = new Timestamp(date.getTime());

			//ProductRequestKey p2Key = new ProductRequestKey(sProductRequestName);
			//ProductRequest p2Data = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);

			//Check Data
			//Can't Reserve if Product Request is OnHold
			/*if(pData.getProductRequestHoldState().equals("Y"))
			{
				throw new CustomException("PRODUCTREQUEST-0001", sProductRequestName);
			}*/

			//1.Check Plan Released Time
			if(tPlanReleasedTime.before(pData.getPlanReleasedTime()))
			{
				throw new CustomException("PRODUCTREQUEST-0006", "");
			}

			//2.Check Product Request State
			//			if(pData.getProductRequestState().equals(GenericServiceProxy.getConstantMap().Prq_Completed))
			//			{
			//				throw new CustomException("PRODUCTREQUEST-0003", sProductRequestName, GenericServiceProxy.getConstantMap().Prq_Completed);
			//			}

			//3.Check Plan Quantity
			if(Integer.valueOf(sPlanQuantity) <= 0)
			{
				throw new CustomException("PRODUCTREQUEST-0008", sPlanQuantity);
			}

			ProductRequestPlan reservedPlan = new ProductRequestPlan();
			ProductRequestPlanKey pPlanKey = new ProductRequestPlanKey(sProductRequestName, assignedMachineName, tPlanReleasedTime);

			//If Exist, Update. else Create.
			try
			{
				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				reservedPlan = ProductRequestPlanServiceProxy.getProductRequestPlanService().selectByKey(pPlanKey);
				reservedPlan = ProductRequestPlanServiceProxy.getProductRequestPlanService().selectByKeyForUpdate(pPlanKey);

				if(reservedPlan.getProductRequestPlanState().equals(GenericServiceProxy.getConstantMap().PrqPlan_Completed))
				{
					continue;
					//throw new CustomException("PRODUCTREQUEST-0025", reservedPlan.getKey().getProductRequestName() + "/" + reservedPlan.getKey().getAssignedMachineName() + "/" + reservedPlan.getKey().getPlanReleasedTime());
				}

				if(!StringUtils.equals(reservedPlan.getKey().getPlanReleasedTime().toString(), sPlanReleasedTime))
				{
					//if(!StringUtil.equals(String.valueOf(oldPosition), "0") && Integer.parseInt(sPosition) <= oldPosition)
					//{
					//	throw new CustomException( "PRODUCTREQUEST-0024" , PlanOldData.getKey().getProductRequestName() + "/" + PlanOldData.getKey().getAssignedMachineName() + "/" + PlanOldData.getKey().getPlanReleasedTime());
					//}
					int lastPosition = 0;

					reservedPlan.setLastEventName("Reserve");
					reservedPlan.setPlanQuantity(Long.valueOf(sPlanQuantity));
					
					// Modified by smkang on 2019.05.17 - EventInfo.LastEventTimekey is null.
//					reservedPlan.setLastEventTimeKey(eventInfo.getLastEventTimekey());
					reservedPlan.setLastEventTimeKey(eventInfo.getEventTimeKey());
					
					reservedPlan.setLastEventTime(eventInfo.getEventTime());
					reservedPlan.setLastEventUser(eventInfo.getEventUser());
					reservedPlan.setLastEventComment(eventInfo.getEventComment());

					lastPosition = GetPositionByWOPlan(assignedMachineName);
					Map<String, String> udfs = reservedPlan.getUdfs();
					udfs.put("position", String.valueOf(lastPosition+1));
					reservedPlan.setUdfs(udfs);

					ProductRequestPlanServiceProxy.getProductRequestPlanService().update(reservedPlan);
					MESWorkOrderServiceProxy.getProductRequestServiceUtil().addPlanHistory(reservedPlan, eventInfo);
				}
			}
			catch (NotFoundSignal ne)
			{
				int	lastPosition = GetPositionByWOPlan(assignedMachineName);

				//4.Check Plan Quantity2
				this.checkQty(pData,Integer.parseInt(sPlanQuantity));

				CreateInfo createInfo = new CreateInfo();
				createInfo.setAssignedMachineName(assignedMachineName);
				createInfo.setPlanFinishedTime(pData.getPlanFinishedTime());
				createInfo.setPlanQuantity(Long.valueOf(sPlanQuantity));
				createInfo.setPlanReleasedTime(tPlanReleasedTime);
				createInfo.setProductRequestName(sProductRequestName);

				Map<String, String> udfs = new HashMap<String, String>();
				udfs.put("position", String.valueOf(lastPosition+1));
				udfs.put("processFlowName", sProcessFlowName);
				udfs.put("ECCode", sECCode);
				udfs.put("departmentName", sDepartmentName);
				udfs.put("priority", sPriority);
				createInfo.setUdfs(udfs);

				eventInfo.setEventName("Reserve");
				reservedPlan = ProductRequestPlanServiceProxy.getProductRequestPlanService().create(pPlanKey, eventInfo, createInfo);

				reservedPlan.setProductRequestPlanState(GenericServiceProxy.getConstantMap().Prq_Planned);
				reservedPlan.setProductRequestPlanHoldState(pData.getProductRequestHoldState());
				ProductRequestPlanServiceProxy.getProductRequestPlanService().update(reservedPlan);

				//Add PlanHistory
				MESWorkOrderServiceProxy.getProductRequestServiceUtil().addPlanHistory(reservedPlan, eventInfo);

				//2018.02.12 dmlee : Make Product Request State = 'Planned'
				if(pData.getProductRequestState().equals(GenericServiceProxy.getConstantMap().Prq_Created))
				{
					pData.setProductRequestState(GenericServiceProxy.getConstantMap().Prq_Planned);

					eventInfo.setEventName("Reserve");
					ProductRequestServiceProxy.getProductRequestService().update(pData);

					//  Add History
					MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(pData, eventInfo);
				}
			}
			catch (CustomException ex)
			{
				if(ex.errorDef.getErrorCode().equals("PRODUCTREQUEST-0006"))
				{
					throw new CustomException(ex.errorDef.getErrorCode(), "");
				}

				else if(ex.errorDef.getErrorCode().equals("PRODUCTREQUEST-0008"))
				{
					throw new CustomException(ex.errorDef.getErrorCode(), sPlanQuantity);
				}
				else if(ex.errorDef.getErrorCode().equals("PRODUCTREQUEST-0025"))
				{
					throw new CustomException(ex.errorDef.getErrorCode(), reservedPlan.getKey().getProductRequestName() + "/" + reservedPlan.getKey().getAssignedMachineName() + "/" + reservedPlan.getKey().getPlanReleasedTime());
				}
				else if(ex.errorDef.getErrorCode().equals("PRODUCTREQUEST-0024"))
				{
					throw new CustomException(ex.errorDef.getErrorCode() , PlanOldData.getKey().getProductRequestName() + "/" + PlanOldData.getKey().getAssignedMachineName() + "/" + PlanOldData.getKey().getPlanReleasedTime());					
				}
				else
				{
					throw new CustomException("PRODUCTREQUEST-0025", reservedPlan.getKey().getProductRequestName() + "/" + reservedPlan.getKey().getAssignedMachineName() + "/" + reservedPlan.getKey().getPlanReleasedTime());
				}				
			}
			catch (Exception ex)
			{

			}
			finally
			{
				//remove delivered data
				for (int idx=0; idx < currentBook.size(); idx++)
				{
					if (reservedPlan != null 
							&& currentBook.get(idx).getKey().getProductRequestName().equals(reservedPlan.getKey().getProductRequestName())
							&& currentBook.get(idx).getKey().getPlanReleasedTime().equals(reservedPlan.getKey().getPlanReleasedTime())
							&& currentBook.get(idx).getKey().getAssignedMachineName().equals(reservedPlan.getKey().getAssignedMachineName()))
					{				
						currentBook.remove(idx);
					}
				}
			}
		}

		//unassigned from book with no longer existing reservations
		for (ProductRequestPlan data : currentBook)
		{
			getLotList(data);

			eventInfo.setEventName("CancelReserve");
			ProductRequestPlanServiceProxy.getProductRequestPlanService().remove(data.getKey());
			//Add PlanHistory
			MESWorkOrderServiceProxy.getProductRequestServiceUtil().addPlanHistory(data, eventInfo);	

			//2018.02.12 dmlee : Make Product Request State = 'Created'
			if(!chkReservedPlan(data.getKey().getProductRequestName()))
			{
				ProductRequestKey objKey = new ProductRequestKey();
				objKey.setProductRequestName(data.getKey().getProductRequestName());
				ProductRequest WOData = ProductRequestServiceProxy.getProductRequestService().selectByKey(objKey);

				if(WOData.getProductRequestState().equals(GenericServiceProxy.getConstantMap().Prq_Planned))
				{
					if(!chkPlanStateReservedPlan(data.getKey().getProductRequestName()))
					{
						WOData.setProductRequestState(GenericServiceProxy.getConstantMap().Prq_Created);

						eventInfo.setEventName("CancelReserve");
						ProductRequestServiceProxy.getProductRequestService().update(WOData);

						//  Add History
						MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(WOData, eventInfo);

					}
				}
			}

		}

//		if(createFlag == true)
//		{
//			pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
//			if(StringUtil.equals(pData.getProductRequestHoldState(), GenericServiceProxy.getConstantMap().Prq_OnHold))
//			{
//				pData.setProductRequestHoldState(GenericServiceProxy.getConstantMap().Prq_OnHold);
//				ProductRequestServiceProxy.getProductRequestService().update(pData);	
//			}
//		}

		//this.validateOrderSequence(assignedMachineName);

		return doc;
	}

	private void ChangeWorkOrderToComfirmed(ProductRequest pData)
			throws CustomException
	{
		//ChangeSpecInfo
		ChangeSpecInfo changespecInfo = new ChangeSpecInfo();
		changespecInfo.setFactoryName(pData.getFactoryName());
		changespecInfo.setProductSpecName(pData.getProductSpecName());
		changespecInfo.setProductRequestType(pData.getProductRequestType());

		changespecInfo.setPlanQuantity(Long.valueOf(pData.getPlanQuantity()));
		changespecInfo.setProductRequestState(pData.getProductRequestState());

		changespecInfo.setPlanReleasedTime(pData.getPlanReleasedTime());
		changespecInfo.setPlanFinishedTime(pData.getPlanFinishedTime());

		HashMap<String, String> productRequestUserColumns = new HashMap<String, String>();
		productRequestUserColumns.put("confirmState", "Confirmed");

		changespecInfo.setUdfs(productRequestUserColumns);

		//EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeProductRequest", this.getEventUser(), this.getEventComment(), "", "");
		ProductRequest resultData = ProductRequestServiceProxy.getProductRequestService().changeSpec(pData.getKey(), eventInfo, changespecInfo);

		//Add History
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(resultData, eventInfo);

		//1) Insert MESProductRequest WO Data
		//			if(ProductRequestServiceUtil.CheckERPWorkOrder(pData.getKey().getProductRequestName()))
		//			{
		//			    ProductRequestServiceUtil.WriteMESProductRequest(pData.getKey().getProductRequestName());
		//			}
	}


	private List<Map<String, Object>> getLotList(ProductRequestPlan pPlan) throws CustomException 
	{
		try 
		{
			String sql = "SELECT L.LOTNAME FROM LOT L, CT_RESERVELOT R WHERE L.LOTNAME = R.LOTNAME " 
					//+ " AND R.RESERVESTATE <> :reserveState " 
					+ " AND R.PRODUCTREQUESTNAME = :productRequestName"
					+ " AND R.MACHINENAME = :machineName AND R.PLANRELEASEDTIME = :planReleasedTime "; 
			
			Map bindMap = new HashMap<String, Object>();
			bindMap.put("productRequestName", pPlan.getKey().getProductRequestName());
			bindMap.put("machineName", pPlan.getKey().getAssignedMachineName());
			bindMap.put("planReleasedTime", pPlan.getKey().getPlanReleasedTime());
			//bindMap.put("reserveState", "Completed");

			List<Map<String, Object>> lotList = new ArrayList<Map<String, Object>>();

			lotList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

			if(lotList.size() > 0)
				throw new CustomException("PRODUCTREQUEST-0028", pPlan.getKey().getProductRequestName());

			return lotList;
		} 
		catch (greenFrameDBErrorSignal ex) 
		{
			// throw new CustomException();
		} 
		catch (Exception ex) 
		{
			throw new CustomException("PRODUCTREQUEST-0028", pPlan.getKey().getProductRequestName());
		}

		return new ArrayList<Map<String, Object>>();
	}

	private long getAllPlanQuantityByProductRequestName(
			String productRequestName) throws CustomException {
		try {
			long planQuantity = 0;

			String condition = "ProductRequestName = ?";
			Object bindSet[] = new Object[] { productRequestName };

			List<ProductRequestPlan> productRequestPlanList = ProductRequestPlanServiceProxy
					.getProductRequestPlanService().select(condition, bindSet);

			if (productRequestPlanList.size() > 0) {
				for (int i = 0; i < productRequestPlanList.size(); i++) {
					ProductRequestPlan objPlan = productRequestPlanList.get(i);

					planQuantity += objPlan.getPlanQuantity();
				}
			}

			return planQuantity;
		} catch (greenFrameDBErrorSignal ex) {
			// throw new CustomException();
		} catch (Exception ex) {
			// throw new CustomException();
		}

		return 0;
	}

	/**
	 * getFirstPlanByMachine
	 * 151105 by xzquan : service object changed
	 * @author xzquan
	 * @since 2015.11.08
	 * @param eventInfo
	 * @param machineName
	 * @throws CustomException
	 */
	private ProductRequestPlan getFirstPlanByMachine(String machineName)
			throws CustomException
	{
		ProductRequestPlan pPlan = null;

		try
		{
			String condition = "assignedMachineName = ? and productRequestPlanState IN (?, ?) "
					+ "and position = (select max(position) from productRequestPlan Where assignedMachineName = ? and productRequestPlanState IN (?, ?))";
			Object bindSet[] = new Object[]{machineName, "Completing", "Started", machineName, "Completing", "Started"};
			List<ProductRequestPlan> pPlanList = ProductRequestPlanServiceProxy.getProductRequestPlanService().select(condition, bindSet);

			if(pPlanList.size() > 1 && pPlanList != null)
			{
				throw new CustomException("PRODUCTREQUEST-0022", machineName);
			}

			pPlan = pPlanList.get(0);
		}
		catch(Exception ex)
		{
			eventLog.info("Not Found ProductRequestPlan");
		}
		return pPlan;

	}

	private boolean chkReservedPlan(String ProductRequestName) throws CustomException
	{
		ProductRequestPlan pPlan = null;

		try
		{
			String condition = "RELEASEDQUANTITY > 0 AND PRODUCTREQUESTPLANSTATE = ? AND PRODUCTREQUESTNAME = ?";
			Object bindSet[] = new Object[]{GenericServiceProxy.getConstantMap().Prq_Released, ProductRequestName};

			List<ProductRequestPlan> pPlanList = ProductRequestPlanServiceProxy.getProductRequestPlanService().select(condition, bindSet);

			//Exist Released Glass in WO
			return true;

		}
		catch(NotFoundSignal ex)
		{
			//Not Exist Released Glass in WO
			return false;
		}
		catch(FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "ProductRequestPlan", fe.getMessage());
		}
	}

	//add 2018.05.04 hsryu 
	private boolean chkPlanStateReservedPlan(String ProductRequestName) throws CustomException
	{
		ProductRequestPlan pPlan = null;

		try
		{
			String condition = "PRODUCTREQUESTPLANSTATE = ? AND PRODUCTREQUESTNAME = ?";
			Object bindSet[] = new Object[]{GenericServiceProxy.getConstantMap().Prq_Planned, ProductRequestName};

			List<ProductRequestPlan> pPlanList = ProductRequestPlanServiceProxy.getProductRequestPlanService().select(condition, bindSet);

			return true;

		}
		catch(NotFoundSignal ex)
		{
			//Not Exist Planned Glass in WO
			return false;
		}
		catch(FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "ProductRequestPlan", fe.getMessage());
		}
	}

	private int GetPositionByWOPlan(String assignedMachineName)
	{
		StringBuffer queryBuffer = new StringBuffer()
		.append("SELECT POSITION FROM PRODUCTREQUESTPLAN  \n")
		.append("  WHERE 1=1 \n")
		.append("  AND ASSIGNEDMACHINENAME = :ASSIGNEDMACHINENAME \n")
		.append(" AND NOT PRODUCTREQUESTPLANSTATE = :PRODUCTREQUESTSTATE  \n")
		.append(" ORDER BY POSITION DESC \n");

		HashMap<String, Object> bindMap = new HashMap<String, Object>();

		bindMap.put("ASSIGNEDMACHINENAME", assignedMachineName);
		bindMap.put("PRODUCTREQUESTSTATE", "Completed");

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);

		if(sqlResult.size()!=0)
		{
			return Integer.parseInt(sqlResult.get(0).get("POSITION").toString());
		}
		else
		{
			return 0;
		}

	}

	//add 2018.05.04 hsryu 
	private void checkQty(ProductRequest woData, int planQty) throws CustomException
	{
		StringBuffer queryBuffer = new StringBuffer()
		.append("SELECT P.PLANQUANTITY - NVL(NRQ.QUANTITY,0) - P.RELEASEDQUANTITY AS REMAINQUANTITY, \n")
		.append("  NVL(NRQ.SUMPLANQUANTITY,0) AS SUMPLANQUANTITY \n")
		.append("  FROM PRODUCTREQUEST P, \n")
		.append(" (SELECT PL.PRODUCTREQUESTNAME,  \n")
		.append(" SUM(PL.PLANQUANTITY) AS SUMPLANQUANTITY, SUM (PL.PLANQUANTITY - PL.RELEASEDQUANTITY) AS QUANTITY \n")
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
			int sumPlanQty = Integer.parseInt(sqlResult.get(0).get("SUMPLANQUANTITY").toString());
			int remainQty = Integer.parseInt(sqlResult.get(0).get("REMAINQUANTITY").toString());

			// sum PlanQuantity in PlanWO > PlanQty in WO -> Error!
			if(sumPlanQty > woData.getPlanQuantity())
			{
				throw new CustomException("WORKORDER-0005");
			}

			// 2019.06.14_hsryu_Delete Logic. for ChangeWO Situation. Mantis 0004188.
//			if( sumPlanQty + planQty > woData.getPlanQuantity())
//			{
//				throw new CustomException("WORKORDER-0006", woData.getKey().getProductRequestName(), String.valueOf(planQty), String.valueOf(sumPlanQty), woData.getPlanQuantity());
//			}

			// RemainQty < 0 -> Error! 
			if(remainQty < 0)
			{
				throw new CustomException("WORKORDER-0004");
			}

			if(remainQty < planQty)
			{
				throw new CustomException("WORKORDER-0007", woData.getKey().getProductRequestName(), String.valueOf(planQty), String.valueOf(remainQty));
			}
		}
	}

	private List<ProductRequestPlan> getPlanListByAssignedMachineNameByWO(
			String assignedMachineName, String productRequestName, String factoryName) throws CustomException {
		try {
			String condition = "Where assignedMachineName = ? and productRequestName = ? and productRequestPlanState != ?";

			Object bindSet[] = new Object[] { assignedMachineName, productRequestName, GenericServiceProxy.getConstantMap().PrqPlan_Completed };

			List<ProductRequestPlan> productRequestPlanList = ProductRequestPlanServiceProxy
					.getProductRequestPlanService().select(condition, bindSet);

			return productRequestPlanList;
		} catch (greenFrameDBErrorSignal ex) {
			// throw new CustomException();
		} catch (Exception ex) {
			// throw new CustomException();
		}

		return new ArrayList<ProductRequestPlan>();
	}
}
