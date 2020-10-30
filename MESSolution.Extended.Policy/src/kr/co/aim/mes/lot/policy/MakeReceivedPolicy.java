package kr.co.aim.mes.lot.policy;

import org.apache.commons.lang.StringUtils;

import kr.co.aim.greenframe.orm.info.DataInfo;
import kr.co.aim.greentrack.generic.GenericServiceProxy;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.TransitionInfo;
import kr.co.aim.greentrack.generic.policy.MovePOFlag;
import kr.co.aim.greentrack.generic.state.LotStateModel;
import kr.co.aim.greentrack.generic.state.support.StateMachine;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.MakeReceivedInfo;

public class MakeReceivedPolicy
		extends kr.co.aim.greentrack.lot.management.policy.MakeReceivedPolicy{
	
	public void makeReceived(DataInfo oldLot, DataInfo newLot, EventInfo eventInfo, TransitionInfo transitionInfo)
			throws NotFoundSignal, DuplicateNameSignal, FrameworkErrorSignal, InvalidStateTransitionSignal
	{
		Lot newLotData = (Lot) newLot;
		Lot oldLotData = (Lot) oldLot;
		MakeReceivedInfo makeReceivedInfo = (MakeReceivedInfo) transitionInfo;

		executeValidation(eventInfo.getBehaviorName(), "CanNotDoAtLotState", newLotData.getKey(),
			oldLotData.getLotState(), oldLotData.getLotProcessState(), oldLotData.getLotHoldState());

		executeValidation(eventInfo.getBehaviorName(), "AreaNotFound", newLotData.getKey(),
			makeReceivedInfo.getAreaName());

		executeValidation(eventInfo.getBehaviorName(), "ProductSpecNotFound", newLotData.getKey(),
			oldLotData.getDestinationFactoryName(), makeReceivedInfo.getProductSpecName(),
			makeReceivedInfo.getProductSpecVersion());

		if (StringUtils.isNotEmpty(makeReceivedInfo.getProductSpec2Name()))
		{
			executeValidation(eventInfo.getBehaviorName(), "ProductSpecNotFound", newLotData.getKey(),
				oldLotData.getDestinationFactoryName(), makeReceivedInfo.getProductSpec2Name(),
				makeReceivedInfo.getProductSpec2Version());
		}

		newLotData.setReleaseTime(eventInfo.getEventTime());
		newLotData.setReleaseUser(eventInfo.getEventUser());

		if(newLotData.getDestinationFactoryName().equals("CELL")){
			newLotData.setLotState("Received");
			newLotData.setLotProcessState("");
			newLotData.setLotHoldState("");
		}else{
			makeLotState(oldLotData, newLotData, GenericServiceProxy.getConstantMap().Lot_Released);
		}

		newLotData.setProductionType(makeReceivedInfo.getProductionType());
		newLotData.setProductSpecName(makeReceivedInfo.getProductSpecName());
		newLotData.setProductSpecVersion(makeReceivedInfo.getProductSpecVersion());
		newLotData.setProductSpec2Name(makeReceivedInfo.getProductSpec2Name());
		newLotData.setProductSpec2Version(makeReceivedInfo.getProductSpec2Version());

		newLotData.setProductRequestName(makeReceivedInfo.getProductRequestName());

		newLotData.setProductType(makeReceivedInfo.getProductType());
		newLotData.setSubProductType(makeReceivedInfo.getSubProductType());

		newLotData.setFactoryName(newLotData.getDestinationFactoryName());
		newLotData.setDestinationFactoryName("");
		newLotData.setAreaName(makeReceivedInfo.getAreaName());

		newLotData.setProcessFlowName(makeReceivedInfo.getProcessFlowName());
		newLotData.setProcessFlowVersion(makeReceivedInfo.getProcessFlowVersion());
		newLotData.setNodeStack(makeReceivedInfo.getNodeStack());
		
		//reworkNode don't need to be move.
		newLotData.setReworkNodeId("");

		LotServiceProxy.getLotProcessPolicyUtil().moveLotProcessOperation(oldLotData, newLotData,
			makeReceivedInfo.getProcessFlowName(), makeReceivedInfo.getProcessFlowVersion(),
			makeReceivedInfo.getProcessOperationName(), makeReceivedInfo.getProcessOperationVersion(),
			makeReceivedInfo.getNodeStack(), "", 0, "", MovePOFlag.MP_MoveFirst);

		executeValidation(eventInfo.getBehaviorName(), "ProcessFlowNotFound", newLotData.getKey(),
			newLotData.getFactoryName(), newLotData.getProcessFlowName(), newLotData.getProcessFlowVersion());

		executeValidation(eventInfo.getBehaviorName(), "ProcessOperationNotFound", newLotData.getKey(),
			newLotData.getFactoryName(), newLotData.getProcessOperationName(), newLotData.getProcessOperationVersion());

		if (StringUtils.isEmpty(makeReceivedInfo.getAreaName()))
		{
			String areaName =
					getAreaName(newLotData.getFactoryName(), newLotData.getProcessOperationName(),
						newLotData.getProcessOperationVersion());
			newLotData.setAreaName(areaName);
		}

	}
/*	
	protected DataInfo makeLotState(DataInfo oldData, DataInfo newData, String lotState)
			throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal
	{
		Lot oldLotData = (Lot) oldData;
		Lot newLotData = (Lot) newData;

		LotStateModel lotStateModel = GenericServiceProxy.getStateModelManager().getLotStateModel();
		StateMachine stateMachine = lotStateModel.getLotStateMachine();

		// 0. Current LotState
		String currentLotState = "";
		currentLotState += oldLotData.getLotState();
		currentLotState += "$";
		currentLotState += oldLotData.getLotProcessState();

		// 1. Check an avaliable state and initialize child states

		if (stateMachine.changeStateRaw(oldLotData.getLotState()) == false)
		{
			throw new InvalidStateTransitionSignal(oldLotData.getKey().getLotName(), currentLotState, lotState);
		}
		if(newLotData.getDestinationFactoryName().equals("CELL")){
			if (StringUtils.equals(oldLotData.getLotState(), "Received"))
			{
				StateMachine psm = stateMachine.findChildStateMachine(0);
				StateMachine hsm = stateMachine.findChildStateMachine(1);

				if (psm.changeStateRaw(oldLotData.getLotProcessState()) == false
					|| hsm.changeStateRaw(oldLotData.getLotHoldState()) == false)
				{
					throw new InvalidStateTransitionSignal(oldLotData.getKey().getLotName(), currentLotState, lotState);
				}
			}
		
		}else{
			if (StringUtils.equals(oldLotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Released))
			{
				StateMachine psm = stateMachine.findChildStateMachine(0);
				StateMachine hsm = stateMachine.findChildStateMachine(1);

				if (psm.changeStateRaw(oldLotData.getLotProcessState()) == false
					|| hsm.changeStateRaw(oldLotData.getLotHoldState()) == false)
				{
					throw new InvalidStateTransitionSignal(oldLotData.getKey().getLotName(), currentLotState, lotState);
				}
			}
		}

		// 2. Check an available event and initialize child states
		if (stateMachine.changeState(lotState) == false)
		{
			throw new InvalidStateTransitionSignal(oldLotData.getKey().getLotName(), currentLotState, lotState);
		}

		// 3. Set currently changed lotState
		if (StringUtils.isEmpty(stateMachine.getCurrentState().getStateName()))
		{
			throw new InvalidStateTransitionSignal(oldLotData.getKey().getLotName(), currentLotState, lotState);
		}

		newLotData.setLotState(stateMachine.getCurrentState().getStateName());
		if(newLotData.getDestinationFactoryName().equals("CELL")){
			if (StringUtils.equals(newLotData.getLotState(), "Received"))
			{
				StateMachine psm = stateMachine.findChildStateMachine(0);
				StateMachine hsm = stateMachine.findChildStateMachine(1);
	
				if (StringUtils.isEmpty(psm.getCurrentState().getStateName())
					|| StringUtils.isEmpty(hsm.getCurrentState().getStateName()))
				{
					throw new InvalidStateTransitionSignal(oldLotData.getKey().getLotName(), currentLotState, lotState);
				}
	
				newLotData.setLotProcessState(psm.getCurrentState().getStateName());
				newLotData.setLotHoldState(hsm.getCurrentState().getStateName());
			}
			else
			{
				newLotData.setLotProcessState("");
				newLotData.setLotHoldState("");
			}
		}else{
			if (StringUtils.equals(newLotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Released))
			{
				StateMachine psm = stateMachine.findChildStateMachine(0);
				StateMachine hsm = stateMachine.findChildStateMachine(1);
	
				if (StringUtils.isEmpty(psm.getCurrentState().getStateName())
					|| StringUtils.isEmpty(hsm.getCurrentState().getStateName()))
				{
					throw new InvalidStateTransitionSignal(oldLotData.getKey().getLotName(), currentLotState, lotState);
				}
	
				newLotData.setLotProcessState(psm.getCurrentState().getStateName());
				newLotData.setLotHoldState(hsm.getCurrentState().getStateName());
			}
			else
			{
				newLotData.setLotProcessState("");
				newLotData.setLotHoldState("");
			}
		}
		return newLotData;

	}*/

}
