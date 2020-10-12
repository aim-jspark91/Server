/**
 * 
 */
package kr.co.aim.messolution.machine.event;


import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MachineAlarmList;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;

import org.jdom.Document;
import org.jdom.Element;

/**
 * @author Administrator
 *
 */
public class IndexerAbnormalReport  extends AsyncHandler{
	@Override
	public void doWorks(Document doc) throws CustomException {
		
	    try
		{
	        /* 20190115, hhlee, modify, change logic send IndexerAbnormalReport to FMC */
	        ///* Copy to FMCSender. Add, hhlee, 20180421 */
	        /* 20190211, hhlee, add, Record Indexer Abnormal State */
	        Document alarmReportdoc = (Document)doc.clone();
	        
	        /* Validate Machine */
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			Machine mainMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			
			/* Validate Unit */
			String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
			Machine unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
			
            //add by wghuang 20190121, for FMC
    		Element unitStatename = new Element("UNITSTATENAME");
    		unitStatename.setText(unitData.getMachineStateName());
    		SMessageUtil.getBodyElement(doc).addContent(unitStatename);
    		
    		/* 20190211, hhlee, add, Record Indexer Abnormal State ==>> */
    		String abnormalCode = StringUtil.EMPTY;
            String abnormalState = StringUtil.EMPTY;
            String abnormalText = StringUtil.EMPTY;
    		
            EventInfo eventInfo = EventInfoUtil.makeEventInfo("IndexerAbnormalReport", getEventUser(), getEventComment(), null, null);
            
            try
    		{	
        		abnormalCode = SMessageUtil.getBodyItemValue(doc, "ABNORMALCODE", true);
        		abnormalState = SMessageUtil.getBodyItemValue(doc, "ABNORMALSTATE", true);
        		abnormalText = SMessageUtil.getBodyItemValue(doc, "ABNORMALTEXT", false);
        		
        		String indexerAbnormaState = StringUtil.equals(abnormalState, GenericServiceProxy.getConstantMap().ALARMSTATE_ISSUE) ? 
        		        GenericServiceProxy.getConstantMap().INDEXER_ABNORMAL_STATE_PAUSE : GenericServiceProxy.getConstantMap().INDEXER_ABNORMAL_STATE_NORMAL ;
        		
        		unitData.getUdfs().put("INDEXERABNORMALSTATE", indexerAbnormaState);
        		
        		SetEventInfo setEventInfo = new SetEventInfo();
                setEventInfo.setUdfs(unitData.getUdfs());
        		MESMachineServiceProxy.getMachineServiceImpl().setEvent(unitData, setEventInfo, eventInfo);
        		
    		}
    		catch(Exception ex)
            {
                eventLog.warn("Indexer Abnormal State Update Failed!");
            }
    		
    		// Alarm Sever Send
    		try
    		{
    		    String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", false);    		
        		alarmReportdoc = generateAlarmReportTemplate(alarmReportdoc, machineName, unitName, StringUtil.EMPTY, 
        		        abnormalCode, abnormalState, GenericServiceProxy.getConstantMap().ALARMSTATE_LIGHT, abnormalText, productName);
        		
                GenericServiceProxy.getESBServive().sendBySender(alarmReportdoc, "ALMSender");
    		}
    		catch(Exception ex)
            {
                eventLog.warn("ALMSender Report Failed!");
            }    		
    		/* <<== 20190211, hhlee, add, Record Indexer Abnormal State */
    		
			///* Abnormal Data */
			//String alarmCode = SMessageUtil.getBodyItemValue(doc, "ABNORMALCODE", true);
			//String alarmState = SMessageUtil.getBodyItemValue(doc, "ABNORMALSTATE", true);
			//String alarmSeverity = "LIGHT";
			//String description = SMessageUtil.getBodyItemValue(doc, "ABNORMALTEXT", false);
			//String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", false);			
			//
			///* Whether to process it by alarm or not will require further confirmation. ==> */
			///* Make Alarm Report Message */
			//alarmReportdoc = generateAlarmReportTemplate(alarmReportdoc, machineName, unitName, "", 
			//		                       alarmCode, alarmState, alarmSeverity, description, productName);
			//
			//String eqpName= unitName;
			//if (!unitName.isEmpty())
			//{
			//	eqpName = unitName;
			//}
			//else
			//{
			//	eqpName = machineName;
			//}
			//
			//MachineSpec machineData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);
			//
			///* action parameters */
			//EventInfo eventInfo = EventInfoUtil.makeEventInfo("Issue", getEventUser(), getEventComment(), null, null);
			//
			///* sync time tracing */
			//eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
			//
			///* Further confirmation of alarm logic is required. ==>> */
			/////* get spec	*/			
			////MachineAlarmList eqpAlarmData = new MachineAlarmList(machineName, alarmCode);			
			////eqpAlarmData.setMachineName(machineName);		
			////eqpAlarmData.setUnitName(unitName);
			////eqpAlarmData.setSubUnitName("");       
			////eqpAlarmData.setAlarmCode(alarmCode);
			////eqpAlarmData.setAlarmState(alarmState);
			////eqpAlarmData.setAlarmSeverity(alarmSeverity);
			////eqpAlarmData.setAlarmText(description);
			////eqpAlarmData.setProductList(productName);				
			////				
			/////* MachineAlarmList Modify */	
			////this.setMachineAlarm(eqpAlarmData, machineName, alarmCode, alarmState, eventInfo);
			////
			/////* Product Validate */	
			////if(StringUtil.isNotEmpty(productName))
			////{	
			////	Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
			////	issueAlarm(eqpName, alarmCode, productData.getLotName(), productData.getCarrierName());
			////}
			////
			/////* Mailing Part */
			////try
			////{
			////	ExtendedObjectProxy.getAlarmDefinitionService().selectByKey(true, new Object[] {alarmCode});
			////	
			////	if(StringUtil.equals(alarmState, "ISSUE"))
			////	{
			////		/* To Do Alarm Mailing */
			////	}
			////			
			////}catch(Exception ex)
			////{
			////	eventLog.warn(ex.getLocalizedMessage());
			////}
			///* <<== Further confirmation of alarm logic is required. */
			///* <<== Whether to process it by alarm or not will require further confirmation. */
		}
		catch(CustomException ce)
		{
			eventLog.warn(String.format("[%s]%s", ce.errorDef.getErrorCode(), ce.errorDef.getLoc_errorMessage()));
		}	
		
		//success then report to FMC
		try
		{
		    /* 20190115, hhlee, modify, change logic send IndexerAbnormalReport to FMC */
			//GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), alarmReportdoc, "FMCSender");	
		    GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");  
		}
		catch(Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}		
	}
	
