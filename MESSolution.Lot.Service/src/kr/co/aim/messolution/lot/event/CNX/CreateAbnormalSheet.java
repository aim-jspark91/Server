
package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.service.LotInfoUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotFutureAction;
import kr.co.aim.greentrack.lot.management.data.LotFutureActionKey;
import kr.co.aim.greentrack.lot.management.data.LotFutureCondition;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.info.MakeOnHoldInfo;
import org.jdom.Document;
import org.jdom.Element;

public class CreateAbnormalSheet extends SyncHandler {
	/**
	 * 151106 by xzquan : Create CreateLot
	 */
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateAbnormalSheet", getEventUser(), getEventComment(), "", "");
		String preFix = "";
		String abnormalSheetName = "";
		List<Element> abnormalSheetList = doc.getRootElement().getChild( "Body" ).getChild( "ABNORMALSHEETLIST" ).getChildren( "ABNORMALSHEET" );

		String insertSql = " INSERT INTO CT_ABNORMALSHEET (ABNORMALSHEETNAME,LOTNAME,PRODUCTNAME,ABNORMALCODE,PROCESSSTATE,PROCESSOPERATIONNAME,MACHINENAME,ENGDEPARTMENT, "
				+ " SLOTPOSITION,DUEDATE,CREATEUSER,CREATETIME,LASTEVENTTIMEKEY,LASTEVENTTIME,LASTEVENTNAME,LASTEVENTUSER,LASTEVENTCOMMENT) " + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

		String insertHistSql = " INSERT INTO CT_ABNORMALSHEETHISTORY (TIMEKEY,ABNORMALSHEETNAME,LOTNAME,PRODUCTNAME,ABNORMALCODE,PROCESSSTATE,PROCESSOPERATIONNAME,MACHINENAME,ENGDEPARTMENT, "
				+ " SLOTPOSITION,DUEDATE,CREATEUSER,CREATETIME,EVENTTIME,EVENTNAME,EVENTUSER,EVENTCOMMENT) " + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

		List<Object[]> insertAbnormalSheetList = new ArrayList<Object[]>();
		List<Object[]> insertAbnormalSheetHistList = new ArrayList<Object[]>();

