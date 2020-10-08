package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LotMultiHold;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.MakeNotOnHoldInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ReleaseHoldLot extends SyncHandler 
{
	public Object doWorks(Document doc) throws CustomException
	{
		// ----------------------------------------------------------------------------------------------------------------------------------
		// Added by smkang on 2019.07.11 - According to Liu Hongwei's request, Lot should be locked to be prevented concurrent executing.
		List<Element> eleLotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);		
		
		eventLog.debug("Lot will be locked to be prevented concurrent executing.");
		Map<String, Lot> lotDataMap = new ConcurrentHashMap<String, Lot>();
		for (Element eleLot : eleLotList) {
			String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
			lotDataMap.put(lotName, LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName)));
		}
		eventLog.debug("Lot is locked to be prevented concurrent executing.");
		// ----------------------------------------------------------------------------------------------------------------------------------
				
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReleaseHold", getEventUser(), getEventComment(), "", "");

		String note = SMessageUtil.getBodyItemValue(doc, "NOTE", true);

		for (Element eleLot : eleLotList)
		{
			String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
			String reasonCode = SMessageUtil.getChildText(eleLot, "REASONCODE", true);
			String department = SMessageUtil.getChildText(eleLot, "USERDEPARTMENT", false);
			String holdUser = SMessageUtil.getChildText(eleLot, "HOLDUSER", false);
			String seq = SMessageUtil.getChildText(eleLot, "SEQ", false);

			// Added by smkang on 2018.08.15 - Need to set EventComment as EventComment of OPI.
			String eventComment = SMessageUtil.getChildText(eleLot, "EVENTCOMMENT", true);
			eventInfo.setEventComment(eventComment);
			
			// Modified by smkang on 2019.07.11 - According to Liu Hongwei's request, Lot should be locked to be prevented concurrent executing.
//			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			Lot lotData = lotDataMap.get(lotName);
			
			//2019.01.28_hsryu_Modify Logic. Mantis 0002608.
			// validation
//			if (!lotData.getLotState().equals(GenericServiceProxy.getConstantMap().Lot_Released))
//			{
//				throw new CustomException("LOT-0016", lotName, lotData.getLotState());
//			}
						
			if(StringUtil.equals(reasonCode, "GHLD"))
			{
				// ResonCode GHLD is Only Released by SYS
				throw new CustomException("COMMON-0001","LotHoldCode is GHLD");
			}
			
			// 2019.04.24_hsryu_Delete Logic. Requested by CIM. if LotProcessState = 'RUN', Can ReleaseHold.
//			if (lotData.getLotProcessState().equalsIgnoreCase("RUN") == true)
//			{
//				//CT_LOTACTION
//				String condition = "WHERE FACTORYNAME = ?"; //PK
//				condition += " AND LOTNAME = ?"; //PK
//				condition += " AND PROCESSFLOWNAME = ?"; //PK
//				condition += " AND PROCESSOPERATIONNAME = ?"; //PK
//				condition += " AND ACTIONSTATE = ?";
//				condition += " AND HOLDCODE = ?";
//
//				Object[] bindSet = new Object[6];
//				bindSet[0] = lotData.getFactoryName();
//				bindSet[1] = lotName;
//				bindSet[2] = lotData.getProcessFlowName();
//				bindSet[3] = lotData.getProcessOperationName();
//				bindSet[4] = GenericServiceProxy.getConstantMap().ACTIONSTATE_CREATED;
//				bindSet[5] = reasonCode;
//				
//				try
//				{
//					List<LotAction> lotActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);
//					LotAction selectedLotAction = lotActionList.get(0);
//					selectedLotAction.setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_EXECUTED);
//					
//					ExtendedObjectProxy.getLotActionService().update(selectedLotAction);
//				}
//				catch(Throwable e)
//				{
//					throw new CustomException("HOLD-0002", lotName,lotData.getProcessFlowName(),lotData.getProcessOperationName(),reasonCode);
//				}
//			}
//			else	
//			{
				if (lotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().Flag_N))
				{
					throw new CustomException("LOT-0021", lotName);
				}
				
				LotMultiHoldRelease(eventInfo,lotName, department, note, reasonCode, seq, eventComment);

				// -------------------------------------------------------------------------------------------------------------------------------------------
				// Modified by smkang on 2018.08.13 - According to user's requirement, LotName/ReasonCode/Department/EventComment are necessary to be keys.
