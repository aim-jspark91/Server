package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.service.ConsumableServiceUtil;
import kr.co.aim.messolution.durable.service.DurableServiceImpl;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.NodeStack;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.generic.util.StringUtils;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.lot.service.LotInfoUtil;
import kr.co.aim.messolution.lot.service.LotServiceUtil;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.LotService;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.impl.LotServiceImpl;
import kr.co.aim.greentrack.lot.management.info.AssignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedOutInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.name.NameServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;

import org.apache.commons.collections.map.ListOrderedMap;
import org.jdom.Document;
import org.jdom.Element;

import com.sun.org.apache.bcel.internal.generic.RETURN;

public class ModuleTrackOutLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOut", getEventUser(), getEventComment(), "", "");
		
		Element body = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
		Element productList = body.getChild( "PRODUCTLIST" );
		
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String carrierType = SMessageUtil.getBodyItemValue(doc, "CARRIERTYPE", true);
		String lotGrade = SMessageUtil.getBodyItemValue(doc, "LOTGRADE", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String transactionId = SMessageUtil.getHeaderItemValue(doc, "TRANSACTIONID", true);
		String agingFlag = GenericServiceProxy.getConstantMap().FLAG_N;
		String applyBatchFunctionFlag = GenericServiceProxy.getConstantMap().FLAG_N;
				
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		String srcDurableName = lotData.getCarrierName();
		
		ProcessOperationSpec processData = CommonUtil.getProcessOperationSpec(factoryName, lotData.getProcessOperationName());
		String detailProcessOperationType = processData.getDetailProcessOperationType();
		
		if(StringUtil.equals(detailProcessOperationType, GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_AGING))
			agingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
		
		//ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, productSpecName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
		//String processFlowName = productSpecData.getProcessFlowName();
		//String processFlowVersion = productSpecData.getProcessFlowVersion();
		
		List<String> lotList = LotServiceUtil.getDistinctLotNameByProductList( body, carrierName );
		
		// Validation - LotState & LotHoldState
		CommonValidation.checkLotStateAndLotHoldStateByLotList( lotList );
		
		// Validation - ProductHoldState
		List<String> productNameList = CommonUtil.makeList( body, "PRODUCTLIST", "PRODUCTNAME" );
		CommonValidation.checkProductHoldState( productNameList );
		
		// Validation - DurableHoldState
		Durable durableData = CommonUtil.getDurableInfo(carrierName);
		LotServiceUtil.checkDurableHoldState( durableData );
		
		// Validation for Multiple LotProductionType
		CommonValidation.checkDistinctProductionType( lotList );

		// Validation - Check ProductionType
		CommonValidation.checkSameProductionType( productNameList );

		// Get CellTestLotName
		Machine machineData = CommonUtil.getMachineInfo( machineName );
		
		Port portData = new Port();
				
		String mesPortType = "";
		
		if ( !portName.isEmpty() )
		{
			portData = CommonUtil.getPortInfo( machineName, portName );
			mesPortType=portData.getUdfs().get( "PORTTYPE" ).toString();
		}
		
		if ( lotList.size() == 1 )
		{
			// 1. Report List Lot Information Qty different = > Create new Lot
			// after TrackOut
			// 2. Report List Lot Information Quy equal = > TrackOut

			// 1. Report List and Lot Information Qty differrnt
			if ( lotData.getProductQuantity() != productList.getChildren().size() )
			{
				// CreateNewLot
				String splitLotName = generateSplitLotName( lotName );
				eventInfo.setEventName( "CreateNewLot" );
				lotData = MESLotServiceProxy.getLotServiceUtil().createWithParentLot(eventInfo, splitLotName, lotData, lotData.getCarrierName(), false,
						new HashMap<String, String>(), lotData.getUdfs());

				List<String> notAssignedCSTLotList = LotServiceUtil.getDistinctLotNameByProductListExceptCarrier( body, carrierName );
				
				for ( int i = 0; i < notAssignedCSTLotList.size(); i++ )
				{
					String sourceLotName = notAssignedCSTLotList.get( i ).toString();
					Lot sourceLotData1 = CommonUtil.getLotInfoByLotName( sourceLotName );

					// Load Port ProductPSequence
					List<ProductP> productPSequence1 = LotServiceUtil.setProductPSequence_Module( body, sourceLotName );

					// TransferToLot
					eventInfo.setEventName( "TransferProductToLot" );
					TransferProductsToLot( eventInfo, sourceLotData1, splitLotName, productPSequence1, String.valueOf( productPSequence1.size() ) );

				}

				// Ready to TrackOut Change lotData
				lotData = CommonUtil.getLotInfoByLotName( splitLotName );
			}
			else
			{
//				// 2. Report List and Lot Information is Qty equal = > TrackOut
//				Lot lotDataByCarrier = new Lot();
//				try
//				{
//					lotDataByCarrier = CommonUtil.getLotInfoBydurableName( carrierName );
//				}
//				catch ( Exception e )
//				{
//					lotDataByCarrier = null;
//				}
//
//				if ( lotDataByCarrier != null )
//				{
//					String lotIdByCarrier = lotDataByCarrier.getKey().getLotName();
//					String lotIdRule = CommonUtil.getPrefix( lotIdByCarrier, "-" );
//
//					if ( StringUtils.isNotEmpty( lotIdRule ) )
//					{
//						
//					}
//				}
//
//				if ( lotDataByCarrier == null )
//				{
//					// Create New Lot
//
//					// TransferToLot
//					eventInfo.setEventName( "TransferProductToLot" );
//
//					List<String> notAssignedCSTLotList = LotServiceUtil.getDistinctLotNameByProductListExceptCarrier( body, carrierName );
//
//					for ( int i = 0; i < notAssignedCSTLotList.size(); i++ )
//					{
//						String sourceLotName = notAssignedCSTLotList.get( i ).toString();
//						Lot sourceLotData1 = CommonUtil.getLotInfoByLotName( sourceLotName );
//
//						// Load Port ProductPSequence
//						List<ProductP> productPSequence1 = LotServiceUtil.setProductPSequence_Module( body, sourceLotName );
//
//						// TransferToLot
//						eventInfo.setEventName( "TransferProductToLot" );
//
//						oldBehaviorName = eventInfo.getBehaviorName();
//						if ( ApplyBatchFunctionFlag.equals( GenericServiceProxy.getConstantMap().FLAG_Y ) )
//						{
//							eventInfo.setBehaviorName( GenericServiceProxy.getConstantMap().action_Batch );
//						}
//
//						// TransferProductsToLot( eventInfo, sourceLotData1,
//						// lotData.getKey().getLotName() , productPSequence1,
//						// String.valueOf( productPSequence1.size() ) );
//
//						if ( ApplyBatchFunctionFlag.equals( GenericServiceProxy.getConstantMap().FLAG_Y ) )
//						{
//							eventInfo.setBehaviorName( oldBehaviorName );
//						}
//
//					}
//
//				}
			}
		}
		else if ( lotList.size() > 1 )
		{
			// Report CST Check Lot Assign
			Lot lotDataByCarrier = new Lot();
			try
			{
				lotDataByCarrier = CommonUtil.getLotInfoBydurableName( carrierName );
			}
			catch ( Exception e )
			{
				lotDataByCarrier = null;
			}

			// UnLoader Port ProductPSequence
			List<ProductP> productPSequence = LotServiceUtil.getNotAssigenCarrierProductPSequence_Loader( body, carrierName );

			if ( lotDataByCarrier != null )
			{
				// if Report CST is Lot Assign = > Tranfer Assing Lot Product
				// (CST Noting Assing)
				List<String> notAssignedCSTLotList = LotServiceUtil.getDistinctLotNameByProductListExceptCarrier( body, carrierName );

				for ( int i = 0; i < notAssignedCSTLotList.size(); i++ )
				{
					String sourceLotName = notAssignedCSTLotList.get( i ).toString();
					Lot sourceLotData1 = CommonUtil.getLotInfoByLotName( sourceLotName );

					// Load Port ProductPSequence
					List<ProductP> productPSequence1 = LotServiceUtil.setProductPSequence_Module( body, sourceLotName );

					// TransferToLot
					eventInfo.setEventName( "TransferProductToLot" );

					TransferProductsToLot( eventInfo, sourceLotData1, lotDataByCarrier.getKey().getLotName(), productPSequence1, String.valueOf( productPSequence1.size() ) );
					}

				// Change to TrackOut lotData
				lotData = lotDataByCarrier;
			}
			else
			{
				// CreateRaw Lot
				String splitLotName = generateSplitLotName( lotName );
				eventInfo.setEventName( "CreateNewLot" );
				lotData = MESLotServiceProxy.getLotServiceUtil().createWithParentLot(eventInfo, splitLotName, lotData, lotData.getCarrierName(), false,
						new HashMap<String, String>(), lotData.getUdfs());

				List<String> notAssignedCSTLotList = LotServiceUtil.getDistinctLotNameByProductListExceptCarrier( body, carrierName );

				for ( int i = 0; i < notAssignedCSTLotList.size(); i++ )
				{
					String sourceLotName = notAssignedCSTLotList.get( i ).toString();
					Lot sourceLotData1 = CommonUtil.getLotInfoByLotName( sourceLotName );

					// Load Port ProductPSequence
					List<ProductP> productPSequence1 = LotServiceUtil.setProductPSequence_Module( body, sourceLotName );

					// TransferToLot
					eventInfo.setEventName( "TransferProductToLot" );

					TransferProductsToLot( eventInfo, sourceLotData1, splitLotName, productPSequence1, String.valueOf( productPSequence1.size() ) );
				}

				// Change to TrackOut lotData
				lotData = CommonUtil.getLotInfoByLotName( splitLotName );

				// Assign Carrier To Lot
				eventInfo.setEventName( "AssignCarrier" );
				AssignCarrierInfo assignCarrierInfo = LotInfoUtil.assignCarrierinfo( lotData, durableData, carrierName, LotServiceUtil.setProductPSimpleSequence( doc ) );

				LotServiceProxy.getLotService().assignCarrier( lotData.getKey(), eventInfo, assignCarrierInfo );

			}
		}
		
		
		String toProcessFlowName = "";
		String toProcessOperationName = "";

		ProcessFlowKey curProcessFlowkey = new ProcessFlowKey( lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion() );
		ProcessFlow curProcessFlow = ProcessFlowServiceProxy.getProcessFlowService().selectByKey( curProcessFlowkey );

		String oqaLotName = "";
		String maxSeq = "";
		int seq = 1;

		lotData.setLotGrade(lotGrade);
		//ListOrderedMap alterPolicy = commonAlterPolicy( lotData, "ALL", lotGrade, "","" );
		String sequenceId = "";
		String returnSequenceId = "";
		String reworkFlag = "";
		boolean toRework = false;

		Node nextProcessFlowSeq = null;
		ProcessFlow processFlow = CommonUtil.getProcessFlowData( lotData );

		LotKey lotKey = new LotKey();
		lotKey.setLotName( lotData.getKey().getLotName() );

		// Load Port ProductPGSRCSequence
		List<ProductPGSRC> productPGSRCSequence = LotInfoUtil.setProductPGSRCSequence_Module_OIC( body, lotData.getKey().getLotName(), carrierType, eventInfo );

		// Track Out
		Map<String, String> assignCarrierUserColumns = new HashMap<String, String>();
		Map<String, String> deassignCarrierUserColumns = new HashMap<String, String>();
		List<ConsumedMaterial> lotConsumedMaterail = new ArrayList<ConsumedMaterial>();
		List<ConsumedMaterial> consumedMaterial = new ArrayList<ConsumedMaterial>();

		// makeLoggedOutInfo

		Map<String, String> lotUserColumns = new HashMap<String, String>();

		MakeLoggedOutInfo makeLoggedOutInfo = new MakeLoggedOutInfo();

		makeLoggedOutInfo.setAreaName( lotData.getAreaName() );
		makeLoggedOutInfo.setCarrierName( carrierName );
		makeLoggedOutInfo.setMachineName( machineName );
		makeLoggedOutInfo.setNodeStack( sequenceId );
		makeLoggedOutInfo.setReworkFlag( reworkFlag );
		makeLoggedOutInfo.setProductPGSRCSequence( productPGSRCSequence );

		if ( !portName.isEmpty() )
		{
			lotUserColumns.put( "PORTNAME", portData.getKey().getPortName() );
			lotUserColumns.put( "PORTTYPE", portData.getUdfs().get( "PORTTYPE" ) );
			lotUserColumns.put( "PORTUSETYPE", portData.getUdfs().get( "PORTUSETYPE" ) );
		}
		lotUserColumns.put( "COMMUNICATIONSTATE", machineData.getCommunicationState() );
		lotUserColumns.put( "TRANSACTIONID", transactionId );
		lotUserColumns.put( "RETURNNODESTACK", returnSequenceId );

		makeLoggedOutInfo.setUdfs( lotUserColumns );

		eventInfo.setEventName( "TrackOut" );
		LotServiceProxy.getLotService().makeLoggedOut( lotData.getKey(), eventInfo, makeLoggedOutInfo );

		// Pol rework & OLB TrackOut
/*		if ( carrierType.equals( GenericServiceProxy.getConstantMap().Type_Box )
				&& ( ( machineData.getMachineGroupName().equals( GenericServiceProxy.getConstantMap().ConsumableType_POL ) || ( machineData.getMachineGroupName().equals( GenericServiceProxy
						.getConstantMap().MachineGroup_OLB ) ) ) ) )*/
		if ( ( machineData.getMachineGroupName().equals( GenericServiceProxy.getConstantMap().CONSUMABLE_TYPE_POL ) || ( machineData.getMachineGroupName().equals( GenericServiceProxy
						.getConstantMap().MachineGroup_OLB ) ) ) ) 
		{
			try {
				ConsumableServiceUtil.insertCT_MaterialConsumedByTrackOut( eventInfo, productList, lotData );
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// set return lotname
		List<Map<String, Object>> returnInfoList = new ArrayList<Map<String, Object>>();
		ListOrderedMap returnInfo = new ListOrderedMap();
		returnInfo.put( "RETURNLOTNAME", lotData.getKey().getLotName() );
		returnInfo.put( "OQALOTNAME", oqaLotName );
		returnInfoList.add( returnInfo );
		SMessageUtil.addReturnBodyElement( doc, returnInfoList );
		
		// ScrapLot if created Lot Quantity is 0.
		LotServiceUtil.ScrapLotByTrackOut( eventInfo, lotData.getKey().getLotName() );

		//return lotData.getKey().getLotName();
		
		
		return doc;
	}
	
	public String generateSplitLotName(String lotName)
	{
	    String shortLotName = StringUtil.substring(lotName, 0, 8);
		List<String> argSeq = new ArrayList<String>();
		argSeq.add(shortLotName);
		List<String> lstName = NameServiceProxy.getNameGeneratorRuleDefService().generateName("GlassSplitLotNaming", argSeq, 1);
		
		int i = 0;
		String childLotName = lstName.get(i++);
		
		return childLotName;
	}
	
	public static boolean IsAgingOperation(Product productData )
	{
		if (StringUtil.equals(StringUtil.EMPTY, productData.getUdfs().get("AGINGSHELFID").toString()))
			return false;
		return true;
	}
	
	public static void TransferProductsToLot( EventInfo eventInfo, Lot lotData, String newLotName, List<ProductP> productPSequence, String productQuantity ) 
	{
		Map<String, String> deassignCarrierUserColumns = new HashMap<String, String>();

		TransferProductsToLotInfo transferProductsToLotInfo = new TransferProductsToLotInfo();

		transferProductsToLotInfo.setDestinationLotName( newLotName );
		transferProductsToLotInfo.setProductQuantity( Double.valueOf( productQuantity ).doubleValue() );
		transferProductsToLotInfo.setProductPSequence( productPSequence );
		transferProductsToLotInfo.setEmptyFlag( "Y" );
		// transferProductsToLotInfo.setDestinationLotUserColumns(lotData.getUserColumns());
		transferProductsToLotInfo.setDeassignCarrierUdfs( deassignCarrierUserColumns );

		LotServiceProxy.getLotService().transferProductsToLot( lotData.getKey(), eventInfo, transferProductsToLotInfo );
		eventInfo.setBehaviorName("");
	}
	
	public String getFirstOperationName(String factoryName,String processFlowName)
	{
		String operationName ="";
		String sql ="SELECT NODEID,NODETYPE,PROCESSOPERATIONNAME,PROCESSOPERATIONVERSION FROM V_PROCESSFLOWSEQ WHERE FACTORYNAME=:FACTORYNAME AND PROCESSFLOWNAME=:PROCESSFLOWNAME "
				 + " AND NODETYPE='ProcessOperation' ORDER BY POSITION ";
		Map<String,Object> args = new HashMap<String,Object>();
		args.put( "FACTORYNAME", factoryName );
		args.put( "PROCESSFLOWNAME", processFlowName );
		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
		if(result.size()>0)
		{
			operationName = (String)result.get( 0 ).get( "PROCESSOPERATIONNAME" );
		}
				
        return operationName;
				
	}

}
