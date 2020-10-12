package kr.co.aim.messolution.port.service;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeAccessModeInfo;
import kr.co.aim.greentrack.port.management.info.MakeE10StateInfo;
import kr.co.aim.greentrack.port.management.info.MakePortStateByStateInfo;
import kr.co.aim.greentrack.port.management.info.MakePortStateInfo;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;
import kr.co.aim.greentrack.port.management.info.UndoInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class PortServiceImpl implements ApplicationContextAware
{
	/**
	 * @uml.property  name="applicationContext"
	 * @uml.associationEnd  
	 */
	private ApplicationContext     	applicationContext;
	private static Log 				log = LogFactory.getLog(PortServiceImpl.class); 

	/**
	 * @param arg0
	 * @throws BeansException
	 * @uml.property  name="applicationContext"
	 */
	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		applicationContext = arg0;
	}


	/**
	 * make access mode
	 * @author swcho
	 * @since 2014.05.19
	 * @param portData
	 * @param makeAccessModeInfo
	 * @param eventInfo
	 * @throws CustomException
	 */
	public void makeAccessMode(Port portData, 
								MakeAccessModeInfo makeAccessModeInfo,
								EventInfo eventInfo)
		throws CustomException
	{
		String machineName = portData.getKey().getMachineName();
		String portName = portData.getKey().getPortName();
		
		try
		{
			if(!StringUtils.equals(portData.getAccessMode(), makeAccessModeInfo.getAccessMode()))
			{
//				if(!StringUtils.equals(portData.getAccessMode(), makeAccessModeInfo.getAccessMode()))
//				{					
					 //Not change accessmode - By user request
				    portData.setAccessMode(makeAccessModeInfo.getAccessMode());
				    
					PortServiceProxy.getPortService().makeAccessMode(portData.getKey(), eventInfo, makeAccessModeInfo);
					log.info("Event Name = " + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
			}
			else
			{
				log.info("AccessMode is Same. DB=" + portData.getAccessMode() + " DATA=" + makeAccessModeInfo.getAccessMode());
			}
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("PORT-9003", machineName, portName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("PORT-9999", fe.getMessage());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("PORT-9001", machineName, portName);
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("PORT-9002", machineName, portName);
		}
	}
	
	/*
	* Name : makPortState
	* Desc : This function is makPortState
	* Author : AIM Systems, Inc
	* Date : 2011.01.10
	*/
	public void makPortState( Port portData,
			 MakePortStateInfo makePortStateInfo,
			 EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		//Same Value Check
		if( !StringUtils.equals(portData.getPortStateName(), makePortStateInfo.getPortEventName()))
		{
			PortServiceProxy.getPortService().makePortState( portData.getKey(), eventInfo, makePortStateInfo );
			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		}
		else
		{
			log.info("PortState is Same. DB : " + portData.getPortStateName() + " DATA : " + makePortStateInfo.getPortEventName() );
		}		
	}

	/*
	* Name : makeE10state
	* Desc : This function is makeE10state
	* Author : AIM Systems, Inc
	* Date : 2011.01.10
	*/
	public void makeE10state( Port portData,
							  MakeE10StateInfo makeE10StateInfo,
							  EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		//Same Value Check
		if( !StringUtils.equals(portData.getE10State(), makeE10StateInfo.getE10State()))
		{
			PortServiceProxy.getPortService().makeE10State( portData.getKey(), eventInfo, makeE10StateInfo );
			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		}
		else
		{
			log.info("E10State is Same. DB : " + portData.getE10State() + " DATA : " + makeE10StateInfo.getE10State() );
		}		
	}

	/**
	 * make portStateName by state model
	 * @author swcho
	 * @since 2014.05.19
	 * @param portData
	 * @param makePortStateByStateInfo
	 * @param eventInfo
	 * @throws CustomException
	 */
	public void makePortStateByState(Port portData,
									 MakePortStateByStateInfo makePortStateByStateInfo,
									 EventInfo eventInfo)
		throws CustomException
	{
		String machineName = portData.getKey().getMachineName();
		String portName = portData.getKey().getPortName();
		
		try
		{
			//Same Value Check
			if( !StringUtils.equals(portData.getPortStateName(), makePortStateByStateInfo.getPortStateName()))
			{
				PortServiceProxy.getPortService().makePortStateByState(portData.getKey(), eventInfo, makePortStateByStateInfo);
				
				log.info("EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
			}
			else
			{
				log.info("portStateName is Same. DB=" + portData.getPortStateName() + " DATA=" + makePortStateByStateInfo.getPortStateName() );
			}	
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("PORT-9003", machineName, portName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("PORT-9999", fe.getMessage());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("PORT-9001", machineName, portName);
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("PORT-9002", machineName, portName);
		}
	}

	/**
	 * make transfer state
	 * @author swcho
	 * @since 2014.05.19
	 * @param portData
	 * @param makeTransferStateInfo
	 * @param eventInfo
	 * @throws CustomException
	 */
	public void makeTransferState( Port portData,
								   MakeTransferStateInfo makeTransferStateInfo,
								   EventInfo eventInfo )
		throws CustomException
	{
		String machineName = portData.getKey().getMachineName();
		String portName = portData.getKey().getPortName();
		
		try
		{
			//Same Value Check
			if( !StringUtils.equals(portData.getTransferState(), makeTransferStateInfo.getTransferState()))
			{
				PortServiceProxy.getPortService().makeTransferState(portData.getKey(), eventInfo, makeTransferStateInfo);
				log.info("EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
			}
			else
			{
				log.info("transferState is Same. DB=" + portData.getTransferState() + " DATA=" + makeTransferStateInfo.getTransferState() );
			}
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("PORT-9003", machineName, portName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("PORT-9999", fe.getMessage());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("PORT-9001", machineName, portName);
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("PORT-9002", machineName, portName);
		}
	}
	
	/**
	 * set event with UDF
	 * @author swcho
	 * @since 2014.05.19
	 * @param portData
	 * @param setEventInfo
	 * @param eventInfo
	 * @throws CustomException
	 */
	public void setEvent(Port portData, SetEventInfo setEventInfo, EventInfo eventInfo)
		throws CustomException
	{
		String machineName = portData.getKey().getMachineName();
		String portName = portData.getKey().getPortName();
		
		try
		{
			PortServiceProxy.getPortService().setEvent(portData.getKey(), eventInfo, setEventInfo);
			log.info("EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("PORT-9003", machineName, portName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("PORT-9999", fe.getMessage());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("PORT-9001", machineName, portName);
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("PORT-9002", machineName, portName);
		}
	}
	
	public void undo( Port portData,
			UndoInfo undoInfo,
			  EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		PortServiceProxy.getPortService().undo( portData.getKey(), eventInfo, undoInfo );
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}
	
	
	
	
	
	
}