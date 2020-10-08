package kr.co.aim.messolution.port.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.data.PortKey;
import kr.co.aim.greentrack.port.management.data.PortSpec;
import kr.co.aim.greentrack.port.management.data.PortSpecKey;
import kr.co.aim.greentrack.port.management.info.MakeAccessModeInfo;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * @author gksong
 * @date   2009.02.25
 */

public class PortServiceUtil implements ApplicationContextAware
{
	/**
	 * @uml.property  name="applicationContext"
	 * @uml.associationEnd  
	 */
	private ApplicationContext		applicationContext;
	private static Log 				log = LogFactory.getLog(PortServiceImpl.class); 
	
	/**
	 * @param arg0
	 * @throws BeansException
	 * @uml.property  name="applicationContext"
	 */
	@Override
    public void setApplicationContext( ApplicationContext arg0 ) throws BeansException
	{
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}

	/*
	* Name : getPortData
	* Desc : This function is getPortData
	* Author : AIM Systems, Inc
	* Date : 2011.01.10
	*/
	public Port getPortData(String machineName, String portName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		try{
			if(log.isInfoEnabled()){
				log.info("machineName = " + machineName);
				log.info("portName = " + portName);
			}
			PortKey portKey = new PortKey();
			portKey.setMachineName(machineName);
			portKey.setPortName(portName);
			
			Port portData = null;
			portData = PortServiceProxy.getPortService().selectByKey(portKey);
	
			return portData;
		} catch ( Exception e ){
			// Modified by smkang on 2018.05.02 - PORT-9000 error needs 2 arguments.
//			throw new CustomException("PORT-9000", portName);
			throw new CustomException("PORT-9000", machineName, portName);
		}
	}

	/*
	* Name : getPortSpecInfo
	* Desc : This function is getPortSpecInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.10
	*/
	public  PortSpec getPortSpecInfo ( String machineName, String portName ) throws CustomException{
		
		try
		{
		PortSpecKey portSpecKey = new PortSpecKey();
		portSpecKey.setMachineName(machineName);
		portSpecKey.setPortName(portName);
		
		PortSpec portSpecData = null;
		
		portSpecData = PortServiceProxy.getPortSpecService().selectByKey(portSpecKey);
		
		return portSpecData;
		}
		catch (Exception e) {
			throw new CustomException("PORT-9000",portName);
		}
	}
	
