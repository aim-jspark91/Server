package kr.co.aim.messolution.pms.event;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.UserClothes;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class SetClothesLimitTime extends SyncHandler
{

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		// TODO EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), "", "");
		
		//Get message from xml
		String departmentID = SMessageUtil.getBodyItemValue(doc, "DEPARTMENTID", true);
		String userID = SMessageUtil.getBodyItemValue(doc, "USERID", false);
		String limitDay = SMessageUtil.getBodyItemValue(doc, "LIMITDAY", false);
		
		if(!StringUtils.isEmpty(departmentID) && StringUtils.isEmpty(userID))
		{
			//update everyUser
			 StringBuffer sqlBuffer = new StringBuffer("")
	          .append(" SELECT A.USERID,A.CLOTHESID ").append("\n")
	          .append("   FROM PMS_USERCLOTHES a, USERPROFILE b ").append("\n")
	          .append(" WHERE A.USERID = B.USERID ").append("\n")
	          .append("       AND B.USERGROUPNAME = ? ").append("\n");
	         

			// Object sqlBuffer;
			Object[] bindSet = new String[]{departmentID};
			@SuppressWarnings("unchecked")
			List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bindSet);
			for(ListOrderedMap clotheMap : sqlResult)
			{
				String clothesID = CommonUtil.getValue(clotheMap, "CLOTHESID");
				Object[] keySet =  new Object[]{clothesID};
				UserClothes userClothes = PMSServiceProxy.getCreateClothesService().selectByKey(true, keySet);
				try
				{
					userClothes.setCleanLimitedTime(NumberFormat.getInstance().parse(limitDay));
				}
				catch (ParseException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				PMSServiceProxy.getCreateClothesService().modify(eventInfo, userClothes );
			}
			
			/*SMessageUtil.addItemToBody(doc, "DEPARTMENTID", departmentID);*/
			
		}	
		
		else if(!StringUtils.isEmpty(departmentID) && !StringUtils.isEmpty(userID))
		{
			String condition = "where USERID=?";
			Object[] bindSet = new Object[]{userID};
		    List<UserClothes> clothesList = PMSServiceProxy.getCreateClothesService().select(condition, bindSet);
			/*UserClothes clothesInfo = null;*/
			//update one User

			for(UserClothes clothesInfo : clothesList)
			{
				try
				{
					clothesInfo.setCleanLimitedTime(NumberFormat.getInstance().parse(limitDay));
				}
				catch (ParseException e)
				{
					e.printStackTrace();
				}
				PMSServiceProxy.getCreateClothesService().modify(eventInfo, clothesInfo );
				
			}
	/*		SMessageUtil.addItemToBody(doc, "DEPARTMENTID", departmentID);
			SMessageUtil.addItemToBody(doc, "USERID", userID);
		*/
		}
		
		return doc;
		
	}

}
