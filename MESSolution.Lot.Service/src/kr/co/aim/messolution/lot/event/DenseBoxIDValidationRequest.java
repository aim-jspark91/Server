package kr.co.aim.messolution.lot.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpecKey;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlan;

import org.jdom.Document;
import org.jdom.Element;

public class DenseBoxIDValidationRequest extends SyncHandler { 
	 
	@Override 
	public Object doWorks(Document doc) throws CustomException {
		 
 		Element root = doc.getDocument().getRootElement();
		Element messageNameElement = root.getChild("Header").getChild("MESSAGENAME");
		messageNameElement.setText("DenseBoxIDValidationReply");
 
		String crateName   = SMessageUtil.getBodyItemValue(doc, "CRATENAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName    = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String portType    = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true); 
		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		
		Port portData = CommonUtil.getPortInfo(machineName, portName);
		
		//1. Get Plan Data / Get Product Request Data
		ProductRequestPlan pPlanData = CommonUtil.getFirstPlanByMachine(machineName, false);
		if(pPlanData == null)
		{
			throw new CustomException("PRODUCTREQUEST-0021", machineName);
		}
		
		String productRequestName = pPlanData.getKey().getProductRequestName();
		
		//1.1) Check Plan Hold State
		if(pPlanData.getProductRequestPlanHoldState().equals("Y"))
		{
			throw new CustomException("PRODUCTREQUEST-0001", pPlanData.getKey().getProductRequestName() + "/" + pPlanData.getKey().getAssignedMachineName() + "/" + pPlanData.getKey().getPlanReleasedTime());
		}
		
		ProductRequestKey pKey = new ProductRequestKey(productRequestName);
		ProductRequest pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
				
		//2. Get Machine Name
		String factoryName = pData.getFactoryName();
		
		//3. Get Product Spec
		ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, pData.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);

		//base flow info
		ProcessFlowKey processFlowKey = new ProcessFlowKey();
		processFlowKey.setFactoryName(factoryName);
		processFlowKey.setProcessFlowName(productSpecData.getProcessFlowName());
		processFlowKey.setProcessFlowVersion(GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
		ProcessFlow processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
				
		//waiting step
		ProcessOperationSpec operationData = CommonUtil.getFirstOperation(factoryName, processFlowData.getKey().getProcessFlowName());

		//4. Get Machine Recipe
		String machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(pData.getFactoryName(), productSpecData.getKey().getProductSpecName(),
									processFlowData.getKey().getProcessFlowName(), operationData.getKey().getProcessOperationName(), machineName,  "");
		
		//5. Get Crate Spec Data
		ConsumableKey cKey = new ConsumableKey(crateName);
		Consumable cData = ConsumableServiceProxy.getConsumableService().selectByKey(cKey);
		
		String remainCrateGlassQuantity = String.valueOf((int)cData.getQuantity());
		
		ConsumableSpecKey cSpecKey = new ConsumableSpecKey();
		cSpecKey.setFactoryName(factoryName);
		cSpecKey.setConsumableSpecName(cData.getConsumableSpecName());
		cSpecKey.setConsumableSpecVersion("00001");
		ConsumableSpec cSpecData = ConsumableServiceProxy.getConsumableSpecService().selectByKey(cSpecKey);
		
		//6. Check Crate Spec
		CommonValidation.CheckCrateSpec(factoryName, productSpecData.getKey().getProductSpecName(), cData.getConsumableSpecName(), pData.getProductRequestType());
		
		String productThickness = cSpecData.getUdfs().get("GLASSTHICKNESS").toString();
		String productSize = cSpecData.getUdfs().get("GLASSSIZE").toString();
		String glassMaker = cSpecData.getUdfs().get("GLASSVENDOR").toString();
		
//		String plan = String.valueOf(pPlanData.getPlanQuantity() - Integer.valueOf(pPlanData.getUdfs().get("oicReleasedQuantity")));
		String plan = String.valueOf(pPlanData.getPlanQuantity() - pPlanData.getReleasedQuantity());
		
		// Create Body Element
		this.generateBodyTemplate(doc, machineName, portData, crateName, remainCrateGlassQuantity, productSpecData.getKey().getProductSpecName(), 
				productRequestName, plan, productThickness, productSize, glassMaker, machineRecipeName);
		
		//7. Check Plan Hold State
//		if(pPlanData.getProductRequestHoldState().equals(GenericServiceProxy.getConstantMap().Prq_OnHold))
//		{
//			throw new CustomException("PRODUCTREQUEST-0001", productRequestName);
//		}
		
		return doc;
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
}
