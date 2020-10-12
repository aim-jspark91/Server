package kr.co.aim.mes.lot.policy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import kr.co.aim.greenframe.greenFrameServiceProxy;
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
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.ProductMultiHold;
import kr.co.aim.greentrack.product.management.data.ProductMultiHoldKey;

public class MakeNotOnHoldPolicy
		extends kr.co.aim.greentrack.lot.management.policy.MakeNotOnHoldPolicy{
	
	public void makeNotOnHold(DataInfo oldLot, DataInfo newLot, EventInfo eventInfo, TransitionInfo transitionInfo)
			throws NotFoundSignal, DuplicateNameSignal, FrameworkErrorSignal, InvalidStateTransitionSignal
	{
		Lot newLotData = (Lot) newLot;
		Lot oldLotData = (Lot) oldLot;
		
		executeValidation(eventInfo.getBehaviorName(), "CanNotDoAtLotState", newLotData.getKey(),
			oldLotData.getLotState(), oldLotData.getLotProcessState(), oldLotData.getLotHoldState());

		makeLotState(oldLotData, newLotData, getConstantMap().Lot_NotOnHold, eventInfo);
	}

	protected DataInfo makeLotState(DataInfo oldData, DataInfo newData, String lotState, EventInfo eventInfo)
			throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal
	{
		
		Lot oldLotData = (Lot) oldData;
		Lot newLotData = (Lot) newData;
		
		boolean multiHold =false;
		multiHold = checkProductMultiHold(oldLotData, eventInfo);

		if(multiHold){
			return oldLotData;
		}
		
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

	private boolean checkProductMultiHold( Lot oldLotData, EventInfo eventInfo )
	{
		String sql = "SELECT * FROM PRODUCTMULTIHOLD "
				+ " WHERE LOTNAME =:LOTNAME AND REASONCODE <>:REASONCODE ";

		Map<String, String> bindMap = new HashMap<String, String>();

		bindMap.put("LOTNAME", oldLotData.getKey().getLotName() );
		bindMap.put("REASONCODE", eventInfo.getReasonCode());
		
		List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList( sql, bindMap );
		
		
		if(sqlResult.size()>0){
			return true;
		}
	
		return false;
	}

	
}
