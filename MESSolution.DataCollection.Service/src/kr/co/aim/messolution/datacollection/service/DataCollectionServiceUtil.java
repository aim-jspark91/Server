package kr.co.aim.messolution.datacollection.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SpcProcessedOperation;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.datacollection.management.data.DCSpec;
import kr.co.aim.greentrack.datacollection.management.data.DCSpecItem;
import kr.co.aim.greentrack.datacollection.management.data.DCSpecItemKey;
import kr.co.aim.greentrack.datacollection.management.sql.SqlStatement;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class DataCollectionServiceUtil implements ApplicationContextAware
{
	
	/**
	 * @uml.property  name="applicationContext"
	 * @uml.associationEnd  
	 */
	private ApplicationContext		applicationContext;
	private static Log				log = LogFactory.getLog(DataCollectionServiceUtil.class);
	
	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	
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
	* Name : getItemName
	* Desc : This function is Add ItemName Return
	* Author : AIM Systems, Inc
	* Date : 2011.03.12
	*/
	private String getItemName(String dcSpecName, String siteName)
	{
		String itemName = "";
		
		String args = "%"+siteName+"%";
		
		String sql = "SELECT ITEMNAME FROM DCSPECITEM " +
				     " WHERE DCSPECNAME = ? " +
				     "   AND SITENAMES LIKE '"+args+"'";
		
		String[] bindSet = new String[]{ dcSpecName };
		
		//List resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, bindSet);
		List resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindSet);
		
		if(resultList.size() > 0)
		{
			ListOrderedMap dcSpecList = (ListOrderedMap) resultList.get(0);
			itemName = dcSpecList.get("ITEMNAME").toString();	
		}
		
		return itemName;
	}
	
	/*
	* Name : convertStringValue
	* Desc : This function is List Add cValue Return
	* Author : AIM Systems, Inc
	* Date : 2011.03.12
	*/
	private String convertSiteValue(String siteValue)
	{
		double preNumber = 0;
		double exponential = 0;
		double cValue = 0;
		
		if(siteValue.contains("E+")){
			preNumber = Double.valueOf(siteValue.substring(0, siteValue.indexOf("E+"))).doubleValue();
			exponential = Double.valueOf(siteValue.substring(siteValue.indexOf("E+")+1, siteValue.length())).doubleValue();
		
			cValue = preNumber * Math.pow(10, exponential);
			System.out.println(preNumber);
			System.out.println(exponential);
			System.out.println(cValue);
			
		}else if(siteValue.contains("E-")){
			preNumber = Double.valueOf(siteValue.substring(0, siteValue.indexOf("E-"))).doubleValue();
			exponential = Double.valueOf(siteValue.substring(siteValue.indexOf("E-")+1, siteValue.length())).doubleValue();
			
			cValue = preNumber * Math.pow(10, exponential);
			System.out.println(preNumber);
			System.out.println(exponential);
			System.out.println(cValue);
		}
		
		return String.valueOf(cValue).toString();
	}
	
	public static String getNextDCDataId() 
	{
		List<ListOrderedMap> result = GenericServiceProxy.getDcolQueryTemplate().queryForList(SqlStatement.SELECT_NEXT_DCDATAID, new Object[] {});

		return result.get(0).getValue(0).toString();
	}
	
	public static String getCurrDcDataId()
	{
		String sql = "SELECT DCDATAID.CURRVAL FROM DUAL";
		
		List<ListOrderedMap> result = GenericServiceProxy.getDcolQueryTemplate().queryForList(sql, new Object[] {});
		
		return result.get(0).getValue(0).toString();
	}
	
	/**
	 * getDCSiteName
	 * @author Lhkim
	 * @since 2015.04.07
	 * @throws CustomException
	 */
	public List<Map<String, Object>>  getDCSiteName() throws CustomException
	{
		String sql = "SELECT SITENAME,DESCRIPTION,XMIN,XMAX,YMIN,YMAX FROM DCSITE ORDER BY SITENAME";
		
		Object[] bindSet = new Object[] {};
		
		//List<Map<String, Object>> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindSet);
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindSet);
		
		return sqlResult;
	}
	
	/**
	 * getDCSpecItemName
	 * @author Lhkim
	 * @since 2015.04.27
	 * @throws CustomException
	 */
	public List<Map<String, Object>>  getDCSpecItemName() throws CustomException
	{
		String sql = "SELECT ITEMNAME,TARGET,LOWERSPECLIMIT,UPPERSPECLIMIT FROM DCSPECITEM ORDER BY ITEMNAME";
		
		Object[] bindSet = new Object[] {};
		
		//List<Map<String, Object>> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindSet);
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindSet);
		
		return sqlResult;
	}
	
	/**
	 * checkXYLimit
	 * @author Lhkim
	 * @since 2015.04.07
	 * @param x
	 * @param y
	 * @param siteName
	 * @param XMIN
	 * @param XMAX
	 * @param YMIN
	 * @param YMAX
	 * @return sqlResult
	 * @throws CustomException
	 */
	public List<ListOrderedMap>getXYLimit(String X,String Y,String siteName,String XMIN,String XMAX,String YMIN,String YMAX) throws CustomException
	{
		String sql = "SELECT CASE WHEN :X > :XMIN and :X < :XMAX  and :Y >:YMIN and :Y < :YMAX THEN"
				+ " DECODE(SITENAME,'La-01',SITENAME,'Lb-01',SITENAME,'Lb-02',SITENAME,'Lc-01',SITENAME,'Lc-02',SITENAME,'Ld-01',SITENAME,'Ld-02',SITENAME,0)"
				+ " WHEN :X <= :XMIN and :Y < :YMIN  THEN  DECODE(SITENAME,'Le-01',SITENAME,0)"
				+ " WHEN :X >= :XMAX and :Y >= :YMAX THEN  DECODE(SITENAME,'Le-02',SITENAME,0)"
				+ " ELSE 'NotExist' END RESULT"
				+ " FROM DCSITE WHERE SITENAME =:SITENAME";
		
		Map bindMap = new HashMap<String, Object>();
		
		bindMap.put("X", X);
		bindMap.put("Y", Y);
		bindMap.put("XMIN", XMIN);
		bindMap.put("XMAX", XMAX);
		bindMap.put("YMIN", YMIN);
		bindMap.put("YMAX", YMAX);
		bindMap.put("SITENAME", siteName);
		
//		List<ListOrderedMap>  sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		List<ListOrderedMap>  sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return sqlResult;
	}
	
	/**
	 * getDCSpecGrade
	 * @author Lhkim
	 * @since 2015.04.27
	 * @throws CustomException
	 */
	public List<ListOrderedMap> getDCSpecGrade(String L,String LOWERSPECLIMIT,String UPPERSPECLIMIT, String dcItemName) throws CustomException
	{
		String sql = "SELECT "
				+ "CASE "
				+ "	WHEN TO_NUMBER(:L) > TO_NUMBER(:LOWERSPECLIMIT) and TO_NUMBER(:L) <= TO_NUMBER(:UPPERSPECLIMIT) "
				+ " 	 THEN DECODE(TARGET,  "
				+ " 	:S,TARGET,  "
				+ " 	:A,TARGET,  "
				+ "		:B,TARGET,  "
				+ "		:C,TARGET,0)"
				+ "	WHEN TO_NUMBER(:L) <= TO_NUMBER(:LOWERSPECLIMIT) or TO_NUMBER(:L) > TO_NUMBER(:UPPERSPECLIMIT)   "
				+ "		THEN DECODE(TARGET,:Y,TARGET,0)   "
				+ " 	ELSE :NOTELIST END RESULT "
				+ " FROM DCSPECITEM WHERE ITEMNAME =:ITEMNAME";
				
		Map bindMap = new HashMap<String, Object>();
		
		bindMap.put("L", L);
		bindMap.put("S", "S");
		bindMap.put("A", "A");
		bindMap.put("B", "B");
		bindMap.put("C", "C");
		bindMap.put("LOWERSPECLIMIT", LOWERSPECLIMIT);
		bindMap.put("UPPERSPECLIMIT", UPPERSPECLIMIT);
		bindMap.put("Y", "Y");
		bindMap.put("NOTELIST", "NotEList");
		bindMap.put("ITEMNAME", dcItemName);
		
//		List<ListOrderedMap>  sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		List<ListOrderedMap>  sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return sqlResult;
	}
	
	/**
	 * extract DC spec item from message
	 * @author swcho
	 * @since 2016.06.22
	 * @param DCSpecData
	 * @param itemList
	 * @return
	 * @throws CustomException
	 */
	public List<DCSpecItem> getDCSpecItem(DCSpec DCSpecData, List<Element> itemList) throws CustomException
	{
		List<DCSpecItem> result = new ArrayList<DCSpecItem>();
		
		for (Element item : itemList)
		{
			try
			{
				String itemName = SMessageUtil.getChildText(item, "ITEMNAME", true);
				
				DCSpecItemKey keyInfo = new DCSpecItemKey();
				keyInfo.setDCSpecName(DCSpecData.getKey().getDCSpecName());
				keyInfo.setDCSpecVersion(DCSpecData.getKey().getDCSpecVersion());
				keyInfo.setItemName(itemName);
				
				DCSpecItem pseudoItem = new DCSpecItem();
				pseudoItem.setKey(keyInfo);
				pseudoItem.setDataType("String");
				pseudoItem.setLowerControlLimit("0");
				pseudoItem.setLowerScreenLimit("0");
				pseudoItem.setLowerSpecLimit("0");
				pseudoItem.setSiteCount(0);
				pseudoItem.setSiteNames("");
				pseudoItem.setTarget("");
				pseudoItem.setUpperControlLimit("0");
				pseudoItem.setUpperScreenLimit("0");
				pseudoItem.setUpperSpecLimit("0");
				
				result.add(pseudoItem);
			}
			catch (Exception ex)
			{
				log.error(ex.getMessage());
			}
		}
		
		return result;
	}
	
	/**
	 * 
	 * @Name     generateSpcProductProcessDataByReceiveData
	 * @since    2018. 12. 6.
	 * @author   hhlee
	 * @contents 
	 *           
	 * @param elementBodyTemplate
	 * @param machineName
	 * @param unitName
	 * @param subUnitName
	 * @param lotName
	 * @param carrierName
	 * @param productName
	 * @param machineRecipeName
	 * @param processFlowName
	 * @param processOperationName
	 * @param processOperationType
	 * @param factoryName
	 * @param productSpecName
	 * @param woType
	 * @param itemList
	 * @return
	 * @throws CustomException
	 */
	/* 20190423, hhlee, Added ECCODE Item */
	public Element generateSpcProductProcessDataByReceiveData(Element elementBodyTemplate, String machineName, String unitName, String subUnitName,
            String lotName, String carrierName, String productName, String machineRecipeName, String processFlowName, 
            String processOperationName, String processOperationType, String factoryName, String productSpecName, String woType, String ecCode, Element itemList)
            throws CustomException
    {
        
        /*DCTYPE
          FACTORYNAME
          PRODUCTSPECNAME
          PROCESSFLOWNAME
          PROCESSOPERATIONNAME
          MACHINENAME
          UNITNAME
          SUBUNITNAME
          MACHINERECIPENAME
          LOTNAME
          PRODUCTNAME
          PROCESSTIME
          PROCESSID
          WOTYPE
          ITEMLIST
          */
        
        //SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "SPC_ProductProcessData");
        //SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", GenericServiceProxy.getESBServive().getSendSubject("CNXsvr"));
        
        //Element elementBodyTemplate = SMessageUtil.getBodyElement(doc);
        elementBodyTemplate.removeContent();
        
        //boolean result = doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
        //Element elementBodyTemplate = new Element(SMessageUtil.Body_Tag);
        
        /* 2018121206, hhlee,  */
        Element elementDcType = new Element("DCTYPE");
        elementDcType.setText(GenericServiceProxy.getConstantMap().Pos_Production);
        elementBodyTemplate.addContent(elementDcType);
        
        Element elementFactoryName= new Element("FACTORYNAME");
        elementFactoryName.setText(factoryName);
        elementBodyTemplate.addContent(elementFactoryName);
        
        Element elementProductSpecName = new Element("PRODUCTSPECNAME");
        elementProductSpecName.setText(productSpecName);
        elementBodyTemplate.addContent(elementProductSpecName);
        
        Element elementProcessFlowName = new Element("PROCESSFLOWNAME");
        elementProcessFlowName.setText(processFlowName);
        elementBodyTemplate.addContent(elementProcessFlowName);
        
        Element elementProcessOperationName = new Element("PROCESSOPERATIONNAME");
        elementProcessOperationName.setText(processOperationName);
        elementBodyTemplate.addContent(elementProcessOperationName);
        
        Element elementMachineName = new Element("MACHINENAME");
        elementMachineName.setText(machineName);
        elementBodyTemplate.addContent(elementMachineName);
        
        Element elementUnitName = new Element("UNITNAME");
        elementUnitName.setText(unitName);
        elementBodyTemplate.addContent(elementUnitName);
        
        Element elementSubUnitName = new Element("SUBUNITNAME");
        elementSubUnitName.setText(subUnitName);
        elementBodyTemplate.addContent(elementSubUnitName);
        
        Element elementMachineRecipeName = new Element("MACHINERECIPENAME");
        elementMachineRecipeName.setText(machineRecipeName);
        elementBodyTemplate.addContent(elementMachineRecipeName);
        
        Element elementLotName = new Element("LOTNAME");
        elementLotName.setText(lotName);
        elementBodyTemplate.addContent(elementLotName);
        
        Element elementProductName = new Element("PRODUCTNAME");
        elementProductName.setText(productName);
        elementBodyTemplate.addContent(elementProductName);
        
        Element elementProcessTime = new Element("PROCESSTIME");
        elementProcessTime.setText("");
        elementBodyTemplate.addContent(elementProcessTime);
        
        Element elementProcessID = new Element("PROCESSID");
        elementProcessID.setText("");
        elementBodyTemplate.addContent(elementProcessID);
        
        Element elementWoType = new Element("WOTYPE");
        elementWoType.setText(woType);
        elementBodyTemplate.addContent(elementWoType);
                
        /* 20190423, hhlee, Added ECCODE Item */
        Element elementEcCode = new Element("ECCODE");
        elementEcCode.setText(ecCode);
        elementBodyTemplate.addContent(elementEcCode);
        
        //ItemList
        Element itemListElement =itemList;
        elementBodyTemplate.addContent(itemListElement);
        
        //itemListElement = itemList;
        //
        //itemListElement.addContent(itemList);
        
        //eventLog.info(doc.toString());
        
        //elementBodyTemplate.addContent(itemList);
        
        ////overwrite
        //doc.getRootElement().addContent(elementBodyTemplate);
        
        return elementBodyTemplate;    
    }
	
	/**
	 * 
	 * @Name     generateSpcLotProcessDataByReceiveData
	 * @since    2018. 12. 6.
	 * @author   hhlee
	 * @contents 
	 *           
	 * @param elementBodyTemplate
	 * @param machineName
	 * @param unitName
	 * @param subUnitName
	 * @param lotName
	 * @param carrierName
	 * @param machineRecipeName
	 * @param processFlowName
	 * @param processOperationName
	 * @param processOperationType
	 * @param factoryName
	 * @param productSpecName
	 * @param woType
	 * @param itemList
	 * @return
	 * @throws CustomException
	 */
	/* 20190423, hhlee, Added ECCODE Item */
	public Element generateSpcLotProcessDataByReceiveData(Element elementBodyTemplate, String machineName, String unitName, String subUnitName,
            String lotName, String carrierName, String machineRecipeName, String processFlowName, String processOperationName, 
            String processOperationType, String factoryName, String productSpecName, String woType, String ecCode, Element itemList)
            throws CustomException
    {
        
        /*DCTYPE
          FACTORYNAME
          PRODUCTSPECNAME
          PROCESSFLOWNAME
          PROCESSOPERATIONNAME
          MACHINENAME
          UNITNAME
          SUBUNITNAME
          MACHINERECIPENAME
          LOTNAME
          PROCESSTIME
          PROCESSID
          WOTYPE
          ITEMLIST
          */
        
        //SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "SPC_ProductProcessData");
        //SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", GenericServiceProxy.getESBServive().getSendSubject("CNXsvr"));
        
        //Element elementBodyTemplate = SMessageUtil.getBodyElement(doc);
        elementBodyTemplate.removeContent();
        
        //boolean result = doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
        //Element elementBodyTemplate = new Element(SMessageUtil.Body_Tag);
        
        /* 2018121206, hhlee,  */
        Element elementDcType = new Element("DCTYPE");
        elementDcType.setText(GenericServiceProxy.getConstantMap().Pos_Production);
        elementBodyTemplate.addContent(elementDcType);
        
        Element elementFactoryName= new Element("FACTORYNAME");
        elementFactoryName.setText(factoryName);
        elementBodyTemplate.addContent(elementFactoryName);
        
        Element elementProductSpecName = new Element("PRODUCTSPECNAME");
        elementProductSpecName.setText(productSpecName);
        elementBodyTemplate.addContent(elementProductSpecName);
        
        Element elementProcessFlowName = new Element("PROCESSFLOWNAME");
        elementProcessFlowName.setText(processFlowName);
        elementBodyTemplate.addContent(elementProcessFlowName);
        
        Element elementProcessOperationName = new Element("PROCESSOPERATIONNAME");
        elementProcessOperationName.setText(processOperationName);
        elementBodyTemplate.addContent(elementProcessOperationName);
        
        Element elementMachineName = new Element("MACHINENAME");
        elementMachineName.setText(machineName);
        elementBodyTemplate.addContent(elementMachineName);
        
        Element elementUnitName = new Element("UNITNAME");
        elementUnitName.setText(unitName);
        elementBodyTemplate.addContent(elementUnitName);
        
        Element elementSubUnitName = new Element("SUBUNITNAME");
        elementSubUnitName.setText(subUnitName);
        elementBodyTemplate.addContent(elementSubUnitName);
        
        Element elementMachineRecipeName = new Element("MACHINERECIPENAME");
        elementMachineRecipeName.setText(machineRecipeName);
        elementBodyTemplate.addContent(elementMachineRecipeName);
        
        Element elementLotName = new Element("LOTNAME");
        elementLotName.setText(lotName);
        elementBodyTemplate.addContent(elementLotName);
                
        Element elementProcessTime = new Element("PROCESSTIME");
        elementProcessTime.setText("");
        elementBodyTemplate.addContent(elementProcessTime);
        
        Element elementProcessID = new Element("PROCESSID");
        elementProcessID.setText("");
        elementBodyTemplate.addContent(elementProcessID);
        
        Element elementWoType = new Element("WOTYPE");
        elementWoType.setText(woType);
        elementBodyTemplate.addContent(elementWoType);
                                
        /* 20190423, hhlee, Added ECCODE Item */
        Element elementEcCode = new Element("ECCODE");
        elementEcCode.setText(ecCode);
        elementBodyTemplate.addContent(elementEcCode);
        
        //ItemList
        Element itemListElement =itemList;
        elementBodyTemplate.addContent(itemListElement);
        
        //itemListElement = itemList;
        //
        //itemListElement.addContent(itemList);
        
        //eventLog.info(doc.toString());
        
        //elementBodyTemplate.addContent(itemList);
        
        ////overwrite
        //doc.getRootElement().addContent(elementBodyTemplate);
        
        return elementBodyTemplate;    
    }
	
	/**
	 * 
	 * @Name     generateSpcLotProcessDataProcList_V2
	 * @since    2019. 3. 20.
	 * @author   hhlee
	 * @contents 
	 *           
	 * @param eventInfo
	 * @param bodyElement
	 * @param lotName
	 * @param downLoadId
	 * @return
	 * @throws CustomException
	 */
	public Element generateSpcLotProcessDataProcList_V2(EventInfo eventInfo, Element bodyElement, 
            String lotName, String downLoadId) throws CustomException
    {
     
        /*
        TIMEKEY             
        PRODUCTANME         
        FACTORYNAME         
        LOTNAME             
        PRODUCTSPECNAME     
        PROCESSFLOWNAME     
        PROCESSOPERATIONNAME
        MACHINENAME         
        MACHINERECIPENAME   
        UNITNAME            
        UNITRECIPENAME      
        UNITTYPE            
        SUBUNITNAME         
        CREATETIME          
        EVENTNAME           
        EVENTTIMEKEY        
        EVENTUSER           
        */
        
        String elementFactoryName = StringUtil.EMPTY;
        String elementProductSpecName = StringUtil.EMPTY;
        String elementProcessFlowName = StringUtil.EMPTY;
        String elementProcessOperationName = StringUtil.EMPTY;
        String elementMachineName = StringUtil.EMPTY;
        String elementMachineRecipeName = StringUtil.EMPTY;
        
        String elementUnitName = StringUtil.EMPTY;
        String elementUnitRecipe = StringUtil.EMPTY;        
        String elementUnitType = StringUtil.EMPTY; 
        String elementSubUnitName = StringUtil.EMPTY;
        
        String elementUnitNameList = StringUtil.EMPTY;
        String elementUnitRecipeList = StringUtil.EMPTY;        
        String elementUnitTypeList = StringUtil.EMPTY; 
        String elementSubUnitNameList = StringUtil.EMPTY;
        
        String elementLotName = StringUtil.EMPTY;
        String elementEventTimeKey = StringUtil.EMPTY;
        
        String preElementEventTimeKey = StringUtil.EMPTY;   
        
        //PROCLIST
        Element procListElement = new Element("PROCLIST");
        
        List<Map<String, Object>> spcProcessedOperationDataList = null;
        
        if(StringUtil.isNotEmpty(downLoadId))
        {
            try
            {
                String strSql = " SELECT SPO.FACTORYNAME, SPO.LOTNAME, SPO.PRODUCTSPECNAME,                            \n"
                              + "        SPO.PROCESSFLOWNAME, SPO.PROCESSOPERATIONNAME,                                \n"
                              + "        SPO.MACHINENAME, SPO.MACHINERECIPENAME, SPO.UNITNAME,                         \n"
                              + "        SPO.UNITRECIPENAME, SPO.UNITTYPE, SPO.SUBUNITNAME,                            \n"
                              + "        SPO.EVENTTIMEKEY                                                              \n"
                              + "   FROM CT_SPCPROCESSEDOPERATION SPO                                                  \n"
                              + "  WHERE 1=1                                                                           \n"
                              + "    AND SPO.LOTNAME = :LOTNAME                                                        \n"
                              + "    AND SPO.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME                              \n"
                              /* 20190625, hhlee, modify, last eventtime upload ==>> */                                
                              + "    AND SPO.EVENTTIMEKEY = (SELECT MAX(SSPO.EVENTTIMEKEY)                             \n"
                              + "                              FROM CT_SPCPROCESSEDOPERATION SSPO                      \n"
                              + "                             WHERE SSPO.LOTNAME = :LOTNAME                            \n"
                              + "                               AND SSPO.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME) \n"                              
                              /* <<== 20190625, hhlee, modify, last eventtime upload */
                              + "  GROUP BY SPO.FACTORYNAME, SPO.LOTNAME, SPO.PRODUCTSPECNAME,                         \n"
                              + "           SPO.PROCESSFLOWNAME, SPO.PROCESSOPERATIONNAME,                             \n"
                              + "         SPO.MACHINENAME, SPO.MACHINERECIPENAME, SPO.UNITNAME,                        \n"
                              + "         SPO.UNITRECIPENAME, SPO.UNITTYPE, SPO.SUBUNITNAME,                           \n"
                              + "         SPO.EVENTTIMEKEY                                                             \n"
                              + "  ORDER BY SPO.EVENTTIMEKEY, SPO.MACHINENAME, SPO.UNITNAME,                           \n"
                              + "           SPO.SUBUNITNAME                                                              ";
                
                Map<String, Object> bindMap = new HashMap<String, Object>();
                bindMap.put("LOTNAME", lotName);
                bindMap.put("PROCESSOPERATIONNAME", downLoadId);
                
                spcProcessedOperationDataList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
                
                if(spcProcessedOperationDataList != null &&
                        spcProcessedOperationDataList.size() > 0)
                {
                    Element procElement = null;
                                                      
                    for(int i = 0; i < spcProcessedOperationDataList.size() ; i++)
                    {   
                        elementEventTimeKey = spcProcessedOperationDataList.get(i).get("EVENTTIMEKEY").toString();
                                                        
                        if(!StringUtil.equals(preElementEventTimeKey, elementEventTimeKey))
                        {
                            if(StringUtil.isNotEmpty(preElementEventTimeKey))
                            {
                                elementUnitNameList = StringUtil.substring(elementUnitNameList, 0, elementUnitNameList.length() -1);
                                elementUnitTypeList = StringUtil.substring(elementUnitTypeList, 0, elementUnitTypeList.length() -1);          
                                elementUnitRecipeList = StringUtil.substring(elementUnitRecipeList, 0, elementUnitRecipeList.length() -1);
                                elementSubUnitNameList = StringUtil.substring(elementSubUnitNameList, 0, elementSubUnitNameList.length() -1);
                                
                                this.setProcElement(procElement, elementFactoryName, elementProductSpecName, elementProcessFlowName, 
                                        elementProcessOperationName, elementMachineName, elementMachineRecipeName, elementLotName, elementEventTimeKey,
                                        elementUnitNameList, elementUnitTypeList, elementUnitRecipeList, elementSubUnitNameList);
                                
                                procListElement.addContent(procElement);
                            }
                            
                            preElementEventTimeKey = elementEventTimeKey;
                            
                            procElement = new Element("PROC"); 
                            
                            elementFactoryName = spcProcessedOperationDataList.get(i).get("FACTORYNAME") != null ? 
                                    spcProcessedOperationDataList.get(i).get("FACTORYNAME").toString() : StringUtil.EMPTY;
                            elementProductSpecName = spcProcessedOperationDataList.get(i).get("PRODUCTSPECNAME") != null ?
                                    spcProcessedOperationDataList.get(i).get("PRODUCTSPECNAME").toString() : StringUtil.EMPTY;
                            elementProcessFlowName = spcProcessedOperationDataList.get(i).get("PROCESSFLOWNAME") != null ?
                                    spcProcessedOperationDataList.get(i).get("PROCESSFLOWNAME").toString() : StringUtil.EMPTY;
                            elementProcessOperationName = spcProcessedOperationDataList.get(i).get("PROCESSOPERATIONNAME") != null ?
                                    spcProcessedOperationDataList.get(i).get("PROCESSOPERATIONNAME").toString() : StringUtil.EMPTY;
                            elementMachineName = spcProcessedOperationDataList.get(i).get("MACHINENAME") != null ? 
                                    spcProcessedOperationDataList.get(i).get("MACHINENAME").toString() : StringUtil.EMPTY;
                            elementMachineRecipeName = spcProcessedOperationDataList.get(i).get("MACHINERECIPENAME") != null ? 
                                    spcProcessedOperationDataList.get(i).get("MACHINERECIPENAME").toString() : StringUtil.EMPTY;
                            elementLotName = spcProcessedOperationDataList.get(i).get("LOTNAME") != null ? 
                                    spcProcessedOperationDataList.get(i).get("LOTNAME").toString() : StringUtil.EMPTY;
                            
                            elementUnitNameList = StringUtil.EMPTY;
                            elementUnitTypeList = StringUtil.EMPTY;
                            elementUnitRecipeList = StringUtil.EMPTY;
                            elementSubUnitNameList = StringUtil.EMPTY;
                        }
                        
                        elementUnitName = spcProcessedOperationDataList.get(i).get("UNITNAME") != null ?
                                spcProcessedOperationDataList.get(i).get("UNITNAME").toString() : StringUtil.EMPTY;
                        elementUnitType = spcProcessedOperationDataList.get(i).get("UNITTYPE") != null ?
                                spcProcessedOperationDataList.get(i).get("UNITTYPE").toString() : StringUtil.EMPTY;        
                        elementUnitRecipe = spcProcessedOperationDataList.get(i).get("UNITRECIPENAME") != null ?
                                spcProcessedOperationDataList.get(i).get("UNITRECIPENAME").toString() : StringUtil.EMPTY;
                        elementSubUnitName = spcProcessedOperationDataList.get(i).get("SUBUNITNAME") != null ?
                                spcProcessedOperationDataList.get(i).get("SUBUNITNAME").toString() : StringUtil.EMPTY;
                        
                        if(StringUtil.isNotEmpty(elementUnitName))
                        {
                            elementUnitNameList += elementUnitName + ",";
                        }
                        
                        if(StringUtil.isNotEmpty(elementUnitType))
                        {
                            elementUnitTypeList += elementUnitType + ",";
                        }
                        
                        if(StringUtil.isNotEmpty(elementUnitRecipe))
                        {
                            elementUnitRecipeList += elementUnitRecipe + ",";
                        }
                         
                        if(StringUtil.isNotEmpty(elementSubUnitName))
                        {
                            elementSubUnitNameList += elementSubUnitName + ",";
                        }                                                  
                    }
                    
                    elementUnitNameList = StringUtil.substring(elementUnitNameList, 0, elementUnitNameList.length() -1);
                    elementUnitTypeList = StringUtil.substring(elementUnitTypeList, 0, elementUnitTypeList.length() -1);          
                    elementUnitRecipeList = StringUtil.substring(elementUnitRecipeList, 0, elementUnitRecipeList.length() -1);
                    elementSubUnitNameList = StringUtil.substring(elementSubUnitNameList, 0, elementSubUnitNameList.length() -1);
                    
                    this.setProcElement(procElement, elementFactoryName, elementProductSpecName, elementProcessFlowName, 
                            elementProcessOperationName, elementMachineName, elementMachineRecipeName, elementLotName, elementEventTimeKey,
                            elementUnitNameList, elementUnitTypeList, elementUnitRecipeList, elementSubUnitNameList);
                    
                    procListElement.addContent(procElement);
                }
                else
                {
                    procListElement.setText(StringUtil.EMPTY);
                }
            }
            catch(Exception ex)
            {
                log.error("generateSpcLotProcessDataProcList_V2 - " + ex.getMessage());
                procListElement.setText(StringUtil.EMPTY);
            }
        }
        else
        {
            procListElement.setText(StringUtil.EMPTY); 
        }
        
        bodyElement.addContent(procListElement);        
        
        return bodyElement;
    }
	
