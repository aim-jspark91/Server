package kr.co.aim.messolution.alarm.event;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AlarmDefinition;
import kr.co.aim.messolution.extended.object.management.data.AlarmMailTemplate;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.userprofile.MESUserServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.user.management.data.UserProfile;

import org.jdom.Document;
import org.jdom.Element;

public class OCAPAlarmReport extends AsyncHandler {

    @Override
    public void doWorks(Document doc) throws CustomException {    	
    	
    	String ocapID = SMessageUtil.getBodyItemValue(doc, "OCAPID", false);
    	String ocapResultID = SMessageUtil.getBodyItemValue(doc, "OCAPRESULTID", false);
    	String resultDepartment = SMessageUtil.getBodyItemValue(doc, "REQUESTDEPARTMENT", false);
    	String resultUserID = SMessageUtil.getBodyItemValue(doc, "REQUESTUSERID", false);
    	String requestTime = SMessageUtil.getBodyItemValue(doc, "REQUESTTIME", false);
    	String responseDepartment = SMessageUtil.getBodyItemValue(doc, "RESPONSEDEPARTMENT", false);
    	String responseUserID = SMessageUtil.getBodyItemValue(doc, "RESPONSEUSERID", true);
    	String alarmID = SMessageUtil.getBodyItemValue(doc, "ALARMID", true);

        String ocapReport = "OCAPID: " + ocapID + " / OCAPRESULTID: " + ocapResultID + " / REQUESTDEPARTMENT: " + resultDepartment + " / REQUESTUSERID: " + resultUserID + " "
        		+ "/ REQUESTTIME: " + requestTime + " / RESPONSEDEPARTMENT :" + responseDepartment + " / RESPONSEUSERID :" + responseUserID + " / ALARMID :" + alarmID;
        
        EventInfo eventInfo = EventInfoUtil.makeEventInfo("Issue", getEventUser(), getEventComment(), "", "");
        //sync time tracing
        eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

        try
        {
        	AlarmDefinition alarmDefData = ExtendedObjectProxy.getAlarmDefinitionService().selectByKey(true, new Object[] {alarmID});
        	
			String title = "";
			StringBuilder alarmContent = new StringBuilder();
		
			String contentTemp = "";
        	
  
    		AlarmMailTemplate mailTemplateData = ExtendedObjectProxy.getAlarmMailTemplateService().selectByKey(false, new Object[]{alarmDefData.getAlarmType(), alarmDefData.getAlarmCode()});
    			
    			
			try
			{
    			title = mailTemplateData.getTitle();
    			//title = title.replaceAll("!<MACHINENAME>", machineName);

    			contentTemp = mailTemplateData.getComments().replaceAll("!<OCAPREPORT>", ocapReport);
    			//contentTemp = contentTemp.replaceAll("!<RECIPENAME>", recipeName);
			}
			catch(Exception ex)
			{
				eventLog.info("-------------------Non Define Mail Template ! -----------------------");
			}
			
			alarmContent.append(contentTemp);
			
			
			try
			{
				List<String> userEmailList = new ArrayList<String>();
				UserProfile userProfile = MESUserServiceProxy.getUserProfileServiceUtil().getUser(responseUserID);
				userEmailList.add(userProfile.getUdfs().get("EMAIL"));
				
				eventLog.info("-------------------Send E-Mail Title ["+title+"]-----------------------");
				eventLog.info("-------------------Send E-Mail User ["+userEmailList.toString()+"]-----------------------");
				
				MESUserServiceProxy.getUserProfileServiceUtil().MailSend(userEmailList, title, alarmContent.toString());
				
				eventLog.info("-------------------Send E-Mail Success ! -----------------------");
			}
			catch(CustomException ex)
			{
				eventLog.error(ex);
				eventLog.error("-------------------Send E-Mail Fail ! -----------------------");
			}
        	
        	this.sendBySPCCreateAlarm(eventInfo, doc, alarmDefData.getAlarmCode(), "", "", "", "", alarmDefData.getAlarmType(), "", "", "", ocapReport);
    			
	
        }
        catch(Exception ex)
        {
        	eventLog.error("Not Define Alardm Def Data Alarm Code : ["+alarmID+"]");
        }
 
    }
    
    
    
    
    
    //18.12.19 dmlee : Send RMS Alarm
    public void sendBySPCCreateAlarm(EventInfo eventInfo, Document doc, String alarmCode, String machineName, String chartName, String ruleList, String lotName, String alarmType, String materialName, String itemName, String chartID, String ocapReport) throws CustomException
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
    		
    		Element element10 = new Element("OCAPREPORT");
    		element10.setText(ocapReport);
    		eleBodyTemp.addContent(element10);
    		
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