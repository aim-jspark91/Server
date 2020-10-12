package kr.co.aim.messolution.lot.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.extended.object.management.data.SortJobCarrier;
import kr.co.aim.messolution.extended.object.management.data.SortJobProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.GradeDefUtil;
import kr.co.aim.messolution.generic.util.NodeStack;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.machine.service.MachineServiceUtil;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.data.LotHistoryKey;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.AssignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.AssignNewProductsInfo;
import kr.co.aim.greentrack.lot.management.info.AssignProcessGroupInfo;
import kr.co.aim.greentrack.lot.management.info.AssignProductsInfo;
import kr.co.aim.greentrack.lot.management.info.CancelWaitingToLoginInfo;
import kr.co.aim.greentrack.lot.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.ConsumeMaterialsInfo;
import kr.co.aim.greentrack.lot.management.info.CreateAndAssignAllProductsInfo;
import kr.co.aim.greentrack.lot.management.info.CreateAndCreateAllProductsInfo;
import kr.co.aim.greentrack.lot.management.info.CreateInfo;
import kr.co.aim.greentrack.lot.management.info.CreateRawInfo;
import kr.co.aim.greentrack.lot.management.info.CreateWithParentLotInfo;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.DeassignProcessGroupInfo;
import kr.co.aim.greentrack.lot.management.info.DeassignProductsInfo;
import kr.co.aim.greentrack.lot.management.info.MakeCompletedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeEmptiedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeInReworkInfo;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedInInfo;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedOutInfo;
import kr.co.aim.greentrack.lot.management.info.MakeNotInReworkInfo;
import kr.co.aim.greentrack.lot.management.info.MakeNotOnHoldInfo;
import kr.co.aim.greentrack.lot.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.lot.management.info.MakeReceivedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeReleasedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeScrappedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeShippedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeUnScrappedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeUnShippedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeWaitingToLoginInfo;
import kr.co.aim.greentrack.lot.management.info.MergeInfo;
import kr.co.aim.greentrack.lot.management.info.RecreateAndCreateAllProductsInfo;
import kr.co.aim.greentrack.lot.management.info.RecreateInfo;
import kr.co.aim.greentrack.lot.management.info.RecreateProductsInfo;
import kr.co.aim.greentrack.lot.management.info.RelocateProductsInfo;
import kr.co.aim.greentrack.lot.management.info.SeparateInfo;
import kr.co.aim.greentrack.lot.management.info.SetAreaInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.lot.management.info.SplitInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.lot.management.info.UndoInfo;
import kr.co.aim.greentrack.lot.management.info.ext.ConsumedMaterial;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineKey;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductC;
import kr.co.aim.greentrack.product.management.info.ext.ProductGSC;
import kr.co.aim.greentrack.product.management.info.ext.ProductNPGS;
import kr.co.aim.greentrack.product.management.info.ext.ProductNSubProductPGQS;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
import kr.co.aim.greentrack.product.management.info.ext.ProductRU;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class LotInfoUtil implements ApplicationContextAware {

	private static Log log = LogFactory.getLog(LotInfoUtil.class);;
	/**
	 * @uml.property name="applicationContext"
	 * @uml.associationEnd
	 */
	private ApplicationContext applicationContext;

	/**
	 * @param arg0
	 * @throws BeansException
	 * @uml.property name="applicationContext"
	 */
	@Override
    public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}

	/*
	 * Name : getAllProductPSequence Desc : This function is Get All
	 * ProductPSequence Author : AIM Systems, Inc Date : 2011.01.03
	 */
	public List<ProductP> getAllProductPSequence(Lot lotData) {
		// 1. Set Variable
		List<ProductP> productPSequence = new ArrayList<ProductP>();
		List<Product> productDatas = new ArrayList<Product>();

		// 2. Get Product Data List
		productDatas = ProductServiceProxy.getProductService()
				.allProductsByLot(lotData.getKey().getLotName());

		// 3. Get ProductName, Position By Product
		for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct
				.hasNext();) {
			Product product = iteratorProduct.next();

			ProductP productP = new ProductP();

			productP.setProductName(product.getKey().getProductName());
			productP.setPosition(product.getPosition());

			// 4. Set Udfs
			productP.setUdfs(product.getUdfs());

			// Add productPSequence By Product
			productPSequence.add(productP);
		}
		return productPSequence;
	}

	/*
	 * Name : getAllProductPGSSequence Desc : This function is Get All
	 * ProductPGSSequence Author : AIM Systems, Inc Date : 2011.01.03
	 */
	public List<ProductPGS> getAllProductPGSSequence(Lot lotData) {
		// 1. Set Variable
		List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();
		List<Product> productDatas = new ArrayList<Product>();

		// 2. Get Product Data List
		productDatas = ProductServiceProxy.getProductService()
				.allUnScrappedProductsByLot(lotData.getKey().getLotName());

		// 3. Get ProductName, Position By Product
		for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct
				.hasNext();) {
			Product product = iteratorProduct.next();

			ProductPGS productPGS = new ProductPGS();

			productPGS.setProductName(product.getKey().getProductName());
			productPGS.setPosition(product.getPosition());
			productPGS.setProductGrade(product.getProductGrade());
			productPGS.setSubProductGrades1(product.getSubProductGrades1());
			productPGS.setSubProductGrades2(product.getSubProductGrades2());
			productPGS.setSubProductQuantity1(product.getSubProductQuantity1());
			productPGS.setSubProductQuantity2(product.getSubProductQuantity2());

			productPGS.setUdfs(product.getUdfs());

			// Add productPSequence By Product
			productPGSSequence.add(productPGS);
		}
		return productPGSSequence;
	}

	/**
	 * generate ProductU sequence
	 * 
	 * @author swcho
	 * @since 2014.05.23
	 * @param lotData
	 * @return
	 * @throws CustomException
	 */
	public List<ProductU> getAllProductUSequence(Lot lotData)
			throws CustomException {
		// 1. Set Variable
		List<ProductU> ProductUSequence = new ArrayList<ProductU>();
		List<Product> productDatas = new ArrayList<Product>();

		// 2. Get Product Data List
		try {
			productDatas = ProductServiceProxy.getProductService()
					.allUnScrappedProductsByLot(lotData.getKey().getLotName());
		} catch (NotFoundSignal ne) {
			throw new CustomException("LOT-9001", lotData.getKey().getLotName());
		} catch (FrameworkErrorSignal fe) {
			throw new CustomException("LOT-9999", fe.getMessage());
		}

		// 3. Get ProductName, Position By Product
		for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct
				.hasNext();) {
			Product product = iteratorProduct.next();

			ProductU productU = new ProductU();

			productU.setProductName(product.getKey().getProductName());

			productU.setUdfs(product.getUdfs());

			// Add productPSequence By Product
			ProductUSequence.add(productU);
		}

		return ProductUSequence;
	}
	
	/*
	 * Name : assignNewProductsInfo Desc : This function is Create
	 * assignNewProductsInfo Author : AIM Systems, Inc Date : 2011.01.03
	 */
	public AssignNewProductsInfo assignNewProductsInfo(Lot lotData,
			double productQuantity, List<ProductPGS> productPGSSequence) {
		// 1. Validation

		AssignNewProductsInfo assignNewProductsInfo = new AssignNewProductsInfo();

		assignNewProductsInfo.setProductQuantity(productQuantity);
		assignNewProductsInfo.setProductPGSSequence(productPGSSequence);

		Map<String, String> lotUdfs = lotData.getUdfs();
		assignNewProductsInfo.setUdfs(lotUdfs);

		return assignNewProductsInfo;
	}

	/*
	 * Name : assignProcessGroupInfo Desc : This function is Create
	 * assignProcessGroupInfo Author : AIM Systems, Inc Date : 2011.01.03
	 */
	public AssignProcessGroupInfo assignProcessGroupInfo(Lot lotData,
			String processGroupName) {
		// 1. Validation

		AssignProcessGroupInfo assignProcessGroupInfo = new AssignProcessGroupInfo();

		assignProcessGroupInfo.setProcessGroupName(processGroupName);

		Map<String, String> lotUdfs = lotData.getUdfs();
		assignProcessGroupInfo.setUdfs(lotUdfs);

		return assignProcessGroupInfo;
	}

	/*
	 * Name : assignProductsInfo Desc : This function is Create
	 * assignProductsInfo Author : AIM Systems, Inc Date : 2011.01.03
	 */
	public AssignProductsInfo assignProductsInfo(Lot lotData, String gradeFlag,
			List<ProductP> productPSequence, double productQuantity,
			String sourceLotName, String validationFlag) {
		// 1. Validation

		AssignProductsInfo assignProductsInfo = new AssignProductsInfo();

		assignProductsInfo.setGradeFlag(gradeFlag);
		assignProductsInfo.setProductPSequence(productPSequence);
		assignProductsInfo.setProductQuantity(productQuantity);
		assignProductsInfo.setSourceLotName(sourceLotName);
		assignProductsInfo.setValidationFlag(validationFlag);

		Map<String, String> lotUdfs = lotData.getUdfs();
		assignProductsInfo.setUdfs(lotUdfs);

		return assignProductsInfo;
	}

	/*
	 * Name : cancelWaitingToLoginInfo Desc : This function is Create
	 * cancelWaitingToLoginInfo Author : AIM Systems, Inc Date : 2011.01.03
	 */
	public CancelWaitingToLoginInfo cancelWaitingToLoginInfo(Lot lotData,
			String areaName, String machineName, String machineRecipeName,
			List<ProductU> productUdfs) {
		// 1. Validation

		CancelWaitingToLoginInfo cancelWaitingToLoginInfo = new CancelWaitingToLoginInfo();

		cancelWaitingToLoginInfo.setAreaName(areaName);
		cancelWaitingToLoginInfo.setMachineName(machineName);
		cancelWaitingToLoginInfo.setMachineRecipeName(machineRecipeName);
		//cancelWaitingToLoginInfo.setProductUdfs(productUdfs);

		Map<String, String> lotUdfs = lotData.getUdfs();
		cancelWaitingToLoginInfo.setUdfs(lotUdfs);

		return cancelWaitingToLoginInfo;
	}

	/**
	 * if target not specified, system would find next automatically on same
	 * flow, but return could be not avail otherwise, once target is specified,
	 * that is better to set return than not
	 * 
	 * @author swcho
	 * @since 2015-12-07
	 * @param lotName
	 * @param productionType
	 * @param productSpecName
	 * @param productSpecVersion
	 * @param productSpec2Name
	 * @param productSpec2Version
	 * @param productRequestName
	 * @param subProductUnitQuantity1
	 * @param subProductUnitQuantity2
	 * @param dueDate
	 * @param priority
	 * @param factoryName
	 * @param areaName
	 * @param lotState
	 * @param lotProcessState
	 * @param lotHoldState
	 * @param processFlowName
	 * @param processFlowVersion
	 * @param processOperationName
	 * @param processOperationVersion
	 * @param targetFlowName
	 * @param targetOperationName
	 * @param returnFlowName
	 * @param returnOperationName
	 * @param lotUdfs
	 * @param productUdfs
	 * @param moveFlag
	 * @return
	 * @throws CustomException
	 */
	public ChangeSpecInfo changeSpecInfo(String lotName, String productionType,
			String productSpecName, String productSpecVersion,
			String productSpec2Name, String productSpec2Version,
			String productRequestName, double subProductUnitQuantity1,
			double subProductUnitQuantity2, Timestamp dueDate, long priority,
			String factoryName, String areaName, String lotState,
			String lotProcessState, String lotHoldState,
			String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion,
			String targetFlowName, String targetOperationName,
			String returnFlowName, String returnOperationName,
			String currentNodeStack, Map<String, String> lotUdfs,
			List<ProductU> productUdfs, boolean moveFlag)
			throws CustomException 
	{
		if (lotUdfs == null)
			lotUdfs = new HashMap<String, String>();

		// prepare transition info
		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo(productionType,
				productSpecName, productSpecVersion, productSpec2Name,
				productSpec2Version, productRequestName,
				subProductUnitQuantity1, subProductUnitQuantity2, dueDate,
				priority, factoryName, areaName, lotState, lotProcessState,
				lotHoldState, "", "", "", "", "", productUdfs);

		if (moveFlag) 
		{
			StringBuilder nodeStackBuilder = new StringBuilder();

			// get destination
			String postNodeId;
			try 
			{
				if (!StringUtil.isEmpty(targetFlowName) && !StringUtil.isEmpty(targetOperationName)) 
				{
					Node nextNode = ProcessFlowServiceProxy.getNodeService().getNode(factoryName, targetFlowName, "00001", "ProcessOperation", targetOperationName, "");
					postNodeId = nextNode.getKey().getNodeId();
				} 
				else 
				{
					// toward next on identical flow
					// no need of return address
					postNodeId = "";
				}
			} 
			catch (Exception ex) 
			{
				throw new CustomException("SPEC-1001", factoryName,targetFlowName, targetOperationName);
			}

			//modify 2018.05.14 hsryu 
			String priorNodeId = "";
			try 
			{
				if (!StringUtil.isEmpty(returnFlowName) && !StringUtil.isEmpty(returnOperationName)) 
				{
					lotUdfs.put("RETURNFLOWNAME", returnFlowName);
					lotUdfs.put("RETURNOPERATIONNAME", returnOperationName);

					Node returnNode = ProcessFlowServiceProxy.getNodeService().getNode(factoryName, returnFlowName, "00001", "ProcessOperation", returnOperationName, "");
					priorNodeId = returnNode.getKey().getNodeId();
				}
				else if (!StringUtil.isEmpty(currentNodeStack) && currentNodeStack.contains(".")) 
				{
					String[] arrayNodeStack = StringUtil.split(currentNodeStack, ".");

					// tail catch
					if (arrayNodeStack.length > 1) 
					{
						// departed from other flow
						for(int i=0; i<arrayNodeStack.length-1;i++)
						{
							priorNodeId += arrayNodeStack[i]+".";
						}
						//priorNodeId = arrayNodeStack[arrayNodeStack.length - 2];
					} 
					else 
					{
						// in root flow
						priorNodeId = "";
					}
				} 
				else 
				{
					priorNodeId = "";
				}
			} 
			catch (Exception ex) 
			{
				throw new CustomException("SPEC-1001", factoryName, returnFlowName, returnOperationName);
			}

			if (!StringUtil.isEmpty(postNodeId)) 
			{
				if (!StringUtil.isEmpty(priorNodeId)) 
				{
					nodeStackBuilder.append(priorNodeId);
					nodeStackBuilder.append(".");
				}

				// only allowed level-1 depth
				nodeStackBuilder.append(postNodeId);
			}

			// set trace info
			lotUdfs.put("BEFOREFLOWNAME", processFlowName);
			lotUdfs.put("BEFOREOPERATIONNAME", processOperationName);

			changeSpecInfo.setProcessFlowName("");
			changeSpecInfo.setProcessFlowVersion("");
			changeSpecInfo.setProcessOperationName("");
			changeSpecInfo.setProcessOperationVersion("");
			changeSpecInfo.setNodeStack(nodeStackBuilder.toString());
		} 
		else 
		{
			// non-movable
			changeSpecInfo.setProcessFlowName(processFlowName);
			changeSpecInfo.setProcessFlowVersion(processFlowVersion);
			changeSpecInfo.setProcessOperationName(processOperationName);
			changeSpecInfo.setProcessOperationVersion(processOperationVersion);
			// 2016.05.24
			// hjung modified
			try 
			{
				changeSpecInfo.setNodeStack(NodeStack.getNodeID(factoryName, processFlowName, processOperationName));
			} 
			catch (Exception ex) 
			{
				// throw new CustomException("SYS-9999", "Node",
				// "Not found destination");
				changeSpecInfo.setNodeStack("");
			}
		}

		changeSpecInfo.setUdfs(lotUdfs);

		return changeSpecInfo;
	}

	
	public ChangeSpecInfo changeSpecInfoForSort(String lotName, String productionType,
			String productSpecName, String productSpecVersion,
			String productSpec2Name, String productSpec2Version,
			String productRequestName, double subProductUnitQuantity1,
			double subProductUnitQuantity2, Timestamp dueDate, long priority,
			String factoryName, String areaName, String lotState,
			String lotProcessState, String lotHoldState,
			String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion,
			String targetFlowName, String targetOperationName,
			String returnFlowName, String returnOperationName,
			String currentNodeStack, Map<String, String> lotUdfs,
			List<ProductU> productUdfs, boolean moveFlag)
			throws CustomException 
	{
		if (lotUdfs == null)
			lotUdfs = new HashMap<String, String>();

		// prepare transition info
		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo(productionType,
				productSpecName, productSpecVersion, productSpec2Name,
				productSpec2Version, productRequestName,
				subProductUnitQuantity1, subProductUnitQuantity2, dueDate,
				priority, factoryName, areaName, lotState, lotProcessState,
				lotHoldState, "", "", "", "", "", productUdfs);

		if (moveFlag) 
		{
			StringBuilder nodeStackBuilder = new StringBuilder();

			// get destination
			String postNodeId;
			try 
			{
				if (!StringUtil.isEmpty(targetFlowName) && !StringUtil.isEmpty(targetOperationName)) 
				{
					Node nextNode = ProcessFlowServiceProxy.getNodeService().getNode(factoryName, targetFlowName, "00001", "ProcessOperation", targetOperationName, "");
					postNodeId = nextNode.getKey().getNodeId();
				} 
				else 
				{
					// toward next on identical flow
					// no need of return address
					postNodeId = "";
				}
			} 
			catch (Exception ex) 
			{
				throw new CustomException("SPEC-1001", factoryName,targetFlowName, targetOperationName);
			}

			//modify 2018.05.14 hsryu 
			String priorNodeId = "";
			try 
			{
				if (!StringUtil.isEmpty(returnFlowName) && !StringUtil.isEmpty(returnOperationName)) 
				{
					lotUdfs.put("RETURNFLOWNAME", returnFlowName);
					lotUdfs.put("RETURNOPERATIONNAME", returnOperationName);

					Node returnNode = ProcessFlowServiceProxy.getNodeService().getNode(factoryName, returnFlowName, "00001", "ProcessOperation", returnOperationName, "");
					priorNodeId = returnNode.getKey().getNodeId();
				}
				else if (!StringUtil.isEmpty(currentNodeStack) && currentNodeStack.contains(".")) 
				{
					String[] arrayNodeStack = StringUtil.split(currentNodeStack, ".");

					// tail catch
					if (arrayNodeStack.length > 1) 
					{
						// departed from other flow
						for(int i=0; i<arrayNodeStack.length-1;i++)
						{
							priorNodeId += arrayNodeStack[i]+".";
						}
						//priorNodeId = arrayNodeStack[arrayNodeStack.length - 2];
					} 
					else 
					{
						// in root flow
						priorNodeId = "";
					}
				} 
				else 
				{
					priorNodeId = "";
				}
			} 
			catch (Exception ex) 
			{
				throw new CustomException("SPEC-1001", factoryName, returnFlowName, returnOperationName);
			}

			if (!StringUtil.isEmpty(postNodeId)) 
			{
				if (!StringUtil.isEmpty(priorNodeId)) 
				{
					nodeStackBuilder.append(priorNodeId);
				}

				// only allowed level-1 depth
				nodeStackBuilder.append(".");
				nodeStackBuilder.append(postNodeId);
			}

			// set trace info
			lotUdfs.put("BEFOREFLOWNAME", targetFlowName);
			lotUdfs.put("BEFOREOPERATIONNAME", targetOperationName);

			changeSpecInfo.setProcessFlowName(returnFlowName);
			changeSpecInfo.setProcessFlowVersion("00001");
			changeSpecInfo.setProcessOperationName(returnOperationName);
			changeSpecInfo.setProcessOperationVersion("00001");
			//changeSpecInfo.setNodeStack(nodeStackBuilder.toString());
			changeSpecInfo.setNodeStack(priorNodeId.toString());
		} 
		else 
		{
			// non-movable
			changeSpecInfo.setProcessFlowName(processFlowName);
			changeSpecInfo.setProcessFlowVersion(processFlowVersion);
			changeSpecInfo.setProcessOperationName(processOperationName);
			changeSpecInfo.setProcessOperationVersion(processOperationVersion);
			// 2016.05.24
			// hjung modified
			try 
			{
				changeSpecInfo.setNodeStack(NodeStack.getNodeID(factoryName, processFlowName, processOperationName));
			} 
			catch (Exception ex) 
			{
				// throw new CustomException("SYS-9999", "Node",
				// "Not found destination");
				changeSpecInfo.setNodeStack("");
			}
		}

		changeSpecInfo.setUdfs(lotUdfs);

		return changeSpecInfo;
	}
	/**
	 * if target not specified, system would find next automatically on same
	 * flow, but return could be not avail otherwise, once target is specified,
	 * that is better to set return than not
	 * 
	 * @author zhongsl
	 * @since 2016-06-15
	 * @param lotName
	 * @param productionType
	 * @param productSpecName
	 * @param productSpecVersion
	 * @param productSpec2Name
	 * @param productSpec2Version
	 * @param productRequestName
	 * @param subProductUnitQuantity1
	 * @param subProductUnitQuantity2
	 * @param dueDate
	 * @param priority
	 * @param factoryName
	 * @param areaName
	 * @param lotState
	 * @param lotProcessState
	 * @param lotHoldState
	 * @param processFlowName
	 * @param processFlowVersion
	 * @param processOperationName
	 * @param processOperationVersion
	 * @param targetFlowName
	 * @param targetOperationName
	 * @param returnFlowName
	 * @param returnOperationName
	 * @param lotUdfs
	 * @param productUdfs
	 * @param moveFlag
	 * @param sortFlag
	 * @return
	 * @throws CustomException
	 */
	public ChangeSpecInfo changeSpecInfo(String lotName, String productionType,
			String productSpecName, String productSpecVersion,
			String productSpec2Name, String productSpec2Version,
			String productRequestName, double subProductUnitQuantity1,
			double subProductUnitQuantity2, Timestamp dueDate, long priority,
			String factoryName, String areaName, String lotState,
			String lotProcessState, String lotHoldState,
			String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion,
			String targetFlowName, String targetOperationName,
			String returnFlowName, String returnOperationName,
			String currentNodeStack, Map<String, String> lotUdfs,
			List<ProductU> productUdfs, boolean moveFlag, boolean sortFlag)
			throws CustomException {
		if (lotUdfs == null)
			lotUdfs = new HashMap<String, String>();

		ProcessFlow targetFlowData = null;
		ProcessFlow processFlowData = null;
		Lot lotData = null;
		lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		if (!StringUtil.isEmpty(returnFlowName) && returnFlowName != null
				&& !StringUtil.isEmpty(returnOperationName)
				&& returnOperationName != null) {
			targetFlowData = GenericServiceProxy.getSpecUtil().getProcessFlow(
					factoryName, targetFlowName, processFlowVersion);
		}

		if (!StringUtil.isEmpty(processFlowName) && processFlowName != null) {
			processFlowData = GenericServiceProxy.getSpecUtil().getProcessFlow(
					factoryName, processFlowName, processFlowVersion);
		}

		// prepare transition info
		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo(productionType,
				productSpecName, productSpecVersion, productSpec2Name,
				productSpec2Version, productRequestName,
				subProductUnitQuantity1, subProductUnitQuantity2, dueDate,
				priority, factoryName, areaName, lotState, lotProcessState,
				lotHoldState, "", "", "", "", "", productUdfs);

		if (moveFlag && sortFlag) {
			StringBuilder nodeStackBuilder = new StringBuilder();

			// get destination
			String postNodeId;
			try {
				if (!StringUtil.isEmpty(targetFlowName)
						&& !StringUtil.isEmpty(targetOperationName)) {
					Node nextNode = ProcessFlowServiceProxy
							.getNodeService()
							.getNode(factoryName, targetFlowName, "00001",
									"ProcessOperation", targetOperationName, "");
					postNodeId = nextNode.getKey().getNodeId();
				} else {
					// toward next on identical flow
					// no need of return address
					postNodeId = "";
				}
			} catch (Exception ex) {
				throw new CustomException("SPEC-1001", factoryName,
						targetFlowName, targetOperationName);
			}

			// get return
			String priorNodeId;
			try {
				if (!StringUtil.isEmpty(returnFlowName)
						&& !StringUtil.isEmpty(returnOperationName)
						&& targetFlowData.getProcessFlowType().equals("Sort")
						&& !lotData.getNodeStack().isEmpty()) {
					lotUdfs.put("FIRSTRETURNFLOWNAME", returnFlowName);
					lotUdfs.put("FIRSTRETURNOPERATIONNAME", returnOperationName);

					priorNodeId = lotData.getNodeStack();
				}

				else if (StringUtil.isEmpty(returnFlowName)
						&& StringUtil.isEmpty(returnOperationName)
						&& processFlowData.getProcessFlowType().equals("Sort")
						&& !StringUtil.isEmpty(currentNodeStack)
						&& currentNodeStack.contains(".")) {
					lotUdfs.put("FIRSTRETURNFLOWNAME", "");
					lotUdfs.put("FIRSTRETURNOPERATIONNAME", "");

					String[] arrayNodeStack = StringUtil.split(
							currentNodeStack, ".");

					if (arrayNodeStack.length > 2) {
						// priorNodeId = arrayNodeStack[arrayNodeStack.length -
						// 3];
						priorNodeId = arrayNodeStack[0];
					} else {
						priorNodeId = "";
					}
				} else {
					priorNodeId = "";
				}
			} catch (Exception ex) {
				throw new CustomException("SPEC-1001", factoryName,
						returnFlowName, returnOperationName);
			}

			if (!StringUtil.isEmpty(postNodeId)) {
				if (!StringUtil.isEmpty(priorNodeId)) {
					nodeStackBuilder.append(priorNodeId);
					nodeStackBuilder.append(".");
				}

				// only allowed level-1 depth
				nodeStackBuilder.append(postNodeId);
			}

			// set trace info
			lotUdfs.put("BEFOREFLOWNAME", processFlowName);
			lotUdfs.put("BEFOREOPERATIONNAME", processOperationName);

			changeSpecInfo.setProcessFlowName("");
			changeSpecInfo.setProcessFlowVersion("");
			changeSpecInfo.setProcessOperationName("");
			changeSpecInfo.setProcessOperationVersion("");
			changeSpecInfo.setNodeStack(nodeStackBuilder.toString());
		} else {
			// non-movable
			changeSpecInfo.setProcessFlowName(processFlowName);
			changeSpecInfo.setProcessFlowVersion(processFlowVersion);
			changeSpecInfo.setProcessOperationName(processOperationName);
			changeSpecInfo.setProcessOperationVersion(processOperationVersion);

			try {
				changeSpecInfo.setNodeStack(NodeStack.getNodeID(factoryName,
						processFlowName, processOperationName));
			} catch (Exception ex) {
				// throw new CustomException("SYS-9999", "Node",
				// "Not found destination");
				changeSpecInfo.setNodeStack("");
			}
		}

		changeSpecInfo.setUdfs(lotUdfs);

		return changeSpecInfo;
	}

	/**
	 * This function is Make ChangeSpecInfo - skip by sampling 150307 by swcho :
	 * modified to take just one step
	 * 
	 * @author swcho
	 * @since 2014.07.31
	 * @param lotData
	 * @param udfs
	 * @param productUdfs
	 * @return
	 * @throws CustomException
	 */
	public ChangeSpecInfo skipInfo(Lot lotData, Map<String, String> udfs,
			List<ProductU> productUdfs) throws CustomException {
		ChangeSpecInfo skipInfo = new ChangeSpecInfo();

		skipInfo.setProductionType(lotData.getProductionType());
		skipInfo.setProductSpecName(lotData.getProductSpecName());
		skipInfo.setProductSpecVersion(lotData.getProductSpecVersion());
		skipInfo.setProductSpec2Name(lotData.getProductSpec2Name());
		skipInfo.setProductSpec2Version(lotData.getProductSpec2Version());

		skipInfo.setProductRequestName(lotData.getProductRequestName());

		skipInfo.setSubProductUnitQuantity1(lotData
				.getSubProductUnitQuantity1());
		skipInfo.setSubProductUnitQuantity2(lotData
				.getSubProductUnitQuantity2());

		skipInfo.setDueDate(lotData.getDueDate());
		skipInfo.setPriority(lotData.getPriority());

		skipInfo.setFactoryName(lotData.getFactoryName());
		skipInfo.setAreaName(lotData.getAreaName());

		skipInfo.setProcessFlowName(lotData.getProcessFlowName());
		skipInfo.setProcessFlowVersion(lotData.getProcessFlowVersion());

		// NEXT OPERATION INFO
		// to next operation
		{
			// current node stack
			String[] arrayNodeStack = StringUtil.split(lotData.getNodeStack(),
					".");

			StringBuilder strBuilder = new StringBuilder();
			for (int idx = 0; idx < (arrayNodeStack.length - 1); idx++) {
				if (strBuilder.length() > 0)
					strBuilder.append(".");

				strBuilder.append(arrayNodeStack[idx]);
			}

			Node targetNode = ProcessFlowServiceProxy.getProcessFlowService()
					.getNextNode(arrayNodeStack[arrayNodeStack.length - 1],
							"Normal", "");

			// at end of flow
			if (!targetNode.getNodeType().equalsIgnoreCase("End")) {
				if (strBuilder.length() > 0)
					strBuilder.append(".");
				strBuilder.append(targetNode.getKey().getNodeId());
			} else {
				if (arrayNodeStack.length < 2)
					throw new CustomException("SYS-1501",
							"there no destination to return");
			}

			skipInfo.setProcessOperationName("");
			skipInfo.setProcessOperationVersion("");
			skipInfo.setNodeStack(strBuilder.toString());
		}

		// trace setting
		udfs.put("BEFOREFLOWNAME", lotData.getProcessFlowName());
		udfs.put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());

		skipInfo.setUdfs(udfs);
		//skipInfo.setProductUdfs(productUdfs);

		return skipInfo;
	}

	/*
	 * Name : consumeMaterialsInfo Desc : This function is Create
	 * consumeMaterialsInfo Author : AIM Systems, Inc Date : 2011.01.03
	 */
	public ConsumeMaterialsInfo consumeMaterialsInfo(Lot lotData,
			List<ConsumedMaterial> consumedMaterialSequence,
			List<ProductGSC> productGSCSequence) {
		// 1. Validation

		ConsumeMaterialsInfo consumeMaterialsInfo = new ConsumeMaterialsInfo();

		consumeMaterialsInfo
				.setConsumedMaterialSequence(consumedMaterialSequence);
		consumeMaterialsInfo.setLotGrade(lotData.getLotGrade());
		consumeMaterialsInfo.setProductGSCSequence(productGSCSequence);

		Map<String, String> lotUdfs = lotData.getUdfs();
		consumeMaterialsInfo.setUdfs(lotUdfs);

		return consumeMaterialsInfo;
	}

	/*
	 * Name : createInfo Desc : This function is Create createInfo Author : AIM
	 * Systems, Inc Date : 2011.01.03
	 */
	public CreateInfo createInfo(Timestamp dueDate, String factoryName,
			String lotName, String nodeStack, long priority,
			String processFlowName, String processFlowVersion,
			String processGroupName, String processOperationName,
			String processOperationVersion, String productionType,
			double productQuantity, String productRequestName,
			String productSpec2Name, String productSpec2Version,
			String productSpecName, String productSpecVersion,
			String productType, String subProductType,
			double subProductUnitQuantity1, double subProductUnitQuantity2,
			Map<String, String> lotUdfs) {
		// Set Variable
		String lotGrade = GradeDefUtil.getGrade(
				GenericServiceProxy.getConstantMap().DEFAULT_FACTORY,
				GenericServiceProxy.getConstantMap().GradeType_Lot, true)
				.getGrade();

		if (dueDate == null)
			dueDate = TimeUtils.getCurrentTimestamp();

		CreateInfo createInfo = new CreateInfo();

		createInfo.setDueDate(dueDate);
		createInfo.setFactoryName(factoryName);
		createInfo.setLotGrade(lotGrade);
		createInfo.setLotName(lotName);
		createInfo.setNodeStack(nodeStack);
		createInfo.setPriority(priority);
		createInfo.setProcessFlowName(processFlowName);
		createInfo.setProcessFlowVersion(processFlowVersion);
		createInfo.setProcessGroupName(processGroupName);
		createInfo.setProcessOperationName(processOperationName);
		createInfo.setProcessOperationVersion(processOperationVersion);
		createInfo.setProductionType(productionType);
		createInfo.setProductQuantity(productQuantity);
		createInfo.setProductRequestName(productRequestName);
		createInfo.setProductSpec2Name(productSpec2Name);
		createInfo.setProductSpec2Version(productSpec2Version);
		createInfo.setProductSpecName(productSpecName);
		createInfo.setProductSpecVersion(productSpecVersion);
		createInfo.setProductType(productType);
		createInfo.setSubProductType(subProductType);
		createInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
		createInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);

		createInfo.setUdfs(lotUdfs);

		return createInfo;
	}
	/*
	 * Name : createInfo Desc : This function is Create createInfo Author : AIM
	 * Systems, Inc Date : 2014.04.29
	 */
	public CreateRawInfo createRawInfo(Timestamp dueDate,
			String destinationCarrierName, String factoryName, String lotGrade,
			String lotName, String lotState, String lotProcessState,
			String lotHoldState, String nodeStack, long priority,
			String processFlowName, String processFlowVersion,
			String processGroupName, String processOperationName,
			String processOperationVersion, String productionType,
			double productQuantity, String productRequestName,
			String productSpec2Name, String productSpec2Version,
			String productSpecName, String productSpecVersion,
			String productType, String subProductType,
			double subProductUnitQuantity1, double subProductUnitQuantity2,
			Lot lotData) {
		CreateRawInfo createRawInfo = new CreateRawInfo();

		createRawInfo.setDueDate(dueDate);
		createRawInfo.setCarrierName(destinationCarrierName);
		createRawInfo.setFactoryName(factoryName);
		createRawInfo.setLotGrade(lotGrade);
		createRawInfo.setLotName(lotName);
		createRawInfo.setLotState(lotState);
		createRawInfo.setLotProcessState(lotProcessState);
		createRawInfo.setLotHoldState(lotHoldState);
		createRawInfo.setNodeStack(nodeStack);
		createRawInfo.setPriority(priority);
		createRawInfo.setProcessFlowName(processFlowName);
		createRawInfo.setProcessFlowVersion(processFlowVersion);
		createRawInfo.setProcessGroupName(processGroupName);
		createRawInfo.setProcessOperationName(processOperationName);
		createRawInfo.setProcessOperationVersion(processOperationVersion);
		createRawInfo.setProductionType(productionType);
		createRawInfo.setProductQuantity(productQuantity);
		createRawInfo.setProductRequestName(productRequestName);
		createRawInfo.setProductSpec2Name(productSpec2Name);
		createRawInfo.setProductSpec2Version(productSpec2Version);
		createRawInfo.setProductSpecName(productSpecName);
		createRawInfo.setProductSpecVersion(productSpecVersion);
		createRawInfo.setProductType(productType);
		createRawInfo.setSubProductType(subProductType);
		createRawInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
		createRawInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);
		createRawInfo.setUdfs(lotData.getUdfs());

		return createRawInfo;
	}

	/*
	 * Name : createInfo Desc : This function is Create createInfo Author : AIM
	 * Systems, Inc Date : 2014.04.29
	 */
	public CreateWithParentLotInfo createWithParentLotInfo(String areaName,
			String assignCarrierFlag, Map<String, String> assignCarrierUdfs,
			String carrierName, Timestamp dueDate, String factoryName,
			Timestamp lastLoggedInTime, String lastLoggedInUser,
			Timestamp lastLoggedOutTime, String lastLoggedOutUser,
			String lotGrade, String lotHoldState, String lotName,
			String lotProcessState, String lotState, String machineName,
			String machineRecipeName, String nodeStack, String originalLotName,
			long priority, String processFlowName, String processFlowVersion,
			String processGroupName, String processOperationName,
			String processOperationVersion, String productionType,
			List<ProductP> productPSequence, double productQuantity,
			String productRequestName, String productSpec2Name,
			String productSpec2Version, String productSpecName,
			String productSpecVersion, String productType, long reworkCount,
			String reworkFlag, String reworkNodeId, String rootLotName,
			String sourceLotName, String subProductType,
			double subProductUnitQuantity1, double subProductUnitQuantity2,
			Map<String, String> udfs, Lot lotData) {
		CreateWithParentLotInfo createWithParentLotInfo = new CreateWithParentLotInfo();

		createWithParentLotInfo.setAreaName(areaName);
		createWithParentLotInfo.setAssignCarrierFlag(assignCarrierFlag);
		createWithParentLotInfo.setAssignCarrierUdfs(assignCarrierUdfs);
		createWithParentLotInfo.setCarrierName(carrierName);
		createWithParentLotInfo.setDueDate(dueDate);
		createWithParentLotInfo.setFactoryName(factoryName);
		createWithParentLotInfo.setLastLoggedInTime(lastLoggedInTime);
		createWithParentLotInfo.setLastLoggedInUser(lastLoggedInUser);
		createWithParentLotInfo.setLastLoggedOutTime(lastLoggedOutTime);
		createWithParentLotInfo.setLastLoggedOutUser(lastLoggedOutUser);
		createWithParentLotInfo.setLotGrade(lotGrade);
		createWithParentLotInfo.setLotHoldState(lotHoldState);
		createWithParentLotInfo.setLotName(lotName);
		createWithParentLotInfo.setLotProcessState(lotProcessState);
		createWithParentLotInfo.setLotState(lotState);
		createWithParentLotInfo.setMachineName(machineName);
		createWithParentLotInfo.setMachineRecipeName(machineRecipeName);
		createWithParentLotInfo.setNodeStack(nodeStack);
		createWithParentLotInfo.setOriginalLotName(originalLotName);
		createWithParentLotInfo.setPriority(priority);
		createWithParentLotInfo.setProcessFlowName(processFlowName);
		createWithParentLotInfo.setProcessFlowVersion(processFlowVersion);
		createWithParentLotInfo.setProcessGroupName(processGroupName);
		createWithParentLotInfo.setProcessOperationName(processOperationName);
		createWithParentLotInfo
				.setProcessOperationVersion(processOperationVersion);
		createWithParentLotInfo.setProductionType(productionType);
		createWithParentLotInfo.setProductPSequence(productPSequence);
		createWithParentLotInfo.setProductQuantity(productQuantity);
		createWithParentLotInfo.setProductRequestName(productRequestName);
		createWithParentLotInfo.setProductSpec2Name(productSpec2Name);
		createWithParentLotInfo.setProductSpec2Version(productSpec2Version);
		createWithParentLotInfo.setProductSpecName(productSpecName);
		createWithParentLotInfo.setProductSpecVersion(productSpecVersion);
		createWithParentLotInfo.setProductType(productType); // modify by jhyeom
																// 2014-12-04
																// subProductType
																// ->
																// ProductType
		createWithParentLotInfo.setReworkCount(reworkCount);
		createWithParentLotInfo.setReworkFlag(reworkFlag);
		createWithParentLotInfo.setReworkNodeId(reworkNodeId);
		createWithParentLotInfo.setRootLotName(rootLotName);
		createWithParentLotInfo.setSourceLotName(sourceLotName);
		createWithParentLotInfo.setSubProductType(subProductType);
		createWithParentLotInfo
				.setSubProductUnitQuantity1(subProductUnitQuantity1);
		createWithParentLotInfo
				.setSubProductUnitQuantity2(subProductUnitQuantity2);
		createWithParentLotInfo.setUdfs(udfs);

		return createWithParentLotInfo;
	}

	/*
	 * Name : createAndAssignAllProductsInfo Desc : This function is Create
	 * CreateAndAssignAllProductsInfo Author : AIM Systems, Inc Date :
	 * 2011.01.04
	 */
	public CreateAndAssignAllProductsInfo createAndAssignAllProductsInfo(
			Lot lotData, String areaName, String assignCarrierFlag,
			Map<String, String> assignCarrierUdfs, String carrierName,
			Timestamp dueDate, String factoryName, Timestamp lastLoggedInTime,
			String lastLoggedInUser, Timestamp lastLoggedOutTime,
			String lastLoggedOutUser, String lotGrade, String lotName,
			String machineName, String machineRecipeName, String nodeStack,
			String originalLotName, long priority, String processFlowName,
			String processFlowVersion, String processGroupName,
			String processOperationName, String processOperationVersion,
			String productionType, List<ProductP> productPSequence,
			double productQuantity, String productRequestName,
			String productSpec2Name, String productSpec2Version,
			String productSpecName, String productSpecVersion,
			String productType, long reworkCount, String reworkFlag,
			String reworkNodeId, String sourceLotName, String subProductType,
			double subProductUnitQuantity1, double subProductUnitQuantity2) {
		// 1. Validation

		CreateAndAssignAllProductsInfo createAndAssignAllProductsInfo = new CreateAndAssignAllProductsInfo();

		createAndAssignAllProductsInfo.setAreaName(areaName);
		createAndAssignAllProductsInfo.setAssignCarrierFlag(assignCarrierFlag);
		createAndAssignAllProductsInfo.setAssignCarrierUdfs(assignCarrierUdfs);
		createAndAssignAllProductsInfo.setCarrierName(carrierName);
		createAndAssignAllProductsInfo.setDueDate(dueDate);
		createAndAssignAllProductsInfo.setFactoryName(factoryName);
		createAndAssignAllProductsInfo.setLastLoggedInTime(lastLoggedInTime);
		createAndAssignAllProductsInfo.setLastLoggedInUser(lastLoggedInUser);
		createAndAssignAllProductsInfo.setLastLoggedOutTime(lastLoggedOutTime);
		createAndAssignAllProductsInfo.setLastLoggedOutUser(lastLoggedOutUser);
		createAndAssignAllProductsInfo.setLotGrade(lotGrade);
		createAndAssignAllProductsInfo.setLotName(lotName);
		createAndAssignAllProductsInfo.setMachineName(machineName);
		createAndAssignAllProductsInfo.setMachineRecipeName(machineRecipeName);
		createAndAssignAllProductsInfo.setNodeStack(nodeStack);
		createAndAssignAllProductsInfo.setOriginalLotName(originalLotName);
		createAndAssignAllProductsInfo.setPriority(priority);
		createAndAssignAllProductsInfo.setProcessFlowName(processFlowName);
		createAndAssignAllProductsInfo
				.setProcessFlowVersion(processFlowVersion);
		createAndAssignAllProductsInfo.setProcessGroupName(processGroupName);
		createAndAssignAllProductsInfo
				.setProcessOperationName(processOperationName);
		createAndAssignAllProductsInfo
				.setProcessOperationVersion(processOperationVersion);
		createAndAssignAllProductsInfo.setProductionType(productionType);
		createAndAssignAllProductsInfo.setProductPSequence(productPSequence);
		createAndAssignAllProductsInfo.setProductQuantity(productQuantity);
		createAndAssignAllProductsInfo
				.setProductRequestName(productRequestName);
		createAndAssignAllProductsInfo.setProductSpec2Name(productSpec2Name);
		createAndAssignAllProductsInfo
				.setProductSpec2Version(productSpec2Version);
		createAndAssignAllProductsInfo.setProductSpecName(productSpecName);
		createAndAssignAllProductsInfo
				.setProductSpecVersion(productSpecVersion);
		createAndAssignAllProductsInfo.setProductType(productType);
		createAndAssignAllProductsInfo.setReworkCount(reworkCount);
		createAndAssignAllProductsInfo.setReworkFlag(reworkFlag);
		createAndAssignAllProductsInfo.setReworkNodeId(reworkNodeId);
		createAndAssignAllProductsInfo.setSourceLotName(sourceLotName);
		createAndAssignAllProductsInfo.setSubProductType(subProductType);
		createAndAssignAllProductsInfo
				.setSubProductUnitQuantity1(subProductUnitQuantity1);
		createAndAssignAllProductsInfo
				.setSubProductUnitQuantity2(subProductUnitQuantity2);

		Map<String, String> lotUdfs = lotData.getUdfs();
		createAndAssignAllProductsInfo.setUdfs(lotUdfs);

		return createAndAssignAllProductsInfo;
	}

	/*
	 * Name : createAndCreateAllProductsInfo Desc : This function is Create
	 * CreateAndCreateAllProductsInfo Author : AIM Systems, Inc Date :
	 * 2011.01.04
	 */
	public CreateAndCreateAllProductsInfo createAndCreateAllProductsInfo(
			Lot lotData, String areaName, String assignCarrierFlag,
			Map<String, String> assignCarrierUdfs, String carrierName,
			Timestamp dueDate, String factoryName, Timestamp lastLoggedInTime,
			String lastLoggedInUser, Timestamp lastLoggedOutTime,
			String lastLoggedOutUser, String lotGrade, String lotName,
			String machineName, String machineRecipeName, String nodeStack,
			String originalLotName, long priority, String processFlowName,
			String processFlowVersion, String processGroupName,
			String processOperationName, String processOperationVersion,
			String productionType, List<ProductPGS> productPGSSequence,
			double productQuantity, String productRequestName,
			String productSpec2Name, String productSpec2Version,
			String productSpecName, String productSpecVersion,
			String productType, long reworkCount, String reworkFlag,
			String reworkNodeId, String sourceLotName, String subProductType,
			double subProductUnitQuantity1, double subProductUnitQuantity2) {
		// 1. Validation

		CreateAndCreateAllProductsInfo createAndCreateAllProductsInfo = new CreateAndCreateAllProductsInfo();

		createAndCreateAllProductsInfo.setAreaName(areaName);
		createAndCreateAllProductsInfo.setAssignCarrierFlag(assignCarrierFlag);
		createAndCreateAllProductsInfo.setAssignCarrierUdfs(assignCarrierUdfs);
		createAndCreateAllProductsInfo.setCarrierName(carrierName);
		createAndCreateAllProductsInfo.setDueDate(dueDate);
		createAndCreateAllProductsInfo.setFactoryName(factoryName);
		createAndCreateAllProductsInfo.setLastLoggedInTime(lastLoggedInTime);
		createAndCreateAllProductsInfo.setLastLoggedInUser(lastLoggedInUser);
		createAndCreateAllProductsInfo.setLastLoggedOutTime(lastLoggedOutTime);
		createAndCreateAllProductsInfo.setLastLoggedOutUser(lastLoggedOutUser);
		createAndCreateAllProductsInfo.setLotGrade(lotGrade);
		createAndCreateAllProductsInfo.setLotName(lotName);
		createAndCreateAllProductsInfo.setMachineName(machineName);
		createAndCreateAllProductsInfo.setMachineRecipeName(machineRecipeName);
		createAndCreateAllProductsInfo.setNodeStack(nodeStack);
		createAndCreateAllProductsInfo.setOriginalLotName(originalLotName);
		createAndCreateAllProductsInfo.setPriority(priority);
		createAndCreateAllProductsInfo.setProcessFlowName(processFlowName);
		createAndCreateAllProductsInfo
				.setProcessFlowVersion(processFlowVersion);
		createAndCreateAllProductsInfo.setProcessGroupName(processGroupName);
		createAndCreateAllProductsInfo
				.setProcessOperationName(processOperationName);
		createAndCreateAllProductsInfo
				.setProcessOperationVersion(processOperationVersion);
		createAndCreateAllProductsInfo.setProductionType(productionType);
		createAndCreateAllProductsInfo
				.setProductPGSSequence(productPGSSequence);
		createAndCreateAllProductsInfo.setProductQuantity(productQuantity);
		createAndCreateAllProductsInfo
				.setProductRequestName(productRequestName);
		createAndCreateAllProductsInfo.setProductSpec2Name(productSpec2Name);
		createAndCreateAllProductsInfo
				.setProductSpec2Version(productSpec2Version);
		createAndCreateAllProductsInfo.setProductSpecName(productSpecName);
		createAndCreateAllProductsInfo
				.setProductSpecVersion(productSpecVersion);
		createAndCreateAllProductsInfo.setProductType(productType);
		createAndCreateAllProductsInfo.setReworkCount(reworkCount);
		createAndCreateAllProductsInfo.setReworkFlag(reworkFlag);
		createAndCreateAllProductsInfo.setReworkNodeId(reworkNodeId);
		createAndCreateAllProductsInfo.setSourceLotName(sourceLotName);
		createAndCreateAllProductsInfo.setSubProductType(subProductType);
		createAndCreateAllProductsInfo
				.setSubProductUnitQuantity1(subProductUnitQuantity1);
		createAndCreateAllProductsInfo
				.setSubProductUnitQuantity2(subProductUnitQuantity2);

		Map<String, String> lotUdfs = lotData.getUdfs();
		createAndCreateAllProductsInfo.setUdfs(lotUdfs);

		return createAndCreateAllProductsInfo;
	}

	/*
	 * Name : createRawInfo Desc : This function is Create createRawInfo Author
	 * : AIM Systems, Inc Date : 2011.01.04
	 */
	public CreateRawInfo createRawInfo(Lot lotData, String areaName,
			String assignCarrierFlag, Map<String, String> assignCarrierUdfs,
			String carrierName, Timestamp dueDate, String factoryName,
			Timestamp lastLoggedInTime, String lastLoggedInUser,
			String lotGrade, String lotHoldState, String lotName,
			String lotProcessState, String lotState, String machineName,
			String machineRecipeName, String nodeStack, String originalLotName,
			long priority, String processFlowName, String processFlowVersion,
			String processGroupName, String processOperationName,
			String processOperationVersion, String productionType,
			List<ProductP> productPSequence, double productQuantity,
			String productRequestName, String productSpec2Name,
			String productSpec2Version, String productSpecName,
			String productSpecVersion, String productType, long reworkCount,
			String reworkFlag, String reworkNodeId, String sourceLotName,
			String subProductType, double subProductUnitQuantity1,
			double subProductUnitQuantity2) {
		// 1. Validation

		CreateRawInfo createRawInfo = new CreateRawInfo();

		createRawInfo.setAreaName(areaName);
		createRawInfo.setAssignCarrierFlag(assignCarrierFlag);
		createRawInfo.setAssignCarrierUdfs(assignCarrierUdfs);
		createRawInfo.setCarrierName(carrierName);
		createRawInfo.setDueDate(dueDate);
		createRawInfo.setFactoryName(factoryName);
		createRawInfo.setLastLoggedInTime(lastLoggedInTime);
		createRawInfo.setLastLoggedInUser(lastLoggedInUser);
		createRawInfo.setLotGrade(lotGrade);
		createRawInfo
				.setLotHoldState(GenericServiceProxy.getConstantMap().Lot_NotOnHold);
		createRawInfo.setLotName(lotName);
		createRawInfo.setLotProcessState(lotProcessState);
		createRawInfo.setLotState(lotState);
		createRawInfo.setMachineName(machineName);
		createRawInfo.setMachineRecipeName(machineRecipeName);
		createRawInfo.setNodeStack(nodeStack);
		createRawInfo.setOriginalLotName(originalLotName);
		createRawInfo.setPriority(priority);
		createRawInfo.setProcessFlowName(processFlowName);
		createRawInfo.setProcessFlowVersion(processFlowVersion);
		createRawInfo.setProcessGroupName(processGroupName);
		createRawInfo.setProcessOperationName(processOperationName);
		createRawInfo.setProcessOperationVersion(processOperationVersion);
		createRawInfo.setProductionType(productionType);
		createRawInfo.setProductPSequence(productPSequence);
		createRawInfo.setProductQuantity(productQuantity);
		createRawInfo.setProductRequestName(productRequestName);
		createRawInfo.setProductSpec2Name(productSpec2Name);
		createRawInfo.setProductSpec2Version(productSpec2Version);
		createRawInfo.setProductSpecName(productSpecName);
		createRawInfo.setProductSpecVersion(productSpecVersion);
		createRawInfo.setProductType(productType);
		createRawInfo.setReworkCount(reworkCount);
		createRawInfo.setReworkFlag(reworkFlag);
		createRawInfo.setReworkNodeId(reworkNodeId);
		createRawInfo.setSourceLotName(sourceLotName);
		createRawInfo.setSubProductType(subProductType);
		createRawInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
		createRawInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);

		Map<String, String> lotUdfs = lotData.getUdfs();
		createRawInfo.setUdfs(lotUdfs);

		return createRawInfo;
	}

	/*
	 * Name : deassignProcessGroupInfo Desc : This function is Create
	 * deassignProcessGroupInfo Author : AIM Systems, Inc Date : 2011.01.04
	 */
	public DeassignProcessGroupInfo deassignProcessGroupInfo(Lot lotData) {
		// 1. Validation

		DeassignProcessGroupInfo deassignProcessGroupInfo = new DeassignProcessGroupInfo();

		Map<String, String> lotUdfs = lotData.getUdfs();
		deassignProcessGroupInfo.setUdfs(lotUdfs);

		return deassignProcessGroupInfo;
	}

	/*
	 * Name : deassignProductsInfo Desc : This function is Create
	 * deassignProductsInfo Author : AIM Systems, Inc Date : 2011.01.04
	 */
	public DeassignProductsInfo deassignProductsInfo(Lot lotData,
			String consumerLotName, String emptyFlag,
			List<ProductP> productPSequence, double productQuantity) {
		// 1. Validation

		DeassignProductsInfo deassignProductsInfo = new DeassignProductsInfo();

		deassignProductsInfo.setConsumerLotName(consumerLotName);
		deassignProductsInfo.setEmptyFlag(emptyFlag);
		deassignProductsInfo.setProductPSequence(productPSequence);
		deassignProductsInfo.setProductQuantity(productQuantity);

		Map<String, String> lotUdfs = lotData.getUdfs();
		deassignProductsInfo.setUdfs(lotUdfs);

		return deassignProductsInfo;
	}

	/*
	 * Name : makeCompletedInfo Desc : This function is Create makeCompletedInfo
	 * Author : AIM Systems, Inc Date : 2011.01.04
	 */
	public MakeCompletedInfo makeCompletedInfo(Lot lotData,
			List<ProductU> productUdfs) {
		// 1. Validation

		MakeCompletedInfo makeCompletedInfo = new MakeCompletedInfo();

		//makeCompletedInfo.setProductUdfs(productUdfs);

		Map<String, String> lotUdfs = lotData.getUdfs();
		makeCompletedInfo.setUdfs(lotUdfs);

		return makeCompletedInfo;
	}

	/*
	 * Name : makeEmptiedInfo Desc : This function is Create makeEmptiedInfo
	 * Author : AIM Systems, Inc Date : 2011.01.04
	 */
	public MakeEmptiedInfo makeEmptiedInfo(Lot lotData,
			Map<String, String> deassignCarrierUdfs, List<ProductU> productUdfs) {
		MakeEmptiedInfo makeEmptiedInfo = new MakeEmptiedInfo();

		makeEmptiedInfo.setDeassignCarrierUdfs(deassignCarrierUdfs);
		//makeEmptiedInfo.setProductUdfs(productUdfs);

		Map<String, String> lotUdfs = lotData.getUdfs();
		makeEmptiedInfo.setUdfs(lotUdfs);

		return makeEmptiedInfo;
	}

	/*
	 * Name : makeInReworkInfo Desc : This function is Create makeInReworkInfo
	 * Author : AIM Systems, Inc Date : 2011.01.07
	 */
	public MakeInReworkInfo makeInReworkInfo(Lot lotData, String areaName,
			String nodeStack, String processFlowName,
			String processFlowVersion, String processOperationName,
			String processOperationVersion, List<ProductRU> productRUdfs,
			String reworkNodeId) {
		MakeInReworkInfo makeInReworkInfo = new MakeInReworkInfo();

		makeInReworkInfo.setAreaName(areaName);
		makeInReworkInfo.setNodeStack(nodeStack);
		makeInReworkInfo.setProcessFlowName(processFlowName);
		makeInReworkInfo.setProcessFlowVersion(processFlowVersion);
		makeInReworkInfo.setProcessOperationName(processOperationName);
		makeInReworkInfo.setProcessOperationVersion(processOperationVersion);
		//makeInReworkInfo.setProductRUdfs(productRUdfs);
		makeInReworkInfo.setReworkNodeId(reworkNodeId);

		Map<String, String> lotUdfs = lotData.getUdfs();
		makeInReworkInfo.setUdfs(lotUdfs);

		return makeInReworkInfo;
	}

	/*
	 * Name : makeInReworkInfo Desc : This function is Create makeInReworkInfo
	 * Author : JHYEOM Date : 2014.04.29
	 */
	public MakeInReworkInfo makeInReworkInfo(Lot lotData, EventInfo eventInfo,
			String lotName, String processFlowName,
			String processOperationName, String returnProcessFlowName,
			String returnProcessOperationName, Map<String, String> udfs,
			List<ProductRU> productRUdfs, String beforeNodeStack) {
		MakeInReworkInfo makeInReworkInfo = new MakeInReworkInfo();
		makeInReworkInfo.setAreaName(lotData.getAreaName());
		makeInReworkInfo.setProcessFlowName(processFlowName);
		makeInReworkInfo.setProcessFlowVersion("00001");
		makeInReworkInfo.setProcessOperationName(processOperationName);
		makeInReworkInfo.setProcessOperationVersion("00001");

		eventInfo.setEventName("Rework");

		List<Map<String, Object>> nodeInfo = null;
		
		NodeStack nodeStack = null;
		
		try
		{			
/*			nodeInfo = MESLotServiceProxy.getLotServiceUtil()
					.getReworkNodeInfo(lotName, processFlowName,
							processOperationName);
*/			
			nodeStack = NodeStack.stringToNodeStack(beforeNodeStack);		
		}
		catch (Exception e)
		{
			//
		}
		
		/*
		String[] arr = lotData.getNodeStack().split("\\.");
		
		//2018.02.20 hsryu add
		if(lotData.getNodeStack().contains("."))
		{
			String stack = "";
			
			try
			{			
				nodeInfo = MESLotServiceProxy.getLotServiceUtil()
						.getReworkNodeInfo(lotName, returnProcessOperationName,
								returnProcessFlowName);
				
				for(int i=0; i<arr.length-1;i++)
				{
					stack = stack + NodeStack.stringToNodeStack(arr[i]);
				}
				
				nodeStack = NodeStack.stringToNodeStack(stack + (String) nodeInfo
						.get(0).get("NODEID"));				
			}
			catch (Exception e)
			{
				//
			}
		}
		else
		{
			try {
				nodeInfo = MESLotServiceProxy.getLotServiceUtil()
						.getReworkNodeInfo(lotName, returnProcessOperationName,
								returnProcessFlowName);
				
				nodeStack = NodeStack.stringToNodeStack((String) nodeInfo
						.get(0).get("NODEID"));

			} catch (Exception e) {
				//
			}
		}
		
		try
		{
			String stack = "";
			
			nodeInfo = MESLotServiceProxy.getLotServiceUtil()
					.getReworkNodeInfo(lotName, returnProcessOperationName,
							returnProcessFlowName);
			
			if(lotData.getNodeStack().contains("."))
			{
				for(int i=0; i<arr.length-1;i++)
				{
					stack = stack + arr[i].toString();
				}
				nodeStack = NodeStack.stringToNodeStack(stack + "." + (String) nodeInfo
						.get(0).get("NODEID"));				
			}
			else
			{
				nodeStack = NodeStack.stringToNodeStack((String) nodeInfo
						.get(0).get("NODEID"));
			}
		}
		catch (Exception e) {
			//
		}
		*/


		// 1. Set targetNodeId
		String targetNodeId = null;
		try {
			targetNodeId = NodeStack.getNodeID(lotData.getFactoryName(),
					processFlowName, processOperationName);
		} catch (Exception e) {
			//
		}
		
		nodeStack.add(targetNodeId);
		makeInReworkInfo.setNodeStack(NodeStack.nodeStackToString(nodeStack));

		makeInReworkInfo.setReworkNodeId(targetNodeId);

		ProcessFlowKey processFlowKey = new ProcessFlowKey();
		processFlowKey.setFactoryName(lotData.getFactoryName());
		processFlowKey.setProcessFlowName(lotData.getProcessFlowName());
		processFlowKey.setProcessFlowVersion(lotData.getProcessFlowVersion());
		ProcessFlow processFlowData = ProcessFlowServiceProxy
				.getProcessFlowService().selectByKey(processFlowKey);
		udfs = lotData.getUdfs();
		
		if (!StringUtils.equals(processFlowData.getProcessFlowType(),
				GenericServiceProxy.getConstantMap().Arc_Rework)) {
			udfs.put("RETURNFLOWNAME", returnProcessFlowName);
			udfs.put("RETURNOPERATIONNAME", returnProcessOperationName);
		}
		makeInReworkInfo.setUdfs(udfs);

		//makeInReworkInfo.setProductRUdfs(productRUdfs);

		return makeInReworkInfo;
	}

	/**
	 * simple & quick rework info
	 * 
	 * @since 2015.02.04
	 * @author swcho
	 * @param lotName
	 * @param processFlowName
	 * @param processOperationName
	 * @param returnFlowName
	 * @param returnOperationName
	 * @param nodeStack
	 * @param udfs
	 * @param productRUdfs
	 * @return
	 */
	public MakeInReworkInfo makeInReworkInfo(String lotName, String areaName,
			String processFlowName, String processOperationName,
			String returnFlowName, String returnOperationName,
			String nodeStack, Map<String, String> udfs,
			List<ProductRU> productRUdfs) {
		MakeInReworkInfo makeInReworkInfo = new MakeInReworkInfo();
		makeInReworkInfo.setAreaName(areaName);
		makeInReworkInfo.setProcessFlowName(processFlowName);
		makeInReworkInfo.setProcessFlowVersion("00001");
		makeInReworkInfo.setProcessOperationName(processOperationName);
		makeInReworkInfo.setProcessOperationVersion("00001");

		makeInReworkInfo.setNodeStack(nodeStack);

		String[] reworkNodeId = StringUtil.split(nodeStack, ".");
		// makeInReworkInfo.setNodeStack(reworkNodeId[1]);
		makeInReworkInfo.setReworkNodeId(reworkNodeId[reworkNodeId.length - 1]);

		udfs.put("RETURNFLOWNAME", returnFlowName);
		udfs.put("RETURNOPERATIONNAME", returnOperationName);

		makeInReworkInfo.setUdfs(udfs);

		//makeInReworkInfo.setProductRUdfs(productRUdfs);

		return makeInReworkInfo;
	}

	/*
	 * Name : makeNotInReworkInfo Desc : This function is Create
	 * MakeNotInReworkInfo Author : AIM Systems, Inc Date : 2011.01.07
	 */
	public MakeNotInReworkInfo makeNotInReworkInfo(Lot lotData,
			String areaName, String nodeStack, String processFlowName,
			String processFlowVersion, String processOperationName,
			String processOperationVersion, List<ProductU> productUdfs) {
		MakeNotInReworkInfo makeNotInReworkInfo = new MakeNotInReworkInfo();

		makeNotInReworkInfo.setAreaName(areaName);
		makeNotInReworkInfo.setNodeStack(nodeStack);
		makeNotInReworkInfo.setProcessFlowName(processFlowName);
		makeNotInReworkInfo.setProcessFlowVersion(processFlowVersion);
		makeNotInReworkInfo.setProcessOperationName(processOperationName);
		makeNotInReworkInfo.setProcessOperationVersion(processOperationVersion);
		//makeNotInReworkInfo.setProductUdfs(productUdfs);

		Map<String, String> lotUdfs = lotData.getUdfs();
		makeNotInReworkInfo.setUdfs(lotUdfs);

		return makeNotInReworkInfo;
	}

	/*
	 * Name : makeNotInRework Desc : This function is makeNotInRework Author :
	 * AIM Systems, Inc Date : 2011.03.28
	 */
	public MakeNotInReworkInfo makeNotInReworkInfo(Lot lotData,
			EventInfo eventInfo, String lotName, String returnFlowName,
			String returnOperationName, Map<String, String> udfs,
			List<ProductU> productU) throws CustomException {
		MakeNotInReworkInfo makeNotInReworkInfo = new MakeNotInReworkInfo();

		makeNotInReworkInfo.setAreaName(lotData.getAreaName());
		makeNotInReworkInfo.setProcessFlowName(returnFlowName);
		makeNotInReworkInfo.setProcessFlowVersion("00001");
		makeNotInReworkInfo.setProcessOperationName(returnOperationName);
		makeNotInReworkInfo.setProcessOperationVersion("00001");

		/* 2018.02.22 hsryu - do not need.
		String returnNodeId = null;
		try {
			returnNodeId = NodeStack.getNodeID(lotData.getFactoryName(),
					returnFlowName, returnOperationName);
		} catch (Exception e) {
		}
		 */
		
//		NodeStack nodeStack = NodeStack.stringToNodeStack(lotData
//					.getNodeStack());
//
//		int lastIndex = nodeStack.size();
//		nodeStack.remove(lastIndex - 1);
//
//		String strNodeStack = NodeStack.nodeStackToString(nodeStack);
//		
//		makeNotInReworkInfo.setNodeStack(strNodeStack);
//		
		/*
		makeNotInReworkInfo.setNodeStack(returnNodeId);
		
		
		udfs = lotData.getUdfs();

		udfs.put("RETURNFLOWNAME", "");
		udfs.put("RETURNOPERATIONNAME", "");
		 */

		//2018.12.13_hsryu_Modify Serch nodeStack Logic. 
		String[] arrNodeStack = StringUtil.split(lotData.getNodeStack(), ".");
		int count = arrNodeStack.length;
		int nodeNum = 0;
		String nextNodeID = "";
		String nodeStack = "";

		for ( int i = count-2; i >=0; i-- )
		{
			Map<String, String> flowMap = MESLotServiceProxy.getLotServiceUtil().getProcessFlowInfo(arrNodeStack[i]);

			String flowName = flowMap.get("PROCESSFLOWNAME");
			String flowVersion = flowMap.get("PROCESSFLOWVERSION");
			String operationName = flowMap.get("PROCESSOPERATIONNAME");

			boolean EndFlagForBeforeFlow = MESLotServiceProxy.getLotServiceUtil().checkEndOperation(flowName, flowVersion, arrNodeStack[i]);

			//if not end Operation
			if(!EndFlagForBeforeFlow)
			{
				nextNodeID = MESLotServiceProxy.getLotServiceUtil().GetReturnAfterNodeStackForSampling(lotData.getFactoryName(), arrNodeStack[i]);
				nodeNum = i;
				break;
			}
		}
		
		if(nodeNum == 0)
		{
			nodeStack = nextNodeID;
		}
		else
		{
			for(int i=0; i<=nodeNum-1; i++)
			{
				nodeStack += arrNodeStack[i] + ".";
			}
			nodeStack += nextNodeID;
		}

		makeNotInReworkInfo.setNodeStack(nodeStack);

		makeNotInReworkInfo.setUdfs(udfs);
		//makeNotInReworkInfo.setProductUdfs(productU);

		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey"
				+ eventInfo.getEventTimeKey());

		return makeNotInReworkInfo;

	}

	/**
	 * make not on hold info
	 * 
	 * @author swcho
	 * @since 2014.05.23
	 * @param lotData
	 * @param productUdfs
	 * @param udfs
	 * @return
	 */
	public MakeNotOnHoldInfo makeNotOnHoldInfo(Lot lotData,
			List<ProductU> productUdfs, Map<String, String> udfs) {
		MakeNotOnHoldInfo makeNotOnHoldInfo = new MakeNotOnHoldInfo();

		//makeNotOnHoldInfo.setProductUdfs(productUdfs);

		// Map<String, String> lotUdfs = lotData.getUdfs();
		makeNotOnHoldInfo.setUdfs(udfs);

		return makeNotOnHoldInfo;
	}

	/*
	 * Name : makeReceivedInfo Desc : This function is Create MakeReceivedInfo
	 * Author : AIM Systems, Inc Date : 2011.01.08
	 */
	public MakeReceivedInfo makeReceivedInfo(Lot lotData, String areaName,
			String nodeStack, String processFlowName,
			String processFlowVersion, String processOperationName,
			String processOperationVersion, String productionType,
			String productRequestName, String productSpec2Name,
			String productSpec2Version, String productSpecName,
			String productSpecVersion, String productType,
			List<ProductU> productUdfs, String subProductType) {
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
		//makeReceivedInfo.setProductUdfs(productUdfs);
		makeReceivedInfo.setSubProductType(subProductType);

		Map<String, String> lotUdfs = lotData.getUdfs();
		makeReceivedInfo.setUdfs(lotUdfs);

		return makeReceivedInfo;
	}

	/*
	 * Name : makeScrappedInfo Desc : This function is Create MakeScrappedInfo
	 * Author : AIM Systems, Inc Date : 2011.01.08
	 */
	public MakeScrappedInfo makeScrappedInfo(Lot lotData,
			double productQuantity, List<ProductU> productUSequence) {
		MakeScrappedInfo makeScrappedInfo = new MakeScrappedInfo();

		makeScrappedInfo.setProductQuantity(productQuantity);
		makeScrappedInfo.setProductUSequence(productUSequence);

		Map<String, String> lotUdfs = lotData.getUdfs();
		makeScrappedInfo.setUdfs(lotUdfs);

		return makeScrappedInfo;
	}

	/*
	 * Name : make shipment info Desc : This function is Create MakeShippedInfo
	 * Author : jhyeom Date : 2014.04.17
	 */
	public MakeShippedInfo makeShippedInfo(Lot lotData, String areaName,
			String directShipFlag, String shipBankName, String factoryName,
			List<ProductU> productUdfs, String shipBankState) {
		MakeShippedInfo makeShippedInfo = new MakeShippedInfo();

		makeShippedInfo.setAreaName(areaName);
		makeShippedInfo.setDirectShipFlag(directShipFlag);
		makeShippedInfo.setFactoryName(factoryName);
		//makeShippedInfo.setProductUdfs(productUdfs);
		
		// Modified by smkang on 2019.06.20 - Unsafe Code.
//		lotData.getUdfs().put("SHIPBANK", shipBankName);
//		lotData.getUdfs().put("SHIPBANKSTATE", shipBankState);
//		LotServiceProxy.getLotService().update(lotData);
//		makeShippedInfo.setUdfs(lotData.getUdfs());
		makeShippedInfo.getUdfs().put("SHIPBANK", shipBankName);
		makeShippedInfo.getUdfs().put("SHIPBANKSTATE", shipBankState);

		return makeShippedInfo;
	}
	
	/*
	 * Name : make shipment info Desc : This function is Create MakeShippedInfo
	 * Author : jhyeom Date : 2014.04.17
	 */
	public MakeShippedInfo makeShippedInfo(Lot lotData, String areaName,
			String directShipFlag, String shipBankName, String factoryName,
			List<ProductU> productUdfs) {
		MakeShippedInfo makeShippedInfo = new MakeShippedInfo();

		makeShippedInfo.setAreaName(areaName);
		makeShippedInfo.setDirectShipFlag(directShipFlag);
		makeShippedInfo.setFactoryName(factoryName);
		makeShippedInfo.setProductUSequence(productUdfs);
		
		makeShippedInfo.getUdfs().put("SHIPBANK", shipBankName);

		return makeShippedInfo;
	}

	/*
	 * Name : makeUnScrappedInfo Desc : This function is Create
	 * MakeUnScrappedInfo Author : AIM Systems, Inc Date : 2011.01.27
	 */
	public MakeUnScrappedInfo makeUnScrappedInfo(Lot lotData,
			String lotProcessState, double productQuantity,
			List<ProductU> productUSequence) {
		MakeUnScrappedInfo makeUnScrappedInfo = new MakeUnScrappedInfo();

		makeUnScrappedInfo.setLotProcessState(lotData.getLotProcessState());
		makeUnScrappedInfo.setProductQuantity(productUSequence.size());
		makeUnScrappedInfo.setProductUSequence(productUSequence);

		Map<String, String> lotUdfs = lotData.getUdfs();
		makeUnScrappedInfo.setUdfs(lotUdfs);

		return makeUnScrappedInfo;
	}

	/*
	 * Name : makeUnShippedInfo Desc : This function is Create MakeUnShippedInfo
	 * Author : jhyeom Date : 2014.04.17
	 */
	public MakeUnShippedInfo makeUnShippedInfo(Lot lotData, String areaName,
			List<ProductU> productUdfs) {
		MakeUnShippedInfo makeUnShippedInfo = new MakeUnShippedInfo();

		makeUnShippedInfo.setAreaName(areaName);
		//makeUnShippedInfo.setProductUdfs(productUdfs);

		Map<String, String> lotUdfs = lotData.getUdfs();
		makeUnShippedInfo.setUdfs(lotUdfs);

		return makeUnShippedInfo;
	}

	/*
	 * Name : makeWaitingToLoginInfo Desc : This function is Create
	 * MakeWaitingToLoginInfo Author : AIM Systems, Inc Date : 2011.01.27
	 */
	public MakeWaitingToLoginInfo makeWaitingToLoginInfo(Lot lotData,
			String areaName, String machineName, String machineRecipeName,
			List<ProductU> productUdfs) {
		MakeWaitingToLoginInfo makeWaitingToLoginInfo = new MakeWaitingToLoginInfo();

		makeWaitingToLoginInfo.setAreaName(areaName);
		makeWaitingToLoginInfo.setMachineName(machineName);
		makeWaitingToLoginInfo.setMachineRecipeName(machineRecipeName);
		//makeWaitingToLoginInfo.setProductUdfs(productUdfs);

		Map<String, String> lotUdfs = lotData.getUdfs();
		makeWaitingToLoginInfo.setUdfs(lotUdfs);

		return makeWaitingToLoginInfo;
	}

	/*
	 * Name : recreateInfo Desc : This function is Create RecreateInfo Author :
	 * AIM Systems, Inc Date : 2011.01.27
	 */
	public RecreateInfo recreateInfo(Lot lotData, String areaName,
			Timestamp dueDate, String newLotName,
			Map<String, String> newLotUdfs, String nodeStack, long priority,
			String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion,
			String productionType, String productRequestName,
			String productSpec2Name, String productSpec2Version,
			String productSpecName, String productSpecVersion,
			String productType, List<ProductU> productUdfs,
			String subProductType, double subProductUnitQuantity1,
			double subProductUnitQuantity2) {
		RecreateInfo recreateInfo = new RecreateInfo();

		recreateInfo.setAreaName(areaName);
		recreateInfo.setDueDate(dueDate);
		recreateInfo.setNewLotName(newLotName);
		recreateInfo.setNewLotUdfs(newLotUdfs);
		recreateInfo.setNodeStack(nodeStack);
		recreateInfo.setPriority(priority);
		recreateInfo.setProcessFlowName(processFlowName);
		recreateInfo.setProcessFlowVersion(processFlowVersion);
		recreateInfo.setProcessOperationName(processOperationName);
		recreateInfo.setProcessOperationVersion(processOperationVersion);
		recreateInfo.setProductionType(productionType);
		recreateInfo.setProductRequestName(productRequestName);
		recreateInfo.setProductSpec2Name(productSpec2Name);
		recreateInfo.setProductSpec2Version(productSpec2Version);
		recreateInfo.setProductSpecName(productSpecName);
		recreateInfo.setProductSpecVersion(productSpecVersion);
		recreateInfo.setProductType(productType);
		//recreateInfo.setProductUdfs(productUdfs);
		recreateInfo.setSubProductType(subProductType);
		recreateInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
		recreateInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);

		Map<String, String> lotUdfs = lotData.getUdfs();
		recreateInfo.setUdfs(lotUdfs);

		return recreateInfo;
	}

	/*
	 * Name : recreateAndCreateAllProductsInfo Desc : This function is Create
	 * RecreateAndCreateAllProductsInfo Author : AIM Systems, Inc Date :
	 * 2011.01.27
	 */
	public RecreateAndCreateAllProductsInfo recreateAndCreateAllProductsInfo(
			Lot lotData, String areaName, Timestamp dueDate, String newLotName,
			Map<String, String> newLotUdfs, String nodeStack, long priority,
			String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion,
			String productionType, List<ProductNPGS> productNPGSSequence,
			String productRequestName, String productSpec2Name,
			String productSpec2Version, String productSpecName,
			String productSpecVersion, String productType,
			String subProductType, double subProductUnitQuantity1,
			double subProductUnitQuantity2) {
		RecreateAndCreateAllProductsInfo recreateAndCreateAllProductsInfo = new RecreateAndCreateAllProductsInfo();

		recreateAndCreateAllProductsInfo.setAreaName(areaName);
		recreateAndCreateAllProductsInfo.setDueDate(dueDate);
		recreateAndCreateAllProductsInfo.setNewLotName(newLotName);
		recreateAndCreateAllProductsInfo.setNewLotUdfs(newLotUdfs);
		recreateAndCreateAllProductsInfo.setNodeStack(nodeStack);
		recreateAndCreateAllProductsInfo.setPriority(priority);
		recreateAndCreateAllProductsInfo.setProcessFlowName(processFlowName);
		recreateAndCreateAllProductsInfo
				.setProcessFlowVersion(processFlowVersion);
		recreateAndCreateAllProductsInfo
				.setProcessOperationName(processOperationName);
		recreateAndCreateAllProductsInfo
				.setProcessOperationVersion(processOperationVersion);
		recreateAndCreateAllProductsInfo.setProductionType(productionType);
		recreateAndCreateAllProductsInfo
				.setProductNPGSSequence(productNPGSSequence);
		recreateAndCreateAllProductsInfo
				.setProductRequestName(productRequestName);
		recreateAndCreateAllProductsInfo.setProductSpec2Name(productSpec2Name);
		recreateAndCreateAllProductsInfo
				.setProductSpec2Version(productSpec2Version);
		recreateAndCreateAllProductsInfo.setProductSpecName(productSpecName);
		recreateAndCreateAllProductsInfo
				.setProductSpecVersion(productSpecVersion);
		recreateAndCreateAllProductsInfo.setProductType(productType);
		recreateAndCreateAllProductsInfo.setSubProductType(subProductType);
		recreateAndCreateAllProductsInfo
				.setSubProductUnitQuantity1(subProductUnitQuantity1);
		recreateAndCreateAllProductsInfo
				.setSubProductUnitQuantity2(subProductUnitQuantity2);

		Map<String, String> lotUdfs = lotData.getUdfs();
		recreateAndCreateAllProductsInfo.setUdfs(lotUdfs);

		return recreateAndCreateAllProductsInfo;
	}

	/*
	 * Name : recreateProductsInfo Desc : This function is Create
	 * RecreateProductsInfo Author : AIM Systems, Inc Date : 2011.01.27
	 */
	public RecreateProductsInfo recreateProductsInfo(Lot lotData,
			List<ProductNPGS> productNPGSSequence, double productQuantity) {
		RecreateProductsInfo recreateProductsInfo = new RecreateProductsInfo();

		recreateProductsInfo.setProductNPGSSequence(productNPGSSequence);
		recreateProductsInfo.setProductQuantity(productQuantity);

		Map<String, String> lotUdfs = lotData.getUdfs();
		recreateProductsInfo.setUdfs(lotUdfs);

		return recreateProductsInfo;
	}

	/*
	 * Name : relocateProductsInfo Desc : This function is Create
	 * RelocateProductsInfo Author : AIM Systems, Inc Date : 2011.01.27
	 */
	public RelocateProductsInfo relocateProductsInfo(Lot lotData,
			List<ProductP> productPSequence, double productQuantity) {
		RelocateProductsInfo relocateProductsInfo = new RelocateProductsInfo();

		relocateProductsInfo.setProductPSequence(productPSequence);
		relocateProductsInfo.setProductQuantity(productQuantity);

		Map<String, String> lotUdfs = lotData.getUdfs();
		relocateProductsInfo.setUdfs(lotUdfs);

		return relocateProductsInfo;
	}

	/*
	 * Name : separateInfo Desc : This function is Create SeparateInfo Author :
	 * AIM Systems, Inc Date : 2011.01.27
	 */
	public SeparateInfo separateInfo(Lot lotData,
			List<ProductNSubProductPGQS> productNSubProductPGQSSequence,
			double productQuantity, Product productData) {
		SeparateInfo separateInfo = new SeparateInfo();

		separateInfo
				.setProductNSubProductPGQSSequence(productNSubProductPGQSSequence);
		separateInfo.setProductQuantity(productQuantity);
		separateInfo.setProductType("Glass");
		separateInfo.setSubProductType("Panel");

		Map<String, String> lotUdfs = lotData.getUdfs();
		separateInfo.setUdfs(lotUdfs);

		lotUdfs.put("CRATENAME", productData.getUdfs().get("CRATENAME")
				.toString());

		return separateInfo;
	}

	/*
	 * Name : separateInfo Desc : This function is Create SeparateInfo Author :
	 * AIM Systems, Inc Date : 2011.01.27
	 */
	public SeparateInfo separateInfo(Lot lotData,
			List<ProductNSubProductPGQS> productNSubProductPGQSSequence,
			double productQuantity, String productType, String subProductType) {
		SeparateInfo separateInfo = new SeparateInfo();

		separateInfo
				.setProductNSubProductPGQSSequence(productNSubProductPGQSSequence);
		separateInfo.setProductQuantity(productQuantity);
		separateInfo.setProductType(productType);
		separateInfo.setSubProductType(subProductType);

		Map<String, String> lotUdfs = lotData.getUdfs();
		separateInfo.setUdfs(lotUdfs);

		return separateInfo;
	}

	/*
	 * Name : setAreaInfo Desc : This function is Create SetAreaInfo Author :
	 * AIM Systems, Inc Date : 2011.01.27
	 */
	public SetAreaInfo setAreaInfo(Lot lotData, String areaName,
			List<ProductU> productUSequence) {
		SetAreaInfo setAreaInfo = new SetAreaInfo();

		setAreaInfo.setAreaName(areaName);
		setAreaInfo.setProductUSequence(productUSequence);

		Map<String, String> lotUdfs = lotData.getUdfs();
		setAreaInfo.setUdfs(lotUdfs);

		return setAreaInfo;
	}

	/*
	 * Name : setEventInfo Desc : This function is Create SetEventInfo Author :
	 * AIM Systems, Inc Date : 2011.01.27
	 */
	public SetEventInfo setEventInfo(Lot lotData, double productQuantity,
			List<ProductU> productUSequence) {
		SetEventInfo setEventInfo = new SetEventInfo();

		setEventInfo.setProductQuantity(productQuantity);
		setEventInfo.setProductUSequence(productUSequence);

		Map<String, String> lotUdfs = lotData.getUdfs();
		setEventInfo.setUdfs(lotUdfs);

		return setEventInfo;
	}

	/*
	 * Name : splitInfo Desc : This function is Create SplitInfo Author : AIM
	 * Systems, Inc Date : 2011.01.31
	 */
	public SplitInfo splitInfo(Lot lotData, String childCarrierName, String childLotName, List<ProductP> productPSequence, String productQuantity) throws CustomException 
	{
		Durable assignDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(childCarrierName);

		Durable deassignDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());

		// splitInfo & Udfs Set
		SplitInfo splitInfo = new SplitInfo();

		splitInfo.setChildCarrierName(childCarrierName);
		splitInfo.setChildLotName(childLotName);
		splitInfo.setProductPSequence(productPSequence);
		splitInfo.setProductQuantity(Long.valueOf(productQuantity).doubleValue());

		Map<String, String> lotUdfs = lotData.getUdfs();
		splitInfo.setUdfs(lotUdfs);

		Map<String, String> childLotUdfs = lotData.getUdfs();
		splitInfo.setChildLotUdfs(childLotUdfs);

		Map<String, String> assignCarrierUdfs = assignDurableData.getUdfs();
		splitInfo.setAssignCarrierUdfs(assignCarrierUdfs);

		Map<String, String> deassignCarrierUdfs = deassignDurableData.getUdfs();
		splitInfo.setDeassignCarrierUdfs(deassignCarrierUdfs);

		return splitInfo;
	}

	/*
	 * Name : mergeInfo Desc : This function is Create MergeInfo Author : AIM
	 * Systems, Inc Date : 2011.01.27
	 */
	public MergeInfo mergeInfo(Lot lotData,
			Map<String, String> deassignCarrierUdfs, String parentLotName,
			Map<String, String> parentLotUdfs, List<ProductP> productPSequence) {
		MergeInfo mergeInfo = new MergeInfo();

		mergeInfo.setDeassignCarrierUdfs(deassignCarrierUdfs);
		mergeInfo.setParentLotName(parentLotName);
		mergeInfo.setParentLotUdfs(parentLotUdfs);
		mergeInfo.setProductPSequence(productPSequence);

		return mergeInfo;
	}

	/*
	 * Name : undoInfo Desc : This function is Create UndoInfo Author : AIM
	 * Systems, Inc Date : 2011.01.31
	 */
	public UndoInfo undoInfo(Lot lotData, String carrierUndoFlag,
			String eventName, Timestamp eventTime, String eventTimeKey,
			String eventUser, String lastEventTimeKey, String undoFlag) {
		UndoInfo undoInfo = new UndoInfo();

		undoInfo.setCarrierUndoFlag(carrierUndoFlag);
		undoInfo.setEventName(eventName);
		undoInfo.setEventTime(eventTime);
		undoInfo.setEventTimeKey(eventTimeKey);
		undoInfo.setEventUser(eventUser);
		undoInfo.setLastEventTimeKey(lastEventTimeKey);
		undoInfo.setUndoFlag(undoFlag);

		Map<String, String> lotUdfs = lotData.getUdfs();
		undoInfo.setUdfs(lotUdfs);

		return undoInfo;
	}

	/**
	 * to prepare for makeReleased
	 * 
	 * @author swcho
	 * @since 2014.04.21
	 * @param lotData
	 * @param areaName
	 * @param nodeStack
	 * @param processFlowName
	 * @param processFlowVersion
	 * @param processOperationName
	 * @param processOperationVersion
	 * @param productionType
	 * @param assignCarrierUdfs
	 * @param carrierName
	 * @param dueDate
	 * @param priority
	 * @return
	 */
	public MakeReleasedInfo makeReleasedInfo(Lot lotData, String areaName,
			String nodeStack, String processFlowName,
			String processFlowVersion, String processOperationName,
			String processOperationVersion, String productionType,
			Map<String, String> assignCarrierUdfs, String carrierName,
			Timestamp dueDate, long priority) {
		MakeReleasedInfo makeReleasedInfo = new MakeReleasedInfo();

		makeReleasedInfo.setAreaName(areaName);
		makeReleasedInfo.setAssignCarrierUdfs(assignCarrierUdfs);
		makeReleasedInfo.setCarrierName(carrierName);
		makeReleasedInfo.setDueDate(dueDate);
		makeReleasedInfo.setNodeStack(nodeStack);
		makeReleasedInfo.setPriority(priority);
		makeReleasedInfo.setProcessFlowName(processFlowName);
		makeReleasedInfo.setProcessFlowVersion(processFlowVersion);
		makeReleasedInfo.setProcessOperationName(processOperationName);
		makeReleasedInfo.setProcessOperationVersion(processOperationVersion);
		// overriding by implement parameters
		makeReleasedInfo.setProductPGSSequence(new ArrayList<ProductPGS>());
		makeReleasedInfo.setProductQuantity(0);

		Map<String, String> lotUdfs = lotData.getUdfs();
		makeReleasedInfo.setUdfs(lotUdfs);

		return makeReleasedInfo;
	}

	/*
	 * Name : getAllProductCSequence Desc : This function is Get All
	 * ProductCSequence Author : AIM Systems, Inc Date : 2011.01.06
	 */
	public List<ProductC> getAllProductCSequence(
			Lot lotData,
			List<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial> cms) {
		// 1. Set Variable
		List<ProductC> productCSequence = new ArrayList<ProductC>();
		List<Product> productDatas = new ArrayList<Product>();

		// 2. Get Product Data List
		productDatas = ProductServiceProxy.getProductService()
				.allProductsByLot(lotData.getKey().getLotName());

		// 3. Get ProductName, Position By Product
		for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct
				.hasNext();) {
			Product product = iteratorProduct.next();

			ProductC productC = new ProductC();

			productC.setProductName(product.getKey().getProductName());
			//productC.setCms(cms);
			LotServiceUtil lotServiceUtil = new LotServiceUtil();

			productC.setUdfs(product.getUdfs());

			// Add productPSequence By Product
			productCSequence.add(productC);
		}
		return productCSequence;
	}

	/*
	 * Name : changeGradeInfo Desc : This function is Create ChangeGradeInfo
	 * Author : AIM Systems, Inc Date : 2011.01.11
	 */
	public ChangeGradeInfo changeGradeInfo(Lot lotData, String lotGrade,
			List<ProductPGS> productPGSSequence) {
		// 1. Validation

		ChangeGradeInfo changeGradeInfo = new ChangeGradeInfo();

		changeGradeInfo.setLotGrade(lotGrade);
		changeGradeInfo.setLotProcessState(lotData.getLotProcessState());
		changeGradeInfo.setProductQuantity(lotData.getProductQuantity());
		changeGradeInfo.setProductPGSSequence(productPGSSequence);

		// 2019.06.20_hsryu_Avoid update Old Udfs. 
//		Map<String, String> lotUdfs = lotData.getUdfs();
//		changeGradeInfo.setUdfs(lotUdfs);

		return changeGradeInfo;
	}

	/*
	 * Name : getAllProductPGSRCSequence Desc : This function is Get All
	 * ProductPGSRCSequence Author : AIM Systems, Inc Date : 2011.01.11
	 */
	public List<ProductPGSRC> getAllProductPGSRCSequence(Lot lotData) {
		// 1. Set Variable
		List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();
		List<Product> productDatas = new ArrayList<Product>();

		// 2. Get Product Data List
		productDatas = ProductServiceProxy.getProductService()
				.allProductsByLot(lotData.getKey().getLotName());

		// 3. Get ProductName, Position By Product
		for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct
				.hasNext();) {
			Product product = iteratorProduct.next();

			ProductPGSRC productPGSRC = new ProductPGSRC();

			productPGSRC.setPosition(product.getPosition());
			productPGSRC.setProductGrade(product.getProductGrade());
			productPGSRC.setProductName(product.getKey().getProductName());
			productPGSRC.setReworkFlag("N");
			productPGSRC.setSubProductGrades1(product.getSubProductGrades1());
			productPGSRC.setSubProductGrades2(product.getSubProductGrades2());
			productPGSRC.setSubProductQuantity1(product
					.getSubProductQuantity1());
			productPGSRC.setSubProductQuantity2(product
					.getSubProductQuantity2());

			// 4. Set Udfs
			productPGSRC.setUdfs(product.getUdfs());

			// Add productPSequence By Product
			productPGSRCSequence.add(productPGSRC);
		}
		return productPGSRCSequence;
	}

	/*
	 * Name : assignProducts Desc : This function is Make AssignProductsInfo
	 * Author : AIM Systems, Inc Date : 2011.01.21
	 */
	public AssignProductsInfo assignProducts(EventInfo eventInfo,
			String lotName, String carrierName) {

		List<Product> productList = ProductServiceProxy.getProductService()
				.allProductsByCarrier(carrierName);
		List<ProductP> list = new ArrayList<ProductP>();

		AssignProductsInfo assignProductsInfo = new AssignProductsInfo();
		for (int i = 0; i < productList.size(); i++) {
			ProductP productP = new ProductP();
			productP.setProductName(productList.get(i).getKey()
					.getProductName());
			productP.setPosition(productList.get(i).getPosition());
			list.add(productP);
		}
		assignProductsInfo.setGradeFlag("G");
		assignProductsInfo.setProductQuantity(productList.size());
		assignProductsInfo.setSourceLotName(lotName);
		assignProductsInfo.setValidationFlag("Y");
		assignProductsInfo.setProductPSequence(list);

		return assignProductsInfo;
	}
	
	/*
	* Name : assignCarrierinfo
	* Desc : This function is Create assignCarrierinfo
	* Author : AIM Systems, Inc
	* Date : 2019.05.10
	*/
	public static AssignCarrierInfo assignCarrierinfo(Lot lotData, Durable durableData, String carrierName,	List<ProductP> productPSequence) throws CustomException
	{
		Lot newLotData = LotServiceUtil.getLotData(lotData.getKey().getLotName());
		
		// 1. Validation		
		if((newLotData.getLotState().equals(GenericServiceProxy.getConstantMap().Lot_Scrapped))) {
			throw new CustomException("LOT-9001", newLotData.getKey().getLotName(), newLotData.getLotState()); 
		}
		if(newLotData.getProductQuantity() != Double.parseDouble(Integer.toString(productPSequence.size()))){
			throw new CustomException("LOT-9086", newLotData.getProductQuantity(), productPSequence.size()); 
		}
		
		// 2. Get Carrier Info
		AssignCarrierInfo assignCarrierinfo = new AssignCarrierInfo();
		assignCarrierinfo.setCarrierName(carrierName);		
		assignCarrierinfo.setProductPSequence(productPSequence);

		return assignCarrierinfo;
	}

	/**
	 * 151127 by swcho : modified
	 * 
	 * @author swcho
	 * @since 2013-05-18
	 * @param lotData
	 * @param durableData
	 * @param productPSequence
	 * @return assignCarrierinfo
	 */
	public AssignCarrierInfo assignCarrierInfo(Lot lotData,
			Durable durableData, List<ProductP> productPSequence) {
		AssignCarrierInfo assignCarrierinfo = new AssignCarrierInfo();

		assignCarrierinfo.setCarrierName(durableData.getKey().getDurableName());

		assignCarrierinfo.setProductPSequence(productPSequence);

		Map<String, String> assignCarrierUdfs = durableData.getUdfs();
		assignCarrierinfo.setAssignCarrierUdfs(assignCarrierUdfs);

		Map<String, String> lotUdfs = lotData.getUdfs();
		assignCarrierinfo.setUdfs(lotUdfs);

		return assignCarrierinfo;
	}

	/**
	 * @author swcho
	 * @since 2013-05-18
	 * @param lotData
	 * @param carrierName
	 * @param productPSequence
	 * @return deassignCarrierinfo
	 */
	public static DeassignCarrierInfo deassignCarrierInfo(Lot lotData,
			Durable durableData, List<ProductU> productUSequence) {
		DeassignCarrierInfo deassignCarrierInfo = new DeassignCarrierInfo();

		deassignCarrierInfo.setProductUSequence(productUSequence);

		Map<String, String> deassignCarrierUdfs = durableData.getUdfs();
		deassignCarrierInfo.setDeassignCarrierUdfs(deassignCarrierUdfs);

		Map<String, String> lotUdfs = lotData.getUdfs();
		deassignCarrierInfo.setUdfs(lotUdfs);

		return deassignCarrierInfo;
	}

	/**
	 * @author smkang
	 * @since 2013-05-20
	 * @param lotData
	 * @param newLotName
	 * @param carrierName
	 * @param assignCarrierUdfs
	 * @return createWithParentLotInfo
	 * @throws Exception
	 */
	@Deprecated
	public CreateWithParentLotInfo createWithParentLotInfo(Lot lotData,
			String newLotName, String carrierName,
			Map<String, String> assignCarrierUdfs) throws Exception {
		CreateWithParentLotInfo createWithParentLotInfo = new CreateWithParentLotInfo();

		createWithParentLotInfo.setLotName(newLotName);
		createWithParentLotInfo.setProductionType(lotData.getProductionType());
		createWithParentLotInfo
				.setProductSpecName(lotData.getProductSpecName());
		createWithParentLotInfo.setProductSpecVersion(lotData
				.getProductSpecVersion());
		createWithParentLotInfo.setProductSpec2Name(lotData
				.getProductSpec2Name());
		createWithParentLotInfo.setProductSpec2Version(lotData
				.getProductSpec2Version());
		createWithParentLotInfo.setProcessGroupName(lotData
				.getProcessGroupName());
		createWithParentLotInfo.setProductRequestName(lotData
				.getProductRequestName());
		createWithParentLotInfo.setOriginalLotName(newLotName);
		createWithParentLotInfo.setSourceLotName(newLotName);
		createWithParentLotInfo.setRootLotName(newLotName);
		createWithParentLotInfo.setCarrierName(carrierName);
		createWithParentLotInfo.setProductType(lotData.getProductType());
		createWithParentLotInfo.setSubProductType(lotData.getSubProductType());
		createWithParentLotInfo.setSubProductUnitQuantity1(lotData
				.getSubProductUnitQuantity1());
		createWithParentLotInfo.setSubProductUnitQuantity2(lotData
				.getSubProductUnitQuantity2());
		createWithParentLotInfo.setProductQuantity(0); // Commented by smkang on
														// 2013.05.20 -
														// ProductQuantity
		createWithParentLotInfo.setLotGrade(lotData.getLotGrade());
		createWithParentLotInfo.setDueDate(lotData.getDueDate());
		createWithParentLotInfo.setPriority(lotData.getPriority());
		createWithParentLotInfo.setFactoryName(lotData.getFactoryName());
		createWithParentLotInfo.setAreaName(lotData.getAreaName());
		createWithParentLotInfo.setLotState(lotData.getLotState());
		createWithParentLotInfo
				.setLotProcessState(lotData.getLotProcessState());
		createWithParentLotInfo.setLotHoldState(lotData.getLotHoldState());
		createWithParentLotInfo.setLastLoggedInTime(lotData
				.getLastLoggedInTime());
		createWithParentLotInfo.setLastLoggedInUser(lotData
				.getLastLoggedInUser());
		createWithParentLotInfo.setLastLoggedOutTime(lotData
				.getLastLoggedOutTime());
		createWithParentLotInfo.setLastLoggedOutUser(lotData
				.getLastLoggedOutUser());
		createWithParentLotInfo
				.setProcessFlowName(lotData.getProcessFlowName());
		createWithParentLotInfo.setProcessFlowVersion(lotData
				.getProcessFlowVersion());
		createWithParentLotInfo.setProcessOperationName(lotData
				.getProcessOperationName());
		createWithParentLotInfo.setProcessOperationVersion(lotData
				.getProcessOperationVersion());
		createWithParentLotInfo.setNodeStack(lotData.getNodeStack());
		createWithParentLotInfo.setReworkFlag(lotData.getReworkState().equals(
				GenericServiceProxy.getConstantMap().Lot_InRework) ? "Y" : "N");
		createWithParentLotInfo.setReworkCount(lotData.getReworkCount());
		createWithParentLotInfo.setReworkNodeId(lotData.getReworkNodeId());
		createWithParentLotInfo.setMachineName(lotData.getMachineName());
		createWithParentLotInfo.setMachineRecipeName(lotData
				.getMachineRecipeName());
		createWithParentLotInfo.setProductPSequence(new ArrayList<ProductP>()); // Commented
																				// by
																				// smkang
																				// on
																				// 2013.05.20
																				// -
																				// ProductQuantity
		createWithParentLotInfo.setAssignCarrierFlag("Y");
		createWithParentLotInfo.setAssignCarrierUdfs(assignCarrierUdfs);
		createWithParentLotInfo.setUdfs(lotData.getUdfs());

		return createWithParentLotInfo;
	}

	/**
	 * 151119 by swcho : modified
	 * 
	 * @author hykim
	 * @since 2014-04-16
	 * @param lotName
	 * @param productQuantity
	 * @param productPSequence
	 * @param destinationLotUdfs
	 * @param deassignCarrierUdfs
	 * @return transferProductsToLotInfo
	 * @throws CustomException
	 */
	public TransferProductsToLotInfo transferProductsToLotInfo(String lotName,
			int productQuantity, List<ProductP> productPSequence,
			Map<String, String> destinationLotUdfs,
			Map<String, String> deassignCarrierUdfs) throws CustomException {
		TransferProductsToLotInfo transferProductsToLotInfo = new TransferProductsToLotInfo();

		transferProductsToLotInfo.setDestinationLotName(lotName);
		transferProductsToLotInfo.setProductQuantity(productQuantity);
		transferProductsToLotInfo.setEmptyFlag("Y");
		transferProductsToLotInfo.setValidationFlag("Y");
		transferProductsToLotInfo.setProductPSequence(productPSequence);
		transferProductsToLotInfo.setDestinationLotUdfs(destinationLotUdfs);
		transferProductsToLotInfo.setDeassignCarrierUdfs(deassignCarrierUdfs);

		return transferProductsToLotInfo;
	}

	/**
     * 151119 by swcho : modified
     * 
     * @author hykim
     * @since 2014-04-16
     * @param lotName
     * @param productQuantity
     * @param productPSequence
     * @param destinationLotUdfs
     * @param deassignCarrierUdfs
     * @return transferProductsToLotInfo
     * @throws CustomException
     */
    public TransferProductsToLotInfo transferProductsToLotInfoWithoutValidation(String lotName,
            int productQuantity, List<ProductP> productPSequence,
            Map<String, String> destinationLotUdfs,
            Map<String, String> deassignCarrierUdfs) throws CustomException {
        TransferProductsToLotInfo transferProductsToLotInfo = new TransferProductsToLotInfo();

        transferProductsToLotInfo.setDestinationLotName(lotName);
        transferProductsToLotInfo.setProductQuantity(productQuantity);
        transferProductsToLotInfo.setEmptyFlag("Y");
        transferProductsToLotInfo.setValidationFlag("N");
        transferProductsToLotInfo.setProductPSequence(productPSequence);
        transferProductsToLotInfo.setDestinationLotUdfs(destinationLotUdfs);
        transferProductsToLotInfo.setDeassignCarrierUdfs(deassignCarrierUdfs);

        return transferProductsToLotInfo;
    }
	
	/**
	 * 140523 by swcho, udfs added
	 * 
	 * @author smkang
	 * @since 2013-05-20
	 * @param productUSequence
	 * @return makeOnHoldInfo
	 * @throws Exception
	 */
	public MakeOnHoldInfo makeOnHoldInfo(List<ProductU> productUSequence,
			Map<String, String> udfs) throws CustomException {
		MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo();

		//makeOnHoldInfo.setProductUdfs(productUSequence);
		makeOnHoldInfo.setUdfs(udfs);

		return makeOnHoldInfo;
	}

	/**
	 * @author hykim
	 * @since 2013-04-16
	 * @param machineName
	 * @param machineRecipeName
	 * @param productCSequence
	 * @param lotUdfs
	 * @return makeLoggedInInfo
	 */
	public MakeLoggedInInfo makeLoggedInInfo(String machineName, String machineRecipeName, List<ProductC> productCSequence, Map<String, String> lotUdfs) 
	{
		MakeLoggedInInfo makeLoggedInInfo = new MakeLoggedInInfo();

		MachineKey machineKey = new MachineKey();
		machineKey.setMachineName(machineName);
		Machine machineData = MachineServiceProxy.getMachineService().selectByKey(machineKey);

		makeLoggedInInfo.setAreaName(machineData.getAreaName());
		makeLoggedInInfo.setMachineName(machineName);
		makeLoggedInInfo.setMachineRecipeName(machineRecipeName);
		makeLoggedInInfo.setConsumedMaterialSequence(new ArrayList<ConsumedMaterial>());
		makeLoggedInInfo.setProductCSequence(productCSequence);
		
        // Added by smkang on 2019.05.21 - According to Jiang Haiying's request, after StartCSTInfoCheckRequest is succeeded, 
		//								   HoldLot/ForceSampling/StartBranch/StartRework/ChangeProductSpec/ChangeProcessFlow/ChangeProcessOperation
		//								   PrepareSort/CancelPrepareSort/ForceOutSampling/CancelBranch/CancelRework/CompleteRework should be rejected.
        //								   So after TrackIn is succeeded, StartCheckResult is changed to N.
		if (lotUdfs != null)
			lotUdfs.put("STARTCHECKRESULT", "N");
		
		makeLoggedInInfo.setUdfs(lotUdfs);

		return makeLoggedInInfo;
	}

	/**
	 * @author smkang
	 * @since 2013-05-17
	 * @param lotData
	 * @param machineName
	 * @param machineRecipeName
	 * @param lotGrade
	 * @param carrierName
	 * @param processFlowName
	 * @param processOperationName
	 * @param productPGSRCSequence
	 * @param consumedMaterialSequence
	 * @param assignCarrierUdfs
	 * @param deassignCarrierUdfs
	 * @param lotUdfs
	 * @return makeLoggedOutInfo
	 * @throws Exception
	 */
	public MakeLoggedOutInfo makeLoggedOutInfo(
			Lot lotData,
			String areaName,
			Map<String, String> assignCarrierUdfs,
			String carrierName,
			// List<ConsumedMaterial> consumedMaterialSequence,
			String completeFlag, Map<String, String> deassignCarrierUdfs,
			String lotGrade, String machineName, String machineRecipeName,
			String nodeStack, String processFlowName,
			String processFlowVersion, String processOperationName,
			String processOperationVersion,
			List<ProductPGSRC> productPGSRCSequence, String reworkFlag,
			String reworkNodeId, Map<String, String> lotUdfs) {
		MakeLoggedOutInfo makeLoggedOutInfo = new MakeLoggedOutInfo();

		makeLoggedOutInfo.setAreaName(areaName);
		makeLoggedOutInfo.setAssignCarrierUdfs(assignCarrierUdfs);
		makeLoggedOutInfo.setCarrierName(carrierName);
		//makeLoggedOutInfo.setCms(new ArrayList<ConsumedMaterial>());
		makeLoggedOutInfo.setCompleteFlag(completeFlag);
		makeLoggedOutInfo.setDeassignCarrierUdfs(deassignCarrierUdfs);
		makeLoggedOutInfo.setLotGrade(lotGrade);
		makeLoggedOutInfo.setMachineName(machineName);
		makeLoggedOutInfo.setMachineRecipeName(machineRecipeName);
		makeLoggedOutInfo.setNodeStack(nodeStack);
		makeLoggedOutInfo.setProcessFlowName(processFlowName);
		makeLoggedOutInfo.setProcessFlowVersion(processFlowVersion);
		makeLoggedOutInfo.setProcessOperationName(processOperationName);
		makeLoggedOutInfo.setProcessOperationVersion(processOperationVersion);
		makeLoggedOutInfo.setProductPGSRCSequence(productPGSRCSequence);
		makeLoggedOutInfo.setReworkFlag(reworkFlag);
		makeLoggedOutInfo.setReworkNodeId(reworkNodeId);
		makeLoggedOutInfo.setUdfs(lotUdfs);

		return makeLoggedOutInfo;
	}

	/**
	 * 151118 by swcho : done overriding
	 * 
	 * @author hykim
	 * @since 2014-04-16
	 * @param productElementList
	 * @return productCSequence
	 */
	public List<ProductP> setProductPSequence(List<Element> productElementList)
			throws CustomException {
		List<ProductP> productPSequence = new ArrayList<ProductP>();

		for (Element productElement : productElementList) {
			String productName = SMessageUtil.getChildText(productElement,
					"PRODUCTNAME", true);
			Product productData = MESProductServiceProxy
					.getProductServiceUtil().getProductData(productName);

			ProductP productP = new ProductP();
			productP.setProductName(productName);

			String position = SMessageUtil.getChildText(productElement,
					"POSITION", true);
			productP.setPosition(Long.valueOf(position));

			Map<String, String> productUdfs = CommonUtil.setNamedValueSequence(
					productElement, "Product");

			productP.setUdfs(productUdfs);

			productPSequence.add(productP);
		}

		return productPSequence;
	}

	/**
	 * generate ProductP sequence with Lot
	 * 
	 * @author swcho
	 * @since 2016.09.12
	 * @param lotName
	 * @return
	 * @throws CustomException
	 */
	public List<ProductP> setProductPSequence(String lotName)
			throws CustomException {
		List<ProductP> productPSequence = new ArrayList<ProductP>();

		try {
			List<Product> productList = ProductServiceProxy.getProductService()
					.allUnScrappedProductsByLot(lotName);

			for (Product productData : productList) {
				String productName = productData.getKey().getProductName();

				ProductP productP = new ProductP();
				productP.setProductName(productName);
				productP.setPosition(productData.getPosition());

				Map<String, String> productUdfs = productData.getUdfs();

				productP.setUdfs(productUdfs);

				productPSequence.add(productP);
			}
		} catch (NotFoundSignal ne) {
			// consider 0 size
			log.error("setProductPSequence size is zero");
		}

		return productPSequence;
	}

	/**
	 * ProductU sequence generator
	 * 
	 * @author swcho
	 * @since 2015-11-27
	 * @param productElementList
	 * @return
	 * @throws CustomException
	 */
	public List<ProductU> setProductUSequence(List<Element> productElementList)
			throws CustomException {
		List<ProductU> productUSequence = new ArrayList<ProductU>();

		for (Element productElement : productElementList) {
			String productName = SMessageUtil.getChildText(productElement,
					"PRODUCTNAME", true);
			Product productData = MESProductServiceProxy
					.getProductServiceUtil().getProductData(productName);

			ProductU productP = new ProductU();
			productP.setProductName(productName);

			Map<String, String> productUdfs = CommonUtil.setNamedValueSequence(
					productElement, "Product");

			productP.setUdfs(productUdfs);

			productUSequence.add(productP);
		}

		return productUSequence;
	}

	/**
	 * @author hykim
	 * @since 2014-04-16
	 * @param lotName
	 * @return productCSequence
	 */
	public List<ProductC> setProductCSequence(String lotName) {

		List<ProductC> productCSequence = new ArrayList<ProductC>();

		try {
			List<Product> productDataList = ProductServiceProxy
					.getProductService().allUnScrappedProductsByLot(lotName);

			for (Product productData : productDataList) {
				ProductC productC = new ProductC();
				productC.setProductName(productData.getKey().getProductName());
				//productC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());

				productCSequence.add(productC);
			}
		} catch (NotFoundSignal ne) {
			// consider 0 size
			log.error("setProductCSequence size is zero");
		}

		return productCSequence;
	}

	/**
	 * 150408 by swcho : to increase flexibility
	 * 
	 * @author swcho
	 * @since 2015-11-13
	 * @param productElementList
	 * @return List<ProductPGSRC>
	 */
	public List<ProductPGSRC> setProductPGSRCSequence(List<Element> productElementList, String machineName) throws CustomException 
	{
		List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();

		for (Element productElement : productElementList) 
		{
			String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

			ProductPGSRC productPGSRC = new ProductPGSRC();
			productPGSRC.setProductName(productName);

			String position = SMessageUtil.getChildText(productElement, "POSITION", true);
			productPGSRC.setPosition(Long.valueOf(position));

			String productGrade = SMessageUtil.getChildText(productElement, "PRODUCTJUDGE", false);

			// 20170623 Modify by yudan
			if (StringUtil.isEmpty(productGrade))
			{
				productPGSRC.setProductGrade(productData.getProductGrade());
			}
			else 
			{
				if (productData.getReworkState().equals("InRework")) 
				{
					productPGSRC.setProductGrade("R");
				}
				else if (productData.getReworkState().equals("NotInRework")) 
				{
					productPGSRC.setProductGrade(productGrade);
				}
			}

			productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
			productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());
			productPGSRC.setReworkFlag("N");

			// Consumable ignored
			//productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());

			Map<String, String> productUdfs = CommonUtil.setNamedValueSequence(productElement, "Product");

			String processingInfo = SMessageUtil.getChildText(productElement, "PROCESSINGINFO", false);
			
			productUdfs.put("PROCESSINGINFO", processingInfo);
			productUdfs.put("REWORKGRADE",CommonUtil.getValue(productData.getUdfs(), "REWORKGRADE"));

/*			// PHOTO TRCKFLAG MANAGEMENT
			try
			{
				Machine machineData = MESMachineServiceProxy.getMachineServiceUtil().getMachineData(machineName);
				MachineSpec machineSpec = CommonUtil.getMachineSpecByMachineName(machineName);
				//if(StringUtil.equals(machineData.getAreaName(), GenericServiceProxy.getConstantMap().MACHINE_AREA_PHOTO))
                //{
    			//	if (StringUtil.equals(machineSpec.getUdfs().get("TRACKFLAG"), GenericServiceProxy.getConstantMap().PHOTO_TRCK) && 
    			//	        StringUtil.equals(processingInfo , GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
    			//	{
    			//	    productUdfs.put("TRACKFLAG", "Y");
    			//	}
    			//	else
    			//	{
    			//	    //productUdfs.put("TRACKFLAG", "");
    			//	    productUdfs.put("TRACKFLAG", productData.getUdfs().get("TRACKFLAG"));
    			//	}
                //}
				
				//if (StringUtil.equals(machineData.getCommunicationState(), GenericServiceProxy.getConstantMap().Mac_OnLineRemote))
				//{
				//  if(StringUtil.equals(machineData.getAreaName(), GenericServiceProxy.getConstantMap().MACHINE_AREA_ETCH))
				//    {
    			//		//if ( (StringUtil.equals(machineSpec.getUdfs().get("TRACKFLAG"), GenericServiceProxy.getConstantMap().PHOTO_DRYF) || 
    			//		//	 StringUtil.equals(machineSpec.getUdfs().get("TRACKFLAG"), GenericServiceProxy.getConstantMap().PHOTO_STRP)) && 
    			//		//	 StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
    			//	    if (StringUtil.equals(machineSpec.getUdfs().get("TRACKFLAG"), GenericServiceProxy.getConstantMap().PHOTO_STRP) && 
                //             StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
    			//		{
    			//			productUdfs.put("TRACKFLAG", "");
    			//		}
    			//	    else
    			//	    {
    			//	        productUdfs.put("TRACKFLAG", productData.getUdfs().get("TRACKFLAG"));
    			//	    }
				 //   }
				//}
				
				boolean flag = false;
				
				if(StringUtil.equals(machineData.getAreaName(), GenericServiceProxy.getConstantMap().MACHINE_AREA_PHOTO))
				{
					//add condition 'StringUtil.isEmpty(processingInfo)' -> For OPI.
    				if (StringUtil.equals(machineSpec.getUdfs().get("TRACKFLAG"), GenericServiceProxy.getConstantMap().PHOTO_TRCK) && 
                            (StringUtil.equals(processingInfo , GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P)||StringUtil.equals(processingInfo , "Y")))
                    {
                        productUdfs.put("TRACKFLAG", "Y");
                        flag = true;
                    }
                    else
                    {
                        //productUdfs.put("TRACKFLAG", "");
                        productUdfs.put("TRACKFLAG", productData.getUdfs().get("TRACKFLAG"));
                    }
    			}
				
				if(StringUtil.equals(machineData.getAreaName(), GenericServiceProxy.getConstantMap().MACHINE_AREA_PHOTO) ||
				        StringUtil.equals(machineData.getAreaName(), GenericServiceProxy.getConstantMap().MACHINE_AREA_ETCH))
				{
    				if ( StringUtil.equals(machineSpec.getUdfs().get("TRACKFLAG"), GenericServiceProxy.getConstantMap().PHOTO_STRP) && 
                            (StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P)||StringUtil.equals(processingInfo , "Y")))
                    {
                        productUdfs.put("TRACKFLAG", "");
                    }
                    else
                    {
                    	if(!flag)
                    	{
                            productUdfs.put("TRACKFLAG", productData.getUdfs().get("TRACKFLAG"));
                    	}
                    }
				}
			}
			catch (Throwable e)
			{
				log.error("TRACKFLAG Fail!");
			}*/
			
			// 160521 by swcho : for ELA
			String lastELAProcessingTime = SMessageUtil.getChildText(productElement, "LASTPROCESSENDTIME", false);
			if (!lastELAProcessingTime.isEmpty())
			{
				productUdfs.put("LASTPROCESSENDTIME", lastELAProcessingTime);
			}
			
			String ELARecipeName = SMessageUtil.getChildText(productElement, "ELARECIPENAME", false);
			if (!ELARecipeName.isEmpty())
			{
				productUdfs.put("ELARECIPENAME", ELARecipeName);
			}
			
			String ELAEnergy = SMessageUtil.getChildText(productElement, "ELAENERGYUSAGE", false);
			if (!ELAEnergy.isEmpty())
			{
				productUdfs.put("ELAENERGYUSAGE", ELAEnergy);
			}

			productPGSRC.setUdfs(productUdfs);
			productPGSRCSequence.add(productPGSRC);
		}

		return productPGSRCSequence;
	}

	/**
     * 150408 by swcho : to increase flexibility
     * 
     * @author swcho
     * @since 2015-11-13
     * @param productElementList
     * @return List<ProductPGSRC>
     */
    public List<ProductPGSRC> setProductPGSRCSequence(List<Element> productElementList, String machineName, String actualSamplePosition) throws CustomException 
    {
        boolean isProcessFlag = false;
        
        List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();

        String[] actualsamplepostion = actualSamplePosition.trim().split(",");
        
        for (Element productElement : productElementList) 
        {
            isProcessFlag = false;
            
            String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
            Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

            ProductPGSRC productPGSRC = new ProductPGSRC();
            productPGSRC.setProductName(productName);

            String position = SMessageUtil.getChildText(productElement, "POSITION", true);
            productPGSRC.setPosition(Long.valueOf(position));
            
            String productGrade = SMessageUtil.getChildText(productElement, "PRODUCTJUDGE", false);

            // 20170623 Modify by yudan
            if (StringUtil.isEmpty(productGrade))
            {
                productPGSRC.setProductGrade(productData.getProductGrade());
            }
            else 
            {
                if (productData.getReworkState().equals("InRework")) 
                {
                    productPGSRC.setProductGrade("R");
                }
                else if (productData.getReworkState().equals("NotInRework")) 
                {
                    productPGSRC.setProductGrade(productGrade);
                }
            }

            productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
            productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());
            productPGSRC.setReworkFlag(GenericServiceProxy.getConstantMap().Flag_N);

            // Consumable ignored
            //productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());

            Map<String, String> productUdfs = CommonUtil.setNamedValueSequence(productElement, "Product");

            String processingInfo = SMessageUtil.getChildText(productElement, "PROCESSINGINFO", false);
            
            productUdfs.put("PROCESSINGINFO", processingInfo);
            productUdfs.put("REWORKGRADE",CommonUtil.getValue(productData.getUdfs(), "REWORKGRADE"));

            if(StringUtil.equals(processingInfo,GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
            {
                if(actualsamplepostion != null && actualsamplepostion.length > 0)
                {
                    for(int i = 0; i < actualsamplepostion.length; i++)
                    {
                        if(StringUtil.equals(position, actualsamplepostion[i]))
                        {
                            isProcessFlag = true;
                            break;
                        }
                    }
                }
            }
            // PHOTO TRCKFLAG MANAGEMENT
            try
            {
                Machine machineData = MESMachineServiceProxy.getMachineServiceUtil().getMachineData(machineName);
                MachineSpec machineSpec = CommonUtil.getMachineSpecByMachineName(machineName);
                 if(StringUtil.equals(machineData.getAreaName(), GenericServiceProxy.getConstantMap().MACHINE_AREA_PHOTO))
                 {
                    if (StringUtil.equals(machineSpec.getUdfs().get("TRACKFLAG"), GenericServiceProxy.getConstantMap().PHOTO_TRCK) && 
                            StringUtil.equals(processingInfo , GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
                    {
                        productUdfs.put("TRACKFLAG", GenericServiceProxy.getConstantMap().Flag_Y);
                    }
                    else
                    {
                        //productUdfs.put("TRACKFLAG", "");
                        productUdfs.put("TRACKFLAG", productData.getUdfs().get("TRACKFLAG"));
                    }
                 }
                
                if (StringUtil.equals(machineData.getCommunicationState(), GenericServiceProxy.getConstantMap().Mac_OnLineRemote))
                {
                    if(StringUtil.equals(machineData.getAreaName(), GenericServiceProxy.getConstantMap().MACHINE_AREA_ETCH))
                    {
                        //if ( (StringUtil.equals(machineSpec.getUdfs().get("TRACKFLAG"), GenericServiceProxy.getConstantMap().PHOTO_DRYF) || 
                        //   StringUtil.equals(machineSpec.getUdfs().get("TRACKFLAG"), GenericServiceProxy.getConstantMap().PHOTO_STRP)) && 
                        //   StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
                        if (StringUtil.equals(machineSpec.getUdfs().get("TRACKFLAG"), GenericServiceProxy.getConstantMap().PHOTO_STRP) && 
                               StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
                        {
                            productUdfs.put("TRACKFLAG", "");
                        }
                        else
                        {
                            productUdfs.put("TRACKFLAG", productData.getUdfs().get("TRACKFLAG"));
                        }
                    }
                }
            }
            catch (Throwable e)
            {
                log.error("TRACKFLAG Fail!");
            }
            
            // 160521 by swcho : for ELA
            String lastELAProcessingTime = SMessageUtil.getChildText(productElement, "LASTPROCESSENDTIME", false);
            if (!lastELAProcessingTime.isEmpty())
            {
                productUdfs.put("LASTPROCESSENDTIME", lastELAProcessingTime);
            }
            
            String ELARecipeName = SMessageUtil.getChildText(productElement, "ELARECIPENAME", false);
            if (!ELARecipeName.isEmpty())
            {
                productUdfs.put("ELARECIPENAME", ELARecipeName);
            }
            
            String ELAEnergy = SMessageUtil.getChildText(productElement, "ELAENERGYUSAGE", false);
            if (!ELAEnergy.isEmpty())
            {
                productUdfs.put("ELAENERGYUSAGE", ELAEnergy);
            }

            if(isProcessFlag)
            {
                productUdfs.put("PROCESSFLAG", GenericServiceProxy.getConstantMap().Flag_Y);
            }
            productPGSRC.setUdfs(productUdfs);
            productPGSRCSequence.add(productPGSRC);
        }

        return productPGSRCSequence;
    }
    
	public List<ProductPGSRC> setProductPGSRCSequenceProcessingInfo(
			List<ProductPGSRC> productPGSRCSequence, String processingInfo)
			throws CustomException {
		List<ProductPGSRC> newProductPGSRCSequence = new ArrayList<ProductPGSRC>();

		for (ProductPGSRC productPGSRC : productPGSRCSequence) {
			Map<String, String> productUdfs = productPGSRC.getUdfs();
			productUdfs.remove("PROCESSINGINFO");
			productUdfs.put("PROCESSINGINFO", processingInfo);
			newProductPGSRCSequence.add(productPGSRC);
		}

		return newProductPGSRCSequence;
	}

	/**
	 * extract sequence by Lot
	 * 
	 * @author swcho
	 * @since 2016.04.26
	 * @param lotName
	 * @param productElementList
	 * @return
	 * @throws CustomException
	 */
	public List<ProductPGSRC> setProductPGSRCSequence(String lotName,
			List<Element> productElementList) throws CustomException {
		List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();

		//List<Element> productElementList = SMessageUtil.getSubSequenceItemList(bodyElement, "PRODUCTLIST", true);
		List<Product> productList;
		
		try
		{
			productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotName);
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-9999", "Product", "No product in Lot");
		}
		
		//there is no exception for productData only or productElement only.
		for (Product productData : productList) 
		{
			String productName = productData.getKey().getProductName();
			
			for (Element productElement : productElementList)
			{
				//String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
				//Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
				
				if (productName.equals(SMessageUtil.getChildText(productElement, "PRODUCTNAME", true)))
				{
					ProductPGSRC productPGSRC = new ProductPGSRC();
					productPGSRC.setProductName(productName);
					
					String position = SMessageUtil.getChildText(productElement, "POSITION", true);
					productPGSRC.setPosition(Long.valueOf(position));
					
					String productGrade = SMessageUtil.getChildText(productElement, "PRODUCTJUDGE", false);
					if (StringUtil.isEmpty(productGrade))
						productPGSRC.setProductGrade(productData.getProductGrade());
					else
						productPGSRC.setProductGrade(productGrade);
					
					productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
					productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());

					productPGSRC.setReworkFlag("N");
					
					//Consumable ignored
					//productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());

					Map<String, String> productUdfs = CommonUtil.setNamedValueSequence(productElement, "Product");
					
					String processingInfo = SMessageUtil.getChildText(productElement, "PROCESSINGINFO", false);
					productUdfs.put("PROCESSINGINFO", processingInfo);
					
					productPGSRC.setUdfs(productUdfs);

					productPGSRCSequence.add(productPGSRC);
				}
			}
		}

		return productPGSRCSequence;
	}

	public List<ProductPGSRC> setProductPGSRCSequence(Element productElement) throws CustomException
	{
		List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();

		//List<Element> productElementList = SMessageUtil.getSubSequenceItemList(bodyElement, "PRODUCTLIST", true);

		String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", false);
		Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
		
		ProductPGSRC productPGSRC = new ProductPGSRC();
		productPGSRC.setProductName(productName);
		
		String position = SMessageUtil.getChildText(productElement, "POSITION", true);
		productPGSRC.setPosition(Long.valueOf(position));
		
		String productGrade = SMessageUtil.getChildText(productElement, "PRODUCTJUDGE", false);
		
		//20170623 Modify by yudan
		if (StringUtil.isEmpty(productGrade))
			productPGSRC.setProductGrade(productData.getProductGrade());
		else 
		{
			if(productData.getReworkState().equals("InRework"))
			{
				productPGSRC.setProductGrade("R");
			}
			else if(productData.getReworkState().equals("NotInRework"))
			{
				productPGSRC.setProductGrade(productGrade);
			}
		}				
		
		productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
		productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());

		productPGSRC.setReworkFlag("N");
		
		//Consumable ignored
		//productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());

		Map<String, String> productUdfs = CommonUtil.setNamedValueSequence(productElement, "Product");
		
		/*String processingInfo = SMessageUtil.getChildText(productElement, "PROCESSINGINFO", false);*/
		productUdfs.put("PROCESSINGINFO", "");
		productUdfs.put("REWORKGRADE", CommonUtil.getValue(productData.getUdfs(), "REWORKGRADE"));
		
		//160521 by swcho : for ELA
		String lastELAProcessingTime = SMessageUtil.getChildText(productElement, "LASTPROCESSENDTIME", false);
		if (!lastELAProcessingTime.isEmpty()) productUdfs.put("LASTPROCESSENDTIME", lastELAProcessingTime);		
		String ELARecipeName = SMessageUtil.getChildText(productElement, "ELARECIPENAME", false);
		if (!ELARecipeName.isEmpty()) productUdfs.put("ELARECIPENAME", ELARecipeName);
		String ELAEnergy = SMessageUtil.getChildText(productElement, "ELAENERGYUSAGE", false);
		if (!ELAEnergy.isEmpty()) productUdfs.put("ELAENERGYUSAGE", ELAEnergy);
		
		productPGSRC.setUdfs(productUdfs);

		productPGSRCSequence.add(productPGSRC);

		return productPGSRCSequence;
	}
	
	/**
	 * sequence by Lot
	 * 
	 * @author swcho
	 * @since 2016.11.16
	 * @param lotName
	 * @return
	 * @throws CustomException
	 */
	public List<ProductPGSRC> setProductPGSRCSequence(String lotName)
			throws CustomException {
		List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();

		List<Product> productList;

		try {
			productList = LotServiceProxy.getLotService()
					.allUnScrappedProducts(lotName);
		} catch (Exception ex) {
			throw new CustomException("SYS-9999", "Product",
					"No product in Lot");
		}

		for (Product productData : productList) {
			String productName = productData.getKey().getProductName();

			ProductPGSRC productPGSRC = new ProductPGSRC();
			productPGSRC.setProductName(productName);

			productPGSRC.setPosition(productData.getPosition());

			productPGSRC.setProductGrade(productData.getProductGrade());

			productPGSRC.setSubProductQuantity1(productData
					.getSubProductQuantity1());
			productPGSRC.setSubProductQuantity2(productData
					.getSubProductQuantity2());

			productPGSRC.setReworkFlag("N");

			// Consumable ignored
			//productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());

			Map<String, String> productUdfs = productData.getUdfs();

			productPGSRC.setUdfs(productUdfs);

			productPGSRCSequence.add(productPGSRC);
		}

		return productPGSRCSequence;
	}
	
	/*
	 * Name : setProductPGSRCSequence_CellTest_OIC Desc : This function is
	 * setProductPGSRCSequence_CellTest_OIC Author : AIM Systems, Inc Date :
	 * 2019.07.09
	 */
	public static List<ProductPGSRC> setProductPGSRCSequence_Module_OIC(Element element, String lotName, String carrierType, EventInfo eventInfo) throws CustomException 
	{
		Element ListElement = element.getChild("PRODUCTLIST");
		String machineGroupName = (MESMachineServiceProxy.getMachineInfoUtil().getMachineData( element.getChildText( "MACHINENAME" ))).getMachineGroupName();
		
		List<Product> productDatas = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
		
		Lot lotData = CommonUtil.getLotInfoByLotName( lotName );
		
		ProcessOperationSpec processOperationSpecData = CommonUtil.getProcessOperationSpec( lotData, lotData.getProcessOperationName() );
 
		List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();

		for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct.hasNext();) 
		{
			Product product = iteratorProduct.next();

			for (Iterator iterator = ListElement.getChildren().iterator(); iterator.hasNext();) 
			{
				Element productE = (Element) iterator.next();
				String productName = productE.getChild("PRODUCTNAME").getText();

				try 
				{
					if (product.getKey().getProductName().equals(productName)) 
					{
						String position = productE.getChild("POSITION").getText();
					//	String productGrade = productE.getChild("PRODUCTGRADE").getText();
						String productGrade = productE.getChild("PRODUCTGRADE").getText();
						
						List<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial> cms = new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>();
						
						ProductPGSRC productPGSRC = new ProductPGSRC();
						productPGSRC.setProductName(productName);
						productPGSRC.setPosition(Long.valueOf(position).longValue());
						productPGSRC.setProductGrade(productGrade);

						productPGSRC.setConsumedMaterialSequence(cms);

						productPGSRCSequence.add(productPGSRC);
					}
				} catch (Exception e) {
					throw new CustomException("PRODUCT-9007", productName);
				}
			}
		}

		return productPGSRCSequence;
	}

	/**
	 * @author jhyeom
	 * @since 2014-04-16
	 * @param lotName
	 * @return productUdfs
	 */
	public List<ProductU> setProductUdfs(String lotName) {

		List<ProductU> productUList = new ArrayList<ProductU>();

		try {
			List<Product> productList = ProductServiceProxy.getProductService()
					.allUnScrappedProductsByLot(lotName);

			for (Product product : productList) {
				Map<String, String> productsUdfs = new HashMap<String, String>();
				productsUdfs = product.getUdfs();

				ProductU productU = new ProductU();
				productU.setProductName(product.getKey().getProductName());
				productU.setUdfs(productsUdfs);
				productUList.add(productU);
			}
		} catch (NotFoundSignal ne) {
			//
		}
		return productUList;
	}
	
	/**
	 * @author jhyeom
	 * @since 2014-04-16
	 * @param lotName
	 * @return productUdfs
	 */
	public List<ProductU> setAllProductUdfs(String lotName) {

		List<ProductU> productUList = new ArrayList<ProductU>();

		try {
			List<Product> productList = ProductServiceProxy.getProductService().allProductsByLot(lotName);

			for (Product product : productList) {
				Map<String, String> productsUdfs = new HashMap<String, String>();
				productsUdfs = product.getUdfs();

				ProductU productU = new ProductU();
				productU.setProductName(product.getKey().getProductName());
				productU.setUdfs(productsUdfs);
				productUList.add(productU);
			}
		} catch (NotFoundSignal ne) {
			//
		}
		return productUList;
	}

	/**
	 * @author aim
	 * @since 2016-12-12
	 * @param
	 * @return
	 */
	public List<ProductU> setProductUdfsProcessingInfo(
			List<ProductU> productUList, String processingInfo) {
		List<ProductU> newProductUList = new ArrayList<ProductU>();

		try {
			for (ProductU productU : productUList) {
				Map<String, String> productsUdfs = new HashMap<String, String>();
				productsUdfs.put("PROCESSINGINFO", processingInfo);
				productU.setUdfs(productsUdfs);
				newProductUList.add(productU);
			}
		} catch (NotFoundSignal ne) {

		}
		return newProductUList;
	}

	/**
	 * @author zhongsl
	 * @since 2017-07-19
	 * @param productRUList
	 * @param processingInfo
	 * @return newProductRUList
	 */
	public List<ProductRU> setProductRUdfsProcessingInfo(
			List<ProductRU> productRUList, String processingInfo) {
		List<ProductRU> newProductRUList = new ArrayList<ProductRU>();

		try {
			for (ProductRU productRU : productRUList) {
				Map<String, String> productsUdfs = new HashMap<String, String>();
				productsUdfs.put("PROCESSINGINFO", processingInfo);
				productRU.setUdfs(productsUdfs);
				newProductRUList.add(productRU);
			}
		} catch (NotFoundSignal ne) {

		}
		return newProductRUList;
	}

	/*
	 * Name : setProductGSCSequence Desc : This function is Set
	 * ProductGSCSequence Author : jhyeom Date : 2014.07.04
	 */
	public List<ProductGSC> setProductGSCSequenceForAssy(
			Lot lotData,
			List<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial> cms,
			String productName, org.jdom.Document doc) throws CustomException {
		Element root = doc.getDocument().getRootElement();
		Element bodyElement = root.getChild("Body");

		String machineName = bodyElement.getChild("MACHINENAME").getText();
		// String productJudge = bodyElement.getChild("PRODUCTJUDGE").getText();

		// 1. Set Variable
		List<ProductGSC> productPGSSequence = new ArrayList<ProductGSC>();
		List<Product> productDatas = new ArrayList<Product>();

		// 2. Get Product Data List
		productDatas = ProductServiceProxy.getProductService()
				.allProductsByLot(lotData.getKey().getLotName());

		// 3. Get ProductName, Position By Product
		for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct
				.hasNext();) {
			Product product = iteratorProduct.next();
			if (productName.equals(product.getKey().getProductName())) {
				ProductGSC productGSC = new ProductGSC();

				String subProductQuantity1 = String.valueOf(product
						.getSubProductQuantity1());
				String subProductQuantity2 = String.valueOf(product
						.getSubProductQuantity2());
				String productGrade = CommonUtil.getProductAttributeByLength(
						machineName, productName, "PRODUCTGRADE",
						product.getProductGrade());
				// String productSubProductGrades1 =
				// CommonUtil.getProductAttributeByLength(machineName,
				// productName, "SUBPRODUCTGRADES1", productJudge);

				//productGSC.setCms(cms);
				productGSC.setProductGrade(productGrade);
				productGSC.setProductName(product.getKey().getProductName());
				// productGSC.setSubProductGrades1(productSubProductGrades1);
				productGSC.setSubProductGrades1(product.getSubProductGrades1());
				productGSC.setSubProductGrades2(product.getSubProductGrades2());
				productGSC.setSubProductQuantity1(Double.valueOf(
						subProductQuantity1).doubleValue());
				productGSC.setSubProductQuantity2(Double.valueOf(
						subProductQuantity2).doubleValue());

				Map<String, String> productUdfs = product.getUdfs();
				productUdfs
						.put("PAIRPRODUCTNAME", cms.get(0).getMaterialName());
				// productUdfs.put("ASSAYPRODUCTNAME",
				// product.getKey().getProductName());

				productGSC.setUdfs(productUdfs);

				// Add productPSequence By Product
				productPGSSequence.add(productGSC);
			}
		}
		return productPGSSequence;
	}

	/**
	 * @author jhyeom
	 * @since 2014-04-28
	 */
	public static Map<String, String> copyUdfs1(Map<String, String> fromUdfs,
			Map<String, String> toUdfs) {

		for (String key : fromUdfs.keySet()) {
			toUdfs.put(key, fromUdfs.get(key));
		}

		return toUdfs;
	}

	public Lot getLotData(String lotName) throws FrameworkErrorSignal,
			NotFoundSignal, CustomException {
		try {
			LotKey lotKey = new LotKey();
			lotKey.setLotName(lotName);
			Lot lotData = LotServiceProxy.getLotService().selectByKey(lotKey);

			return lotData;

		} catch (Exception e) {
			throw new CustomException("LOT-9000", lotName);
		}
	}
	
	public LotHistory getLotHistory(String lotName) throws FrameworkErrorSignal,
	NotFoundSignal, CustomException {
		try {
			LotKey lotKey = new LotKey();
			lotKey.setLotName(lotName);
			Lot lotData = LotServiceProxy.getLotService().selectByKey(lotKey);
			
			LotHistoryKey lotHKey = new LotHistoryKey();
			lotHKey.setLotName(lotName);
			lotHKey.setTimeKey(lotData.getLastEventTimeKey());
			
			LotHistory lotHData = LotServiceProxy.getLotHistoryService().selectByKey(lotHKey);
		
			return lotHData;
		
		} catch (Exception e) {
			throw new CustomException("LOT-9000", lotName);
		}
	}

	/**
	 * 131025 by swcho : modified
	 * 
	 * @author swcho
	 * @since 2013.10.25
	 * @param carrierName
	 * @return
	 * @throws FrameworkErrorSignal
	 * @throws NotFoundSignal
	 */
	public String getLotNameByCarrierName(String carrierName)
			throws FrameworkErrorSignal, NotFoundSignal {
		if (log.isInfoEnabled()) {
			log.info("carrierName = " + carrierName);
		}

		String lotName = "";
		if (carrierName != null && carrierName != "") {
			String condition = "WHERE carrierName = ? And RowNum = ?";

			Object[] bindSet = new Object[] { carrierName, 1 };

			try {
				List<Lot> arrayList = LotServiceProxy.getLotService().select(
						condition, bindSet);
				lotName = arrayList.get(0).getKey().getLotName();
			} catch (Exception e) {
				log.error(e);
				lotName = "";
			}
		}

		if (log.isInfoEnabled()) {
			log.info("Return lotName = " + lotName);
		}

		return lotName;
	}

	/**
	 * 131025 by swcho : modified
	 * 
	 * @author swcho
	 * @since 2013.10.25
	 * @param carrierName
	 * @return
	 * @throws FrameworkErrorSignal
	 * @throws NotFoundSignal
	 */
	public String getCarrierNameByLotName(String lotName)
			throws FrameworkErrorSignal, NotFoundSignal {

		String carrierName = "";
		if (lotName != null && lotName != "") {
			LotKey lotKey = new LotKey();
			lotKey.setLotName(lotName);

			Lot lot = null;

			try {
				lot = LotServiceProxy.getLotService().selectByKey(lotKey);
				carrierName = lot.getCarrierName();
			} catch (Exception e) {
				log.error(e);
				carrierName = "";
			}

		} else {
		}

		return carrierName;
	}

	public Lot getLotInfoBydurableName(String carrierName)
			throws CustomException {
		// log.info("START getLotInfoBydurableName");
		String condition = "WHERE carrierName = :carrierName ";

		Object[] bindSet = new Object[] { carrierName };
		List<Lot> lotList = new ArrayList<Lot>();
		try {
			lotList = LotServiceProxy.getLotService()
					.select(condition, bindSet);
		} catch (Exception e) {
			throw new CustomException("CARRIER-9002", carrierName);
		}
		// log.info("END getLotInfoBydurableName");
		return lotList.get(0);
	}

	/**
	 * getProcessFlowData
	 * 
	 * @author hykim
	 * @since 2014.04.22
	 * @param lotName
	 * @return ProcessFlow
	 * @throws CustomException
	 */
	public ProcessFlow getProcessFlowData(Lot lotData) throws CustomException {
		ProcessFlowKey processFlowKey = new ProcessFlowKey();
		processFlowKey.setFactoryName(lotData.getFactoryName());
		processFlowKey.setProcessFlowName(lotData.getProcessFlowName());
		processFlowKey.setProcessFlowVersion(lotData.getProcessFlowVersion());
		ProcessFlow processFlowData = ProcessFlowServiceProxy
				.getProcessFlowService().selectByKey(processFlowKey);

		return processFlowData;
	}

	/**
	 * generic PGSSequence generator
	 * 
	 * @author swcho
	 * @since 2015.05.06
	 * @param lotName
	 * @param productGrade
	 * @param productList
	 * @param udfs
	 * @return
	 * @throws CustomException
	 */
	public List<ProductPGS> getProductPGSSequence(String lotName,
			String productGrade, List<Product> productList,
			Map<String, String> udfs) throws CustomException {
		List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();

		for (Iterator<Product> iteratorProduct = productList.iterator(); iteratorProduct
				.hasNext();) {
			Product product = iteratorProduct.next();

			ProductPGS productPGS = new ProductPGS();

			productPGS.setProductName(product.getKey().getProductName());
			productPGS.setPosition(product.getPosition());
			productPGS.setProductGrade(productGrade);
			productPGS.setSubProductGrades1(product.getSubProductGrades1());
			productPGS.setSubProductGrades2(product.getSubProductGrades2());
			productPGS.setSubProductQuantity1(product.getSubProductQuantity1());
			productPGS.setSubProductQuantity2(product.getSubProductQuantity2());

			if (udfs != null)
				productPGS.setUdfs(udfs);
			else
				productPGS.setUdfs(product.getUdfs());

			productPGSSequence.add(productPGS);
		}

		return productPGSSequence;
	}

	/**
	 * 현재 LOT으로 ChangeSpecInfo를 만들어서 리턴한다
	 * 
	 * @param lot
	 * @return ChangeSpecInfo
	 */
	// -COMMENT-
	// 2011.02.10 PARK SANG HYUN // 2016.02.17 LEE HYEON WOO
	public ChangeSpecInfo getCurrentChangSpectInfo(Lot lot) {
		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();
		changeSpecInfo.setProductionType(lot.getProductionType());
		changeSpecInfo.setProductSpecName(lot.getProductSpecName());
		changeSpecInfo.setProductSpecVersion(lot.getProductSpecVersion());
		changeSpecInfo.setProductSpec2Name(lot.getProductSpec2Name());
		changeSpecInfo.setProductSpec2Version(lot.getProductSpec2Version());
		//2019.02.22 Modify, hsryu, lotData.getProductRequestName -> "", Because WorkOrder of Product must not be changed.
		//changeSpecInfo.setProductRequestName(lot.getProductRequestName());
		changeSpecInfo.setSubProductUnitQuantity1(lot
				.getSubProductUnitQuantity1());
		changeSpecInfo.setSubProductUnitQuantity2(lot
				.getSubProductUnitQuantity2());
		changeSpecInfo.setDueDate(lot.getDueDate());
		changeSpecInfo.setPriority(lot.getPriority());
		changeSpecInfo.setFactoryName(lot.getFactoryName());
		changeSpecInfo.setAreaName(lot.getAreaName());
		changeSpecInfo.setLotState(lot.getLotState());
		changeSpecInfo.setLotProcessState(lot.getLotProcessState());
		changeSpecInfo.setLotHoldState(lot.getLotHoldState());
		changeSpecInfo.setProcessFlowName(lot.getProcessFlowName());
		changeSpecInfo.setProcessFlowVersion(lot.getProcessFlowVersion());
		changeSpecInfo.setProcessOperationName(lot.getProcessOperationName());
		changeSpecInfo.setProcessOperationVersion(lot
				.getProcessOperationVersion());
		changeSpecInfo.setNodeStack(lot.getNodeStack());
		changeSpecInfo.setUdfs(lot.getUdfs());
		//changeSpecInfo.setProductUdfs(new ArrayList<ProductU>());
		return changeSpecInfo;
	}

	// -COMMENT-
	// 2010.02.09 JUNG SUN KYU //2016.02.18 LEE HYEON WOO
	public ChangeSpecInfo changeProcessOperationInfo(Lot lotData,
			String ProcessFlowName, String processOperationName,
			String reasonCodeSize) throws Exception {
		String nodeId = null;
		nodeId = NodeStack.getNodeID(lotData.getFactoryName(), ProcessFlowName,
				processOperationName);

		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();

		changeSpecInfo.setProductionType(lotData.getProductionType());
		changeSpecInfo.setProductSpecName(lotData.getProductSpecName());
		changeSpecInfo.setProductSpecVersion(lotData.getProductSpecVersion());
		changeSpecInfo.setProductSpec2Name(lotData.getProductSpec2Name());
		changeSpecInfo.setProductSpec2Version(lotData.getProductSpec2Version());
		//2019.02.22 Modify, hsryu, lotData.getProductRequestName -> "", Because WorkOrder of Product must not be changed.
		//changeSpecInfo.setProductRequestName(lotData.getProductRequestName());
		changeSpecInfo.setSubProductUnitQuantity1(lotData
				.getSubProductUnitQuantity1());
		changeSpecInfo.setSubProductUnitQuantity2(lotData
				.getSubProductUnitQuantity2());
		changeSpecInfo.setDueDate(lotData.getDueDate());
		changeSpecInfo.setPriority(lotData.getPriority());
		changeSpecInfo.setFactoryName(lotData.getFactoryName());
		changeSpecInfo.setAreaName(lotData.getAreaName());
		changeSpecInfo.setLotState(lotData.getLotState());
		changeSpecInfo.setLotProcessState(lotData.getLotProcessState());
		changeSpecInfo.setLotHoldState(lotData.getLotHoldState());
		changeSpecInfo.setProcessFlowName(ProcessFlowName);
		changeSpecInfo.setProcessFlowVersion(lotData.getProcessFlowVersion());
		changeSpecInfo.setProcessOperationName(processOperationName);
		changeSpecInfo.setProcessOperationVersion(lotData
				.getProcessOperationVersion());
		changeSpecInfo.setNodeStack(nodeId);

		Map<String, String> lotUdfs = lotData.getUdfs();
		lotUdfs.put("REASONCODESIZE", reasonCodeSize);
		changeSpecInfo.setUdfs(lotUdfs);

		return changeSpecInfo;
	}

	// -COMMENT-
	// 2011.02.10 PARK SANG HYUN // 2016.02.18 LEE HYEON WOO
	public List<ProductU> getModuleAllProductUSequence(Lot lotData) {
		// 1. Set Variable
		List<ProductU> ProductUSequence = new ArrayList<ProductU>();

		// 2. Get Product Data List
		try {
			ProductU productU = new ProductU();

			productU.setProductName(lotData.getKey().getLotName());

			productU.setUdfs(lotData.getUdfs());

			ProductUSequence.add(productU);

		} catch (Exception e) {
		}
		return ProductUSequence;
	}

	// -COMMENT- DT
	// 2011.02.15 ALLEN YONG ZENG // 2016.02.23 LEE HYEON WOO
	public static String getLineName(String machineName) {
		String lineName = "";

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT LINENAME FROM MACHINESPEC WHERE MACHINENAME=:machineName");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("machineName", machineName);

		lineName = greenFrameServiceProxy.getSqlTemplate()
				.getSimpleJdbcTemplate().queryForMap(sql.toString(), bindMap)
				.get("LINENAME").toString();
		return lineName;
	}

	// -COMMENT- DT
	// 2011.02.15 ALLEN YONG ZENG // 2016.02.23 LEE HYEON WOO
	public static long increaseCurrentSampleQuantity(String lineName,
			String productSpecName) {
		long currentQuantity = 0;

		StringBuilder sql = null;
		Map<String, Object> bindMap = new HashMap<String, Object>();

		sql = new StringBuilder();
		sql.append(" SELECT CURRENTSAMPLEQUANTITY")
				.append("   FROM CT_OQALOTQUANTITY")
				.append("  WHERE LINENAME = :lineName")
				.append("    AND PRODUCTSPECNAME = :productSpecName");

		bindMap.put("lineName", lineName);
		bindMap.put("productSpecName", productSpecName);
		Map<String, Object> quantityInfo = null;

		try {
			quantityInfo = greenFrameServiceProxy.getSqlTemplate()
					.getSimpleJdbcTemplate()
					.queryForMap(sql.toString(), bindMap);
		} catch (Exception e) {
			// just ignore
		}

		if (quantityInfo != null && !quantityInfo.isEmpty()) {
			if (quantityInfo.get("CURRENTSAMPLEQUANTITY") != null) {
				currentQuantity = Long.valueOf(quantityInfo.get(
						"CURRENTSAMPLEQUANTITY").toString());
			}
			currentQuantity += 1;

			sql = new StringBuilder();
			sql.append(
					" UPDATE CT_OQALOTQUANTITY SET CURRENTSAMPLEQUANTITY=:currentQuantity")
					.append("  WHERE LINENAME=:lineName and PRODUCTSPECNAME = :productSpecName");

			bindMap.put("currentQuantity", currentQuantity);

			greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate()
					.update(sql.toString(), bindMap);
		} else {
			currentQuantity += 1;

			sql = new StringBuilder();
			sql.append(
					" INSERT INTO CT_OQALOTQUANTITY(LINENAME, PRODUCTSPECNAME, CURRENTSAMPLEQUANTITY)")
					.append("      VALUES(:lineName, :productSpecName, :currentQuantity)");

			bindMap.put("currentQuantity", currentQuantity);

			greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate()
					.update(sql.toString(), bindMap);
		}

		return currentQuantity;
	}

	// -COMMENT- DT
	// 2011.02.15 ALLEN YONG ZENG // 2016.02.23 LEE HYEON WOO
	public static long getOQALotSampleQuantity(String productSpecName) {
		long oqaLotSampleQuantity = 0;

		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT OQALOTSAMPLEQUANTITY FROM PRODUCTSPEC")
				.append("  WHERE PRODUCTSPECNAME=:productSpecName")
				.append("    AND FACTORYNAME='MODULE'")
				.append("    AND PRODUCTSPECVERSION='00001'");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("productSpecName", productSpecName);

		Map<String, Object> result = greenFrameServiceProxy.getSqlTemplate()
				.getSimpleJdbcTemplate().queryForMap(sql.toString(), bindMap);
		if (result != null
				&& result.get("OQALOTSAMPLEQUANTITY") != null
				&& StringUtils.isNotEmpty(result.get("OQALOTSAMPLEQUANTITY")
						.toString())) {
			oqaLotSampleQuantity = Long.valueOf(result.get(
					"OQALOTSAMPLEQUANTITY").toString());
		}

		return oqaLotSampleQuantity;
	}

	// -COMMENT-
	// 2010.02.17 JUNG SUN KYU
	public static Lot setConsumableUdfsData(List<ConsumedMaterial> cms,
			Lot lotData) throws CustomException {

		// 쿼리문 생성
		String sql = "SELECT MATERIALTYPE FROM TOPOLICY T, POSMATERIALPOSITION P "
				+ "WHERE T.CONDITIONID = P.CONDITIONID "
				+ "AND PROCESSOPERATIONNAME = " + ":processOperationName";

		Map<String, String> bindMap = new HashMap<String, String>();

		bindMap.put("processOperationName", lotData.getProcessOperationName());

		// JDBC를 통해 값 호출
		List<Map<String, Object>> sqlResult = greenFrameServiceProxy
				.getSqlTemplate().getSimpleJdbcTemplate()
				.queryForList(sql, bindMap);

		List<String> argSeq = new ArrayList<String>();

		// String List 값으로 변환
		for (int i = 0; i < sqlResult.size(); i++) {
			argSeq.add(sqlResult.get(i).get("MaterialType").toString());
		}

		// 반복문을 통해 ConsumableType값 비교
		for (int j = 0; j < cms.size(); j++) {
			String consumableType = cms.get(j).getMaterialType().toString();
			String consumableName = cms.get(j).getMaterialName().toString();

			int count = 0;

			for (int k = 0; k < argSeq.size(); k++) {
				if (consumableType.equals(argSeq.get(k).toString())) {
					if (StringUtils.isEmpty(consumableName)) {
						throw new CustomException("PROCESSOPERATION-9003",
								lotData.getProcessOperationName());
					} else {
						Map<String, String> lotUdfs = lotData.getUdfs();
						lotUdfs.put(consumableType, consumableName);
						count++;
					}
				}
			}

			// 매칭되는 경우가 없는 경우 에러 발생
			if (count == 0) {
				throw new CustomException("PROCESSOPERATION-9002",
						lotData.getProcessOperationName());
			}

		}
		return lotData;
	}

	// -- COMMENT
	// 2016.02.23 LEE HYEON WOO
	public static MakeLoggedOutInfo makeLoggedOutInfo(Lot lotData,
			String areaName, String carrierName, List<ConsumedMaterial> cms,
			String completeFlag, String lotGrade, String machineName,
			String machineRecipeName, String nodeStack, String processFlowName,
			String processFlowVersion, String processOperationName,
			String processOperationVersion,
			List<ProductPGSRC> productPGSRCSequence, String reworkNodeId,
			String reworkFlag) {
		MakeLoggedOutInfo makeLoggedOutInfo = new MakeLoggedOutInfo();

		makeLoggedOutInfo.setAreaName(areaName);
		Map<String, String> assignCarrierUdfs = new HashMap<String, String>();
		makeLoggedOutInfo.setAssignCarrierUdfs(assignCarrierUdfs);

		makeLoggedOutInfo.setCarrierName(carrierName);
		//makeLoggedOutInfo.setCms(cms);
		makeLoggedOutInfo.setCompleteFlag(completeFlag);

		Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
		makeLoggedOutInfo.setDeassignCarrierUdfs(deassignCarrierUdfs);

		makeLoggedOutInfo.setLotGrade(lotGrade);
		makeLoggedOutInfo.setMachineName(machineName);
		makeLoggedOutInfo.setMachineRecipeName(machineRecipeName);
		makeLoggedOutInfo.setNodeStack(nodeStack);
		makeLoggedOutInfo.setProcessFlowName(processFlowName);
		makeLoggedOutInfo.setProcessFlowVersion(processFlowVersion);
		makeLoggedOutInfo.setProcessOperationName(processOperationName);
		makeLoggedOutInfo.setProcessOperationVersion(processOperationVersion);
		makeLoggedOutInfo.setProductPGSRCSequence(productPGSRCSequence);
		makeLoggedOutInfo.setReworkFlag(reworkFlag);
		makeLoggedOutInfo.setReworkNodeId(reworkNodeId);

		Map<String, String> lotUdfs = lotData.getUdfs();
		makeLoggedOutInfo.setUdfs(lotUdfs);

		return makeLoggedOutInfo;
	}

	// -COMMENT-
	// 2010.01.30 ALLEN YONG ZENG // 2016.02.23 LEE HYEON WOO
	public static void setTrackOutReturnMessage(String lotName, Document message)
			throws CustomException {
		Element bodyElem = message.getRootElement().getChild(
				SMessageUtil.Body_Tag);
		Lot lotData = CommonValidation.checkPanelExist(lotName);
		Map<String, String> lotUdfs = lotData.getUdfs();

		String[] nodeNames = new String[] { "REVISIONCODE", "CHECKINCODE",
				"LEVELNO", "GROUPNAME", "MODULENAME", "GRADE" };
		String[] nodeValues = new String[] { lotUdfs.get("RevisionCode"),
				lotUdfs.get("CheckInCode"), lotUdfs.get("LevelNo"),
				lotUdfs.get("GroupName"), lotUdfs.get("ModuleName"),
				lotData.getLotGrade() };

		for (int i = 0; i < nodeNames.length; i++) {
			Element node = bodyElem.getChild(nodeNames[i]);
			if (node == null) {
				node = new Element(nodeNames[i]);
				bodyElem.addContent(node);
			}
			if (StringUtils.isEmpty(node.getText())) {
				node.setText(nodeValues[i]);
			}
		}
	}

	// 2016.03.02 by LEE HYEON WOO
	public CreateRawInfo makeCreateRawInfo(Lot lotData, String productName) {
		CreateRawInfo createRawInfo = new CreateRawInfo();
		createRawInfo.setLotName(productName);
		createRawInfo.setProductSpecName(lotData.getProductSpecName());
		createRawInfo.setProductSpecVersion("00001");
		createRawInfo.setProductSpec2Name(lotData.getProductSpec2Name());
		createRawInfo.setProductSpec2Version(lotData.getProductSpec2Version());
		createRawInfo.setOriginalLotName(lotData.getOriginalLotName());
		createRawInfo.setSourceLotName(lotData.getSourceLotName());
		createRawInfo.setCarrierName(lotData.getCarrierName());
		createRawInfo.setProductType(lotData.getProductType());
		createRawInfo.setSubProductType(lotData.getSubProductType());
		// createRawInfo.setSubProductUnitQuantity1(lotData.getSubProductUnitQuantity1());
		// createRawInfo.setSubProductUnitQuantity2(lotData.getSubProductUnitQuantity2());
		createRawInfo.setProductQuantity(lotData.getProductQuantity());
		createRawInfo.setLotGrade(lotData.getLotGrade());
		createRawInfo.setDueDate(lotData.getDueDate());
		createRawInfo.setPriority(lotData.getPriority());
		createRawInfo.setFactoryName(lotData.getFactoryName());
		createRawInfo.setAreaName(lotData.getAreaName());
		createRawInfo.setLotState(lotData.getLotState());
		createRawInfo.setLotProcessState(lotData.getLotProcessState());
		createRawInfo.setLotHoldState(lotData.getLotHoldState());
		createRawInfo.setLastLoggedInTime(lotData.getLastLoggedInTime());
		createRawInfo.setLastLoggedInUser(lotData.getLastLoggedInUser());
		createRawInfo.setLastLoggedOutTime(lotData.getLastLoggedOutTime());
		createRawInfo.setLastLoggedInUser(lotData.getLastLoggedOutUser());
		createRawInfo.setProcessFlowName(lotData.getProcessFlowName());
		createRawInfo.setProcessFlowVersion("00001");
		createRawInfo
				.setProcessOperationName(lotData.getProcessOperationName());
		createRawInfo.setProcessOperationVersion("00001");
		createRawInfo.setNodeStack(lotData.getNodeStack());
		// createRawInfo.setReworkFlag("N");
		createRawInfo.setReworkCount(lotData.getReworkCount());
		createRawInfo.setReworkNodeId(lotData.getReworkNodeId());
		createRawInfo.setMachineName(lotData.getMachineName());
		createRawInfo.setMachineRecipeName("");

		Map<String, String> lotUdfs = lotData.getUdfs();
		createRawInfo.setUdfs(lotUdfs);

		return createRawInfo;
	}

	/**
	 * @author 170428 add by lszhen
	 * @param productData
	 * @param newProductName
	 * @return
	 */
	public ProductNPGS setProductNPGS(Product productData, String newProductName) {
		ProductNPGS productNPGS = new ProductNPGS();

		productNPGS.setProductName(productData.getKey().getProductName());
		productNPGS.setNewProductName(newProductName);
		productNPGS.setNewPosition(productData.getPosition());
		productNPGS.setNewProductGrade(productData.getProductGrade());
		productNPGS.setNewSubProductGrades1(productData.getSubProductGrades1());
		productNPGS.setNewSubProductGrades2(productData.getSubProductGrades2());
		productNPGS.setNewSubProductQuantity1(productData
				.getSubProductUnitQuantity1());
		productNPGS.setNewSubProductQuantity2(productData
				.getSubProductUnitQuantity2());
		productNPGS.setUdfs(productData.getUdfs());
		productNPGS.setNewUdfs(productData.getUdfs());

		return productNPGS;
	}
	
	/*
	 * Name : makeNotInRework Desc : This function is makeNotInRework Author :
	 * AIM Systems, Inc Date : 2011.03.28
	 */
	public MakeInReworkInfo makeInReworkInfoForCancelRW(Lot lotData,
			EventInfo eventInfo, String lotName, String returnFlowName,
			String returnOperationName, String beforeNodeId, Map<String, String> udfs,
			List<ProductU> productU) throws CustomException {
		 
		MakeInReworkInfo makeInReworkInfo = new MakeInReworkInfo();
		List<ProductRU> productRUdfs = new ArrayList<ProductRU>();


		makeInReworkInfo.setAreaName(lotData.getAreaName());
		makeInReworkInfo.setProcessFlowName(returnFlowName);
		makeInReworkInfo.setProcessFlowVersion("00001");
		makeInReworkInfo.setProcessOperationName(returnOperationName);
		makeInReworkInfo.setProcessOperationVersion("00001");
		makeInReworkInfo.setReworkNodeId(beforeNodeId);

		/* 2018.02.22 hsryu - to not need.
		String returnNodeId = null;
		try {
			returnNodeId = NodeStack.getNodeID(lotData.getFactoryName(),
					returnFlowName, returnOperationName);
		} catch (Exception e) {
		}
		 */
		
		NodeStack nodeStack = NodeStack.stringToNodeStack(lotData
					.getNodeStack());

		int lastIndex = nodeStack.size();
		nodeStack.remove(lastIndex - 1);

		String strNodeStack = NodeStack.nodeStackToString(nodeStack);
		
		makeInReworkInfo.setNodeStack(strNodeStack);
		
		/*
		makeNotInReworkInfo.setNodeStack(returnNodeId);
		
		
		udfs = lotData.getUdfs();

		udfs.put("RETURNFLOWNAME", "");
		udfs.put("RETURNOPERATIONNAME", "");
		*/
		
		for (ProductU product : productU) 
		{					
			ProductRU productRU = new ProductRU();
			productRU.setProductName(product.getProductName());						
			productRU.setUdfs(product.getUdfs());
			/*
			if (productNameList.contains(product.getKey().getProductName()))
			{
				productRU.setReworkFlag("Y");
			}
			else
			{
				productRU.setReworkFlag("N");
			}
			*/
		
			productRU.setReworkFlag("N");
			
			productRUdfs.add(productRU);
		}				

		makeInReworkInfo.setUdfs(udfs);
		//makeInReworkInfo.setProductRUdfs(productRUdfs);

		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey"
				+ eventInfo.getEventTimeKey());

		return makeInReworkInfo;

	}
	
	
	
	/*
	 * Name : makeNotInRework Desc : This function is makeNotInRework Author :
	 * AIM Systems, Inc Date : 2011.03.28
	 */
	public MakeNotInReworkInfo newMakeNotInReworkInfo(Lot lotData,
			EventInfo eventInfo, String lotName, String returnFlowName,
			String returnOperationName, Map<String, String> udfs,
			List<ProductU> productU) throws CustomException {
		MakeNotInReworkInfo makeNotInReworkInfo = new MakeNotInReworkInfo();

		makeNotInReworkInfo.setAreaName(lotData.getAreaName());
		makeNotInReworkInfo.setProcessFlowName(returnFlowName);
		makeNotInReworkInfo.setProcessFlowVersion("00001");
		makeNotInReworkInfo.setProcessOperationName(returnOperationName);
		makeNotInReworkInfo.setProcessOperationVersion("00001");

		/* 2018.02.22 hsryu - do not need.
		String returnNodeId = null;
		try {
			returnNodeId = NodeStack.getNodeID(lotData.getFactoryName(),
					returnFlowName, returnOperationName);
		} catch (Exception e) {
		}
		 */
		
		/*NodeStack nodeStack = NodeStack.stringToNodeStack(lotData
					.getNodeStack());

		int lastIndex = nodeStack.size();
		nodeStack.remove(lastIndex - 1);

		String strNodeStack = NodeStack.nodeStackToString(nodeStack);
		*/

		makeNotInReworkInfo.setNodeStack(lotData.getNodeStack());
		
		/*
		makeNotInReworkInfo.setNodeStack(returnNodeId);
		
		
		udfs = lotData.getUdfs();

		udfs.put("RETURNFLOWNAME", "");
		udfs.put("RETURNOPERATIONNAME", "");
		*/

		makeNotInReworkInfo.setUdfs(udfs);
		//makeNotInReworkInfo.setProductUdfs(productU);

		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey"
				+ eventInfo.getEventTimeKey());

		return makeNotInReworkInfo;

	}
	
	
	
	/*
	 * Name : makeNotInRework Desc : This function is makeNotInRework Author :
	 * AIM Systems, Inc Date : 2011.03.28
	 */
	public MakeNotInReworkInfo makeNotInReworkInfoForTO(Lot lotData,
			EventInfo eventInfo, String lotName, String returnFlowName,
			String returnOperationName, Map<String, String> udfs,
			List<ProductU> productU) throws CustomException {
		MakeNotInReworkInfo makeNotInReworkInfo = new MakeNotInReworkInfo();

		makeNotInReworkInfo.setAreaName(lotData.getAreaName());
		makeNotInReworkInfo.setProcessFlowName(returnFlowName);
		makeNotInReworkInfo.setProcessFlowVersion("00001");
		makeNotInReworkInfo.setProcessOperationName(returnOperationName);
		makeNotInReworkInfo.setProcessOperationVersion("00001");

		/* 2018.02.22 hsryu - do not need.
		String returnNodeId = null;
		try {
			returnNodeId = NodeStack.getNodeID(lotData.getFactoryName(),
					returnFlowName, returnOperationName);
		} catch (Exception e) {
		}
		 */
		
		NodeStack nodeStack = NodeStack.stringToNodeStack(lotData
					.getNodeStack());

		//int lastIndex = nodeStack.size();
		//nodeStack.remove(lastIndex - 1);

		String strNodeStack = NodeStack.nodeStackToString(nodeStack);
		
		makeNotInReworkInfo.setNodeStack(strNodeStack);
		
		/*
		makeNotInReworkInfo.setNodeStack(returnNodeId);
		
		
		udfs = lotData.getUdfs();

		udfs.put("RETURNFLOWNAME", "");
		udfs.put("RETURNOPERATIONNAME", "");
		*/

		makeNotInReworkInfo.setUdfs(udfs);
		//makeNotInReworkInfo.setProductUdfs(productU);

		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey"
				+ eventInfo.getEventTimeKey());

		return makeNotInReworkInfo;

	}
	
	
	
	/**
	 * if target not specified, system would find next automatically on same
	 * flow, but return could be not avail otherwise, once target is specified,
	 * that is better to set return than not
	 * 
	 * @author swcho
	 * @since 2015-12-07
	 * @param lotName
	 * @param productionType
	 * @param productSpecName
	 * @param productSpecVersion
	 * @param productSpec2Name
	 * @param productSpec2Version
	 * @param productRequestName
	 * @param subProductUnitQuantity1
	 * @param subProductUnitQuantity2
	 * @param dueDate
	 * @param priority
	 * @param factoryName
	 * @param areaName
	 * @param lotState
	 * @param lotProcessState
	 * @param lotHoldState
	 * @param processFlowName
	 * @param processFlowVersion
	 * @param processOperationName
	 * @param processOperationVersion
	 * @param targetFlowName
	 * @param targetOperationName
	 * @param returnFlowName
	 * @param returnOperationName
	 * @param lotUdfs
	 * @param productUdfs
	 * @param moveFlag
	 * @return
	 * @throws CustomException
	 */
	public ChangeSpecInfo changeSpecInfoForForceOutSampling(String lotName, String productionType,
			String productSpecName, String productSpecVersion,
			String productSpec2Name, String productSpec2Version,
			String productRequestName, double subProductUnitQuantity1,
			double subProductUnitQuantity2, Timestamp dueDate, long priority,
			String factoryName, String areaName, String lotState,
			String lotProcessState, String lotHoldState,
			String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion,
			String targetFlowName, String targetOperationName,
			String returnFlowName, String returnOperationName,
			String currentNodeStack, Map<String, String> lotUdfs,
			List<ProductU> productUdfs, boolean moveFlag, boolean sampleFlag)
			throws CustomException {
		if (lotUdfs == null)
			lotUdfs = new HashMap<String, String>();
		
		// prepare transition info
		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo(productionType,
				productSpecName, productSpecVersion, productSpec2Name,
				productSpec2Version, productRequestName,
				subProductUnitQuantity1, subProductUnitQuantity2, dueDate,
				priority, factoryName, areaName, lotState, lotProcessState,
				lotHoldState, "", "", "", "", "", productUdfs);
		
		StringBuilder nodeStackBuilder = new StringBuilder();
		
		String postNodeId;
		try {
			if (!StringUtil.isEmpty(targetFlowName)
					&& !StringUtil.isEmpty(targetOperationName)) {
				Node nextNode = ProcessFlowServiceProxy
						.getNodeService()
						.getNode(factoryName, targetFlowName, "00001",
								"ProcessOperation", targetOperationName, "");
				postNodeId = nextNode.getKey().getNodeId();
				
			} else {
				// toward next on identical flow
				// no need of return address
				postNodeId = "";
			}
		} catch (Exception ex) 
		{
			throw new CustomException("SPEC-1001", factoryName,
					targetFlowName, targetOperationName);
		}
		
		String priorNodeId = "";
		
		priorNodeId = currentNodeStack;
		
		/*if (!StringUtil.isEmpty(currentNodeStack) && currentNodeStack.contains(".")) 
		{
			String[] arrayNodeStack = StringUtil.split(
					currentNodeStack, ".");
			// tail catch
			if (arrayNodeStack.length > 1) 
			{
				if(sampleFlag)
				{
					// departed from other flow
					for(int i=0; i<arrayNodeStack.length-1;i++)
					{
						priorNodeId += arrayNodeStack[i]+".";
					}
					//priorNodeId = arrayNodeStack[arrayNodeStack.length - 2];
				}
				else
				{
					// departed from other flow
					for(int i=0; i<arrayNodeStack.length-2;i++)
					{
						priorNodeId += arrayNodeStack[i]+".";
					}
					//priorNodeId = arrayNodeStack[arrayNodeStack.length - 2];

				}
			} 
			else 
			{
				// in root flow
				priorNodeId = "";
			}
		}
		else 
		{
			priorNodeId = "";
		}
		
		if (!StringUtil.isEmpty(postNodeId)) {
			if (!StringUtil.isEmpty(priorNodeId)) {
				nodeStackBuilder.append(priorNodeId);
			}
			// only allowed level-1 depth
			nodeStackBuilder.append(postNodeId);
		}
		
		*/
		if(StringUtil.isNotEmpty(priorNodeId))
		{
			nodeStackBuilder.append(priorNodeId + "." + postNodeId);
		}
		else
		{
			nodeStackBuilder.append(postNodeId);
		}
		
		// set trace info
		lotUdfs.put("BEFOREFLOWNAME", processFlowName);
		lotUdfs.put("BEFOREOPERATIONNAME", processOperationName);

		changeSpecInfo.setProcessFlowName("");
		changeSpecInfo.setProcessFlowVersion("");
		changeSpecInfo.setProcessOperationName("");
		changeSpecInfo.setProcessOperationVersion("");
		changeSpecInfo.setNodeStack(nodeStackBuilder.toString());

		changeSpecInfo.setUdfs(lotUdfs);
		
		return changeSpecInfo;
	}
	
	
	/**
	 * if target not specified, system would find next automatically on same
	 * flow, but return could be not avail otherwise, once target is specified,
	 * that is better to set return than not
	 * 
	 * @author hsryu
	 * @since 2018-06-11
	 * @param lotName
	 * @param productionType
	 * @param productSpecName
	 * @param productSpecVersion
	 * @param productSpec2Name
	 * @param productSpec2Version
	 * @param productRequestName
	 * @param subProductUnitQuantity1
	 * @param subProductUnitQuantity2
	 * @param dueDate
	 * @param priority
	 * @param factoryName
	 * @param areaName
	 * @param lotState
	 * @param lotProcessState
	 * @param lotHoldState
	 * @param processFlowName
	 * @param processFlowVersion
	 * @param processOperationName
	 * @param processOperationVersion
	 * @param targetFlowName
	 * @param targetOperationName
	 * @param returnFlowName
	 * @param returnOperationName
	 * @param lotUdfs
	 * @param productUdfs
	 * @param moveFlag
	 * @return
	 * @throws CustomException
	 */
	public ChangeSpecInfo changeSpecInfoForSampling(String lotName, String productionType,
			String productSpecName, String productSpecVersion,
			String productSpec2Name, String productSpec2Version,
			String productRequestName, double subProductUnitQuantity1,
			double subProductUnitQuantity2, Timestamp dueDate, long priority,
			String factoryName, String areaName, String lotState,
			String lotProcessState, String lotHoldState,
			String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion,
			String targetFlowName, String targetOperationName,
			String returnFlowName, String returnOperationName,
			String currentNodeStack, Map<String, String> lotUdfs,
			List<ProductU> productUdfs, boolean moveFlag, boolean sampleFlag)
			throws CustomException {
		if (lotUdfs == null)
			lotUdfs = new HashMap<String, String>();
		
		// prepare transition info
		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo(productionType,
				productSpecName, productSpecVersion, productSpec2Name,
				productSpec2Version, productRequestName,
				subProductUnitQuantity1, subProductUnitQuantity2, dueDate,
				priority, factoryName, areaName, lotState, lotProcessState,
				lotHoldState, "", "", "", "", "", productUdfs);
		
		StringBuilder nodeStackBuilder = new StringBuilder();
		
		String postNodeId;
		try {
			if (!StringUtil.isEmpty(targetFlowName)
					&& !StringUtil.isEmpty(targetOperationName)) {
				Node nextNode = ProcessFlowServiceProxy
						.getNodeService()
						.getNode(factoryName, targetFlowName, "00001",
								"ProcessOperation", targetOperationName, "");
				postNodeId = nextNode.getKey().getNodeId();
				
			} else {
				// toward next on identical flow
				// no need of return address
				postNodeId = "";
			}
		} catch (Exception ex) 
		{
			throw new CustomException("SPEC-1001", factoryName,
					targetFlowName, targetOperationName);
		}
		
		String priorNodeId = "";
		
		if (!StringUtil.isEmpty(currentNodeStack) && currentNodeStack.contains(".")) 
		{
			String[] arrayNodeStack = StringUtil.split(
					currentNodeStack, ".");
			// tail catch
			if (arrayNodeStack.length > 1) 
			{
				if(sampleFlag)
				{
					// departed from other flow
					for(int i=0; i<arrayNodeStack.length;i++)
					{
						priorNodeId += arrayNodeStack[i]+".";
					}
					//priorNodeId = arrayNodeStack[arrayNodeStack.length - 2];

				}
				else
				{
					// departed from other flow
					for(int i=0; i<arrayNodeStack.length-2;i++)
					{
						priorNodeId += arrayNodeStack[i]+".";
					}
					//priorNodeId = arrayNodeStack[arrayNodeStack.length - 2];

				}
			} 
			else 
			{
				// in root flow
				priorNodeId = "";
			}
		}
		else 
		{
			priorNodeId = currentNodeStack + ".";
		}
		
		if (!StringUtil.isEmpty(postNodeId)) {
			if (!StringUtil.isEmpty(priorNodeId)) {
				nodeStackBuilder.append(priorNodeId);
			}
			// only allowed level-1 depth
			nodeStackBuilder.append(postNodeId);
		}
		
		// set trace info
		lotUdfs.put("BEFOREFLOWNAME", processFlowName);
		lotUdfs.put("BEFOREOPERATIONNAME", processOperationName);

		changeSpecInfo.setProcessFlowName("");
		changeSpecInfo.setProcessFlowVersion("");
		changeSpecInfo.setProcessOperationName("");
		changeSpecInfo.setProcessOperationVersion("");
		changeSpecInfo.setNodeStack(nodeStackBuilder.toString());

		changeSpecInfo.setUdfs(lotUdfs);
		
		return changeSpecInfo;
	}
	
	
	/**
	 * @author aim
	 * @since 2016-12-12
	 * @param
	 * @return
	 */
	public List<ProductU> setProductUdfsProcessFlag(
			List<ProductU> productUList, String processFlag) {
		List<ProductU> newProductUList = new ArrayList<ProductU>();

		try {
			for (ProductU productU : productUList) {
				Map<String, String> productsUdfs = new HashMap<String, String>();
				productsUdfs.put("PROCESSFLAG", processFlag);
				productU.setUdfs(productsUdfs);
				newProductUList.add(productU);
			}
		} catch (NotFoundSignal ne) {

		}
		return newProductUList;
	}
	
	public Lot getLotValidateData(String carrierName) throws CustomException
    {
        String condition = "WHERE carrierName = ?";

        Object[] bindSet = new Object[] {carrierName};

        List<Lot> lotList;

        try
        {
            lotList = LotServiceProxy.getLotService().select(condition, bindSet);

            //check MultiLot
            if(lotList.size() != 1)
            {
                throw new CustomException("LOT-0222", "");
            }
        }
        catch(NotFoundSignal ne)
        {
            //empty CST
            lotList = new ArrayList<Lot>();

            return null;
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("SYS-9999", "Lot", "Lot binding CST incorrect");
        }

        return lotList.get(0);
    }
    
	public void rearrangeSorterPort(String Jobname, String machineName, EventInfo eventInfo)  throws CustomException
	{
		try 
		{
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			String machineStateName = machineData.getMachineStateName();
			String machineCommunicationState = machineData.getCommunicationState();
			
			if(!machineStateName.equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_DOWN) && !machineCommunicationState.equals(GenericServiceProxy.getConstantMap().Mac_OffLine))
			{
				try 
				{
					// change seq
					SortJob sortJob = new SortJob("");
					
					String sql = "SELECT JOBNAME, SEQ FROM CT_SORTJOB WHERE MACHINENAME = :MACHINENAME AND JOBSTATE = :JOBSTATE ORDER BY SEQ ASC";
					
					Map<String, String> tbindMap = new HashMap<String, String>();
					tbindMap.put("MACHINENAME", machineName);
					tbindMap.put("JOBSTATE", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_WAIT);
					
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, tbindMap);
					if( sqlResult.size() > 0 )
					{	
						for( int i=0; i < sqlResult.size(); i++)
						{
							sortJob = ExtendedObjectProxy.getSortJobService().selectByKey(false,new Object[] {sqlResult.get(i).get("jobname").toString()});
							sortJob.setJobState(GenericServiceProxy.getConstantMap().SORT_JOBSTATE_WAIT);
							sortJob.setSeq(i+1);
							sortJob.setEventTime(eventInfo.getEventTime());
							sortJob.setEventName(eventInfo.getEventName());
							sortJob.setEventUser(eventInfo.getEventUser());
							sortJob.setEventComment(eventInfo.getEventComment());
							ExtendedObjectProxy.getSortJobService().update(sortJob);
						}
					}

					// change port
					String Query = "SELECT PT.PORTNAME";
					Query += " FROM PORT P, PORTSPEC PT";
					Query += " WHERE PT.MACHINENAME = P.MACHINENAME";
					Query += " AND P.PORTNAME = PT.PORTNAME";
					Query += " AND P.FACTORYNAME = PT.FACTORYNAME";
					Query += " AND PT.MACHINENAME = :MACHINENAME";	
					Query += " AND PT.PORTNAME NOT IN (SELECT CC.PORTNAME FROM CT_SORTJOB C JOIN CT_SORTJOBCARRIER CC ON C.JOBNAME = CC.JOBNAME WHERE CC.MACHINENAME = :MACHINENAME AND C.JOBSTATE IN (:JOBSTATE1, :JOBSTATE2, :JOBSTATE3))";
					Query += " AND P.PORTTYPE = :PORTTYPE";
					Query += " AND P.PORTSTATENAME != :PORTSTATENAME";
					
					// Added by smkang on 2019.02.25 - Need to check PortTransferState and PortInInhibitFlag.
					Query += " AND P.TRANSFERSTATE = :TRANSFERSTATE";
					Query += " AND (P.PORTININHIBITFLAG IS NULL OR P.PORTININHIBITFLAG = :PORTININHIBITFLAG)";
					
					Query += " ORDER BY PT.PORTNAME";
					
					Map<String, String> bindMap = new HashMap<String, String>();
					bindMap.put("MACHINENAME", machineName);
					bindMap.put("JOBSTATE1", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_WAIT);
					bindMap.put("JOBSTATE2", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_STARTED);
					bindMap.put("JOBSTATE3", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CONFIRMED);
					bindMap.put("PORTTYPE", "PS");
					bindMap.put("PORTSTATENAME", GenericServiceProxy.getConstantMap().Port_DOWN);
					
					// Added by smkang on 2019.02.25 - Need to check PortTransferState and PortInInhibitFlag.
					bindMap.put("TRANSFERSTATE", GenericServiceProxy.getConstantMap().Port_ReadyToLoad);
					bindMap.put("PORTININHIBITFLAG", "N");
					
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> NEWPortResult = GenericServiceProxy.getSqlMesTemplate().queryForList(Query, bindMap);
					int portCountleft = NEWPortResult.size(); // the count of port to re-arrange
					int portnumber = 0;
					
					for(int kk=0; kk < portCountleft; kk++)
					{
						Query = "SELECT A.JOBNAME, A.JOBSTATE, A.JOBTYPE , A.SEQ, B.CARRIERNAME FROM CT_SORTJOB A JOIN CT_SORTJOBCARRIER B ON A.JOBNAME = B.JOBNAME WHERE A.MACHINENAME = :MACHINENAME AND B.PORTNAME = :PORTNAME AND A.JOBSTATE = :JOBSTATE ORDER BY A.SEQ ASC";

						bindMap.put("MACHINENAME", machineName);
						bindMap.put("PORTNAME", "Auto");
						bindMap.put("JOBSTATE", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_WAIT);
						
						@SuppressWarnings("unchecked")
						List<Map<String, Object>> NEWJobResult = GenericServiceProxy.getSqlMesTemplate().queryForList(Query, bindMap);
						
						
						if(NEWJobResult.size() < 1)
						{
							break; // the end
						}

						//2019.08.27 dmlee : Request By CIM Mantis 4654
						boolean assignFlag = false;
						try
						{

							String temp_JobtypeTemp = NEWJobResult.get(0).get("jobtype").toString();
							
							if (StringUtil.equals(temp_JobtypeTemp.toUpperCase(), "CHANGE")) 
							{
								String Ccondition = "where jobname=?";
								Object[] CbindSet = new Object[] {NEWJobResult.get(0).get("jobname").toString()};
								List<SortJobCarrier> TempSortJobCarrier = ExtendedObjectProxy.getSortJobCarrierService().select(Ccondition,CbindSet);
								int needPortCount = TempSortJobCarrier.size();
								
								if( needPortCount > portCountleft)
								{  // the count of CST left > the count of port left 
									break;
								}
								else
								{
									assignFlag = true;
								}

							}
							else
							{
								assignFlag = true;		
							}
						}
						catch(Exception ex)
						{
							log.error("Find Next Job Fail !");
						}
						log.info("Find Job["+NEWJobResult.get(0).get("jobname").toString()+"]");
						//2019.08.27 dmlee : Request By CIM Mantis 4654
						
						if(assignFlag)
						{
							String temp_Jobtype = NEWJobResult.get(0).get("jobtype").toString();

							
							if (StringUtil.equals(temp_Jobtype.toUpperCase(), "CHANGE")) 
							{
								// it needs more than one port.
								String Ccondition = "where jobname=?";
								Object[] CbindSet = new Object[] {NEWJobResult.get(0).get("jobname").toString()};
								List<SortJobCarrier> TempSortJobCarrier = ExtendedObjectProxy.getSortJobCarrierService().select(Ccondition,CbindSet);
								
								log.info("CST left Count["+TempSortJobCarrier.size()+"] / Port left Count["+portCountleft+"]");
								if( false )
								{  // the count of CST left > the count of port left 
									break;
								}
								else
								{
									// port re-arrange
									for (SortJobCarrier Carrier : TempSortJobCarrier) {
										String transferdiraction = Carrier.getTransferDirection(); // Target or Source
										String carriername = Carrier.getCarrierName();
										Carrier.setPortName(NEWPortResult.get(portnumber).get("portname").toString());
										ExtendedObjectProxy.getSortJobCarrierService().modify(eventInfo, Carrier);
										if(StringUtil.equals(transferdiraction, "TARGET"))
										{
											eventInfo.setCheckTimekeyValidation(false);
											eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
											eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
											// Update port in the 'TOCARRIERNAME';
											String condition = "where jobname=?" + " and tocarriername = ?";
											Object[] bindSet = new Object[] {NEWJobResult.get(0).get("jobname").toString(), carriername };
											List<SortJobProduct> SortJobProductList = ExtendedObjectProxy.getSortJobProductService().select(condition,bindSet);
											for (SortJobProduct product : SortJobProductList) {
												product.setToPortName(NEWPortResult.get(portnumber).get("portname").toString());
												ExtendedObjectProxy.getSortJobProductService().modify(eventInfo, product);
											}
										}
										else {
											eventInfo.setCheckTimekeyValidation(false);
											eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
											eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
											// Update port in the 'FROMCARRIERNAME';
											String condition = "where jobname=?" + " and fromcarriername = ?";
											Object[] bindSet = new Object[] {NEWJobResult.get(0).get("jobname").toString(), carriername };
											List<SortJobProduct> SortJobProductList = ExtendedObjectProxy.getSortJobProductService().select(condition,bindSet);
											for (SortJobProduct product : SortJobProductList) {
												product.setFromPortName(NEWPortResult.get(portnumber).get("portname").toString());
												ExtendedObjectProxy.getSortJobProductService().modify(eventInfo, product);
											}
										}
										portnumber++;
										portCountleft--; // - arrange port
									}
								}
								
							}
							else {
								// it needs one port
								if( false )
								{
									break;
								}
								else {
									// port re-arrange
									String NewPort = NEWPortResult.get(portnumber).get("portname").toString();
									String Ccondition = "where jobname=?";
									Object[] CbindSet = new Object[] {NEWJobResult.get(0).get("jobname").toString()};
									List<SortJobCarrier> TempSortJobCarrier = ExtendedObjectProxy.getSortJobCarrierService().select(Ccondition,CbindSet);
									for (SortJobCarrier Carrier : TempSortJobCarrier) {
										Carrier.setPortName(NewPort);
										ExtendedObjectProxy.getSortJobCarrierService().modify(eventInfo, Carrier);
									}
									String condition = "where jobname=?";
									Object[] bindSet = new Object[] {NEWJobResult.get(0).get("jobname").toString()};
									List<SortJobProduct> SortJobProductList = ExtendedObjectProxy.getSortJobProductService().select(condition,bindSet);

									for (SortJobProduct product : SortJobProductList) {
										product.setToPortName(NewPort);product.setFromPortName(NewPort);
										ExtendedObjectProxy.getSortJobProductService().modify(eventInfo, product);
									}		
									portnumber++;
									portCountleft--;  // - arrange port
								}
							}
						}
						
					}
				}  
				catch (Exception ex) 
				{
				    log.info(ex.getStackTrace());
				}
			}
		}
		catch (Exception ex) 
		{
		    log.info(ex.getStackTrace());
		}
	}
	
	/**
	 * @author JeongSu
	 * @since 2019-06-18
	 * Get MQC Job OperationList
	 */
	public List<Map<String, Object>> getMQCJobOperationList(String MQCJobName,String factoryName,String processFlowName) throws CustomException{
    	List<Map<String, Object>> MQCOperationList = null;
    	
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PROCESSFLOWNAME", processFlowName);
		bindMap.put("MQCJOBNAME", MQCJobName);
		
		StringBuilder MQCSql= new StringBuilder();
    	MQCSql.append("SELECT *  ");
    	MQCSql.append("  FROM CT_MQCJOBOPER  ");
    	MQCSql.append("       LEFT JOIN  ");
    	MQCSql.append("       (    SELECT LEVEL LV,  ");
    	MQCSql.append("                   FACTORYNAME,  ");
    	MQCSql.append("                   PROCESSOPERATIONNAME,  ");
    	MQCSql.append("                   PROCESSFLOWNAME,  ");
    	MQCSql.append("                   PROCESSFLOWVERSION,  ");
    	MQCSql.append("                   NODEID  ");
    	MQCSql.append("              FROM (SELECT N.FACTORYNAME,  ");
    	MQCSql.append("                           N.NODEATTRIBUTE1 PROCESSOPERATIONNAME,  ");
    	MQCSql.append("                           N.PROCESSFLOWNAME,  ");
    	MQCSql.append("                           N.PROCESSFLOWVERSION,  ");
    	MQCSql.append("                           N.NODEID,  ");
    	MQCSql.append("                           N.NODETYPE,  ");
    	MQCSql.append("                           A.FROMNODEID,  ");
    	MQCSql.append("                           A.TONODEID  ");
    	MQCSql.append("                      FROM ARC A, NODE N, PROCESSFLOW PF  ");
    	MQCSql.append("                     WHERE     1 = 1  ");
    	MQCSql.append("                           AND 1 = 1  ");
    	MQCSql.append("                           AND PF.PROCESSFLOWNAME = :PROCESSFLOWNAME  ");
    	MQCSql.append("                           AND N.FACTORYNAME = :FACTORYNAME  ");
    	MQCSql.append("                           AND N.PROCESSFLOWNAME = PF.PROCESSFLOWNAME  ");
    	MQCSql.append("                           AND N.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION  ");
    	MQCSql.append("                           AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME  ");
    	MQCSql.append("                           AND N.FACTORYNAME = PF.FACTORYNAME  ");
    	MQCSql.append("                           AND A.FROMNODEID = N.NODEID)  ");
    	MQCSql.append("        START WITH NODETYPE = 'Start'  ");
    	MQCSql.append("        CONNECT BY NOCYCLE FROMNODEID = PRIOR TONODEID) OPER  ");
    	MQCSql.append("          ON CT_MQCJOBOPER.PROCESSOPERATIONNAME = OPER.PROCESSOPERATIONNAME  ");
    	MQCSql.append(" WHERE CT_MQCJOBOPER.MQCJOBNAME = :MQCJOBNAME  ");
    	
    	try {
    		// JDBC를 통해 값 호출
    		MQCOperationList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(MQCSql.toString(), bindMap);
		} catch (Exception e) {
			// MQCJob Operation Select Error
			log.info("MQCJobOper Select Error");
			throw new CustomException("COMMON-0001", "MQC JOB OPER SELECT ERROR");
		}
    	
		return MQCOperationList;
	}
	
	/**
	 * @author JeongSu
	 * @since 2019-06-18
	 * Get ProcessFlow OperationList
	 */
	
	public List<Map<String, Object>> getProcessOperationList(String factoryName,String processFlowName) throws CustomException{
		List<Map<String, Object>> operationList =null;
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PROCESSFLOWNAME", processFlowName);
		
		StringBuilder sql = new StringBuilder();
    	sql.append("	SELECT  ");
    	sql.append("	    QL.LV,  ");
    	sql.append("	    QL.FACTORYNAME,   ");
    	sql.append("	    QL.PROCESSOPERATIONNAME,   ");
    	sql.append("	    QL.PROCESSFLOWNAME,   ");
    	sql.append("	    QL.NODEID   ");
    	sql.append("	    FROM PROCESSOPERATIONSPEC PO,   ");
    	sql.append("	         ( SELECT LEVEL LV,   ");
    	sql.append("	                  FACTORYNAME,   ");
    	sql.append("	                  PROCESSOPERATIONNAME,   ");
    	sql.append("	                  PROCESSFLOWNAME,   ");
    	sql.append("	                  PROCESSFLOWVERSION,   ");
    	sql.append("	                  NODEID   ");
    	sql.append("	         FROM (   SELECT    ");
    	sql.append("	                     N.FACTORYNAME,   ");
    	sql.append("	                     N.NODEATTRIBUTE1 PROCESSOPERATIONNAME,   ");
    	sql.append("	                     N.PROCESSFLOWNAME,   ");
    	sql.append("	                     N.PROCESSFLOWVERSION,   ");
    	sql.append("	                     N.NODEID,   ");
    	sql.append("	                     N.NODETYPE,   ");
    	sql.append("	                     A.FROMNODEID,   ");
    	sql.append("	                     A.TONODEID   ");
    	sql.append("	                FROM ARC A,   ");
    	sql.append("	                     NODE N,   ");
    	sql.append("	                     PROCESSFLOW PF   ");
    	sql.append("	               WHERE 1 = 1   ");
    	sql.append("	                 AND 1=1  ");
    	sql.append("	                 AND PF.PROCESSFLOWNAME = :PROCESSFLOWNAME   ");
    	sql.append("	                 AND N.FACTORYNAME = :FACTORYNAME   ");
    	sql.append("	                 AND N.PROCESSFLOWNAME = PF.PROCESSFLOWNAME   ");
    	sql.append("	                 AND N.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION   ");
    	sql.append("	                 AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME   ");
    	sql.append("	                 AND N.FACTORYNAME = PF.FACTORYNAME   ");
    	sql.append("	                 AND A.FROMNODEID = N.NODEID)   ");
    	sql.append("	          START WITH NODETYPE = 'Start'   ");
    	sql.append("	          CONNECT BY NOCYCLE FROMNODEID = PRIOR TONODEID) QL   ");
    	sql.append("	   WHERE 1 = 1   ");
    	sql.append("	     AND PO.PROCESSOPERATIONNAME = QL.PROCESSOPERATIONNAME   ");
    	sql.append("	     AND PO.FACTORYNAME = :FACTORYNAME   ");
    	sql.append("	ORDER BY QL.LV  ");

    	try {
    		// JDBC를 통해 값 호출
    		operationList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), bindMap);
		} catch (Exception e) {
			// OperationList Select Error
			log.info("OperationList Select Error");
			throw new CustomException("COMMON-0001", "OperationList Select Error");
		}

		return operationList;
	}
	
	 /*
	* Name : nextMandatoryInfo
	* Desc : This function is Get Next Mandatory Info
	* Author : AIM Systems, Inc
	* Date : 2019.08.23
	*/
	 public static Node nextMandatoryInfo(String factoryName, String processOperationName, String processFlowName, String curSequenceId,
			 String processOperationVersion, String processFlowVersion)
	 {
		 Node pfseq = null;
		 
		 Map<String,Object> bindMap = new HashMap<String,Object>();
			
		 bindMap.put("FACTORYNAME", factoryName);
		 bindMap.put("PROCESSFLOWNAME", processFlowName);
		 bindMap.put("PROCESSFLOWVERSION", processFlowVersion);
		 bindMap.put("PROCESSOPERATIONVERSION", processOperationVersion);
		 bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		 			 
		 StringBuilder sqlPos = new StringBuilder();
		 sqlPos.append("SELECT POSITION ");
		 sqlPos.append("  FROM V_PROCESSFLOWSEQ ");
		 sqlPos.append(" WHERE     FACTORYNAME = :FACTORYNAME ");
		 sqlPos.append("       AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		 sqlPos.append("       AND PROCESSFLOWNAME = :PROCESSFLOWNAME");
		 sqlPos.append("       AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION");
		 List<Map<String, Object>> sqlPosResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sqlPos.toString(), bindMap);
		 
		 bindMap.put("POSITION", sqlPosResult.get(0).get("POSITION").toString()); 
		 
		 StringBuilder sql = new StringBuilder();
		 sql.append("SELECT PFS.NODEID ");
		 sql.append("  FROM PROCESSOPERATIONSPEC PO, ");
		 sql.append("       (SELECT * ");
		 sql.append("          FROM V_PROCESSFLOWSEQ ");
		 sql.append("         WHERE FACTORYNAME = :FACTORYNAME ");
		 sql.append("           AND PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		 sql.append("           AND POSITION > :POSITION) PFS ");
		 sql.append(" WHERE PO.FACTORYNAME = PFS.FACTORYNAME ");
		 sql.append("   AND PO.PROCESSOPERATIONNAME = PFS.PROCESSOPERATIONNAME ");
		// sql.append("   AND PO.MANDATORYOPERATIONFLAG = 'Y' ");
		 sql.append(" ORDER BY POSITION ");
	 	
 		 

		 List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), bindMap);
		
		 if(sqlResult.size() > 0)
		 {
			 return ProcessFlowServiceProxy.getNodeService().getNode(sqlResult.get(0).get("NODEID").toString());
		 }
		 else
		 {
			 log.info("Can not Find the Next Mandatory Operation. Current Operation: " + processOperationName);
			 return null;
		 }
	 }
 
}