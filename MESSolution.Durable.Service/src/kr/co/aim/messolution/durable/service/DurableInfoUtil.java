package kr.co.aim.messolution.durable.service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.data.DurableSpecKey;
import kr.co.aim.greentrack.durable.management.info.AssignTransportGroupInfo;
import kr.co.aim.greentrack.durable.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.durable.management.info.CleanInfo;
import kr.co.aim.greentrack.durable.management.info.CreateInfo;
import kr.co.aim.greentrack.durable.management.info.DeassignTransportGroupInfo;
import kr.co.aim.greentrack.durable.management.info.DecrementDurationUsedInfo;
import kr.co.aim.greentrack.durable.management.info.DecrementTimeUsedInfo;
import kr.co.aim.greentrack.durable.management.info.DirtyInfo;
import kr.co.aim.greentrack.durable.management.info.IncrementDurationUsedInfo;
import kr.co.aim.greentrack.durable.management.info.IncrementTimeUsedInfo;
import kr.co.aim.greentrack.durable.management.info.MakeAvailableInfo;
import kr.co.aim.greentrack.durable.management.info.MakeInUseInfo;
import kr.co.aim.greentrack.durable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.durable.management.info.MakeNotInUseInfo;
import kr.co.aim.greentrack.durable.management.info.MakeScrappedInfo;
import kr.co.aim.greentrack.durable.management.info.RepairInfo;
import kr.co.aim.greentrack.durable.management.info.SetAreaInfo;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.management.info.UndoInfo;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DurableInfoUtil implements ApplicationContextAware {
	/**
	 * @uml.property name="applicationContext"
	 * @uml.associationEnd
	 */
	private ApplicationContext applicationContext;
	// private NamedValue[] durableData;
	private static Log log = LogFactory.getLog(DurableInfoUtil.class);

	/**
	 * @param arg0
	 * @throws BeansException
	 * @uml.property name="applicationContext"
	 */
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		applicationContext = arg0;
	}

	/*
	* Name : AssignTransportGroup
	* Desc : This function is Make durable AssignTransportGroupInfo 
	* Author : AIM Systems, Inc
	* Date : 2010.12.23
	*/

	public AssignTransportGroupInfo assignTransportGroupInfo(
			Durable durableData, String transportGroupName) {

		AssignTransportGroupInfo assignTransportGroupInfo = new AssignTransportGroupInfo();

		assignTransportGroupInfo.setTransportGroupName(transportGroupName);

		Map<String, String> durableUdfs = durableData.getUdfs();
		assignTransportGroupInfo.setUdfs(durableUdfs);
		
		return assignTransportGroupInfo;
	}

	/*
	* Name : changeSpecInfo
	* Desc : This function is Make durable ChangeSpecInfo
	* Author : AIM Systems, Inc
	* Date : 2010.12.23
	*/
	public ChangeSpecInfo changeSpecInfo(Durable durableData,
			String areaName, long capacity, String durableSpecName,
			String durableSpecVersion, double durationUsedLimit,
			String factoryName, double timeUsedLimit) {

		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();

		changeSpecInfo.setAreaName(areaName);
		changeSpecInfo.setCapacity(capacity);
		changeSpecInfo.setDurableSpecName(durableSpecName);
		changeSpecInfo.setDurableSpecVersion(durableSpecVersion);
		changeSpecInfo.setDurationUsedLimit(durationUsedLimit);
		changeSpecInfo.setFactoryName(factoryName);
		changeSpecInfo.setTimeUsedLimit(timeUsedLimit);

		Map<String, String> durableUdfs = durableData.getUdfs();
		changeSpecInfo.setUdfs(durableUdfs);

		return changeSpecInfo;
	}
	/*
	* Name : changeSpecInfo
	* Desc : This function is  Clean Durable Make cleanInfo and Update udf MACHINENAME 
	* Author : AIM Systems, Inc
	* Date : 2010.12.23
	*/
	public  CleanInfo cleanInfo(Durable durableData,
			String machineName) {

		CleanInfo cleanInfo = new CleanInfo();

		Map<String, String> durableUdfs = durableData.getUdfs();
		if(!StringUtils.isEmpty(machineName)) {
			durableUdfs.put("MACHINENAME", machineName);
		}
		/*
		durableUdfs.put("INSPECTFLAG", "N");
		durableUdfs.put("INSPECTSTARTTIMEKEY", "");
		durableUdfs.put("INSPECTENDTIMEKEY", "");
		durableUdfs.put("INSPECTNECESSARY", "Y");
		*/
		
		cleanInfo.setUdfs(durableUdfs);

		return cleanInfo;
	}
	
	/*
	* Name : createInfo
	* Desc : This function is Make durable CreateInfo
	* Author : AIM Systems, Inc
	* Date : 2010.12.23
	*/
	public CreateInfo createInfo(Durable durableData, String durableName,
											String durableType,
											String durableSpecName,
											String durableSpecVersion,
											double timeUsedLimit,
											double durationUsedLimit,
											long capacity,
											String factoryName,
											String areaName) {
		
		CreateInfo createInfo = new CreateInfo();
		
		createInfo.setAreaName(areaName);
		createInfo.setCapacity(capacity);
		createInfo.setDurableName(durableName);
		createInfo.setDurableSpecName(durableSpecName);
		createInfo.setDurableSpecVersion(durableSpecVersion);
		createInfo.setDurableType(durableType);
		createInfo.setDurationUsedLimit(durationUsedLimit);
		createInfo.setFactoryName(factoryName);
		createInfo.setTimeUsedLimit(timeUsedLimit);
		
		return createInfo;		
	}

	/*
	* Name : deassignTransportGroupInfo
	* Desc : This function is Make durable DeassignTransportGroupInfo
	* Author : AIM Systems, Inc
	* Date : 2010.12.23
	*/
	public DeassignTransportGroupInfo deassignTransportGroupInfo(
			Durable durableData, String transportGroupName) {

		DeassignTransportGroupInfo deassignTransportGroupInfo = new DeassignTransportGroupInfo();

		Map<String, String> durableUdfs = durableData.getUdfs();
		deassignTransportGroupInfo.setUdfs(durableUdfs);

		return deassignTransportGroupInfo;
	}

	/*
	* Name : decrementDurationUsedInfo
	* Desc : This function is Make durable DecrementDurationUsedInfo
	* Author : AIM Systems, Inc
	* Date : 2010.12.23
	*/
	public DecrementDurationUsedInfo decrementDurationUsedInfo(
			Durable durableData, double durationUsed) {

		DecrementDurationUsedInfo decrementDurationUsedInfo = new DecrementDurationUsedInfo();

		decrementDurationUsedInfo.setDurationUsed(durationUsed);

		Map<String, String> durableUdfs = durableData.getUdfs();
		decrementDurationUsedInfo.setUdfs(durableUdfs);

		return decrementDurationUsedInfo;
	}

	/*
	* Name : decrementDurationUsedInfo
	* Desc : This function is Make durable DecrementTimeUsedInfo
	* Author : AIM Systems, Inc
	* Date : 2010.12.23
	*/
	public DecrementTimeUsedInfo decrementTimeUsedInfo(
			Durable durableData, double timeUsed) {

		DecrementTimeUsedInfo decrementTimeUsedInfo = new DecrementTimeUsedInfo();

		decrementTimeUsedInfo.setTimeUsed(timeUsed);

		Map<String, String> durableUdfs = durableData.getUdfs();
		decrementTimeUsedInfo.setUdfs(durableUdfs);

		return decrementTimeUsedInfo;
	}

	/*
	* Name : dirtyInfo
	* Desc : This function is Dirty Durable Make incrementDurationUsedInfo
	* Author : AIM Systems, Inc
	* Date : 2010.12.23
	*/
	public DirtyInfo dirtyInfo(Durable durableData, String machineName) {

		DirtyInfo dirtyInfo = new DirtyInfo();

		Map<String, String> durableUdfs = durableData.getUdfs();
		if(!StringUtils.isEmpty(machineName)) {
			durableUdfs.put("MACHINENAME", machineName);
		}
		dirtyInfo.setUdfs(durableUdfs);

		return dirtyInfo;
	}

	/*
	* Name : incrementDurationUsedInfo
	* Desc : This function is Make Durable IncrementDurationUsedInfo
	* Author : AIM Systems, Inc
	* Date : 2010.12.23
	*/
	public IncrementDurationUsedInfo incrementDurationUsedInfo(
			Durable durableData, double durationUsed) {

		IncrementDurationUsedInfo incrementDurationUsedInfo = new IncrementDurationUsedInfo();

		incrementDurationUsedInfo.setDurationUsed(durationUsed);

		Map<String, String> durableUdfs = durableData.getUdfs();
		incrementDurationUsedInfo.setUdfs(durableUdfs);

		return incrementDurationUsedInfo;
	}

	/*
	* Name : incrementTimeUsedInfo
	* Desc : This function is Make Durable IncrementTimeUsedInfo
	* Author : AIM Systems, Inc
	* Date : 2010.12.23
	*/
	public IncrementTimeUsedInfo incrementTimeUsedInfo(Durable durableData, int timeUsed, String consumerLotName, String consumerProductName, 
															String consumerTimeKey, String consumerPOName, String consumerPOVersion)
	{
		IncrementTimeUsedInfo incrementTimeUsedInfo = new IncrementTimeUsedInfo();

		incrementTimeUsedInfo.setTimeUsed(timeUsed);
		incrementTimeUsedInfo.setConsumerLotName(consumerLotName);
		incrementTimeUsedInfo.setConsumerProductName(consumerProductName);
		incrementTimeUsedInfo.setConsumerTimeKey(consumerTimeKey);
		incrementTimeUsedInfo.setConsumerPOName(consumerPOName);
		incrementTimeUsedInfo.setConsumerPOVersion(consumerPOVersion);

		Map<String, String> durableUdfs = durableData.getUdfs();
		incrementTimeUsedInfo.setUdfs(durableUdfs);

		return incrementTimeUsedInfo;
	}

	/*
	* Name : makeAvailableInfo
	* Desc : This function is Make Durable MakeAvailableInfo
	* Author : AIM Systems, Inc
	* Date : 2010.12.23
	*/
	public MakeAvailableInfo makeAvailableInfo(Durable durableData) {
		MakeAvailableInfo makeAvailableInfo = new MakeAvailableInfo();

		Map<String, String> durableUdfs = durableData.getUdfs();
		makeAvailableInfo.setUdfs(durableUdfs);

		return makeAvailableInfo;
	}
	/*
	* Name : makeInUseInfo
	* Desc : This function is Make Durable MakeInUseInfo
	* Author : AIM Systems, Inc
	* Date : 2010.12.23
	*/
	public MakeInUseInfo makeInUseInfo(Durable durableData,
			String consumerLotName, String consumerTimeKey,
			String consumerPOName, String consumerPOVersion) {
		MakeInUseInfo makeInUseInfo = new MakeInUseInfo();

		makeInUseInfo.setConsumerLotName(consumerLotName);
		makeInUseInfo.setConsumerPOName(consumerPOName);
		makeInUseInfo.setConsumerPOVersion(consumerPOVersion);
		makeInUseInfo.setConsumerTimeKey(consumerTimeKey);

		Map<String, String> durableUdfs = durableData.getUdfs();
		makeInUseInfo.setUdfs(durableUdfs);

		return makeInUseInfo;
	}

	/*
	* Name : makeNotAvailableInfo
	* Desc : This function is Make Durable MakeNotAvailableInfo
	* Author : AIM Systems, Inc
	* Date : 2010.12.23
	*/
	public MakeNotAvailableInfo makeNotAvailableInfo(Durable durableData) {
		MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();

		Map<String, String> durableUdfs = durableData.getUdfs();
		makeNotAvailableInfo.setUdfs(durableUdfs);

		return makeNotAvailableInfo;
	}

	/*
	* Name : makeNotInUseInfo
	* Desc : This function is Make Durable MakeNotInUseInfo
	* Author : AIM Systems, Inc
	* Date : 2010.12.23
	*/
	public MakeNotInUseInfo makeNotInUseInfo(Durable durableData,
			String consumerLotName, String consumerTimeKey,
			String consumerPOName, String consumerPOVersion) {
		MakeNotInUseInfo makeNotInUseInfo = new MakeNotInUseInfo();

		makeNotInUseInfo.setConsumerLotName(consumerLotName);
		makeNotInUseInfo.setConsumerPOName(consumerPOName);
		makeNotInUseInfo.setConsumerPOVersion(consumerPOVersion);
		makeNotInUseInfo.setConsumerTimeKey(consumerTimeKey);

		Map<String, String> durableUdfs = durableData.getUdfs();
		makeNotInUseInfo.setUdfs(durableUdfs);

		return makeNotInUseInfo;
	}

	/*
	* Name : makeScrappedInfo
	* Desc : This function is Make Durable MakeScrappedInfo
	* Author : AIM Systems, Inc
	* Date : 2010.12.23
	*/
	public MakeScrappedInfo makeScrappedInfo(Durable durableData) {
		MakeScrappedInfo makeScrappedInfo = new MakeScrappedInfo();

		Map<String, String> durableUdfs = durableData.getUdfs();
		makeScrappedInfo.setUdfs(durableUdfs);

		return makeScrappedInfo;
	}

	/*
	* Name : repairInfo
	* Desc : This function is Make Durable RepairInfo udf MACHINENAME
	* Author : AIM Systems, Inc
	* Date : 2011.01.02
	*/
	public RepairInfo repairInfo(Durable durableData, String machineName) {
		RepairInfo repairInfo = new RepairInfo();

		Map<String, String> durableUdfs = durableData.getUdfs();
		if(!StringUtils.isEmpty(machineName)) {
			durableUdfs.put("MACHINENAME", machineName);
		}
		repairInfo.setUdfs(durableUdfs);

		return repairInfo;
	}

	/*
	* Name : setAreaInfo
	* Desc : This function is Make Durable setAreaName
	* Author : AIM Systems, Inc
	* Date : 2011.01.02
	*/
	public SetAreaInfo setAreaInfo(Durable durableData, String areaName) {
		SetAreaInfo setAreaInfo = new SetAreaInfo();

		setAreaInfo.setAreaName(areaName);

		Map<String, String> durableUdfs = durableData.getUdfs();
		setAreaInfo.setUdfs(durableUdfs);

		return setAreaInfo;
	}
	
	/**
	 * @author swcho
	 * @since 2013-05-18
	 * @param lotData
	 * @param areaName
	 * @param udfs
	 * @return deassignCarrierinfo
	 */
	public SetAreaInfo AreaInfo(String areaName,
							    Map<String,String> udfs)
	{		
		SetAreaInfo areaInfo = new SetAreaInfo();
		
		areaInfo.setAreaName(areaName);
		areaInfo.setUdfs(udfs);
		
		return areaInfo;
	}

	/*
	* Name : setEventInfo
	* Desc : This function is Make Durable SetEventInfo 
	* Author : AIM Systems, Inc
	* Date : 2011.01.02
	*/
	public SetEventInfo setEventInfo(Durable durableData, Map<String, String>  areaName) {
		SetEventInfo setEventInfo = new SetEventInfo();

		Map<String, String> durableUdfs = durableData.getUdfs();
		setEventInfo.setUdfs(durableUdfs);

		return setEventInfo;
	}

	/*
	* Name : setEventInfo
	* Desc : This function is Make Durable SetEventInfo 
	* Author : AIM Systems, Inc
	* Date : 2011.01.02
	*/
	public SetEventInfo setEventInfo(Map<String, String> udfs) {
		SetEventInfo setEventInfo = new SetEventInfo();

		Map<String, String> durableUdfs = udfs;
		setEventInfo.setUdfs(durableUdfs);

		return setEventInfo;
	}
	/*
	* Name : setMaterialLocationInfo
	* Desc : This function is Make Durable SetMaterialLocationInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.02
	*/
	public SetMaterialLocationInfo setMaterialLocationInfo(
			Durable durableData, String materialLocationName) {
		SetMaterialLocationInfo setMaterialLocationInfo = new SetMaterialLocationInfo();

		setMaterialLocationInfo.setMaterialLocationName(materialLocationName);

		Map<String, String> durableUdfs = durableData.getUdfs();
		setMaterialLocationInfo.setUdfs(durableUdfs);

		return setMaterialLocationInfo;
	}

	/*
	* Name : undoInfo
	* Desc : This function is Make Durable UndoInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.02
	*/
	public UndoInfo undoInfo(Durable durableData, String eventName,
			Timestamp eventTime, String eventTimeKey, String eventUser,
			String lastEventTimeKey) {
		UndoInfo undoInfo = new UndoInfo();
		undoInfo.setEventName(eventName);
		undoInfo.setEventTime(eventTime);
		undoInfo.setEventTimeKey(eventTimeKey);
		undoInfo.setEventUser(eventUser);
		undoInfo.setLastEventTimeKey(lastEventTimeKey);

		Map<String, String> durableUdfs = durableData.getUdfs();
		undoInfo.setUdfs(durableUdfs);

		return undoInfo;
	}
	
	public CreateInfo createInfo(String durableName, 
										String durableSpecName, 
										String capacity, 
										String factoryName) 
	{

		DurableSpecKey durableSpecKey = new DurableSpecKey();
		
		durableSpecKey.setDurableSpecName(durableSpecName);
		durableSpecKey.setDurableSpecVersion("00001");
		durableSpecKey.setFactoryName(factoryName);
		
		DurableSpec durableSpecData = DurableServiceProxy.getDurableSpecService().selectByKey(durableSpecKey);
		
		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(durableName);
		
		CreateInfo createInfo = new CreateInfo();
		createInfo.setAreaName("");
		createInfo.setCapacity(Long.valueOf(capacity).longValue());
		createInfo.setDurableName(durableName);
		createInfo.setDurableSpecName(durableSpecName);
		createInfo.setDurableSpecVersion("00001");
		createInfo.setDurableType(durableSpecData.getDurableType());
		createInfo.setDurationUsedLimit(durableSpecData.getDurationUsedLimit());
		createInfo.setTimeUsedLimit(durableSpecData.getTimeUsedLimit());
		createInfo.setFactoryName(factoryName);
		
		Map<String, String> durableUdfs = new HashMap<String, String>();
		durableUdfs.put("DURABLECLEANSTATE", GenericServiceProxy.getConstantMap().Dur_Clean);

		// Modified by smkang on 2018.10.18 - Default value will be N.
//		durableUdfs.put("TRANSPORTSTATE", "");
		durableUdfs.put("TRANSPORTSTATE", "N");
		
		durableUdfs.put("DURABLEHOLDSTATE", GenericServiceProxy.getConstantMap().Mac_NotOnHold);
		durableUdfs.put("LASTCLEANTIME", TimeUtils.getCurrentTime());
		durableUdfs.put("TOTALUSEDCOUNT", "0");

		// Added by smkang on 2018.10.18 - According to EDO's request, common carrier can be created in shared factory.
		//								   But a user doesn't want to register DurableSpec in shared factory.
		//								   So we need to search naming rule in owner's factory.
		durableUdfs.put("OWNER", factoryName);
		
		createInfo.setUdfs(durableUdfs);
		
		return createInfo;
	}
	
	/*
	* Name : CreateMaskInfo
	* Desc : Initialize Durable CreateInfo
	* Author : AIM
	* Date : 2015.11.16
	*/
	public CreateInfo createMaskInfo(String durableName,
				String factoryName, String durableType, String durableSpecName, long capacity, double durationUsedLimit, double timeUsedLimit,
				Map<String, String> udfs)
		throws CustomException  
	{
		CreateInfo createInfo = new CreateInfo();
		createInfo.setDurableName(durableName);
		createInfo.setDurableSpecName(durableSpecName);
		createInfo.setDurableSpecVersion("00001");
		createInfo.setDurableType(durableType);
		createInfo.setFactoryName(factoryName);
		createInfo.setAreaName("");
		createInfo.setCapacity(capacity);
		createInfo.setDurationUsedLimit(durationUsedLimit);
		createInfo.setTimeUsedLimit(timeUsedLimit);
		
		createInfo.setUdfs(udfs);
		
		return createInfo;
	}
	
	/*
	* Name : setAssignMaskInfo(ARRAY,CELL)
	* Desc : This function is Make setAssignMaskInfo
	* Author : LHKIM
	* Date : 2014.09.01
	*/
	public SetEventInfo setAssignMaskInfo(String durableType, Map<String, String> udfs, String machineName, String unitName, String sSubUnitName, String maskPosition) 
			throws CustomException  
	{		
		if(StringUtil.equals(durableType , "PhotoMask"))
		{
			udfs.put("TRANSPORTSTATE", "INBUFFER");
			udfs.put("MACHINENAME", machineName);
			udfs.put("UNITNAME", unitName);
			
		}
		else if(StringUtil.equals(durableType , "EVAMask"))
		{
			udfs.put("TRANSPORTSTATE", "ONEQP");
			udfs.put("MACHINENAME", machineName);
			udfs.put("UNITNAME", unitName);
			udfs.put("MASKPOSITION", maskPosition);//EvaMaskCST SlotNo.
			udfs.put("POSITIONNAME", sSubUnitName);//EvaEQP Chamber#SlotNo. (Shelf No + EV.Chamber#1)
		} 
		
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs(udfs);

		return setEventInfo;
	}
	/*
	* Name : setDeassignMaskInfo(ARRAY,CELL)
	* Desc : This function is Make setDeassignMaskInfo
	* Author : LHKIM
	* Date : 2014.09.01
	*/
	public SetEventInfo setDeassignMaskInfo(String durableType, Map<String, String> udfs, String machineName,String unitName,String subUnitName ) 
	{
		
		if(StringUtil.equals(durableType, "PhotoMask"))
		{
			udfs.put("TRANSPORTSTATE", "UNKNOWN");
			udfs.put("MACHINENAME", machineName);
			udfs.put("UNITNAME", unitName);
			udfs.put("POSITIONNAME", "");
		}
		else if(StringUtil.equals(durableType, "EVAMask"))
		{
			udfs.put("TRANSPORTSTATE", "OUTSTK");
			udfs.put("MACHINENAME", "");
			udfs.put("UNITNAME", "");
			udfs.put("POSITIONNAME", "");
		}		
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs(udfs);

		return setEventInfo;
	}
	/*
	* Name : setCreateMaskCSTInfo
	* Desc : This function is Make CreateMaskCSTInfo
	* Author : LHKIM
	* Date : 2014.09.23
	*/
	public CreateInfo createMaskCSTInfo(String durableName, 
			String durableSpecName, 
			String capacity, 
			String factoryName) 
	{

		DurableSpecKey durableSpecKey = new DurableSpecKey();
		
		durableSpecKey.setDurableSpecName(durableSpecName);
		durableSpecKey.setDurableSpecVersion("00001");
		durableSpecKey.setFactoryName(factoryName);
		
		DurableSpec durableSpecData = DurableServiceProxy.getDurableSpecService().selectByKey(durableSpecKey);
		
		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(durableName);
		
		CreateInfo createInfo = new CreateInfo();
		createInfo.setAreaName("");
		createInfo.setCapacity(Long.valueOf(capacity).longValue());
		createInfo.setDurableName(durableName);
		createInfo.setDurableSpecName(durableSpecName);
		createInfo.setDurableSpecVersion("00001");
		createInfo.setDurableType(durableSpecData.getDurableType());
		createInfo.setDurationUsedLimit(durableSpecData.getDurationUsedLimit());
		createInfo.setTimeUsedLimit(durableSpecData.getTimeUsedLimit());
		createInfo.setFactoryName(factoryName);
		
		Map<String, String> durableUdfs = new HashMap<String, String>();
		durableUdfs.put("TRANSPORTSTATE", "OUTSTK");
		durableUdfs.put("DURABLEHOLDSTATE", GenericServiceProxy.getConstantMap().Mac_NotOnHold);
	
	createInfo.setUdfs(durableUdfs);
	
	return createInfo;
}
	
	/*
	* Name : setAssignEVAMaskCSTInfo
	* Desc : This function is Make setAssignEVAMaskCSTInfo
	* Author : LHKIM
	* Date : 2014.09.25
	*/
	public SetEventInfo setAssignEVAMaskCSTInfo(Durable maskCSTData,Map<String, String> udfs,String maskCarrierName, String maskPosition) throws CustomException 
	{
//		StringBuffer maskpositionBuffer = new StringBuffer();
//		
//		String originalPosition = maskCSTData.getUdfs().get("MASKPOSITION");
//		
//		
//		if(originalPosition.indexOf(maskPosition) > -1 == true)
//		{
//			throw new CustomException("MASK-0006", maskCSTData.getKey()+"MASKPOSITION"+ maskPosition);
//			
//		}
//		
//		if(StringUtil.isEmpty(originalPosition) == true)
//		{
//			originalPosition = maskPosition;
//			maskpositionBuffer.append(originalPosition);
//			maskpositionBuffer.append('^');
//			
//		}else 
//		{
//			maskpositionBuffer.append(originalPosition);
//			maskpositionBuffer.append(maskPosition);
//			maskpositionBuffer.append('^');
//		}

		// Put data into UDF
		//udfs.put("MASKCARRIERNAME", maskCarrierName);	
		//udfs.put("MASKPOSITION", maskpositionBuffer.toString());
		maskCSTData.setUdfs(udfs);
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs(udfs);
		
		return setEventInfo;
		
	}
				
	/*
	* Name : setAssignEVAMaskCSTInfo
	* Desc : This function is Make setAssignEVAMaskCSTInfo
	* Author : LHKIM
	* Date : 2014.09.25
	*/
	public SetEventInfo setAssignEVAMaskPositionInfo(Durable maskData,Map<String, String> udfs,String maskCarrierName, String maskPosition) throws CustomException 
	{
		// Put data into UDF
		udfs.put("MASKCARRIERNAME", maskCarrierName);	
		udfs.put("MASKPOSITION", maskPosition);
		
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs(udfs);
		
		return setEventInfo;
		}
	/*
	* Name : setDeassignEVAMaskCSTInfo
	* Desc : This function is Make setDeassignEVAMaskCSTInfo
	* Author : LHKIM
	* Date : 2014.09.25
	*/
	public SetEventInfo setDeassignEVAMaskCSTInfo(Map<String, String> udfs,String maskCarrierName, String maskPosition)
	{		 

		// Put data into UDF

		udfs.put("MASKCARRIERNAME", maskCarrierName);	
		udfs.put("MASKPOSITION", maskPosition);

		SetEventInfo setEventInfo = new SetEventInfo();

		setEventInfo.setUdfs(udfs);

		return setEventInfo;

	}
	/*
	* Name : setAssignMaskInfo(ARRAY,CELL)
	* Desc : This function is Make setAssignMaskInfo
	* Author : LHKIM
	* Date : 2014.09.01
	*/
	public SetEventInfo setReserveMaskInfo(String durableType, Map<String, String> udfs,String transportState, String machineName,String unitName,  String sSubUnitName ,String position, String maskPosition) 
	{		

		
		udfs.put("TRANSPORTSTATE", transportState);
		udfs.put("MACHINENAME", machineName);
		udfs.put("UNITNAME", unitName);
		udfs.put("MASKPOSITION", maskPosition);//EvaMaskCST SlotNo.
		udfs.put("POSITIONNAME", position +"+"+ sSubUnitName);//EvaEQP Chamber#SlotNo. (Shelf No + EV.Chamber#1)
		
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs(udfs);

		return setEventInfo;
	}
	
	/*
	* Name : setMaskProcessEndInfo
	* Author : LHKIM
	* Date : 2014.11.26
	*/
	public SetEventInfo setMaskProcessEndInfo( Map<String, String> udfs, String machinename, String unitname,String maskCarrierName, String offset ) 
	{
		udfs.put("TRANSPORTSTATE", "OUTSTK");
		udfs.put("MACHINENAME","");
		udfs.put("UNITNAME", "");
		udfs.put("POSITIONNAME", "");
		udfs.put("POSITIONTYPE", "");
		udfs.put("PORTNAME", "");
		udfs.put("OFFSETPRE",offset);
		
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs(udfs);

		return setEventInfo;
	}
	
	/*
	* Name : CreatePPBoxIDInfo(ARRAY,CELL)
	* Desc : This function is Make PPBoxID CreateInfo
	* Author : LHKIM
	* Date : 2014.12.01
	*/
	public CreateInfo CreatePPBoxIDInfo(String ppboxID, String capacity,String factoryName, String durableSpecName)
		throws CustomException  
	{
		DurableSpecKey durableSpecKey = new DurableSpecKey();
		durableSpecKey.setDurableSpecName(durableSpecName);
		durableSpecKey.setDurableSpecVersion("00001");
		durableSpecKey.setFactoryName(factoryName);
		
		DurableSpec durableSpecData = DurableServiceProxy.getDurableSpecService().selectByKey(durableSpecKey);
		
		CreateInfo createInfo = new CreateInfo();
		createInfo.setDurableName(ppboxID);
		createInfo.setDurableSpecName(durableSpecName);
		createInfo.setDurableSpecVersion("00001");
		createInfo.setCapacity(Long.valueOf(capacity).longValue());
		createInfo.setFactoryName(factoryName);
		createInfo.setDurableType("PPBox");
		createInfo.setTimeUsedLimit(durableSpecData.getTimeUsedLimit());
		
		Map<String, String> durableUdfs = new HashMap<String, String>();
		
		createInfo.setUdfs(durableUdfs);
		
		return createInfo;
	}
	/*
	* Name : createProbeInfo
	* Desc : This function is Make ProbeID CreateInfo
	* Author : LHKIM
	* Date : 2014.04.17
	*/
	public CreateInfo createProbeInfo(String durableName, String durableSpecName,
			String capacity, String factoryName)
	{
		DurableSpecKey durableSpecKey = new DurableSpecKey();

		durableSpecKey.setDurableSpecName(durableSpecName);
		durableSpecKey.setDurableSpecVersion("00001");
		durableSpecKey.setFactoryName(factoryName);

		DurableSpec durableSpecData = DurableServiceProxy
				.getDurableSpecService().selectByKey(durableSpecKey);

		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(durableName);

		CreateInfo createInfo = new CreateInfo();
		createInfo.setAreaName("");
		createInfo.setCapacity(Long.valueOf(capacity).longValue());
		createInfo.setDurableName(durableName);
		createInfo.setDurableSpecName(durableSpecName);
		createInfo.setDurableSpecVersion("00001");
		createInfo.setDurableType(durableSpecData.getDurableType());
		createInfo.setDurationUsedLimit(durableSpecData.getDurationUsedLimit());
		createInfo.setTimeUsedLimit(durableSpecData.getTimeUsedLimit());
		createInfo.setFactoryName(factoryName);
		Map<String, String> durableUdfs = new HashMap<String, String>();
		durableUdfs.put("DURABLEHOLDSTATE", GenericServiceProxy
				.getConstantMap().Mac_NotOnHold);

		createInfo.setUdfs(durableUdfs);

		return createInfo;
	}
	
	/*
	* Name : incrementTimeUsedInfo
	* Desc : This function is Make Durable IncrementTimeUsedInfo
	* Author : hwlee89
	* Date : 2016.03.19
	*/
	public IncrementTimeUsedInfo incrementTimeUsedInfo(Durable durableData, int timeUsed)
	{
		IncrementTimeUsedInfo incrementTimeUsedInfo = new IncrementTimeUsedInfo();

		incrementTimeUsedInfo.setTimeUsed(timeUsed);
		incrementTimeUsedInfo.setConsumerLotName("");
		incrementTimeUsedInfo.setConsumerProductName("");
		incrementTimeUsedInfo.setConsumerTimeKey("");
		incrementTimeUsedInfo.setConsumerPOName("");
		incrementTimeUsedInfo.setConsumerPOVersion("");

		Map<String, String> durableUdfs = durableData.getUdfs();
		try
		{
			durableUdfs.put("TOTALUSEDCOUNT", Integer.toString(Integer.parseInt(durableData.getUdfs().get("TOTALUSEDCOUNT").toString())+1));
		}
		catch(Exception ex)
		{
			durableUdfs.put("TOTALUSEDCOUNT", "1" );
		}
		incrementTimeUsedInfo.setUdfs(durableUdfs);

		return incrementTimeUsedInfo;
	}
}
