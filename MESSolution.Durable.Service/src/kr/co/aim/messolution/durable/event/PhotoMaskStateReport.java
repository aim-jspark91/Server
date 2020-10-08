package kr.co.aim.messolution.durable.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class PhotoMaskStateReport extends AsyncHandler{
	private static Log log = LogFactory.getLog(PhotoMaskStateReport.class);
	@Override
	public void doWorks(Document doc) throws CustomException {

		try {
			String smachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String  sunitName = ""; //SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);

			String sDurableName = "";
			String maskSubLocation = "";
			
			Document docCopy = (Document) doc.clone();
			
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("StateReport", getEventUser(), getEventComment(), "", "");

			Element eleBody = SMessageUtil.getBodyElement(doc);

			//add by wghuang 20181121
			this.unMountAllPhotoMaskByMachineName(smachineName, eventInfo);
			
			/***************** 2019.07.26_jspark_delete Logic. ********************/
			try{
				ExtendedObjectProxy.getPhtMaskStockerService().getPhtMaskStockerDataByMachineNameAndAllDelete(smachineName);
			}
			catch(Throwable e){
				log.info("Fail delete PhotoMaskStock by MachineName.");
			}
			/****************************************************************************/
			
			String [] sendTerminalContent = new String[5];
			
			if(eleBody!=null)
			{
				for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "UNITLIST", false))
				{
					sunitName = SMessageUtil.getChildText(eledur, "UNITNAME", true);

					List<Element> seqItemList = SMessageUtil.getSubSequenceItemList(eledur, "MASKLIST", false);

					int count = 0;
					
					for(Element elem: seqItemList)
					{
						sDurableName = SMessageUtil.getChildText(elem, "MASKNAME", false);
						String sposition = SMessageUtil.getChildText(elem, "POSITION", true);

						if(StringUtil.isNotEmpty(sDurableName))
						{
							// 2019.05.09_hsryu_Move Logic Location. For MaskStocker Management..
							//String sposition = SMessageUtil.getChildText(elem, "POSITION", true);
							String sMaskTransferState = SMessageUtil.getChildText(elem, "TRANSFERSTATE", true);

							String sSubunitName = SMessageUtil.getChildText(elem, "SUBUNITNAME", false); //Column:POSITIONNAME
							String sUsedCount = SMessageUtil.getChildText(elem, "USEDCOUNT", false);
							String sUsedCountLimit = SMessageUtil.getChildText(elem, "USEDCOUNTLIMIT", false);

							String sMaskState = StringUtil.EMPTY;

							//getMachineData
							CommonUtil.getMachineInfo(smachineName);
							
							//getDurableData
							Durable durableData = null ;
							
							try
							{
								// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//								durableData = CommonUtil.getDurableInfo(sDurableName);
								durableData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(sDurableName));
							}
							catch(Exception ce)
							{
								log.warn(String.format("PhotoMask:[%s] does't exist", sDurableName));
								
								sendTerminalContent[count] = sDurableName;
								
								count ++;
								
								continue;
							}
							
							/***************** 2019.05.09_hsryu_Insert Missed Logic. ********************/
							try{
								//ExtendedObjectProxy.getPhtMaskStockerService().checkMaskStockerAndRecord(durableData, smachineName, sunitName, sposition, eventInfo);
							}
							catch(Throwable e){
								log.info("Fail record PhotoMaskStock InOut.");
							}
							/****************************************************************************/
							 
							//PhotoMask
							/*************************************************************************************
							  PhotoMask TransportState : [ EMPTY | INSTK | INLINE | PROCESSING | MOVING | OUTSTK ]
							**************************************************************************************/
							
							//modified by wghuang 20181122, need to check again
							// Validation
							/*if(StringUtils.equals(durableData.getUdfs().get("MACHINENAME"), ""))
							{
								throw new CustomException("MASK-0002"+"sDurableName is "+sDurableName);
							}*/

							//setPhotoMaskState
					        if(sMaskTransferState.equals(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INLINE))
					        {
					        	maskSubLocation = GenericServiceProxy.getConstantMap().PHTMASKLOCATION_INLIB;
					            sMaskState = GenericServiceProxy.getConstantMap().Cons_Mount;
					        }
					        else if(sMaskTransferState.equals(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_PROCESSING))
					        {
					        	maskSubLocation = GenericServiceProxy.getConstantMap().PHTMASKLOCATION_ONSTAGE;
					            sMaskState = GenericServiceProxy.getConstantMap().Cons_InUse;
					        }
					        else if(sMaskTransferState.equals(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_MOVING))
					        {
					            sMaskState = GenericServiceProxy.getConstantMap().Cons_Mount;
					        }
					        else if(sMaskTransferState.equals(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK))
		                    {
					            sMaskState = GenericServiceProxy.getConstantMap().Cons_Available;
		                    }
					        else if(sMaskTransferState.equals(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INSTK))
		                    {
					            sMaskState = GenericServiceProxy.getConstantMap().Cons_Available;
		                    }
					        else
					        {
					        }
					        
					        if(!durableData.getDurableState().equals(GenericServiceProxy.getConstantMap().Dur_Scrapped))
					        {
					            if(!StringUtils.isEmpty(sUsedCount))
			                    {
			                        durableData.setTimeUsed(Integer.parseInt(sUsedCount));
			                    }
			                    
			                    if(!StringUtils.isEmpty(sUsedCountLimit))
			                    {
			                        durableData.setTimeUsedLimit(Integer.parseInt(sUsedCountLimit));
			                    }
			                    
					            if(!StringUtils.isEmpty(sMaskState))
					            {
			                        durableData.setDurableState(sMaskState);
					            }			            
					        }
					        
					        // MESDurableServiceProxy.getDurableServiceImpl()
							Map<String, String> udfs = durableData.getUdfs();
							/* 20180922, hhlee, delete ==>> */
							//Map<String, String> udfs = CommonUtil.setNamedValueSequence(elem, Lot.class.getSimpleName());
							/* <<== 20180922, hhlee, delete */
							
							/* 20180922, hhlee, delete ==>> */
							//if(!StringUtils.isEmpty(sUsedCount))
		                    //{
		                    //    durableData.setTimeUsed(Integer.parseInt(sUsedCount));
		                    //}                    
		                    //if(!StringUtils.isEmpty(sUsedCountLimit))
		                    //{
		                    //    durableData.setTimeUsedLimit(Integer.parseInt(sUsedCountLimit));
		                    //}                    
		                    //if(!StringUtils.isEmpty(sMaskState))
		                    //{
		                    //    durableData.setDurableState(sMaskState);
		                    //}   
							/* <<== 20180922, hhlee, delete */
							
							// Put data into UDF
							udfs.put("MACHINENAME", smachineName);
							udfs.put("UNITNAME", sunitName);
							//udfs.put("DURABLENAME", sDurableName);
							udfs.put("MASKPOSITION", sposition);
							udfs.put("TRANSPORTSTATE", sMaskTransferState);
							udfs.put("MASKSUBLOCATION", maskSubLocation);
							durableData.setUdfs(udfs);
							
							DurableServiceProxy.getDurableService().update(durableData);

							// SetEvent Info create
							SetEventInfo setEventInfo = new SetEventInfo();
							setEventInfo.setUdfs(udfs);

							// Excute greenTrack API call- setEvent
							MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
							
							log.info("DurableName = "+sDurableName + "Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
						}
						else
						{
							log.warn("MaskName is Empty !!!");
							
							try{
								ExtendedObjectProxy.getPhtMaskStockerService().checkMaskStockerAndRecordForNotExistMask(smachineName, sunitName, sposition, eventInfo);
							}
							catch(Throwable e){
								log.info("Fail record PhotoMaskStock InOut For NotExistMask.");
							}
						}	
					}	
					
					MESDurableServiceProxy.getDurableServiceUtil().sendTerminalMessage(docCopy,smachineName,sendTerminalContent);
				}
			}
			
			GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");
			
		} catch (Exception e) {
			eventLog.error(e);

			if (e instanceof CustomException) {
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ((CustomException) e).errorDef.getErrorCode());
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ((CustomException) e).errorDef.getLoc_errorMessage());
			} else {
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, (e != null) ? e.getClass().getName() : "SYS-0000");
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, (e != null && StringUtils.isNotEmpty(e.getMessage())) ? e.getMessage() : "Unknown exception is occurred.");
			}
			
			GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");
			
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
		}

	}
	
	//add by wghuang 20181121, Photo and PhotoMaskSTK 
	public void unMountAllPhotoMaskByMachineName(String machineName, EventInfo eventInfo) throws CustomException
	{
		eventInfo.setEventUser(machineName);
		eventInfo.setEventComment(eventInfo.getEventName());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		String durableTransferState = GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK;
		String durableState = GenericServiceProxy.getConstantMap().Cons_Available;
		
		/* 20181218, hhlee, modify, add where condition ==>> */
        //String condition = "WHERE DURABLETYPE = ? AND MACHINENAME = ? ";
		//Object[]bindSet = new Object[]{GenericServiceProxy.getConstantMap().PHMask, machineName};
		// 2019.03.25_hsryu_Modify Logic. Mantis 0003193.
        //String condition = "WHERE DURABLETYPE = ? AND MACHINENAME = ? AND DURABLENAME <> ? ";
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//        String condition = "WHERE DURABLETYPE = ? AND MACHINENAME = ? AND NOT DURABLENAME LIKE ? ";
        String condition = "WHERE DURABLETYPE = ? AND MACHINENAME = ? AND NOT DURABLENAME LIKE ? FOR UPDATE";

		// 2019.03.25_hsryu_Modify Logic. Mantis 0003193. 
		//Object[]bindSet = new Object[]{GenericServiceProxy.getConstantMap().DURABLETYPE_PHOTOMASK, machineName, GenericServiceProxy.getConstantMap().VIRTUAL_PHOTOMASKNAME, "NONE%" };
		Object[]bindSet = new Object[]{GenericServiceProxy.getConstantMap().DURABLETYPE_PHOTOMASK, machineName, "NONE%" };
		/* <<== 20181218, hhlee, modify, add where condition */
		
		List<Durable> durableList = new ArrayList<Durable>();
		
		try
		{
			durableList = DurableServiceProxy.getDurableService().select(condition, bindSet);
		}
		catch(Exception ex)
		{		
		}
		
		if(durableList.size() > 0)
		{
			for(Durable durable : durableList)
			{
				durable.setDurableState(durableState);
				durable.setLastEventName(eventInfo.getEventName());
				durable.setLastEventTimeKey(eventInfo.getEventTimeKey());
				durable.setLastEventTime(eventInfo.getEventTime());
				durable.setLastEventUser(eventInfo.getEventUser());
				durable.setLastEventComment(eventInfo.getEventComment());
				
				Map<String, String> udfs = durable.getUdfs();
				
				// Put data into UDF
				udfs.put("MACHINENAME", "");
				udfs.put("UNITNAME", "");
				udfs.put("MASKPOSITION", "");
				udfs.put("TRANSPORTSTATE", durableTransferState);
				udfs.put("MASKSUBLOCATION", "");
				
				durable.setUdfs(udfs);
				
				DurableServiceProxy.getDurableService().update(durable);

				// SetEvent Info create
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.setUdfs(udfs);

				// Excute greenTrack API call- setEvent
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(durable, setEventInfo, eventInfo);
			}
		}
	}
}
