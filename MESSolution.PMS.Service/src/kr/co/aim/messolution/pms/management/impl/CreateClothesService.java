package kr.co.aim.messolution.pms.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.pms.management.data.UserClothes;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@CTORMHeader(tag = "PMS",divider = "_")
public class CreateClothesService extends CTORMService<UserClothes>
{

	
	public static Log logger = LogFactory.getLog(CheckListService.class);
	
	private final String historyEntity = "";
	
	public List<UserClothes> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<UserClothes> result = new ArrayList<UserClothes>();
		
		try
		{
			result = super.select(condition, bindSet, UserClothes.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public UserClothes selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(UserClothes.class, isLock, keySet);
	}
	
	public UserClothes create(EventInfo eventInfo, UserClothes dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, "USERCLOTHESHISTORY", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, UserClothes dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, "USERCLOTHESHISTORY", dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public UserClothes modify(EventInfo eventInfo, UserClothes dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, "USERCLOTHESHISTORY", dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

}