//	/**
//     * 
//     * @Name     generateSpcLotProcessDataProcList_V2
//     * @since    2019. 2. 26.
//     * @author   hhlee
//     * @contents 
//     *           
//     * @param eventInfo
//     * @param bodyElement
//     * @param lotName
//     * @param downLoadId
//     * @return
//     * @throws CustomException
//     */
//    public Element generateSpcLotProcessDataProcList_V2(EventInfo eventInfo, Element bodyElement, 
//            String lotName, String downLoadId) throws CustomException
//    {
//     
//        /*
//        TIMEKEY             
//        PRODUCTANME         
//        FACTORYNAME         
//        LOTNAME             
//        PRODUCTSPECNAME     
//        PROCESSFLOWNAME     
//        PROCESSOPERATIONNAME
//        MACHINENAME         
//        MACHINERECIPENAME   
//        UNITNAME            
//        UNITRECIPENAME      
//        UNITTYPE            
//        SUBUNITNAME         
//        CREATETIME          
//        EVENTNAME           
//        EVENTTIMEKEY        
//        EVENTUSER           
//        */
//        
//        String elementFactoryName = StringUtil.EMPTY;
//        String elementProductSpecName = StringUtil.EMPTY;
//        String elementProcessFlowName = StringUtil.EMPTY;
//        String elementProcessOperationName = StringUtil.EMPTY;
//        String elementMachineName = StringUtil.EMPTY;
//        String elementMachineRecipeName = StringUtil.EMPTY;
//        String elementUnitName = StringUtil.EMPTY;
//        String elementUnitRecipeName = StringUtil.EMPTY;
//        String elementUnitType = StringUtil.EMPTY;
//        String elementSubUnitName = StringUtil.EMPTY;
//        String elementLotName = StringUtil.EMPTY;
//        String elementEventTimeKey = StringUtil.EMPTY;
//        
//        String preElementEventTimeKey = StringUtil.EMPTY;   
//        
//        //PROCLIST
//        Element procListElement = new Element("PROCLIST");
//        
//        List<Map<String, Object>> spcProcessedOperationDataList = null;
//        
//        if(StringUtil.isNotEmpty(downLoadId))
//        {
//            try
//            {
//                String strSql = " SELECT SPO.FACTORYNAME, SPO.LOTNAME, SPO.PRODUCTSPECNAME,     \n"
//                              + "        SPO.PROCESSFLOWNAME, SPO.PROCESSOPERATIONNAME,         \n"
//                              + "        SPO.MACHINENAME, SPO.MACHINERECIPENAME, SPO.UNITNAME,  \n"
//                              + "        SPO.UNITRECIPENAME, SPO.UNITTYPE, SPO.SUBUNITNAME,     \n"
//                              + "        SPO.EVENTTIMEKEY                                       \n"
//                              + "   FROM CT_SPCPROCESSEDOPERATION SPO                           \n"
//                              + "  WHERE 1=1                                                    \n"
//                              + "    AND SPO.LOTNAME = :LOTNAME                                 \n"
//                              + "    AND SPO.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME       \n"
//                              + "  GROUP BY SPO.FACTORYNAME, SPO.LOTNAME, SPO.PRODUCTSPECNAME,  \n"
//                              + "           SPO.PROCESSFLOWNAME, SPO.PROCESSOPERATIONNAME,      \n"
//                              + "         SPO.MACHINENAME, SPO.MACHINERECIPENAME, SPO.UNITNAME, \n"
//                              + "         SPO.UNITRECIPENAME, SPO.UNITTYPE, SPO.SUBUNITNAME,    \n"
//                              + "         SPO.EVENTTIMEKEY                                      \n"
//                              + "  ORDER BY SPO.EVENTTIMEKEY, SPO.MACHINENAME, SPO.UNITNAME,    \n"
//                              + "           SPO.SUBUNITNAME                                       ";
//                
//                Map<String, Object> bindMap = new HashMap<String, Object>();
//                bindMap.put("LOTNAME", lotName);
//                bindMap.put("PROCESSOPERATIONNAME", downLoadId);
//                
//                spcProcessedOperationDataList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
//                
//                if(spcProcessedOperationDataList != null &&
//                        spcProcessedOperationDataList.size() > 0)
//                {
//                    Element procElement = null;
//                    Element procUnitListElement = null;
//                    Element procUnitRecipeListElement = null;
//                                  
//                    //for(SpcProcessedOperation spcProcessedOperationData : spcProcessedOperationDataList)
//                    for(int i = 0; i < spcProcessedOperationDataList.size() ; i++)
//                    {   
//                        elementEventTimeKey = spcProcessedOperationDataList.get(i).get("EVENTTIMEKEY").toString();
//                                                        
//                        if(!StringUtil.equals(preElementEventTimeKey, elementEventTimeKey))
//                        {
//                            if(StringUtil.isNotEmpty(preElementEventTimeKey))
//                            {
//                                procElement.addContent(procUnitListElement);
//                                procElement.addContent(procUnitRecipeListElement);
//                                procListElement.addContent(procElement);
//                            }
//                            
//                            preElementEventTimeKey = elementEventTimeKey;
//                            
//                            procElement = new Element("PROC"); 
//                            elementFactoryName = spcProcessedOperationDataList.get(i).get("FACTORYNAME") != null ? 
//                                    spcProcessedOperationDataList.get(i).get("FACTORYNAME").toString() : StringUtil.EMPTY;
//                            elementProductSpecName = spcProcessedOperationDataList.get(i).get("PRODUCTSPECNAME") != null ?
//                                    spcProcessedOperationDataList.get(i).get("PRODUCTSPECNAME").toString() : StringUtil.EMPTY;
//                            elementProcessFlowName = spcProcessedOperationDataList.get(i).get("PROCESSFLOWNAME") != null ?
//                                    spcProcessedOperationDataList.get(i).get("PROCESSFLOWNAME").toString() : StringUtil.EMPTY;
//                            elementProcessOperationName = spcProcessedOperationDataList.get(i).get("PROCESSOPERATIONNAME") != null ?
//                                    spcProcessedOperationDataList.get(i).get("PROCESSOPERATIONNAME").toString() : StringUtil.EMPTY;
//                            elementMachineName = spcProcessedOperationDataList.get(i).get("MACHINENAME") != null ? 
//                                    spcProcessedOperationDataList.get(i).get("MACHINENAME").toString() : StringUtil.EMPTY;
//                            elementMachineRecipeName = spcProcessedOperationDataList.get(i).get("MACHINERECIPENAME") != null ? 
//                                    spcProcessedOperationDataList.get(i).get("MACHINERECIPENAME").toString() : StringUtil.EMPTY;
//                            elementLotName = spcProcessedOperationDataList.get(i).get("LOTNAME") != null ? 
//                                    spcProcessedOperationDataList.get(i).get("LOTNAME").toString() : StringUtil.EMPTY;
//                            
//                            this.setProcElement(procElement, elementFactoryName, elementProductSpecName, elementProcessFlowName, 
//                                    elementProcessOperationName, elementMachineName, elementMachineRecipeName, elementLotName, elementEventTimeKey);
//                            
//                            procUnitListElement = new Element("PROCUNITLIST");                        
//                            procUnitRecipeListElement = new Element("UNITRECIPELIST");
//                            
//                        }
//                        
//                        elementUnitName = spcProcessedOperationDataList.get(i).get("UNITNAME") != null ?
//                                spcProcessedOperationDataList.get(i).get("UNITNAME").toString() : StringUtil.EMPTY;
//                        elementUnitType = spcProcessedOperationDataList.get(i).get("UNITTYPE") != null ?
//                                spcProcessedOperationDataList.get(i).get("UNITTYPE").toString() : StringUtil.EMPTY;        
//                        elementUnitRecipeName = spcProcessedOperationDataList.get(i).get("UNITRECIPENAME") != null ?
//                                spcProcessedOperationDataList.get(i).get("UNITRECIPENAME").toString() : StringUtil.EMPTY;
//                        elementSubUnitName = spcProcessedOperationDataList.get(i).get("SUBUNITNAME") != null ?
//                                spcProcessedOperationDataList.get(i).get("SUBUNITNAME").toString() : StringUtil.EMPTY;
//                        
//                        this.setProcUintElement(procUnitListElement, elementUnitName, elementUnitType, elementSubUnitName);    
//                        if(StringUtil.isNotEmpty(elementUnitName))
//                        {
//                            this.setProcUintRecipeElement(procUnitRecipeListElement, elementUnitRecipeName);  
//                        }                          
//                    }
//                    
//                    procElement.addContent(procUnitListElement);
//                    procElement.addContent(procUnitRecipeListElement);
//                    procListElement.addContent(procElement);
//                }
//                else
//                {
//                    procListElement.setText(StringUtil.EMPTY);
//                }
//            }
//            catch(Exception ex)
//            {
//                log.error("generateSpcLotProcessDataProcList_V2 - " + ex.getMessage());
//                procListElement.setText(StringUtil.EMPTY);
//            }
//        }
//        else
//        {
//            procListElement.setText(StringUtil.EMPTY); 
//        }
//        
//        bodyElement.addContent(procListElement);        
//        
//        return bodyElement;
//    }
    
	/**
	 * 
	 * @Name     generateSpcProductProcessDataProcList_V2
	 * @since    2019. 3. 20.
	 * @author   hhlee
	 * @contents 
	 *           
	 * @param eventInfo
	 * @param bodyElement
	 * @param productName
	 * @param downLoadId
	 * @return
	 * @throws CustomException
	 */
    public Element generateSpcProductProcessDataProcList_V2(EventInfo eventInfo, Element bodyElement, 
            String productName, String downLoadId) throws CustomException
    {
     
        /*
        TIMEKEY             
        PRODUCTANME         
        FACTORYNAME         
        LOTNAME             
        PRODUCTSPECNAME     
        PROCESSFLOWNAME     
        PROCESSOPERATIONNAME
        MACHINENAME         
        MACHINERECIPENAME   
        UNITNAME            
        UNITRECIPENAME      
        UNITTYPE            
        SUBUNITNAME         
        CREATETIME          
        EVENTNAME           
        EVENTTIMEKEY        
        EVENTUSER           
        */
        
        String elementFactoryName = StringUtil.EMPTY;
        String elementProductSpecName = StringUtil.EMPTY;
        String elementProcessFlowName = StringUtil.EMPTY;
        String elementProcessOperationName = StringUtil.EMPTY;
        String elementMachineName = StringUtil.EMPTY;
        String elementMachineRecipeName = StringUtil.EMPTY;
        
        String elementUnitNameList = StringUtil.EMPTY;
        String elementUnitRecipeList = StringUtil.EMPTY;        
        String elementUnitTypeList = StringUtil.EMPTY; 
        String elementSubUnitNameList = StringUtil.EMPTY;
        
        String elementLotName = StringUtil.EMPTY;
        String elementEventTimeKey = StringUtil.EMPTY;
        
        String preElementEventTimeKey = StringUtil.EMPTY;   
        
        //PROCLIST
        Element procListElement = new Element("PROCLIST");
        
        List<SpcProcessedOperation> spcProcessedOperationDataList = null;
        
        if(StringUtil.isNotEmpty(downLoadId))
        {
            try
            {                   
                /* 20190625, hhlee, modify, last eventtime upload ==>> */
                //spcProcessedOperationDataList = ExtendedObjectProxy.getSpcProcessedOperationService().select
                //        (" WHERE PRODUCTNAME = ? AND PROCESSOPERATIONNAME = ? ORDER BY EVENTTIMEKEY, MACHINENAME, UNITNAME, SUBUNITNAME ", 
                //               new Object[]{productName, downLoadId});
                spcProcessedOperationDataList = ExtendedObjectProxy.getSpcProcessedOperationService().select
                        (" WHERE PRODUCTNAME = ? AND EVENTTIMEKEY = " +
                                 " (SELECT MAX(EVENTTIMEKEY) FROM CT_SPCPROCESSEDOPERATION  WHERE PRODUCTNAME = ? AND PROCESSOPERATIONNAME = ?) " +
                         " ORDER BY TIMEKEY, MACHINENAME, UNITNAME, SUBUNITNAME ", 
                               new Object[]{productName, productName, downLoadId}); 
                /* <<== 20190625, hhlee, modify, last eventtime upload */
                
                if(spcProcessedOperationDataList != null &&
                        spcProcessedOperationDataList.size() > 0)
                {
                    Element procElement = null;
                    for(SpcProcessedOperation spcProcessedOperationData : spcProcessedOperationDataList)
                    {   
                        elementEventTimeKey = spcProcessedOperationData.getEventTimeKey();
                                                        
                        if(!StringUtil.equals(preElementEventTimeKey, elementEventTimeKey))
                        {
                            if(StringUtil.isNotEmpty(preElementEventTimeKey))
                            {
                                elementUnitNameList = StringUtil.substring(elementUnitNameList, 0, elementUnitNameList.length() -1);
                                elementUnitTypeList = StringUtil.substring(elementUnitTypeList, 0, elementUnitTypeList.length() -1);          
                                elementUnitRecipeList = StringUtil.substring(elementUnitRecipeList, 0, elementUnitRecipeList.length() -1);
                                elementSubUnitNameList = StringUtil.substring(elementSubUnitNameList, 0, elementSubUnitNameList.length() -1);
                                
                                this.setProcElement(procElement, elementFactoryName, elementProductSpecName, elementProcessFlowName, 
                                        elementProcessOperationName, elementMachineName, elementMachineRecipeName, elementLotName, elementEventTimeKey,
                                        elementUnitNameList, elementUnitTypeList, elementUnitRecipeList, elementSubUnitNameList);
                                
                                procListElement.addContent(procElement);                                
                            }
                            
                            preElementEventTimeKey = elementEventTimeKey;
                            
                            procElement = new Element("PROC"); 
                            
                            elementFactoryName = spcProcessedOperationData.getFactoryName();
                            elementProductSpecName = spcProcessedOperationData.getProductSpecName();
                            elementProcessFlowName = spcProcessedOperationData.getProcessFlowName();
                            elementProcessOperationName = spcProcessedOperationData.getProcessOperationName();
                            elementMachineName = spcProcessedOperationData.getMachineName();
                            elementMachineRecipeName = spcProcessedOperationData.getMachineRecipeName();
                            elementLotName = spcProcessedOperationData.getLotName();
                            
                            elementUnitNameList = StringUtil.EMPTY;
                            elementUnitTypeList = StringUtil.EMPTY;
                            elementUnitRecipeList = StringUtil.EMPTY;
                            elementSubUnitNameList = StringUtil.EMPTY;
                        }
                        
                        if(StringUtil.isNotEmpty(spcProcessedOperationData.getUnitName()))
                        {
                            elementUnitNameList += spcProcessedOperationData.getUnitName() + ",";
                        }
                        
                        if(StringUtil.isNotEmpty(spcProcessedOperationData.getUnitType()))
                        {
                            elementUnitTypeList += spcProcessedOperationData.getUnitType() + ",";
                        }
                         
                        if(StringUtil.isNotEmpty(spcProcessedOperationData.getUnitRecipeName()))
                        {
                            elementUnitRecipeList += spcProcessedOperationData.getUnitRecipeName() + ",";
                        }
                        
                        if(StringUtil.isNotEmpty(spcProcessedOperationData.getSubUnitName()))
                        {
                            elementSubUnitNameList = spcProcessedOperationData.getSubUnitName() + ","; 
                        }                                               
                    }
                    
                    elementUnitNameList = StringUtil.substring(elementUnitNameList, 0, elementUnitNameList.length() -1);
                    elementUnitTypeList = StringUtil.substring(elementUnitTypeList, 0, elementUnitTypeList.length() -1);          
                    elementUnitRecipeList = StringUtil.substring(elementUnitRecipeList, 0, elementUnitRecipeList.length() -1);
                    elementSubUnitNameList = StringUtil.substring(elementSubUnitNameList, 0, elementSubUnitNameList.length() -1);
                    
                    this.setProcElement(procElement, elementFactoryName, elementProductSpecName, elementProcessFlowName, 
                            elementProcessOperationName, elementMachineName, elementMachineRecipeName, elementLotName, elementEventTimeKey,
                            elementUnitNameList, elementUnitTypeList, elementUnitRecipeList, elementSubUnitNameList);
                    
                    procListElement.addContent(procElement);
                }
                else
                {
                    procListElement.setText(StringUtil.EMPTY);
                }
            }
            catch(Exception ex)
            {
                log.error("generateSpcProductProcessDataProcList_V2 - " + ex.getMessage());
                procListElement.setText(StringUtil.EMPTY);
            }
        }
        else
        {
            procListElement.setText(StringUtil.EMPTY); 
        }
        
        bodyElement.addContent(procListElement);        
        
        return bodyElement;
    }
	
