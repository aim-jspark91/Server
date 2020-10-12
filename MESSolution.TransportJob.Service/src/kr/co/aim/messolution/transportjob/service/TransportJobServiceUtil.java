package kr.co.aim.messolution.transportjob.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ProductFlag;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class TransportJobServiceUtil implements ApplicationContextAware{
	/**
	 * @uml.property  name="applicationContext"
	 * @uml.associationEnd  
	 */
	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog(TransportJobServiceUtil.class);
	
	/**
	 * @param arg0
	 * @throws BeansException
	 * @uml.property  name="applicationContext"
	 */
	public void setApplicationContext(ApplicationContext arg0)
	throws BeansException 
	{
		applicationContext = arg0;
	}

	/*
	* Name : timeDelay
	* Desc : This function is timeDelay
	* Author : AIM Systems, Inc
	* Date : 2011.01.20
	*/
	public void timeDelay(long delay)
	{
		Timer timer = new Timer();
		timer.schedule(null, delay);	
	}

	/*
	* Name : getTransportJob
	* Desc : This function is getTransportJob
	* Author : AIM Systems, Inc
	* Date : 2011.01.20
	*/
	public String getTransportJob(String carrierName) throws CustomException
	{
		if(log.isInfoEnabled()){
			log.info("messageName = " + carrierName);
		}
		
		String timeKey =  ConvertUtil.getCurrTime();
		String transportJob = carrierName + "_" + timeKey + "_" + "MESSYS";
		return transportJob;
	}
	
	/*
	* Name : getJobState
	* Desc : This function is getJobState
	* Author : AIM Systems, Inc
	* Date : 2011.03.28
	*/
	public String getJobState( String messageName, Document document ) throws CustomException
	{
		String jobState = "";
		
		//RequestTransportJobRequest
		if(messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_REQUESTTRANSPORTJOBREQUEST))
		{
			jobState = GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Requested;
		} 
		//RequestTransportJobReply
		else if (messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_REQUESTTRANSPORTJOBREPLY)) 
		{
			String returnCode = document.getRootElement().getChild("Return").getChild("RETURNCODE").getText();
			
			if("0".equals(returnCode))
			{
				jobState = GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Accepted;
			} 
			else 
			{
				jobState = GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Rejected;
			}
			
		}
		//TransportJobStarted
		//TransportJobStartedByMCS
		//CarrierLocationChanged
		else if (messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_TRANSPORTJOBSTARTED) ||
			       messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_TRANSPORTJOBSTARTEDBYMCS) ||
				   messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_CARRIERLOACATIONCHANGED)) 
		{
			jobState = GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Started;
 		} 
		//TransportJobCompleted
		else if (messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_TRANSPORTJOBCOMPLETED)) 
		{
 			jobState = GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Completed;
		} 
		//TransportJobTerminatedByMCS
		else if (messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_TRANSPORTJOBTERMINATEDBYMCS)) 
		{
			jobState = GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Terminated;
		} 
		// Deleted by smkang on 2018.04.17 - When TEX server receives ChangeDestinationRequest, JobState is changed to Started.
		//									 But it is inappropriate.
//		else 
//		{
//			jobState = GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Started;
//		}
		
		return jobState;
	}
	
	/*
	* Name : getCancelState
	* Desc : This function is getCancelState
	* Author : AIM Systems, Inc
	* Date : 2011.03.28
	*/
	public String getCancelState(String messageName,Document document) throws CustomException
	{
		String cancelState = "";
		
		//CancelTransportJobRequest
		if(messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_CANCELTRANSPORTJOBREQUEST))
		{
			cancelState = GenericServiceProxy.getConstantMap().MCS_CANCELSTATE_Requested;
		} 
		//CancelTransportJobReply
		else if(messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_CANCELTRANSPORTJOBREPLY))
		{
			String returnCode = document.getRootElement().getChild("Return").getChild("RETURNCODE").getText();
			
			if("0".equals(returnCode))
			{
				cancelState = GenericServiceProxy.getConstantMap().MCS_CANCELSTATE_Accepted;
			}
			else 
			{
				cancelState = GenericServiceProxy.getConstantMap().MCS_CANCELSTATE_Rejected;
			}
		} 
		//TransportJobCancelStarted
		else if (messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_TRANSPORTJOBCANCELSTARTED)) 
		{
			cancelState = GenericServiceProxy.getConstantMap().MCS_CANCELSTATE_Started;
		} 
		//TransportJobCancelCompleted
		else if (messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_TRANSPORTJOBCANCELCOMPLETED)) 
		{
			cancelState = GenericServiceProxy.getConstantMap().MCS_CANCELSTATE_Completed;
		} 
		//TransportJobCancelFailed
		else if (messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_TRANSPORTJOBCANCELFAILED)) 
		{
			cancelState = GenericServiceProxy.getConstantMap().MCS_CANCELSTATE_Failed;
		}
		return cancelState;
	}
	
	/*
	* Name : getChangeState
	* Desc : This function is getChangeState
	* Author : AIM Systems, Inc
	* Date : 2011.03.28
	*/
	public String getChangeState(String messageName, Document document) throws CustomException
	{
		String changeState = "";
		
		//ChangeDestinationRequest
		if(messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_CHANGEDESTINATIONREQUEST))
		{
			changeState = GenericServiceProxy.getConstantMap().MCS_CHANGESTATE_Requested;
		} 
		//ChangeDestinationReply
		else if (messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_CHANGEDESTINATIONREPLY)) 
		{
			String returnCode = document.getRootElement().getChild("Return").getChild("RETURNCODE").getText();
			
			if("0".equals(returnCode))
			{
				changeState = GenericServiceProxy.getConstantMap().MCS_CHANGESTATE_Accepted;
			}
			else
			{
				changeState = GenericServiceProxy.getConstantMap().MCS_CHANGESTATE_Rejected;
			}
		} 
		//DestinationChanged
		else if (messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_DESTINATIONCHANGED)) 
		{
			changeState = GenericServiceProxy.getConstantMap().MCS_CHANGESTATE_Changed;
		} 
		//DestinationChangeFailed
		else if (messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_DESTINATIONCHANGEFAILED)) 
		{
			changeState = GenericServiceProxy.getConstantMap().MCS_CHANGESTATE_Failed;
		}
		return changeState;
	}
	
	/*
	* Name : generateTransportJobId
	* Desc : This function is getJobState
	* Author : AIM Systems, Inc
	* Date : 2011.03.28
	
	public String generateTransportJobId( String carrierName, String sender) throws CustomException
	{	
		String transportDate = ConvertUtil.getCurrTime(ConvertUtil.NONFORMAT_FULL);
		return (transportDate + "-" + carrierName + "-" + sender);
	}
	*/
	
	/*
	* Name : generateTransportJobId
	* Desc : This function is getJobState
	* Author : AIM Systems, Inc
	* Date : 2011.03.28
	*/
	public String generateTransportJobId(String carrierName, String sender) throws CustomException
	{	
		String transportDate = ConvertUtil.getCurrTime(ConvertUtil.NONFORMAT_FULL);
		return ("U" + transportDate + "-" + carrierName );
	}
	
	public String generateTransportJobIdBySender(String carrierName, String sender) throws CustomException
	{	
		String transportDate = ConvertUtil.getCurrTime(ConvertUtil.NONFORMAT_FULL);
		
		if(StringUtils.equals(sender, GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_OIC))
		{
			return ("U" + transportDate + "-" + carrierName );
		}
		else
		{
			return ("R" + transportDate + "-" + carrierName );
		}
	}

	/*
	* Name : generateTransportJobId
	* Desc : This function is generateTransportJobId
	* Author : AIM Systems, Inc
	* Date : 2011.01.20
	*/
	/*
	public String generateTransportJobId(String carrierName, String sender) {		
		String transportDate = ConvertUtil.getCurrTime(ConvertUtil.NONFORMAT_FULL);
		
		return (GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBNAME_H + transportDate + "-" + carrierName );
	}
	*/
	
	// Deleted by smkang on 2018.05.05 - There is no referenced.
	/*
	* Name : setCarrierLocationSequence
	* Desc : This function is set Carrier Location Sequence
	* Author : 
	* Date : 2011.04.08 
	*/
