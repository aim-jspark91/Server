package kr.co.aim.messolution.port.event;

import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CrateLoadComplete extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String crateName = SMessageUtil.getBodyItemValue(doc, "CRATENAME", true);
		
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		
		if(!StringUtils.equals(portData.getTransferState(), GenericServiceProxy.getConstantMap().Port_ReadyToProcess))
		{	
			eventInfo.setEventName("ChangeTransferState");
			
			MakeTransferStateInfo makeTransferStateInfo = new MakeTransferStateInfo();
			makeTransferStateInfo.setTransferState( GenericServiceProxy.getConstantMap().Port_ReadyToProcess );
			makeTransferStateInfo.setValidateEventFlag( "N" );
			// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//			Map<String,String> udfs = portData.getUdfs();
//			udfs.put("FULLSTATE", GenericServiceProxy.getConstantMap().Port_FULL);
//			udfs.put("CARRIERNAME", crateName);
//			makeTransferStateInfo.setUdfs(udfs);
			makeTransferStateInfo.getUdfs().put("FULLSTATE", GenericServiceProxy.getConstantMap().Port_FULL);
			makeTransferStateInfo.getUdfs().put("CARRIERNAME", crateName);

			MESPortServiceProxy.getPortServiceImpl().makeTransferState(portData, makeTransferStateInfo, eventInfo);
		}

		if(!StringUtils.equals(CommonUtil.getValue(portData.getUdfs(), "FULLSTATE"), GenericServiceProxy.getConstantMap().Port_FULL))
		{
			eventInfo.setEventName("ChangeFullState");
			
			SetEventInfo setEventInfo = new SetEventInfo();
			
			// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//			Map<String,String> udfs = portData.getUdfs();
//			udfs.put("FULLSTATE", GenericServiceProxy.getConstantMap().Port_FULL);
//			udfs.put("CARRIERNAME", crateName);
//			setEventInfo.setUdfs(udfs);
			setEventInfo.getUdfs().put("FULLSTATE", GenericServiceProxy.getConstantMap().Port_FULL);
			setEventInfo.getUdfs().put("CARRIERNAME", crateName);
			
			MESPortServiceProxy.getPortServiceImpl().setEvent(portData, setEventInfo, eventInfo);
		}
		
		/* 2181106, hhlee, add, Current Load CrateName Update, Before CrateName remove portName, machineName ==>> */
		MESConsumableServiceProxy.getConsumableServiceUtil().checkLoadedCrate(eventInfo, portData.getFactoryName(), crateName, machineName, portName);
        /* <<== 2181106, hhlee, add, Current Load CrateName Update, Before CrateName remove portName, machineName */
		
		/* 20181204, hhlee, add, add EventName ==>> */
        eventInfo.setEventName("Load");
        /* <<== 20181204, hhlee, add, add EventName */
        
		MESConsumableServiceProxy.getConsumableServiceUtil().changeConsumableLocation(eventInfo, crateName, portData.getAreaName(), machineName, portName);
		
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(crateName);
		Consumable consumableData = ConsumableServiceProxy.getConsumableService().selectByKeyForUpdate(new ConsumableKey(crateName));
		
		consumableData.setMaterialLocationName(portName);
		consumableData.setFactoryName(portData.getFactoryName());
		ConsumableServiceProxy.getConsumableService().update(consumableData);
				
		//150117 by swcho : success then report to FMC
		try
		{
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
		}
		catch(Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}	
	}
}