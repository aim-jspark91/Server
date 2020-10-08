package kr.co.aim.messolution.machine.event;

import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.info.MakeCommunicationStateInfo;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class CommunicationStateChanged  extends SyncHandler{
	private static Log log = LogFactory.getLog(CommunicationStateChanged.class);
	@Override
	public Object doWorks(Document doc) throws CustomException {

	    SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_CommunicationStateCheckReply");
	    
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);		

		SMessageUtil.setBodyItemValue(doc, "RECIPEBYPASSFLAG", "N",true);
		SMessageUtil.setBodyItemValue(doc, "RESULT", "OK",true);
		SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "",true);

		//String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String communicationName = SMessageUtil.getBodyItemValue(doc, "COMMUNICATIONSTATE", true);  //[ OFFLINE | LOCAL | REMOTE ]

		/* 20181112, hhlee, add, machineData Location Change ==>> */
        Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
        /* <<== 20181112, hhlee, add, machineData Location Change */
        
		/* Communications status upper case, lower case classification  */
		if(StringUtil.equals(StringUtil.upperCase(communicationName), StringUtil.upperCase(GenericServiceProxy.getConstantMap().Mac_OnLine)))
		{
			communicationName = GenericServiceProxy.getConstantMap().Mac_OnLine;
		}
		else if(StringUtil.equals(StringUtil.upperCase(communicationName), StringUtil.upperCase(GenericServiceProxy.getConstantMap().Mac_OnLineLocal)))
		{
			communicationName = GenericServiceProxy.getConstantMap().Mac_OnLineLocal;
		}
		else if(StringUtil.equals(StringUtil.upperCase(communicationName), StringUtil.upperCase(GenericServiceProxy.getConstantMap().Mac_OnLineRemote)))
		{
			communicationName = GenericServiceProxy.getConstantMap().Mac_OnLineRemote;
		}
		/* 20181112, hhlee, add, Communicationstate Validation Check(OnLineRemote, OnLineLocal, OffLine) ==>> */
		else if(StringUtil.equals(StringUtil.upperCase(communicationName), StringUtil.upperCase(GenericServiceProxy.getConstantMap().Mac_OffLine)))
		{
			communicationName = GenericServiceProxy.getConstantMap().Mac_OffLine;
			try
			{
				//20190701 wsw OnLine -> OffLine 
			    String runReaonCode = MESMachineServiceProxy.getMachineServiceUtil().getRunStateReasonCode("CommunicationState", "Mac_OffLine");
				
				String reasonCode_State = MESMachineServiceProxy.getMachineServiceUtil().getReasonCode(runReaonCode);
				if(!reasonCode_State.isEmpty())
				{
					String state = machineData.getMachineStateName();
					if( state.equals("IDLE") || state.equals("RUN") )
					{
						machineData.getUdfs().put("STATEREASONCODE", runReaonCode);
						EventInfo eventInfo_state = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");
						MakeMachineStateByStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, reasonCode_State);
						MESMachineServiceProxy.getMachineServiceImpl().makeMachineStateByState(machineData, transitionInfo, eventInfo_state);
					}	
				}
			}
			catch(Exception ex)
			{
				eventLog.warn("Not Registerd State ReasonCode!");
			}
		}
		else
		{		    
            throw new CustomException("MACHINE-0008", machineData.getCommunicationState(), communicationName);            
		}
		/* <<== 20181112, hhlee, add, Communicationstate Validation Check(OnLineRemote, OnLineLocal, OffLine) */
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeCommState", getEventUser(), getEventComment(), null, null);

		/* 20181112, hhlee, delete, machineData Location Change ==>> */
        //Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
        /* <<== 20181112, hhlee, delete, machineData Location Change */
		String currentCommunicationName = machineData.getCommunicationState();
		
		/* hhlee, 20180420, Recipe by pass flag Modify ==> */
		MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
		//String recipeByPassFlag = CommonUtil.getValue(machineData.getUdfs(), "RECIPEBYPASSFLAG");
		
		/* hhlee, 20180918, Recipe by pass flag Modify ==> */
		//String rmsFlag = CommonUtil.getValue(machineSpecData.getUdfs(), "RMSFLAG");
		String rmsByPassFlag = CommonUtil.getValue(machineSpecData.getUdfs(), "RMSBYPASSFLAG");
		/* <<== hhlee, 20180918, Recipe by pass flag Modify */
		
		MakeCommunicationStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeCommunicationStateInfo(machineData, communicationName);
		MESMachineServiceProxy.getMachineServiceImpl().makeCommunicationState(machineData, transitionInfo, eventInfo);
		
		/* <<== hhlee, 20180420, Recipe by pass flag Modify */
		/* hhlee, 20180918, Recipe by pass flag Modify ==> */
		//SMessageUtil.setBodyItemValue(doc, "RECIPEBYPASSFLAG", "N",true);
		SMessageUtil.setBodyItemValue(doc, "RECIPEBYPASSFLAG", "N",true);
		//if(rmsFlag.equals("") ||rmsFlag.equals("N"))
		if(rmsByPassFlag.equals("") ||rmsByPassFlag.equals("Y"))
		{
			SMessageUtil.setBodyItemValue(doc, "RECIPEBYPASSFLAG", "Y",true);
		}
		/* <<== hhlee, 20180918, Recipe by pass flag Modify */
		
 	    //success then report to FMC
		try
		{
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");
		}
		catch(Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}

		return doc;
	}	
}
