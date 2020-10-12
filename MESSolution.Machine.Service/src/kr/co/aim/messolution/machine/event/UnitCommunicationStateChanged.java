package kr.co.aim.messolution.machine.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.info.MakeCommunicationStateInfo;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeAccessModeInfo;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class UnitCommunicationStateChanged  extends SyncHandler{
    private static Log log = LogFactory.getLog(UnitCommunicationStateChanged.class);
    @Override
    public Object doWorks(Document doc) throws CustomException {

        SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_UnitCommunicationStateCheckReply");

        SMessageUtil.setBodyItemValue(doc, "RESULT", "OK",true);
        SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "",true);

        /* Copy to FMCSender. Add, hhlee, 20180421 */
        Document originaldoc = (Document)doc.clone();
        
        EventInfo eventInfo_state = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");

        EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");

        /* Validate Machine */
        String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
        Machine mainMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

        MachineSpec mainMachineSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);
        
        String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
        String unitCommunicationstate = SMessageUtil.getBodyItemValue(doc, "COMMUNICATIONSTATE", true);
        
        /* 20181112, hhlee, add, UnitData Location Change ==>> */
        Machine unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
        /* <<== 20181112, add, delete, UnitData Location Change */
        
        if(StringUtil.equals(StringUtil.upperCase(unitCommunicationstate), StringUtil.upperCase(GenericServiceProxy.getConstantMap().Mac_OnLine)))
        {
            unitCommunicationstate = GenericServiceProxy.getConstantMap().Mac_OnLineRemote;
        }
        else if(StringUtil.equals(StringUtil.upperCase(unitCommunicationstate), StringUtil.upperCase(GenericServiceProxy.getConstantMap().Mac_OnLineLocal)))
        {
            unitCommunicationstate = GenericServiceProxy.getConstantMap().Mac_OnLineLocal;
        }
        else if(StringUtil.equals(StringUtil.upperCase(unitCommunicationstate), StringUtil.upperCase(GenericServiceProxy.getConstantMap().Mac_OnLineRemote)))
        {
            unitCommunicationstate = GenericServiceProxy.getConstantMap().Mac_OnLineRemote;
        }
        /* 20181112, hhlee, add, Communicationstate Validation Check(OnLineRemote, OnLineLocal, OffLine) ==>> */
        else if(StringUtil.equals(StringUtil.upperCase(unitCommunicationstate), StringUtil.upperCase(GenericServiceProxy.getConstantMap().Mac_OffLine)))
        {
            unitCommunicationstate = GenericServiceProxy.getConstantMap().Mac_OffLine;
            try
			{    //20190701 wsw OnLine -> OffLine
	            String indpMode = MESMachineServiceProxy.getMachineServiceUtil().getINDPMode(unitName);
	            if(indpMode.equals("INDP") )
	            {
		            unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);  
		            String state = unitData.getMachineStateName();
					if( state.equals("IDLE") || state.equals("RUN") )
					{
					    String runReaonCode = MESMachineServiceProxy.getMachineServiceUtil().getRunStateReasonCode("CommunicationState", "Mac_OffLine");
					    unitData.getUdfs().put("STATEREASONCODE", runReaonCode);
						
						String reasonCode_State = MESMachineServiceProxy.getMachineServiceUtil().getReasonCode(runReaonCode);
						if(!reasonCode_State.isEmpty())
						{	
							MakeMachineStateByStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(unitData, reasonCode_State);
							MESMachineServiceProxy.getMachineServiceImpl().makeMachineStateByState(unitData, transitionInfo, eventInfo_state);
						}
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
            throw new CustomException("MACHINE-0008", unitData.getCommunicationState(), unitCommunicationstate);            
        }
        /* <<== 20181112, hhlee, add, Communicationstate Validation Check(OnLineRemote, OnLineLocal, OffLine) */
        
        /* 20180717, Add, Unit Offline Check ==>> */
        //changeUnitCommunicationState
        eventInfo.setEventName("ChangeUnitCommState");
        /* 20181112, hhlee, delete, UnitData Location Change ==>> */
        //Machine unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);          
        /* <<== 20181112, hhlee, delete, UnitData Location Change */
        
        /* Unit Communication Validation */
        if(StringUtil.equals(unitData.getCommunicationState(), unitCommunicationstate))
        {
            //Only write log for OnlineInitial
            CommonUtil.CustomExceptionLog("MACHINE-0001", unitData.getKey().getMachineName(),
                    unitData.getCommunicationState(), unitCommunicationstate);       
            //return doc;
        }   
                
        /* Get Machine Port List */
        List<Map<String, Object>> machinePortList = searchMachinePortList(machineName);
        if(machinePortList == null || machinePortList.size() <= 0)
        {
            //Only write log for OnlineInitial
            CommonUtil.CustomExceptionLog("PORT-9001", machineName, StringUtil.EMPTY);
            return doc;
        }
        
        //MakeCommunicationStateInfo commtransitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeCommunicationStateInfo(unitData, unitCommunicationstate);
        //MESMachineServiceProxy.getMachineServiceImpl().makeCommunicationState(unitData, commtransitionInfo, eventInfo);               
        /* <<== 20180717, Add, Unit Offline Check */
        
        //Check Port part 
        List<Element> portList = SMessageUtil.getSubSequenceItemList(SMessageUtil.getBodyElement(doc), "PORTLIST", false);
        
        ///* 20190103, hhlee, add, check LINKEDUNIT of PORT Table 
        // *           Compare portList and port of LINKEDUNIT ==>> */
        ///* 20190104, hhlee, add, add validation [if(portList != null && portList.size() > 0)] */
        //if(portList != null && portList.size() > 0)
        //{
        //    MESPortServiceProxy.getPortServiceUtil().checkLinkedUnitPortInfo(machineName, unitName, portList);
        //}
        ///* <<== 20190103, hhlee, add, check LINKEDUNIT of PORT Table */ 
        
        /* 20190107, hhlee, modify, change logic ==>> */
        /* Unit Communication State Change */
        eventInfo.setEventName("ChangeUnitCommState");
        unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);            
        MakeCommunicationStateInfo commtransitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeCommunicationStateInfo(unitData, unitCommunicationstate);       
        MESMachineServiceProxy.getMachineServiceImpl().makeCommunicationState(unitData, commtransitionInfo, eventInfo);
        
        if(StringUtil.equals(CommonUtil.getValue(mainMachineData.getUdfs(), "OPERATIONMODE"),GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
        {
            /* 20190103, hhlee, add, check LINKEDUNIT of PORT Table 
             *           Compare portList and port of LINKEDUNIT ==>> */
            /* 20190104, hhlee, add, add validation [if(portList != null && portList.size() > 0)] */
            if(portList != null && portList.size() > 0)
            {
                MESPortServiceProxy.getPortServiceUtil().checkLinkedUnitPortInfo(machineName, unitName, portList);
            }
            /* <<== 20190103, hhlee, add, check LINKEDUNIT of PORT Table */     
            
            MESPortServiceProxy.getPortServiceUtil().unitCommunicationStateChangeByIndp(eventInfo, machineName, unitName, portList);          
        }
        else
        {
            MESPortServiceProxy.getPortServiceUtil().unitCommunicationStateChangeByNormal(eventInfo, machineName, unitName, portList);   
        }
        /* <<== 20190107, hhlee, modify, change logic */        
        
        //---------------Send to FMC---------------------
        try
        {
            /* Make Communication State Change Message */
            originaldoc = generateCommunicationStateChangeTemplate(originaldoc, unitName, unitCommunicationstate);
            GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), originaldoc, "FMCSender");
        }
        catch(Exception ex)
        {
            eventLog.warn("FMC Report Failed!");
        }
        //---------------Send to FMC---------------------

        return doc;
    }

    /**
     * @Name     generateCommunicationStateChangeTemplate
     * @since    2018. 4. 21.
     * @author   hhlee
     * @contents Make Communication State Change Message
     * @param doc
     * @param machinename
     * @param communicatestate
     * @return doc
     * @throws CustomException
     */
    private Document generateCommunicationStateChangeTemplate(Document doc, String machinename,
                                                                       String communicatestate ) throws CustomException
    {
        SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_CommunicationStateChanged");
        SMessageUtil.setHeaderItemValue(doc, "EVENTCOMMENT", "ChangeAccessMode");


        Element bodyElement = SMessageUtil.getBodyElement(doc);

        bodyElement.removeContent();

        Element machineNameElement = new Element("MACHINENAME");
        machineNameElement.setText(machinename);
        bodyElement.addContent(machineNameElement);

        Element communicateState = new Element("COMMUNICATIONSTATE");
        communicateState.setText(communicatestate);
        bodyElement.addContent(communicateState);

        return doc;
    }

    /**
     * @Name     generatePortAccessModeChangeTemplate
     * @since    2018. 4. 21.
     * @author   hhlee
     * @contents Make Port Access Mode Change Message
     * @param doc
     * @param machinename
     * @param portname
     * @param porttype
     * @param portusetype
     * @param accessmode
     * @return doc
     * @throws CustomException
     */
    private Document generatePortAccessModeChangeTemplate(Document doc, String machinename,
                                                     String portname, String porttype, String portusetype,
                                                                       String accessmode) throws CustomException
    {
        SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_PortAccessModeChanged");
        SMessageUtil.setHeaderItemValue(doc, "EVENTCOMMENT", "ChangeCommState");

        Element bodyElement = SMessageUtil.getBodyElement(doc);

        bodyElement.removeContent();

        Element machineNameElement = new Element("MACHINENAME");
        machineNameElement.setText(machinename);
        bodyElement.addContent(machineNameElement);

        Element portNameElement = new Element("PORTNAME");
        portNameElement.setText(portname);
        bodyElement.addContent(portNameElement);

        /* 2018.04.02, hhlee, porttype element Add */
        Element portTypeElement = new Element("PORTTYPE");
        portTypeElement.setText(porttype);
        bodyElement.addContent(portTypeElement);

        Element portUseTypeElement = new Element("PORTUSETYPE");
        portUseTypeElement.setText(portusetype);
        bodyElement.addContent(portUseTypeElement);

        Element portAccessModeElement = new Element("PORTACCESSMODE");
        portAccessModeElement.setText(accessmode);
        bodyElement.addContent(portAccessModeElement);

        return doc;
    }

    /**
     * @Name     releaseAllportAccessMode
     * @since    2018. 4. 23.
     * @author   hhlee
     * @contents Release for the Machine/DoubleMainMachieName
     * @param eventinfo
     * @param machinename
     * @param doublemainmachinename
     * @throws CustomException
     */
    private void releaseAllportAccessMode(EventInfo eventinfo, String machinename, String doublemainmachinename, Document originaldoc)  throws CustomException
    {
        String machineName = StringUtil.EMPTY;
        String portName = StringUtil.EMPTY;
        Port portData = null;
        
        String strSql = "SELECT A.MACHINENAME, A.PORTNAME, A.DOUBLEMAINMACHINENAME " +
                        "  FROM PORT A " +
                        " WHERE 1=1 " +
                        "   AND A.MACHINENAME = :MACHINENAME " +
                        "   AND A.DOUBLEMAINMACHINENAME  = :DOUBLEMAINMACHINENAME ";
        
        Map<String, Object> bindMap = new HashMap<String, Object>();
        bindMap.put("MACHINENAME", machinename);
        bindMap.put("DOUBLEMAINMACHINENAME", doublemainmachinename);

        List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);

        if(sqlResult.size () > 0)
        {
             machineName = StringUtil.EMPTY;
             portName = StringUtil.EMPTY;
            
            for(int i = 0; i < sqlResult.size(); i++)
            {
                //Release for the Machine/DoubleMainMachieName.
                
                machineName = sqlResult.get(i).get("MACHINENAME").toString();
                portName = sqlResult.get(i).get("PORTNAME").toString();

                portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
                
                MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
                
                eventinfo.setEventName("ChangeAccessMode");

                boolean result = false;
                
                if(!StringUtil.equals(portData.getAccessMode(), GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO))
                {
                    porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);
                    result = true;
                }
                   

                Map<String, String> portUdfs = portData.getUdfs();
                portUdfs.put("DOUBLEMAINMACHINENAME", StringUtil.EMPTY);
                portData.setUdfs(portUdfs);

                porttransitionInfo.setUdfs(portUdfs);
                
                //to aviod repetition of AccessMode
                if(result == true)
                {
                    MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, porttransitionInfo, eventinfo);
                }
                else
                {
                    SetEventInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(portUdfs);
                    MESPortServiceProxy.getPortServiceImpl().setEvent(portData, transitionInfo, eventinfo);
                }
                
                try
                {
                    /* Make Port Access Mode Change Message */
                    originaldoc = generatePortAccessModeChangeTemplate(originaldoc, machinename, portName, portData.getUdfs().get("PORTTYPE"),
                                                                          portData.getUdfs().get("PORTUSETYPE"), GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);
                    GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), originaldoc, "FMCSender");
                }
                catch(Exception ex)
                {
                    eventLog.warn("FMC Report Failed!");
                }
            }
        }
        else
        {
        }       
    }   
        
    /**
     * 
     * @Name     searchPortList
     * @since    2018. 7. 23.
     * @author   hhlee
     * @contents Search Port List
     *           
     * @param machineName
     * @param accessMode
     * @return
     * @throws CustomException
     */
    private List<Map<String, Object>> searchPortList(String machineName, String accessMode) throws CustomException
    {
        List<Map<String, Object>> result = null;
        try
        {
            //AND DOUBLEMAINMACHINENAME IS NULL
            //List<Port> result = PortServiceProxy.getPortService().select(" MACHINENAME = ? AND ACCESSMODE = ? ", new Object[] {machineName, accessMode});    
            
            String strSql = "SELECT A.*                              \n"
                          + "  FROM PORT A                           \n"
                          + " WHERE 1=1                              \n"
                          + "   AND A.MACHINENAME = :MACHINENAME     \n"
                          + "   AND A.ACCESSMODE  = :ACCESSMODE      \n"
                          + "   AND A.DOUBLEMAINMACHINENAME IS NULL  \n";
            
            Map<String, Object> bindMap = new HashMap<String, Object>();
            bindMap.put("MACHINENAME", machineName);
            bindMap.put("ACCESSMODE", accessMode);
            
            result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
                    
        }
        catch (Exception ex)
        {
          //throw new CustomException();
        }
        
        return result;
    }
    
    /**
     * 
     * @Name     searchPortListByUnitName
     * @since    2018. 9. 27.
     * @author   hhlee
     * @contents 
     *           
     * @param eventinfo
     * @param machineName
     * @param unitName
     * @param accessMode
     * @param portList
     * @throws CustomException
     */
    private void searchPortListByUnitName(EventInfo eventInfo, String machineName, String unitName, String accessMode, List<Element> portList) throws CustomException
    {
        //List<Map<String, Object>> result = null;
        try
        {
            boolean isManualPortExist = false;
            
            //AND DOUBLEMAINMACHINENAME IS NULL
            List<Port> portResult = PortServiceProxy.getPortService().select(" WHERE MACHINENAME = ? AND ACCESSMODE = ? AND DOUBLEMAINMACHINENAME = ? ", new Object[] {machineName, accessMode, unitName});    
            
            if(portResult != null && portResult.size() > 0)    
            {
                for(Port portData : portResult)
                {
                    isManualPortExist = false;
                    
                    for(Element elementPort : portList)
                    {
                        String elePortName = SMessageUtil.getChildText(elementPort, "PORTNAME", false);
                        
                        if(StringUtil.equals(portData.getKey().getPortName(), elePortName))
                        {
                            isManualPortExist = true;
                            break;
                        }
                    }
                    
                    if(!isManualPortExist)
                    {
                        MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
                        
                        eventInfo.setEventName("ChangeAccessMode");

                        porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);
                        
                        Map<String, String> portUdfs = portData.getUdfs();
                        portUdfs.put("DOUBLEMAINMACHINENAME", StringUtil.EMPTY);
                        portData.setUdfs(portUdfs);
                        porttransitionInfo.setUdfs(portUdfs);
                        
                        MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, porttransitionInfo, eventInfo);   
                        
                        eventInfo.setCheckTimekeyValidation(false);
                        eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
                    }                    
                }
            }            
        }
        catch (Exception ex)
        {
            log.error(ex.getMessage());
        }        
    }
    /**
     * 
     * @Name     searchPortAccessModeList
     * @since    2018. 7. 23.
     * @author   hhlee
     * @contents Search Port Access Mode List
     *           
     * @param machineName
     * @param accessMode
     * @return
     * @throws CustomException
     */
    private List<Map<String, Object>> searchPortAccessModeList(String machineName, String accessMode) throws CustomException
    {
        List<Map<String, Object>> result = null;
        try
        {
            //AND DOUBLEMAINMACHINENAME IS NULL
            //List<Port> result = PortServiceProxy.getPortService().select(" MACHINENAME = ? AND ACCESSMODE = ? ", new Object[] {machineName, accessMode});    
            
            String strSql = "SELECT A.*                              \n"
                          + "  FROM PORT A                           \n"
                          + " WHERE 1=1                              \n"
                          + "   AND A.MACHINENAME = :MACHINENAME     \n"
                          + "   AND A.ACCESSMODE  = :ACCESSMODE      \n";                          
            
            Map<String, Object> bindMap = new HashMap<String, Object>();
            bindMap.put("MACHINENAME", machineName);
            bindMap.put("ACCESSMODE", accessMode);
            
            result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
                    
        }
        catch (Exception ex)
        {
          //throw new CustomException();
        }
        
        return result;
    }
    
    /**
     * 
     * @Name     checkReleaseDoubleMainMachinePortAccessMode
     * @since    2018. 7. 23.
     * @author   hhlee
     * @contents Release DoubleMainMachine PortAccessMode
     *           
     * @param eventinfo
     * @param machinename
     * @param doublemainmachinename
     * @param portNameList
     * @param accessMode
     * @param isPortAccessModeManual
     * @param originaldoc
     * @throws CustomException
     */
    private void checkReleaseDoubleMainMachinePortAccessMode(EventInfo eventinfo, String machinename, 
            String doublemainmachinename, List<String> portNameList, String accessMode, boolean isPortAccessModeManual, Document originaldoc)  throws CustomException
    {
        Port portData = null;
        
        String strSql = "SELECT A.MACHINENAME, A.PORTNAME, A.DOUBLEMAINMACHINENAME \n" +
                      "    FROM PORT A                                             \n" +
                      "   WHERE 1 = 1                                              \n" +
                      "     AND A.MACHINENAME = :MACHINENAME                       \n" +
                      "     AND A.DOUBLEMAINMACHINENAME = :DOUBLEMAINMACHINENAME   \n" ;

        Map<String, Object> bindMap = new HashMap<String, Object>();        
        bindMap.put("MACHINENAME", machinename);      
        bindMap.put("DOUBLEMAINMACHINENAME", doublemainmachinename);

        List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);

        /* If there is a list of ports, change from Manual to Auto. 
         * When changing a port from manual to auto Check the overall port status and change the status. 
         */ 
        if(sqlResult.size () > 0)
        {   
            String resultMachineName = StringUtil.EMPTY;
            String resultPortName = StringUtil.EMPTY;
            boolean isDoubleMainMachineNullUpdate = false;
            
            if(portNameList == null || portNameList.size() <= 0)
            {
                for(int i = 0; i < sqlResult.size(); i++)
                {
                    //Release for the Machine/DoubleMainMachieName. 
                    resultMachineName = sqlResult.get(i).get("MACHINENAME").toString();
                    resultPortName =  sqlResult.get(i).get("PORTNAME").toString();
                    
                    portData = MESPortServiceProxy.getPortInfoUtil().getPortData(resultMachineName, resultPortName);    
                    
                    Map<String, String> portUdfs = portData.getUdfs();
                    MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
                    
                    portUdfs.put("DOUBLEMAINMACHINENAME", StringUtil.EMPTY);
                    porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);
                    
                    if(!StringUtils.equals(portData.getAccessMode(), porttransitionInfo.getAccessMode()))
                    {
                        MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, porttransitionInfo, eventinfo);
                    }
                    else
                    {
                        SetEventInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(portUdfs);
                        MESPortServiceProxy.getPortServiceImpl().setEvent(portData, transitionInfo, eventinfo);
                    }  
                }
            }
            else
            {
                if(portNameList != null && portNameList.size() > 0)
                {
                    for(int i = 0; i < sqlResult.size(); i++)
                    {
                        //Release for the Machine/DoubleMainMachieName. 
                        resultMachineName = sqlResult.get(i).get("MACHINENAME").toString();
                        resultPortName =  sqlResult.get(i).get("PORTNAME").toString();
                        
                        portData = MESPortServiceProxy.getPortInfoUtil().getPortData(resultMachineName, resultPortName);    
                        
                        Map<String, String> portUdfs = portData.getUdfs();
                        MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
                        
                        
                        isDoubleMainMachineNullUpdate = true;
                        for(String portname : portNameList)
                        {
                            if (StringUtil.equals(resultPortName,portname))
                            {
                                isDoubleMainMachineNullUpdate = false;
                                break;
                            }
                        }
                        
                        if(isDoubleMainMachineNullUpdate)
                        {   
                            portUdfs.put("DOUBLEMAINMACHINENAME", StringUtil.EMPTY);
                            if(!isPortAccessModeManual)
                            {                       
                                porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);  
                            }
                            else
                            {
                                porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, portData.getAccessMode());
                            }                                  
                        }
                        else
                        {
                            porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, portData.getAccessMode());
                        }
                        
                        portData.setUdfs(portUdfs);
                        porttransitionInfo.setUdfs(portUdfs);
                        
                        if(!StringUtils.equals(portData.getAccessMode(), porttransitionInfo.getAccessMode()))
                        {
                            MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, porttransitionInfo, eventinfo);
                        }
                        else
                        {
                            SetEventInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(portUdfs);
                            MESPortServiceProxy.getPortServiceImpl().setEvent(portData, transitionInfo, eventinfo);
                        }                        
                    }
                }
            }
