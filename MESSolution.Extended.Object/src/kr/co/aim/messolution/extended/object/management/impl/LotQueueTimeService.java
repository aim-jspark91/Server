package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.LotQueueTime;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LotQueueTimeService extends CTORMService<LotQueueTime> {

	public static Log logger = LogFactory.getLog(LotQueueTimeService.class);
	
	private final String historyEntity = "LotQueueTimeHist";
	
	public List<LotQueueTime> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<LotQueueTime> result = super.select(condition, bindSet, LotQueueTime.class);
		
		return result;
	}
	
	public LotQueueTime selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(LotQueueTime.class, isLock, keySet);
	}
	
	public LotQueueTime create(EventInfo eventInfo, LotQueueTime dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, LotQueueTime dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public LotQueueTime modify(EventInfo eventInfo, LotQueueTime dataInfo)
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	/**
	 * check whether Lot is in Q-time or not
	 * @author swcho
	 * @since 2014.06.08
	 * @param lotName
	 * @return
	 */
	public boolean isInQTime(String lotName)
	{
		boolean result = false;
		
		try
		{
			List<LotQueueTime> resultList = ExtendedObjectProxy.getQTimeService().select("lotName = ?", new Object[] {lotName});
			
			if (resultList.size() > 0)
				result = true;
		}
		catch (greenFrameDBErrorSignal ne)
		{
			result = false;
		}
		catch (CustomException ce)
		{
			result = false;
		}
		
		return result;
	}
	
	/**
	 * get all activated Q-time on Lot
	 * @author swcho
	 * @since 2014.06.20
	 * @param lotName
	 * @return
	 * @throws CustomException
	 */
	public List<LotQueueTime> findQTimeByLot(String lotName)
		throws CustomException
	{
		List<LotQueueTime> resultList;
		
		try
		{
			resultList = ExtendedObjectProxy.getQTimeService().select("lotName = ? AND queueTimeState <> ?",
							new Object[] {lotName, GenericServiceProxy.getConstantMap().QTIME_STATE_CONFIRM});
		}
		catch (Exception ex)
		{
			resultList = new ArrayList<LotQueueTime>();
		}
		
		return resultList;
	}
	
	
	/**
	 * manage and monitor Q-time
	 * @author swcho
	 * @since 2014.06.20
	 * @param eventInfo
	 * @param lotName
	 * @throws CustomException
	 */
	public void monitorQTime(EventInfo eventInfo, String lotName) throws CustomException
	{
		List<LotQueueTime> QTimeDataList = findQTimeByLot(lotName);
		
		for (LotQueueTime QTimeData : QTimeDataList)
		{
			try
			{
				//because Q-time operand is sensitive
				//in form of millisecond
				long expired = ConvertUtil.getDiffTime(TimeUtils.toTimeString(QTimeData.getEnterTime(), TimeStampUtil.FORMAT_TIMEKEY),
														TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY));
				////limit unit is hour and decimal form
				//expired = (expired / 60 / 60); //hour
				expired = (expired / 60); //minute
				
				//not dependent on master transaction result
				
				// 2018.02.07 hsryu 
				/*
				if(QTimeData.getqueueType().equals("Max"))
				{
					//compare
					if (expired >= Long.parseLong(QTimeData.getInterlockDurationLimit())
							&& QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_WARN))
					{
						GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
						lockQTime(eventInfo, QTimeData.getLotName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName(), QTimeData.gettoProcessFlowName(), QTimeData.getqueueType());
						GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					}
					else if (expired >= Long.parseLong(QTimeData.getWarningDurationLimit())
							&& QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_IN))
					{
						GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
						warnQTime(eventInfo, QTimeData.getLotName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName(), QTimeData.gettoProcessFlowName(), QTimeData.getqueueType());
						GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					}
				}
				if(QTimeData.getqueueType().equals("Min"))
				{
					//compare
					if (expired >= Long.parseLong(QTimeData.getInterlockDurationLimit())
							&& QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_OVER))
					{
						GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
						ExtendedObjectProxy.getQTimeService().resolveQTime(eventInfo, QTimeData.getLotName(), QTimeData.getProcessOperationName(), 
								QTimeData.getToProcessOperationName(), QTimeData.gettoProcessFlowName(), QTimeData.getqueueType());
						GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					}
				}
				*/
				
				if (expired >= Long.parseLong(QTimeData.getInterlockDurationLimit())
						&& QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_WARN))
				{
					GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
					lockQTime(eventInfo, QTimeData.getLotName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName(), QTimeData.gettoProcessFlowName());
					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
				}
				else if (expired >= Long.parseLong(QTimeData.getWarningDurationLimit())
						&& QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_IN))
				{
					GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
					warnQTime(eventInfo, QTimeData.getLotName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName(), QTimeData.gettoProcessFlowName());
					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
				}
			}
			catch (Exception ex)
			{
				GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
				
				logger.warn(String.format("Q-Time[%s %s %s] monitoring failed",
						QTimeData.getLotName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName()));
			}			
		}
	}
	
	/**
	 * handle to in or out Q-time
	 * @author swcho
	 * @since 2014.06.19
	 * @param eventInfo
	 * @param lotName
	 * @param factoryName
	 * @param productSpecName
	 * @param processFlowName
	 * @param processOperationName
	 * @throws CustomException
	 */
	public void processQTime(EventInfo eventInfo, String lotName, String factoryName, String productSpecName, String processFlowName, String processOperationName)
		throws CustomException
	{
		if(eventInfo.getEventComment().equals("TrackInLot"))
		{
			exitQTime(eventInfo, lotName, processOperationName);
		}
		else
		{
			moveInQTime(eventInfo, lotName, factoryName, productSpecName, processFlowName, processOperationName);
		}
	}
	
	/**
	 * enter to all Q-time by Lot 
	 * @author swcho
	 * @since 2014.06.26
	 * @param eventInfo
	 * @param lotName
	 * @param factoryName
	 * @param productSpecName
	 * @param processFlowName
	 * @param processOperationName
	 * @throws CustomException
	 */
	public void moveInQTime(EventInfo eventInfo, String lotName, String factoryName, String productSpecName, String processFlowName, String processOperationName)
		throws CustomException
	{
		List<ListOrderedMap> QTimePolicyList = PolicyUtil.getQTimeSpec(factoryName, productSpecName, processFlowName, processOperationName);
		
		for (ListOrderedMap QtimePolicy : QTimePolicyList)
		{
			String toProcessOperationName = CommonUtil.getValue(QtimePolicy, "TOPROCESSOPERATIONNAME");
			String warningLimit = CommonUtil.getValue(QtimePolicy, "WARNINGDURATIONLIMIT");
			String interLockLimit = CommonUtil.getValue(QtimePolicy, "INTERLOCKDURATIONLIMIT");
			String toProcessFlowName = CommonUtil.getValue(QtimePolicy, "TOPROCESSFLOWNAME");
			//2018.02.22 hsryu - remove 
			//String queueType = CommonUtil.getValue(QtimePolicy, "QUEUETYPE");
			//String alarmCode = CommonUtil.getValue(QtimePolicy, "ALARMCODE");
			
			try
			{
				//GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
				//enter into Q time
				moveInQTime(eventInfo, lotName, processOperationName, toProcessOperationName, warningLimit, interLockLimit, processFlowName, toProcessFlowName, false);
				
				/* 2018.02.07 hsryu - remove 
				if(queueType.equals("Min"))
				{
					List<LotQueueTime> lotQueueTimeList = null;
					try
					{
						String condition = "lotname = :lotname AND queuetype = :queuetype ";
						Object[] bindSet = new Object[]{lotName, queueType};
						lotQueueTimeList = ExtendedObjectProxy.getQTimeService().select(condition, bindSet);
					}
					catch(Exception ex)
					{
						logger.info("Not Found Min QueueType Data");
					}
					
					if(lotQueueTimeList != null)
					{
						lockQTime(eventInfo, lotQueueTimeList.get(0).getLotName(), lotQueueTimeList.get(0).getProcessOperationName(),
								lotQueueTimeList.get(0).getToProcessOperationName(), lotQueueTimeList.get(0).gettoProcessFlowName(), lotQueueTimeList.get(0).getqueueType());
						
						//if Exist Alarm Code, Call Alarm
						if(!alarmCode.isEmpty())
						{
							//ResolveQueueTiem
							ExtendedObjectProxy.getQTimeService().resolveQTime(eventInfo, lotName, processOperationName, toProcessOperationName, toProcessFlowName, queueType);
							
							//Call Alarm
							try
							{
								String targetSubject = GenericServiceProxy.getESBServive().getSendSubject("CNXsvr");
								
								//generate Alarm request message
								Element eleBody = new Element(SMessageUtil.Body_Tag);
								{
									Element eleAlarmCode = new Element("ALARMCODE");
									eleAlarmCode.setText(alarmCode);
									eleBody.addContent(eleAlarmCode);
									
									Element eleAlarmType = new Element("ALARMTYPE");
									eleAlarmType.setText("MES");
									eleBody.addContent(eleAlarmType);
									
									Element eleLotName = new Element("LOTNAME");
									eleLotName.setText(lotName);
									eleBody.addContent(eleLotName);
								}
								
								Document requestDoc = SMessageUtil.createXmlDocument(eleBody, "CreateAlarm",
																	"",//SMessageUtil.getHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", false),
																	targetSubject,
																	eventInfo.getEventUser(),//SMessageUtil.getHeaderItemValue(doc, "EVENTUSER", false),
																	"Queue Time Alarm request");
								
								GenericServiceProxy.getESBServive().sendBySender(targetSubject, requestDoc, "GenericSender");
							}
							catch (Exception ex)
							{
								logger.error(String.format("Lot[%s] is failed to Alarm", lotName));
							}
						}
					}
				}
				*/
				
			}
			catch (Exception ex)
			{
				//Q-time process is optional
				if (logger.isWarnEnabled())
					logger.warn(String.format("Q-time IN process for Lot[%s] to Operation[%s] is failed at Operation[%s]", lotName, toProcessOperationName, processOperationName));
//				GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			}
			finally
			{
//				GenericServiceProxy.getTxDataSourceManager().commitTransaction();
			}
		}
	}
	
	/**
	 * Q-time out process by Lot
	 * @author swcho
	 * @since 2014.06.26
	 * @param eventInfo
	 * @param lotName
	 * @param toProcessOperationName
	 * @throws CustomException
	 */
	public void exitQTime(EventInfo eventInfo, String lotName, String toProcessOperationName)
		throws CustomException
	{
		List<LotQueueTime> QTimeDataList;
		
		try
		{
			QTimeDataList = ExtendedObjectProxy.getQTimeService().select("lotName = ? AND toProcessOperationName = ?",
																			new Object[] {lotName, toProcessOperationName});
		}
		catch (Exception ex)
		{
			QTimeDataList = new ArrayList<LotQueueTime>();
		}
		
		for (LotQueueTime QTimeData : QTimeDataList)
		{
			try
			{				
				//enter into Q time
				exitQTime(eventInfo, QTimeData);
			}
			catch (Exception ex)
			{
				//Q-time process is optional
				if (logger.isWarnEnabled())
					logger.warn(String.format("Q-time OUT process for Lot[%s] to Operation[%s] is failed at Operation[%s]",
								QTimeData.getLotName(), QTimeData.getToProcessOperationName(), QTimeData.getProcessOperationName()));
			}
		}
	}
	
	/**
	 * check Q-time violation by Lot
	 * @author swcho
	 * @since 2014.06.25
	 * @param eventInfo
	 * @param lotName
	 * @throws CustomException
	 */
	public void validateQTime(EventInfo eventInfo, String lotName)
		throws CustomException
	{
		List<LotQueueTime> QTimeDataList = findQTimeByLot(lotName);;
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		String processOp = lotData.getProcessOperationName();
		String processFlow = lotData.getProcessFlowName();
		
		for (LotQueueTime QTimeData : QTimeDataList)
		{
			validateQTime(QTimeData,processOp,processFlow);
		}
		
		//only one Q-time violation report
	}
	
	/**
	 * only to validate and make prohibited by Q-time
	 * @author swcho
	 * @since 2014.06.19
	 * @param QTimeData
	 * @throws CustomException
	 */
	public void validateQTime(LotQueueTime QTimeData,String processOp,String processFlow)
		throws CustomException
	{
		//no need so that refer only to Q-time state
		//because Q-time operand is sensitive
		//in form of millisecond
		//long expired = ConvertUtil.getDiffTime(TimeStampUtil.toTimeString(TimeUtils.getCurrentTimestamp()),
		//										TimeStampUtil.toTimeString(QTimeData.getEnterTime()));
		//limit unit is hour and decimal form
		//expired = (expired / 60 / 60);
		
		//compare
		if (QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_OVER))
		{
			/* 2018.02.07 hsryu - remove
			//20170814 Add by yudan
			if(QTimeData.getqueueType().equals("Min") && QTimeData.gettoProcessFlowName().equals(processFlow) 
					&& QTimeData.getToProcessOperationName().equals(processOp))
			{
			    throw new CustomException("LOT-0203", QTimeData.getLotName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName());				
			}
			else if(QTimeData.getqueueType().equals("Max"))
			{
				//interlock action prior to log-in
				throw new CustomException("LOT-0203", QTimeData.getLotName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName());
			}
			*/
			//interlock action prior to log-in
			throw new CustomException("LOT-0203", QTimeData.getLotName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName());

			
		}
		else if (QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_WARN))
		{
			//interlock action prior to log-in
			logger.warn(String.format("Q-Time[%s %s %s] would be expired soon",
							QTimeData.getLotName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName()));
		}
	}
	
	/**
	 * make designated action for Q-time which returns action type
	 * @author swcho
	 * @since 2014.06.20
	 * @param QTimeData
	 * @return
	 * @throws CustomException
	 */
	public String doActionQTime(LotQueueTime QTimeData,String productSpecName)
			throws CustomException
		{
			if (QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_OVER))
			{
				//do interlock action
				try
				{
					List<ListOrderedMap> QTimePOSPolicyList = PolicyUtil.getPOSQTimeSpec(productSpecName,QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName(), QTimeData.gettoProcessFlowName()); 
					
					for (ListOrderedMap QtimePolicy : QTimePOSPolicyList)
					{
						//String toProcessOperationName = CommonUtil.getValue(QtimePolicy, "TOPROCESSOPERATIONNAME");
						//String holdFlag = CommonUtil.getValue(QtimePolicy, "HOLDFLAG");
						//String reworkFlowName = CommonUtil.getValue(QtimePolicy, "REWORKFLOWNAME");
						String alarmCode = CommonUtil.getValue(QtimePolicy, "ALARMCODE");
						String queueType = CommonUtil.getValue(QtimePolicy, "QUEUETYPE");
					
						//if(holdFlag.equals("Y"))
							//return "HOLD";
						
						//if(!reworkFlowName.isEmpty())
							//return "REWORK";
						
						if(!alarmCode.isEmpty() && queueType.equals("Max"))
							return alarmCode.toString();
					}
				}
				catch (Exception ex)
				{
					logger.warn(String.format("Q-Time[%s %s %s] interlock action failed",
									QTimeData.getLotName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName()));
				}
			}
			else if (QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_WARN))
			{
				//do warning action
				logger.warn(String.format("Q-Time[%s %s %s] is warning to interlock",
								QTimeData.getLotName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName()));
				
				//return "HOLD";
			}
			
			return "";
		}
	
	/**
	 * make resolved Q-time
	 * @author swcho
	 * @since 2014.06.19
	 * @param eventInfo
	 * @param lotName
	 * @param processOperationName
	 * @param toProcessOperationName
	 * @throws CustomException
	 */
	public void resolveQTime(EventInfo eventInfo, String lotName, String processOperationName, String toProcessOperationName, String toProcessFlowName, String queueType)
			throws CustomException
		{
			try
			{
				eventInfo.setEventName("Resolve");
				
				LotQueueTime QtimeData = ExtendedObjectProxy.getQTimeService().selectByKey(true, new Object[] {lotName, processOperationName, toProcessOperationName, toProcessFlowName, queueType});
				QtimeData.setResolveTime(eventInfo.getEventTime());
				QtimeData.setResolveUser(eventInfo.getEventUser());
				QtimeData.setQueueTimeState(GenericServiceProxy.getConstantMap().QTIME_STATE_CONFIRM);
				
				ExtendedObjectProxy.getQTimeService().modify(eventInfo, QtimeData);
				//2017-08-17 modify by yuhonghao //delete CT_LOTQUEUETIME
				ExtendedObjectProxy.getQTimeService().delete(QtimeData);
			}
			catch (greenFrameDBErrorSignal ne)
			{
				//ignore error to consider as not found exception signal
				throw new CustomException("LOT-0201", lotName, processOperationName, toProcessOperationName);
			}
		}

	/**
	 * exit and remove Q-time
	 * @author swcho
	 * @since 2014.06.26
	 * @param eventInfo
	 * @param QTimeData
	 * @throws CustomException
	 */
	public void exitQTime(EventInfo eventInfo, LotQueueTime QTimeData)
		throws CustomException
	{
		try
		{
			eventInfo.setEventName("Exit");
			
			QTimeData.setExitTime(eventInfo.getEventTime());
			ExtendedObjectProxy.getQTimeService().remove(eventInfo, QTimeData);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			//ignore error to consider as not found exception signal
			throw new CustomException("LOT-0201", QTimeData.getLotName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName());
		}
	}
	
	/**
	 * Q-time in
	 * @author swcho
	 * @since 2014.06.19
	 * @param eventInfo
	 * @param lotName
	 * @param processOperationName
	 * @param toProcessOperationName
	 * @param warningLimit
	 * @param interLockLimit
	 * @param dummyFlag
	 * @throws CustomException
	 */
	public void moveInQTime(EventInfo eventInfo, String lotName, String processOperationName, String toProcessOperationName,
							String warningLimit, String interLockLimit, String processFlowName, String toProcessFlowName, boolean dummyFlag)
		throws CustomException
	{
		eventInfo.setEventName("Enter");
		
		LotQueueTime QtimeData = new LotQueueTime(lotName, processOperationName, toProcessOperationName, toProcessFlowName);
		QtimeData.setEnterTime(eventInfo.getEventTime());
		QtimeData.setWarningDurationLimit(warningLimit);
		QtimeData.setInterlockDurationLimit(interLockLimit);
		QtimeData.setQueueTimeState(GenericServiceProxy.getConstantMap().QTIME_STATE_IN);
		
		try
		{
			ExtendedObjectProxy.getQTimeService().create(eventInfo, QtimeData);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			//ignore error to consider as duplicated exception signal
			throw new CustomException("LOT-0202", lotName, processOperationName, toProcessOperationName);
		}
	}
	
	/**
	 * make warned Lot in Q-time
	 * @author swcho
	 * @since 2014.06.20
	 * @param eventInfo
	 * @param lotName
	 * @param processOperationName
	 * @param toProcessOperationName
	 * @throws CustomException
	 */
	public void warnQTime(EventInfo eventInfo, String lotName, String processOperationName, String toProcessOperationName, String toProcessFlowName)
		throws CustomException
	{
		try
		{
			eventInfo.setEventName("Warn");
			
			/* 2018.02.07 hsryu - remove QueueType
			LotQueueTime QtimeData = ExtendedObjectProxy.getQTimeService().selectByKey(true, new Object[] {lotName, processOperationName, toProcessOperationName, toProcessFlowName, queueType});
			*/
			LotQueueTime QtimeData = ExtendedObjectProxy.getQTimeService().selectByKey(true, new Object[] {lotName, processOperationName, toProcessOperationName, toProcessFlowName});
			QtimeData.setWarningTime(eventInfo.getEventTime());
			QtimeData.setQueueTimeState(GenericServiceProxy.getConstantMap().QTIME_STATE_WARN);
			
			ExtendedObjectProxy.getQTimeService().modify(eventInfo, QtimeData);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			//ignore error to consider as not found exception signal
			throw new CustomException("LOT-0201", lotName, processOperationName, toProcessOperationName);
		}
	}
	
	/**
	 * make locked Lot in Q-time
	 * @author swcho
	 * @since 2014.06.20
	 * @param eventInfo
	 * @param lotName
	 * @param processOperationName
	 * @param toProcessOperationName
	 * @throws CustomException
	 */
	public void lockQTime(EventInfo eventInfo, String lotName, String processOperationName, String toProcessOperationName, String toProcessFlowName)
		throws CustomException
	{
		try
		{
			eventInfo.setEventName("Interlock");
			/* 2018.02.07 hsryu - remove queueType
			LotQueueTime QtimeData = ExtendedObjectProxy.getQTimeService().selectByKey(true, new Object[] {lotName, processOperationName, toProcessOperationName, toProcessFlowName, queueType});
			*/
			LotQueueTime QtimeData = ExtendedObjectProxy.getQTimeService().selectByKey(true, new Object[] {lotName, processOperationName, toProcessOperationName, toProcessFlowName});
			QtimeData.setInterlockTime(eventInfo.getEventTime());
			QtimeData.setQueueTimeState(GenericServiceProxy.getConstantMap().QTIME_STATE_OVER);
			
			ExtendedObjectProxy.getQTimeService().modify(eventInfo, QtimeData);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			//ignore error to consider as not found exception signal
			throw new CustomException("LOT-0201", lotName, processOperationName, toProcessOperationName);
		}
	}
	
	/**
	 * checkPcellQtime
	 * @author hykim
	 * @since 2014.06.20
	 * @param lotData
	 * @throws CustomException
	 */
	public void checkPcellQtime(Lot lotData)
		throws CustomException
	{
		List<ListOrderedMap> QTimePolicyList = PolicyUtil.getQTimeSpec(
				lotData.getFactoryName(), lotData.getProductSpecName(), 
				lotData.getProcessFlowName(), lotData.getProcessOperationName());
		
		for(ListOrderedMap QTimePolicy : QTimePolicyList)
		{
			String processOperationName = CommonUtil.getValue(QTimePolicy, "PROCESSOPERATIONNAME");
			String toProcessOperationName = CommonUtil.getValue(QTimePolicy, "TOPROCESSOPERATIONNAME");
			String interLockLimit = CommonUtil.getValue(QTimePolicy, "INTERLOCKDURATIONLIMIT");
			
			if(interLockLimit.isEmpty())
				return;
			
			if(StringUtil.equals(processOperationName, toProcessOperationName))
			{
				long expired = ConvertUtil.getDiffTime(
						TimeUtils.toTimeString(lotData.getLastLoggedInTime(), TimeStampUtil.FORMAT_TIMEKEY),
						TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY));
				
				//CurrentTime - TrackInTime
				expired = (expired / 60 / 60);
				
				if (expired < Long.parseLong(interLockLimit))
				{
					throw new CustomException("LOT-0204", lotData.getKey().getLotName(), 
							TimeUtils.toTimeString(lotData.getLastLoggedInTime(), TimeStampUtil.FORMAT_TIMEKEY),
							interLockLimit);
				}
			}
		}
	}
	
	/**
	 * exitAllQTimeData
	 * @author hwlee89
	 * @since 2016.09.08
	 * @param lotData
	 * @throws CustomException
	 */
	public void exitAllQTimeData(EventInfo eventInfo, Lot lotData)
		throws CustomException
	{
		try
		{
			eventInfo.setEventName("Exit");
			String condition = " WHERE lotName = ? AND queueTimeState IN (? , ?)";
			Object[] bindSet = new Object[]{lotData.getKey().getLotName(), GenericServiceProxy.getConstantMap().QTIME_STATE_WARN
					, GenericServiceProxy.getConstantMap().QTIME_STATE_IN};
			List<LotQueueTime> qTimeList = ExtendedObjectProxy.getQTimeService().select(condition, bindSet);
			
			for(LotQueueTime qTimeData : qTimeList)
			{
				this.exitQTime(eventInfo, qTimeData);
			}
		}
		catch(Exception ex)
		{
			logger.info("Not Found QTimeData");
		}
		
	}
	
	/**
	 * checkInterlocked
	 * @author hwlee89
	 * @since 2016.09.21
	 * @param lotData
	 * @throws CustomException
	 */
	public void checkInterlocked(Lot lotData)
		throws CustomException
	{
		List<LotQueueTime> qTimeList = null;
		try
		{
			String condition = " WHERE lotName = ? AND queueTimeState IN (?)";
			Object[] bindSet = new Object[]{lotData.getKey().getLotName(), GenericServiceProxy.getConstantMap().QTIME_STATE_OVER};
			qTimeList = ExtendedObjectProxy.getQTimeService().select(condition, bindSet);
		}
		catch(Exception ex)
		{
			logger.info("Not Found QTimeData");
		}
		
		if(qTimeList != null && qTimeList.size() > 0)
		{
			throw new CustomException("SYS-9999", "Lot", "QueueTimeState Interlocked");
		}
		
	}
}
