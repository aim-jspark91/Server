package kr.co.aim.messolution.alarm.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.alarm.MESAlarmServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Alarm;
import kr.co.aim.messolution.extended.object.management.data.AlarmActionDef;
import kr.co.aim.messolution.extended.object.management.data.AlarmDefinition;
import kr.co.aim.messolution.extended.object.management.data.AlarmMailTemplate;
import kr.co.aim.messolution.extended.object.management.data.LotAction;
import kr.co.aim.messolution.extended.object.management.data.MachineAlarmList;
import kr.co.aim.messolution.extended.object.management.data.MailingUser;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.userprofile.MESUserServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author gksong
 * @date 2009.02.16
 */

public class AlarmServiceImpl implements ApplicationContextAware {
    /**
     */
    private ApplicationContext applicationContext;
    private static Log log = LogFactory.getLog("AlarmServiceImpl");

    /**
     * @param arg0
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext arg0)
            throws BeansException {
        applicationContext = arg0;
    }
    
    // Added by smkang on 2018.05.07 - Move create action from CreateAlarm.
    // Modified by smkang on 2019.02.21 - SubUnitName is added in CT_ALARM.
//    public Alarm createAlarm(String alarmCode, String alarmSeverity, String alarmState, String alarmType, String description, String factoryName, String machineName, String unitName, String productList, EventInfo eventInfo) throws CustomException {
    public Alarm createAlarm(String alarmCode, String alarmSeverity, String alarmState, String alarmType, String description, String factoryName, String machineName, String unitName, String subUnitName, String productList, EventInfo eventInfo) throws CustomException {
        
        Alarm alarmData = new Alarm(alarmCode, eventInfo.getEventTimeKey());
        
        alarmData.setAlarmSeverity(alarmSeverity);
        alarmData.setAlarmState(alarmState);
        alarmData.setAlarmType(alarmType);
        alarmData.setDescription(description);
        alarmData.setFactoryName(factoryName);
        
        //history trace
        // Modified by smkang on 2018.05.30 - If a alarm is set, CreateTimeKey would be updated and if a alarm is cleared, ResolvedTimeKey would be updated.
//      alarmData.setCreateTimeKey(eventInfo.getEventTimeKey());
//      alarmData.setCreateUser(eventInfo.getEventUser());
        if (StringUtils.equals(alarmState, GenericServiceProxy.getConstantMap().ALARMSTATE_ISSUE)) {
            alarmData.setCreateTimeKey(eventInfo.getEventTimeKey());
            alarmData.setCreateUser(eventInfo.getEventUser());
        } else if (StringUtils.equals(alarmState, GenericServiceProxy.getConstantMap().ALARMSTATE_CLEAR)) {
            alarmData.setResolveTimeKey(eventInfo.getEventTimeKey());
            alarmData.setResolveUser(eventInfo.getEventUser());
        }
            
        alarmData.setCreateUser(eventInfo.getEventUser());        
        alarmData.setLastEventName(eventInfo.getEventName());
        alarmData.setLastEventTimeKey(eventInfo.getEventTimeKey());
        alarmData.setLastEventUser(eventInfo.getEventUser());
        alarmData.setLastEventComment(eventInfo.getEventComment());        
        alarmData.setMachineName(machineName);
        alarmData.setUnitName(unitName);
        alarmData.setSubUnitName(subUnitName);	// Added by smkang on 2019.02.21 - SubUnitName is added in CT_ALARM.
        alarmData.setProductList(productList);
        
        return ExtendedObjectProxy.getAlarmService().create(eventInfo, alarmData);
    }
    
    public void doAlarmAction(EventInfo eventInfo, Document doc, AlarmDefinition alarmDefData) throws CustomException
    {
    	String factoryName = alarmDefData.getFactoryName();
		String alarmCode = alarmDefData.getAlarmCode();
		String alarmType = alarmDefData.getAlarmType();

		String machineName =  SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String lotName =  SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String carrierName =  SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", false);
		String chartName = SMessageUtil.getBodyItemValue(doc, "CHARTNAME", false);
		String ruleList = SMessageUtil.getBodyItemValue(doc, "RULELIST", false);
		// 2019.03.18_hsryu_Add MaterialName. requested by CIM&SPC.
		String materialName = SMessageUtil.getBodyItemValue(doc, "MATERIALNAME", false);
		String itemName = SMessageUtil.getBodyItemValue(doc, "ITEMNAME", false);
		String chartID = SMessageUtil.getBodyItemValue(doc, "CHARTID", false);

		String changeRecipeList = SMessageUtil.getBodyItemValue(doc, "CHANGERECIPELIST", false);
		String changeParamList = SMessageUtil.getBodyItemValue(doc, "CHANGEPARAMLIST", false);
		
		String ocapReport = SMessageUtil.getBodyItemValue(doc, "OCAPREPORT", false);
		String SPCAlarmType = SMessageUtil.getBodyItemValue(doc, "SPCALARMTYPE", false);
		
        List<AlarmActionDef> actionList = null;
        try {
            actionList = ExtendedObjectProxy.getAlarmActionDefService().select("alarmCode = ? ORDER BY seq ", new Object[] {alarmCode});
        } catch (greenFrameDBErrorSignal ne) {
            log.info(ne);
            return;
        } catch (Exception e) {
            // TODO: handle exception
            log.error(e);
            return;
        }
        
        for (AlarmActionDef actionData : actionList)
        {
            log.info(String.format("Alarm[%s] AlarmAction[%s] would be executed soon", actionData.getAlarmCode(), actionData.getActionName()));
            
            if (StringUtil.equals(actionData.getActionName(), "CSTHold")) 
            {
                try {
                    DurableKey durableKey = new DurableKey();
                    durableKey.setDurableName(carrierName);
                    
                    Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
                    
                    if (StringUtil.equals(durableData.getUdfs().get("DURABLEHOLDSTATE"), "Y"))
                        throw new CustomException("CST-0005", carrierName);
                
                       // 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//                    Map<String, String> udfs = durableData.getUdfs();
//                    udfs.put("DURABLEHOLDSTATE", "Y");
                    
                    SetEventInfo setEventInfo = new SetEventInfo();
//                    setEventInfo.setUdfs(udfs);
                    setEventInfo.getUdfs().put("DURABLEHOLDSTATE", "Y");
                    
                    eventInfo = EventInfoUtil.makeEventInfo("Hold", eventInfo.getEventUser(), eventInfo.getEventComment(), actionData.getReasonCodeType(), actionData.getReasonCode());
                    eventInfo.setReasonCode(actionData.getReasonCode());
                    eventInfo.setReasonCodeType(actionData.getReasonCodeType());
    
                    MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
                    
                } catch (Exception e) {
                	log.info(String.format("Alarm[%s] AlarmAction[%s] Fail !", actionData.getAlarmCode(), actionData.getActionName()));
                	log.info(e.getStackTrace().toString());
                }               
            } 
            else if (StringUtil.equals(actionData.getActionName(), "LotHold")) 
            {          
                try {
                    
                    /*============================================================================
                     * Added by jjyoo 2018.5.13
                     * If action name is 'LotHold', do Lot hold future action.
                     *============================================================================*/
                    eventInfo = EventInfoUtil.makeEventInfo("FutureAHold", eventInfo.getEventUser(), eventInfo.getEventComment(), actionData.getReasonCodeType(), actionData.getReasonCode());
                    
                    Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
                    