//                        
//            for(int i = 0; i < sqlResult.size(); i++)
//            {
//                //Release for the Machine/DoubleMainMachieName. 
//                resultMachineName = sqlResult.get(i).get("MACHINENAME").toString();
//                resultPortName =  sqlResult.get(i).get("PORTNAME").toString();
//               
//                isDoubleMainMachineNullUpdatebyNullPortList = false;
//                isDoubleMainMachineNullUpdate = true;
//                if(portNameList != null && portNameList.size() > 0)
//                {     
//                    isDoubleMainMachineNullUpdatebyNullPortList = true;
//                    for(String portname : portNameList)
//                    {
//                        if (StringUtil.equals(resultPortName,portname))
//                        {
//                            isDoubleMainMachineNullUpdate = false;
//                            break;
//                        }
//                    }
//                }
//                else
//                {                    
//                }
//                
//                MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
//                
//                eventinfo.setEventName("ChangeAccessMode");
//                portData = MESPortServiceProxy.getPortInfoUtil().getPortData(resultMachineName, resultPortName);                 
//                
//                Map<String, String> portUdfs = portData.getUdfs();
//                
//                if(isDoubleMainMachineNullUpdatebyNullPortList)
//                {
//                    portUdfs.put("DOUBLEMAINMACHINENAME", StringUtil.EMPTY);
//                    porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);
//                }
//                else if(isDoubleMainMachineNullUpdate)
//                {   
//                    portUdfs.put("DOUBLEMAINMACHINENAME", StringUtil.EMPTY);
//                    if(!isOnlineRemoteUpdate)
//                    {                       
//                        porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);  
//                    }
//                    else
//                    {
//                        porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, portData.getAccessMode());
//                    }
//                        
//                }
//                else
//                {
//                    portUdfs.put("DOUBLEMAINMACHINENAME", portData.getUdfs().get("DOUBLEMAINMACHINENAME"));
//                    //porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
//                    porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, portData.getAccessMode());
//                }
                                
            
            //success then report to FMC
            try
            {
                /* Make Port Access Mode Change Message */
                originaldoc = generatePortAccessModeChangeTemplate(originaldoc, resultMachineName, resultPortName, portData.getUdfs().get("PORTTYPE"),
                                                                      portData.getUdfs().get("PORTUSETYPE"), GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);
                GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), originaldoc, "FMCSender");
            }
            catch(Exception ex)
            {
                eventLog.warn("FMC Report Failed!");
            }
        }       
        
    }
    
    /**
     * 
     * @Name     searchMachinePortList
     * @since    2018. 7. 23.
     * @author   hhlee
     * @contents Search Machine PortList
     *           
     * @param machineName
     * @return
     * @throws CustomException
     */
    private List<Map<String, Object>> searchMachinePortList(String machineName) throws CustomException
    {
//        String condition = " WHERE machineName = ? ";
//        Object[] bindSet = new Object[] {machineName};
//        
//        List<Port> portList;
//        try
//        {
//            portList = PortServiceProxy.getPortService().select(condition, bindSet);
//            return portList;
//        }
//        catch(Exception ex)
//        {
//            return portList = new ArrayList<Port>();
//        }
        
       String strSql = "SELECT A.MACHINENAME, A.PORTNAME, A.DOUBLEMAINMACHINENAME \n" +
                       "  FROM PORT A                                             \n" +
                       " WHERE 1 = 1                                              \n" +
                       "   AND A.MACHINENAME = :MACHINENAME                       \n";               

        Map<String, Object> bindMap = new HashMap<String, Object>();        
        bindMap.put("MACHINENAME", machineName);
    
        List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
        
        return sqlResult;

    }
    
