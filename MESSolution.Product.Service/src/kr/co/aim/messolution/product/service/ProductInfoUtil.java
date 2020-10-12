package kr.co.aim.messolution.product.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.MakeUnScrappedInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.info.AssignCarrierInfo;
import kr.co.aim.greentrack.product.management.info.AssignLotAndCarrierInfo;
import kr.co.aim.greentrack.product.management.info.AssignLotInfo;
import kr.co.aim.greentrack.product.management.info.AssignProcessGroupInfo;
import kr.co.aim.greentrack.product.management.info.AssignTransportGroupInfo;
import kr.co.aim.greentrack.product.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.product.management.info.ConsumeMaterialsInfo;
import kr.co.aim.greentrack.product.management.info.CreateInfo;
import kr.co.aim.greentrack.product.management.info.CreateRawInfo;
import kr.co.aim.greentrack.product.management.info.CreateWithLotInfo;
import kr.co.aim.greentrack.product.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.product.management.info.DeassignLotAndCarrierInfo;
import kr.co.aim.greentrack.product.management.info.DeassignLotInfo;
import kr.co.aim.greentrack.product.management.info.DeassignProcessGroupInfo;
import kr.co.aim.greentrack.product.management.info.DeassignTransportGroupInfo;
import kr.co.aim.greentrack.product.management.info.MakeAllocatedInfo;
import kr.co.aim.greentrack.product.management.info.MakeCompletedInfo;
import kr.co.aim.greentrack.product.management.info.MakeConsumedInfo;
import kr.co.aim.greentrack.product.management.info.MakeIdleInfo;
import kr.co.aim.greentrack.product.management.info.MakeInProductionInfo;
import kr.co.aim.greentrack.product.management.info.MakeInReworkInfo;
import kr.co.aim.greentrack.product.management.info.MakeNotInReworkInfo;
import kr.co.aim.greentrack.product.management.info.MakeNotOnHoldInfo;
import kr.co.aim.greentrack.product.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.product.management.info.MakeProcessingInfo;
import kr.co.aim.greentrack.product.management.info.MakeReceivedInfo;
import kr.co.aim.greentrack.product.management.info.MakeScrappedInfo;
import kr.co.aim.greentrack.product.management.info.MakeShippedInfo;
import kr.co.aim.greentrack.product.management.info.MakeTravelingInfo;
import kr.co.aim.greentrack.product.management.info.MakeUnShippedInfo;
import kr.co.aim.greentrack.product.management.info.RecreateInfo;
import kr.co.aim.greentrack.product.management.info.SeparateInfo;
import kr.co.aim.greentrack.product.management.info.SetAreaInfo;
import kr.co.aim.greentrack.product.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.management.info.UndoInfo;
import kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGQS;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ProductInfoUtil implements ApplicationContextAware
{
	/**
	 * @uml.property  name="applicationContext"
	 * @uml.associationEnd  
	 */
	private ApplicationContext     	applicationContext;
	private static Log 				log = LogFactory.getLog(ProductInfoUtil.class); 

	/**
	 * @param arg0
	 * @throws BeansException
	 * @uml.property  name="applicationContext"
	 */
	@Override
    public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		applicationContext = arg0;
	}

	/*
	* Name : getConsumedMaterial
	* Desc : This function is getConsumedMaterial
	* Author : AIM Systems, Inc
	* Date : 2011.01.13
	*/
	public List<ConsumedMaterial> getConsumedMaterial(List<ConsumedMaterial> consumedMaterials ) {
		List<ConsumedMaterial> consumedMaterial = new ArrayList<ConsumedMaterial>();
		
		consumedMaterial = consumedMaterials;
		
		return consumedMaterial;
	}

	/*
	* Name : assignCarrierInfo
	* Desc : This function is assignCarrierInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.13
	*/
	public  AssignCarrierInfo assignCarrierInfo( Product productData, String carrierName,
													   long position)
	{
		AssignCarrierInfo assignCarrierInfo = new AssignCarrierInfo();		
		assignCarrierInfo.setCarrierName(carrierName);
		assignCarrierInfo.setPosition(position);
		
		Map<String,String> productUdfs = productData.getUdfs();
		assignCarrierInfo.setUdfs(productUdfs);
					
		return assignCarrierInfo;
	}

	/*
	* Name : assignLotInfo
	* Desc : This function is assignLotInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.13
	*/
	public AssignLotInfo assignLotInfo( Product productData, String lotName )
	{
		AssignLotInfo assignLotInfo = new AssignLotInfo();		
		assignLotInfo.setLotName(lotName);
		
		Map<String,String> productUdfs = productData.getUdfs();
		assignLotInfo.setUdfs(productUdfs);
	
		return assignLotInfo;
	}

	/*
	* Name : assignLotAndCarrierInfo
	* Desc : This function is assignLotAndCarrierInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.13
	*/
	public AssignLotAndCarrierInfo assignLotAndCarrierInfo( Product productData, String carrierName
																   , String gradeFlag, String lotName, long position, 
																   String productProcessState)
	{
		AssignLotAndCarrierInfo assignLotAndCarrierInfo = new AssignLotAndCarrierInfo();		
		
		assignLotAndCarrierInfo.setCarrierName(carrierName);
		assignLotAndCarrierInfo.setGradeFlag(gradeFlag);
		assignLotAndCarrierInfo.setLotName(lotName);
		assignLotAndCarrierInfo.setPosition(position);
		assignLotAndCarrierInfo.setProductProcessState(productProcessState);
		
		Map<String,String> productUdfs = productData.getUdfs();
		assignLotAndCarrierInfo.setUdfs(productUdfs);
	
		return assignLotAndCarrierInfo;
	}

	/*
	* Name : assignProcessGroupInfo
	* Desc : This function is assignProcessGroupInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.13
	*/
	public AssignProcessGroupInfo assignProcessGroupInfo( Product productData, String processGroupName )
	{
		AssignProcessGroupInfo assignProcessGroupInfo = new AssignProcessGroupInfo();		
		assignProcessGroupInfo.setProcessGroupName(processGroupName);
		Map<String,String> productUdfs = productData.getUdfs();
		assignProcessGroupInfo.setUdfs(productUdfs);
		
		return assignProcessGroupInfo;
	}

	/*
	* Name : assignTransportGroupInfo
	* Desc : This function is assignTransportGroupInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.13
	*/
	public AssignTransportGroupInfo assignTransportGroupInfo( Product productData, String transportGroupName )
	{
		AssignTransportGroupInfo assignTransportGroupInfo = new AssignTransportGroupInfo();	
		assignTransportGroupInfo.setTransportGroupName(transportGroupName);
		Map<String,String> productUdfs = productData.getUdfs();
		assignTransportGroupInfo.setUdfs(productUdfs);
		
		return assignTransportGroupInfo;
	}

	/*
	* Name : changeGradeInfo
	* Desc : This function is changeGradeInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.13
	*/
	public ChangeGradeInfo changeGradeInfo( Product productData, long position, 
													String productGrade, String productProcessState, 
													String subProductGrades1, String subProductGrades2,
													double subProductQuantity1, double subProductQuantity2)
	{
		ChangeGradeInfo changeGradeInfo = new ChangeGradeInfo();	
		changeGradeInfo.setPosition(position);
		changeGradeInfo.setProductGrade(productGrade);
		changeGradeInfo.setProductProcessState(productProcessState);
		changeGradeInfo.setSubProductGrades1(subProductGrades1);
		changeGradeInfo.setSubProductGrades2(subProductGrades2);
		changeGradeInfo.setSubProductQuantity1(subProductQuantity1);
		changeGradeInfo.setSubProductQuantity2(subProductQuantity2);
		
		Map<String,String> productUdfs = productData.getUdfs();
		changeGradeInfo.setUdfs(productUdfs);
		
		return changeGradeInfo;
	}
	
	/**
	 * make ProductPGSSequence for product judge
	 * @author swcho
	 * @param productEList
	 * @return
	 * @throws CustomException
	 */
	public List<ProductPGS> getProductPGSSequence(Element productEList)
	 	throws CustomException
	 {
		 try
		 {
			 List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();
			 
			 for (Iterator prodIterator = productEList.getChildren().iterator(); prodIterator.hasNext(); )
			 {
				 Element productE = (Element) prodIterator.next();
				 
				 String productName = SMessageUtil.getChildText(productEList, "PRODUCTNAME", true);
				 
				 //comprehensive for EIS
				 String productGrade = "";
				 if(productE.getChild("PRODUCTJUDGE") != null)
					 productGrade = productE.getChildText("PRODUCTJUDGE");
				 else
					 productGrade = XmlUtil.getNodeText(productE, "PRODUCTGRADE");
				 
				 long position = Long.parseLong(XmlUtil.getNodeText(productE, "POSITION"));
				 
				 ProductPGS productPGS = new ProductPGS();
				 productPGS.setProductName(productName);
				 productPGS.setProductGrade(productGrade);
				 productPGS.setPosition(position);
				 
				 productPGSSequence.add(productPGS);
			 }
			 
			 return productPGSSequence;
		 }
		 catch (NotFoundSignal ex)
		 {
			 throw new CustomException("SYS-0001", ex.getMessage());
		 }
		 catch (FrameworkErrorSignal fe)
		 {
			 throw new CustomException("SYS-0001", fe.getMessage());
		 }
		 catch (Exception ex)
		 {
			 throw new CustomException("SYS-0001", ex.getMessage());
		 }
	 }

	/*
	* Name : createInfo
	* Desc : This function is createInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.13
	*/
	public CreateInfo createInfo( Product productData, Timestamp dueDate, String factoryName,
								String materialLocationName, String nodeStack, String originalProductName,  long priority, 
								String processFlowName, String processFlowVersion, String processGroupName, String processOperationName,
								String processOperationVersion, String productGrade, String productionType, 
								String productName, String productRequestName, String productSpec2Name,String productSpec2Version,
								String productSpecName, String productSpecVersion, String productType, 
								String sourceProductName, String subProductGrades1, String subProductGrades2, 
								double subProductQuantity1, double subProductQuantity2, String subProductType, 
								double subProductUnitQuantity1, double subProductUnitQuantity2, String transportGroupName )
	{
		CreateInfo createInfo = new CreateInfo();	
		createInfo.setDueDate(dueDate);
		createInfo.setFactoryName(factoryName);
		createInfo.setMaterialLocationName(materialLocationName);
		createInfo.setNodeStack(nodeStack);
		createInfo.setOriginalProductName(originalProductName);
		createInfo.setPriority(priority);
		createInfo.setProcessFlowName(processFlowName);
		createInfo.setProcessFlowVersion(processFlowVersion);
		createInfo.setProcessGroupName(processGroupName);
		createInfo.setProcessOperationName(processOperationName);
		createInfo.setProcessOperationVersion(processOperationVersion);
		createInfo.setProductGrade(productGrade);
		createInfo.setProductionType(productionType);
		createInfo.setProductName(productName);
		createInfo.setProductRequestName(productRequestName);
		createInfo.setProductSpec2Name(productSpec2Name);
		createInfo.setProductSpec2Version(productSpec2Version);
		createInfo.setProductSpecName(productSpecName);
		createInfo.setProductSpecVersion(productSpecVersion);
		createInfo.setProductType(productType);
		createInfo.setSourceProductName(sourceProductName);
		createInfo.setSubProductGrades1(subProductGrades1);
		createInfo.setSubProductGrades2(subProductGrades2);
		createInfo.setSubProductQuantity1(subProductQuantity1);
		createInfo.setSubProductQuantity2(subProductQuantity2);
		createInfo.setSubProductType(subProductType);
		createInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
		createInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);
		createInfo.setTransportGroupName(transportGroupName);
		
		Map<String,String> productUdfs = productData.getUdfs();
		createInfo.setUdfs(productUdfs);
		
		return createInfo;
	}

	/*
	* Name : createRawInfo
	* Desc : This function is createRawInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.13
	*/
	public CreateRawInfo createRawInfo( Product productData, String areaName, String carrierName, 
			Timestamp dueDate, String factoryName, Timestamp lastIdleTime, 
			String lastIdleUser, Timestamp lastProcessingTime, 
			String lastProcessingUser, String lotName, String machineName,
			String machineRecipeName, String materialLocationName, String nodeStack, 
			String originalProductName, long position, long priority,String processFlowName, 
			String processFlowVersion, String processGroupName, String processOperationName,
			String processOperationVersion, String productGrade,String productHoldState,
			String productionType, String productName, String productProcessState,
			String productRequestName, String productSpec2Name,String productSpec2Version,String productSpecName,
			String productSpecVersion, String productState, String productType,
			long reworkCount, String reworkFlag,String reworkNodeId, 
			String sourceProductName, String subProductGrades1, String subProductGrades2,
			double subProductQuantity1, double subProductQuantity2,
			String subProductType, double subProductUnitQuantity1, double subProductUnitQuantity2, 
			String transportGroupName	)
	{
		CreateRawInfo createRawInfo = new CreateRawInfo();	
		createRawInfo.setAreaName(areaName);
		createRawInfo.setCarrierName(carrierName);
		createRawInfo.setDueDate(dueDate);
		createRawInfo.setFactoryName(factoryName);
		createRawInfo.setLastIdleTime(lastIdleTime);
		createRawInfo.setLastIdleUser(lastIdleUser);
		createRawInfo.setLastProcessingTime(lastProcessingTime);
		createRawInfo.setLastProcessingUser(lastProcessingUser);
		createRawInfo.setLotName(lotName);
		createRawInfo.setMachineName(machineName);
		createRawInfo.setMachineRecipeName(machineRecipeName);
		createRawInfo.setMaterialLocationName(materialLocationName);
		createRawInfo.setNodeStack(nodeStack);
		createRawInfo.setOriginalProductName(originalProductName);
		createRawInfo.setPosition(position);
		createRawInfo.setPriority(priority);
		createRawInfo.setProcessFlowName(processFlowName);
		createRawInfo.setProcessFlowVersion(processFlowVersion);
		createRawInfo.setProcessGroupName(processGroupName);
		createRawInfo.setProcessOperationName(processOperationName);
		createRawInfo.setProcessOperationVersion(processOperationVersion);
		createRawInfo.setProductGrade(productGrade);
		createRawInfo.setProductHoldState(productHoldState);
		createRawInfo.setProductionType(productionType);
		createRawInfo.setProductName(productName);
		createRawInfo.setProductProcessState(productProcessState);
		createRawInfo.setProductRequestName(productRequestName);
		createRawInfo.setProductSpec2Name(productSpec2Name);
		createRawInfo.setProductSpec2Version(productSpec2Version);
		createRawInfo.setProductSpecName(productSpecName);
		createRawInfo.setProductSpecVersion(productSpecVersion);
		createRawInfo.setProductState(productState);
		createRawInfo.setProductType(productType);
		createRawInfo.setReworkCount(reworkCount);
		createRawInfo.setReworkFlag(reworkFlag);
		createRawInfo.setReworkNodeId(reworkNodeId);
		createRawInfo.setSourceProductName(sourceProductName);
		createRawInfo.setSubProductGrades1(subProductGrades1);
		createRawInfo.setSubProductGrades2(subProductGrades2);
		createRawInfo.setSubProductQuantity1(subProductQuantity1);
		createRawInfo.setSubProductQuantity2(subProductQuantity2);
		createRawInfo.setSubProductType(subProductType);
		createRawInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
		createRawInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);
		createRawInfo.setTransportGroupName(transportGroupName);
		
		Map<String,String> productUdfs = productData.getUdfs();
		createRawInfo.setUdfs(productUdfs);
	
		return createRawInfo;
	}

	/*
	* Name : createWithLotInfo
	* Desc : This function is createWithLotInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.13
	*/
	public CreateWithLotInfo createWithLotInfo( Product productData, 
			String areaName, String carrierName, Timestamp dueDate, 
			String factoryName, Timestamp lastIdleTime, String lastIdleUser, 
			Timestamp lastProcessingTime, String lastProcessingUser,
			String lotName, String materialLocationName,String nodeStack, 
			String originalProductName, long position, long priority,String processFlowName,
			String processFlowVersion, String processGroupName, String processOperationName,
			String processOperationVersion, String productGrade,String productHoldState, 
			String productionType, String productName, String productProcessState,
			String productRequestName, String productSpec2Name,String productSpec2Version,
			String productSpecName, String productSpecVersion, String productState, 
			String productType, long reworkCount, String reworkFlag,
			String reworkNodeId, String sourceProductName, String subProductGrades1,
			String subProductGrades2, double subProductQuantity1, double subProductQuantity2,
			String subProductType, double subProductUnitQuantity1, double subProductUnitQuantity2, 
			String transportGroupName	)
	{
		CreateWithLotInfo createWithLotInfo = new CreateWithLotInfo();	
		
		createWithLotInfo.setAreaName(areaName);
		createWithLotInfo.setCarrierName(carrierName);
		createWithLotInfo.setDueDate(dueDate);
		createWithLotInfo.setFactoryName(factoryName);
		createWithLotInfo.setLastIdleTime(lastIdleTime);
		createWithLotInfo.setLastIdleUser(lastIdleUser);
		createWithLotInfo.setLastProcessingTime(lastProcessingTime);
		createWithLotInfo.setLastProcessingUser(lastProcessingUser);
		createWithLotInfo.setLotName(lotName);
		createWithLotInfo.setMaterialLocationName(materialLocationName);
		createWithLotInfo.setNodeStack(nodeStack);
		createWithLotInfo.setOriginalProductName(originalProductName);
		createWithLotInfo.setPosition(position);
		createWithLotInfo.setPriority(priority);
		createWithLotInfo.setProcessFlowName(processFlowName);
		createWithLotInfo.setProcessFlowVersion(processFlowVersion);
		createWithLotInfo.setProcessGroupName(processGroupName);
		createWithLotInfo.setProcessOperationName(processOperationName);
		createWithLotInfo.setProcessOperationVersion(processOperationVersion);
		createWithLotInfo.setProductGrade(productGrade);
		createWithLotInfo.setProductHoldState(productHoldState);
		createWithLotInfo.setProductionType(productionType);
		createWithLotInfo.setProductName(productName);
		createWithLotInfo.setProductProcessState(productProcessState);
		createWithLotInfo.setProductRequestName(productRequestName);
		createWithLotInfo.setProductSpec2Name(productSpec2Name);
		createWithLotInfo.setProductSpec2Version(productSpec2Version);
		createWithLotInfo.setProductSpecName(productSpecName);
		createWithLotInfo.setProductSpecVersion(productSpecVersion);
		createWithLotInfo.setProductState(productState);
		createWithLotInfo.setProductType(productType);
		createWithLotInfo.setReworkCount(reworkCount);
		createWithLotInfo.setReworkFlag(reworkFlag);
		createWithLotInfo.setReworkNodeId(reworkNodeId);
		createWithLotInfo.setSourceProductName(sourceProductName);
		createWithLotInfo.setSubProductGrades1(subProductGrades1);
		createWithLotInfo.setSubProductGrades2(subProductGrades2);
		createWithLotInfo.setSubProductQuantity1(subProductQuantity1);
		createWithLotInfo.setSubProductQuantity2(subProductQuantity2);
		createWithLotInfo.setSubProductType(subProductType);
		createWithLotInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
		createWithLotInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);
		createWithLotInfo.setTransportGroupName(transportGroupName);
		
		Map<String,String> productUdfs = productData.getUdfs();
		createWithLotInfo.setUdfs(productUdfs);
	
		return createWithLotInfo;
	}

	/*
	* Name : consumeMaterialsInfo
	* Desc : This function is consumeMaterialsInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.13
	*/
	public ConsumeMaterialsInfo consumeMaterialsInfo( Product productData, 
			String productGrade, String subProductGrades1, String subProductGrades2, 
			 double subProductQuantity1, double subProductQuantity2 ,String factoryName, 
			 List<ConsumedMaterial> cms)
	{
		ConsumeMaterialsInfo consumeMaterialsInfo = new ConsumeMaterialsInfo();	
		consumeMaterialsInfo.setProductGrade(productGrade);
		consumeMaterialsInfo.setSubProductGrades1(subProductGrades1);
		consumeMaterialsInfo.setSubProductGrades2(subProductGrades2);
		consumeMaterialsInfo.setSubProductQuantity1(subProductQuantity1);
		consumeMaterialsInfo.setSubProductQuantity2(subProductQuantity2);
		
		Map<String,String> productUdfs = productData.getUdfs();
		consumeMaterialsInfo.setUdfs(productUdfs);
		
		//consumeMaterialsInfo.setCms(cms);
	
		return consumeMaterialsInfo;
	}

	/*
	* Name : deassignCarrierInfo
	* Desc : This function is deassignCarrierInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.13
	*/
	public DeassignCarrierInfo deassignCarrierInfo( Product productData )
	{
		DeassignCarrierInfo deassignCarrierInfo = new DeassignCarrierInfo();	
				
		Map<String,String> productUdfs = productData.getUdfs();
		deassignCarrierInfo.setUdfs(productUdfs);
		
		return deassignCarrierInfo;
	}

	/*
	* Name : deassignLotInfo
	* Desc : This function is deassignLotInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.13
	*/
	public DeassignLotInfo deassignLotInfo( Product productData )
	{
		DeassignLotInfo deassignLotInfo = new DeassignLotInfo();	
		
		Map<String,String> productUdfs = productData.getUdfs();
		deassignLotInfo.setUdfs(productUdfs);
		
		return deassignLotInfo;
	}

	/*
	* Name : deassignLotAndCarrierInfo
	* Desc : This function is deassignLotAndCarrierInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.13
	*/
	public DeassignLotAndCarrierInfo deassignLotAndCarrierInfo( Product productData )
	{
		DeassignLotAndCarrierInfo deassignLotAndCarrierInfo = new DeassignLotAndCarrierInfo();	
		
		Map<String,String> productUdfs = productData.getUdfs();
		deassignLotAndCarrierInfo.setUdfs(productUdfs);
		
		return deassignLotAndCarrierInfo;
	}

	/*
	* Name : deassignProcessGroupInfo
	* Desc : This function is deassignProcessGroupInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.13
	*/
	public DeassignProcessGroupInfo deassignProcessGroupInfo( Product productData )
	{
		DeassignProcessGroupInfo deassignProcessGroupInfo = new DeassignProcessGroupInfo();	
		
		Map<String,String> productUdfs = productData.getUdfs();
		deassignProcessGroupInfo.setUdfs(productUdfs);
		
		return deassignProcessGroupInfo;
	}

	/*
	* Name : deassignTransportGroupInfo
	* Desc : This function is deassignTransportGroupInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.17
	*/
	public DeassignTransportGroupInfo deassignTransportGroupInfo( Product productData )
	{
		DeassignTransportGroupInfo deassignTransportGroupInfo = new DeassignTransportGroupInfo();	
		
		Map<String,String> productUdfs = productData.getUdfs();
		deassignTransportGroupInfo.setUdfs(productUdfs);
		
		return deassignTransportGroupInfo;
	}

	/*
	* Name : makeAllocatedInfo
	* Desc : This function is makeAllocatedInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.17
	*/
	public MakeAllocatedInfo makeAllocatedInfo( Product productData )
	{
		MakeAllocatedInfo makeAllocatedInfo = new MakeAllocatedInfo();	
		
		Map<String,String> productUdfs = productData.getUdfs();
		makeAllocatedInfo.setUdfs(productUdfs);
		
		return makeAllocatedInfo;
	}

	/*
	* Name : makeCompletedInfo
	* Desc : This function is makeCompletedInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.17
	*/
	public MakeCompletedInfo makeCompletedInfo( Product productData )
	{
		MakeCompletedInfo makeCompletedInfo = new MakeCompletedInfo();	
		
		Map<String,String> productUdfs = productData.getUdfs();
		makeCompletedInfo.setUdfs(productUdfs);
		
		return makeCompletedInfo;
	}

	/*
	* Name : makeConsumedInfo
	* Desc : This function is makeConsumedInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.17
	*/
	public MakeConsumedInfo makeConsumedInfo( Product productData, String consumerLotName, 
													String consumerProductName , String consumerTimeKey)
	{
		MakeConsumedInfo makeConsumedInfo = new MakeConsumedInfo();	
		
		makeConsumedInfo.setConsumerLotName(consumerLotName);
		makeConsumedInfo.setConsumerProductName(consumerProductName);
		makeConsumedInfo.setConsumerTimeKey(consumerTimeKey);
		Map<String,String> productUdfs = productData.getUdfs();
		makeConsumedInfo.setUdfs(productUdfs);
		
		return makeConsumedInfo;
	}

	/*
	* Name : makeIdleInfo
	* Desc : This function is makeIdleInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.17
	*/
	public MakeIdleInfo makeIdleInfo( Product productData, String consumerLotName, String areaName,
			String branchEndNodeId , String carrierName, List<ConsumedMaterial> cms,
			String completeFlag, String machineName, String machineRecipeName,
			String nodeStack, long position, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion,
			String productGrade, String reworkFlag, String reworkNodeId,
			String subProductGrades1, String subProductGrades2, double subProductQuantity1, double subProductQuantity2)
	{
		MakeIdleInfo makeIdleInfo = new MakeIdleInfo();	

		makeIdleInfo.setAreaName(areaName);
		makeIdleInfo.setBranchEndNodeId(branchEndNodeId);
		makeIdleInfo.setCarrierName(carrierName);
		//makeIdleInfo.setCms(cms);
		makeIdleInfo.setCompleteFlag(completeFlag);
		makeIdleInfo.setMachineName(machineName);
		makeIdleInfo.setMachineRecipeName(machineRecipeName);
		makeIdleInfo.setNodeStack(nodeStack);
		makeIdleInfo.setPosition(position);
		makeIdleInfo.setProcessFlowName(processFlowName);
		makeIdleInfo.setProcessFlowVersion(processFlowVersion);
		makeIdleInfo.setProcessOperationName(processOperationName);
		makeIdleInfo.setProcessOperationVersion(processOperationVersion);
		makeIdleInfo.setProductGrade(productGrade);
		makeIdleInfo.setReworkFlag(reworkFlag);
		makeIdleInfo.setReworkNodeId(reworkNodeId);
		makeIdleInfo.setSubProductGrades1(subProductGrades1);
		makeIdleInfo.setSubProductGrades2(subProductGrades2);
		makeIdleInfo.setSubProductQuantity1(subProductQuantity1);
		makeIdleInfo.setSubProductQuantity2(subProductQuantity2);
		
		Map<String,String> productUdfs = productData.getUdfs();
		makeIdleInfo.setUdfs(productUdfs);
		
		return makeIdleInfo;
	}

	/*
	* Name : makeInProductionInfo
	* Desc : This function is makeInProductionInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.17
	*/
	public MakeInProductionInfo makeInProductionInfo( Product productData, String areaName, String carrierName,
															Timestamp dueDate , String nodeStack, long priority,
															String processFlowName, String processFlowVersion, String processOperationName,
															String processOperationVersion )
	{
		MakeInProductionInfo makeInProductionInfo = new MakeInProductionInfo();	
		makeInProductionInfo.setAreaName(areaName);
		makeInProductionInfo.setCarrierName(carrierName);
		makeInProductionInfo.setDueDate(dueDate);
		makeInProductionInfo.setNodeStack(nodeStack);
		makeInProductionInfo.setPriority(priority);
		makeInProductionInfo.setProcessFlowName(processFlowName);
		makeInProductionInfo.setProcessFlowVersion(processFlowVersion);
		makeInProductionInfo.setProcessOperationName(processOperationName);
		makeInProductionInfo.setProcessOperationVersion(processOperationVersion);
		
		Map<String,String> productUdfs = productData.getUdfs();
		makeInProductionInfo.setUdfs(productUdfs);
		
		return makeInProductionInfo;
	}

	/*
	* Name : makeInReworkInfo
	* Desc : This function is makeInReworkInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public MakeInReworkInfo makeInReworkInfo( Product productData, String areaName, String processFlowName,
													String	processFlowVersion, String processOperationName,
													String	processOperationVersion, String	nodeStack,
													String	reworkNodeId)
	{
		MakeInReworkInfo makeInReworkInfo = new MakeInReworkInfo();
		makeInReworkInfo.setAreaName(areaName);
		makeInReworkInfo.setNodeStack(nodeStack);
		makeInReworkInfo.setProcessFlowName(processFlowName);
		makeInReworkInfo.setProcessFlowVersion(processFlowVersion);
		makeInReworkInfo.setProcessOperationName(processOperationName);
		makeInReworkInfo.setProcessOperationVersion(processOperationVersion);
		makeInReworkInfo.setReworkNodeId(reworkNodeId);
		
		Map<String, String> productUdfs = productData.getUdfs();
		makeInReworkInfo.setUdfs(productUdfs);
		
		return makeInReworkInfo;
	}

	/*
	* Name : makeNotInReworkInfo
	* Desc : This function is makeNotInReworkInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public MakeNotInReworkInfo makeNotInReworkInfo( Product productData, String areaName, String nodeStack,
														String processFlowName , String processFlowVersion, 
														String processOperationName, String processOperationVersion )
	{
		MakeNotInReworkInfo makeNotInReworkInfo = new MakeNotInReworkInfo();	
		makeNotInReworkInfo.setAreaName(areaName);
		makeNotInReworkInfo.setNodeStack(nodeStack);
		makeNotInReworkInfo.setProcessFlowName(processFlowName);
		makeNotInReworkInfo.setProcessFlowVersion(processFlowVersion);
		makeNotInReworkInfo.setProcessOperationName(processOperationName);
		makeNotInReworkInfo.setProcessOperationVersion(processOperationVersion);
			
		Map<String,String> productUdfs = productData.getUdfs();
		makeNotInReworkInfo.setUdfs(productUdfs);

		return makeNotInReworkInfo;
	}

	/*
	* Name : makeNotOnHoldInfo
	* Desc : This function is makeNotOnHoldInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public MakeNotOnHoldInfo makeNotOnHoldInfo( Product productData, String areaName, String nodeStack,
			String processFlowName , String processFlowVersion, 
			String processOperationName, String processOperationVersion )
	{
		MakeNotOnHoldInfo makeNotInReworkInfo = new MakeNotOnHoldInfo();	
		
		Map<String,String> productUdfs = productData.getUdfs();
		makeNotInReworkInfo.setUdfs(productUdfs);
		
		return makeNotInReworkInfo;
	}

	/*
	* Name : makeOnHoldInfo
	* Desc : This function is makeOnHoldInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public MakeOnHoldInfo makeOnHoldInfo( Product productData, String areaName, String nodeStack,
			String processFlowName , String processFlowVersion, 
			String processOperationName, String processOperationVersion )
	{
		MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo();	
		
		Map<String,String> productUdfs = productData.getUdfs();
		makeOnHoldInfo.setUdfs(productUdfs);
		
		return makeOnHoldInfo;
	}

	/*
	* Name : makeProcessingInfo
	* Desc : This function is makeProcessingInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public MakeProcessingInfo makeProcessingInfo( Product productData, String areaName, 
														List<ConsumedMaterial> cms, String machineName , 
														String machineRecipeName ) 
			
	{
		MakeProcessingInfo makeProcessingInfo = new MakeProcessingInfo();	
		makeProcessingInfo.setAreaName(areaName);
		//makeProcessingInfo.setCms(cms);
		makeProcessingInfo.setMachineName(machineName);
		makeProcessingInfo.setMachineRecipeName(machineRecipeName);
				
		Map<String,String> productUdfs = productData.getUdfs();
		makeProcessingInfo.setUdfs(productUdfs);
		
		return makeProcessingInfo;
	}

	/*
	* Name : makeReceivedInfo
	* Desc : This function is makeReceivedInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public MakeReceivedInfo makeReceivedInfo( Product productData, String areaName, 
								String nodeStack, String processFlowName , String processFlowVersion, 
								String processOperationName,String processOperationVersion,
								String productionType, String productRequestName,String productSpec2Name,
								String productSpec2Version, String productSpecName,String productSpecVersion,
								String productType, String subProductType) 

	{
		MakeReceivedInfo makeReceivedInfo = new MakeReceivedInfo();	
		makeReceivedInfo.setAreaName(areaName);
		makeReceivedInfo.setNodeStack(nodeStack);
		makeReceivedInfo.setProcessFlowName(processFlowName);
		makeReceivedInfo.setProcessFlowVersion(processFlowVersion);
		makeReceivedInfo.setProcessOperationName(processOperationName);
		makeReceivedInfo.setProcessOperationVersion(processOperationVersion);
		makeReceivedInfo.setProductionType(productionType);
		makeReceivedInfo.setProductRequestName(productRequestName);
		makeReceivedInfo.setProductSpec2Name(productSpec2Name);
		makeReceivedInfo.setProductSpec2Version(productSpec2Version);
		makeReceivedInfo.setProductSpecName(productSpecName);
		makeReceivedInfo.setProductSpecVersion(productSpecVersion);
		makeReceivedInfo.setProductType(productType);
		makeReceivedInfo.setSubProductType(subProductType);
		
		Map<String,String> productUdfs = productData.getUdfs();
		makeReceivedInfo.setUdfs(productUdfs);
		
		return makeReceivedInfo;
	}

	/*
	* Name : makeScrappedInfo
	* Desc : This function is makeScrappedInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public MakeScrappedInfo makeScrappedInfo( Product productData, double productQuantity, 
													List<ProductU> productUSequence)
	{
		MakeScrappedInfo makeScrappedInfo = new MakeScrappedInfo();
		makeScrappedInfo.setProductQuantity(productQuantity);
		makeScrappedInfo.setProductUSequence(productUSequence);
				
		Map<String,String> productUdfs = productData.getUdfs();
		makeScrappedInfo.setUdfs(productUdfs);
		
		return makeScrappedInfo;
	}

	/*
	* Name : makeShippedInfo
	* Desc : This function is makeShippedInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public MakeShippedInfo makeShippedInfo( Product productData, String areaName, 
												   String directShipFlag, String factoryName)
	{
		MakeShippedInfo makeShippedInfo = new MakeShippedInfo();
		makeShippedInfo.setAreaName(areaName);
		makeShippedInfo.setDirectShipFlag(directShipFlag);
		makeShippedInfo.setFactoryName(factoryName);
		
		Map<String,String> productUdfs = productData.getUdfs();
		makeShippedInfo.setUdfs(productUdfs);
		
		return makeShippedInfo;
	}

	/*
	* Name : makeTravelingInfo
	* Desc : This function is makeTravelingInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public MakeTravelingInfo makeTravelingInfo( Product productData, String areaName )
	{
		MakeTravelingInfo makeTravelingInfo = new MakeTravelingInfo();
		makeTravelingInfo.setAreaName(areaName);
	
		Map<String,String> productUdfs = productData.getUdfs();
		makeTravelingInfo.setUdfs(productUdfs);
		
		return makeTravelingInfo;
	}

	/*
	* Name : makeUnScrappedInfo
	* Desc : This function is makeUnScrappedInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public MakeUnScrappedInfo makeUnScrappedInfo( Product productData, String lotProcessState, 
			 double productQuantity,List<ProductU> productUSequence )
	{
		MakeUnScrappedInfo makeUnScrappedInfo = new MakeUnScrappedInfo();
		makeUnScrappedInfo.setLotProcessState(lotProcessState);
		makeUnScrappedInfo.setProductQuantity(productQuantity);
		makeUnScrappedInfo.setProductUSequence(productUSequence);
		
		Map<String,String> productUdfs = productData.getUdfs();
		makeUnScrappedInfo.setUdfs(productUdfs); 
		
		return makeUnScrappedInfo;
	}
	
	/*
	* Name : makeUnScrappedInfoByProduct
	* Desc : This function is makeUnScrappedInfo Return Product
	* Author : AIM Systems, Inc by dmlee
	* Date : 2016.07.28
	*/
	public kr.co.aim.greentrack.product.management.info.MakeUnScrappedInfo makeUnScrappedInfoByProduct( Product productData, String productProcessState, 
														 double productQuantity,List<ProductU> productUSequence )
	{
		kr.co.aim.greentrack.product.management.info.MakeUnScrappedInfo makeUnScrappedInfo = new kr.co.aim.greentrack.product.management.info.MakeUnScrappedInfo();
		makeUnScrappedInfo.setProductProcessState(productProcessState);
		
		Map<String,String> productUdfs = productData.getUdfs();
		makeUnScrappedInfo.setUdfs(productUdfs); 
		
		return makeUnScrappedInfo;
	}

	/*
	* Name : makeUnShippedInfo
	* Desc : This function is makeUnShippedInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public MakeUnShippedInfo makeUnShippedInfo( Product productData, String areaName )
	{
		MakeUnShippedInfo makeUnShippedInfo = new MakeUnShippedInfo();
		makeUnShippedInfo.setAreaName(areaName);
		
		Map<String,String> productUdfs = productData.getUdfs();
		makeUnShippedInfo.setUdfs(productUdfs); 
		
		return makeUnShippedInfo;
	}

	/*
	* Name : recreateInfo
	* Desc : This function is recreateInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public RecreateInfo recreateInfo( Product productData, String areaName, String carrierName,
							Timestamp dueDate, String lotName,String newProductName, String nodeStack,
							long position, long priority,String processFlowName, 
							String processFlowVersion, String processOperationName, 
							String processOperationVersion,String productGrade, 
							String productionType, String productRequestName, String productSpec2Name,
							String productSpec2Version, String productSpecName,
							String productSpecVersion, String productType,String subProductGrades1,
							String subProductGrades2, double subProductQuantity1,
							double subProductQuantity2,String subProductType, 
							double subProductUnitQuantity1, double subProductUnitQuantity2)
	{
		RecreateInfo recreateInfo = new RecreateInfo();
		recreateInfo.setAreaName(areaName);
		recreateInfo.setCarrierName(carrierName);
		recreateInfo.setDueDate(dueDate);
		recreateInfo.setLotName(lotName);
		recreateInfo.setNewProductName(newProductName);
		recreateInfo.setNodeStack(nodeStack);
		recreateInfo.setPosition(position);
		recreateInfo.setPriority(priority);
		recreateInfo.setProcessFlowName(processFlowName);
		recreateInfo.setProcessFlowVersion(processFlowVersion);
		recreateInfo.setProcessOperationName(processOperationName);
		recreateInfo.setProcessOperationVersion(processOperationVersion);
		recreateInfo.setProductGrade(productGrade);
		recreateInfo.setProductionType(productionType);
		recreateInfo.setProductRequestName(productRequestName);
		recreateInfo.setProductSpec2Name(productSpec2Name);
		recreateInfo.setProductSpec2Version(productSpec2Version);
		recreateInfo.setProductSpecName(productSpecName);
		recreateInfo.setProductSpecVersion(productSpecVersion);
		recreateInfo.setProductType(productType);
		recreateInfo.setSubProductGrades1(subProductGrades1);
		recreateInfo.setSubProductGrades2(subProductGrades2);
		recreateInfo.setSubProductQuantity1(subProductQuantity1);
		recreateInfo.setSubProductQuantity2(subProductQuantity2);
		recreateInfo.setSubProductType(subProductType);
		recreateInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
		recreateInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);
				
		Map<String,String> productUdfs = productData.getUdfs();
		recreateInfo.setNewUdfs(productUdfs);
		
		return recreateInfo;
	}

	/*
	* Name : separateInfo
	* Desc : This function is separateInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.20
	*/
	public SeparateInfo separateInfo( Product productData, List<ProductPGQS> subProductPGQSSequence )
	{
		SeparateInfo separateInfo = new SeparateInfo();
		separateInfo.setSubProductPGQSSequence(subProductPGQSSequence);
		
		Map<String,String> productUdfs = productData.getUdfs();
		separateInfo.setUdfs(productUdfs);
		
		return separateInfo;
	}

	/*
	* Name : setAreaInfo
	* Desc : This function is setAreaInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.20
	*/
	public SetAreaInfo setAreaInfo( Product productData, String areaName )
	{
		SetAreaInfo setAreaInfo = new SetAreaInfo();
		setAreaInfo.setAreaName(areaName);
			
		Map<String,String> productUdfs = productData.getUdfs();
		setAreaInfo.setUdfs(productUdfs);
		
		return setAreaInfo;
	}

	/*
	* Name : setEventInfo
	* Desc : This function is setEventInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.20
	*/
	public SetEventInfo setEventInfo( Product productData, List<ProductU> productListUdfs )
	{
		SetEventInfo setEventInfo = new SetEventInfo();
		//setEventInfo.setProductUdfs(productListUdfs);
				
		Map<String,String> productUdfs = productData.getUdfs();
		setEventInfo.setUdfs(productUdfs);
		
		return setEventInfo;
	}
	
	/*
	* Name : undoInfo
	* Desc : This function is undoInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.20
	*/
	public UndoInfo undoInfo( Product productData, String carrierUndoFlag, String eventName , 
									 Timestamp eventTime, String eventTimeKey,
									 String eventUser, String  lastEventTimeKey )
	{
		UndoInfo undoInfo = new UndoInfo();
		
		undoInfo.setCarrierUndoFlag(carrierUndoFlag);
		undoInfo.setEventName( eventName );
		undoInfo.setEventTime( eventTime );
		undoInfo.setEventTimeKey( eventTimeKey );
		undoInfo.setEventUser( eventUser );
		undoInfo.setLastEventTimeKey( lastEventTimeKey );
		
		Map<String, String> productUdfs = productData.getUdfs();
		undoInfo.setUdfs(productUdfs);
		
		return undoInfo;
	}
	
	public SetMaterialLocationInfo setMaterialLocationInfo(Product productData ,String materialLocationName, Map<String, String> udfs)
	{
		SetMaterialLocationInfo setMaterialLocationInfo = new SetMaterialLocationInfo();
		setMaterialLocationInfo.setMaterialLocationName(materialLocationName);
		
		//Map<String,String> productUdfs = productData.getUdfs();
		setMaterialLocationInfo.setUdfs(udfs);
		
		return setMaterialLocationInfo;
	}

	/*
	* Name : setProductUInfo
	* Desc : This function is setProductUInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.17
	*/
	public ProductU setProductUInfo(Product productData){
		ProductU productU = new ProductU(productData.getKey().getProductName());
		productU.setUdfs(productData.getUdfs());
		return productU;
	}
	
	/*
	* Name : getProductByProductName
	* Desc : This function is getProductByProductName
	* Author : AIM Systems, Inc
	* Date : 2011.03.07
	*/
	public static Product getProductByProductName(String productName)
	{
		ProductKey productKey = new ProductKey();
		productKey.setProductName(productName);
		
		Product productData = null;
		productData = ProductServiceProxy.getProductService().selectByKey(productKey);
		
		return productData;
	}
	
	/**
	 * make ProductPGSSequence for product judge
	 * @author swcho
	 * @since 2016.05.31
	 * @param lotData
	 * @return
	 * @throws CustomException
	 */
	public List<ProductPGSRC> getProductPGSRCSequence(Lot lotData)
	 	throws CustomException
	 {
		 try
		 {
			 List<Product> productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
			 
			 List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();

			 for (Product productData : productList)
			 {
				ProductPGSRC productPGSRC = new ProductPGSRC();
				
				productPGSRC.setProductName(productData.getKey().getProductName());
					
				productPGSRC.setPosition(productData.getPosition());
				productPGSRC.setProductGrade(productData.getProductGrade());
					
				productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
				productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());

				productPGSRC.setReworkFlag("N");
					
				//productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());
				
				productPGSRC.setUdfs(productData.getUdfs());

				productPGSRCSequence.add(productPGSRC);
			 }
			 
			 return productPGSRCSequence;
		 }
		 catch (NotFoundSignal ex)
		 {
			 throw new CustomException("SYS-0001", ex.getMessage());
		 }
		 catch (FrameworkErrorSignal fe)
		 {
			 throw new CustomException("SYS-0001", fe.getMessage());
		 }
		 catch (Exception ex)
		 {
			 throw new CustomException("SYS-0001", ex.getMessage());
		 }
	 }
    
    /**
     * @Name     getUnitRecipeNames
     * @since    2018. 9. 12.
     * @author   hhlee
     * @contents 
     *           
     * @param machineName
     * @param machineRecipeName
     * @param unitName
     * @return
     */
    public String getUnitRecipeNames(String machineName, String machineRecipeName, String unitName)
    {
        String unitRecipeName = StringUtil.EMPTY;
        
        List<Recipe> unitRecipeNameList = new ArrayList<Recipe>();
        
        String condition = "WHERE MACHINENAME = ?" ;
        Object[]bindSet = new Object[]{unitName};
        
        try
        {
        	unitRecipeNameList = ExtendedObjectProxy.getRecipeService().select(condition, bindSet);
        }
        catch(Exception e)
        {
        	
        }
        
        if(unitRecipeNameList.size() > 0)
        	unitRecipeName = unitRecipeNameList.get(0).getRecipeName();
        
        return unitRecipeName;
    }
}
