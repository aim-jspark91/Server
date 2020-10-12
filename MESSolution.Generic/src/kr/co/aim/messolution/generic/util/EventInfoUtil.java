package kr.co.aim.messolution.generic.util;

import java.text.SimpleDateFormat;

import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.GenericServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class EventInfoUtil implements ApplicationContextAware
{
	/**
	 * @uml.property  name="applicationContext"
	 * @uml.associationEnd  
	 */
	private ApplicationContext	applicationContext;

	/**
	 * @param arg0
	 * @throws BeansException
	 * @uml.property  name="applicationContext"
	 */
	public void setApplicationContext( ApplicationContext arg0 ) throws BeansException
	{
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}
	
	/*
	* Name : makeEventInfo
	* Desc : This function is makeEventInfo
	* Author : AIM Systems, Inc
	* Date : 2011.02.19
	*/
	public static EventInfo makeEventInfo( String eventName, String eventUser, String eventComment,
			String reasonCodeType, String reasonCode )
	{
		EventInfo eventInfo = new EventInfo();
		
		eventInfo.setBehaviorName( "greenTrack" );
		eventInfo.setEventName( eventName );
		eventInfo.setEventUser( eventUser );
		
		if ( eventComment == null )
			eventComment = "";
		// modify by JHIYING ON2019.07.12 0004289 START
		eventInfo.setReasonCodeType( reasonCodeType );
		eventInfo.setReasonCode( reasonCode );
		
		// modify by JHIYING ON2019.07.12 0004289 end
		eventInfo.setEventComment( eventComment );
		eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
		
		//2019.01.31 dmlee : TimeKey Seq Setting
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String currentTimeKey = sdf.format(System.currentTimeMillis());
		int nextVal = getNextTimeSequence();
		currentTimeKey = currentTimeKey + String.format("%03d", nextVal);
		
		eventInfo.setEventTimeKey( currentTimeKey );
		//2019.01.31 dmlee : ---------------------
		
		// Do Not Check TimeKey Validation
		eventInfo.setCheckTimekeyValidation(false);
		
		if ( reasonCodeType == null )
			reasonCodeType = "";
		
		eventInfo.setReasonCodeType( reasonCodeType );
		
		if ( reasonCode == null )
			reasonCode = "";
		
		eventInfo.setReasonCode( reasonCode );
		
		return eventInfo;
	}
	
	
	public static int getNextTimeSequence()
	{
		String sql = "select TIMEKEYID.nextval from dual";
		int nextVal = GenericServiceProxy.getSqlMesTemplate().queryForInt(sql);
		return nextVal;
	}
}