//    /**
//     * 
//     * @Name     checkReleaseDoubleMainMachinePortAccessMode
//     * @since    2018. 9. 28.
//     * @author   hhlee
//     * @contents 
//     *           
//     * @param eventInfo
//     * @param machineName
//     * @throws CustomException
//     */
//    private void checkReleaseDoubleMainMachinePortAccessMode(EventInfo eventInfo, String machineName) throws CustomException
//    {
//        List<Map<String, Object>> dubleMachineNameList = this.searchDubleMachineNamePortAccessModeList(machineName, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
//        
//        List<Map<String, Object>> prounitList = getMachineDataByCommunicationState(machineName, "2", StringUtil.EMPTY, GenericServiceProxy.getConstantMap().Mac_ProductionMachine);
//        
//        if(dubleMachineNameList != null && prounitList != null)
//        {
//            if(dubleMachineNameList.size() == prounitList.size())
//            {
//                try
//                {
//                    List<Port> portList = PortServiceProxy.getPortService().select(" MACHINENAME = ? AND ACCESSMODE = ? ", new Object[] {machineName, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO});  
//                
//                    for(Port portData : portList)
//                    {
//                        eventInfo.setCheckTimekeyValidation(false);
//                        eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
//                        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//                        
//                        MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
//                        porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
//                        MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, porttransitionInfo, eventInfo);
//                    }
//                }
//                catch(Exception ex)
//                {
//                    eventLog.warn(ex.getStackTrace());
//                }                
//            }
//        }    
//    }
    
    
    
    private List<Map<String, Object>> searchDubleMachineNamePortAccessModeList(String machineName, String accessMode) throws CustomException
    {
        //List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
        List<Map<String, Object>> result = null;
        try
        {
            String strSql = "SELECT A.DOUBLEMAINMACHINENAME          \n"
                          + "  FROM PORT A                           \n"
                          + " WHERE 1=1                              \n"
                          + "   AND A.MACHINENAME = :MACHINENAME     \n"
                          + "   AND A.ACCESSMODE  = :ACCESSMODE      \n"
                          + "   AND A.DOUBLEMAINMACHINENAME IS NOT NULL \n"        
                          + "  GROUP BY A.DOUBLEMAINMACHINENAME      \n";
            
            Map<String, Object> bindMap = new HashMap<String, Object>();
            bindMap.put("MACHINENAME", machineName);
            bindMap.put("ACCESSMODE", accessMode);
           
            result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
                    
        }
        catch (Exception ex)
        {
            //throw new CustomException();
            eventLog.warn(ex.getStackTrace());
        }
        
        return result;
    }    
    
    private List<Map<String, Object>> getPortLinkedUnitData(String machineName, String unitName) throws CustomException
    {
        List<Map<String, Object>> result = null;
        try
        {
            String strSql = "SELECT A.LINKEDUNITNAME                   \n"
                          + "  FROM PORT A                             \n"
                          + " WHERE 1=1                                \n"
                          + "   AND A.MACHINENAME = :MACHINENAME       \n"
                          + "   AND A.LINKEDUNITNAME = :LINKEDUNITNAME \n"
                          + "  GROUP BY A.LINKEDUNITNAME                 ";                        
            
            Map<String, Object> bindMap = new HashMap<String, Object>();
            bindMap.put("MACHINENAME", machineName);
            bindMap.put("LINKEDUNITNAME", unitName);
            
            result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
                    
        }
        catch (Exception ex)
        {
            //throw new CustomException();
            eventLog.warn(ex.getStackTrace());
        }
        
        return result;
    }
    
    
    
    /**
     * 
     * @Name     validateLinkedUnitPortandReceivedPortList
     * @since    2019. 1. 4.
     * @author   hhlee
     * @contents 
     *           
     * @param machineName
     * @param unitName
     * @param portDataList
     * @param elementPortList
     * @return
     * @throws CustomException
     */
    private boolean validateLinkedUnitPortandReceivedPortList(String machineName, String unitName, List<Port> portDataList, List<Element> elementPortList) throws CustomException
    {
        String unitPortName = StringUtil.EMPTY;
        String wrongPortName = StringUtil.EMPTY;
        
        String linkedUnitPortName  = StringUtil.EMPTY;
        String operationMode  = StringUtil.EMPTY;
        boolean isExist = false;
        boolean isFirst = true;
        if(elementPortList != null && elementPortList.size() > 0)
        {
            String portName = StringUtil.EMPTY;
            for(Element elementPort : elementPortList)
            {
                portName = SMessageUtil.getChildText(elementPort, "PORTNAME", false);
                isExist = false;
                if (portDataList != null && portDataList.size() > 0)
                {
                    for(Port linkedUnitPortData : portDataList)
                    {
                        linkedUnitPortName = linkedUnitPortData.getKey().getPortName();
                        
                        if(StringUtil.equals(linkedUnitPortName, portName))
                        {
                            isExist = true;
                            break;
                        } 
                        
                        if(isFirst)
                        {
                            if(StringUtil.isEmpty(unitPortName))
                            {
                                unitPortName = linkedUnitPortName;
                            }
                            else
                            {
                                unitPortName += "," + linkedUnitPortName;
                            }
                        }
                    }                
                    isFirst = false;  
                    if(!isExist)
                    {                
                        if(StringUtil.isEmpty(wrongPortName))
                        {
                            wrongPortName = portName;
                        }
                        else
                        {
                            wrongPortName += "," + portName;
                        }                
                    }  
                }                          
            }            
        }
        
        if(StringUtil.isNotEmpty(wrongPortName))
        {
            log.warn(String.format("LinkedUnit Port[LinkedUnitPort:%s, DifferentPort:%s] set for this Unit[Machine:%s, UnitName:%s] is different.", 
                    unitPortName, wrongPortName, machineName, unitName));
            throw new CustomException("PORT-9008",unitPortName, wrongPortName, machineName, unitName);
        }   
        
        return true;
    } 
    
    /* ======================================================================================================================================================== */
    /* ======================================================================================================================================================== */
    /* ======================================================================================================================================================== */
    
//    private void unitCommunicationStateChangeByNormal(EventInfo eventInfo, String machineName, String unitName, List<Element> portList) throws CustomException
//    {
//        //Machine mainMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
//        //Machine unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
//        
//        /* 1. clear DoubleManinMachine */
//        this.clearDoubleMainMachine(eventInfo, machineName, unitName, portList);
//        
//        /* 2. set DoubleMainMachine and PoetAccessMOde */
//        this.setDoubleMainMachineAccessModeNormalIndp(eventInfo, machineName, unitName, portList);
//        
//        /* 3. Machine - Unit : check All Production Unit OffLine */
//        this.updateAccessModeByValidateDoubleMainMachineNormal(eventInfo, machineName);        
//    }
    
//    /**
//     * 
//     * @Name     unitCommunicationStateChangeByIndp
//     * @since    2019. 1. 4.
//     * @author   hhlee
//     * @contents 
//     *           
//     * @param eventInfo
//     * @param machineName
//     * @param unitName
//     * @param machinePortList
//     * @param portList
//     * @throws CustomException
//     */
//    private void unitCommunicationStateChangeByIndp(EventInfo eventInfo, String machineName, String unitName, List<Element> portList) throws CustomException
//    {        
//        //Machine mainMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
//        //Machine unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
//        
//        /* 1. clear DoubleManinMachine */
//        this.clearDoubleMainMachine(eventInfo, machineName, unitName, portList);
//        
//        /* 2. set DoubleMainMachine and PoetAccessMOde */
//        this.setDoubleMainMachineAccessModeNormalIndp(eventInfo, machineName, unitName, portList);
//        
//        /* 3. Machine - Unit : check All Production Unit OffLine */
//        this.updateAccessModeByValidateDoubleMainMachineIndp(eventInfo, machineName, unitName);  
//        
//        
//    }    
    
//    private void updateAccessModeByValidateDoubleMainMachineNormal(EventInfo eventInfo, String machineName) throws CustomException
//    {
//        List<Map<String, Object>> dubleMachineNameList = this.dubleMachineNamePortAccessModeListNormal(machineName, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
//        
//        List<Map<String, Object>> prounitList = getMachineDataByCommunicationState(machineName, "2", StringUtil.EMPTY, GenericServiceProxy.getConstantMap().Mac_ProductionMachine);
//        
//        if(dubleMachineNameList != null && prounitList != null)
//        {
//            if(dubleMachineNameList.size() == prounitList.size())
//            {
//                try
//                {
//                    List<Port> portList = PortServiceProxy.getPortService().select(" MACHINENAME = ? AND ACCESSMODE = ? ", new Object[] {machineName, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO});  
//                
//                    for(Port portData : portList)
//                    {
//                        eventInfo.setCheckTimekeyValidation(false);
//                        //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
//                        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//                        
//                        MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
//                        porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
//                        MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, porttransitionInfo, eventInfo);
//                    }
//                }
//                catch(Exception ex)
//                {
//                    eventLog.warn(ex.getStackTrace());
//                }                
//            }
//        }  
//    }
    
