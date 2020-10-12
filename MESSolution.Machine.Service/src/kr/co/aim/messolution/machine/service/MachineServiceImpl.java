package kr.co.aim.messolution.machine.service;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AutoMQCSetting;
import kr.co.aim.messolution.extended.object.management.data.MQCCondition;
import kr.co.aim.messolution.extended.object.management.data.MachineIdleTime;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.MakeCommunicationStateInfo;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;
import kr.co.aim.greentrack.port.management.data.Port;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.sun.org.glassfish.external.statistics.annotations.Reset;

//Start 20190614188 UpdateMachineIdleTimeInfo add MQC Route Control 
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
//end   20190614188 UpdateMachineIdleTimeInfo add MQC Route Control

/**
 * @author gksong
 * @date   2009.02.16
 */

@SuppressWarnings({"unused", "-access"})
public class MachineServiceImpl implements ApplicationContextAware
{
	/**
	 * @uml.property  name="applicationContext"
	 * @uml.associationEnd
	 */
	private ApplicationContext     	applicationContext;
	private static Log 				log = LogFactory.getLog(MachineServiceImpl.class);

	/**
	 * @param arg0
	 * @throws BeansException
	 * @uml.property  name="applicationContext"
	 */
	@Override
    public void setApplicationContext(ApplicationContext arg0)
			throws BeansException
	{
		applicationContext = arg0;
	}

