package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspReserveLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class ModifyReserveLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Modify", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String rsvName = SMessageUtil.getBodyItemValue(doc, "RSVNAME", true);
	
		String sql = " SELECT POSITION FROM CT_DSPRESERVELOT WHERE MACHINENAME = :machineName AND POSITION = :position ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("machineName", machineName);
		bindMap.put("position", rsvName);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		if(sqlResult.size() > 0)
		{
			if( rsvName.equals(sqlResult.get(0).get("POSITION").toString()) )
			{
				throw new CustomException("SYS-0005", "");
			}
		}
		DspReserveLot reserveLotData = ExtendedObjectProxy.getDspReserveLotService().selectByKey(false, new Object[] {machineName, carrierName});
				
		reserveLotData.setPosition(Long.parseLong(rsvName));
		
		ExtendedObjectProxy.getDspReserveLotService().modify(eventInfo, reserveLotData);		 
		
		return doc;
	}

}
