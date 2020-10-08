package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.UserGroupTableAccess;
import kr.co.aim.messolution.extended.object.management.data.UserTableAccess;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UserGroupTableAccessService extends CTORMService<UserGroupTableAccess> {
	
	public static Log logger = LogFactory.getLog(UserTableAccess.class);
	
	
	public List<UserGroupTableAccess> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<UserGroupTableAccess> result = super.select(condition, bindSet, UserGroupTableAccess.class);
		
		return result;
	}
	
	public UserGroupTableAccess selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(UserGroupTableAccess.class, isLock, keySet);
	}
	
	public UserGroupTableAccess create(EventInfo eventInfo, UserGroupTableAccess dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, UserGroupTableAccess dataInfo)
		throws CustomException
	{
		
		super.delete(dataInfo);
	}
	
	public UserGroupTableAccess modify(EventInfo eventInfo, UserGroupTableAccess dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
