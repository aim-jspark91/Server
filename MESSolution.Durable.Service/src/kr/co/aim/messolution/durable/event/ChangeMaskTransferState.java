package kr.co.aim.messolution.durable.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class ChangeMaskTransferState extends SyncHandler {
	private static Log log = LogFactory.getLog(ChangeMaskTransferState.class);
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String messageName = SMessageUtil.getMessageName(doc);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, getEventUser(), getEventComment(), "", "");
		
		Element eleBody = SMessageUtil.getBodyElement(doc);
		
		if(eleBody!=null)
		{
			
				String sDurableName = SMessageUtil.getChildText(eleBody, "DURABLENAME", true);
				String sTransportState = SMessageUtil.getChildText(eleBody, "TRANSPORTSTATE", true);
				String sMaskUsedLimit = SMessageUtil.getChildText(eleBody, "MASKUSEDLIMIT", false);//
				String sMaskUseCount = SMessageUtil.getChildText(eleBody, "TRANSPORTSTATE", false);//
			

				//getDurableData
				Durable durableData = CommonUtil.getDurableInfo(sDurableName);
				//getTimeUsedLimit
				Double maskUsedLimit = durableData.getTimeUsedLimit();
				Double tempUsedCount = 0.0;
						
				// Validation - Check DurableType
				if(!(StringUtils.equals(durableData.getDurableType(),GenericServiceProxy.getConstantMap().PHMask)))
				{
					throw new CustomException("MASK-0005");
				}
				if(StringUtils.isEmpty(sMaskUseCount)) {
					
					tempUsedCount = durableData.getTimeUsed();
					
				} else 
				{
					tempUsedCount = Double.valueOf(sMaskUseCount);

				}
				
				// Put data into UDF
				Map<String, String> udfs = new HashMap<String, String>();
				udfs.put("TRANSPORTSTATE", sTransportState);	
				
				// SetEvent Info create
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.setUdfs(udfs);

				// Excute greenTrack API call- setEvent 
				DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
				log.info("Excute  ScrapMask : "+sDurableName);
			
		}	
		return doc;
	}

}
