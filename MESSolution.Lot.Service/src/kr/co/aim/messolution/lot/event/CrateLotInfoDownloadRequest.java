package kr.co.aim.messolution.lot.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpecKey;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequestplan.ProductRequestPlanServiceProxy;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlan;

import org.jdom.Document;
import org.jdom.Element;

public class CrateLotInfoDownloadRequest extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {

 		Element root = doc.getDocument().getRootElement();
		Element messageNameElement = root.getChild("Header").getChild("MESSAGENAME");
		messageNameElement.setText("A_CrateLotInfoDownloadSend");

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName    = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String portType    = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		String crateName   = SMessageUtil.getBodyItemValue(doc, "CRATENAME", true);
		//String remainCrateGlassQuantity = SMessageUtil.getBodyItemValue(doc, "REMAINCRATEGLASSQUANTITY", false);

		/* 20181120, hhlee, add , Add CrateSpec Name */
		// Create Body Element
		this.generateNewBodyTemplate(doc, machineName, portName, portType, portUseType, "", crateName, "", "", "", "", "", "", "", "");

		//1. Port Validate
		Port portData = CommonUtil.getPortInfo(machineName, portName);
		SMessageUtil.setBodyItemValue(doc, "PORTNAME", portData.getKey().getPortName().toString());
		SMessageUtil.setBodyItemValue(doc, "PORTTYPE", portData.getUdfs().get("PORTTYPE"));
		SMessageUtil.setBodyItemValue(doc, "PORTUSETYPE", portData.getUdfs().get("PORTUSETYPE"));

		//2. Get Crate Data
        ConsumableKey cKey = new ConsumableKey(crateName);
        Consumable cData = ConsumableServiceProxy.getConsumableService().selectByKey(cKey);

        /* 20181120, hhlee, add , Add CrateSpec Name ==>> */
        SMessageUtil.setBodyItemValue(doc, "CRATESPECNAME", cData.getConsumableSpecName());
        /* 20181120, hhlee, add , Add CrateSpec Name ==>> */
        
        //20170121 Add by yudan *Crate HoldState Validation*
        if (cData.getUdfs().get("CONSUMABLEHOLDSTATE").equals("Y"))
        {
            throw new CustomException("CRATE-0008", crateName);
        }

        String remainCrateGlassQuantity = String.valueOf((int)cData.getQuantity());

        //3. Get Crate Spec Data
        ConsumableSpecKey cSpecKey = new ConsumableSpecKey();
        cSpecKey.setFactoryName(cData.getFactoryName().toString());
        cSpecKey.setConsumableSpecName(cData.getConsumableSpecName());
        cSpecKey.setConsumableSpecVersion("00001");
        ConsumableSpec cSpecData = ConsumableServiceProxy.getConsumableSpecService().selectByKey(cSpecKey);

        /* 20181127, hhlee, add , Add CrateSpec Name ==>> */
        SMessageUtil.setBodyItemValue(doc, "GLASSTHICKNESS", CommonUtil.getValue(cSpecData.getUdfs(), "GLASSTHICKNESS"));
        /* 20181127, hhlee, add , Add CrateSpec Name ==>> */
        

		//4. Get Plan Data / Get Product Request Data
		//ProductRequestPlan pPlanData = CommonUtil.getFirstPlanByMachine(machineName, false);
        ProductRequestPlan pPlanData = CommonUtil.getFirstPlanByCrateSpecName(cData.getConsumableSpecName().toString(), GenericServiceProxy.getConstantMap().RESV_LOT_STATE_RESV);
		if(pPlanData == null)
		{
			//throw new CustomException("PRODUCTREQUEST-0021", cData.getConsumableSpecName());
		    throw new CustomException("PRODUCTREQUEST-0002", cData.getConsumableSpecName());
		}

		String productRequestName = pPlanData.getKey().getProductRequestName();
		SMessageUtil.setBodyItemValue(doc, "WORKORDERNAME", productRequestName);

		/* 20181106, hhlee, delete Product Request Plan State(Planed -> Planed) ==>> */
		//this.ChangeWorkOrder(pPlanData);
		/* 20181106, hhlee, delete Product Request Plan State(Planed -> Planed) ==>> */
		
//		//1.1) Check Plan Hold State
//		if(pPlanData.getProductRequestHoldState().equals("Y"))
//		{
//			//throw new CustomException("PRODUCTREQUEST-0001", pPlanData.getKey().getProductRequestName() + "/" + pPlanData.getKey().getAssignedMachineName() + "/" + pPlanData.getKey().getPlanReleasedTime());
//		    throw new CustomException("PRODUCTREQUEST-0001", pPlanData.getKey().getProductRequestName() + "/" + pPlanData.getPlanReleasedTime());
//		}

