/**
 *
 */
package kr.co.aim.messolution.machine.event;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
//import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
//import kr.co.aim.greentrack.port.management.info.MakeAccessModeInfo;
//import kr.co.aim.greentrack.port.management.info.MakePortStateByStateInfo;
//import kr.co.aim.greentrack.port.management.info.SetEventInfo;
import kr.co.aim.greentrack.machine.management.info.MakeCommunicationStateInfo;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeAccessModeInfo;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

/**
 * @author Administrator
 *
 */
public class UnitCommunicationStateReply  extends AsyncHandler{
    @Override
    public void doWorks(Document doc) throws CustomException {

        /* Copy to FMCSender. Add, hhlee, 20180421 */
        Document originaldoc = (Document)doc.clone();

        try
        {
            EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnitCommunicationStateReport", getEventUser(), getEventComment(), "", "");

            /* Validate Machine */
            String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
            Machine mainMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

            List<Element> unitList = SMessageUtil.getSubSequenceItemList(SMessageUtil.getBodyElement(doc), "UNITLIST", false);
            if(unitList != null)
            {
                for (Element unitElementE : unitList)
                {
                    String unitName = SMessageUtil.getChildText(unitElementE, "UNITNAME", true);
                    String unitCommunicationstate = SMessageUtil.getChildText(unitElementE, "COMMUNICATIONSTATE", true);

                    if(StringUtil.equals(StringUtil.upperCase(unitCommunicationstate), StringUtil.upperCase(GenericServiceProxy.getConstantMap().Mac_OnLine)))
                    {
                        unitCommunicationstate = GenericServiceProxy.getConstantMap().Mac_OnLine;
                    }
                    else if(StringUtil.equals(StringUtil.upperCase(unitCommunicationstate), StringUtil.upperCase(GenericServiceProxy.getConstantMap().Mac_OnLineLocal)))
                    {
                        unitCommunicationstate = GenericServiceProxy.getConstantMap().Mac_OnLineLocal;
                    }
                    else if(StringUtil.equals(StringUtil.upperCase(unitCommunicationstate), StringUtil.upperCase(GenericServiceProxy.getConstantMap().Mac_OnLineRemote)))
                    {
                        unitCommunicationstate = GenericServiceProxy.getConstantMap().Mac_OnLineRemote;
                    }
                    else
                    {
                        unitCommunicationstate = GenericServiceProxy.getConstantMap().Mac_OffLine;
                    }

                    /* Validate UNIT */
                    Machine unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);

                    //Check Port part 
                    List<Element> portList = SMessageUtil.getSubSequenceItemList(unitElementE, "PORTLIST", false);
                    
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
                }
            }
        }
        catch(CustomException ce)
        {
            eventLog.warn(String.format("[%s]%s", ce.errorDef.getErrorCode(), ce.errorDef.getLoc_errorMessage()));
        }
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
    private List<Map<String, Object>> getMachineDataByCommunicationState(String machineName, String machineLevel, 
            String communicationState, String machineType) throws CustomException
    {
        List<Map<String, Object>> machineDataByCommunicationState = null;
        String strSql = StringUtil.EMPTY;
        try
        {
            strSql = " SELECT SQ.LV                                                                         \n" 
                    + "       ,SQ.COMBINEMACHINE                                                             \n"
                    + "       ,REGEXP_SUBSTR(SQ.COMBINEMACHINE, '[^-]+', 1, 1) AS MACHINENAME                \n"
                    + "       ,REGEXP_SUBSTR(SQ.COMBINEMACHINE, '[^-]+', 1, 2) AS UNITNAME                   \n"
                    + "       ,REGEXP_SUBSTR(SQ.COMBINEMACHINE, '[^-]+', 1, 3) AS SUBUNITNAME                \n"
                    + "       ,SQ.FACTORYNAME,SQ.AREANAME,SQ.SUPERMACHINENAME                                \n"
                    + "       ,SQ.MACHINEGROUPNAME,SQ.PROCESSCOUNT,SQ.RESOURCESTATE                          \n"
                    + "       ,SQ.E10STATE,SQ.COMMUNICATIONSTATE,SQ.MACHINESTATENAME                         \n"
                    + "       ,SQ.LASTEVENTNAME,SQ.LASTEVENTTIMEKEY,SQ.LASTEVENTTIME                         \n"
                    + "       ,SQ.LASTEVENTUSER,SQ.LASTEVENTCOMMENT,SQ.LASTEVENTFLAG                         \n"
                    + "       ,SQ.REASONCODETYPE,SQ.REASONCODE,SQ.MCSUBJECTNAME                              \n"
                    + "       ,SQ.OPERATIONMODE,SQ.DSPFLAG,SQ.FULLSTATE                                      \n"
                    + "       ,SQ.ONLINEINITIALCOMMSTATE                                                     \n"
                    + "   FROM (                                                                             \n"
                    + "         SELECT LEVEL LV                                                              \n"
                    + "               ,SUBSTR(SYS_CONNECT_BY_PATH(M.MACHINENAME, '-'), 2) AS COMBINEMACHINE  \n"
                    + "               ,M.*                                                                   \n"
                    + "               FROM MACHINE M                                                         \n"
                    + "         START WITH M.SUPERMACHINENAME IS NULL                                        \n"
                    + "                AND M.MACHINENAME = :MACHINENAME                                      \n"
                    + "   CONNECT BY PRIOR M.MACHINENAME = M.SUPERMACHINENAME                                \n"
                    + "         ORDER BY M.MACHINENAME                                                       \n"
                    + "         ) SQ,                                                                        \n"
                    + "  MACHINESPEC MS                                                                      \n"
                    + " WHERE 1=1                                                                            \n"
                    + " AND SQ.MACHINENAME = MS.MACHINENAME                                                  \n";
            
            Map<String, Object> bindMap = new HashMap<String, Object>(); 
            bindMap.put("MACHINENAME", machineName);
            
            if(StringUtil.isNotEmpty(machineLevel))
            {
                strSql = strSql  + " AND SQ.LV = :MACHINELEVEL                                              \n";
                bindMap.put("MACHINELEVEL", machineLevel);
            }
            if(StringUtil.isNotEmpty(machineLevel))
            {
                strSql = strSql  + " AND MS.MACHINETYPE = :MACHINETYPE                                      \n";
                bindMap.put("MACHINETYPE", machineType);
            }
            if(StringUtil.isNotEmpty(machineLevel))
            {
                strSql = strSql  + " AND NVL(SQ.COMMUNICATIONSTATE, 'OffLine') = :COMMUNICATIONSTATE        \n";
                bindMap.put("COMMUNICATIONSTATE", communicationState);
            }
            strSql = strSql  + " ORDER BY SQ.LV, SQ.COMBINEMACHINE                                                    \n";
                    
            machineDataByCommunicationState = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
        }
        catch (Exception ex)
        {
            //throw new CustomException();
        }
        
        return machineDataByCommunicationState;
    }
    
    
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
    
