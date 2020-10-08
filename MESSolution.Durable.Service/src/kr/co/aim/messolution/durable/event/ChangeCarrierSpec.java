package kr.co.aim.messolution.durable.event;

import java.util.HashMap;
import java.util.List;
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
import kr.co.aim.greentrack.durable.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;
import org.jdom.Element;

public class ChangeCarrierSpec extends SyncHandler {

	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{		
		String originalFactoryName = SMessageUtil.getBodyItemValue(doc,"ORIGINALFACTORYNAME", true);
		String durableSpecName = SMessageUtil.getBodyItemValue(doc,"DURABLESPECNAME", true);
				
		List<Element> eleList = SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", false);
		
		EventInfo eventInfo = 
				EventInfoUtil.makeEventInfo("ChangeCarrierSpec", getEventUser(), getEventComment(), "", "");
		
		if(eleList!=null)
		{
			// Added by smkang on 2018.10.02 - According to EDO's request, carrier data should be synchronized with shared factory.
			//								   Before DurableSpec is changed, this message should be published for getting SHAREDFACTORY.
	/*		if (eleList.size() > 0)
				MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(doc, eleList.get(0).getChildText("DURABLENAME"));*/
			
			for(Element eleCarrier : eleList)
			{
				String DurableName = SMessageUtil.getChildText(eleCarrier, "DURABLENAME", true);
				String timeUsed = SMessageUtil.getChildText(eleCarrier,"TIMEUSED", false);
				String durationUsed = SMessageUtil.getChildText(eleCarrier,"DURATIONUSED", false);
				String lotQuantity = SMessageUtil.getChildText(eleCarrier,"LOTQUANTITY", false);
				
				DurableKey durableKey = new DurableKey();
				durableKey.setDurableName(DurableName);

				Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
				
				if(StringUtil.equals(durableData.getDurableState(), "InUse"))
					throw new CustomException("CST-0006", DurableName);
											
				Map<String, String> durableSpecInfo = this.DurableSpecInfo(originalFactoryName,durableSpecName);
											
				String newDurableSpecName = durableSpecInfo.get("DURABLESPECNAME");
				String newSpecVersion = durableSpecInfo.get("DURABLESPECVERSION");
				String timeUsedLimit = durableSpecInfo.get("TIMEUSEDLIMIT");
				String durationUsedLimit = durableSpecInfo.get("DURATIONUSEDLIMIT");
				String capacity = durableSpecInfo.get("DEFAULTCAPACITY");
													
				ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();

				changeSpecInfo.setFactoryName(originalFactoryName);
				changeSpecInfo.setCapacity(Long.valueOf(capacity));
				changeSpecInfo.setDurableSpecName(newDurableSpecName);
				changeSpecInfo.setDurableSpecVersion(newSpecVersion);
				changeSpecInfo.setDurationUsedLimit(Double.valueOf(durationUsedLimit));
				changeSpecInfo.setTimeUsedLimit(Double.valueOf(timeUsedLimit));			

				Map<String, String> durableUdfs = durableData.getUdfs();
				durableUdfs.put("ORIGINALFACTORYNAME", originalFactoryName);
				
				changeSpecInfo.setUdfs(durableUdfs);
				
				durableData.setTimeUsed(Double.valueOf(timeUsed));
				durableData.setDurationUsed(Double.valueOf(durationUsed));
				durableData.setLotQuantity(Long.valueOf(lotQuantity));		
				
				Durable resultData = DurableServiceProxy.getDurableService().changeSpec(durableKey, eventInfo, changeSpecInfo);								
				
			}
		}

		return doc;
	}

	private Map<String, String> DurableSpecInfo(String factoryName,String durableSpecName)throws CustomException 
	{
		String sql = "SELECT FACTORYNAME,DURABLESPECNAME,DURABLESPECVERSION,TIMEUSEDLIMIT,DURATIONUSEDLIMIT,DEFAULTCAPACITY FROM DURABLESPEC  "
				+ "WHERE FACTORYNAME = :factoryName AND DURABLESPECNAME = :durableSpecName AND DURABLESPECVERSION = :durableSpecVersion ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("factoryName", factoryName);
		bindMap.put("durableSpecName", durableSpecName);
		bindMap.put("durableSpecVersion", "00001");

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		Map<String, String> udfs = new HashMap<String, String>();

		if (sqlResult.size() > 0) 
		{
			udfs.put("FACTORYNAME", sqlResult.get(0).get("FACTORYNAME").toString());			
			udfs.put("DURABLESPECNAME",sqlResult.get(0).get("DURABLESPECNAME").toString());
			udfs.put("DURABLESPECVERSION",sqlResult.get(0).get("DURABLESPECVERSION").toString());
			udfs.put("TIMEUSEDLIMIT",sqlResult.get(0).get("TIMEUSEDLIMIT").toString());
			udfs.put("DURATIONUSEDLIMIT",sqlResult.get(0).get("DURATIONUSEDLIMIT").toString());
			udfs.put("DEFAULTCAPACITY",sqlResult.get(0).get("DEFAULTCAPACITY").toString());						
		} 
		
		else 
		{			
			throw new CustomException("Not find Data");				
		}

		return udfs;
	}
	
}