//				Object[] array = new Object[] {lotName, reasonCode};
//				List<LotMultiHold> multiHoldLot = LotServiceProxy.getLotMultiHoldService().select("lotName = ? And reasoncode = ?", array);
//				
//				if( multiHoldLot.size() > 0 )
//				{
//					if (StringUtil.equals(department, "ADMIN") ||
//						StringUtil.equals(department, multiHoldLot.get(0).getUdfs().get("eventuserdep")) ||
//						StringUtil.isEmpty(multiHoldLot.get(0).getUdfs().get("eventuserdep")) )
//					{
//						// delete lot multi hold data
//						LotServiceProxy.getLotMultiHoldService().delete("lotName = ? And reasoncode = ?", array);
//										
//						// delete product multi hold data
//						List <Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
//						for(Product eleProduct : productList)
//						{
//							try
//							{
//								String productName = eleProduct.getKey().getProductName();
//								Object[] array1 = new Object[] {productName, reasonCode};
//								ProductServiceProxy.getProductMultiHoldService().delete("productName = ? And reasoncode = ?", array1);
//							}
//							catch(Exception ex)
//							{
//								eventLog.warn("ProductMultiHold not found");
//							}
//						}
//						
//						List<LotMultiHold> lotMultiHoldlist = null;
//						try
//						{
//							lotMultiHoldlist = LotServiceProxy.getLotMultiHoldService().select("lotname = ? ", new Object[]{lotName});
//						}
//						catch(Exception ex)
//						{
//						}
//						
//						if(lotMultiHoldlist == null)
//						{
//							List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
//							
//							Map<String, String> udfs = CommonUtil.setNamedValueSequence(eleLot, Lot.class.getSimpleName());
//							MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(lotData, productUSequence, udfs);
//							
//							LotServiceProxy.getLotService().makeNotOnHold(lotData.getKey(),	eventInfo, makeNotOnHoldInfo);
//						}
//					}
//					else
//					{
//						throw new CustomException("LOT-9025", lotName, department); 
//					}
//				}
				
				int remainedMultiHoldLotCount = MESLotServiceProxy.getLotServiceImpl().removeMultiHoldLot(lotName, reasonCode, department, seq, eventInfo);
								
				// delete product multi hold data
				List <Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
				for(Product eleProduct : productList) {
					try	{
						MESProductServiceProxy.getProductServiceImpl().removeMultiHoldProduct(eleProduct.getKey().getProductName(), reasonCode, department, seq, eventInfo);
					} catch(Exception ex) {
						eventLog.warn(eleProduct.getKey().getProductName() + "'s ProductMultiHold not found");
					}
				}
				
				eventInfo.setEventComment("ReleaseHoldLot");
				
				if (remainedMultiHoldLotCount == 0) {

					String HoldDuration = GetLotHoldDuration(eventInfo,lotName);
					
					List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
					
//					String holdDepartment = MESLotServiceProxy.getLotServiceImpl().getHoldDepartment(lotData.getKey().getLotName());
					Map<String, String> udfs = CommonUtil.setNamedValueSequence(eleLot, Lot.class.getSimpleName());
					// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//					udfs.put("NOTE", note);
//					udfs.put("HOLDTIME",StringUtils.EMPTY);
//					udfs.put("HOLDRELEASETIME",eventInfo.getEventTime().toString());
//					udfs.put("HOLDDURATION",HoldDuration);
//					udfs.put("HOLDDEPARTMENT", holdDepartment);
					
					eventInfo.setEventComment(eventInfo.getEventComment() + "/ lotName : " + lotName+" reasonCode : " +reasonCode+" department : " +department+" holdUser : " + holdUser);
					
					MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(lotData, productUSequence, udfs);
					makeNotOnHoldInfo.getUdfs().put("NOTE", note);
					makeNotOnHoldInfo.getUdfs().put("HOLDTIME", StringUtils.EMPTY);
					makeNotOnHoldInfo.getUdfs().put("HOLDRELEASETIME", eventInfo.getEventTime().toString());
					makeNotOnHoldInfo.getUdfs().put("HOLDDURATION", HoldDuration);
					LotServiceProxy.getLotService().makeNotOnHold(lotData.getKey(),	eventInfo, makeNotOnHoldInfo);
					
					// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//					lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//					
//					//Note clear - YJYU
//					Map<String, String> udfs_note = lotData.getUdfs();
//					udfs_note.put("NOTE", "");
//					
//					//2019.02.19_hsryu_Insert Logic. Mantis 0002775.
//					udfs_note.put("HOLDRELEASETIME", StringUtils.EMPTY);
//					udfs_note.put("HOLDDURATION", StringUtils.EMPTY);
//					LotServiceProxy.getLotService().update(lotData);
					Map<String, String> updateUdfs = new HashMap<String, String>();
					updateUdfs.put("NOTE", "");
					updateUdfs.put("HOLDRELEASETIME", "");
					updateUdfs.put("HOLDDURATION", "");
					MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
					
					lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
					
					eventInfo.setEventComment("ReleaseHoldLot");

					//aHoldFlag = MESLotServiceProxy.getLotServiceUtil().isExistAhold(lotName, lotData.getProcessFlowName(), lotData.getProcessOperationName());

					if(!StringUtils.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed))
					{
						// 2019.05.30_Delete Logic. 
//						if(!MESLotServiceProxy.getLotServiceUtil().checkSampling(lotName,eventInfo))
//						{
//							if(!MESLotServiceProxy.getLotServiceUtil().isExistBholdorOperHold(lotName, eventInfo))
//							{
//								//Reserve Change
//								lotData = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionChange(lotData.getKey().getLotName(), eventInfo);
//							}
//						}
						
						// 2019.05.30_hsryu_Modify Logic. if TrackOut and Sampling, Not Execute BHold. So, Check Sampling and BHold at the same time.
						boolean isCheckSampling = false;
						
						isCheckSampling = MESLotServiceProxy.getLotServiceUtil().checkSampling(lotName,eventInfo);
						
						if(!MESLotServiceProxy.getLotServiceUtil().isExistBholdorOperHold(lotName, eventInfo) && !isCheckSampling ){
							lotData = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionChange(lotData.getKey().getLotName(), eventInfo);
						}
					}
				}
				else{
//					String holdDepartment = MESLotServiceProxy.getLotServiceImpl().getHoldDepartment(lotData.getKey().getLotName());
					
					SetEventInfo setEventInfo = new SetEventInfo();
					// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//					Map<String, String> udfs = lotData.getUdfs();
//					udfs.put("NOTE", note);
//					udfs.put("HOLDRELEASETIME",eventInfo.getEventTime().toString());
					setEventInfo.getUdfs().put("NOTE", note);
					setEventInfo.getUdfs().put("HOLDRELEASETIME", eventInfo.getEventTime().toString());
					//2019.02.19_hsryu_Modify Logic. Mantis 0002775.
					//udfs.put("HOLDDURATION",HoldDuration);
//					udfs.put("HOLDDEPARTMENT", holdDepartment);
					
					eventInfo.setEventComment(eventInfo.getEventComment() + "/ lotName : " + lotName+" reasonCode : " +reasonCode+" department : " +department+" holdUser : " + holdUser);
					LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
				}
				// -------------------------------------------------------------------------------------------------------------------------------------------

				// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//				lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//				
