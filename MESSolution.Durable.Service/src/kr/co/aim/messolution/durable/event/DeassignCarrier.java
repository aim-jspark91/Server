package kr.co.aim.messolution.durable.event;

import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.durable.management.data.DurableHistoryKey;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class DeassignCarrier extends SyncHandler {

	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
 		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeassignCarrier", getEventUser(), getEventComment(), "", "");
		
		String sFactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String sDurableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		
		Element eleBody = SMessageUtil.getBodyElement(doc);
		
		//GetDurableData
		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(sDurableName);
		
		Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
		
		//2019.01.08_hsryu_Check SortJob!
		CommonValidation.checkExistSortJob(durableData.getFactoryName(), durableData.getKey().getDurableName());
		
		if(eleBody!=null)
		{
			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", false))
			{
				String sLotName = SMessageUtil.getChildText(eledur, "LOTNAME", true);
				
				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(sLotName);
				
				//160524 by swcho : validation
				/**2018.12.21_hsryu_Delete Logic. Requested by CIM **/
//				CommonValidation.checkLotHoldState(lotData);
				//CommonValidation.checkLotProcessState(lotData);
				
				if (!StringUtils.isEmpty(lotData.getProcessGroupName())) 
				{
					throw new CustomException("LOT-0116",sLotName);
				}
				
				List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
				
				DeassignCarrierInfo deassignInfo =  MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, 
																										  durableData, 
																										  productUSequence); 
				
				MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, deassignInfo, eventInfo);
				
				try
				{
					DurableHistoryKey DurableHistory = new DurableHistoryKey();
					DurableHistory.setDurableName(sDurableName);
					DurableHistory.setTimeKey(eventInfo.getEventTimeKey());
					
					// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					DurableHistory durableHistory = DurableServiceProxy.getDurableHistoryService().selectByKey(DurableHistory);
					DurableHistory durableHistory = DurableServiceProxy.getDurableHistoryService().selectByKeyForUpdate(DurableHistory);
					
					durableHistory.setConsumerLotName("");
					DurableServiceProxy.getDurableHistoryService().update(durableHistory);
				}
				catch(Throwable e)
				{
					eventLog.error("Insert DurableHistory fail.");
				}
			}
		}

		return doc;
	}

}