//    private void updateAccessModeByValidateDoubleMainMachineIndp(EventInfo eventInfo, String machineName, String unitName) throws CustomException
//    {
//        List<Map<String, Object>> linkedUnitNameList = this.dubleMachineNamePortAccessModeListIndp(machineName, unitName, 
//                GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
//        
//        if(linkedUnitNameList != null && linkedUnitNameList.size() > 0)
//        {            
//            try
//            {
//                String linkedUnitName = StringUtil.EMPTY;
//                
//                for(int i=0; i < linkedUnitNameList.size(); i++)
//                {      
//                    linkedUnitName = linkedUnitNameList.get(i).get("LINKEDUNITNAME").toString();
//                    
//                    List<Port> portList = PortServiceProxy.getPortService().select(" MACHINENAME = ? AND LINKEDUNITNAME = ? AND ACCESSMODE = ? ", 
//                            new Object[] {machineName, linkedUnitName, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO});  
//                
//                    for(Port portData : portList)
//                    {
//                        eventInfo.setCheckTimekeyValidation(false);
//                        //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
//                        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//                        
//                        MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
//                        porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
//                        MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, porttransitionInfo, eventInfo);
//                    }
//                }
//            }
//            catch(Exception ex)
//            {
//                eventLog.warn(ex.getStackTrace());
//            } 
//        }  
//    }
    
    
//    private List<Map<String, Object>> dubleMachineNamePortAccessModeListNormal(String machineName, String accessMode) throws CustomException
//    {
//        //List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
//        List<Map<String, Object>> result = null;
//        try
//        {
//            String strSql = "SELECT A.DOUBLEMAINMACHINENAME          \n"
//                          + "  FROM PORT A                           \n"
//                          + " WHERE 1=1                              \n"
//                          + "   AND A.MACHINENAME = :MACHINENAME     \n"
//                          + "   AND A.ACCESSMODE  = :ACCESSMODE      \n"
//                          + "   AND A.DOUBLEMAINMACHINENAME IS NOT NULL \n"        
//                          + "  GROUP BY A.DOUBLEMAINMACHINENAME      \n";
//            
//            Map<String, Object> bindMap = new HashMap<String, Object>();
//            bindMap.put("MACHINENAME", machineName);
//            bindMap.put("ACCESSMODE", accessMode);
//           
//            result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
//                    
//        }
//        catch (Exception ex)
//        {
//            //throw new CustomException();
//            eventLog.warn(ex.getStackTrace());
//        }
//        
//        return result;
//    } 
    
    private List<Map<String, Object>> dubleMachineNamePortAccessModeListIndp(String machineName, String unitName, String accessMode) throws CustomException
    {
        //List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
        List<Map<String, Object>> result = null;
        try
        {
            String strSql = "SELECT A.LINKEDUNITNAME          \n"
                          + "  FROM PORT A                           \n"
                          + " WHERE 1=1                              \n"
                          + "   AND A.MACHINENAME = :MACHINENAME     \n"
                          + "   AND A.ACCESSMODE  = :ACCESSMODE      \n"
                          + "   AND A.DOUBLEMAINMACHINENAME = : DOUBLEMAINMACHINENAME \n"        
                          + "  GROUP BY A.LINKEDUNITNAME      \n";
            
            Map<String, Object> bindMap = new HashMap<String, Object>();
            bindMap.put("MACHINENAME", machineName);
            bindMap.put("ACCESSMODE", accessMode);
            bindMap.put("DOUBLEMAINMACHINENAME", unitName);
           
            result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
                    
        }
        catch (Exception ex)
        {
            //throw new CustomException();
            eventLog.warn(ex.getStackTrace());
        }
        
        return result;
    }
    
//    /**
//     * 
//     * @Name     clearDoubleMainMachine
//     * @since    2019. 1. 5.
//     * @author   hhlee
//     * @contents 
//     *           
//     * @param machineName
//     * @param unitName
//     */
//    private void clearDoubleMainMachine(EventInfo eventInfo, String machineName, String unitName, List<Element> elementPortList) throws CustomException
//    {
//        String portName = StringUtil.EMPTY;
//        String elementPortName = StringUtil.EMPTY;
//        try
//        {
//            List<Port> portDoubleMainMachineDataList = PortServiceProxy.getPortService().select(" WHERE MACHINENAME = ? AND DOUBLEMAINMACHINENAME = ? ORDER BY TO_NUMBER(PORTNAME) ", 
//                    new Object[] {machineName, unitName});  
//            
//            if(portDoubleMainMachineDataList != null && portDoubleMainMachineDataList.size() > 0)
//            {
//                Port portData = null;
//                for(Port portDoubleMainMachineData : portDoubleMainMachineDataList)
//                {        
//                    portName = portDoubleMainMachineData.getKey().getPortName();
//                    
//                    for(Element elementPort : elementPortList)
//                    {
//                        elementPortName = SMessageUtil.getChildText(elementPort, "PORTNAME", false);
//                        if(StringUtil.equals(portName, elementPortName))
//                        {
//                            break;
//                        }
//                        portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
//                        
//                        if(StringUtil.isNotEmpty(CommonUtil.getValue(portData.getUdfs(), "DOUBLEMAINMACHINENAME")))
//                        {
//                            Map<String, String> portUdfs = portData.getUdfs();
//                            portUdfs.put("DOUBLEMAINMACHINENAME", StringUtil.EMPTY);
//                            portData.setUdfs(portUdfs);
//                            
//                            PortServiceProxy.getPortService().update(portData);    
//                        }
//                    }
//                }                
//                //Port portData = null;
//                //for(Port portDoubleMainMachineData : portDoubleMainMachineDataList)
//                //{        
//                //    portName = portDoubleMainMachineData.getKey().getPortName();
//                //    
//                //    for(Element elementPort : elementPortList)
//                //    {
//                //        elementPortName = SMessageUtil.getChildText(elementPort, "PORTNAME", false);
//                //        if(StringUtil.equals(portName, elementPortName))
//                //        {
//                //            break;
//                //        }
//                //        portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
//                //        
//                //        if(StringUtil.isNotEmpty(CommonUtil.getValue(portData.getUdfs(), "DOUBLEMAINMACHINENAME")))
//                //        {
//                //            Map<String, String> portUdfs = portData.getUdfs();
//                //            portUdfs.put("DOUBLEMAINMACHINENAME", StringUtil.EMPTY);
//                //            portData.setUdfs(portUdfs);
//                //            
//                //            PortServiceProxy.getPortService().update(portData);    
//                //        }
//                //    }
//                //}
//            }
//        }
//        catch (Exception ex)
//        {
//            eventLog.warn(String.format("DoubleMainMachine is not found.!![MachineName:%s, DoubleMainMachineName:%s]", machineName, unitName));
//        }
//        
//    }
    
//    private void setDoubleMainMachineAccessModeNormalIndp(EventInfo eventInfo, String machineName, String unitName, List<Element> elementPortList) throws CustomException
//    {
//        String portName = StringUtil.EMPTY;
//        boolean isAccessModeDuplicate = false;
//        for(Element elementPort : elementPortList)
//        {
//            try
//            {
//                portName = SMessageUtil.getChildText(elementPort, "PORTNAME", false);
//                Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
//                
//                MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
//                
//                eventInfo.setEventName("ChangeAccessMode");
//                
//                porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
//                
//                Map<String, String> portUdfs = portData.getUdfs();
//                portUdfs.put("DOUBLEMAINMACHINENAME", unitName);
//                portData.setUdfs(portUdfs);
//                porttransitionInfo.setUdfs(portUdfs);
//                
//                /* Check Port AccessMode Duplicate */
//                isAccessModeDuplicate = false;
//                if(!StringUtil.equals(portData.getAccessMode(), 
//                        GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL))
//                {                    
//                    isAccessModeDuplicate = true;
//                }
//                
//                /* To aviod repetition of AccessMode */
//                if(isAccessModeDuplicate == true)
//                {
//                    MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, porttransitionInfo, eventInfo); 
//                }
//                else
//                {
//                    SetEventInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(portUdfs);
//                    MESPortServiceProxy.getPortServiceImpl().setEvent(portData, transitionInfo, eventInfo);
//                }
//            }
//            catch (Exception ex)
//            {
//                eventLog.warn(String.format("Set DoubleMainMachine AccessMode Normal update fail.!![MachineName:%s, UnitName:%s]", machineName, unitName));
//            }
//        }
//    }
    
//    /**
//     * 
//     * @Name     getMachineDataByCommunicationState
//     * @since    2018. 7. 23.
//     * @author   hhlee
//     * @contents Get Machine Data By CommunicationState
//     *           
//     * @param machineName
//     * @param machineLevel
//     * @param communicationState
//     * @param machineType
//     * @return
//     * @throws CustomException
//     */
//    private List<Map<String, Object>> getMachineDataByCommunicationState(String machineName, String machineLevel, 
//            String communicationState, String machineType) throws CustomException
//    {
//        List<Map<String, Object>> machineDataByCommunicationState = null;
//        String strSql = StringUtil.EMPTY;
//        try
//        {
//            strSql = " SELECT SQ.LV                                                                         \n" 
//                    + "       ,SQ.COMBINEMACHINE                                                             \n"
//                    + "       ,REGEXP_SUBSTR(SQ.COMBINEMACHINE, '[^-]+', 1, 1) AS MACHINENAME                \n"
//                    + "       ,REGEXP_SUBSTR(SQ.COMBINEMACHINE, '[^-]+', 1, 2) AS UNITNAME                   \n"
//                    + "       ,REGEXP_SUBSTR(SQ.COMBINEMACHINE, '[^-]+', 1, 3) AS SUBUNITNAME                \n"
//                    + "       ,SQ.FACTORYNAME,SQ.AREANAME,SQ.SUPERMACHINENAME                                \n"
//                    + "       ,SQ.MACHINEGROUPNAME,SQ.PROCESSCOUNT,SQ.RESOURCESTATE                          \n"
//                    + "       ,SQ.E10STATE,SQ.COMMUNICATIONSTATE,SQ.MACHINESTATENAME                         \n"
//                    + "       ,SQ.LASTEVENTNAME,SQ.LASTEVENTTIMEKEY,SQ.LASTEVENTTIME                         \n"
//                    + "       ,SQ.LASTEVENTUSER,SQ.LASTEVENTCOMMENT,SQ.LASTEVENTFLAG                         \n"
//                    + "       ,SQ.REASONCODETYPE,SQ.REASONCODE,SQ.MCSUBJECTNAME                              \n"
//                    + "       ,SQ.OPERATIONMODE,SQ.DSPFLAG,SQ.FULLSTATE                                      \n"
//                    + "       ,SQ.ONLINEINITIALCOMMSTATE                                                     \n"
//                    + "   FROM (                                                                             \n"
//                    + "         SELECT LEVEL LV                                                              \n"
//                    + "               ,SUBSTR(SYS_CONNECT_BY_PATH(M.MACHINENAME, '-'), 2) AS COMBINEMACHINE  \n"
//                    + "               ,M.*                                                                   \n"
//                    + "               FROM MACHINE M                                                         \n"
//                    + "         START WITH M.SUPERMACHINENAME IS NULL                                        \n"
//                    + "                AND M.MACHINENAME = :MACHINENAME                                      \n"
//                    + "   CONNECT BY PRIOR M.MACHINENAME = M.SUPERMACHINENAME                                \n"
//                    + "         ORDER BY M.MACHINENAME                                                       \n"
//                    + "         ) SQ,                                                                        \n"
//                    + "  MACHINESPEC MS                                                                      \n"
//                    + " WHERE 1=1                                                                            \n"
//                    + " AND SQ.MACHINENAME = MS.MACHINENAME                                                  \n";
//            
//            Map<String, Object> bindMap = new HashMap<String, Object>(); 
//            bindMap.put("MACHINENAME", machineName);
//            
//            if(StringUtil.isNotEmpty(machineLevel))
//            {
//                strSql = strSql  + " AND SQ.LV = :MACHINELEVEL                                              \n";
//                bindMap.put("MACHINELEVEL", machineLevel);
//            }
//            if(StringUtil.isNotEmpty(machineType))
//            {
//                strSql = strSql  + " AND MS.MACHINETYPE = :MACHINETYPE                                      \n";
//                bindMap.put("MACHINETYPE", machineType);
//            }
//            if(StringUtil.isNotEmpty(communicationState))
//            {
//                strSql = strSql  + " AND NVL(SQ.COMMUNICATIONSTATE, 'OffLine') = :COMMUNICATIONSTATE        \n";
//                bindMap.put("COMMUNICATIONSTATE", communicationState);
//            }
//            strSql = strSql  + " ORDER BY SQ.LV, SQ.COMBINEMACHINE                                                    \n";
//                    
//            machineDataByCommunicationState = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
//        }
//        catch (Exception ex)
//        {
//            //throw new CustomException();
//        }
//        
//        return machineDataByCommunicationState;
//    }
}

