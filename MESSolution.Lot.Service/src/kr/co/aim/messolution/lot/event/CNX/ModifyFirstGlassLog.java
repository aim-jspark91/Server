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

public class ModifyFirstGlassLog extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		String dataType = SMessageUtil.getBodyItemValue(doc, "DATATYPE", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String dataID = SMessageUtil.getBodyItemValue(doc, "DATAID", true);
		
		List<Element> dataList = SMessageUtil.getBodySequenceItemList(doc, "DATALIST", false);
		List<Element> commonDataList = SMessageUtil.getBodySequenceItemList(doc, "COMMONDATALIST", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		Map<String, String> dataMap = new HashMap<String, String>();
		
		for(Element eleComData : commonDataList)
		{
			String judge = SMessageUtil.getChildText(eleComData, "JUDGE", false);
			String operator = SMessageUtil.getChildText(eleComData, "OPERATOR", false);
//			String confirmor = SMessageUtil.getChildText(eleComData, "Confirmor", false);
			
			eventInfo.setEventName("Modify");
			
			String condition = " WHERE dataID = ? ";
			Object[] bindSet = new Object[]{dataID};
			
			List<FirstGlassLogM> firstGlassLogMList = ExtendedObjectProxy.getFirstGlassLogMService().select(condition, bindSet);
			
			FirstGlassLogM firstGlassLogMData = firstGlassLogMList.get(0);
			//firstGlassLogMData.setDataType(dataType);
			//firstGlassLogMData.setDataId(dataID);
			//firstGlassLogMData.setTimeKey(eventInfo.getEventTimeKey());
			//firstGlassLogMData.setProcessFlowName(lotData.getProcessFlowName());
			//firstGlassLogMData.setCreateTime(eventInfo.getEventTime().toString());
			//firstGlassLogMData.setCreateUser(eventInfo.getEventUser());
			//firstGlassLogMData.setLotName(lotData.getKey().getLotName());
			//firstGlassLogMData.setProductSpecName(lotData.getProductSpecName());
			firstGlassLogMData.setJudge(judge);
			firstGlassLogMData.setOperator(operator);
//			firstGlassLogMData.setConfirmor(confirmor);
			firstGlassLogMData.setLastEventComment(eventInfo.getEventComment());
			firstGlassLogMData.setLastEventTime(eventInfo.getEventTime());
			firstGlassLogMData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			firstGlassLogMData.setLastEventName(eventInfo.getEventName());
			
			ExtendedObjectProxy.getFirstGlassLogMService().update(firstGlassLogMData);
		}
		
		for(Element eleData : dataList)
		{
			eventInfo.setEventName("Modify");
			
			for(int i=0; i<eleData.getChildren().size(); i++)
			{
				Element e = (Element)eleData.getChildren().get(i);
				String colName = e.getName();
				String value = SMessageUtil.getChildText(eleData, colName, false);
				
				try
				{
					Object[] keySet = new Object[]{dataType, dataID, colName};
					FirstGlassLogDetail firstGlassLogDetailData = ExtendedObjectProxy.getFirstGlassLogDetailService().selectByKey(false, keySet);
					
					firstGlassLogDetailData.setValue(value);
					ExtendedObjectProxy.getFirstGlassLogDetailService().update(firstGlassLogDetailData);
				}
				catch(Exception ex)
				{
					eventLog.info("Not Found FirstGlassLogDetail Data");
					FirstGlassLogDetail firstGlassLogDetailData = new FirstGlassLogDetail();
					firstGlassLogDetailData.setDataType(dataType);
					firstGlassLogDetailData.setDataId(dataID);
					firstGlassLogDetailData.setKey(colName);
					firstGlassLogDetailData.setValue(value);
					
					ExtendedObjectProxy.getFirstGlassLogDetailService().create(eventInfo, firstGlassLogDetailData);
				}
			}
		}
		
		return doc;
	}

}
