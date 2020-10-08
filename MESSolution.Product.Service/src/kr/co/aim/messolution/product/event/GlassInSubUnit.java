package kr.co.aim.messolution.product.event;

import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.VirtualGlass;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.product.management.data.Product;

import org.jdom.Document;

public class GlassInSubUnit extends AsyncHandler {

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
		*/
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String productType = SMessageUtil.getBodyItemValue(doc, "PRODUCTTYPE", false);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String productJudge = SMessageUtil.getBodyItemValue(doc, "PRODUCTJUDGE", false);
		String productGrade = SMessageUtil.getBodyItemValue(doc, "PRODUCTGRADE", false);
		String fromSlot = SMessageUtil.getBodyItemValue(doc, "FROMSLOTID", false);
		String toSlot = SMessageUtil.getBodyItemValue(doc, "TOSLOTID", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("SubUnitIn", getEventUser(), getEventComment(), null, null);
		
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
		    
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
			
			Map<String, String> udfs = CommonUtil.setNamedValueSequence(SMessageUtil.getBodyElement(doc), Product.class.getSimpleName());
			/* 20190116, hhlee, add, ProcessingUintName Column ==>> */
            udfs.put("PROCESSINGUNITNAME", subUnitName);           
            /* <<== 20190116, hhlee, add, ProcessingUintName Column */
            
			eventInfo.setEventName("SubUnitIn");			
			//SetMaterialLocationInfo setMaterialLocationInfo = MESProductServiceProxy.getProductInfoUtil().setMaterialLocationInfo(productData, subUnitName, udfs);
			SetMaterialLocationInfo setMaterialLocationInfo = MESProductServiceProxy.getProductInfoUtil().setMaterialLocationInfo(productData, materialLocationInfo, udfs);
			
			MESProductServiceProxy.getProductServiceImpl().setMaterialLocation(eventInfo, productData, setMaterialLocationInfo);
			
			/* 20180912, hhlee, Add Product Location History Insert ==>> */
			// Deleted by smkang on 2018.10.08 - Because MaterialLocationName is recorded in Product table, CT_MATERIALLOCATIONHIST is unnecessary.
            //MESProductServiceProxy.getProductInfoUtil().setMaterialLocationHistory(eventInfo, lotName, productName, productType, productJudge, 
            //                                                                         productGrade, machineName, unitName, subUnitName, StringUtil.EMPTY, 
            //                                                                         productData.getCarrierName(), fromSlot, toSlot);
			
            /* <<== 20180912, hhlee, Add Product Location History Insert */
			
			/* 20190219, hhlee, Add Virtual Glass Processed Operation Before AOI(Record SUBUNITNAME) ==>> */
            //MESProductServiceProxy.getProductServiceImpl().recordProcessedOperationOfSubUnit(eventInfo, productName, unitName, subUnitName);
			/* <<== 20190219, hhlee, Add Virtual Glass  Processed Operation Before AOI(Record SUBUNITNAME) */
		}
		catch(CustomException e)
		{
			if(e.errorDef.getErrorCode().equals("PRODUCT-9001"))
			{
				VirtualGlass vGlassData = ExtendedObjectProxy.getVirtualGlassService().selectByKey(false, new Object[] {productName});
				
				vGlassData.setLocation(subUnitName);
				vGlassData.setMachineName(machineName);
				
				/* 20190219, hhlee, Add Virtual Glass Processed Operation Before AOI(Record SUBUNITNAME) ==>> */
                //vGlassData = MESProductServiceProxy.getProductServiceImpl().recordVirtualGlassOfSubUnit(vGlassData, unitName, subUnitName);
                /* <<== 20190219, hhlee, Add Virtual Glass  Processed Operation Before AOI(Record SUBUNITNAME) */
				
				ExtendedObjectProxy.getVirtualGlassService().modify(eventInfo, vGlassData);
			}
			else
			{
				throw e;
			}
		}
	}
}
