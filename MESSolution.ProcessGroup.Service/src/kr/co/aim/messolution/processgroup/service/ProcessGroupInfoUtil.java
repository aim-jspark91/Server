package kr.co.aim.messolution.processgroup.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.NodeStack;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.info.AssignMaterialsInfo;
import kr.co.aim.greentrack.processgroup.management.info.AssignSuperProcessGroupInfo;
import kr.co.aim.greentrack.processgroup.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.processgroup.management.info.CreateInfo;
import kr.co.aim.greentrack.processgroup.management.info.DeassignMaterialsInfo;
import kr.co.aim.greentrack.processgroup.management.info.DeassignSuperProcessGroupInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeCompletedInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeIdleInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeInReworkInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeLoggedInInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeLoggedOutInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeNotOnHoldInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeProcessingInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeReceivedInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeShippedInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeTravelingInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeWaitingToLoginInfo;
import kr.co.aim.greentrack.processgroup.management.info.SetAreaInfo;
import kr.co.aim.greentrack.processgroup.management.info.SetEventInfo;
import kr.co.aim.greentrack.processgroup.management.info.UndoInfo;
import kr.co.aim.greentrack.processgroup.management.info.ext.MaterialU;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * @author gksong
 * @date   2009.02.27
 */

public class ProcessGroupInfoUtil implements ApplicationContextAware
{
	/**
	 * @uml.property  name="applicationContext"
	 * @uml.associationEnd  
	 */
	private ApplicationContext     	applicationContext;
	private static Log				log = LogFactory.getLog("ProcessGroupServiceImpl");
	
	/**
	 * @param arg0
	 * @throws BeansException
	 * @uml.property  name="applicationContext"
	 */
	public void setApplicationContext(ApplicationContext arg0)
	throws BeansException
	{
		applicationContext = arg0;
	}
	
	/*
	* Name : assignMaterialsInfo
	* Desc : This function is assignMaterialsInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.11
	*/
	public AssignMaterialsInfo assignMaterialsInfo( ProcessGroup processGroupData, long materialQuantity,
														   List<MaterialU> materialUSequence )
	{
		AssignMaterialsInfo assignMaterialsInfo = new AssignMaterialsInfo();
		assignMaterialsInfo.setMaterialQuantity(materialQuantity);
		assignMaterialsInfo.setMaterialUSequence(materialUSequence);
		
		Map<String,String> processGroupUdfs = processGroupData.getUdfs();
		assignMaterialsInfo.setUdfs(processGroupUdfs);
		
		return assignMaterialsInfo;
	}

	/*
	* Name : assignSuperProcessGroupInfo
	* Desc : This function is assignSuperProcessGroupInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.11
	*/
	public AssignSuperProcessGroupInfo assignSuperProcessGroupInfo( ProcessGroup processGroupData, String superProcessGroupName )
	{
		AssignSuperProcessGroupInfo assignSuperProcessGroupInfo = new AssignSuperProcessGroupInfo();
		assignSuperProcessGroupInfo.setSuperProcessGroupName(superProcessGroupName);
		
		Map<String,String> processGroupUdfs = processGroupData.getUdfs();
		assignSuperProcessGroupInfo.setUdfs(processGroupUdfs);

		return assignSuperProcessGroupInfo;
	}

	/*
	* Name : changeSpecInfo
	* Desc : This function is changeSpecInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.11
	*/
	public ChangeSpecInfo changeSpecInfo( ProcessGroup processGroupData, String areaName, 
												 Timestamp dueDate, String factoryName, Map<String, String> materialUdfs,
												 String nodeStack, long priority, String processFlowName, String processFlowVersion,
												 String processOperationName, String processOperationVersion, String productionType,
												 String productRequestName, String productSpec2Name, String productSpec2Version, 
												 String productSpecName, String productSpecVersion, Map<String, String> subMaterialUdfs,
												 long subProductUnitQuantity1, long subProductUnitQuantity2  )
	{
		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();
		changeSpecInfo.setAreaName(areaName);
		changeSpecInfo.setDueDate(dueDate);
		changeSpecInfo.setFactoryName(factoryName);
		changeSpecInfo.setMaterialUdfs(materialUdfs);
		changeSpecInfo.setNodeStack(nodeStack);
		changeSpecInfo.setPriority(priority);
		changeSpecInfo.setProcessFlowName(processFlowName);
		changeSpecInfo.setProcessFlowVersion(processFlowVersion);
		changeSpecInfo.setProcessOperationName(processOperationName);
		changeSpecInfo.setProcessOperationVersion(processOperationVersion);
		changeSpecInfo.setProductionType(productionType);
		changeSpecInfo.setProductRequestName(productRequestName);
		changeSpecInfo.setProductSpec2Name(productSpec2Name);
		changeSpecInfo.setProductSpec2Version(productSpec2Version);
		changeSpecInfo.setProductSpecName(productSpecName);
		changeSpecInfo.setProductSpecVersion(productSpecVersion);
		changeSpecInfo.setSubMaterialUdfs(subMaterialUdfs);
		changeSpecInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
		changeSpecInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);
				
