package kr.co.aim.messolution.product.event;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.VirtualGlass;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
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

import org.apache.commons.collections.map.ListOrderedMap;
import org.jdom.Document;
import org.jdom.Element;

public class GlassInUnit extends AsyncHandler {

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
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String productType = SMessageUtil.getBodyItemValue(doc, "PRODUCTTYPE", false);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String productJudge = SMessageUtil.getBodyItemValue(doc, "PRODUCTJUDGE", false);
		String productGrade = SMessageUtil.getBodyItemValue(doc, "PRODUCTGRADE", false);
		String fromSlot = SMessageUtil.getBodyItemValue(doc, "FROMSLOTID", false);
		String toSlot = SMessageUtil.getBodyItemValue(doc, "TOSLOTID", false);
		
		/* 20181027, hhlee, add , ==>> */
		/* 20180922, hhlee, Add Processed Operation Before AOI(Record UNIT/RECIPE) ==>> */
        String unitRecipeName = SMessageUtil.getBodyItemValue(doc, "UNITRECIPENAME", false);
        /* <<== 20180922, hhlee, Add Processed Operation Before AOI(Record UNIT/RECIPE) */
        /* <<== 20181027, hhlee, add , */
        
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnitIn", getEventUser(), getEventComment(), null, null);
		
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
			// Start 2019.09.10 Move By Park Jeong Su For DeadLock with LotProcessStart
			
            /* <<== 20180912, hhlee, Add Product Location History Insert */
			
			/* 20181027, hhlee, add , ==>> */
			/* 20180922, hhlee, Add Processed Operation Before AOI(Record UNIT/RECIPE) ==>> */
            MESProductServiceProxy.getProductServiceImpl().recordProcessedOperationOfUnit(eventInfo, productName, unitName, unitRecipeName);
            /* <<== 20180922, hhlee, Add Processed Operation Before AOI(Record UNIT/RECIPE) */
            /* <<== 20181027, hhlee, add , */
            // End 2019.09.10 Move By Park Jeong Su For DeadLock with LotProcessStart
			
		    /* 20181227, hhlee, add, Machine validation ==>> */
            Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(materialLocationInfo);
            /* <<== 20181227, hhlee, add, Machine validation */
            
            Product productData = ProductServiceProxy.getProductService().selectByKey(new ProductKey(productName));
			
			Map<String, String> udfs = CommonUtil.setNamedValueSequence(SMessageUtil.getBodyElement(doc), Product.class.getSimpleName());
			/* 20190116, hhlee, add, ProcessingUintName Column ==>> */
			udfs.put("PROCESSINGUNITNAME", unitName);			
			/* <<== 20190116, hhlee, add, ProcessingUintName Column */
			
			eventInfo.setEventName("UnitIn");	
			//SetMaterialLocationInfo setMaterialLocationInfo = MESProductServiceProxy.getProductInfoUtil().setMaterialLocationInfo(productData, unitName, udfs);
			SetMaterialLocationInfo setMaterialLocationInfo = MESProductServiceProxy.getProductInfoUtil().setMaterialLocationInfo(productData, materialLocationInfo, udfs);
			
			MESProductServiceProxy.getProductServiceImpl().setMaterialLocation(eventInfo, productData, setMaterialLocationInfo);
			
						
			/* 20180912, hhlee, Add Product Location History Insert ==>> */
			// Deleted by smkang on 2018.10.08 - Because MaterialLocationName is recorded in Product table, CT_MATERIALLOCATIONHIST is unnecessary.
            //MESProductServiceProxy.getProductInfoUtil().setMaterialLocationHistory(eventInfo, lotName, productName, productType, productJudge, 
            //                                                                       productGrade, machineName, unitName, subUnitName, StringUtil.EMPTY, 
            //                                                                       productData.getCarrierName(), fromSlot, toSlot);


		}
		catch(NotFoundSignal ns)
		{
			MachineSpec macSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);
			
			if(StringUtil.equals(macSpecData.getUdfs().get("CONSTRUCTTYPE"), "UNPK"))
			{
				eventInfo.setEventName("UnitIn");
				
				VirtualGlass vGlassData = ExtendedObjectProxy.getVirtualGlassService().selectByKey(false, new Object[] {productName});
				
				vGlassData.setLocation(unitName);
				
				/* 20181027, hhlee, add , ==>> */
				/* 20180922, hhlee, Add Virtual Glass Processed Operation Before AOI(Record UNIT/RECIPE) ==>> */
                vGlassData = MESProductServiceProxy.getProductServiceImpl().recordVirtualGlassOfUnit(vGlassData, unitName, unitRecipeName);
                /* <<== 20180922, hhlee, Add Virtual Glass  Processed Operation Before AOI(Record UNIT/RECIPE) */
                /* <<== 20181027, hhlee, add , */
                
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
