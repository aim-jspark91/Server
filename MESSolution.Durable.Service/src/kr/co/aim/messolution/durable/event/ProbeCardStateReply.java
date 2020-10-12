package kr.co.aim.messolution.durable.event;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class ProbeCardStateReply extends AsyncHandler{
	private static Log log = LogFactory.getLog(ProbeCardStateReply.class);
	@Override
	public void doWorks(Document doc) throws CustomException {

String smachineName= SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
        
        //getMachineData
        Machine mainMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(smachineName);
        
        String communicationState = mainMachineData.getCommunicationState();
        
        //EventInfo eventInfo = EventInfoUtil.makeEventInfo("ProbeCardStateReport", getEventUser(), getEventComment(), "", "");
        EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
        
        eventInfo.setEventName("ProbeCardChangeState");
        
        Element eleBody = SMessageUtil.getBodyElement(doc);
        
        /* 20180813, Add, ProbeCard Mount ==>> */
        List<Durable> probeCardList = null;
        try
        {
    		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//            probeCardList = DurableServiceProxy.getDurableService().select("WHERE DURABLETYPE = ? AND MACHINENAME = ?", new Object[] {GenericServiceProxy.getConstantMap().DURABLETYPE_PBOBECARD, smachineName });
            probeCardList = DurableServiceProxy.getDurableService().select("WHERE DURABLETYPE = ? AND MACHINENAME = ? FOR UPDATE", new Object[] {GenericServiceProxy.getConstantMap().DURABLETYPE_PBOBECARD, smachineName });
        }
        catch(NotFoundSignal ne)
        {
            log.info("Not Mount");  
        }
        if(probeCardList != null &&  probeCardList.size() > 0)
        { 
            for (Durable probeCard : probeCardList)
            {
                Map<String, String> probeCardUdf = probeCard.getUdfs();
                
                //probeCard.setDurableState(GenericServiceProxy.getConstantMap().Cons_Unmount);
                probeCard.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
                
                DurableServiceProxy.getDurableService().update(probeCard);
                
                /* 20180813, Add, Change the MaskPosition value to NULL when unmounting ProbeCard. ==>> */
                CommonUtil.setMaskPositionUpdate(probeCard.getKey().getDurableName());
                /* <<== 20180813, Add, Change the MaskPosition value to NULL when unmounting ProbeCard. */
                
                // Put data into UDF
                probeCardUdf.put("MACHINENAME", StringUtil.EMPTY);
                probeCardUdf.put("UNITNAME", StringUtil.EMPTY);
                //probeCardUdf.put("DURABLENAME", probeCard.getKey().getDurableName());
                //probeCardUdf.put("MASKPOSITION", StringUtil.EMPTY);   
                probeCardUdf.put("POSITIONNAME", StringUtil.EMPTY);   
                probeCardUdf.put("MACHINERECIPE", StringUtil.EMPTY);   
                
                probeCardUdf.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTEQP);
                                
                // SetEvent Info create
                SetEventInfo setEventInfo = new SetEventInfo();
                setEventInfo.setUdfs(probeCardUdf);
                
                // Excute
                MESDurableServiceProxy.getDurableServiceImpl().setEvent(probeCard, setEventInfo, eventInfo);
                
                eventInfo.setCheckTimekeyValidation(false);
                eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
            }
        }   
        /* <<== 20180813, Add, ProbeCard Mount */                   
        
        
        if(eleBody!=null)
        {
            for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "PROBECARDLIST", false))
            {
                String sDurableName = SMessageUtil.getChildText(eledur, "PROBECARDNAME", true);
                
                if(StringUtil.isNotEmpty(sDurableName))     
                {       
                    try
                    {
                        String sMachineRecipeName = SMessageUtil.getChildText(eledur, "MACHINERECIPENAME", true);
                        String sposition = SMessageUtil.getChildText(eledur, "POSITION", false);
                        String sunitName = SMessageUtil.getChildText(eledur, "UNITNAME", true);             
                        String sSubunitName = SMessageUtil.getChildText(eledur, "SUBUNITNAME", false); //Column:POSITIONNAME
                        String sUsedCount = SMessageUtil.getChildText(eledur, "USEDCOUNT", false);
                        String sUsedCountLimit = SMessageUtil.getChildText(eledur, "USEDCOUNTLIMIT", false);
                        
                        //getDurableData
                        Durable durableData = CommonUtil.getDurableInfo(sDurableName);
                        
                        //CheckDurableSpec
                        MESDurableServiceProxy.getDurableServiceUtil().probeCheckByDurableSpec(durableData.getDurableSpecName());
                        
                        /* 20180925, hhlee, Add TPEFOMPolicy Validation ==>> */
                        //MESDurableServiceProxy.getDurableServiceUtil().probeCheckByTPEFOMPolicy(sDurableName,smachineName,sMachineRecipe);
                        MESDurableServiceProxy.getDurableServiceUtil().probeCheckByTPEFOMPolicyNotMachine(durableData.getDurableSpecName(),smachineName,sMachineRecipeName);
                        /* <<== 20180925, hhlee, Add TPEFOMPolicy Validation */
                        
                        //CheckProbeCardDuplication
                        if(MESDurableServiceProxy.getDurableServiceUtil().checkPBCardDuplicationByMachinePBList(smachineName, sDurableName) == true)
                            throw new CustomException("PROBECARD-0004", smachineName,sDurableName);
                        
                        if(StringUtil.equals(CommonUtil.getValue(mainMachineData.getUdfs(), "OPERATIONMODE"), GenericServiceProxy.getConstantMap().OPERATIONMODE_NORMAL))
                        {
                            //OperationMode:NORMAL
                            if(MESDurableServiceProxy.getDurableServiceUtil().getPBListByMachine(smachineName).size() <=0)
                            {
                                MESDurableServiceProxy.getDurableServiceUtil().updateProbeCardData(sDurableName,GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP,sunitName,smachineName,sposition,sMachineRecipeName,eventInfo);
                            }
                            /* 20180814, Add, When there is no probecard which a mount becomes to Unit ==>> */
                            else if(MESDurableServiceProxy.getDurableServiceUtil().getPBListByUnitName(sunitName).size() <=0)
                            {
                                MESDurableServiceProxy.getDurableServiceUtil().updateProbeCardData(sDurableName,GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP,sunitName,smachineName,sposition,sMachineRecipeName,eventInfo);
                            }
                            /* <<== 20180814, Add, When there is no probecard which a mount becomes to Unit */
                            else
                            {           
                                //One Unit MaxPBcount = 4, One Machine 2Uits = 8
                                if(MESDurableServiceProxy.getDurableServiceUtil().getPBListByMachine(smachineName).size() > 8)
                                    throw new CustomException("PROBECARD-0010", smachineName);
                                                                        
                                if(!MESDurableServiceProxy.getDurableServiceUtil().getPBListByMachine(smachineName).get(0).getDurableSpecName().equals(durableData.getDurableSpecName()))
                                    throw new CustomException("PROBECARD-0002", smachineName, 
                                            MESDurableServiceProxy.getDurableServiceUtil().getPBListByMachine(smachineName).get(0).getKey().getDurableName(),
                                            MESDurableServiceProxy.getDurableServiceUtil().getPBListByMachine(smachineName).get(0).getDurableSpecName(),
                                            durableData.getKey().getDurableName(), durableData.getDurableSpecName());
                                
                                //check ProbeCardCondition
                                MESDurableServiceProxy.getDurableServiceUtil().checkProbeCardCondition(smachineName, CommonUtil.getValue(mainMachineData.getUdfs(), "OPERATIONMODE"),true);
                                
                                //execute
                                MESDurableServiceProxy.getDurableServiceUtil().updateProbeCardData(sDurableName,GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP,sunitName,smachineName,sposition,sMachineRecipeName,eventInfo);
                            }
                        }
                        else if(StringUtil.equals(CommonUtil.getValue(mainMachineData.getUdfs(), "OPERATIONMODE"), GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
                        {
                            //OperationMode:INDP
                            //means no probeCard in this unit
                            if(MESDurableServiceProxy.getDurableServiceUtil().getPBListByUnitName(sunitName).size() <=0)
                            {
                                MESDurableServiceProxy.getDurableServiceUtil().updateProbeCardData(sDurableName,GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP,sunitName,smachineName,sposition,sMachineRecipeName,eventInfo);
                            }
                            else
                            {
                                if(MESDurableServiceProxy.getDurableServiceUtil().getPBListByUnitName(sunitName).size() > 4)
                                    throw new CustomException("PROBECARD-0007", sunitName, smachineName);
                                    
                                if(!MESDurableServiceProxy.getDurableServiceUtil().getPBListByUnitName(sunitName).get(0).getDurableSpecName().equals(durableData.getDurableSpecName()))
                                    throw new CustomException("PROBECARD-0003",smachineName + "-" + sunitName,  
                                            MESDurableServiceProxy.getDurableServiceUtil().getPBListByUnitName(sunitName).get(0).getKey().getDurableName(),
                                            MESDurableServiceProxy.getDurableServiceUtil().getPBListByUnitName(sunitName).get(0).getDurableSpecName(),
                                            durableData.getKey().getDurableName(),
                                            durableData.getDurableSpecName());
                                
                                //check ProbeCardCondition
                                MESDurableServiceProxy.getDurableServiceUtil().checkProbeCardCondition(smachineName, CommonUtil.getValue(mainMachineData.getUdfs(), "OPERATIONMODE"),true);
                                
                                //execute
                                MESDurableServiceProxy.getDurableServiceUtil().updateProbeCardData(sDurableName,GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP,sunitName,smachineName,sposition,sMachineRecipeName,eventInfo);
                            }
                        }
                        
                        eventInfo.setCheckTimekeyValidation(false);
                        eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
                    }
                    catch (CustomException ce)
                    {
                        log.error(String.format("[ERRORCODE : %s] , [ERRORMESSAGE : %s]",  ce.errorDef.getErrorCode(), ce.errorDef.getLoc_errorMessage()));
                    }
                }   
            }
        }
        
//		String smachineName= SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
//
//		//getMachineData
//		Machine mainMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(smachineName);
//
//		//EventInfo eventInfo = EventInfoUtil.makeEventInfo("StateReport", getEventUser(), getEventComment(), "", "");
//		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
//		
//		eventInfo.setEventName("ProbeCardChangeState");
//
//		Element eleBody = SMessageUtil.getBodyElement(doc);
//
//		/* 20180813, Add, ProbeCard Mount ==>> */
//        List<Durable> probeCardList = null;
//        try
//        {
////          probeCardList = DurableServiceProxy.getDurableService().select(" WHERE 1=1 "
////                                                                         + " AND DURABLETYPE = ? "
////                                                                         + " AND MACHINENAME = ? AND UNITNAME = ? "
////                                                                         + " AND MASKPOSITION = ? AND TRANSPORTSTATE = ? ",
////          new Object[] {durableData.getDurableType(),
////                    smachineName, sunitName, sposition, GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP });
//                                                                           
//            probeCardList = DurableServiceProxy.getDurableService().select(" WHERE 1=1 "
//                                                                           + " AND DURABLETYPE = ? "
//                                                                           + " AND MACHINENAME = ? " ,
//                    new Object[] {GenericServiceProxy.getConstantMap().DURABLETYPE_PBOBECARD, smachineName });
//        }
//        catch(NotFoundSignal ne)
//        {
//            log.info("Not Mount");  
//        }
//        if(probeCardList != null &&  probeCardList.size() > 0)
//        { 
//            for (Durable probeCard : probeCardList)
//            {
//                Map<String, String> probeCardUdf = probeCard.getUdfs();
//                
//                // Modified by smkang on 2018.09.25 - Available state is used instead of UnMount state.
////                probeCard.setDurableState(GenericServiceProxy.getConstantMap().Cons_Unmount);
//                probeCard.setDurableState(GenericServiceProxy.getConstantMap().Cons_Available);
//                
//                DurableServiceProxy.getDurableService().update(probeCard);
//                
//                /* 20180813, Add, Change the MaskPosition value to NULL when unmounting ProbeCard. ==>> */
//                CommonUtil.setMaskPositionUpdate(probeCard.getKey().getDurableName());
//                /* <<== 20180813, Add, Change the MaskPosition value to NULL when unmounting ProbeCard. */
//                
//                // Put data into UDF
//                probeCardUdf.put("MACHINENAME", StringUtil.EMPTY);
//                probeCardUdf.put("UNITNAME", StringUtil.EMPTY);
//                //probeCardUdf.put("DURABLENAME", probeCard.getKey().getDurableName());
//                probeCardUdf.put("MASKPOSITION", StringUtil.EMPTY);   
//                probeCardUdf.put("POSITIONNAME", StringUtil.EMPTY);   
//                probeCardUdf.put("MACHINERECIPE", StringUtil.EMPTY);   
//                
//                probeCardUdf.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTEQP);
//                                
//                // SetEvent Info create
//                SetEventInfo setEventInfo = new SetEventInfo();
//                setEventInfo.setUdfs(probeCardUdf);
//                
//                // Excute
//                MESDurableServiceProxy.getDurableServiceImpl().setEvent(probeCard, setEventInfo, eventInfo);
//                
//                eventInfo.setCheckTimekeyValidation(false);
//                eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
//                eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//            }
//        }   
//        /* <<== 20180813, Add, ProbeCard Mount */  
//        
//        if(eleBody!=null)
//        {
//            for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "PROBECARDLIST", false))
//            {
//                String sDurableName = SMessageUtil.getChildText(eledur, "PROBECARDNAME", true);
//                
//                if(StringUtil.isNotEmpty(sDurableName))     
//                {       
//                    String sMachineRecipe = SMessageUtil.getChildText(eledur, "MACHINERECIPE", true);
//                    String sposition = SMessageUtil.getChildText(eledur, "POSITION", false);
//                    String sunitName = SMessageUtil.getChildText(eledur, "UNITNAME", true);             
//                    String sSubunitName = SMessageUtil.getChildText(eledur, "SUBUNITNAME", false); //Column:POSITIONNAME
//                    String sUsedCount = SMessageUtil.getChildText(eledur, "USEDCOUNT", false);
//                    String sUsedCountLimit = SMessageUtil.getChildText(eledur, "USEDCOUNTLIMIT", false);
//                    
//                    MESDurableServiceProxy.getDurableServiceUtil().probeCheckByTPEFOMPolicy(sDurableName,smachineName,sMachineRecipe);
//                                
//                    //getDurableData
//                    Durable durableData = CommonUtil.getDurableInfo(sDurableName);
//                    
//                    //CheckDurableSpec
//                    MESDurableServiceProxy.getDurableServiceUtil().probeCheckByDurableSpec(durableData.getDurableSpecName());
//                    
//                    Map<String, String> udfs = CommonUtil.setNamedValueSequence(eledur, Durable.class.getSimpleName());
//                    
//                    if(!StringUtils.isEmpty(sUsedCount))
//                        durableData.setTimeUsed(Integer.parseInt(sUsedCount));
//                    if(!StringUtils.isEmpty(sUsedCountLimit))
//                        durableData.setTimeUsedLimit(Integer.parseInt(sUsedCountLimit));                    
//                    
//                    /* 20180813, Add, ProbeCard Mount ==>> */
//                    durableData.setDurableState(GenericServiceProxy.getConstantMap().Cons_Mount);
//                    
//                    DurableServiceProxy.getDurableService().update(durableData);
//                    /* <<== 20180813, Add, ProbeCard Mount */
//                    
//                    // Put data into UDF
//                    udfs.put("MACHINENAME", smachineName);
//                    udfs.put("UNITNAME", sunitName);
//                    //udfs.put("DURABLENAME", sDurableName);
//                    udfs.put("MASKPOSITION", sposition);   
//                    udfs.put("POSITIONNAME", sSubunitName);   
//                    udfs.put("MACHINERECIPE", sMachineRecipe);   
//                    
//                    /* 20180813, Add, ProbeCard Mount ==>> */
//                    udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP);
//                    /* <<== 20180813, Add, ProbeCard Mount */                   
//        
//                    // SetEvent Info create
//                    SetEventInfo setEventInfo = new SetEventInfo();
//                    setEventInfo.setUdfs(udfs);
//                    
//                    // Excute
//                    MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
//                    
//                    log.info("DurableName = "+sDurableName + " , Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
//                    
//                    eventInfo.setCheckTimekeyValidation(false);
//                    eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
//                    eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//                }   
//            }
//        }
	    
	    
        /* 20180814, delete, ==>> */
//		if(eleBody!=null)
//		{
//			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "PROBECARDLIST", false))
//			{
//				String sDurableName = SMessageUtil.getChildText(eledur, "PROBECARDNAME", true);
//				String sMachineRecipe = SMessageUtil.getChildText(eledur, "MACHINERECIPE", true);
//				String sposition = SMessageUtil.getChildText(eledur, "POSITION", true);
//				String sunitName = SMessageUtil.getChildText(eledur, "UNITNAME", true);
//				String sSubunitName = SMessageUtil.getChildText(eledur, "SUBUNITNAME", false); //Column:POSITIONNAME
//				String sUsedCount = SMessageUtil.getChildText(eledur, "USEDCOUNT", false);
//				String sUsedCountLimit = SMessageUtil.getChildText(eledur, "USEDCOUNTLIMIT", false);
//
//				//getDurableData
//				Durable durableData = CommonUtil.getDurableInfo(sDurableName);
//
//				// Validation
//				if(StringUtils.equals(durableData.getUdfs().get("MACHINENAME"), ""))
//				{
//					throw new CustomException("MASK-0002"+"sDurableName is "+sDurableName);
//				}
//
//				Map<String, String> udfs = CommonUtil.setNamedValueSequence(eledur, Durable.class.getSimpleName());
//
//				if(!StringUtils.isEmpty(sUsedCount))
//					durableData.setTimeUsed(Integer.parseInt(sUsedCount));
//				if(!StringUtils.isEmpty(sUsedCountLimit))
//					durableData.setTimeUsedLimit(Integer.parseInt(sUsedCountLimit));
//                
//                /* 20180813, Add, ProbeCard Mount ==>> */
//                durableData.setDurableState(GenericServiceProxy.getConstantMap().Cons_Mount);
//                
//                DurableServiceProxy.getDurableService().update(durableData);
//                /* <<== 20180813, Add, ProbeCard Mount */
//                
//				// Put data into UDF
//				udfs.put("MACHINENAME", smachineName);
//				udfs.put("UNITNAME", sunitName);
//				//udfs.put("DURABLENAME", sDurableName);
//				udfs.put("MASKPOSITION", sposition);
//				udfs.put("POSITIONNAME", sSubunitName);
//				udfs.put("MACHINERECIPE", sMachineRecipe);
//
//				/* 20180813, Add, ProbeCard Mount ==>> */
//                udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP);
//                /* <<== 20180813, Add, ProbeCard Mount */                   
//                
//				// SetEvent Info create
//				SetEventInfo setEventInfo = new SetEventInfo();
//				setEventInfo.setUdfs(udfs);
//
//				// Excute
//				MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
//
//				log.info("DurableName = "+sDurableName + "Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
//
//				eventInfo.setCheckTimekeyValidation(false);
//                eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
//                eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//			}
//		}
        /* <<== 20180814, delete, */
	}
}
