package kr.co.aim.messolution.port.service;

import java.sql.Timestamp;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.data.PortKey;
import kr.co.aim.greentrack.port.management.info.MakeAccessModeInfo;
import kr.co.aim.greentrack.port.management.info.MakeE10StateInfo;
import kr.co.aim.greentrack.port.management.info.MakePortStateByStateInfo;
import kr.co.aim.greentrack.port.management.info.MakePortStateInfo;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;
import kr.co.aim.greentrack.port.management.info.UndoInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class PortInfoUtil implements ApplicationContextAware
{
	/**
	 * @uml.property  name="applicationContext"
	 * @uml.associationEnd  
	 */
	private ApplicationContext     	applicationContext;
	private static Log 				log = LogFactory.getLog(PortInfoUtil.class); 

	/**
	 * @param arg0
	 * @throws BeansException
	 * @uml.property  name="applicationContext"
	 */
	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		applicationContext = arg0;
	}

	/*
	* Name : makeE10StateInfo
	* Desc : This function is makeE10StateInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.06
	*/
	public MakeE10StateInfo makeE10StateInfo( Port portData, String e10MachineStateName )
	{
		MakeE10StateInfo makeE10StateInfo = new MakeE10StateInfo();
		makeE10StateInfo.setE10State(e10MachineStateName);
		makeE10StateInfo.setValidateEventFlag("N");
		Map<String,String> portUdfs = portData.getUdfs();
		makeE10StateInfo.setUdfs(portUdfs);
			
		return makeE10StateInfo;
	}
	
	/**
	 * make access mode info
	 * @since 2014.05.19
	 * @author swcho
	 * @param portData
	 * @param accessMode
	 * @return
	 */
	public MakeAccessModeInfo makeAccessModeInfo(Port portData, String accessMode)
	{
		MakeAccessModeInfo makeAccessModeInfo = new MakeAccessModeInfo();		
		makeAccessModeInfo.setAccessMode(accessMode);
		makeAccessModeInfo.setValidateEventFlag("N");
		
		Map<String,String> portUdfs = portData.getUdfs();
		makeAccessModeInfo.setUdfs( portUdfs );
			
		return makeAccessModeInfo;
	}	

	/**
	 * transfer state change info
	 * @author swcho
	 * @since 2014.05.19
	 * @param portData
	 * @param transferState
	 * @return
	 */
	public MakeTransferStateInfo makeTransferStateInfo(Port portData, String transferState)
	{
		MakeTransferStateInfo makeTransferStateInfo = new MakeTransferStateInfo();
		makeTransferStateInfo.setTransferState(transferState);
		makeTransferStateInfo.setValidateEventFlag("N");
		
		Map<String,String> portUdfs = portData.getUdfs();
		makeTransferStateInfo.setUdfs(portUdfs);
			
		return makeTransferStateInfo;
	}

	/*
	* Name : makePortStateInfo
	* Desc : This function is makePortStateInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.06
	*/
	public MakePortStateInfo makePortStateInfo( Port portData, String portEventName )
	{
		MakePortStateInfo makePortStateInfo = new MakePortStateInfo();
		makePortStateInfo.setPortEventName( portEventName );
		Map<String,String> portUdfs = portData.getUdfs();
		makePortStateInfo.setUdfs( portUdfs );
			
		return makePortStateInfo;
	}

	/**
	 * set event info
	 * @author swcho
	 * @since 2014.05.19
	 * @param portUdfs
	 * @return
	 */
	public SetEventInfo setEventInfo(Map<String, String> portUdfs)
	{
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs( portUdfs );
			
		return setEventInfo;
	}

	/*
	* Name : undoInfo
	* Desc : This function is undoInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.06
	*/
	public UndoInfo undoInfo( Port portData, String eventName , 
									 Timestamp eventTime, String eventTimeKey,
									 String eventUser, String  lastEventTimeKey )
	{			
		UndoInfo undoInfo = new UndoInfo();
		undoInfo.setEventName( eventName );
		undoInfo.setEventTime( eventTime );
		undoInfo.setEventTimeKey( eventTimeKey );
		undoInfo.setEventUser( eventUser );
		undoInfo.setLastEventTimeKey( lastEventTimeKey );
		
		Map<String, String> portUdfs = portData.getUdfs();
		undoInfo.setUdfs(portUdfs);
			
		return undoInfo;
	}
	
	/**
	 * select by Port key
	 * @author swcho
	 * @since 2014.05.19
	 * @param machineName
	 * @param portName
	 * @return
	 * @throws CustomException
	 */
	public Port getPortData(String machineName, String portName) throws CustomException
	{
		try
		{
			PortKey portKey = new PortKey();
			portKey.setMachineName(machineName);
			portKey.setPortName(portName);
			
			Port portData = null;
			portData = PortServiceProxy.getPortService().selectByKey(portKey);
	
			return portData;
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("PORT-9001", machineName, portName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("PORT-9999", fe.getMessage());
		}
	}
	
	/**
	 * makePortState info by state model
	 * @param portData
	 * @param portStateName
	 * @return
	 * @throws CustomException
	 */
	public MakePortStateByStateInfo makePortStateByStateInfo(Port portData, String portStateName)
		throws CustomException
	{
		MakePortStateByStateInfo makePortStateByStateInfo = new MakePortStateByStateInfo();	
		
		if(StringUtil.equals(portStateName, GenericServiceProxy.getConstantMap().Port_EMPTY) &&
				StringUtil.equals(portStateName, GenericServiceProxy.getConstantMap().Port_FULL))
		{
			portData.getUdfs().put("FULLSTATE", portStateName);
		}
		else
			makePortStateByStateInfo.setPortStateName( portStateName );
		
		makePortStateByStateInfo.setValidateEventFlag( "Y" );
		
		makePortStateByStateInfo.setUdfs(portData.getUdfs());
			
		return makePortStateByStateInfo;
	}
}
