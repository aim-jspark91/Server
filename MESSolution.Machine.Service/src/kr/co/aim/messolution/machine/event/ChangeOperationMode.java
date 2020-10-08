package kr.co.aim.messolution.machine.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;

import org.jdom.Document;

public class ChangeOperationMode extends SyncHandler 
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		// TODO Auto-generated method stub
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sOperationMode = SMessageUtil.getBodyItemValue(doc, "OPERATIONMODE", true);
		String Description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
		String OLDsOperationMode = SMessageUtil.getBodyItemValue(doc, "OLDOPERATIONMODE", false);
		String FACTORYNAME = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeOperMode", this.getEventUser(), this.getEventComment(), "", "");
		
		this.checkProcessingLotData(sMachineName, FACTORYNAME);
		
		SetEventInfo setEventInfo = new SetEventInfo();

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sMachineName);
		// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//		Map<String, String> udfs = machineData.getUdfs();
//		udfs.put("OPERATIONMODE", sOperationMode);
//		setEventInfo.setUdfs(udfs);
		setEventInfo.getUdfs().put("OPERATIONMODE", sOperationMode);
		MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, eventInfo);
			
		return doc;
	}	
	
	private void checkProcessingLotData(String sMachineName, String FACTORYNAME) throws CustomException
	{
		String sql = "SELECT L.LOTPROCESSSTATE " +
				"  FROM MACHINE M JOIN LOT L ON L.MACHINENAME = M.MACHINENAME " +
				" WHERE L.FACTORYNAME = :FACTORYNAME AND L.MACHINENAME = :MACHINENAME " ;

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("FACTORYNAME", FACTORYNAME);
		bindMap.put("MACHINENAME", sMachineName);
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		if(sqlResult.size() > 0)
		{
			for( int i=0; i<sqlResult.size(); i++)
			{
				if(sqlResult.get(i).get("LOTPROCESSSTATE").toString().equals("RUN"))
				{
					throw new CustomException("MACHINESTATE-0001");
				}
			}
		}		
	}
}