    //add by wghuang 20180531
    private void checkReleaseDoubleMainMachinePortAccessMode(EventInfo eventinfo, String machinename, 
            String doublemainmachinename, List<String> portNameList, String accessMode, Document originaldoc)  throws CustomException
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

        if(sqlResult.size () > 0)
        {   
            String resultMachineName = StringUtil.EMPTY;
            String resultPortName = StringUtil.EMPTY;
            boolean isDoubleMainMachineNullUpdate = false;
                        
            for(int i = 0; i < sqlResult.size(); i++)
            {
                //Release for the Machine/DoubleMainMachieName. 
                resultMachineName = sqlResult.get(i).get("MACHINENAME").toString();
                resultPortName =  sqlResult.get(i).get("PORTNAME").toString();
                
                isDoubleMainMachineNullUpdate = false;
                for(String portname : portNameList)
                {
                    if (StringUtil.equals(resultPortName,portname))
                    {
                        isDoubleMainMachineNullUpdate = true;
                        break;
                    }
                }
                MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
                
                eventinfo.setEventName("ChangeAccessMode");
                portData = MESPortServiceProxy.getPortInfoUtil().getPortData(resultMachineName, resultPortName);                 
                
                if(!isDoubleMainMachineNullUpdate)
                {                                       
                    porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);                    
                }
                else
                {     
                    //porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
                    porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, portData.getAccessMode());
                }
                
                
                Map<String, String> portUdfs = portData.getUdfs();
                portUdfs.put("DOUBLEMAINMACHINENAME", StringUtil.EMPTY);
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
        else
        {          
        }
        
    }
}






