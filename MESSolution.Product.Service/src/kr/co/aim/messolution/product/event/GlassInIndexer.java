package kr.co.aim.messolution.product.event;

import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.VirtualGlass;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;

import org.jdom.Document;

public class GlassInIndexer extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		/*
		MACHINENAME 
		UNITNAME
		SUBUNITNAME
		LOTNAME
		PRODUCTNAME
		PRODUCTJUDGE
		PRODUCTGRADE
		FROMSLOTID
		TOSLOTID
		PORTNAME
		CARRIERNAME
		*/
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String productJudge = SMessageUtil.getBodyItemValue(doc, "PRODUCTJUDGE", false);
		String productGrade = SMessageUtil.getBodyItemValue(doc, "PRODUCTGRADE", false);
		String fromSlot = SMessageUtil.getBodyItemValue(doc, "FROMSLOTID", false);
		String toSlot = SMessageUtil.getBodyItemValue(doc, "TOSLOTID", false);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String lastGlassReport = SMessageUtil.getBodyItemValue(doc, "LASTGLASSREPORT", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("IndexerIn", getEventUser(), getEventComment(), null, null);
		
		String materialLocationInfo = StringUtil.EMPTY;
		if(StringUtil.isNotEmpty(subUnitName))
		{
		    materialLocationInfo = subUnitName;
		}
		else if(StringUtil.isNotEmpty(unitName))
		{
		    materialLocationInfo = unitName;
		}
		else
		{
		    materialLocationInfo = machineName;
		}
		
		try
		{
		    /* 20181227, hhlee, add, Machine validation ==>> */
		    Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(materialLocationInfo);
		    /* <<== 20181227, hhlee, add, Machine validation */
		    
		    /* 2018-11-20 jspark Add For AutoMQCSetting update LastRuntime */
			MESMachineServiceProxy.getMachineServiceImpl().updateAutoMQCSettingTimeRunInfo(machineName);
			/* 2018-11-20 jspark Add For AutoMQCSetting update LastRuntime */
			
			Product productData = ProductServiceProxy.getProductService().selectByKey(new ProductKey(productName));
			
			Map<String, String> udfs = CommonUtil.setNamedValueSequence(SMessageUtil.getBodyElement(doc), Product.class.getSimpleName());
			/* 20190116, hhlee, add, ProcessingUintName Column ==>> */
            udfs.put("PROCESSINGUNITNAME", StringUtil.EMPTY);           
            /* <<== 20190116, hhlee, add, ProcessingUintName Column */
			
			//SetMaterialLocationInfo setMaterialLocationInfo = MESProductServiceProxy.getProductInfoUtil().setMaterialLocationInfo(productData, StringUtil.isEmpty(unitName)? machineName:unitName, udfs);
			/* 20181227, hhlee, modify, materialLocationInfo unitName => machineName */
			//SetMaterialLocationInfo setMaterialLocationInfo = MESProductServiceProxy.getProductInfoUtil().setMaterialLocationInfo(productData, materialLocationInfo, udfs);
			/* 20181227, hhlee, modify, materialLocationInfo machineName => Empty */
			//SetMaterialLocationInfo setMaterialLocationInfo = MESProductServiceProxy.getProductInfoUtil().setMaterialLocationInfo(productData, machineName, udfs);
			SetMaterialLocationInfo setMaterialLocationInfo = MESProductServiceProxy.getProductInfoUtil().setMaterialLocationInfo(productData, StringUtil.EMPTY, udfs);
						
			MESProductServiceProxy.getProductServiceImpl().setMaterialLocation(eventInfo, productData, setMaterialLocationInfo);
			
			/* 20180912, hhlee, Add Product Location History Insert ==>> */
			// Deleted by smkang on 2018.10.08 - Because MaterialLocationName is recorded in Product table, CT_MATERIALLOCATIONHIST is unnecessary.
//            MESProductServiceProxy.getProductInfoUtil().setMaterialLocationHistory(eventInfo, lotName, productName, productData.getProductType(), productJudge, 
//                                                                                   productGrade, machineName, unitName, subUnitName, StringUtil.EMPTY, 
//                                                                                   productData.getCarrierName(), fromSlot, toSlot);

            /* <<== 20180912, hhlee, Add Product Location History Insert */
			
			// -----------------------------------------------------------------------------------------------------------------------------------------------------------
			// Added by smkang on 2018.11.15 - Update MachineIdleTime or MQCCondition.
			//								   According to Honewei's request, LastRunTime will be updated at GlassOutIndexer and GlassInIndexer time.
			try {
				/********** 2019.02.01_hsryu_Delete Logic ***********/
				//String condition = "SUPERMACHINENAME = ? AND DETAILMACHINETYPE = ?";
				//Object[] bindSet = new Object[] {machineName, "UNIT"};
				//List<MachineSpec> unitSpecList = MachineServiceProxy.getMachineSpecService().select(condition, bindSet);
				
				Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(productData.getLotName());

				//for (MachineSpec unitSpec : unitSpecList) {
				MESMachineServiceProxy.getMachineServiceImpl().updateMachineIdleTimeRunInfo(machineName, lotData, portName, machineData.getUdfs().get("OPERATIONMODE"), eventInfo);
				//}
			} catch (Exception e) {
				eventLog.warn(e);
				
				/********** 2019.02.01_hsryu_Delete Logic ***********/
				// Added by smkang on 2018.11.17 - Although a machine has no unit, updateMachineIdleTimeRunInfo should be invoked.
				//Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(productData.getLotName());
				//MESMachineServiceProxy.getMachineServiceImpl().updateMachineIdleTimeRunInfo(machineName, "", lotData, eventInfo);
			}
			// -----------------------------------------------------------------------------------------------------------------------------------------------------------
		}
		catch(NotFoundSignal ns)
		{
			MachineSpec macSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);
			
			if(StringUtil.equals(macSpecData.getUdfs().get("CONSTRUCTTYPE"), "UNPK"))
			{
				VirtualGlass vGlassData = ExtendedObjectProxy.getVirtualGlassService().selectByKey(false, new Object[] {productName});
				
				eventInfo.setEventName("IndexerIn");
				
				vGlassData.setLocation(unitName);
				vGlassData.setMachineName(machineName);
				/* 20181023, hhlee, add Virglass LotName ==>> */
				//vGlassData.setCrateName(carrierName);
				vGlassData.setCarrier(carrierName);
				vGlassData.setPosition(Long.parseLong(toSlot));
                /* <<== 20181023, hhlee, add Virglass LotName */
				
				ExtendedObjectProxy.getVirtualGlassService().modify(eventInfo, vGlassData);
			}
			else
			{
				throw ns;
			}
		}
		catch(Exception e)
		{			
			throw e;
		}
	}
}
