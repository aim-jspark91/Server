package kr.co.aim.messolution.consumable.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CrateProcessStarted extends AsyncHandler {

	@Override
	public void doWorks(Document doc)
		throws CustomException
	{
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sPortName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);	
		
		/* Use as supplementary information. */
		String sCrateName = SMessageUtil.getBodyItemValue(doc, "CRATENAME", true);	
		String sLotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);	
		String sProductSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);	
		String sWorkorderName = SMessageUtil.getBodyItemValue(doc, "WORKORDERNAME", true);	
		String sPlanQuantity = SMessageUtil.getBodyItemValue(doc, "PLANQUANTITY", true);	
		String sProductSizeName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSIZE", true);	
		String sMachineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", true);	
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(sMachineName, sPortName);
				
		//port handling
		makePortWorking(eventInfo, portData);
		
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
	
	private void makePortWorking(EventInfo eventInfo, Port portData)
	{
		try
		{
			if( !StringUtils.equals(portData.getTransferState(), GenericServiceProxy.getConstantMap().Port_Processing) )
			{
				eventInfo.setEventName("ChangeTransferState");
				
				MakeTransferStateInfo makeTransferStateInfo = new MakeTransferStateInfo();
				makeTransferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_Processing);
				makeTransferStateInfo.setValidateEventFlag("N");
				makeTransferStateInfo.setUdfs(portData.getUdfs());
				
				MESPortServiceProxy.getPortServiceImpl().makeTransferState(portData, makeTransferStateInfo, eventInfo);
			}
		}
		catch (Exception ex)
		{
			eventLog.error("Port handling is failed");
		}
	}
}