//public class UnitCommunicationStateReply  extends AsyncHandler{
//    @Override
//    public void doWorks(Document doc) throws CustomException {
//
//        /* Copy to FMCSender. Add, hhlee, 20180421 */
//        Document originaldoc = (Document)doc.clone();
//
//        try
//        {
//            EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnitCommunicationStateReport", getEventUser(), getEventComment(), "", "");
//
//            /* Validate Machine */
//            String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
//            Machine mainMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
//
//            List<Element> unitList = SMessageUtil.getSubSequenceItemList(SMessageUtil.getBodyElement(doc), "UNITLIST", false);
//            if(unitList != null)
//            {
//                for (Element unitElementE : unitList)
//                {
//                    String unitName = SMessageUtil.getChildText(unitElementE, "UNITNAME", true);
//                    String unitCommunicationstate = SMessageUtil.getChildText(unitElementE, "COMMUNICATIONSTATE", true);
//
//                    if(StringUtil.equals(StringUtil.upperCase(unitCommunicationstate), StringUtil.upperCase(GenericServiceProxy.getConstantMap().Mac_OnLine)))
//                    {
//                        unitCommunicationstate = GenericServiceProxy.getConstantMap().Mac_OnLine;
//                    }
//                    else if(StringUtil.equals(StringUtil.upperCase(unitCommunicationstate), StringUtil.upperCase(GenericServiceProxy.getConstantMap().Mac_OnLineLocal)))
//                    {
//                        unitCommunicationstate = GenericServiceProxy.getConstantMap().Mac_OnLineLocal;
//                    }
//                    else if(StringUtil.equals(StringUtil.upperCase(unitCommunicationstate), StringUtil.upperCase(GenericServiceProxy.getConstantMap().Mac_OnLineRemote)))
//                    {
//                        unitCommunicationstate = GenericServiceProxy.getConstantMap().Mac_OnLineRemote;
//                    }
//                    else
//                    {
//                        unitCommunicationstate = GenericServiceProxy.getConstantMap().Mac_OffLine;
//                    }
//
//                    /* Validate UNIT */
//                    Machine unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
//
//                    eventInfo.setEventName("ChangeCommState");
//                    MakeCommunicationStateInfo commtransitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeCommunicationStateInfo(unitData, unitCommunicationstate);
//                    MESMachineServiceProxy.getMachineServiceImpl().makeCommunicationState(unitData, commtransitionInfo, eventInfo);
//
//                    //159512 by swcho : success then report to FMC
//                    try
//                    {
//                        /* Make Communication State Change Message */
//                        originaldoc = generateCommunicationStateChangeTemplate(originaldoc, unitName, unitCommunicationstate);
//                        GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), originaldoc, "FMCSender");
//                    }
//                    catch(Exception ex)
//                    {
//                        eventLog.warn("FMC Report Failed!");
//                    }
//
//                    List<Element> portList = SMessageUtil.getSubSequenceItemList(unitElementE, "PORTLIST", false);
//                    if(portList != null)
//                    {
//                        /* DoubleMainMachineName("")/portAccessMode("Auto") State all release */
//                        releaseAllportAccessMode(eventInfo, machineName, unitName);
//
//                        for (Element portElementE : portList)
//                        {
//                            /* Validate Port */
//                            String portName = SMessageUtil.getChildText(portElementE, "PORTNAME", true);
//                            Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
//
//                            String portAccessMode = StringUtil.EMPTY;
//
//                            if(StringUtil.equals(unitCommunicationstate,GenericServiceProxy.getConstantMap().Mac_OffLine))
//                            {
//                                portAccessMode = GenericServiceProxy.getConstantMap().Port_Manual;
//                            }
//                            else
//                            {
//                                portAccessMode = GenericServiceProxy.getConstantMap().Port_Auto;
//                            }
//
//                            eventInfo.setEventName("ChangeAccessMode");
//
//                            MakeAccessModeInfo porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, portAccessMode);
//
//                            Map<String, String> portUdfs = portData.getUdfs();
//                            portUdfs.put("DOUBLEMAINMACHINENAME", unitName);
//                            portData.setUdfs(portUdfs);
//
//                            porttransitionInfo.setUdfs(portUdfs);
//                            MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, porttransitionInfo, eventInfo);
//
//                            //159512 by swcho : success then report to FMC
//                            try
//                            {
//                                /* Make Port Access Mode Change Message */
//                                originaldoc = generatePortAccessModeChangeTemplate(originaldoc, machineName, portName, portData.getUdfs().get("PORTTYPE"),
//                                                                                      portData.getUdfs().get("PORTUSETYPE"), portAccessMode);
//                                GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), originaldoc, "FMCSender");
//                            }
//                            catch(Exception ex)
//                            {
//                                eventLog.warn("FMC Report Failed!");
//                            }
//
//                        }
//                    }
//                }
//            }
//        }
//        catch(CustomException ce)
//        {
//            eventLog.warn(String.format("[%s]%s", ce.errorDef.getErrorCode(), ce.errorDef.getLoc_errorMessage()));
//        }
//    }
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
//    private Document generateCommunicationStateChangeTemplate(Document doc, String machinename,
//                                                                       String communicatestate ) throws CustomException
//    {
//        SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_CommunicationStateChanged");
//        SMessageUtil.setHeaderItemValue(doc, "EVENTCOMMENT", "ChangeAccessMode");
//
//
//        Element bodyElement = SMessageUtil.getBodyElement(doc);
//
//        bodyElement.removeContent();
//
//        Element machineNameElement = new Element("MACHINENAME");
//        machineNameElement.setText(machinename);
//        bodyElement.addContent(machineNameElement);
//
//        Element communicateState = new Element("COMMUNICATIONSTATE");
//        communicateState.setText(communicatestate);
//        bodyElement.addContent(communicateState);
//
//        return doc;
//    }
//
//    /**
//     * @Name     generatePortAccessModeChangeTemplate
//     * @since    2018. 4. 21.
//     * @author   hhlee
//     * @contents Make Port Access Mode Change Message
//     * @param doc
//     * @param machinename
//     * @param portname
//     * @param porttype
//     * @param portusetype
//     * @param accessmode
//     * @return doc
//     * @throws CustomException
//     */
//    private Document generatePortAccessModeChangeTemplate(Document doc, String machinename,
//                                                     String portname, String porttype, String portusetype,
//                                                                       String accessmode) throws CustomException
//    {
//        SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_PortAccessModeChanged");
//        SMessageUtil.setHeaderItemValue(doc, "EVENTCOMMENT", "ChangeCommState");
//
//        Element bodyElement = SMessageUtil.getBodyElement(doc);
//
//        bodyElement.removeContent();
//
//        Element machineNameElement = new Element("MACHINENAME");
//        machineNameElement.setText(machinename);
//        bodyElement.addContent(machineNameElement);
//
//        Element portNameElement = new Element("PORTNAME");
//        portNameElement.setText(portname);
//        bodyElement.addContent(portNameElement);
//
//        /* 2018.04.02, hhlee, porttype element Add */
//        Element portTypeElement = new Element("PORTTYPE");
//        portTypeElement.setText(porttype);
//        bodyElement.addContent(portTypeElement);
//
//        Element portUseTypeElement = new Element("PORTUSETYPE");
//        portUseTypeElement.setText(portusetype);
//        bodyElement.addContent(portUseTypeElement);
//
//        Element portAccessModeElement = new Element("PORTACCESSMODE");
//        portAccessModeElement.setText(accessmode);
//        bodyElement.addContent(portAccessModeElement);
//
//        return doc;
//    }
//
//    /**
//     * @Name     releaseAllportAccessMode
//     * @since    2018. 4. 23.
//     * @author   hhlee
//     * @contents Release for the Machine/DoubleMainMachieName
//     * @param eventinfo
//     * @param machinename
//     * @param doublemainmachinename
//     * @throws CustomException
//     */
//    private void releaseAllportAccessMode(EventInfo eventinfo, String machinename, String doublemainmachinename)  throws CustomException
//    {
//        String strSql = "SELECT A.MACHINENAME, A.PORTNAME, A.DOUBLEMAINMACHINENAME " +
//                        "  FROM PORT A " +
//                        " WHERE 1=1 " +
//                        "   AND A.MACHINENAME = :MACHINENAME " +
//                        "   AND A.DOUBLEMAINMACHINENAME  = :DOUBLEMAINMACHINENAME ";
//
//        Map<String, Object> bindMap = new HashMap<String, Object>();
//        bindMap.put("MACHINENAME", machinename);
//        bindMap.put("DOUBLEMAINMACHINENAME", doublemainmachinename);
//
//        List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
//
//        if(sqlResult.size() > 0)
//        {
//            String machineName = StringUtil.EMPTY;
//            String portName = StringUtil.EMPTY;
//            for(int i=0; i<sqlResult.size(); i++)
//            {
//                /* Release for the Machine/DoubleMainMachieName. */
//                machineName = sqlResult.get(i).get("MACHINENAME").toString();
//                portName = sqlResult.get(i).get("PORTNAME").toString();
//
//                Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
//                eventinfo.setEventName("ChangeAccessMode");
//
//                MakeAccessModeInfo porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().Port_Auto);
//
//                Map<String, String> portUdfs = portData.getUdfs();
//                portUdfs.put("DOUBLEMAINMACHINENAME", "");
//                portData.setUdfs(portUdfs);
//
//                porttransitionInfo.setUdfs(portUdfs);
//                MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, porttransitionInfo, eventinfo);
//            }
//        }
//        else
//        {
//        }
//    }
//}