//				//Note clear - YJYU
//				Map<String, String> udfs_note = lotData.getUdfs();
//				udfs_note.put("NOTE", "");
//				LotServiceProxy.getLotService().update(lotData);
				Map<String, String> updateUdfs = new HashMap<String, String>();
				updateUdfs.put("NOTE", "");
				MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
			}
//		}
		
		return doc;
	}
	
	private boolean checkRemainholdType(String lotName, String holdType) throws CustomException
	{
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		String condition = "lotName = ? and holdType = ? ";
		Object[] bindSet = new Object[]{lotData.getKey().getLotName(), holdType};

		List<LotMultiHold> lotMultiHoldList;
		try
		{
			lotMultiHoldList = ExtendedObjectProxy.getLotMultiHoldService().select(condition, bindSet);
		}
		catch(Exception ex)
		{
			return true;
		}
		
		if(lotMultiHoldList.size()>0)
		{
			return false;
		}
		
		return false;
	}
	
	private void LotMultiHoldRelease(EventInfo eventInfo, String lotName, String department, String note, String reasonCode, String seq, String eventComment) 
	{
		try
		{
			/*** 2019.03.27_hsryu_if department is null, Change " ". ***/
			if(StringUtils.isEmpty(department)){
				department = " ";
			}
			
			// 2019.04.25_hsryu_Modify Logic. Add select "HOLDTIMEKEY". Requested by Report.
			String strSql = 
				"    SELECT A.LOTNAME  " +
				"    , A.PROCESSOPERATIONNAME  "+ 
				"    , A.CARRIERNAME  "+
				"    , A.PRODUCTQUANTITY  "+ 
				"    , A.LOTHOLDSTATE  "+
				"    , A.LOTSTATE  "+
				"    , A.PRODUCTSPECNAME  "+ 
				"    , A.ECCODE  "+
				"    , A.PROCESSFLOWNAME  "+ 
				"    , A.PRODUCTREQUESTNAME  "+ 
				"    , TO_CHAR(B.EVENTTIME,'YYYY-MM-DD HH24:MI:SS') HOLDTIME  "+ 
				"    , B.EVENTUSER HOLDUSER  "+ 
				"    , B.EVENTNAME HOLDEVENTNAME  "+ 
				"    , B.REASONCODE HOLDREASONCODE  "+ 
				"    , B.DEPARTMENT HOLDDEPT    "+
				"    , B.EVENTCOMMENT HOLDNOTE  "+
				"    , B.EVENTTIMEKEY HOLDTIMEKEY  "+
				"  FROM  LOT A  "+
				"   INNER JOIN CT_LOTMULTIHOLD B ON A.LOTNAME = B.LOTNAME   "+ 
				" WHERE A.LOTNAME = :LOTNAME  "+
				"  AND B.SEQ = :SEQ  "+
				"  AND B.DEPARTMENT = :DEPARTMENT  "+
				"  AND B.EVENTCOMMENT = :EVENTCOMMENT  "+
				"  AND B.REASONCODE = :REASONCODE  "; 
				   
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTNAME", lotName);
			bindMap.put("REASONCODE", reasonCode);
			bindMap.put("DEPARTMENT", department);
			bindMap.put("SEQ", seq);
			bindMap.put("EVENTCOMMENT", eventComment);

			List<Map<String, Object>> HoldList = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindMap);

			if(HoldList != null && HoldList.size() > 0)
			{
				String  sLOTNAME = lotName;  
				String  sSeq = seq;
				String  sProcessoperationName = (String)HoldList.get(0).get("PROCESSOPERATIONNAME");
				String  sReleaseUser = eventInfo.getEventUser();
				String  sReleaseDept = this.getDepartmentByUser(eventInfo.getEventUser());
				String  sCarrierName = (String)HoldList.get(0).get("CARRIERNAME"); 
				String  sProductQuantity = HoldList.get(0).get("PRODUCTQUANTITY").toString(); 
				
				String  sLotholdState = (String)HoldList.get(0).get("LOTHOLDSTATE");  
				String  sLotState = (String)HoldList.get(0).get("LOTSTATE");
				String  sProductspecName = (String)HoldList.get(0).get("PRODUCTSPECNAME");
				String  sEcCode = (String)HoldList.get(0).get("ECCODE"); 
				String  sProcessflowName = (String)HoldList.get(0).get("PROCESSFLOWNAME");
				String  sProductrequestName = (String)HoldList.get(0).get("PRODUCTREQUESTNAME");

				String  sReleaseNote = note;
				String  sHoldTime = (String)HoldList.get(0).get("HOLDTIME") +".000";
				
				String  sHoldUser = (String)HoldList.get(0).get("HOLDUSER"); 
				String  sHoldeventName = (String)HoldList.get(0).get("HOLDEVENTNAME");
				String  sHoldreasonCode = reasonCode;  
				String  sHoldDept = (String)HoldList.get(0).get("HOLDDEPT");
				String  sHoldNote = (String)HoldList.get(0).get("HOLDNOTE");
				String  sHoldTimeKey = (String)HoldList.get(0).get("HOLDTIMEKEY");
				
				/*** 2019.03.27_hsryu_add SEQ Column. ***/ 
				String sql = "INSERT INTO CT_LOTMULTIHOLDRELEASE "
						+ " (LOTNAME, SEQ, PROCESSOPERATIONNAME, RELEASETIME, RELEASEUSER, RELEASEDEPT, CARRIERNAME, PRODUCTQUANTITY,HOLDTIME"
						+ "  ,LOTHOLDSTATE, LOTSTATE, PRODUCTSPECNAME, ECCODE, PROCESSFLOWNAME, PRODUCTREQUESTNAME"
						+ "  ,RELEASENOTE, HOLDUSER, HOLDEVENTNAME, HOLDREASONCODE, HOLDDEPT, HOLDNOTE "
						+ "  ,LASTEVENTNAME,LASTEVENTTIMEKEY,LASTEVENTTIME,LASTEVENTUSER,LASTEVENTCOMMENT, HOLDTIMEKEY "
						+ "  )"
 						+ " VALUES "
						+ " (:lotName, :seq, :processoperationName, :releaseTime, :releaseUser, :releaseDept, :carrierName, :productQuantity, :holdTime"
						+ "  ,:lotholdState, :lotState, :productspecName, :ecCode, :processflowName, :productrequestName "
						+ "  ,:releaseNote, :holdUser, :holdeventName, :holdreasonCode, :holdDept, :holdNote "
						+ "  ,:lasteventName, :lasteventTimekey, :lasteventTime, :lasteventUser, :lasteventCommet, :holdTimeKey "
						+ "  ) "; 
 				
				Map<String,Object> InsertbindMap = new HashMap<String,Object>();
	
				InsertbindMap.put("lotName", sLOTNAME);
				
				InsertbindMap.put("seq", sSeq);
				InsertbindMap.put("processoperationName", sProcessoperationName);
				InsertbindMap.put("releaseTime", eventInfo.getEventTime());
				InsertbindMap.put("releaseUser", sReleaseUser);
				InsertbindMap.put("releaseDept", sReleaseDept);
				InsertbindMap.put("carrierName", sCarrierName);
				InsertbindMap.put("productQuantity", sProductQuantity);
				InsertbindMap.put("holdTime", TimeStampUtil.getTimestamp(sHoldTime ));
				
				InsertbindMap.put("lotholdState", sLotholdState);
				InsertbindMap.put("lotState", sLotState);
				InsertbindMap.put("productspecName", sProductspecName);
				InsertbindMap.put("ecCode", sEcCode);
				InsertbindMap.put("processflowName", sProcessflowName);
				InsertbindMap.put("productrequestName", sProductrequestName);
				
				InsertbindMap.put("releaseNote", sReleaseNote);	
				InsertbindMap.put("holdUser", sHoldUser);
				InsertbindMap.put("holdeventName", sHoldeventName);
				InsertbindMap.put("holdreasonCode", sHoldreasonCode);
				InsertbindMap.put("holdDept", sHoldDept);
				InsertbindMap.put("holdNote", sHoldNote);
	
				InsertbindMap.put("lasteventName", eventInfo.getEventName());
				InsertbindMap.put("lasteventTimekey", eventInfo.getEventTimeKey());
				InsertbindMap.put("lasteventUser", eventInfo.getEventUser()) ;
				InsertbindMap.put("lasteventCommet", eventInfo.getEventComment());
				InsertbindMap.put("lasteventTime", eventInfo.getEventTime());
				InsertbindMap.put("holdTimeKey", sHoldTimeKey);
						
				long rv = GenericServiceProxy.getSqlMesTemplate().update(sql, InsertbindMap);
			}
		}
		catch (Exception ex)
		{
			eventLog.warn("CT_LOTMULTIHOLDRELEASE ERROR");
		}
	}
	
	//2019.03.27_hsryu_Add Logic. 
	private String getDepartmentByUser(String eventUser) {
		try {
			
 			String sql = "SELECT DEPARTMENT "
					   + "  FROM USERPROFILE"
					   + " WHERE USERID = :USERID"; 
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("USERID", eventUser);
			
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

			if(sqlResult.size() > 0) {
				return (String)sqlResult.get(0).get("DEPARTMENT");
			}
			
			return "";

		}
		catch(Throwable e){
			eventLog.warn("Fail getDepartmentByUser");
			return "";
		}
	}
	
	
	private String GetLotHoldDuration(EventInfo eventInfo, String lotName) 
	{
		try
		{
 			String sql = "SELECT ROUND((TO_DATE(:EVENTTIME, :EVENTTIMEFORMAT) - HOLDTIME)*24*60*60,2) HOLDDURATION"
					   + "  FROM LOT"
					   + " WHERE LOTNAME = :LOTNAME"; 
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTNAME", lotName);
			bindMap.put("EVENTTIME",  eventInfo.getEventTime().toString().substring(0,19));
			bindMap.put("EVENTTIMEFORMAT",  "yyyy-MM-dd HH24:mi:ss");

			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			
			String sDuration = "";
				
			if(sqlResult != null && sqlResult.size() > 0)
			{
				sDuration = sqlResult.get(0).get("HOLDDURATION").toString();
			}
			else
			{
				sDuration = "0";
			}
			
			return sDuration;
		}
		catch (Exception ex)
		{
			eventLog.warn("GetLotHoldDuration ERROR");		
			return "0";
		}
	}
	
	
	
}