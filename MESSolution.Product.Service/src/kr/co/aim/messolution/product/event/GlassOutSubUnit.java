package kr.co.aim.messolution.product.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.VirtualGlass;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.product.service.ProductServiceImpl;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;

import org.jdom.Document;

public class GlassOutSubUnit extends AsyncHandler {

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
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String productType = SMessageUtil.getBodyItemValue(doc, "PRODUCTTYPE", false);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String productJudge = SMessageUtil.getBodyItemValue(doc, "PRODUCTJUDGE", false);
		String productGrade = SMessageUtil.getBodyItemValue(doc, "PRODUCTGRADE", false);
		String fromSlot = SMessageUtil.getBodyItemValue(doc, "FROMSLOTID", false);
		String toSlot = SMessageUtil.getBodyItemValue(doc, "TOSLOTID", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("SubUnitOut", getEventUser(), getEventComment(), null, null);
		
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
			
			/* 20190617, hhlee, delete, MaterialLocationName is not update(GlassOutSubUnit) ==>> */
			//Map<String, String> udfs = CommonUtil.setNamedValueSequence(SMessageUtil.getBodyElement(doc), Product.class.getSimpleName());
			///* 20190116, hhlee, add, ProcessingUintName Column ==>> */
            //udfs.put("PROCESSINGUNITNAME", subUnitName);           
            ///* <<== 20190116, hhlee, add, ProcessingUintName Column */
            //
			//eventInfo.setEventName("SubUnitOut");
			////SetMaterialLocationInfo setMaterialLocationInfo = MESProductServiceProxy.getProductInfoUtil().setMaterialLocationInfo(productData, subUnitName, udfs);
			///* 20181227, hhlee, add, update materialLocation unitName ==>> */
			////SetMaterialLocationInfo setMaterialLocationInfo = MESProductServiceProxy.getProductInfoUtil().setMaterialLocationInfo(productData, materialLocationInfo, udfs);
			//SetMaterialLocationInfo setMaterialLocationInfo = MESProductServiceProxy.getProductInfoUtil().setMaterialLocationInfo(productData, unitName, udfs);
			///* <<== 20181227, hhlee, add, update materialLocation unitName */
			//
			//MESProductServiceProxy.getProductServiceImpl().setMaterialLocation(eventInfo, productData, setMaterialLocationInfo);
			/* <<== 20190617, hhlee, delete, MaterialLocationName is not update(GlassOutSubUnit) */
			
			/* 20190617, hhlee, modify, MaterialLocationName is not update(GlassOutSubUnit) ==>> */
            kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
            
            // 2019.06.21_hsryu_Change setUdfs.  save Memory.
//            Map<String, String> udfs = new HashMap<String, String>();
//            udfs.put("PROCESSINGUNITNAME", subUnitName);  
//            setEventInfo.setUdfs(udfs);         
            setEventInfo.getUdfs().put("PROCESSINGUNITNAME", subUnitName);
            
            eventInfo.setEventName("SubUnitOut");
            MESProductServiceProxy.getProductServiceImpl().setEvent(productData, setEventInfo, eventInfo);              
            /* <<== 20190617, hhlee, modify, MaterialLocationName is not update(GlassOutSubUnit) */
            
            //add insertProductProcLocationHist for SPC
            ProductServiceImpl.insertProductProcLocationHist(eventInfo, productName, machineName, subUnitName);
			
			
			//deleted by wghuang 20190513, old logic
/*			if(StringUtil.isNotEmpty(productGrade) && !StringUtil.equals(productData.getProductGrade(), productGrade))
			{
				try
				{
					this.changeGrade(eventInfo, productData, productGrade);
				}
				catch (Exception ex)
				{
					eventLog.error("Change Grade failed");
				}
			}*/
			
			/* 20180912, hhlee, Add Product Location History Insert ==>> */
			// Deleted by smkang on 2018.10.08 - Because MaterialLocationName is recorded in Product table, CT_MATERIALLOCATIONHIST is unnecessary.
//            MESProductServiceProxy.getProductInfoUtil().setMaterialLocationHistory(eventInfo, lotName, productName, productType, productJudge, 
//                                                                                   productGrade, machineName, unitName, subUnitName, StringUtil.EMPTY, 
//                                                                                   productData.getCarrierName(), fromSlot, toSlot);

            /* <<== 20180912, hhlee, Add Product Location History Insert */
		}
		catch(CustomException e)
		{
			if(e.errorDef.getErrorCode().equals("PRODUCT-9001"))
			{
				VirtualGlass vGlassData = ExtendedObjectProxy.getVirtualGlassService().selectByKey(false, new Object[] {productName});
				
				vGlassData.setLocation(unitName);
				vGlassData.setMachineName(machineName);
				
				ExtendedObjectProxy.getVirtualGlassService().modify(eventInfo, vGlassData);
			}
			else
			{
				throw e;
			}
		}
		
	}
	
	private void changeGrade(EventInfo eventInfo, Product productData, String productGrade) throws CustomException
	{
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(productData.getLotName());
		
		List<ProductPGSRC> productPGSRCSequence = MESProductServiceProxy.getProductInfoUtil().getProductPGSRCSequence(lotData);
		
		for (ProductPGSRC productPGSRC : productPGSRCSequence)
		{
			if (productPGSRC.getProductName().equals(productData.getKey().getProductName()))
			{
				productPGSRC.setProductGrade(productGrade);
			}
		}
		
		String lotGrade = MESLotServiceProxy.getLotServiceUtil().decideLotJudge(lotData, "", productPGSRCSequence);
		
		ProductPGS productPGS = new ProductPGS();
		productPGS.setProductName(productData.getKey().getProductName());
		productPGS.setProductGrade(productGrade);
		
		List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();
		productPGSSequence.add(productPGS);
		
		ChangeGradeInfo changeGradeInfo = MESLotServiceProxy.getLotInfoUtil().changeGradeInfo(lotData, lotGrade, productPGSSequence);
		eventInfo.setEventName("ChangeGrade");
		Lot result = MESLotServiceProxy.getLotServiceImpl().ChangeGrade(eventInfo, lotData, changeGradeInfo);
	}
}
