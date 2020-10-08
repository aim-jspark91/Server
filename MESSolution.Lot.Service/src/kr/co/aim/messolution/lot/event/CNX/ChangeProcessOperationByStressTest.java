package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.extended.object.management.data.SampleProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ChangeProcessOperationByStressTest extends SyncHandler
{

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String targetOperationName = SMessageUtil.getBodyItemValue(doc, "NEWPROCESSOPERATIONNAME", true);
		// String nodeStack = SMessageUtil.getBodyItemValue(doc, "NODESTACK", true);
		String sFactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
				
		if (StringUtil.equals(lotData.getLotProcessState(), "RUN"))
			throw new CustomException("LOT-0008", lotName);

		//20170206 zhongsl validate targetOperationName dismatched with processOperationName
		if(lotData.getProcessOperationName().equals(targetOperationName))
		{
			throw new CustomException("LOT-0217", lotName);
		}

		// wzm 2016/11/4 add,OnLine状态下，CST必须在STK上才可以进行changeProcessOperation操作
		if (!StringUtils.equals(sFactoryName, "OLED")) 
		{
			CommonValidation.checkCarrierLocation(lotData);
		}
						
		// For ChangeOperation Exit QTime
		//ExtendedObjectProxy.getQTimeService().exitAllQTimeData(eventInfo, lotData);

		ProcessOperationSpec processOperationSpecData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), targetOperationName);

		//20170321 Add by yudan
		//String firstglassJobName = ExtendedObjectProxy.getFirstGlassLotService().getActiveJobNameByLotName(lotData.getKey().getLotName());
		
		if (StringUtil.equals(processOperationSpecData.getProcessOperationType(), "Inspection"))
		{
//			if(firstglassJobName.isEmpty())
//			{
//				eventInfo.setEventName("Reserve");
//				this.setInspectionData(eventInfo, lotData, targetOperationName);
//			}		
		}

		eventInfo.setEventName("ChangeOper");
		Map<String, String> lotUdfs = lotData.getUdfs();
		List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotName);
		
		//Operation Changed, Update Product ProcessingInfo to N
		productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productUdfs, "");

		String areaName = lotData.getAreaName();
		String factoryName = lotData.getFactoryName();
		String lotHoldState = lotData.getLotHoldState();
		String lotProcessState = lotData.getLotProcessState();
		String lotState = lotData.getLotState();
		String processFlowName = lotData.getProcessFlowName();
		String processFlowVersion = lotData.getProcessFlowVersion();
		String processOperationName = lotData.getProcessOperationName();
		String processOperationVersion = lotData.getProcessOperationVersion();
		String productionType = lotData.getProductionType();
		String productRequestName = lotData.getProductRequestName();
		String productSpec2Name = lotData.getProductSpec2Name();
		String productSpec2Version = lotData.getProductSpec2Version();
		String productSpecName = lotData.getProductSpecName();
		String productSpecVersion = lotData.getProductSpecVersion();
		long priority = lotData.getPriority();
		Timestamp dueDate = lotData.getDueDate();
		double subProductUnitQuantity1 = lotData.getSubProductUnitQuantity1();
		double subProductUnitQuantity2 = lotData.getSubProductUnitQuantity2();

		// 151228 by swcho : only single interval tracing from flow to flow
		//2018.11.01 Modify, hsryu, lotData.getProductRequestName -> "", Because WorkOrder of Product must not be changed.
		ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotName, productionType, productSpecName,
				productSpecVersion, productSpec2Name, productSpec2Version, "", subProductUnitQuantity1, subProductUnitQuantity2,
				dueDate, priority, factoryName, areaName, "Released", "WAIT", "N", processFlowName, processFlowVersion,
				processOperationName, processOperationVersion, processFlowName, targetOperationName, "", "", lotData.getNodeStack(), lotUdfs,
				productUdfs, true);

		lotData.setLotState("Released");
		lotData.setLotProcessState("WAIT");
		lotData.setLotHoldState("N");
		
		
		LotServiceProxy.getLotService().update(lotData);
		
		lotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
		
		return doc;
	}

	private void setInspectionData(EventInfo eventInfo, Lot lotData, String targetOperationName) throws CustomException
	{
		List<SampleLot> sampleLotList = null;
		try
		{
			String condition = " WHERE lotName = ? AND factoryName = ? AND productSpecName = ? AND processFlowName = ?"
					+ " AND toProcessOperationName = ? AND lotSampleFlag = ? ";
			Object[] bindSet = new Object[] { lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
					lotData.getProcessFlowName(), targetOperationName, "Y" };
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception ex)
		{
			eventLog.info("Not Found SampleLotData");
		}

		if (sampleLotList == null)
		{
			List<String> actualSamplePositionList = new ArrayList<String>();
			List<Product> allUnScrappedProductList = ProductServiceProxy.getProductService()
					.allUnScrappedProductsByLot(lotData.getKey().getLotName());

			for (Product productData : allUnScrappedProductList)
			{
				List<SampleProduct> sampleProductList = null;
				try
				{
					String condition = " WHERE lotName = ? AND productName = ? AND factoryName = ? AND productSpecName = ? AND processFlowName = ?"
							+ " AND toProcessOperationName = ? AND productSampleFlag = ? ";
					Object[] bindSet = new Object[] { lotData.getKey().getLotName(),productData.getKey().getProductName(), lotData.getFactoryName(), lotData.getProductSpecName(),
							lotData.getProcessFlowName(), targetOperationName, "Y" };
					sampleProductList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);
				}
				catch (Exception ex)
				{
					eventLog.info("Not Found SampleProductData");
				}

				if (sampleProductList == null)
				{
					ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, productData.getKey().getProductName(),
							lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(),
							targetOperationName, "NA", targetOperationName, "Y", "", "", String.valueOf(productData.getPosition()), "Y");

					// Add ProductHistory
					kr.co.aim.greentrack.product.management.info.SetEventInfo setPEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
					setPEventInfo.setUdfs(productData.getUdfs());

					MESProductServiceProxy.getProductServiceImpl().setEvent(productData, setPEventInfo, eventInfo);

					actualSamplePositionList.add(String.valueOf(productData.getPosition()));
				}
			}

			ExtendedObjectProxy.getSampleLotService().insertSampleLot(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(),
					lotData.getProductSpecName(), lotData.getProcessFlowName(), targetOperationName, "NA", targetOperationName, "Y", "", "", "", "",
					"", String.valueOf(actualSamplePositionList.size()), CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "Y");

			// Add LotHistory
			LotKey lotKey = new LotKey(lotData.getKey().getLotName());
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.setUdfs(lotData.getUdfs());

			LotServiceProxy.getLotService().setEvent(lotKey, eventInfo, setEventInfo);

		}
		return;
	}
}
