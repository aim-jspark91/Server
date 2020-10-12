package kr.co.aim.messolution.lot.event;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class LotProcessStarted extends SyncHandler
{
	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackIn", getEventUser(), getEventComment(), "", "");
		String carrierName = "";

		// 2019.05.17_hsryu_Up try Logic. Mantis 0003892.
		try
		{
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_LotProcessStartedReply");

			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
			String lotName = StringUtil.EMPTY;
			carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
			String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", true);

			/* 20190125, hhlee, change, PU Port and UNPACKER PU Port are treated with the same logic ==>> */
			//if(CommonUtil.isInitialInput(machineName))
			//{
			//  this.UnPackProcessStarted(doc);
			//  /*this.UnpackerProcessStarted(doc);
			//  return doc ;*/
			//}
			//else
			//{
			/* <<== 20190125, hhlee, change, PU Port and UNPACKER PU Port are treated with the same logic */

			//lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);

			/* 20181122, hhlee, add, LotProcessStartTrackIn Error Hold ==>> */
			Lot trackInLot = null;
			Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName); 

			/* 20190402, hhlee, add, update TransferState = 'Processing' ==>> */
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			if(StringUtil.equals(CommonUtil.getValue(durableData.getUdfs(), "PORTNAME") , portName))
			{
				MESLotServiceProxy.getLotServiceImpl().makePortWorking(eventInfo, portData);
			}
			else
			{
				throw new CustomException("CST-9004", carrierName, CommonUtil.getValue(durableData.getUdfs(), "PORTNAME"), portName);
			}
			/* <<== 20190402, hhlee, add, update TransferState = 'Processing' */

			if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PB") ||
                    CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PL") ||
                    CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PO")) 
            {
                /* 20180811, Modify, LotProcessstart(TrackIn) common logic change ==>> */ 
                //Lot trackInLot = MESLotServiceProxy.getLotServiceImpl().LotProcessStartTrackIn(doc, eventInfo);
                trackInLot = MESLotServiceProxy.getLotServiceImpl().LotProcessStartTrackIn(doc, eventInfo);
            }
            else
            {
                /* 20190402, hhlee, modify, Seperated logic of Lot and Carrier  */
                try
                {
                    // PU Port LotProcess Start
                    MESLotServiceProxy.getLotServiceImpl().LotProcessStartTrackInbyEmptyCassette(doc, eventInfo);
                }
                /* 20190410, hhlee, modify, CleanCSTStart fail ==>> */
                catch (Exception ex) 
                {
                    /* 20190416, hhlee, add, rollbackTransaction  */
                    GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
                    try {
                    	GenericServiceProxy.getTxDataSourceManager().beginTransaction();
                        /* EventInfo eventInfo, Exception exceptionEx, Document doc, String carrierName, 
                         * boolean setLotHold, boolean setCarrierHold, boolean carrierStart
                         */
                        /* 20190425, hhlee, modify, change variable(carrierStart delete) */
                        //doc = MESLotServiceProxy.getLotServiceImpl().setReturnMessageAndLotHoldByTrackIn(eventInfo, ex, doc, 
                        //        carrierName, StringUtil.EMPTY, false, false, true, true);
                        /* 20190426, hhlee, modify, add variable(setFutureHold, setEventLog) */
                        //doc = MESLotServiceProxy.getLotServiceImpl().setReturnMessageByTrackIn(eventInfo, ex, doc, 
                        //        carrierName, StringUtil.EMPTY, false, false, "CarrierStartFail");
                        doc = MESLotServiceProxy.getLotServiceImpl().setReturnMessageByTrackIn(eventInfo, ex, doc, 
                                carrierName, StringUtil.EMPTY, false, false, false, false, "CarrierStartFail");
                        GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					} catch (Exception e) {
	                    GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
					}
                    return doc;
                }
                //catch (CustomException ce)
                //{
                //    SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
                //    SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
                //    return doc;
                //}
                /* <<== 20190410, hhlee, modify, CleanCSTStart fail */
            }

			// -----------------------------------------------------------------------------------------------------------------------------------------------------------
			// Added by smkang on 2018.08.31 - Update MQCPreRunCount of MQCCondition.
			try {
				/********** 2019.02.01_hsryu_Delete Logic ***********/
				//String condition = "SUPERMACHINENAME = ? AND DETAILMACHINETYPE = ?";
				//Object[] bindSet = new Object[] {machineName, "UNIT"};
				//List<MachineSpec> unitSpecList = MachineServiceProxy.getMachineSpecService().select(condition, bindSet);

				//2019.02.01_add Logic. get MachineData.
				Machine machinedata = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

				//for (MachineSpec unitSpec : unitSpecList) {
				MESMachineServiceProxy.getMachineServiceImpl().updateMachineIdleTimePreRunInfo(machineName, trackInLot, portName, machinedata.getUdfs().get("OPERATIONMODE"),  eventInfo);
				//}
			} catch (Exception e) {
				eventLog.warn(e);
				/********** 2019.02.01_hsryu_Delete Logic ***********/
				// Added by smkang on 2018.11.17 - Although a machine has no unit, updateMachineIdleTimePreRunInfo should be invoked.
				//				MESMachineServiceProxy.getMachineServiceImpl().updateMachineIdleTimePreRunInfo(machineName, "", trackInLot, eventInfo);
			}

			try
			{
				//2019.10.23 dmlee : 'A_LotProcessStartedReply' No Regist ClassMap in FMCsvr, If you want, Regist Class Map.
				//GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");
			}
			catch(Exception ex)
			{
				eventLog.warn("FMC Report Failed!");
			}

			return doc;
        }
        /* 20190410, hhlee, modify, LotProcessStarted fail is lotHold ==>> */
        catch (Exception ex) 
        {
            /* 20190416, hhlee, add, rollbackTransaction  */
            GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
            
            /* EventInfo eventInfo, Exception exceptionEx, Document doc, String carrierName, 
             * boolean setLotHold, boolean setCarrierHold, boolean carrierStart
             */
            /* 20190425, hhlee, modify, change variable(carrierStart delete) */
            //doc = MESLotServiceProxy.getLotServiceImpl().setReturnMessageAndLotHoldByTrackIn(eventInfo, ex, doc, 
            //        carrierName, StringUtil.EMPTY, true, false, false, true);
            /* 20190426, hhlee, modify, add variable(setFutureHold, setEventLog) */
            //doc = MESLotServiceProxy.getLotServiceImpl().setReturnMessageByTrackIn(eventInfo, ex, doc, 
            //        carrierName, StringUtil.EMPTY, true, false, "TrackInFail");
            
            try
            {
            	GenericServiceProxy.getTxDataSourceManager().beginTransaction();
                doc = MESLotServiceProxy.getLotServiceImpl().setReturnMessageByTrackIn(eventInfo, ex, doc, 
                        carrierName, StringUtil.EMPTY, true, false, true, false, "TrackInFail");
                
                GenericServiceProxy.getTxDataSourceManager().commitTransaction();
            }
            catch(Exception ex2)
            {
            	GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
            }
            
            
            return doc;
        }
        //catch (CustomException ce)
        //{
        //    eventInfo.setEventName("Hold");
        //    eventInfo.setEventComment("TrackIn Fail.! - " + ce.errorDef.getLoc_errorMessage());
        //    /* 20190201, hhlee, add, if only processstate = 'WAIT' , lot hold */
        //    if(StringUtil.equals(ce.errorDef.getErrorCode(), "LOT-9046"))
        //    {                
        //    }
        //    else
        //    {
        //        MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABST",""); // Abnormal Start
        //    }
        //    SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
        //    /* 20190402, hhlee, modify, Add "TrackIn Fail.!" Message */
        //    SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, "TrackIn Fail.! - " + ce.errorDef.getLoc_errorMessage());
        //    return doc;        
        //}
        /* <<== 20190410, hhlee, modify, LotProcessStarted fail is lotHold */        
        /* <<== 20181122, hhlee, add, LotProcessStartTrackIn Error Hold */
	}
}