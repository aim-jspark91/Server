package kr.co.aim.messolution.lot.event;

import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

import org.jdom.Document;

public class LotProcessHoldRequest extends AsyncHandler 
{
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String portName    = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
		String reasonCodeDescription = SMessageUtil.getBodyItemValue(doc, "REASONCODEDESCRIPTION", false);

		/* 1. Set EventInfo */
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Hold", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventComment("Lot Process Hold Request");
		
		/* 2. Machine, Port Validation */
		Machine machineData = MESMachineServiceProxy.getMachineServiceUtil().getMachineData(machineName);
		Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);
		
		/* 3. Lot Validataion by carrierName */
		List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataFromCarrier(carrierName);
		if(lotList == null || lotList.size() <= 0)
		{
		    throw new CustomException("LOT-9001", "LotName = , CarrerName = " + carrierName + ", MachineName  = " + machineName + ", PortName = " + portName);
            //eventLog.error(String.format("[LOT-9001] Lot[%s] does not exist", "LotName = , CarrerName = " + carrierName + ", MachineName  = " + machineName + ", PortName = " + portName));
		}
		
		/* 4. Lot Hold execute */
		for(Lot lotData : lotList)
		{
		    // 4-1. LotProcessState Validation
    		if(StringUtil.equals(lotData.getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Wait))
    		{
    		    eventInfo.setEventName("Hold");
    		    //4-2. Lot Hold
    		    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName, null, "HoldLot", "LAHL", "");
    		    /* 20190424, hhlee, modify, changed function ==>> */
                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName, lotData, "HoldLot", reasonCode, reasonCodeDescription);
    		    /* 20190426, hhlee, modify, add variable(setFutureHold) */
    		    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LAHL",reasonCodeDescription, true, "", "");
    		    try {
    		    	GenericServiceProxy.getTxDataSourceManager().beginTransaction();
    		    	MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LAHL",reasonCodeDescription, true, false, "", "");
    		    	GenericServiceProxy.getTxDataSourceManager().commitTransaction();
				} catch (Exception e) {
					GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
				}
    		    
                /* <<== 20190424, hhlee, modify, changed function */
    		}
    		else if(StringUtil.equals(lotData.getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Run))
    		{
    		    throw new CustomException("LOT-9003", lotList.get(0).getKey().getLotName() +". Current State is " + lotList.get(0).getLotProcessState());
    		    //eventLog.error(String.format("[LOT-9003] Lot[%s Current State is %s ] has invalid state for this function.", lotData.getKey().getLotName(), lotData.getLotProcessState()));
    		}
    		else
    		{
    		}
		}
	}
}
