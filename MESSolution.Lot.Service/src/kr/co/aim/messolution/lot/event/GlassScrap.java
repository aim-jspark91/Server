package kr.co.aim.messolution.lot.event;

import java.util.HashMap;
import java.util.Map;

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
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.product.management.data.Product;

import org.jdom.Document;

public class GlassScrap extends AsyncHandler{

    @Override
    public void doWorks(Document doc) throws CustomException {

        String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
        String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
        String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
        String crateName = SMessageUtil.getBodyItemValue(doc, "CRATENAME", false);
        String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", false);
        String vcrproductName = SMessageUtil.getBodyItemValue(doc, "VCRPRODUCTNAME", false);
        String productJudge = SMessageUtil.getBodyItemValue(doc, "PRODUCTJUDGE", false);
        String productGrade = SMessageUtil.getBodyItemValue(doc, "PRODUCTGRADE", false);
        String scrapCode = SMessageUtil.getBodyItemValue(doc, "SCRAPCODE", false);

        /* 20190429, hhlee, change ==> */
        /* 1. Scrap MachineName is reported Scrap£¬Scrap is not proceed
         * 2. Record Lot Note(SetEvent)
         * 3. Mantis : 0003709
         * /        
        ///* 20181226, hhlee, change ==>> */
        ///* 1.ProductHistory EventName ChangeSGrade
        // *   And £¬ScrapReasonCode SG-003£¬Scrap MachineName is reported Scrap£¬Scrap Department is machine Department
        // *   And£¬CT_SCRAPPRODUCT Record
        // *  2.LotHistory add ChangeSGrade History 
        // */
        ///* <<== 20181226, hhlee, change */
        /* <<== 20190429, hhlee, change */
        
        /* 20181226, hhlee, change, Add ReasonCodeType ==>> */
        /* 20190117, hhlee, Mantis: 0002141 [ScrapReasonCode = SG-003, Scrap MachineName = machineName, Scrap Department = machine Department]*/
        //scrapCode = "SG-003";
        //EventInfo eventInfo = EventInfoUtil.makeEventInfo("Scrap", this.getEventUser(), this.getEventComment(), GenericServiceProxy.getConstantMap().REASONCODETYPE_SCRAPGLASS, scrapCode);
        EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), GenericServiceProxy.getConstantMap().REASONCODETYPE_SCRAPGLASS, scrapCode);
        /* <<== 20181226, hhlee, change, Add ReasonCodeType */
        
        try
        {
            /* Validation machineName and unitName */
            Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
            machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);

            if(StringUtil.isEmpty(productName))
            {
                throw new CustomException("PRODUCT-9001", productName);
            }
            
            /* Validation productName */
            Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
            
            /* Record Lot Note(SetEvent) */
            lotName = (StringUtil.isEmpty(lotName) ? productData.getLotName() : lotName);
            Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
            
            eventInfo.setEventName("GlassScrap");
            
         // 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//            Map<String, String> lotDataUdf = lotData.getUdfs();
//            lotDataUdf.put("NOTE", "EQP Report Scrap ProductName : " + productData.getKey().getProductName());                 
//            lotData.setUdfs(lotDataUdf);
//            
//            SetEventInfo setEventInfo = new SetEventInfo();
//            setEventInfo.setUdfs(lotDataUdf);
            
            SetEventInfo setEventInfo = new SetEventInfo();
            setEventInfo.getUdfs().put("NOTE", "EQP Report Scrap ProductName : " + productData.getKey().getProductName());
            lotData = LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);                      
            
            // Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//            lotDataUdf.put("NOTE", StringUtil.EMPTY);                 
//            lotData.setUdfs(lotDataUdf);  
//            LotServiceProxy.getLotService().update(lotData);
            Map<String, String> updateUdfs = new HashMap<String, String>();
			updateUdfs.put("NOTE", "");
			MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
        }
        catch(CustomException e)
        {
            if(e.errorDef.getErrorCode().equals("PRODUCT-9001"))
            {
                eventInfo.setEventName("Scrapped");

                VirtualGlass vGlassData = ExtendedObjectProxy.getVirtualGlassService().selectByKey(false, new Object[] {vcrproductName});

                vGlassData.setGrade(GenericServiceProxy.getConstantMap().ProductGrade_S);
                vGlassData.setLocation("");

                ExtendedObjectProxy.getVirtualGlassService().modify(eventInfo, vGlassData);
            }
            else
            {
                throw e;
            }

        }
    }
    
    
}

/* 20190429, hhlee, backup */
/* 20190429, hhlee, change ==> */
/* 1. Scrap MachineName is reported Scrap£¬Scrap is not proceed
 * 2. Record Lot Note(SetEvent)
 * 3. Mantis : 0003709
 * /        
///* 20181226, hhlee, change ==>> */
///* 1.ProductHistory EventName ChangeSGrade
// *   And £¬ScrapReasonCode SG-003£¬Scrap MachineName is reported Scrap£¬Scrap Department is machine Department
// *   And£¬CT_SCRAPPRODUCT Record
// *  2.LotHistory add ChangeSGrade History 
// */
///* <<== 20181226, hhlee, change */
/* <<== 20190429, hhlee, change */