	/**
	 * @Name     generateAlarmReportTemplate
	 * @since    2018. 4. 21.
	 * @author   hhlee
	 * @contents Make Alarm Report Message           
	 * @param doc
	 * @param machinename
	 * @param unitname
	 * @param subunitname
	 * @param alarmcode
	 * @param alarmstate
	 * @param alarmseverity
	 * @param alarmtext
	 * @param productname
	 * @return
	 * @throws CustomException
	 */
	private Document generateAlarmReportTemplate(Document doc, String machinename, String unitname, String subunitname,
			        String alarmcode, String alarmstate, String alarmseverity, String alarmtext, String productname) throws CustomException
	{
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_AlarmReport");
		SMessageUtil.setHeaderItemValue(doc, "EVENTCOMMENT", "IndexerAbnormalReport");
		
		
		Element bodyElement = SMessageUtil.getBodyElement(doc);
		
		bodyElement.removeContent();
		
		Element machineNameElement = new Element("MACHINENAME");
		machineNameElement.setText(machinename);
		bodyElement.addContent(machineNameElement);
		
		Element unitNameElement = new Element("UNITNAME");
		unitNameElement.setText(unitname);
		bodyElement.addContent(unitNameElement);
		
		Element subUnitNameElement = new Element("SUBUNITNAME");
		subUnitNameElement.setText(subunitname);
		bodyElement.addContent(subUnitNameElement);
		
		Element alarmCodeElement = new Element("ALARMCODE");
		alarmCodeElement.setText(alarmcode);
		bodyElement.addContent(alarmCodeElement);
		
		Element alarmStateElement = new Element("ALARMSTATE");
		alarmStateElement.setText(alarmstate);
		bodyElement.addContent(alarmStateElement);
		
		Element alarmSeverityElement = new Element("ALARMSEVERITY");
		alarmSeverityElement.setText(alarmseverity);
		bodyElement.addContent(alarmSeverityElement);
		
		Element alarmTextElement = new Element("ALARMTEXT");
		alarmTextElement.setText(alarmtext);
		bodyElement.addContent(alarmTextElement);
		
		
		//ProductList
		Element productListElement = new Element("PRODUCTLIST");
		if(StringUtil.isNotEmpty(productname))
		{	
			Element productElement = new Element("PRODUCT");
			
			Element productNameElement = new Element("PRODUCTNAME");
			productNameElement.setText(productname);
			productElement.addContent(productNameElement);
				
			productListElement.addContent(productElement);
		}
		bodyElement.addContent(productListElement);
			
		return doc;				
	}
	
	/**
	 * @Name     issueAlarm
	 * @since    2018. 4. 21.
	 * @author   hhlee
	 * @contents Issued Alarm           
	 * @param machineName
	 * @param alarmCode
	 * @param lotName
	 * @param carrierName
	 * @throws CustomException
	 */
	private void issueAlarm(String machineName, String alarmCode, String lotName, String carrierName)
			throws CustomException
	{	
		//generate Alarm request message
		Element eleBody = new Element(SMessageUtil.Body_Tag);
		{
			Element eleAlarmCode = new Element("ALARMCODE");
			eleAlarmCode.setText(alarmCode);
			eleBody.addContent(eleAlarmCode);
			
			Element eleAlarmType = new Element("ALARMTYPE");
			eleAlarmType.setText("EQP");
			eleBody.addContent(eleAlarmType);
			
			Element eleMachineName = new Element("MACHINENAME");
			eleMachineName.setText(machineName);
			eleBody.addContent(eleMachineName);
			
			Element eleLotName = new Element("LOTNAME");
			eleLotName.setText(lotName);
			eleBody.addContent(eleLotName);
			
			Element eleCarrierName = new Element("CARRIERNAME");
			eleCarrierName.setText(carrierName);
			eleBody.addContent(eleCarrierName);
		}
		
		try
		{
			String targetSubject = GenericServiceProxy.getESBServive().getSendSubject("CNXsvr");
			
			Document requestDoc = SMessageUtil.createXmlDocument(eleBody, "CreateAlarm",
					"",//SMessageUtil.getHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", false),
					targetSubject,
					"MES",
					"EQP Alarm Report");

			GenericServiceProxy.getESBServive().sendBySender(targetSubject, requestDoc, "LocalSender");
		}
		catch (Exception ex)
		{
		}
	}
	
}
