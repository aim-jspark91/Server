/**
 * 
 */
package kr.co.aim.messolution.userprofile.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.userprofile.event.MailSender;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.user.UserServiceProxy;
import kr.co.aim.greentrack.user.management.data.UserProfile;
import kr.co.aim.greentrack.user.management.data.UserProfileKey;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * @author sjlee
 *
 */
public class UserProfileServiceUtil implements ApplicationContextAware {

	/**
	 * @uml.property  name="applicationContext"
	 * @uml.associationEnd  
	 */
	private ApplicationContext     	applicationContext;
	private static Log 				log = LogFactory.getLog(UserProfileServiceImpl.class);
	
	
	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	/**
	 * @param arg0
	 * @throws BeansException
	 * @uml.property  name="applicationContext"
	 */
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}
	
	/**
	 * write user account
	 * @author swcho
	 * @since 2014.04.14
	 * @param userId
	 * @param password
	 * @return
	 * @throws FrameworkErrorSignal
	 * @throws NotFoundSignal
	 */
	public Element createUserProfileElement(String userId, String password)
		throws FrameworkErrorSignal, NotFoundSignal
	{
		//if(log.isInfoEnabled()){
		//	log.info("userId = " + userId);
		//}
        
		UserProfileKey userProfileKey = new UserProfileKey();
		userProfileKey.setUserId( userId );

		UserProfile userProfileData = null;
		userProfileData  = UserServiceProxy.getUserProfileService().selectByKey( userProfileKey );
		
		Element element = null;
		String node = null;

		node = "USERPROFILE";
		element = new Element(node);		

		Element elmUserId = new Element("USERID");
		elmUserId.setText(userProfileData.getKey().getUserId());
		element.addContent(elmUserId);
		
		Element elmPassword = new Element("PASSWORD");
		elmPassword.setText(password);
		element.addContent(elmPassword);
		
		Element elmUserName = new Element("USERNAME");
		elmUserName.setText(userProfileData.getUserName());
		element.addContent(elmUserName);

		Element elmUserGroupName = new Element("USERGROUPNAME");
		elmUserGroupName.setText(userProfileData.getUserGroupName());
		element.addContent(elmUserGroupName);

		return element;
	}
	
	/**
	 * validate User access to factory
	 * @author swcho
	 * @since 2016-03-23
	 * @param factoryName
	 * @param userId
	 * @param separatorChar
	 * @throws CustomException
	 */
	public void validateFactoryAccessible(String factoryName, String userId, String separatorChar) throws CustomException
	{
		UserProfile userData = this.getUser(userId);
		
		String strFactorys = CommonUtil.getValue(userData.getUdfs(), "AVAILFACTORYNAME");
		
		if (strFactorys.length() < 1)
		{
			//no assigned & old user, allows to all
			log.warn("allowable access for any factory not yet set");
			return;
		}
		else if (factoryName.isEmpty())
		{
			//old client version, allows to all
			log.warn("client not send request including FACTORYNAME");
			return;
		}
		
		String[] loginFactorys = StringUtil.split(factoryName.trim(), separatorChar);
		String[] arrFactorys = StringUtil.split(strFactorys.trim(), separatorChar);
		
		boolean isFound = false;
		
		for(String loginFactoryName : loginFactorys)
		{
			for (String accessFactoryName : arrFactorys)
			{
				if (accessFactoryName.equalsIgnoreCase("ALL"))
				{
					log.warn("enable to access for all factories");
					isFound = true;
					break;
				}
				else if (accessFactoryName.equalsIgnoreCase(loginFactoryName))
				{
					isFound = true;
					break;
				}
				
				isFound = false;
			}
			
			if (!isFound)
			{
				factoryName = loginFactoryName;
				
				throw new CustomException("USER-0004", userId, factoryName);
			}
		}
	}
	
	public void validateDisableFlag(String userId) throws CustomException
	{
		UserProfile userData = this.getUser(userId);
		
		//String disableFlag = CommonUtil.getValue(userData.getUdfs(), "DISABLEFLAG");
		String disableFlag = userData.getUdfs().get("DISABLEFLAG");
		
		if(StringUtil.equals(disableFlag, "Y"))
		{
			throw new CustomException("USER-0017", userId);
		}

	}
	
	/**
	 * get User
	 * @author swcho
	 * @since 2016-03-23
	 * @param userId
	 * @return
	 * @throws CustomException
	 */
	public UserProfile getUser(String userId) throws CustomException
	{
		UserProfileKey userProfileKey = new UserProfileKey();
		
		userProfileKey.setUserId(userId);
		
		try
		{
			UserProfile userData = UserServiceProxy.getUserProfileService().selectByKey(userProfileKey);
			
			return userData;
		}
		catch ( Exception e )
		{
			throw new CustomException("USER-0001", userId);
		}
	}
	
	public void MailSend(List<String> receiveIDList,String subject, String text) throws CustomException
	{
		try
		{
			StringBuffer contents = new StringBuffer();
			
			//메일 내용을 추가한다.
			contents.append(text);
			
			//메일 보내기
			MailSender mailSend = new MailSender("amsarm2","welcome88!!");
			mailSend.sendSmtp2(receiveIDList, subject, contents.toString(), "", "smtp", "25", "mail.everdisplay.com", "false");

		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0000", ex);
		}	

	}
	
	public List<String> getUserByDept(String department) throws CustomException
	{
		List<String> userList = new ArrayList<String>();
		
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT USERID, EMAIL ");
		sql.append("   FROM USERPROFILE ");
		sql.append("  WHERE DEPARTMENT =:DEPARTMENT  ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("DEPARTMENT", department);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		
		if(sqlResult.size()>0)
		{
			for(int i=0; i<sqlResult.size();i++)
			{
				String mail = (String)sqlResult.get(i).get("EMAIL")!=null?(String)sqlResult.get(i).get("EMAIL").toString():" ";
				
				if(StringUtils.isNotEmpty(mail.trim()))
				{
					userList.add(mail);
				}
			}
		}
		
		return userList;
	}
	
	public List<String> getUserByUserGroup(String userGroupName) throws CustomException
	{
		List<String> emailList = new ArrayList<String>();
		
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT USERID,EMAIL ");
		sql.append("   FROM USERPROFILE ");
		sql.append("  WHERE USERGROUPNAME =:USERGROUPNAME ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("USERGROUPNAME", userGroupName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		
		if(sqlResult.size()>0)
		{
			for(int i=0; i<sqlResult.size();i++)
			{
				if((String)sqlResult.get(i).get("EMAIL") != "")
				{
					emailList.add((String)sqlResult.get(i).get("EMAIL"));	
				}
			}
		}
		return emailList;
	}

}
