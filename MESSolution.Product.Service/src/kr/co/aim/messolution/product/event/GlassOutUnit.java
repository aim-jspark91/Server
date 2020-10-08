package kr.co.aim.messolution.product.event;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.VirtualGlass;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.product.service.ProductServiceImpl;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;

import org.jdom.Document;

public class GlassOutUnit extends AsyncHandler {

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
		
		/* 20180922, hhlee, Add Processed Operation Before AOI(Record UNIT/RECIPE) ==>> */
		String unitRecipeName = SMessageUtil.getBodyItemValue(doc, "UNITRECIPENAME", false);
		/* <<== 20180922, hhlee, Add Processed Operation Before AOI(Record UNIT/RECIPE) */
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnitOut", getEventUser(), getEventComment(), null, null);
		
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
			//add by wghuang 20190513
			MachineSpec macSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(materialLocationInfo);
			
		    /* 20181227, hhlee, add, Machine validation ==>> */
            Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(materialLocationInfo);
            /* <<== 20181227, hhlee, add, Machine validation */
            
            //add by wghuang 20190513
            //!=TransportMachine : abnormal case : A2OVN040 -> Message sequence of BC reported is unstable
            if(!StringUtil.equals(macSpecData.getMachineType(), GenericServiceProxy.getConstantMap().MACHINE_MACHINETYPE_TransportMachine))
            {		
    			Product productData = ProductServiceProxy.getProductService().selectByKey(new ProductKey(productName));
    			
    			/* 20190617, hhlee, delete, MaterialLocationName is not update(GlassOutUnit) ==>> */
    			//Map<String, String> udfs = CommonUtil.setNamedValueSequence(SMessageUtil.getBodyElement(doc), Product.class.getSimpleName());
    			///* 20190116, hhlee, add, ProcessingUintName Column ==>> */
                //udfs.put("PROCESSINGUNITNAME", unitName);           
                ///* <<== 20190116, hhlee, add, ProcessingUintName Column */
                //
    			//eventInfo.setEventName("UnitOut");							
    			////SetMaterialLocationInfo setMaterialLocationInfo = MESProductServiceProxy.getProductInfoUtil().setMaterialLocationInfo(productData, unitName, udfs);
    			///* 20181227, hhlee, add, update materialLocation Machinename ==>> */
    			////SetMaterialLocationInfo setMaterialLocationInfo = MESProductServiceProxy.getProductInfoUtil().setMaterialLocationInfo(productData, materialLocationInfo, udfs);
    			//SetMaterialLocationInfo setMaterialLocationInfo = MESProductServiceProxy.getProductInfoUtil().setMaterialLocationInfo(productData, machineName, udfs);
    			///* <<== 20181227, hhlee, add, update materialLocation Machinename */
    			//
    			//MESProductServiceProxy.getProductServiceImpl().setMaterialLocation(eventInfo, productData, setMaterialLocationInfo);
    			/* <<== 20190617, hhlee, delete, MaterialLocationName is not update(GlassOutUnit) */
    			
    			/* 20190617, hhlee, modify, MaterialLocationName is not update(GlassOutUnit) ==>> */
                kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
                
               // 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//            Map<String, String> udfs = new HashMap<String, String>();
//            udfs.put("PROCESSINGUNITNAME", unitName);  
//            setEventInfo.setUdfs(udfs);      
                
                setEventInfo.getUdfs().put("PROCESSINGUNITNAME", unitName);
                
                eventInfo.setEventName("UnitOut");
                MESProductServiceProxy.getProductServiceImpl().setEvent(productData, setEventInfo, eventInfo);    			
                /* <<== 20190617, hhlee, modify, MaterialLocationName is not update(GlassOutUnit) */
    			
            }
            
            //add insertProductProcLocationHist for SPC
            ProductServiceImpl.insertProductProcLocationHist(eventInfo, productName, machineName, unitName);
            			