//    /**
//     * 
//     * @Name     generateSpcProductProcessDataProcList_V2
//     * @since    2019. 2. 26.
//     * @author   hhlee
//     * @contents 
//     *           
//     * @param eventInfo
//     * @param bodyElement
//     * @param productName
//     * @param downLoadId
//     * @return
//     * @throws CustomException
//     */
//    public Element generateSpcProductProcessDataProcList_V2(EventInfo eventInfo, Element bodyElement, 
//            String productName, String downLoadId) throws CustomException
//    {
//     
//        /*
//        TIMEKEY             
//        PRODUCTANME         
//        FACTORYNAME         
//        LOTNAME             
//        PRODUCTSPECNAME     
//        PROCESSFLOWNAME     
//        PROCESSOPERATIONNAME
//        MACHINENAME         
//        MACHINERECIPENAME   
//        UNITNAME            
//        UNITRECIPENAME      
//        UNITTYPE            
//        SUBUNITNAME         
//        CREATETIME          
//        EVENTNAME           
//        EVENTTIMEKEY        
//        EVENTUSER           
//        */
//        
//        String elementFactoryName = StringUtil.EMPTY;
//        String elementProductSpecName = StringUtil.EMPTY;
//        String elementProcessFlowName = StringUtil.EMPTY;
//        String elementProcessOperationName = StringUtil.EMPTY;
//        String elementMachineName = StringUtil.EMPTY;
//        String elementMachineRecipeName = StringUtil.EMPTY;
//        String elementUnitName = StringUtil.EMPTY;        
//        String elementUnitRecipeName = StringUtil.EMPTY;
//        String elementUnitType = StringUtil.EMPTY;        
//        String elementSubUnitName = StringUtil.EMPTY;
//        String elementLotName = StringUtil.EMPTY;
//        String elementEventTimeKey = StringUtil.EMPTY;
//        
//        String preElementEventTimeKey = StringUtil.EMPTY;   
//        
//        //PROCLIST
//        Element procListElement = new Element("PROCLIST");
//        
//        List<SpcProcessedOperation> spcProcessedOperationDataList = null;
//        
//        if(StringUtil.isNotEmpty(downLoadId))
//        {
//            try
//            {                   
//                spcProcessedOperationDataList = ExtendedObjectProxy.getSpcProcessedOperationService().select
//                        (" WHERE PRODUCTNAME = ? AND PROCESSOPERATIONNAME = ? ORDER BY EVENTTIMEKEY, MACHINENAME, UNITNAME, SUBUNITNAME ", 
//                               new Object[]{productName, downLoadId});                
//                
//                if(spcProcessedOperationDataList != null &&
//                        spcProcessedOperationDataList.size() > 0)
//                {
//                    Element procElement = null;
//                    Element procUnitListElement = null;
//                    Element procUnitRecipeListElement = null;
//                                  
//                    for(SpcProcessedOperation spcProcessedOperationData : spcProcessedOperationDataList)
//                    {   
//                        elementEventTimeKey = spcProcessedOperationData.getEventTimeKey();
//                                                        
//                        if(!StringUtil.equals(preElementEventTimeKey, elementEventTimeKey))
//                        {
//                            if(StringUtil.isNotEmpty(preElementEventTimeKey))
//                            {
//                                procElement.addContent(procUnitListElement);
//                                procElement.addContent(procUnitRecipeListElement);
//                                procListElement.addContent(procElement);
//                            }
//                            
//                            preElementEventTimeKey = elementEventTimeKey;
//                            
//                            procElement = new Element("PROC"); 
//                            
//                            elementFactoryName = spcProcessedOperationData.getFactoryName();
//                            elementProductSpecName = spcProcessedOperationData.getProductSpecName();
//                            elementProcessFlowName = spcProcessedOperationData.getProcessOperationName();
//                            elementProcessOperationName = spcProcessedOperationData.getProcessOperationName();
//                            elementMachineName = spcProcessedOperationData.getMachineName();
//                            elementMachineRecipeName = spcProcessedOperationData.getMachineRecipeName();
//                            elementLotName = spcProcessedOperationData.getLotName();
//                            
//                            this.setProcElement(procElement, elementFactoryName, elementProductSpecName, elementProcessFlowName, 
//                                    elementProcessOperationName, elementMachineName, elementMachineRecipeName, elementLotName, elementEventTimeKey);
//                            
//                            procUnitListElement = new Element("PROCUNITLIST");                        
//                            procUnitRecipeListElement = new Element("UNITRECIPELIST");
//                            
//                        }
//                        
//                        elementUnitName = spcProcessedOperationData.getUnitName();
//                        elementUnitType = spcProcessedOperationData.getUnitType();                    
//                        elementUnitRecipeName = spcProcessedOperationData.getUnitRecipeName();
//                        elementSubUnitName = spcProcessedOperationData.getSubUnitName();
//                        
//                        this.setProcUintElement(procUnitListElement, elementUnitName, elementUnitType, elementSubUnitName);    
//                        if(StringUtil.isNotEmpty(elementUnitName))
//                        {
//                            this.setProcUintRecipeElement(procUnitRecipeListElement, elementUnitRecipeName);
//                        }
//                    }
//                    
//                    procElement.addContent(procUnitListElement);
//                    procElement.addContent(procUnitRecipeListElement);
//                    procListElement.addContent(procElement);
//                }
//                else
//                {
//                    procListElement.setText(StringUtil.EMPTY);
//                }
//            }
//            catch(Exception ex)
//            {
//                log.error("generateSpcProductProcessDataProcList_V2 - " + ex.getMessage());
//                procListElement.setText(StringUtil.EMPTY);
//            }
//        }
//        else
//        {
//            procListElement.setText(StringUtil.EMPTY); 
//        }
//        
//        bodyElement.addContent(procListElement);        
//        
//        return bodyElement;
//    }
    
    /**
     * 
     * @Name     setProcElement
     * @since    2019. 3. 20.
     * @author   hhlee
     * @contents 
     *           
     * @param procElement
     * @param elementFactoryName
     * @param elementProductSpecName
     * @param elementProcessFlowName
     * @param elementProcessOperationName
     * @param elementMachineName
     * @param elementMachineRecipeName
     * @param elementLotName
     * @param elementEventTimeKey
     * @param elementUnitNameList
     * @param elementUnitTypeList
     * @param elementUnitRecipeList
     * @param elementSubUnitNameList
     * @return
     */
    private Element setProcElement(Element procElement, String elementFactoryName, String elementProductSpecName, String elementProcessFlowName, 
            String elementProcessOperationName, String elementMachineName, String elementMachineRecipeName, String elementLotName, String elementEventTimeKey,
            String elementUnitNameList, String elementUnitTypeList, String elementUnitRecipeList, String elementSubUnitNameList)
    {
        Element factoryNameElement = new Element("FACTORYNAME");
        factoryNameElement.setText(elementFactoryName);
        procElement.addContent(factoryNameElement);
        
        Element productSpecNameElement = new Element("PRODUCTSPECNAME");
        productSpecNameElement.setText(elementProductSpecName);
        procElement.addContent(productSpecNameElement);
        
        Element processFlowNameElement = new Element("PROCESSFLOWNAME");
        processFlowNameElement.setText(elementProcessFlowName);
        procElement.addContent(processFlowNameElement);
        
        Element processOperationNameElement = new Element("PROCESSOPERATIONNAME");
        processOperationNameElement.setText(elementProcessOperationName);
        procElement.addContent(processOperationNameElement);
        
        Element machineNameElement = new Element("MACHINENAME");
        machineNameElement.setText(elementMachineName);
        procElement.addContent(machineNameElement);
        
        Element machineRecipeNameElement = new Element("MACHINERECIPENAME");
        machineRecipeNameElement.setText(elementMachineRecipeName);
        procElement.addContent(machineRecipeNameElement);
        
        Element lotNameElement = new Element("LOTNAME");
        lotNameElement.setText(elementLotName);
        procElement.addContent(lotNameElement);        
                        
        Element processTimeElement = new Element("PROCESSTIME");
        processTimeElement.setText(elementEventTimeKey);
        procElement.addContent(processTimeElement);
        
        /* 20190320, hhlee, Changed(add) Element(MessageSpec Change) ==>> */
        Element unitNameListElement = new Element("UNITNAMELIST");
        unitNameListElement.setText(elementUnitNameList);
        procElement.addContent(unitNameListElement);
        
        Element unitTypeListElement = new Element("UNITTYPELIST");
        unitTypeListElement.setText(elementUnitTypeList);
        procElement.addContent(unitTypeListElement);
        
        Element unitRecipeListElement = new Element("UNITRECIPELIST");
        unitRecipeListElement.setText(elementUnitRecipeList);
        procElement.addContent(unitRecipeListElement);
        
        Element subUnitNameListElement = new Element("SUBUNITNAMELIST");
        subUnitNameListElement.setText(elementSubUnitNameList);
        procElement.addContent(subUnitNameListElement);
        /* <<== 20190320, hhlee, Changed(add) Element(MessageSpec Change) */
        
        return procElement;
    }
    
    /**
     * 
     * @Name     setProcUintElement
     * @since    2019. 2. 26.
     * @author   hhlee
     * @contents 
     *           
     * @param procUnitListElement
     * @param elementUnitName
     * @param elementUnitType
     * @param elementSubUnitName
     * @return
     */
    private Element setProcUintElement(Element procUnitListElement, String elementUnitName, String elementUnitType, String elementSubUnitName)
    {        
        if(StringUtil.isNotEmpty(elementUnitName))
        {
            Element procUnitElement = new Element("PROCUNIT");
            
            Element unitNameElement = new Element("UNITNAME");
            unitNameElement.setText(elementUnitName);
            procUnitElement.addContent(unitNameElement);
              
            Element unitTypeElement = new Element("UNITTYPE");
            unitTypeElement.setText(elementUnitType);                       
            procUnitElement.addContent(unitTypeElement);
            
            Element procSubUnitListElement = new Element("PROCSUBUNITLIST");
            if(StringUtil.isNotEmpty(elementSubUnitName))
            {                            
                Element procSubUnitElement = new Element("PROCSUBUNIT");
                
                Element subUnitNameElement = new Element("SUBUNITNAME");
                subUnitNameElement.setText(elementSubUnitName);
                procSubUnitElement.addContent(subUnitNameElement);
                                    
                procSubUnitListElement.addContent(procSubUnitElement);   
            }
            else
            {
                procSubUnitListElement.setText(StringUtil.EMPTY);
            }
            
            procUnitElement.addContent(procSubUnitListElement);
            
            procUnitListElement.addContent(procUnitElement);
        }
        else
        {
            //if(procUnitListElement.getContentSize() <= 0)
            //{
            //    procUnitListElement.setText(StringUtil.EMPTY);
            //}            
            Element procUnitElement = new Element("PROCUNIT");
            procUnitElement.setText(StringUtil.EMPTY);
            procUnitListElement.addContent(procUnitElement);
        }   
        
        return procUnitListElement;
    }
    
    /**
     * 
     * @Name     setProcUintRecipeElement
     * @since    2019. 2. 26.
     * @author   hhlee
     * @contents 
     *           
     * @param procUnitRecipeListElement
     * @param elementUnitRecipeName
     * @return
     */
    private Element setProcUintRecipeElement(Element procUnitRecipeListElement, String elementUnitRecipeName)
    {        
        if(StringUtil.isNotEmpty(elementUnitRecipeName))
        {
            Element procUnitRecipeElement = new Element("UNITRECIPE");
            
            Element unitRecipeNameElement = new Element("UNITRECIPENAME");
            unitRecipeNameElement.setText(elementUnitRecipeName);
            procUnitRecipeElement.addContent(unitRecipeNameElement);
            
            procUnitRecipeListElement.addContent(procUnitRecipeElement);
        }
        else
        {
            //if(procUnitRecipeListElement.getContentSize() <= 0)
            //{
            //    procUnitRecipeListElement.setText(StringUtil.EMPTY);
            //}
            Element procUnitRecipeElement = new Element("UNITRECIPE");
            procUnitRecipeElement.setText(StringUtil.EMPTY);
            procUnitRecipeListElement.addContent(procUnitRecipeElement);
            
        }   
        
        return procUnitRecipeListElement;
    }
    
    /**
     * 
     * @Name     setProcUnitList
     * @since    2019. 2. 22.
     * @author   hhlee
     * @contents 
     *           
     * @param elementUnitNameList
     * @param elementUnitTypeList
     * @param elementSubUnitNameList
     * @return
     * @throws CustomException
     */
    private Element setProcUnitList(String elementUnitNameList, String elementUnitTypeList, String elementSubUnitNameList) throws CustomException
    {
        Element procUnitListElement = new Element("PROCUNITLIST");
        if(StringUtil.isNotEmpty(elementUnitNameList))
        {            
            String[] splitUnitName = StringUtil.split(elementUnitNameList , "|");
            String[] splitUnitType = StringUtil.split(elementUnitTypeList , "|");
            for(int j = 0; j < splitUnitName.length; j++ )
            {   
                Element procUnitElement = new Element("PROCUNIT");
                
                Element unitNameElement = new Element("UNITNAME");
                unitNameElement.setText(splitUnitName[j]);
                procUnitElement.addContent(unitNameElement);
                  
                Element unitTypeElement = new Element("UNITTYPE");
                unitTypeElement.setText(splitUnitType[j]);
               
                procUnitElement.addContent(unitTypeElement);
                
                procUnitElement.addContent(this.setProcSubUnitList(splitUnitName[j], elementSubUnitNameList));
                                
                procUnitListElement.addContent(procUnitElement);
            }                             
        }
        else
        {   
            procUnitListElement.setText(StringUtil.EMPTY);        
        }
        
        return procUnitListElement;        
    }
    
    /**
     * 
     * @Name     setUnitRecipeList
     * @since    2019. 2. 22.
     * @author   hhlee
     * @contents 
     *           
     * @param elementUnitRecipeList
     * @return
     */
    private Element setUnitRecipeList(String elementUnitRecipeList)
    {
        Element UnitRecipeListElement = new Element("UNITRECIPELIST");
        if(StringUtil.isNotEmpty(elementUnitRecipeList))
        {
            try
            {
                //Element unitRecipeListElement = new Element("UNITRECIPELIST");
                //unitRecipeListElement.setText(elementUnitRecipeList);
                //procElement.addContent(unitRecipeListElement);
                
                String[] splitUnitRecipeName = StringUtil.split(elementUnitRecipeList, "|");
                            
                for(int j = 0; j < splitUnitRecipeName.length; j++ )
                {
                    Element UnitRecipeElement = new Element("UNITRECIPE");
                    
                    Element unitRecipeNameElement = new Element("UNITRECIPENAME");
                    unitRecipeNameElement.setText(splitUnitRecipeName[j]);
                    
                    UnitRecipeElement.addContent(unitRecipeNameElement);    
                    
                    UnitRecipeListElement.addContent(UnitRecipeElement);
                }  
            }
            catch (Exception ex)
            {
                log.error("setUnitRecipeList - " + ex.getMessage());
                UnitRecipeListElement.setText(StringUtil.EMPTY);
            } 
        }
        else
        {
            UnitRecipeListElement.setText(StringUtil.EMPTY);            
        }
        
        return UnitRecipeListElement;
    }
    
    /**
     * 
     * @Name     setProcSubUnitList
     * @since    2019. 2. 14.
     * @author   hhlee
     * @contents 
     *           
     * @param unitName
     * @param elementSubUnitNameList
     * @return
     * @throws CustomException
     */
    private Element setProcSubUnitList(String unitName, String elementSubUnitNameList) throws CustomException
    {
        Element procSubUnitListElement = new Element("PROCSUBUNITLIST");
        if(StringUtil.isNotEmpty(elementSubUnitNameList))
        {
            try
            {
                String[] splitSubUnitNameList = StringUtil.split(elementSubUnitNameList , "|");
                for(int i = 0; i < splitSubUnitNameList.length; i++ )
                {
                    if(StringUtil.isNotEmpty(splitSubUnitNameList[i]))
                    {
                        String[] splitSubUnitName = StringUtil.split(splitSubUnitNameList[i] , "=");
                        
                        if(StringUtil.equals(unitName, splitSubUnitName[0]))
                        {
                            Element procSubUnitElement = new Element("PROCSUBUNIT");
                            
                            Element subUnitNameElement = new Element("SUBUNITNAME");
                            subUnitNameElement.setText(splitSubUnitName[1]);
                            procSubUnitElement.addContent(subUnitNameElement);
                                                
                            procSubUnitListElement.addContent(procSubUnitElement);   
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                log.error("setProcSubUnitList - " + ex.getMessage());
                procSubUnitListElement.setText(StringUtil.EMPTY);
            } 
        }
        else
        {                 
            procSubUnitListElement.setText(StringUtil.EMPTY);        
        }
        
        return procSubUnitListElement;        
    }
    
    
    
    
	/**
     * 
     * @Name     generateSpcProductProcessDataProcList
     * @since    2018. 10. 9.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param bodyElement
     * @param factoryName
     * @param productSpecName
     * @param processFlowName
     * @param processOperationName
     * @param lotName
     * @param productName
     * @param machineName
     * @param unitName
     * @param machineRecipeName
     * @return
     * @throws CustomException
     */
    public Element generateSpcProductProcessDataProcList(EventInfo eventInfo, Element bodyElement, String factoryName, 
            String productSpecName, String processFlowName,  String processOperationName, String lotName, String productName, 
            String machineName, String unitName, String machineRecipeName) throws CustomException
    {
     
        //Element bodyElement = SMessageUtil.getBodyElement(doc);
        
        /*FACTORYNAME
        PRODUCTSPECNAME
        PROCESSFLOWNAME
        PROCESSOPERATIONNAME
        MACHINENAME
        MACHINERECIPENAME
        PROCUNITLIST
        PROCUNITTYPE
        UNITRECIPELIST
        LOTNAME
        PROCESSTIME*/
        
        //PROCLIST
        Element procListElement = new Element("PROCLIST");
        
        try
        {
            List<Map<String, Object>> processOperationData = ExtendedObjectProxy.getProcessedOperationService().getProcessedOperationList(productName);
            
            if(processOperationData != null &&
                    processOperationData.size() > 0)
            {        
                String elementFactoryName = StringUtil.EMPTY;
                String elementProductSpecName = StringUtil.EMPTY;
                String elementProcessFlowName = StringUtil.EMPTY;
                String elementProcessOperationName = StringUtil.EMPTY;
                String elementMachineName = StringUtil.EMPTY;
                String elementMachineRecipeName = StringUtil.EMPTY;
                String elementUnitNameList = StringUtil.EMPTY;
                String elementUnitRecipeList = StringUtil.EMPTY;
                String elementUnitTypeName = StringUtil.EMPTY;
                String elementLotName = StringUtil.EMPTY;
                String elementEventdTimeKey = StringUtil.EMPTY;
                
                /* 20190214, hhlee, add, PROCSUBUNITLIST added ==>> */
                String elementSubUnitNameList = StringUtil.EMPTY;
                /* <<== 20190214, hhlee, add, PROCSUBUNITLIST added */
                            
                for(int i = 0; i < processOperationData.size(); i++ )
                {                  
                    /* 20190123, hhlee, modify, change Query ==>> */
                    //elementFactoryName = processOperationData.get(i).get("FACTORYNAME").toString().trim();
                    //elementProductSpecName = processOperationData.get(i).get("PRODUCTSPECNAME").toString().trim();
                    //elementProcessFlowName = processOperationData.get(i).get("PROCESSFLOWNAME").toString().trim();
                    //elementProcessOperationName = processOperationData.get(i).get("PROCESSOPERATIONNAME").toString().trim();
                    //elementMachineName = processOperationData.get(i).get("PROCESSMACHINENAME").toString().trim();
                    //elementMachineRecipeName = processOperationData.get(i).get("MACHINERECIPENAME").toString().trim();
                    //elementUnitNameList = processOperationData.get(i).get("UNITNAMELIST").toString().trim();
                    //elementUnitRecipeList = processOperationData.get(i).get("UNITRECIPELIST").toString().trim();
                    //elementLotName = processOperationData.get(i).get("LOTNAME").toString().trim();
                    //elementEventdTimeKey = processOperationData.get(i).get("EVENTTIMEKEY").toString().trim();
                    
                    elementFactoryName = processOperationData.get(i).get("FACTORYNAME").toString();
                    elementProductSpecName = processOperationData.get(i).get("PRODUCTSPECNAME").toString();
                    elementProcessFlowName = processOperationData.get(i).get("PROCESSFLOWNAME").toString();
                    elementProcessOperationName = processOperationData.get(i).get("PROCESSOPERATIONNAME").toString();
                    elementMachineName = processOperationData.get(i).get("PROCESSMACHINENAME").toString();
                    elementMachineRecipeName = processOperationData.get(i).get("MACHINERECIPENAME").toString();
                    elementUnitNameList = processOperationData.get(i).get("UNITNAMELIST").toString();
                    elementUnitRecipeList = processOperationData.get(i).get("UNITRECIPELIST").toString();
                    elementLotName = processOperationData.get(i).get("LOTNAME").toString();
                    elementEventdTimeKey = processOperationData.get(i).get("EVENTTIMEKEY").toString();
                    /* <<== 20190123, hhlee, modify, change Query */
                    
                    /* 20190214, hhlee, add, PROCSUBUNITLIST added ==>> */
                    elementSubUnitNameList = processOperationData.get(i).get("SUBUNITNAMELIST").toString();
                    /* <<== 20190214, hhlee, add, PROCSUBUNITLIST added */
                    
                    Element procElement = new Element("PROC");
                    
                    Element factoryNameElement = new Element("FACTORYNAME");
                    factoryNameElement.setText(elementFactoryName);
                    procElement.addContent(factoryNameElement);
                    
                    Element productSpecNameElement = new Element("PRODUCTSPECNAME");
                    productSpecNameElement.setText(elementProductSpecName);
                    procElement.addContent(productSpecNameElement);
                    
                    Element processFlowNameElement = new Element("PROCESSFLOWNAME");
                    processFlowNameElement.setText(elementProcessFlowName);
                    procElement.addContent(processFlowNameElement);
                    
                    Element processOperationNameElement = new Element("PROCESSOPERATIONNAME");
                    processOperationNameElement.setText(elementProcessOperationName);
                    procElement.addContent(processOperationNameElement);
                    
                    Element machineNameElement = new Element("MACHINENAME");
                    machineNameElement.setText(elementMachineName);
                    procElement.addContent(machineNameElement);
                    
                    Element machineRecipeNameElement = new Element("MACHINERECIPENAME");
                    machineRecipeNameElement.setText(elementMachineRecipeName);
                    procElement.addContent(machineRecipeNameElement);
                    
                    /* 20190214, hhlee, add, PROCSUBUNITLIST added ==>> */
                    //procElement.addContent(setProcUnitList(elementUnitNameList));
                    procElement.addContent(this.setProcUnitList(elementUnitNameList, elementSubUnitNameList));
                    /* <<== 20190214, hhlee, add, PROCSUBUNITLIST added */
                    
                    procElement.addContent(setUnitRecipeList(elementUnitRecipeList)); 
                    
                    Element lotNameElement = new Element("LOTNAME");
                    lotNameElement.setText(elementLotName);
                    procElement.addContent(lotNameElement);        
                    
                    //Element eventTimeKeyElement = new Element("EVENTTIMEKEY");
                    //eventTimeKeyElement.setText(elementEventdTimeKey);
                    //procElement.addContent(eventTimeKeyElement);
                    
                    Element processTimeElement = new Element("PROCESSTIME");
                    processTimeElement.setText(elementEventdTimeKey);
                    procElement.addContent(processTimeElement);
                    
                    procListElement.addContent(procElement);
                }
            }
        }
        catch (Exception ex)
        {
            log.error("generateSpcProductProcessDataProcList - " + ex.getMessage()); 
        }
        
        bodyElement.addContent(procListElement);        
        
        return bodyElement;
    }
    
    /**
     * 
     * @Name     setProcUnitList
     * @since    2018. 10. 9.
     * @author   hhlee
     * @contents 
     *           
     * @param elementUnitNameList
     * @return
     * @throws CustomException
     */
    /* 20190214, hhlee, add, PROCSUBUNITLIST added*/
    //private Element setProcUnitList(String elementUnitNameList) throws CustomException
    private Element setProcUnitList(String elementUnitNameList, String elementSubUnitNameList) throws CustomException
    {
        String procUnitType = StringUtil.EMPTY;
        Element procUnitListElement = new Element("PROCUNITLIST");
        if(StringUtil.isNotEmpty(elementUnitNameList))
        {
            try
            {
                //procUnitType = MESMachineServiceProxy.getMachineServiceUtil().getMachineTypeList(elementUnitNameList);                    
                //Element procUnitListElement = new Element("PROCUNITLIST");
                //procUnitListElement.setText(elementUnitNameList);
                //procElement.addContent(procUnitListElement);
                
                //Element procUnitTypeElement = new Element("PROCUNITTYPE");
                //procUnitTypeElement.setText(procUnitType);
                //procElement.addContent(procUnitTypeElement);
                
                String[] splitUnitName = StringUtil.split(elementUnitNameList , "|");
                String[] splitUnitType = StringUtil.split(procUnitType , "|");
                MachineSpec machineSpecData = null;
                for(int j = 0; j < splitUnitName.length; j++ )
                {
                    
                    Element procUnitElement = new Element("PROCUNIT");
                    
                    Element unitNameElement = new Element("UNITNAME");
                    unitNameElement.setText(splitUnitName[j]);
                    procUnitElement.addContent(unitNameElement);
                    
                    procUnitType = StringUtil.EMPTY; 
                    try
                    {
                        //procUnitType = MESMachineServiceProxy.getMachineServiceUtil().getMachineTypeList(splitUnitName[j]);
                        machineSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(splitUnitName[j]);
                        procUnitType = machineSpecData.getMachineType();  
                    }
                    catch(Exception ex)
                    {                    
                    }                
                      
                    Element unitTypeElement = new Element("UNITTYPE");
                    unitTypeElement.setText(procUnitType);
                    //if(j <= splitUnitType.length - 1)
                    //{                            
                    //    unitTypeElement.setText(splitUnitType[j]);                           
                    //}
                    //else
                    //{
                    //    unitTypeElement.setText(StringUtil.EMPTY);
                    //}
                    procUnitElement.addContent(unitTypeElement);
                    
                    /* 20190214, hhlee, add, PROCSUBUNITLIST added ==>> */
                    procUnitElement.addContent(this.setProcSubUnitList(splitUnitName[j], elementSubUnitNameList));
                    /* 20190214, hhlee, add, PROCSUBUNITLIST added ==>> */
                    
                    procUnitListElement.addContent(procUnitElement);
                }   
            }
            catch (Exception ex)
            {
                log.error("setProcUnitList - " + ex.getMessage());
                procUnitListElement.setText(StringUtil.EMPTY);
            }            
        }
        else
        {                 
            //Element procUnitListElement = new Element("PROCUNITLIST");
            //procUnitListElement.setText(StringUtil.EMPTY);
            //procElement.addContent(procUnitListElement);
            
            //Element procUnitTypeElement = new Element("PROCUNITTYPE");
            //procUnitTypeElement.setText(StringUtil.EMPTY);
            //procElement.addContent(procUnitTypeElement);
                        
            procUnitListElement.setText(StringUtil.EMPTY);        
        }
        
        return procUnitListElement;        
    }
    
    
    
    
    ///**
    // * 
    // * @Name     setUnitRecipeList
    // * @since    2018. 10. 9.
    // * @author   hhlee
    // * @contents 
    // *           
    // * @param elementUnitRecipeList
    // * @return
    // */
    //private Element setUnitRecipeList(String elementUnitRecipeList)
    //{
    //    Element UnitRecipeListElement = new Element("UNITRECIPELIST");
    //    if(StringUtil.isNotEmpty(elementUnitRecipeList))
    //    {
    //        //Element unitRecipeListElement = new Element("UNITRECIPELIST");
    //        //unitRecipeListElement.setText(elementUnitRecipeList);
    //        //procElement.addContent(unitRecipeListElement);
    //        
    //        String[] splitUnitRecipeName = StringUtil.split(elementUnitRecipeList, "|");
    //                    
    //        for(int j = 0; j < splitUnitRecipeName.length; j++ )
    //        {
    //            Element UnitRecipeElement = new Element("UNITRECIPE");
    //            
    //            Element unitRecipeNameElement = new Element("UNITRECIPENAME");
    //            unitRecipeNameElement.setText(splitUnitRecipeName[j]);
    //            
    //            UnitRecipeElement.addContent(unitRecipeNameElement);    
    //            
    //            UnitRecipeListElement.addContent(UnitRecipeElement);
    //        }                          
    //    }
    //    else
    //    {
    //        UnitRecipeListElement.setText(StringUtil.EMPTY);            
    //    }
    //    
    //    return UnitRecipeListElement;
    //}
    //
    ///**
    // * 
    // * @Name     setProcSubUnitList
    // * @since    2019. 2. 14.
    // * @author   hhlee
    // * @contents 
    // *           
    // * @param unitName
    // * @param elementSubUnitNameList
    // * @return
    // * @throws CustomException
    // */
    //private Element setProcSubUnitList(String unitName, String elementSubUnitNameList) throws CustomException
    //{
    //    Element procSubUnitListElement = new Element("PROCSUBUNITLIST");
    //    if(StringUtil.isNotEmpty(elementSubUnitNameList))
    //    {
    //        String[] splitSubUnitNameList = StringUtil.split(elementSubUnitNameList , "|");
    //        for(int i = 0; i < splitSubUnitNameList.length; i++ )
    //        {
    //            if(StringUtil.isNotEmpty(splitSubUnitNameList[i]))
    //            {
    //                String[] splitSubUnitName = StringUtil.split(splitSubUnitNameList[i] , "=");
    //                
    //                if(StringUtil.equals(unitName, splitSubUnitName[0]))
    //                {
    //                    Element procSubUnitElement = new Element("PROCSUBUNIT");
    //                    
    //                    Element subUnitNameElement = new Element("SUBUNITNAME");
    //                    subUnitNameElement.setText(splitSubUnitName[1]);
    //                    procSubUnitElement.addContent(subUnitNameElement);
    //                                        
    //                    procSubUnitListElement.addContent(procSubUnitElement);   
    //                }
    //            }
    //        }
    //    }
    //    else
    //    {                 
    //        procSubUnitListElement.setText(StringUtil.EMPTY);        
    //    }
    //    
    //    return procSubUnitListElement;        
    //}
}
