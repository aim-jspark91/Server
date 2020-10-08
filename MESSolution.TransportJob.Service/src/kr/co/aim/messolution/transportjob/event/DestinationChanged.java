package kr.co.aim.messolution.transportjob.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greenframe.util.sys.SystemPropHelper;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
 
public class DestinationChanged extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException 
	{
		// Modified by smkang on 2018.05.09 - Design is changed for DestinationChanged action.
//		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Change", getEventUser(), getEventComment(), "", "");
//		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
//		
//		String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
//		//Validation : Exist Carrier
//		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
//		MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
//		
////		String oldDestinationMachineName = SMessageUtil.getBodyItemValue(doc, "OLDDESTINATIONMACHINENAME", false);
////		String oldDestinationPositionType = SMessageUtil.getBodyItemValue(doc, "OLDDESTINATIONPOSITIONTYPE", false);
////		String oldDestinationPositionName = SMessageUtil.getBodyItemValue(doc, "OLDDESTINATIONPOSITIONNAME", false);
////		String oldDestinationZoneName = SMessageUtil.getBodyItemValue(doc, "OLDDESTINATIONZONENAME", false);
////		String newDestinationMachineName = SMessageUtil.getBodyItemValue(doc, "NEWDESTINATIONMACHINENAME", false);
////		String newDestinationPositionType = SMessageUtil.getBodyItemValue(doc, "NEWDESTINATIONPOSITIONTYPE", false);
////		String newDestinationPositionName = SMessageUtil.getBodyItemValue(doc, "NEWDESTINATIONPOSITIONNAME", false);
////		String newDestinationZoneName = SMessageUtil.getBodyItemValue(doc, "NEWDESTINATIONZONENAME", false);		
////		String priority = SMessageUtil.getBodyItemValue(doc, "PRIORITY", false);
//		
//		//Update CT_TRANSPORTJOBCOMMAND
//		MESTransportServiceProxy.getTransportJobServiceImpl().updateTransportJobCommand(transportJobName, doc, eventInfo);
//		
//		//20161118 zhongsl update oldDestination Port TransferState
//		String oldDestinationMachineName = SMessageUtil.getBodyItemValue(doc, "OLDDESTINATIONMACHINENAME", false);
//		String oldDestinationPositionName = SMessageUtil.getBodyItemValue(doc, "OLDDESTINATIONPOSITIONNAME", false);
//		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(oldDestinationMachineName, oldDestinationPositionName);
//		String transferState = GenericServiceProxy.getConstantMap().Port_ReadyToLoad;		
//		MakeTransferStateInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().makeTransferStateInfo(portData, transferState);		
//		MESPortServiceProxy.getPortServiceImpl().makeTransferState(portData, transitionInfo, eventInfo);		
//		try
//		{
//			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
//		}
//		catch(Exception ex)
//		{
//			eventLog.warn("FMC Report Failed!");
//		}
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Change", getEventUser(), getEventComment(), "", "");
		
		String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
		String oldDestMachineName = SMessageUtil.getBodyItemValue(doc, "OLDDESTINATIONMACHINENAME", true);
		String oldDestPositionName = SMessageUtil.getBodyItemValue(doc, "OLDDESTINATIONPOSITIONNAME", false);
		String newDestMachineName = SMessageUtil.getBodyItemValue(doc, "NEWDESTINATIONMACHINENAME", true);
		String newDestPositionName = SMessageUtil.getBodyItemValue(doc, "NEWDESTINATIONPOSITIONNAME", false);
		
		// Deleted by smkang on 2018.05.19 - Although the carrier is not existed in MES DB, the transport job should be updated.
//		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
//		MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		
		TransportJobCommand transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceImpl().updateTransportJobCommand(transportJobName, doc, eventInfo);

		// -----------------------------------------------------------------------------------------------------------------------------------------
		// Commented by smkang on 2018.05.09 - If destination is changed, old destination should be released and new destination should be reserved.
		
		// Modify By Park Jeong Su on 2019.08.13  Mantis 4571 
		// MODIFY BY JHIYING ON20191209 MANTIS:5357 将 原来注释掉的代码释放出来
		MachineSpec oldDestMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(oldDestMachineName));
	
		if(StringUtils.equals(oldDestMachineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine)) {
			Port oldDestPortData = MESPortServiceProxy.getPortServiceUtil().getPortData(oldDestMachineName, oldDestPositionName);
			
			if(!checkOtherTransportCommand(transportJobName, doc)){ //add by jhying on20200417 mantis:6020
			//if (!oldDestPortData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReadyToLoad)) 
			if (oldDestPortData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReservedToLoad)) //modify by jhiying on20191230 mantis :5538
			{
				MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
				makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToLoad);
				makeTranferStateInfo.setValidateEventFlag("N");
				
				PortServiceProxy.getPortService().makeTransferState(oldDestPortData.getKey(), eventInfo, makeTranferStateInfo);
			}
		  }
		}
		
		MachineSpec newDestMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(newDestMachineName));
		
		if(StringUtils.equals(newDestMachineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine)) {
			Port newDestPortData = MESPortServiceProxy.getPortServiceUtil().getPortData(newDestMachineName, newDestPositionName);
			
			if (!newDestPortData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReservedToLoad)) {
				MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
				makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReservedToLoad);
				makeTranferStateInfo.setValidateEventFlag("N");
				
				PortServiceProxy.getPortService().makeTransferState(newDestPortData.getKey(), eventInfo, makeTranferStateInfo);
			}
		}
		// -----------------------------------------------------------------------------------------------------------------------------------------
		
		/*// Added by smkang on 2018.10.07 - Need to forward a message to linked factory.
		MachineSpec sourceMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(transportJobCommandInfo.getSourceMachineName()));
		MachineSpec destMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(transportJobCommandInfo.getDestinationMachineName()));
		if (!sourceMachineSpecData.getFactoryName().equals(destMachineSpecData.getFactoryName())) {
			// Modified by smkang on 2018.10.23 - EDO CIM team doesn't want to register CT_SHIPPINGSTOCKER.
//			if (sourceMachineSpecData.getFactoryName().equals(System.getProperty("shop")))
//				MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToShippingShop(doc, transportJobCommandInfo.getSourceMachineName());
//			else if (destMachineSpecData.getFactoryName().equals(System.getProperty("shop")))
//				MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToShippingShop(doc, transportJobCommandInfo.getDestinationMachineName());
			MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToSharedShop(doc, transportJobCommandInfo.getCarrierName());
		}
		*/
	}
	//add by jhying on20200417 mantis:6020
	private Boolean checkOtherTransportCommand(String transportJobName, Document doc)
			throws CustomException
	{   
		String transportJobID = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
		String oldDestMachineName = SMessageUtil.getBodyItemValue(doc, "OLDDESTINATIONMACHINENAME", true);
		String oldDestPositionName = SMessageUtil.getBodyItemValue(doc, "OLDDESTINATIONPOSITIONNAME", false);
		String oldDestPositionType = SMessageUtil.getBodyItemValue(doc, "OLDDESTINATIONPOSITIONTYPE", false);
		String oldDestZoneName = SMessageUtil.getBodyItemValue(doc, "OLDDESTINATIONZONENAME", false);
		
		Boolean isExistOtherTransportCommand = false ;
	
		  String transportCommandsql = "select * from CT_TRANSPORTJOBCOMMAND where DESTINATIONMACHINENAME = :DESTINATIONMACHINENAME "
				+ "and DESTINATIONPOSITIONTYPE = :DESTINATIONPOSITIONTYPE "
				+ "and (DESTINATIONPOSITIONNAME = :DESTINATIONPOSITIONNAME or :DESTINATIONPOSITIONNAME is null)"
				+ "and  (DESTINATIONZONENAME = :DESTINATIONZONENAME or :DESTINATIONZONENAME is null) "
				+ "and TRANSPORTJOBNAME <> :TRANSPORTJOBNAME";
		
		Map<String, Object> transportCommandBindSet = new HashMap<String, Object>();
		
		transportCommandBindSet.put("DESTINATIONMACHINENAME", oldDestMachineName);
		transportCommandBindSet.put("DESTINATIONPOSITIONTYPE",oldDestPositionType );
		transportCommandBindSet.put("DESTINATIONPOSITIONNAME",oldDestPositionName );
		transportCommandBindSet.put("DESTINATIONZONENAME",oldDestZoneName );
		transportCommandBindSet.put("TRANSPORTJOBNAME",transportJobID );
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(transportCommandsql, transportCommandBindSet);

	    if(sqlResult.size() >0)
	    {
	    	isExistOtherTransportCommand = true;
	    }
		else
		{
			isExistOtherTransportCommand = false;
		}
		return isExistOtherTransportCommand;
	}
}