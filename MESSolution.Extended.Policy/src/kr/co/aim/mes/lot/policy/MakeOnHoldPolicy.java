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
import kr.co.aim.greentrack.generic.state.LotStateModel;
import kr.co.aim.greentrack.generic.state.support.StateMachine;
import kr.co.aim.greentrack.lot.management.data.Lot;

public class MakeOnHoldPolicy
		extends kr.co.aim.greentrack.lot.management.policy.MakeOnHoldPolicy{
	
	public void makeOnHold(DataInfo oldLot, DataInfo newLot, EventInfo eventInfo, TransitionInfo transitionInfo)
			throws NotFoundSignal, DuplicateNameSignal, FrameworkErrorSignal, InvalidStateTransitionSignal
	{
		Lot newLotData = (Lot) newLot;
		Lot oldLotData = (Lot) oldLot;
		// MakeOnHoldInfo makeOnHoldInfo = (MakeOnHoldInfo) transitionInfo;

		/*executeValidation(eventInfo.getBehaviorName(), this.getClass().getName(), "CanNotDoAtLotState",
			newLotData.getKey(), oldLotData.getLotState(), oldLotData.getLotProcessState(),
			oldLotData.getLotHoldState());*/
		makeLotState(oldLotData, newLotData, GenericServiceProxy.getConstantMap().Lot_OnHold);
	}
	
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
		
		if(oldLotData.getLotHoldState().equals(  GenericServiceProxy.getConstantMap().Lot_OnHold)){
			return oldLotData;
		}

		// 1. Check an avaliable state and initialize child states

		if (stateMachine.changeStateRaw(oldLotData.getLotState()) == false)
		{
			throw new InvalidStateTransitionSignal(oldLotData.getKey().getLotName(), currentLotState, lotState);
		}
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
		return newLotData;

	}

}