			//deleted by wghuang 20190513, old logic
/*			if(StringUtil.isNotEmpty(productGrade) && !StringUtil.equals(productData.getProductGrade(), productGrade))
			{
				try
				{
					this.changeGrade(eventInfo, productData, productGrade);
					
					productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
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
            
			/* 20180922, hhlee, delete , Because of move GlassInUnit ==>> */
            /* 20180922, hhlee, Add Processed Operation Before AOI(Record UNIT/RECIPE) ==>> */
			//MESProductServiceProxy.getProductServiceImpl().recordProcessedOperationOfUnit(eventInfo, productName, unitName, unitRecipeName);
            /* <<== 20180922, hhlee, Add Processed Operation Before AOI(Record UNIT/RECIPE) */
			/* <<== 20180922, hhlee, delete , Because of move GlassInUnit */
		}
		catch(NotFoundSignal ns)
		{
			MachineSpec macSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);
			
			if(StringUtil.equals(macSpecData.getUdfs().get("CONSTRUCTTYPE"), "UNPK"))
			{
				eventInfo.setEventName("UnitOut");
				
				VirtualGlass vGlassData = ExtendedObjectProxy.getVirtualGlassService().selectByKey(false, new Object[] {productName});
				
				vGlassData.setLocation(unitName);
				
				/* 20180922, hhlee, delete , Because of move GlassInUnit ==>> */
				/* 20180922, hhlee, Add Virtual Glass Processed Operation Before AOI(Record UNIT/RECIPE) ==>> */
				//vGlassData = MESProductServiceProxy.getProductServiceImpl().recordVirtualGlassOfUnit(vGlassData, unitName, unitRecipeName);
	            /* <<== 20180922, hhlee, Add Virtual Glass  Processed Operation Before AOI(Record UNIT/RECIPE) */
				/* <<== 20180922, hhlee, delete , Because of move GlassInUnit */
				
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
	
//	/**
//	 * 
//	 * @Name     recordProcessedOperationOfUnit
//	 * @since    2018. 9. 22.
//	 * @author   hhlee
//	 * @contents 
//	 *           
//	 * @param eventInfo
//	 * @param productName
//	 * @param unitName
//	 * @param unitRecipeName
//	 * @throws CustomException
//	 */
//	private void recordProcessedOperationOfUnit(EventInfo eventInfo, String productName, String unitName, String unitRecipeName) throws CustomException
//	{
//	    try
//	    {
//	        ProcessedOperation processedOperationData = null;
//	        try
//	        {
//	            processedOperationData = ExtendedObjectProxy.getProcessedOperationService().selectByKey(false, new Object[] {productName});
//	        }
//	        catch (Exception ex)
//	        {
//	            processedOperationData = new ProcessedOperation(productName);
//	            processedOperationData = ExtendedObjectProxy.getProcessedOperationService().create(eventInfo, processedOperationData);
//	        }
//	        
//	        String unitNameList = processedOperationData.getUnitNameList();
//	        if(StringUtil.isEmpty(unitNameList))
//	        {
//	            unitNameList = unitName;
//	        }
//	        else
//	        {
//	            unitNameList = unitNameList + "|" + unitName;
//	        }
//	        
//	        String unitRecipeNameList = processedOperationData.getUnitRecipeNameList();
//            if(StringUtil.isEmpty(unitRecipeNameList))
//            {
//                unitRecipeNameList = unitRecipeName;
//            }
//            else
//            {
//                unitRecipeNameList = unitRecipeNameList + "|" + unitRecipeName;
//            }
//            
//            processedOperationData.setUnitNameList(unitNameList);
//            processedOperationData.setUnitRecipeNameList(unitRecipeNameList);
//            
//            ExtendedObjectProxy.getProcessedOperationService().modify(eventInfo, processedOperationData);
//                        
//	    }
//	    catch (Exception ex)
//        {
//            eventLog.error(ex.getMessage());            
//        }
//	}
//	
//	/**
//	 * 
//	 * @Name     recordVirtualGlassOfUnit
//	 * @since    2018. 9. 22.
//	 * @author   hhlee
//	 * @contents 
//	 *           
//	 * @param virtualGlassData
//	 * @param unitName
//	 * @param unitRecipeName
//	 * @return
//	 * @throws CustomException
//	 */
//	private VirtualGlass recordVirtualGlassOfUnit(VirtualGlass virtualGlassData, String unitName, String unitRecipeName) throws CustomException
//    {
//	   
//	    VirtualGlass virtualglassdata = virtualGlassData;
//	    
//	    try
//        {
//	        String unitNameList = virtualglassdata.getUnitNameList();
//            if(StringUtil.isEmpty(unitNameList))
//            {
//                unitNameList = unitName;
//            }
//            else
//            {
//                unitNameList = unitNameList + "|" + unitName;
//            }
//            
//            String unitRecipeNameList = virtualglassdata.getUnitRecipeNameList();
//            if(StringUtil.isEmpty(unitRecipeNameList))
//            {
//                unitRecipeNameList = unitRecipeName;
//            }
//            else
//            {
//                unitRecipeNameList = unitRecipeNameList + "|" + unitRecipeName;
//            }
//            
//            virtualglassdata.setUnitNameList(unitNameList);
//            virtualglassdata.setUnitRecipeNameList(unitRecipeNameList);
//        }
//	    catch (Exception ex)
//        {
//            eventLog.error(ex.getMessage());            
//        }
//	    
//        return virtualglassdata;
//    }
	
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
		productPGS.setPosition(productData.getPosition());
		productPGS.setSubProductQuantity1(productData.getSubProductQuantity1());
		productPGS.setSubProductQuantity2(productData.getSubProductQuantity2());
		productPGS.setUdfs(productData.getUdfs());
		
		List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();
		productPGSSequence.add(productPGS);
		
		ChangeGradeInfo changeGradeInfo = MESLotServiceProxy.getLotInfoUtil().changeGradeInfo(lotData, lotGrade, productPGSSequence);
		eventInfo.setEventName("ChangeGrade");
		Lot result = MESLotServiceProxy.getLotServiceImpl().ChangeGrade(eventInfo, lotData, changeGradeInfo);
	}
}