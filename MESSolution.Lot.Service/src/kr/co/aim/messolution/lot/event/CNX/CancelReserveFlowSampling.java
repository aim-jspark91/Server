package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.FlowSampleLot;
import kr.co.aim.messolution.extended.object.management.data.FlowSampleProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.lot.service.LotServiceImpl;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class CancelReserveFlowSampling extends SyncHandler {
	private static Log log = LogFactory.getLog(LotServiceImpl.class);
	
	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		//for common 
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelFlowReserveSample", getEventUser(), getEventComment(), null, null);
		
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		
		List<Element> eleSampleLotLsit = SMessageUtil.getBodySequenceItemList(doc, "SAMPLELOTLIST", true);
		
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		for (Element eleSampleLotData : eleSampleLotLsit)
		{
			String productSpecName = SMessageUtil.getChildText(eleSampleLotData, "PRODUCTSPECNAME", true);
			String processFlowName = SMessageUtil.getChildText(eleSampleLotData, "PROCESSFLOWNAME", true);
			String manualSampleFlag = SMessageUtil.getChildText(eleSampleLotData, "MANUALSAMPLEFLAG", false);
			String processOperationName = SMessageUtil.getChildText(eleSampleLotData, "PROCESSOPERATIONNAME", true);
			String machineName = SMessageUtil.getChildText(eleSampleLotData, "MACHINENAME", true);
			String toProcessOperationName = SMessageUtil.getChildText(eleSampleLotData, "TOPROCESSOPERATIONNAME", true);
			
			String condition = " WHERE lotName = ? AND factoryName = ? AND productSpecName = ? AND processFlowName = ?"
					+ " AND processOperationName = ? AND machineName = ? AND toProcessOperationName = ? AND productSampleFlag = ? ";
			
			Object[] bindSet = null;			
			if(manualSampleFlag.isEmpty())
			{
				condition += " AND manualSampleFlag IS NULL ";
				bindSet = new Object[]{lotData.getKey().getLotName(), factoryName, productSpecName, processFlowName,
									processOperationName, machineName, toProcessOperationName, "Y"};
			}
			else
			{
				condition += " AND manualSampleFlag = ? ";
				bindSet = new Object[]{lotData.getKey().getLotName(), factoryName, productSpecName, processFlowName,
									processOperationName, machineName, toProcessOperationName, "Y", "Y"};				
			}
			
			List<FlowSampleProduct> sampleProductList = new ArrayList<FlowSampleProduct>();
			try
			{
				sampleProductList = ExtendedObjectProxy.getFlowSampleProductService().select(condition, bindSet);
			}
			catch(Exception ex)
			{
			}
			
			for(FlowSampleProduct sampleProductData : sampleProductList)
			{
				Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(sampleProductData.getPRODUCTNAME());
				
				ExtendedObjectProxy.getFlowSampleProductService().remove(eventInfo, sampleProductData);
				
				// Add ProductHistory
				kr.co.aim.greentrack.product.management.info.SetEventInfo setPEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
				setPEventInfo.setUdfs(productData.getUdfs());
				
				MESProductServiceProxy.getProductServiceImpl().setEvent(productData, setPEventInfo, eventInfo);
			}
			
			condition = " WHERE 1=1 AND lotName = ? AND factoryName = ? AND productSpecName = ? AND processFlowName = ? "
					+ " AND processOperationName = ? AND machineName = ? AND toProcessOperationName = ? AND lotSampleFlag = ? ";
			
			if(manualSampleFlag.isEmpty())
			{
				condition += " AND manualSampleFlag IS NULL ";
				bindSet = new Object[]{lotData.getKey().getLotName(), factoryName, productSpecName, processFlowName, processOperationName, machineName, toProcessOperationName, "Y"};
			}
			else
			{
				condition += " AND manualSampleFlag = ? ";
				bindSet = new Object[]{lotData.getKey().getLotName(), factoryName, productSpecName, processFlowName, processOperationName, machineName, toProcessOperationName, "Y", "Y"};
			}
			
			List<FlowSampleLot> sampleLotList = ExtendedObjectProxy.getFlowSampleLotService().select(condition, bindSet);
			
			for(FlowSampleLot sampleLotData : sampleLotList)
			{
				ExtendedObjectProxy.getFlowSampleLotService().remove(eventInfo, sampleLotData);
				
				// Add LotHistory
				LotKey lotKey = new LotKey(lotData.getKey().getLotName());
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.setUdfs(lotData.getUdfs());
				
				LotServiceProxy.getLotService().setEvent(lotKey, eventInfo, setEventInfo);
			}
		}
		 
		return doc;
	}
}