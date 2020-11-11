
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

public class ClosingAbnormalSheet extends SyncHandler {
	/**
	 * 151106 by xzquan : Create CreateLot
	 */
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ClosingAbnormalSheet", getEventUser(), getEventComment(), "", "");
		List<Element> abnormalSheetList = doc.getRootElement().getChild( "Body" ).getChild( "ABNORMALSHEETLIST" ).getChildren( "ABNORMALSHEET" );
		
		String note = SMessageUtil.getBodyItemValue( doc, "NOTE", true );
		
		for ( Element abnormalSheet : abnormalSheetList )
		{
			String abnormalSheetName = SMessageUtil.getChildText( abnormalSheet, "ABNORMALSHEETNAME", true );
			String lotName = SMessageUtil.getChildText( abnormalSheet, "LOTNAME", true );
			String productName = SMessageUtil.getChildText( abnormalSheet, "PRODUCTNAME", true );
			String abnormalCode = SMessageUtil.getChildText( abnormalSheet, "ABNORMALCODE", true );

			String updateSql = " UPDATE CT_ABNORMALSHEET SET PROCESSSTATE = '009',LASTEVENTTIMEKEY = :LASTEVENTTIMEKEY , "
					+ "LASTEVENTTIME = :LASTEVENTTIME ,LASTEVENTNAME = :LASTEVENTNAME ,LASTEVENTUSER = :LASTEVENTUSER , "
					+ "LASTEVENTCOMMENT = :LASTEVENTCOMMENT WHERE ABNORMALSHEETNAME = :ABNORMALSHEETNAME AND LOTNAME = :LOTNAME "
					+ "AND PRODUCTNAME = :PRODUCTNAME AND ABNORMALCODE = :ABNORMALCODE ";

			Map<String, Object> map = new HashMap<String, Object>();
			
			map.put( "LASTEVENTTIMEKEY", TimeUtils.getCurrentEventTimeKey());
			map.put( "LASTEVENTTIME", eventInfo.getEventTime() );
			map.put( "LASTEVENTNAME", "Closed" );
			map.put( "LASTEVENTUSER", eventInfo.getEventUser() );
			map.put( "LASTEVENTCOMMENT", eventInfo.getEventComment() );
			map.put( "ABNORMALSHEETNAME", abnormalSheetName );
			map.put( "LOTNAME", lotName );
			map.put( "PRODUCTNAME", productName );
			map.put( "ABNORMALCODE", abnormalCode );

			String sql = "SELECT * FROM CT_ABNORMALSHEET WHERE ABNORMALSHEETNAME = :ABNORMALSHEETNAME AND LOTNAME = :LOTNAME AND PRODUCTNAME = :PRODUCTNAME AND ABNORMALCODE = :ABNORMALCODE";

			Map<String, Object> map2 = new HashMap<String, Object>();
			map2.put( "ABNORMALSHEETNAME", abnormalSheetName );
			map2.put( "LOTNAME", lotName );
			map2.put( "PRODUCTNAME", productName );
			map2.put( "ABNORMALCODE", abnormalCode );

			String insertSql = " INSERT INTO CT_ABNORMALSHEETHISTORY (TIMEKEY,ABNORMALSHEETNAME,LOTNAME,PRODUCTNAME,ABNORMALCODE,PROCESSSTATE,PROCESSOPERATIONNAME,MACHINENAME,ENGINEER,LEADER, "
					+ " SLOTPOSITION,DUEDATE,CREATEUSER,CREATETIME,ACTIONCODE,EVENTTIME,EVENTNAME,EVENTUSER,EVENTCOMMENT) " + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

			List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList( sql.toString(), map2 );
			List<Object[]> insertInfo = new ArrayList<Object[]>();
			if ( sqlResult.size() > 0 )
			{
				List<Object> insert = new ArrayList<Object>();

				insert.add( TimeUtils.getCurrentEventTimeKey() );
				insert.add( abnormalSheetName );
				insert.add( lotName );
				insert.add( productName );
				insert.add( abnormalCode );
				insert.add( "009" );
				insert.add( (String) sqlResult.get( 0 ).get( "PROCESSOPERATIONNAME" ) );
				insert.add( (String) sqlResult.get( 0 ).get( "MACHINENAME" ) );
				insert.add( (String) sqlResult.get( 0 ).get( "ENGINEER" )  );
				insert.add( (String) sqlResult.get( 0 ).get( "LEADER" )  );
				insert.add( (String) sqlResult.get( 0 ).get( "SLOTPOSITION" ) );
				insert.add( (String) sqlResult.get( 0 ).get( "DUEDATE" ) );
				insert.add( (String) sqlResult.get( 0 ).get( "CREATEUSER" ) );
				insert.add( (java.sql.Timestamp ) sqlResult.get( 0 ).get( "CREATETIME" ) );
				insert.add( (String) sqlResult.get( 0 ).get( "ACTIONCODE" ) );
				insert.add( eventInfo.getEventTime() );
				insert.add( "Close" );
				insert.add( eventInfo.getEventUser() );
				insert.add( eventInfo.getEventComment() );

				insertInfo.add( insert.toArray() );
			}

			greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update( updateSql, map );
			GenericServiceProxy.getSqlMesTemplate().updateBatch( insertSql, insertInfo );
			
			String updateNoteSql = "UPDATE CT_ABNORMALSHEETNOTE SET NOTE=:NOTE WHERE ABNORMALSHEETNAME = :ABNORMALSHEETNAME "
					+ "AND LOTNAME = :LOTNAME AND PRODUCTNAME = :PRODUCTNAME ";

			Map<String, Object> note_map = new HashMap<String, Object>();
			note_map.put( "NOTE", note );
			note_map.put( "ABNORMALSHEETNAME", abnormalSheetName );
			note_map.put( "LOTNAME", lotName );
			note_map.put( "PRODUCTNAME", productName );
			greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update( updateNoteSql, note_map );
		}
		

		return doc;

	}
}
