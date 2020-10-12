package kr.co.aim.messolution.lot.event;

import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.management.data.Port;

import org.jdom.Document;

public class LotProcessCanceled extends AsyncHandler 
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

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Cancel", getEventUser(), getEventComment(), null, null);
		
		/* 20190425, hhlee, add, machineSpec validation */
		try
		{
    		MachineSpec machineSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);
    		Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);
    		
    		if (!StringUtil.equals(CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE") , 
                    GenericServiceProxy.getConstantMap().ConstructType_CassetteClenaer))
            {
                /* 20190425, hhlee, modify, change logic(LotHistory record) ==>> */
    		    List<Lot> lotList = null;
    		    
    		    try
                {
                    lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataFromCarrier(carrierName);
                }
                catch (Exception ex) 
                {
                    eventLog.error(ex);
                }
    		    
    		    try
    		    {
                    if(lotList != null && lotList.get(0) != null)
                    {
        		        /* 20181016, hhlee, modify, Because lotprocess is not exist CancelTrakcin ==>> */
                        //if(StringUtil.equals(lotList.get(0).getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_LoggedIn))
                        /* 20190425, hhlee, modify, change logic(LotHistory record) ==>> */
                        if(StringUtil.equals(lotList.get(0).getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Run))
                        {
                            throw new CustomException("LOT-9003", lotList.get(0).getKey().getLotName() +". Current State is " + lotList.get(0).getLotProcessState()); 
                        }
                    }
                }
                catch (Exception ex) 
                {
                    eventLog.error(ex);
                    
                    /* 20190426, hhlee, modify, add variable(setFutureHold) */
                    //doc = MESLotServiceProxy.getLotServiceImpl().setReturnMessageByTrackOut(eventInfo, ex, doc, 
                    //          carrierName, StringUtil.EMPTY, true, false, "LotProcessCancel");
                    doc = MESLotServiceProxy.getLotServiceImpl().setReturnMessageByTrackOut(eventInfo, ex, doc, 
                            carrierName, StringUtil.EMPTY, true, false, false, false, "LotProcessCancel");
                }
                /* <<== 20190425, hhlee, modify, change logic(LotHistory record) ==>> */
            }
    		
    		// ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
            // Added by smkang on 2019.05.21 - According to Jiang Haiying's request, after StartCSTInfoCheckRequest is succeeded, 
			//								   HoldLot/ForceSampling/StartBranch/StartRework/ChangeProductSpec/ChangeProcessFlow/ChangeProcessOperation
			//								   PrepareSort/CancelPrepareSort/ForceOutSampling/CancelBranch/CancelRework/CompleteRework should be rejected.
            //								   So after LotProcessCanceled is succeeded, StartCheckResult is changed to N.
            eventInfo.setBehaviorName("ARRAY");
            eventInfo.setEventName(SMessageUtil.getMessageName(doc));
            eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
            
            SetEventInfo setEventInfo = new SetEventInfo();
            setEventInfo.getUdfs().put("STARTCHECKRESULT", "N");
//            //2019.09.02 Add By Park Jeong Su Mantis 4691
//            setEventInfo.getUdfs().put("FIRSTCHECKRESULT", "N");
            LotServiceProxy.getLotService().setEvent(new LotKey(lotName), eventInfo, setEventInfo);
            // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		}
		catch (Exception ex) 
        {
		    eventLog.error(ex);
        }
		
		
		/* 20181016, hhlee, delete, Because array is not exist CancelTrackin ==>> */
		////Added by jjyoo on 2018.09.28
		////When Track in Cancel, if there is inhibit data applied before, process lot count should be decreased
		//MESLotServiceProxy.getLotServiceImpl().decreaseInhibitProcessLotCount(eventInfo, lotList.get(0), machineName);
		/* <<== 20181016, hhlee, delete, Because array is not exist CancelTrackin */
		
		//Cancel History
/*		Map<String, String> udfs = lotList.get(0).getUdfs();
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs(udfs);		
		LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo);*/
		
		
		/*if(CommonUtil.isInitialInput(machineName))
		{
			if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PU"))
			{  
				//CST unloader
				ReserveLot reserveLot = ExtendedObjectProxy.getReserveLotService().selectByKey(false, new Object[] {machineName, lotName});
				
				if(reserveLot.getReserveState().equals(GenericServiceProxy.getConstantMap().RESV_STATE_START))
				{
					eventInfo.setEventName("ChangeState");
					reserveLot.setReserveState(GenericServiceProxy.getConstantMap().RESV_STATE_RESV);			
					ExtendedObjectProxy.getReserveLotService().modify(eventInfo, reserveLot);
				}
			}
		}*/	
	}
}
