
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

		String insertSql = " INSERT INTO CT_ABNORMALSHEET (ABNORMALSHEETNAME,LOTNAME,PRODUCTNAME,ABNORMALCODE,ACTIONCODE, PROCESSSTATE,PROCESSOPERATIONNAME,MACHINENAME,ENGINEER,LEADER,"
				+ " SLOTPOSITION,DUEDATE,CREATEUSER,CREATETIME,LASTEVENTTIMEKEY,LASTEVENTTIME,LASTEVENTNAME,LASTEVENTUSER,LASTEVENTCOMMENT) " + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

		String insertHistSql = " INSERT INTO CT_ABNORMALSHEETHISTORY (TIMEKEY,ABNORMALSHEETNAME,LOTNAME,PRODUCTNAME,ABNORMALCODE,ACTIONCODE,PROCESSSTATE,PROCESSOPERATIONNAME,MACHINENAME,ENGINEER,LEADER, "
				+ " SLOTPOSITION,DUEDATE,CREATEUSER,CREATETIME,EVENTTIME,EVENTNAME,EVENTUSER,EVENTCOMMENT) " + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

		List<Object[]> insertAbnormalSheetList = new ArrayList<Object[]>();
		List<Object[]> insertAbnormalSheetHistList = new ArrayList<Object[]>();

		for ( Element abnormalSheet : abnormalSheetList )
		{
			String lotName = SMessageUtil.getChildText( abnormalSheet, "LOTNAME", false );
			String productName = SMessageUtil.getChildText( abnormalSheet, "PRODUCTNAME", false );
			String abnormalCode = SMessageUtil.getChildText( abnormalSheet, "ABNORMALCODE", false );
			String actionCode = SMessageUtil.getChildText( abnormalSheet, "ACTIONCODE", false );
			String processOperationName = SMessageUtil.getChildText( abnormalSheet, "PROCESSOPERATIONNAME", false );
			String machineName = SMessageUtil.getChildText( abnormalSheet, "MACHINENAME", false );
			String engineer = SMessageUtil.getChildText( abnormalSheet, "ENGINEER", false );
			String leader = SMessageUtil.getChildText( abnormalSheet, "LEADER", false );
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
			insertInfo.add(actionCode);
			insertInfo.add( "Created" );
			insertInfo.add( processOperationName );
			insertInfo.add( machineName );
			insertInfo.add( engineer );
			insertInfo.add( leader );
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
			insertHistory.add(actionCode);
			insertHistory.add( "Created" );
			insertHistory.add( processOperationName );
			insertHistory.add( machineName );
			insertHistory.add( engineer );
			insertHistory.add( leader );
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

			//Action
			
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
