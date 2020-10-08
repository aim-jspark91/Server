package kr.co.aim.messolution.userprofile.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.user.UserServiceProxy;
import kr.co.aim.greentrack.user.management.data.UserProfileKey;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class UserProfileServiceImpl implements ApplicationContextAware{
	/**
	 * @uml.property  name="applicationContext"
	 * @uml.associationEnd  
	 */
	private ApplicationContext     	applicationContext;
	private static Log 				log = LogFactory.getLog(UserProfileServiceImpl.class);
			
	/**
	 * @param arg0
	 * @throws BeansException
	 * @uml.property  name="applicationContext"
	 */
	public void setApplicationContext(ApplicationContext arg0)throws BeansException 
	{
			applicationContext = arg0;
	}

	/*
	* Name : login
	* Desc : This function is login
	* Author : AIM Systems, Inc
	* Date : 2011.01.03
	*/
	public void login(String userId,
				  	  String password,
					  String uiName,
					  String workStationName)
	throws kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, Exception
	{
		
		this.verifyUser(userId);
		
		this.verifyPassword(userId, password);
		
		try
		{
			UserServiceProxy.getUserProfileService().logIn(userId, password, uiName, workStationName);
		}
		catch ( Exception e)
		{
			log.error(e);
			this.logOut(userId, uiName, workStationName);
			
			UserServiceProxy.getUserProfileService().logIn(userId, password, uiName, workStationName);
		}
	}

	/*
	* Name : logOut
	* Desc : This function is logOut
	* Author : AIM Systems, Inc
	* Date : 2011.01.03
	*/
	public void logOut(String userId,
					   String uiName, 
					   String workStationName)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		try{
			UserServiceProxy.getUserProfileService().logOut(userId, uiName, workStationName);
		}catch(Exception e){
			log.warn(e);
		}
	}

	/*
	* Name : changePassword
	* Desc : This function is changePassword
	* Author : AIM Systems, Inc
	* Date : 2011.01.03
	*/
	public void changePassword(String userId,
							   String oldPassword, 
							   String newPassword)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, Exception
	{
		try{
			UserServiceProxy.getUserProfileService().changePassword(userId, oldPassword, newPassword);
		}catch(Exception e){
			throw new CustomException("USER-0002", userId, newPassword);
		}
	}

	/*
	* Name : verifyPassword
	* Desc : This function is verifyPassword
	* Author : AIM Systems, Inc
	* Date : 2011.01.03
	*/
	public void verifyPassword(String userId,
							   String password)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, Exception
	
	{
        if ( !(UserServiceProxy.getUserProfileService().verifyPassword(userId, password)))
        {
        	throw new CustomException("USER-0002", userId, password);
        }
	}

	/*
	* Name : removeUserProfile
	* Desc : This function is removeUserProfile
	* Author : AIM Systems, Inc
	* Date : 2011.01.03
	*/
	public void removeUserProfile(String userId){
		UserServiceProxy.getUserProfileService().removeUserProfile(userId);
	}

	/*
	* Name : verifyUser
	* Desc : This function is verifyUser
	* Author : AIM Systems, Inc
	* Date : 2011.01.03
	*/
	public void verifyUser(String userId) throws Exception
	{
		UserProfileKey userProfileKey = new UserProfileKey();
		
		userProfileKey.setUserId( userId );
		
		try
		{
			UserServiceProxy.getUserProfileService().selectByKey( userProfileKey );
		}
		catch ( Exception e )
		{
			throw new CustomException("USER-0001", userId);
		}
	}
	
	/*
	 * Name : changeShop Desc : This function is changeShop - ChangeShop.bpel
	 * Author : AIM Systems, Inc Date : 2011.04.13
	 */
	public void changeShop(String factoryName, String areaName,
			String eventUser, String eventName) throws CustomException {
		String sql = "SELECT UPPER(G.ACCESSFACTORY) AS ACCESSFACTORY, G.USERGROUPNAME "
				+ "  FROM USERPROFILE P, USERGROUP G "
				+ " WHERE P.USERGROUPNAME = G.USERGROUPNAME "
				+ "   AND P.USERID = :userName ";
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("userName", eventUser);

//		List<Map<String, Object>> sqlResult = greenFrameServiceProxy
//				.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql,
//						bindMap);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		String accessFactory = (String) sqlResult.get(0).get("ACCESSFACTORY");
		if (StringUtils.equals(accessFactory, factoryName)) {
			return;
		} else if (StringUtils.equals(accessFactory, "ALL")
				|| StringUtils.isEmpty(accessFactory)) {
			return;
		} else if (accessFactory.contains("_")) {
			String[] accessFactorys = StringUtils.split(accessFactory, "_");
			for (int i = 0; accessFactorys.length > i; i++) {
				if (StringUtils.equals(accessFactorys[i], factoryName)) {
					return;
				}
			}
		}
		throw new CustomException("USER-0001", eventUser, (String) sqlResult
				.get(0).get("USERGROUPNAME"), factoryName);
	}
}