	/**
	 * change machine state by state model
	 * @author swcho
	 * @since 2014.05.16
	 * @param machineData
	 * @param makeMachineStateByStateInfo
	 * @param eventInfo
	 * @throws CustomException
	 */
	public void makeMachineStateByState(Machine machineData, MakeMachineStateByStateInfo makeMachineStateByStateInfo, EventInfo eventInfo)
		throws CustomException
	{
		try
		{
			// 2019.05.16_hsryu_Add Logic. if current ReasonCode and new ReasonCode is different, Change MachineState.
			//Same Value Check
			if(!StringUtils.equals(machineData.getMachineStateName(), makeMachineStateByStateInfo.getMachineStateName())
			   ||!StringUtils.equals(machineData.getReasonCode(), eventInfo.getReasonCode()))
			{
				MachineServiceProxy.getMachineService().makeMachineStateByState(machineData.getKey(), eventInfo, makeMachineStateByStateInfo);

				log.info("EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
			}
			else
			{
				//Only write log for OnlineInitial
				CommonUtil.CustomExceptionLog("MACHINE-0001", machineData.getKey().getMachineName(),
											machineData.getMachineStateName(), makeMachineStateByStateInfo.getMachineStateName());

				/*throw new CustomException("MACHINE-0001", machineData.getKey().getMachineName(),
											machineData.getMachineStateName(), makeMachineStateByStateInfo.getMachineStateName());*/
			}
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("MACHINE-9003", machineData.getKey().getMachineName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MACHINE-9999", machineData.getKey().getMachineName());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("MACHINE-9002", machineData.getKey().getMachineName());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("MACHINE-9001", machineData.getKey().getMachineName());
		}
	}


	/**
	 * change communication state
	 * @author swcho
	 * @since 2014.05.16
	 * @param machineData
	 * @param makeCommunicationStateInfo
	 * @param eventInfo
	 * @throws CustomException
	 */
	public void makeCommunicationState(Machine machineData, MakeCommunicationStateInfo makeCommunicationStateInfo,
			                               EventInfo eventInfo	)
		throws CustomException
	{
		try
		{
			//Same Value Check
			if(!StringUtils.equals(machineData.getCommunicationState(), makeCommunicationStateInfo.getCommunicationState()))
			{
				// Added by smkang on 2018.11.20 - According to Wangli's request, OldStateReasonCode is added.
				makeCommunicationStateInfo.getUdfs().put("OLDSTATEREASONCODE", machineData.getUdfs().get("STATEREASONCODE"));
				
				MachineServiceProxy.getMachineService().makeCommunicationState(machineData.getKey(), eventInfo, makeCommunicationStateInfo);

				log.info("EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
			}
			else
			{
				//throw new CustomException("MACHINE-0001", machineData.getKey().getMachineName(),
				//							machineData.getCommunicationState(), makeCommunicationStateInfo.getCommunicationState());

				//Only write log for OnlineInitial
				CommonUtil.CustomExceptionLog("MACHINE-0001", machineData.getKey().getMachineName(),
						                    machineData.getCommunicationState(), makeCommunicationStateInfo.getCommunicationState());
			}
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("MACHINE-9003", machineData.getKey().getMachineName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MACHINE-9999", machineData.getKey().getMachineName());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("MACHINE-9002", machineData.getKey().getMachineName());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("MACHINE-9001", machineData.getKey().getMachineName());
		}
	}

	/**
	 * set UDF and add history
	 * @author swcho
	 * @since 2014.05.16
	 * @param machineData
	 * @param setEventInfo
	 * @param eventInfo
	 * @throws CustomException
	 */
	public void setEvent(Machine machineData, SetEventInfo setEventInfo, EventInfo eventInfo)
		throws CustomException
	{
		try
		{
			// Added by smkang on 2018.11.20 - According to Wangli's request, OldStateReasonCode is added.
			setEventInfo.getUdfs().put("OLDSTATEREASONCODE", machineData.getUdfs().get("STATEREASONCODE"));
						
			MachineServiceProxy.getMachineService().setEvent(machineData.getKey(), eventInfo, setEventInfo);

			log.info("EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MACHINE-9999", machineData.getKey().getMachineName());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("MACHINE-9002", machineData.getKey().getMachineName());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("MACHINE-9001", machineData.getKey().getMachineName());
		}
	}
	
	/**
	 * 
	 * @Name     setEventNotUpdateOldStateReasonCode
	 * @since    2019. 3. 25.
	 * @author   hhlee
	 * @contents 
	 *           
	 * @param machineData
	 * @param setEventInfo
	 * @param eventInfo
	 * @throws CustomException
	 */
	public void setEventByNotUpdateOldStateReasonCode(Machine machineData, SetEventInfo setEventInfo, EventInfo eventInfo)
	        throws CustomException
    {
        try
        {
            //// Added by smkang on 2018.11.20 - According to Wangli's request, OldStateReasonCode is added.
            //setEventInfo.getUdfs().put("OLDSTATEREASONCODE", machineData.getUdfs().get("STATEREASONCODE"));
            
            MachineServiceProxy.getMachineService().setEvent(machineData.getKey(), eventInfo, setEventInfo);

            log.info("EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("MACHINE-9999", machineData.getKey().getMachineName());
        }
        catch (DuplicateNameSignal de)
        {
            throw new CustomException("MACHINE-9002", machineData.getKey().getMachineName());
        }
        catch (NotFoundSignal ne)
        {
            throw new CustomException("MACHINE-9001", machineData.getKey().getMachineName());
        }
    }
	
	/**
	 * @author smkang
	 * @since 2018.08.11
	 * @param machineName
	 * @param unitName
	 * @param lotData
	 * @param eventInfo
	 * @see Update MachineIdleTime or MQCCondition.
	 *      확인 중인 사항 - 1. Idle Time Over가 아닌 상황에 설비 상태를 MQC로 변경하고 Run 진행 시 MQCRunCount를 증가시켜야 하는지 아니면 LastRunTime 또는 LastGlassCount를 증가시켜야 하는가
	 *                    - MQCRunCount를 증가
	 *                 2. Idle Time 조건 생성 후 WIP이 없는 상태에서 IDLE_TIME만큼 시간이 흐르면 Idle Time Over로 간주해야 하는지 아니면 첫번째 Glass 진행 후 부터 체크해야 하는가
	 *                    - Idle Time 조건 생성 시 LastRunTime을 함께 업데이트
	 *                 3. LastRunTime 또는 LastGlassCount를 TrackOut 시점에 업데이트 하면 다음 Lot 연속 투입 시 Idle Time Over 상황을 미리 감지 할 수 없는데 어떻게 해야 하는가
	 *                    - 추후 다시 고려
	 */
	public void updateMachineIdleTimeRunInfo(String machineName, Lot lotData, String portName, String operationMode, EventInfo eventInfo) {
		try {
			String unitName = "";
			boolean fmcFlag = false;
			
			if(StringUtils.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_NORMAL))
				unitName = "*";
			else if(StringUtils.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP)) {
				Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
				unitName = portData.getUdfs().get("LINKEDUNITNAME");
				
				if (StringUtils.isEmpty(unitName))
					log.warn("OperationMode is 'INDP'. But LinkedUnitName is Empty!!");
			}
			
			log.info("machineName : " + machineName + ", unitName : " + unitName);
			
			if (StringUtils.isNotEmpty(machineName) && lotData != null && StringUtils.isNotEmpty(unitName)){
			
				 if (lotData.getProductionType().equals(GenericServiceProxy.getConstantMap().PRODUCTION_TYPE_MQCA)) {	
					String condition = "MACHINENAME = ? AND UNITNAME = ? ";
					Object[] bindSet = new Object[] {machineName , unitName};

					List<MQCCondition> mqcConditionList = ExtendedObjectProxy.getMQCConditionService().select(condition, bindSet);
					
					for (MQCCondition mqcCondition : mqcConditionList) {
						long mqcRunCount = mqcCondition.getMqcRunCount();
						
						if (mqcCondition.getMqcProductSpecName().equals("*") || mqcCondition.getMqcProductSpecName().equals(lotData.getProductSpecName())) {
							mqcCondition.setMqcRunCount(mqcRunCount + 1);
							
							// Added by smkang on 2018.08.31 - Need to decrease MQCPreRunCount.
							long mqcPreRunCount = mqcCondition.getMqcPreRunCount();
							mqcCondition.setMqcPreRunCount((mqcPreRunCount - 1) > 0 ? (mqcPreRunCount - 1) : 0);
							
							mqcCondition.setLastEventName(eventInfo.getEventName());
							mqcCondition.setLastEventTimekey(eventInfo.getEventTimeKey());
							mqcCondition.setLastEventTime(eventInfo.getEventTime());
							mqcCondition.setLastEventUser(eventInfo.getEventUser());
							mqcCondition.setLastEventComment(eventInfo.getEventComment());
							
							ExtendedObjectProxy.getMQCConditionService().modify(eventInfo, mqcCondition);
						}
						
					}
					
				}
				 else {
					MachineIdleTime machineIdleTime = ExtendedObjectProxy.getMachineIdleTimeService().selectByKey(false, new Object[] {machineName, unitName});
					
					if (!machineIdleTime.getIsIdleTimeOver().equals("Y")) {
						machineIdleTime.setLastRunTime(eventInfo.getEventTime());
						
						long lastGlassCount = machineIdleTime.getLastGlassCount();							
						machineIdleTime.setLastGlassCount(lastGlassCount + 1);
						
						if (machineIdleTime.getMqcType().equals("GLASS_COUNT")) {
							if (lastGlassCount + 1 >= machineIdleTime.getGlassCount()){
								machineIdleTime.setIsIdleTimeOver("Y");
								fmcFlag = true;
							}
						}
						
						machineIdleTime.setLastEventName(eventInfo.getEventName());
						machineIdleTime.setLastEventTimekey(eventInfo.getEventTimeKey());
						machineIdleTime.setLastEventTime(eventInfo.getEventTime());
						machineIdleTime.setLastEventUser(eventInfo.getEventUser());
						machineIdleTime.setLastEventComment(eventInfo.getEventComment());
						
						ExtendedObjectProxy.getMachineIdleTimeService().modify(eventInfo, machineIdleTime);
						
						if (fmcFlag)
							MESLotServiceProxy.getSendServiceImpl().SendToFMCIsIdleTimeOver(machineIdleTime.getMachineName());
					}
				}
			}

		} catch (Exception e) {
			log.warn(e);
		}
	}
	
	/**
	 * @author smkang
	 * @since 2019.01.10
	 * @param machineName
	 * @param unitName
	 * @param lotData
	 * @param eventInfo
	 * @see update LastRunTime only.
	 */
	public void updateMachineIdleTimeRunTime(String machineName, Lot lotData, String portName, String operationMode, EventInfo eventInfo) {
		try {
			String unitName = "";

			if(StringUtils.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_NORMAL))
				unitName = "*";
			else if(StringUtils.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP)) {
				Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
				unitName = portData.getUdfs().get("LINKEDUNITNAME");
				
				if (StringUtils.isEmpty(unitName))
					log.warn("OperationMode is 'INDP'. But LinkedUnitName is Empty!!");
			}
				
			log.info("machineName : " + machineName + ", unitName : " + unitName);

			if (StringUtils.isNotEmpty(machineName) && lotData != null && StringUtils.isNotEmpty(unitName)) {
				MachineIdleTime machineIdleTime = ExtendedObjectProxy.getMachineIdleTimeService().selectByKey(false, new Object[] {machineName, unitName}); 

				if (!machineIdleTime.getIsIdleTimeOver().equals("Y") && !lotData.getProductionType().equals(GenericServiceProxy.getConstantMap().PRODUCTION_TYPE_MQCA)) {
					//2019.02.01_hsryu_insert Logic. Because ORA-0001.
					eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					eventInfo.setEventTime(TimeUtils.getTimestampByTimeKey(eventInfo.getEventTimeKey()));
					
					machineIdleTime.setLastRunTime(eventInfo.getEventTime());
					machineIdleTime.setLastEventName(eventInfo.getEventName());
					machineIdleTime.setLastEventTimekey(eventInfo.getEventTimeKey());
					machineIdleTime.setLastEventTime(eventInfo.getEventTime());
					machineIdleTime.setLastEventUser(eventInfo.getEventUser());
					machineIdleTime.setLastEventComment(eventInfo.getEventComment());

					ExtendedObjectProxy.getMachineIdleTimeService().modify(eventInfo, machineIdleTime);
				}


			}
			
		} catch (Exception e) {
			log.warn(e);
		}
	}
	
	/**
	 * @author smkang
	 * @since 2018.08.31
	 * @param machineName
	 * @param unitName
	 * @param lotData
	 * @param eventInfo
	 * @see Update MQCPreRunCount of MQCCondition.
	 */
	public void updateMachineIdleTimePreRunInfo(String machineName, Lot lotData, String portName, String operationMode,  EventInfo eventInfo) {
		try {
			String unitName = "";

			if(StringUtils.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_NORMAL))
				unitName = "*";
			else if(StringUtils.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP)) {
				Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
				unitName = portData.getUdfs().get("LINKEDUNITNAME");
				
				if (StringUtils.isEmpty(unitName))
					log.warn("OperationMode is 'INDP'. But LinkedUnitName is Empty!!");
			}

			log.info("machineName : " + machineName + ", unitName : " + unitName);

			if (StringUtils.isNotEmpty(machineName) && lotData != null && StringUtils.isNotEmpty(portName)) {
				MachineIdleTime machineIdleTime = ExtendedObjectProxy.getMachineIdleTimeService().selectByKey(false, new Object[] {machineName, unitName}); 
							
				if (machineIdleTime.getIsIdleTimeOver().equals("Y") && lotData.getProductionType().equals(GenericServiceProxy.getConstantMap().PRODUCTION_TYPE_MQCA)) {
					String condition = "MACHINENAME = ? AND UNITNAME = ? ";
					Object[] bindSet = new Object[] {machineName , unitName};

					List<MQCCondition> mqcConditionList = ExtendedObjectProxy.getMQCConditionService().select(condition, bindSet);

					for (MQCCondition mqcCondition : mqcConditionList) {
						String actualProcessingPosition = MESProductServiceProxy.getProductServiceUtil().getActualSlotByProductProcessFlag(lotData.getKey().getLotName());
						long currentGlassCount = StringUtils.isNotEmpty(actualProcessingPosition) ? actualProcessingPosition.split(",").length : 0;
						
						long mqcPreRunCount = mqcCondition.getMqcPreRunCount();

						if (mqcCondition.getMqcProductSpecName().equals("*") || mqcCondition.getMqcProductSpecName().equals(lotData.getProductSpecName())) {
							mqcCondition.setMqcPreRunCount(mqcPreRunCount + currentGlassCount);
							mqcCondition.setLastEventName(eventInfo.getEventName());
							mqcCondition.setLastEventTimekey(eventInfo.getEventTimeKey());
							mqcCondition.setLastEventTime(eventInfo.getEventTime());
							mqcCondition.setLastEventUser(eventInfo.getEventUser());
							mqcCondition.setLastEventComment(eventInfo.getEventComment());

							ExtendedObjectProxy.getMQCConditionService().modify(eventInfo, mqcCondition);
						}
					}
				}
			}
			
		} catch (Exception e) {
			log.warn(e);
		}
	}
	
	/**
	 * @author smkang
	 * @since 2018.08.11
	 * @param machineName
	 * @param unitName
	 * @param eventInfo
	 * @see Reset MachineIdleTime and MQCCondition. 
	 */
	public void resetMachineIdleTime(String machineName, String unitName, EventInfo eventInfo, Lot lotData) {
		try {
			MachineIdleTime machineIdleTime = ExtendedObjectProxy.getMachineIdleTimeService().selectByKey(false, new Object[] {machineName, unitName});
			
			String resetType = machineIdleTime.getResetType();
			boolean needToResetMachineIdleTime = resetType.equals("MULTI") ? true : false;
			boolean mqcConditionCheckProductSpec = false;//modify by jhying on20200318 mantis:5836
			String condition = "MACHINENAME = ? AND UNITNAME = ? ";
			Object[] bindSet = new Object[] {machineName, unitName};

			List<MQCCondition> mqcConditionList = ExtendedObjectProxy.getMQCConditionService().select(condition, bindSet);
			//add start by jhying on20200318 mantis:5836
			for (MQCCondition mqcCondition : mqcConditionList) {
				if (mqcCondition.getMqcProductSpecName().equals(lotData.getProductSpecName())) {
					
					mqcConditionCheckProductSpec = true;
				}
			}
			//add end by jhying on20200318 mantis:5836
			for (MQCCondition mqcCondition : mqcConditionList) {
				if (mqcCondition.getMqcRunCount() >= mqcCondition.getMqcPlanCount()) {
					if (resetType.equals("SINGLE"))
						needToResetMachineIdleTime = true;
				} else {
					if (resetType.equals("MULTI"))
						needToResetMachineIdleTime = false;
				}
			}
			
			//if (needToResetMachineIdleTime) {
			if (needToResetMachineIdleTime && mqcConditionCheckProductSpec) { //add start by jhying on20200318 mantis:5836
				log.info("Reset Machine Idle Time! machineName : " + machineName + ", unitName : " + unitName);
				
				String originalEventName = eventInfo.getEventName();
				eventInfo.setEventName("ResetMachineIdleTime");
				
				machineIdleTime.setLastRunTime(eventInfo.getEventTime());
				machineIdleTime.setLastGlassCount(0);
				machineIdleTime.setIsIdleTimeOver("N");
				machineIdleTime.setLastEventName(eventInfo.getEventName());
				machineIdleTime.setLastEventTimekey(eventInfo.getEventTimeKey());
				machineIdleTime.setLastEventTime(eventInfo.getEventTime());
				machineIdleTime.setLastEventUser(eventInfo.getEventUser());
				machineIdleTime.setLastEventComment(eventInfo.getEventComment());
				
				ExtendedObjectProxy.getMachineIdleTimeService().modify(eventInfo, machineIdleTime);
				MESLotServiceProxy.getSendServiceImpl().SendToFMCIsIdleTimeOver(machineIdleTime.getMachineName());
				
				for (MQCCondition mqcCondition : mqcConditionList) {
					//2019.02.01_hsryu_insert Logic. Because ORA-0001.
					eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					
					mqcCondition.setMqcRunCount(0);
					mqcCondition.setMqcPreRunCount(0);
					mqcCondition.setLastEventName(eventInfo.getEventName());
					mqcCondition.setLastEventTimekey(eventInfo.getEventTimeKey());
					mqcCondition.setLastEventTime(eventInfo.getEventTime());
					mqcCondition.setLastEventUser(eventInfo.getEventUser());
					mqcCondition.setLastEventComment(eventInfo.getEventComment());
					
					ExtendedObjectProxy.getMQCConditionService().modify(eventInfo, mqcCondition);
				}
				
				eventInfo.setEventName(originalEventName);
			}
		} catch (Exception e) {
			log.debug(e);
		}
	}
	
	/**
	 * @author smkang
	 * @since 2019.05.28
	 * @param machineName
	 * @param unitName
	 * @param eventInfo
	 * @see Force Reset MachineIdleTime and MQCCondition. 
	 */
	public void forceResetMachineIdleTime(String machineName, String unitName, EventInfo eventInfo) {
		try {
			log.info("Reset Machine Idle Time! machineName : " + machineName + ", unitName : " + unitName);
			
			List<MachineIdleTime> machineIdleTimeList = null;
			String condition = "MACHINENAME = ? AND UNITNAME = ? ";
			Object[] bindSet = new Object[] {machineName , unitName};

			machineIdleTimeList = ExtendedObjectProxy.getMachineIdleTimeService().select(condition, bindSet);

			for (MachineIdleTime machineIdleTime : machineIdleTimeList) {
				machineIdleTime.setLastRunTime(eventInfo.getEventTime());
				machineIdleTime.setLastGlassCount(0);
				machineIdleTime.setIsIdleTimeOver("N");
				machineIdleTime.setLastEventName(eventInfo.getEventName());
				machineIdleTime.setLastEventTimekey(eventInfo.getEventTimeKey());
				machineIdleTime.setLastEventTime(eventInfo.getEventTime());
				machineIdleTime.setLastEventUser(eventInfo.getEventUser());
				machineIdleTime.setLastEventComment(eventInfo.getEventComment());
				
				ExtendedObjectProxy.getMachineIdleTimeService().modify(eventInfo, machineIdleTime);
				MESLotServiceProxy.getSendServiceImpl().SendToFMCIsIdleTimeOver(machineIdleTime.getMachineName());
			}
			
			List<MQCCondition> mqcConditionList = ExtendedObjectProxy.getMQCConditionService().select(condition, bindSet);
			
			for (MQCCondition mqcCondition : mqcConditionList) {
				
				//2019.02.01_hsryu_insert Logic. Because ORA-0001.
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				
				mqcCondition.setMqcRunCount(0);
				mqcCondition.setMqcPreRunCount(0);
				mqcCondition.setLastEventName(eventInfo.getEventName());
				mqcCondition.setLastEventTimekey(eventInfo.getEventTimeKey());
				mqcCondition.setLastEventTime(eventInfo.getEventTime());
				mqcCondition.setLastEventUser(eventInfo.getEventUser());
				mqcCondition.setLastEventComment(eventInfo.getEventComment());
				
				ExtendedObjectProxy.getMQCConditionService().modify(eventInfo, mqcCondition);
			}
		} catch (Exception e) {
			log.warn(e);
		}
	}
	
	/**
	 * @param machineName
	 * @author ParkJeongSu
	 */
	public void updateAutoMQCSettingTimeRunInfo(String machineName)
	{
		try {
			EventInfo eventinfo = EventInfoUtil.makeEventInfo("UpdateLastRunTime", "SYS", "", "", "");
			String condition = " WHERE MACHINENAME = ? ";
			Object[] bindSet = new Object[]{machineName};
			List<AutoMQCSetting> AutoMQCSettingList = ExtendedObjectProxy.getAutoMQCSettingService().select(condition, bindSet);
			
			for(AutoMQCSetting autoMQCSetting: AutoMQCSettingList) {
				autoMQCSetting.setLastRunTime(eventinfo.getEventTime());
				autoMQCSetting.setLastEventName(eventinfo.getEventName());
				autoMQCSetting.setLastEventTimeKey(eventinfo.getEventTimeKey());
				autoMQCSetting.setLastEventComment(eventinfo.getEventComment());
				autoMQCSetting.setLastEventUser(eventinfo.getEventUser());
				autoMQCSetting.setLastEventTime(eventinfo.getEventTime());
				
				ExtendedObjectProxy.getAutoMQCSettingService().modify(eventinfo, autoMQCSetting);
			}
		} catch (Exception e) {
		}
	}
}