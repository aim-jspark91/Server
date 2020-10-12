package kr.co.aim.messolution.alarm.event.ALM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AlarmMailTemplate;
import kr.co.aim.messolution.extended.object.management.data.MailingUser;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.userprofile.MESUserServiceProxy;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class TransactionElapsedAlarm extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		
		String alarmCode = SMessageUtil.getBodyItemValue(doc, "ALARMCODE", true);
		
		String serverName = SMessageUtil.getBodyItemValue(doc, "SERVERNAME", false);
		String eventName = SMessageUtil.getBodyItemValue(doc, "EVENTNAME", false);
		String eventUser = SMessageUtil.getBodyItemValue(doc, "EVENTUSER", false);
		String timeKey = SMessageUtil.getBodyItemValue(doc, "TIMEKEY", false);
		String transactionID = SMessageUtil.getBodyItemValue(doc, "TRANSACTIONID", false);
		String ip = SMessageUtil.getBodyItemValue(doc, "IP", false);
		String elapsedTime = SMessageUtil.getBodyItemValue(doc, "ELAPSEDTIME", false);
		
		List<MailingUser> mailingUserData = null;	
    	
    	try
    	{
    		mailingUserData = ExtendedObjectProxy.getMailingUserService().select("WHERE ALARMCODE = ? AND SYSTEM = ? ", new Object[]{alarmCode, "ERR"});
    	}
    	catch(Exception ex){}
    	
    	if(mailingUserData.size() > 0)
    	{
    		List<String> userEmailList = new ArrayList<String>();
    		
    		for(MailingUser mailingUser : mailingUserData)
    		{
    			if(mailingUser.getUserGroupName() != "-") // In case that user group is enrolled.
    			{
    				StringBuilder sql = new StringBuilder();
    				sql.append(" SELECT USERID,EMAIL ");
    				sql.append("   FROM USERPROFILE ");
    				sql.append("  WHERE USERGROUPNAME =:USERGROUPNAME ");

    				Map<String, Object> bindMap = new HashMap<String, Object>();
    				bindMap.put("USERGROUPNAME", mailingUser.getUserGroupName());

    				@SuppressWarnings("unchecked")
    				List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
    				
    				if(sqlResult.size()>0)
    				{
    					for(int i=0; i<sqlResult.size();i++)
    					{
   							if(sqlResult.get(i).get("EMAIL") != null && StringUtils.endsWith((String) sqlResult.get(i).get("EMAIL"), "@everdisplay.com"))
    						{
    							if(!userEmailList.contains((String)sqlResult.get(i).get("EMAIL")))
    							{
    								userEmailList.add((String)sqlResult.get(i).get("EMAIL"));	
    							}
    						}
    					}
    				}
    			}
    			
    			if(!StringUtils.equals(mailingUser.getUserID(), "-")) // In case that userID is enrolled.
    			{
    				// Modified by smkang on 2019.03.19 - Skip invalid e-mail address.
    				//  if(StringUtils.isNotEmpty(mailingUser.getMailAddr()))
    				if(StringUtils.endsWith(mailingUser.getMailAddr(), "@everdisplay.com"))
    				{
    					if(!userEmailList.contains(mailingUser.getMailAddr()))
    					{
    						userEmailList.add(mailingUser.getMailAddr());    					
    					}
    				}
    			}
    		}
    		
    		String title = "";
    		String contentTemp = "";
    		StringBuilder alarmContent = new StringBuilder();
    		
    		try
    		{
    			AlarmMailTemplate mailTemplateData = ExtendedObjectProxy.getAlarmMailTemplateService().selectByKey(false, new Object[]{"ERR", "ElapsedTimeOver"});
    			
    			try
    			{
        			title = mailTemplateData.getTitle();
        			title = title.replaceAll("!<SERVERNAME>", serverName);
        			title = title.replaceAll("!<EVENTNAME>", eventName);
        			title = title.replaceAll("!<EVENTUSER>", eventUser);
        			title = title.replaceAll("!<TIMEKEY>", timeKey);
        			title = title.replaceAll("!<TRANSACTIONID>", transactionID);
        			title = title.replaceAll("!<IP>", ip);
        			title = title.replaceAll("!<ELAPSEDTIME>", elapsedTime);

        			contentTemp = mailTemplateData.getComments().replaceAll("!<SERVERNAME>", serverName);
        			contentTemp = contentTemp.replaceAll("!<EVENTNAME>", eventName);
        			contentTemp = contentTemp.replaceAll("!<EVENTUSER>", eventUser);
        			contentTemp = contentTemp.replaceAll("!<TIMEKEY>", timeKey);
        			contentTemp = contentTemp.replaceAll("!<TRANSACTIONID>", transactionID);
        			contentTemp = contentTemp.replaceAll("!<IP>", ip);
        			contentTemp = contentTemp.replaceAll("!<ELAPSEDTIME>", elapsedTime);
    			}
    			catch(Exception ex)
    			{
    				
    			}
    			
    			alarmContent.append(contentTemp);
    			
    		}
    		catch(Exception ex)
    		{
				title="Alarm Type [ERR] Send Alarm Mail Fail !, Plase Setting Alarm Mail Template !";
				eventLog.error("Alarm Type [ERR] Send Alarm Mail Fail !, Plase Setting Alarm Mail Template !");
    		}
			
			try
			{
				if(userEmailList.size() > 0){
					eventLog.info(title);
					MESUserServiceProxy.getUserProfileServiceUtil().MailSend(userEmailList, title, alarmContent.toString());
					eventLog.info("*****************************Send Mail Success**************************************");
				}
			}catch(CustomException ex){
				eventLog.warn("*****************************Fail Send Mail!!!!**************************************");
			}
    	}

		return null;
	}

}
