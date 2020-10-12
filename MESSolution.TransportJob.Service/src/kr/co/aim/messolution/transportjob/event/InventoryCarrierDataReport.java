package kr.co.aim.messolution.transportjob.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class InventoryCarrierDataReport extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
//		Case 1. MCS reports a carrier data and the carrier is existed in MES database, but location of the carrier is different.
//		        - MES updates location with MCS data.
//		Case 2. MCS reports a carrier data, but it isn't existed in MES database.
//		        - MES can't create the carrier because durable information is not enough to create a carrier.
//		Case 3. MCS doesn't report a carrier but the carrier isn't existed in the machine of MES database.
//		        - MES deletes location.
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);		
		List<Element> carrierElementList = SMessageUtil.getBodySequenceItem(doc, "CARRIERLIST", true).getChildren();
		Element eCarrierList = SMessageUtil.getBodySequenceItem(doc, "CARRIERLIST", true);
		
		if (carrierElementList != null && carrierElementList.size() > 0) {
			EventInfo eventInfo = null;
			
			List<String> mcsCarrierNames = new ArrayList<String>();
			for (Element carrierElement : carrierElementList) {
				try {
					eventInfo = EventInfoUtil.makeEventInfo(SMessageUtil.getMessageName(doc), this.getEventUser(), this.getEventComment(), "", "");
					
					String carrierName = carrierElement.getChildText("CARRIERNAME");
					String positionType = carrierElement.getChildText("CURRENTPOSITIONTYPE");
					String positionName = carrierElement.getChildText("CURRENTPOSITIONNAME");
					String zoneName = carrierElement.getChildText("CURRENTZONENAME");
					String carrierRestrictedStatus = carrierElement.getChildText("CARRIERRESTRICTEDSTATUS"); //ADD BYJHIYING ON20191125 MANTIS:5257
					
					//mcsCarrierNames.add(carrierName);// modfiy by GJJ on 20200407 start mantis:5985
					
					// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
					Durable carrierData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(carrierName));
					mcsCarrierNames.add(carrierName);// add by GJJ on 20200407 start mantis:5985
					
					try {
						// Case 1. MCS reports a carrier data and the carrier is existed in MES database, but location of the carrier is different.
						if(!StringUtils.equals(carrierData.getUdfs().get("MACHINENAME"), machineName) || 
							!StringUtils.equals(carrierData.getUdfs().get("POSITIONTYPE"), positionType) ||
							!StringUtils.equals(carrierData.getUdfs().get("POSITIONNAME"), positionName) ||
							!StringUtils.equals(carrierData.getUdfs().get("ZONENAME"), zoneName)||
							!StringUtils.equals(carrierData.getUdfs().get("CARRIERRESTRICTEDSTATUS"), carrierRestrictedStatus)) //MODIFY BY JHIYING ON20191203 
						{
							// Commented by smkang on 2018.05.06 - Need to update FactoryName and AreaName and using common method.
							String transportState = MESTransportServiceProxy.getTransportJobServiceUtil().judgeTransportState(machineName, positionType);
							
							// Modified by smkang on 2019.06.26 - Avoid concurrent report of InventoryCarrierDataReport and InventoryZoneDataReport.
//							MESTransportServiceProxy.getTransportJobServiceImpl().changeCurrentCarrierLocation(carrierData, machineName, positionType, positionName, zoneName, transportState, "", eventInfo);
							// MODIFY BY JHIYING ON 20191125 MANTIS:5257
			//				MESTransportServiceProxy.getTransportJobServiceImpl().changeCurrentCarrierLocationWithoutCalculateShelfCount(carrierData, machineName, positionType, positionName, zoneName, transportState, "", eventInfo);
							MESTransportServiceProxy.getTransportJobServiceImpl().changeCurrentCarrierLocationWithoutCalculateShelfCountV2(carrierData, machineName, positionType, positionName, zoneName, transportState, "", eventInfo,carrierRestrictedStatus);
							
						}
					} catch (InvalidStateTransitionSignal ie) {
						eventLog.error(ie);
					} catch (FrameworkErrorSignal fe) {
						eventLog.error(fe);
					} catch (NotFoundSignal ne) {
						eventLog.error(ne);
					}
				//} catch (CustomException e) {// modfiy by GJJ on 20200407 start mantis:5985
				} catch (Exception e) {// add by GJJ on 20200407 start mantis:5985
					eventLog.warn(e);
					// TODO: handle exception
					// Commented by smkang on 2018.05.06 - Although any port has problem in for loop, another ports should be updated.
					//									   So CustomException handler is added here.
					// Case 2. MCS reports a carrier data, but it isn't existed in MES database.
				}
			}
			
			// Case 3. MCS doesn't report a carrier but the carrier isn't existed in the machine of MES database.
			List<Durable> carrierList = MESDurableServiceProxy.getDurableServiceUtil().getCarrierListByEQP(machineName, mcsCarrierNames);
			removeLocationInfo(doc, carrierList);
			
			//add inventoryCarreirData -only insert, not use(H4)
			CT_inventoryCarrierData(doc, eCarrierList);
			CT_inventoryCarrierDataHistory(doc, eCarrierList);
		} else {
			// Case 3. MCS doesn't report a carrier but the carrier isn't existed in the machine of MES database.
			List<Durable> carrierList = MESDurableServiceProxy.getDurableServiceUtil().getCarrierListByEQP(machineName, null);
			removeLocationInfo(doc, carrierList);
		}
		
		// Added by smkang on 2018.07.03 - Need to forward a message to linked factory.
		// Modified by smkang on 2018.10.23 - According to EDO's request, inventory data and machine control state should be synchronized with shared factory without CT_SHIPPINGSTOCKER.