                    //2019.05.05 dmlee : If Lot WAIT -> Direct Hold
                    if(lotData.getLotProcessState().equals(GenericServiceProxy.getConstantMap().Lot_Wait))
                    {
                    	eventInfo.setEventName("Hold");
                    	eventInfo.setEventUser(machineName);
                    	
                        //2019.04.30 dmlee : If SPC Alarm, Lot Action Note Record SPC Alarm Info Mantis 3695
                        String lotActionEventComInfo = null;
                        if(alarmType.equals("SPC"))
                        {
                        	lotActionEventComInfo = "SPC Alarm Lot["+lotName+"] SPCAlarmType["+SPCAlarmType+"] MaterialName["+materialName+"] ItemName["+itemName+"] ChartID["+chartID+"]";
                        	eventInfo.setEventComment(lotActionEventComInfo);
                        }
                    	
                    	List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
                    	Map<String, String> udfs = new HashMap<String, String>();
                    	
    					MakeOnHoldInfo makeOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeOnHoldInfo(productUSequence, udfs);
    					makeOnHoldInfo.getUdfs().put("NOTE", lotActionEventComInfo != null ? lotActionEventComInfo : eventInfo.getEventComment());
    					LotServiceProxy.getLotService().makeOnHold(lotData.getKey(), eventInfo, makeOnHoldInfo);
    					
    					// Modified by smkang on 2019.05.24 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//    					// 2019.05.21_hsryu_reselect. 
//    					lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
//
//    					//2019.05.13 dmlee : Clear Note
//    					Map<String, String> lotUdfs = new HashMap<String, String>();
//    					lotUdfs.put("NOTE", "");
//    					lotData.setUdfs(lotUdfs);
//    					LotServiceProxy.getLotService().update(lotData);
    					Map<String, String> updateUdfs = new HashMap<String, String>();
    					updateUdfs.put("NOTE", "");
    					MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
    					
    					MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(lotName, actionData.getReasonCode(), actionData.getDepartment(), "BHOLD", eventInfo);
                    }
                    //2019.05.05 dmlee : If Lot RUN -> AHold
                    else
                    {
                        String processFlowName = lotData.getProcessFlowName();
                        String processOperationName = lotData.getProcessOperationName();
                        String holdType = "AHOLD";
                        String holdPermanentFlag = "N";
                    	
                    	/*============= Get Last Position =============*/   
                        long lastPosition = Integer.parseInt(this.getLastPosition(lotName, factoryName, processFlowName, processOperationName));
                        
                        LotAction lotActionData = new LotAction(lotName, factoryName, processFlowName,"00001", processOperationName, "00001", lastPosition + 1);
                        String actionName = GenericServiceProxy.getConstantMap().ACTIONNAME_SYSTEMHOLD;
                        
                        lotActionData.setActionName(actionName);
                        lotActionData.setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_CREATED);
                        lotActionData.setFactoryName(factoryName);
                        lotActionData.setHoldCode(actionData.getReasonCode());
                        lotActionData.setHoldPermanentFlag(holdPermanentFlag);
                        lotActionData.setHoldType(holdType);
                        lotActionData.setDepartment(actionData.getDepartment());
                        
