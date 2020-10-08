package kr.co.aim.messolution.extended.object.management.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.PhtMaskStocker;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PhtMaskStockerService extends CTORMService<PhtMaskStocker>  {
	
	public static Log logger = LogFactory.getLog(PhtMaskStocker.class);
	
	private final String historyEntity = "PhtMaskStockerHist";
	
	public List<PhtMaskStocker> select(String condition, Object[] bindSet)
			throws CustomException
	{
		List<PhtMaskStocker> result = super.select(condition, bindSet, PhtMaskStocker.class);
		
		return result;
	}
	
	public PhtMaskStocker selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(PhtMaskStocker.class, isLock, keySet);
	}
	
	public PhtMaskStocker create(EventInfo eventInfo, PhtMaskStocker dataInfo)
			throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, PhtMaskStocker dataInfo)
			throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public PhtMaskStocker modify(EventInfo eventInfo, PhtMaskStocker dataInfo)
			throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void checkStockInByMaskName(String maskName) throws CustomException
	{
		StringBuffer queryBuffer = new StringBuffer()
		.append("SELECT MACHINENAME, \n")
		.append("       UNITNAME, \n")
		.append("       LOCATION, \n")
		.append("       CURRENTMASKNAME, \n")
		.append("       CURRENTINTIME, \n")
		.append("       LASTOUTMASKNAME, \n")
		.append("       LASTOUTTIME \n")
		.append("  FROM CT_PHTMASKSTOCKER \n")
		.append(" WHERE CURRENTMASKNAME = :MASKNAME \n");
		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("MASKNAME", maskName);
		
//		List<ListOrderedMap> result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
			
		if(result.size() > 0)
		{
			throw new CustomException("STK-0003", maskName, (String) result
					.get(0).get("MACHINENAME"), result
					.get(0).get("UNITNAME"),result
					.get(0).get("LOCATION"));
		}

	}

	// 2019.05.09_hsryu_Insert Logic. for PhotoMaskStocker Managerment. 
	// case 1. when PhotoMaskStateReport, MaskData is not null. 
	public void checkMaskStockerAndRecord(Durable maskData, String machineName, String unitName, String maskPosition, EventInfo eventInfo) throws CustomException {

		if(StringUtils.isNotEmpty(machineName) && StringUtils.isNotEmpty(machineName) && StringUtils.isNotEmpty(maskPosition)) {
			MachineSpec machineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(machineName));

			if(StringUtils.equals(CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE"), 
					GenericServiceProxy.getConstantMap().ConstructType_PHOTOMASKSTOCKER)) {

				PhtMaskStocker phtMaskStockerData = null;
				
				try
				{
					phtMaskStockerData = this.selectByKey(false, new Object[] {machineName, unitName, maskPosition});
				}
				catch(Exception e){
					phtMaskStockerData = null;
				}
				
				this.takeOutCurrentMask(machineName, unitName, maskPosition, maskData, eventInfo);
				
				if( phtMaskStockerData != null ){
					// When CurrentMaskName is not null.
					if(StringUtils.isNotEmpty(phtMaskStockerData.getCurrentMaskName())){
						if(StringUtils.equals(phtMaskStockerData.getCurrentMaskName(), maskData.getKey().getDurableName())){
							return;
						}
						else
						{
							// Take Out ! 
							phtMaskStockerData.setLastOutMaskName(phtMaskStockerData.getCurrentMaskName());
							phtMaskStockerData.setLastOutTime(eventInfo.getEventTime());
							
							// Take In !
							phtMaskStockerData.setCurrentMaskName(maskData.getKey().getDurableName());
							phtMaskStockerData.setCurrentInTime(eventInfo.getEventTime());
							
							phtMaskStockerData.setLastEventName(eventInfo.getEventName());
							phtMaskStockerData.setLastEventTime(eventInfo.getEventTime());
							phtMaskStockerData.setLastEventTimeKey(eventInfo.getEventTimeKey());
							phtMaskStockerData.setLastEventComment(eventInfo.getEventComment());
							phtMaskStockerData.setLastEventUser(eventInfo.getEventUser());

							phtMaskStockerData = this.modify(eventInfo, phtMaskStockerData);
						}
					}
					// when CurrentMaskName is null. 
					else{
						// Take In !
						phtMaskStockerData.setCurrentMaskName(maskData.getKey().getDurableName());
						phtMaskStockerData.setCurrentInTime(eventInfo.getEventTime());
						
						phtMaskStockerData.setLastEventName(eventInfo.getEventName());
						phtMaskStockerData.setLastEventTime(eventInfo.getEventTime());
						phtMaskStockerData.setLastEventTimeKey(eventInfo.getEventTimeKey());
						phtMaskStockerData.setLastEventComment(eventInfo.getEventComment());
						phtMaskStockerData.setLastEventUser(eventInfo.getEventUser());

						phtMaskStockerData = this.modify(eventInfo, phtMaskStockerData);
					}
				}
				else
				{
		            phtMaskStockerData = new PhtMaskStocker();
		            
		            phtMaskStockerData.setMachineName(machineName);
		            phtMaskStockerData.setUnitName(unitName);
		            phtMaskStockerData.setLocation(Long.parseLong(maskPosition));
		            
		            phtMaskStockerData.setCurrentMaskName(maskData.getKey().getDurableName());
	                phtMaskStockerData.setCurrentInTime(eventInfo.getEventTime());
	                
					phtMaskStockerData.setLastEventName(eventInfo.getEventName());
					phtMaskStockerData.setLastEventTime(eventInfo.getEventTime());
					phtMaskStockerData.setLastEventTimeKey(eventInfo.getEventTimeKey());
					phtMaskStockerData.setLastEventComment(eventInfo.getEventComment());
					phtMaskStockerData.setLastEventUser(eventInfo.getEventUser());
	                
	                phtMaskStockerData = this.create(eventInfo, phtMaskStockerData);
				}
			
			}
		}
		else{
			logger.info("MachineName or UnitName or MaskPosition is null! can't record PhotoMaskStocker Management Table.");
		}
	}
	
	public void takeOutCurrentMask(String machineName, String unitName, String maskPosition, Durable maskData, EventInfo eventInfo) throws CustomException {
		
		StringBuffer queryBuffer = new StringBuffer()
		.append("SELECT MACHINENAME, \n")
		.append("       UNITNAME, \n")
		.append("       LOCATION, \n")
		.append("       CURRENTMASKNAME, \n")
		.append("       CURRENTINTIME, \n")
		.append("       LASTOUTMASKNAME, \n")
		.append("       LASTOUTTIME \n")
		.append("  FROM CT_PHTMASKSTOCKER \n")
		.append(" WHERE CURRENTMASKNAME = :MASKNAME \n");
		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("MASKNAME", maskData.getKey().getDurableName());
		
//		List<ListOrderedMap> result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
			
		if(result.size() > 0)
		{
			String beforeMachineName = result.get(0).get("MACHINENAME").toString();
			String beforeUnitName = result.get(0).get("UNITNAME").toString();	
			String beforePosition = result.get(0).get("LOCATION").toString();
			
			if(!StringUtils.equals(beforeMachineName, machineName)||
				!StringUtils.equals(beforeUnitName, unitName)||
				!StringUtils.equals(beforePosition, maskPosition)){
				
				PhtMaskStocker phtMaskStockerData = null;
				
				try
				{
					phtMaskStockerData = this.selectByKey(false, new Object[] {beforeMachineName, beforeUnitName, beforePosition});
				}
				catch(Exception e){
					phtMaskStockerData = null;
				}
				
				if(phtMaskStockerData != null){
					if(StringUtils.equals(maskData.getKey().getDurableName(), phtMaskStockerData.getCurrentMaskName())){
						// Take Out ! 
						phtMaskStockerData.setLastOutMaskName(phtMaskStockerData.getCurrentMaskName());
						phtMaskStockerData.setLastOutTime(eventInfo.getEventTime());
						
						phtMaskStockerData.setCurrentMaskName("");
						phtMaskStockerData.setCurrentInTime(null);
						
						phtMaskStockerData.setLastEventName(eventInfo.getEventName());
						phtMaskStockerData.setLastEventTime(eventInfo.getEventTime());
						phtMaskStockerData.setLastEventTimeKey(eventInfo.getEventTimeKey());
						phtMaskStockerData.setLastEventComment(eventInfo.getEventComment());
						phtMaskStockerData.setLastEventUser(eventInfo.getEventUser());
						
						phtMaskStockerData = this.modify(eventInfo, phtMaskStockerData);
					}
				}
			}
		}
	}

	
	// 2019.05.09_hsryu_Insert Logic. for PhotoMaskStocker Managerment. 
	// case 1. when PhotoMaskStateReport, MaskData is null. Change CurrentMask -> LastOutMask
	public void checkMaskStockerAndRecordForNotExistMask(String machineName, String unitName, String maskPosition, EventInfo eventInfo) throws CustomException {

		MachineSpec machineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(machineName));

		if(StringUtils.equals(CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE"), 
				GenericServiceProxy.getConstantMap().ConstructType_PHOTOMASKSTOCKER)) {

			PhtMaskStocker phtMaskStockerData = null;
			
			try
			{
				phtMaskStockerData = this.selectByKey(false, new Object[] {machineName, unitName, maskPosition});
			}
			catch(Exception e){
				phtMaskStockerData = null;
			}
			
			if( phtMaskStockerData != null ){
				// When CurrentMaskName is not null.
				if(StringUtils.isNotEmpty(phtMaskStockerData.getCurrentMaskName())){
					// Take Out ! 
					phtMaskStockerData.setLastOutMaskName(phtMaskStockerData.getCurrentMaskName());
					phtMaskStockerData.setLastOutTime(eventInfo.getEventTime());
					
					phtMaskStockerData.setCurrentMaskName("");
					phtMaskStockerData.setCurrentInTime(null);
					
					phtMaskStockerData.setLastEventName(eventInfo.getEventName());
					phtMaskStockerData.setLastEventTime(eventInfo.getEventTime());
					phtMaskStockerData.setLastEventTimeKey(eventInfo.getEventTimeKey());
					phtMaskStockerData.setLastEventComment(eventInfo.getEventComment());
					phtMaskStockerData.setLastEventUser(eventInfo.getEventUser());
					
					phtMaskStockerData = this.modify(eventInfo, phtMaskStockerData);
				}
			}
			else{
	            phtMaskStockerData = new PhtMaskStocker();
	            
	            phtMaskStockerData.setMachineName(machineName);
	            phtMaskStockerData.setUnitName(unitName);
	            phtMaskStockerData.setLocation(Long.parseLong(maskPosition));
	            
				phtMaskStockerData.setLastEventName(eventInfo.getEventName());
				phtMaskStockerData.setLastEventTime(eventInfo.getEventTime());
				phtMaskStockerData.setLastEventTimeKey(eventInfo.getEventTimeKey());
				phtMaskStockerData.setLastEventComment(eventInfo.getEventComment());
				phtMaskStockerData.setLastEventUser(eventInfo.getEventUser());
                
                phtMaskStockerData = this.create(eventInfo, phtMaskStockerData);
			}
		}
	}
	
	// 2019-07-26
	// Get PhtMaskStockerData By MachineName and Delete Data All
	public void getPhtMaskStockerDataByMachineNameAndAllDelete(String machineName){
		
		List<PhtMaskStocker> phtMaskStockerList=null;
		try {
			phtMaskStockerList = this.select(" MACHINENAME = ? ", new Object[]{machineName});
		} catch (CustomException e) {
			logger.info("phtMaskStockerList size 0");
			return;
		}
		if(phtMaskStockerList!=null && phtMaskStockerList.size()>0){
			String deleteSql = " DELETE FROM CT_PHTMASKSTOCKER WHERE MACHINENAME=:MACHINENAME ";
			
			Map<String, Object> deleteBindMap = new HashMap<String, Object>();
			deleteBindMap.put("MACHINENAME", machineName);
			
			int result = GenericServiceProxy.getSqlMesTemplate().update(deleteSql, deleteBindMap);
		}
		
	}

}