//		MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToShippingShop(doc, machineName);
		//MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToSharedShop(doc);
	}
	
	private void CT_inventoryCarrierDataHistory(Document doc, Element eCarrierList) {
		try{
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String sql = "INSERT INTO CT_INVENTORYCARRIERDATAHISTORY " 
					   + "(SELECT MACHINENAME, CARRIERNAME, CARRIERSTATE, CURRENTPOSITIONTYPE, CURRENTPOSITIONNAME, "
					   + "		  CURRENTZONENAME, LASTEVENTNAME AS EVENTNAME, LASTEVENTTIMEKEY AS TIMEKEY, "
					   + "	      LASTEVENTUSER AS EVENTUSER, LASTEVENTCOMMENT AS EVENTCOMMENT, DURABLEHOLDSTATE "
					   + "	 FROM CT_INVENTORYCARRIERDATA WHERE MACHINENAME = :machineName ) ";

			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("machineName", machineName);
			greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql, bindMap);
		}catch(Exception e){}
	}

	private void CT_inventoryCarrierData(Document doc, Element carrierList) throws CustomException {
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(SMessageUtil.getMessageName(doc), this.getEventUser(), this.getEventComment(), "", "");
		
		if ( carrierList != null  )
		{
			String delSql = "DELETE FROM CT_INVENTORYCARRIERDATA WHERE MACHINENAME = :machineName " ;
			
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("machineName", machineName);

			greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(delSql, bindMap);
			
			String carrierName = "";
			String currentPositionType = "";
			String currentPositionName = "";
			String currentZoneName = "";
			String carrierState = "";
			String durableHoldState = "";
			String eventName = eventInfo.getEventName().toString();
			String eventUser = eventInfo.getEventUser().toString();
			String eventComment = eventInfo.getEventComment().toString();
			String lastEventTimeKey = TimeStampUtil.getCurrentEventTimeKey();
			
			StringBuilder sql = new StringBuilder();
			sql.append("INSERT INTO CT_INVENTORYCARRIERDATA ");
			sql.append(" ( MACHINENAME, CARRIERNAME, CURRENTPOSITIONTYPE, ");
			sql.append("   CURRENTPOSITIONNAME, CURRENTZONENAME, CARRIERSTATE, DURABLEHOLDSTATE, ");
			sql.append("   LASTEVENTTIMEKEY, LASTEVENTNAME, LASTEVENTUSER, LASTEVENTCOMMENT ) ");
			sql.append(" VALUES ");
			sql.append(" ( :machineName , :carrierName , :currentPositionType , ");
			sql.append("   :currentPositionName , :currentZoneName , :carrierState , :durableHoldState, ");
			sql.append("   :lastEventTimeKey , :lastEventName , :lastEventUser , :lastEventComment ) ");
			
			StringBuilder updateSql = new StringBuilder();
			updateSql.append("UPDATE CT_INVENTORYCARRIERDATA ");
			updateSql.append("   SET CARRIERSTATE = :carrierState, ");
			updateSql.append("       CURRENTPOSITIONTYPE = :currentPositionType, ");
			updateSql.append("       CURRENTPOSITIONNAME = :currentPositionName, ");
			updateSql.append("       CURRENTZONENAME = :currentZoneName, ");
			updateSql.append("       DURABLEHOLDSTATE = :durableHoldState, ");
			updateSql.append("       LASTEVENTNAME = :lastEventName, ");
			updateSql.append("       LASTEVENTTIMEKEY = :lastEventTimeKey, ");
			updateSql.append("       LASTEVENTUSER = :lastEventUser, ");
			updateSql.append("       LASTEVENTCOMMENT = :lastEventComment ");
			updateSql.append(" WHERE MACHINENAME = :machineName ");
			updateSql.append("   AND CARRIERNAME = :carrierName ");
			
			for ( Iterator<?> iterator = carrierList.getChildren().iterator(); iterator.hasNext(); )
			{
				Element carrierE = (Element) iterator.next();
				carrierName = carrierE.getChild("CARRIERNAME").getText();
				currentPositionType = carrierE.getChild("CURRENTPOSITIONTYPE").getText();
				currentPositionName = carrierE.getChild("CURRENTPOSITIONNAME").getText();
				currentZoneName = carrierE.getChild("CURRENTZONENAME").getText();
				carrierState = carrierE.getChild("CARRIERSTATE").getText();
				durableHoldState = carrierE.getChild("DURABLEHOLDSTATE").getText();
				
				bindMap = new HashMap<String, String>();
				bindMap.put("machineName", machineName);
				bindMap.put("carrierName", carrierName);
				bindMap.put("currentPositionType", currentPositionType);
				bindMap.put("currentPositionName", currentPositionName);
				bindMap.put("currentZoneName", currentZoneName);
				bindMap.put("carrierState", carrierState);
				bindMap.put("lastEventTimeKey", lastEventTimeKey);
				bindMap.put("lastEventName", eventName);
				bindMap.put("lastEventUser", eventUser);
				bindMap.put("lastEventComment", eventComment);
				bindMap.put("durableHoldState", durableHoldState);
				
				try{
					// Insert
					greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql.toString(), bindMap);
				}catch(Exception e){
					// Update - if there is exist data
					greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(updateSql.toString(), bindMap);
				}
				
			}
		}
		
	}

	// Added by smkang on 2018.05.06 - MCS doesn't report a carrier but the carrier isn't existed in the machine of MES database.
	//								   MES deletes location.
	private void removeLocationInfo(Document doc, List<Durable> carrierList) {
		if (carrierList != null && carrierList.size() > 0) {
			EventInfo eventInfo = null;
			
			for (Durable carrierData : carrierList) {
				try {
					eventInfo = EventInfoUtil.makeEventInfo(SMessageUtil.getMessageName(doc), this.getEventUser(), this.getEventComment(), "", "");
					
					// Commented by smkang on 2018.05.06 - Need to update FactoryName and AreaName and using common method.
					String transportState = MESTransportServiceProxy.getTransportJobServiceUtil().judgeTransportState("", "");
					
					// Modified by smkang on 2019.06.26 - Avoid concurrent report of InventoryCarrierDataReport and InventoryZoneDataReport.
//					MESTransportServiceProxy.getTransportJobServiceImpl().changeCurrentCarrierLocation(carrierData, "", "", "",	"", transportState, "", eventInfo);
					MESTransportServiceProxy.getTransportJobServiceImpl().changeCurrentCarrierLocationWithoutCalculateShelfCount(carrierData, "", "", "",	"", transportState, "", eventInfo);
				} catch (Exception e) {
					// TODO: handle exception
					eventLog.error(e);
				}
			}
		}
	}
}