		Map<String,String> processGroupUdfs = processGroupData.getUdfs();
		changeSpecInfo.setUdfs(processGroupUdfs);

		return changeSpecInfo;
	}

	/*
	* Name : createInfo
	* Desc : This function is createInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.11
	*/
	public CreateInfo createInfo( ProcessGroup processGroupData, String detailMaterialType, List<String> materialNames,
										 long materialQuantity, String materialType, Map<String, String> subMaterialUdfs,
										 String processGroupName, String processGroupType  )
	{
		CreateInfo createInfo = new CreateInfo();
		createInfo.setDetailMaterialType(detailMaterialType);
		createInfo.setMaterialNames(materialNames);
		createInfo.setMaterialQuantity(materialQuantity);
		createInfo.setMaterialType(materialType);
		createInfo.setMaterialUdfs(subMaterialUdfs);
		createInfo.setProcessGroupName(processGroupName);
		createInfo.setProcessGroupType(processGroupType);
				
		Map<String,String> processGroupUdfs = processGroupData.getUdfs();
		createInfo.setUdfs(processGroupUdfs);
	
		return createInfo;
	}

	/*
	* Name : deassignMaterialsInfo
	* Desc : This function is deassignMaterialsInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.11
	*/
	public DeassignMaterialsInfo deassignMaterialsInfo( ProcessGroup processGroupData, long materialQuantity, List<MaterialU> materialUSequence  )
	{
		DeassignMaterialsInfo deassignMaterialsInfo = new DeassignMaterialsInfo();
		deassignMaterialsInfo.setMaterialQuantity(materialQuantity);
		deassignMaterialsInfo.setMaterialUSequence(materialUSequence);
				
		Map<String,String> processGroupUdfs = processGroupData.getUdfs();
		deassignMaterialsInfo.setUdfs(processGroupUdfs);
		
		return deassignMaterialsInfo;
	}

	/*
	* Name : deassignSuperProcessGroupInfo
	* Desc : This function is deassignSuperProcessGroupInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.11
	*/
	public DeassignSuperProcessGroupInfo deassignSuperProcessGroupInfo( ProcessGroup processGroupData, long materialQuantity, List<MaterialU> materialUSequence  )
	{
		DeassignSuperProcessGroupInfo deassignSuperProcessGroupInfo = new DeassignSuperProcessGroupInfo();
	
		Map<String,String> processGroupUdfs = processGroupData.getUdfs();
		deassignSuperProcessGroupInfo.setUdfs(processGroupUdfs);
		
		return deassignSuperProcessGroupInfo;
	}

	/*
	* Name : makeCompletedInfo
	* Desc : This function is makeCompletedInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.11
	*/
	public MakeCompletedInfo makeCompletedInfo( ProcessGroup processGroupData, Map<String, String> materialUdfs,
													   Map<String, String> subMaterialUdfs)
	{
		MakeCompletedInfo makeCompletedInfo = new MakeCompletedInfo();
		makeCompletedInfo.setMaterialUdfs(materialUdfs);
		makeCompletedInfo.setSubMaterialUdfs(subMaterialUdfs);
	
		Map<String,String> processGroupUdfs = processGroupData.getUdfs();
		makeCompletedInfo.setUdfs(processGroupUdfs);
		
		return makeCompletedInfo;
	}

	/*
	* Name : makeIdleInfo
	* Desc : This function is makeIdleInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.11
	*/
	public MakeIdleInfo makeIdleInfo( ProcessGroup processGroupData, String areaName, String completeFlag, 
											 String machineName, String machineRecipeName, Map<String, String> materialUdfs, 
											 String nodeStack, String processFlowName, String processFlowVersion, 
											 String processOperationName, String processOperationVersion, String reworkFlag, 
											 String reworkNodeId, Map<String, String> subMaterialUdfs )
	{
		MakeIdleInfo makeIdleInfo = new MakeIdleInfo();
		makeIdleInfo.setAreaName(areaName);
		makeIdleInfo.setCompleteFlag(completeFlag);
		makeIdleInfo.setMachineName(machineName);
		makeIdleInfo.setMachineRecipeName(machineRecipeName);
		makeIdleInfo.setMaterialUdfs(materialUdfs);
		makeIdleInfo.setNodeStack(nodeStack);
		makeIdleInfo.setProcessFlowName(processFlowName);
		makeIdleInfo.setProcessFlowVersion(processFlowVersion);
		makeIdleInfo.setProcessOperationName(processOperationName);
		makeIdleInfo.setProcessOperationVersion(processOperationVersion);
		makeIdleInfo.setReworkFlag(reworkFlag);
		makeIdleInfo.setReworkNodeId(reworkNodeId);
		makeIdleInfo.setSubMaterialUdfs(subMaterialUdfs);
					
		Map<String,String> processGroupUdfs = processGroupData.getUdfs();
		makeIdleInfo.setUdfs(processGroupUdfs);
		
		return makeIdleInfo;
	}

	/*
	* Name : makeInReworkInfo
	* Desc : This function is makeInReworkInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.11
	*/
	public MakeInReworkInfo makeInReworkInfo( ProcessGroup processGroupData, String areaName, Map<String, String> materialUdfs, 
													 String nodeStack, String processFlowName, String processFlowVersion, 
													 String processOperationName, String reworkNodeId, Map<String, String> subMaterialUdfs )
	{
		MakeInReworkInfo makeInReworkInfo = new MakeInReworkInfo();
		makeInReworkInfo.setAreaName(areaName);
		makeInReworkInfo.setMaterialUdfs(materialUdfs);
		makeInReworkInfo.setNodeStack(nodeStack);
		makeInReworkInfo.setProcessFlowName(processFlowName);
		makeInReworkInfo.setProcessFlowVersion(processFlowVersion);
		makeInReworkInfo.setProcessOperationName(processOperationName);
		makeInReworkInfo.setReworkNodeId(reworkNodeId);
		makeInReworkInfo.setSubMaterialUdfs(subMaterialUdfs);
		Map<String,String> processGroupUdfs = processGroupData.getUdfs();
		makeInReworkInfo.setUdfs(processGroupUdfs);
		
		return makeInReworkInfo;
	}

	/*
	* Name : makeLoggedInInfo
	* Desc : This function is makeLoggedInInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.11
	*/
	public MakeLoggedInInfo makeLoggedInInfo( ProcessGroup processGroupData, String areaName, String machineName, 
													 String machineRecipeName, Map<String, String> materialUdfs, Map<String, String> subMaterialUdfs)
	{
		MakeLoggedInInfo makeLoggedInInfo = new MakeLoggedInInfo();
		makeLoggedInInfo.setAreaName(areaName);
		makeLoggedInInfo.setMachineName(machineName);
		makeLoggedInInfo.setMachineRecipeName(machineRecipeName);
		makeLoggedInInfo.setMaterialUdfs(materialUdfs);
		makeLoggedInInfo.setSubMaterialUdfs(subMaterialUdfs);
	
		Map<String,String> processGroupUdfs = processGroupData.getUdfs();
		makeLoggedInInfo.setUdfs(processGroupUdfs);
		
		return makeLoggedInInfo;
	}

	/*
	* Name : makeLoggedOutInfo
	* Desc : This function is makeLoggedOutInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.11
	*/
	public MakeLoggedOutInfo makeLoggedOutInfo( ProcessGroup processGroupData, String areaName, String completeFlag, 
													   String machineName, String machineRecipeName, Map<String, String> materialUdfs, 
													   String nodeStack, String processFlowName, String processFlowVersion, 
													   String processOperationName, String processOperationVersion, String reworkFlag, 
													   String reworkNodeId, Map<String, String> subMaterialUdfs)
	{
		MakeLoggedOutInfo makeLoggedOutInfo = new MakeLoggedOutInfo();
		makeLoggedOutInfo.setAreaName(areaName);
		makeLoggedOutInfo.setCompleteFlag(completeFlag);
		makeLoggedOutInfo.setMachineName(machineName);
		makeLoggedOutInfo.setMachineRecipeName(machineRecipeName);
		makeLoggedOutInfo.setMaterialUdfs(materialUdfs);
		makeLoggedOutInfo.setNodeStack(nodeStack);
		makeLoggedOutInfo.setProcessFlowName(processFlowName);
		makeLoggedOutInfo.setProcessFlowVersion(processFlowVersion);
		makeLoggedOutInfo.setProcessOperationName(processOperationName);
		makeLoggedOutInfo.setProcessOperationVersion(processOperationVersion);
		makeLoggedOutInfo.setReworkFlag(reworkFlag);
		makeLoggedOutInfo.setReworkNodeId(reworkNodeId);
		makeLoggedOutInfo.setSubMaterialUdfs(subMaterialUdfs);
			
		Map<String,String> processGroupUdfs = processGroupData.getUdfs();
		makeLoggedOutInfo.setUdfs(processGroupUdfs);
		
		return makeLoggedOutInfo;
	}

	/*
	* Name : makeNotOnHoldInfo
	* Desc : This function is makeNotOnHoldInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.11
	*/
	public MakeNotOnHoldInfo makeNotOnHoldInfo( ProcessGroup processGroupData, Map<String, String> materialUdfs,
													   Map<String, String> subMaterialUdfs)
	{
		MakeNotOnHoldInfo makeNotOnHoldInfo = new MakeNotOnHoldInfo();
		makeNotOnHoldInfo.setMaterialUdfs(materialUdfs);
		makeNotOnHoldInfo.setSubMaterialUdfs(subMaterialUdfs);
		
		Map<String,String> processGroupUdfs = processGroupData.getUdfs();
		makeNotOnHoldInfo.setUdfs(processGroupUdfs);
		
		return makeNotOnHoldInfo;
	}

	/*
	* Name : makeOnHoldInfo
	* Desc : This function is makeOnHoldInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.11
	*/
	public MakeOnHoldInfo makeOnHoldInfo( ProcessGroup processGroupData, Map<String, String> materialUdfs, 
												 Map<String, String> subMaterialUdfs)
	{
		MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo();
		makeOnHoldInfo.setMaterialUdfs(materialUdfs);
		makeOnHoldInfo.setSubMaterialUdfs(subMaterialUdfs);

		Map<String,String> processGroupUdfs = processGroupData.getUdfs();
		makeOnHoldInfo.setUdfs(processGroupUdfs);
		
		return makeOnHoldInfo;
	}

	/*
	* Name : makeProcessingInfo
	* Desc : This function is makeProcessingInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.11
	*/
	public MakeProcessingInfo makeProcessingInfo( ProcessGroup processGroupData, String areaName, String machineName,
														 String machineRecipeName, Map<String, String> materialUdfs, Map<String, String> subMaterialUdfs)
	{
		MakeProcessingInfo makeProcessingInfo = new MakeProcessingInfo();
		makeProcessingInfo.setAreaName(areaName);
		makeProcessingInfo.setMachineName(machineName);
		makeProcessingInfo.setMachineRecipeName(machineRecipeName);
		makeProcessingInfo.setMaterialUdfs(materialUdfs);
		makeProcessingInfo.setSubMaterialUdfs(subMaterialUdfs);
		
		Map<String,String> processGroupUdfs = processGroupData.getUdfs();
		makeProcessingInfo.setUdfs(processGroupUdfs);
		
		return makeProcessingInfo;
	}

	/*
	* Name : makeReceivedInfo
	* Desc : This function is makeReceivedInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.11
	*/
	public MakeReceivedInfo makeReceivedInfo( ProcessGroup processGroupData, String areaName, Map<String, String> materialUdfs,
													 String nodeStack, String processFlowName, String processFlowVersion, 
													 String processOperationName, String processOperationVersion, String productionType,
													 String productRequestName, String productSpec2Name, String productSpec2Version, 
													 String productSpecName, String productSpecVersion, String productType, 
													 Map<String, String> subMaterialUdfs, String subProductType)
	{
		MakeReceivedInfo makeReceivedInfo = new MakeReceivedInfo();
		makeReceivedInfo.setAreaName(areaName);
		makeReceivedInfo.setMaterialUdfs(materialUdfs);
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
		makeReceivedInfo.setSubMaterialUdfs(subMaterialUdfs);
		makeReceivedInfo.setSubProductType(subProductType);
		
		Map<String,String> processGroupUdfs = processGroupData.getUdfs();
		makeReceivedInfo.setUdfs(processGroupUdfs);
		
		return makeReceivedInfo;
	}

	/*
	* Name : makeShippedInfo
	* Desc : This function is makeShippedInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.11
	*/
	public MakeShippedInfo makeShippedInfo( ProcessGroup processGroupData, String areaName, String directShipFlag, 
												   String factoryName, Map<String, String> materialUdfs, Map<String, String> subMaterialUdfs )
	{
		MakeShippedInfo makeShippedInfo = new MakeShippedInfo();
		makeShippedInfo.setAreaName(areaName);
		makeShippedInfo.setDirectShipFlag(directShipFlag);
		makeShippedInfo.setFactoryName(factoryName);
		makeShippedInfo.setMaterialUdfs(materialUdfs);
		makeShippedInfo.setSubMaterialUdfs(subMaterialUdfs);
		
		Map<String,String> processGroupUdfs = processGroupData.getUdfs();
		makeShippedInfo.setUdfs(processGroupUdfs);

		return makeShippedInfo;
	}

	/*
	* Name : makeTravelingInfo
	* Desc : This function is makeTravelingInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.11
	*/
	public MakeTravelingInfo makeTravelingInfo( ProcessGroup processGroupData, String areaName, Map<String, String> materialUdfs,
													   Map<String, String> subMaterialUdfs)
	{
		MakeTravelingInfo makeTravelingInfo = new MakeTravelingInfo();
		makeTravelingInfo.setAreaName(areaName);
		makeTravelingInfo.setMaterialUdfs(materialUdfs);
		makeTravelingInfo.setSubMaterialUdfs(subMaterialUdfs);

		Map<String,String> processGroupUdfs = processGroupData.getUdfs();
		makeTravelingInfo.setUdfs(processGroupUdfs);
		
		return makeTravelingInfo;
	}

	/*
	* Name : makeWaitingToLoginInfo
	* Desc : This function is makeWaitingToLoginInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.11
	*/
	public MakeWaitingToLoginInfo makeWaitingToLoginInfo( ProcessGroup processGroupData, String areaName, String machineName, 
																 String machineRecipeName, Map<String, String> materialUdfs, 
																 Map<String, String> subMaterialUdfs )
	{
		MakeWaitingToLoginInfo makeWaitingToLoginInfo = new MakeWaitingToLoginInfo();
		makeWaitingToLoginInfo.setAreaName(areaName);
		makeWaitingToLoginInfo.setMachineName(machineName);
		makeWaitingToLoginInfo.setMachineRecipeName(machineRecipeName);
		makeWaitingToLoginInfo.setMaterialUdfs(materialUdfs);
		makeWaitingToLoginInfo.setSubMaterialUdfs(subMaterialUdfs);
		
		Map<String,String> processGroupUdfs = processGroupData.getUdfs();
		makeWaitingToLoginInfo.setUdfs(processGroupUdfs);
		
		return makeWaitingToLoginInfo;
	}

	/*
	* Name : setAreaInfo
	* Desc : This function is setAreaInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.11
	*/
	public SetAreaInfo setAreaInfo( ProcessGroup processGroupData, String areaName, Map<String, String> materialUdfs,
										   Map<String, String> subMaterialUdfs)
	{
		SetAreaInfo setAreaInfo = new SetAreaInfo();
		setAreaInfo.setAreaName(areaName);
		setAreaInfo.setMaterialUdfs(materialUdfs);
		setAreaInfo.setSubMaterialUdfs(subMaterialUdfs);
		
		Map<String,String> processGroupUdfs = processGroupData.getUdfs();
		setAreaInfo.setUdfs(processGroupUdfs);
	
		return setAreaInfo;
	}

	/*
	* Name : setEventInfo
	* Desc : This function is setEventInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.11
	*/
	public SetEventInfo setEventInfo( ProcessGroup processGroupData, Map<String, String> materialUdfs, Map<String, String> subMaterialUdfs )
	{
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setMaterialUdfs(materialUdfs);
		setEventInfo.setSubMaterialUdfs(subMaterialUdfs);
				
		Map<String,String> processGroupUdfs = processGroupData.getUdfs();
		setEventInfo.setUdfs(processGroupUdfs);
		
		return setEventInfo;
	}

	/*
	* Name : undoInfo
	* Desc : This function is undoInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.11
	*/
	public UndoInfo undoInfo( ProcessGroup processGroupData, String eventName, Timestamp eventTime, String eventTimeKey, String eventUser, String lastEventTimeKey)
	{
		UndoInfo undoInfo = new UndoInfo();
		undoInfo.setEventName(eventName);
		undoInfo.setEventTime(eventTime);
		undoInfo.setEventTimeKey(eventTimeKey);
		undoInfo.setEventUser(eventUser);
		undoInfo.setLastEventTimeKey(lastEventTimeKey);
	
		Map<String,String> processGroupUdfs = processGroupData.getUdfs();
		undoInfo.setUdfs(processGroupUdfs);
		
		return undoInfo;
	}
	
	// -COMMENT- DT
	// 2011.02.15 rangzi // 2016.02.20 LEE HYEON WOO
	public static ChangeSpecInfo changeSpecInfo(ProcessGroup processGroup,
												String toProcessOperationName) throws Exception
	{		
		List<Lot> lotList = LotServiceProxy.getLotService().allLotsByProcessGroup(processGroup.getKey().getProcessGroupName());

		Lot lotData = null;
		if(lotList.size() > 0)
			lotData = lotList.get(0);
		
		// Set Variable
		String processFlowName =  CommonUtil.getProcessFlowNameByFGCode(lotData, processGroup.getUdfs().get("FGCODE"));
		
		ProcessOperationSpec processOperationData = CommonUtil.getProcessOperationSpec(lotData, toProcessOperationName);
		
		String areaName = processOperationData.getDefaultAreaName();
		
		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();
		changeSpecInfo.setProductionType(lotData.getProductionType());
		changeSpecInfo.setProductSpecName(lotData.getProductSpecName());
		changeSpecInfo.setProductSpecVersion(lotData.getProductSpecVersion());
		changeSpecInfo.setProductSpec2Name(lotData.getProductSpec2Name());
		changeSpecInfo.setProductSpec2Version(lotData.getProductSpec2Version());
		changeSpecInfo.setProductRequestName(lotData.getProductRequestName());
		changeSpecInfo.setDueDate(lotData.getDueDate());
		changeSpecInfo.setPriority(lotData.getPriority());
		changeSpecInfo.setFactoryName(lotData.getFactoryName());
		changeSpecInfo.setAreaName(areaName);
		changeSpecInfo.setProcessFlowName(processFlowName);
		changeSpecInfo.setProcessFlowVersion(lotData.getProcessFlowVersion());
		changeSpecInfo.setProcessOperationName(toProcessOperationName);
		changeSpecInfo.setProcessOperationVersion(lotData.getProcessOperationVersion());

		String nodeStack = NodeStack.getNodeID(lotData.getFactoryName(), processFlowName, toProcessOperationName);

		changeSpecInfo.setNodeStack(nodeStack);
		
		return changeSpecInfo;
	}
	
	// -COMMENT- DT
	// 2011.02.15 rangzi // 2016.02.22 LEE HYEON WOO
	public static DeassignMaterialsInfo deassignMaterialsInfo(ProcessGroup processGroup)
	{
		List<MaterialU> materialUList = new ArrayList<MaterialU>();
		
		List<Lot> lotList = LotServiceProxy.getLotService().allLotsByProcessGroup(processGroup.getKey().getProcessGroupName());
		
		for(int i = 0; i < lotList.size(); i++)
		{
			MaterialU materialU = new MaterialU();
			materialU.setMaterialName(lotList.get(i).getKey().getLotName());
			materialU.setUdfs(new HashMap<String, String>());
			
			materialUList.add(materialU);
		}
		
		DeassignMaterialsInfo deassignMaterialsInfo = new DeassignMaterialsInfo();
		deassignMaterialsInfo.setMaterialQuantity(processGroup.getMaterialQuantity());
		deassignMaterialsInfo.setMaterialUSequence(materialUList);
		
		Map<String, String> processGroupUdfs = processGroup.getUdfs();
		deassignMaterialsInfo.setUdfs(processGroupUdfs);
		
		return deassignMaterialsInfo;
	}
	
	// -COMMENT- DT
	// 2011.02.16 JUNG SUN KYU
	public static ChangeSpecInfo changeSpecBoxInfo(ProcessGroup processGroup,
												String toProcessOperationName) throws Exception
	{		
		List<ProcessGroup> boxList = 
			ProcessGroupServiceProxy.getProcessGroupService().allProcessGroupsBySuperProcessGroup(processGroup.getKey().getProcessGroupName());

		ProcessGroup boxData = null;
		if(boxList.size() > 0)
			boxData = boxList.get(0);
		
		List<Lot> lotList = LotServiceProxy.getLotService().allLotsByProcessGroup(boxData.getKey().getProcessGroupName());

		Lot lotData = null;
		if(lotList.size() > 0)
			lotData = lotList.get(0);
		
		// Set Variable
		String processFlowName =  CommonUtil.getProcessFlowNameByFGCode(processGroup.getUdfs().get("FGCODE"));
		
		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();
		changeSpecInfo.setProductionType(lotData.getProductionType());
		changeSpecInfo.setProductSpecName(lotData.getProductSpecName());
		changeSpecInfo.setProductSpecVersion(lotData.getProductSpecVersion());
		changeSpecInfo.setProductSpec2Name(lotData.getProductSpec2Name());
		changeSpecInfo.setProductSpec2Version(lotData.getProductSpec2Version());
		changeSpecInfo.setProductRequestName(lotData.getProductRequestName());
		changeSpecInfo.setDueDate(lotData.getDueDate());
		changeSpecInfo.setPriority(lotData.getPriority());
		changeSpecInfo.setFactoryName(lotData.getFactoryName());
		changeSpecInfo.setAreaName("MODULE");
		changeSpecInfo.setProcessFlowName(processFlowName);
		changeSpecInfo.setProcessFlowVersion(lotData.getProcessFlowVersion());
		changeSpecInfo.setProcessOperationName(toProcessOperationName);
		changeSpecInfo.setProcessOperationVersion(lotData.getProcessOperationVersion());

		String nodeStack = NodeStack.getNodeID(lotData.getFactoryName(), processFlowName, toProcessOperationName);

		changeSpecInfo.setNodeStack(nodeStack);
		
		return changeSpecInfo;
	}
	
	// -COMMENT- DT
	// 2011.02.16 JUNG SUN KYU //2016.02.26 LEE HYEON WOO
	public static DeassignMaterialsInfo deassignMaterialsPalletInfo(ProcessGroup processGroup)
	{
		List<MaterialU> materialUList = new ArrayList<MaterialU>();
		
		List<ProcessGroup> boxList = 
			ProcessGroupServiceProxy.getProcessGroupService().allProcessGroupsBySuperProcessGroup(processGroup.getKey().getProcessGroupName());
		
		for(int i = 0; i < boxList.size(); i++)
		{
			MaterialU materialU = new MaterialU();
			materialU.setMaterialName(boxList.get(i).getKey().getProcessGroupName());
			materialU.setUdfs(new HashMap<String, String>());
			
			materialUList.add(materialU);
		}
		
		DeassignMaterialsInfo deassignMaterialsInfo = new DeassignMaterialsInfo();
		
		deassignMaterialsInfo.setMaterialQuantity(processGroup.getMaterialQuantity());
		deassignMaterialsInfo.setMaterialUSequence(materialUList);
		
		Map<String, String> processGroupUdfs = processGroup.getUdfs();
		deassignMaterialsInfo.setUdfs(processGroupUdfs);
		
		return deassignMaterialsInfo;
	}
	
	/**
	 * 
	 * @author swcho
	 * @since 2016.03.12
	 * @param processGroupName
	 * @param processGroupType
	 * @param materialType
	 * @param detailMaterialType
	 * @param materialNames
	 * @param materialQuantity
	 * @param subMaterialUdfs
	 * @param processGroupUdfs
	 * @return
	 */
	public CreateInfo createInfo(String processGroupName,
								String processGroupType, String materialType, String detailMaterialType,
								List<String> materialNames, long materialQuantity,
								Map<String, String> subMaterialUdfs, Map<String, String> processGroupUdfs)
		throws CustomException
	{
		CreateInfo createInfo = new CreateInfo();
		
		createInfo.setDetailMaterialType(detailMaterialType);
		createInfo.setMaterialNames(materialNames);
		createInfo.setMaterialQuantity(materialQuantity);
		createInfo.setMaterialType(materialType);
		createInfo.setMaterialUdfs(subMaterialUdfs);
		createInfo.setProcessGroupName(processGroupName);
		createInfo.setProcessGroupType(processGroupType);
		
		createInfo.setUdfs(processGroupUdfs);
		
		return createInfo;
	}
}
