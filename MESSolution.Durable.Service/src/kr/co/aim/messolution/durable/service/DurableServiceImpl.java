package kr.co.aim.messolution.durable.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.durable.management.data.DurableHistoryKey;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.data.DurableSpecKey;
import kr.co.aim.greentrack.durable.management.info.AssignTransportGroupInfo;
import kr.co.aim.greentrack.durable.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.durable.management.info.CleanInfo;
import kr.co.aim.greentrack.durable.management.info.CreateInfo;
import kr.co.aim.greentrack.durable.management.info.DeassignTransportGroupInfo;
import kr.co.aim.greentrack.durable.management.info.DecrementDurationUsedInfo;
import kr.co.aim.greentrack.durable.management.info.DecrementTimeUsedInfo;
import kr.co.aim.greentrack.durable.management.info.DirtyInfo;
import kr.co.aim.greentrack.durable.management.info.IncrementDurationUsedInfo;
import kr.co.aim.greentrack.durable.management.info.IncrementTimeUsedInfo;
import kr.co.aim.greentrack.durable.management.info.MakeAvailableInfo;
import kr.co.aim.greentrack.durable.management.info.MakeInUseInfo;
import kr.co.aim.greentrack.durable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.durable.management.info.MakeNotInUseInfo;
import kr.co.aim.greentrack.durable.management.info.MakeScrappedInfo;
import kr.co.aim.greentrack.durable.management.info.RepairInfo;
import kr.co.aim.greentrack.durable.management.info.SetAreaInfo;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.management.info.UndoInfo;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;
import kr.co.aim.greentrack.generic.info.UndoTimeKeys;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.port.management.data.Port;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DurableServiceImpl implements ApplicationContextAware {
	/**
	 * @uml.property name="applicationContext"
	 * @uml.associationEnd
	 */
	private ApplicationContext applicationContext;
	// private NamedValue[] durableData;
	private static Log log = LogFactory.getLog(DurableServiceImpl.class);

	/**
	 * @param arg0
	 * @throws BeansException
	 * @uml.property name="applicationContext"
	 */
	@Override
    public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		applicationContext = arg0;
	}

	/*
	 * Name : assignTransportGroup Desc : This function is NanaTrack API Call
	 * Durable assignTransportGroup Author : AIM Systems, Inc Date : 2011.01.23
	 */
	public void assignTransportGroup(Durable durableData,
			AssignTransportGroupInfo assignTransportGroupInfo,
			EventInfo eventInfo) throws InvalidStateTransitionSignal,
			FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal {
		DurableServiceProxy.getDurableService().assignTransportGroup(
				durableData.getKey(), eventInfo, assignTransportGroupInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey"
				+ eventInfo.getEventTimeKey());
	}

	/*
	 * Name : changeSpec Desc : This function is NanaTrack API Call Durable
	 * changeSpec Author : AIM Systems, Inc Date : 2011.01.23
	 */
	public void changeSpec(Durable durableData, ChangeSpecInfo changeSpecInfo,
			EventInfo eventInfo) throws InvalidStateTransitionSignal,
			FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal {

		DurableServiceProxy.getDurableService().changeSpec(
				durableData.getKey(), eventInfo, changeSpecInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey"
				+ eventInfo.getEventTimeKey());
	}

	/*
	 * Name : clean Desc : This function is NanaTrack API Call Durable clean
	 * Author : AIM Systems, Inc Date : 2011.01.23
	 */
	public Durable clean(Durable durableData, CleanInfo cleanInfo, EventInfo eventInfo) throws CustomException
	{
		try
		{
			Durable result = DurableServiceProxy.getDurableService().clean(durableData.getKey(), eventInfo, cleanInfo);
			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
			
			return result;
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("SYS-0000", ie.getMessage());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-0000", fe.getMessage());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("SYS-0000", de.getMessage());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("SYS-0000", ne.getMessage());
		}
	}

	/*
	 * Name : create Desc : This function is NanaTrack API Call Durable create
	 * Author : AIM Systems, Inc Date : 2011.02.10
	 */
	public Durable create(String durableName, CreateInfo createInfo,
			EventInfo eventInfo) throws InvalidStateTransitionSignal,
			FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal {

		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(durableName);

		Durable durableData = DurableServiceProxy.getDurableService().create(durableKey, eventInfo,
				createInfo);

		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey"
				+ eventInfo.getEventTimeKey());
		
		return durableData;
	}

	/*
	 * Name : deassignTransportGroup Desc : This function is NanaTrack API Call
	 * Durable deassignTransportGroup Author : AIM Systems, Inc Date :
	 * 2011.01.23
	 */
	public void deassignTransportGroup(Durable durableData,
			DeassignTransportGroupInfo deassignTransportGroupInfo,
			EventInfo eventInfo) throws InvalidStateTransitionSignal,
			FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal {

		DurableServiceProxy.getDurableService().deassignTransportGroup(
				durableData.getKey(), eventInfo, deassignTransportGroupInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey"
				+ eventInfo.getEventTimeKey());
	}

	/*
	 * Name : decrementDurationUsed Desc : This function is NanaTrack API Call
	 * Durable decrementDurationUsed Author : AIM Systems, Inc Date : 2011.01.23
	 */
	public void decrementDurationUsed(Durable durableData,
			DecrementDurationUsedInfo decrementDurationUsedInfo,
			EventInfo eventInfo) throws InvalidStateTransitionSignal,
			FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal {

		DurableServiceProxy.getDurableService().decrementDurationUsed(
				durableData.getKey(), eventInfo, decrementDurationUsedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey"
				+ eventInfo.getEventTimeKey());
	}

	/*
	 * Name : decrementTimeUsed Desc : This function is NanaTrack API Call
	 * Durable decrementTimeUsed Author : AIM Systems, Inc Date : 2011.01.23
	 */
	public void decrementTimeUsed(Durable durableData,
			DecrementTimeUsedInfo decrementTimeUsedInfo, EventInfo eventInfo)
			throws InvalidStateTransitionSignal, FrameworkErrorSignal,
			NotFoundSignal, DuplicateNameSignal {

		DurableServiceProxy.getDurableService().decrementTimeUsed(
				durableData.getKey(), eventInfo, decrementTimeUsedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey"
				+ eventInfo.getEventTimeKey());
	}

	/*
	 * Name : dirty Desc : This function is NanaTrack API Call Durable dirty
	 * Author : AIM Systems, Inc Date : 2011.01.23
	 */
	public Durable dirty(Durable durableData, DirtyInfo dirtyInfo,
			EventInfo eventInfo) throws InvalidStateTransitionSignal,
			FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal {

		durableData = DurableServiceProxy.getDurableService().dirty(durableData.getKey(),
				eventInfo, dirtyInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey"
				+ eventInfo.getEventTimeKey());
		
		return durableData;
	}

	/*
	 * Name : incrementDurationUsed Desc : This function is NanaTrack API Call
	 * Durable incrementDutationUsed Author : AIM Systems, Inc Date : 2011.01.23
	 */
	public void incrementDurationUsed(Durable durableData,
			IncrementDurationUsedInfo incrementDurationUsedInfo,
			EventInfo eventInfo) throws InvalidStateTransitionSignal,
			FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal {

		DurableServiceProxy.getDurableService().incrementDurationUsed(
				durableData.getKey(), eventInfo, incrementDurationUsedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey"
				+ eventInfo.getEventTimeKey());
	}

	/*
	 * Name : incrementTimeUsed Desc : This function is NanaTrack API Call
	 * Durable incrementTimeUsed Author : AIM Systems, Inc Date : 2011.01.23
	 */
	public Durable incrementTimeUsed(Durable durableData,
			IncrementTimeUsedInfo incrementTimeUsedInfo, EventInfo eventInfo)
			throws InvalidStateTransitionSignal, FrameworkErrorSignal,
			NotFoundSignal, DuplicateNameSignal {
		
		// Added by smkang on 2019.05.09 - Before incrementTimeUsed is invoked, TimeUsedLimit of Durable should be updated.
		DurableSpecKey durableSpecKey = new DurableSpecKey();
		durableSpecKey.setFactoryName(System.getProperty("shop"));
		durableSpecKey.setDurableSpecName(durableData.getDurableSpecName());
		durableSpecKey.setDurableSpecVersion(durableData.getDurableSpecVersion());
		
		DurableSpec durableSpec = DurableServiceProxy.getDurableSpecService().selectByKey(durableSpecKey);
		if (durableData.getTimeUsedLimit() != durableSpec.getTimeUsedLimit()) {
			durableData.setTimeUsedLimit(durableSpec.getTimeUsedLimit());
			
			DurableServiceProxy.getDurableService().update(durableData);
			log.debug("TimeUsedLimit of DurableSpec and Durable are different, so TimeUsedLimit of Durable is changed to " + durableSpec.getTimeUsedLimit());
		}
		
		// 2019.04.09_hsryu_Insert Logic.
		String previousCleanState = durableData.getDurableCleanState();

		durableData = DurableServiceProxy.getDurableService().incrementTimeUsed(
				durableData.getKey(), eventInfo, incrementTimeUsedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey"
				+ eventInfo.getEventTimeKey());
		
	/*	// 2019.04.09_hsryu_Insert Logic.
		if(!StringUtils.equals(previousCleanState, durableData.getDurableCleanState())){
        	try {	
        		Element bodyElement = new Element(SMessageUtil.Body_Tag);
        		bodyElement.addContent(new Element("DURABLENAME").setText(durableData.getKey().getDurableName()));
        		bodyElement.addContent(new Element("DURABLECLEANSTATE").setText(durableData.getDurableCleanState()));
        		bodyElement.addContent(new Element("DURABLEDRYFLAG").setText(GenericServiceProxy.getConstantMap().FLAG_N));

        		Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), eventInfo.getEventName());

        		MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(synchronizeCarrierMessage, durableData.getKey().getDurableName());
        	} catch (Exception e) {
        		log.warn(e);
        	}
        }*/
		
		return durableData;
	}

	/*
	 * Name : makeAvailable Desc : This function is NanaTrack API Call Durable
	 * makeAvailable Author : AIM Systems, Inc Date : 2011.01.23
	 */
	public void makeAvailable(Durable durableData,
			MakeAvailableInfo makeAvailableInfo, EventInfo eventInfo)
			throws InvalidStateTransitionSignal, FrameworkErrorSignal,
			NotFoundSignal, DuplicateNameSignal {

		DurableServiceProxy.getDurableService().makeAvailable(
				durableData.getKey(), eventInfo, makeAvailableInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey"
				+ eventInfo.getEventTimeKey());
	}

	/*
	 * Name : makeInUse Desc : This function is NanaTrack API Call Durable
	 * makeInUse Author : AIM Systems, Inc Date : 2011.01.23
	 */
	public void makeInUse(Durable durableData, MakeInUseInfo makeInUseInfo,
			EventInfo eventInfo) throws InvalidStateTransitionSignal,
			FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal {

		DurableServiceProxy.getDurableService().makeInUse(durableData.getKey(),
				eventInfo, makeInUseInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey"
				+ eventInfo.getEventTimeKey());
	}

	/*
	 * Name : makeNotAvailable Desc : This function is NanaTrack API Call
	 * Durable makeNotAvailable Author : AIM Systems, Inc Date : 2011.01.23
	 */
	public void makeNotAvailable(Durable durableData,
			MakeNotAvailableInfo makeNotAvailableInfo, EventInfo eventInfo)
			throws InvalidStateTransitionSignal, FrameworkErrorSignal,
			NotFoundSignal, DuplicateNameSignal {

		DurableServiceProxy.getDurableService().makeNotAvailable(
				durableData.getKey(), eventInfo, makeNotAvailableInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey"
				+ eventInfo.getEventTimeKey());
	}

	/*
	 * Name : makeNotInUse Desc : This function is NanaTrack API Call Durable
	 * makeNotInUse Author : AIM Systems, Inc Date : 2011.01.23
	 */
	public void makeNotInUse(Durable durableData,
			MakeNotInUseInfo makeNotInUseInfo, EventInfo eventInfo)
			throws InvalidStateTransitionSignal, FrameworkErrorSignal,
			NotFoundSignal, DuplicateNameSignal {

		DurableServiceProxy.getDurableService().makeNotInUse(
				durableData.getKey(), eventInfo, makeNotInUseInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey"
				+ eventInfo.getEventTimeKey());
	}

	/*
	 * Name : makeScrapped Desc : This function is NanaTrack API Call Durable
	 * makeScrapped Author : AIM Systems, Inc Date : 2011.01.23
	 */
	public void makeScrapped(Durable durableData,
			MakeScrappedInfo makeScrappedInfo, EventInfo eventInfo)
			throws InvalidStateTransitionSignal, FrameworkErrorSignal,
			NotFoundSignal, DuplicateNameSignal {

		DurableServiceProxy.getDurableService().makeScrapped(
				durableData.getKey(), eventInfo, makeScrappedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey"
				+ eventInfo.getEventTimeKey());
	}

	/*
	 * Name : repair Desc : This function is NanaTrack API Call Durable repair
	 * Author : AIM Systems, Inc Date : 2011.01.23
	 */
	public void repair(Durable durableData, RepairInfo repairInfo,
			EventInfo eventInfo) throws InvalidStateTransitionSignal,
			FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal {

		DurableServiceProxy.getDurableService().repair(durableData.getKey(),
				eventInfo, repairInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey"
				+ eventInfo.getEventTimeKey());
	}

	/*
	 * Name : setArea Desc : This function is NanaTrack API Call Durable setArea
	 * Author : AIM Systems, Inc Date : 2011.01.23
	 */
	public void setArea(Durable durableData, SetAreaInfo setAreaInfo,
			EventInfo eventInfo) throws InvalidStateTransitionSignal,
			FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal {

		DurableServiceProxy.getDurableService().setArea(durableData.getKey(),
				eventInfo, setAreaInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey"
				+ eventInfo.getEventTimeKey());
	}

	/*
	 * Name : setEvent Desc : This function is NanaTrack API Call Durable
	 * setEvent Author : AIM Systems, Inc Date : 2011.01.23
	 */
	public void setEvent(Durable durableData, SetEventInfo setEventInfo,
			EventInfo eventInfo) throws InvalidStateTransitionSignal,
			FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal {

		DurableServiceProxy.getDurableService().setEvent(durableData.getKey(),
				eventInfo, setEventInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey"
				+ eventInfo.getEventTimeKey());
	}

	/*
	 * Name : setMaterialLocation Desc : This function is NanaTrack API Call
	 * Durable setMaterialLocation Author : AIM Systems, Inc Date : 2011.01.23
	 */
	public void setMaterialLocation(Durable durableData,
			SetMaterialLocationInfo setMaterialLocationInfo, EventInfo eventInfo)
			throws InvalidStateTransitionSignal, FrameworkErrorSignal,
			NotFoundSignal, DuplicateNameSignal {

		DurableServiceProxy.getDurableService().setMaterialLocation(
				durableData.getKey(), eventInfo, setMaterialLocationInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey"
				+ eventInfo.getEventTimeKey());
	}

	/*
	 * Name : undo Desc : This function is NanaTrack API Call Durable undo
	 * Author : AIM Systems, Inc Date : 2011.01.23
	 */
	public void undo(Durable durableData, UndoInfo undoInfo, EventInfo eventInfo)
			throws InvalidStateTransitionSignal, FrameworkErrorSignal,
			NotFoundSignal, DuplicateNameSignal {

		UndoTimeKeys undoTimeKeys = new UndoTimeKeys();
		undoTimeKeys = DurableServiceProxy.getDurableService().undo(durableData.getKey(), eventInfo, undoInfo);

		try {
			durableData = DurableServiceProxy.getDurableService().selectByKey(durableData.getKey());
			
			DurableHistoryKey keyInfo = new DurableHistoryKey();
			keyInfo.setDurableName(durableData.getKey().getDurableName());
			keyInfo.setTimeKey(undoTimeKeys.getRemarkedTimeKey());

			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			DurableHistory durableHistory = DurableServiceProxy.getDurableHistoryService().selectByKey(keyInfo);
			DurableHistory durableHistory = DurableServiceProxy.getDurableHistoryService().selectByKeyForUpdate(keyInfo);
			
			durableHistory.setEventComment(eventInfo.getEventComment());

			DurableServiceProxy.getDurableHistoryService().update(durableHistory);
		} catch (Exception e) {
			DurableHistoryKey keyInfo = new DurableHistoryKey();
			keyInfo.setDurableName(durableData.getKey().getDurableName());
			keyInfo.setTimeKey(undoTimeKeys.getRemarkedTimeKey());

			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			DurableHistory durableHistory = DurableServiceProxy.getDurableHistoryService().selectByKey(keyInfo);
			DurableHistory durableHistory = DurableServiceProxy.getDurableHistoryService().selectByKeyForUpdate(keyInfo);

			durableHistory.setEventComment(eventInfo.getEventComment());

			DurableServiceProxy.getDurableHistoryService().update(durableHistory);
		}
		
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}
	
	/**
	 * make tranport state to MOVING
	 * 2015.08.27 by swcho : modified with optimized event and error handling
	 * @author hykim
	 * @since 2014.05.19
	 * @param portData
	 * @param makeTransportStateMoving
	 * @param eventInfo
	 * @throws CustomException
	 */
	public Durable unload(EventInfo eventInfo, String carrierName)
		throws CustomException 
	{
		Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		
		SetAreaInfo setAreaInfo = new SetAreaInfo();

		setAreaInfo.setAreaName("");

		Map<String, String> carrierUdfs = carrierData.getUdfs();
		carrierUdfs.put("MACHINENAME", "");
		carrierUdfs.put("PORTNAME", "");
		carrierUdfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_MOVING);
		carrierUdfs.put("POSITIONTYPE", "");
		carrierUdfs.put("POSITIONNAME", "");
		carrierUdfs.put("ZONENAME", "");
		setAreaInfo.setUdfs(carrierUdfs);
		
		eventInfo.setEventName("ChangeTransportState");

		try
		{
			carrierData = DurableServiceProxy.getDurableService().setArea(carrierData.getKey(), eventInfo, setAreaInfo);
			return carrierData;
		}
		//catch (NotFoundSignal ne)
		//{
		//	
		//}
		//catch (InvalidStateTransitionSignal te)
		//{
		//	
		//}
		//catch (DuplicateNameSignal de)
		//{
		//	
		//}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("CST-9999", fe.getMessage());
		}
	}
	
	/**
     * @author wghuang
     *         20181203, hhlee, modify , change function name
     * @since 2018.11.29
     * @param portData
     * @param makeTransportStateMoving and check CSTClean machine [EDO]
     * @param eventInfo
     * @throws CustomException
     */
    public Durable unload(EventInfo eventInfo, String carrierName, Port portData, String constructType, String areaName)
        throws CustomException 
    {
        Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
        
        SetAreaInfo setAreaInfo = new SetAreaInfo();

        //setAreaInfo.setAreaName("");
        setAreaInfo.setAreaName(areaName);

        Map<String, String> carrierUdfs = carrierData.getUdfs();
                
        //CCLN:CST Clean Machine.
        if(StringUtil.equals(constructType, GenericServiceProxy.getConstantMap().ConstructType_CassetteClenaer))
        {
            if(StringUtil.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PU"))
            {
                carrierUdfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_MOVING);
                
                //if(StringUtil.equals(CommonUtil.getValue(carrierData.getUdfs(), "TRANSPORTSTATE"), 
                //        GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP))
                //{
                //    carrierUdfs.put("MACHINENAME", "");
                //    carrierUdfs.put("PORTNAME", "");
                //    carrierUdfs.put("POSITIONTYPE", "");
                //    carrierUdfs.put("POSITIONNAME", "");
                //    carrierUdfs.put("ZONENAME", "");
                //}
                
                carrierUdfs.put("MACHINENAME", "");
            }
            else
            {
                carrierUdfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP);
                //carrierUdfs.put("PORTNAME", "");
                //carrierUdfs.put("POSITIONTYPE", "");
                //carrierUdfs.put("POSITIONNAME", "");
                //carrierUdfs.put("ZONENAME", "");
            }
        }
        else
        {
            carrierUdfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_MOVING);            
            
            //if(StringUtil.equals(CommonUtil.getValue(carrierData.getUdfs(), "TRANSPORTSTATE"), 
            //        GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP))
            //{
            //    carrierUdfs.put("MACHINENAME", "");            
            //    carrierUdfs.put("PORTNAME", "");
            //    carrierUdfs.put("POSITIONTYPE", "");
            //    carrierUdfs.put("POSITIONNAME", "");
            //    carrierUdfs.put("ZONENAME", "");
            //}
            
            carrierUdfs.put("MACHINENAME", "");  
        }
        
        carrierUdfs.put("PORTNAME", "");
        carrierUdfs.put("POSITIONTYPE", "");
        carrierUdfs.put("POSITIONNAME", "");
        carrierUdfs.put("ZONENAME", "");
        
        setAreaInfo.setUdfs(carrierUdfs);
        
        eventInfo.setEventName("ChangeTransportState");

        try
        {
            carrierData = DurableServiceProxy.getDurableService().setArea(carrierData.getKey(), eventInfo, setAreaInfo);
            return carrierData;
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("CST-9999", fe.getMessage());
        }
    }
    
	/**
	 * make tranport state to ON EQP
	 * 2015.08.27 by swcho : modified with optimized event and error handling
	 * @author hykim
	 * @since 2014.05.19
	 * @param portData
	 * @param makeTransportStateOnEQP
	 * @param eventInfo
	 * @return
	 * @throws CustomException
	 */
	public Durable load(EventInfo eventInfo, String carrierName, String machineName, String portName) 
			throws CustomException 
	{
		Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		
		SetAreaInfo setAreaInfo = new SetAreaInfo();
		setAreaInfo.setAreaName("");
		
		Map<String, String> carrierUdfs = carrierData.getUdfs();
		carrierUdfs.put("MACHINENAME", machineName);
		carrierUdfs.put("PORTNAME", portName);
		carrierUdfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP);
		carrierUdfs.put("POSITIONTYPE", "PORT");
		carrierUdfs.put("POSITIONNAME", "");
		carrierUdfs.put("ZONENAME", "");
		
		setAreaInfo.setUdfs(carrierUdfs);
		
		eventInfo.setEventName("Load");
		
		try
		{
			carrierData = DurableServiceProxy.getDurableService().setArea(carrierData.getKey(), eventInfo, setAreaInfo);
			
			return carrierData;
		}
		//catch (NotFoundSignal ne)
		//{
		//	
		//}
		//catch (InvalidStateTransitionSignal te)
		//{
		//	
		//}
		//catch (DuplicateNameSignal de)
		//{
		//	
		//}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("CST-9999", fe.getMessage());
		}
	}

	/**
     * make tranport state to ON EQP
     * 2015.08.27 by swcho : modified with optimized event and error handling
     * @author hykim
     * @since 2014.05.19
     * @param portData
     * @param makeTransportStateOnEQP
     * @param eventInfo
     * @return
     * @throws CustomException
     */
    public Durable load(EventInfo eventInfo, String carrierName, String machineName, String portName, String areaName) 
            throws CustomException 
    {
        Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
        
        SetAreaInfo setAreaInfo = new SetAreaInfo();
        /* 20181203, hhlee, modify, durable Data Modify ==>> */
        //setAreaInfo.setAreaName("");
        setAreaInfo.setAreaName(areaName);
        /* <<== 20181203, hhlee, modify, durable Data Modify */
        
        Map<String, String> carrierUdfs = carrierData.getUdfs();
        carrierUdfs.put("MACHINENAME", machineName);
        carrierUdfs.put("PORTNAME", portName);
        carrierUdfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP);
        carrierUdfs.put("POSITIONTYPE", "PORT");
        carrierUdfs.put("POSITIONNAME", "");
        carrierUdfs.put("ZONENAME", "");
        
        setAreaInfo.setUdfs(carrierUdfs);
        
        eventInfo.setEventName("Load");
        
        try
        {
            carrierData = DurableServiceProxy.getDurableService().setArea(carrierData.getKey(), eventInfo, setAreaInfo);
            
            return carrierData;
        }
        //catch (NotFoundSignal ne)
        //{
        //  
        //}
        //catch (InvalidStateTransitionSignal te)
        //{
        //  
        //}
        //catch (DuplicateNameSignal de)
        //{
        //  
        //}
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("CST-9999", fe.getMessage());
        }
    }
    
	/**
	 * 
	 * @Name     checkLoadedCarrier
	 * @since    2018. 10. 31.
	 * @author   hhlee
	 * @contents 
	 *           
	 * @param eventInfo
	 * @param factoryName
	 * @param carrierName
	 * @param machineName
	 * @param portName
	 * @throws CustomException
	 */
	public void checkLoadedCarrier(EventInfo eventInfo, String factoryName, String carrierName, String machineName, String portName) throws CustomException 
	{
	    try
        {
	        String condition = " FACTORYNAME = ? AND MACHINENAME = ? AND PORTNAME = ? ";
	        Object[] bindSet = new Object[] {factoryName, machineName, portName};
	         
	        List <Durable> durableDataList = DurableServiceProxy.getDurableService().select(condition, bindSet);
	        
	        if(durableDataList.size() > 0)
	        {
	            SetEventInfo setEventInfo = new SetEventInfo();
	            Map<String, String> durableUdfs = null;
	            for(Durable durableData : durableDataList)
	            {
	                if(StringUtil.equals(carrierName, durableData.getKey().getDurableName()) && 
	                        StringUtil.equals(machineName, CommonUtil.getValue(durableData.getUdfs(), "MACHINENAME")) &&
	                        StringUtil.equals(portName, CommonUtil.getValue(durableData.getUdfs(), "PORTNAME")) )
	                {
	                    // To Do .......
	                }
	                else
	                {
    	                durableUdfs = durableData.getUdfs();
    	                durableUdfs.put("MACHINENAME", "");
    	                durableUdfs.put("PORTNAME", "");
    	                durableUdfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTEQP);
    	                durableUdfs.put("POSITIONTYPE", "");
    	                durableUdfs.put("POSITIONNAME", "");
    	                durableUdfs.put("ZONENAME", "");
    	                
    	                eventInfo.setEventName("ChangeTransportState");
    	                eventInfo.setEventComment("[MachineName : " + machineName + ", " + " PortName : " + portName + "] - " +
    	                                          "Current Load CarrierName : " + carrierName + ", Before load CarrierName : " + durableData.getKey().getDurableName());
                       
                        setEventInfo.setUdfs(durableUdfs);
    	                try
    	                {
    	                    durableData = DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);	                   
    	                }
    	                catch(Exception ex)
    	                {
    	                    //log.warn(ex.getStackTrace());
    	                }
    	                
    	                eventInfo.setCheckTimekeyValidation(false);
                        eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
	                }
	            }
	        }
	    
        }	    
	    catch(Exception ex)
        {
	        //log.warn(ex.getStackTrace());
        }
	}
	/*
	* Name : checkExistDurable
	* Desc : This function is NanaTrack API Call Durable checkExistDurable
	* Author : AIM Systems, Inc
	* Date : 2011.02.10
	*/
	public boolean checkExistDurable(String durableName) throws CustomException{

		boolean existDurableName= false;
		
		String condition = "DURABLENAME = ?";
		
		Object[] bindSet = new Object[] {durableName};
		
		List <Durable> sqlResult = null;
		
		try
		{
			sqlResult = DurableServiceProxy.getDurableService().select(condition, bindSet);
			if(sqlResult.size() > 0)
			{
				throw new CustomException("MASK-0001",durableName);
			}
			return existDurableName= false;
		}
		catch(Exception e)
		{
			if (sqlResult != null)
				throw new CustomException("MASK-0001",durableName);
			
			return false;
		}
	}
		
			 
	/**
	 * @author LHKIM
	 * @since 2014.10.23
	 * @param eventInfo
	 * @param durableData
	 * @param machineName
	 * @param portName
	 * @throws CustomException
	 */
	public SetAreaInfo makeEVAMaskCSTTransportStateOnEQP(EventInfo eventInfo, Durable durableData, String machineName, String portName) 
	throws CustomException 
	{		
		SetAreaInfo setAreaInfo = new SetAreaInfo();

		setAreaInfo.setAreaName("");
		Map<String, String> carrierUdfs = durableData.getUdfs();
		carrierUdfs.put("MACHINENAME", machineName);
		carrierUdfs.put("PORTNAME", portName);
		carrierUdfs.put("TRANSPORTSTATE",
				GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP);
		carrierUdfs.put("POSITIONTYPE", "PORT");
		carrierUdfs.put("POSITIONNAME", "");
		carrierUdfs.put("ZONENAME", "");
		setAreaInfo.setUdfs(carrierUdfs);
		
		return setAreaInfo;
	}
	
	/**
	 * @author LHKIM
	 * @since 2014.10.23
	 * @param 
	 * @param makeTransportStateOutSTK
	 * @param eventInfo
	 * @throws CustomException
	 */
	public SetAreaInfo  makeTransportStateOutSTK(EventInfo eventInfo, String carrierName, String machineName, String portName) 
	throws CustomException 
	{
		Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		
		SetAreaInfo setAreaInfo = new SetAreaInfo();

		setAreaInfo.setAreaName("");

		Map<String, String> carrierUdfs = carrierData.getUdfs();
		carrierUdfs.put("MACHINENAME", "");
		carrierUdfs.put("PORTNAME", "");
		carrierUdfs.put("TRANSPORTSTATE",
				GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
		carrierUdfs.put("POSITIONTYPE","");
		carrierUdfs.put("POSITIONNAME", "");
		carrierUdfs.put("ZONENAME", "");
		setAreaInfo.setUdfs(carrierUdfs);
		
		return setAreaInfo;
		
		
	}
		
	/*
	 * Name : setEvent Desc : This function is NanaTrack API Call Durable
	 * setEvent Author : hykim , Inc Date : 2015.02.02
	 */
	public void makeUnScrap(Durable durableData,
			EventInfo eventInfo) throws InvalidStateTransitionSignal,
			FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException {

		String sql = " UPDATE DURABLE SET " + 
					 " DURABLESTATE = :durableState WHERE DURABLENAME = :durableName ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("durableState" , "Available");
		bindMap.put("durableName"  , durableData.getKey().getDurableName());
		
		try
		{
//			greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql, bindMap);
			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		}
		catch(Exception e)
		{
			throw new CustomException("SYS-8001", e.getMessage());
		}

		SetEventInfo setEventInfo = new SetEventInfo();
		DurableServiceProxy.getDurableService().setEvent(durableData.getKey(),
				eventInfo, setEventInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey"
				+ eventInfo.getEventTimeKey());
	}
	
	/**
     * @author smkang
     * @since 2019.05.28
     * @param durableData
     * @param updateUdfs
     * @see DurableServiceProxy.getDurableService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
     */
    public synchronized void updateDurableWithoutHistory(Durable durableData, Map<String, String> updateUdfs) {
    	if (durableData != null && updateUdfs != null && updateUdfs.size() > 0) {
    		try {
    			String sqlStatement = "";
        		String setStatement = "";    		
        		
        		Set<String> keys = updateUdfs.keySet();
        		for (String key : keys) {
        			durableData.getUdfs().put(key, updateUdfs.get(key));
        			setStatement = setStatement.concat(key).concat(" = :").concat(key).concat(", "); 
    			}
        		
        		sqlStatement = sqlStatement.concat("UPDATE DURABLE SET ").concat(StringUtils.removeEnd(setStatement, ", ")).concat(" WHERE DURABLENAME = :DURABLENAME");
        		updateUdfs.put("DURABLENAME", durableData.getKey().getDurableName());
        		
        		if (GenericServiceProxy.getSqlMesTemplate().update(sqlStatement, updateUdfs) > 0)
        			log.debug(sqlStatement + " is succeeded to be executed.");
        		else
        			log.debug(sqlStatement + " is failed to be executed.");
			} catch (Exception e) {
				throw e;
			}
    	}
    }
}