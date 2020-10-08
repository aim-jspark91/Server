package kr.co.aim.messolution.consumable.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaterialConsumed;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.consumable.management.info.SetAreaInfo;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
//import kr.co.aim.greentrack.durable.DurableServiceProxy;
//import kr.co.aim.greentrack.durable.management.data.Durable;
//import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.TransitionInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.management.data.Port;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * @author gksong
 * @date   2009.02.16
 */

public class ConsumableServiceUtil implements ApplicationContextAware
{
	/**
	 */
	private ApplicationContext     	applicationContext;
	private  Log				log = LogFactory.getLog(ConsumableServiceUtil.class);				
	
	/**
	 * @param arg0
	 * @throws BeansException
	 */
	@Override
    public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		applicationContext = arg0;
	}
	
	public void changeConsumableLocation(EventInfo eventInfo, String consumableName, String areaName, String machineName, String portName)
	throws CustomException
	{   
		//Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(consumableName);
		//
		////Crate set udfsData 
		//Map<String, String> udfs = consumableData.getUdfs();
		//udfs.put("MACHINENAME", machineName);
		//udfs.put("PORTNAME", portName);
		//
		//if(eventInfo.getEventName().equals("Load"))
		//{
		//	consumableData.setMaterialLocationName(portName);
		//}
		//
		//SetAreaInfo setAreaInfo = new SetAreaInfo();
		//setAreaInfo.setAreaName(areaName);
		//setAreaInfo.setUdfs(udfs);
		//
		//MESConsumableServiceProxy.getConsumableServiceImpl().setArea(consumableData, setAreaInfo, eventInfo);
	    
        Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(consumableName);
        
        //Crate set udfsData 
        Map<String, String> udfs = consumableData.getUdfs();
        SetAreaInfo setAreaInfo = new SetAreaInfo();
        
        if(eventInfo.getEventName().equals("Load"))
        {
            udfs.put("MACHINENAME", machineName);
            udfs.put("PORTNAME", portName);
            
            consumableData.setMaterialLocationName(portName);
            
            setAreaInfo.setAreaName(areaName);
        }
        else if(eventInfo.getEventName().equals("Unload"))
        {
            udfs.put("MACHINENAME", StringUtil.EMPTY);
            udfs.put("PORTNAME", StringUtil.EMPTY);
            consumableData.setMaterialLocationName(StringUtil.EMPTY);
            /* 20181204, modify, modify validation Error(Area is not Empty) ==>> */
            //setAreaInfo.setAreaName(StringUtil.EMPTY);
            setAreaInfo.setAreaName(areaName);
            /* <<== 20181204, modify, modify validation Error(Area is not Empty) */
        }
        
        setAreaInfo.setUdfs(udfs);
        
        MESConsumableServiceProxy.getConsumableServiceImpl().setArea(consumableData, setAreaInfo, eventInfo);	    
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
    public void checkLoadedCrate(EventInfo eventInfo, String factoryName, String crateName, String machineName, String portName) throws CustomException 
    {
        try
        {
        	// Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//            String condition = " FACTORYNAME = ? AND MACHINENAME = ? AND PORTNAME = ? ";
            String condition = " FACTORYNAME = ? AND MACHINENAME = ? AND PORTNAME = ? FOR UPDATE";

            Object[] bindSet = new Object[] {factoryName, machineName, portName};
            
            List <Consumable> consumableDataList = ConsumableServiceProxy.getConsumableService().select(condition, bindSet);
            
            if(consumableDataList.size() > 0)
            {
                SetEventInfo setEventInfo = new SetEventInfo();
                Map<String, String> consumableUdfs = null;
                for(Consumable consumableData : consumableDataList)
                {
                    if(StringUtil.equals(crateName, consumableData.getKey().getConsumableName()) && 
                            StringUtil.equals(machineName, CommonUtil.getValue(consumableData.getUdfs(), "MACHINENAME")) &&
                            StringUtil.equals(portName, CommonUtil.getValue(consumableData.getUdfs(), "PORTNAME")) )
                    {
                        // To Do .......
                    }
                    else
                    {
                        consumableData.setMaterialLocationName(StringUtil.EMPTY);
                        consumableData.setAreaName(StringUtil.EMPTY);
                        
                        consumableUdfs = consumableData.getUdfs();
                        consumableUdfs.put("MACHINENAME", StringUtil.EMPTY);
                        consumableUdfs.put("PORTNAME", StringUtil.EMPTY);
                        
                        eventInfo.setEventName("ChangeTransportState");
                        eventInfo.setEventComment("[MachineName : " + machineName + ", " + " PortName : " + portName + "] - " +
                                                  "Current Load CrateName : " + crateName + ", Before load CrateName : " + consumableData.getKey().getConsumableName());
                       
                        setEventInfo.setUdfs(consumableUdfs);
                        
                        ConsumableServiceProxy.getConsumableService().update(consumableData);
                        
                        try
                        {
                            consumableData = ConsumableServiceProxy.getConsumableService().setEvent(consumableData.getKey(), eventInfo, setEventInfo);                     
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
	* Name : crateRemainedCountReply
	* Desc : This function is crateRemainedCountReply
	* Author : hykim
	* Date : 2014.08.01
	*/
	public void crateRemainedCountReply(String machineName, String portName, String crateName, 
								 org.jdom.Document message)
			throws CustomException 
	{
		String remainCrateGlassQuantity = "0";
		// getMachineInfo
		Machine machineData = CommonUtil.getMachineInfo(machineName);
		//machineGroupName = machineData.getMachineGroupName().toString();
			
		// getPortInfo
		Port portData = CommonUtil.getPortInfo(machineName, portName);
		//portUseType = portData.getUdfs().get("PORTUSETYPE").toString();
		
		/* 2018.04.02, hhlee, porttype Add */
		String portType = portData.getUdfs().get("PORTTYPE");
		
		// getDurableInfo
		Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(crateName);
		//remainCrateGlassQuantity = String.valueOf((int) consumableData.getCreateQuantity());
		remainCrateGlassQuantity = String.valueOf((int) consumableData.getQuantity());
		
		// Create Body Element
		Element bodyElement = null;
		bodyElement = new Element("Body");

		Element machineNameElement = new Element("MACHINENAME");
		machineNameElement.setText(machineName);
		bodyElement.addContent(machineNameElement);

		Element portNameElement = new Element("PORTNAME");
		portNameElement.setText(portName);
		bodyElement.addContent(portNameElement);
		
		/* 2018.04.02, hhlee, porttype element Add */
		Element portTypeElement = new Element("PORTTYPE");
		portTypeElement.setText(portType);
		bodyElement.addContent(portTypeElement);

		Element crateNameElement = new Element("CRATENAME");
		crateNameElement.setText(crateName);
		bodyElement.addContent(crateNameElement);

		Element remainCrateGlassQuantityElement = new Element("REMAINCRATEGLASSQUANTITY");
		remainCrateGlassQuantityElement.setText(remainCrateGlassQuantity);
		bodyElement.addContent(remainCrateGlassQuantityElement);
		
		
		//first removal of existing node would be duplicated
		message.getRootElement().removeChild(SMessageUtil.Body_Tag);
		//index of Body node is static
		message.getRootElement().addContent(2, bodyElement);
	}
	//20170228 by zhanghao validateMaterialMapping
	public void validateMaterialMapping(String factoryName, String productSpecName, String processOperationName,
			String machineName)   throws CustomException
 {
		StringBuffer sqlBuffer = new StringBuffer();
		        sqlBuffer.append("   SELECT PB.MATERIALPOSITIONNAME         "+ "\n");
		        sqlBuffer.append("   FROM TPOMPOLICY TP,POSBOM PB          "+ "\n");
		        sqlBuffer.append("   WHERE 1 = 1                           "+ "\n");
		        sqlBuffer.append("	AND TP.CONDITIONID = PB.CONDITIONID   "+ "\n");
		        sqlBuffer.append("	AND TP.FACTORYNAME = ?                "+ "\n");
		        sqlBuffer.append("	AND TP.PRODUCTSPECNAME = ?            "+ "\n");
		        sqlBuffer.append("	AND TP.PROCESSOPERATIONNAME = ?       "+ "\n");
		        sqlBuffer.append("   AND TP.MACHINENAME = ?               "+ "\n");
		String qryString = sqlBuffer.toString();
		Object[] bindSet = new String[] { factoryName, productSpecName,processOperationName,machineName };
        
		StringBuffer sqlBuffer2 = new StringBuffer();
		        sqlBuffer2.append("   SELECT C.MATERIALPOSITIONNAME        " + "\n");
		        sqlBuffer2.append("   FROM CONSUMABLE C                    " + "\n");
		        sqlBuffer2.append("   WHERE 1 = 1                           " + "\n");
		        sqlBuffer2.append("	AND C.FACTORYNAME = ?                " + "\n");
		        sqlBuffer2.append("   AND C.MACHINENAME = ?               " + "\n");
		String qryString2 = sqlBuffer2.toString();
		Object[] bindSet2 = new String[] { factoryName, machineName };
		try {
			List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(qryString, bindSet);
			List<ListOrderedMap> sqlResult2 = GenericServiceProxy.getSqlMesTemplate().queryForList(qryString2, bindSet2);
			if (!sqlResult.isEmpty())
			{
				if(sqlResult2.isEmpty())
				{
					throw new CustomException("MATERIAL-0024");
				}
				else
				{
					if(!sqlResult2.containsAll(sqlResult))
					{
						throw new CustomException("MATERIAL-0025");
					}
				}								
			}						
		} catch (FrameworkErrorSignal de) {
			throw new CustomException("SYS-9999", "TPOMPolicy (Material)",
					de.getMessage());
		}
	}
	//20170228 by zhanghao getMaterialPositionNameList
	public static List<ListOrderedMap> getMaterialPositionNameList(String machineName)throws CustomException
	{
		StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append("   SELECT DISTINCT D.MATERIALPOSITIONNAME  "+ "\n");
        sqlBuffer.append("   FROM CONSUMABLE D                    "+ "\n");
        sqlBuffer.append("   WHERE 1 = 1                           "+ "\n");
        sqlBuffer.append("   AND D.MACHINENAME = ?               "+ "\n");
        String qryString = sqlBuffer.toString();
        Object[] bindSet = new String[] { machineName };
        try
		{ 
			List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(qryString, bindSet);
			if(!sqlResult.isEmpty())
				return sqlResult;
			else return null;
		}
		catch (FrameworkErrorSignal de)
		{
			throw new CustomException("SYS-9999", "TPOMPolicy (Material)", de.getMessage());
		}
	}
	  //20170317 by zhanghao getMaterialPositionNameList
		public static List<ListOrderedMap> getTPOMMaterialPositionNameList(String factoryName, String productSpecName, String processOperationName,
				String machineName)throws CustomException
		{
			StringBuffer sqlBuffer = new StringBuffer();
	        sqlBuffer.append("   SELECT DISTINCT PB.MATERIALPOSITIONNAME  "+ "\n");
	        sqlBuffer.append("   FROM TPOMPOLICY TP,POSBOM PB       "+ "\n");
	        sqlBuffer.append("   WHERE 1 = 1                           "+ "\n");
	        sqlBuffer.append("	AND TP.CONDITIONID = PB.CONDITIONID   "+ "\n");
            sqlBuffer.append("	AND TP.FACTORYNAME = ?                "+ "\n");
            sqlBuffer.append("	AND TP.PRODUCTSPECNAME = ?            "+ "\n");
            sqlBuffer.append("	AND TP.PROCESSOPERATIONNAME = ?       "+ "\n");
            sqlBuffer.append("   AND TP.MACHINENAME = ?               "+ "\n");
	        String qryString = sqlBuffer.toString();
	        Object[] bindSet = new String[] {factoryName,productSpecName,processOperationName, machineName };
	        try
			{ 
				List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(qryString, bindSet);
				if(!sqlResult.isEmpty())
					return sqlResult;
				else return null;
			}
			catch (FrameworkErrorSignal de)
			{
				throw new CustomException("SYS-9999", "TPOMPolicy (Material)", de.getMessage());
			}
		}
	//20170303 by zhanghao getPosbomConsumeQuantity
	public static List<ListOrderedMap> getPosbomConsumeQuantity(String factoryName, String productSpecName, String processOperationName,
			String machineName,String consumableSpecName)throws CustomException
	{
		StringBuffer sqlBuffer = new StringBuffer();
                     sqlBuffer.append("   SELECT PB.REQUIREDQUANTITY           "+ "\n");
                     sqlBuffer.append("   FROM TPOMPOLICY TP,POSBOM PB          "+ "\n");
                     sqlBuffer.append("   WHERE 1 = 1                           "+ "\n");
                     sqlBuffer.append("	AND TP.CONDITIONID = PB.CONDITIONID   "+ "\n");
                     sqlBuffer.append("	AND TP.FACTORYNAME = ?                "+ "\n");
                     //sqlBuffer.append("	AND TP.PRODUCTSPECNAME = ?            "+ "\n");
                     //sqlBuffer.append("	AND TP.PROCESSOPERATIONNAME = ?       "+ "\n");
                     sqlBuffer.append("   AND TP.MACHINENAME = ?               "+ "\n");
                     sqlBuffer.append("   AND PB.MATERIALSPECNAME = ?          "+ "\n");
        String qryString = sqlBuffer.toString();             
        Object[] bindSet = new String[] { factoryName,machineName,consumableSpecName };
        try
		{ 
			List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(qryString, bindSet);
			if(!sqlResult.isEmpty())
				return sqlResult;
			else 
				throw new CustomException("MATERIAL-0024", machineName);
		}
		catch (FrameworkErrorSignal de)
		{
			throw new CustomException("SYS-9999", "TPOMPolicy (Material)", de.getMessage());
		}
	}
	
	public static void insertCT_MaterialConsumedByTrackOut( EventInfo eventInfo, Element productListE, Lot lotData ) throws Exception
	{
		for ( Iterator iterator = productListE.getChildren().iterator(); iterator.hasNext(); )
		{
			Element productE = (Element) iterator.next();
			String productName = SMessageUtil.getElementValueByName( productE, "PRODUCTNAME" );
			String lotName = SMessageUtil.getElementValueByName( productE, "LOTNAME" );

			List<Element> materialEList = SMessageUtil.getSubSequenceItemList( productE, "MATERIALLIST", false );

			for ( Iterator iterator2 = materialEList.iterator(); iterator2.hasNext(); )
			{
				createMaterialConsumed( (Element) iterator2.next(),productName,lotName,lotData,eventInfo);
			}

			List<Element> ePCBList = SMessageUtil.getSubSequenceItemList( productE, "PCBLIST", false );
			for ( Element element : ePCBList )
			{
				createMaterialConsumed( element,productName,lotName,lotData,eventInfo);
			}
		}
	}
	
	public static void createMaterialConsumed( Element materialElement,String panelName,String lotID,
			Lot lotData,EventInfo eventInfo) throws Exception
	{	
		String materialName = materialElement.getChildText( "MATERIALNAME" );
		String materialID = materialElement.getChildText( "MATERIALID" );
		String materialType = materialElement.getChildText( "MATERIALTYPE" );
		String materialPosition = materialElement.getChildText( "MATERIALPOSITION" );
		
		MaterialConsumed mcData = new MaterialConsumed();
		mcData.setEventInfo( eventInfo );
		mcData.setProductName( panelName );
		mcData.setLotName( lotID );
		mcData.setMaterialName( materialName );
		if(materialID == null)
		{
			mcData.setMATERIALID( "-" );
		}
		else
		{
			mcData.setMATERIALID( materialID );
		}
		mcData.setQuantity( 1 );
		mcData.setFactoryName( lotData.getFactoryName() );
		mcData.setProductSpecName( lotData.getProductSpecName() );
		mcData.setProductSpecVersion( lotData.getProductSpecVersion() );
		mcData.setProcessFlowName( lotData.getProcessFlowName() );
		mcData.setProcessFlowVersion( lotData.getProcessFlowVersion() );
		mcData.setProcessOperationName( lotData.getProcessOperationName() );
		mcData.setProcessOperationVersion( lotData.getProcessOperationVersion() );
		mcData.setMachineName( lotData.getMachineName() );
		mcData.setMaterialLocationName( lotData.getMachineName() );
		mcData.setMaterialType( materialType );
		mcData.setMaterialPosition( materialPosition );
		
		Object[] bindSet = new Object[3];
		bindSet[0] = panelName;
		bindSet[1] = materialName;
		if(materialID == null)
		{
			bindSet[2] = "-";
		}
		else
		{
			bindSet[2] = materialID;
		}
		try{
		MaterialConsumed MC =ExtendedObjectProxy.getMaterialConsumedService().selectByKey(MaterialConsumed.class, false, bindSet);
		ExtendedObjectProxy.getMaterialConsumedService().delete(MaterialConsumed.class, bindSet);
		ExtendedObjectProxy.getMaterialConsumedService().create( eventInfo, mcData );
		}	
		catch (Exception e)
		{
			ExtendedObjectProxy.getMaterialConsumedService().create( eventInfo, mcData );
		}

		
		/*if(!MC.getProductName().isEmpty() )
		{
			ExtendedObjectProxy.getMaterialConsumedService().delete(MaterialConsumed.class, bindSet);
		}
		
		ExtendedObjectProxy.getMaterialConsumedService().create( eventInfo, mcData );*/
	
	}
	
	/**
    * Name : kitConsumableData
    * Desc : Execute Kit event
    * Author : aim system
    * Date : 2016.07.29
    */
    public void kitConsumableData(EventInfo eventInfo, Consumable materialData, String MachineName, String materialState, String materialPosition) 
            throws CustomException
    {
        Map<String, String> udfs = materialData.getUdfs();
        udfs.put("MACHINENAME", MachineName);
        udfs.put("MATERIALPOSITION",materialPosition);
        udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP);
        
        materialData.setUdfs(udfs);
        
        //materialData.setConsumableState(GenericServiceProxy.getConstantMap().Cons_InUse);
        //materialData.setMaterialLocationName(MachineName);
        materialData.setConsumableState(materialState);
        materialData.setMaterialLocationName(materialPosition);
        
        ConsumableServiceProxy.getConsumableService().update(materialData);
        
        kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfo = 
                MESConsumableServiceProxy.getConsumableInfoUtil().setEventInfo(materialData, materialData.getAreaName());
        MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialData.getKey().getConsumableName(), setEventInfo, eventInfo);
    }
    
    /**
     * 
     * @Name     unkitConsumableData
     * @since    2018. 9. 21.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param materialData
     * @param usedQty
     * @param materialState
     * @throws CustomException
     */
    public void unkitConsumableData(EventInfo eventInfo, Consumable materialData, String usedQty, String materialState) throws CustomException
    {
        Map<String, String> udfs = materialData.getUdfs();
        udfs.put("MACHINENAME", "");
        udfs.put("UNITNAME", "");
        udfs.put("MATERIALPOSITION", "");
        udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
        
        materialData.setConsumableState(GenericServiceProxy.getConstantMap().Cons_Available);
        //materialData.setConsumableState(materialState);
        materialData.setMaterialLocationName("");
        materialData.setUdfs(udfs);
        
        kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfo = MESConsumableServiceProxy.getConsumableInfoUtil().setEventInfo(materialData, materialData.getAreaName());
        ConsumableServiceProxy.getConsumableService().update(materialData);
        
        if(Double.valueOf(usedQty) != 0 && 
                Double.valueOf(materialData.getQuantity()) != 0)
        {
            //decrement
            TransitionInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().decrementQuantityInfo("", "", "", "",
                                                        eventInfo.getEventTimeKey(), Double.parseDouble(usedQty), udfs);
            
            MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(materialData,
                                        (DecrementQuantityInfo) transitionInfo, eventInfo);
            /*double quantity=materialData.getQuantity();
            quantity=quantity-Double.valueOf(usedQty);
            materialData.setQuantity(quantity);
            ConsumableServiceProxy.getConsumableService().update(materialData);*/
            //makeNotAvailable
            materialData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialData.getKey().getConsumableName());
            if(materialData.getQuantity() == 0 && StringUtil.equals(materialData.getConsumableState(), "Available"))
            {
                //eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), null, null);
                eventInfo.setEventName("ChangeState");
                MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();
                makeNotAvailableInfo.setUdfs(materialData.getUdfs());
                MESConsumableServiceProxy.getConsumableServiceImpl().makeNotAvailable(materialData, makeNotAvailableInfo, eventInfo);
            }
        }
        else
        {
           MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialData.getKey().getConsumableName(), setEventInfo, eventInfo);
        }
    }
	    
	/**
	 * 
	 * @Name     checkExistenceByMachineNamePosition
	 * @since    2018. 9. 21.
	 * @author   hhlee
	 * @contents 
	 *           
	 * @param eventInfo
	 * @param machineName
	 * @param unitName
	 * @param materialLocationName
	 * @param consumableState
	 * @throws CustomException
	 */
    public void checkExistenceByMachineNamePosition(EventInfo eventInfo, String ConsumableName, String machineName, String unitName, 
            String materialLocationName, String consumableState)throws CustomException
    {
        log.info("checkExistenceByMachineNamePosition Started.");

        String strSql = " WHERE MACHINENAME = ? AND CONSUMABLESTATE IN ( ?, ? ) AND MATERIALLOCATIONNAME = ? ";
        Object [] bindSet ;
        
        if(StringUtil.isNotEmpty(unitName))
        {
            strSql = strSql + " AND UNITNAME = ? ";
            //bindSet = new Object[]{machineName,  consumableState, materialLocationName, unitName};
            bindSet = new Object[]{machineName,  GenericServiceProxy.getConstantMap().Cons_Mount, GenericServiceProxy.getConstantMap().Cons_InUse, materialLocationName, unitName};
        }
        else
        {
            bindSet = new Object[]{machineName, consumableState, materialLocationName};
        }
        
        List<Consumable>consumableList = new ArrayList<Consumable>();

        try
        {
        	// Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
        	strSql += " FOR UPDATE";
        	
            consumableList = ConsumableServiceProxy.getConsumableService().select(strSql,bindSet);
            
            if(consumableList.size() > 0)
            {
                eventInfo.setEventName(GenericServiceProxy.getConstantMap().Cons_Unmount);
                //and change mask state to UNMOUNT/OUTSTK
                for(Consumable consumableData : consumableList)
                {
                    if(StringUtil.equals(consumableData.getKey().getConsumableName(), ConsumableName) && 
                            StringUtil.equals(consumableData.getMaterialLocationName(), materialLocationName) )
                    {
                    }
                    else
                    {
                        Map<String, String> udfs = consumableData.getUdfs();
                        udfs.put("MACHINENAME", "");
                        udfs.put("UNITNAME", "");
                        udfs.put("MATERIALPOSITION", "");
                        udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
                        
                        consumableData.setConsumableState(GenericServiceProxy.getConstantMap().Cons_Available);
                        //consumableData.setConsumableState(GenericServiceProxy.getConstantMap().Cons_Unmount);
                        consumableData.setMaterialLocationName("");
                        consumableData.setUdfs(udfs);
                        
                        ConsumableServiceProxy.getConsumableService().update(consumableData);
                        
                        kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfo = MESConsumableServiceProxy.getConsumableInfoUtil().setEventInfo(consumableData, consumableData.getAreaName());
                        MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(consumableData.getKey().getConsumableName(), setEventInfo, eventInfo);
                        
                        eventInfo.setCheckTimekeyValidation(false);
                        eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
                    }
                }
            }
        }
        catch(Exception e)
        {
            log.error(e.getMessage());
        }

        log.info("checkExistenceByMachineNamePosition Ended.");

    }
    
    /**
     * 
     * @Name     checkExistenceByMachineNamePosition
     * @since    2018. 9. 21.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param machineName
     * @param unitName
     * @param materialLocationName
     * @param consumableState
     * @throws CustomException
     */
    public void checkExistenceByMaterialName(EventInfo eventInfo, String ConsumableName, String unitName, 
            String materialLocationName, String consumableState)throws CustomException
    {
        log.info("checkExistenceByMaterialName Started.");

        // Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//        String strSql = " WHERE CONSUMABLENAME = ? AND CONSUMABLESTATE IN (?, ?) ";
        String strSql = " WHERE CONSUMABLENAME = ? AND CONSUMABLESTATE IN (?, ?) FOR UPDATE";
        
        Object [] bindSet = new Object[]{ConsumableName,  GenericServiceProxy.getConstantMap().Cons_Mount, GenericServiceProxy.getConstantMap().Cons_InUse};
        
        List<Consumable>consumableList = new ArrayList<Consumable>();

        try
        {
            consumableList = ConsumableServiceProxy.getConsumableService().select(strSql,bindSet);
            
            if(consumableList.size() > 0)
            {
                eventInfo.setEventName(GenericServiceProxy.getConstantMap().Cons_Unmount);
                //and change mask state to UNMOUNT/OUTSTK
                for(Consumable consumableData : consumableList)
                {
                    if(!StringUtil.equals(consumableData.getMaterialLocationName(), materialLocationName))
                    {
                        Map<String, String> udfs = consumableData.getUdfs();
                        udfs.put("MACHINENAME", "");
                        udfs.put("UNITNAME", "");
                        udfs.put("MATERIALPOSITION", "");
                        udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
                        
                        consumableData.setConsumableState(GenericServiceProxy.getConstantMap().Cons_Available);
                        //consumableData.setConsumableState(GenericServiceProxy.getConstantMap().Cons_Unmount);
                        consumableData.setMaterialLocationName("");
                        consumableData.setUdfs(udfs);
                        
                        ConsumableServiceProxy.getConsumableService().update(consumableData);
                        
                        kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfo = MESConsumableServiceProxy.getConsumableInfoUtil().setEventInfo(consumableData, consumableData.getAreaName());
                        MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(consumableData.getKey().getConsumableName(), setEventInfo, eventInfo); 
                        
                        eventInfo.setCheckTimekeyValidation(false);
                        eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
                    }
                }
            }
        }
        catch(Exception e)
        {
            log.error(e.getMessage());
        }

        log.info("checkExistenceByMaterialName Ended.");

    }
    
    /**
     * 
     * @Name     checkExistenceByMachineNamePosition
     * @since    2018. 9. 21.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param machineName
     * @param unitName
     * @param materialLocationName
     * @param consumableState
     * @throws CustomException
     */
    public void checkExistenceByMachineName(EventInfo eventInfo, List<Element> materialList, String machineName, String unitName, 
            String materialLocationName, String consumableState)throws CustomException
    {
        log.info("checkExistenceByMachineName Started.");

        // Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//        String strSql = " WHERE MACHINENAME = ? AND UNITNAME = ? AND CONSUMABLESTATE IN ( ?, ? ) ";
        String strSql = " WHERE MACHINENAME = ? AND UNITNAME = ? AND CONSUMABLESTATE IN ( ?, ? ) FOR UPDATE";

        Object [] bindSet =  new Object[]{machineName,  unitName, GenericServiceProxy.getConstantMap().Cons_Mount, GenericServiceProxy.getConstantMap().Cons_InUse };
        
        
        List<Consumable>consumableList = new ArrayList<Consumable>();

        String consumableName = StringUtil.EMPTY;
        boolean existCheckConsumableName = false;
        
        try
        {
            consumableList = ConsumableServiceProxy.getConsumableService().select(strSql, bindSet);
            
            if(consumableList.size() > 0)
            {          
                eventInfo.setEventName(GenericServiceProxy.getConstantMap().Cons_Unmount);
                //and change mask state to UNMOUNT/OUTSTK
                for(Consumable consumableData : consumableList)
                {
                    existCheckConsumableName = false;
                    consumableName = StringUtil.EMPTY;
                    
                    if (materialList != null && materialList.size() > 0) 
                    {
                        for(Element materialE : materialList)
                        {
                            consumableName = SMessageUtil.getChildText(materialE, "MATERIALNAME", true);
                            
                            if(StringUtil.equals(consumableData.getKey().getConsumableName(), consumableName))
                            {
                                existCheckConsumableName = true;
                                break;
                            }                            
                        }
                    }
                    
                    if(!existCheckConsumableName)
                    {
                        Map<String, String> udfs = consumableData.getUdfs();
                        udfs.put("MACHINENAME", "");
                        udfs.put("UNITNAME", "");
                        udfs.put("MATERIALPOSITION", "");
                        udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
                        
                        consumableData.setConsumableState(GenericServiceProxy.getConstantMap().Cons_Available);
                        //consumableData.setConsumableState(GenericServiceProxy.getConstantMap().Cons_Unmount);
                        consumableData.setMaterialLocationName("");
                        consumableData.setUdfs(udfs);
                        
                        ConsumableServiceProxy.getConsumableService().update(consumableData);
                        
                        kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfo = MESConsumableServiceProxy.getConsumableInfoUtil().setEventInfo(consumableData, consumableData.getAreaName());
                        MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(consumableData.getKey().getConsumableName(), setEventInfo, eventInfo);
                        
                        eventInfo.setCheckTimekeyValidation(false);
                        eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
                    }
                }                
            }
        }
        catch(Exception e)
        {
            log.error(e.getMessage());
        }

        log.info("checkExistenceByMachineName Ended.");

    }
    
    //add by wghuang 20180913
    public boolean checkConsumableExistence(String consumableName) throws CustomException
    {
        boolean existence = false;
        
        String condition = "CONSUMABLENAME = ?";
        
        Object[] bindSet = new Object[] {consumableName};
        try
        {
            List <Consumable> sqlResult = ConsumableServiceProxy.getConsumableService().select(condition, bindSet);
            
            if(sqlResult.size() > 0)
            {
                existence = true;
                return existence;
            }   
        }
        catch (NotFoundSignal ex)
        {
             return existence;
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("SYS-0001", fe.getMessage());
        }
        return existence;
    }
    
    public boolean checkRelationSpec(String machineName, String unitName, String materialPosition, String MaterialSpecName) throws CustomException
    {
        //OK = true, NG = false
        boolean OK_NG = false;        
        
        List<Map<String, Object>> checkSqlResult = new ArrayList<Map<String,Object>>();
        
        boolean checkResult = this.checkRelationSpecExistenceByMU(machineName, unitName, true);        
        
        if(this.checkRelationSpecExistenceByMU(machineName, unitName, true) == true)
        {
            String checkSql = " SELECT PRSPECNAME "
                            + " FROM CT_RELATIONPRSPEC "
                            + " WHERE MACHINENAME = :MACHINENAME "
                            + " AND UNITNAME = :UNITNAME "
                            + " AND POSITION = :POSITION "
                            + " AND PRSPECNAME = :PRSPECNAME ";
    
            Map<String, Object> checkBindSet = new HashMap<String, Object>();
            checkBindSet.put("MACHINENAME", machineName);
            checkBindSet.put("UNITNAME", unitName);
            checkBindSet.put("POSITION", materialPosition);
            checkBindSet.put("PRSPECNAME", MaterialSpecName);

            checkSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(checkSql, checkBindSet);
            
            if(checkSqlResult.size() > 0)
                OK_NG = true;
        }
        else
        {
            OK_NG = true;
        }
                    
        return OK_NG;
    }
    
    //M:MachineName, U:unitName
    private boolean checkRelationSpecExistenceByMU(String machineName, String unitName,boolean checkPRSpec) throws CustomException
    {
        boolean existence = true ;
        
        List<Map<String, Object>> checkSqlResult = new ArrayList<Map<String,Object>>();
        
        String checkSql = " SELECT PRSPECNAME "
                        + " FROM CT_RELATIONPRSPEC "
                        + " WHERE MACHINENAME = :MACHINENAME "
                        + " AND UNITNAME = :UNITNAME " ;
        
        Map<String, Object> checkBindSet = new HashMap<String, Object>();
        checkBindSet.put("MACHINENAME", machineName);
        checkBindSet.put("UNITNAME", unitName);


        checkSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(checkSql, checkBindSet);
        
        if(checkSqlResult.size() > 0)
        {
            if(checkPRSpec == true)
            {
                for(int i = 0; i < checkSqlResult.size(); i ++)
                {
                    if(i == checkSqlResult.size() - 1)
                        break;
                    
                    for(int j = i + 1; j < checkSqlResult.size(); j ++)
                    {
                        if(!CommonUtil.getValue(checkSqlResult.get(i), "PRSPECNAME").equals
                          (CommonUtil.getValue(checkSqlResult.get(j), "PRSPECNAME")))
                        {
                            //need to add another ErrorCode
                            throw new CustomException("MATERIAL-0031", unitName, CommonUtil.getValue(checkSqlResult.get(i), "PRSPECNAME"), 
                                    CommonUtil.getValue(checkSqlResult.get(j), "PRSPECNAME"));
                        }
                    }
                }
            }
        }
        else
        {
            existence = false;
        }
        
        return existence;
    }
    
    /**
     * 
     * @Name     checkPrSpecExistenceByMU
     * @since    2018. 9. 26.
     * @author   hhlee
     * @contents 
     *           //M:MachineName, U:unitName
     * @param machineName
     * @param unitName
     * @param prSpecName
     * @return
     * @throws CustomException
     */
    public boolean checkPrSpecExistenceByMU(String machineName, String unitName, String prSpecName) throws CustomException
    {
        boolean existence = true;
        
        String strSql = " WHERE SUPERMACHINENAME = ? AND MACHINENAME = ? ";
        Object [] bindSet = new Object[]{machineName, unitName};        
       
        List<MachineSpec> checkSqlResult = MachineServiceProxy.getMachineSpecService().select(strSql, bindSet);
        
        if(checkSqlResult.size() > 0 && 
                StringUtil.isNotEmpty(checkSqlResult.get(0).getUdfs().get("PRSPECNAME")))
        {   
            if(!StringUtil.equals(checkSqlResult.get(0).getUdfs().get("PRSPECNAME"), prSpecName))
            {
                throw new CustomException("MATERIAL-0031", unitName, checkSqlResult.get(0).getUdfs().get("PRSPECNAME"), prSpecName);
            }                             
        }               
        
        return existence;
        
//      boolean existence = true;
//      
//      String checkSql = " SELECT PRSPECNAME "
//              + " FROM MACHINESPEC "
//              + " WHERE SUPERMACHINENAME = :MACHINENAME "
//              + " AND MACHINENAME = :UNITNAME " ;
//
//      Map<String, Object> checkBindSet = new HashMap<String, Object>();
//      checkBindSet.put("MACHINENAME", machineName);
//      checkBindSet.put("UNITNAME", unitName);
//      
//      List<Map<String, Object>> checkSqlResult = new ArrayList<Map<String,Object>>();       
//              
//      //List<MachineSpec> checkSqlResult = MachineServiceProxy.getMachineSpecService().select(strSql, bindSet);
//      checkSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(checkSql, checkBindSet);
//      
//      if(checkSqlResult.size() > 0 && StringUtil.isNotEmpty(CommonUtil.getValue(checkSqlResult.get(0), "PRSPECNAME").toString()))
//      {   
//          if(!StringUtil.equals(CommonUtil.getValue(checkSqlResult.get(0), "PRSPECNAME").toString(), prSpecName))
//          {
//              //throw new CustomException("MATERIAL-0031", unitName, checkSqlResult.get(0).getUdfs().get("PRSPECNAME").toString(), prSpecName);
//              throw new CustomException("MATERIAL-0031", unitName, CommonUtil.getValue(checkSqlResult.get(0), "PRSPECNAME").toString(), prSpecName);
//          }                             
//      }           
//              
//      return existence;
    }
    
    //add by wghuang 20180913
    public String getMaterialSpecByLength(String materialName, String materialSpecReported, boolean checkMaterialSpec) throws CustomException
    {
        boolean existence = false;
        
        String materialSpec = "";
        String enumName = "";   
        String enumValue = String.valueOf(materialName.length());
                
        List<Map<String,Object>> enumNamelist = new ArrayList<Map<String,Object>>();
        //put enumName New
        enumNamelist = CommonUtil.getEnumDefValueByEnumName(GenericServiceProxy.getConstantMap().PRMaterial_Relation);
        
        //20181119, hhlee, modify sqlResult null check
        if(enumNamelist != null && enumNamelist.size() <= 0)
            throw new CustomException("MATERIAL-0029",GenericServiceProxy.getConstantMap().PRMaterial_Relation);
        
        for(Map<String,Object> enumN : enumNamelist)
        {
            enumName = CommonUtil.getValue(enumN, "ENUMVALUE");
            
            if(checkEnumDefValueExistence(enumName,enumValue) == true)
            {
                materialSpec = enumName;
                existence = true;
                break;
            }   
        }
        
        if(existence == false)
            throw new CustomException("MATERIAL-0027",enumName,enumValue);
        else
            this.checkConsumableSpecExistence(materialSpec);//checkConsumableSpecExistence
        
        
        if(checkMaterialSpec == true)
        {
            if(!StringUtil.equals(materialSpec, materialSpecReported))
                throw new CustomException("MATERIAL-0028",materialSpec,materialSpecReported);       
        }
            
        return materialSpec;
    }
    
    //add by wghuang 20180913
    private boolean checkEnumDefValueExistence(String enumName, String enumvalue )
    {
        boolean result =  false;
        
        String sql = "SELECT ENUMVALUE FROM ENUMDEFVALUE WHERE ENUMNAME = :enumName AND ENUMVALUE = :enumValue ";

        Map<String, String> bindMap = new HashMap<String, String>();
        bindMap.put("enumName", enumName);
        bindMap.put("enumValue", enumvalue);

        List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

        //20181119, hhlee, modify sqlResult null check
        if(sqlResult != null && sqlResult.size() > 0)
        {
            result = true;
        }

        return result;
    }
    
    //add by wghuang 20180913
    private boolean checkConsumableSpecExistence(String consumableSpecName) throws CustomException
    {
        boolean existence = false;
    
        String condition = "CONSUMABLESPECNAME = ?";
        
        Object[] bindSet = new Object[] {consumableSpecName};
        
        try
        {
            
            List <ConsumableSpec> sqlResult = ConsumableServiceProxy.getConsumableSpecService().select(condition, bindSet);
            
            if(sqlResult.size() > 0)
            {
                existence = true;
                return existence;
            }   
        }
        catch (NotFoundSignal ex)
        {
             throw new CustomException("MATERIAL-0030",consumableSpecName); 
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("SYS-0001", fe.getMessage());
        }
        
        return existence;
    }
}
