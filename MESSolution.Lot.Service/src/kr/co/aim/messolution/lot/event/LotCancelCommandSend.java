package kr.co.aim.messolution.lot.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.jdom.Document;

public class LotCancelCommandSend extends SyncHandler 
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String carrierState = SMessageUtil.getBodyItemValue(doc, "CARRIERSTATE", false);
		String cancelDescription = SMessageUtil.getBodyItemValue(doc, "CANCELDESCRIPTION", false);
		
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		if(lotData != null)
		{
			if(StringUtil.equals(lotData.getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_LoggedIn))
			{
				throw new CustomException("LOT-9003", lotData.getKey().getLotName() +". Current State is " + lotData.getLotProcessState()); 
			}
			
			if(StringUtil.equals(lotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_OnHold))
			{
				throw new CustomException("LOT-9003", lotData.getKey().getLotName() +". Current Hold State is " + lotData.getLotHoldState()); 
			}
			
			if(StringUtil.isEmpty(lotData.getCarrierName()))
			{
				throw new CustomException("CST-9001", carrierName); 
			}
			
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			SMessageUtil.setHeaderItemValue(doc, "EVENTUSER", machineData.getKey().getMachineName());
			
			String targetSubjectName = CommonUtil.getValue(machineData.getUdfs(), "MCSUBJECTNAME");
			GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "EISSender");
		}
		return doc;
	}
}