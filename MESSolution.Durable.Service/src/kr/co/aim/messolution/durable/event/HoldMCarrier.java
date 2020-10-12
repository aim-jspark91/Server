package kr.co.aim.messolution.durable.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class HoldMCarrier extends SyncHandler {

	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		String sDurableHoldState = SMessageUtil.getBodyItemValue(doc,"DURABLEHOLDSTATE", true);
		String sReasonCodeType = SMessageUtil.getBodyItemValue(doc,"REASONCODETYPE", false);
		String sReasonCode = SMessageUtil.getBodyItemValue(doc,"REASONCODE", false);
		
		Element eDurableList = SMessageUtil.getBodySequenceItem(doc, "DURABLELIST", true);
		
		for (@SuppressWarnings("rawtypes")
		Iterator iDurable = eDurableList.getChildren().iterator(); iDurable.hasNext();)
		{
			Element eDurable = (Element) iDurable.next();
			String sDurableName = SMessageUtil.getChildText(eDurable, "DURABLENAME", true);
		DurableKey durableKey = new DurableKey();

		durableKey.setDurableName(sDurableName);

		Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
		
		if(!StringUtils.equals(durableData.getFactoryName(), System.getProperty("shop")))
		{
			if(!StringUtil.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_Available))
			{
				throw new CustomException("CST-0039", sDurableName, durableData.getFactoryName());
			}
		}
		
		if(StringUtil.equals(durableData.getUdfs().get("DURABLEHOLDSTATE"), "Y"))
			throw new CustomException("CST-0005", sDurableName);
	
		SetEventInfo setEventInfo = new SetEventInfo();
		
		// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//		Map<String, String> durableUdfs = durableData.getUdfs();
		Map<String, String> durableUdfs = setEventInfo.getUdfs();
		
		// 멘티스 요구사항
		// EventComment 를 Durable 의 Note 컬럼에 기록
		durableUdfs.put("DURABLEHOLDSTATE", sDurableHoldState);
		durableUdfs.put("NOTE", getEventComment());
		setEventInfo.setUdfs(durableUdfs);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Hold", getEventUser(),"", sReasonCodeType, sReasonCode);
		
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
		
		// Added by smkang on 2018.10.02 - According to EDO's request, carrier data should be synchronized with shared factory.
		//MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(doc, sDurableName);
		
		// Modified by smkang on 2019.05.28 - DurableServiceProxy.getDurableService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//		durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableData.getKey().getDurableName());
//
//		// For Clear Note, Add By Park Jeong Su
//		durableData.getUdfs().put("NOTE", "");
//		DurableServiceProxy.getDurableService().update(durableData);
		Map<String, String> updateUdfs = new HashMap<String, String>();
		updateUdfs.put("NOTE", "");
		MESDurableServiceProxy.getDurableServiceImpl().updateDurableWithoutHistory(durableData, updateUdfs);

		}
		return doc;
	}

}
