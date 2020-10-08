package kr.co.aim.messolution.durable.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class PhotoMaskIdReadRequest  extends SyncHandler{
	private static Log log = LogFactory.getLog(PhotoMaskIdReadRequest.class);
	@Override
	public Object doWorks(Document doc) throws CustomException {

		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_PhotoMaskIdReadSend");
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME" , true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME" , false);	
		String position = SMessageUtil.getBodyItemValue(doc, "POSITION" , true);	
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME" , true);	
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");
		
		//getDurableData
		Durable durableData = CommonUtil.getDurableInfo(maskName);
		
		return doc;
	}
}