//	public void setCarrierLocationSequence(String machineName,String portName,String portType,String durableName) throws CustomException
//	{
//		try{
//			String subMachine=machineName.substring(0, 4);
//			if(subMachine.equals("AFBF")||subMachine.equals("FFBF"))
//			{
//				//new stocker,check port
//				if(!portType.equals("PORT")) return;
//				else
//				{
//					String sql="SELECT PORTTYPE FROM PORT ";
//					sql+="WHERE MACHINENAME=:machinename ";
//					sql+="AND PORTNAME=:portname ";
//					Map<String, String> bindMap = new HashMap<String, String>();
//					bindMap.put("machinename", machineName);
//					bindMap.put("portname", portName);
////					List<Map<String, Object>> sqlResult=greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
//					List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
//					if(sqlResult.size()>0)
//					{
//						String porttype=sqlResult.get(0).get("PORTTYPE").toString();
//						if(porttype.equals("PL"))
//						{
//							sql="SELECT DURABLENAME,DURABLESEQUENCE FROM DURABLE D ";
//							sql+="WHERE MACHINENAME=:machinename ";
//							sql+="AND PORTNAME=:portname ";
//							sql+="AND DURABLESEQUENCE IS NOT NULL ";
//							sql+="ORDER BY TO_NUMBER(DURABLESEQUENCE) DESC ";
//							bindMap.clear();
//							bindMap.put("machinename", machineName);
//							bindMap.put("portname", portName);
////							sqlResult=greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
//							sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
//							
//							//add by lyp 2013.08.17
//							//check CST is exist on port
//							for(int i=0;i<sqlResult.size();i++)
//							{
//								if(durableName.equals(sqlResult.get(i).get("DURABLENAME").toString()))
//									return;
//							}
//							
//							//end
//							//UPDATE DURABLE COUNT
//							String sequence="001";
//							if(sqlResult.size()>0)
//							{
//								try{
//									sequence=Integer.parseInt(sqlResult.get(0).get("DURABLESEQUENCE").toString())+1+"";
//									int len=3-sequence.length();
//									for(int j=0;j<len;j++)
//									{
//										sequence="0"+sequence;
//									}
//								}
//								catch(Exception e){
//									sequence="001";
//								}
//							}
//							sql="UPDATE DURABLE ";
//							sql+="SET DURABLESEQUENCE=:sequence ";
//							sql+="WHERE DURABLENAME=:durablename ";
//							bindMap.clear();
//							bindMap.put("sequence", sequence+"");
//							bindMap.put("durablename", durableName);
////							greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql, bindMap);
//							GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
//						}
//						else
//						{
//							//add by lyp,2013-7-29
//							//when port is not PL,sequence is null
//							String sequence="";
//							sql="UPDATE DURABLE ";
//							sql+="SET DURABLESEQUENCE=:sequence ";
//							sql+="WHERE DURABLENAME=:durablename ";
//							bindMap.clear();
//							bindMap.put("sequence", sequence);
//							bindMap.put("durablename", durableName);
////							greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql, bindMap);
//							GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
//						}
//					}
//				}//end else
//			}
//			else return;
//		}
//		catch(Exception e){
//			log.info(e);
//		}
//	}
	
	/*
	* Name : setCarrierLocationSequence
	* Desc : This function is set Carrier Location Sequence
	* Author : 
	* Date : 2011.04.08 
	*/
	public TransportJobCommand setTransportJobCommandEventInfo(TransportJobCommand transportJobCommand, EventInfo eventInfo) 
																throws CustomException
	{
		transportJobCommand.setLastEventName(eventInfo.getEventName());
		transportJobCommand.setLastEventUser(eventInfo.getEventUser());
		transportJobCommand.setLastEventTime(eventInfo.getEventTime());
		transportJobCommand.setLastEventTimeKey(eventInfo.getEventTimeKey());
		transportJobCommand.setLastEventComment(eventInfo.getEventComment());
		//transportJobCommand.setReasonCode(eventInfo.getReasonCode());
		//transportJobCommand.setReasonMessage(eventInfo.getReasonCodeType());
		
		return transportJobCommand;
	}
	
	/*
	* Name : checkExistTransportJobCommand
	* Desc : This function is check Exist TransportJobCommand
	* Author : 
	* Date : 2011.04.08 
	*/
	public List<TransportJobCommand> checkExistTransportJobCommand(
										List<TransportJobCommand> transportJobCommandList, String transportJobName) 
										throws CustomException
																
	{
		if(transportJobCommandList.size() == 0)
			throw new CustomException("JOB-9001", transportJobName);
		
		return transportJobCommandList;
	}
	
	// Deleted by smkang on 2018.05.05 - Move to TransportJobServiceImpl, because this method modifies database information.
	/*
	* Name : changeCarrierCurrentLocation
	* Desc : This function is change Carrier CurrentLocation
	* Author : 
	* Date : 2011.04.08 
	*/
