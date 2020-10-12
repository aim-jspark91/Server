package kr.co.aim.messolution.generic.master;

import java.lang.reflect.Field;

import kr.co.aim.messolution.generic.timer.MessageSchedulerServiceImpl;
import kr.co.aim.greenframe.util.bundle.BundleUtil;
import kr.co.aim.greenframe.util.sys.SystemPropHelper;
import kr.co.aim.greentrack.generic.exception.ExceptionNotify;
import kr.co.aim.greentrack.generic.master.AbstractConstantMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConstantMap extends AbstractConstantMap {
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(ConstantMap.class);

	public void setLogger() {
		parentLog = log;
	}

	public static final String ConstantDef = "select constantName, constantValue" + SystemPropHelper.CR +
	 " from ConstantDef where cacheFlag = 'Y' ORDER BY constantName" ;


	/**
	 * @uml.property  name="spec_CheckedOut"
	 */
	public String					Spec_CheckedOut					= "CheckedOut";
	/**
	 * @uml.property  name="spec_CheckedIn"
	 */
	public String					Spec_CheckedIn					= "CheckedIn";
	/**
	 * @uml.property  name="spec_NotActive"
	 */
	public String					Spec_NotActive					= "NotActive";
	/**
	 * @uml.property  name="spec_Active"
	 */
	public String					Spec_Active						= "Active";

	// StateModel
	/**
	 * @uml.property  name="stml_Resource"
	 */
	public String					Stml_Resource					= "ResourceState";
	/**
	 * @uml.property  name="stml_Communication"
	 */
	public String					Stml_Communication				= "MachineCommunicationState";
	/**
	 * @uml.property  name="stml_PortAccessMode"
	 */
	public String					Stml_PortAccessMode				= "PortAccessMode";
	/**
	 * @uml.property  name="stml_PortTransferState"
	 */
	public String					Stml_PortTransferState			= "PortTransferState";
	/**
	 * @uml.property  name="stml_LotState"
	 */
	public String					Stml_LotState					= "LotState";
	/**
	 * @uml.property  name="stml_LotProcessState"
	 */
	public String					Stml_LotProcessState			= "LotProcessState";
	/**
	 * @uml.property  name="stml_LotHoldState"
	 */
	public String					Stml_LotHoldState				= "LotHoldState";
	/**
	 * @uml.property  name="stml_ProductState"
	 */
	public String					Stml_ProductState				= "ProductState";
	/**
	 * @uml.property  name="stml_ProductProcessState"
	 */
	public String					Stml_ProductProcessState		= "ProductProcessState";
	/**
	 * @uml.property  name="stml_ProductHoldState"
	 */
	public String					Stml_ProductHoldState			= "ProductHoldState";
	/**
	 * @uml.property  name="stml_DurableState"
	 */
	public String					Stml_DurableState				= "DurableState";
	/**
	 * @uml.property  name="stml_DurableCleanState"
	 */
	public String					Stml_DurableCleanState			= "DurableCleanState";
	/**
	 * @uml.property  name="PHMask"
	 */
	public String					PHMask			= "PhotoMask";
	/**
	 * @uml.property  name="EVPMask"
	 */
	public String					EVPMask			= "EVPMask";
	/**
	 * @uml.property  name="stml_ConsumableState"
	 */
	public String					Stml_ConsumableState			= "ConsumableState";

	// 2. tracking
	// 2.1. Lot
	/**
	 * @uml.property  name="lot_Created"
	 */
	public String					Lot_Wait						= "WAIT";
	/**
	 * @uml.property  name="lot_Created"
	 */
	public String					Lot_Run				   			= "RUN";
	/**
	 * @uml.property  name="lot_Created"
	 */
	public String					Lot_Created						= "Created";
	/**
	 * @uml.property  name="lot_Released"
	 */
	public String					Lot_Released					= "Released";
	/**
	 * @uml.property  name="lot_Completed"
	 */
	public String					Lot_Completed					= "Completed";
	/**
	 * @uml.property  name="lot_Shipped"
	 */
	public String					Lot_Shipped						= "Shipped";
	/**
	 * @uml.property  name="lot_Shipped"
	 */
	public String					Lot_Received						= "Received";
	/**
	 * @uml.property  name="lot_Scrapped"
	 */
	public String					Lot_Scrapped					= "Scrapped";
	/**
	 * @uml.property  name="lot_Emptied"
	 */
	public String					Lot_Emptied						= "Emptied";
	/**
	 * @uml.property  name="lot_InitialState"
	 */
	public String					Lot_InitialState				= "Created";
	/**
	 * @uml.property  name="lot_WaitingToLogin"
	 */
	public String					Lot_WaitingToLogin				= "WaitingToLogin";
	/**
	 * @uml.property  name="lot_LoggedIn"
	 */
	public String					Lot_LoggedIn					= "LoggedIn";
	/**
	 * @uml.property  name="lot_LoggedOut"
	 */
	public String					Lot_LoggedOut					= "LoggedOut";
	/**
	 * @uml.property  name="lot_InitialProcessState"
	 */
	public String					Lot_InitialProcessState			= "LoggedIn";
	/**
	 * @uml.property  name="lot_NotOnHold"
	 */
	public String					Lot_NotOnHold					= "NotOnHold";
	/**
	 * @uml.property  name="lot_OnHold"
	 */
	public String					Lot_OnHold						= "OnHold";
	/**
	 * @uml.property  name="lot_InitialHoldState"
	 */
	public String					Lot_InitialHoldState			= "NotOnHold";
	/**
	 * @uml.property  name="lot_InRework"
	 */
	public String					Lot_InRework					= "InRework";
	/**
	 * @uml.property  name="lot_NotInRework"
	 */
	public String					Lot_NotInRework					= "NotInRework";
	/**
	 * @uml.property  name="lot_InitialReworkState"
	 */
	public String					Lot_InitialReworkState			= "NotInRework";
	/**
	 * @uml.property  name="lot_Outted"
	 */
	public String					Lot_Outted						= "Outted";



	// 2.2. Product
	/**
	 * @uml.property  name="prod_NotAllocated"
	 */
	public String					Prod_NotAllocated				= "NotAllocated";
	/**
	 * @uml.property  name="prod_Allocated"
	 */
	public String					Prod_Allocated					= "Allocated";
	/**
	 * @uml.property  name="prod_InProduction"
	 */
	public String					Prod_InProduction				= "InProduction";
	/**
	 * @uml.property  name="prod_Completed"
	 */
	public String					Prod_Completed					= "Completed";
	/**
	 * @uml.property  name="prod_Shipped"
	 */
	public String					Prod_Shipped					= "Shipped";
	/**
	 * @uml.property  name="prod_Scrapped"
	 */
	public String					Prod_Scrapped					= "Scrapped";
	/**
	 * @uml.property  name="prod_Consumed"
	 */
	public String					Prod_Consumed					= "Consumed";
	/**
	 * @uml.property  name="prod_InitialState"
	 */
	public String					Prod_InitialState				= "Allocated";
	/**
	 * @uml.property  name="prod_Traveling"
	 */
	public String					Prod_Traveling					= "Traveling";
	/**
	 * @uml.property  name="prod_Idle"
	 */
	public String					Prod_Idle						= "Idle";
	/**
	 * @uml.property  name="prod_Processing"
	 */
	public String					Prod_Processing					= "Processing";
	/**
	 * @uml.property  name="prod_InitialProcessState"
	 */
	public String					Prod_InitialProcessState		= "Idle";
	/**
	 * @uml.property  name="prod_NotOnHold"
	 */
	public String					Prod_NotOnHold					= "NotOnHold";
	/**
	 * @uml.property  name="prod_OnHold"
	 */
	public String					Prod_OnHold						= "OnHold";
	/**
	 * @uml.property  name="prod_InitialHoldState"
	 */
	public String					Prod_InitialHoldState			= "NotOnHold";
	/**
	 * @uml.property  name="prod_InRework"
	 */
	public String					Prod_InRework					= "InRework";
	/**
	 * @uml.property  name="prod_NotInRework"
	 */
	public String					Prod_NotInRework				= "NotInRework";
	/**
	 * @uml.property  name="prod_InitialReworkState"
	 */
	public String					Prod_InitialReworkState			= "NotInRework";
	/**
	 * @uml.property  name="prod_InitialGrade"
	 */
	public String					prod_InitialGrade				= "G";
	/**
	 * @uml.property  name="prod_InitialGrade"
	 */
	public String					prod_Outted						= "Outted";


	// 2.3. Durable
	/**
	 * @uml.property  name="dur_NotAvailable"
	 */
	public String					Dur_NotAvailable				= "NotAvailable";
	/**
	 * @uml.property  name="dur_Available"
	 */
	public String					Dur_Available					= "Available";
	/**
	 * @uml.property  name="dur_InUse"
	 */
	public String					Dur_InUse						= "InUse";
	/**
	 * @uml.property  name="dur_Scrapped"
	 */
	public String					Dur_Scrapped					= "Scrapped";
	/**
	 * @uml.property  name="dur_Clean"
	 */
	public String					Dur_Clean						= "Clean";
	/**
	 * @uml.property  name="dur_Dirty"
	 */
	public String					Dur_Dirty						= "Dirty";
	/**
	 * @uml.property  name="dur_Repairing"
	 */
	public String					Dur_Repairing					= "Repairing";
	/**
	 * @uml.property  name="dur_InitialState"
	 */
	public String					Dur_InitialState				= "Available";
	/**
	 * @uml.property  name="dur_InitialCleanState"
	 */
	public String					Dur_InitialCleanState			= "Clean";


	// 2.4. Consumable
	/**
	 * @uml.property  name="cons_NotAvailable"
	 */
	public String					Cons_NotAvailable				= "NotAvailable";
	/**
	 * @uml.property  name="cons_Available"
	 */
	public String					Cons_Available					= "Available";
	/**
	 * @uml.property  name="cons_InitialState"
	 */
	public String					Cons_InitialState				= "Available";
	/**
	 * @uml.property  name="cons_Released"
	 */
	public String					Cons_Released					= "Released";
	/**
	 * @uml.property  name="cons_InUse"
	 */
	public String					Cons_InUse						= "InUse";

	/**
	 * @uml.property  name="cons_Scrapped"
	 */
	public String					Cons_Scrapped					= "Scrapped";


	/* 20180328, hhlee, Material Mount Constant Add */
	/**
	 * @uml.property  name="cons_Scrapped"
	 */
	public String					Cons_Mount						= "Mount";

	/* 20180328, hhlee, Material Mount Constant Add */
	/**
	 * @uml.property  name="cons_Scrapped"
	 */
	public String					Cons_Unmount					= "Unmount";


	// 2.5. MaterialGroup
	/**
	 * @uml.property  name="mgp_Lot"
	 */
	public String					Mgp_Lot							= "Lot";
	/**
	 * @uml.property  name="mgp_Product"
	 */
	public String					Mgp_Product						= "Product";

	// 3. ProcessFlow
	// 3.1 Node Type
	/**
	 * @uml.property  name="node_Start"
	 */
	public String					Node_Start						= "Start";
	/**
	 * @uml.property  name="node_End"
	 */
	public String					Node_End						= "End";
	/**
	 * @uml.property  name="node_ProcessOperation"
	 */
	public String					Node_ProcessOperation			= "ProcessOperation";
	/**
	 * @uml.property  name="node_ProcessFlow"
	 */
	public String					Node_ProcessFlow				= "ProcessFlow";
	/**
	 * @uml.property  name="node_ConditionalDivergence"
	 */
	public String					Node_ConditionalDivergence		= "ConditionalDivergence";
	/**
	 * @uml.property  name="node_ConditionalConvergence"
	 */
	public String					Node_ConditionalConvergence		= "ConditionalConvergence";
	/**
	 * @uml.property  name="node_ReworkDivergence"
	 */
	public String					Node_ReworkDivergence			= "ReworkDivergence";
	/**
	 * @uml.property  name="node_ReworkConvergence"
	 */
	public String					Node_ReworkConvergence			= "ReworkConvergence";

    // Rework Step
    public String                     STEP_R_FA                       = "R510";
    public String                     STEP_R_SOLDERING                = "R511";
    public String                     STEP_R_DISASSY                  = "R520";
    public String                     STEP_R_LASER_REPAIR             = "R530";
    public String                     STEP_R_POL_ATTACH               = "R540";
    public String                     STEP_R_TCP_PCB_BONDING          = "R550";
    public String                     STEP_R_PCB_TEST                 = "R551";
    public String                     STEP_R_ASSEMBLY                 = "R560";
    public String                     STEP_R_AGING                    = "R570";
    public String                     STEP_R_FINAL_TEST               = "R571";
    public String                     STEP_R_APP_TEST                 = "R580";
    public String                     STEP_R_REWORK_OUT_WAIT          = "M600";

    // RT Step
    public String                   STEP_RT_PANELINSPECTION         = "RT01";
    public String                   STEP_RT_OQA                     = "RT02";
    public String                   STEP_RT_BOXING                  = "RT03";
    public String                   STEP_RT_PALLETIZING             = "RT04";


    // RMA Step
    public String                   STEP_RMA_WAIT                   = "RMA01";
    public String                   STEP_RMA_FA                     = "RMA02";
    public String                   STEP_RMA_DISASSY                = "RMA03";
    public String                   STEP_RMA_BOXING                 = "RMA04";
    public String                   STEP_RMA_PALLETIZING            = "RMA05";
    public String                   STEP_RMA_OQA                    = "RMA06";
    public String                   STEP_RMA_RT                     = "RMA07";
    public String                   STEP_RMA_OUT_WAIT               = "RMA08";


	// 3.2 Arc Type
	/**
	 * @uml.property  name="arc_Normal"
	 */
	public String					Arc_Normal						= "Normal";
	/**
	 * @uml.property  name="arc_Conditional"
	 */
	public String					Arc_Conditional					= "Conditional";
	/**
	 * @uml.property  name="arc_Rework"
	 */
	public String					Arc_Rework						= "Rework";

	// 4. DataCollection
	/**
	 * @uml.property  name="dcs_Item"
	 */
	public String					Dcs_Item						= "N";

	// 5. ProductRequest
	/**
	 * @uml.property  name="prq_Created"
	 */
	public String					Prq_Created						= "Created";
	/**
	 * @uml.property  name="prq_Released"
	 */
	public String					Prq_Released					= "Released";
	/**
	 * @uml.property  name="prq_Planned"
	 */
	public String					Prq_Planned					= "Planned";
	/**
	 * @uml.property  name="prq_Completed"
	 */
	public String					Prq_Completed					= "Completed";
	/**
	 * @uml.property  name="prq_Finished"
	 */
	public String					Prq_Finished					= "Finished";
	/**
	 * @uml.property  name="Prql_Completed"
	 */
	public String					PrqPlan_Completed					= "Completed";
	/**
	 * @uml.property  name="prq_NotOnHold"
	 */
	public String					Prq_NotOnHold					= "N";
	/**
	 * @uml.property  name="prq_OnHold"
	 */
	public String					Prq_OnHold						= "Y";

	public String					PrqConfirm_Created						= "Created";

	public String					PrqConfirm_Released						= "Released";

	public String					PrqConfirm_Confirmed					= "Confirmed";
	/**
     * @uml.property  name="prq_Created"
     */
    public String                   Prq_Started                     = "Started";

	/**
	 * @uml.property  name="spar_Available"
	 */
	public String					Spar_Available					= "Available";
	/**
	 * @uml.property  name="spar_NotAvailable"
	 */
	public String					Spar_NotAvailable				= "NotAvailable";
	/**
	 * @uml.property  name="spar_InitialState"
	 */
	public String					Spar_InitialState				= "Available";

	/**
	 * @uml.property  name="pMJob_Queued"
	 */
	public String					PMJob_Queued					= "Queued";
	/**
	 * @uml.property  name="pMJob_Paused"
	 */
	public String					PMJob_Paused					= "Paused";
	/**
	 * @uml.property  name="pMJob_Executing"
	 */
	public String					PMJob_Executing					= "Executing";
	/**
	 * @uml.property  name="pMJob_Aborted"
	 */
	public String					PMJob_Aborted					= "Aborted";
	/**
	 * @uml.property  name="pMJob_Canceled"
	 */
	public String					PMJob_Canceled					= "Canceled";
	/**
	 * @uml.property  name="pMJob_Completed"
	 */
	public String					PMJob_Completed					= "Completed";
	/**
	 * @uml.property  name="pMJob_InitialState"
	 */
	public String					PMJob_InitialState				= "Queued";
	/**
	 * @uml.property  name="pMJob_Stopped"
	 */
	public String					PMJob_Stopped					= "Stopped";

	// 6. Machine
	// Resource State
	/**
	 * @uml.property  name="rsc_OutOfService"
	 */
	public String					Rsc_OutOfService				= "OutOfService";						// Default
	/**
	 * @uml.property  name="rsc_InService"
	 */
	public String					Rsc_InService					= "InService";

	/**
	 * @uml.property  name="rsc_InitialResourceState"
	 */
	public String					Rsc_InitialResourceState		= "InService";

	// Machine Type
	/**
	 * @uml.property  name="mac_ProductionMachine"
	 */
	public String					Mac_ProductionMachine			= "ProductionMachine";
	/**
	 * @uml.property  name="mac_StorageMachine"
	 */
	public String					Mac_StorageMachine				= "StorageMachine";
	/**
	 * @uml.property  name="mac_TransportMachine"
	 */
	public String					Mac_TransportMachine			= "TransportMachine";
	/**
	 * @uml.property  name="mac_ProductionResource"
	 */
	public String					Mac_ProductionResource			= "ProductionResource";
	/**
	 * @uml.property  name="mac_StorageResource"
	 */
	public String					Mac_StorageResource				= "StorageResource";
	/**
	 * @uml.property  name="mac_TransportResource"
	 */
	public String					Mac_TransportResource			= "TransportResource";
	/**
	 * @uml.property  name="mac_Port"
	 */
	public String					Mac_Port						= "Port";

	// E10 State
	/**
	 * @uml.property  name="mac_StandBy"
	 */
	public String					Mac_StandBy						= "StandBy";
	/**
	 * @uml.property  name="mac_Productive"
	 */
	public String					Mac_Productive					= "Productive";
	/**
	 * @uml.property  name="mac_Engineering"
	 */
	public String					Mac_Engineering					= "Engineering";
	/**
	 * @uml.property  name="mac_ScheduledDown"
	 */
	public String					Mac_ScheduledDown				= "ScheduledDown";
	/**
	 * @uml.property  name="mac_UnscheduledDown"
	 */
	public String					Mac_UnscheduledDown				= "UnScheduledDown";
	/**
	 * @uml.property  name="mac_NonScheduled"
	 */
	public String					Mac_NonScheduled				= "NonScheduled";

	/**
	 * @uml.property  name="mac_InitialE10State"
	 */
	public String					Mac_InitialE10State				= "StandBy";

	// OnLine State
	/**
	 * @uml.property  name="Mac_OnLine"
	 */
	public String					Mac_OnLine						= "OnLine";

	/**
	 * @uml.property  name="mac_OffLine"
	 */
	public String					Mac_OffLine						= "OffLine";

	/**
	 * @uml.property  name="mac_OnLineLocal"
	 */
	public String					Mac_OnLineLocal					= "OnLineLocal";
	/**
	 * @uml.property  name="mac_OnLineRemote"
	 */
	public String					Mac_OnLineRemote				= "OnLineRemote";

	/**
	 * @uml.property  name="mac_OnHold"
	 */
	public String					Mac_OnHold						= "Y";
	/**
	 * @uml.property  name="mac_NotOnHold"
	 */
	public String					Mac_NotOnHold					= "N";

	/**
	 * @uml.property  name="mac_InitialCommunicationState"
	 */
	public String					Mac_InitialCommunicationState	= "OffLine";

	//Machin unit type
	public String					Mac_PhotoUnit					= "Photo";
	public String					Mac_StockUnit    				= "Buffer";
	public String					Mac_RackUnit					= "Rack";
	public String					Mac_StageUnit					= "Stage";
	public String					Mac_ExposeUnit					= "Exposure";
	public String					Mac_CleanUnit					= "Cleaner";
	public String					Mac_RobotUnit					= "Robot";
	public String					Mac_IndexUnit					= "Index";
	public String					Mac_ReadUnit					= "BCRReader";
	public String					Mac_ProcessUnit					= "Process";
	public String					Mac_InspectUnit					= "Inspection";
	public String					Mac_TransferUnit				= "Transport";

	//Type
	public String                   TYPE_PALLET                     = "Pallet";
	public String                   TYPE_PROCESSGROUP               = "ProcessGroup";
	public String                   TYPE_BOX                        = "Box";
	public String                   TYPE_LOT 						= "Lot";
	public String                   TYPE_PANEL 						= "Panel";
	public String                   TYPE_GLASS 						= "Glass";
	public String                   TYPE_PPBOX	                    = "PPBox";

	// Port Access Mode
	/**
	 * @uml.property  name="port_Manual"
	 */
	public String					Port_Manual						= "Manual";
	/**
	 * @uml.property  name="port_Auto"
	 */
	public String					Port_Auto						= "Auto";

	/**
	 * @uml.property  name="port_InitialAccessMode"
	 */
	public String					Port_InitialAccessMode			= "Auto";


	public String					Port_UP				= "UP";

	public String					Port_DOWN				= "DOWN";

	// Port Transfer State
	/**
	 * @uml.property  name="port_ReadyToLoad"
	 */
	public String					Port_ReadyToLoad				= "ReadyToLoad";
	/**
	 * @uml.property  name="port_ReservedToLoad"
	 */
	public String					Port_ReservedToLoad				= "ReservedToLoad";
	/**
	 * @uml.property  name="port_ReadyToProcess"
	 */
	public String					Port_ReadyToProcess				= "ReadyToProcess";
	/**
	 * @uml.property  name="port_Processing"
	 */
	public String					Port_Processing					= "Processing";
	/**
	 * @uml.property  name="port_ReadyToUnload"
	 */
	public String					Port_ReadyToUnload				= "ReadyToUnload";
	/**
	 * @uml.property  name="port_ReservedToUnload"
	 */
	public String					Port_ReservedToUnload			= "ReservedToUnload";

	/**
	 * @uml.property  name="port_InitialTransferState"
	 */
	public String					Port_InitialTransferState		= "ReadyToLoad";

	// PortStateName
	/**
	 * @uml.property  name="Port_EMPTY"
	 */
	public String					Port_EMPTY						= "EMPTY";

	/**
	 * @uml.property  name="Port_FULL"
	 */
	public String					Port_FULL						= "FULL";

	/*
	 * Maximum
	 */
	/**
	 * @uml.property  name="max_Priority"
	 */
	public long						Max_Priority					= 1000;
	/**
	 * @uml.property  name="max_ConsumableQuantity"
	 */
	public long						Max_ConsumableQuantity			= 1000000;
	/**
	 * @uml.property  name="max_DurableTimeUsed"
	 */
	public double					Max_DurableTimeUsed				= 1000000;
	/**
	 * @uml.property  name="max_DurableDurationUsed"
	 */
	public double					Max_DurableDurationUsed			= 1000000;
	/**
	 * @uml.property  name="max_CarrierCapacity"
	 */
	public long						Max_CarrierCapacity				= 1000;
	/**
	 * @uml.property  name="max_ProductPosition"
	 */
	public long						Max_ProductPosition				= 1000;
	/**
	 * @uml.property  name="max_ProductQuantity"
	 */
	public double					Max_ProductQuantity				= 1000;
	/**
	 * @uml.property  name="max_SubProductUnitQuantity"
	 */
	public double					Max_SubProductUnitQuantity		= 1000;
	/**
	 * @uml.property  name="max_SubProductQuantity"
	 */
	public double					Max_SubProductQuantity			= 1000;
	/**
	 * @uml.property  name="max_MaterialQuantity"
	 */
	public long						Max_MaterialQuantity;

	// SubjectName
	/**
	 * @uml.property  name="subject_PEX"
	 */
	public String                   Subject_PEX						= "PEX";
	/**
	 * @uml.property  name="subject_CNX"
	 */
	public String					Subject_CNX						= "CNX";
	/**
	 * @uml.property  name="subject_MCS"
	 */
	public String					Subject_MCS						= "MCS";
	/**
	 * @uml.property  name="subject_MCS"
	 */
	public String					Subject_RTD						= "RTD";
	/**
	 * @uml.property  name="subject_TEX"
	 */
	public String					Subject_TEX						= "TEX";
	/**
	 * @uml.property  name="subject_FGX"
	 */
	public String					Subject_FGX						= "FGX";
	/**
	 * @uml.property  name="subject_FGM"
	 */
	public String					Subject_FGM						= "FGM";
	/**
     * @uml.property  name="Subject_SPC"
     */
    public String                   Subject_SPC                     = "SPC";
    
	//SYSTEM
	public String					DEFAULT_ACTIVE_VERSION						= "00001";
	public String					DEFAULT_FACTORY								= "ARRAY";

	//EVENT NAME
	public String                   EVENTNAME_REWORK                            = "Rework";
	public String                   EVENTNAME_STARTREWORK                       = "StartRework";

	//Production Type
	public String					Pord_Production								= "P";
	public String					Pord_Engineering							= "E";
	public String					Pord_MQC									= "D";
	
	//ProcessOperationType 
	public String					Pos_Production								= "Production";
	public String					Pos_Inspection								= "Inspection";
	public String                   Pos_Sorter                                  = "Sorter";

	//System common
	public String					Flag_Y										= "Y";
	public String					Flag_N										= "N";

	// Flag Y or N
	public String                   FLAG_Y                               	  = "Y";
	public String                   FLAG_N                               	  = "N";
	public String                   FLAG_Z                               	  = "Z";

	public String					FLAG_HOLD								  = "HOLD";

	// Durable Hold Info
	public String					DURABLE_HOLDSTATE_Y				  		  = "Y";
	public String					DURABLE_HOLDSTATE_N				  		  = "N";

	// Machine Group
	public String                   MachineGroup_CST_CLEANER			       = "CST CLEANER";
	public String                   MachineGroup_OLB					       = "OLB";

	// PortType
	public String                   PORT_TYPE_PB                       		  = "PB";
	public String                   PORT_TYPE_PL                    		  = "PL";
	public String					PORT_TYPE_PU							  = "PU";
	public String                   PORT_TYPE_PP                              = "PP";

	// SlotMap
	public String                   PRODUCT_IN_SLOT                           = "O";
	public String                   PRODUCT_NOT_IN_SLOT                       = "X";

	// SlotMapEDO
	public String                   E_PRODUCT_IN_SLOT                           = "Y";
	public String                   E_PRODUCT_NOT_IN_SLOT                       = "N";

	//Grade
	public String                   LotGrade_G                                  = "G";
	public String                   LotGrade_P                                  = "P";
	public String                   LotGrade_R                                  = "R";
	public String                   LotGrade_S                                  = "S";

	public String                   ProductGrade_G                              = "G";
	public String                   ProductGrade_P                              = "P";
	public String                   ProductGrade_R                              = "R";
	public String                   ProductGrade_S                              = "S";
	public String                   ProductGrade_N                              = "N";

	public String					GradeType_Lot								= "Lot";
	public String					GradeType_Product							= "Product";
	public String					GradeType_SubProduct						= "SubProduct";

	public String					SORT_JOBSTATE_CREATE     					  = "CREATE";
	public String                   SORT_JOBSTATE_WAIT                            = "WAIT";
	public String					SORT_JOBSTATE_CONFIRMED     				  = "CONFIRMED";
	public String					SORT_JOBSTATE_STARTED     					  = "STARTED";
	public String					SORT_JOBSTATE_CANCELED     					  = "CANCELED";
	public String					SORT_JOBSTATE_ENDED     					  = "ENDED";
	public String					SORT_JOBSTATE_ABORT     					  = "ABORT";



	public String					SORT_JOBTYPE_SPLIT     					= "Split";
	public String					SORT_JOBTYPE_MERGE     					= "Merge";
	public String					SORT_JOBTYPE_SCRAP     					= "Scrap";
	public String					SORT_JOBTYPE_SCRAPFORAFTER     			= "ScrapForAfter";
	public String					SORT_JOBTYPE_TURNOVER     				= "TurnOver";
	public String					SORT_JOBTYPE_TURNSIDE     				= "TurnSide";
	public String					SORT_JOBTYPE_SLOTMAPCHANGE   			= "SlotMapChange";
	public String                   SORT_JOBTYPE_CHANGE                     = "Change";
	public String                   SORT_JOBTYPE_OUTSTAGE                   = "OutStage";

	public String					SORT_SORTPRODUCTSTATE_READY     			= "READY";
	public String					SORT_SORTPRODUCTSTATE_EXECUTING     		= "EXECUTING";
	public String					SORT_SORTPRODUCTSTATE_COMPLETED    			= "COMPLETED";

	public String					SORT_TRANSFERDIRECTION_SOURCE    			= "SOURCE";
	public String					SORT_TRANSFERDIRECTION_TARGET    			= "TARGET";
	public String					SORT_TRANSFERDIRECTION_BOTH    				= "BOTH";
	public String					SORT_TRANSFERDIRECTION_ALONE    			= "ALONE";

	// MCS
	//[ OIC | RTD | MCS ]
	// CEC GVO Add
	// Modified by smkang on 2019.03.14 - EDO users called OIC as OPI.
//	public String					MCS_TRANSPORTJOBTYPE_OIC               = "OIC";
	public String					MCS_TRANSPORTJOBTYPE_OIC               = "OPI";
		
	public String					MCS_TRANSPORTJOBTYPE_RTD               = "RTD";
	public String					MCS_TRANSPORTJOBTYPE_MCS               = "MCS";
	// Added by smkang on 2018.05.06 - If TransportJobName starts with 'M', it would be supposed manual transport.
	public String					MCS_TRANSPORTJOBTYPE_EQP               = "EQP";

//	[ Requested | Accepted | Rejected | Started | Completed | Terminated ]

	public String					MCS_JOBSTATE_Requested           = "Requested";
	public String					MCS_JOBSTATE_Accepted            = "Accepted";
	public String					MCS_JOBSTATE_Rejected            = "Rejected";
	public String					MCS_JOBSTATE_Started             = "Started";
	public String					MCS_JOBSTATE_Completed           = "Completed";
	public String					MCS_JOBSTATE_Terminated          = "Terminated";
	//public String					MCS_JOBSTATE_REPLIED             = "REPLIED";


    //	JOBCANCELSTATE [ Requested | Accepted | Rejected | Started | Completed | Failed ]
	public String					MCS_CANCELSTATE_Requested          	= "Requested";
	public String					MCS_CANCELSTATE_Accepted         	= "Accepted";
	public String					MCS_CANCELSTATE_Rejected         	= "Rejected";
	public String					MCS_CANCELSTATE_Started          	= "Started";
	public String					MCS_CANCELSTATE_Completed           = "Completed";
	public String					MCS_CANCELSTATE_Failed          	= "Failed";
	//public String					MCS_CANCELSTATE_REPLIED          	= "REPLIED";

	//	[ Requested | Accepted | Rejected | Changed | Failed ]
	public String					MCS_CHANGESTATE_Requested          	= "Requested";
	public String					MCS_CHANGESTATE_Accepted          	= "Accepted";
	public String					MCS_CHANGESTATE_Rejected          	= "Rejected";
	public String					MCS_CHANGESTATE_Changed          	= "Changed";
	public String					MCS_CHANGESTATE_Failed          	= "Failed";
	//public String					MCS_CHANGESTATE_REPLIED          	= "REPLIED";

	public String					MCS_TRANSPORTJOBEVENT_REQUESTTRANSPORTJOBREQUEST  = "RequestTransportJobRequest";
	public String					MCS_TRANSPORTJOBEVENT_REQUESTTRANSPORTJOBREPLY    = "RequestTransportJobReply";
	public String					MCS_TRANSPORTJOBEVENT_CANCELTRANSPORTJOBREQUEST   = "CancelTransportJobRequest";
	public String					MCS_TRANSPORTJOBEVENT_CANCELTRANSPORTJOBREPLY     = "CancelTransportJobReply";
	public String					MCS_TRANSPORTJOBEVENT_TRANSPORTJOBSTARTED         = "TransportJobStarted";
	public String					MCS_TRANSPORTJOBEVENT_TRANSPORTJOBSTARTEDBYMCS    = "TransportJobStartedByMCS";
	public String					MCS_TRANSPORTJOBEVENT_TRANSPORTJOBTERMINATEDBYMCS = "TransportJobTerminatedByMCS";
	public String					MCS_TRANSPORTJOBEVENT_TRANSPORTJOBCOMPLETED       = "TransportJobCompleted";
	public String					MCS_TRANSPORTJOBEVENT_ACTIVETRANSPORTJOB       	  = "ActiveTransportJobReport";

	public String					MCS_TRANSPORTJOBEVENT_TRANSPORTJOBCANCELSTARTED   = "TransportJobCancelStarted";
	public String					MCS_TRANSPORTJOBEVENT_TRANSPORTJOBCANCELCOMPLETED = "TransportJobCancelCompleted";
	public String					MCS_TRANSPORTJOBEVENT_TRANSPORTJOBCANCELFAILED    = "TransportJobCancelFailed";

	public String					MCS_TRANSPORTJOBEVENT_CHANGEDESTINATIONREQUEST    = "ChangeDestinationRequest";
	public String					MCS_TRANSPORTJOBEVENT_CHANGEDESTINATIONREPLY      = "ChangeDestinationReply";
	public String					MCS_TRANSPORTJOBEVENT_DESTINATIONCHANGED          = "DestinationChanged";
	public String					MCS_TRANSPORTJOBEVENT_DESTINATIONCHANGEFAILED     = "DestinationChangeFailed";
	public String					MCS_TRANSPORTJOBEVENT_CARRIERLOACATIONCHANGED     = "CarrierLocationChanged";

	public String					MCS_TRANSFERSTATE_MOVING     					  = "MOVING";
	public String					MCS_TRANSFERSTATE_INSTK     					  = "INSTK";
	public String					MCS_TRANSFERSTATE_ONEQP     					  = "ONEQP";
	public String					MCS_TRANSFERSTATE_OUTEQP     					  = "OUTEQP";
	public String					MCS_TRANSFERSTATE_OUTSTK     					  = "OUTSTK";
	public String					MCS_TRANSFERSTATE_INPORT     					  = "INPORT";
	public String					MCS_TRANSFERSTATE_RESERVED     					  = "RESERVED";
	public String					MCS_TRANSFERSTATE_INLINE						  = "INLINE";
	public String					MCS_TRANSFERSTATE_INBUFFER						  = "INBUFFER";
	public String					MCS_TRANSFERSTATE_INSTAGE						  = "INSTAGE";
	public String					MCS_TRANSFERSTATE_PROCESSING					  = "PROCESSING";
	public String					MCS_TRANSFERSTATE_STORED						  = "STORED";

	public String                   MCS_CARRIERSTATE_EMPTY                       	  = "EMPTY";
	public String                   MCS_CARRIERSTATE_FULL                    	  	  = "FULL";
	public String                   MCS_POSITIONTYPE_PORT                        	  = "PORT";
	public String                   MCS_POSITIONTYPE_SHELF                        	  = "SHELF";
	// Added by smkang on 2018.04.04 - PositionType of crane.
	public String                   MCS_POSITIONTYPE_CRANE                        	  = "CRANE";
	// Added by smkang on 2018.04.29 - PositionType of vehicle.
	public String                   MCS_POSITIONTYPE_VEHICLE                       	  = "VEHICLE";

	//CutType
	public String                   CUTTYPE_HALF                       	  			= "Half";
	public String                   CUTTYPE_QUARTER                       	  		= "Quarter";
	
	//Q-Time
	public String					QTIME_STATE_IN									= "Entered";
	public String					QTIME_STATE_OUT									= "Exited";
	public String					QTIME_STATE_WARN								= "Warning";
	public String					QTIME_STATE_OVER								= "Interlocked";
	public String					QTIME_STATE_CONFIRM								= "Resolved";
	public String					QTIME_DEPART									= "TrackOut";
	public String					QTIME_TARGET									= "TrackIn";
	public String					QTIME_ACTION_HOLD								= "Hold";
	public String					QTIME_ACTION_REWORK								= "Rework";

	//EVAMask
	public String					EVAMask											= "EVAMask";

	//EVAMask workflow
	public String					WORKFLOW_AOIAndPPA 								= "AOIAndPPA";
	public String					WORKFLOW_AOI									= "AOI";
	public String					WORKFLOW_PPA									= "PPA";
	public String					WORKFLOW_AllSkip								= "AllSkip";

	//EVAMask Inspect State
	public String					INSPECTSTATE_Inspected							= "Inspected";
	public String					INSPECTSTATE_AOIWait							= "AOIWait";
	public String					INSPECTSTATE_PPAWait							= "PPAWait";

	//141103 by swcho : Reservation
	public String					RESV_STATE_RESV									= "Reserved";
	public String					RESV_STATE_START								= "Started";
	public String					RESV_STATE_END									= "Completed";

	//150805 by xzquan : DetailProcessOperationType
	public String					DETAIL_PROCESSOPERATION_TYPE_SHIP	 		    = "SHIP";
	public String					DETAIL_PROCESSOPERATION_TYPE_PACK	 			= "PACK";
	public String                   DETAIL_PROCESSOPERATION_TYPE_SORT               = "Sorter";
	/* 20181015, hhlee, add Dummy Operation type ==> */
	public String                   DETAIL_PROCESSOPERATION_TYPE_DUMMY              = "DUMMY";
	/* 20201006, mgkang, add AGING Operation type ==> */
	public String                   DETAIL_PROCESSOPERATION_TYPE_AGING              = "AGING";
	/* 20201006, mgkang, add Consumable type ==> */
	public String                   CONSUMABLE_TYPE_POL           					= "POL";
	/* <<== 20181015, hhlee, add Dummy Operation type */
	
	/* 20190320, hhlee, add Repair Operation type ==>> */
    public String                   DETAIL_PROCESSOPERATION_TYPE_REP                = "REP";
    /* <<== 20190320, hhlee, add Repair Operation type */

	//Insert Message Log Flag
	public String					INSERT_LOG_NONE	 			                	= "NONE";
	public String					INSERT_LOG_MESSAGE	 		            		= "MSG";
	public String					INSERT_LOG_ERROR	 		            		= "ERR";
	public String					INSERT_LOG_TRANSATION	 		        		= "TRX";

	public String					INSERT_LOG_TYPE_RECEIVE	 		        		= "RECV";
	public String					INSERT_LOG_TYPE_SEND	 		        		= "SEND";

	//FGMS Location State
	public String					FGMS_LOCATION_FULL	 			                = "Full";
	public String					FGMS_LOCATION_EMPTY	 			                = "Empty";
	public String					FGMS_STOCKSTATE_STOCKED							= "Stocked";
	public String					FGMS_STOCKSTATE_NOTSTOCKED						= "NotStocked";

	//FGMS Resource State
	public String					FGMS_RESOURCE_INSERVICE							= "InService";
	public String					FGMS_RESOURCE_NOTINSERVICE						= "NotInService";

	//FGMS Aea Type
	public String					FGMS_AREATYPE_STORAGEAREA						= "StorageArea";
	public String					FGMS_AREATYPE_PRODUCTIONAREA					= "ProductionArea";

	//FGMS Domestic Export
	public String					FGMS_DOMESTICEXPORT_DOMESTIC					= "D";
	public String					FGMS_DOMESTICEXPORT_EXPORT						= "E";

	//MOD LEEHYEON WOO
	// Code
	public String                   CODE_CHECKINCODENORMAL          = "M01";
	public String                   CODE_REVISIONCODENORMAL         = "R01";

	//Flow
	public String                   FLOW_MODULEBANK                 = "MMB";
	public String                   FLOW_NORMAL                     = "MMP";
	public String                   FLOW_REWORKINWAIT               = "MRI";
	public String                   FLOW_FA                         = "MFA";
	public String                   FLOW_REWORK                     = "MRW";
	public String                   FLOW_REWORKOUTWAIT              = "MRO";
	public String                   FLOW_OQA                        = "MOQ";
	public String                   FLOW_RT                         = "MRT";
	public String                   FLOW_RMA                        = "MRM";
	public String                   FLOW_NG_PALLET                  = "MNP";
	public String                   FLOW_BA_RT                      = "MBR";
	public String                   FLOW_NG_RT                      = "MNR";
	public String                   FLOW_QA                         = "MQA";

	//Normal Step
	public String                   STEP_MODULEBANK                 = "M000";
	public String                   STEP_POL_CLEANER                = "M010";
	public String                   STEP_POL_ATTACH                 = "M020";
	public String                   STEP_AUTOCLAVE                  = "M030";
	public String                   STEP_TCP              		    = "M040";
	public String                   STEP_PCB                        = "M050";
	public String                   STEP_MMT                        = "M060";
	public String                   STEP_ASSEMBLE                   = "M100";
	public String                   STEP_INPUT_TEST                 = "M120";
	public String                   STEP_NG_Pallet                  = "M123";
	public String                   STEP_AGING                      = "M140";
	public String                   STEP_AGING_TEST                 = "M141";
	public String                   STEP_BT                         = "M142";
	public String                   STEP_FINAL_TEST                 = "M151";
	public String                   STEP_APP_TEST                   = "M160";
	public String                   STEP_INLINE_RETEST              = "M200";
	public String                   STEP_BATCH_RETEST               = "M210";
	public String                   STEP_NG_RETEST                  = "M220";
	public String                   STEP_OQA                        = "M170";
	public String                   STEP_LINE_OUT                   = "M180";
	public String                   STEP_BOXING                     = "M300";
	public String                   STEP_PALLETIZING                = "M500";
	public String                   STEP_FGMS                       = "M550";
	public String                   STEP_REWORK_INWAIT              = "M700";

	// Type
//	public String                   TYPE_LOT                        = "Lot";
//	public String                   TYPE_PANEL                      = "Panel";
	public String                   TYPE_MODULE                     = "MODULE";
//	public String                   TYPE_BOX                        = "Box";
//	public String                   TYPE_PALLET                     = "Pallet";
	public String                   TYPE_OQALOT                     = "OQALOT";
	public String                   TYPE_PRODUCT                    = "Product";
//	public String                   TYPE_PROCESSGROUP               = "ProcessGroup";

	//Pilot
	public String					Pilot_ReasonCode_Generic		= "FirstGlass";

	//LotAction
	public String					ACTIONSTATE_CREATED				= "Created";
	public String 					ACTIONSTATE_EXECUTED			= "Executed";
	public String 					ACTIONSTATE_CANCELED			= "Canceled";
	public String 					HOLDTYPE_AHOLD					= "AHOLD";
	public String 					HOLDTYPE_BHOLD					= "BHOLD";
	public String 					ACTIONNAME_HOLD					= "Hold";
	public String 					ACTIONNAME_CHANGE				= "ChangeProduct";
	public String 					ACTIONNAME_SYSTEMHOLD			= "SystemHold";

	//ReturnMessage_Body Requested by EDO
	public String                   RETURN_BODY_RESULT              = "RESULT";
	public String                   RETURN_BODY_RESULT_DESCRIPTION  = "RESULTDESCRIPTION";

	//Product Processing Info Flag
	public String                   PRODUCT_PROCESSINGINFO_E        ="E";
	public String                   PRODUCT_PROCESSINGINFO_P        ="P";
	public String                   PRODUCT_PROCESSINGINFO_W        ="W";
	public String                   PRODUCT_PROCESSINGINFO_S        ="S";
	public String                   PRODUCT_PROCESSINGINFO_F        ="F";
	public String                   PRODUCT_PROCESSINGINFO_R        ="R";

	//PhotoMask Location
	public String 					PHTMASKLOCATION_INLIB			="InLib";
	public String 					PHTMASKLOCATION_MOVING			="Moving";
	public String 					PHTMASKLOCATION_ONSTAGE			="OnStage";

	//Operation Mode
	public String                   OPERATIONMODE_UNPACKING         = "UNPACKING";
	public String                   OPERATIONMODE_SORTING           = "SORTING";
	public String                   OPERATIONMODE_CLEANING          = "CLEANING";
	public String                   OPERATIONMODE_REWORK            = "REWORK";
	public String                   OPERATIONMODE_NORMAL            = "NORMAL";
	public String                   OPERATIONMODE_PVD               = "PVD";
	public String                   OPERATIONMODE_CVD               = "CVD";
	public String                   OPERATIONMODE_INDP              = "INDP";
	public String                   OPERATIONMODE_OTME              = "OTME";

	// Machine ConstructType
	public String					ConstructType_InitialCleaner			= "ICL";
	public String					ConstructType_Box_Packer				= "BPK";
	public String					ConstructType_Pallet_Packer				= "PPK";
	// Modified by smkang on 2018.11.28 - ConstructType of cassette cleaner is changed to CCLN.
	public String					ConstructType_CassetteClenaer			= "CCLN";
	public String					ConstructType_Exposure					= "EXP";
	public String					ConstructType_CFInline					= "CFINLINE";
	public String					ConstructType_Sort						= "SORT";
	public String					ConstructType_PIPrint					= "PIL";
	public String					ConstructType_UNPK					    = "UNPK";
	public String					ConstructType_PHOTO					    = "TRACK";
	public String					ConstructType_PHOTOMASKSTOCKER			= "MSTK";

	//Photo(TRACK) Machine PRMaterial
	public String					PRMaterial_Normal					    = "Normal_PR";
	public String					PRMaterial_Organic					    = "Organic_PR";
	public String					PRMaterial_Relation					    = "PRMaterial";
	
	// UNIT ConstructType
	/**
     * @uml.property  name="ConstructType_TRACK_SCNN"
     */
    public String                   ConstructType_TRACK_SCNN             = "SCNN";
    /**
     * @uml.property  name="ConstructType_TRACK_EGEY"
     */
    public String                   ConstructType_TRACK_EGEY             = "EGEY";
    /**
     * @uml.property  name="ConstructType_TRACK_TRKD"
     */
    public String                   ConstructType_TRACK_TRKD             = "TRKD";

	//DurableType
	public String                   DURABLETYPE_PBOBECARD           = "ProbeCard";
	public String                   DURABLETYPE_PHOTOMASK           = "PhotoMask";

	//Lot Reservation
    public String                   RESV_LOT_STATE_RESV                                 = "Reserved";
    public String                   RESV_LOT_STATE_START                                = "Started";
    public String                   RESV_LOT_STATE_END                                  = "Completed";

    //Product Flag
    /**
     * @uml.property  name="PRODUCTFLAGDOWNLOAD"
     */
    public String                   PRODUCTFLAGDOWNLOAD             = "ProductFlagDownload";
    /**
     * @uml.property  name="PRODUCTFLAGDOWNUPLOAD"
     */
    public String                   PRODUCTFLAGUPLOAD               = "ProductFlagUpload";
    /**
     * @uml.property  name="PRODUCTFLAGMANUAL"
     */
    public String                   PRODUCTFLAGMANUAL               = "ProductFlagManual";
    /**
     * @uml.property  name="PRODUCTFLAGDATASTATE_UPDATE"
     */
    public String                   PRODUCTFLAGDATASTATE_UPDATE     = "FLAGUPDATE";
    /**
     * @uml.property  name="PRODUCTFLAGDATASTATE_INSERT"
     */
    public String                   PRODUCTFLAGDATASTATE_INSERT     = "FLAGINSERT";

    //Recipe State
    /**
     * @uml.property  name="RECIPE_STATE_CREATED"
     */
    public String                   RECIPE_STATE_CREATED            = "Created";
    /**
     * @uml.property  name="RECIPE_STATE_APPROVED"
     */
    public String                   RECIPE_STATE_APPROVED           = "Approved";
    /**
     * @uml.property  name="RECIPE_STATE_UNAPPROVED"
     */
    public String                   RECIPE_STATE_UNAPPROVED         = "UnApproved";
    /**
     * @uml.property  name="RECIPE_STATE_DELETED"
     */
    public String                   RECIPE_STATE_REMOVED            = "Removed";

    //Recipe ValidationType
    /**
     * @uml.property  name="RECIPE_VALIDATIONTYPE_NONE"
     */
    public String                   RECIPE_VALIDATIONTYPE_NONE      = "None";
    /**
     * @uml.property  name="RECIPE_VALIDATIONTYPE_EXACT"
     */
    public String                   RECIPE_VALIDATIONTYPE_EXACT     = "Exact";
    /**
     * @uml.property  name="RECIPE_STATE_UNAPPROVED"
     */
    public String                   RECIPE_VALIDATIONTYPE_RANGE     = "Range";

    //Recipe Type
    /**
     * @uml.property  name="RECIPE_TYPE_MAIN"
     */
    public String                   RECIPE_TYPE_MAIN                = "MAIN";
    /**
     * @uml.property  name="RECIPE_TYPE_UNIT"
     */
    public String                   RECIPE_TYPE_UNIT                = "UNIT";
    /**
     * @uml.property  name="RECIPE_TYPE_SUBUNIT"
     */
    public String                   RECIPE_TYPE_SUBUNIT             = "SUBUNIT";

    //add by wghuang 20190513
    //MachineType
    public String                   MACHINE_MACHINETYPE_TransportMachine      = "TransportMachine";
    public String                   MACHINE_MACHINETYPE_ProductionMachine     = "ProductionMachine";
    public String                   MACHINE_MACHINETYPE_StorageMachine        = "StorageMachine";
    
    //MachineState
    public String                   MACHINE_STATE_RUN                    = "RUN";
    public String                   MACHINE_STATE_IDLE                   = "IDLE";
    public String                   MACHINE_STATE_DOWN                   = "DOWN";
    
    // Added by smkang on 2018.10.07 - MQC, PM and NONSCHEDULEDTIME states are added.
    public String                   MACHINE_STATE_MQC					 = "MQC";
    public String                   MACHINE_STATE_PM					 = "PM";
    public String                   MACHINE_STATE_NONSCHEDULEDTIME		 = "NONSCHEDULEDTIME";

    //PortAccessMode
    public String                   PORT_ACCESSMODE_MANUAL               ="Manual";
    public String                   PORT_ACCESSMODE_AUTO                 ="Auto";

    //ReasonCode Type
    public String					REASONCODETYPE_ALARM					= "Alarm";
    public String 					REASONCODETYPE_MQCCHANGE				= "MqcChange";
    public String 					REASONCODETYPE_SCRAPGLASS				= "ScrapGlass";
    public String 					REASONCODETYPE_SCRAPCST					= "ScrapCST";
    public String 					REASONCODETYPE_HOLDCST					= "HoldCST";
    public String 					REASONCODETYPE_CHANGEMACHINESTATE		= "ChangeMachineState";
    public String 					REASONCODETYPE_CHANGECOMSTATE			= "ChangeCOMState";
    public String 					REASONCODETYPE_CHANGEPORTSTATE			= "ChangePortState";
    public String 					REASONCODETYPE_CHANGEPORTTRANSFERSTATE	= "ChangePortTransferState";
    public String 					REASONCODETYPE_SCRAPMASK				= "ScrapMask";
    public String 					REASONCODETYPE_HOLDMASK					= "HoldMask";
    public String 					REASONCODETYPE_HOLDMATERIAL				= "HoldMaterial";
    public String 					REASONCODETYPE_SCRAPMATERIAL			= "ScrapMaterial";
    public String 					REASONCODETYPE_DEFECTCODE				= "DefectCode";
    public String 					REASONCODETYPE_REWORK					= "Rework";
    public String 					REASONCODETYPE_TRANSPORT				= "Transport";
    public String 					REASONCODETYPE_SCRAPPROBECARD			= "ScrapProbeCard";
    public String 					REASONCODETYPE_HOLDLOT			= "HoldLot";

    //Process Flow Type
    /**
     * @uml.property  name="PROCESSFLOW_TYPE_MAIN"
     */
    public String                   PROCESSFLOW_TYPE_MAIN                = "MAIN";
    /**
     * @uml.property  name="PROCESSFLOW_TYPE_REWORK"
     */
    public String                   PROCESSFLOW_TYPE_REWORK              = "Rework";
    /**
     * @uml.property  name="PROCESSFLOW_TYPE_BRANCH"
     */
    public String                   PROCESSFLOW_TYPE_BRANCH              = "Branch";
    /**
     * @uml.property  name="PROCESSFLOW_TYPE_SAMPLING"
     */
    public String                   PROCESSFLOW_TYPE_SAMPLING            = "Sampling";
    /**
     * @uml.property  name="PROCESSFLOW_TYPE_SORT"
     */
    public String                   PROCESSFLOW_TYPE_SORT                = "Sort";
    
    /* 20190513, hhlee, add */
    /**
     * @uml.property  name="PROCESSFLOW_TYPE_MQC"
     */
    public String                   PROCESSFLOW_TYPE_MQC                 = "MQC";
    
    // PhotoTrack Flag 
  	public String					PHOTO_TRCK 			                 = "TRCK";
  	public String					PHOTO_DRYF	 		            	 = "DRYF";
  	public String					PHOTO_STRP	 		            	 = "STRP";

  	// Machine Area
  	/**
     * @uml.property  name="MACHINE_AREA_ETCH"
     */
  	public String                    MACHINE_AREA_ETCH                   = "ETCH";
  	public String                    MACHINE_AREA_PHOTO                  = "PHOTO";
  	
    // PRODUCT FLAG
    /**
     * @uml.property  name="PRODUCTFLAG_ELAQTIMEFLAG"
     */
    public String                    PRODUCTFLAG_ELAQTIMEFLAG            = "ELAQTIMEFLAG";
  	
    // SORTER JOB PROCESS TYPE
    public String                    SORT_JOB_PROCESS_TYPE_END           = "SortProcessEnd";
    public String                    SORT_JOB_PROCESS_TYPE_ABORT         = "SortProcessAbort";
	
    // Product Type
    /**
     * @uml.property  name="PRODUCTTYPE_GLASS"
     */
    public String                   PRODUCTTYPE_GLASS               = "Glass";
    /**
     * @uml.property  name="PRODUCTTYPE_CUT"
     */
    public String                   PRODUCTTYPE_CUT                 = "Cut";
    
    // New DSP
    public String                   DSPSTATUS_RESERVED              = "Reserved";
    public String                   DSPSTATUS_EXECUTING             = "Executing";
    public String                   DSPSTATUS_COMPLETED             = "Completed";
    
    public String                   STOCKERREGION_PULL              = "Pull";
    public String                   STOCKERREGION_PUSH              = "Push";
    public String                   STOCKERREGION_STOCK             = "Stock";
    
    //MACHINE ALARM STATE
    /**
     * @uml.property  name="ALARMSTATE_ISSUE"
     */
    public String                   ALARMSTATE_ISSUE   = "ISSUE";
    /**
     * @uml.property  name="ALARMSTATE_CLEAR"
     */
    public String                   ALARMSTATE_CLEAR   = "CLEAR";
    
    //SORT TRACKIN EVENTNAME
    /**
     * @uml.property  name="ALARMSTATE_ISSUE"
     */
    public String                   CHANGERIN_TRACKIN               = "ChangerInTrackIn";
    public String                   CHANGEROUT_TRACKIN              = "ChangerOutTrackIn";
    public String                   CHANGERINOUT_TRACKIN            = "ChangerInOutTrackIn";
    
    //Machine Type
    /**
     * @uml.property  name="MACHINETYPE_PRODUCTION"
     */
    public String                   MACHINETYPE_PRODUCTION          = "ProductionMachine";    
    
    //Virtual PhotoMask Name
    /**
     * @uml.property  name="VIRTUAL_PHOTOMASKNAME"
     */
    public String                   VIRTUAL_PHOTOMASKNAME          = "NONE";    
    
    /* 20190102, hhlee, add, SORTERJOBCARRIER TRACKFLAG */
    //Sorter Job Carrier Track Flag
    /**
     * @uml.property  name="SORTERJOBCARRIER_TRACKFLAG_IN"
     */
    public String                   SORT_JOBCARRIER_TRACKFLAG_IN  = "IN";
    /**
     * @uml.property  name="SORTERJOBCARRIER_TRACKFLAG_IN"
     */
    public String                   SORT_JOBCARRIER_TRACKFLAG_OUT = "OUT";
    
    /* 20190211, hhlee, add, INDEXERABNORMALSTATE */
    /**
     * @uml.property  name="INDEXER_ABNORMAL_STATE_PAUSE"
     */
    public String                   INDEXER_ABNORMAL_STATE_PAUSE  = "PAUSE";
    /**
     * @uml.property  name="INDEXER_ABNORMAL_STATE_NORMAL"
     */
    public String                   INDEXER_ABNORMAL_STATE_NORMAL = "NORMAL";
    
    /* 20190211, hhlee, add, MACHINE ALARM SEVERITY */
    /**
     * @uml.property  name="ALARMSTATE_LIGHT"
     */
    public String                   ALARMSTATE_LIGHT   = "LIGHT";
    /**
     * @uml.property  name="ALARMSTATE_SERIOUS"
     */
    public String                   ALARMSTATE_SERIOUS   = "SERIOUS";    
    /* 20190329, hhlee, add, PROCESSOPERATIONNAME BAR */
    /**
     * @uml.property  name="PROCESSOPERATIONNAME_BAR"
     */
    public String                   PROCESSOPERATIONNAME_BAR   = "-";
    
    /* 20190409, hhlee, add, REPLY RESULT OK */
    /**
     * @uml.property  name="RESULT_OK"
     */
    public String                   RESULT_OK   = "OK";
    /**
     * @uml.property  name="RESULT_NG"
     */
    public String                   RESULT_NG   = "NG";
    
    /* 20190410, hhlee, add, ERRORCODE UNDEFINECODE */
    /**
     * @uml.property  name="ERRORCODE_UNDEFINECODE"
     */
    public String                   ERRORCODE_UNDEFINECODE   = "UndefinedCode";
    
    /* 20190513, hhlee, add */
    /**
     * @uml.property  name="PRODUCTION_TYPE_MQCA"
     */
    public String                   PRODUCTION_TYPE_MQCA                 = "MQCA";    
    
    /*20200228,JHYING,ADD*/
    /**
     * @uml.property  name="PRODUCTION_TYPE_PROD"
     */
    public String                   PRODUCTION_TYPE_PROD                 = "PROD";    
    /**
     * @uml.property  name="RECIPE_PARAMETERNAME_PHOTOMASKNAME"
     */
    public String                   RECIPE_PARAMETERNAME_PHOTOMASKNAME  = "Mask Name";
    

	///**
	// * @uml.property name="classDefsMap"
	// */
	//private Map<String, String>	classDefsMap					= new HashMap<String, String>();


	public ConstantMap()
	{

	}

	public String getEventName(String eventNameConst){
		try {
			Field filed = ConstantMap.class.getDeclaredField(eventNameConst);
			filed.setAccessible(true);
			return filed.get(this).toString();
		} catch (SecurityException e) {
			return "";
		} catch (NoSuchFieldException e) {
			return "";
		} catch (IllegalArgumentException e) {
			return "";
		} catch (IllegalAccessException e) {
			return "";
		}
	}

	@Override
	public void load() {
		try {
			super.load();
			Field[] fields = ConstantMap.class.getDeclaredFields();
			for (Field field : fields) {
				setValue(constantDefsMap, field);
			}

			log.info("Load Custom Common ConstantMap successfully.");

			// scheduler reload action.
//			try {
//				MessageSchedulerServiceImpl messageScheduler = (MessageSchedulerServiceImpl) BundleUtil
//						.getServiceByBeanName("MessageSchedulerServiceImpl");
//
//				if (messageScheduler != null)
//					messageScheduler.reload();
//
//			} catch (Exception e) {
//				log.warn(e);
//			}

		} catch (Exception e) {
			log.error(e, e);

			ExceptionNotify.notifyException(e);
		}
	}
}