///**
// *
// */
//package kr.co.aim.messolution.machine.event;
//
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import kr.co.aim.messolution.generic.GenericServiceProxy;
//import kr.co.aim.messolution.generic.errorHandler.CustomException;
//import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
////import kr.co.aim.messolution.generic.util.CommonUtil;
//import kr.co.aim.messolution.generic.util.EventInfoUtil;
//import kr.co.aim.messolution.generic.util.SMessageUtil;
//import kr.co.aim.messolution.machine.MESMachineServiceProxy;
//import kr.co.aim.messolution.port.MESPortServiceProxy;
//import kr.co.aim.greenframe.greenFrameServiceProxy;
//import kr.co.aim.greentrack.generic.info.EventInfo;
//import kr.co.aim.greentrack.generic.util.StringUtil;
//import kr.co.aim.greentrack.machine.management.data.Machine;
////import kr.co.aim.greentrack.port.management.info.MakeAccessModeInfo;
////import kr.co.aim.greentrack.port.management.info.MakePortStateByStateInfo;
////import kr.co.aim.greentrack.port.management.info.SetEventInfo;
//import kr.co.aim.greentrack.machine.management.info.MakeCommunicationStateInfo;
//import kr.co.aim.greentrack.port.management.data.Port;
//import kr.co.aim.greentrack.port.management.info.MakeAccessModeInfo;
//import kr.co.aim.greentrack.port.management.info.SetEventInfo;
//
//import org.apache.commons.lang.StringUtils;
//import org.jdom.Document;
//import org.jdom.Element;
//
///**
// * @author Administrator
// *
// */
//public class UnitCommunicationStateReply  extends AsyncHandler{
//	@Override
//	public void doWorks(Document doc) throws CustomException {
//
//		/* Copy to FMCSender. Add, hhlee, 20180421 */
//		Document originaldoc = (Document)doc.clone();
//
//		try
//		{
//			EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnitCommunicationStateReport", getEventUser(), getEventComment(), "", "");
//
//			/* Validate Machine */
//			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
//			Machine mainMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
//
//			List<Element> unitList = SMessageUtil.getSubSequenceItemList(SMessageUtil.getBodyElement(doc), "UNITLIST", false);
//			if(unitList != null)
//			{
//				for (Element unitElementE : unitList)
//				{
//					String unitName = SMessageUtil.getChildText(unitElementE, "UNITNAME", true);
//					String unitCommunicationstate = SMessageUtil.getChildText(unitElementE, "COMMUNICATIONSTATE", true);
//
//					if(StringUtil.equals(StringUtil.upperCase(unitCommunicationstate), StringUtil.upperCase(GenericServiceProxy.getConstantMap().Mac_OnLine)))
//					{
//						unitCommunicationstate = GenericServiceProxy.getConstantMap().Mac_OnLine;
//					}
//					else if(StringUtil.equals(StringUtil.upperCase(unitCommunicationstate), StringUtil.upperCase(GenericServiceProxy.getConstantMap().Mac_OnLineLocal)))
//					{
//						unitCommunicationstate = GenericServiceProxy.getConstantMap().Mac_OnLineLocal;
//					}
//					else if(StringUtil.equals(StringUtil.upperCase(unitCommunicationstate), StringUtil.upperCase(GenericServiceProxy.getConstantMap().Mac_OnLineRemote)))
//					{
//						unitCommunicationstate = GenericServiceProxy.getConstantMap().Mac_OnLineRemote;
//					}
//					else
//					{
//						unitCommunicationstate = GenericServiceProxy.getConstantMap().Mac_OffLine;
//					}
//
//					/* Validate UNIT */
//					Machine unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
//
//					//eventInfo.setEventName("ChangeCommState");
//					//MakeCommunicationStateInfo commtransitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeCommunicationStateInfo(unitData, unitCommunicationstate);
//					//MESMachineServiceProxy.getMachineServiceImpl().makeCommunicationState(unitData, commtransitionInfo, eventInfo);
//
//					////159512 by swcho : success then report to FMC
//					//try
//					//{
//					//	/* Make Communication State Change Message */
//					//	originaldoc = generateCommunicationStateChangeTemplate(originaldoc, unitName, unitCommunicationstate);
//					//	GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), originaldoc, "FMCSender");
//					//}
//					//catch(Exception ex)
//					//{
//					//	eventLog.warn("FMC Report Failed!");
//					//}
//
//					//Check Port part 
//			        List<Element> portList = SMessageUtil.getSubSequenceItemList(unitElementE, "PORTLIST", false);
//			        
//			        /* 20190103, hhlee, add, check LINKEDUNIT of PORT Table 
//			         *           Compare portList and port of LINKEDUNIT ==>> */
//			        /* 20190104, hhlee, add, add validation [if(portList != null && portList.size() > 0)] */
//			        if(portList != null && portList.size() > 0)
//			        {
//			            MESPortServiceProxy.getPortServiceUtil().checkLinkedUnitPortInfo(machineName, unitName, portList);
//			        }
//			        /* <<== 20190103, hhlee, add, check LINKEDUNIT of PORT Table */
//			        
//			        if(StringUtil.equals(GenericServiceProxy.getConstantMap().Mac_OffLine, unitCommunicationstate))
//			        {
//			          //changeUnitCommunicationState
//			            eventInfo.setEventName("ChangeUnitCommState");
//
//			            unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);            
//			            
//			            MakeCommunicationStateInfo commtransitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeCommunicationStateInfo(unitData, unitCommunicationstate);
//			        
//			            MESMachineServiceProxy.getMachineServiceImpl().makeCommunicationState(unitData, commtransitionInfo, eventInfo);
//			            
//			            if(portList != null && portList.size() > 0)
//			            {
//			                for(Element elementPort : portList)
//			                {
//			                    String portName = SMessageUtil.getChildText(elementPort, "PORTNAME", false);
//			                    
//			                    if(StringUtil.isNotEmpty(portName))
//			                    {
//			                        Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
//			                                        
//			                        MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
//			                        
//			                        eventInfo.setEventName("ChangeAccessMode");
//			    
//			                        boolean result = false;
//			                        
//			                        if(!StringUtil.equals(portData.getAccessMode(), GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL))
//			                        {
//			                            porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
//			                            result = true;
//			                        }
//			                            
//			                        Map<String, String> portUdfs = portData.getUdfs();
//			                        portUdfs.put("DOUBLEMAINMACHINENAME", unitName);
//			                        portData.setUdfs(portUdfs);
//			                        porttransitionInfo.setUdfs(portUdfs);
//			                        
//			                        //to aviod repetition of AccessMode
//			                        if(result == true)
//			                            MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, porttransitionInfo, eventInfo);   
//			                        else
//			                        {
//			                            SetEventInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(portUdfs);
//			                            MESPortServiceProxy.getPortServiceImpl().setEvent(portData, transitionInfo, eventInfo);
//			                        }
//			                                
//			                        //success then report to FMC
//			                        try
//			                        {
//			                            /* Make Port Access Mode Change Message */
//			                            originaldoc = generatePortAccessModeChangeTemplate(originaldoc, machineName, portName, portData.getUdfs().get("PORTTYPE"),
//			                                                                                  portData.getUdfs().get("PORTUSETYPE"), GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
//			                            GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), originaldoc, "FMCSender");
//			                        }
//			                        catch(Exception ex)
//			                        {
//			                            eventLog.warn("FMC Report Failed!");
//			                        }
//			                    }
//			                }   
//			            }
//			                        
//			            /* 20180717, Add, Unit Offline Check ==>> */
//			            //List<Map<String, Object>> unitDatabyHierarchy = MESMachineServiceProxy.getMachineServiceUtil().getMachineDataByHierarchy(machineName, "2", StringUtil.EMPTY, StringUtil.EMPTY, GenericServiceProxy.getConstantMap().Mac_ProductionMachine);
//			            List<Map<String, Object>> unitDatabyCommunicationState = getMachineDataByCommunicationState(machineName, "2", 
//			                    GenericServiceProxy.getConstantMap().Mac_OnLineRemote, GenericServiceProxy.getConstantMap().Mac_ProductionMachine);
//			            if(unitDatabyCommunicationState == null || unitDatabyCommunicationState.size() <= 0)
//			            {
//			                List<Map<String, Object>> portDataList = searchPortList(machineName,GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);
//			                String resultMachineName = StringUtil.EMPTY;
//			                String resultPortName = StringUtil.EMPTY;
//			                
//			                if(portDataList != null && portDataList.size() > 0)
//			                {
//			                    MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
//			                    //for (Port portData : portDataList)
//			                    for(int i = 0; i < portDataList.size(); i++)
//			                    {
//			                        
//			                        resultMachineName = portDataList.get(i).get("MACHINENAME").toString();
//			                        resultPortName = portDataList.get(i).get("PORTNAME").toString();
//
//			                        Port resultPortData = MESPortServiceProxy.getPortInfoUtil().getPortData(resultMachineName, resultPortName);
//			                        
//			                        porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(resultPortData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
//			                        MESPortServiceProxy.getPortServiceImpl().makeAccessMode(resultPortData, porttransitionInfo, eventInfo); 
//			                        
//			                        /* Make Port Access Mode Change Message */
//			                        originaldoc = generatePortAccessModeChangeTemplate(originaldoc, machineName, resultPortData.getKey().getPortName(), resultPortData.getUdfs().get("PORTTYPE"),
//			                                resultPortData.getUdfs().get("PORTUSETYPE"), GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
//			                        GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), originaldoc, "FMCSender");
//			                    }
//			                }               
//			            }
//			            /* <<== 20180717, Add, Unit Offline Check */
//			        }
//			        else if(StringUtil.equals(GenericServiceProxy.getConstantMap().Mac_OnLineRemote,unitCommunicationstate))
//			        {
//			            boolean isOnlineRemoteUpdate = false;
//			            List<Map<String, Object>> unitDatabyCommunicationState = getMachineDataByCommunicationState(machineName, "2", 
//			                    GenericServiceProxy.getConstantMap().Mac_OnLineRemote, GenericServiceProxy.getConstantMap().Mac_ProductionMachine);
//			            if(unitDatabyCommunicationState == null || unitDatabyCommunicationState.size() <= 0)
//			            {
//			                isOnlineRemoteUpdate = true;
//			            }
//			            
//			            //changeUnitCommunicationState
//			            eventInfo.setEventName("ChangeUnitCommState");
//
//			            unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);            
//			            
//			            MakeCommunicationStateInfo commtransitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeCommunicationStateInfo(unitData, unitCommunicationstate);
//			        
//			            MESMachineServiceProxy.getMachineServiceImpl().makeCommunicationState(unitData, commtransitionInfo, eventInfo);
//			            
//			            if(isOnlineRemoteUpdate)
//			            {
//			                List<Map<String, Object>> portDataList = searchPortList(machineName,GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
//			                String resultMachineName = StringUtil.EMPTY;
//			                String resultPortName = StringUtil.EMPTY;
//			                
//			                if(portDataList != null && portDataList.size() > 0)
//			                {
//			                    MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
//			                    //for (Port portData : portDataList)
//			                    for(int i = 0; i < portDataList.size(); i++)
//			                    {
//			                        
//			                        resultMachineName = portDataList.get(i).get("MACHINENAME").toString();
//			                        resultPortName = portDataList.get(i).get("PORTNAME").toString();
//			    
//			                        Port resultPortData = MESPortServiceProxy.getPortInfoUtil().getPortData(resultMachineName, resultPortName);
//			                        
//			                        porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(resultPortData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);
//			                        MESPortServiceProxy.getPortServiceImpl().makeAccessMode(resultPortData, porttransitionInfo, eventInfo); 
//			                        
//			                        /* Make Port Access Mode Change Message */
//			                        originaldoc = generatePortAccessModeChangeTemplate(originaldoc, machineName, resultPortData.getKey().getPortName(), resultPortData.getUdfs().get("PORTTYPE"),
//			                                resultPortData.getUdfs().get("PORTUSETYPE"), GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);
//			                        GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), originaldoc, "FMCSender");
//			                    }
//			                }
//			            }
//			            
//			            //Release all ports in this unit
//			            List<String> portNameList =null;
//			                        
//			            if(portList.size() > 0)
//			            {
//			                portNameList = new ArrayList<String>();
//			                for(Element elementPort : portList)
//			                {
//			                    String portName = SMessageUtil.getChildText(elementPort, "PORTNAME", false);
//			                    if (StringUtil.isNotEmpty(portName))
//			                    {
//			                        portNameList.add(portName);
//			                    }
//			                }             
//			            }
//			            this.checkReleaseDoubleMainMachinePortAccessMode(eventInfo, machineName, unitName, portNameList, "", originaldoc);
//			        }
//			        
//			        //---------------Send to FMC---------------------
//			        try
//			        {
//			            /* Make Communication State Change Message */
//			            originaldoc = generateCommunicationStateChangeTemplate(originaldoc, unitName, unitCommunicationstate);
//			            GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), originaldoc, "FMCSender");
//			        }
//			        catch(Exception ex)
//			        {
//			            eventLog.warn("FMC Report Failed!");
//			        }
//			        //---------------Send to FMC---------------------
//				}
//			}
//		}
//		catch(CustomException ce)
//		{
//			eventLog.warn(String.format("[%s]%s", ce.errorDef.getErrorCode(), ce.errorDef.getLoc_errorMessage()));
//		}
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
//            if(StringUtil.isNotEmpty(machineLevel))
//            {
//                strSql = strSql  + " AND MS.MACHINETYPE = :MACHINETYPE                                      \n";
//                bindMap.put("MACHINETYPE", machineType);
//            }
//            if(StringUtil.isNotEmpty(machineLevel))
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
//    
//    private List<Map<String, Object>> searchPortList(String machineName, String accessMode) throws CustomException
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
//    //add by wghuang 20180531
//    private void checkReleaseDoubleMainMachinePortAccessMode(EventInfo eventinfo, String machinename, 
//            String doublemainmachinename, List<String> portNameList, String accessMode, Document originaldoc)  throws CustomException
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
//        if(sqlResult.size () > 0)
//        {   
//            String resultMachineName = StringUtil.EMPTY;
//            String resultPortName = StringUtil.EMPTY;
//            boolean isDoubleMainMachineNullUpdate = false;
//                        
//            for(int i = 0; i < sqlResult.size(); i++)
//            {
//                //Release for the Machine/DoubleMainMachieName. 
//                resultMachineName = sqlResult.get(i).get("MACHINENAME").toString();
//                resultPortName =  sqlResult.get(i).get("PORTNAME").toString();
//                
//                isDoubleMainMachineNullUpdate = false;
//                for(String portname : portNameList)
//                {
//                    if (StringUtil.equals(resultPortName,portname))
//                    {
//                        isDoubleMainMachineNullUpdate = true;
//                        break;
//                    }
//                }
//                MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
//                
//                eventinfo.setEventName("ChangeAccessMode");
//                portData = MESPortServiceProxy.getPortInfoUtil().getPortData(resultMachineName, resultPortName);                 
//                
//                if(!isDoubleMainMachineNullUpdate)
//                {                                       
//                    porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);                    
//                }
//                else
//                {     
//                    //porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
//                    porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, portData.getAccessMode());
//                }
//                
//                
//                Map<String, String> portUdfs = portData.getUdfs();
//                portUdfs.put("DOUBLEMAINMACHINENAME", StringUtil.EMPTY);
//                portData.setUdfs(portUdfs);
//                porttransitionInfo.setUdfs(portUdfs);
//                
//                if(!StringUtils.equals(portData.getAccessMode(), porttransitionInfo.getAccessMode()))
//                {
//                    MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, porttransitionInfo, eventinfo);
//                }
//                else
//                {
//                    SetEventInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(portUdfs);
//                    MESPortServiceProxy.getPortServiceImpl().setEvent(portData, transitionInfo, eventinfo);
//                }
//                //success then report to FMC
//                try
//                {
//                    /* Make Port Access Mode Change Message */
//                    originaldoc = generatePortAccessModeChangeTemplate(originaldoc, resultMachineName, resultPortName, portData.getUdfs().get("PORTTYPE"),
//                                                                          portData.getUdfs().get("PORTUSETYPE"), GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);
//                    GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), originaldoc, "FMCSender");
//                }
//                catch(Exception ex)
//                {
//                    eventLog.warn("FMC Report Failed!");
//                }
//            }
//        }
//        else
//        {          
//        }
//        
//    }
//}
//
//
//
//
//
//
////public class UnitCommunicationStateReply  extends AsyncHandler{
////    @Override
////    public void doWorks(Document doc) throws CustomException {
////
////        /* Copy to FMCSender. Add, hhlee, 20180421 */
////        Document originaldoc = (Document)doc.clone();
////
////        try
////        {
////            EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnitCommunicationStateReport", getEventUser(), getEventComment(), "", "");
////
////            /* Validate Machine */
////            String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
////            Machine mainMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
////
////            List<Element> unitList = SMessageUtil.getSubSequenceItemList(SMessageUtil.getBodyElement(doc), "UNITLIST", false);
////            if(unitList != null)
////            {
////                for (Element unitElementE : unitList)
////                {
////                    String unitName = SMessageUtil.getChildText(unitElementE, "UNITNAME", true);
////                    String unitCommunicationstate = SMessageUtil.getChildText(unitElementE, "COMMUNICATIONSTATE", true);
////
////                    if(StringUtil.equals(StringUtil.upperCase(unitCommunicationstate), StringUtil.upperCase(GenericServiceProxy.getConstantMap().Mac_OnLine)))
////                    {
////                        unitCommunicationstate = GenericServiceProxy.getConstantMap().Mac_OnLine;
////                    }
////                    else if(StringUtil.equals(StringUtil.upperCase(unitCommunicationstate), StringUtil.upperCase(GenericServiceProxy.getConstantMap().Mac_OnLineLocal)))
////                    {
////                        unitCommunicationstate = GenericServiceProxy.getConstantMap().Mac_OnLineLocal;
////                    }
////                    else if(StringUtil.equals(StringUtil.upperCase(unitCommunicationstate), StringUtil.upperCase(GenericServiceProxy.getConstantMap().Mac_OnLineRemote)))
////                    {
////                        unitCommunicationstate = GenericServiceProxy.getConstantMap().Mac_OnLineRemote;
////                    }
////                    else
////                    {
////                        unitCommunicationstate = GenericServiceProxy.getConstantMap().Mac_OffLine;
////                    }
////
////                    /* Validate UNIT */
////                    Machine unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
////
////                    eventInfo.setEventName("ChangeCommState");
////                    MakeCommunicationStateInfo commtransitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeCommunicationStateInfo(unitData, unitCommunicationstate);
////                    MESMachineServiceProxy.getMachineServiceImpl().makeCommunicationState(unitData, commtransitionInfo, eventInfo);
////
////                    //159512 by swcho : success then report to FMC
////                    try
////                    {
////                        /* Make Communication State Change Message */
////                        originaldoc = generateCommunicationStateChangeTemplate(originaldoc, unitName, unitCommunicationstate);
////                        GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), originaldoc, "FMCSender");
////                    }
////                    catch(Exception ex)
////                    {
////                        eventLog.warn("FMC Report Failed!");
////                    }
////
////                    List<Element> portList = SMessageUtil.getSubSequenceItemList(unitElementE, "PORTLIST", false);
////                    if(portList != null)
////                    {
////                        /* DoubleMainMachineName("")/portAccessMode("Auto") State all release */
////                        releaseAllportAccessMode(eventInfo, machineName, unitName);
////
////                        for (Element portElementE : portList)
////                        {
////                            /* Validate Port */
////                            String portName = SMessageUtil.getChildText(portElementE, "PORTNAME", true);
////                            Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
////
////                            String portAccessMode = StringUtil.EMPTY;
////
////                            if(StringUtil.equals(unitCommunicationstate,GenericServiceProxy.getConstantMap().Mac_OffLine))
////                            {
////                                portAccessMode = GenericServiceProxy.getConstantMap().Port_Manual;
////                            }
////                            else
////                            {
////                                portAccessMode = GenericServiceProxy.getConstantMap().Port_Auto;
////                            }
////
////                            eventInfo.setEventName("ChangeAccessMode");
////
////                            MakeAccessModeInfo porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, portAccessMode);
////
////                            Map<String, String> portUdfs = portData.getUdfs();
////                            portUdfs.put("DOUBLEMAINMACHINENAME", unitName);
////                            portData.setUdfs(portUdfs);
////
////                            porttransitionInfo.setUdfs(portUdfs);
////                            MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, porttransitionInfo, eventInfo);
////
////                            //159512 by swcho : success then report to FMC
////                            try
////                            {
////                                /* Make Port Access Mode Change Message */
////                                originaldoc = generatePortAccessModeChangeTemplate(originaldoc, machineName, portName, portData.getUdfs().get("PORTTYPE"),
////                                                                                      portData.getUdfs().get("PORTUSETYPE"), portAccessMode);
////                                GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), originaldoc, "FMCSender");
////                            }
////                            catch(Exception ex)
////                            {
////                                eventLog.warn("FMC Report Failed!");
////                            }
////
////                        }
////                    }
////                }
////            }
////        }
////        catch(CustomException ce)
////        {
////            eventLog.warn(String.format("[%s]%s", ce.errorDef.getErrorCode(), ce.errorDef.getLoc_errorMessage()));
////        }
////    }
////
////    /**
////     * @Name     generateCommunicationStateChangeTemplate
////     * @since    2018. 4. 21.
////     * @author   hhlee
////     * @contents Make Communication State Change Message
////     * @param doc
////     * @param machinename
////     * @param communicatestate
////     * @return doc
////     * @throws CustomException
////     */
////    private Document generateCommunicationStateChangeTemplate(Document doc, String machinename,
////                                                                       String communicatestate ) throws CustomException
////    {
////        SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_CommunicationStateChanged");
////        SMessageUtil.setHeaderItemValue(doc, "EVENTCOMMENT", "ChangeAccessMode");
////
////
////        Element bodyElement = SMessageUtil.getBodyElement(doc);
////
////        bodyElement.removeContent();
////
////        Element machineNameElement = new Element("MACHINENAME");
////        machineNameElement.setText(machinename);
////        bodyElement.addContent(machineNameElement);
////
////        Element communicateState = new Element("COMMUNICATIONSTATE");
////        communicateState.setText(communicatestate);
////        bodyElement.addContent(communicateState);
////
////        return doc;
////    }
////
////    /**
////     * @Name     generatePortAccessModeChangeTemplate
////     * @since    2018. 4. 21.
////     * @author   hhlee
////     * @contents Make Port Access Mode Change Message
////     * @param doc
////     * @param machinename
////     * @param portname
////     * @param porttype
////     * @param portusetype
////     * @param accessmode
////     * @return doc
////     * @throws CustomException
////     */
////    private Document generatePortAccessModeChangeTemplate(Document doc, String machinename,
////                                                     String portname, String porttype, String portusetype,
////                                                                       String accessmode) throws CustomException
////    {
////        SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_PortAccessModeChanged");
////        SMessageUtil.setHeaderItemValue(doc, "EVENTCOMMENT", "ChangeCommState");
////
////        Element bodyElement = SMessageUtil.getBodyElement(doc);
////
////        bodyElement.removeContent();
////
////        Element machineNameElement = new Element("MACHINENAME");
////        machineNameElement.setText(machinename);
////        bodyElement.addContent(machineNameElement);
////
////        Element portNameElement = new Element("PORTNAME");
////        portNameElement.setText(portname);
////        bodyElement.addContent(portNameElement);
////
////        /* 2018.04.02, hhlee, porttype element Add */
////        Element portTypeElement = new Element("PORTTYPE");
////        portTypeElement.setText(porttype);
////        bodyElement.addContent(portTypeElement);
////
////        Element portUseTypeElement = new Element("PORTUSETYPE");
////        portUseTypeElement.setText(portusetype);
////        bodyElement.addContent(portUseTypeElement);
////
////        Element portAccessModeElement = new Element("PORTACCESSMODE");
////        portAccessModeElement.setText(accessmode);
////        bodyElement.addContent(portAccessModeElement);
////
////        return doc;
////    }
////
////    /**
////     * @Name     releaseAllportAccessMode
////     * @since    2018. 4. 23.
////     * @author   hhlee
////     * @contents Release for the Machine/DoubleMainMachieName
////     * @param eventinfo
////     * @param machinename
////     * @param doublemainmachinename
////     * @throws CustomException
////     */
////    private void releaseAllportAccessMode(EventInfo eventinfo, String machinename, String doublemainmachinename)  throws CustomException
////    {
////        String strSql = "SELECT A.MACHINENAME, A.PORTNAME, A.DOUBLEMAINMACHINENAME " +
////                        "  FROM PORT A " +
////                        " WHERE 1=1 " +
////                        "   AND A.MACHINENAME = :MACHINENAME " +
////                        "   AND A.DOUBLEMAINMACHINENAME  = :DOUBLEMAINMACHINENAME ";
////
////        Map<String, Object> bindMap = new HashMap<String, Object>();
////        bindMap.put("MACHINENAME", machinename);
////        bindMap.put("DOUBLEMAINMACHINENAME", doublemainmachinename);
////
////        List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
////
////        if(sqlResult.size() > 0)
////        {
////            String machineName = StringUtil.EMPTY;
////            String portName = StringUtil.EMPTY;
////            for(int i=0; i<sqlResult.size(); i++)
////            {
////                /* Release for the Machine/DoubleMainMachieName. */
////                machineName = sqlResult.get(i).get("MACHINENAME").toString();
////                portName = sqlResult.get(i).get("PORTNAME").toString();
////
////                Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
////                eventinfo.setEventName("ChangeAccessMode");
////
////                MakeAccessModeInfo porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().Port_Auto);
////
////                Map<String, String> portUdfs = portData.getUdfs();
////                portUdfs.put("DOUBLEMAINMACHINENAME", "");
////                portData.setUdfs(portUdfs);
////
////                porttransitionInfo.setUdfs(portUdfs);
////                MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, porttransitionInfo, eventinfo);
////            }
////        }
////        else
////        {
////        }
////    }
////}
