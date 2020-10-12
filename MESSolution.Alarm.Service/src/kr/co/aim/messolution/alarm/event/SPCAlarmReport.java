package kr.co.aim.messolution.alarm.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AlarmDefinition;
import kr.co.aim.messolution.extended.object.management.data.MailingUser;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class SPCAlarmReport extends AsyncHandler {

    @Override
    public void doWorks(Document doc) throws CustomException {    	
    	String alarmCode = SMessageUtil.getBodyItemValue(doc, "ALARMID", true);
    	
        String alarmSeq 	  = SMessageUtil.getBodyItemValue(doc, "ALARMSEQ", false);
        String chartID 		  = SMessageUtil.getBodyItemValue(doc, "CHARTDEFNAME", false);
        String controlDataSeq = SMessageUtil.getBodyItemValue(doc, "CONTROLDATASEQ", false);
        String controlChartType = SMessageUtil.getBodyItemValue(doc, "CONTROLCHARTTYPE", false);
        String chartName = SMessageUtil.getBodyItemValue(doc, "CHARTNAME", false);
        String alarmType = SMessageUtil.getBodyItemValue(doc, "ALARMTYPE", false);
        String dcDataId = SMessageUtil.getBodyItemValue(doc, "DCDATAID", false);
        String ruleList = SMessageUtil.getBodyItemValue(doc, "RULELIST", false);
        String itemName = SMessageUtil.getBodyItemValue(doc, "ITEMNAME", false);

        String description = "AlarmSeq: " + alarmSeq + " / ChartID: " + chartID + " / ControlDataSeq: " + controlDataSeq + " / ControlChartType: " + controlChartType + " / ChartName: " + chartName + " / AlarmType :" + alarmType; 
        
        List<Element> dcDataList = SMessageUtil.getSubSequenceItemList(SMessageUtil.getBodyElement(doc), "DCDATALIST", false);
        
        EventInfo eventInfo = EventInfoUtil.makeEventInfo("Issue", getEventUser(), getEventComment(), "", "");
        //sync time tracing
        eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
        
        //2018.12.06 dmlee : modify Later this class ................
        
        try
        {
        	AlarmDefinition alarmDefData = ExtendedObjectProxy.getAlarmDefinitionService().selectByKey(true, new Object[] {alarmCode});
        	
            if(dcDataList != null)
            {
                for (Element dcDataElement : dcDataList)
                {     
                	// 2019.03.15_hsryu_lotName true -> false. requested by SPC Team.
                	String lotName = SMessageUtil.getChildText(dcDataElement, "LOTNAME", false);
                	String factoryName = SMessageUtil.getChildText(dcDataElement, "FACTORYNAME", false);
                	String subFactoryName = SMessageUtil.getChildText(dcDataElement, "SUBFACTORYNAME", false);
                	String productSpecName = SMessageUtil.getChildText(dcDataElement, "PRODUCTSPECNAME", false);
                	String productSpecVersion = SMessageUtil.getChildText(dcDataElement, "PRODUCTSPECVERSION", false);
                	String ecCode = SMessageUtil.getChildText(dcDataElement, "ECCODE", false);
                	String processFlowName = SMessageUtil.getChildText(dcDataElement, "PROCESSFLOWNAME", false);
                	String processFlowVersion = SMessageUtil.getChildText(dcDataElement, "PROCESSFLOWVERSION", false);
                	String processOperationName = SMessageUtil.getChildText(dcDataElement, "PROCESSOPERATIONNAME", false);
                	String machineName = SMessageUtil.getChildText(dcDataElement, "MACHINENAME", false);
                	String unitName = SMessageUtil.getChildText(dcDataElement, "UNITNAME", false);
                	String subUnitName = SMessageUtil.getChildText(dcDataElement, "SUBUNITNAME", false);
                	String processTime = SMessageUtil.getChildText(dcDataElement, "PROCESSTIME", false);
                	// 2019.03.18_hsryu_add MaterialName. requested by CIM&SPC
                	String materialName = SMessageUtil.getChildText(dcDataElement, "MATERIALNAME", false);

                	this.sendBySPCCreateAlarm(eventInfo, doc, alarmDefData.getAlarmCode(), machineName, chartName, ruleList, lotName, alarmType, materialName, itemName, chartID);
                	
                }
                
            }
        }
        catch(Exception ex)
        {
        	eventLog.error("Not Define Alardm Def Data Alarm Code : ["+alarmCode+"]");
        }
 
    }
    
    
    
    
    
    private String getUserAddr(List<MailingUser> userList)
    {
        String userAddr = StringUtil.EMPTY;
        
        for (MailingUser mailingUser : userList) 
        {
            if(StringUtil.isEmpty(userAddr))
                userAddr=mailingUser.getMailAddr();
            else
                userAddr+=";"+mailingUser.getMailAddr();
        }
        
        return userAddr;
    }
    
    
    //18.12.19 dmlee : Send RMS Alarm
    public void sendBySPCCreateAlarm(EventInfo eventInfo, Document doc, String alarmCode, String machineName, String chartName, String ruleList, String lotName, String alarmType, String materialName, String itemName, String chartID) throws CustomException
    {
    	try
    	{
    		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "CreateAlarm");

    		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);

    		Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);

    		Element element1 = new Element("ALARMCODE");
    		element1.setText(alarmCode);
    		eleBodyTemp.addContent(element1);

    		Element element2 = new Element("MACHINENAME");
    		element2.setText(machineName);
    		eleBodyTemp.addContent(element2);

    		Element element3 = new Element("CHARTNAME");
    		element3.setText(chartName);
    		eleBodyTemp.addContent(element3);

    		Element element4 = new Element("RULELIST");
    		element4.setText(ruleList);
    		eleBodyTemp.addContent(element4);

    		Element element5 = new Element("LOTNAME");
    		element5.setText(lotName);
    		eleBodyTemp.addContent(element5);

    		Element element6 = new Element("SPCALARMTYPE");
    		element6.setText(alarmType);
    		eleBodyTemp.addContent(element6);
    		
    		/*** 2019.03.19_hsryu_Insert Logic. Requested by SPC&CIM ***/
    		
    		Element element7 = new Element("MATERIALNAME");
    		element7.setText(materialName);
    		eleBodyTemp.addContent(element7);
    		
    		Element element8 = new Element("ITEMNAME");
    		element8.setText(itemName);
    		eleBodyTemp.addContent(element8);
    		
    		Element element9 = new Element("CHARTID");
    		element9.setText(chartID);
    		eleBodyTemp.addContent(element9);
    		
    		/***********************************************************/
    		
    		SMessageUtil.setHeaderItemValue(doc, "EVENTCOMMENT", eventInfo.getEventComment());

    		//overwrite
    		doc.getRootElement().addContent(eleBodyTemp);



    		Element returnEle = new Element(SMessageUtil.Return_Tag);

    		Element returnCode = new Element("RETURNCODE");
    		returnCode.setText("0");

    		Element returnMessage = new Element("RETURNMESSAGE");
    		returnMessage.setText("");

    		returnEle.addContent(returnCode);		
    		returnEle.addContent(returnMessage);

    		doc.getRootElement().addContent(returnEle);

    		//Send ALM Server : Create Alarm
    		GenericServiceProxy.getESBServive().sendBySender(doc, "LocalSender");


    	}
    	catch(Exception ex)
    	{
    		eventLog.error(String.format("E-Mail Send Fail !"));
    	}
    }
}