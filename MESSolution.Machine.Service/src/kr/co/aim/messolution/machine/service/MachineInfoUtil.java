
package kr.co.aim.messolution.machine.service;

import java.sql.Timestamp;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineKey;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;
import kr.co.aim.greentrack.machine.management.info.MakeCommunicationStateInfo;
import kr.co.aim.greentrack.machine.management.info.MakeE10StateInfo;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateInfo;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;
import kr.co.aim.greentrack.machine.management.info.UndoInfo;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class MachineInfoUtil implements ApplicationContextAware
{
	
	/**
	 * @uml.property  name="applicationContext"
	 * @uml.associationEnd  
	 */
	private ApplicationContext	applicationContext;


	/**
	 * @param arg0
	 * @throws BeansException
	 * @uml.property  name="applicationContext"
	 */
	public void setApplicationContext( ApplicationContext arg0 ) throws BeansException
	{
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}

	/*
	* Name : makeMachineStateInfo
	* Desc : This function is makeMachineStateInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.06
	*/
	public MakeMachineStateInfo makeMachineStateInfo(Machine machineData,
			   String machineEventName)
	{
		// 1. Validation		
		
		
		MakeMachineStateInfo makeMachineStateInfo = new MakeMachineStateInfo();
		
		makeMachineStateInfo.setMachineEventName(machineEventName);
					
		Map<String, String> machineUdfs = machineData.getUdfs();		
		makeMachineStateInfo.setUdfs(machineUdfs);
		
		return makeMachineStateInfo;
	}

	/*
	* Name : makeCommunicationStateInfo
	* Desc : This function is makeCommunicationStateInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.06
	*/
	public MakeCommunicationStateInfo makeCommunicationStateInfo(Machine machineData, String communicationState)
	{
		// 1. Validation
		
		MakeCommunicationStateInfo makeCommunicationState = new MakeCommunicationStateInfo();
		
		makeCommunicationState.setCommunicationState(communicationState);
		makeCommunicationState.setValidateEventFlag("Y");
					
		Map<String, String> machineUdfs = machineData.getUdfs();		
		makeCommunicationState.setUdfs(machineUdfs);
		
		return makeCommunicationState;
	}

	/*
	* Name : makeE10StateInfo
	* Desc : This function is makeE10StateInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.06
	*/
	public MakeE10StateInfo makeE10StateInfo( Machine machineData,
			String state)
	{
		// 1. Validation				
		
		MakeE10StateInfo makeE10StateInfo = new MakeE10StateInfo();
		
		makeE10StateInfo.setE10State(state);
		makeE10StateInfo.setValidateEventFlag("Y");
		
		Map<String, String> machineUdfs = machineData.getUdfs();
		makeE10StateInfo.setUdfs(machineUdfs);		
		
		return makeE10StateInfo;
	}

	/*
	* Name : makeMachineStateByStateInfo
	* Desc : This function is makeMachineStateByStateInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.06
	*/
	public MakeMachineStateByStateInfo makeMachineStateByStateInfo( Machine machineData,
			String machineStateName) throws CustomException	
	{
		// 1. Validation		
		CommonValidation.checkNotNull("MachineStateName", machineStateName);
		//CommonValidation.checkStateModelEvent("MACHINE", machineData.getKey().getMachineName(), "", machineData.getMachineStateName(), machineStateName);
		
		MakeMachineStateByStateInfo makeMachineStateByStateInfo = new MakeMachineStateByStateInfo();
		
		makeMachineStateByStateInfo.setMachineStateName( machineStateName );
		
		// Modified by smkang on 2018.10.31 - According to EDO's request, Need to check state model.
//		makeMachineStateByStateInfo.setValidateEventFlag( "N" );
		makeMachineStateByStateInfo.setValidateEventFlag( "Y" );
		
		Map<String, String> machineUdfs = machineData.getUdfs();		
		makeMachineStateByStateInfo.setUdfs( machineUdfs );		
		
		return makeMachineStateByStateInfo;
	}
	
	//setEventInfo
	/**
	 * to update UDF fields
	 * @author swcho
	 * @since 2014.05.16
	 * @param udfs
	 * @return
	 */
	public SetEventInfo setEventInfo(Map<String, String> udfs)
	{
		SetEventInfo setEventInfo = new SetEventInfo();
		
		setEventInfo.setUdfs(udfs);
		
		return setEventInfo;
	}
	
	//undoInfo
	public UndoInfo undoInfo( Machine machineData, 
			 String eventName,
	 		 Timestamp eventTime,
	 		 String eventTimeKey,
	 		 String eventUser,
	 		 String lastEventTimeKey )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{					
		UndoInfo undoInfo = new UndoInfo();
		
		undoInfo.setEventName( eventName );
		undoInfo.setEventTime( eventTime );
		undoInfo.setEventTimeKey( eventTimeKey);
		undoInfo.setEventUser( eventUser );
		undoInfo.setEventTimeKey( lastEventTimeKey );
		
		Map<String, String> machineUdfs = machineData.getUdfs();
		undoInfo.setUdfs(machineUdfs);
		
		return undoInfo;
			
	}
	
	/**
	 * query Machine data by EQP ID
	 * @author swcho
	 * @since 2014.04.21
	 * @param machineName
	 * @return
	 * @throws CustomException
	 */
	public Machine getMachineData(String machineName) throws CustomException
	{
		MachineKey keyInfo = new MachineKey();
		keyInfo.setMachineName(machineName);
		
		Machine dataInfo;
		
		try
		{
			dataInfo = MachineServiceProxy.getMachineService().selectByKey(keyInfo);
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("MACHINE-9001", machineName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MACHINE-9999", fe.getMessage());
		}
		
		return dataInfo;
	}
	
	/**
	 * query MachineSpec data by EQP ID
	 * @author hykim
	 * @since 2014.04.21
	 * @param machineName
	 * @return
	 * @throws CustomException
	 */
	public MachineSpec getMachineSpec(String machineName) throws CustomException
	{
		MachineSpecKey machineSpecKey = new MachineSpecKey();
		machineSpecKey.setMachineName(machineName);
		
		MachineSpec machineSpecData;
		try
		{
			machineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(machineSpecKey);
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("MACHINE-9001", machineName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MACHINE-9999", fe.getMessage());
		}
		
		return machineSpecData;
	}
	
	//add by wghuang 20181224, requested by EDO
	public boolean checkMachineStateName(String machineStateName) throws CustomException
	{
		boolean result = true;
		
		if(StringUtil.equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_RUN, machineStateName) ||
		  StringUtil.equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_IDLE, machineStateName) ||
		  StringUtil.equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_DOWN, machineStateName))
		{
			result = false;
		}
		
		return result;
	}
}
