package kr.co.aim.messolution.datacollection.event;

import kr.co.aim.messolution.datacollection.MESEDCServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspStockerZone;
import kr.co.aim.messolution.extended.object.management.data.SpcProcessedOperation;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.collections.map.ListOrderedMap;//GJJ
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;//GJJ
import java.util.Map;//GJJ
import java.util.HashMap;//GJJ

import org.jdom.Document;
import org.jdom.Element;

public class ProductProcessDataPEX extends AsyncHandler {
 
    @Override
    public void doWorks(Document doc) throws CustomException  
    {
        /*MACHINENAME
        UNITNAME
        SUBUNITNAME
        LOTNAME
        CARRIERNAME
        PRODUCTNAME 
        MACHINERECIPENAME
        PROCESSOPERATIONNAME
        PRODUCTSPECNAME
        PROCESSDATATYPE
        ITEMLIST*/

        EventInfo eventInfo = EventInfoUtil.makeEventInfo("ProductProcessData", getEventUser(), getEventComment(), null, null);
        
        Document spcDoc = (Document) doc.clone();

        String machineName          = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
        String unitName             = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
        String subUnitName          = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
        String lotName              = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
        String carrierName          = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
        String productName          = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", false);
        String machineRecipeName    = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);
        String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", false);
        String productSpecName      = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", false);
        String processDataType      = SMessageUtil.getBodyItemValue(doc, "PROCESSDATATYPE", false);
        
        Element itemList = SMessageUtil.getBodySequenceItem(spcDoc, "ITEMLIST", false);
        
        /* 20181130, hhlee, add Machine Validation */
        Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
             
        try
        {
            /*MACHINENAME
            UNITNAME
            SUBUNITNAME
            LOTNAME
            CARRIERNAME
            PRODUCTNAME 
            MACHINERECIPENAME
            PROCESSFLOWNAME --
            PROCESSOPERATIONNAME
            PROCESSOPERATIONTYPE --
            FACTORYNAME --
            PRODUCTSPECNAME
            ITEMLIST*/

            /////////////////////////////////////
            // 1. Get Data & Validation        //
            /////////////////////////////////////
            String factoryName = machineData.getFactoryName();
            String processFlowName = StringUtil.EMPTY;
            String processOperationType = StringUtil.EMPTY;
            
            String woType = StringUtil.EMPTY;
            String downLoadID = StringUtil.EMPTY;
            
            /* 20190423, hhlee, Added ECCODE Item */
            String ecCode = StringUtil.EMPTY;
            
            if(StringUtil.isNotEmpty(productName))
            {
                try
                {
                    Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
                                        
                    Lot lotData    = MESLotServiceProxy.getLotInfoUtil().getLotData(productData.getLotName());
                   
                    factoryName = lotData.getFactoryName();
                    processFlowName = lotData.getProcessFlowName();
                    processOperationName = lotData.getProcessOperationName();
                    
                    ProcessOperationSpec processOperationSpecData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName()); 
                    processOperationType = processOperationSpecData.getProcessOperationType();
                    
                    /* 20190410, hhlee, modify, add inquery productspecname */
                    productSpecName = lotData.getProductSpecName();
                    
                    downLoadID = CommonUtil.getValue(processOperationSpecData.getUdfs(), "DOWNLOADID");
                    
                    if(!StringUtil.equals(lotData.getProductRequestName(), StringUtil.EMPTY))
                    {
                        woType = CommonUtil.getWorkOrderType(lotData);
                    }
                    
                    /* 20190423, hhlee, Added ECCODE Item */
                    ecCode = CommonUtil.getValue(lotData.getUdfs(), "ECCODE");
                }
                catch (Exception ex)
                {
                    eventLog.warn(ex.getMessage());
                }
            }
            
            /* 1. SPC Message Setting */
            SMessageUtil.setHeaderItemValue(spcDoc, "MESSAGENAME", "ProductProcessData");
            /* 20190423, hhlee, Added ECCODE Item */
            MESEDCServiceProxy.getDataCollectionServiceUtil().generateSpcProductProcessDataByReceiveData(SMessageUtil.getBodyElement(spcDoc), machineName, 
                    unitName, subUnitName, lotName, carrierName, productName, machineRecipeName, processFlowName, processOperationName, 
                    processOperationType, factoryName, productSpecName, woType, ecCode, itemList);
            