//	public Durable changeCurrentCarrierLocation(Durable durableData, String currentMachineName, String currentPositionType, 
//												String currentPositionName, String currentZoneName, String transferState,
//												String transportLockFlag, EventInfo eventInfo) throws CustomException													
//	{
//		Machine machineData = new Machine();
//		try{
//			machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(currentMachineName);
//		}
//		catch(Exception e)
//		{}
//		
//		Map<String, String> durableUdfs = durableData.getUdfs();
//		durableUdfs.put("MACHINENAME", currentMachineName);
//		if(currentPositionType.equals(GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
//		{
//			durableUdfs.put("PORTNAME", currentPositionName);
//			durableUdfs.put("POSITIONNAME", "");
//		}
//		else
//		{
//			durableUdfs.put("PORTNAME", "");
//			durableUdfs.put("POSITIONNAME", currentPositionName);
//		}
//
//		durableUdfs.put("POSITIONTYPE", currentPositionType);
//		durableUdfs.put("ZONENAME", currentZoneName);
//		durableUdfs.put("TRANSPORTSTATE", transferState);
//		if(StringUtil.isNotEmpty(transportLockFlag))
//			durableUdfs.put("TRANSPORTLOCKFLAG", transportLockFlag);
//		
//		// SetArea Info 
//		
//		SetAreaInfo setAreaInfo = new SetAreaInfo();
//		setAreaInfo.setAreaName(machineData.getAreaName());
//		setAreaInfo.setUdfs(durableUdfs);
//		
//		durableData = DurableServiceProxy.getDurableService().setArea(durableData.getKey(), eventInfo, setAreaInfo);
//		
//		Durable durableData1=durableData;
//		//durableUdfs.put("MACHINENAME", currentMachineName);
//		durableData1.setFactoryName(machineData.getFactoryName());
//		DurableServiceProxy.getDurableService().update(durableData1);
//		
//		return durableData;
//	}
	
	// Deleted by smkang on 2018.05.05 - There is no referenced.
	/*
	* Name : changeCarrierCurrentLocation
	* Desc : This function is change Carrier CurrentLocation
	* Author : 
	* Date : 2011.04.08 
	*/
//	public TransportJobCommand changeDestination(TransportJobCommand transportJobCommandInfo, 
//												 String oldDestinationMachineName, String oldDestinationPositionType,
//												 String oldDestinationPositionName, String oldDestinationZoneName, 
//												 String newDestinationMachineName, String newDestinationPositionType, 
//												 String newDestinationPositionName, String newDestinationZoneName,
//												 String changeState, String priority, 
//												 String returnCode, String returnMessage,
//												 EventInfo eventInfo) 
//	throws CustomException													
//	{
//		if(StringUtil.equals(oldDestinationMachineName, newDestinationMachineName) &&
//				   StringUtil.equals(oldDestinationPositionType, newDestinationPositionType) &&
//				   StringUtil.equals(oldDestinationPositionName, newDestinationPositionName) && 
//				   StringUtil.equals(oldDestinationZoneName, newDestinationZoneName))
//		{
//		}
//		else // Case : Destination Changed
//		{	
//			transportJobCommandInfo.setDestinationMachineName(newDestinationMachineName);
//			transportJobCommandInfo.setDestinationPositionType(newDestinationPositionType);
//			transportJobCommandInfo.setDestinationPositionName(newDestinationPositionName);
//			transportJobCommandInfo.setDestinationZoneName(newDestinationZoneName);
//			transportJobCommandInfo.setChangeState(changeState);		
//			transportJobCommandInfo.setPriority(priority);
//			transportJobCommandInfo.setLastEventName(eventInfo.getEventName());
//			transportJobCommandInfo.setLastEventUser(eventInfo.getEventUser());
//			transportJobCommandInfo.setLastEventComment(eventInfo.getEventComment());
//			transportJobCommandInfo.setLastEventTime(eventInfo.getEventTime());
//			transportJobCommandInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
//			//transportJobCommandInfo.setLastEventResultCode(returnCode);
//			//transportJobCommandInfo.setLastEventResultText(returnMessage);
//			
//			try
//			{
//				ExtendedObjectProxy.getTransportJobCommand().modify(eventInfo, transportJobCommandInfo);
//			}
//			catch(Exception e)
//			{
//				throw new CustomException("JOB-8012", e.getMessage());
//			}
//		}
//				
//		return transportJobCommandInfo;
//	}
	
	// Deleted by smkang on 2018.05.05 - Move to TransportJobServiceImpl, because this method modifies database information.
	/*
	* Name : changeCarrierCurrentLocation
	* Desc : This function is change Carrier CurrentLocation
	* Author : 
	* Date : 2011.04.08 
	*/
