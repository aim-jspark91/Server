package kr.co.aim.messolution.processgroup.service;

import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupKey;
import kr.co.aim.greentrack.processgroup.management.info.AssignMaterialsInfo;
import kr.co.aim.greentrack.processgroup.management.info.AssignSuperProcessGroupInfo;
import kr.co.aim.greentrack.processgroup.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.processgroup.management.info.CreateInfo;
import kr.co.aim.greentrack.processgroup.management.info.DeassignMaterialsInfo;
import kr.co.aim.greentrack.processgroup.management.info.DeassignSuperProcessGroupInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeCompletedInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeIdleInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeInReworkInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeLoggedInInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeLoggedOutInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeNotOnHoldInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeProcessingInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeReceivedInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeShippedInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeTravelingInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeWaitingToLoginInfo;
import kr.co.aim.greentrack.processgroup.management.info.SetAreaInfo;
import kr.co.aim.greentrack.processgroup.management.info.SetEventInfo;
import kr.co.aim.greentrack.processgroup.management.info.UndoInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * @author gksong
 * @date   2009.02.27
 */

public class ProcessGroupServiceImpl implements ApplicationContextAware
{
	/**
	 * @uml.property  name="applicationContext"
	 * @uml.associationEnd  
	 */
	private ApplicationContext     	applicationContext;
	private static Log				log = LogFactory.getLog("ProcessGroupServiceImpl");
	
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