            /* 1-1. SPC Message Processtime Setting */
            SMessageUtil.setBodyItemValue(spcDoc, "PROCESSTIME", eventInfo.getEventTime().toString());
                        
            /* 2. PROCLIST Message Setting (When Sampling) */
            /* 2019620, hhlee, add, DCTYPE */
            if(StringUtil.equals(processOperationType, GenericServiceProxy.getConstantMap().Pos_Inspection))
            {
            	/* 2-1. SPC Message DCTYPE Setting */
                SMessageUtil.setBodyItemValue(spcDoc, "DCTYPE", GenericServiceProxy.getConstantMap().Pos_Inspection);
            }
            /* 2019620, hhlee, modify, deleted validation ProcessOperationType */
            //if(StringUtil.equals(processOperationType, GenericServiceProxy.getConstantMap().Pos_Inspection))
            if(StringUtil.isNotEmpty(downLoadID))
            {
                /* 2-1. SPC Message DCTYPE Setting */
            	/* 2019620, hhlee, delete, DCTYPE */
                //SMessageUtil.setBodyItemValue(spcDoc, "DCTYPE", GenericServiceProxy.getConstantMap().Pos_Inspection);
//                
//                MESEDCServiceProxy.getDataCollectionServiceUtil().generateSpcProductProcessDataProcList_V2(eventInfo, 
//                        SMessageUtil.getBodyElement(spcDoc), productName, downLoadID);
//                
                //eventLog.info(JdomUtils.toString(spcDoc));
            	
                
                // add by GJJ 20191218 mantis: 4998
                if(downLoadID.contains("MAPPING"))
                {
                	String[] listDownLoadID  = downLoadID.split(",");

                	List<ListOrderedMap> sqlResult = getSPCMappingInfo(productName,processFlowName,processOperationName,machineName,unitName,subUnitName);
      	    	  if(sqlResult!=null && sqlResult.size()>0){

      	    		generateSpcProductProcessDataProcList_V3(eventInfo, 
      	    				spcDoc,sqlResult, productName, listDownLoadID[0]);
      	    		
                    MESEDCServiceProxy.getDataCollectionServiceUtil().generateSpcProductProcessDataProcList_V2(eventInfo, 
                            SMessageUtil.getBodyElement(spcDoc), productName, listDownLoadID[0]);
      	    		
    	    	  }
      	    	  else
      	    	  {
                      MESEDCServiceProxy.getDataCollectionServiceUtil().generateSpcProductProcessDataProcList_V2(eventInfo, 
                              SMessageUtil.getBodyElement(spcDoc), productName, listDownLoadID[0]);
      	    	  }
      	    	  
                	
                }
                else if(downLoadID.contains(",")){            //ADD BY FWQ 20200506
                	String[] listDownLoadID_R  = downLoadID.split(",");
                	String RealdownLoadID = getRealMainProcessInfo(listDownLoadID_R,productName);
                	MESEDCServiceProxy.getDataCollectionServiceUtil().generateSpcProductProcessDataProcList_V2(eventInfo, 
                            SMessageUtil.getBodyElement(spcDoc), productName, RealdownLoadID);
                }
                else {
                    MESEDCServiceProxy.getDataCollectionServiceUtil().generateSpcProductProcessDataProcList_V2(eventInfo, 
                            SMessageUtil.getBodyElement(spcDoc), productName, downLoadID);
				}  
            }
            else
            {
                //PROCLIST
                Element bodyElement = SMessageUtil.getBodyElement(spcDoc);
                Element procListElement = new Element("PROCLIST");
                procListElement.setText(StringUtil.EMPTY);
                bodyElement.addContent(procListElement);
            }
            //eventLog.info(JdomUtils.toString(spcDoc));    
            GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("SPCsvr"), spcDoc, "EDCSender");
        }
        catch (Exception ex)
        {
            eventLog.error(ex.getMessage());            
        }
    }
    
    
	public static List<ListOrderedMap> getSPCMappingInfo( String productName,String processFlowName,String processOperationName,String machineName,String unitID, String subUnitID) throws CustomException
	{
		try {
			StringBuilder qry = new StringBuilder();
			qry.append("  WITH MAPPINGCOUNT AS"); 
			qry.append("  	("); 
			qry.append("  		SELECT DISTINCT"); 
			qry.append("  			                                                       MAPPINGGROUP , ITEMNAME"); 
			qry.append("  		  , COUNT(*) OVER (PARTITION BY ITEMNAME, MAPPINGGROUP) AS COUNTA"); 
			qry.append("  		FROM"); 
			qry.append("  			CT_SPCMAPPING"); 
			qry.append("  	)"); 
			qry.append("  SELECT"); 
			qry.append("  	A.ITEMNAME            , A.PRODUCTNAME"); 
			qry.append("    , A.LOTNAME             , A.PRODUCTSPECNAME"); 
			qry.append("    , A.PROCESSFLOWNAME     , A.PROCPROCESSOPERATIONNAME"); 
			qry.append("    , A.MACHINENAME         , A.UNITNAME"); 
			qry.append("    , A.UNITTYPE            , A.SUBUNITNAME"); 
			qry.append("    , A.UNITRECIPENAME      , A.TIMEKEY"); 
			qry.append("    , A.INSPECTIONOPERATION , CASE"); 
			qry.append("  		WHEN A.MAPPINGGROUP = 'NORMAL'"); 
			qry.append("  			THEN A.MACHINERECIPENAME"); 
			qry.append("  			ELSE A.MAPPINGGROUP"); 
			qry.append("  	END AS MAPPINGGROUP , A.MACHINERECIPENAME"); 
			qry.append("  FROM"); 
			qry.append("  	("); 
			qry.append("  		SELECT"); 
			qry.append("  			SC.ITEMNAME                                    , SO.PRODUCTNAME"); 
			qry.append("  		  , SO.LOTNAME                                     , SO.PRODUCTSPECNAME"); 
			qry.append("  		  , SO.PROCESSFLOWNAME                             , SC.PROCPROCESSOPERATIONNAME"); 
			qry.append("  		  , SO.MACHINENAME                                 , SO.UNITNAME"); 
			qry.append("  		  , SO.UNITTYPE                                    , SO.SUBUNITNAME"); 
			qry.append("  		  , SO.UNITRECIPENAME                              , SO.TIMEKEY"); 
			qry.append("  		  , SC.PROCESSOPERATIONNAME AS INSPECTIONOPERATION , SC.MAPPINGGROUP"); 
			qry.append("  		  , SO.MACHINERECIPENAME                           , COUNT(*) OVER ( PARTITION BY SC.ITEMNAME, SC.PROCESSOPERATIONNAME, SC.MAPPINGGROUP ORDER BY"); 
			qry.append("  																			SO.TIMEKEY DESC ) AS DATAMAPCOUNT"); 
			qry.append("  		FROM"); 
			qry.append("  			CT_SPCPROCESSEDOPERATION SO , CT_SPCMAPPING SC"); 
			qry.append("  		WHERE"); 
			qry.append("  			1                           = 1"); 
			qry.append("  			AND SO.PRODUCTNAME          = :PRODUCTNAME"); 
			qry.append("  			AND SC.PROCESSOPERATIONNAME =:PROCESSOPERATIONNAME"); 
			qry.append("  			AND"); 
			qry.append("  			("); 
			qry.append("  				SC.PROCESSFLOWNAME    =:PROCESSFLOWNAME"); 
			qry.append("  				OR SC.PROCESSFLOWNAME ='*'"); 
			qry.append("  			)"); 
			qry.append("  			AND"); 
			qry.append("  			("); 
			qry.append("  				SC.MACHINENAME    = :MACHINENAME"); 
			qry.append("  				OR SC.MACHINENAME = '*'"); 
			qry.append("  			)"); 
			qry.append("  			AND"); 
			qry.append("  			("); 
			qry.append("  				SC.UNITNAME    = :UNITNAME"); 
			qry.append("  				OR SC.UNITNAME = '*'"); 
			qry.append("  			)"); 
			qry.append("  			AND"); 
			qry.append("  			("); 
			qry.append("  				SC.SUBUNITNAME    = :SUBUNITNAME"); 
			qry.append("  				OR SC.SUBUNITNAME = '*'"); 
			qry.append("  			)"); 
			qry.append("  			AND SO.PROCESSOPERATIONNAME = SC.PROCPROCESSOPERATIONNAME"); 
			qry.append("  			AND"); 
			qry.append("  			("); 
			qry.append("  				SO.PROCESSFLOWNAME        = SC.PROCPROCESSFLOWNAME"); 
			qry.append("  				OR SC.PROCPROCESSFLOWNAME = '*'"); 
			qry.append("  			)"); 
			qry.append("  			AND"); 
			qry.append("  			("); 
			qry.append("  				SO.MACHINENAME        = SC.PROCMACHINENAME"); 
			qry.append("  				OR SC.PROCMACHINENAME = '*'"); 
			qry.append("  			)"); 
			qry.append("  			AND"); 
			qry.append("  			("); 
			qry.append("  				SO.UNITNAME        = SC.PROCUNITNAME"); 
			qry.append("  				OR SC.PROCUNITNAME = '*'"); 
			qry.append("  			)"); 
			qry.append("  			AND"); 
			qry.append("  			("); 
			qry.append("  				SO.SUBUNITNAME        = SC.PROCSUBUNITNAME"); 
			qry.append("  				OR SC.PROCSUBUNITNAME = '*'"); 
			qry.append("  			)"); 
			qry.append("  			AND"); 
			qry.append("  			("); 
			qry.append("  				SO.MACHINERECIPENAME = SC.PROCRECIPENAME"); 
			qry.append("  				OR SC.PROCRECIPENAME = '*'"); 
			qry.append("  			)"); 
			qry.append("  		ORDER BY"); 
			qry.append("  			SO.TIMEKEY DESC"); 
			qry.append("  	)"); 
			qry.append("  	A , MAPPINGCOUNT C"); 
			qry.append("  WHERE"); 
			qry.append("  	(A.DATAMAPCOUNT = C.COUNTA OR A.MAPPINGGROUP = 'NORMAL')"); 
			qry.append("  	AND A.ITEMNAME = C.ITEMNAME"); 
			qry.append("  	AND"); 
			qry.append("  	("); 
			qry.append("  		A.MAPPINGGROUP    = C.MAPPINGGROUP"); 
			qry.append("  	) ");                                                                                                 
			
			Map<String, String> newbindMap = new HashMap<String, String>();
			newbindMap.put("PRODUCTNAME", productName);
			newbindMap.put("PROCESSFLOWNAME", processFlowName);
			newbindMap.put("PROCESSOPERATIONNAME", processOperationName);
			newbindMap.put("MACHINENAME", machineName);
			newbindMap.put("UNITNAME", unitID);
			newbindMap.put("SUBUNITNAME", subUnitID);
			
			@SuppressWarnings("unchecked")
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(qry.toString(), newbindMap);
						
			return result ;
			
			
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	//ADD BY FWQ 20200506 Get the last ProcessOperation as DownLoadID
	public String getRealMainProcessInfo(String[] downLoudID1,String productName) throws CustomException{
		try {
			
			Object[] bindSet = new Object[downLoudID1.length + 1];
			bindSet[0] = productName;
			
			for (int index = 0; index < downLoudID1.length; index++) {
				bindSet[index + 1] = downLoudID1[index];
			}
			
			String condition = "PRODUCTNAME = ? AND PROCESSOPERATIONNAME IN (" + StringUtils.removeEnd(StringUtils.repeat("?,", downLoudID1.length), ",") + ") ORDER BY TIMEKEY DESC";
															
			List<SpcProcessedOperation> spcProcessedOperationDataList = ExtendedObjectProxy.getSpcProcessedOperationService().select(condition, bindSet);			
			return spcProcessedOperationDataList.get(0).getProcessOperationName().toString();
			
		}catch (Exception e) {
			eventLog.error(e);

		}
		return null;		
	}
	
	
	
	
	public void generateSpcProductProcessDataProcList_V3(EventInfo eventInfo,
			Document spcDoc, List<ListOrderedMap> sqlResult,
			String productName, String downLoadId) throws CustomException {
		try {

			Element bodyElement = SMessageUtil.getBodyElement(spcDoc);

			List<Element> itemElement = SMessageUtil.getSubSequenceItemList(
					bodyElement, "ITEMLIST", false);

			Element newItemList = new Element("ITEMLIST");

			for (Element item : itemElement) {
				Boolean sendFlag = false;
				String itemName = SMessageUtil.getChildText(item, "ITEMNAME",
						true);

				for (ListOrderedMap row : sqlResult) {
					if (itemName.equalsIgnoreCase(row.get("ITEMNAME")
							.toString())) {
						bodyElement.removeChild("ITEMLIST");
						sendFlag = true;
						Element newItemListOne = new Element("ITEMLIST");
						newItemListOne.addContent(setItemElement(item));

						Element procElement = new Element("PROC");
						Element procElementList = new Element("PROCLIST");

						this.setProcElement(procElement, "ARRAY",
								(String) row.get("PRODUCTSPECNAME"),
								(String) row.get("PROCESSFLOWNAME"),
								(String) row.get("PROCPROCESSOPERATIONNAME"),
								(String) row.get("MACHINENAME"),
								(String) row.get("MAPPINGGROUP"),
								(String) row.get("LOTNAME"),
								(String) row.get("TIMEKEY"),
								(String) row.get("UNITNAME"),
								(String) row.get("UNITTYPE"),
								(String) row.get("UNITRECIPENAME"),
								(String) row.get("SUBUNITNAME"));
						procElementList.addContent(procElement);

						bodyElement.addContent(newItemListOne);
						bodyElement.addContent(procElementList);
						// eventLog.info(JdomUtils.toString(spcDoc));
						GenericServiceProxy.getESBServive().sendBySender(
								GenericServiceProxy.getESBServive()
										.getSendSubject("SPCsvr"), spcDoc,
								"EDCSender");

						bodyElement.removeChild("PROCLIST");
						break;

					}
				}

				if (!sendFlag) {
					newItemList.addContent(setItemElement(item));
				}

			}

			bodyElement.removeChild("ITEMLIST");
			bodyElement.addContent(newItemList);

			return;

		} catch (Exception e) {
			eventLog.error(e);
		}

	}
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
    	procElement.removeContent();
    	
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
        
        return procElement;
    }
	
    private Element setItemElement(Element itemElement) throws CustomException
    {
    	Element newItemList = new Element("ITEM");
        Element newBCItemName = new Element("ITEMNAME");
        Element newDataType = new Element("DATATYPE");


    	
    	
        String itemName = SMessageUtil.getChildText(itemElement, "ITEMNAME", true);
        String dataType = SMessageUtil.getChildText(itemElement, "DATATYPE", false);
        
        newBCItemName.setText(itemName);
        newDataType.setText(dataType);
               
        Element childSeqItem = null;		
		childSeqItem = itemElement.getChild("SITELIST");
		
		Element newSiteList = new Element("SITELIST");
		
		
		for ( Iterator iterator = childSeqItem.getChildren().iterator(); iterator.hasNext(); )
		{
			Element childItem = (Element) iterator.next();	
			
	        Element newSite = new Element("SITE");
			
			String oldSiteName =  childItem.getChildText("SITENAME");
			String oldSiteType =  childItem.getChildText("SITETYPE");
			String oldSiteValue =  childItem.getChildText("SITEVALUE");
			
            Element newBCSiteName = new Element("SITENAME");
            newBCSiteName.setText(oldSiteName);
	        newSite.addContent(newBCSiteName);
            
            Element newBCSiteType = new Element("SITETYPE");
            newBCSiteType.setText(oldSiteType);
	        newSite.addContent(newBCSiteType);
            
            
            Element newBCSiteValue = new Element("SITEVALUE");
            newBCSiteValue.setText(oldSiteValue);
	        newSite.addContent(newBCSiteValue);    
	            
	            
	        newSiteList.addContent(newSite);
		}
        newItemList.addContent(newBCItemName);
        newItemList.addContent(newDataType);
        newItemList.addContent(newSiteList);
               
        return newItemList;
    }
}


