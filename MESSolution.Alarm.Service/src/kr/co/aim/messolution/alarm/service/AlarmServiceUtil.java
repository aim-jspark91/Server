package kr.co.aim.messolution.alarm.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.orm.ObjectAttributeDef;
import kr.co.aim.greentrack.alarm.AlarmServiceProxy;
import kr.co.aim.greentrack.alarm.management.data.AlarmDefinition;
import kr.co.aim.greentrack.alarm.management.data.AlarmDefinitionKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.name.NameServiceProxy;
import kr.co.aim.greentrack.name.management.data.NameGeneratorRuleDefKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class AlarmServiceUtil implements ApplicationContextAware
{
	
	/**
	 */
	private ApplicationContext		applicationContext;
	private static Log				log = LogFactory.getLog("AlarmServiceUtil");
	
	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	
	/**
	 * @param arg0
	 * @throws BeansException
	 */
	public void setApplicationContext( ApplicationContext arg0 ) throws BeansException
	{
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}
	
	/*
	* Name : setNamedValueSequence 
	* Desc : This function is set Alarm NameValueSequence
	* Author : AIM Systems, Inc
	* Date : 2011.01.20
	*/
	public  Map<String, String> setNamedValueSequence(Element element) 
	throws FrameworkErrorSignal, NotFoundSignal
	{
		Map<String, String> namedValuemap = new HashMap<String, String>();

		List<ObjectAttributeDef> objectAttributeDefs 
		  = greenFrameServiceProxy.getObjectAttributeMap().getAttributeNames("Alarm", "ExtendedC");

		log.info("UDF SIZE=" + objectAttributeDefs.size());

		if ( objectAttributeDefs != null )
		{
			for ( int i = 0; i < objectAttributeDefs.size(); i++ )
			{
				String name = "";
				String value = "";
				
				if ( element != null )
				{
					for ( int j = 0; j < element.getContentSize(); j++ )
					{
						if ( element.getChildText(objectAttributeDefs.get(i).getAttributeName()) != null )
						{							
							name  = objectAttributeDefs.get(i).getAttributeName();
							value = element.getChildText(objectAttributeDefs.get(i).getAttributeName());
							
							log.info("AttributName : " + i + " " + objectAttributeDefs.get(i).getAttributeName());
							log.info("ElementText : " + i + " " + element.getChildText(objectAttributeDefs.get(i).getAttributeName()));

							break;
						}
						else
						{
							name  = objectAttributeDefs.get(i).getAttributeName();
						}
					}
				}
				else
				{
					
				}
								
				if ( name.equals("") != true )
					namedValuemap.put(name, value);
			}
		}
		
		log.info("UDF SIZE=" + namedValuemap.size());
		return namedValuemap;
	}
	
	/*
	* Name : generateAlarmID
	* Desc : This function is Generate AlarmID
	* Author : AIM Systems, Inc
	* Date : 2011.01.20
	*/
	public List<String> generateAlarmId(String alarmDefName, String quantity)
	{
		if(log.isInfoEnabled()){
			log.info("alarmDefName = " + alarmDefName);
			log.info("quantity = " + quantity);
		}
		
		log.info("Generate alarm Name");
		
		ArrayList<String> list = new ArrayList<String>();
		
		int count = Integer.parseInt(quantity);
		
		NameGeneratorRuleDefKey nameGeneratorRuleDefKey = new NameGeneratorRuleDefKey();
		nameGeneratorRuleDefKey.setRuleName("ALARMID");
		
		try
		{
			NameServiceProxy.getNameGeneratorRuleDefService().selectByKey(nameGeneratorRuleDefKey);
		}
		catch (FrameworkErrorSignal e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (NotFoundSignal e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<String> argSeq = new ArrayList<String>();
		argSeq.add(alarmDefName);

		List<String> alarmIdList = null;
		
		try 
		{
			alarmIdList = NameServiceProxy.getNameGeneratorRuleDefService().generateName("ALARMID", argSeq, count);
		} 
		catch (FrameworkErrorSignal e)
		{
			log.error(e);
		}
		catch (NotFoundSignal e)
		{
		}
		
		log.info("AlarmIDList Length : " + alarmIdList.size());
		
		for ( int i = 0; i < count; i++)
		{
			list.add(alarmIdList.get(i));
			log.info(list.get(i));
		}
		return list;
	}
	
	/*
	* Name : getAlarmDefinitionData
	* Desc : This function is Get Alarm DefinitionData
	* Author : AIM Systems, Inc
	* Date : 2011.01.20
	*/
	public AlarmDefinition getAlarmDefinitionData(String alarmId)
	{
		if(log.isInfoEnabled()){
			log.info("alarmId = " + alarmId);
		}
		
		AlarmDefinitionKey key = new AlarmDefinitionKey();
		key.setAlarmId(alarmId);
		AlarmDefinition alarmDefinition = new AlarmDefinition();
		try
		{
			alarmDefinition = AlarmServiceProxy.getAlarmDefinitionService().selectByKey(key);
		}
		catch (Exception e)
		{
			

		}

		return alarmDefinition;
	}
}