//package kr.co.aim.messolution.lot.event;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
//import kr.co.aim.messolution.extended.object.management.data.ScrapProduct;
//import kr.co.aim.messolution.extended.object.management.data.VirtualGlass;
//import kr.co.aim.messolution.generic.GenericServiceProxy;
//import kr.co.aim.messolution.generic.errorHandler.CustomException;
//import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
//import kr.co.aim.messolution.generic.util.EventInfoUtil;
//import kr.co.aim.messolution.generic.util.SMessageUtil;
//import kr.co.aim.messolution.lot.MESLotServiceProxy;
//import kr.co.aim.messolution.machine.MESMachineServiceProxy;
//import kr.co.aim.messolution.product.MESProductServiceProxy;
//import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
//import kr.co.aim.greentrack.generic.info.EventInfo;
//import kr.co.aim.greentrack.generic.util.StringUtil;
//import kr.co.aim.greentrack.generic.util.TimeUtils;
//import kr.co.aim.greentrack.lot.LotServiceProxy;
//import kr.co.aim.greentrack.lot.management.data.Lot;
//import kr.co.aim.greentrack.lot.management.data.LotHistory;
//import kr.co.aim.greentrack.lot.management.data.LotKey;
//import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
//import kr.co.aim.greentrack.lot.management.info.MakeScrappedInfo;
//import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
//import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
//import kr.co.aim.greentrack.machine.management.data.Machine;
//import kr.co.aim.greentrack.machine.management.data.MachineSpec;
//import kr.co.aim.greentrack.product.ProductServiceProxy;
//import kr.co.aim.greentrack.product.management.data.Product;
//import kr.co.aim.greentrack.product.management.info.ChangeGradeInfo;
//import kr.co.aim.greentrack.product.management.info.ext.ProductP;
//import kr.co.aim.greentrack.product.management.info.ext.ProductU;
//
//import org.apache.commons.lang.StringUtils;
//import org.jdom.Document;
//
//public class GlassScrap extends AsyncHandler{
//
//    @Override
//    public void doWorks(Document doc) throws CustomException {
//
//        String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
//        String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
//        String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
//        String crateName = SMessageUtil.getBodyItemValue(doc, "CRATENAME", false);
//        String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", false);
//        String vcrproductName = SMessageUtil.getBodyItemValue(doc, "VCRPRODUCTNAME", false);
//        String productJudge = SMessageUtil.getBodyItemValue(doc, "PRODUCTJUDGE", false);
//        String productGrade = SMessageUtil.getBodyItemValue(doc, "PRODUCTGRADE", false);
//        String scrapCode = SMessageUtil.getBodyItemValue(doc, "SCRAPCODE", false);
//
//        /* 20181226, hhlee, change ==>> */
//        /* 1.ProductHistory EventName ChangeSGrade
//         *   And £¬ScrapReasonCode SG-003£¬Scrap MachineName is reported Scrap£¬Scrap Department is machine Department
//         *   And£¬CT_SCRAPPRODUCT Record
//         *  2.LotHistory add ChangeSGrade History 
//         */
//        /* <<== 20181226, hhlee, change */
//        
//        /* 20181226, hhlee, change, Add ReasonCodeType ==>> */
//        /* 20190117, hhlee, Mantis: 0002141 [ScrapReasonCode = SG-003, Scrap MachineName = machineName, Scrap Department = machine Department]*/
//        //scrapCode = "SG-003";
//        EventInfo eventInfo = EventInfoUtil.makeEventInfo("Scrap", this.getEventUser(), this.getEventComment(), GenericServiceProxy.getConstantMap().REASONCODETYPE_SCRAPGLASS, scrapCode);
//        /* <<== 20181226, hhlee, change, Add ReasonCodeType */
//        
//        try
//        {
//            //Check Machine and UnitInfo
//            Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
//            machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
//
//            /* 20181226, Add , machineSpec Select ==>> */
//            MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
//            /* <<== 20181226, Add , machineSpec Select */
//            
//            if(StringUtil.isEmpty(productName))
//            {
//                throw new CustomException("PRODUCT-9001", productName);
//            }
//            
//            double productQuantity = 1.0;
//
//            Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
//
//            if(StringUtil.equals(productData.getProductState(), "Scrapped"))
//            {
//                throw new CustomException("PRODUCT-0005", productName);
//            }
//
//            /* 20180813, Add , The glass scrap after change grade ==>> */
//            eventInfo.setEventName("ChangeSGrade");
//            
//            /* 20181226, hhlee, change, Add ScrapMachineName, ScrapDepartment ==>> */
//            String scrapMachineName = machineName;
//            String scrapDepartmentName = machineSpecData.getUdfs().get("DEPARTMENT");
//            
//            Map<String, String> productUdf = productData.getUdfs();
//            productUdf.put("SCRAPMACHINE", scrapMachineName);
//            productUdf.put("SCRAPDEPARTMENTNAME", scrapDepartmentName);
//            productData.setUdfs(productUdf);
//            ProductServiceProxy.getProductService().update(productData);
//            /* <<== 20181226, hhlee, change, Add ScrapMachineName, ScrapDepartment */
//            
//            //judge
//            ChangeGradeInfo changeGradeInfo = MESProductServiceProxy.getProductInfoUtil().changeGradeInfo(productData, productData.getPosition(), GenericServiceProxy.getConstantMap().ProductGrade_S, productData.getProductProcessState(),
//                                                productData.getSubProductGrades1(), productData.getSubProductGrades2(), productData.getSubProductQuantity1(), productData.getSubProductQuantity2());
//            
//            productData = MESProductServiceProxy.getProductServiceImpl().changeGrade(productData, changeGradeInfo, eventInfo);
//                
//            /* 20180813, Delete , The glass scrap after change grade ==>> */
//            //productData.setProductGrade(GenericServiceProxy.getConstantMap().ProductGrade_S);
//            //ProductServiceProxy.getProductService().update(productData);
//            /* <<== 20180813, Delete , The glass scrap after change grade */
//            
//            //eventInfo.setBehaviorName("ARRAY");
//            //eventInfo.setEventName("SplitByScrap");
//            
//            /* 20181226, hhlee, delete */
//            //eventInfo.setEventName("Scrap");
//            /* <<== 20180813, Add , The glass scrap after change grade */
//            
//            //eventInfo.setReasonCode(sReasonCode);
//
//            Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//            LotKey lotKey = lotData.getKey();
//
//            /* 20181226, hhlee, change, Add ChangeSGrade History in LotHistory ==>> */
//            Map<String, String> lotUdf = lotData.getUdfs();
//            /* 20190117, hhlee, Mantis: 0002141 [Note = EQP Report Scrap£¬ProductName£º~~~~]*/
//            lotUdf.put("NOTE", "EQP Report Scrap ProductName : " + productName);                 
//            lotData.setUdfs(lotUdf);  
//            LotServiceProxy.getLotService().update(lotData);
//            
//            eventInfo.setEventName("ChangeSGrade");
//            
//            SetEventInfo setEventInfo = new SetEventInfo();
//            lotData = LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);                      
//            
//            lotUdf.put("NOTE", StringUtil.EMPTY);                 
//            lotData.setUdfs(lotUdf);  
//            LotServiceProxy.getLotService().update(lotData);            
//            /* <<== 20181226, hhlee, change, Add ChangeSGrade History in LotHistory */
//                        
//            List<Product> productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
//            
//            List<ProductP> productPSequence = new ArrayList<ProductP>();
//            ProductP productP = new ProductP();
//            productP.setProductName(productName);
//            productP.setPosition(productData.getPosition());
//            productP.setUdfs(productData.getUdfs());
//            productPSequence.add(productP);            
//            
//            List<ProductU> productUSequence = new ArrayList<ProductU>();
//            ProductU productU = new ProductU();
//            productU.setProductName(productName);
//            productUSequence.add(productU);
//            
//            Lot targetLot = lotData;
//            if(productList != null && productList.size() > 1) // productList.size >= 2
//            {            
//                eventInfo.setEventName("Create");
//                targetLot = MESLotServiceProxy.getLotServiceUtil().createWithParentLot(eventInfo, lotData, "", true, new HashMap<String, String>(), lotData.getUdfs());
//                
//                TransferProductsToLotInfo  transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(
//                        targetLot.getKey().getLotName(), productPSequence.size(), productPSequence, lotData.getUdfs(), new HashMap<String, String>());
//    
//                eventInfo.setEventName("SplitByScrap");
//                eventInfo.setCheckTimekeyValidation(false);
//                eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//                
//                lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);
//                targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());
//                
//                /**************************** Check Mixed WO Name *********************************/
//        		//2019.04.24_hsryu_Insert Logic. Change call Common Fuction. Mantis 0002757.
//        		try{
//        			String desWOName = MESLotServiceProxy.getLotServiceUtil().isMixedWorkOrder(targetLot);
//        			
//        			if(!StringUtils.isEmpty(desWOName)) {
//        				targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());
//        				if(!StringUtils.equals(targetLot.getProductRequestName(), desWOName)) {
//        					targetLot.setProductRequestName(desWOName);
//        					LotServiceProxy.getLotService().update(targetLot);
//        					
//        					String condition = "where lotname=?" + " and timekey= ? " ;
//        					Object[] bindSet = new Object[]{targetLot.getKey().getLotName(), eventInfo.getEventTimeKey()};
//        					List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
//        					LotHistory lotHistory = arrayList.get(0);
//        					lotHistory.setProductRequestName(desWOName);
//        					LotServiceProxy.getLotHistoryService().update(lotHistory);
//        				}
//        			}
//        			
//        			//2019.04.23_hsryu_Insert Logic. Change call Common Fuction. Mantis 0002757.
//        			String sourceWOName = MESLotServiceProxy.getLotServiceUtil().isMixedWorkOrder(lotData);
//        			
//        			if(!StringUtils.isEmpty(sourceWOName)) {
//        				if(!StringUtils.equals(lotData.getProductRequestName(), sourceWOName)) {
//        					lotData.setProductRequestName(sourceWOName);
//        					LotServiceProxy.getLotService().update(lotData);
//        					
//        					String condition = "where lotname=?" + " and timekey= ? " ;
//        					Object[] bindSet = new Object[]{lotData.getKey().getLotName(), eventInfo.getEventTimeKey()};
//        					List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
//        					LotHistory lotHistory = arrayList.get(0);
//        					lotHistory.setProductRequestName(sourceWOName);
//        					LotServiceProxy.getLotHistoryService().update(lotHistory);
//        				}
//        			}
//        		}
//        		catch(Throwable e){
//        			eventLog.warn("Fail update WorkOrder.");
//        		}
//        		/*********************************************************************************/
//        		
//        		targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());
//        		
//                lotKey = targetLot.getKey();
//            }
//            else if(productList != null && productList.size() > 0) // productList.size = 1
//            {
//                DeassignCarrierInfo deassignCarrierInfo =  new DeassignCarrierInfo();
//
//                eventInfo.setEventName("DeassignCarrier");
//                eventInfo.setCheckTimekeyValidation(false);
//                eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//                targetLot = MESLotServiceProxy.getLotServiceImpl().deassignCarrier(targetLot, deassignCarrierInfo, eventInfo);
//            }
//            
//            eventInfo.setEventName("Scrap");
//            eventInfo.setCheckTimekeyValidation(false);
//            eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//            
//            MakeScrappedInfo makeLotScrappedInfo = MESLotServiceProxy.getLotInfoUtil().makeScrappedInfo(targetLot, productQuantity, productUSequence);
//            LotServiceProxy.getLotService().makeScrapped(lotKey, eventInfo, makeLotScrappedInfo);
//
//            targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());
//            
//            /* 20181226, hhlee, add, Insert/Update CT_SCRAPPRODUCT ==>> */
//            /* 20190117, hhlee, Mantis: 0002141 [Note = EQP Report Scrap£¬ProductName£º~~~~]*/
//            String scrapProductNote = "EQP Report Scrap ProductName : " + productName;            
//            ScrapProduct scrapProduct = new ScrapProduct();
//            
//            try
//            {
//                scrapProduct = ExtendedObjectProxy.getScrapProductService().selectByKey(false, new Object[] {productName});
//            }
//            catch(Throwable e)
//            {
//                scrapProduct = null;
//            }
//            
//            /* 20190117, hhlee, Mantis: 0002141 [Note = EQP Report Scrap£¬ProductName£º~~~~]*/
//            //productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
//            
//            if(scrapProduct!=null)
//            {
//                this.ModifyScrapProductInfo(productData, scrapProduct, scrapMachineName, scrapDepartmentName, scrapProductNote, eventInfo);
//            }
//            else
//            {
//                this.CreateScrapProductInfo(productData, scrapDepartmentName, scrapMachineName, scrapProductNote, eventInfo);
//            }
//            /* <<== 20181226, hhlee, add, Insert/Update CT_SCRAPPRODUCT */
//            
//            /********* 2019.04.16_hsryu_adjust WO Quantity **********/
//            try{
//                String productRequestName = productData.getProductRequestName();
//                
//                if(StringUtils.isNotEmpty(productRequestName)){
//                    MESWorkOrderServiceProxy.getProductRequestServiceUtil().calculateProductRequestQty(productRequestName, null, "S", 1, eventInfo);    		
//                }
//            }
//            catch(Throwable e){
//            	eventLog.warn(" Fail adjust WO Quantity. ");
//            }
//            /********************************************************/
//        }
//        catch(CustomException e)
//        {
//            if(e.errorDef.getErrorCode().equals("PRODUCT-9001"))
//            {
//                eventInfo.setEventName("Scrapped");
//
//                VirtualGlass vGlassData = ExtendedObjectProxy.getVirtualGlassService().selectByKey(false, new Object[] {vcrproductName});
//
//                vGlassData.setGrade(GenericServiceProxy.getConstantMap().ProductGrade_S);
//                vGlassData.setLocation("");
//
//                ExtendedObjectProxy.getVirtualGlassService().modify(eventInfo, vGlassData);
//            }
//            else
//            {
//                throw e;
//            }
//
//        }
//    }
//    
//    /**
//     * 
//     * @Name     ModifyScrapProductInfo
//     * @since    2018. 12. 26.
//     * @author   smkang
//     * @contents Modify ScrapProduct
//     *           
//     * @param product
//     * @param scrapProduct
//     * @param scrapDepartmentName
//     * @param scrapMachineName
//     * @param note
//     * @param eventInfo
//     * @throws CustomException
//     */
//    private void ModifyScrapProductInfo(Product product, ScrapProduct scrapProduct, String scrapDepartmentName, String scrapMachineName, String note, EventInfo eventInfo) throws CustomException
//    {
//        eventLog.warn("Already Exist ScrapGlass Info. Start Modify OriginalInfo");
//        
//        eventInfo.setEventName("ModifyScrapProduct");
//
//        boolean isSampleFlow = false;
//        
//        String processFlowName = product.getProcessFlowName();
//        String processFlowVersion = product.getProcessFlowVersion();
//        String processOperationName = product.getProcessOperationName();
//        String processOperationVersion = product.getProcessOperationVersion();
//        String nodeStack = product.getNodeStack();
//
//        if(StringUtil.equals(MESLotServiceProxy.getLotServiceUtil().returnFlowType(product.getFactoryName(), product.getProcessFlowName(), product.getProcessOperationName()).toUpperCase(), "SAMPLING"))
//        {
//            eventLog.info("Start Insert ScrapProudct.if product in SampleFlow, register MainFlow,MainOper at CT_SCRAPPRODUCT.");
//            isSampleFlow = true;
//
//            String currentNodeStack = product.getNodeStack();
//            String[] arrNodeStack = StringUtil.split(currentNodeStack, ".");
//            
//            if(arrNodeStack.length>1)
//            {
//                nodeStack = arrNodeStack[arrNodeStack.length-2];
//                //String nextNodeID = MESLotServiceProxy.getLotServiceUtil().GetReturnAfterNodeStackForSampling(product.getFactoryName(), nodeStack);
//                
//                if(StringUtils.isNotEmpty(nodeStack))
//                {
//                    Map<String, String> flowMap = MESLotServiceProxy.getLotServiceUtil().getProcessFlowInfo(nodeStack);
//
//                    String mainFlowName = flowMap.get("PROCESSFLOWNAME");
//                    String mainFlowNameFlowVersion = flowMap.get("PROCESSFLOWVERSION");
//                    String mainFlowNameOperationName = flowMap.get("PROCESSOPERATIONNAME");
//                    String mainFlowNameOperationVersion = flowMap.get("PROCESSOPERATIONVERSION");
//                    
//                    if(StringUtils.isNotEmpty(mainFlowNameOperationName))
//                    {
//                        processFlowName = mainFlowName;
//                        processFlowVersion = mainFlowNameFlowVersion;
//                        processOperationName = mainFlowNameOperationName;
//                        processOperationVersion = mainFlowNameOperationVersion;
//                    }
//                }
//            }
//            else
//            {
//                throw new CustomException("COMMON-0001", "currentFlow is SampleFlow. But NodeStack is lack." );
//            }
//        }
//        else
//        {
//            eventLog.info("Start Insert ScrapProudct.");
//        }
//        
//        scrapProduct.setFactoryName(product.getFactoryName());
//        scrapProduct.setProductionType(product.getProductionType());
//        scrapProduct.setProductSpecName(product.getProductSpecName());
//        scrapProduct.setProductSpecVersion(product.getProductSpecVersion());
//        scrapProduct.setProductRequestName(product.getProductRequestName());
//        scrapProduct.setLotName(product.getLotName());
//        scrapProduct.setCarrierName(product.getCarrierName());
//        scrapProduct.setPosition(product.getPosition());
//        scrapProduct.setProductType(product.getProductType());
//        scrapProduct.setProductGrade(product.getProductGrade());
//        scrapProduct.setPriority(product.getPriority());
//        scrapProduct.setDueDate(product.getDueDate());
//        scrapProduct.setAreaName(product.getAreaName());
//        scrapProduct.setProductState(product.getProductState());
//        scrapProduct.setProductProcessState(product.getProductProcessState());
//        scrapProduct.setProductHoldState(product.getProductHoldState());
//        scrapProduct.setProcessFlowName(processFlowName);
//        scrapProduct.setProcessFlowVersion(processFlowVersion);
//        scrapProduct.setProcessOperationName(processOperationName);
//        scrapProduct.setProcessOperationVersion(processOperationVersion);
//        scrapProduct.setNodeStack(nodeStack);
//        scrapProduct.setMachineName(product.getMachineName());
//        scrapProduct.setEcCode(product.getUdfs().get("ECCODE"));
//        scrapProduct.setDummyUsedCount(StringUtils.isEmpty(product.getUdfs().get("DUMMYUSEDCOUNT"))?0:Long.parseLong(product.getUdfs().get("DUMMYUSEDCOUNT")));
//        scrapProduct.setMqcCount(StringUtils.isEmpty(product.getUdfs().get("MQCCOUNT"))?0:Long.parseLong(product.getUdfs().get("MQCCOUNT")));
//        scrapProduct.setTotalMQCCount(StringUtils.isEmpty(product.getUdfs().get("TOTALMQCCOUNT"))?0:Long.parseLong(product.getUdfs().get("TOTALMQCCOUNT")));
//        scrapProduct.setMqcUSEProductSpec(product.getUdfs().get("MQCUSEPRODUCTSPEC"));
//        scrapProduct.setMqcUSEECCode(product.getUdfs().get("MQCUSEECCODE"));
//        scrapProduct.setMqcUSEProcessFlow(product.getUdfs().get("MQCUSEPROCESSFLOW"));
//        scrapProduct.setScrapDepartmentName(scrapMachineName);
//        scrapProduct.setScrapMachine(scrapMachineName);
//        scrapProduct.setNote(note);
//        scrapProduct.setReasonCode(product.getReasonCode());
//        scrapProduct.setReasonCodeType(product.getReasonCodeType());
//        //scrapProduct.setReasonCode(eventInfo.getReasonCode());
//        //scrapProduct.setReasonCodeType(eventInfo.getReasonCode());
//        scrapProduct.setLastEventTime(eventInfo.getEventTime());
//        scrapProduct.setLastEventTimeKey(eventInfo.getEventTimeKey());
//        scrapProduct.setLastEventUser(eventInfo.getEventUser());
//        scrapProduct.setLastEventComment(eventInfo.getEventComment());
//        
//        ExtendedObjectProxy.getScrapProductService().modify(eventInfo, scrapProduct);
//
//
//    }
//    
//    /**
//     * 
//     * @Name     CreateScrapProductInfo
//     * @since    2018. 12. 26.
//     * @author   hhlee
//     * @contents Create ScrapProduct
//     *           
//     * @param product
//     * @param scrapDepartmentName
//     * @param scrapMachineName
//     * @param note
//     * @param eventInfo
//     * @throws CustomException
//     */
//    private void CreateScrapProductInfo(Product product, String scrapDepartmentName, String scrapMachineName, String note, EventInfo eventInfo) throws CustomException
//    {
//        eventInfo.setEventName("CreateScrapProduct");
//
//        boolean isSampleFlow = false;
//        
//        String processFlowName = product.getProcessFlowName();
//        String processFlowVersion = product.getProcessFlowVersion();
//        String processOperationName = product.getProcessOperationName();
//        String processOperationVersion = product.getProcessOperationVersion();
//        String nodeStack = product.getNodeStack();
//
//        if(StringUtil.equals(MESLotServiceProxy.getLotServiceUtil().returnFlowType(product.getFactoryName(), product.getProcessFlowName(), product.getProcessFlowVersion()).toUpperCase(), "SAMPLING"))
//        {
//            eventLog.info("Start Insert ScrapProudct.if product in SampleFlow, register MainFlow,MainOper at CT_SCRAPPRODUCT.");
//            isSampleFlow = true;
//
//            String currentNodeStack = product.getNodeStack();
//            String[] arrNodeStack = StringUtil.split(currentNodeStack, ".");
//            
//            if(arrNodeStack.length>1)
//            {
//                nodeStack = arrNodeStack[arrNodeStack.length-2];
//                //String nextNodeID = MESLotServiceProxy.getLotServiceUtil().GetReturnAfterNodeStackForSampling(product.getFactoryName(), nodeStack);
//                
//                if(StringUtils.isNotEmpty(nodeStack))
//                {
//                    Map<String, String> flowMap = MESLotServiceProxy.getLotServiceUtil().getProcessFlowInfo(nodeStack);
//
//                    String nextFlowName = flowMap.get("PROCESSFLOWNAME");
//                    String nextFlowVersion = flowMap.get("PROCESSFLOWVERSION");
//                    String nextOperationName = flowMap.get("PROCESSOPERATIONNAME");
//                    String nextOperationVersion = flowMap.get("PROCESSOPERATIONVERSION");
//                    
//                    if(StringUtils.isNotEmpty(nextOperationName))
//                    {
//                        processFlowName = nextFlowName;
//                        processFlowVersion = nextFlowVersion;
//                        processOperationName = nextOperationName;
//                        processOperationVersion = nextOperationVersion;
//                    }
//                }
//            }
//            else
//            {
//                throw new CustomException("COMMON-0001", "currentFlow is SampleFlow. But NodeStack is lack." );
//            }
//        }
//        else
//        {
//            eventLog.info("Start Insert ScrapProudct.");
//        }
//
//        ScrapProduct scrapProduct = new ScrapProduct();
//
//        scrapProduct.setProductName(product.getKey().getProductName());
//        scrapProduct.setFactoryName(product.getFactoryName());
//        scrapProduct.setProductionType(product.getProductionType());
//        scrapProduct.setProductSpecName(product.getProductSpecName());
//        scrapProduct.setProductSpecVersion(product.getProductSpecVersion());
//        scrapProduct.setProductRequestName(product.getProductRequestName());
//        scrapProduct.setLotName(product.getLotName());
//        scrapProduct.setCarrierName(product.getCarrierName());
//        scrapProduct.setPosition(product.getPosition());
//        scrapProduct.setProductType(product.getProductType());
//        scrapProduct.setProductGrade(product.getProductGrade());
//        scrapProduct.setPriority(product.getPriority());
//        scrapProduct.setDueDate(product.getDueDate());
//        scrapProduct.setAreaName(product.getAreaName());
//        scrapProduct.setProductState(product.getProductState());
//        scrapProduct.setProductProcessState(product.getProductProcessState());
//        scrapProduct.setProductHoldState(product.getProductHoldState());
//        scrapProduct.setProcessFlowName(processFlowName);
//        scrapProduct.setProcessFlowVersion(processFlowVersion);
//        scrapProduct.setProcessOperationName(processOperationName);
//        scrapProduct.setProcessOperationVersion(processOperationVersion);
//        scrapProduct.setNodeStack(product.getNodeStack());
//        scrapProduct.setMachineName(product.getMachineName());
//        scrapProduct.setEcCode(product.getUdfs().get("ECCODE"));
//        scrapProduct.setDummyUsedCount(StringUtils.isEmpty(product.getUdfs().get("DUMMYUSEDCOUNT"))?0:Long.parseLong(product.getUdfs().get("DUMMYUSEDCOUNT")));
//        scrapProduct.setMqcCount(StringUtils.isEmpty(product.getUdfs().get("MQCCOUNT"))?0:Long.parseLong(product.getUdfs().get("MQCCOUNT")));
//        scrapProduct.setTotalMQCCount(StringUtils.isEmpty(product.getUdfs().get("TOTALMQCCOUNT"))?0:Long.parseLong(product.getUdfs().get("TOTALMQCCOUNT")));
//        scrapProduct.setMqcUSEProductSpec(product.getUdfs().get("MQCUSEPRODUCTSPEC"));
//        scrapProduct.setMqcUSEECCode(product.getUdfs().get("MQCUSEECCODE"));
//        scrapProduct.setMqcUSEProcessFlow(product.getUdfs().get("MQCUSEPROCESSFLOW"));
//        scrapProduct.setScrapDepartmentName(scrapDepartmentName);
//        scrapProduct.setScrapMachine(scrapMachineName);
//        scrapProduct.setNote(note);
//        scrapProduct.setReasonCode(product.getReasonCode());
//        scrapProduct.setReasonCodeType(product.getReasonCodeType());
//        //scrapProduct.setReasonCode(eventInfo.getReasonCode());
//        //scrapProduct.setReasonCodeType(eventInfo.getReasonCode());
//        scrapProduct.setLastEventName(eventInfo.getEventName());
//        scrapProduct.setLastEventTime(eventInfo.getEventTime());
//        scrapProduct.setLastEventTimeKey(eventInfo.getEventTimeKey());
//        scrapProduct.setLastEventUser(eventInfo.getEventUser());
//        scrapProduct.setLastEventComment(eventInfo.getEventComment());
//        scrapProduct.setLastEventComment(note);
//
//        ExtendedObjectProxy.getScrapProductService().create(eventInfo, scrapProduct);
//    }
//    
//}
//
//
////package kr.co.aim.messolution.lot.event;
////
////import java.util.ArrayList;
////import java.util.List;
////
////import kr.co.aim.messolution.durable.MESDurableServiceProxy;
////import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
////import kr.co.aim.messolution.extended.object.management.data.VirtualGlass;
////import kr.co.aim.messolution.generic.GenericServiceProxy;
////import kr.co.aim.messolution.generic.errorHandler.CustomException;
////import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
////import kr.co.aim.messolution.generic.util.EventInfoUtil;
////import kr.co.aim.messolution.generic.util.SMessageUtil;
////import kr.co.aim.messolution.lot.MESLotServiceProxy;
////import kr.co.aim.messolution.machine.MESMachineServiceProxy;
////import kr.co.aim.messolution.product.MESProductServiceProxy;
////import kr.co.aim.greentrack.durable.management.data.Durable;
////import kr.co.aim.greentrack.generic.info.EventInfo;
////import kr.co.aim.greentrack.generic.util.StringUtil;
////import kr.co.aim.greentrack.lot.LotServiceProxy;
////import kr.co.aim.greentrack.lot.management.data.Lot;
////import kr.co.aim.greentrack.lot.management.data.LotKey;
////import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
////import kr.co.aim.greentrack.lot.management.info.MakeScrappedInfo;
////import kr.co.aim.greentrack.machine.management.data.Machine;
////import kr.co.aim.greentrack.product.ProductServiceProxy;
////import kr.co.aim.greentrack.product.management.data.Product;
////import kr.co.aim.greentrack.product.management.info.ChangeGradeInfo;
////import kr.co.aim.greentrack.product.management.info.ext.ProductU;
////
////import org.jdom.Document;
////
////public class GlassScrap extends AsyncHandler{
////
////	@Override
////	public void doWorks(Document doc) throws CustomException {
////
////		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
////		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
////		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
////		String crateName = SMessageUtil.getBodyItemValue(doc, "CRATENAME", false);
////		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", false);
////		String vcrproductName = SMessageUtil.getBodyItemValue(doc, "VCRPRODUCTNAME", false);
////		String productJudge = SMessageUtil.getBodyItemValue(doc, "PRODUCTJUDGE", false);
////		String productGrade = SMessageUtil.getBodyItemValue(doc, "PRODUCTGRADE", false);
////		String scrapCode = SMessageUtil.getBodyItemValue(doc, "SCRAPCODE", false);
////
////		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Scrap", this.getEventUser(), this.getEventComment(), "", scrapCode);
////		try
////        {
////    		//Check Machine and UnitInfo
////    		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
////    		machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
////
////    		if(StringUtil.isEmpty(productName))
////    		{
////    			throw new CustomException("PRODUCT-9001", productName);
////    		}
////    		
////    		double productQuantity = 1.0;
////
////    		Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
////
////    		if(StringUtil.equals(productData.getProductState(), "Scrapped"))
////    		{
////    			throw new CustomException("PRODUCT-0005", productName);
////    		}
////
////    		/* 20180813, Add , The glass scrap after change grade ==>> */
////    		eventInfo.setEventName("ChangeGrade");
////    		//judge
////            ChangeGradeInfo changeGradeInfo = MESProductServiceProxy.getProductInfoUtil().changeGradeInfo(productData, productData.getPosition(), GenericServiceProxy.getConstantMap().ProductGrade_S, productData.getProductProcessState(),
////                                                productData.getSubProductGrades1(), productData.getSubProductGrades2(), productData.getSubProductQuantity1(), productData.getSubProductQuantity2());
////            
////            productData = MESProductServiceProxy.getProductServiceImpl().changeGrade(productData, changeGradeInfo, eventInfo);
////    		    
////            /* 20180813, Delete , The glass scrap after change grade ==>> */
////    		//productData.setProductGrade(GenericServiceProxy.getConstantMap().ProductGrade_S);
////    		//ProductServiceProxy.getProductService().update(productData);
////            /* <<== 20180813, Delete , The glass scrap after change grade */
////            
////    		eventInfo.setEventName("Scrap");
////    		/* <<== 20180813, Add , The glass scrap after change grade */
////    		
////    		//eventInfo.setReasonCode(sReasonCode);
////
////    		Lot sLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
////    		LotKey lotKey = sLotData.getKey();
////
////    		List<ProductU> productUSequence = new ArrayList<ProductU>();
////    		ProductU productU = new ProductU();
////    		productU.setProductName(productName);
////    		productUSequence.add(productU);
////
////    		MakeScrappedInfo makeLotScrappedInfo = MESLotServiceProxy.getLotInfoUtil().makeScrappedInfo(sLotData, productQuantity, productUSequence);
////
////    	    LotServiceProxy.getLotService().makeScrapped(lotKey, eventInfo, makeLotScrappedInfo);
////
////    		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
////
////    		if(StringUtil.equals(lotData.getLotState(), "Scrapped") && StringUtil.isNotEmpty(lotData.getCarrierName()))
////    		{
////    			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
////
////    			//List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
////    			DeassignCarrierInfo createInfo =  MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, durableData, new ArrayList<ProductU>());
////
////    			//eventInfo = EventInfoUtil.makeEventInfo("DeassignCarrier", getEventUser(), getEventComment(), "", "");
////    			//MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, createInfo, eventInfo);
////    		}
////    	}
////		catch(CustomException e)
////        {
////            if(e.errorDef.getErrorCode().equals("PRODUCT-9001"))
////            {
////                eventInfo.setEventName("Scrapped");
////
////                VirtualGlass vGlassData = ExtendedObjectProxy.getVirtualGlassService().selectByKey(false, new Object[] {vcrproductName});
////
////                vGlassData.setGrade(GenericServiceProxy.getConstantMap().ProductGrade_S);
////                vGlassData.setLocation("");
////
////                ExtendedObjectProxy.getVirtualGlassService().modify(eventInfo, vGlassData);
////            }
////            else
////            {
////                throw e;
////            }
////
////        }
////	}
////	
////	
////}
