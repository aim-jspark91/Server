package kr.co.aim.messolution.durable.event;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class ProbeCardStateChanged  extends SyncHandler{
	private static Log log = LogFactory.getLog(ProbeCardStateChanged.class);
	@Override
	public Object doWorks(Document doc) throws CustomException {

		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_ProbeCardStateChangedCheckReply");
		
		SMessageUtil.setBodyItemValue(doc, "RESULT", "OK",true);
		SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "",true);
		
		String smachineName= SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sUnitName= SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		
		/* 20181019, hhlee, Modify, MACHINERECIPENAME is not used ==>> */
		///* 20180925, hhlee, Add Machine Recipe ==>> */
		String sMachineRecipeName = StringUtil.EMPTY;
		//String sMachineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", true);
		String sprobeCardSpecName = SMessageUtil.getBodyItemValue(doc, "PROBECARDSPECNAME", false);
		///* <<== 20180925, hhlee, Add Machine Recipe */
		/* <<== 20181019, hhlee, Modify, MACHINERECIPENAME is not used */
		
		String spCardPosition= SMessageUtil.getBodyItemValue(doc, "POSITION", true);
		String sProbeCardName = SMessageUtil.getBodyItemValue(doc, "PROBECARDNAME", true);
		String sTransportState = SMessageUtil.getBodyItemValue(doc, "TRANSFERSTATE", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ProbeCardChangeState", getEventUser(), getEventComment(), "", "");
	
		Machine machineData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(smachineName);
		
		Durable pbCardData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sProbeCardName);
		
        /* 20181022, hhlee, add, Validate PROBECARDSPECNAME by MantisID 0001118 ==>> */
		if(!StringUtil.equals(pbCardData.getDurableSpecName(), sprobeCardSpecName))
        {
		    throw new CustomException("PROBECARD-0012", pbCardData.getDurableSpecName(), sprobeCardSpecName);
        }        
        /* <<== 20181022, hhlee, add, Validate PROBECARDSPECNAME by MantisID 0001118  */
		
		MESDurableServiceProxy.getDurableServiceUtil().probeCheckByDurableSpec(pbCardData.getDurableSpecName());		
		
		if(StringUtil.equals(sTransportState, GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTEQP))
		{
		    /* 20180814, Modify, the machine, unit, position don't have a value When TransferState is an OUTEQP ==>> */
			//updateProbeCardData(sProbeCardName,sTransportState,sUnitName,smachineName,spCardPosition,eventInfo);
		    MESDurableServiceProxy.getDurableServiceUtil().updateProbeCardData(sProbeCardName, GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTEQP, 
		            StringUtil.EMPTY, StringUtil.EMPTY, StringUtil.EMPTY, StringUtil.EMPTY, eventInfo);
		    /* <<== 20180814, Modify, the machine, unit, position don't have a value When TransferState is an OUTEQP */
		}
		else
		{
		    /* 20181019, hhlee, Delete, MACHINERECIPENAME is not used ==>> */
		    ///* 20180925, hhlee, Add TPEFOMPolicy Validation ==>> */
		    ////MESDurableServiceProxy.getDurableServiceUtil().probeCheckByTPEFOMPolicy(sDurableName,smachineName,sMachineRecipe);
            //MESDurableServiceProxy.getDurableServiceUtil().probeCheckByTPEFOMPolicyNotMachine(pbCardData.getDurableSpecName(),smachineName,sMachineRecipeName);
            ///* <<== 20180925, hhlee, Add TPEFOMPolicy Validation */
            /* <<== 20181019, hhlee, Delete, MACHINERECIPENAME is not used */
            
            /* 20180926, Delete, when unit is offline, machine can not send Card9ProbCardStateChanged ==>> */
			//CheckProbeCardDuplication
			//if(MESDurableServiceProxy.getDurableServiceUtil().checkPBCardDuplicationByMachinePBList(smachineName, sProbeCardName) == true)
			//	throw new CustomException("PROBECARD-0004", smachineName,sProbeCardName);
			/* <<== 20180926, Delete, when unit is offline, machine can not send Card9ProbCardStateChanged */
            
            /* 20180926, add, when unit is offline, machine can not send Card9ProbCardStateChanged ==>> */
            
            MESDurableServiceProxy.getDurableServiceUtil().checkExistenceByMachineNamePosition(eventInfo, pbCardData.getKey().getDurableName(), smachineName, sUnitName, 
                    spCardPosition, sTransportState);
            
            MESDurableServiceProxy.getDurableServiceUtil().checkExistenceByDurableName(eventInfo, pbCardData.getKey().getDurableName(), smachineName, sUnitName, 
                    spCardPosition, sTransportState);
            
            /* <<== 20180926, add, when unit is offline, machine can not send Card9ProbCardStateChanged */            
            
			if(StringUtil.equals(CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"), GenericServiceProxy.getConstantMap().OPERATIONMODE_NORMAL))
			{
				//OperationMode:NORMAL
				if(MESDurableServiceProxy.getDurableServiceUtil().getPBListByMachine(smachineName).size() <=0)
				{
				    MESDurableServiceProxy.getDurableServiceUtil().updateProbeCardData(sProbeCardName,sTransportState,sUnitName,smachineName,spCardPosition,sMachineRecipeName,eventInfo);
				}
				/* 20180814, Add, When there is no probecard which a mount becomes to Unit ==>> */
				else if(MESDurableServiceProxy.getDurableServiceUtil().getPBListByUnitName(sUnitName).size() <=0)
                {
				    MESDurableServiceProxy.getDurableServiceUtil().updateProbeCardData(sProbeCardName,sTransportState,sUnitName,smachineName,spCardPosition,sMachineRecipeName,eventInfo);
                }
				/* <<== 20180814, Add, When there is no probecard which a mount becomes to Unit */
				else
				{			
					//One Unit MaxPBcount = 4, One Machine 2Uits = 8
					if(MESDurableServiceProxy.getDurableServiceUtil().getPBListByMachine(smachineName).size() > 8)
						throw new CustomException("PROBECARD-0010", smachineName);
															
					if(!MESDurableServiceProxy.getDurableServiceUtil().getPBListByMachine(smachineName).get(0).getDurableSpecName().equals(pbCardData.getDurableSpecName()))
						throw new CustomException("PROBECARD-0002", smachineName, 
						        MESDurableServiceProxy.getDurableServiceUtil().getPBListByMachine(smachineName).get(0).getKey().getDurableName(),
						        MESDurableServiceProxy.getDurableServiceUtil().getPBListByMachine(smachineName).get(0).getDurableSpecName(),
						        pbCardData.getKey().getDurableName(),pbCardData.getDurableSpecName());
					
					/* 20180926, hhlee, add ProbeCard Count = 0 ==>> */
					try
					{
    					//check ProbeCardCondition
    					MESDurableServiceProxy.getDurableServiceUtil().checkProbeCardCondition(smachineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"),true);
					}
					catch(CustomException ce)
			        {
					    if(!ce.errorDef.getErrorCode().equals("PROBECARD-0008"))
			            {
					        throw ce;
			            }			            
			        }
					/* <<== 20180926, hhlee, add ProbeCard Count = 0 */
					
					//execute
					MESDurableServiceProxy.getDurableServiceUtil().updateProbeCardData(sProbeCardName,sTransportState,sUnitName,smachineName,spCardPosition,sMachineRecipeName,eventInfo);
				}
			}
			else if(StringUtil.equals(CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"), GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
			{
				//OperationMode:INDP
				//means no probeCard in this unit
				if(MESDurableServiceProxy.getDurableServiceUtil().getPBListByUnitName(sUnitName).size() <=0)
				{
				    MESDurableServiceProxy.getDurableServiceUtil().updateProbeCardData(sProbeCardName,sTransportState,sUnitName,smachineName,spCardPosition,sMachineRecipeName,eventInfo);
				}
				else
				{
					if(MESDurableServiceProxy.getDurableServiceUtil().getPBListByUnitName(sUnitName).size() > 4)
						throw new CustomException("PROBECARD-0007", sUnitName, smachineName);
						
					if(!MESDurableServiceProxy.getDurableServiceUtil().getPBListByUnitName(sUnitName).get(0).getDurableSpecName().equals(pbCardData.getDurableSpecName()))
						throw new CustomException("PROBECARD-0003",smachineName + "-" + sUnitName,  
						        MESDurableServiceProxy.getDurableServiceUtil().getPBListByUnitName(sUnitName).get(0).getKey().getDurableName(),
						        MESDurableServiceProxy.getDurableServiceUtil().getPBListByUnitName(sUnitName).get(0).getDurableSpecName(),
						        pbCardData.getKey().getDurableName(),
						        pbCardData.getDurableSpecName());
					
					/* 20180926, hhlee, add ProbeCard Count = 0 ==>> */
					try
                    {
					    //check ProbeCardCondition
	                    MESDurableServiceProxy.getDurableServiceUtil().checkProbeCardCondition(smachineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"),true);
                    }
                    catch(CustomException ce)
                    {
                        if(!ce.errorDef.getErrorCode().equals("PROBECARD-0008"))
                        {
                            throw ce;
                        }                       
                    }
					/* <<== 20180926, hhlee, add ProbeCard Count = 0 */
					
					//execute
					MESDurableServiceProxy.getDurableServiceUtil().updateProbeCardData(sProbeCardName,sTransportState,sUnitName,smachineName,spCardPosition,sMachineRecipeName,eventInfo);
				}
			}
		}					
		return doc;
	}
	
//	private void updateProbeCardData(String sProbeCardName, String sTransportState, String sUnitName, String smachineName, String spCardPosition, EventInfo eventInfo)
//	throws CustomException
//	{
//		//getDurableData
//		Durable durableData = CommonUtil.getDurableInfo(sProbeCardName);
//		String UnitName = "";
//		
//		//ONEQP -> Mount, OUTEQP -> Unmount
//		if(StringUtil.equals(sTransportState, GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP))
//		{
//			durableData.setDurableState(GenericServiceProxy.getConstantMap().Cons_Mount);
//			UnitName = sUnitName;
//		}		
//		else if(StringUtil.equals(sTransportState, GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTEQP))
//		{
//		    /* 20180813, Add, Change the MaskPosition value to NULL when unmounting ProbeCard. ==>> */
//            CommonUtil.setMaskPositionUpdate(durableData.getKey().getDurableName());
//            /* <<== 20180813, Add, Change the MaskPosition value to NULL when unmounting ProbeCard. */
//            
//			//durableData.setDurableState(GenericServiceProxy.getConstantMap().Cons_Unmount);
//            durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
//			
//		}
//				
//		DurableServiceProxy.getDurableService().update(durableData);
//		
//		/************************************************************
//		  ProbeCard TransportState : ONEQP / OUTEQP
//		*************************************************************/		
//		// Put data into UDF
//		Map<String, String> udfs = new HashMap<String, String>();
//		udfs.put("MACHINENAME", smachineName);	
//		udfs.put("UNITNAME", UnitName);
//		udfs.put("MASKPOSITION", spCardPosition);
//		udfs.put("TRANSPORTSTATE", sTransportState);	
//		
//		// SetEvent Info create
//		SetEventInfo setEventInfo = new SetEventInfo();
//		setEventInfo.setUdfs(udfs);
//
//		// Excute	
//		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
//	}
}
