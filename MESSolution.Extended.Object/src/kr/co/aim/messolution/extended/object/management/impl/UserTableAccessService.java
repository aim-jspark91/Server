package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.AlarmActionDef;
import kr.co.aim.messolution.extended.object.management.data.UserTableAccess;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UserTableAccessService extends CTORMService<UserTableAccess> {
	
	public static Log logger = LogFactory.getLog(UserTableAccess.class);
	
	
	public List<UserTableAccess> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<UserTableAccess> result = super.select(condition, bindSet, UserTableAccess.class);
		
		return result;
	}
	
	public UserTableAccess selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(UserTableAccess.class, isLock, keySet);
	}
	
	public UserTableAccess create(EventInfo eventInfo, UserTableAccess dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, UserTableAccess dataInfo)
		throws CustomException
	{
		
		super.delete(dataInfo);
	}
	
	public UserTableAccess modify(EventInfo eventInfo, UserTableAccess dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