		for ( Element abnormalSheet : abnormalSheetList )
		{
			String lotName = SMessageUtil.getChildText( abnormalSheet, "LOTNAME", false );
			String productName = SMessageUtil.getChildText( abnormalSheet, "PRODUCTNAME", false );
			String abnormalCode = SMessageUtil.getChildText( abnormalSheet, "ABNORMALCODE", false );
			String processOperationName = SMessageUtil.getChildText( abnormalSheet, "PROCESSOPERATIONNAME", false );
			String machineName = SMessageUtil.getChildText( abnormalSheet, "MACHINENAME", false );
			String engDepartment = SMessageUtil.getChildText( abnormalSheet, "ENGDEPARTMENT", false );
			String sLotPosition = SMessageUtil.getChildText( abnormalSheet, "SLOTPOSITION", false );
			String durdate = SMessageUtil.getChildText( abnormalSheet, "DUEDATE", false );

			String abnormalSheetNameSql = "SELECT DISTINCT ABNORMALSHEETNAME FROM CT_ABNORMALSHEET A WHERE A.LOTNAME = :LOTNAME";
			Map<String, String> map = new HashMap<String, String>();
			map.put( "LOTNAME", lotName );

			List<Map<String, Object>> abnormalSheetNameResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList( abnormalSheetNameSql.toString(), map );

			if ( abnormalSheetNameResult.size() > 0 )
			{
				abnormalSheetName = (String) abnormalSheetNameResult.get( 0 ).get( "ABNORMALSHEETNAME" );
			}
			else
			{
				LotKey lotKey = new LotKey();
				lotKey.setLotName( lotName );
				Lot lotData = new Lot();
				lotData = LotServiceProxy.getLotService().selectByKey( lotKey );
				String factoryName = lotData.getFactoryName();

				if ( factoryName.equals( "ARRAY" ) ) preFix = "A";
				else if ( factoryName.equals( "CELL" ) ) preFix = "C";
				else if ( factoryName.equals( "CF" ) ) preFix = "F";

				abnormalSheetName = preFix + eventInfo.getEventTime().toString().substring( 2, 4 ) + eventInfo.getEventTime().toString().substring( 5, 7 )
						+ eventInfo.getEventTime().toString().substring( 8, 10 ) + eventInfo.getEventTime().toString().substring( 20, 23 );

			}

			List<Object> insertInfo = new ArrayList<Object>();

			insertInfo.add( abnormalSheetName );
			insertInfo.add( lotName );
			insertInfo.add( productName );
			insertInfo.add( abnormalCode );
			insertInfo.add( "Created" );
			insertInfo.add( processOperationName );
			insertInfo.add( machineName );
			insertInfo.add( engDepartment );
			insertInfo.add( sLotPosition );
			insertInfo.add( durdate );
			insertInfo.add( eventInfo.getEventUser() );
			insertInfo.add( eventInfo.getEventTime() );
			insertInfo.add( TimeUtils.getCurrentEventTimeKey() );
			insertInfo.add( eventInfo.getEventTime() );
			insertInfo.add( "Create" );
			insertInfo.add( eventInfo.getEventUser() );
			insertInfo.add( eventInfo.getEventComment() );

			insertAbnormalSheetList.add( insertInfo.toArray() );

			List<Object> insertHistory = new ArrayList<Object>();

			insertHistory.add( TimeUtils.getCurrentEventTimeKey() );
			insertHistory.add( abnormalSheetName );
			insertHistory.add( lotName );
			insertHistory.add( productName );
			insertHistory.add( abnormalCode );
			insertHistory.add( "Created" );
			insertHistory.add( processOperationName );
			insertHistory.add( machineName );
			insertHistory.add( engDepartment );
			insertHistory.add( sLotPosition );
			insertHistory.add( durdate );
			insertHistory.add( eventInfo.getEventUser() );
			insertHistory.add( eventInfo.getEventTime() );
			insertHistory.add( eventInfo.getEventTime() );
			insertHistory.add( "Create" );
			insertHistory.add( eventInfo.getEventUser() );
			insertHistory.add( eventInfo.getEventComment() );

			insertAbnormalSheetHistList.add( insertHistory.toArray() );

			LotKey lotKey = new LotKey();
			lotKey.setLotName( lotName );
			Lot lotData = new Lot();
			lotData = LotServiceProxy.getLotService().selectByKey( lotKey );

			ProductKey productKey = new ProductKey();
			productKey.setProductName( productName );

			Product productData = null;
			productData = ProductServiceProxy.getProductService().selectByKey( productKey );

			// Check Product Hold
			String checkSql = "SELECT REASONCODE, EVENTNAME, EVENTUSER FROM PRODUCTMULTIHOLD " + "WHERE PRODUCTNAME = :productName AND REASONCODE = :reasonCode";
			Map<String, String> obj = new HashMap<String, String>();
			obj.put( "productName", productData.getKey().getProductName() );
			obj.put( "reasonCode", "ABSHOLD" );
			List<Map<String, Object>> checkSqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList( checkSql, obj );
			if ( checkSqlResult.size() > 0 ) { throw new CustomException( "PRHOLD-02" ); }
			if ( StringUtil.equals( lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Released ) && StringUtil.equals( lotData.getLotProcessState(), "WAIT" ) )
			{
				eventInfo.setReasonCode( "ABSHOLD" );
				eventInfo.setReasonCodeType( "HoldLot" );
				eventInfo.setEventComment( "Hold Lot By Abnormal Sheet. AbnormalSheetName: " + abnormalSheetName );

				MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo();
				ProductServiceProxy.getProductService().makeOnHold( productData.getKey(), eventInfo, makeOnHoldInfo );
			}
			else if ( StringUtil.equals( lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Released ) && StringUtil.equals( lotData.getLotProcessState(), "RUN" ) )
			{
				Node nextMandatorySeq = LotInfoUtil.nextMandatoryInfo( lotData.getFactoryName(), lotData.getProcessOperationName(), lotData.getProcessFlowName(), null,
						lotData.getProcessOperationVersion(), lotData.getProcessFlowVersion() );
				
				String nextOperationName = nextMandatorySeq.getNodeAttribute1();
				
				// ReserveLotHold, insert LotfutureAction
				StringBuilder sql = new StringBuilder();
				sql.append( "SELECT LOTNAME, PROCESSOPERATIONNAME, POSITION, EVENTUSER, REASONCODE " );
				sql.append( "  FROM LOTFUTUREACTION " );
				sql.append( " WHERE LOTNAME = :LOTNAME " );
				sql.append( "   AND FACTORYNAME = :FACTORYNAME " );
				sql.append( "   AND PROCESSFLOWNAME = :PROCESSFLOWNAME " );
				sql.append( "   AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME " );
				sql.append( "   AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION " );
				sql.append( "   AND ACTIONNAME = 'hold' " );
				sql.append( "ORDER BY POSITION " );

				Map<String, String> bindMap = new HashMap<String, String>();
				bindMap.put( "LOTNAME", lotName );
				bindMap.put( "FACTORYNAME", lotData.getFactoryName() );
				bindMap.put( "PROCESSFLOWNAME", lotData.getProcessFlowName() );
				bindMap.put( "PROCESSOPERATIONNAME", nextOperationName);
				bindMap.put( "PROCESSOPERATIONVERSION", lotData.getProcessOperationVersion() );

				List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList( sql.toString(), bindMap );

				if ( sqlResult.size() > 0 )
				{
					for ( Map<String, Object> result : sqlResult )
					{
						String LotFutureAction_ReasonCode = ConvertUtil.getMapValueByName( result, "REASONCODE" );

						//CommonValidation.deleteReserveHoldByReserveHoldUser( lotName, "ABSHOLD", eventInfo.getEventUser() );
					}
				}

				// SetEvent
				SetEventInfo setEventInfo = new SetEventInfo();
				eventInfo.setEventName( "CreateAbnormalSheet " + abnormalSheetName );
				eventInfo.setEventComment( "Reserve Hold by AbnormalSheet" );
				LotServiceProxy.getLotService().setEvent( lotData.getKey(), eventInfo, setEventInfo );
				eventLog.info( "Event Name = " + eventInfo.getEventName() + " , EventTimeKey + " + eventInfo.getEventTimeKey() );

				// Delete LotFutureCondition & LotFutureAction
				// Delete LotFutureAction
				sql.setLength( 0 );
				sql.append( "DELETE LOTFUTUREACTION " );
				sql.append( " WHERE LOTNAME = :LOTNAME " );
				sql.append( "   AND FACTORYNAME = :FACTORYNAME " );
				sql.append( "   AND PROCESSFLOWNAME = :PROCESSFLOWNAME " );
				sql.append( "   AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME " );
				sql.append( "   AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION " );
				sql.append( "   AND ACTIONNAME = 'hold' " );

				greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update( sql.toString(), bindMap );

				// LotFutureCondition
				LotFutureCondition lotFCData = new LotFutureCondition();
				lotFCData.getKey().setLotName( lotName );
				lotFCData.getKey().setFactoryName( lotData.getFactoryName() );
				lotFCData.getKey().setProcessFlowName( lotData.getProcessFlowName() );
				lotFCData.getKey().setProcessOperationName( nextOperationName );
				lotFCData.getKey().setProcessFlowVersion( lotData.getProcessFlowVersion() );
				lotFCData.getKey().setProcessOperationVersion( lotData.getProcessOperationVersion() );
				lotFCData.setDescription( "Reserve Hold by AbnormalSheet" );

				LotFutureCondition lotFutureCondition = null;
				try
				{
					lotFutureCondition = LotServiceProxy.getLotFutureConditionService().selectByKey( lotFCData.getKey() );
				}
				catch ( NotFoundSignal e )
				{}

				try
				{
					if ( lotFutureCondition == null ) LotServiceProxy.getLotFutureConditionService().insert( lotFCData );
					else LotServiceProxy.getLotFutureConditionService().update( lotFCData );
				}
				catch ( DuplicateNameSignal e )
				{}

				// LotFutureAction
				long position = 0;
				LotFutureActionKey lotFAKey = new LotFutureActionKey();
				lotFAKey.setLotName( lotName );
				lotFAKey.setFactoryName( lotData.getFactoryName() );
				lotFAKey.setProcessFlowName( lotData.getProcessFlowName() );
				lotFAKey.setProcessOperationName( nextOperationName );
				lotFAKey.setPosition( position );
				lotFAKey.setProcessFlowVersion( lotData.getProcessFlowVersion() );
				lotFAKey.setProcessOperationVersion( lotData.getProcessOperationVersion() );

				LotFutureAction lotFutureAction = new LotFutureAction();
				lotFutureAction.setKey( lotFAKey );
				lotFutureAction.setActionName( "hold" );
				lotFutureAction.setActionType( "System" );
				lotFutureAction.setEventName( eventInfo.getEventName() );
				lotFutureAction.setEventUser( eventInfo.getEventUser() );
				lotFutureAction.setReasonCodeType( "HOLD" );
				lotFutureAction.setReasonCode( "ABSHOLD" );
				lotFutureAction.setEventUser( eventInfo.getEventUser() );

				try
				{
					LotServiceProxy.getLotFutureActionService().insert( lotFutureAction );
				}
				catch ( DuplicateNameSignal e )
				{}

				//LotServiceImpl.insertCT_RESERVEHOLDLOTBYPRODUCT( machineName, productName, nextOperationName, lotName, "ABSHOLD", eventInfo, "" );

			}
		}
		if ( insertAbnormalSheetList.size() > 0 && insertAbnormalSheetHistList.size() > 0 )
		{
			GenericServiceProxy.getSqlMesTemplate().updateBatch(insertSql, insertAbnormalSheetList);
			GenericServiceProxy.getSqlMesTemplate().updateBatch(insertHistSql, insertAbnormalSheetHistList);
		}
		
		SMessageUtil.setBodyItemValue( doc, "ABNORMALSHEETNAME", abnormalSheetName );
		return doc;
	}
}
