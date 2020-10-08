package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class CreateMaskStockerSlot extends SyncHandler {
	
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TakeIn", this.getEventUser(), this.getEventComment(), "", "");

		Element eleBody = SMessageUtil.getBodyElement(doc);	
		
		String sMachineName = SMessageUtil.getBodyItemValue(doc,"MACHINENAME", true);
		String sUnitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String sSlotCount = SMessageUtil.getBodyItemValue(doc, "SLOTCOUNT", true);
		int slotCount = Integer.parseInt(sSlotCount);
		
		if (eleBody != null) 
		{
			String existSql = " SELECT * FROM CT_PHTMASKSTOCKER A "
					+ " WHERE A.MACHINENAME = :MACHINENAME "
					+ " AND A.UNITNAME = :UNITNAME ";

			Map<String, Object> bindSet = new HashMap<String, Object>();
			bindSet.put("MACHINENAME", sMachineName);
			bindSet.put("UNITNAME", sUnitName);

			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(existSql, bindSet);
			if ( sqlResult.size() == 0 )
			{
				String insertSql = " INSERT INTO CT_PHTMASKSTOCKER (MACHINENAME, UNITNAME, LOCATION) "
						+ " VALUES (:MACHINENAME, :UNITNAME, :LOCATION) ";
				
				List<Object[]> insertArgList = new ArrayList<Object[]>();
				
				for ( int i = 1; i <= slotCount; i++ )
				{
					Object[] inbindSet = new Object[3];
					inbindSet[0] = sMachineName;
					inbindSet[1] = sUnitName;
					inbindSet[2] = Integer.toString(i);
					insertArgList.add(inbindSet);
				}
				
				GenericServiceProxy.getSqlMesTemplate().updateBatch(insertSql, insertArgList);
			}
			else
			{
				String maxCountSql = " SELECT MAX(LOCATION) M_LOCATION FROM CT_PHTMASKSTOCKER A " 
						+ " WHERE A.MACHINENAME = :MACHINENAME " 
						+ " AND A.UNITNAME = :UNITNAME "; 

				bindSet.put("MACHINENAME", sMachineName);
				bindSet.put("UNITNAME", sUnitName);

				List<Map<String, Object>> maxCountSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(maxCountSql, bindSet);
				if ( maxCountSqlResult.size() > 0 )
				{
					int maxCount = Integer.parseInt(maxCountSqlResult.get(0).get("M_LOCATION").toString());
					
					if ( slotCount - maxCount == 0 )
					{
						// Nothing
					}
					else if ( slotCount - maxCount > 0 )
					{
						int count = maxCount + 1;
						
						String insertSql = " INSERT INTO CT_PHTMASKSTOCKER (MACHINENAME, UNITNAME, LOCATION) "
								+ " VALUES (:MACHINENAME, :UNITNAME, :LOCATION) ";
						
						List<Object[]> insertArgList = new ArrayList<Object[]>();
						
						for ( int k = count; k <= slotCount; k++ )
						{
							Object[] inbindSet = new Object[3];
							inbindSet[0] = sMachineName;
							inbindSet[1] = sUnitName;
							inbindSet[2] = Integer.toString(k);
							insertArgList.add(inbindSet);
						}
						
						GenericServiceProxy.getSqlMesTemplate().updateBatch(insertSql, insertArgList);
					}
					else if ( slotCount - maxCount < 0 )
					{
						throw new CustomException("MASK-0097", sSlotCount, maxCount, sUnitName);
					}
				}
			}
		}
		
		return doc;
	}
}