/* ##################################################################################################### */
/* #########################################[BACKUP : 20190104]######################################### */
/* ##################################################################################################### */

//package kr.co.aim.messolution.machine.event;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import kr.co.aim.messolution.durable.MESDurableServiceProxy;
//import kr.co.aim.messolution.generic.GenericServiceProxy;
//import kr.co.aim.messolution.generic.errorHandler.CustomException;
//import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
//import kr.co.aim.messolution.generic.util.CommonUtil;
//import kr.co.aim.messolution.generic.util.EventInfoUtil;
//import kr.co.aim.messolution.generic.util.SMessageUtil;
//import kr.co.aim.messolution.machine.MESMachineServiceProxy;
//import kr.co.aim.messolution.port.MESPortServiceProxy;
//import kr.co.aim.greenframe.greenFrameServiceProxy;
//import kr.co.aim.greenframe.util.time.TimeStampUtil;
//import kr.co.aim.greentrack.durable.DurableServiceProxy;
//import kr.co.aim.greentrack.generic.info.EventInfo;
//import kr.co.aim.greentrack.generic.util.StringUtil;
//import kr.co.aim.greentrack.machine.management.data.Machine;
//import kr.co.aim.greentrack.machine.management.data.MachineSpec;
//import kr.co.aim.greentrack.machine.management.info.MakeCommunicationStateInfo;
//import kr.co.aim.greentrack.port.PortServiceProxy;
//import kr.co.aim.greentrack.port.management.data.Port;
//import kr.co.aim.greentrack.port.management.info.MakeAccessModeInfo;
//import kr.co.aim.greentrack.port.management.info.SetEventInfo;
//
//import org.apache.commons.lang.StringUtils;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.jdom.Document;
//import org.jdom.Element;
//
//public class UnitCommunicationStateChanged  extends SyncHandler{
//	private static Log log = LogFactory.getLog(UnitCommunicationStateChanged.class);
//	@Override
//	public Object doWorks(Document doc) throws CustomException {
//
//        SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_UnitCommunicationStateCheckReply");
//
//		SMessageUtil.setBodyItemValue(doc, "RESULT", "OK",true);
//		SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "",true);
//
//		/* Copy to FMCSender. Add, hhlee, 20180421 */
//		Document originaldoc = (Document)doc.clone();
//
//		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
//
//		/* Validate Machine */
//		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
//		Machine mainMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
//
//		MachineSpec mainMachineSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);
//		
//		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
//		String unitCommunicationstate = SMessageUtil.getBodyItemValue(doc, "COMMUNICATIONSTATE", true);
//		
//		/* 20181112, hhlee, add, UnitData Location Change ==>> */
//		Machine unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
//		/* <<== 20181112, add, delete, UnitData Location Change */
//		
//		if(StringUtil.equals(StringUtil.upperCase(unitCommunicationstate), StringUtil.upperCase(GenericServiceProxy.getConstantMap().Mac_OnLine)))
//		{
//			unitCommunicationstate = GenericServiceProxy.getConstantMap().Mac_OnLineRemote;
//		}
//		else if(StringUtil.equals(StringUtil.upperCase(unitCommunicationstate), StringUtil.upperCase(GenericServiceProxy.getConstantMap().Mac_OnLineLocal)))
//		{
//			unitCommunicationstate = GenericServiceProxy.getConstantMap().Mac_OnLineLocal;
//		}
//		else if(StringUtil.equals(StringUtil.upperCase(unitCommunicationstate), StringUtil.upperCase(GenericServiceProxy.getConstantMap().Mac_OnLineRemote)))
//		{
//			unitCommunicationstate = GenericServiceProxy.getConstantMap().Mac_OnLineRemote;
//		}
//		/* 20181112, hhlee, add, Communicationstate Validation Check(OnLineRemote, OnLineLocal, OffLine) ==>> */
//		else if(StringUtil.equals(StringUtil.upperCase(unitCommunicationstate), StringUtil.upperCase(GenericServiceProxy.getConstantMap().Mac_OffLine)))
//		{
//			unitCommunicationstate = GenericServiceProxy.getConstantMap().Mac_OffLine;
//		}
//		else
//		{		    
//		    throw new CustomException("MACHINE-0008", unitData.getCommunicationState(), unitCommunicationstate);		    
//		}
//		/* <<== 20181112, hhlee, add, Communicationstate Validation Check(OnLineRemote, OnLineLocal, OffLine) */
//		
//		/* 20180717, Add, Unit Offline Check ==>> */
//		//changeUnitCommunicationState
//		eventInfo.setEventName("ChangeUnitCommState");
//		/* 20181112, hhlee, delete, UnitData Location Change ==>> */
//		//Machine unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);			
//		/* <<== 20181112, hhlee, delete, UnitData Location Change */
//		
//		/* Unit Communication Validation */
//		if(StringUtil.equals(unitData.getCommunicationState(), unitCommunicationstate))
//        {
//          //Only write log for OnlineInitial
//            CommonUtil.CustomExceptionLog("MACHINE-0001", unitData.getKey().getMachineName(),
//                    unitData.getCommunicationState(), unitCommunicationstate);       
//            //return doc;
//        }   
//				
//		/* Get Machine Port List */
//		List<Map<String, Object>> machinePortList = searchMachinePortList(machineName);
//		if(machinePortList == null || machinePortList.size() <= 0)
//		{
//		    //Only write log for OnlineInitial
//            CommonUtil.CustomExceptionLog("PORT-9001", machineName, StringUtil.EMPTY);
//            return doc;
//		}
//		
//		//MakeCommunicationStateInfo commtransitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeCommunicationStateInfo(unitData, unitCommunicationstate);
//		//MESMachineServiceProxy.getMachineServiceImpl().makeCommunicationState(unitData, commtransitionInfo, eventInfo);				
//		/* <<== 20180717, Add, Unit Offline Check */
//		
//		//Check Port part 
//		List<Element> portList = SMessageUtil.getSubSequenceItemList(SMessageUtil.getBodyElement(doc), "PORTLIST", false);
//		
//		/* 20190103, hhlee, add, check LINKEDUNIT of PORT Table 
//		 *           Compare portList and port of LINKEDUNIT ==>> */
//		/* 20190104, hhlee, add, add validation [if(portList != null && portList.size() > 0)] */
//		if(portList != null && portList.size() > 0)
//		{
//		    MESPortServiceProxy.getPortServiceUtil().checkLinkedUnitPortInfo(machineName, unitName, portList);
//		}
//		/* <<== 20190103, hhlee, add, check LINKEDUNIT of PORT Table */		
//		
//		/* UnitCommunicationStateChange OffLIne */
//		if(StringUtil.equals(GenericServiceProxy.getConstantMap().Mac_OffLine, unitCommunicationstate))
//		{
//		    /* 20180926, hhlee, Add, ProbeCard Unmount ==>> */
//		    if (StringUtil.equals(unitData.getCommunicationState(), GenericServiceProxy.getConstantMap().Mac_OffLine) && portList.size() > 0)
//            {
//		        this.searchPortListByUnitName(eventInfo, machineName, unitName, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL, portList);
//            }
//		    /* <<== 20180926, hhlee, Add, ProbeCard Unmount */
//		    
//		    /* 20181022, hhlee, delete, Logic is delete by MantisID 0001118  ==>> */
//		    ///* 20180926, hhlee, Add, ProbeCard Unmount ==>> */
//		    //if (CommonUtil.getValue(mainMachineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("ATST"))
//		    //{
//		    //    MESDurableServiceProxy.getDurableServiceUtil().probeCardUnMountByUnitOffLine(eventInfo, machineName, unitName);		  
//		    //}
//		    ///* <<== 20180926, hhlee, Add, ProbeCard Unmount */
//		    /* <<== 20181022, hhlee, delete, Logic is delete by MantisID 0001118 */
//		    
//		    /* Unit Communication State Change */
//		    //changeUnitCommunicationState
//	        eventInfo.setEventName("ChangeUnitCommState");
//	        unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);	        
//	        MakeCommunicationStateInfo commtransitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeCommunicationStateInfo(unitData, unitCommunicationstate);	    
//	        MESMachineServiceProxy.getMachineServiceImpl().makeCommunicationState(unitData, commtransitionInfo, eventInfo);
//	        
//	        /* UnitCommunicationStateChange Access Mode Check */
//	        List<Map<String, Object>> portDataManualList = searchPortList(machineName,GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
//	        if(portDataManualList != null && portDataManualList.size() > 0)
//	        {
//	            //Only write log for OnlineInitial
//	            CommonUtil.CustomExceptionLog("PORT-0008", machineName, portDataManualList.get(0).get("PORTNAME").toString(), 
//	                    portDataManualList.get(0).get("PORTTYPE").toString() , GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
//	            return doc;
//	        }
//	        
//			if(portList != null && portList.size() > 0)
//			{
//				for(Element elementPort : portList)
//				{
//					String portName = SMessageUtil.getChildText(elementPort, "PORTNAME", false);
//					
//					if(StringUtil.isNotEmpty(portName))
//					{
//    					Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
//    									
//    					MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
//    					
//    				    eventInfo.setEventName("ChangeAccessMode");
//    
//    				    boolean result = false;
//    				    
//    				    if(!StringUtil.equals(portData.getAccessMode(), GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL))
//    				    {
//    				    	porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
//    				    	result = true;
//    				    }
//    				        
//    				    Map<String, String> portUdfs = portData.getUdfs();
//    					portUdfs.put("DOUBLEMAINMACHINENAME", unitName);
//    					portData.setUdfs(portUdfs);
//    					porttransitionInfo.setUdfs(portUdfs);
//    					
//    					//to aviod repetition of AccessMode
//    					if(result == true)
//    						MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, porttransitionInfo, eventInfo);	
//    					else
//    					{
//    						SetEventInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(portUdfs);
//    						MESPortServiceProxy.getPortServiceImpl().setEvent(portData, transitionInfo, eventInfo);
//    					}
//    							
//    					//success then report to FMC
//    					try
//    					{
//    						/* Make Port Access Mode Change Message */
//    						originaldoc = generatePortAccessModeChangeTemplate(originaldoc, machineName, portName, portData.getUdfs().get("PORTTYPE"),
//    								                                              portData.getUdfs().get("PORTUSETYPE"), GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
//    						GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), originaldoc, "FMCSender");
//    					}
//    					catch(Exception ex)
//    					{
//    						eventLog.warn("FMC Report Failed!");
//    					}
//					}
//				}	
//			}
//						
//			/* 20180717, Add, Unit Offline Check ==>> */
//			//List<Map<String, Object>> unitDatabyHierarchy = MESMachineServiceProxy.getMachineServiceUtil().getMachineDataByHierarchy(machineName, "2", StringUtil.EMPTY, StringUtil.EMPTY, GenericServiceProxy.getConstantMap().Mac_ProductionMachine);
//			List<Map<String, Object>> unitDatabyCommunicationState = getMachineDataByCommunicationState(machineName, "2", 
//			        GenericServiceProxy.getConstantMap().Mac_OnLineRemote, GenericServiceProxy.getConstantMap().Mac_ProductionMachine);
//			if(unitDatabyCommunicationState == null || unitDatabyCommunicationState.size() <= 0)
//			{
//			    List<Map<String, Object>> portDataList = searchPortList(machineName,GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);
//			    String resultMachineName = StringUtil.EMPTY;
//	            String resultPortName = StringUtil.EMPTY;
//	            
//			    if(portDataList != null && portDataList.size() > 0)
//			    {
//			        MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
//			        //for (Port portData : portDataList)
//			        for(int i = 0; i < portDataList.size(); i++)
//		            {
//			            
//			            resultMachineName = portDataList.get(i).get("MACHINENAME").toString();
//			            resultPortName = portDataList.get(i).get("PORTNAME").toString();
//
//			            Port resultPortData = MESPortServiceProxy.getPortInfoUtil().getPortData(resultMachineName, resultPortName);
//		                
//			            porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(resultPortData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
//			            MESPortServiceProxy.getPortServiceImpl().makeAccessMode(resultPortData, porttransitionInfo, eventInfo); 
//			            
//			            /* Make Port Access Mode Change Message */
//                        originaldoc = generatePortAccessModeChangeTemplate(originaldoc, machineName, resultPortData.getKey().getPortName(), resultPortData.getUdfs().get("PORTTYPE"),
//                                resultPortData.getUdfs().get("PORTUSETYPE"), GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
//                        GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), originaldoc, "FMCSender");
//		            }
//			    }			    
//			}
//			/* <<== 20180717, Add, Unit Offline Check */
//		}
//		/* UnitCommunicationStateChange Online */
//		else if(StringUtil.equals(GenericServiceProxy.getConstantMap().Mac_OnLineRemote,unitCommunicationstate))
//		{
//		    /* 20180926, hhlee, Add, ProbeCard Unmount ==>> */
//            if (StringUtil.equals(unitData.getCommunicationState(), GenericServiceProxy.getConstantMap().Mac_OffLine) && portList.size() > 0)
//            {
//                this.searchPortListByUnitName(eventInfo, machineName, unitName, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL, portList);
//            }
//            /* <<== 20180926, hhlee, Add, ProbeCard Unmount */
//		    
//		    boolean isUnitOffLine = false;
//		    
//		    /* Unit Communication State Check */
//		    List<Map<String, Object>> unitDatabyCommunicationState = getMachineDataByCommunicationState(machineName, "2", 
//                    GenericServiceProxy.getConstantMap().Mac_OnLineRemote, GenericServiceProxy.getConstantMap().Mac_ProductionMachine);
//            if(unitDatabyCommunicationState == null || unitDatabyCommunicationState.size() <= 0)
//            {
//                isUnitOffLine = true;
//            }
//            
//            /* Port Access Mode Check */
//            boolean isPortAccessModeManual = false;
//            List<Map<String, Object>> portAccessModeDataList = searchPortAccessModeList(machineName,GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
//            if(portAccessModeDataList != null || portAccessModeDataList.size() > 0)
//            {
//                if(machinePortList.size() == portAccessModeDataList.size())
//                {
//                    isPortAccessModeManual = true;
//                }
//            }
//            
//            /* Unit Communication State Change */
//		    //changeUnitCommunicationState
//	        eventInfo.setEventName("ChangeUnitCommState");
//	        unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);	        
//	        MakeCommunicationStateInfo commtransitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeCommunicationStateInfo(unitData, unitCommunicationstate);	    
//	        MESMachineServiceProxy.getMachineServiceImpl().makeCommunicationState(unitData, commtransitionInfo, eventInfo);
//		    	        
//	        //if(isUnitOffLine)
//	        /* All ports are in manual mode */
//	        if(isPortAccessModeManual)
//	        {
//	            /* If there is no port list, change from Manual to Auto. */
//	            if(portList.size() <= 0)
//	            {
//    	            List<Map<String, Object>> portDataList = searchPortList(machineName,GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
//                    String resultMachineName = StringUtil.EMPTY;
//                    String resultPortName = StringUtil.EMPTY;
//                    
//                    if(portDataList != null && portDataList.size() > 0)
//                    {
//                        MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
//                        //for (Port portData : portDataList)
//                        for(int i = 0; i < portDataList.size(); i++)
//                        {
//                            
//                            resultMachineName = portDataList.get(i).get("MACHINENAME").toString();
//                            resultPortName = portDataList.get(i).get("PORTNAME").toString();
//        
//                            Port resultPortData = MESPortServiceProxy.getPortInfoUtil().getPortData(resultMachineName, resultPortName);
//                            
//                            porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(resultPortData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);
//                            MESPortServiceProxy.getPortServiceImpl().makeAccessMode(resultPortData, porttransitionInfo, eventInfo); 
//                            
//                            /* Make Port Access Mode Change Message */
//                            originaldoc = generatePortAccessModeChangeTemplate(originaldoc, machineName, resultPortData.getKey().getPortName(), resultPortData.getUdfs().get("PORTTYPE"),
//                                    resultPortData.getUdfs().get("PORTUSETYPE"), GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);
//                            GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), originaldoc, "FMCSender");
//                        }
//                    }
//	            }
//	        }
//	        
//		    //Release all ports in this unit
//		    List<String> portNameList =null;
//		    /* If there is a list of ports, change from Manual to Auto. 
//		     * When changing a port from manual to auto Check the overall port status and change the status. 
//		     */	
//		    
//		    if(portList.size() > 0)
//		    {
//		        portNameList = new ArrayList<String>();
//		        for(Element elementPort : portList)
//		        {
//		            String portName = SMessageUtil.getChildText(elementPort, "PORTNAME", false);
//		            if (StringUtil.isNotEmpty(portName))
//		            {
//    		            portNameList.add(portName);
//		            }
//		        }		      
//		    }
//		    
//		    this.checkReleaseDoubleMainMachinePortAccessMode(eventInfo, machineName, unitName, portNameList, "", isPortAccessModeManual ,originaldoc);
//		}
//		
//		/* 20180928, Add, Machine - 1, Unit - 1-1, 1-2, Port 1,2,3,4 
//		 * Unit 1-1 : P 1(Manual), Unit 1-2 : P 2(Manual) ==>> All Port Manual ==>> */		
//		this.checkReleaseDoubleMainMachinePortAccessMode(eventInfo, machineName);		
//		/* <<== 20180928, Add, Machine - 1, Unit - 1-1, 1-2, Port 1,2,3,4 
//         * Unit 1-1 : P 1(Manual), Unit 1-2 : P 2(Manual) ==>> All Port Manual */
//		
//		
//		//---------------Send to FMC---------------------
//		try
//		{
//			/* Make Communication State Change Message */
//			originaldoc = generateCommunicationStateChangeTemplate(originaldoc, unitName, unitCommunicationstate);
//			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), originaldoc, "FMCSender");
//		}
//		catch(Exception ex)
//		{
//			eventLog.warn("FMC Report Failed!");
//		}
//		//---------------Send to FMC---------------------
//
//		return doc;
//	}
//
//    /**
//     * @Name     generateCommunicationStateChangeTemplate
//     * @since    2018. 4. 21.
//     * @author   hhlee
//     * @contents Make Communication State Change Message
//     * @param doc
//     * @param machinename
//     * @param communicatestate
//     * @return doc
//     * @throws CustomException
//     */
//	private Document generateCommunicationStateChangeTemplate(Document doc, String machinename,
//			                                                           String communicatestate ) throws CustomException
//	{
//		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_CommunicationStateChanged");
//		SMessageUtil.setHeaderItemValue(doc, "EVENTCOMMENT", "ChangeAccessMode");
//
//
//		Element bodyElement = SMessageUtil.getBodyElement(doc);
//
//		bodyElement.removeContent();
//
//		Element machineNameElement = new Element("MACHINENAME");
//		machineNameElement.setText(machinename);
//		bodyElement.addContent(machineNameElement);
//
//		Element communicateState = new Element("COMMUNICATIONSTATE");
//		communicateState.setText(communicatestate);
//		bodyElement.addContent(communicateState);
//
//		return doc;
//	}
//
//	/**
//	 * @Name     generatePortAccessModeChangeTemplate
//	 * @since    2018. 4. 21.
//	 * @author   hhlee
//	 * @contents Make Port Access Mode Change Message
//	 * @param doc
//	 * @param machinename
//	 * @param portname
//	 * @param porttype
//	 * @param portusetype
//	 * @param accessmode
//	 * @return doc
//	 * @throws CustomException
//	 */
//	private Document generatePortAccessModeChangeTemplate(Document doc, String machinename,
//			                                         String portname, String porttype, String portusetype,
//			                                                           String accessmode) throws CustomException
//	{
//		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_PortAccessModeChanged");
//		SMessageUtil.setHeaderItemValue(doc, "EVENTCOMMENT", "ChangeCommState");
//
//		Element bodyElement = SMessageUtil.getBodyElement(doc);
//
//		bodyElement.removeContent();
//
//		Element machineNameElement = new Element("MACHINENAME");
//		machineNameElement.setText(machinename);
//		bodyElement.addContent(machineNameElement);
//
//		Element portNameElement = new Element("PORTNAME");
//		portNameElement.setText(portname);
//		bodyElement.addContent(portNameElement);
//
//		/* 2018.04.02, hhlee, porttype element Add */
//		Element portTypeElement = new Element("PORTTYPE");
//		portTypeElement.setText(porttype);
//		bodyElement.addContent(portTypeElement);
//
//		Element portUseTypeElement = new Element("PORTUSETYPE");
//		portUseTypeElement.setText(portusetype);
//		bodyElement.addContent(portUseTypeElement);
//
//		Element portAccessModeElement = new Element("PORTACCESSMODE");
//		portAccessModeElement.setText(accessmode);
//		bodyElement.addContent(portAccessModeElement);
//
//		return doc;
//	}
//
//	/**
//	 * @Name     releaseAllportAccessMode
//	 * @since    2018. 4. 23.
//	 * @author   hhlee
//	 * @contents Release for the Machine/DoubleMainMachieName
//	 * @param eventinfo
//	 * @param machinename
//	 * @param doublemainmachinename
//	 * @throws CustomException
//	 */
//	private void releaseAllportAccessMode(EventInfo eventinfo, String machinename, String doublemainmachinename, Document originaldoc)  throws CustomException
//	{
//       	String machineName = StringUtil.EMPTY;
//		String portName = StringUtil.EMPTY;
//		Port portData = null;
//		
//		String strSql = "SELECT A.MACHINENAME, A.PORTNAME, A.DOUBLEMAINMACHINENAME " +
//         		        "  FROM PORT A " +
//		                " WHERE 1=1 " +
//		                "   AND A.MACHINENAME = :MACHINENAME " +
//		                "   AND A.DOUBLEMAINMACHINENAME  = :DOUBLEMAINMACHINENAME ";
//		
//		Map<String, Object> bindMap = new HashMap<String, Object>();
//		bindMap.put("MACHINENAME", machinename);
//		bindMap.put("DOUBLEMAINMACHINENAME", doublemainmachinename);
//
//		List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
//
//		if(sqlResult.size () > 0)
//		{
//			 machineName = StringUtil.EMPTY;
//			 portName = StringUtil.EMPTY;
//			
//			for(int i = 0; i < sqlResult.size(); i++)
//			{
//				//Release for the Machine/DoubleMainMachieName.
//				
//				machineName = sqlResult.get(i).get("MACHINENAME").toString();
//				portName = sqlResult.get(i).get("PORTNAME").toString();
//
//			    portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
//			    
//			    MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
//			    
//				eventinfo.setEventName("ChangeAccessMode");
//
//				boolean result = false;
//				
//				if(!StringUtil.equals(portData.getAccessMode(), GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO))
//				{
//					porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);
//					result = true;
//				}
//			       
//
//			    Map<String, String> portUdfs = portData.getUdfs();
//				portUdfs.put("DOUBLEMAINMACHINENAME", StringUtil.EMPTY);
//				portData.setUdfs(portUdfs);
//
//				porttransitionInfo.setUdfs(portUdfs);
//				
//				//to aviod repetition of AccessMode
//				if(result == true)
//				{
//				    MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, porttransitionInfo, eventinfo);
//				}
//				else
//				{
//					SetEventInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(portUdfs);
//					MESPortServiceProxy.getPortServiceImpl().setEvent(portData, transitionInfo, eventinfo);
//				}
//				
//				try
//		        {
//		            /* Make Port Access Mode Change Message */
//		            originaldoc = generatePortAccessModeChangeTemplate(originaldoc, machinename, portName, portData.getUdfs().get("PORTTYPE"),
//		                                                                  portData.getUdfs().get("PORTUSETYPE"), GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);
//		            GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), originaldoc, "FMCSender");
//		        }
//		        catch(Exception ex)
//		        {
//		            eventLog.warn("FMC Report Failed!");
//		        }
//			}
//		}
//		else
//		{
//		}		
//	}	
//	/**
//	 * 
//	 * @Name     getMachineDataByCommunicationState
//	 * @since    2018. 7. 23.
//	 * @author   hhlee
//	 * @contents Get Machine Data By CommunicationState
//	 *           
//	 * @param machineName
//	 * @param machineLevel
//	 * @param communicationState
//	 * @param machineType
//	 * @return
//	 * @throws CustomException
//	 */
//	private List<Map<String, Object>> getMachineDataByCommunicationState(String machineName, String machineLevel, 
//            String communicationState, String machineType) throws CustomException
//    {
//        List<Map<String, Object>> machineDataByCommunicationState = null;
//        String strSql = StringUtil.EMPTY;
//        try
//        {
//            strSql = " SELECT SQ.LV                                                                         \n" 
//                    + "       ,SQ.COMBINEMACHINE                                                             \n"
//                    + "       ,REGEXP_SUBSTR(SQ.COMBINEMACHINE, '[^-]+', 1, 1) AS MACHINENAME                \n"
//                    + "       ,REGEXP_SUBSTR(SQ.COMBINEMACHINE, '[^-]+', 1, 2) AS UNITNAME                   \n"
//                    + "       ,REGEXP_SUBSTR(SQ.COMBINEMACHINE, '[^-]+', 1, 3) AS SUBUNITNAME                \n"
//                    + "       ,SQ.FACTORYNAME,SQ.AREANAME,SQ.SUPERMACHINENAME                                \n"
//                    + "       ,SQ.MACHINEGROUPNAME,SQ.PROCESSCOUNT,SQ.RESOURCESTATE                          \n"
//                    + "       ,SQ.E10STATE,SQ.COMMUNICATIONSTATE,SQ.MACHINESTATENAME                         \n"
//                    + "       ,SQ.LASTEVENTNAME,SQ.LASTEVENTTIMEKEY,SQ.LASTEVENTTIME                         \n"
//                    + "       ,SQ.LASTEVENTUSER,SQ.LASTEVENTCOMMENT,SQ.LASTEVENTFLAG                         \n"
//                    + "       ,SQ.REASONCODETYPE,SQ.REASONCODE,SQ.MCSUBJECTNAME                              \n"
//                    + "       ,SQ.OPERATIONMODE,SQ.DSPFLAG,SQ.FULLSTATE                                      \n"
//                    + "       ,SQ.ONLINEINITIALCOMMSTATE                                                     \n"
//                    + "   FROM (                                                                             \n"
//                    + "         SELECT LEVEL LV                                                              \n"
//                    + "               ,SUBSTR(SYS_CONNECT_BY_PATH(M.MACHINENAME, '-'), 2) AS COMBINEMACHINE  \n"
//                    + "               ,M.*                                                                   \n"
//                    + "               FROM MACHINE M                                                         \n"
//                    + "         START WITH M.SUPERMACHINENAME IS NULL                                        \n"
//                    + "                AND M.MACHINENAME = :MACHINENAME                                      \n"
//                    + "   CONNECT BY PRIOR M.MACHINENAME = M.SUPERMACHINENAME                                \n"
//                    + "         ORDER BY M.MACHINENAME                                                       \n"
//                    + "         ) SQ,                                                                        \n"
//                    + "  MACHINESPEC MS                                                                      \n"
//                    + " WHERE 1=1                                                                            \n"
//                    + " AND SQ.MACHINENAME = MS.MACHINENAME                                                  \n";
//            
//            Map<String, Object> bindMap = new HashMap<String, Object>(); 
//            bindMap.put("MACHINENAME", machineName);
//            
//            if(StringUtil.isNotEmpty(machineLevel))
//            {
//                strSql = strSql  + " AND SQ.LV = :MACHINELEVEL                                              \n";
//                bindMap.put("MACHINELEVEL", machineLevel);
//            }
//            if(StringUtil.isNotEmpty(machineType))
//            {
//                strSql = strSql  + " AND MS.MACHINETYPE = :MACHINETYPE                                      \n";
//                bindMap.put("MACHINETYPE", machineType);
//            }
//            if(StringUtil.isNotEmpty(communicationState))
//            {
//                strSql = strSql  + " AND NVL(SQ.COMMUNICATIONSTATE, 'OffLine') = :COMMUNICATIONSTATE        \n";
//                bindMap.put("COMMUNICATIONSTATE", communicationState);
//            }
//            strSql = strSql  + " ORDER BY SQ.LV, SQ.COMBINEMACHINE                                                    \n";
//                    
//            machineDataByCommunicationState = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
//        }
//        catch (Exception ex)
//        {
//            //throw new CustomException();
//        }
//        
//        return machineDataByCommunicationState;
//    }
//	
//	/**
//	 * 
//	 * @Name     searchPortList
//	 * @since    2018. 7. 23.
//	 * @author   hhlee
//	 * @contents Search Port List
//	 *           
//	 * @param machineName
//	 * @param accessMode
//	 * @return
//	 * @throws CustomException
//	 */
//	private List<Map<String, Object>> searchPortList(String machineName, String accessMode) throws CustomException
//    {
//	    List<Map<String, Object>> result = null;
//        try
//        {
//            //AND DOUBLEMAINMACHINENAME IS NULL
//            //List<Port> result = PortServiceProxy.getPortService().select(" MACHINENAME = ? AND ACCESSMODE = ? ", new Object[] {machineName, accessMode});    
//            
//            String strSql = "SELECT A.*                              \n"
//                          + "  FROM PORT A                           \n"
//                          + " WHERE 1=1                              \n"
//                          + "   AND A.MACHINENAME = :MACHINENAME     \n"
//                          + "   AND A.ACCESSMODE  = :ACCESSMODE      \n"
//                          + "   AND A.DOUBLEMAINMACHINENAME IS NULL  \n";
//            
//            Map<String, Object> bindMap = new HashMap<String, Object>();
//            bindMap.put("MACHINENAME", machineName);
//            bindMap.put("ACCESSMODE", accessMode);
//            
//            result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
//                    
//        }
//        catch (Exception ex)
//        {
//          //throw new CustomException();
//        }
//        
//        return result;
//    }
//	
//	/**
//	 * 
//	 * @Name     searchPortListByUnitName
//	 * @since    2018. 9. 27.
//	 * @author   hhlee
//	 * @contents 
//	 *           
//	 * @param eventinfo
//	 * @param machineName
//	 * @param unitName
//	 * @param accessMode
//	 * @param portList
//	 * @throws CustomException
//	 */
//	private void searchPortListByUnitName(EventInfo eventInfo, String machineName, String unitName, String accessMode, List<Element> portList) throws CustomException
//    {
//        //List<Map<String, Object>> result = null;
//        try
//        {
//            boolean isManualPortExist = false;
//            
//            //AND DOUBLEMAINMACHINENAME IS NULL
//            List<Port> portResult = PortServiceProxy.getPortService().select(" WHERE MACHINENAME = ? AND ACCESSMODE = ? AND DOUBLEMAINMACHINENAME = ? ", new Object[] {machineName, accessMode, unitName});    
//            
//            if(portResult != null && portResult.size() > 0)    
//            {
//                for(Port portData : portResult)
//                {
//                    isManualPortExist = false;
//                    
//                    for(Element elementPort : portList)
//                    {
//                        String elePortName = SMessageUtil.getChildText(elementPort, "PORTNAME", false);
//                        
//                        if(StringUtil.equals(portData.getKey().getPortName(), elePortName))
//                        {
//                            isManualPortExist = true;
//                            break;
//                        }
//                    }
//                    
//                    if(!isManualPortExist)
//                    {
//                        MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
//                        
//                        eventInfo.setEventName("ChangeAccessMode");
//
//                        porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);
//                        
//                        Map<String, String> portUdfs = portData.getUdfs();
//                        portUdfs.put("DOUBLEMAINMACHINENAME", StringUtil.EMPTY);
//                        portData.setUdfs(portUdfs);
//                        porttransitionInfo.setUdfs(portUdfs);
//                        
//                        MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, porttransitionInfo, eventInfo);   
//                        
//                        eventInfo.setCheckTimekeyValidation(false);
//                        eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
//                        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//                    }                    
//                }
//            }            
//        }
//        catch (Exception ex)
//        {
//            log.error(ex.getMessage());
//        }        
//    }
//	/**
//	 * 
//	 * @Name     searchPortAccessModeList
//	 * @since    2018. 7. 23.
//	 * @author   hhlee
//	 * @contents Search Port Access Mode List
//	 *           
//	 * @param machineName
//	 * @param accessMode
//	 * @return
//	 * @throws CustomException
//	 */
//	private List<Map<String, Object>> searchPortAccessModeList(String machineName, String accessMode) throws CustomException
//    {
//        List<Map<String, Object>> result = null;
//        try
//        {
//            //AND DOUBLEMAINMACHINENAME IS NULL
//            //List<Port> result = PortServiceProxy.getPortService().select(" MACHINENAME = ? AND ACCESSMODE = ? ", new Object[] {machineName, accessMode});    
//            
//            String strSql = "SELECT A.*                              \n"
//                          + "  FROM PORT A                           \n"
//                          + " WHERE 1=1                              \n"
//                          + "   AND A.MACHINENAME = :MACHINENAME     \n"
//                          + "   AND A.ACCESSMODE  = :ACCESSMODE      \n";                          
//            
//            Map<String, Object> bindMap = new HashMap<String, Object>();
//            bindMap.put("MACHINENAME", machineName);
//            bindMap.put("ACCESSMODE", accessMode);
//            
//            result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
//                    
//        }
//        catch (Exception ex)
//        {
//          //throw new CustomException();
//        }
//        
//        return result;
//    }
//	
//	/**
//	 * 
//	 * @Name     checkReleaseDoubleMainMachinePortAccessMode
//	 * @since    2018. 7. 23.
//	 * @author   hhlee
//	 * @contents Release DoubleMainMachine PortAccessMode
//	 *           
//	 * @param eventinfo
//	 * @param machinename
//	 * @param doublemainmachinename
//	 * @param portNameList
//	 * @param accessMode
//	 * @param isPortAccessModeManual
//	 * @param originaldoc
//	 * @throws CustomException
//	 */
//    private void checkReleaseDoubleMainMachinePortAccessMode(EventInfo eventinfo, String machinename, 
//            String doublemainmachinename, List<String> portNameList, String accessMode, boolean isPortAccessModeManual, Document originaldoc)  throws CustomException
//    {
//        Port portData = null;
//        
//        String strSql = "SELECT A.MACHINENAME, A.PORTNAME, A.DOUBLEMAINMACHINENAME \n" +
//                      "    FROM PORT A                                             \n" +
//                      "   WHERE 1 = 1                                              \n" +
//                      "     AND A.MACHINENAME = :MACHINENAME                       \n" +
//                      "     AND A.DOUBLEMAINMACHINENAME = :DOUBLEMAINMACHINENAME   \n" ;
//
//        Map<String, Object> bindMap = new HashMap<String, Object>();        
//        bindMap.put("MACHINENAME", machinename);      
//        bindMap.put("DOUBLEMAINMACHINENAME", doublemainmachinename);
//
//        List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
//
//        /* If there is a list of ports, change from Manual to Auto. 
//         * When changing a port from manual to auto Check the overall port status and change the status. 
//         */ 
//        if(sqlResult.size () > 0)
//        {   
//            String resultMachineName = StringUtil.EMPTY;
//            String resultPortName = StringUtil.EMPTY;
//            boolean isDoubleMainMachineNullUpdate = false;
//            
//            if(portNameList == null || portNameList.size() <= 0)
//            {
//                for(int i = 0; i < sqlResult.size(); i++)
//                {
//                    //Release for the Machine/DoubleMainMachieName. 
//                    resultMachineName = sqlResult.get(i).get("MACHINENAME").toString();
//                    resultPortName =  sqlResult.get(i).get("PORTNAME").toString();
//                    
//                    portData = MESPortServiceProxy.getPortInfoUtil().getPortData(resultMachineName, resultPortName);    
//                    
//                    Map<String, String> portUdfs = portData.getUdfs();
//                    MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
//                    
//                    portUdfs.put("DOUBLEMAINMACHINENAME", StringUtil.EMPTY);
//                    porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);
//                    
//                    if(!StringUtils.equals(portData.getAccessMode(), porttransitionInfo.getAccessMode()))
//                    {
//                        MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, porttransitionInfo, eventinfo);
//                    }
//                    else
//                    {
//                        SetEventInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(portUdfs);
//                        MESPortServiceProxy.getPortServiceImpl().setEvent(portData, transitionInfo, eventinfo);
//                    }  
//                }
//            }
//            else
//            {
//                if(portNameList != null && portNameList.size() > 0)
//                {
//                    for(int i = 0; i < sqlResult.size(); i++)
//                    {
//                        //Release for the Machine/DoubleMainMachieName. 
//                        resultMachineName = sqlResult.get(i).get("MACHINENAME").toString();
//                        resultPortName =  sqlResult.get(i).get("PORTNAME").toString();
//                        
//                        portData = MESPortServiceProxy.getPortInfoUtil().getPortData(resultMachineName, resultPortName);    
//                        
//                        Map<String, String> portUdfs = portData.getUdfs();
//                        MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
//                        
//                        
//                        isDoubleMainMachineNullUpdate = true;
//                        for(String portname : portNameList)
//                        {
//                            if (StringUtil.equals(resultPortName,portname))
//                            {
//                                isDoubleMainMachineNullUpdate = false;
//                                break;
//                            }
//                        }
//                        
//                        if(isDoubleMainMachineNullUpdate)
//                        {   
//                            portUdfs.put("DOUBLEMAINMACHINENAME", StringUtil.EMPTY);
//                            if(!isPortAccessModeManual)
//                            {                       
//                                porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);  
//                            }
//                            else
//                            {
//                                porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, portData.getAccessMode());
//                            }                                  
//                        }
//                        else
//                        {
//                            porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, portData.getAccessMode());
//                        }
//                        
//                        portData.setUdfs(portUdfs);
//                        porttransitionInfo.setUdfs(portUdfs);
//                        
//                        if(!StringUtils.equals(portData.getAccessMode(), porttransitionInfo.getAccessMode()))
//                        {
//                            MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, porttransitionInfo, eventinfo);
//                        }
//                        else
//                        {
//                            SetEventInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(portUdfs);
//                            MESPortServiceProxy.getPortServiceImpl().setEvent(portData, transitionInfo, eventinfo);
//                        }                        
//                    }
//                }
//            }
////                        
////            for(int i = 0; i < sqlResult.size(); i++)
////            {
////                //Release for the Machine/DoubleMainMachieName. 
////                resultMachineName = sqlResult.get(i).get("MACHINENAME").toString();
////                resultPortName =  sqlResult.get(i).get("PORTNAME").toString();
////               
////                isDoubleMainMachineNullUpdatebyNullPortList = false;
////                isDoubleMainMachineNullUpdate = true;
////                if(portNameList != null && portNameList.size() > 0)
////                {     
////                    isDoubleMainMachineNullUpdatebyNullPortList = true;
////                    for(String portname : portNameList)
////                    {
////                        if (StringUtil.equals(resultPortName,portname))
////                        {
////                            isDoubleMainMachineNullUpdate = false;
////                            break;
////                        }
////                    }
////                }
////                else
////                {                    
////                }
////                
////                MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
////                
////                eventinfo.setEventName("ChangeAccessMode");
////                portData = MESPortServiceProxy.getPortInfoUtil().getPortData(resultMachineName, resultPortName);                 
////                
////                Map<String, String> portUdfs = portData.getUdfs();
////                
////                if(isDoubleMainMachineNullUpdatebyNullPortList)
////                {
////                    portUdfs.put("DOUBLEMAINMACHINENAME", StringUtil.EMPTY);
////                    porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);
////                }
////                else if(isDoubleMainMachineNullUpdate)
////                {   
////                    portUdfs.put("DOUBLEMAINMACHINENAME", StringUtil.EMPTY);
////                    if(!isOnlineRemoteUpdate)
////                    {                       
////                        porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);  
////                    }
////                    else
////                    {
////                        porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, portData.getAccessMode());
////                    }
////                        
////                }
////                else
////                {
////                    portUdfs.put("DOUBLEMAINMACHINENAME", portData.getUdfs().get("DOUBLEMAINMACHINENAME"));
////                    //porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
////                    porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, portData.getAccessMode());
////                }
//                                
//            
//            //success then report to FMC
//            try
//            {
//                /* Make Port Access Mode Change Message */
//                originaldoc = generatePortAccessModeChangeTemplate(originaldoc, resultMachineName, resultPortName, portData.getUdfs().get("PORTTYPE"),
//                                                                      portData.getUdfs().get("PORTUSETYPE"), GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);
//                GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), originaldoc, "FMCSender");
//            }
//            catch(Exception ex)
//            {
//                eventLog.warn("FMC Report Failed!");
//            }
//        }       
//        
//    }
//    
//    /**
//     * 
//     * @Name     searchMachinePortList
//     * @since    2018. 7. 23.
//     * @author   hhlee
//     * @contents Search Machine PortList
//     *           
//     * @param machineName
//     * @return
//     * @throws CustomException
//     */
//    private List<Map<String, Object>> searchMachinePortList(String machineName) throws CustomException
//    {
////        String condition = " WHERE machineName = ? ";
////        Object[] bindSet = new Object[] {machineName};
////        
////        List<Port> portList;
////        try
////        {
////            portList = PortServiceProxy.getPortService().select(condition, bindSet);
////            return portList;
////        }
////        catch(Exception ex)
////        {
////            return portList = new ArrayList<Port>();
////        }
//        
//       String strSql = "SELECT A.MACHINENAME, A.PORTNAME, A.DOUBLEMAINMACHINENAME \n" +
//                       "  FROM PORT A                                             \n" +
//                       " WHERE 1 = 1                                              \n" +
//                       "   AND A.MACHINENAME = :MACHINENAME                       \n";               
//
//        Map<String, Object> bindMap = new HashMap<String, Object>();        
//        bindMap.put("MACHINENAME", machineName);
//    
//        List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
//        
//        return sqlResult;
//
//    }
//    
//    /**
//     * 
//     * @Name     checkReleaseDoubleMainMachinePortAccessMode
//     * @since    2018. 9. 28.
//     * @author   hhlee
//     * @contents 
//     *           
//     * @param eventInfo
//     * @param machineName
//     * @throws CustomException
//     */
//    private void checkReleaseDoubleMainMachinePortAccessMode(EventInfo eventInfo, String machineName) throws CustomException
//    {
//        List<Map<String, Object>> dubleMachineNameList = this.searchDubleMachineNamePortAccessModeList(machineName, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
//        
//        List<Map<String, Object>> prounitList = getMachineDataByCommunicationState(machineName, "2", StringUtil.EMPTY, GenericServiceProxy.getConstantMap().Mac_ProductionMachine);
//        
//        if(dubleMachineNameList != null && prounitList != null)
//        {
//            if(dubleMachineNameList.size() == prounitList.size())
//            {
//                try
//                {
//                    List<Port> portList = PortServiceProxy.getPortService().select(" MACHINENAME = ? AND ACCESSMODE = ? ", new Object[] {machineName, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO});  
//                
//                    for(Port portData : portList)
//                    {
//                        eventInfo.setCheckTimekeyValidation(false);
//                        eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
//                        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//                        
//                        MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
//                        porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
//                        MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, porttransitionInfo, eventInfo);
//                    }
//                }
//                catch(Exception ex)
//                {
//                    eventLog.warn(ex.getStackTrace());
//                }                
//            }
//        }    
//    }
//    
//    private List<Map<String, Object>> searchDubleMachineNamePortAccessModeList(String machineName, String accessMode) throws CustomException
//    {
//        //List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
//        List<Map<String, Object>> result = null;
//        try
//        {
//            String strSql = "SELECT A.DOUBLEMAINMACHINENAME          \n"
//                          + "  FROM PORT A                           \n"
//                          + " WHERE 1=1                              \n"
//                          + "   AND A.MACHINENAME = :MACHINENAME     \n"
//                          + "   AND A.ACCESSMODE  = :ACCESSMODE      \n"
//                          + "   AND A.DOUBLEMAINMACHINENAME IS NOT NULL \n"
//                          + "  GROUP BY A.DOUBLEMAINMACHINENAME      \n";
//            
//            Map<String, Object> bindMap = new HashMap<String, Object>();
//            bindMap.put("MACHINENAME", machineName);
//            bindMap.put("ACCESSMODE", accessMode);
//            
//            result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
//                    
//        }
//        catch (Exception ex)
//        {
//            //throw new CustomException();
//            eventLog.warn(ex.getStackTrace());
//        }
//        
//        return result;
//    }
//}