	/** MaskloadComplete
	 * @author LHKIM
	 * @since 2014.11.11
	 * @param eventInfo
	 * @param machineName
	 * @param portName
	 * @param portuseType
	 * @param portType
	 * @throws CustomException
	 */
	public SetEventInfo MaskloadComplete(EventInfo eventInfo, String machineName, String portName, String portuseType, String portType)
		throws CustomException
	{
		Port portData = CommonUtil.getPortInfo(machineName, portName);
		
		eventInfo = EventInfoUtil.makeEventInfo("ChangeTransferState", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
		
		MakeTransferStateInfo makeTransferStateInfo = new MakeTransferStateInfo();
		makeTransferStateInfo.setTransferState( GenericServiceProxy.getConstantMap().Port_ReadyToProcess );
		makeTransferStateInfo.setValidateEventFlag( "N" );
		MESPortServiceProxy.getPortServiceImpl().makeTransferState(portData, makeTransferStateInfo, eventInfo);
		
		//Put UDF
		Map<String,String> udfs = portData.getUdfs();
		udfs.put("FULLSTATE", GenericServiceProxy.getConstantMap().Port_FULL);
		udfs.put("PORTTYPE",portType);
		udfs.put("PORTUSETYPE",portuseType);
		
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs(udfs);
		
		return setEventInfo;
		
		
	}
	
	/** MaskUnloadComplete
	 * @author LHKIM
	 * @since 2014.11.11
	 * @param eventInfo
	 * @param machineName
	 * @param portName
	 * @param portType
	 * @throws CustomException
	 */
	public SetEventInfo MaskUnloadComplete(EventInfo eventInfo, String machineName, String portName,String portType)
		throws CustomException
	{
		Port portData = CommonUtil.getPortInfo(machineName, portName);
		
		eventInfo = EventInfoUtil.makeEventInfo("ChangeTransferState", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
		
		MakeTransferStateInfo makeTransferStateInfo = new MakeTransferStateInfo();
		makeTransferStateInfo.setTransferState( GenericServiceProxy.getConstantMap().Port_ReadyToUnload );
		makeTransferStateInfo.setValidateEventFlag( "N" );
		MESPortServiceProxy.getPortServiceImpl().makeTransferState(portData, makeTransferStateInfo, eventInfo);
		
		//Put UDF
		Map<String,String> udfs = portData.getUdfs();
		udfs.put("FULLSTATE", GenericServiceProxy.getConstantMap().Port_EMPTY);
		udfs.put("PORTNAME",portName);
		udfs.put("PORTTYPE",portType);
		
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs(udfs);
		
		return setEventInfo;
	}
	
	public Port searchLoaderPort(String machineName) throws CustomException
	{
		try
		{
			List<Port> result = PortServiceProxy.getPortService().select("machineName = ? AND portType IN (?, ?, ?)", new Object[] {machineName, "PB", "PL", "PS"});
			
			return result.get(0);
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("PORT-9001", machineName, "");
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("PORT-9999", fe.getMessage());
		}
	}
	
	public Port searchUnloaderPort(Port portData) throws CustomException
	{
		if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PB"))
		{
			return portData;
		}
		else
		{
			try
			{
				List<Port> result = PortServiceProxy.getPortService().select("machineName = ? AND portType IN (?, ?)", new Object[] {portData.getKey().getMachineName(), "PU", "PS"});
				
				return result.get(0);
			}
			catch (NotFoundSignal ne)
			{
				throw new CustomException("PORT-9001", portData.getKey().getMachineName(), "");
			}
			catch (FrameworkErrorSignal fe)
			{
				throw new CustomException("PORT-9999", fe.getMessage());
			}
		}
	}
	
    //add by wghuang 20181116
    public void checkPortStateModelName(PortSpec portSpecData,String portName) throws CustomException
    {
    	//checkPortStateModelName
        if(StringUtil.isEmpty(portSpecData.getPortStateModelName()))
        {
        	throw new CustomException("PORT-0011", portName);
        }
    }
	
    /**
     * 
     * @Name     checkLinkedUnitPortInfo
     * @since    2019. 1. 3.
     * @author   hhlee
     * @contents check LINKEDUNIT of PORT Table
     *           Compare portList and port of LINKEDUNIT
     *           
     * @param machineName
     * @param unitName
     * @param portList
     */
    public void checkLinkedUnitPortInfo(String machineName, String unitName,  List<Element> portList) throws CustomException
    {
        log.info("Check MachineOperationMode");   
        
        List<ListOrderedMap> operationModeDataList = null;
        boolean isExistINDPMode = false;
        String strSql = StringUtil.EMPTY;
        
        /* 1. Machine Operation Mode Check */
        try
        {
            strSql = "SELECT CT.MACHINENAME, CT.OPERATIONMODE     \n"
                   +  "  FROM CT_OPERATIONMODE CT                 \n"
                   +  " WHERE     1 = 1                           \n"
                   +  "       AND CT.MACHINENAME = :MACHINENAME   \n"
                   +  "       AND CT.OPERATIONMODE = :OPERATIONMODE " ;

            Map<String, Object> bindMap = new HashMap<String, Object>();
            bindMap.put("MACHINENAME", machineName);
            bindMap.put("OPERATIONMODE", GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP);

            operationModeDataList = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindMap);

            if (operationModeDataList != null && operationModeDataList.size() > 0)
            {
                isExistINDPMode = true;
            }
            
        }
        catch (Exception ex)
        {
            log.warn("Not Exist MachineOperationMode.!!");
            isExistINDPMode = false;
        }
        
        /* 2. LinkedUint Check */
        List<ListOrderedMap> linkedUnitPortDataList = null;
        boolean isExistLinkedUnit = false;
        try
        {
            strSql = " SELECT P.PORTNAME                        \n"
                   + "  FROM PORT P                             \n"
                   + " WHERE 1=1                                \n"
                   + "   AND P.MACHINENAME = :MACHINENAME \n"
                   + "   AND P.LINKEDUNITNAME = :LINKEDUNITNAME \n"
                   + " ORDER BY TO_NUMBER(P.PORTNAME)             ";
                    
             Map<String, Object> bindMap = new HashMap<String, Object>();
             bindMap.put("MACHINENAME", machineName);
             bindMap.put("LINKEDUNITNAME", unitName);
             
             linkedUnitPortDataList = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindMap);
             
             if (linkedUnitPortDataList != null && linkedUnitPortDataList.size() > 0)
             {
                 isExistLinkedUnit = true;
             }    
        }
        catch (Exception ex)
        {
            log.warn("Not Exist LinkedUnit.!!");
            isExistLinkedUnit = false;
        }
        
        if(isExistINDPMode && !isExistLinkedUnit)
        {
            log.warn(String.format("The unit[Machine:%s, UnitName:%s] was not allocated by the port in INDP mode.", machineName, unitName));
            throw new CustomException("PORT-9007",machineName, unitName);
        }
        
        /* 3. portList and portinfo of LinkedUnit Check */
        String unitPortName = StringUtil.EMPTY;
        String wrongPortName = StringUtil.EMPTY;
        
        String linkedUnitPortName  = StringUtil.EMPTY;
        String operationMode  = StringUtil.EMPTY;
        boolean isExist = false;
        boolean isFirst = true;
        if(portList != null && portList.size() > 0)
        {
            String portName = StringUtil.EMPTY;
            for(Element elementPort : portList)
            {
                portName = SMessageUtil.getChildText(elementPort, "PORTNAME", false);
                isExist = false;
                if (linkedUnitPortDataList != null && linkedUnitPortDataList.size() > 0)
                {
                    for(ListOrderedMap linkedUnitPortData : linkedUnitPortDataList)
                    {
                        linkedUnitPortName = CommonUtil.getValue(linkedUnitPortData, "PORTNAME");
                        
                        if(StringUtil.equals(linkedUnitPortName, portName))
                        {
                            isExist = true;
                            //break; /* 20190111, hhlee, modify */
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
        
        log.info("Check MachineOperationMode");   
    }
    
    /* ============================= */
    /* 20190107, hhlee, add function */
    /* ============================= */
    /**
     * 
     * @Name     validateOperationModeChangedNormalToIndp
     * @since    2019. 1. 10.
     * @author   hhlee
     * @contents 
     *           
     * @param machineName
     * @param unitName
     * @throws CustomException
     */
    public void validateOperationModeChangedNormalToIndp(String machineName, String unitName) throws CustomException
    {
        List<Map<String, Object>> linkedUnitNameList = this.linkedUnitNamePortAccessModeListIndp(machineName, unitName, 
                GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
        
        if(linkedUnitNameList != null && linkedUnitNameList.size() > 0)
        {        
            String linkedUnitName = StringUtil.EMPTY;
            
            for(int i = 0; i < linkedUnitNameList.size(); i++)
            {
                if(!StringUtil.equals(linkedUnitNameList.get(i).get("LINKEDUNITNAME").toString(), 
                        linkedUnitNameList.get(i).get("DOUBLEMAINMACHINENAME").toString()))
                {
                    log.warn(String.format("LINKEDUNITNAME[%s] and DOUBLEMAINMACHINENAME[%s] do not match in INDP mode.!!",
                            linkedUnitNameList.get(i).get("LINKEDUNITNAME").toString(), 
                            linkedUnitNameList.get(i).get("DOUBLEMAINMACHINENAME").toString()));
                    
                    throw new CustomException("MACHINE-9005",linkedUnitNameList.get(i).get("LINKEDUNITNAME").toString(), 
                            linkedUnitNameList.get(i).get("DOUBLEMAINMACHINENAME").toString());
                }
            }
        }  
    }
    
    public void  updateAccessModeByOperationModeChange(EventInfo eventInfo, String machineName, String machineOperationMode) throws CustomException
    {
        if(StringUtil.equals(machineOperationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
        {
            List<Map<String, Object>> unitDataByProductMachine = getMachineDataByCommunicationState(machineName, "2", 
                  StringUtil.EMPTY, GenericServiceProxy.getConstantMap().Mac_ProductionMachine);
            
            if(unitDataByProductMachine != null || unitDataByProductMachine.size() > 0)
            {
                for(int i = 0; i < unitDataByProductMachine.size(); i++)
                {
                    this.updateAccessModeByValidateDoubleMainMachineIndp(eventInfo, machineName, unitDataByProductMachine.get(i).get("UNITNAME").toString()); 
                }
            }            
        }
        else
        {
            this.updateAccessModeByValidateDoubleMainMachineNormal(eventInfo, machineName);   
        }
    }
    
    /**
     * 
     * @Name     unitCommunicationStateChangeByNormal
     * @since    2019. 1. 10.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param machineName
     * @param unitName
     * @param portList
     * @throws CustomException
     */
    public void unitCommunicationStateChangeByNormal(EventInfo eventInfo, String machineName, String unitName, List<Element> portList) throws CustomException
    {
        //Machine mainMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
        //Machine unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
        
        /* 0. Validate portList */
        if(portList != null && portList.size() > 0)
        {
            /* 1. clear DoubleManinMachine */
            /* 20190111, hhlee, modify, add parameter(OperationMode) */
            //this.clearDoubleMainMachine(eventInfo, machineName, unitName, portList);
            this.clearDoubleMainMachine(eventInfo, machineName, unitName, portList, GenericServiceProxy.getConstantMap().OPERATIONMODE_NORMAL);
            
            /* 2. set DoubleMainMachine and PoetAccessMOde */
            this.setDoubleMainMachineAccessModeNormalIndp(eventInfo, machineName, unitName, portList);
            
            /* 3. Machine - Unit : check All Production Unit OffLine */
            this.updateAccessModeByValidateDoubleMainMachineNormal(eventInfo, machineName);      
        }
        else
        {
            /* 4. portList == 0, AccessMode = Auto, DoubleMainMachineName = "",  */
            this.portAccessModeAllAutoAndDoubleMainMachineIsNull(eventInfo, machineName, unitName, GenericServiceProxy.getConstantMap().OPERATIONMODE_NORMAL);            
        }               
    }
    
    /**
     * 
     * @Name     unitCommunicationStateChangeByIndp
     * @since    2019. 1. 4.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param machineName
     * @param unitName
     * @param machinePortList
     * @param portList
     * @throws CustomException
     */
    public void unitCommunicationStateChangeByIndp(EventInfo eventInfo, String machineName, String unitName, List<Element> portList) throws CustomException
    {        
        //Machine mainMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
        //Machine unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
        
        /* 0. Validate portList */
        if(portList != null && portList.size() > 0)
        {
            /* 1. clear DoubleManinMachine */
            /* 20190111, hhlee, modify, add parameter(OperationMode) */
            //this.clearDoubleMainMachine(eventInfo, machineName, unitName, portList);
            this.clearDoubleMainMachine(eventInfo, machineName, unitName, portList,  GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP);
            
            /* 2. set DoubleMainMachine and PoetAccessMOde */
            this.setDoubleMainMachineAccessModeNormalIndp(eventInfo, machineName, unitName, portList);
            
            /* 3. Machine - Unit : check All Production Unit OffLine */
            this.updateAccessModeByValidateDoubleMainMachineIndp(eventInfo, machineName, unitName);     
        }
        else
        {
            /* 4. portList == 0, AccessMode = Auto, DoubleMainMachineName = "",  */
            this.portAccessModeAllAutoAndDoubleMainMachineIsNull(eventInfo, machineName, unitName, GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP);            
        }
        
    }  
    
    /**
     * 
     * @Name     clearDoubleMainMachine
     * @since    2019. 1. 5.
     * @author   hhlee
     * @contents 
     *           
     * @param machineName
     * @param unitName
     */
    private void clearDoubleMainMachine(EventInfo eventInfo, String machineName, String unitName, List<Element> elementPortList, String operationMode) throws CustomException
    {
        String portName = StringUtil.EMPTY;
        String elementPortName = StringUtil.EMPTY;
        boolean isAccessModeDuplicate = false;
        boolean isExistDoubleMachineName = false;
        try
        {
            List<Port> portDoubleMainMachineDataList = PortServiceProxy.getPortService().select(" WHERE MACHINENAME = ? AND DOUBLEMAINMACHINENAME = ? ORDER BY TO_NUMBER(PORTNAME) ", 
                    new Object[] {machineName, unitName});  
            
            if(portDoubleMainMachineDataList != null && portDoubleMainMachineDataList.size() > 0)
            {
                for(Port portDoubleMainMachineData : portDoubleMainMachineDataList)
                {        
                    portName = portDoubleMainMachineData.getKey().getPortName();
                    isExistDoubleMachineName = false;
                    
                    for(Element elementPort : elementPortList)
                    {
                        elementPortName = SMessageUtil.getChildText(elementPort, "PORTNAME", false);
                        if(StringUtil.equals(portName, elementPortName))
                        {
                            isExistDoubleMachineName = true;
                            break;
                        }                                          
                    }
                    
                    /* 20190111, hhlee, Modify, Change Logic ==>> */
                    if(!isExistDoubleMachineName)
                    {
                        if(!StringUtil.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
                        {
                            /* Check Port AccessMode Duplicate */
                            isAccessModeDuplicate = false;
                            if(StringUtil.equals(portDoubleMainMachineData.getAccessMode(), 
                                    GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO))
                            {                    
                                isAccessModeDuplicate = true;
                            }
                            
                            eventInfo.setEventName("ChangeAccessMode");
                            
                            MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
                            porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portDoubleMainMachineData, 
                                    GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);
                            
                            Map<String, String> portUdfs = portDoubleMainMachineData.getUdfs();
                            portUdfs.put("DOUBLEMAINMACHINENAME", StringUtil.EMPTY);
                            portDoubleMainMachineData.setUdfs(portUdfs);
                            porttransitionInfo.setUdfs(portUdfs);                        
                            
                            if(!isAccessModeDuplicate)
                            {
                                MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portDoubleMainMachineData, porttransitionInfo, eventInfo); 
                            }
                            else
                            {
                                SetEventInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(portUdfs);
                                MESPortServiceProxy.getPortServiceImpl().setEvent(portDoubleMainMachineData, transitionInfo, eventInfo);
                            }
                        }
                        else
                        {
                            eventInfo.setEventName("ChangeUnitCommState");
                            
                            Map<String, String> portUdfs = portDoubleMainMachineData.getUdfs();
                            portUdfs.put("DOUBLEMAINMACHINENAME", StringUtil.EMPTY);
                            portDoubleMainMachineData.setUdfs(portUdfs);
                            
                            SetEventInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(portUdfs);
                            MESPortServiceProxy.getPortServiceImpl().setEvent(portDoubleMainMachineData, transitionInfo, eventInfo);
                        }
                    }
                    /* <<== 20190111, hhlee, Modify, Change Logic */
                }                
            }
        }
        catch(NotFoundSignal ne)
        {    
            log.warn(String.format("DoubleMainMachine is not found.!![MachineName:%s, DoubleMainMachineName:%s]", machineName, unitName));
        }
        catch (Exception ex)
        {
            log.warn(String.format("DoubleMainMachine is not found.!![MachineName:%s, DoubleMainMachineName:%s]", machineName, unitName));
        }
        
    }
    
    /**
     * 
     * @Name     setDoubleMainMachineAccessModeNormalIndp
     * @since    2019. 1. 7.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param machineName
     * @param unitName
     * @param elementPortList
     * @throws CustomException
     */
    private void setDoubleMainMachineAccessModeNormalIndp(EventInfo eventInfo, String machineName, String unitName, List<Element> elementPortList) throws CustomException
    {
        String portName = StringUtil.EMPTY;
        boolean isAccessModeDuplicate = false;
        boolean isExistDoubleMainMachine = false;
        for(Element elementPort : elementPortList)
        {
            try
            {
                portName = SMessageUtil.getChildText(elementPort, "PORTNAME", false);
                Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
                                
                isExistDoubleMainMachine = false;
                if(StringUtil.equals(CommonUtil.getValue(portData.getUdfs(), "DOUBLEMAINMACHINENAME"), unitName))
                {
                    isExistDoubleMainMachine = true;
                }
                
                MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
                
                eventInfo.setEventName("ChangeAccessMode");
                
                porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
                
                Map<String, String> portUdfs = portData.getUdfs();
                portUdfs.put("DOUBLEMAINMACHINENAME", unitName);
                portData.setUdfs(portUdfs);
                porttransitionInfo.setUdfs(portUdfs);
                
                /* Check Port AccessMode Duplicate */
                isAccessModeDuplicate = false;
                if(StringUtil.equals(portData.getAccessMode(), 
                        GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL))
                {                    
                    isAccessModeDuplicate = true;
                }
                
                /* To aviod repetition of AccessMode */
                if(!isAccessModeDuplicate)
                {
                    MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, porttransitionInfo, eventInfo); 
                }
                else
                {
                    if(!isExistDoubleMainMachine)
                    {
                        SetEventInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(portUdfs);
                        MESPortServiceProxy.getPortServiceImpl().setEvent(portData, transitionInfo, eventInfo);
                    }
                    else
                    {
                        log.info(String.format("There is no change.[AccessMode:Old(%s), New(%s)], [DoubleMainMachine: Old(%s), New(%s)]", 
                                portData.getAccessMode(), GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL, 
                                CommonUtil.getValue(portData.getUdfs(), "DOUBLEMAINMACHINENAME"),unitName));
                    }
                }
            }
            catch (Exception ex)
            {
                log.warn(String.format("Set DoubleMainMachine AccessMode Normal update fail.!![MachineName:%s, UnitName:%s]", machineName, unitName));
            }
        }        
    }
    
    /**
     * 
     * @Name     updateAccessModeByValidateDoubleMainMachineIndp
     * @since    2019. 1. 7.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param machineName
     * @param unitName
     * @throws CustomException
     */
    private void updateAccessModeByValidateDoubleMainMachineIndp(EventInfo eventInfo, String machineName, String unitName) throws CustomException
    {
        
        /* 20190111, hhlee, add, validate DoubleMainMachine and LinkedUnitName ==>> */        
        List<Map<String, Object>> misMatchlinkedUnitNameList = this.validatelinkedUnitNameDoubleMainMachineIndp(machineName, unitName);
        if(misMatchlinkedUnitNameList != null && misMatchlinkedUnitNameList.size() > 0)
        {
            throw new CustomException("PORT-9009", misMatchlinkedUnitNameList.get(0).get("LINKEDUNITNAME").toString(), 
                                                   misMatchlinkedUnitNameList.get(0).get("DOUBLEMAINMACHINENAME").toString(), 
                                                   misMatchlinkedUnitNameList.get(0).get("PORTNAME"), unitName);
        }        
        /* <<== 20190111, hhlee, add, validate DoubleMainMachine and LinkedUnitName */
        
        List<Map<String, Object>> linkedUnitNameList = this.linkedUnitNamePortAccessModeListIndp(machineName, unitName, 
                GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
        
        if(linkedUnitNameList != null && linkedUnitNameList.size() > 0)
        {   
            String linkedUnitName = StringUtil.EMPTY;
            try
            {                
                List<Port> portList = PortServiceProxy.getPortService().select(" MACHINENAME = ? AND LINKEDUNITNAME = ? AND ACCESSMODE = ? ", 
                        new Object[] {machineName, unitName, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO});  
                
                for(Port portData : portList)
                {
                    eventInfo.setCheckTimekeyValidation(false);
                    //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                    eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
                    
                    MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
                    porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
                    MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, porttransitionInfo, eventInfo);
                }               
            }
            catch(NotFoundSignal ne)
            {    
                //log.warn(ne.getStackTrace());
                log.warn(String.format("updateAccessModeByValidateDoubleMainMachineIndp is not found.!![MachineName:%s, LINKEDUNITNAME:%s, ACCESSMODE:%s]",
                        machineName, linkedUnitName, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO));
            }
            catch(Exception ex)
            {
                //log.warn(ex.getStackTrace());
                log.warn(String.format("updateAccessModeByValidateDoubleMainMachineIndp is not found.!![MachineName:%s, LINKEDUNITNAME:%s, ACCESSMODE:%s]", 
                        machineName, linkedUnitName, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO));
            } 
        }  
    }
    
    /**
     * 
     * @Name     updateAccessModeByValidateDoubleMainMachineNormal
     * @since    2019. 1. 7.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param machineName
     * @throws CustomException
     */
    private void updateAccessModeByValidateDoubleMainMachineNormal(EventInfo eventInfo, String machineName) throws CustomException
    {
        List<Map<String, Object>> dubleMachineNameList = this.dubleMachineNamePortAccessModeListNormal(machineName, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
        
        List<Map<String, Object>> prounitList = getMachineDataByCommunicationState(machineName, "2", StringUtil.EMPTY, GenericServiceProxy.getConstantMap().Mac_ProductionMachine);
        
        if(dubleMachineNameList != null && prounitList != null)
        {
            if(dubleMachineNameList.size() == prounitList.size())
            {
                try
                {
                    List<Port> portList = PortServiceProxy.getPortService().select(" MACHINENAME = ? AND ACCESSMODE = ? ", new Object[] {machineName, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO});  
                
                    for(Port portData : portList)
                    {
                        eventInfo.setCheckTimekeyValidation(false);
                        //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
                        
                        MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
                        porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL);
                        MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, porttransitionInfo, eventInfo);
                    }
                }
                catch(NotFoundSignal ne)
                {    
                    log.warn(String.format("updateAccessModeByValidateDoubleMainMachineNormal is not found.!![MachineName:%s, ACCESSMODE:%s]",
                            machineName, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO));
                }
                catch(Exception ex)
                {
                    log.warn(String.format("updateAccessModeByValidateDoubleMainMachineNormal is not found.!![MachineName:%s, ACCESSMODE:%s]",
                            machineName, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO));
                }                
            }
            /* 20190111, hhlee add, if DoublMainMachine is NULL, update AccessMode = 'Auto' ==>> */
            else
            {
                this.updateAccessModeToAutoByValidateDoubleMainMachineNullNormal(eventInfo, machineName);
            }
            /* <<== 20190111, hhlee add, if DoublMainMachine is NULL, update AccessMode = 'Auto' */
        }  
    }
    
    /**
     * 
     * @Name     dubleMachineNamePortAccessModeListIndp
     * @since    2019. 1. 7.
     * @author   hhlee
     * @contents 
     *           
     * @param machineName
     * @param unitName
     * @param accessMode
     * @return
     * @throws CustomException
     */
    private List<Map<String, Object>> linkedUnitNamePortAccessModeListIndp(String machineName, String unitName, String accessMode) throws CustomException
    {
        //List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
        List<Map<String, Object>> result = null;
        try
        {
            String strSql = "SELECT A.LINKEDUNITNAME, A.DOUBLEMAINMACHINENAME     \n"
                          + "  FROM PORT A                                        \n"
                          + " WHERE 1=1                                           \n"
                          + "   AND A.MACHINENAME = :MACHINENAME                  \n"
                          + "   AND A.ACCESSMODE  = :ACCESSMODE                   \n"
                          + "   AND A.LINKEDUNITNAME = :LINKEDUNITNAME            \n"        
                          + "  GROUP BY A.LINKEDUNITNAME, A.DOUBLEMAINMACHINENAME   ";
            
            Map<String, Object> bindMap = new HashMap<String, Object>();
            bindMap.put("MACHINENAME", machineName);
            bindMap.put("ACCESSMODE", accessMode);
            bindMap.put("LINKEDUNITNAME", unitName);
           
            result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
                    
        }
        catch (Exception ex)
        {
            //throw new CustomException();
            //log.warn(ex.getStackTrace());
            log.warn(String.format("linkedUnitNamePortAccessModeListIndp is not found.!![MachineName:%s, ACCESSMODE:%s, LINKEDUNITNAME:%s]", machineName, accessMode, unitName));
        }
        
        return result;
    }
    /**
     * 
     * @Name     dubleMachineNamePortAccessModeListNormal
     * @since    2019. 1. 7.
     * @author   hhlee
     * @contents 
     *           
     * @param machineName
     * @param accessMode
     * @return
     * @throws CustomException
     */
    private List<Map<String, Object>> dubleMachineNamePortAccessModeListNormal(String machineName, String accessMode) throws CustomException
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
            //log.warn(ex.getStackTrace());
            log.warn(String.format("dubleMachineNamePortAccessModeListNormal is not found.!![MachineName:%s, ACCESSMODE:%s, DOUBLEMAINMACHINENAME:%s]",
                    machineName, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO));
        }
        
        return result;
    } 
    
    /**
     * 
     * @Name     getMachineDataByCommunicationState
     * @since    2018. 7. 23.
     * @author   hhlee
     * @contents Get Machine Data By CommunicationState
     *           
     * @param machineName
     * @param machineLevel
     * @param communicationState
     * @param machineType
     * @return
     * @throws CustomException
     */
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
            if(StringUtil.isNotEmpty(machineType))
            {
                strSql = strSql  + " AND MS.MACHINETYPE = :MACHINETYPE                                      \n";
                bindMap.put("MACHINETYPE", machineType);
            }
            if(StringUtil.isNotEmpty(communicationState))
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
    
    /**
     * 
     * @Name     portAccessModeAllAutoAndDoubleMainMachineIsNull
     * @since    2019. 1. 9.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param machineName
     * @param unitName
     * @param operationMode
     * @throws CustomException
     */
    private void portAccessModeAllAutoAndDoubleMainMachineIsNull(EventInfo eventInfo, String machineName, String unitName, String operationMode) throws CustomException
    {
        List<Port> portList  = null;
        
        if(StringUtil.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
        {
            try
            {
                portList = PortServiceProxy.getPortService().select(" MACHINENAME = ? AND LINKEDUNITNAME = ?  ", new Object[] {machineName, unitName});                
            }
            catch(NotFoundSignal ne)
            {    
                log.warn(String.format("portAccessModeAllAutoAndDoubleMainMachineIsNull is not found.!![MachineName:%s, LINKEDUNITNAME:%s]", machineName, unitName));
            }
            catch(Exception ex)
            {
                log.warn(String.format("portAccessModeAllAutoAndDoubleMainMachineIsNull is not found.!![MachineName:%s, LINKEDUNITNAME:%s]", machineName, unitName));
            } 
        }
        else
        {
            try
            {
                portList = PortServiceProxy.getPortService().select(" MACHINENAME = ? AND (DOUBLEMAINMACHINENAME = ? OR DOUBLEMAINMACHINENAME IS NULL) ", 
                        new Object[] {machineName, unitName});                
            }
            catch(NotFoundSignal ne)
            {    
                log.warn(String.format("portAccessModeAllAutoAndDoubleMainMachineIsNull is not found.!![MachineName:%s, DOUBLEMAINMACHINENAME:%s]", machineName, unitName));
            }
            catch(Exception ex)
            {
                log.warn(String.format("portAccessModeAllAutoAndDoubleMainMachineIsNull is not found.!![MachineName:%s, DOUBLEMAINMACHINENAME:%s]", machineName, unitName));
            } 
        }
        
        if(portList != null && portList.size() > 0)
        {
            boolean isAccessModeDuplicate = false;
            boolean isExistDoubleMainMachine = false;
            for(Port portData : portList)
            {
                MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
                
                isExistDoubleMainMachine = true;
                if(StringUtil.isEmpty(CommonUtil.getValue(portData.getUdfs(), "DOUBLEMAINMACHINENAME")))
                {
                    isExistDoubleMainMachine = false;
                }
                
                /* Check Port AccessMode Duplicate */
                isAccessModeDuplicate = false;
                if(StringUtil.equals(portData.getAccessMode(), 
                        GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO))
                {                    
                    isAccessModeDuplicate = true;
                }
                                                
                eventInfo.setEventName("ChangeAccessMode");
                
                porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);
                
                Map<String, String> portUdfs = portData.getUdfs();
                portUdfs.put("DOUBLEMAINMACHINENAME", StringUtil.EMPTY);
                portData.setUdfs(portUdfs);
                porttransitionInfo.setUdfs(portUdfs);
                
                /* To aviod repetition of AccessMode */
                if(!isAccessModeDuplicate)
                {
                    MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, porttransitionInfo, eventInfo); 
                }
                else
                {
                    if(isExistDoubleMainMachine)
                    {
                        SetEventInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(portUdfs);
                        MESPortServiceProxy.getPortServiceImpl().setEvent(portData, transitionInfo, eventInfo);
                    }
                    else
                    {
                        log.info(String.format("There is no change.[AccessMode:Old(%s), New(%s)], [DoubleMainMachine: Old(%s), New(%s)]", 
                                portData.getAccessMode(), GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO, 
                                CommonUtil.getValue(portData.getUdfs(), "DOUBLEMAINMACHINENAME"),StringUtil.EMPTY));
                    }
                }                
            }
        }
    }
    
    /**
     * 
     * @Name     updateAccessModeToAutoByValidateDoubleMainMachineNullNormal
     * @since    2019. 1. 11.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param machineName
     * @throws CustomException
     */
    private void updateAccessModeToAutoByValidateDoubleMainMachineNullNormal(EventInfo eventInfo, String machineName) throws CustomException
    {        
        try
        {
            List<Port> portList = PortServiceProxy.getPortService().select(" MACHINENAME = ? AND ACCESSMODE = ? AND DOUBLEMAINMACHINENAME IS NULL ", 
                    new Object[] {machineName, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL});  
        
            for(Port portData : portList)
            {
                eventInfo.setCheckTimekeyValidation(false);
                //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
                
                MakeAccessModeInfo porttransitionInfo = new MakeAccessModeInfo();
                porttransitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO);
                MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, porttransitionInfo, eventInfo);
            }
        }
        catch(NotFoundSignal ne)
        {    
            log.warn(String.format("updateAccessModeToAutoByValidateDoubleMainMachineNullNormal() is not found.!![MachineName:%s, ACCESSMODE:%s]",
                    machineName, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO));
        }
        catch(Exception ex)
        {
            log.warn(String.format("updateAccessModeToAutoByValidateDoubleMainMachineNullNormal() is not found.!![MachineName:%s, ACCESSMODE:%s]",
                    machineName, GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO));
        }       
    }
    
