/*============================================================================
 * Class Name   : CancelReceive
 * Author       : AIM-jjyoo
 * Date         : 2018.05.13
============================================================================*/
package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.HQGlassJudge;
import kr.co.aim.messolution.extended.object.management.data.LotMultiHold;
import kr.co.aim.messolution.extended.object.management.data.PanelJudge;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductHistory;
import kr.co.aim.greentrack.product.management.data.ProductHistoryKey;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;

import org.apache.commons.dbcp.BasicDataSource;
import org.jdom.Document;
import org.jdom.Element;

public class CancelReceiveLot extends SyncHandler {
	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		/*============= Set event =============*/
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelReceive", getEventUser(), getEventComment(), null, null);
		
		List<Element> eCSTList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true); 		
		
		String eventUser = eventInfo.getEventUser();
		
		for(Element eCarrier : eCSTList)
		{	
			String lotName = SMessageUtil.getChildText(eCarrier, "LOTNAME", true);
			
			Lot lotData = CommonUtil.getLotInfoByLotName(lotName);
			
			StringBuffer sbSql = new StringBuffer();
			sbSql.append( "SELECT RPS.TOFACTORYNAME, RPS.TOPRODUCTSPECNAME, RPS.TOPROCESSOPERATIONNAME, TP.FACTORYNAME FROMFACTORYNAME ");
			sbSql.append( "FROM TPPOLICY TP, POSFACTORYRELATION RPS " );
			sbSql.append( "WHERE TP.CONDITIONID = RPS.CONDITIONID " );
			sbSql.append( "AND TP.FACTORYNAME = :FACTORYNAME " );
			sbSql.append( "AND TP.PRODUCTSPECNAME = :PRODUCTSPECNAME " );
			sbSql.append( "AND RPS.JOBTYPE = :JOBTYPE " );
			
			Map<String, Object> mBind = new HashMap<String, Object>();
			mBind.put( "FACTORYNAME", lotData.getFactoryName() );
			mBind.put( "PRODUCTSPECNAME", lotData.getProductSpecName() );
			mBind.put( "JOBTYPE", "CancelReceive" );
			
			List<Map<String, Object>> results = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList( sbSql.toString(), mBind );

			if ( results == null || results.size() == 0 ) { throw new CustomException( "Lot-0059" ); }
			
			String factoryName = ConvertUtil.getMapValueByName( results.get( 0 ), "TOFACTORYNAME" );
			String productSpecName = ConvertUtil.getMapValueByName( results.get( 0 ), "TOPRODUCTSPECNAME" );
			String processOperationName = ConvertUtil.getMapValueByName( results.get( 0 ), "TOPROCESSOPERATIONNAME" );
			String destinationFactoryName = ConvertUtil.getMapValueByName( results.get( 0 ), "FROMFACTORYNAME" );

			ProductSpec productSpec = CommonUtil.getProductSpecByProductSpecName( factoryName, productSpecName, "00001" );

			String processFlowName = lotData.getUdfs().get("BEFOREFLOWNAME");

			Node nodeData = ProcessFlowServiceProxy.getNodeService().getNode(factoryName, processFlowName, "00001", "ProcessOperation", processOperationName, "00001");
		
			List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotName);
			
			lotData.setDestinationFactoryName(destinationFactoryName);  
		//	lotData.setAreaName(lotData.getUdfs().get("OLDAREANAME"));
			LotServiceProxy.getLotService().update(lotData);
			
			eventInfo.setEventName( "CancelReceive" );

			ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();
			changeSpecInfo.setFactoryName( factoryName );
			changeSpecInfo.setProductSpecName( productSpecName );
			changeSpecInfo.setProcessOperationName( processOperationName );
			changeSpecInfo.setProcessFlowName( processFlowName );
			changeSpecInfo.setNodeStack(nodeData.getKey().getNodeId());
			changeSpecInfo.setProductSpecVersion("00001");
			changeSpecInfo.setPriority(lotData.getPriority());
			changeSpecInfo.setProductionType(lotData.getProductionType());
			changeSpecInfo.setDueDate(lotData.getDueDate());
			changeSpecInfo.setProductRequestName(lotData.getProductRequestName());
			changeSpecInfo.setLotState(GenericServiceProxy.getConstantMap().Lot_Shipped);
			changeSpecInfo.setLotProcessState("");
			changeSpecInfo.setLotHoldState("");
			changeSpecInfo.setAreaName(lotData.getAreaName());
			changeSpecInfo.setSubProductUnitQuantity1(lotData.getSubProductUnitQuantity1());
			changeSpecInfo.setProductUSequence( productUdfs );
			Map<String, String> userColumns = new HashMap<String, String>();

			userColumns.put("RECEIVEFLAG", "");
			userColumns.put("SHIPBANKSTATE", "");
			
			changeSpecInfo.setUdfs( userColumns );

			Lot canceledLot = LotServiceProxy.getLotService().changeSpec( lotData.getKey(), eventInfo, changeSpecInfo );
			
			List<Product> productList = ProductServiceProxy.getProductService().allProductsByLot(lotData.getKey().getLotName());
			
			for(Product productData : productList)
			{
				productData.setDestinationFactoryName(destinationFactoryName);
				productData.setProductState(GenericServiceProxy.getConstantMap().Prod_Shipped);
				productData.setProductProcessState("");
				productData.setProductHoldState("");
				ProductServiceProxy.getProductService().update(productData);
				
				ProductHistoryKey productHistoryKey = new ProductHistoryKey();
	            productHistoryKey.setProductName(productData.getKey().getProductName());
	            productHistoryKey.setTimeKey(productData.getLastEventTimeKey());
	            ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKeyForUpdate(productHistoryKey);
	            productHistory.setDestinationFactoryName(destinationFactoryName);
	            productHistory.setProductState(GenericServiceProxy.getConstantMap().Prod_Shipped);
	            productHistory.setProductProcessState("");
	            productHistory.setProductHoldState("");
	            
				ProductServiceProxy.getProductHistoryService().update(productHistory);
				
			}
		}
		
		return doc;
	}
	
	
	private void updateOnlyArrayCSTData(String cstName, String arrDBUserName) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelReceive", getEventUser(), getEventComment(), null, null);
		
		try
		{
			//Update Array Durable
			/*String sql = "UPDATE "+arrDBUserName+".DURABLE SET RECEIVEFLAG = '' WHERE DURABLENAME = :DURABLENAME ";
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("DURABLENAME", cstName);
			
			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);*/
			
			//Select Array Durable
			String sql_2 = "SELECT * FROM DURABLE@ARRAYDB WHERE DURABLENAME = :DURABLENAME ";
			
			Map<String, Object> bindMap2 = new HashMap<String, Object>();
			bindMap2.put("DURABLENAME", cstName);
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql_2, bindMap2);
			
			//Insert Durable History
			if ( sqlResult.size() > 0)
			{
				for(Map<String, Object> map : sqlResult)
				{
					String histSql = "Insert into "+arrDBUserName+".DURABLEHISTORY " +
							"   (DURABLENAME, TIMEKEY, EVENTTIME, EVENTNAME, OLDDURABLESPECNAME, DURABLESPECNAME, OLDDURABLESPECVERSION, " +
							"   DURABLESPECVERSION, MATERIALLOCATIONNAME, TRANSPORTGROUPNAME, TIMEUSEDLIMIT, " +
							"   TIMEUSED, DURATIONUSEDLIMIT, DURATIONUSED, CAPACITY, LOTQUANTITY, OLDFACTORYNAME, FACTORYNAME, OLDAREANAME, AREANAME, " +
							"   DURABLESTATE, DURABLECLEANSTATE, EVENTUSER, EVENTCOMMENT, " +
							"   EVENTFLAG, REASONCODETYPE, REASONCODE, RECEIVEFLAG) " +
							" Values " +
							"   (:DURABLENAME, :TIMEKEY, TO_DATE(:EVENTTIME, 'YYYY/MM/DD HH24:MI:SS') , :EVENTNAME, :OLDDURABLESPECNAME, :DURABLESPECNAME, " +
							"   :OLDDURABLESPECVERSION, :DURABLESPECVERSION, :MATERIALLOCATIONNAME, " +
							"   :TRANSPORTGROUPNAME, :TIMEUSEDLIMIT, :TIMEUSED, :DURATIONUSEDLIMIT, :DURATIONUSED, :CAPACITY, :LOTQUANTITY, :OLDFACTORYNAME, :FACTORYNAME, :OLDAREANAME, :AREANAME, " +
							"   :DURABLESTATE, :DURABLECLEANSTATE, :EVENTUSER, :EVENTCOMMENT, :EVENTFLAG, :REASONCODETYPE, :REASONCODE, :RECEIVEFLAG) " ;
					
					Map<String, Object> histbindMap = new HashMap<String, Object>();
					histbindMap.put("DURABLENAME", (String) map.get("DURABLENAME"));
					histbindMap.put("TIMEKEY", eventInfo.getEventTimeKey());
					histbindMap.put("EVENTTIME", TimeStampUtil.toTimeString((Timestamp)eventInfo.getEventTime()));
					histbindMap.put("EVENTNAME", eventInfo.getEventName());
					histbindMap.put("OLDDURABLESPECNAME", (String)map.get("DURABLESPECNAME"));
					histbindMap.put("OLDDURABLESPECVERSION", (String)map.get("DURABLESPECVERSION"));
					histbindMap.put("DURABLESPECVERSION", (String) map.get("DURABLESPECVERSION"));
					histbindMap.put("DURABLESPECNAME", (String) map.get("DURABLESPECNAME"));
					histbindMap.put("MATERIALLOCATIONNAME", (String) map.get("MATERIALLOCATIONNAME"));
					histbindMap.put("TRANSPORTGROUPNAME", (String) map.get("TRANSPORTGROUPNAME"));
					histbindMap.put("TIMEUSEDLIMIT", map.get("TIMEUSEDLIMIT").toString());
					histbindMap.put("TIMEUSED", map.get("TIMEUSED").toString());
					histbindMap.put("DURATIONUSEDLIMIT", map.get("DURATIONUSEDLIMIT").toString());
					histbindMap.put("DURATIONUSED", map.get("DURATIONUSED").toString());
					histbindMap.put("CAPACITY", map.get("CAPACITY").toString());
					histbindMap.put("LOTQUANTITY", map.get("LOTQUANTITY").toString());
					histbindMap.put("OLDFACTORYNAME", (String)map.get("FACTORYNAME"));
					histbindMap.put("FACTORYNAME", (String) map.get("FACTORYNAME"));
					histbindMap.put("OLDAREANAME", (String) map.get("AREANAME"));
					histbindMap.put("AREANAME", (String) map.get("AREANAME"));
					histbindMap.put("DURABLESTATE", (String) map.get("DURABLESTATE"));
					histbindMap.put("DURABLECLEANSTATE", (String) map.get("DURABLECLEANSTATE"));
					histbindMap.put("EVENTUSER", eventInfo.getEventUser());
					histbindMap.put("EVENTCOMMENT", eventInfo.getEventComment());
					histbindMap.put("EVENTFLAG", (String) map.get("LASTEVENTFLAG"));
					histbindMap.put("REASONCODETYPE", (String) map.get("REASONCODETYPE"));
					histbindMap.put("REASONCODE", (String) map.get("REASONCODE"));
					histbindMap.put("RECEIVEFLAG", (String) map.get("RECEIVEFLAG"));

					GenericServiceProxy.getSqlMesTemplate().update(histSql, histbindMap);
				}
			}	
		}
		catch(Exception ex)
		{
			throw new CustomException("LOT-0231","");
		}
	}
	
	private void updateOnlyArrayLotData(String originalLotName, String arrDBUserName) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelReceive", getEventUser(), getEventComment(), null, null);
		
		try
		{
			//Update Array Lot
			String sql = "UPDATE "+arrDBUserName+".LOT SET RECEIVEFLAG = '', LOTSTATE = 'Shipped' WHERE LOTNAME = :LOTNAME ";
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTNAME", originalLotName);
			
			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
			
			//Select Array Lot
			String sql_2 = "SELECT * FROM LOT@ARRAYDB WHERE LOTNAME = :LOTNAME ";
			
			Map<String, Object> bindMap2 = new HashMap<String, Object>();
			bindMap2.put("LOTNAME", originalLotName);
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql_2, bindMap2);
			
			//Insert Lot History
			if ( sqlResult.size() > 0)
			{
				for(Map<String, Object> map : sqlResult)
				{
					String histSql = "INSERT INTO "+arrDBUserName+".LOTHISTORY (LOTNAME,TIMEKEY,EVENTTIME,EVENTNAME,OLDPRODUCTIONTYPE,PRODUCTIONTYPE,OLDPRODUCTSPECNAME,PRODUCTSPECNAME,OLDPRODUCTSPECVERSION,    " +
							"  PRODUCTSPECVERSION,PROCESSGROUPNAME, PRODUCTREQUESTNAME,ORIGINALLOTNAME, SOURCELOTNAME, DESTINATIONLOTNAME, ROOTLOTNAME, PARENTLOTNAME, CARRIERNAME,  " +
							"  OLDPRODUCTTYPE, PRODUCTTYPE, OLDSUBPRODUCTTYPE, SUBPRODUCTTYPE, SUBPRODUCTUNITQUANTITY1, SUBPRODUCTUNITQUANTITY2,PRODUCTQUANTITY,  " +
							"  SUBPRODUCTQUANTITY,SUBPRODUCTQUANTITY1,SUBPRODUCTQUANTITY2, LOTGRADE, DUEDATE,  " +
							"  PRIORITY, OLDFACTORYNAME, FACTORYNAME, OLDDESTINATIONFACTORYNAME,DESTINATIONFACTORYNAME, OLDAREANAME, AREANAME, LOTSTATE, LOTPROCESSSTATE, LOTHOLDSTATE,  " +
							"  EVENTUSER, EVENTCOMMENT,EVENTFLAG, LASTLOGGEDINTIME, LASTLOGGEDINUSER, LASTLOGGEDOUTTIME, LASTLOGGEDOUTUSER, REASONCODETYPE, REASONCODE,  " +
							"  OLDPROCESSFLOWNAME, PROCESSFLOWNAME, OLDPROCESSFLOWVERSION, PROCESSFLOWVERSION, OLDPROCESSOPERATIONNAME, PROCESSOPERATIONNAME, OLDPROCESSOPERATIONVERSION,  " +
							"  PROCESSOPERATIONVERSION, NODESTACK, MACHINENAME, MACHINERECIPENAME, REWORKSTATE, REWORKCOUNT, REWORKNODEID, CONSUMERLOTNAME, CONSUMERTIMEKEY, CONSUMEDLOTNAME, " +
							"  CONSUMEDDURABLENAME, CONSUMEDCONSUMABLENAME, SYSTEMTIME, CANCELFLAG,  CANCELTIMEKEY, BRANCHENDNODEID, NOTE, BEFOREFLOWNAME, BEFOREOPERATIONNAME, ECCODE,  " +
							"  DEPARTMENTNAME, RECEIVEFLAG, SUPERLOTFLAG, ENDBANK, SHIPBANK,SHIPBANKSTATE, PORTNAME, LASTLOGGEDOUTMACHINE, OLDPRODUCTREQUESTNAME, OLDPRODUCTQUANTITY, OLDSUBPRODUCTQUANTITY, OLDSUBPRODUCTQUANTITY1, OLDSUBPRODUCTQUANTITY2)  " +
							"  VALUES " +
							"  (:LOTNAME,:TIMEKEY,TO_DATE(:EVENTTIME, 'YYYY/MM/DD HH24:MI:SS'),:EVENTNAME,:OLDPRODUCTIONTYPE,:PRODUCTIONTYPE,:OLDPRODUCTSPECNAME,:PRODUCTSPECNAME,:OLDPRODUCTSPECVERSION,    " +
							"  :PRODUCTSPECVERSION,:PROCESSGROUPNAME,:PRODUCTREQUESTNAME,:ORIGINALLOTNAME,:SOURCELOTNAME,:DESTINATIONLOTNAME,:ROOTLOTNAME,:PARENTLOTNAME, :CARRIERNAME,  " +
							"  :OLDPRODUCTTYPE, :PRODUCTTYPE, :OLDSUBPRODUCTTYPE, :SUBPRODUCTTYPE, :SUBPRODUCTUNITQUANTITY1, :SUBPRODUCTUNITQUANTITY2, :PRODUCTQUANTITY,  " +
							"  :SUBPRODUCTQUANTITY,:SUBPRODUCTQUANTITY1, :SUBPRODUCTQUANTITY2, :LOTGRADE, :DUEDATE,  " +
							"  :PRIORITY, :OLDFACTORYNAME, :FACTORYNAME, :OLDDESTINATIONFACTORYNAME, :DESTINATIONFACTORYNAME, :OLDAREANAME, :AREANAME, :LOTSTATE, :LOTPROCESSSTATE, :LOTHOLDSTATE,  " +
							"  :EVENTUSER, :EVENTCOMMENT, :EVENTFLAG, :LASTLOGGEDINTIME, :LASTLOGGEDINUSER, :LASTLOGGEDOUTTIME, :LASTLOGGEDOUTUSER, :REASONCODETYPE, :REASONCODE,  " +
							"  :OLDPROCESSFLOWNAME, :PROCESSFLOWNAME, :OLDPROCESSFLOWVERSION, :PROCESSFLOWVERSION, :OLDPROCESSOPERATIONNAME, :PROCESSOPERATIONNAME, :OLDPROCESSOPERATIONVERSION,  " +
							"  :PROCESSOPERATIONVERSION, :NODESTACK, :MACHINENAME, :MACHINERECIPENAME, :REWORKSTATE, :REWORKCOUNT, :REWORKNODEID, :CONSUMERLOTNAME, :CONSUMERTIMEKEY, :CONSUMEDLOTNAME, " +
							"  :CONSUMEDDURABLENAME, :CONSUMEDCONSUMABLENAME, :SYSTEMTIME, :CANCELFLAG,  :CANCELTIMEKEY, :BRANCHENDNODEID, :NOTE, :BEFOREFLOWNAME, :BEFOREOPERATIONNAME, :ECCODE,  " +
							"  :DEPARTMENTNAME, :RECEIVEFLAG, :SUPERLOTFLAG, :ENDBANK, :SHIPBANK, :SHIPBANKSTATE, :PORTNAME, :LASTLOGGEDOUTMACHINE, :OLDPRODUCTREQUESTNAME, :OLDPRODUCTQUANTITY, :OLDSUBPRODUCTQUANTITY, :OLDSUBPRODUCTQUANTITY1, :OLDSUBPRODUCTQUANTITY2) " ;
					
					Map<String, Object> histbindMap = new HashMap<String, Object>();
					
					histbindMap.put("LOTNAME",(String)map.get("LOTNAME"));
				    histbindMap.put("TIMEKEY", eventInfo.getEventTimeKey());
				    histbindMap.put("EVENTTIME", TimeStampUtil.toTimeString((Timestamp)eventInfo.getEventTime()));
		    		histbindMap.put("EVENTNAME", eventInfo.getEventName());
    				histbindMap.put("OLDPRODUCTIONTYPE",(String)map.get("PRODUCTIONTYPE"));
					histbindMap.put("PRODUCTIONTYPE",(String) map.get("PRODUCTIONTYPE"));
					histbindMap.put("OLDPRODUCTSPECNAME",(String)map.get("PRODUCTSPECNAME"));
					histbindMap.put("PRODUCTSPECNAME", (String) map.get("PRODUCTSPECNAME"));
					histbindMap.put("OLDPRODUCTSPECVERSION",(String)map.get("PRODUCTSPECVERSION"));    
					histbindMap.put("PRODUCTSPECVERSION",(String) map.get("PRODUCTSPECVERSION"));
					histbindMap.put("PROCESSGROUPNAME",(String) map.get("PROCESSGROUPNAME"));
					histbindMap.put("PRODUCTREQUESTNAME",(String) map.get("PRODUCTREQUESTNAME"));
					histbindMap.put("ORIGINALLOTNAME",(String) map.get("ORIGINALLOTNAME"));
					histbindMap.put("SOURCELOTNAME",(String) map.get("SOURCELOTNAME"));
					histbindMap.put("DESTINATIONLOTNAME",(String) map.get("DESTINATIONLOTNAME"));
					histbindMap.put("ROOTLOTNAME",(String) map.get("ROOTLOTNAME")); 
					histbindMap.put("PARENTLOTNAME",(String) map.get("PARENTLOTNAME")); 
					histbindMap.put("CARRIERNAME",(String) map.get("CARRIERNAME")); 
					histbindMap.put("OLDPRODUCTTYPE",(String)map.get("PRODUCTTYPE"));
					histbindMap.put("PRODUCTTYPE",(String) map.get("PRODUCTTYPE")); 
					histbindMap.put("OLDSUBPRODUCTTYPE",(String)map.get("SUBPRODUCTTYPE")); 
					histbindMap.put("SUBPRODUCTTYPE",(String) map.get("SUBPRODUCTTYPE")); 
					histbindMap.put("SUBPRODUCTUNITQUANTITY1", map.get("SUBPRODUCTUNITQUANTITY1").toString()); 
					histbindMap.put("SUBPRODUCTUNITQUANTITY2", map.get("SUBPRODUCTUNITQUANTITY2").toString()); 
					histbindMap.put("PRODUCTQUANTITY", map.get("PRODUCTQUANTITY").toString()); 
					histbindMap.put("SUBPRODUCTQUANTITY",map.get("SUBPRODUCTQUANTITY").toString()); 
					histbindMap.put("SUBPRODUCTQUANTITY1",map.get("SUBPRODUCTQUANTITY1").toString());
					histbindMap.put("SUBPRODUCTQUANTITY2",map.get("SUBPRODUCTQUANTITY2").toString()); 
					histbindMap.put("LOTGRADE",(String) map.get("LOTGRADE")); 
					histbindMap.put("DUEDATE",(Timestamp) map.get("DUEDATE")); 
					histbindMap.put("PRIORITY", map.get("PRIORITY").toString()); 
					histbindMap.put("OLDFACTORYNAME",(String)map.get("FACTORYNAME")); 
					histbindMap.put("FACTORYNAME",(String) map.get("FACTORYNAME")); 
					histbindMap.put("OLDDESTINATIONFACTORYNAME",(String)map.get("DESTINATIONFACTORYNAME"));
					histbindMap.put("DESTINATIONFACTORYNAME",(String) map.get("DESTINATIONFACTORYNAME")); 
					histbindMap.put("OLDAREANAME",(String)map.get("AREANAME"));
					histbindMap.put("AREANAME",(String) map.get("AREANAME")); 
					histbindMap.put("LOTSTATE","Shipped");  
					histbindMap.put("LOTPROCESSSTATE",(String) map.get("LOTPROCESSSTATE")); 
					histbindMap.put("LOTHOLDSTATE",(String) map.get("LOTHOLDSTATE")); 
					histbindMap.put("EVENTUSER",eventInfo.getEventUser()); 
					histbindMap.put("EVENTCOMMENT",eventInfo.getEventComment());
					histbindMap.put("EVENTFLAG",(String) map.get("EVENTFLAG")); 
					histbindMap.put("LASTLOGGEDINTIME",(Timestamp) map.get("LASTLOGGEDINTIME")); 
					histbindMap.put("LASTLOGGEDINUSER",(String) map.get("LASTLOGGEDINUSER"));
					histbindMap.put("LASTLOGGEDOUTTIME",(Timestamp) map.get("LASTLOGGEDOUTTIME")); 
					histbindMap.put("LASTLOGGEDOUTUSER", (String) map.get("LASTLOGGEDOUTUSER"));
					histbindMap.put("REASONCODETYPE", (String) map.get("REASONCODETYPE"));
					histbindMap.put("REASONCODE", (String) map.get("REASONCODE"));
					histbindMap.put("OLDPROCESSFLOWNAME", (String)map.get("PROCESSFLOWNAME")); 
					histbindMap.put("PROCESSFLOWNAME",(String) map.get("PROCESSFLOWNAME")); 
					histbindMap.put("OLDPROCESSFLOWVERSION",(String)map.get("PROCESSFLOWVERSION")); 
					histbindMap.put("PROCESSFLOWVERSION",(String) map.get("PROCESSFLOWVERSION"));
					histbindMap.put("OLDPROCESSOPERATIONNAME",(String)map.get("PROCESSOPERATIONNAME"));
					histbindMap.put("PROCESSOPERATIONNAME",(String) map.get("PROCESSOPERATIONNAME"));
					histbindMap.put("OLDPROCESSOPERATIONVERSION",(String)map.get("PROCESSOPERATIONVERSION")); 
					histbindMap.put("PROCESSOPERATIONVERSION",(String) map.get("PROCESSOPERATIONVERSION")); 
					histbindMap.put("NODESTACK",(String) map.get("NODESTACK")); 
					histbindMap.put("MACHINENAME",(String) map.get("MACHINENAME"));
					histbindMap.put("MACHINERECIPENAME",(String) map.get("MACHINERECIPENAME")); 
					histbindMap.put("REWORKSTATE",(String) map.get("REWORKSTATE")); 
					histbindMap.put("REWORKCOUNT", map.get("REWORKCOUNT").toString());
					histbindMap.put("REWORKNODEID",(String) map.get("REWORKNODEID")); 
					histbindMap.put("CONSUMERLOTNAME",(String) map.get("CONSUMERLOTNAME")); 
					histbindMap.put("CONSUMERTIMEKEY",(String) map.get("CONSUMERTIMEKEY")); 
					histbindMap.put("CONSUMEDLOTNAME",(String) map.get("CONSUMEDLOTNAME"));
					histbindMap.put("CONSUMEDDURABLENAME",(String) map.get("CONSUMEDDURABLENAME")); 
					histbindMap.put("CONSUMEDCONSUMABLENAME",(String) map.get("CONSUMEDCONSUMABLENAME")); 
					histbindMap.put("SYSTEMTIME",(Timestamp)map.get("SYSTEMTIME")); 
					histbindMap.put("CANCELFLAG",(String) map.get("CANCELFLAG"));  
					histbindMap.put("CANCELTIMEKEY",(String) map.get("CANCELTIMEKEY")); 
					histbindMap.put("BRANCHENDNODEID",(String) map.get("BRANCHENDNODEID")); 
					histbindMap.put("NOTE",(String) map.get("NOTE")); 
					histbindMap.put("BEFOREFLOWNAME",(String) map.get("BEFOREFLOWNAME")); 
					histbindMap.put("BEFOREOPERATIONNAME",(String) map.get("BEFOREOPERATIONNAME")); 
					histbindMap.put("ECCODE",(String) map.get("ECCODE")); 
					histbindMap.put("DEPARTMENTNAME",(String) map.get("DEPARTMENTNAME")); 
					histbindMap.put("RECEIVEFLAG",""); 
					histbindMap.put("SUPERLOTFLAG",(String) map.get("SUPERLOTFLAG"));
					histbindMap.put("ENDBANK",(String) map.get("ENDBANK")); 
					histbindMap.put("SHIPBANK",(String) map.get("SHIPBANK"));
					histbindMap.put("SHIPBANKSTATE",(String) map.get("SHIPBANKSTATE")); 
					histbindMap.put("PORTNAME",(String) map.get("PORTNAME")); 
					histbindMap.put("LASTLOGGEDOUTMACHINE",(String) map.get("LASTLOGGEDOUTMACHINE")); 
					histbindMap.put("OLDPRODUCTREQUESTNAME",(String)map.get("PRODUCTREQUESTNAME"));
					histbindMap.put("OLDPRODUCTQUANTITY",map.get("PRODUCTQUANTITY").toString()); 
					histbindMap.put("OLDSUBPRODUCTQUANTITY",map.get("SUBPRODUCTQUANTITY").toString()); 
					histbindMap.put("OLDSUBPRODUCTQUANTITY1",map.get("SUBPRODUCTQUANTITY1").toString()); 
					histbindMap.put("OLDSUBPRODUCTQUANTITY2",map.get("SUBPRODUCTQUANTITY2").toString()); 
					
					GenericServiceProxy.getSqlMesTemplate().update(histSql, histbindMap);
				}
			}	
		}
		catch(Exception ex)
		{
			throw new CustomException("LOT-0232","");
		}
	}
	
	private void updateOnlyArrayProductData(String productName, String arrDBUserName) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelReceive", getEventUser(), getEventComment(), null, null);
		
		try
		{
			//Update Array Product
			String sql = "UPDATE "+arrDBUserName+".PRODUCT SET PRODUCTSTATE = 'Shipped' WHERE PRODUCTNAME = :PRODUCTNAME ";
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PRODUCTNAME", productName);
			
			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
			
			//Select Array Product
			String sql_2 = "SELECT * FROM PRODUCT@ARRAYDB WHERE PRODUCTNAME = :PRODUCTNAME ";
			
			Map<String, Object> bindMap2 = new HashMap<String, Object>();
			bindMap2.put("PRODUCTNAME", productName);
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql_2, bindMap2);
			
			//Insert Durable History
			if ( sqlResult.size() > 0)
			{
				for(Map<String, Object> map : sqlResult)
				{
					String histSql = " INSERT INTO "+arrDBUserName+".PRODUCTHISTORY(PRODUCTNAME,TIMEKEY, EVENTTIME,EVENTNAME,OLDPRODUCTIONTYPE,PRODUCTIONTYPE,OLDPRODUCTSPECNAME,PRODUCTSPECNAME,OLDPRODUCTSPECVERSION,PRODUCTSPECVERSION, " +
							"  MATERIALLOCATIONNAME,PROCESSGROUPNAME,TRANSPORTGROUPNAME,PRODUCTREQUESTNAME,ORIGINALPRODUCTNAME, SOURCEPRODUCTNAME,DESTINATIONPRODUCTNAME,OLDLOTNAME, " +
							"  LOTNAME,CARRIERNAME,POSITION,OLDPRODUCTTYPE,PRODUCTTYPE,OLDSUBPRODUCTTYPE,SUBPRODUCTTYPE,SUBPRODUCTUNITQUANTITY1,SUBPRODUCTUNITQUANTITY2, " +
							"  SUBPRODUCTQUANTITY,SUBPRODUCTQUANTITY1,SUBPRODUCTQUANTITY2,PRODUCTGRADE,SUBPRODUCTGRADES1,SUBPRODUCTGRADES2, " +
							"  DUEDATE,PRIORITY,OLDFACTORYNAME,FACTORYNAME,OLDDESTINATIONFACTORYNAME,DESTINATIONFACTORYNAME,OLDAREANAME,AREANAME,SERIALNO,PRODUCTSTATE,PRODUCTPROCESSSTATE, " +
							"  PRODUCTHOLDSTATE,EVENTUSER,EVENTCOMMENT,EVENTFLAG,LASTPROCESSINGTIME,LASTPROCESSINGUSER,LASTIDLETIME,LASTIDLEUSER,REASONCODETYPE,REASONCODE, " +
							"  OLDPROCESSFLOWNAME,PROCESSFLOWNAME,OLDPROCESSFLOWVERSION, PROCESSFLOWVERSION,OLDPROCESSOPERATIONNAME,PROCESSOPERATIONNAME,OLDPROCESSOPERATIONVERSION, " +
							"  PROCESSOPERATIONVERSION,NODESTACK,MACHINENAME,MACHINERECIPENAME,REWORKSTATE,REWORKCOUNT,REWORKNODEID,CONSUMERLOTNAME,CONSUMERPRODUCTNAME,CONSUMERTIMEKEY, " +
							"  CONSUMEDPRODUCTNAME,CONSUMEDDURABLENAME,CONSUMEDCONSUMABLENAME,SYSTEMTIME,CANCELFLAG,CANCELTIMEKEY,BRANCHENDNODEID,CRATENAME,PROCESSINGINFO,PAIRPRODUCTNAME,   " +
							"  NOTE,VCRPRODUCTNAME,ECCODE,SCRAPDEPARTMENTNAME, OLDSUBPRODUCTQUANTITY, OLDSUBPRODUCTQUANTITY1,OLDSUBPRODUCTQUANTITY2) " +
							"  VALUES " +
							"  (:PRODUCTNAME, :TIMEKEY, TO_DATE(:EVENTTIME, 'YYYY/MM/DD HH24:MI:SS'), :EVENTNAME,:OLDPRODUCTIONTYPE,:PRODUCTIONTYPE,:OLDPRODUCTSPECNAME,:PRODUCTSPECNAME,:OLDPRODUCTSPECVERSION,:PRODUCTSPECVERSION, " +
							"  :MATERIALLOCATIONNAME,:PROCESSGROUPNAME,:TRANSPORTGROUPNAME,:PRODUCTREQUESTNAME,:ORIGINALPRODUCTNAME, :SOURCEPRODUCTNAME,:DESTINATIONPRODUCTNAME,:OLDLOTNAME, " +
							"  :LOTNAME,:CARRIERNAME,:POSITION,:OLDPRODUCTTYPE,:PRODUCTTYPE,:OLDSUBPRODUCTTYPE,:SUBPRODUCTTYPE,:SUBPRODUCTUNITQUANTITY1,:SUBPRODUCTUNITQUANTITY2, " +
							"  :SUBPRODUCTQUANTITY,:SUBPRODUCTQUANTITY1,:SUBPRODUCTQUANTITY2,:PRODUCTGRADE,:SUBPRODUCTGRADES1,:SUBPRODUCTGRADES2, " +
							"  :DUEDATE,:PRIORITY,:OLDFACTORYNAME,:FACTORYNAME,:OLDDESTINATIONFACTORYNAME,:DESTINATIONFACTORYNAME,:OLDAREANAME,:AREANAME,:SERIALNO,:PRODUCTSTATE,:PRODUCTPROCESSSTATE, " +
							"  :PRODUCTHOLDSTATE,:EVENTUSER,:EVENTCOMMENT,:EVENTFLAG,:LASTPROCESSINGTIME,:LASTPROCESSINGUSER,:LASTIDLETIME,:LASTIDLEUSER,:REASONCODETYPE,:REASONCODE, " +
							"  :OLDPROCESSFLOWNAME,:PROCESSFLOWNAME,:OLDPROCESSFLOWVERSION, :PROCESSFLOWVERSION,:OLDPROCESSOPERATIONNAME,:PROCESSOPERATIONNAME,:OLDPROCESSOPERATIONVERSION, " +
							"  :PROCESSOPERATIONVERSION,:NODESTACK,:MACHINENAME,:MACHINERECIPENAME,:REWORKSTATE,:REWORKCOUNT,:REWORKNODEID,:CONSUMERLOTNAME,:CONSUMERPRODUCTNAME,:CONSUMERTIMEKEY, " +
							"  :CONSUMEDPRODUCTNAME,:CONSUMEDDURABLENAME,:CONSUMEDCONSUMABLENAME,:SYSTEMTIME,:CANCELFLAG,:CANCELTIMEKEY,:BRANCHENDNODEID,:CRATENAME,:PROCESSINGINFO,:PAIRPRODUCTNAME,   " +
							"  :NOTE,:VCRPRODUCTNAME,:ECCODE,:SCRAPDEPARTMENTNAME, :OLDSUBPRODUCTQUANTITY, :OLDSUBPRODUCTQUANTITY1, :OLDSUBPRODUCTQUANTITY2) "; 

					Map<String, Object> histbindMap = new HashMap<String, Object>();
					
					histbindMap.put("PRODUCTNAME", (String) map.get("PRODUCTNAME"));
					histbindMap.put("TIMEKEY", eventInfo.getEventTimeKey());
					histbindMap.put("EVENTTIME", TimeStampUtil.toTimeString((Timestamp)eventInfo.getEventTime()));
					histbindMap.put("EVENTNAME", eventInfo.getEventName());
					histbindMap.put("OLDPRODUCTIONTYPE", (String)map.get("PRODUCTIONTYPE"));
					histbindMap.put("PRODUCTIONTYPE", (String) map.get("PRODUCTIONTYPE"));
					histbindMap.put("OLDPRODUCTSPECNAME", (String)map.get("PRODUCTSPECNAME"));
					histbindMap.put("PRODUCTSPECNAME", (String) map.get("PRODUCTSPECNAME"));
					histbindMap.put("OLDPRODUCTSPECVERSION", (String)map.get("PRODUCTSPECVERSION"));
					histbindMap.put("PRODUCTSPECVERSION", (String) map.get("PRODUCTSPECVERSION"));
					histbindMap.put("MATERIALLOCATIONNAME", (String) map.get("MATERIALLOCATIONNAME"));
					histbindMap.put("PROCESSGROUPNAME", (String) map.get("PROCESSGROUPNAME"));
					histbindMap.put("TRANSPORTGROUPNAME", (String) map.get("TRANSPORTGROUPNAME"));
					histbindMap.put("PRODUCTREQUESTNAME", (String) map.get("PRODUCTREQUESTNAME"));
					histbindMap.put("ORIGINALPRODUCTNAME", (String) map.get("ORIGINALPRODUCTNAME"));
					histbindMap.put("SOURCEPRODUCTNAME", (String) map.get("SOURCEPRODUCTNAME"));
					histbindMap.put("DESTINATIONPRODUCTNAME", (String) map.get("DESTINATIONPRODUCTNAME"));
					histbindMap.put("OLDLOTNAME", (String)map.get("LOTNAME"));
					histbindMap.put("LOTNAME", (String) map.get("LOTNAME"));
					histbindMap.put("CARRIERNAME", (String) map.get("CARRIERNAME"));
					histbindMap.put("POSITION",map.get("POSITION").toString());
					histbindMap.put("OLDPRODUCTTYPE", (String)map.get("PRODUCTTYPE"));
					histbindMap.put("PRODUCTTYPE", (String) map.get("PRODUCTTYPE"));
					histbindMap.put("OLDSUBPRODUCTTYPE", (String)map.get("SUBPRODUCTTYPE"));
					histbindMap.put("SUBPRODUCTTYPE", (String) map.get("SUBPRODUCTTYPE"));
					histbindMap.put("SUBPRODUCTUNITQUANTITY1",map.get("SUBPRODUCTUNITQUANTITY1").toString());
					histbindMap.put("SUBPRODUCTUNITQUANTITY2",map.get("SUBPRODUCTUNITQUANTITY2").toString());
					histbindMap.put("SUBPRODUCTQUANTITY",map.get("SUBPRODUCTQUANTITY").toString());
					histbindMap.put("SUBPRODUCTQUANTITY1", map.get("SUBPRODUCTQUANTITY1").toString());
					histbindMap.put("SUBPRODUCTQUANTITY2",map.get("SUBPRODUCTQUANTITY2").toString());
					histbindMap.put("PRODUCTGRADE", (String) map.get("PRODUCTGRADE"));
					histbindMap.put("SUBPRODUCTGRADES1", (String) map.get("SUBPRODUCTGRADES1"));
					histbindMap.put("SUBPRODUCTGRADES2", (String) map.get("SUBPRODUCTGRADES2"));
					histbindMap.put("DUEDATE", (Timestamp) map.get("DUEDATE"));
					histbindMap.put("PRIORITY", map.get("PRIORITY").toString());
					histbindMap.put("OLDFACTORYNAME", (String)map.get("FACTORYNAME"));
					histbindMap.put("FACTORYNAME", (String) map.get("FACTORYNAME"));
					histbindMap.put("OLDDESTINATIONFACTORYNAME", (String)map.get("DESTINATIONFACTORYNAME"));
					histbindMap.put("DESTINATIONFACTORYNAME", (String) map.get("DESTINATIONFACTORYNAME"));
					histbindMap.put("OLDAREANAME",(String)map.get("AREANAME"));
					histbindMap.put("AREANAME", (String) map.get("AREANAME"));
					histbindMap.put("SERIALNO", (String) map.get("SERIALNO"));
					histbindMap.put("PRODUCTSTATE", "Shipped");
					histbindMap.put("PRODUCTPROCESSSTATE", (String) map.get("PRODUCTPROCESSSTATE"));
					histbindMap.put("PRODUCTHOLDSTATE", (String) map.get("PRODUCTHOLDSTATE"));
					histbindMap.put("EVENTUSER", eventInfo.getEventUser());
					histbindMap.put("EVENTCOMMENT", eventInfo.getEventComment());
					histbindMap.put("EVENTFLAG", (String) map.get("EVENTFLAG"));
					histbindMap.put("LASTPROCESSINGTIME", (Timestamp) map.get("LASTPROCESSINGTIME"));
					histbindMap.put("LASTPROCESSINGUSER", (String) map.get("LASTPROCESSINGUSER"));
					histbindMap.put("LASTIDLETIME", (Timestamp) map.get("LASTIDLETIME"));
					histbindMap.put("LASTIDLEUSER", (String) map.get("LASTIDLEUSER"));
					histbindMap.put("REASONCODETYPE", (String) map.get("REASONCODETYPE"));
					histbindMap.put("REASONCODE", (String) map.get("REASONCODE"));
					histbindMap.put("OLDPROCESSFLOWNAME", (String)map.get("PROCESSFLOWNAME"));
					histbindMap.put("PROCESSFLOWNAME", (String) map.get("PROCESSFLOWNAME"));
					histbindMap.put("OLDPROCESSFLOWVERSION", (String)map.get("PROCESSFLOWVERSION"));
					histbindMap.put("PROCESSFLOWVERSION", (String) map.get("PROCESSFLOWVERSION"));
					histbindMap.put("OLDPROCESSOPERATIONNAME", (String)map.get("PROCESSOPERATIONNAME"));
					histbindMap.put("PROCESSOPERATIONNAME", (String) map.get("PROCESSOPERATIONNAME"));
					histbindMap.put("OLDPROCESSOPERATIONVERSION", (String)map.get("PROCESSOPERATIONVERSION"));
					histbindMap.put("PROCESSOPERATIONVERSION", (String) map.get("PROCESSOPERATIONVERSION"));
					histbindMap.put("NODESTACK", (String) map.get("NODESTACK"));
					histbindMap.put("MACHINENAME", (String) map.get("MACHINENAME"));
					histbindMap.put("MACHINERECIPENAME", (String) map.get("MACHINERECIPENAME"));
					histbindMap.put("REWORKSTATE", (String) map.get("REWORKSTATE"));
					histbindMap.put("REWORKCOUNT", map.get("REWORKCOUNT").toString());
					histbindMap.put("REWORKNODEID", (String) map.get("REWORKNODEID"));
					histbindMap.put("CONSUMERLOTNAME", (String) map.get("CONSUMERLOTNAME"));
					histbindMap.put("CONSUMERPRODUCTNAME", (String) map.get("CONSUMERPRODUCTNAME"));
					histbindMap.put("CONSUMERTIMEKEY", (String) map.get("CONSUMERTIMEKEY"));
					histbindMap.put("CONSUMEDPRODUCTNAME", (String) map.get("CONSUMEDPRODUCTNAME"));
					histbindMap.put("CONSUMEDDURABLENAME", (String) map.get("CONSUMEDDURABLENAME"));
					histbindMap.put("CONSUMEDCONSUMABLENAME", (String) map.get("CONSUMEDCONSUMABLENAME"));
					histbindMap.put("SYSTEMTIME", (Timestamp) map.get("SYSTEMTIME"));
					histbindMap.put("CANCELFLAG", (String) map.get("CANCELFLAG"));
					histbindMap.put("CANCELTIMEKEY", (String) map.get("CANCELTIMEKEY"));
					histbindMap.put("BRANCHENDNODEID", (String) map.get("BRANCHENDNODEID"));
					histbindMap.put("CRATENAME", (String) map.get("CRATENAME"));
					histbindMap.put("PROCESSINGINFO", (String) map.get("PROCESSINGINFO"));
					histbindMap.put("PAIRPRODUCTNAME", (String) map.get("PAIRPRODUCTNAME"));					
					histbindMap.put("NOTE", (String)map.get("NOTE"));
					histbindMap.put("VCRPRODUCTNAME", (String) map.get("VCRPRODUCTNAME"));
					histbindMap.put("ECCODE", (String) map.get("ECCODE"));
					histbindMap.put("SCRAPDEPARTMENTNAME", (String) map.get("SCRAPDEPARTMENTNAME"));
					histbindMap.put("OLDSUBPRODUCTQUANTITY",map.get("SUBPRODUCTQUANTITY").toString());
					histbindMap.put("OLDSUBPRODUCTQUANTITY1",map.get("SUBPRODUCTQUANTITY1").toString());
					histbindMap.put("OLDSUBPRODUCTQUANTITY2",map.get("SUBPRODUCTQUANTITY2").toString());
					
					GenericServiceProxy.getSqlMesTemplate().update(histSql, histbindMap);
				}
			}	
		}
		catch(Exception ex)
		{
			throw new CustomException("LOT-0233","");
		}
	}
	
	private void deleteHQGlassJudge(EventInfo eventInfo, String glassName) throws CustomException
	{
		try
		{
			String sql = "SELECT * FROM CT_HQGLASSJUDGE WHERE GLASSNAME = :GLASSNAME";
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("GLASSNAME", glassName);
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

			if ( sqlResult.size() > 0)
			{
				for(Map<String, Object> map : sqlResult)
				{
					HQGlassJudge HQGlassJudgeData = ExtendedObjectProxy.getHQGlassJudgeService().selectByKey(false, new Object[]{(String)map.get("HQGLASSNAME").toString()});
					
					ExtendedObjectProxy.getHQGlassJudgeService().remove(eventInfo, HQGlassJudgeData);
				}
			}
		}
		catch(Exception ex)
		{
			eventLog.error("HQ Glass Data Remove Fail ! ");
			throw new CustomException("LOT-0240", "");
		}
	}
	
	private void deletePanelJudge(EventInfo eventInfo, String glassName) throws CustomException
	{
		try
		{
			String sql = "SELECT * FROM CT_PANELJUDGE WHERE GLASSNAME = :GLASSNAME";
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("GLASSNAME", glassName);
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

			if ( sqlResult.size() > 0)
			{
				for(Map<String, Object> map : sqlResult)
				{
					PanelJudge panelJudgeData = ExtendedObjectProxy.getPanelJudgeService().selectByKey(false, new Object[]{(String) map.get("PANELNAME")});
					
					ExtendedObjectProxy.getPanelJudgeService().remove(eventInfo, panelJudgeData);	
				}
			}	
		}
		catch(Exception ex)
		{
			eventLog.error("Panel Judge Data Create Fail ! ");
			throw new CustomException("LOT-0241", "");
		}
	}
	
	//Added by jjyoo on 2018.08.29
	private List<Object> getLotHistInsertArgList(EventInfo eventInfo, String lotName, String productName, String toFactoryName, String toProductSpec) throws CustomException
	{			
		List<Object> bindList = new ArrayList<Object>();
		
		try
		{
			String sql = "SELECT * FROM LOT WHERE LOTNAME = :LOTNAME";
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTNAME", lotName);
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			
			if ( sqlResult.size() > 0)
			{
				for(Map<String, Object> map : sqlResult)
				{
					
					double subProductQuantity = Long.parseLong(map.get("SUBPRODUCTQUANTITY1").toString()) / Long.parseLong(map.get("PRODUCTQUANTITY").toString());
					
					bindList.add(productName);
					bindList.add(eventInfo.getEventTimeKey());
					bindList.add(eventInfo.getEventTime());
					bindList.add(eventInfo.getEventName());
					bindList.add((String) map.get("PRODUCTIONTYPE"));
					bindList.add((String) map.get("PRODUCTSPECNAME"));
					bindList.add((String) map.get("PRODUCTSPECVERSION"));
					bindList.add((String) map.get("PROCESSGROUPNAME"));
					bindList.add((String) map.get("PRODUCTREQUESTNAME"));
					bindList.add(lotName);
					bindList.add((String) map.get("SOURCELOTNAME"));
					bindList.add((String) map.get("DESTINATIONLOTNAME"));
					bindList.add((String) map.get("ROOTLOTNAME"));
					bindList.add((String) map.get("PARENTLOTNAME"));
					bindList.add((String) map.get("CARRIERNAME"));
					bindList.add((String) map.get("PRODUCTTYPE"));
					bindList.add((String) map.get("SUBPRODUCTTYPE"));
					bindList.add(String.valueOf(subProductQuantity));
					bindList.add(1);
					bindList.add(String.valueOf(subProductQuantity));
					bindList.add(map.get("SUBPRODUCTQUANTITY1").toString());
					bindList.add(map.get("SUBPRODUCTQUANTITY2").toString());
					bindList.add((String) map.get("LOTGRADE"));
					bindList.add((Timestamp)map.get("DUEDATE"));
					bindList.add(map.get("PRIORITY").toString());
					bindList.add(toFactoryName);
					bindList.add((String) map.get("DESTINATIONFACTORYNAME"));
					bindList.add((String) map.get("AREANAME"));
					bindList.add("Received");
					bindList.add((String) map.get("LOTPROCESSSTATE"));
					bindList.add((String) map.get("LOTHOLDSTATE"));
					//bindList.add((Timestamp)map.get("DUEDATE"));
					bindList.add((String) map.get("PROCESSFLOWNAME"));
					bindList.add((String) map.get("PROCESSFLOWVERSION"));
					bindList.add(""); //ProcessOperationName
					bindList.add((String) map.get("PROCESSOPERATIONVERSION"));
					bindList.add((String) map.get("REWORKSTATE"));
					bindList.add((String) map.get("NOTE"));
					bindList.add((String) map.get("ECCODE"));
					bindList.add((String) map.get("SHIPBANK"));
					bindList.add("");
					bindList.add(eventInfo.getEventUser());
					
					//Added by jjyoo on 2018.10.7 : set lot history 'OLD' releated column value
					bindList.add((String)map.get("PRODUCTIONTYPE"));
					bindList.add((String)map.get("PRODUCTSPECNAME"));
					bindList.add((String)map.get("PRODUCTSPECVERSION"));
					bindList.add((String)map.get("PRODUCTSPEC2NAME"));
					bindList.add((String)map.get("PRODUCTSPEC2VERSION"));
					bindList.add((String)map.get("PRODUCTTYPE"));
					bindList.add((String)map.get("SUBPRODUCTTYPE"));
					bindList.add(map.get("PRODUCTQUANTITY").toString());
					bindList.add(map.get("SUBPRODUCTQUANTITY").toString());
					bindList.add(map.get("SUBPRODUCTQUANTITY1").toString());
					bindList.add(map.get("SUBPRODUCTQUANTITY2").toString());
					bindList.add((String)map.get("FACTORYNAME"));
					bindList.add((String)map.get("DESTINATIONFACTORYNAME"));
					bindList.add((String)map.get("AREANAME"));
					bindList.add((String)map.get("PROCESSFLOWNAME"));
					bindList.add((String)map.get("PROCESSFLOWVERSION"));
					bindList.add((String)map.get("PROCESSOPERATIONNAME"));
					bindList.add((String)map.get("PROCESSOPERATIONVERSION"));
					bindList.add((String)map.get("PRODUCTREQUESTNAME"));
				}
			}
		}
		catch(Exception ex)
		{
			eventLog.error("Lot Delete Fail ! ");
			throw new CustomException("LOT-0242", "");
		}
		return bindList;
	}
	
	//Added by jjyoo on 2018.08.29
	private List<Object> getProductHistInsertArgList(EventInfo eventInfo, String productName, String toFactoryName) throws CustomException
	{			
		List<Object> bindList = new ArrayList<Object>();
		
		try
		{
			String sql = "SELECT * FROM PRODUCT WHERE PRODUCTNAME = :PRODUCTNAME";
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PRODUCTNAME", productName);
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

			if ( sqlResult.size() > 0)
			{	
				for(Map<String, Object> map : sqlResult)
				{
					bindList.add(productName);
					bindList.add(eventInfo.getEventTimeKey());
					bindList.add(eventInfo.getEventTime());
					bindList.add(eventInfo.getEventName());
					bindList.add((String)map.get("PRODUCTIONTYPE"));
					bindList.add((String)map.get("PRODUCTSPECVERSION")); 
					bindList.add((String)map.get("LOTNAME"));
					bindList.add((String)map.get("SUBPRODUCTTYPE"));
					bindList.add(map.get("SUBPRODUCTQUANTITY").toString());
					bindList.add(map.get("SUBPRODUCTQUANTITY1").toString());
					bindList.add(map.get("SUBPRODUCTQUANTITY2").toString());
					bindList.add((String)map.get("FACTORYNAME"));
					bindList.add((String)map.get("DESTINATIONFACTORYNAME"));
					bindList.add((String)map.get("AREANAME"));
					bindList.add((String)map.get("PROCESSFLOWNAME"));
					bindList.add((String)map.get("PROCESSFLOWVERSION"));
					bindList.add((String)map.get("PROCESSOPERATIONNAME"));
					bindList.add((String)map.get("PROCESSOPERATIONVERSION"));
					bindList.add((String) map.get("PRODUCTIONTYPE"));
					bindList.add((String) map.get("PRODUCTSPECNAME"));
					bindList.add((String) map.get("PRODUCTSPECVERSION"));
					bindList.add((String) map.get("PRODUCTREQUESTNAME"));
					bindList.add((String) map.get("ORIGINALPRODUCTNAME"));
					bindList.add(productName); //LotName
					bindList.add((String) map.get("CARRIERNAME"));
					bindList.add(Long.parseLong(map.get("POSITION").toString()));
					bindList.add((String) map.get("PRODUCTTYPE"));
					bindList.add((String) map.get("SUBPRODUCTTYPE"));
					bindList.add(Double.parseDouble(map.get("SUBPRODUCTUNITQUANTITY1").toString()));
					bindList.add(Double.parseDouble(map.get("SUBPRODUCTUNITQUANTITY2").toString()));
					bindList.add(Double.parseDouble(map.get("SUBPRODUCTQUANTITY").toString()));
					bindList.add(Double.parseDouble(map.get("SUBPRODUCTUNITQUANTITY1").toString()));
					bindList.add(Double.parseDouble(map.get("SUBPRODUCTUNITQUANTITY2").toString()));
					bindList.add((String) map.get("PRODUCTGRADE"));
					bindList.add((Timestamp) map.get("DUEDATE"));
					bindList.add(Long.parseLong(map.get("PRIORITY").toString()));
					bindList.add(toFactoryName); //FactoryName
					bindList.add(""); //DestinationFactoryName
					bindList.add(""); //AreaName
					bindList.add("Received"); //ProductState
					bindList.add((String) map.get("PROCESSFLOWNAME"));
					bindList.add((String) map.get("PROCESSFLOWVERSION"));
					bindList.add(""); //ProcessOperationName
					bindList.add((String) map.get("PROCESSOPERATIONVERSION"));
					bindList.add(""); //NodeStack
					bindList.add((String) map.get("REWORKSTATE"));
					bindList.add(Long.parseLong(map.get("REWORKCOUNT").toString()));
					bindList.add((String) map.get("NOTE"));
					bindList.add((String) map.get("ECCODE"));
				}		
			}
		}
		catch(Exception ex)
		{
			eventLog.error("Product Delete Fail ! ");
			throw new CustomException("LOT-0243", "");
		}
		return bindList;
	}
	
	@SuppressWarnings("unchecked")
	private boolean validateLotData(String lotName, String factoryName) throws CustomException
	{
		try
		{
			String sql = "SELECT L.LOTNAME " +
						 "FROM LOT L " +
						 "WHERE L.LOTNAME = :LOTNAME AND L.LOTSTATE = :LOTSTATE AND L.DESTINATIONFACTORYNAME = :DESTINATIONFACTORYNAME " ;
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTNAME", lotName);
			bindMap.put("DESTINATIONFACTORYNAME", factoryName);
			bindMap.put("LOTSTATE", "Received");
			
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

			if ( sqlResult.size() == 0)
			{
				return false;
			}
			
			return true;
		}
		catch(Exception ex)
		{
			return false;
		}
	}
	
	private void ReleaseHold(Lot lotData ,EventInfo eventInfo)
	{
		
		try {
		
			String eventComment ="Array Defect Out Of Spec";
			eventInfo.setEventComment(eventComment);
						
			// validation
			if (lotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().Flag_Y)){
				LotMultiHoldRelease(eventInfo,lotData.getKey().getLotName(), " ", "");
			}	
		} catch (Exception e) {}
	}
	
	private void LotMultiHoldRelease(EventInfo eventInfo, String lotName, String department, String note) 
	{
		try
		{
			String strSql = 
				"    SELECT A.LOTNAME  " +
				"    , A.PROCESSOPERATIONNAME  "+ 
				"    , A.CARRIERNAME  "+
				"    , A.PRODUCTQUANTITY  "+ 
				"    , A.LOTHOLDSTATE  "+
				"    , A.LOTSTATE  "+
				"    , A.PRODUCTSPECNAME  "+ 
				"    , A.ECCODE  "+
				"    , A.PROCESSFLOWNAME  "+ 
				"    , A.PRODUCTREQUESTNAME  "+ 
				"    , TO_CHAR(B.EVENTTIME,'YYYY-MM-DD HH24:MI:SS') HOLDTIME  "+ 
				"    , B.EVENTUSER HOLDUSER  "+ 
				"    , B.EVENTNAME HOLDEVENTNAME  "+ 
				"    , B.REASONCODE HOLDREASONCODE  "+ 
				"    , B.DEPARTMENT HOLDDEPT    "+
				"    , B.EVENTCOMMENT HOLDNOTE  "+ 
				"    , B.EVENTTIMEKEY HOLDTIMEKEY  "+ 
				"  FROM  LOT A  "+
				"   INNER JOIN CT_LOTMULTIHOLD B ON A.LOTNAME = B.LOTNAME   "+ 
				" WHERE A.LOTNAME = :LOTNAME  "+
				"  AND B.EVENTCOMMENT = 'Array Defect Out Of Spec'  "
				; 
				   
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTNAME", lotName);
	
			List<Map<String, Object>> HoldList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
			
			if(HoldList != null && HoldList.size() > 0)
			{
				for( int i=0; i<HoldList.size();i++){
					
					String  sLOTNAME = lotName;  
					String  sProcessoperationName = (String)HoldList.get(i).get("PROCESSOPERATIONNAME");
					String  sReleaseUser = eventInfo.getEventUser();
					String  sReleaseDept = department;
					String  sCarrierName = (String)HoldList.get(i).get("CARRIERNAME"); 
					String  sProductQuantity = HoldList.get(i).get("PRODUCTQUANTITY").toString(); 
					
					String  sLotholdState = (String)HoldList.get(i).get("LOTHOLDSTATE");  
					String  sLotState = (String)HoldList.get(i).get("LOTSTATE");
					String  sProductspecName = (String)HoldList.get(i).get("PRODUCTSPECNAME");
					String  sEcCode = (String)HoldList.get(i).get("ECCODE"); 
					String  sProcessflowName = (String)HoldList.get(i).get("PROCESSFLOWNAME");
					String  sProductrequestName = (String)HoldList.get(i).get("PRODUCTREQUESTNAME");

					String  sReleaseNote = note;
					String  sHoldTime = (String)HoldList.get(i).get("HOLDTIME") +".000";
					
					String  sHoldUser = (String)HoldList.get(i).get("HOLDUSER"); 
					String  sHoldeventName = (String)HoldList.get(i).get("HOLDEVENTNAME");
					String  sHoldreasonCode = (String)HoldList.get(i).get("HOLDREASONCODE");  
					String  sHoldDept = (String)HoldList.get(i).get("HOLDDEPT");
					String  sHoldNote = (String)HoldList.get(i).get("HOLDNOTE");
					String  sHoldTimeKey = (String)HoldList.get(i).get("HOLDTIMEKEY");
					
					String sql = "INSERT INTO CT_LOTMULTIHOLDRELEASE "
							+ " (LOTNAME,PROCESSOPERATIONNAME, RELEASETIME, RELEASEUSER, RELEASEDEPT, CARRIERNAME, PRODUCTQUANTITY,HOLDTIME"
							+ "  ,LOTHOLDSTATE, LOTSTATE, PRODUCTSPECNAME, ECCODE, PROCESSFLOWNAME, PRODUCTREQUESTNAME"
							+ "  ,RELEASENOTE, HOLDUSER, HOLDEVENTNAME, HOLDREASONCODE, HOLDDEPT, HOLDNOTE "
							+ "  ,LASTEVENTNAME,LASTEVENTTIMEKEY,LASTEVENTTIME,LASTEVENTUSER,LASTEVENTCOMMENT, HOLDTIMEKEY "
							+ "  )"
	 						+ " VALUES "
							+ " (:lotName, :processoperationName, :releaseTime, :releaseUser, :releaseDept, :carrierName, :productQuantity, :holdTime"
							+ "  ,:lotholdState, :lotState, :productspecName, :ecCode, :processflowName, :productrequestName "
							+ "  ,:releaseNote, :holdUser, :holdeventName, :holdreasonCode, :holdDept, :holdNote "
							+ "  ,:lasteventName, :lasteventTimekey, :lasteventTime, :lasteventUser, :lasteventCommet, :holdTimeKey "
							+ "  ) "; 
	 				
					Map<String,Object> InsertbindMap = new HashMap<String,Object>();
		
					Calendar cal =  Calendar.getInstance();
					cal.setTimeInMillis(eventInfo.getEventTime().getTime());
					cal.add(Calendar.SECOND, i);
					Timestamp later = new Timestamp(cal.getTime().getTime());
					InsertbindMap.put("lotName", sLOTNAME);
					InsertbindMap.put("processoperationName", sProcessoperationName);
					InsertbindMap.put("releaseTime", later);
					InsertbindMap.put("releaseUser", sReleaseUser);
					InsertbindMap.put("releaseDept", sReleaseDept);
					InsertbindMap.put("carrierName", sCarrierName);
					InsertbindMap.put("productQuantity", sProductQuantity);
					InsertbindMap.put("holdTime", TimeStampUtil.getTimestamp(sHoldTime ));
					
					InsertbindMap.put("lotholdState", sLotholdState);
					InsertbindMap.put("lotState", sLotState);
					InsertbindMap.put("productspecName", sProductspecName);
					InsertbindMap.put("ecCode", sEcCode);
					InsertbindMap.put("processflowName", sProcessflowName);
					InsertbindMap.put("productrequestName", sProductrequestName);
					
					InsertbindMap.put("releaseNote", sReleaseNote);	
					InsertbindMap.put("holdUser", sHoldUser);
					InsertbindMap.put("holdeventName", sHoldeventName);
					InsertbindMap.put("holdreasonCode", sHoldreasonCode);
					InsertbindMap.put("holdDept", sHoldDept);
					InsertbindMap.put("holdNote", sHoldNote);
		
					InsertbindMap.put("lasteventName", eventInfo.getEventName());
					InsertbindMap.put("lasteventTimekey", eventInfo.getEventTimeKey());
					InsertbindMap.put("lasteventUser", eventInfo.getEventUser()) ;
					InsertbindMap.put("lasteventCommet", eventInfo.getEventComment());
					InsertbindMap.put("lasteventTime", eventInfo.getEventTime());
					InsertbindMap.put("holdTimeKey", sHoldTimeKey);
					
					long rv = GenericServiceProxy.getSqlMesTemplate().update(sql, InsertbindMap);
					
		            try {
		            	LotMultiHold lotMultiHoldData = ExtendedObjectProxy.getLotMultiHoldService().selectByKey(false, new Object[] {lotName, sHoldreasonCode, " ", "Array Defect Out Of Spec"});
		                ExtendedObjectProxy.getLotMultiHoldService().remove(eventInfo, lotMultiHoldData);

					} catch (Exception e) {
						eventLog.warn("Not exist LotMultiHold : LotName :"+sLOTNAME+" ReasonCode :"+sHoldreasonCode+" Department :"+sHoldDept+ " EventComment :"+sHoldNote);
		                throw new CustomException("HOLD-0001",lotName,sHoldreasonCode,sHoldDept,sHoldNote);   
					}
					// delete product multi hold data
					List <Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
					for(Product eleProduct : productList) {
						try	{
							MESProductServiceProxy.getProductServiceImpl().removeMultiHoldProduct(eleProduct.getKey().getProductName(), sHoldreasonCode, sHoldDept, eventInfo);
						} catch(Exception ex) {
							eventLog.warn(eleProduct.getKey().getProductName() + "'s ProductMultiHold not found");
						}
					}

				}
		
			}
			
		}
		catch (Exception ex)
		{
			eventLog.warn("CT_LOTMULTIHOLDRELEASE ERROR");
		}
	}
	
}
