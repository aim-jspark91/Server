package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.CreateInfo;
import kr.co.aim.greentrack.product.management.data.ProductSpec;

import org.jdom.Document;
import org.jdom.Element;

public class CreateLotByStressTest extends SyncHandler {

	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		String sFactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String sDueDate = SMessageUtil.getBodyItemValue(doc, "DUEDATE", true);
		String sPriority = SMessageUtil.getBodyItemValue(doc, "PRIORITY", true);
		String sProductSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String sProductQuantity = SMessageUtil.getBodyItemValue(doc, "PRODUCTQUANTITY", true);
		
		//convert
		Timestamp tDueDate = TimeUtils.getTimestamp(sDueDate);
		long nPriority = Long.parseLong(sPriority);
		
		//Product base info
		ProductSpec baseData = GenericServiceProxy.getSpecUtil().getProductSpec(sFactoryName, sProductSpecName,
									GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
		
		String sProcessFlowName = baseData.getProcessFlowName();
		
		Element eLotList = SMessageUtil.getBodySequenceItem(doc, "LOTLIST", true);
		Map<String, String> udfs = baseData.getUdfs();
		udfs.put("ECCODE", "A4");
		
		for (@SuppressWarnings("rawtypes")
		Iterator itLot = eLotList.getChildren().iterator(); itLot.hasNext();)
		{
			Element eLot = (Element) itLot.next();
			String sLotName = SMessageUtil.getChildText(eLot, "LOTNAME", true);
			
			CreateInfo createInfo =  MESLotServiceProxy.getLotInfoUtil().createInfo(tDueDate,
										baseData.getKey().getFactoryName(),
										sLotName, "",
										nPriority,
										sProcessFlowName,
										GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION,
										"",
										"", "",
										baseData.getProductionType(), ConvertUtil.toDecimal(baseData.getProductQuantity(), sProductQuantity),
										"", "", "",
										baseData.getKey().getProductSpecName(), baseData.getKey().getProductSpecVersion(),
										baseData.getProductType(), baseData.getSubProductType(), baseData.getSubProductUnitQuantity1(),
										0,
										udfs);
			
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), "", "");
			
			Lot newLot = MESLotServiceProxy.getLotServiceImpl().createLot(eventInfo, createInfo);
		}
		
		return doc;
	}
}