/*package kr.co.aim.messolution.datacollection.event;

import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;
import org.jdom.Element;

public class ProductProcessDataPEX extends AsyncHandler {
 
    @Override
    public void doWorks(Document doc)
        throws CustomException  
    {
        String machineName          = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
        String lotName              = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
        String carrierName          = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
        String machineRecipeName    = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);
        String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", false);
        String productSpecName      = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", false);
        
        List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);
        
        for (Element product : productList)
        {
            try
            {
                String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", true);
                
                List<Element> unitList = SMessageUtil.getSubSequenceItemList(product, "UNITLIST", true);
                
                for (Element unit : unitList)
                {
                    try
                    {
                        String unitName = SMessageUtil.getChildText(unit, "UNITNAME", true);
                        String subUnitName = SMessageUtil.getChildText(unit, "SUBUNITNAME", false);
                        
                        Document cloneDoc = (Document) doc.clone();
                        Element itemList = (Element) SMessageUtil.getSubSequenceItem(unit, "ITEMLIST", true).clone();
                        
                        cloneDoc = this.writeRequest(cloneDoc, machineName, unitName, subUnitName, carrierName, lotName, productName, productSpecName, processOperationName, machineRecipeName, itemList);
                        
                        try
                        {
                            GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("SPCsvr"), cloneDoc, "EDCSender");
                        }
                        catch (Exception ex)
                        {
                            eventLog.error(ex.getMessage());
                        }
                    }
                    catch (Exception ex)
                    {
                        eventLog.error("UNIT level parsing error");
                        eventLog.error(ex.getMessage());
                    }
                }
            }
            catch (Exception ex)
            {
                eventLog.error("PRODUCT level parsing error");
                eventLog.error(ex.getMessage());
            }
        }
    }
    
    private Document writeRequest(Document doc, String machineName, String unitName, String subUnitName,
                                    String carrierName, String lotName, String productName,
                                    String productSpecName, String processOperationName, String recipeName,
                                    Element itemList)
        throws CustomException
    {
        //SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "CollectProcessData");
        //SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", GenericServiceProxy.getESBServive().getSendSubject("CNXsvr"));
        
        //Element eleBody = SMessageUtil.getBodyElement(doc);

        boolean result = doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
        
        Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);
        
        Element element1 = new Element("MACHINENAME");
        element1.setText(machineName);
        eleBodyTemp.addContent(element1);
        
        Element element2 = new Element("LOTNAME");
        element2.setText(lotName);
        eleBodyTemp.addContent(element2);
        
        Element element3 = new Element("CARRIERNAME");
        element3.setText(carrierName);
        eleBodyTemp.addContent(element3);
        
        Element element4 = new Element("MACHINERECIPENAME");
        element4.setText(recipeName);
        eleBodyTemp.addContent(element4);   
        
        Element element5 = new Element("PROCESSOPERATIONNAME");
        element5.setText(processOperationName);
        eleBodyTemp.addContent(element5);
        
        Element element6 = new Element("PRODUCTSPECNAME");
        element6.setText(productSpecName);
        eleBodyTemp.addContent(element6);
        
        Element element7 = new Element("PRODUCTNAME");
        element7.setText(productName);
        eleBodyTemp.addContent(element7);
        
        Element element8 = new Element("UNITNAME");
        element8.setText(unitName);
        eleBodyTemp.addContent(element8);
        
        Element element9 = new Element("SUBUNITNAME");
        element9.setText(subUnitName);
        eleBodyTemp.addContent(element9);
        
        eleBodyTemp.addContent(itemList);
        
        //overwrite
        doc.getRootElement().addContent(eleBodyTemp);
        
        return doc;
    }
    
}*/
