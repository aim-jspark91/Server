package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MailingUser;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MailingUserService extends CTORMService<MailingUser> {
	
	public static Log logger = LogFactory.getLog(MailingUserService.class);
	
	private final String historyEntity = "mailingUserHist";
	
	public List<MailingUser> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<MailingUser> result = super.select(condition, bindSet, MailingUser.class);
		
		return result;
	}
	
	public MailingUser selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(MailingUser.class, isLock, keySet);
	}
	
	public MailingUser create(EventInfo eventInfo, MailingUser dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, MailingUser dataInfo)
		throws CustomException
	{
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public MailingUser modify(EventInfo eventInfo, MailingUser dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
	
	public List<MailingUser> getUserList(String system, String alarmCode, String sendType) throws CustomException
	{
		String condition = " system = ? AND alarmCode = ? AND sendType = ? ";
				
		List<MailingUser> userList;
		
		try
		{
			userList = this.select(condition, new Object[] {system, alarmCode, sendType});
		}
		catch (Exception ex)
		{
			return userList = new ArrayList<MailingUser>();
		}

		return userList;
	}
	
	public boolean CheckUser(String system, String alarmCode, String userId) throws CustomException
	{
		String condition = " system = ? AND alarmCode = ? AND userID = ? ";
				
		List<MailingUser> userList = new ArrayList<MailingUser>();
		
		try
		{
			userList = this.select(condition, new Object[] {system, alarmCode, userId});
			
			if(userList.size() > 0)
				return true;
		}
		catch (Exception ex)
		{
			if(userList == null || userList.size() < 1)			
				return false;
		}

		return false;
	}
}
