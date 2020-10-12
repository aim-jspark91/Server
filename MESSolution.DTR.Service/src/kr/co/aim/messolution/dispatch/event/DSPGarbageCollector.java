package kr.co.aim.messolution.dispatch.event;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

public class DSPGarbageCollector extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		/**
		 * StockerRegion(102 Push).GarbageTime Over (Push -> Stock)
		 * No Transport. Just Change StockerRegion From 102 Pull To 102 Stock
		 */

		// Pull,Push(Region)에 GABAGETIME을 OVER한 CST 찾기
		String overTimeCSTSql = " SELECT A.DURABLENAME, "
			 + "       A.DURABLESTATE, "
			 + "       A.DURABLEHOLDSTATE, "
			 + "       A.DURABLECLEANSTATE, "
			 + "       A.DURABLETYPE, "
			 + "       NVL (A.TRANSPORTLOCKFLAG, 'N') TRANSPORTLOCKFLAG, "
			 + "       A.STOCKERNAME, "
			 + "       A.STOCKERREGIONTYPE, "
			 + "       A.SETCOUNT, "
			 + "       A.THRESHOLDCOUNT, "
			 + "       A.GABAGETIME, "
			 + "       A.OVERTIME "
			 + "  FROM (SELECT D.DURABLENAME, "
			 + "               D.DURABLESTATE, "
			 + "               D.DURABLEHOLDSTATE, "
			 + "               D.DURABLECLEANSTATE, "
			 + "               D.DURABLETYPE, "
			 + "               SR.STOCKERNAME, "
			 + "               SR.STOCKERREGIONTYPE, "
			 + "               SR.SETCOUNT, "
			 + "               SR.THRESHOLDCOUNT, "
			 + "               SR.GABAGETIME, "
			 + "               D.TRANSPORTLOCKFLAG, "
			 + "               ROUND ( (SYSDATE - NVL (D.LASTEVENTTIME, SYSDATE)) * 60 * 24) OVERTIME "
			 + "          FROM DURABLE D, CT_DSPSTOCKERREGION SR "
			 + "         WHERE D.MACHINENAME = SR.STOCKERNAME "
			 + "           AND SR.STOCKERREGIONTYPE IN (:STOCKERREGIONTYPE1, :STOCKERREGIONTYPE2) "
			 + "           AND D.REGION = SR.STOCKERREGIONTYPE "
			 + "           AND NVL (D.DURABLEHOLDSTATE, 'N') <> 'Y' "
			 + "           AND NVL (D.TRANSPORTLOCKFLAG, 'N') <> 'Y') A "
			 + " WHERE A.OVERTIME > 0 ";
		
		Map<String, Object> overTimeCSTBind = new HashMap<String, Object>();
		overTimeCSTBind.put("STOCKERREGIONTYPE1", GenericServiceProxy.getConstantMap().STOCKERREGION_PULL);
		overTimeCSTBind.put("STOCKERREGIONTYPE2", GenericServiceProxy.getConstantMap().STOCKERREGION_PUSH);
		
		List<Map<String, Object>> overTimeCSTResult = GenericServiceProxy.getSqlMesTemplate().queryForList(overTimeCSTSql, overTimeCSTBind);

		if(overTimeCSTResult.size() > 0)
		{
			// TimeOver된 CST에 대해서 위치변경 (반송은 일어나지 않음) : Region만 변경됨
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeLocation", getEventUser(), getEventComment(), "", "");
			SetEventInfo setEventInfo = new SetEventInfo();
			for ( int i = 0; i < overTimeCSTResult.size(); i++ )
			{
				String carrierName = (String)overTimeCSTResult.get(i).get("DURABLENAME");
				
				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
				setEventInfo.getUdfs().put("REGION", GenericServiceProxy.getConstantMap().STOCKERREGION_STOCK);
				setEventInfo.getUdfs().put("KANBAN", "");
				
				DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
			}
		}
	}
}