		ProductRequestKey pKey = new ProductRequestKey(productRequestName);
		ProductRequest pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);

		String factoryName = pData.getFactoryName();

		//5. Get Product Spec
		ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, pData.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
		SMessageUtil.setBodyItemValue(doc, "PRODUCTSPECNAME", productSpecData.getKey().getProductSpecName());

		
		//base flow info
		ProcessFlowKey processFlowKey = new ProcessFlowKey();
		processFlowKey.setFactoryName(factoryName);
		/* 2018 hhlee, 20181017, modify ==>>*/
		String processFlowName = this.getProcessFlowNameByProductSpecPossiblePF(factoryName, productSpecData.getKey().getProductSpecName(), 
		        GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION, CommonUtil.getValue(pPlanData.getUdfs(), "ECCode"));
		processFlowKey.setProcessFlowName(productSpecData.getProcessFlowName());
		/* <<== 2018 hhlee, 20181017, modify */
		processFlowKey.setProcessFlowVersion(GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
		
		ProcessFlow processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
        
		//waiting step
		ProcessOperationSpec operationData = CommonUtil.getFirstOperation(factoryName, processFlowData.getKey().getProcessFlowName());		
		
		//6. Get Machine Recipe
		String machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(crateName);

//		//5. Get Crate Spec Data
//		ConsumableKey cKey = new ConsumableKey(crateName);
//		Consumable cData = ConsumableServiceProxy.getConsumableService().selectByKey(cKey);
//
//		//20170121 Add by yudan	*Crate HoldState Validation*
//		if (cData.getUdfs().get("CONSUMABLEHOLDSTATE").equals("Y"))
//		{
//			throw new CustomException("CRATE-0008", crateName);
//		}
//
//		String remainCrateGlassQuantity = String.valueOf((int)cData.getQuantity());

//		ConsumableSpecKey cSpecKey = new ConsumableSpecKey();
//		cSpecKey.setFactoryName(factoryName);
//		cSpecKey.setConsumableSpecName(cData.getConsumableSpecName());
//		cSpecKey.setConsumableSpecVersion("00001");
//		ConsumableSpec cSpecData = ConsumableServiceProxy.getConsumableSpecService().selectByKey(cSpecKey);

		//7. Check Crate Spec
		CommonValidation.CheckCrateSpec(factoryName, productSpecData.getKey().getProductSpecName(), cData.getConsumableSpecName(), pData.getProductRequestType());

		String productThickness = cSpecData.getUdfs().get("GLASSTHICKNESS").toString();
		String productSize = cSpecData.getUdfs().get("GLASSSIZE").toString();
		String glassMaker = cSpecData.getUdfs().get("GLASSVENDOR").toString();

//		String plan = String.valueOf(pPlanData.getPlanQuantity() - Integer.valueOf(pPlanData.getUdfs().get("oicReleasedQuantity")));
		//String plan = String.valueOf(pPlanData.getPlanQuantity() - pPlanData.getReleasedQuantity());
		SMessageUtil.setBodyItemValue(doc, "PRODUCTSIZE", productSize);
		SMessageUtil.setBodyItemValue(doc, "MACHINERECIPENAME", machineRecipeName);

		//// Create Body Element
		//this.generateBodyTemplate(doc, machineName, portData, crateName, remainCrateGlassQuantity, productSpecData.getKey().getProductSpecName(),
		//		productRequestName, plan, productThickness, productSize, glassMaker, machineRecipeName);

		//7. Check Plan Hold State
//		if(pPlanData.getProductRequestHoldState().equals(GenericServiceProxy.getConstantMap().Prq_OnHold))
//		{
//			throw new CustomException("PRODUCTREQUEST-0001", productRequestName);
//		}

		//8. Get LotName
		/* 2019110117, hhlee, modify, add condition PLANRELEASEDTIME */
		//String lotName = MESLotServiceProxy.getLotServiceUtil().getFirstReservLotByProductRequestName(machineName, productRequestName);
		String lotName = MESLotServiceProxy.getLotServiceUtil().getFirstReservLotByProductRequestName(machineName, productRequestName, pPlanData.getKey().getPlanReleasedTime());
        if(StringUtil.isEmpty(lotName))
        {
            throw new CustomException("PRODUCTREQUEST-0021", machineName);
        }
        SMessageUtil.setBodyItemValue(doc, "LOTNAME", lotName);

        Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
        //SMessageUtil.setBodyItemValue(doc, "PLANQUANTITY", String.valueOf(lotData.getCreateProductQuantity()));
        SMessageUtil.setBodyItemValue(doc, "PLANQUANTITY", String.valueOf((int)lotData.getCreateProductQuantity()));

        //9. Set ReserveLot State
        ChangeReserveLotState(machineName, lotName);

		return doc;
	}

	private void ChangeReserveLotState(String machinename , String lotname) throws CustomException
    {
        // Change Product Request Plan State
        EventInfo eventInfo = EventInfoUtil.makeEventInfo("Started", getEventUser(), getEventComment(), null, null);

        //ReserveLot reserveLot = new ReserveLot(machinename, lotname);
        ReserveLot reserveLot = ExtendedObjectProxy.getReserveLotService().selectByKey(true,  new Object[] {machinename, lotname});

        reserveLot.setReserveState("Started");

        eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
        ExtendedObjectProxy.getReserveLotService().modify(eventInfo, reserveLot);
    }


	private Element generateBodyTemplate(Document doc, String machineName, Port portData, String crateName, String remainCrateGlassQuantity,
			String ProductSpecName, String productRequestName, String plan, String productThickness, String productSize, String glassMaker,
			String machineRecipeName) throws CustomException
	{
		Element bodyElement = null;
		bodyElement = new Element("Body");

		Element machineNameElement = new Element("MACHINENAME");
		machineNameElement.setText(machineName);
		bodyElement.addContent(machineNameElement);

		Element portNameElement = new Element("PORTNAME");
		portNameElement.setText(portData.getKey().getPortName());
		bodyElement.addContent(portNameElement);

		Element portTypeElement = new Element("PORTTYPE");
		portTypeElement.setText(portData.getUdfs().get("PORTTYPE").toString());
		bodyElement.addContent(portTypeElement);

		Element portUseTypeElement = new Element("PORTUSETYPE");
		portUseTypeElement.setText(portData.getUdfs().get("PORTUSETYPE").toString());
		bodyElement.addContent(portUseTypeElement);

		Element crateNameElement = new Element("CRATENAME");
		crateNameElement.setText(crateName);
		bodyElement.addContent(crateNameElement);

		Element remainCrateGlassQuantityElement = new Element("REMAINCRATEGLASSQUANTITY");
		remainCrateGlassQuantityElement.setText(remainCrateGlassQuantity);
		bodyElement.addContent(remainCrateGlassQuantityElement);

		Element productSpecNameElement = new Element("PRODUCTSPECNAME");
		productSpecNameElement.setText(ProductSpecName);
		bodyElement.addContent(productSpecNameElement);

		Element productRequestNameElement = new Element("WORKORDERNAME");
		productRequestNameElement.setText(productRequestName);
		bodyElement.addContent(productRequestNameElement);

		Element planElement = new Element("PLANQUANTITY");
		planElement.setText(plan);
		bodyElement.addContent(planElement);

		Element productThicknessElement = new Element("PRODUCTTHICKNESS");
		productThicknessElement.setText(productThickness);
		bodyElement.addContent(productThicknessElement);

		Element productSizeElement = new Element("PRODUCTSIZE");
		productSizeElement.setText(productSize);
		bodyElement.addContent(productSizeElement);

		Element glassMakerElement = new Element("GLASSMAKER");
		glassMakerElement.setText(glassMaker);
		bodyElement.addContent(glassMakerElement);

		Element machineRecipeElement = new Element("MACHINERECIPENAME");
		machineRecipeElement.setText(machineRecipeName);
		bodyElement.addContent(machineRecipeElement);

		//first removal of existing node would be duplicated
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		//index of Body node is static
		doc.getRootElement().addContent(2, bodyElement);

		return bodyElement;
	}

	private Element generateNewBodyTemplate(Document doc, String machineName, String portName, String portType, String portUseType, String lotName, String crateName,
			String ProductSpecName, String productRequestName, String planQuantity, String plan, String productSize, String machineRecipeName, 
			String crateSpecName, String glassThickness) throws CustomException
	{
		Element bodyElement = null;
		bodyElement = new Element("Body");

		Element machineNameElement = new Element("MACHINENAME");
		machineNameElement.setText(machineName);
		bodyElement.addContent(machineNameElement);

		Element portNameElement = new Element("PORTNAME");
		portNameElement.setText(portName);
		bodyElement.addContent(portNameElement);

		Element portTypeElement = new Element("PORTTYPE");
		portTypeElement.setText(portType);
		bodyElement.addContent(portTypeElement);

		Element portUseTypeElement = new Element("PORTUSETYPE");
		portUseTypeElement.setText(portUseType);
		bodyElement.addContent(portUseTypeElement);

		Element lotNameElement = new Element("LOTNAME");
		lotNameElement.setText(lotName);
		bodyElement.addContent(lotNameElement);

		Element crateNameElement = new Element("CRATENAME");
		crateNameElement.setText(crateName);
		bodyElement.addContent(crateNameElement);

		Element productSpecNameElement = new Element("PRODUCTSPECNAME");
		productSpecNameElement.setText(ProductSpecName);
		bodyElement.addContent(productSpecNameElement);

		Element productRequestNameElement = new Element("WORKORDERNAME");
		productRequestNameElement.setText(productRequestName);
		bodyElement.addContent(productRequestNameElement);

		Element planElement = new Element("PLANQUANTITY");
		planElement.setText(plan);
		bodyElement.addContent(planElement);

		Element productSizeElement = new Element("PRODUCTSIZE");
		productSizeElement.setText(productSize);
		bodyElement.addContent(productSizeElement);

		Element machineRecipeElement = new Element("MACHINERECIPENAME");
		machineRecipeElement.setText(machineRecipeName);
		bodyElement.addContent(machineRecipeElement);
		
		/* 20181120, hhlee, add , Add CrateSpec Name ==>> */
		Element crateSpecNameElement = new Element("CRATESPECNAME");
		crateSpecNameElement.setText(crateSpecName);
        bodyElement.addContent(crateSpecNameElement);
        /* <<== 20181120, hhlee, add , Add CrateSpec Name */
        
        /* 20181127, hhlee, add , Add GLASSTHICKNESS ==>> */
        Element glassThicknessElement = new Element("GLASSTHICKNESS");
        glassThicknessElement.setText(glassThickness);
        bodyElement.addContent(glassThicknessElement);
        /* <<== 20181127, hhlee, add , Add GLASSTHICKNESS */
        
		//first removal of existing node would be duplicated
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		//index of Body node is static
		doc.getRootElement().addContent(2, bodyElement);

		return bodyElement;
	}
	
	private String getProcessFlowNameByProductSpecPossiblePF(String factoryName, String productSpecName, String productSpecVersion, String ecCode)  throws CustomException
	{
	    String processFlowName = StringUtil.EMPTY;
	    
	    String Sql = StringUtil.EMPTY;
        Sql = Sql + " SELECT PSPF.*                                        \n"
                  + "   FROM CT_PRODUCTSPECPOSSIBLEPF PSPF                 \n"
                  + "  WHERE 1=1                                           \n"
                  + "    AND PSPF.FACTORYNAME = :FACTORYNAME               \n"
                  + "    AND PSPF.PRODUCTSPECNAME =:PRODUCTSPECNAME        \n"
                  + "    AND PSPF.PRODUCTSPECVERSION = :PRODUCTSPECVERSION \n"
                  + "    AND PSPF.ECCODE = :ECCODE                           ";
        
        Map<String, Object> bindMap = new HashMap<String, Object>();
        bindMap.put("FACTORYNAME", factoryName);
        bindMap.put("PRODUCTSPECNAME", productSpecName);
        bindMap.put("PRODUCTSPECVERSION", productSpecVersion);
        bindMap.put("ECCODE", ecCode);
        
        List<Map<String, Object>> productSpecPossiblePfList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(Sql, bindMap);
        
        if ( productSpecPossiblePfList == null || productSpecPossiblePfList.size() <= 0 )
        {
            //throw new CustomException("POLICY-0022",GenericServiceProxy.getConstantMap().DEFAULT_FACTORY,unitName,operationMode);
            throw new CustomException("CRATE-9004",factoryName, productSpecName, productSpecVersion, ecCode);
        }
	    
        processFlowName = productSpecPossiblePfList.get(0).get("PROCESSFLOWNAME").toString();
        
	    return processFlowName;
	    
	}
}
