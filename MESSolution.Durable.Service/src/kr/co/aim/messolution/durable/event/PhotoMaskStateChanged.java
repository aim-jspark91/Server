package kr.co.aim.messolution.durable.event;

import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class PhotoMaskStateChanged  extends SyncHandler{
	private static Log log = LogFactory.getLog(PhotoMaskStateChanged.class);
	@Override
	public Object doWorks(Document doc) throws CustomException {

		Document docCopy = (Document) doc.clone();
		
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_PhotoMaskStateChangedReply");

		SMessageUtil.setBodyItemValue(doc, "RESULT", "OK",true);	
		SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "",true);

		String smachineName= SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sunitName= SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String smaskPosition= SMessageUtil.getBodyItemValue(doc, "POSITION", true);
		String sDurableName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String sTransportState = SMessageUtil.getBodyItemValue(doc, "TRANSFERSTATE", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");

		String [] sendTerminalContent = new String[]{sDurableName};
		
		boolean durableExistenceFlag = true;
		
		if(Integer.parseInt(smaskPosition) > 5)
			throw new CustomException("MASK-0096","");	
		
		Durable durableData = null ;
		
		try
		{
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			durableData = CommonUtil.getDurableInfo(sDurableName);
			durableData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(sDurableName));
		}
		catch(Exception ce)
		{
			//sendTerminalMessage
			MESDurableServiceProxy.getDurableServiceUtil().sendTerminalMessage(docCopy,smachineName,sendTerminalContent);
			durableExistenceFlag = false;
		}
		
		//when not Exist
		if(durableExistenceFlag == false)
			throw new CustomException("DURABLE-9000", sDurableName);
		
		if(StringUtil.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Cons_Scrapped))
        {
		    throw new CustomException("MASK-0076",sDurableName);
        }
		if(StringUtil.equals(CommonUtil.getValue(durableData.getUdfs(), "DURABLEHOLDSTATE"), GenericServiceProxy.getConstantMap().DURABLE_HOLDSTATE_Y))
        {
		    throw new CustomException("MASK-0013",sDurableName);
        }
		
		//if there is data with machinename and maskposition than that mask state will be changed to UNMOUNT/OUTSTK
		//(Requested by guishi 2018.05.28)
		
		//if(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INLINE.equals(sTransportState)||
		//   GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_PROCESSING.equals(sTransportState))
		if(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INLINE.equals(sTransportState) && 
		        StringUtil.equals(CommonUtil.getValue(durableData.getUdfs(), "TRANSPORTSTATE"), GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK))
		{
		    /* 2190516, hhlee, modify, add DURABLETYPE */
			if(MESDurableServiceProxy.getDurableServiceUtil().checkExistenceCountByMachineName(smachineName, sunitName))
			{
			   // throw new CustomException("MASK-0095",""); MODIFY BY JHIYING ON20191213 MANTIS:5401
				//throw new CustomException("MASK-0101","");
				
				log.info("The MaskPosition of Machine:"+ smachineName +" already  has more than 5 pieces mask  !!!");
			}
			eventInfo.getEventComment();
			eventInfo.setEventComment(eventInfo.getEventComment() + " Is Replaced By MaskID : " + sDurableName);
			/* 2190516, hhlee, modify, add DURABLETYPE */
			MESDurableServiceProxy.getDurableServiceUtil().checkExistenceByMachineNamePosition(eventInfo, smachineName, smaskPosition,sTransportState);		    
		}
		