    /**
     * 
     * @Name     validatelinkedUnitNameDoubleMainMachineIndp
     * @since    2019. 1. 11.
     * @author   hhlee
     * @contents 
     *           
     * @param machineName
     * @param unitName
     * @return
     * @throws CustomException
     */
    private List<Map<String, Object>> validatelinkedUnitNameDoubleMainMachineIndp(String machineName, String unitName) throws CustomException
    {
        //List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
        List<Map<String, Object>> result = null;
        try
        {
            String strSql = "SELECT A.PORTNAME, A.LINKEDUNITNAME, A.DOUBLEMAINMACHINENAME     \n"
                          + "  FROM PORT A                                                    \n"
                          + " WHERE 1=1                                                       \n"
                          + "   AND A.MACHINENAME = :MACHINENAME                              \n"
                          + "   AND A.LINKEDUNITNAME = :LINKEDUNITNAME                        \n" 
                          + "   AND A.LINKEDUNITNAME <> A.DOUBLEMAINMACHINENAME               \n"  
                          + "   AND A.DOUBLEMAINMACHINENAME IS NOT NULL                       \n"
                          + "  GROUP BY A.PORTNAME, A.LINKEDUNITNAME, A.DOUBLEMAINMACHINENAME   ";
            
            Map<String, Object> bindMap = new HashMap<String, Object>();
            bindMap.put("MACHINENAME", machineName);
            bindMap.put("LINKEDUNITNAME", unitName);
           
            result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
                    
        }
        catch (Exception ex)
        {
            //throw new CustomException();
            //log.warn(ex.getStackTrace());
            log.warn(String.format("validatelinkedUnitNameDoubleMainMachineIndp is not found.!![MachineName:%s, LINKEDUNITNAME:%s]", machineName, unitName));
        }
        
        return result;
    }
    
	// For machineRecipeIdleTime
	// Deleted by smkang on 2018.08.21 - I think this method is unnecessary.
//	public List<Port> searchOtherLoaderPorts(String machineName, String portName) throws CustomException
//	{
//		try
//		{
//			List<Port> result = PortServiceProxy.getPortService().select("machineName = ? AND portType IN (?, ?, ?) AND portName <> ? ", new Object[] {machineName, "PB", "PL", "PS", portName});
//			
//			return result;
//		}
//		catch (NotFoundSignal ne)
//		{
//			throw new CustomException("PORT-9001", machineName, "");
//		}
//		catch (FrameworkErrorSignal fe)
//		{
//			throw new CustomException("PORT-9999", fe.getMessage());
//		}
//	}
    
    
}
