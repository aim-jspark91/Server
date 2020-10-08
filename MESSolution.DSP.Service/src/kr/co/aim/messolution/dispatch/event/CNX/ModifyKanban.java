package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.HashMap;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspStockerKanban;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.collections.map.ListOrderedMap;
import org.jdom.Document;

public class ModifyKanban extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String stockerName = SMessageUtil.getBodyItemValue(doc, "STOCKERNAME", true);
		String zoneName = SMessageUtil.getBodyItemValue(doc, "ZONENAME", true);
		String kanbanName = SMessageUtil.getBodyItemValue(doc, "KANBANNAME", true);
		String productionType = SMessageUtil.getBodyItemValue(doc, "PRODUCTIONTYPE", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String ecCode = SMessageUtil.getBodyItemValue(doc, "ECCODE", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		//String machineGroupName = SMessageUtil.getBodyItemValue(doc, "MACHINEGROUPNAME", true);
		//String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String useFlag = SMessageUtil.getBodyItemValue(doc, "USEFLAG", true);
		String setCount = SMessageUtil.getBodyItemValue(doc, "SETCOUNT", true);
		
		//	add	aim.yunjm	2019.03.18	Zone SetCount Check
		try{
			String strSql = "SELECT DISTINCT K.STOCKERNAME, K.ZONENAME "
					+ "  , SUM(K.SETCOUNT) AS TOTALKANBANCOUNT "
					+ "  , Z.TOTALCAPACITY "
					+ "  FROM CT_DSPSTOCKERKANBAN K, CT_DSPSTOCKERZONE Z "
					+ "  WHERE 1=1 "
					+ "    AND K.STOCKERNAME = :STOCKERNAME "
					+ "    AND K.ZONENAME = :ZONENAME "
					+ "    AND K.STOCKERNAME = Z.STOCKERNAME "
					+ "    AND K.ZONENAME = Z.ZONENAME "
					+ "  GROUP BY K.STOCKERNAME, K.ZONENAME, Z.TOTALCAPACITY ";
	
			HashMap<String, String> bindList = new HashMap<String, String>();
			bindList.put("STOCKERNAME", stockerName);
			bindList.put("ZONENAME", zoneName);
			
			List<ListOrderedMap> zoneDataList = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindList);
			ListOrderedMap zoneData = zoneDataList.get(0);
			
			if(Integer.parseInt(zoneData.get("TOTALKANBANCOUNT").toString()) + Integer.parseInt(setCount) 
					> Integer.parseInt(zoneData.get("TOTALCAPACITY").toString())){
				throw new CustomException("", "");
			}
		}catch(CustomException c){
			throw new CustomException("COMMON-0001", "Sum of all Kanban's SetCount can't exceed Zone's SetCount");
		}catch(Exception e){
			eventLog.error("Not Found Data");
		}
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Modify", this.getEventUser(), this.getEventComment(), "", "");
			
		DspStockerKanban stockerKanbanData = null;
		
		try
		{
			stockerKanbanData = ExtendedObjectProxy.getDspStockerKanbanService().selectByKey(false, new Object[] {stockerName, zoneName, kanbanName});
		}
		catch (Exception ex)
		{
			stockerKanbanData = null;
		}
		
		if(stockerKanbanData == null)
		{
			throw new CustomException("RECIPE-0009", "");
		}
		
		stockerKanbanData = new DspStockerKanban(stockerName, zoneName, kanbanName);
		stockerKanbanData.setProductionType(productionType);
		stockerKanbanData.setProductSpecName(productSpecName);
		stockerKanbanData.setEcCode(ecCode);
		stockerKanbanData.setProcessFlowName(processFlowName);
		stockerKanbanData.setProcessOperationName(processOperationName);
		//stockerKanbanData.setMachineGroupName(machineGroupName);
		//stockerKanbanData.setMachineName(machineName);
		stockerKanbanData.setUseFlag(useFlag);
		stockerKanbanData.setSetCount(Long.parseLong(setCount));
		stockerKanbanData.setLastEventUser(eventInfo.getEventUser());
		stockerKanbanData.setLastEventComment(eventInfo.getEventComment());
		stockerKanbanData.setLastEventTime(eventInfo.getEventTime());
		stockerKanbanData.setLastEventTimekey(eventInfo.getEventTimeKey());
		stockerKanbanData.setLastEventName(eventInfo.getEventName());
		
		ExtendedObjectProxy.getDspStockerKanbanService().modify(eventInfo, stockerKanbanData);
		
		return doc;
	}
}
