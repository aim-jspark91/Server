package kr.co.aim.messolution.durable.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ChangeCSTLocation extends SyncHandler {

	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		String sFactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String sDurableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		String sTransportState = SMessageUtil.getBodyItemValue(doc,"TRANSPORTSTATE", true);
		String sAreaName = SMessageUtil.getBodyItemValue(doc,"AREANAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc,"MACHINENAME", true);
		String sPositionType = SMessageUtil.getBodyItemValue(doc,"POSITIONTYPE", true);
		String sPortName = SMessageUtil.getBodyItemValue(doc,"PORTNAME", false);
		String sZoneName = SMessageUtil.getBodyItemValue(doc,"ZONENAME", false);
		
		if(StringUtils.isEmpty(sPortName) && StringUtils.isEmpty(sZoneName)){
			throw new CustomException("PORT-0009");
		}
				
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Durable durableData = DurableServiceProxy.getDurableService().selectByKey(new DurableKey(sDurableName));
		Durable durableData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(sDurableName));
		
		if(!StringUtils.equals(durableData.getFactoryName(), System.getProperty("shop"))) {
			if(!StringUtil.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_Available)) {
				throw new CustomException("CST-0039", sDurableName, durableData.getFactoryName());
			}
		}
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeLoc", getEventUser(), getEventComment(), "", "");
		
		// Added by smkang on 2018.12.10 - When a carrier is changed location, UsedShelfCount or EmptyShelfCount of a stocker is changed.
		ExtendedObjectProxy.getDspStockerZoneService().calculateShelfCount(eventInfo, durableData.getUdfs().get("MACHINENAME"), durableData.getUdfs().get("ZONENAME"), "", "");
		
		MachineSpec machineSpecData = new MachineSpec();
		if(!sMachineName.isEmpty())
		{
			// Modified by smkang on 2018.10.19 - This machine can't be existed in this factory.
			//									  So machine should be searched using DB link.
//			machineSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(sMachineName);
			if (System.getProperty("shop").equals(sFactoryName)) {
				machineSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(sMachineName);
			} else {
				String dbUserName = ((BasicDataSource)GenericServiceProxy.getSqlMesTemplate().getDataSource()).getUsername();

				if (StringUtils.equals(sFactoryName, "OLED"))
					dbUserName = dbUserName.replace("array", "oled");
					
				String sql = "SELECT MACHINENAME, MACHINETYPE" +
						 	 "  FROM " + dbUserName + ".MACHINESPEC" +
						 	 " WHERE MACHINENAME = :MACHINENAME";
		
				Map<String, String> bindMap = new HashMap<String, String>();
				bindMap.put("MACHINENAME", sMachineName);
						
				List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
				
				if (sqlResult != null && sqlResult.size() > 0) {
					for (Map<String, Object> map : sqlResult) {
						machineSpecData.setKey(new MachineSpecKey(sMachineName));
						machineSpecData.setMachineType(map.get("MACHINETYPE").toString());
					}
				}
			}
		}
		
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		String condition = "where MachineName=? And PortName = ?";
		String condition = "where MachineName=? And PortName = ? for update";
		
		Object[] bindSet = new Object[]{sMachineName,sPortName};
		List<Durable> oldDurableDataList = null;
		
		try{
			oldDurableDataList = DurableServiceProxy.getDurableService().select(condition, bindSet);
		}catch (NotFoundSignal ne){
			oldDurableDataList = new ArrayList<Durable>();
		}
		
		Map<String, String> udfs = durableData.getUdfs();
		
		if(machineSpecData.getKey() != null)
		{
			String machineType = machineSpecData.getMachineType();
			
			// Modified by smkang on 2018.10.22 - Update logic is wrong.
//			if(StringUtil.equals(machineType, "ProductionMachine"))
//			{
//				udfs.put("PORTNAME", sPortName);
//				udfs.put("ZONENAME", sZoneName);
//				udfs.put("POSITIONNAME", "");
//			}
//			else if(StringUtil.equals(machineType, "StorageMachine"))
//			{
//				udfs.put("PORTNAME", "");
//				udfs.put("POSITIONNAME", sPortName);
//				udfs.put("ZONENAME", sZoneName);
//			}
//			else if(StringUtil.equals(machineType, "TransportMachine"))
//			{
//				udfs.put("POSITIONNAME", sPortName);
//				udfs.put("ZONENAME", sZoneName);
//				udfs.put("PORTNAME", "");
//			}
			if(StringUtil.equals(machineType, "ProductionMachine") || StringUtil.equals(machineType, "TransportMachine"))
			{
				udfs.put("PORTNAME", sPortName);
				udfs.put("ZONENAME", "");
				udfs.put("POSITIONNAME", "");
			}
			else if(StringUtil.equals(machineType, "StorageMachine"))
			{
				udfs.put("PORTNAME", "");
				udfs.put("ZONENAME", sZoneName);
				udfs.put("POSITIONNAME", sPortName);
			}
		}
		
		udfs.put("MACHINENAME", sMachineName);
		udfs.put("POSITIONTYPE", sPositionType);
		udfs.put("TRANSPORTSTATE", sTransportState);
		
		//delete old durable  machine data 
		//if(oldDurableDataList.size()>0)
		if(oldDurableDataList.size()> 0 
				&& (StringUtil.equals(machineSpecData.getMachineType(), "ProductionMachine") 
					|| StringUtil.equals(machineSpecData.getMachineType(), "TransportMachine")) )
		{
			for(int i = 0; i < oldDurableDataList.size(); i++ )
			{
				Durable oldDurable = oldDurableDataList.get(i);
				
				setEventComment("ChangeCarrierLocation(" + sDurableName  + ")");
			 	 
				Map<String, String> oldDurableudfs = oldDurable.getUdfs();
				oldDurableudfs.put("MACHINENAME", "");
				oldDurableudfs.put("PORTNAME", "");
				oldDurableudfs.put("NOTE", "ChangeLoc Delete Machine,Port Data.");
				
				// Added by smkang on 2018.10.22 - Additional information need to be updated.
				oldDurableudfs.put("ZONENAME", "");
				oldDurableudfs.put("POSITIONNAME", "");
				oldDurableudfs.put("POSITIONTYPE", "");
				oldDurableudfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
				
				DurableServiceProxy.getDurableService().update(oldDurable);
							
				SetEventInfo oldSetEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setEventInfo(oldDurableudfs);
				EventInfo oldEventInfo = EventInfoUtil.makeEventInfo("ChangeLoc", getEventUser(), getEventComment(), "", "");
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(oldDurable, oldSetEventInfo, oldEventInfo);
				
				// Modified by smkang on 2019.05.28 - DurableServiceProxy.getDurableService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//				oldDurable = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(oldDurable.getKey().getDurableName());
//				
//				// For Clear Note, Add By Park Jeong Su
//				oldDurable.getUdfs().put("NOTE", "");
//				DurableServiceProxy.getDurableService().update(oldDurable);
				Map<String, String> updateUdfs = new HashMap<String, String>();
				updateUdfs.put("NOTE", "");
				MESDurableServiceProxy.getDurableServiceImpl().updateDurableWithoutHistory(oldDurable, updateUdfs);
			}
		}

		setEventComment("ChangeCarrierLocation");
		
		// For FactoryName
		durableData.setFactoryName(sFactoryName);
		durableData.setAreaName(sAreaName);
		
		DurableServiceProxy.getDurableService().update(durableData);
		
		SetEventInfo setEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setEventInfo(udfs);
		
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
		
		// Added by smkang on 2018.12.10 - When a carrier is changed location, UsedShelfCount or EmptyShelfCount of a stocker is changed.
		ExtendedObjectProxy.getDspStockerZoneService().calculateShelfCount(eventInfo, "", "", sMachineName, sZoneName);
		
		// Added by smkang on 2018.10.02 - According to EDO's request, carrier data should be synchronized with shared factory.
		//MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(doc, sDurableName);
			
		return doc;
	}
}