	/**
	 * @author swcho
	 * @since 2016.03.12
	 * @param processGroupData
	 * @param assignMaterialsInfo
	 * @param eventInfo
	 * @return
	 * @throws CustomException
	 */
	public ProcessGroup assignMaterials( ProcessGroup processGroupData, AssignMaterialsInfo assignMaterialsInfo, EventInfo eventInfo)
			throws CustomException
	{		
		try
		{
			processGroupData = ProcessGroupServiceProxy.getProcessGroupService().assignMaterials(processGroupData.getKey(), eventInfo, assignMaterialsInfo);
			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
			
			return processGroupData;
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("SYS-9999", "ProcessGroup", ie.getMessage());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "ProcessGroup", fe.getMessage());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("SYS-9999", "ProcessGroup", ne.getMessage());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("SYS-9999", "ProcessGroup", de.getMessage());
		}
	}

	/*
	* Name : assignSuperProcessGroup
	* Desc : This function is greenTrack API Call assignSuperProcessGroup
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public void assignSuperProcessGroup( ProcessGroup processGroupData, 
			AssignSuperProcessGroupInfo assignSuperProcessGroupInfo,
			 EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ProcessGroupServiceProxy.getProcessGroupService().assignSuperProcessGroup(processGroupData.getKey(), eventInfo, assignSuperProcessGroupInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : changeSpec
	* Desc : This function is greenTrack API Call changeSpec
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public void changeSpec( ProcessGroup processGroupData, 
			ChangeSpecInfo changeSpecInfo,
			 EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ProcessGroupServiceProxy.getProcessGroupService().changeSpec(processGroupData.getKey(), eventInfo, changeSpecInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : create
	* Desc : This function is greenTrack API Call create
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public void create( ProcessGroup processGroupData, 
						CreateInfo createInfo,
						EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ProcessGroupServiceProxy.getProcessGroupService().create(processGroupData.getKey(), eventInfo, createInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : deassignMaterials
	* Desc : This function is greenTrack API Call deassignMaterials
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public void deassignMaterials( ProcessGroup processGroupData, 
								   DeassignMaterialsInfo deassignMaterialsInfo,
								   EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ProcessGroupServiceProxy.getProcessGroupService().deassignMaterials(processGroupData.getKey(), eventInfo, deassignMaterialsInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : deassignSuperProcessGroup
	* Desc : This function is greenTrack API Call deassignSuperProcessGroup
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public void deassignSuperProcessGroup( ProcessGroup processGroupData, 
										   DeassignSuperProcessGroupInfo deassignSuperProcessGroupInfo,
										   EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ProcessGroupServiceProxy.getProcessGroupService().deassignSuperProcessGroup(processGroupData.getKey(), eventInfo, deassignSuperProcessGroupInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeCompleted
	* Desc : This function is greenTrack API Call makeCompleted
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public void makeCompleted( ProcessGroup processGroupData, 
							   MakeCompletedInfo makeCompletedInfo,
							   EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ProcessGroupServiceProxy.getProcessGroupService().makeCompleted(processGroupData.getKey(), eventInfo, makeCompletedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeIdle
	* Desc : This function is greenTrack API Call makeIdle
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public void makeIdle( ProcessGroup processGroupData, 
						  MakeIdleInfo makeIdleInfo,
						  EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ProcessGroupServiceProxy.getProcessGroupService().makeIdle(processGroupData.getKey(), eventInfo, makeIdleInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeInRework
	* Desc : This function is greenTrack API Call makeInRework
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public void makeInRework( ProcessGroup processGroupData, 
							  MakeInReworkInfo makeInReworkInfo,
							  EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ProcessGroupServiceProxy.getProcessGroupService().makeInRework(processGroupData.getKey(), eventInfo, makeInReworkInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeLoggedIn
	* Desc : This function is greenTrack API Call makeLoggedIn
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public void makeLoggedIn( ProcessGroup processGroupData, 
							  MakeLoggedInInfo makeLoggedInInfo,
							  EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ProcessGroupServiceProxy.getProcessGroupService().makeLoggedIn(processGroupData.getKey(), eventInfo, makeLoggedInInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeLoggedOut
	* Desc : This function is greenTrack API Call makeLoggedOut
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public void makeLoggedOut( ProcessGroup processGroupData, 
							   MakeLoggedOutInfo makeLoggedOutInfo,
							   EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ProcessGroupServiceProxy.getProcessGroupService().makeLoggedOut(processGroupData.getKey(), eventInfo, makeLoggedOutInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeNotOnHold
	* Desc : This function is greenTrack API Call makeNotOnHold
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public void makeNotOnHold( ProcessGroup processGroupData, 
							   MakeNotOnHoldInfo makeNotOnHoldInfo,
							   EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ProcessGroupServiceProxy.getProcessGroupService().makeNotOnHold(processGroupData.getKey(), eventInfo, makeNotOnHoldInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeOnHold
	* Desc : This function is greenTrack API Call makeOnHold
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public void makeOnHold( ProcessGroup processGroupData, 
							MakeOnHoldInfo makeOnHoldInfo,
							EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ProcessGroupServiceProxy.getProcessGroupService().makeOnHold(processGroupData.getKey(), eventInfo, makeOnHoldInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeProcessing
	* Desc : This function is greenTrack API Call makeProcessing
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public void makeProcessing( ProcessGroup processGroupData, 
			MakeProcessingInfo makeProcessingInfo,
			EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ProcessGroupServiceProxy.getProcessGroupService().makeProcessing(processGroupData.getKey(), eventInfo, makeProcessingInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeReceived
	* Desc : This function is greenTrack API Call makeReceived
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public void makeReceived( ProcessGroup processGroupData, 
			MakeReceivedInfo makeReceivedInfo,
			EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ProcessGroupServiceProxy.getProcessGroupService().makeReceived(processGroupData.getKey(), eventInfo, makeReceivedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeShipped
	* Desc : This function is greenTrack API Call makeShipped
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public void makeShipped( ProcessGroup processGroupData, 
			MakeShippedInfo makeShippedInfo,
			EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ProcessGroupServiceProxy.getProcessGroupService().makeShipped(processGroupData.getKey(), eventInfo, makeShippedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeTraveling
	* Desc : This function is greenTrack API Call makeTraveling
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public void makeTraveling( ProcessGroup processGroupData, 
							   MakeTravelingInfo makeTravelingInfo,
							   EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ProcessGroupServiceProxy.getProcessGroupService().makeTraveling(processGroupData.getKey(), eventInfo, makeTravelingInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeWaitingToLogin
	* Desc : This function is greenTrack API Call makeWaitingToLogin
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public void makeWaitingToLogin( ProcessGroup processGroupData, 
									MakeWaitingToLoginInfo makeWaitingToLoginInfo,
									EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ProcessGroupServiceProxy.getProcessGroupService().makeWaitingToLogin(processGroupData.getKey(), eventInfo, makeWaitingToLoginInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : setArea
	* Desc : This function is greenTrack API Call setArea
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public void setArea( ProcessGroup processGroupData, 
						 SetAreaInfo setAreaInfo,
						 EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ProcessGroupServiceProxy.getProcessGroupService().setArea(processGroupData.getKey(), eventInfo, setAreaInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : setEvent
	* Desc : This function is greenTrack API Call setEvent
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public void setEvent( ProcessGroup processGroupData, 
						  SetEventInfo setEventInfo,
						  EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ProcessGroupServiceProxy.getProcessGroupService().setEvent(processGroupData.getKey(), eventInfo, setEventInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : undo
	* Desc : This function is greenTrack API Call undo
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public void undo( ProcessGroup processGroupData, 
					  UndoInfo undoIfo,
					  EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ProcessGroupServiceProxy.getProcessGroupService().undo(processGroupData.getKey(), eventInfo, undoIfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}
	
	/*
	* Name : makeShipped
	* Desc : This function is greenTrack API Call undo
	* Author : AIM Systems, Inc
	* Date : 2016.02.01
	*/
	public ProcessGroup makeShipped(EventInfo eventInfo, 
			  String processGroupName,
			  String factoryName,
			  String areaName,
			  String directShipFlag,
			  Map<String, String> materialUdfs,
			  Map<String, String> subMaterialUdfs,
			  Map<String, String> udfs)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		log.info("Execute makeShipped");
		
		ProcessGroupKey processGroupKey = new ProcessGroupKey();		
		processGroupKey.setProcessGroupName( processGroupName );
		
		MakeShippedInfo makeShippedInfo = new MakeShippedInfo();		
		makeShippedInfo.setFactoryName( factoryName );
		makeShippedInfo.setAreaName( areaName );
		makeShippedInfo.setDirectShipFlag( directShipFlag );
		makeShippedInfo.setMaterialUdfs( materialUdfs );
		makeShippedInfo.setSubMaterialUdfs( subMaterialUdfs );
		makeShippedInfo.setUdfs( udfs );	
		
		ProcessGroup makeShipped = ProcessGroupServiceProxy.getProcessGroupService().makeShipped( processGroupKey, eventInfo, makeShippedInfo );
		return makeShipped;
	}
	
	/**
	 * @author swcho
	 * @since 2016.03.12
	 * @param eventInfo
	 * @param createInfo
	 * @return
	 * @throws CustomException
	 */
	public ProcessGroup create(EventInfo eventInfo, CreateInfo createInfo) throws CustomException
	{
		try
		{
			ProcessGroup processGroupData = ProcessGroupServiceProxy.getProcessGroupService().create(eventInfo, createInfo);
			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
			
			return processGroupData;
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("SYS-9999", "ProcessGroup", ie.getMessage());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "ProcessGroup", fe.getMessage());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("SYS-9999", "ProcessGroup", ne.getMessage());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("SYS-9999", "ProcessGroup", de.getMessage());
		}
	}
}
