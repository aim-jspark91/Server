
package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LotAction;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
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
import kr.co.aim.greentrack.lot.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.collections.map.ListOrderedMap;
import org.jdom.Document;
import org.jdom.Element;

public class DeleteAbnormalSheet extends SyncHandler {
	/**
	 * 151106 by xzquan : Create CreateLot
	 */
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteAbnormalSheet", getEventUser(), getEventComment(), "", "");
		String abnormalSheetName = SMessageUtil.getBodyItemValue( doc, "ABNORMALSHEETNAME", true );
		String lotName = SMessageUtil.getBodyItemValue( doc, "LOTNAME", true );
		String productName = SMessageUtil.getBodyItemValue( doc, "PRODUCTNAME", true );
		String note = SMessageUtil.getBodyItemValue( doc, "NOTE", true );
		
		eventInfo.setEventComment(note);

		//insert History
		String sql = "SELECT * FROM CT_ABNORMALSHEET WHERE ABNORMALSHEETNAME = :ABNORMALSHEETNAME AND LOTNAME = :LOTNAME AND PRODUCTNAME = :PRODUCTNAME ";

		Map<String, Object> map2 = new HashMap<String, Object>();
		map2.put( "ABNORMALSHEETNAME", abnormalSheetName );
		map2.put( "LOTNAME", lotName );
		map2.put( "PRODUCTNAME", productName );

		String insertSql = "INSERT INTO CT_ABNORMALSHEETHISTORY (TIMEKEY,ABNORMALSHEETNAME,LOTNAME,PRODUCTNAME,ABNORMALCODE,PROCESSSTATE,PROCESSOPERATIONNAME,MACHINENAME,DEPARTMENT, DEPARTMENTCOUNT, ENGINEER,LEADER,  "
				+ " SLOTPOSITION,DUEDATE,CREATEUSER,CREATETIME,ACTIONCODE,EVENTTIME,EVENTNAME,EVENTUSER,EVENTCOMMENT) " + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

		List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList( sql.toString(), map2 );
		
		
		List<Object[]> insertInfo = new ArrayList<Object[]>();
		if ( sqlResult.size() > 0 )
		{
			List<Object> insert = new ArrayList<Object>();

			insert.add( TimeUtils.getCurrentEventTimeKey() );
			insert.add( abnormalSheetName );
			insert.add( lotName );
			insert.add( productName );
			insert.add( (String) sqlResult.get(0).get("ABNORMALCODE") );
			insert.add( (String) sqlResult.get(0).get("PROCESSSTATE") );
			insert.add( (String) sqlResult.get( 0 ).get( "PROCESSOPERATIONNAME" ) );
			insert.add( (String) sqlResult.get( 0 ).get( "MACHINENAME" ) );
			insert.add(  (String) sqlResult.get(0).get("DEPARTMENT") );
			insert.add(  (String) sqlResult.get(0).get("DEPARTMENTCOUNT") );
			insert.add( (String) sqlResult.get(0).get("ENGINEER")  );
			insert.add( (String) sqlResult.get(0).get("LEADER")  );
			insert.add( (String) sqlResult.get( 0 ).get( "SLOTPOSITION" ) );
			insert.add( (String) sqlResult.get( 0 ).get( "DUEDATE" ) );
			insert.add( (String) sqlResult.get( 0 ).get( "CREATEUSER" ) );
			insert.add( (java.sql.Timestamp ) sqlResult.get( 0 ).get( "CREATETIME" ) );
			insert.add( (String) sqlResult.get(0).get("ACTIONCODE")  );
			insert.add( eventInfo.getEventTime() );
			insert.add( "Delete" );
			insert.add( eventInfo.getEventUser() );
			insert.add( eventInfo.getEventComment() );

			insertInfo.add( insert.toArray() );
		}

		GenericServiceProxy.getSqlMesTemplate().updateBatch( insertSql, insertInfo );
		
		//Delete 
		String DeleteSql = "DELETE CT_ABNORMALSHEET WHERE ABNORMALSHEETNAME = :ABNORMALSHEETNAME "
				+ "AND LOTNAME = :LOTNAME AND PRODUCTNAME = :PRODUCTNAME ";

		Map<String, Object> map = new HashMap<String, Object>();
		map.put( "ABNORMALSHEETNAME", abnormalSheetName );
		map.put( "LOTNAME", lotName );
		map.put( "PRODUCTNAME", productName );
		
		greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update( DeleteSql, map );
		
		
		String DeleteNoteSql = "DELETE CT_ABNORMALSHEETNOTE WHERE ABNORMALSHEETNAME = :ABNORMALSHEETNAME "
				+ "AND LOTNAME = :LOTNAME AND PRODUCTNAME = :PRODUCTNAME ";

		Map<String, Object> note_map = new HashMap<String, Object>();
		note_map.put( "ABNORMALSHEETNAME", abnormalSheetName );
		note_map.put( "LOTNAME", lotName );
		note_map.put( "PRODUCTNAME", productName );
		greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update( DeleteNoteSql, note_map );
		
		return doc;
	}
}
