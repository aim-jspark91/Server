package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.FirstGlassLogDetail;
import kr.co.aim.messolution.extended.object.management.data.FirstGlassLogM;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.jdom.Document;
import org.jdom.Element;

public class CreateFirstGlassLog extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub		
		String dataType = SMessageUtil.getBodyItemValue(doc, "DATATYPE", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		
		List<Element> dataList = SMessageUtil.getBodySequenceItemList(doc, "DATALIST", false);
		List<Element> commonDataList = SMessageUtil.getBodySequenceItemList(doc, "COMMONDATALIST", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		String dataID = TimeUtils.getCurrentTime("yyyyMMddHHmmss");
		
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		Map<String, String> dataMap = new HashMap<String, String>();
		
		for(Element eleComData : commonDataList)
		{
			String judge = SMessageUtil.getChildText(eleComData, "JUDGE", false);
			String operator = SMessageUtil.getChildText(eleComData, "OPERATOR", false);
//			String confirmor = SMessageUtil.getChildText(eleComData, "Confirmor", false);
			
			eventInfo.setEventName("Create");
			
			// insert MasterData
			
			FirstGlassLogM firstGlassLogMData = new FirstGlassLogM();
			firstGlassLogMData.setDataType(dataType);
			firstGlassLogMData.setDataId(dataID);
			firstGlassLogMData.setTimeKey(eventInfo.getEventTimeKey());
			firstGlassLogMData.setCreateTime(eventInfo.getEventTime().toString());
			firstGlassLogMData.setCreateUser(eventInfo.getEventUser());
			firstGlassLogMData.setLotName(lotData.getKey().getLotName());
			firstGlassLogMData.setJudge(judge);
			firstGlassLogMData.setOperator(operator);
//			firstGlassLogMData.setConfirmor(confirmor);
			firstGlassLogMData.setLastEventComment(eventInfo.getEventComment());
			firstGlassLogMData.setLastEventTime(eventInfo.getEventTime());
			firstGlassLogMData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			firstGlassLogMData.setLastEventName(eventInfo.getEventName());
			
			ExtendedObjectProxy.getFirstGlassLogMService().create(eventInfo, firstGlassLogMData);
		}
		
		for(Element eleData : dataList)
		{
			FirstGlassLogDetail firstGlassLogDetailData = new FirstGlassLogDetail();
			firstGlassLogDetailData.setDataId(dataID);
			firstGlassLogDetailData.setDataType(dataType);
			eventInfo.setEventName("Create");
			
			for(int i=0; i<eleData.getChildren().size(); i++)
			{
				Element e = (Element)eleData.getChildren().get(i);
				String colName = e.getName();
				String value = SMessageUtil.getChildText(eleData, colName, false);
				
				firstGlassLogDetailData.setKey(colName);
				firstGlassLogDetailData.setValue(value);
				ExtendedObjectProxy.getFirstGlassLogDetailService().create(eventInfo, firstGlassLogDetailData);
			}
		}
		
		return doc;
	}

}
