package kr.co.aim.messolution.generic.timer;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.TimeSchedule;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.springframework.beans.factory.InitializingBean;

public class MessageSchedulerServiceImpl implements InitializingBean{

	private static Log       log = LogFactory.getLog(MessageSchedulerServiceImpl.class);
	private Scheduler        scheduler;
	private SchedulerFactory schedulerFactory;
	
	public MessageSchedulerServiceImpl()
	{
		schedulerFactory = new org.quartz.impl.StdSchedulerFactory();
		try 
		{
			scheduler = schedulerFactory.getScheduler();
		} 
		catch ( SchedulerException e ) 
		{
			log.error(e);
		}
	}
	
	@Override
	public void afterPropertiesSet() throws Exception 
	{
		load();
	}
	
	protected void load() throws SchedulerException, ParseException
	{
		String svrName = System.getProperty("Seq");
		
		List<TimeSchedule> timeScheduleList = null;
		try
		{
			String condition = "where ACTIVE = 'Y'" ;
			timeScheduleList = ExtendedObjectProxy.getTimeScheduleService().select(condition, new Object[]{}, TimeSchedule.class);
		}
		catch(Exception e)
		{
			log.error(e);
		}
		
		if ( timeScheduleList != null && timeScheduleList.size() != 0 )
		{
			for ( TimeSchedule ct_timeschedule : timeScheduleList )
			{
				String DBServerName = ct_timeschedule.getRunningServer();
				
				if ( svrName.equals(DBServerName) )
				{
					if ( !scheduler.isStarted() )
					{
						scheduler.start();
					}
					
					String serverName = ct_timeschedule.getServerName();
					String messageName = ct_timeschedule.getMessageName();
					String scheduleName = serverName + "_" + messageName;
					
					String messageBody = this.makeMessageBody(ct_timeschedule);
					
					JobDetail jobDetail = new JobDetail(scheduleName,"timeServer",MessageExcuteJob.class);
					jobDetail.getJobDataMap().put("MessageBody", messageBody);
					
					if ( ct_timeschedule.getStartTime().toUpperCase().equals("NOT") )
					{
						SimpleTrigger simpleTrigger = new SimpleTrigger(scheduleName, "timeServer");
						simpleTrigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
						long repeatInterval = 60000*Integer.valueOf(ct_timeschedule.getTimeSchedule()).intValue();
						simpleTrigger.setRepeatInterval(repeatInterval);
						scheduler.scheduleJob(jobDetail, simpleTrigger);
						log.info(String.format("Schedule added RepeatInterval = [%s], Schedule Name =[%s]",
								ct_timeschedule.getTimeSchedule(),scheduleName));
					}else if ( ct_timeschedule.getStartTime().toUpperCase().equals("DIRECT") ){
						
						String scheduleExpression = "";
						if(ct_timeschedule.getCronExpression() != null && !ct_timeschedule.getCronExpression().isEmpty()){
					        scheduleExpression = ct_timeschedule.getCronExpression().trim();
					        CronTrigger cronJobTrigger = new CronTrigger(scheduleName,"timeServer",scheduleExpression);
							scheduler.scheduleJob(jobDetail, cronJobTrigger);
							log.info(String.format("Schedule added cron = [%s], Schedule Name =[%s]", scheduleExpression,scheduleName));
					    }else{
					    	log.error("It is not Direct");
					    }
					}else{
						String scheduleExpression = this.makeScheduleExpression(ct_timeschedule);
						CronTrigger cronJobTrigger = new CronTrigger(scheduleName,"timeServer",scheduleExpression);
						scheduler.scheduleJob(jobDetail, cronJobTrigger);
						log.info(String.format("Schedule added cron = [%s], Schedule Name =[%s]", scheduleExpression,scheduleName));
					}
				}
				else
				{
					//log.info("Server Name is not same, Running Server = " + svrName + ", DB Server = " + DBServerName);
				}
			}
		}
		
		log.info("Completed to load Scheduler");
	}
	
	public void reload() throws SchedulerException, ParseException
	 {
	  //stop running schedules
	  if ( scheduler.isStarted() )
	  {
		  for (String jobGroupName : scheduler.getJobGroupNames()) {
			  for(String jobName : scheduler.getJobNames(jobGroupName))
			  {
			     boolean deleteResult = scheduler.deleteJob(jobName, jobGroupName);
			     log.info(String.format("Scheduler deletes JobGroupName[%s], JobName[%s], Result=[%s]", jobGroupName, jobName, deleteResult));
			  }
		  }
	  }
	  
	  load();
	 }

	
	private String makeScheduleExpression(TimeSchedule ct_timeschedule)
	{
        String scheduleExpression = "0 0/10 * * * ?";
        
       
    	scheduleExpression = "0 " + ct_timeschedule.getStartTime() + "/" +
		ct_timeschedule.getTimeSchedule() + " * * * ?";
        
        
		return scheduleExpression;
	}
	
	private String makeMessageBody(TimeSchedule ct_timeschedule)
	{
		Element message = new Element(SMessageUtil.Message_Tag);
		Element header = new Element(SMessageUtil.Header_Tag);
		Element messageName = new Element(SMessageUtil.MessageName_Tag);
		Element transactionID = new Element(SMessageUtil.TransactionId_Tag);
		Element body = new Element(SMessageUtil.Body_Tag);
		Element time = new Element("TIME");
		Element eventUser = new Element(SMessageUtil.EventUser);
		Element eventComment = new Element(SMessageUtil.EventComment);
		Element language = new Element(SMessageUtil.Language);
		Element serverName = new Element("SERVERNAME");
		Document doc = new Document(message);
		Map<String,String> messageMap = this.makeBodyMessageMap(ct_timeschedule.getMessageBody());
		
		message.addContent(header);
		message.addContent(body);
		
		header.addContent(messageName);
		header.addContent(transactionID);
		header.addContent(serverName);
		
		messageName.setText(ct_timeschedule.getMessageName());
		transactionID.setText(TimeUtils.getCurrentEventTimeKey());
		serverName.setText(ct_timeschedule.getServerName());
		
		Element ele = null;
		while (messageMap.keySet().iterator().hasNext())
		{
			String keyName = messageMap.keySet().iterator().next();
			String keyValue = messageMap.remove(keyName);
			ele = new Element(keyName);                                                                                                               
			ele.setText(keyValue);                                                                                                                            
			body.addContent(ele);       			
		}
		
		body.addContent(time);
		body.addContent(eventUser);
		body.addContent(eventComment);
		body.addContent(language);
		
	
		time.setText(TimeUtils.getCurrentEventTimeKey());
		eventUser.setText("TIMESERVER");
		eventComment.setText("");
		language.setText("English");
		
		String result = JdomUtils.toString(doc);
		
		log.info(result);
		
		return result;	
	}
	
	private Map<String,String> makeBodyMessageMap(String messageBody)
	{
		Map<String,String> MessageBodyMap = new HashMap<String, String>();
		
		if(messageBody != null && !messageBody.isEmpty()){
			String[] messageArray = messageBody.split(",");
			
			for(String tempMessage : messageArray){
		
				String[] temp = tempMessage.split("=");
				String key = temp[0];
				String value = temp[1];
				
				if ( key != null && !key.isEmpty() )
				{
					MessageBodyMap.put(key.toUpperCase(), value);
				}
			}
		}
		
		return MessageBodyMap;
	}
}