//	public TransportJobCommand updateTransportJobCommand(String transportJobName, Document doc, EventInfo eventInfo) throws CustomException													
//	{
//		List<TransportJobCommand> sqlResult = ExtendedObjectProxy.getTransportJobCommand().select("TRANSPORTJOBNAME = ?", new Object[] {transportJobName});
//		MESTransportServiceProxy.getTransportJobServiceUtil().checkExistTransportJobCommand(sqlResult, transportJobName);
//		TransportJobCommand transportJobCommandInfo = sqlResult.get(0);
//		
//		String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);
//		String jobState = TransportJobServiceUtil.getJobState(messageName, doc);	
//		String cancelState = TransportJobServiceUtil.getCancelState(messageName, doc);
//		String changeState = TransportJobServiceUtil.getChangeState(messageName, doc);
//		
//		transportJobCommandInfo.setJobState(jobState);
//		transportJobCommandInfo.setCancelState(cancelState);
//		transportJobCommandInfo.setChangeState(changeState);
//		
//		// Set REASONCODE
//		transportJobCommandInfo.setReasonCode(SMessageUtil.getBodyItemValue(doc, "REASONCODE", false));
//		transportJobCommandInfo.setReasonMessage(SMessageUtil.getBodyItemValue(doc, "REASONCOMMENT", false));
//		
//		// Set LASTRESULTCODE
//		transportJobCommandInfo.setLastEventResultCode(SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false));
//		transportJobCommandInfo.setLastEventResultText(SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false));
//		
//		// Set EventInfo
//		transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceUtil().setTransportJobCommandEventInfo(transportJobCommandInfo, eventInfo);
//			
//		Element bodyElement = doc.getDocument().getRootElement().getChild("Body");
//		for ( Iterator iterator = bodyElement.getChildren().iterator(); iterator.hasNext(); )
//		{
//			Element bodyItem = (Element) iterator.next();
//			String itemName = bodyItem.getName().toString();
//			String itemValue = bodyElement.getChildText(itemName);
//			
//			if(itemName.equals("NEWDESTINATIONMACHINENAME"))
//				transportJobCommandInfo.setDestinationMachineName(itemValue);
//			
//			if(itemName.equals("NEWDESTINATIONPOSITIONTYPE"))
//				transportJobCommandInfo.setDestinationPositionType(itemValue);
//			
//			if(itemName.equals("NEWDESTINATIONPOSITIONNAME"))
//				transportJobCommandInfo.setDestinationPositionName(itemValue);
//			
//			if(itemName.equals("NEWDESTINATIONZONENAME"))
//				transportJobCommandInfo.setDestinationZoneName(itemValue);	
//			
//			if(itemName.equals("PRIORITY"))
//				transportJobCommandInfo.setPriority(itemValue);
//			
//			if(itemName.equals("CURRENTMACHINENAME"))
//				transportJobCommandInfo.setCurrentMachineName(itemValue);
//			
//			if(itemName.equals("CURRENTPOSITIONTYPE"))
//				transportJobCommandInfo.setCurrentPositionType(itemValue);
//			
//			if(itemName.equals("CURRENTPOSITIONNAME"))
//				transportJobCommandInfo.setCurrentPositionName(itemValue);
//			
//			if(itemName.equals("CURRENTZONENAME"))
//				transportJobCommandInfo.setCurrentZoneName(itemValue);
//			
//			if(itemName.equals("TRANSFERSTATE"))
//				transportJobCommandInfo.setTransferState(itemValue);
//			
//			if(itemName.equals("CARRIERSTATE"))
//				transportJobCommandInfo.setCarrierState(itemValue);
//			
//			if(itemName.equals("ALTERNATEFLAG"))
//				transportJobCommandInfo.setAlternateFlag(itemValue);
//		}
//		
//		try
//		{
//			ExtendedObjectProxy.getTransportJobCommand().modify(eventInfo, transportJobCommandInfo);
//		}
//		catch(Exception e)
//		{
//			throw new CustomException("JOB-8012", e.getMessage());
//		}
//		
//		return transportJobCommandInfo;
//	}
	
	//UNKNOWNEMP-
	/*
	* Name : unknownCarrierChangeName
	* Desc : This function is check Exist TransportJobCommand
	* Author : 
	* Date : 2011.04.08 
	*/
	public String unknownCarrierChangeName(String carrierName) throws CustomException															
	{
		if(carrierName.indexOf("-") > 0)
		{
			String firstCut = carrierName.substring(carrierName.indexOf("-")+1);
			
			if(firstCut.indexOf("-") > 0)
			{
				String secondCut = firstCut.substring(0, firstCut.lastIndexOf("-"));
				carrierName = secondCut;
			}
		}
		
		return carrierName;
	}
	
	/*
	* Name : getTransportJobInfo
	* Desc : getTransportJobInfo
	* Author : 
	* Date : 2011.04.08 
	*/
	public TransportJobCommand getTransportJobInfo(String transportJobName) 
	throws CustomException													
	{
		TransportJobCommand transportJobCommandInfo = 
			ExtendedObjectProxy.getTransportJobCommandService().selectByKey(true, new Object[] {transportJobName});
				
		return transportJobCommandInfo;
	}
	
	/*
	* Name : getReserveProductSpecData
	* Desc : getReserveProductSpecData
	* Author : hykim
	* Date : 2015.02.14 
	*/
	public List<Map<String, Object>> getReserveProductSpecData(
			String machineName, String processOperationGroupName, String processOperationName, String productSpecName) throws CustomException													
	{
		String sql = "" +
		 " SELECT MACHINENAME, PROCESSOPERATIONGROUPNAME, PROCESSOPERATIONNAME, PRODUCTSPECNAME, POSITION, " +
				  " RESERVESTATE, RESERVEDQUANTITY, COMPLETEQUANTITY " +
		 " FROM CT_RESERVEPRODUCT " +
		 " WHERE MACHINENAME = :machineName " +
		 " AND PROCESSOPERATIONGROUPNAME = :processOperationGroupName " +
		 " AND PROCESSOPERATIONNAME = :processOperationName " +
		 " AND PRODUCTSPECNAME = :productSpecName ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("machineName", machineName);
		bindMap.put("processOperationGroupName", processOperationGroupName);
		bindMap.put("processOperationName", processOperationName);
		bindMap.put("productSpecName", productSpecName);
		
//		List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return sqlResult;
	}
	
	/*
	* Name : getReserveProductSpecList
	* Desc : getReserveProductSpecList
	* Author : hykim
	* Date : 2015.02.14 
	*/
	public List<Map<String, Object>> getReserveProductSpecList(
			String productSpecName, String processOperationName, String machineName) throws CustomException													
	{
		String sql = "" +
		 " SELECT ROWNUM-1 SEQ, MACHINENAME, PROCESSOPERATIONGROUPNAME, PROCESSOPERATIONNAME, PRODUCTSPECNAME, POSITION, " +
		 "        RESERVESTATE, RESERVEDQUANTITY, COMPLETEQUANTITY " +
		 "  FROM CT_RESERVEPRODUCT " +
		 " WHERE PRODUCTSPECNAME = :PRODUCTSPECNAME " +
		 " AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME " +
		 " AND MACHINENAME = :MACHINENAME " +
		 " ORDER BY POSITION ASC ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("PRODUCTSPECNAME", productSpecName);
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		bindMap.put("MACHINENAME", machineName);
		
//		List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return sqlResult;
	}
	
	/**
	 * @author smkang
	 * @since 2018.05.02
	 * @param machineName
	 * @param positionType
	 * @return transportState
	 * @see Because MCS doesn't report TRANSFERSTATE, MES should decide and update by current position of a carrier.
	 */
	public String judgeTransportState(String machineName, String positionType) {
		// Commented by smkang on 2018.04.08 - If a machine name of carrier is null, TRANSPORTSTATE would be changed to OUTSTK.
		if (StringUtils.isEmpty(machineName)) {
			return GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK;
		} else {
			if (GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_SHELF.equals(positionType)) {
				return GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INSTK;
			} else if (GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_CRANE.equals(positionType)) {
				return GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_MOVING;
			} else if (GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT.equals(positionType)) {
				// Commented by smkang on 2018.04.08 - When a carrier is on a port of a machine, we should check MES can distinguish this port is a stocker's port or a process machine's port according to received data.					
//					if (stocker's port)
//						return GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_MOVING;
//					else if (process machine's port)
//						return GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP;
				
				// Added by smkang on 2018.05.02 - MCS doesn't report a carrier location at a process machine. DAIFUKU and AIM discussed about this on 2018.04.19.
				// Modified by smkang on 2018.05.08 - Need to distinguish TransferState by machine type.
//				return GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_MOVING;
				try {
					MachineSpec machineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(machineName));
					
					if (machineSpecData.getMachineType().equals(GenericServiceProxy.getConstantMap().Mac_ProductionMachine))
						return GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP;
					else
						return GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_MOVING;
				} catch (Exception e) {
					// TODO: handle exception
					return GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK;
				}
			} else {
				return "";
			}
		}
	}
	
	/**
	 * @author smkang
	 * @since 2018.05.08
	 * @param machineSpecData
	 * @param positionType
	 * @return transportState
	 * @see Because MCS doesn't report TRANSFERSTATE, MES should decide and update by current position of a carrier.
	 */
	public String judgeTransportState(MachineSpec machineSpecData, String positionType) {
		// Commented by smkang on 2018.05.08 - If a machine of carrier is not existed, TRANSPORTSTATE would be changed to OUTSTK.
		if (machineSpecData == null) {
			return GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK;
		} else {
			if (GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_SHELF.equals(positionType)) {
				return GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INSTK;
			} else if (GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_CRANE.equals(positionType)) {
				return GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_MOVING;
			} else if (GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT.equals(positionType)) {
				// Commented by smkang on 2018.04.08 - When a carrier is on a port of a machine, we should check MES can distinguish this port is a stocker's port or a process machine's port according to received data.					
//						if (stocker's port)
//							return GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_MOVING;
//						else if (process machine's port)
//							return GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP;
				
				// Added by smkang on 2018.05.02 - MCS doesn't report a carrier location at a process machine. DAIFUKU and AIM discussed about this on 2018.04.19.
				// Modified by smkang on 2018.05.08 - Need to distinguish TransferState by machine type.
//					return GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_MOVING;
				if (machineSpecData.getMachineType().equals(GenericServiceProxy.getConstantMap().Mac_ProductionMachine))
					return GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP;
				else
					return GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_MOVING;
			} else {
				return "";
			}
		}
	}
	
	/**
	 * @author smkang
	 * @since 2018.05.06
	 * @param transportJobName
	 * @return transportJobType
	 * @see Judge TransportJobType according to the prefix of sTransportJobName.
	 */
	public String judgeTransportJobTypeByTransportJobName(String transportJobName) {
		if (StringUtils.isNotEmpty(transportJobName)) {
			if (transportJobName.startsWith("R"))
				return GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_RTD;
			else if (transportJobName.startsWith("U"))
				return GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_OIC;
			// Modified by smkang on 2018.11.28 - N is also defined as MCS transport.
//			else if (transportJobName.startsWith("S") || transportJobName.startsWith("T") || transportJobName.startsWith("G"))
			else if (transportJobName.startsWith("S") || transportJobName.startsWith("T") || transportJobName.startsWith("G") || transportJobName.startsWith("N"))
				return GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_MCS;
			else if (transportJobName.startsWith("M"))
				//return GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_EQP;//20190805 BY YU.CUI
			    return GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_MCS; //20190805  BY YU.CUI
			else
				return "N/A";
		} else {
			return "N/A";
		}
	}
	
	/**
	 * @author smkang
	 * @since 2018.05.19
	 * @param exceptTransportJobNames
	 * @return List<TransportJobCommand>
	 * @see Returns transport jobs except exceptTransportJobNames.
	 */
	public List<TransportJobCommand> getTransportJobList(List<String> exceptTransportJobNames) {
		
		List<TransportJobCommand> transportJobDataList = null;
		
		try {
			if (exceptTransportJobNames != null && exceptTransportJobNames.size() > 0) {
				Object[] bindSet = new Object[exceptTransportJobNames.size()];
				
				for (int index = 0; index < exceptTransportJobNames.size(); index++) {
					bindSet[index] = exceptTransportJobNames.get(index);
				}
				
				String condition = "transportJobName not in (" + StringUtils.removeEnd(StringUtils.repeat("?,", exceptTransportJobNames.size()), ",") + ")";
				transportJobDataList = ExtendedObjectProxy.getTransportJobCommandService().select(condition, bindSet);
			} else {
				transportJobDataList = ExtendedObjectProxy.getTransportJobCommandService().select("", new Object[] {});
			}
		} catch (Exception e) {
			log.info(e);
		}
		
		return transportJobDataList;
	}
	
	/**
	 * @author smkang
	 * @since 2018.07.03
	 * @param document
	 * @param machineName
	 * @see If MES receives a message about shipping stocker, the message should be published to another shop.
	 *      GetInventoryZoneDataReply/InventoryZoneDataReport/GetCarrierDataReply/InventoryCarrierDataReport/CarrierLocationChanged.
	 */
	// Deleted by smkang on 2018.10.23 - EDO CIM team doesn't want to register CT_SHIPPINGSTOCKER.
