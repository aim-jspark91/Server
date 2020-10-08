package kr.co.aim.messolution.durable.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class ProbeCardStateReport extends SyncHandler{
	private static Log log = LogFactory.getLog(ProbeCardStateReport.class);
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		//Document copyDoc = (Document)doc.clone();
		
        SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_ProbeCardStateReportCheckReply");
		
		SMessageUtil.setBodyItemValue(doc, "RESULT", "OK",true);
		SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "",true);
		
		String smachineName= SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		List<Element> probeCardElement = SMessageUtil.getBodySequenceItemList(doc, "PROBECARDLIST", false);
		
		//getMachineData
		Machine mainMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(smachineName);
		
		String communicationState = mainMachineData.getCommunicationState();
		
		//EventInfo eventInfo = EventInfoUtil.makeEventInfo("ProbeCardStateReport", getEventUser(), getEventComment(), "", "");
        EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
        
        eventInfo.setEventName("ProbeCardChangeState");
        
        /* 20180926, Add, PROBECARDLIST validation ==>> */
        if(probeCardElement.size() <= 0)
        {
            throw new CustomException("PROBECARD-0008",smachineName, StringUtil.EMPTY);
        }        
        /* <<== 20180926, Add, PROBECARDLIST validation */
        
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
		    //for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "PROBECARDLIST", false))
		    for (Element eledur : probeCardElement)
			{
				String sDurableName = SMessageUtil.getChildText(eledur, "PROBECARDNAME", true);
				
				if(StringUtil.isNotEmpty(sDurableName))		
				{		
					/* 20181019, hhlee, Modify, MACHINERECIPENAME is not used ==>> */
				    //String sMachineRecipeName = SMessageUtil.getChildText(eledur, "MACHINERECIPENAME", true);
				    String sprobeCardSpecName = SMessageUtil.getChildText(eledur, "PROBECARDSPECNAME", false);
				    /* <<== 20181019, hhlee, Modify, MACHINERECIPENAME is not used */
				    
					String sposition = SMessageUtil.getChildText(eledur, "POSITION", false);
					String sunitName = SMessageUtil.getChildText(eledur, "UNITNAME", true);				
					String sSubunitName = SMessageUtil.getChildText(eledur, "SUBUNITNAME", false); //Column:POSITIONNAME
					String sUsedCount = SMessageUtil.getChildText(eledur, "USEDCOUNT", false);
					String sUsedCountLimit = SMessageUtil.getChildText(eledur, "USEDCOUNTLIMIT", false);
					
					// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					Durable durableData = CommonUtil.getDurableInfo(sDurableName);
					Durable durableData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(sDurableName));
					
					/* 20181022, hhlee, add, Validate PROBECARDSPECNAME by MantisID 0001118  ==>> */
			        if(!StringUtil.equals(durableData.getDurableSpecName(), sprobeCardSpecName))
			        {
			            throw new CustomException("PROBECARD-0012", durableData.getDurableSpecName(), sprobeCardSpecName);
			        }        
			        /* <<== 20181022, hhlee, add, Validate PROBECARDSPECNAME by MantisID 0001118  */
			        
					//CheckDurableSpec
					MESDurableServiceProxy.getDurableServiceUtil().probeCheckByDurableSpec(durableData.getDurableSpecName());
					
					/* 20181019, hhlee, Delete, MACHINERECIPENAME is not used ==>> */
					////MESDurableServiceProxy.getDurableServiceUtil().probeCheckByTPEFOMPolicy(sDurableName,smachineName,sMachineRecipe);
					//MESDurableServiceProxy.getDurableServiceUtil().probeCheckByTPEFOMPolicyNotMachine(durableData.getDurableSpecName(),smachineName,sMachineRecipeName);
					/* <<== 20181019, hhlee, Delete, MACHINERECIPENAME is not used */
					
					Map<String, String> udfs = CommonUtil.setNamedValueSequence(eledur, Durable.class.getSimpleName());
					
					if(!StringUtils.isEmpty(sUsedCount))
						durableData.setTimeUsed(Integer.parseInt(sUsedCount));
					if(!StringUtils.isEmpty(sUsedCountLimit))
						durableData.setTimeUsedLimit(Integer.parseInt(sUsedCountLimit));				    
					
					/* 20180813, Add, ProbeCard Mount ==>> */
                    durableData.setDurableState(GenericServiceProxy.getConstantMap().Cons_Mount);
                    
                    DurableServiceProxy.getDurableService().update(durableData);
                    /* <<== 20180813, Add, ProbeCard Mount */
                    
					// Put data into UDF
					udfs.put("MACHINENAME", smachineName);
					udfs.put("UNITNAME", sunitName);
					//udfs.put("DURABLENAME", sDurableName);
					udfs.put("MASKPOSITION", sposition);   
					udfs.put("POSITIONNAME", sSubunitName);   
					/* 20181019, hhlee, Delete, MACHINERECIPENAME is not used ==>> */
					//udfs.put("MACHINERECIPE", sMachineRecipeName); 
					/* <<== 20181019, hhlee, Delete, MACHINERECIPENAME is not used */
					
					/* 20180813, Add, ProbeCard Mount ==>> */
					udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP);
					/* <<== 20180813, Add, ProbeCard Mount */					
		
					// SetEvent Info create
					SetEventInfo setEventInfo = new SetEventInfo();
					setEventInfo.setUdfs(udfs);
					
					// Excute
					MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
					
					log.info("DurableName = "+sDurableName + " , Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
					
					eventInfo.setCheckTimekeyValidation(false);
                    eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                    eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
				}	
			}
		}
		
		return doc;
	}
	
	private void setMaskPositionUpdate(String durableName) throws CustomException 
	{
	    try
        {
            StringBuilder sql = new StringBuilder();
            sql.setLength(0);
            sql.append("UPDATE DURABLE A                                                    ");
            sql.append("     SET  A.MASKPOSITION = NULL WHERE A.DURABLENAME = :DURABLENAME  ");
            Map<String, Object> bindMap = new HashMap<String, Object>();
            bindMap.put("DURABLENAME", durableName);
            int rows = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().update(sql.toString(), bindMap);
        }
        catch(Exception ex)
        {
            
        }
	}
}
