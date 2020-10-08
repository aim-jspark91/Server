package kr.co.aim.messolution.alarm.event.ALM;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.alarm.MESAlarmServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LotAction;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ClearAlarm extends SyncHandler 
{
	public Object doWorks(Document doc) throws CustomException 
	{
		Element eleBody = SMessageUtil.getBodyElement(doc);
		
		

		if (eleBody != null) 
		{
			for (Element elementAlarm : SMessageUtil.getBodySequenceItemList(doc, "ALARMLIST", false))
			{
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("ClearAlarm", getEventUser(), "ClearAlarm", "", "");
				String alarmCode = SMessageUtil.getChildText(elementAlarm, "ALARMCODE", true);
				String machineName = SMessageUtil.getChildText(elementAlarm, "MACHINENAME", true);		
				String unitName = SMessageUtil.getChildText(elementAlarm, "UNITNAME", false);
				String subUnitName = SMessageUtil.getChildText(elementAlarm, "SUBUNITNAME", false);
				String alarmState = SMessageUtil.getChildText(elementAlarm, "ALARMSTATE", false);
				String alarmType = SMessageUtil.getChildText(elementAlarm, "ALARMTYPE", false);
				
				MESAlarmServiceProxy.getAlarmServiceImpl().removeAlarm(machineName, unitName, subUnitName, alarmCode, eventInfo);

			}
		}
		

		
		return doc;
	}
}
