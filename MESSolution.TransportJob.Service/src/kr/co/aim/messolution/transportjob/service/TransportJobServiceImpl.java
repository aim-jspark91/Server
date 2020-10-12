package kr.co.aim.messolution.transportjob.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetAreaInfo;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class TransportJobServiceImpl implements ApplicationContextAware{
	/**
	 * @uml.property  name="applicationContext"
	 * @uml.associationEnd  
	 */
	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog(TransportJobServiceImpl.class);
			
	/**
	 * @param arg0
	 * @throws BeansException
	 * @uml.property  name="applicationContext"
	 */
	public void setApplicationContext(ApplicationContext arg0)
	throws BeansException 
	{
		applicationContext = arg0;
	}
	
	// Added by smkang on 2018.05.05 - Move from TransportJobServiceUtil, because this method modifies database information.
	/*
	* Name : changeCarrierCurrentLocation
	* Desc : This function is change Carrier CurrentLocation
	* Author : 
	* Date : 2011.04.08 
	*/
	public Durable changeCurrentCarrierLocation(Durable durableData, String currentMachineName, String currentPositionType, 
												String currentPositionName, String currentZoneName, String transferState,
												String transportLockFlag, EventInfo eventInfo) throws CustomException													
	{
		// Added by smkang on 2018.12.07 - When a carrier is changed location, UsedShelfCount or EmptyShelfCount of a stocker is changed.
		ExtendedObjectProxy.getDspStockerZoneService().calculateShelfCount(eventInfo, durableData.getUdfs().get("MACHINENAME"), durableData.getUdfs().get("ZONENAME"), "", "");
		
		durableData = changeCurrentCarrierLocationWithoutCalculateShelfCount(durableData, currentMachineName, currentPositionType, currentPositionName, currentZoneName, transferState, transportLockFlag, eventInfo);
		
		// Added by smkang on 2018.12.07 - When a carrier is changed location, UsedShelfCount or EmptyShelfCount of a stocker is changed.
		ExtendedObjectProxy.getDspStockerZoneService().calculateShelfCount(eventInfo, "", "", currentMachineName, currentZoneName);
		
		return durableData;
	}
	
	
	// start  modify by jhiying on20191125 mantis:5257 add CARRIERRESTRICTEDSTATUS column in durable table
	public Durable changeCurrentCarrierLocationV2(Durable durableData, String currentMachineName, String currentPositionType, 
			                                      String currentPositionName, String currentZoneName, String transferState,
			                                      String transportLockFlag, EventInfo eventInfo,String carrierRestrictedStatus ) throws CustomException	
	{
		
		ExtendedObjectProxy.getDspStockerZoneService().calculateShelfCount(eventInfo, durableData.getUdfs().get("MACHINENAME"), durableData.getUdfs().get("ZONENAME"), "", "");
				
		durableData = changeCurrentCarrierLocationWithoutCalculateShelfCountV2(durableData, currentMachineName, currentPositionType, currentPositionName, currentZoneName, transferState, transportLockFlag, eventInfo,carrierRestrictedStatus);
				
		// Added by smkang on 2018.12.07 - When a carrier is changed location, UsedShelfCount or EmptyShelfCount of a stocker is changed.
		ExtendedObjectProxy.getDspStockerZoneService().calculateShelfCount(eventInfo, "", "", currentMachineName, currentZoneName);
				
		return durableData;
	}		                                      
	
	// end  modify by jhiying on20191125 mantis:5257 add CARRIERRESTRICTEDSTATUS column in durable table
	/**
	 * @author smkang
	 * @since 2019.06.26
	 * @param durableData
	 * @param currentMachineName
	 * @param currentPositionType
	 * @param currentPositionName
	 * @param currentZoneName
	 * @param transferState
	 * @param transportLockFlag
	 * @param eventInfo
	 * @return Durable
	 * @throws CustomException
	 * @see Avoid concurrent report of InventoryCarrierDataReport and InventoryZoneDataReport.
	 */
	// start  modify by jhiying on20191125 mantis:5257 add CARRIERRESTRICTEDSTATUS column in durable table
	 public Durable changeCurrentCarrierLocationWithoutCalculateShelfCountV2(Durable durableData, String currentMachineName, String currentPositionType, 
			                                                                    String currentPositionName, String currentZoneName, String transferState,
			                                                                    String transportLockFlag, EventInfo eventInfo, String carrierRestrictedStatus) throws CustomException	
	 {

			SetEventInfo setEventInfo = new SetEventInfo();
			Map<String, String> durableUdfs = setEventInfo.getUdfs();
			
			durableUdfs.put("MACHINENAME", currentMachineName);
			durableUdfs.put("POSITIONTYPE", currentPositionType);
			
			durableUdfs.put("POSITIONNAME", currentPositionName);
			
			durableUdfs.put("CARRIERRESTRICTEDSTATUS", carrierRestrictedStatus);// add by jhiying on20191125
			try {
				MachineSpec machineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(currentMachineName));
				
				if (StringUtils.equals(machineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine) && 
						currentPositionType.equals(GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
					durableUdfs.put("PORTNAME", currentPositionName);
				else
					durableUdfs.put("PORTNAME", "");
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			//YJYU STOCKERINTIME START
			
			String sql = " SELECT DESTINATIONMACHINENAME DESTINATIONMACHINENAME "
					+ "     FROM CT_TRANSPORTJOBCOMMAND "
					+ "    WHERE CARRIERNAME = :carrierName ";
			//+ "     AND JOBSTATE <> ''Completed'' ";
			
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("carrierName", durableData.getKey().getDurableName());
			
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			
			if(sqlResult.size() > 0)
			{
				for(int i=0; i<sqlResult.size(); i++)
				{
					String DestinationMachineName = sqlResult.get(i).get("DESTINATIONMACHINENAME").toString();
					
					// Modified by smkang on 2018.12.30 - Need to invoke equals method.
//					if(currentMachineName == DestinationMachineName)// 케리어 네임으로 조회 했을때 있으면 목적지가 현재 설비라면 업데이트
					if(StringUtils.equals(currentMachineName, DestinationMachineName)) { // 케리어 네임으로 조회 했을때 있으면 목적지가 현재 설비라면 업데이트
						durableUdfs.put("STOCKERINTIME",eventInfo.getEventTime().toString());
					}
				}
			}
			else//job에 없다면,케리어 네임으로만 조회 했을때 없으면 업데이트
			{
				durableUdfs.put("STOCKERINTIME",eventInfo.getEventTime().toString());
			}
			
			//YJYU STOCKERINTIME END
			
			durableUdfs.put("ZONENAME", currentZoneName);
			durableUdfs.put("TRANSPORTSTATE", transferState);
			
			if(StringUtil.isNotEmpty(transportLockFlag))
				durableUdfs.put("TRANSPORTLOCKFLAG", transportLockFlag);
			

			try {
				Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(currentMachineName);
				
				if ((StringUtils.isNotEmpty(machineData.getFactoryName()) && !machineData.getFactoryName().equals(durableData.getFactoryName())) || 
						(StringUtils.isNotEmpty(machineData.getAreaName()) && !machineData.getAreaName().equals(durableData.getAreaName()))) {
					
					durableData.setFactoryName(machineData.getFactoryName());
					durableData.setAreaName(machineData.getAreaName());
					
					DurableServiceProxy.getDurableService().update(durableData);
				}
			} catch(Exception e) {
			}
			
			DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
			
			return durableData;
		
	 }		                                                                    
	// end  modify by jhiying on20191125 mantis:5257
	public Durable changeCurrentCarrierLocationWithoutCalculateShelfCount(Durable durableData, String currentMachineName, String currentPositionType, 
																		  String currentPositionName, String currentZoneName, String transferState,
																		  String transportLockFlag, EventInfo eventInfo) throws CustomException													
	{
		// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//		Map<String, String> durableUdfs = durableData.getUdfs();
		SetEventInfo setEventInfo = new SetEventInfo();
		Map<String, String> durableUdfs = setEventInfo.getUdfs();
		
		durableUdfs.put("MACHINENAME", currentMachineName);
		durableUdfs.put("POSITIONTYPE", currentPositionType);
		
		// Modified by smkang on 2018.05.06 - UnitName and PortName of Durable is used for ProductionMachine, PositionName of Durable is used for StorageMachine. 
//		if(currentPositionType.equals(GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
//		{
//			durableUdfs.put("PORTNAME", currentPositionName);
//			durableUdfs.put("POSITIONNAME", "");
//		}
//		else
//		{
//			durableUdfs.put("PORTNAME", "");
//			durableUdfs.put("POSITIONNAME", currentPositionName);
//		}
		durableUdfs.put("POSITIONNAME", currentPositionName);
		
		// Added by smkang on 2018.05.28 - According to Hongwei's request, if current location is not a port of a production machine, PORTNAME would be deleted.
		// Modified by smkang on 2018.05.31 - According to Hongwei's request, if a carrier is arrived to EQ port or departed from EQ port, MES would update PortName of Durable table.
//		try{
//			MachineSpec machineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(currentMachineName));
//			
//			if (!currentPositionType.equals(GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT) || 
//				!StringUtils.equals(machineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine))
//				durableUdfs.put("PORTNAME", "");
//		}
//		catch(Exception e)
//		{}
		try {
			MachineSpec machineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(currentMachineName));
			
			if (StringUtils.equals(machineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine) && 
					currentPositionType.equals(GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
				durableUdfs.put("PORTNAME", currentPositionName);
			else
				durableUdfs.put("PORTNAME", "");
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		//YJYU STOCKERINTIME START
		
		String sql = " SELECT DESTINATIONMACHINENAME DESTINATIONMACHINENAME "
				+ "     FROM CT_TRANSPORTJOBCOMMAND "
				+ "    WHERE CARRIERNAME = :carrierName ";
		//+ "     AND JOBSTATE <> ''Completed'' ";
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("carrierName", durableData.getKey().getDurableName());
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		if(sqlResult.size() > 0)
		{
			for(int i=0; i<sqlResult.size(); i++)
			{
				String DestinationMachineName = sqlResult.get(i).get("DESTINATIONMACHINENAME").toString();
				
				// Modified by smkang on 2018.12.30 - Need to invoke equals method.
//				if(currentMachineName == DestinationMachineName)// 케리어 네임으로 조회 했을때 있으면 목적지가 현재 설비라면 업데이트
				if(StringUtils.equals(currentMachineName, DestinationMachineName)) { // 케리어 네임으로 조회 했을때 있으면 목적지가 현재 설비라면 업데이트
					durableUdfs.put("STOCKERINTIME",eventInfo.getEventTime().toString());
				}
			}
		}
		else//job에 없다면,케리어 네임으로만 조회 했을때 없으면 업데이트
		{
			durableUdfs.put("STOCKERINTIME",eventInfo.getEventTime().toString());
		}
		
		//YJYU STOCKERINTIME END
		
		durableUdfs.put("ZONENAME", currentZoneName);
		durableUdfs.put("TRANSPORTSTATE", transferState);
		
		if(StringUtil.isNotEmpty(transportLockFlag))
			durableUdfs.put("TRANSPORTLOCKFLAG", transportLockFlag);
		
		// Modified by smkang on 2018.05.06 - Why durableData1 is necessary and assign durableData to durableData1?
//		Durable durableData1=durableData;
//		durableData1.setFactoryName(machineData.getFactoryName());
//		DurableServiceProxy.getDurableService().update(durableData1);
		// Modified by smkang on 2018.10.26 - According to Hongwei's request, FactoryName of a carrier shouldn't be deleted.
		// Modified by smkang on 2018.10.31 - When location of a carrier is deleted, below logic has a problem.
//		if (StringUtils.isNotEmpty(machineData.getFactoryName()) && StringUtils.isNotEmpty(machineData.getAreaName())) {
//			durableData.setFactoryName(machineData.getFactoryName());
//			DurableServiceProxy.getDurableService().update(durableData);
//			
//			// SetArea Info 
//			SetAreaInfo setAreaInfo = new SetAreaInfo();
//			setAreaInfo.setAreaName(machineData.getAreaName());
//			setAreaInfo.setUdfs(durableUdfs);
//			
//			durableData = DurableServiceProxy.getDurableService().setArea(durableData.getKey(), eventInfo, setAreaInfo);
//		}
		try {
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(currentMachineName);
			
			if ((StringUtils.isNotEmpty(machineData.getFactoryName()) && !machineData.getFactoryName().equals(durableData.getFactoryName())) || 
					(StringUtils.isNotEmpty(machineData.getAreaName()) && !machineData.getAreaName().equals(durableData.getAreaName()))) {
				
				durableData.setFactoryName(machineData.getFactoryName());
				durableData.setAreaName(machineData.getAreaName());
				
				DurableServiceProxy.getDurableService().update(durableData);
			}
		} catch(Exception e) {
		}
		
		DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
		
		return durableData;
	}
	
	// Added by smkang on 2018.05.05 - Move from TransportJobServiceUtil, because this method modifies database information.
	/*
	* Name : changeCarrierCurrentLocation
	* Desc : This function is change Carrier CurrentLocation
	* Author : 
	* Date : 2011.04.08 
	*/
	public TransportJobCommand updateTransportJobCommand(String transportJobName, Document doc, EventInfo eventInfo) throws CustomException													
	{
		// Modified by smkang on 2019.03.18 - Useless Code.
//		List<TransportJobCommand> sqlResult = ExtendedObjectProxy.getTransportJobCommandService().select("TRANSPORTJOBNAME = ?", new Object[] {transportJobName});
//		MESTransportServiceProxy.getTransportJobServiceUtil().checkExistTransportJobCommand(sqlResult, transportJobName);
//		TransportJobCommand transportJobCommandInfo = sqlResult.get(0);
		TransportJobCommand transportJobCommandInfo = ExtendedObjectProxy.getTransportJobCommandService().selectByKey(false, new Object[] {transportJobName});
		
		String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);
		String jobState = MESTransportServiceProxy.getTransportJobServiceUtil().getJobState(messageName, doc);	
		String cancelState = MESTransportServiceProxy.getTransportJobServiceUtil().getCancelState(messageName, doc);
		String changeState = MESTransportServiceProxy.getTransportJobServiceUtil().getChangeState(messageName, doc);
		
		transportJobCommandInfo.setJobState(jobState);
		transportJobCommandInfo.setCancelState(cancelState);
		transportJobCommandInfo.setChangeState(changeState);
		
		// Set REASONCODE
		transportJobCommandInfo.setReasonCode(SMessageUtil.getBodyItemValue(doc, "REASONCODE", false));
		
		//2018.12.29 dmlee : Transfer REasonMessage
//		String reasonMessage = this.transferReasonMessage(SMessageUtil.getBodyItemValue(doc, "REASONCOMMENT", false));
//		transportJobCommandInfo.setReasonMessage(reasonMessage);
		//Modified By jhy On 2020.05.1 Mantis:6067
		if (StringUtil.equals(messageName, "TransportJobCompleted"))
		{
			String reasonMessage = this.transferReasonMessage(SMessageUtil.getBodyItemValue(doc, "REASONCOMMENT", false));
			transportJobCommandInfo.setReasonMessage(reasonMessage);
		}
		else
		{
			String reasonMessage = this.transferReasonMessage(SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false));
			transportJobCommandInfo.setReasonMessage(reasonMessage);
		}
		
		//2018.12.29 dmlee : -----------------------
		

		// Set LASTRESULTCODE
		transportJobCommandInfo.setLastEventResultCode(SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false));
		transportJobCommandInfo.setLastEventResultText(SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false));
		
		// Set EventInfo
		transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceUtil().setTransportJobCommandEventInfo(transportJobCommandInfo, eventInfo);
			
		Element bodyElement = doc.getDocument().getRootElement().getChild("Body");
		for ( Iterator iterator = bodyElement.getChildren().iterator(); iterator.hasNext(); )
		{
			Element bodyItem = (Element) iterator.next();
			String itemName = bodyItem.getName().toString();
			String itemValue = bodyElement.getChildText(itemName);
			
			// Modified by smkang on 2019.04.08 - If MES receives ChangeDestinationReply with NG, destination should be changed with old destination.
//			if(itemName.equals("NEWDESTINATIONMACHINENAME"))
//				transportJobCommandInfo.setDestinationMachineName(itemValue);
//			
//			if(itemName.equals("NEWDESTINATIONPOSITIONTYPE"))
//				transportJobCommandInfo.setDestinationPositionType(itemValue);
//			
//			if(itemName.equals("NEWDESTINATIONPOSITIONNAME"))
//				transportJobCommandInfo.setDestinationPositionName(itemValue);
//			
//			if(itemName.equals("NEWDESTINATIONZONENAME"))
//				transportJobCommandInfo.setDestinationZoneName(itemValue);
			if (StringUtils.equals(eventInfo.getEventName(), "ChangeReject")) {
				if(itemName.equals("OLDDESTINATIONMACHINENAME"))
					transportJobCommandInfo.setDestinationMachineName(itemValue);
				
				if(itemName.equals("OLDDESTINATIONPOSITIONTYPE"))
					transportJobCommandInfo.setDestinationPositionType(itemValue);
				
				if(itemName.equals("OLDDESTINATIONPOSITIONNAME"))
					transportJobCommandInfo.setDestinationPositionName(itemValue);
				
				if(itemName.equals("OLDDESTINATIONZONENAME"))
					transportJobCommandInfo.setDestinationZoneName(itemValue);
			} else {
				if(itemName.equals("NEWDESTINATIONMACHINENAME"))
					transportJobCommandInfo.setDestinationMachineName(itemValue);
				
				if(itemName.equals("NEWDESTINATIONPOSITIONTYPE"))
					transportJobCommandInfo.setDestinationPositionType(itemValue);
				
				if(itemName.equals("NEWDESTINATIONPOSITIONNAME"))
					transportJobCommandInfo.setDestinationPositionName(itemValue);
				
				if(itemName.equals("NEWDESTINATIONZONENAME"))
					transportJobCommandInfo.setDestinationZoneName(itemValue);
			}
			
			if(itemName.equals("PRIORITY"))
				transportJobCommandInfo.setPriority(itemValue);
			
			if(itemName.equals("CURRENTMACHINENAME"))
				transportJobCommandInfo.setCurrentMachineName(itemValue);
			
			if(itemName.equals("CURRENTPOSITIONTYPE"))
				transportJobCommandInfo.setCurrentPositionType(itemValue);
			
			if(itemName.equals("CURRENTPOSITIONNAME"))
				transportJobCommandInfo.setCurrentPositionName(itemValue);
			
			if(itemName.equals("CURRENTZONENAME"))
				transportJobCommandInfo.setCurrentZoneName(itemValue);
			
			if(itemName.equals("TRANSFERSTATE"))
				transportJobCommandInfo.setTransferState(itemValue);
			
			// Deleted by smkang on 2018.05.07 - CARRIERSTATE of MCS can be incorrect.
//			if(itemName.equals("CARRIERSTATE"))
//				transportJobCommandInfo.setCarrierState(itemValue);
			
			if(itemName.equals("ALTERNATEFLAG"))
				transportJobCommandInfo.setAlternateFlag(itemValue);
		}
		
		try
		{
			// Modified by smkang on 2019.01.29 - Rejected, Completed or Terminated job will be deleted in the table.
			if (StringUtils.equals(jobState, GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Rejected) ||
				StringUtils.equals(jobState, GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Completed) ||
				StringUtils.equals(jobState, GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Terminated)) {
				
				ExtendedObjectProxy.getTransportJobCommandService().remove(eventInfo, transportJobCommandInfo);
			} else {
				ExtendedObjectProxy.getTransportJobCommandService().modify(eventInfo, transportJobCommandInfo);
			}
		}
		catch(Exception e)
		{
			throw new CustomException("JOB-8012", e.getMessage());
		}
		
		return transportJobCommandInfo;
	}
	
	// Added by njpark on 2018.09.05 - Carrier Region Change by GarbageCollector.
	/*
	 * Name : changeCarrierCurrentRegion
	 * Desc : This function is change Carrier CurrentRegion
	 * Author : 
	 * Date : 2018.09.05 
	 */
	public Durable changeCurrentCarrierRegion(Durable durableData, EventInfo eventInfo, String region, String kanban) throws CustomException													
	{
		// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//		Map<String, String> durableUdfs = durableData.getUdfs();
		SetAreaInfo setAreaInfo = new SetAreaInfo();
		Map<String, String> durableUdfs = setAreaInfo.getUdfs();
		
		// Machine,Port,Zone,PositionType은 기존의 값 그대로 반영
		durableUdfs.put("POSITIONNAME", "");
		durableUdfs.put("REGION", region);
		durableUdfs.put("KANBAN", kanban);

		durableData = DurableServiceProxy.getDurableService().setArea(durableData.getKey(), eventInfo, setAreaInfo);

		return durableData;
	}
	
	public String transferReasonMessage(String reasonMessage) throws CustomException
	{
		try
		{
			String sql = "SELECT ENUMVALUE, DESCRIPTION" +
						 "  FROM ENUMDEFVALUE" +
						 " WHERE ENUMNAME = :ENUMNAME" +
						 "   AND ENUMVALUE = :ENUMVALUE";
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("ENUMNAME", "ReasonMessage");
			bindMap.put("ENUMVALUE", reasonMessage);
			
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			
			String desc = "";
			
			for(Map<String, Object> map : sqlResult)
			{
				desc = (String)map.get("DESCRIPTION");
				break;
			}
			
			return StringUtils.isNotEmpty(desc) ? reasonMessage + " [ " + desc + " ]" : reasonMessage;
		}
		catch(Exception ex)
		{
			return reasonMessage;
		}
	}
	
	// Added by smkang on 2018.05.19 - MCS doesn't report a transport job but the job is existed in the job of MES database.
	//								   MES terminates the job.
	public void terminateTransportJob(Document doc, List<TransportJobCommand> transportJobList, EventInfo eventInfo) {
		if (transportJobList != null && transportJobList.size() > 0) {
			eventInfo.setEventComment("TransportTerminate");
			
			for (TransportJobCommand transportJobData : transportJobList) {
				try {
					transportJobData.setJobState(GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Terminated);
					
					// Modified by smkang on 2019.01.29 - Garbage job will be deleted in the table.
//						ExtendedObjectProxy.getTransportJobCommandService().modify(eventInfo, transportJobData);
					ExtendedObjectProxy.getTransportJobCommandService().remove(eventInfo, transportJobData);
					
					// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
					// Added by smkang on 2018.11.15 - Management rule of TransportLockFlag is changed, so until a transport job is finished, TransportLockFlag will be remained 'Y'.
					try {
						List<TransportJobCommand> transportJobCommandList = ExtendedObjectProxy.getTransportJobCommandService().select("CARRIERNAME = ? AND JOBSTATE NOT IN (?, ?)", new Object[] {transportJobData.getCarrierName(), GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Completed, GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Terminated});
						
						if (transportJobCommandList == null || transportJobCommandList.size() == 0) {
							Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(transportJobData.getCarrierName());
							
							// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//								Map<String, String> udfs = durableData.getUdfs();
							
							// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//								if (!StringUtils.equals(udfs.get("TRANSPORTLOCKFLAG"), "N")) {
							if (!StringUtils.equals(durableData.getUdfs().get("TRANSPORTLOCKFLAG"), "N")) {
								SetEventInfo setEventInfo = new SetEventInfo();
								setEventInfo.getUdfs().put("TRANSPORTLOCKFLAG", "N");
								
								DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
								
								// For synchronization of a carrier information, common method will be invoked.
					 /*           try {
									Element bodyElement = new Element(SMessageUtil.Body_Tag);
									bodyElement.addContent(new Element("DURABLENAME").setText(transportJobData.getCarrierName()));
									bodyElement.addContent(new Element("TRANSPORTLOCKFLAG").setText("N"));
									
									// EventName will be recorded triggered EventName.
									Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), eventInfo.getEventName());
									
									MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(synchronizeCarrierMessage, transportJobData.getCarrierName());
					            } catch (Exception e1) {
					            	log.warn(e1);
					            }*/
							}
						}
					} catch (Exception e) {
						log.warn(e);
					}
					// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
					
					// Added by smkang on 2019.03.18 - According to Jiang Haiying's request, PortTransferState is need to be updated.
					try {
						String sourceMachineName = transportJobData.getSourceMachineName();
						String sourcePositionName = transportJobData.getSourcePositionName();
						
						MachineSpec sourceMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(sourceMachineName));
						
						if (StringUtils.equals(sourceMachineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine)) {
							try {
								List<TransportJobCommand> transportJobCommandList = ExtendedObjectProxy.getTransportJobCommandService().select("(SOURCEMACHINENAME = ? AND SOURCEPOSITIONNAME = ?) OR (DESTINATIONMACHINENAME = ? AND DESTINATIONPOSITIONNAME = ?)", new Object[] {sourceMachineName, sourcePositionName, sourceMachineName, sourcePositionName});
								
								if (transportJobCommandList == null || transportJobCommandList.size() == 0) {
									Port sourcePortData = MESPortServiceProxy.getPortServiceUtil().getPortData(sourceMachineName, sourcePositionName);
									
									if (sourcePortData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReservedToUnload)) {
										MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
										makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToUnload);
										makeTranferStateInfo.setValidateEventFlag("N");
										
										PortServiceProxy.getPortService().makeTransferState(sourcePortData.getKey(), eventInfo, makeTranferStateInfo);
									}
								}
							} catch (greenFrameDBErrorSignal e) {
								log.warn(e);
							}
						}
					} catch (Exception e) {
						log.info(e);
					}
					
					try {
						String destinationMachineName = transportJobData.getDestinationMachineName();
						String destinationPositionName = transportJobData.getDestinationPositionName();
						
						MachineSpec destinationMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(destinationMachineName));
						
						if (StringUtils.equals(destinationMachineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine)) {
							try {
								List<TransportJobCommand> transportJobCommandList = ExtendedObjectProxy.getTransportJobCommandService().select("(SOURCEMACHINENAME = ? AND SOURCEPOSITIONNAME = ?) OR (DESTINATIONMACHINENAME = ? AND DESTINATIONPOSITIONNAME = ?)", new Object[] {destinationMachineName, destinationPositionName, destinationMachineName, destinationPositionName});
								
								if (transportJobCommandList == null || transportJobCommandList.size() == 0) {
									Port destinationPortData = MESPortServiceProxy.getPortServiceUtil().getPortData(destinationMachineName, destinationPositionName);
									
									if (destinationPortData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReservedToLoad)) {
										MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
										makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToLoad);
										makeTranferStateInfo.setValidateEventFlag("N");
										
										PortServiceProxy.getPortService().makeTransferState(destinationPortData.getKey(), eventInfo, makeTranferStateInfo);
									}
								}
							} catch (greenFrameDBErrorSignal e) {
								log.warn(e);
							}
						}
					} catch (Exception e) {
						log.warn(e);
					}
				} catch (Exception e) {
					// TODO: handle exception
					log.error(e);
				}
			}
		}
	}
}