                        //2019.04.30 dmlee : If SPC Alarm, Lot Action Note Record SPC Alarm Info Mantis 3695
                        String lotActionEventComInfo = null;
                        if(alarmType.equals("SPC"))
                        {
                        	lotActionEventComInfo = "SPC Alarm Lot["+lotName+"] SPCAlarmType["+SPCAlarmType+"] MaterialName["+materialName+"] ItemName["+itemName+"] ChartID["+chartID+"]";
                        	eventInfo.setEventComment(lotActionEventComInfo);
                        }
            
                        lotActionData.setLastEventName(eventInfo.getEventName());
                        lotActionData.setLastEventTime(eventInfo.getEventTime());
                        lotActionData.setLastEventTimeKey(eventInfo.getEventTimeKey());
                        lotActionData.setLastEventUser(machineName);
                        lotActionData.setLastEventComment(lotActionEventComInfo != null ? lotActionEventComInfo : eventInfo.getEventComment());
                        
                        eventInfo.setReasonCode(actionData.getReasonCode());
                        eventInfo.setReasonCodeType(actionData.getReasonCodeType());
                        
                        /*============= Set lot action data to CT_LOTACTION table =============*/
                        ExtendedObjectProxy.getLotActionService().create(eventInfo, lotActionData);
                    }
                
                } catch (Exception e) {
                	 	
                	log.info(String.format("Alarm[%s] AlarmAction[%s] Fail !", actionData.getAlarmCode(), actionData.getActionName()));
                	log.info(e.getStackTrace().toString());
                	
                }
            } 
            else if (StringUtil.equals(actionData.getActionName(), "Notice")) 
            {   
                try
                {
                    Element eleBody = new Element(SMessageUtil.Body_Tag);
                    
                    Element eleMachineName = new Element("MACHINENAME");
                    eleMachineName.setText(machineName);
                    eleBody.addContent(eleMachineName);
                    
                    Element eleDesc = new Element("DESCRIPTION");
                    eleDesc.setText(alarmDefData.getDescription());
                    eleBody.addContent(eleDesc);
                    
                    Document requestDoc = SMessageUtil.createXmlDocument(eleBody, "AlarmMsg",
                            "", "", "MES", "Alarm Notice");

                    GenericServiceProxy.getESBServive().sendBySender(requestDoc, "OICSender");
                }
                catch (Exception ex)
                {
                	log.info(String.format("Alarm[%s] AlarmAction[%s] Fail !", actionData.getAlarmCode(), actionData.getActionName()));
                	log.info(ex.getStackTrace().toString());
                }
            } 
            else if(StringUtil.equals(actionData.getActionName(), "OPCall")) 
            {    
                /*============= Send machine name info to PEXsvr =============*/
                Element eleBody = new Element(SMessageUtil.Body_Tag);

                Element eleMachineName = new Element("MACHINENAME");
                eleMachineName.setText(machineName);
                eleBody.addContent(eleMachineName); 

                try {
                    String targetSubject = GenericServiceProxy.getESBServive().getSendSubject("PEXsvr");
                    
                    Document requestDoc = SMessageUtil.createXmlDocument(eleBody, "E_OpCallSend",
                            "",//SMessageUtil.getHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", false),
                            targetSubject,
                            "MES",
                            "OpCallSend");

                    GenericServiceProxy.getESBServive().sendBySender(targetSubject, requestDoc, "PEXSender");
                } catch (Exception ex) {
                	log.info(String.format("Alarm[%s] AlarmAction[%s] Fail !", actionData.getAlarmCode(), actionData.getActionName()));
                	log.info(ex.getStackTrace().toString());
                }               
            }
            else if(StringUtil.equals(actionData.getActionName(), "Email")) 
            {  
				List<MailingUser> mailingUserData = null;	
            	
            	try
            	{
            		mailingUserData = ExtendedObjectProxy.getMailingUserService().select("WHERE ALARMCODE = ?", new Object[]{alarmCode});
            	}
            	catch(Exception ex){}
            	
            	if(mailingUserData.size() > 0)
            	{
            		List<String> userEmailList = new ArrayList<String>();
            		
            		for(MailingUser mailingUser : mailingUserData)
            		{
            			if(!StringUtils.equals(mailingUser.getUserGroupName(), "-")) // In case that user group is enrolled.
            			{
            				StringBuilder sql = new StringBuilder();
            				sql.append(" SELECT USERID,EMAIL ");
            				sql.append("   FROM USERPROFILE ");
            				sql.append("  WHERE USERGROUPNAME =:USERGROUPNAME ");

            				Map<String, Object> bindMap = new HashMap<String, Object>();
            				bindMap.put("USERGROUPNAME", mailingUser.getUserGroupName());

            				@SuppressWarnings("unchecked")
            				List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
            				
            				if(sqlResult.size()>0)
            				{
            					for(int i=0; i<sqlResult.size();i++)
            					{
            						// Modified by smkang on 2019.03.19 - Skip invalid e-mail address.
//            						if(sqlResult.get(i).get("EMAIL") != null && StringUtils.isNotEmpty((String) sqlResult.get(i).get("EMAIL")))
           							if(sqlResult.get(i).get("EMAIL") != null && StringUtils.endsWith((String) sqlResult.get(i).get("EMAIL"), "@everdisplay.com"))
            						{
            							if(!userEmailList.contains((String)sqlResult.get(i).get("EMAIL")))
            							{
            								userEmailList.add((String)sqlResult.get(i).get("EMAIL"));	
            							}
            						}
            					}
            				}
            			}
            			
            			if(!StringUtils.equals(mailingUser.getUserID(), "-")) // In case that userID is enrolled.
            			{
            				// Modified by smkang on 2019.03.19 - Skip invalid e-mail address.
//            				if(StringUtils.isNotEmpty(mailingUser.getMailAddr()))
           					if(StringUtils.endsWith(mailingUser.getMailAddr(), "@everdisplay.com"))
                			{
            					if(!userEmailList.contains(mailingUser.getMailAddr()))
            					{
            						if(!mailingUser.getMachineName().isEmpty())
            						{
            							if(!mailingUser.getMachineName().equals(machineName))
            							{
            								userEmailList.add(mailingUser.getMailAddr());
            							}
            						}
            						else
            						{
            							userEmailList.add(mailingUser.getMailAddr());
            						}
            					}
                			}
            			}	
            		}
            		
            		String title = "";
            		String contentTemp = "";
            		StringBuilder alarmContent = new StringBuilder();
            		
            		try
            		{
            			AlarmMailTemplate mailTemplateData = ExtendedObjectProxy.getAlarmMailTemplateService().selectByKey(false, new Object[]{alarmType, alarmCode});
            			
            			try
            			{
                			title = mailTemplateData.getTitle();
                			title = title.replaceAll("!<MACHINENAME>", machineName);
                			title = title.replaceAll("!<RECIPENAME>", recipeName);
                			title = title.replaceAll("!<ALARMTYPE>", alarmType);
                			title = title.replaceAll("!<RULELIST>", ruleList);
                			title = title.replaceAll("!<CHARTID>", chartID);
                			title = title.replaceAll("!<CHARTNAME>", chartName);
                			title = title.replaceAll("!<LOTNAME>", lotName);
                			title = title.replaceAll("!<CHANGERECIPELIST>", changeRecipeList);
                			title = title.replaceAll("!<MATERIALNAME>", materialName);
                			title = title.replaceAll("!<ITEMNAME>", itemName);
                			title = title.replaceAll("!<CHANGEPARAMLIST>", changeParamList);
                			title = title.replaceAll("!<SPCALARMTYPE>", SPCAlarmType);

                			contentTemp = mailTemplateData.getComments().replaceAll("!<MACHINENAME>", machineName);
                			contentTemp = contentTemp.replaceAll("!<RECIPENAME>", recipeName);
                			contentTemp = contentTemp.replaceAll("!<ALARMTYPE>", alarmType);
                			contentTemp = contentTemp.replaceAll("!<RULELIST>", ruleList);
                			contentTemp = contentTemp.replaceAll("!<CHARTID>", chartID);
                			contentTemp = contentTemp.replaceAll("!<CHARTNAME>", chartName);
                			contentTemp = contentTemp.replaceAll("!<LOTNAME>", lotName);
                			contentTemp = contentTemp.replaceAll("!<CHANGERECIPELIST>", changeRecipeList);
                			//2019.03.18_hsryu_Insert Logic. Requested by CIM&SPC.
                			contentTemp = contentTemp.replaceAll("!<MATERIALNAME>", materialName);
                			contentTemp = contentTemp.replaceAll("!<ITEMNAME>", itemName);
                			contentTemp = contentTemp.replaceAll("!<CHANGEPARAMLIST>", changeParamList);
                			contentTemp = contentTemp.replaceAll("!<OCAPREPORT>", ocapReport);
                			contentTemp = contentTemp.replaceAll("!<SPCALARMTYPE>", SPCAlarmType);

            			}
            			catch(Exception ex)
            			{
            				
            			}
            			
            			alarmContent.append(contentTemp);
            			
            		}
            		catch(Exception ex)
            		{
            			try
            			{
            				AlarmMailTemplate mailTemplateData = ExtendedObjectProxy.getAlarmMailTemplateService().selectByKey(false, new Object[]{alarmType, "*"});
            				
                			title = mailTemplateData.getTitle();
                			title = title.replaceAll("!<MACHINENAME>", machineName);
                			title = title.replaceAll("!<RECIPENAME>", recipeName);
                			title = title.replaceAll("!<ALARMTYPE>", alarmType);
                			title = title.replaceAll("!<RULELIST>", ruleList);
                			title = title.replaceAll("!<CHARTID>", chartID);
                			title = title.replaceAll("!<CHARTNAME>", chartName);
                			title = title.replaceAll("!<LOTNAME>", lotName);
                			title = title.replaceAll("!<CHANGERECIPELIST>", changeRecipeList);
                			title = title.replaceAll("!<MATERIALNAME>", materialName);
                			title = title.replaceAll("!<ITEMNAME>", itemName);
                			title = title.replaceAll("!<CHANGEPARAMLIST>", changeParamList);
                			title = title.replaceAll("!<SPCALARMTYPE>", SPCAlarmType);
                			
                			contentTemp = mailTemplateData.getComments().replaceAll("!<MACHINENAME>", machineName);
                			contentTemp = contentTemp.replaceAll("!<RECIPENAME>", recipeName);
                			contentTemp = contentTemp.replaceAll("!<ALARMTYPE>", alarmType);
                			contentTemp = contentTemp.replaceAll("!<RULELIST>", ruleList);
                			contentTemp = contentTemp.replaceAll("!<CHARTID>", chartID);
                			contentTemp = contentTemp.replaceAll("!<CHARTNAME>", chartName);
                			contentTemp = contentTemp.replaceAll("!<LOTNAME>", lotName);
                			contentTemp = contentTemp.replaceAll("!<CHANGERECIPELIST>", changeRecipeList);
                			//2019.03.18_hsryu_Insert Logic. Requested by CIM&SPC.
                			contentTemp = contentTemp.replaceAll("!<MATERIALNAME>", materialName);
                			contentTemp = contentTemp.replaceAll("!<ITEMNAME>", itemName);
                			contentTemp = contentTemp.replaceAll("!<CHANGEPARAMLIST>", changeParamList);
                			contentTemp = contentTemp.replaceAll("!<OCAPREPORT>", ocapReport);
                			contentTemp = contentTemp.replaceAll("!<SPCALARMTYPE>", SPCAlarmType);
                			
                			alarmContent.append(contentTemp);
            			}
            			catch(Exception ex2)
            			{
            				title="Alarm Type ["+ alarmType +"] Send Alarm Mail Fail !, Plase Setting Alarm Mail Template !";
            				
            				log.error("Alarm Type ["+ alarmType +"] Send Alarm Mail Fail !, Plase Setting Alarm Mail Template !");
            			}

            		}
    				
    				try
    				{
    					log.info("-------------------Send E-Mail Title ["+title+"]-----------------------");
    					log.info("-------------------Send E-Mail User ["+userEmailList.toString()+"]-----------------------");
    					
    					MESUserServiceProxy.getUserProfileServiceUtil().MailSend(userEmailList, title, alarmContent.toString());
    					
    					log.info("-------------------Send E-Mail Success ! -----------------------");
    				}
    				catch(CustomException ex)
    				{
    					log.error(ex);
    					log.error("-------------------Send E-Mail Fail ! -----------------------");
    				}
            	}
            }
        }
    }
    
    
    public String getLastPosition(String lotName, String factoryName, String flowName, String operationName)
    {
    	try
    	{
            String getPositionSql = "SELECT POSITION "
                    + " FROM CT_LOTACTION "
                    + " WHERE LOTNAME = :LOTNAME "
                    + " AND FACTORYNAME = :FACTORYNAME "
                    + " AND PROCESSFLOWNAME = :PROCESSFLOWNAME "
                    + " AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION "
                    + " AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME "
                    + " AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION "
                    + " ORDER BY POSITION DESC";
            
            Map<String, Object> getPositionBind = new HashMap<String, Object>();
            getPositionBind.put("LOTNAME", lotName);
            getPositionBind.put("FACTORYNAME", factoryName);
            getPositionBind.put("PROCESSFLOWNAME", flowName);
            getPositionBind.put("PROCESSFLOWVERSION", "00001");
            getPositionBind.put("PROCESSOPERATIONNAME", operationName);
            getPositionBind.put("PROCESSOPERATIONVERSION", "00001");
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> positionSqlBindSet = GenericServiceProxy.getSqlMesTemplate().queryForList(getPositionSql, getPositionBind); 
            
            if(positionSqlBindSet.size() == 0)
            {
                return "0";
            }
            
            return positionSqlBindSet.get(0).get("POSITION").toString();
    	}
    	catch(Exception ex)
    	{
    		return "0";
    	}
    }
    
    /**
     * 
     * @Name     getAlarmData
     * @since    2018. 8. 25.
     * @author   Admin
     * @contents 
     *           
     * @param alarmCode
     * @param factoryName
     * @param machineName
     * @param unitName
     * @param subUnitName
     * @return
     * @throws CustomException
     */
    public List<Alarm> getAlarmData(String alarmCode, String machineName, String unitName, String subUnitName) throws CustomException
    {
        List<Alarm> alarmData = null;
        Object[] bindSet = null ;
        String condition = " WHERE TIMEKEY = (SELECT MIN(TIMEKEY) FROM CT_ALARM CA WHERE 1=1 ";
        condition = condition + " AND CA.ALARMCODE = ? AND CA.ALARMSTATE = ? AND CA.MACHINENAME = ? ";
        
        if(StringUtil.isNotEmpty(subUnitName))
        {
            condition = condition + " AND CA.UNITNAME = ? AND CA.SUBUNITNAME = ? )";
            bindSet = new Object[]{ alarmCode, GenericServiceProxy.getConstantMap().ALARMSTATE_ISSUE, machineName, unitName, subUnitName };
        }
        else if(StringUtil.isNotEmpty(unitName))
        {
            condition = condition + " AND CA.UNITNAME = ? )";
            bindSet = new Object[]{ alarmCode, GenericServiceProxy.getConstantMap().ALARMSTATE_ISSUE, machineName, unitName };
        }
        else
        {
            condition = condition + " )";
            bindSet = new Object[]{ alarmCode, GenericServiceProxy.getConstantMap().ALARMSTATE_ISSUE, machineName };
        }
        
        try
        {
            alarmData = ExtendedObjectProxy.getAlarmService().select(condition, bindSet);
            
        }
        catch (Exception e)
        {
            alarmData = null;
        }
        
        return alarmData;
    }
    public void createMachineAlarm(AlarmDefinition alarmDefinition,String machineName, String unitName, String subUnitName, String alarmCode, String alarmState, EventInfo eventInfo ) throws CustomException{
    	try {
    		Object[] bindMap = new Object[] {machineName, StringUtils.isNotEmpty(unitName) ? unitName : " ", StringUtils.isNotEmpty(subUnitName) ? subUnitName : " ", alarmCode};
            MachineAlarmList machineAlarmList = ExtendedObjectProxy.getMachineAlarmListService().selectByKey(false, bindMap);

    	} catch (Exception ex) {
    		if (StringUtils.equals(alarmState, GenericServiceProxy.getConstantMap().ALARMSTATE_ISSUE)) {
    			MachineAlarmList machineAlarmList = new MachineAlarmList(machineName, StringUtils.isNotEmpty(unitName) ? unitName : " ", StringUtils.isNotEmpty(subUnitName) ? subUnitName : " ", alarmCode);
    			machineAlarmList.setAlarmSeverity(alarmDefinition.getAlarmSeverity());
    			machineAlarmList.setAlarmState(alarmState);
    			machineAlarmList.setAlarmText(alarmDefinition.getDescription());
    			machineAlarmList.setCreateTime(eventInfo.getEventTime());
    			machineAlarmList.setCreateUser(eventInfo.getEventUser());
    			machineAlarmList.setIssueTime(eventInfo.getEventTime());
    			machineAlarmList.setIssueUser(eventInfo.getEventUser());
    			
    			//2019.06.24_hsryu_Add Logic. Requested by Report.
    			machineAlarmList.setLastEventTimeKey(eventInfo.getEventTimeKey());
    			
            	ExtendedObjectProxy.getMachineAlarmListService().create(eventInfo, machineAlarmList);
    		}
        }
    }

    // Modified by smkang on 2019.02.20 - If an alarm is cleared, it will be removed from CT_MACHINEALARMLIST.
    public void setMachineAlarm(MachineAlarmList machineAlarmData, String machineName, String unitName, String subUnitName, String alarmCode, String alarmState, EventInfo eventInfo) throws CustomException {
    	try {
    		Object[] bindMap = new Object[] {machineName, StringUtils.isNotEmpty(unitName) ? unitName : " ", StringUtils.isNotEmpty(subUnitName) ? subUnitName : " ", alarmCode};
            MachineAlarmList machineAlarmList = ExtendedObjectProxy.getMachineAlarmListService().selectByKey(false, bindMap);
            
            machineAlarmData.setCreateTime(machineAlarmList.getCreateTime());
            machineAlarmData.setCreateUser(machineAlarmList.getCreateUser());
            
            //2019.06.24_hsryu_Add Logic. Requested by Report.
            machineAlarmData.setLastEventTimeKey(eventInfo.getEventTimeKey());
            
            if (StringUtils.equals(alarmState, GenericServiceProxy.getConstantMap().ALARMSTATE_ISSUE)) {
            	machineAlarmData.setIssueTime(eventInfo.getEventTime());
            	machineAlarmData.setIssueUser(eventInfo.getEventUser());
            	
            	ExtendedObjectProxy.getMachineAlarmListService().modify(eventInfo, machineAlarmData);
            } else if (StringUtils.equals(alarmState, GenericServiceProxy.getConstantMap().ALARMSTATE_CLEAR)) {
                machineAlarmData.setIssueTime(machineAlarmList.getIssueTime());
                machineAlarmData.setIssueUser(machineAlarmList.getIssueUser());
                machineAlarmData.setResolveTime(eventInfo.getEventTime());
                machineAlarmData.setResolveUser(eventInfo.getEventUser());

                ExtendedObjectProxy.getMachineAlarmListService().remove(eventInfo, machineAlarmData);
            }
    	} catch (Exception ex) {
    		if (StringUtils.equals(alarmState, GenericServiceProxy.getConstantMap().ALARMSTATE_ISSUE)) {
    			machineAlarmData.setCreateTime(eventInfo.getEventTime());
        		machineAlarmData.setCreateUser(eventInfo.getEventUser());
            	machineAlarmData.setIssueTime(eventInfo.getEventTime());
            	machineAlarmData.setIssueUser(eventInfo.getEventUser());
            	
                //2019.06.24_hsryu_Add Logic. Requested by Report.
                machineAlarmData.setLastEventTimeKey(eventInfo.getEventTimeKey());
            	
            	ExtendedObjectProxy.getMachineAlarmListService().create(eventInfo, machineAlarmData);
    		}
        }
    }
    public void removeAlarm(String machineName, String unitName, String subUnitName, String alarmCode, EventInfo eventInfo){
    	try {
    		
    		try {
    			Object[] bindMap = new Object[] {machineName, StringUtils.isNotEmpty(unitName) ? unitName : " ", StringUtils.isNotEmpty(subUnitName) ? subUnitName : " ", alarmCode};
                MachineAlarmList machineAlarmList = ExtendedObjectProxy.getMachineAlarmListService().selectByKey(false, bindMap);
                ExtendedObjectProxy.getMachineAlarmListService().remove(eventInfo, machineAlarmList);	
			} catch (Exception e) {
				// TODO: handle exception
			}
    		
            try {
                List<Alarm> alarmData = null;
                Object[] alarmBindMap = new Object[] {machineName, StringUtils.isNotEmpty(unitName) ? unitName : " ", StringUtils.isNotEmpty(subUnitName) ? subUnitName : " ", alarmCode,"ISSUE"};
                String condition = " WHERE MACHINENAME = ? AND DECODE(UNITNAME,NULL,' ',UNITNAME) = ?  AND DECODE(SUBUNITNAME,NULL,' ',SUBUNITNAME) = ? AND ALARMCODE= ? AND ALARMSTATE = ?";
                alarmData = ExtendedObjectProxy.getAlarmService().select(condition, alarmBindMap);
                MESAlarmServiceProxy.getAlarmServiceImpl().createAlarm(alarmData.get(0).getAlarmCode(),alarmData.get(0).getAlarmSeverity(),"CLEAR",alarmData.get(0).getAlarmType(),alarmData.get(0).getDescription(),alarmData.get(0).getFactoryName(),alarmData.get(0).getMachineName(),alarmData.get(0).getUnitName(),alarmData.get(0).getSubUnitName(),alarmData.get(0).getProductList(),eventInfo);

			} catch (Exception e) {
				// TODO: handle exception
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
    }
}