//	public void publishMessageToShippingShop(Document document, String machineName) throws CustomException {
//		// Modified by smkang on 2018.10.10 - To avoid recursive publishing.
//		String eventUser = SMessageUtil.getHeaderItemValue(document, "EVENTUSER", false);
//		if (!System.getProperty("svr").equals(eventUser)) {
//			String sql = "SELECT DISTINCT FACTORYNAME" +
//					 	 "  FROM CT_SHIPPINGSTOCKER" +
//					 	 " WHERE MACHINENAME = :MACHINENAME";
//	
//			Map<String, String> bindMap = new HashMap<String, String>();
//			bindMap.put("MACHINENAME", machineName);
//					
//			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
//			
//			if (sqlResult != null && sqlResult.size() > 0) {
//				for (Map<String, Object> map : sqlResult) {
//					// Added by smkang on 2018.10.10 - To avoid recursive publishing.
//					SMessageUtil.setHeaderItemValue(document, "EVENTUSER", System.getProperty("svr"));
//					
//					if (StringUtils.equals(map.get("FACTORYNAME").toString(), "OLED"))
//						GenericServiceProxy.getESBServive().sendBySender(document, "OledTEMSender");
//					else if (StringUtils.equals(map.get("FACTORYNAME").toString(), "MOD"))
//						GenericServiceProxy.getESBServive().sendBySender(document, "ModTEMSender");
//				}
//			}
//		}
//	}
	
	// Added by smkang on 2018.10.18 - According to EDO's request, carrier data should be synchronized with shared factory.
	public void publishMessageToSharedShop (Document document, String carrierName) {
		if (StringUtils.isNotEmpty(carrierName)) {
			try {
				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
				
				// Modified by smkang on 2018.10.18 - To avoid recursive publishing.
//				if (durableData != null) {
				String eventUser = SMessageUtil.getHeaderItemValue(document, "EVENTUSER", false);

				if (durableData != null && !System.getProperty("svr").equals(eventUser)) {
					// Modified by smkang on 2018.10.18 - According to EDO's request, common carrier can be created in shared factory.
					//									  But a user doesn't want to register DurableSpec in shared factory.
//					DurableSpecKey durableSpecKey = new DurableSpecKey();
//					durableSpecKey.setFactoryName(durableData.getFactoryName());
//					durableSpecKey.setDurableSpecName(durableData.getDurableSpecName());
//					durableSpecKey.setDurableSpecVersion(durableData.getDurableSpecVersion());
//						
//					DurableSpec durableSpecData = DurableServiceProxy.getDurableSpecService().selectByKey(durableSpecKey);
//						
//					String sharedFactoryName = durableSpecData.getUdfs().get("SHAREDFACTORY");					
					String sharedFactoryName = "";
					try {
						List<DurableSpec> durableSpecDataList = DurableServiceProxy.getDurableSpecService().select("DURABLESPECNAME = ?", new Object[] {durableData.getDurableSpecName()});
						
						if (durableSpecDataList != null && durableSpecDataList.size() > 0)
							sharedFactoryName = durableSpecDataList.get(0).getUdfs().get("SHAREDFACTORY");
						else
							sharedFactoryName = durableData.getUdfs().get("OWNER");
					} catch (Exception e) {
						// TODO: handle exception
						sharedFactoryName = durableData.getUdfs().get("OWNER");
					}
					
					// Modified by smkang on 2018.10.18 - According to EDO's request, common carrier can be created in shared factory.
					//									  But a user doesn't want to register DurableSpec in shared factory.
//						if (StringUtils.isNotEmpty(sharedFactoryName)) {
					if (StringUtils.isNotEmpty(sharedFactoryName) && !System.getProperty("shop").equals(sharedFactoryName)) {
						// Added by smkang on 2018.10.18 - To avoid recursive publishing.
						SMessageUtil.setHeaderItemValue(document, "EVENTUSER", System.getProperty("svr"));
						
						if (StringUtils.equals(sharedFactoryName, "OLED"))
							GenericServiceProxy.getESBServive().sendBySender(document, "OledTEMSender");
						
						SMessageUtil.setHeaderItemValue(document, "EVENTUSER", eventUser);
					}
				}
			} catch (Exception e) {
				log.warn(e);
			}
		}
	}
	
	// Added by smkang on 2018.10.23 - According to EDO's request, inventory data and machine control state should be synchronized with shared factory without CT_SHIPPINGSTOCKER.
	public void publishMessageToSharedShop (Document document) {
		try {
			String eventUser = SMessageUtil.getHeaderItemValue(document, "EVENTUSER", false);
			
			if (!System.getProperty("svr").equals(eventUser)) {
				// To avoid recursive publishing.
				SMessageUtil.setHeaderItemValue(document, "EVENTUSER", System.getProperty("svr"));

				GenericServiceProxy.getESBServive().sendBySender(document, "OledTEMSender");
				
				SMessageUtil.setHeaderItemValue(document, "EVENTUSER", eventUser);
			}
		} catch (Exception e) {
			// TODO: handle exception
			log.warn(e);
		}
	}
	
	/**
	 * @author smkang
	 * @since 2019.07.05
	 * @see
	 */
	/**
	 * @author smkang
	 * @since 2019.07.05
	 * @param durableData
	 * @param lotData
	 * @param destMachineSpecData
	 * @param destMachineData
	 * @param destPortData
	 * @param eventInfo
	 * @throws CustomException
	 * @see This method is moved from RequestTransportJobRequest because this is necessary to be called in ChangeDestinationRequest.
	 */
	public void validateCassetteInfoDownloadRequest(Durable durableData, Lot lotData, MachineSpec destMachineSpecData, Machine destMachineData, Port destPortData, EventInfo eventInfo) throws CustomException {
		// Added by smkang on 2019.01.17 - According to Liu Hongwei's request, when a machine is INDP mode and linked unit is offline, validation will be ignored.
		if (!StringUtils.equals(destMachineData.getUdfs().get("OPERATIONMODE"), GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP) ||
			!MESMachineServiceProxy.getMachineServiceUtil().getMachineData(destPortData.getUdfs().get("LINKEDUNITNAME")).getCommunicationState().equals(GenericServiceProxy.getConstantMap().Mac_OffLine)) {
			
			String carrierName = durableData.getKey().getDurableName();
			String destinationMachineName = destMachineData.getKey().getMachineName();
			
			// Added by smkang on 2018.05.24 - If destination is not a process machine, although this carrier is hold, transport should be enabled.
			//								   So this validation should be executed after find a MachineSpec of destination.
			CommonValidation.CheckDurableHoldState(carrierName);
			CommonValidation.checkMachineState(destMachineData);
			CommonValidation.checkMachineOperationModeExistence(destMachineData);
			
			if (CommonUtil.getValue(destMachineSpecData.getUdfs(), "CONSTRUCTTYPE").equals(GenericServiceProxy.getConstantMap().ConstructType_CassetteClenaer)) {
				CommonValidation.checkEmptyCst(durableData.getKey().getDurableName());
				
				if(StringUtil.equals(durableData.getDurableCleanState(), GenericServiceProxy.getConstantMap().Dur_Clean))
	                throw new CustomException("CST-0041", durableData.getKey().getDurableName());
			} else if (CommonUtil.getValue(destMachineSpecData.getUdfs(), "CONSTRUCTTYPE").equals(GenericServiceProxy.getConstantMap().ConstructType_UNPK)) {
				CommonValidation.checkEmptyCst(carrierName);
				
				if (lotData != null)
                    throw new CustomException("CST-0006", carrierName);
				//start modify by jhiying on20200103 mantis:5512 
				if(!StringUtils.equals(destMachineData.getUdfs().get("OPERATIONMODE"), GenericServiceProxy.getConstantMap().OPERATIONMODE_SORTING) && 
						CommonUtil.getValue(destPortData.getUdfs(), "PORTTYPE").equals("PU"))
				{
					String strSql = "SELECT SJC.CARRIERNAME DURABLENAME "
							+ "FROM CT_SORTJOB SJ, CT_SORTJOBCARRIER SJC, DURABLE D "
							+ "WHERE SJ.JOBNAME = SJC.JOBNAME "
							+ "AND SJC.CARRIERNAME = D.DURABLENAME "
							+ "AND D.FACTORYNAME = :FACTORYNAME "
							+ "AND D.DURABLENAME = :DURABLENAME "
							+ "AND UPPER(SJ.JOBSTATE) NOT IN ('ENDED','CANCELED','ABORT')" ;
					
					Map<String, Object> bindMap = new HashMap<String, Object>();
					bindMap.put("FACTORYNAME", durableData.getFactoryName());
					bindMap.put("DURABLENAME", carrierName);
					
					List<Map<String, Object>> carrierInSortJob = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
					
					if(carrierInSortJob.size() > 0)
					{
						throw new CustomException("CST-2020", carrierName);
					}
				}
				
				//end modify by jhiying on20200103 mantis:5512 
			} else {
				// Modified by smkang on 2019.01.28 - According to Cui Yu's request, DryFlag doesn't be checked for CassetteCleaner or Stocker.
				if(StringUtils.equals(durableData.getUdfs().get("DRYFLAG"), "N"))
					throw new CustomException("TRANSPORT-0007", carrierName);
				
				if (CommonUtil.getValue(destPortData.getUdfs(), "PORTTYPE").equals("PB") ||
					CommonUtil.getValue(destPortData.getUdfs(), "PORTTYPE").equals("PL") ||
					CommonUtil.getValue(destPortData.getUdfs(), "PORTTYPE").equals("PO")) {
					
					if (lotData != null) {
						if(StringUtil.equals(lotData.getLotGrade(), GenericServiceProxy.getConstantMap().LotGrade_S))
		                    throw new CustomException("LOT-9043", carrierName, lotData.getKey().getLotName());
						
						ProcessOperationSpec machineOperationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
		                String operationMachineGroup = machineOperationData.getUdfs().get("MACHINEGROUPNAME");
		                MESMachineServiceProxy.getMachineServiceUtil().getMachineOperationGroup(destMachineData.getKey().getMachineName(), operationMachineGroup);
		                
						CommonValidation.checkLotState(lotData);
		                CommonValidation.checkLotProcessState(lotData);
		                CommonValidation.checkLotHoldState(lotData);
		                
		                List<Product> productDataList = null;
		                try {
		                    productDataList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
		                } catch (Exception ex) {
		                    throw new CustomException("SYS-9999", "Product", "No Product to process");
		                }
		                
		                ProcessFlow processFlowData = null;
		                try {
		                    ProcessFlowKey processFlowKey = new ProcessFlowKey();
		                    processFlowKey.setFactoryName(lotData.getFactoryName());
		                    processFlowKey.setProcessFlowName(lotData.getProcessFlowName());
		                    processFlowKey.setProcessFlowVersion(lotData.getProcessFlowVersion());
		                    processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
		                } catch (Exception ex) {
		                    throw new CustomException("SYS-9999", "Product", "No ProcessFlowData to process");
		                }
		                
		                MESProductServiceProxy.getProductServiceImpl().checkQTime(lotData.getKey().getLotName());
		                MESProductServiceProxy.getProductServiceImpl().checkMQCLot(lotData.getKey().getLotName());
		                
		                if(StringUtils.equals(processFlowData.getProcessFlowType(), "MQC") && StringUtil.isEmpty(MESProductServiceProxy.getProductServiceImpl().checkMQCMachine(lotData, destinationMachineName)))
		                	throw new CustomException("MQC-0049", destinationMachineName, StringUtil.EMPTY);
		             
		                MESLotServiceProxy.getLotServiceUtil().checkOperationModeByCT_OperationMode(destinationMachineName, CommonUtil.getValue(destMachineData.getUdfs(), "OPERATIONMODE"));
		                
		                String toMachineName = "";
		                //start MODIFY BY JHIYING ON20200117 MANTIS:5625 Because ADD getINDPMachineRecipe logic  
		                List<Lot> lotDataList = new ArrayList<Lot>();
                    	lotDataList.add(lotData);
                   
                    	String machineRecipeName = StringUtil.EMPTY;
		                if(StringUtil.equals(CommonUtil.getValue(destMachineData.getUdfs(), "OPERATIONMODE"),GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP)) {
		                    if(StringUtil.isEmpty(CommonUtil.getValue(destPortData.getUdfs(), "LINKEDUNITNAME")))
		                        throw new CustomException("PORT-9006", destinationMachineName);
		                    else
		                    	toMachineName = CommonUtil.getValue(destPortData.getUdfs(), "LINKEDUNITNAME");
		                 
		                    MESLotServiceProxy.getLotServiceUtil().getMachineNameAndOperationNameByTOPolicy(lotDataList, destinationMachineName, toMachineName, CommonUtil.getValue(destMachineData.getUdfs(), "OPERATIONMODE"));
		                	/*String machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(),
    								lotData.getProcessFlowName(), lotData.getProcessOperationName(), destinationMachineName, lotData.getUdfs().get("ECCODE"));*/
		                     machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getINDPMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(),lotData.getProcessFlowName(), 
		                    		lotData.getProcessOperationName(), destinationMachineName, lotData.getUdfs().get("ECCODE"),destPortData.getKey().getPortName());
		                }
		                else
		                {
		                	/*     List<Lot> lotDataList = new ArrayList<Lot>();
	                    	lotDataList.add(lotData);*/

	                    	MESLotServiceProxy.getLotServiceUtil().getMachineNameAndOperationNameByTOPolicy(lotDataList, destinationMachineName, toMachineName, CommonUtil.getValue(destMachineData.getUdfs(), "OPERATIONMODE"));
	                    	 machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(),
    								lotData.getProcessFlowName(), lotData.getProcessOperationName(), destinationMachineName, lotData.getUdfs().get("ECCODE"));
		                 }
                    	//ENDED MODIFY BY JHIYING ON20200117 MANTIS:5625 Because ADD getINDPMachineRecipe logic
                    	
                    	if(StringUtils.equals(processFlowData.getProcessFlowType(), "MQC")) {
                    		for (Product productData : productDataList) {
                                String mqcProductRecipeName = MESProductServiceProxy.getProductServiceImpl().slotMQCProduct(productData);
                                
                                if(StringUtil.isNotEmpty(mqcProductRecipeName)) {
                                    machineRecipeName = mqcProductRecipeName;
                                    break;
                                }
                            }
                        }
                    	
                    	// Deleted by smkang on 2019.02.22 - According to Liu Hongwei and Cui Yu's request, RecipeIdleTime is unnecessary to be checked at manual transport time.
//                    	MESLotServiceProxy.getLotServiceUtil().checkFirstLotFlagByRecipeIdleTime(destinationMachineName, machineRecipeName);
                    	MESLotServiceProxy.getLotServiceImpl().checkInhibitCondition(eventInfo, lotData, destinationMachineName, machineRecipeName, GenericServiceProxy.getConstantMap().Flag_N, toMachineName);
                    	
                    	if (CommonUtil.getValue(destMachineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("ATST")) {
                            if (StringUtil.equals(CommonUtil.getValue(destMachineData.getUdfs(), "OPERATIONMODE"), GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP)) {
                                List<String> unitList = MESDurableServiceProxy.getDurableServiceUtil().getUnitListByMahcineName(destinationMachineName);
            
                                for(String unit : unitList) {
                                	
                                	//2019.08.15 cy
                                	log.info("UnitName : ["+unit+"], Linked Unit Name ["+destPortData.getUdfs().get("LINKEDUNITNAME")+"]");
                                	if(StringUtil.equals(unit, destPortData.getUdfs().get("LINKEDUNITNAME")))
                                	{
                                		String probeCardType = MESDurableServiceProxy.getDurableServiceUtil().getPBListTypeByUnitName(unit);
                                        
                                        PolicyUtil.checkMachineProbeCardTypeByTPEFOMPolicy(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), destinationMachineName, CommonUtil.getValue(lotData.getUdfs(), "ECCODE"), machineRecipeName, probeCardType);
                                	}
                                	
                                }
                            } else {
                            	String probeCardType = MESDurableServiceProxy.getDurableServiceUtil().getPBListTypeByMachine(destinationMachineName);

                                PolicyUtil.checkMachineProbeCardTypeByTPEFOMPolicy(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), destinationMachineName, CommonUtil.getValue(lotData.getUdfs(), "ECCODE"), machineRecipeName, probeCardType);
                            }
                    	} else if (CommonUtil.getValue(destMachineSpecData.getUdfs(), "CONSTRUCTTYPE").equals(GenericServiceProxy.getConstantMap().ConstructType_PHOTO)) {
                    		List<Durable> photoMaskDataList = MESDurableServiceProxy.getDurableServiceUtil().getPhotoMaskNameByMachineName(destinationMachineName);
                    	    
                            if (photoMaskDataList != null && photoMaskDataList.size() > 0) {
                                /* 20190514, hhlee, add validation, PhotoMaskName RecipeParaMeter(Mask Name) Validation */
                                //PolicyUtil.checkMachinePhotoMaskByTPEFOMPolicy(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), destinationMachineName, CommonUtil.getValue(lotData.getUdfs(), "ECCODE"), machineRecipeName, photoMaskDataList);
                                String photoMaskName = PolicyUtil.checkMachinePhotoMaskByTPEFOMPolicy(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), destinationMachineName, CommonUtil.getValue(lotData.getUdfs(), "ECCODE"), machineRecipeName, photoMaskDataList);
                    			
                                /* 20190514, hhlee, add validation, PhotoMaskName RecipeParaMeter(Mask Name) Validation */
                                if(!StringUtils.equals(photoMaskName, GenericServiceProxy.getConstantMap().VIRTUAL_PHOTOMASKNAME))
                                {
                                    if(StringUtil.equals(destMachineSpecData.getUdfs().get("RMSFLAG").toString(), GenericServiceProxy.getConstantMap().FLAG_Y))
                                    {
                                        MESRecipeServiceProxy.getRecipeServiceUtil().validateRecipeParameterPhotoMaskName(eventInfo, destinationMachineName, machineRecipeName, photoMaskName);
                                    }
                                }
                                
                                for(Product productData : productDataList) {
                    				ProductFlag productFlagData = null;
                                    try {
                            			productFlagData = ExtendedObjectProxy.getProductFlagService().selectByKey(false, new Object[] {productData.getKey().getProductName()});
                                    } catch(Throwable e) {
                                    	log.error("Product [" + productData.getKey().getProductName() + " [ProductFlag is not exist");
                                    }
                                    
                                    if (productFlagData != null) {
                                    	if(StringUtils.equals(productFlagData.getTurnOverFlag(), GenericServiceProxy.getConstantMap().Flag_Y))
                                            throw new CustomException("PRODUCT-0034", productData.getKey().getProductName());
                                    	
                                    	if(StringUtils.equals(productFlagData.getProcessTurnFlag(), GenericServiceProxy.getConstantMap().Flag_Y))
                                            throw new CustomException("PRODUCT-0035", productData.getKey().getProductName());
                                    }
                    			}
                            } else {
                                throw new CustomException("MASK-0098", destinationMachineName);
                            }
                    	} else {
                    		for(Product productData : productDataList) {
                				ProductFlag productFlagData = null;
                                try {
                        			productFlagData = ExtendedObjectProxy.getProductFlagService().selectByKey(false, new Object[] {productData.getKey().getProductName()});
                                } catch(Throwable e) {
                                	log.error("Product [" + productData.getKey().getProductName() + " [ProductFlag is not exist");
                                }
                                
                                if (productFlagData != null) {
                    				if (StringUtil.equals(productFlagData.getTrackFlag(), GenericServiceProxy.getConstantMap().Flag_Y)) {
                    					if (!StringUtil.equals(destMachineSpecData.getUdfs().get("TRACKFLAG"), GenericServiceProxy.getConstantMap().PHOTO_DRYF) &&
                    						!StringUtil.equals(destMachineSpecData.getUdfs().get("TRACKFLAG"), GenericServiceProxy.getConstantMap().PHOTO_STRP)) {
                    						
                    						throw new CustomException("MACHINE-1001", destMachineData.getKey().getMachineName());
                                        }
                    					
                    					break;
                    				}
                                }
                			}
                    	}
                    	
                    	if (ExtendedObjectProxy.getProductFlagService().checkProductFlagElaQtimeByProduct(productDataList)) {
                            if(!CommonUtil.getValue(destMachineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("ELA"))
                            	throw new CustomException("PRODUCT-9004", lotData.getKey().getLotName());
                        }
                    	
                    	//2018.11.20_hsryu_add checkMachineIdleTime.
						MESMachineServiceProxy.getMachineServiceUtil().checkMachineIdleTimeOver(destinationMachineName, lotData, destPortData.getKey().getPortName());
					} else {
						throw new CustomException("LOT-0054", carrierName);
					}
				} else if (CommonUtil.getValue(destPortData.getUdfs(), "PORTTYPE").equals("PS")) {
					try {
						StringBuffer sqlBuffer = new StringBuffer();
						sqlBuffer.append("SELECT J.JOBNAME").append("\n")
								 .append("  FROM CT_SORTJOB J, CT_SORTJOBCARRIER C ").append("\n")
                                 .append(" WHERE J.JOBNAME = C.JOBNAME").append("\n")
                                 .append("   AND C.MACHINENAME = ?").append("\n")
                                 .append("   AND C.CARRIERNAME = ?").append("\n")
                                 .append("   AND C.PORTNAME = ?").append("\n")
                                 .append("   AND J.JOBSTATE IN (?, ?)");
						
						List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), new Object[] {destinationMachineName, carrierName, destPortData.getKey().getPortName(), "WAIT", "CONFIRMED"});
						
						if (result == null || result.size() < 1)
							throw new CustomException("SYS-9999", "SortJob", "CST is not enable to Sorter job");
					} catch (Exception e) {
						throw new CustomException("SYS-9999", "SortJob", "CST is not enable to Sorter job");
					}
					
					if (lotData != null) {
						CommonValidation.checkLotState(lotData);
                        CommonValidation.checkLotProcessState(lotData);
                        CommonValidation.checkLotHoldState(lotData);
    
                        MESProductServiceProxy.getProductServiceImpl().checkMQCLot(lotData.getKey().getLotName());
                        
                        ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
                        if (!StringUtil.equals(operationData.getProcessOperationType(), GenericServiceProxy.getConstantMap().Pos_Sorter))
                            throw new CustomException("LOT-9041", lotData.getProcessOperationName(), lotData.getKey().getLotName(), destinationMachineName);
                        
                        MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(),
                        		lotData.getProcessFlowName(), lotData.getProcessOperationName(), destinationMachineName, lotData.getUdfs().get("ECCODE"));
					}
				} else {
					CommonValidation.checkEmptyCst(carrierName);
				}
					
			}
		}
	}
}