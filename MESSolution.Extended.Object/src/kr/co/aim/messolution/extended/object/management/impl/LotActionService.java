package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.LotAction;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LotActionService extends CTORMService<LotAction> {
	
	public static Log logger = LogFactory.getLog(LotActionService.class);
	
	private final String historyEntity = "LotActionHist";
	
	public List<LotAction> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<LotAction> result = super.select(condition, bindSet, LotAction.class);
		
		return result;
	}
	
	public LotAction selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(LotAction.class, isLock, keySet);
	}
	
	public LotAction create(EventInfo eventInfo, LotAction dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, LotAction dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public LotAction modify(EventInfo eventInfo, LotAction dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
	
	public LotAction setSampleActioninfo(EventInfo eventInfo, Lot lotData, String processFlowName, String processOperationName, long position,
			String sampleFlow, String samplePosition, String sampleOutHoldFlag, String departmentName,String ReasonCode,String sampleDepartmentName)
	{
		LotAction sampleAction = new LotAction();
		
		sampleAction.setLotName(lotData.getKey().getLotName());
		sampleAction.setFactoryName(lotData.getFactoryName());
		sampleAction.setProcessFlowName(processFlowName);
		sampleAction.setProcessFlowVersion("00001");
		sampleAction.setProcessOperationName(processOperationName);
		sampleAction.setProcessOperationVersion("00001");
		sampleAction.setPosition(position);
		sampleAction.setActionName("Sampling");
		sampleAction.setActionState("Created");
		sampleAction.setSampleProcessFlowName(sampleFlow);
		sampleAction.setSampleProcessFlowVersion("00001");
		sampleAction.setSamplePosition(samplePosition);
		sampleAction.setSampleOutHoldFlag(sampleOutHoldFlag);
		sampleAction.setDepartment(departmentName);
		sampleAction.setLastEventUser(eventInfo.getEventUser());
		sampleAction.setLastEventComment(eventInfo.getEventComment());
		sampleAction.setLastEventTime(eventInfo.getEventTime());
		sampleAction.setLastEventTimeKey(eventInfo.getEventTimeKey());
		sampleAction.setLastEventName(eventInfo.getEventName());
		
		//  modify by JHIYING start 0004289
		
		sampleAction.setReasonCode(ReasonCode);
		sampleAction.setSampleDepartmentName(sampleDepartmentName);
		
		// modify by JHIYING end  0004289
		return sampleAction;
	}
	private boolean isSameSamplingFlow(String lotName,String sampleProcessFlowName){
		LotKey keyInfo = new LotKey(lotName);
		Lot lotData = LotServiceProxy.getLotService().selectByKey(keyInfo);
		
		if(StringUtils.equalsIgnoreCase(sampleProcessFlowName,lotData.getProcessFlowName())){
			return true;
		}
		else{
			return false;
		}
	}
	/*
	 * Cancel ReserveSampling 에 2가지 validation 이 있다.
	 * 1. 예약한 Sampling Flow와 현재 lot의 ProcessFlow가 같고
	 * 2. Lot의 마지막에서 2번째의 Node Id 를 통해 가져온 OperationName과 예약한 FromProcessOperationName이 같다면 취소 불가
	 *   Ex ) NodeStack : A.B.C.D
	 *   위 경우 C가 마지막에서 2번째 Node Id
	 * */
	public void checkFlowNameAndNodeStack(String lotName,String sampleProcessFlowName) throws CustomException {
		LotKey keyInfo = new LotKey(lotName);
		Lot lotData = LotServiceProxy.getLotService().selectByKey(keyInfo);
		
		if(!this.isSameSamplingFlow(lotName, sampleProcessFlowName)){
			return;
		}
		
		List<LotAction> samplingActionList = new ArrayList<LotAction>();
		String tempNodeStack = lotData.getNodeStack();
		String[] arrNodeStack = StringUtil.split(tempNodeStack, ".");
		int count = arrNodeStack.length;
		
		Node node = ProcessFlowServiceProxy.getNodeService().getNode(arrNodeStack[count-2]);
		// FromProcessOperationName == node.getNodeAttribute1()
		
		
		String condition = " WHERE 1=1 AND lotName = ? AND factoryName = ? AND sampleProcessFlowName = ? AND sampleProcessFlowVersion = ? AND processOperationName = ?"
				+ " AND processOperationVersion = ? AND actionName = ?";
		Object[] bindSet = new Object[]{ lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
				node.getNodeAttribute1(), "00001", "Sampling" };
		try {
			samplingActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);
			
			if(samplingActionList!=null && samplingActionList.size()>0){
				throw new CustomException("LOT-0227");
			}
			
		} catch (Exception e) {
			if(samplingActionList!=null){
				throw new CustomException("LOT-0227");
			}
		}
	}
}