/*		//checkDuplicationuse
		if(sTransportState.equals(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INLINE))
		{
			boolean flag = MESDurableServiceProxy.getDurableServiceUtil().checkPhotoMaskDuplicationByMaskName(smachineName,sDurableName);
			
			if(flag == true)
				throw new CustomException("MASK-0093",sDurableName);
		}*/
	
		//check Mask Only one in InUse
		if(sTransportState.equals(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_PROCESSING))
		{
			if(!durableData.getDurableState().equals(GenericServiceProxy.getConstantMap().Cons_Mount))
				throw new CustomException("MASK-0095","");	
			
			/* 2190516, hhlee, modify, add DURABLETYPE */
			boolean maskStateFlag = MESDurableServiceProxy.getDurableServiceUtil().checkPhotoMaskStateByMachineName(smachineName);
			
			if(maskStateFlag == true)
				throw new CustomException("MASK-0094",smachineName);			
		}

		//setPhotoMaskState
		if(sTransportState.equals(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INLINE))
		{
		    durableData.setDurableState(GenericServiceProxy.getConstantMap().Cons_Mount);
		    
		    /* 20180921, hhlee, add, Lot Note ==>> */
            Map<String,String> durableUdfs = durableData.getUdfs();
            durableUdfs.put("MASKSUBLOCATION", GenericServiceProxy.getConstantMap().PHTMASKLOCATION_INLIB);
            durableData.setUdfs(durableUdfs);
            /* <<== 20180921, hhlee, add, Lot Note */
		}
		else if(sTransportState.equals(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_MOVING))
		{
		    //durableData.setDurableState(StringUtil.upperCase(GenericServiceProxy.getConstantMap().Cons_Mount));
		    durableData.setDurableState(GenericServiceProxy.getConstantMap().Cons_Mount);
		}
		else if(sTransportState.equals(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_PROCESSING))
		{
		    durableData.setDurableState(GenericServiceProxy.getConstantMap().Cons_InUse);
		    /* 20180921, hhlee, add, Lot Note ==>> */
            Map<String,String> durableUdfs = durableData.getUdfs();
            durableUdfs.put("MASKSUBLOCATION", GenericServiceProxy.getConstantMap().PHTMASKLOCATION_ONSTAGE);
            durableData.setUdfs(durableUdfs);
            /* <<== 20180921, hhlee, add, Lot Note */
		}
		else if(sTransportState.equals(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK))
		{
		    //durableData.setDurableState(StringUtil.upperCase(GenericServiceProxy.getConstantMap().Cons_Unmount));
		    //durableData.setDurableState(GenericServiceProxy.getConstantMap().Cons_Unmount);
		    durableData.setDurableState(GenericServiceProxy.getConstantMap().Cons_Available);
		    /* 20180921, hhlee, add, Lot Note ==>> */
            Map<String,String> durableUdfs = durableData.getUdfs();
            durableUdfs.put("MASKSUBLOCATION", StringUtil.EMPTY);
            durableData.setUdfs(durableUdfs);
            /* <<== 20180921, hhlee, add, Lot Note */
		}
		else
		{
		}
		
		/*************************************************************************************
		  PhotoMask TransportState : [ EMPTY | INSTK | INLINE | PROCESSING | MOVING | OUTSTK ]
		**************************************************************************************/
		
		EventInfo eventInfoupdate = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");
		
		if(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK.equals(sTransportState))
		{
			smachineName = "";
			sunitName = "";
			smaskPosition = "";
		}
		
		if(StringUtils.isNotEmpty(smachineName)){
			try{
				ExtendedObjectProxy.getPhtMaskStockerService().checkMaskStockerAndRecord(durableData, smachineName, sunitName, smaskPosition, eventInfo);
			}
			catch(Throwable e){
				log.info("Fail record PhotoMaskStock InOut.");
			}
		}
		else{
			try{
				ExtendedObjectProxy.getPhtMaskStockerService().takeOutCurrentMask(smachineName, sunitName, smaskPosition, durableData, eventInfo);
			}
			catch(Throwable e){
				log.info("Fail TakeOut PhotoMaskStock Management Table.");
			}
		}
			
		
		// Put data into UDF
		//Map<String, String> udfs = new HashMap<String, String>();
		
		Map<String, String> udfs = durableData.getUdfs();
		
		udfs.put("MACHINENAME", smachineName);
		udfs.put("UNITNAME", sunitName);
		udfs.put("MASKPOSITION", smaskPosition);
		udfs.put("TRANSPORTSTATE", sTransportState);

		// SetEvent Info create
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs(udfs);

		DurableServiceProxy.getDurableService().update(durableData);
		
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfoupdate);

		return doc;
	}
}
