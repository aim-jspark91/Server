package kr.co.aim.messolution.timer.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.management.data.ScheduleJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.timer.ScheduleJobFactory;
import kr.co.aim.messolution.userprofile.MESUserServiceProxy;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class MaterialExpiredCheck implements Job, InitializingBean, ApplicationContextAware {

	private static Log log = LogFactory.getLog(MaterialExpiredCheck.class);
	
	// Added by smkang on 2019.04.08 - For avoid duplication of schedule job.
	private static ApplicationContext applicationContext;
		
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}
	
	@Override
	// Added by smkang on 2019.04.08 - For avoid duplication of schedule job.
	public void setApplicationContext(ApplicationContext arg0) throws BeansException {
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// Added by smkang on 2019.04.08 - For avoid duplication of schedule job.
//		ScheduleJobFactory scheduleJobFactory = (ScheduleJobFactory) applicationContext.getBean(ScheduleJobFactory.class.getSimpleName());
//		if (scheduleJobFactory.isRunningScheduleJob(this)) {
//			log.info("Previous " + this.getClass().getName() + " is still running, so this schedule job is terminated.");
//		} else {
//			log.info("Previous " + this.getClass().getName() + " is not running, so this schedule job is executed.");
//			scheduleJobFactory.startScheduleJob(this);
//					
//			try {
//	        	log.info(String.format("Job[%s] START", this.getClass().getName()));
//				
//	        	monitorMeterialDuration();
//				
//				log.info(String.format("Job[%s] END", this.getClass().getName()));
//			} catch (CustomException e) {
//				if (log.isDebugEnabled())
//	                log.error(e.errorDef.getLoc_errorMessage());
//			} catch (Exception e) {
//				log.error(e);
//			}
//			
//			// Added by smkang on 2019.04.08 - For avoid duplication of schedule job.
//			scheduleJobFactory.endScheduleJob(this);
//		}
		// Modified by smkang on 2019.04.24 - According to Liu Hongwei's request, SCHsvr should be executed AP1 and AP2.
    	//									  For avoid duplication of schedule job, running information should be recorded.
		try {
			ScheduleJobFactory scheduleJobFactory = (ScheduleJobFactory) applicationContext.getBean(ScheduleJobFactory.class.getSimpleName());
			ScheduleJob scheduleJob = scheduleJobFactory.startScheduleJob(arg0);
						
			try {
	        	log.info(String.format("Job[%s] START", this.getClass().getName()));
				
	        	monitorMeterialDuration();
				
				log.info(String.format("Job[%s] END", this.getClass().getName()));
			} catch (CustomException e) {
				if (log.isDebugEnabled())
	                log.error(e.errorDef.getLoc_errorMessage());
				
				log.error(String.format("Job[%s] TERMINATE", this.getClass().getName()));
			} catch (Exception e) {
				log.error(e);
				log.error(String.format("Job[%s] TERMINATE", this.getClass().getName()));
			}
			
			scheduleJobFactory.endScheduleJob(scheduleJob);
		} catch (Exception e) {
			log.info(e);
			log.info(String.format("Job[%s] PASS", this.getClass().getName()));
		}
	}

	@SuppressWarnings("unchecked")
	public void monitorMeterialDuration() throws CustomException
	{
		String sql = " SELECT C.CONSUMABLENAME, C.LASTMOUNTTIME, CS.DEPARTMENTNAME, CS.DURATIONUSEDLIMIT, TO_NUMBER((SYSDATE -  NVL(C.LASTMOUNTTIME,SYSDATE))) DIFFTIME " +
				" FROM CONSUMABLE C, CONSUMABLESPEC CS " +
				" WHERE 1 = 1 " + 
				" AND (C.FACTORYNAME,C.CONSUMABLESPECNAME) IN " +
				" (SELECT FACTORYNAME,CONSUMABLESPECNAME " +
				" FROM CONSUMABLESPEC " +
				" WHERE DESCRIPTION = :CONSUMABLETYPE AND FACTORYNAME = :FACTORYNAME ) " +
				" AND C.CONSUMABLESPECNAME = CS.CONSUMABLESPECNAME " +
				" AND C.CONSUMABLETYPE = :CONSUMABLETYPE " +
				" AND CS.DURATIONUSEDLIMIT < TO_NUMBER((SYSDATE -  NVL(C.LASTMOUNTTIME,SYSDATE))) ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("CONSUMABLETYPE", "PI");
		bindMap.put("FACTORYNAME", "ARRAY");

		List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		if(result!=null&&result.size()>0)
		{
			for (ListOrderedMap row : result)
			{   
				String consumableName = CommonUtil.getValue(row, "CONSUMABLENAME");
				//String lastMountTime = CommonUtil.getValue(row, "LASTLOGGEDINTIME");
				String durationUsedLimit = CommonUtil.getValue(row, "DURATIONUSEDLIMIT");
				String departmentName = CommonUtil.getValue(row, "DEPARTMENTNAME");
				String diffTime = CommonUtil.getValue(row, "DIFFTIME");

				if(StringUtils.isNotEmpty(departmentName))
				{
					log.info("consumableName : " + consumableName + ",  Send Mail To Department : " + departmentName + ", pass day : " + diffTime ); 
					
					List<String> userList = new ArrayList<String>();
					
					userList = MESUserServiceProxy.getUserProfileServiceUtil().getUserByDept(departmentName);
					
					MESUserServiceProxy.getUserProfileServiceUtil().MailSend(userList, "Expired PI Material!","Expried Pi : consumableName : " + consumableName + ",  Send Mail To Department : " + departmentName + ", pass day : " + diffTime );
										
					/*
					try
					{
						JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

						Properties mailProperties = new Properties(); 
						//mailProperties.put("mail.smtp.starttls", true);
						//mailProperties.put("mail.transport.protocol", "smtp");
						mailProperties.put("mail.smtp.host", "smtp.naver.com");
						//mailProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
						mailProperties.put("mail.smtp.port", 465);
						mailProperties.put("mail.smtp.auth", "true");
						mailProperties.put("mail.smtp.ssl.enable", "true");
						mailProperties.put("mail.smtp.ssl.trust", "smtp.naver.com");

						Session sess = Session.getDefaultInstance(mailProperties,new javax.mail.Authenticator()
						{
							protected PasswordAuthentication getPasswordAuthentication(){
								return new PasswordAuthentication("meta22","111");
							}
						});
						
						sess.setDebug(true);
						
						Message msg = new MimeMessage(sess);
						msg.setFrom(new InternetAddress("meta22@naver.com"));
						msg.addRecipient(Message.RecipientType.TO,  new InternetAddress("meta22@naver.com"));
						//msg.setHeader("content-type", "text/plain;charset=utf-8");
						msg.setSubject("TEST");
						msg.setText("test");
						Transport.send(msg);
						
						System.out.println("전송완료");
					}
					catch (MessagingException e) {
						// TODO: handle exception
						e.printStackTrace();
					}
					*/
				}
			}
		}
	}
}

