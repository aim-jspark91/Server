package kr.co.aim.messolution.lot.event.CNX;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.lot.service.LotServiceUtil;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.lot.management.info.MakeReceivedInfo;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.messolution.userprofile.MESUserServiceProxy;

import org.apache.commons.dbcp.BasicDataSource;
import org.jdom.Document;
import org.jdom.Element;

public class ReceiveLot extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		//////////////////////////////////
		// Receive Lot 2018.04.27 dmlee //
		//////////////////////////////////
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Receive", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "TOPRODUCTSPECNAME", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "TOFACTORYNAME", true);
		String fromFactoryName = SMessageUtil.getBodyItemValue(doc, "FROMFACTORYNAME", true);
		String fromProductSpecName = SMessageUtil.getBodyItemValue(doc, "FROMPRODUCTSPECNAME", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		
		ProductSpec productSpecData = CommonUtil.getProductSpecByProductSpecName( factoryName, productSpecName, "00001" );
		
		List<Element> eLotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		
		for (Element eLot : eLotList)
		{
			String lotName = SMessageUtil.getChildText(eLot, "LOTNAME", true);
			String carrierName = SMessageUtil.getChildText(eLot, "DURABLENAME", true);
			
			Lot lot = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			List<Product> productList = ProductServiceProxy.getProductService().allProductsByLot(lot.getKey().getLotName());

			if(!this.validateProductData(lotName, factoryName, carrierName))
			{
				throw new CustomException("PRODUCT-0001",lotName);
			}
			
			//2019.02.25 dmlee : mantis(2831) Add Validation Panel Judge (If Panel Judge is not 'G','O','X' then, Can't Receive
			if(!this.validationPanelJudge(factoryName, lotName))
			{
				throw new CustomException("COMMON-0001","Lot ["+lotName+"] Panel Judge is not 'G','O','X' Can't Receive ! ");
			}
			
			ProductSpec productSpec = CommonUtil.getProductSpecByProductSpecName( factoryName, productSpecName, "00001" );
			List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotName);
			if ( LotServiceUtil.isShipForCOA( productSpec, lot ) )
			{
				StringBuffer sbSql = new StringBuffer();
				sbSql.append( "SELECT RPS.TOFACTORYNAME,RPS.TOPRODUCTSPECNAME, RPS.TOPROCESSOPERATIONNAME " );
				sbSql.append( "FROM TPPOLICY TP, POSFACTORYRELATION RPS " );
				sbSql.append( "WHERE     TP.CONDITIONID = RPS.CONDITIONID " );
				sbSql.append( "AND TP.FACTORYNAME = :FACTORYNAME " );
				sbSql.append( "AND TP.PRODUCTSPECNAME = :FROMPRODUCTSPECNAME " );
				sbSql.append( "AND RPS.JOBTYPE = :JOBTYPE " );
				sbSql.append( "AND RPS.TOFACTORYNAME=:TOFACTORYNAME " );
				sbSql.append( "AND RPS.TOPRODUCTSPECNAME=:PRODUCTSPECNAME " );

				Map<String, Object> mBind = new HashMap<String, Object>();
				mBind.put( "FACTORYNAME", fromFactoryName );
				mBind.put( "PRODUCTSPECNAME", productSpecName );
				mBind.put( "JOBTYPE", "Receive" );
				mBind.put( "TOFACTORYNAME", factoryName );
				mBind.put( "FROMPRODUCTSPECNAME", fromProductSpecName );
				// mBind.put("PRODUCTIONTYPE", productionDetailType );

				List<Map<String, Object>> results = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList( sbSql.toString(), mBind );

				if ( results == null || results.size() == 0 ) { throw new CustomException( "Lot-0059" ); }
				eventInfo.setEventName( "ReceiveLot" );
				
				if(factoryName.equals("CELL"))
				{
					lot.setLotState(GenericServiceProxy.getConstantMap().Lot_Received);
					lot.setLotProcessState("");
					lot.setLotHoldState("");
				}
				else
				{
					lot.setLotState(GenericServiceProxy.getConstantMap().Lot_Released);
					lot.setLotProcessState(GenericServiceProxy.getConstantMap().Lot_Wait);
					lot.setLotHoldState("N");
				}
				
				//ExitedFabQTime
				for(Product productData : productList)
				{
					productData.setProductState( GenericServiceProxy.getConstantMap().Prod_InProduction );
					productData.setProductProcessState( GenericServiceProxy.getConstantMap().Prod_Idle );
					productData.setProductHoldState( GenericServiceProxy.getConstantMap().Prod_NotOnHold );
					ProductServiceProxy.getProductService().update( productData );
				}	
				
				factoryName = ConvertUtil.getMapValueByName( results.get( 0 ), "TOFACTORYNAME" );
				productSpecName = ConvertUtil.getMapValueByName( results.get( 0 ), "TOPRODUCTSPECNAME" );
				String processOperationName = ConvertUtil.getMapValueByName( results.get( 0 ), "TOPROCESSOPERATIONNAME" );
				
				if(processOperationName.isEmpty()){
					ProcessOperationSpec processOperationSpec = CommonUtil.getFirstOperation(factoryName, processFlowName);
					processOperationName = processOperationSpec.getKey().getProcessOperationName();
				}
				
				String nodeId = CommonUtil.getNodeStack(factoryName, processFlowName, processOperationName);

				ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();
				changeSpecInfo.setFactoryName( factoryName );
				changeSpecInfo.setProductSpecName( productSpecName );
				changeSpecInfo.setProcessOperationName( processOperationName );
				changeSpecInfo.setProductSpecVersion("00001");
				changeSpecInfo.setProcessFlowName( processFlowName );
				changeSpecInfo.setNodeStack( nodeId );
				changeSpecInfo.setProductRequestName( lot.getProductRequestName() );
				changeSpecInfo.setProductionType( lot.getProductionType() );
				changeSpecInfo.setProductUSequence(productUdfs);
				changeSpecInfo.setPriority(lot.getPriority());
				changeSpecInfo.setLotState(lot.getLotState());
				changeSpecInfo.setLotHoldState(lot.getLotHoldState());
				changeSpecInfo.setLotProcessState(lot.getLotProcessState());
				Map<String, String> userColumns = new HashMap<String, String>();
				userColumns.put("RECEIVEFLAG", GenericServiceProxy.getConstantMap().Flag_Y);
				userColumns.put("BEFOREFLOWNAME", lot.getProcessFlowName());
				userColumns.put("BEFOREOPERATIONNAME", lot.getProcessOperationName());
				changeSpecInfo.setUdfs( userColumns );
				
				LotServiceProxy.getLotService().changeSpec( lot.getKey(), eventInfo, changeSpecInfo );

			}else{
				String eventUser = eventInfo.getEventUser();
				
				MakeReceivedInfo makeReceivedInfo = new MakeReceivedInfo();
				makeReceivedInfo.setProductSpecName( productSpecData.getKey().getProductSpecName() );
				makeReceivedInfo.setProductSpecVersion( productSpecData.getKey().getProductSpecVersion() );
				makeReceivedInfo.setProductRequestName( lot.getProductRequestName() );
				makeReceivedInfo.setProcessFlowName( productSpecData.getProcessFlowName() );
				makeReceivedInfo.setProcessFlowVersion( "00001" );
				makeReceivedInfo.setProductionType( lot.getProductionType() );
				makeReceivedInfo.setProductType( productSpecData.getProductType() );
				makeReceivedInfo.setAreaName("01 PI");
				makeReceivedInfo.setSubProductType( productSpecData.getSubProductType() );
	
				makeReceivedInfo.setProductUSequence( productUdfs );
				
				Map<String, String> userColumns = new HashMap<String, String>();
	
				userColumns.put("RECEIVEFLAG", GenericServiceProxy.getConstantMap().Flag_Y);
				userColumns.put("BEFOREFLOWNAME", lot.getProcessFlowName());
				userColumns.put("BEFOREOPERATIONNAME", lot.getProcessOperationName());
				
				makeReceivedInfo.setUdfs( userColumns );
	
				LotServiceProxy.getLotService().makeReceived( lot.getKey(), eventInfo, makeReceivedInfo );
				eventLog.info( "Event Name = " + eventInfo.getEventName() + " , EventTimeKey = " + eventInfo.getEventTimeKey() );
			
			}
			
			//2019.02.25 dmlee : -------------------------------------------------------------------------------------------------
			
			/*//20180504, kyjung, QTime
			try
			{
				MESProductServiceProxy.getProductServiceImpl().checkFabQTime(lotName, factoryName);
			}
			catch(Exception ex)
			{
				throw ex;
			}*/
			
			// 2019.05.23_hsryu_Add Validation. Mantis 0003973.
			// 2020.09.22_hsryu_Delete Logic. 
			//this.validatePanelJudgeAndHQGlassJudge(factoryName, lotName);
						
			//Receive ARRAY CST Data
			try
			{
				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
				
				if(durableData != null)
				{
					durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
					//2019.04.02 dmlee : durable Factory must be update TEX
					//durableData.setFactoryName("OLED");
					//durableData.setLotQuantity(productList.size());
					durableData.setLastEventComment(eventInfo.getEventComment());
					durableData.setLastEventName(eventInfo.getEventName());
					durableData.setLastEventTime(eventInfo.getEventTime());
					durableData.setLastEventTimeKey(eventInfo.getEventTimeKey());
					durableData.setLastEventUser(eventInfo.getEventUser());
					
					SetEventInfo setEvetnInfo = new SetEventInfo();
					eventInfo.setEventName("Receive");
					DurableServiceProxy.getDurableService().update(durableData);
					
					DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEvetnInfo);
				}
			}
			catch(CustomException ex)
			{
				// h5는 DB 하나로 가니까 주석.
				//this.getCST(eventInfo, carrierName, toFactoryName, productList.size(), factoryName, receiveBank);
			}
			catch(NotFoundSignal e)
			{
				// h5는 DB 하나로 가니까 주석.
				//this.getCST(eventInfo, carrierName, toFactoryName, productList.size(), factoryName, receiveBank);
			}
			
			//Update Array CST Data
			try
			{
				this.updateOnlyCSTData(carrierName, productList.size(), factoryName);
			}
			catch(Exception ex)
			{
				throw new CustomException("LOT-0226","");
			}
			
			
			
			
			//------------------------------------------------------------------------------------------------------------------------------------------------Procedure Receive Lot
			//18.12.05 dmlee : ReceiveLot Using Procedure--------------------------------
//			try	
//			{
//				//Delete CT_RECEIVELOT_TEMP
//				StringBuilder sql = new StringBuilder();
//				Map<String, Object> args = new HashMap<String, Object>();
//				sql.append(" DELETE FROM CT_RECEIVEDLOT_TEMP " );
//				GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().update(sql.toString(), args);
//				
//				//Insert CT_RECEIVELOT_TEMP
//				for(String productName : productList)
//				{
//					sql = new StringBuilder();
//					sql.setLength(0);
//					sql.append(" INSERT INTO CT_RECEIVEDLOT_TEMP ( LOTNAME,PRODUCTNAME,CARRIERNAME, RECEIVEBANK, EVENTUSER  )" );
//					sql.append(" VALUES (  :LOTNAME, :PRODUCTNAME, :CARRIERNAME, :RECEIVEBANK, :EVENTUSER )" );
//					args = new HashMap<String, Object>();
//					args.put("LOTNAME",lotName);
//					args.put("PRODUCTNAME",productName);
//					args.put("CARRIERNAME",carrierName);
//					args.put("RECEIVEBANK", receiveBank);
//					args.put("EVENTUSER", eventUser);
//					GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().update(sql.toString(), args);
//				}
//			
//			}
//			catch(Exception e)
//			{
//				throw new CustomException( e.getMessage() );
//			}
			//18.12.05 dmlee : ------------------------------------------------------------
			
			//Procedure Execute
//			try
//			{		
//				GenericServiceProxy.getSqlMesTemplate().executeProcedure("P_MAKE_RECEIVED_DATA");
//			}
//			catch(Exception e)
//			{
//				throw new CustomException("COMMON-0001", e.getMessage() );
//			}
			
		/*	try
			{
				// Receive Lot
//				List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotName);
//				
//				ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();
//				changeSpecInfo.setLotState(GenericServiceProxy.getConstantMap().Lot_Received);
//				changeSpecInfo.setProductUSequence(productUdfs);
//				
//				lot = MESLotServiceProxy.getLotServiceImpl().changeProductSpec(eventInfo, lot, changeSpecInfo);
				
				lot.setFactoryName(lot.getDestinationFactoryName());
				lot.setLotState(GenericServiceProxy.getConstantMap().Lot_Released);
				lot.setLastEventComment(eventInfo.getEventComment());
				lot.setLastEventName(eventInfo.getEventName());
				lot.setLastEventTime(eventInfo.getEventTime());
				lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
				lot.setLastEventUser(eventInfo.getEventUser());
				
				kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
				LotServiceProxy.getLotService().update(lot);
								
				lot.getUdfs().put("RECEIVEFLAG", GenericServiceProxy.getConstantMap().Flag_Y);
				setEventInfo.setUdfs(lot.getUdfs());

				LotServiceProxy.getLotService().setEvent(lot.getKey(), eventInfo, setEventInfo);
								
				for(Product productData : productList)
				{
					productData.setFactoryName(productData.getDestinationFactoryName());
					productData.setProductState(GenericServiceProxy.getConstantMap().Lot_Released);
					productData.setLastEventComment(eventInfo.getEventComment());
					productData.setLastEventName(eventInfo.getEventName());
					productData.setLastEventTime(eventInfo.getEventTime());
					productData.setLastEventTimeKey(eventInfo.getEventTimeKey());
					productData.setLastEventUser(eventInfo.getEventUser());
					
					ProductServiceProxy.getProductService().update(productData);
					
					kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfoForProduct = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
					ProductServiceProxy.getProductService().setEvent(productData.getKey(), eventInfo, setEventInfoForProduct);
				}
			}
			catch(Exception ex)
			{
			}			
			*/
			
			/*//ExitedFabQTime
			for(Product productData : productList)
			{
				//Lot lot = MESLotServiceProxy.getLotInfoUtil().getLotData(productName);
				Product product = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productData.getKey().getProductName());
				
				//20180504, kyjung, QTime
				MESProductServiceProxy.getProductServiceImpl().ExitedFabQTime(eventInfo, product, "Receive", factoryName);
				
				//20181205, ParkJeongSu, QTimeClosed
				// ExitedFabQTime 에서 OperationName이 '-'로 수정되어있는데, Receive는 '-' 이 아님
				// 수정해야될 필요성 있음.
				//MESProductServiceProxy.getProductServiceImpl().ClosedFabQTime(eventInfo, lot, "Receive");
			}	
			*/

			//------------------------------------------------------------------------------------------------------------------------------------------------Procedure Receive Lot 

			
			// After Receive if Product have Defect Hold OR Send Mail Made By Park Jeong Su
/*			this.DefectHoldProduct(eventInfo, productList, carrierName, factoryName);
			this.DefectMailProduct(eventInfo, productList, carrierName, factoryName);*/
			
			

		}
		
		
		return doc;
	}
	
	private boolean validationPanelJudge(String factoryName,
			String lotName) {
		try
		{
			String sql = "SELECT A.PANELNAME, A.PANELJUDGE FROM CT_PANELJUDGE A WHERE A.GLASSNAME IN (SELECT PRODUCTNAME FROM PRODUCT WHERE FACTORYNAME = :FACTORYNAME AND LOTNAME = :LOTNAME) AND A.PANELJUDGE NOT IN (:PANELA, :PANELB, :PANELC) ";
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			
			bindMap.put("FACTORYNAME", factoryName);
			bindMap.put("LOTNAME", lotName);
			bindMap.put("PANELA", "O");
			bindMap.put("PANELB", "X");
			bindMap.put("PANELC", "G");
			
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

			if ( sqlResult.size() > 0)
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		catch(Exception ex)
		{
			return true;
		}
	}
	private void SendMail(Lot lotData, List<String> userGroupList,String content) throws CustomException
	{
//		StringBuilder sqlStatement = new StringBuilder();
//		sqlStatement.append("SELECT EMAIL FROM USERPROFILE WHERE USERGROUPNAME = :USERGROUPNAME");
		Map<String, String> bindMap = new HashMap<String, String>();
		List<String> userEmailList = new ArrayList<String>();
		
		String sql= "SELECT EMAIL FROM USERPROFILE WHERE USERGROUPNAME = :USERGROUPNAME";
		
		for(String userGroupName : userGroupList){
			bindMap.put("USERGROUPNAME", userGroupName);
			List<Map<String, Object>> sqlResult =GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

			for(Map<String, Object> map : sqlResult)
			{
				userEmailList.add(map.get("EMAIL").toString());
			}
		}
		
		try {
			if(userGroupList!=null && userGroupList.size()>0){
				MESUserServiceProxy.getUserProfileServiceUtil().MailSend(userEmailList, "Defect Mail!", content);
			}
		} catch (Exception e) {
			eventLog.error("Mail Send Error ! ");
		}
		


	}
	private void DefectMailProduct(EventInfo eventInfo, List<Product> productList, String carrierName,String factoryName) throws CustomException
	{
		
		try
		{
			//get DefectCode
			for(Product productData : productList)
			{
				Map<String,String> holdCode = new HashMap<String,String>();
				Map<String,String> userGroupNameList = new HashMap<String,String>();
				List<String> groupList= new ArrayList<String>();
				String content = "";
				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(productData.getKey().getProductName());
				//String sql = "SELECT DEFECTCODE FROM " + arrDBUserName+".CT_DEFECTRESULT WHERE PRODUCTNAME= :PRODUCTNAME AND MAILFLAG='Y' GROUP BY DEFECTCODE ";
				String sql="SELECT * " +
						"  FROM CT_DEFECTRULESETTING DRS " +
						" WHERE     1 = 1 " +
						" AND FACTORYNAME = :FACTORYNAME " +
						"       AND DRS.DEFECTCOUNT <= " +
						"              (SELECT COUNT (*) " +
						"                 FROM CT_DEFECTRESULT DR " +
						"                WHERE     1 = 1 " +
						"                      AND DR.PRODUCTNAME = :PRODUCTNAME " +
						"                      AND DR.FACTORYNAME = DRS.FACTORYNAME " +
						"                      AND DR.PRODUCTSPECNAME = DRS.PRODUCTSPECNAME " +
						"                      AND DR.PROCESSOPERATIONNAME = DRS.PROCESSOPERATIONNAME " +
						"                      AND DR.DEFECTCODE=DRS.DEFECTCODE " +
						"                      AND DR.MAILFLAG=:MAILFLAG " +
						"                      ) ";


				
				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("FACTORYNAME", factoryName);
				bindMap.put("PRODUCTNAME", productData.getKey().getProductName());
				bindMap.put("MAILFLAG", "Y");
				
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
				
				if ( sqlResult!=null && sqlResult.size() > 0)
				{
					for(Map<String, Object> map : sqlResult)
					{
						holdCode.put(map.get("DEFECTCODE").toString(), map.get("DEFECTCODE").toString());

					}
				}
				
				sql = "SELECT USERGROUPNAME FROM CT_DEFECTRESULT WHERE FACTORYNAME = :FACTORYNAME AND PRODUCTNAME= :PRODUCTNAME AND MAILFLAG=:MAILFLAG GROUP BY USERGROUPNAME ";
				sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
				
				if ( sqlResult!=null && sqlResult.size() > 0)
				{
					for(Map<String, Object> map : sqlResult)
					{
						userGroupNameList.put(map.get("USERGROUPNAME").toString(), map.get("USERGROUPNAME").toString());

					}
				}
				
				
				if(!userGroupNameList.isEmpty()){
					for(String key : userGroupNameList.keySet()){
						//key , holdCode.get(key)
						try {
							groupList.add(userGroupNameList.get(key));
						} 
						catch (Exception e) 
						{
							eventLog.warn(e);
						}
					}	
				}
				content+="holdCode : ";
				if(!holdCode.isEmpty()){
					for(String key : holdCode.keySet()){
						//key , holdCode.get(key)
						try {
							content+=holdCode.get(key)+" ";
						} 
						catch (Exception e) 
						{
							eventLog.warn(e);
						}
					}	
				}
				
				// send Mail
				if(groupList!=null && groupList.size()>0){
					SendMail(lotData,groupList,content);
				}
			}

		}
		catch(Exception ex)
		{
			eventLog.error("Mail Send Error ! ");
			//throw new CustomException("LOT-0228","");
		}
	}
	
	private void DefectHoldProduct(EventInfo eventInfo, List<Product> productList, String carrierName,String factoryName) throws CustomException
	{
		
		try
		{
			eventInfo.setEventName("HoldLot");
			eventInfo.setEventComment("Defect Out Of Spec");
			//get DefectCode
			for(Product productData : productList)
			{
				Map<String,String> holdCode = new HashMap<String,String>();
				String note=  productData.getKey().getProductName() +" / "+ carrierName+" / ";
				//String sql = "SELECT DEFECTCODE FROM " + arrDBUserName+".CT_DEFECTRESULT WHERE PRODUCTNAME= :PRODUCTNAME AND HOLDFLAG='Y' GROUP BY DEFECTCODE ";
				String sql= "SELECT * " +
						"  FROM CT_DEFECTRULESETTING DRS " +
						" WHERE     1 = 1 " +
						" AND FACTORYNAME = :FACTORYNAME " +
						"       AND DRS.DEFECTCOUNT <= " +
						"              (SELECT COUNT (*) " +
						"                 FROM CT_DEFECTRESULT DR " +
						"                WHERE     1 = 1 " +
						"                      AND DR.PRODUCTNAME = :PRODUCTNAME " +
						"                      AND DR.FACTORYNAME = DRS.FACTORYNAME " +
						"                      AND DR.PRODUCTSPECNAME = DRS.PRODUCTSPECNAME " +
						"                      AND DR.PROCESSOPERATIONNAME = DRS.PROCESSOPERATIONNAME " +
						"                      AND DR.DEFECTCODE=DRS.DEFECTCODE " +
						"                      AND DR.HOLDFLAG=:HOLDFLAG "  +
						"                      ) ";

				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("FACTORYNAME", factoryName);
				bindMap.put("PRODUCTNAME", productData.getKey().getProductName());
				bindMap.put("HOLDFLAG", "Y");
				
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
				
				if ( sqlResult!=null && sqlResult.size() > 0)
				{
					for(Map<String, Object> map : sqlResult)
					{
						holdCode.put(map.get("DEFECTCODE").toString(), map.get("DEFECTCODE").toString());
						note+=map.get("DEFECTCODE").toString() + " ";
					}
				}
				
				
				// HoldLot
				if(!holdCode.isEmpty()){

					Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(productData.getKey().getProductName());

					
					Map<String, String> udfs = lotData.getUdfs();
					udfs.put("NOTE", note);
					List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
					
					MakeOnHoldInfo makeOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeOnHoldInfo(productUSequence, udfs);

					LotServiceProxy.getLotService().makeOnHold(lotData.getKey(), eventInfo, makeOnHoldInfo);
					

					eventInfo.setReasonCodeType("Defect");
					for(String key : holdCode.keySet()){
						//key , holdCode.get(key)
						eventInfo.setReasonCode(holdCode.get(key));
						try {
							MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(productData.getKey().getProductName(), holdCode.get(key), " ", eventInfo);
						} 
						catch (Exception e) 
						{
							eventLog.warn(e);
						}
					}
						
				}
				
				
			}

		}
		catch(Exception ex)
		{
			eventLog.error("Hold Lot Error ! ");
			throw new CustomException("COMMON-0001","Hold Defect Lot Error !");
		}
	}
	
	@SuppressWarnings("unchecked")
	private boolean validateLotData(String lotName, String factoryName) throws CustomException
	{
		try
		{
			String sql = "SELECT L.LOTNAME " +
					"  FROM LOT L " +
					" WHERE L.LOTNAME = :LOTNAME AND L.LOTSTATE = :LOTSTATE AND L.FACTORYNAME = :FACTORYNAME " ;
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTNAME", lotName);
			bindMap.put("FACTORYNAME", factoryName);
			bindMap.put("LOTSTATE", "Shipped");
			
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
	
	// 2019.05.23_hsryu_Insert Logic. Mantis 0003973.
	private void validatePanelJudgeAndHQGlassJudge ( String factoryName, String lotName ) throws CustomException {
		
		List<Map<String, Object>> sqlResult = null;

		try
		{
			//Select Array Product
			String sql = "SELECT * FROM PRODUCT WHERE LOTNAME = :LOTNAME AND FACTORYNAME = :FACTORYNAME AND PRODUCTSTATE <> 'Scrapped' AND PRODUCTGRADE <> 'S' ";

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTNAME", lotName);
			bindMap.put("FACTORYNAME", factoryName);

			sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		}
		catch(Exception e){
			eventLog.warn(" Fail Search ProductData. ");
		}

		//Insert Product History
		if ( sqlResult.size() > 0)
		{
			for(Map<String, Object> map : sqlResult)
			{
				List<Map<String, Object>> sqlResult_PanelJudge = null;
				List<Map<String, Object>> sqlResult_hqGlassJudge = null;
				List<Map<String, Object>> sqlResult_arrayHqGlassJudge = null;
				
				//****************** Validation PanelJudge. Already Exist PanelJudge Data in OLED, Error ! *****************//
				try{
					String sql_panelJudge = "SELECT COUNT(PANELNAME) PJC FROM CT_PANELJUDGE WHERE GLASSNAME = :GLASSNAME ";

					Map<String, Object> bindMap_PanelJudge = new HashMap<String, Object>();
					bindMap_PanelJudge.put("GLASSNAME", map.get("PRODUCTNAME").toString());

					sqlResult_PanelJudge = GenericServiceProxy.getSqlMesTemplate().queryForList(sql_panelJudge, bindMap_PanelJudge);
				}
				catch(Throwable e){
					eventLog.warn("Fail Search CT_PANELJUDGE Data. ");
				}

				if(sqlResult_PanelJudge.size() > 0){
					if(((BigDecimal)sqlResult_PanelJudge.get(0).get("PJC")).longValue() != 0) {
						throw new CustomException("PJ-0001", map.get("PRODUCTNAME").toString());
					}
				}

				//****************** Validation HQGlassJudge. Already Exist HQGlassJudge Data in OLED, Error ! *****************//
				try{
					String sql_hqGlassJudge = "SELECT COUNT(HQGLASSNAME) HGJC FROM CT_HQGLASSJUDGE WHERE GLASSNAME = :GLASSNAME ";

					Map<String, Object> bindMap_hqGlassJudge = new HashMap<String, Object>();
					bindMap_hqGlassJudge.put("GLASSNAME", map.get("PRODUCTNAME").toString());

					sqlResult_hqGlassJudge = GenericServiceProxy.getSqlMesTemplate().queryForList(sql_hqGlassJudge, bindMap_hqGlassJudge);
				}
				catch(Throwable e){
					eventLog.warn("Fail Search CT_HQGLASSJUDGE Data. ");
				}

				if(sqlResult_hqGlassJudge.size() > 0){
					if(((BigDecimal)sqlResult_hqGlassJudge.get(0).get("HGJC")).longValue() != 0) {
						throw new CustomException("HQJ-0001", map.get("PRODUCTNAME").toString());
					}
				}

				//****************** Validation Array HQGlassJudge.if Not Exist HQGlassJudge Data in ARRAY, Error ! *****************//
				try{
					String sql_arrayHqGlassJudge = "SELECT COUNT(HQGLASSNAME) AHQGJ FROM CT_HQGLASSJUDGE WHERE GLASSNAME = :GLASSNAME ";

					Map<String, Object> bindMap_arrayHqGlassJudge = new HashMap<String, Object>();
					bindMap_arrayHqGlassJudge.put("GLASSNAME", map.get("PRODUCTNAME").toString());

					sqlResult_arrayHqGlassJudge = GenericServiceProxy.getSqlMesTemplate().queryForList(sql_arrayHqGlassJudge, bindMap_arrayHqGlassJudge);
				}
				catch(Throwable e){
					eventLog.warn("Fail Search CT_HQGLASSJUDGE Data in ARRAY. ");
				}
				
				if(sqlResult_arrayHqGlassJudge.size() > 0){
					if(((BigDecimal)sqlResult_arrayHqGlassJudge.get(0).get("AHQGJ")).longValue() == 0) {
						throw new CustomException("HQJ-0002",map.get("PRODUCTNAME").toString());
					}
				}
			}
		}
	}
	
	//2018.11.05 dmlee : Array Product Grade is 'S' then, Can't Receive
	@SuppressWarnings("unchecked")
	private boolean validateProductData(String lotName, String factoryName, String carrierName) throws CustomException
	{
		try
		{
			String sql = "SELECT P.PRODUCTNAME " +
					"  FROM PRODUCT P " +
					" WHERE P.CARRIERNAME = :CARRIERNAME AND P.PRODUCTSTATE = :PRODUCTSTATE AND P.FACTORYNAME = :FACTORYNAME AND P.PRODUCTGRADE = :PRODUCTGRADE " ;
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("CARRIERNAME", carrierName);
			bindMap.put("PRODUCTSTATE", "Scrapped");
			bindMap.put("FACTORYNAME", factoryName);
			bindMap.put("PRODUCTGRADE", "S");
			
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

			if ( sqlResult.size() > 0)
			{
				return false;
			}
			
			return true;
		}
		catch(Exception ex)
		{
			return true;
		}
	}
	
	
	
	private void getCST(EventInfo eventInfo, String cstName, String toFactoryName, int lotQty, String factoryName, String receiveBank) throws CustomException
	{
		
		try
		{
			String sql = "SELECT * FROM DURABLE WHERE DURABLENAME = :DURABLENAME AND FACTORYNAME = :FACTORYNAME";
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("DURABLENAME", cstName);
			bindMap.put("FACTORYNAME", factoryName);
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

			if ( sqlResult.size() > 0)
			{
				for(Map<String, Object> map : sqlResult)
				{
					Durable durableData = new Durable();
					
					DurableKey key = new DurableKey((String)map.get("DURABLENAME") );
					durableData.setKey(key);
					
					durableData.setAreaName(receiveBank); //update
					durableData.setCapacity(Long.parseLong(map.get("CAPACITY").toString()));
					durableData.setCreateTime((Timestamp) map.get("CREATETIME"));
					durableData.setCreateUser((String) map.get("CREATEUSER") );
					durableData.setDurableCleanState((String) map.get("DURABLECLEANSTATE") );
					durableData.setDurableSpecName((String) map.get("DURABLESPECNAME") );
					durableData.setDurableSpecVersion((String) map.get("DURABLESPECVERSION") );
					durableData.setDurableState((String) map.get("DURABLESTATE") );
					durableData.setDurableType((String) map.get("DURABLETYPE") );
					durableData.setDurationUsed(Double.parseDouble(map.get("DURATIONUSED").toString()));
					durableData.setDurationUsedLimit(Double.parseDouble(map.get("DURATIONUSEDLIMIT").toString()));
					durableData.setFactoryName(toFactoryName); //update
					durableData.setLastEventComment((String) map.get("LASTEVENTCOMMENT") );
					durableData.setLastEventFlag((String) map.get("LASTEVENTFLAG") );
					durableData.setLastEventName((String) map.get("LASTEVENTNAME") );
					durableData.setLastEventTime((Timestamp) map.get("LASTEVENTTIME"));
					durableData.setLastEventTimeKey((String) map.get("LASTEVENTTIMEKEY") );
					durableData.setLastEventUser((String) map.get("LASTEVENTUSER") );
					durableData.setLotQuantity(lotQty);
					durableData.setMaterialLocationName((String) map.get("MATERIALLOCATIONNAME") );
					durableData.setReasonCode((String) map.get("REASONCODE") );
					durableData.setReasonCodeType((String) map.get("REASONCODETYPE") );
					durableData.setTimeUsed(Double.parseDouble(map.get("TIMEUSED").toString()));
					durableData.setTimeUsedLimit(Double.parseDouble(map.get("TIMEUSEDLIMIT").toString()));
					durableData.setTransportGroupName((String) map.get("TRANSPORTGROUPNAME") );
					
					Map<String, String> udfs = new HashMap<String, String>();
					
					String totalUsedCount = String.valueOf(map.get("TOTALUSEDCOUNT"));
					if(totalUsedCount.equals("null"))
					{
						totalUsedCount = "0";
					}
					udfs.put("TOTALUSEDCOUNT", totalUsedCount);
					udfs.put("LASTCLEANTIME", String.valueOf(map.get("LASTCLEANTIME")));
					udfs.put("DURABLEHOLDSTATE", (String)map.get("DURABLEHOLDSTATE") );
					udfs.put("TRANSPORTLOCKFLAG", (String)map.get("TRANSPORTLOCKFLAG") );
					udfs.put("TRANSPORTSTATE", (String)map.get("TRANSPORTSTATE") );
					udfs.put("POSITIONTYPE", (String)map.get("POSITIONTYPE") );
					udfs.put("MACHINENAME", (String)map.get("MACHINENAME") );
					udfs.put("UNITNAME", (String)map.get("UNITNAME") );
					udfs.put("PORTNAME", (String)map.get("PORTNAME") );
					udfs.put("POSITIONNAME", (String)map.get("POSITIONNAME") );
					udfs.put("ZONENAME", (String)map.get("ZONENAME") );
					udfs.put("MACHINERECIPE", (String)map.get("MACHINERECIPE") );
					udfs.put("MASKSUBLOCATION", (String)map.get("MASKSUBLOCATION") );
					
					durableData.setUdfs(udfs);
					
					DurableServiceProxy.getDurableService().insert(durableData);
					
					SetEventInfo setEvetnInfo = new SetEventInfo();
					eventInfo.setEventName("Receive");
					DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEvetnInfo);

				}
			}
			
		}
		catch(Exception ex)
		{
			eventLog.error("CST Create Fail ! ");
			throw new CustomException("LOT-0228","");
		}
	}
	
	
	private List<Object> getArrayLotInsertArgList(EventInfo eventInfo, String arrayLotName, String productName, String toFactoryName, String toProductSpec, String receiveBank, String arrDBUserName) throws CustomException
	{	
		
		List<Object> bindList = new ArrayList<Object>();
		
		try
		{
			
			String sql = "SELECT A.*, B.POSITION FROM "+arrDBUserName+".LOT A JOIN " +arrDBUserName+".PRODUCT B ON A.LOTNAME = B.LOTNAME WHERE A.LOTNAME = :LOTNAME AND B.PRODUCTNAME = :PRODUCTNAME";
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTNAME", arrayLotName);
			bindMap.put("PRODUCTNAME", productName);
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			
			
			if ( sqlResult.size() > 0)
			{
				for(Map<String, Object> map : sqlResult)
				{
					
					double subProductQuantity = Long.parseLong(map.get("SUBPRODUCTQUANTITY1").toString()) / Long.parseLong(map.get("PRODUCTQUANTITY").toString());
					
					bindList.add(productName);
					bindList.add((String) map.get("PRODUCTIONTYPE"));
					bindList.add((String) map.get("PRODUCTSPECNAME"));
					bindList.add((String) map.get("PRODUCTSPECVERSION"));
					bindList.add((String) map.get("PROCESSGROUPNAME"));
					bindList.add((String) map.get("PRODUCTREQUESTNAME"));
					bindList.add(arrayLotName);
					bindList.add((String) map.get("SOURCELOTNAME"));
					bindList.add((String) map.get("DESTINATIONLOTNAME"));
					bindList.add((String) map.get("ROOTLOTNAME"));
					bindList.add((String) map.get("PARENTLOTNAME"));
					bindList.add((String) map.get("CARRIERNAME"));
					bindList.add((String) map.get("PRODUCTTYPE"));
					bindList.add((String) map.get("SUBPRODUCTTYPE"));
					bindList.add(String.valueOf(subProductQuantity));
					bindList.add(map.get("CREATEPRODUCTQUANTITY").toString());
					bindList.add(map.get("CREATESUBPRODUCTQUANTITY1").toString());
					bindList.add(map.get("CREATESUBPRODUCTQUANTITY2").toString());
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
					bindList.add((Timestamp)map.get("DUEDATE"));
					bindList.add((String) map.get("CREATEUSER"));
					bindList.add((String) map.get("PROCESSFLOWNAME"));
					bindList.add((String) map.get("PROCESSFLOWVERSION"));
					bindList.add(""); //ProcessOperationName
					bindList.add((String) map.get("PROCESSOPERATIONVERSION"));
					bindList.add((String) map.get("REWORKSTATE"));
					bindList.add((String) map.get("NOTE"));
					bindList.add((String) map.get("ECCODE"));
					bindList.add((String) map.get("SHIPBANK"));
					bindList.add(receiveBank);
					bindList.add((String) map.get("PRODUCTREQUESTNAME"));
					bindList.add(Long.parseLong(map.get("POSITION").toString()));
				}
			}
			
		}
		catch(Exception ex)
		{
			eventLog.error("Lot Create Fail ! ");
			throw new CustomException("LOT-0229","");
		}
		
		return bindList;
	}
	
	private List<Object> getArrayLotHistInsertArgList(EventInfo eventInfo, String arrayLotName, String productName, String toFactoryName, String toProductSpec, String receiveBank, String arrDBUserName) throws CustomException
	{	
		List<Object> bindList = new ArrayList<Object>();
		
		try
		{
			String sql = "SELECT A.*, B.POSITION FROM "+arrDBUserName+".LOT A JOIN " +arrDBUserName+".PRODUCT B ON A.LOTNAME = B.LOTNAME WHERE A.LOTNAME = :LOTNAME AND B.PRODUCTNAME = :PRODUCTNAME";
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTNAME", arrayLotName);
			bindMap.put("PRODUCTNAME", productName);
			
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
					bindList.add(arrayLotName);
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
					bindList.add(receiveBank);
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
                    bindList.add((String)map.get("PRODUCTREQUESTNAME"));
                    bindList.add(Long.parseLong(map.get("POSITION").toString()));
				}
			}			
		}
		catch(Exception ex)
		{
			eventLog.error("Lot Create Fail ! ");
			throw new CustomException("LOT-0229","");
		}
		
		return bindList;
	}
	
	
	private List<Object> getArrayProductInsertArgList(EventInfo eventInfo, String productName, String toFactoryName, String receiveBank, String arrDBUserName) throws CustomException
	{	
		List<Object> bindList = new ArrayList<Object>();
		
		try
		{
			String sql = "SELECT * FROM "+arrDBUserName+".PRODUCT WHERE PRODUCTNAME = :PRODUCTNAME";
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PRODUCTNAME", productName);
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

			if ( sqlResult.size() > 0)
			{	
				for(Map<String, Object> map : sqlResult)
				{
					bindList.add(productName);
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
					bindList.add(Double.parseDouble(map.get("CREATESUBPRODUCTQUANTITY").toString()));
					bindList.add(Double.parseDouble(map.get("CREATESUBPRODUCTQUANTITY1").toString()));
					bindList.add(Double.parseDouble(map.get("CREATESUBPRODUCTQUANTITY2").toString()));
					bindList.add(Double.parseDouble(map.get("SUBPRODUCTQUANTITY").toString()));
					bindList.add(Double.parseDouble(map.get("SUBPRODUCTUNITQUANTITY1").toString()));
					bindList.add(Double.parseDouble(map.get("SUBPRODUCTUNITQUANTITY2").toString()));
					bindList.add((String) map.get("PRODUCTGRADE"));
					bindList.add((Timestamp) map.get("DUEDATE"));
					bindList.add(Long.parseLong(map.get("PRIORITY").toString()));
					bindList.add(toFactoryName); //FactoryName
					bindList.add(""); //DestinationFactoryName
					bindList.add(receiveBank); //AreaName
					bindList.add("Received"); //ProductState
					bindList.add((Timestamp) map.get("CREATETIME"));
					bindList.add((String) map.get("CREATEUSER"));
					bindList.add((Timestamp) map.get("INPRODUCTIONTIME"));
					bindList.add((String) map.get("INPRODUCTIONUSER"));
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
			eventLog.error("Product Create Fail ! ");
			throw new CustomException("LOT-0230","");
		}
		
		return bindList;
	}
	
	
	private List<Object> getArrayProductHistInsertArgList(EventInfo eventInfo, String productName, String toFactoryName, String receiveBank, String arrDBUserName) throws CustomException
	{	
		List<Object> bindList = new ArrayList<Object>();
		
		try
		{
			String sql = "SELECT * FROM "+arrDBUserName+".PRODUCT WHERE PRODUCTNAME = :PRODUCTNAME";
			
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
					bindList.add(receiveBank); //AreaName
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
					
					/*bindList.add(productName);
					bindList.add(eventInfo.getEventTimeKey());
					bindList.add(eventInfo.getEventTime());
					bindList.add(eventInfo.getEventName());
					bindList.add((String) map.get("PRODUCTIONTYPE"));
					bindList.add((String) map.get("PRODUCTSPECVERSION"));
					bindList.add((String) map.get("LOTNAME"));
					bindList.add((String) map.get("SUBPRODUCTTYPE"));
					bindList.add(Double.parseDouble(map.get("SUBPRODUCTQUANTITY").toString()));
					bindList.add(Double.parseDouble(map.get("SUBPRODUCTQUANTITY1").toString()));
					bindList.add(Double.parseDouble(map.get("SUBPRODUCTQUANTITY2").toString()));
					bindList.add((String) map.get("FACTORYNAME"));
					bindList.add((String) map.get("DESTINATIONFACTORYNAME"));
					bindList.add((String) map.get("AREANAME"));
					bindList.add((String) map.get("PROCESSFLOWNAME"));
					bindList.add((String) map.get("PROCESSFLOWVERSION"));
					bindList.add((String) map.get("PROCESSOPERATIONNAME"));
					bindList.add((String) map.get("PROCESSOPERATIONVERSION"));
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
					bindList.add(receiveBank); //AreaName
					bindList.add("Received"); //ProductState
					bindList.add((String) map.get("PROCESSFLOWNAME"));
					bindList.add((String) map.get("PROCESSFLOWVERSION"));
					bindList.add(""); //ProcessOperationName
					bindList.add((String) map.get("PROCESSOPERATIONVERSION"));
					bindList.add(""); //NodeStack
					bindList.add((String) map.get("REWORKSTATE"));
					bindList.add(Long.parseLong(map.get("REWORKCOUNT").toString()));
					bindList.add((String) map.get("NOTE"));
					bindList.add((String) map.get("ECCODE"));*/
				}		
			}
			
		}
		catch(Exception ex)
		{
			eventLog.error("Product Create Fail ! ");
			throw new CustomException("LOT-0230","");
		}
		
		return bindList;
	}
	
	private void updateOnlyCSTData(String cstName, int lotQty, String factoryName) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Receive", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		try
		{
			//Update Array Durable
			String sql = "UPDATE DURABLE SET RECEIVEFLAG = 'Y', LOTQUANTITY = :LOTQUANTITY, LASTEVENTNAME = 'Receive', LASTEVENTTIMEKEY = :LASTEVENTTIMEKEY, LASTEVENTTIME = TO_DATE('"+ TimeStampUtil.toTimeString((Timestamp)eventInfo.getEventTime()) +"', 'YYYY/MM/DD HH24:MI:SS') WHERE DURABLENAME = :DURABLENAME AND FACTORYNAME = :FACTORYNAME";

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTQUANTITY", lotQty);
			bindMap.put("DURABLENAME", cstName);
			bindMap.put("LASTEVENTTIMEKEY", eventInfo.getEventTimeKey());
			bindMap.put("FACTORYNAME", factoryName);
			
			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
			
			//Select Array Durable
			String sql_2 = "SELECT * FROM DURABLE WHERE DURABLENAME = :DURABLENAME AND FACTORYNAME = :FACTORYNAME";
			
			Map<String, Object> bindMap2 = new HashMap<String, Object>();
			bindMap2.put("DURABLENAME", cstName);
			bindMap2.put("FACTORYNAME", factoryName);
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql_2, bindMap2);
			
			//Insert Durable History
			if ( sqlResult.size() > 0)
			{
				for(Map<String, Object> map : sqlResult)
				{
					String histSql = "Insert into DURABLEHISTORY " +
							"   (DURABLENAME, TIMEKEY, EVENTTIME, EVENTNAME, OLDDURABLESPECNAME, DURABLESPECNAME, OLDDURABLESPECVERSION, " +
							"   DURABLESPECVERSION, MATERIALLOCATIONNAME, TRANSPORTGROUPNAME, TIMEUSEDLIMIT, " +
							"   TIMEUSED, DURATIONUSEDLIMIT, DURATIONUSED, CAPACITY, LOTQUANTITY, OLDFACTORYNAME, FACTORYNAME, OLDAREANAME, AREANAME, " +
							"   DURABLESTATE, DURABLECLEANSTATE, EVENTUSER, EVENTCOMMENT, " +
							"   EVENTFLAG, REASONCODETYPE, REASONCODE, RECEIVEFLAG, DURABLEHOLDSTATE, TRANSPORTLOCKFLAG, TRANSPORTSTATE, POSITIONTYPE, MACHINENAME, UNITNAME, PORTNAME, POSITIONNAME, ZONENAME) " +
							" Values " +
							"   (:DURABLENAME, :TIMEKEY, TO_DATE(:EVENTTIME, 'YYYY/MM/DD HH24:MI:SS') , :EVENTNAME, :OLDDURABLESPECNAME, :DURABLESPECNAME, " +
							"   :OLDDURABLESPECVERSION, :DURABLESPECVERSION, :MATERIALLOCATIONNAME, " +
							"   :TRANSPORTGROUPNAME, :TIMEUSEDLIMIT, :TIMEUSED, :DURATIONUSEDLIMIT, :DURATIONUSED, :CAPACITY, :LOTQUANTITY, :OLDFACTORYNAME, :FACTORYNAME, :OLDAREANAME, :AREANAME, " +
							"   :DURABLESTATE, :DURABLECLEANSTATE, :EVENTUSER, :EVENTCOMMENT, :EVENTFLAG, :REASONCODETYPE, :REASONCODE, :RECEIVEFLAG, :DURABLEHOLDSTATE, :TRANSPORTLOCKFLAG, :TRANSPORTSTATE, :POSITIONTYPE, :MACHINENAME, :UNITNAME, :PORTNAME, :POSITIONNAME, :ZONENAME) " ;
					
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
					histbindMap.put("OLDAREANAME",(String) map.get("AREANAME"));
					histbindMap.put("AREANAME", (String) map.get("AREANAME"));
					histbindMap.put("DURABLESTATE", (String) map.get("DURABLESTATE"));
					histbindMap.put("DURABLECLEANSTATE", (String) map.get("DURABLECLEANSTATE"));
					histbindMap.put("EVENTUSER", eventInfo.getEventUser());
					histbindMap.put("EVENTCOMMENT", eventInfo.getEventComment());
					histbindMap.put("EVENTFLAG", (String) map.get("LASTEVENTFLAG"));
					histbindMap.put("REASONCODETYPE", (String) map.get("REASONCODETYPE"));
					histbindMap.put("REASONCODE", (String) map.get("REASONCODE"));
					histbindMap.put("RECEIVEFLAG", (String) map.get("RECEIVEFLAG"));
					
					histbindMap.put("DURABLEHOLDSTATE", (String) map.get("DURABLEHOLDSTATE"));
					histbindMap.put("TRANSPORTLOCKFLAG", (String) map.get("TRANSPORTLOCKFLAG"));
					histbindMap.put("TRANSPORTSTATE", (String) map.get("TRANSPORTSTATE"));
					histbindMap.put("POSITIONTYPE", (String) map.get("POSITIONTYPE"));
					histbindMap.put("MACHINENAME", (String) map.get("MACHINENAME"));
					histbindMap.put("UNITNAME", (String) map.get("UNITNAME"));
					histbindMap.put("PORTNAME", (String) map.get("PORTNAME"));
					histbindMap.put("POSITIONNAME", (String) map.get("POSITIONNAME"));
					histbindMap.put("ZONENAME", (String) map.get("ZONENAME"));

					GenericServiceProxy.getSqlMesTemplate().update(histSql, histbindMap);
				}
			}	
		}
		catch(Exception ex)
		{
			throw new CustomException("LOT-0231","");
		}
	}
	
	private void updateOnlyArrayLotData(String lotName, String arrDBUserName) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Receive", getEventUser(), getEventComment(), null, null);
		
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		try
		{
			//Update Array Lot
			String sql = "UPDATE "+arrDBUserName+".LOT SET RECEIVEFLAG = 'Y', LASTEVENTNAME = 'Receive', LOTSTATE = 'Received', LASTEVENTTIMEKEY = :LASTEVENTTIMEKEY, LASTEVENTTIME = TO_DATE('"+ TimeStampUtil.toTimeString((Timestamp)eventInfo.getEventTime()) +"', 'YYYY/MM/DD HH24:MI:SS') WHERE LOTNAME = :LOTNAME ";
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTNAME", lotName);
			bindMap.put("LASTEVENTTIMEKEY", eventInfo.getEventTimeKey());
			
			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
			
			//Select Array Lot
			String sql_2 = "SELECT * FROM LOT@ARRAYDB WHERE LOTNAME = :LOTNAME ";
			
			Map<String, Object> bindMap2 = new HashMap<String, Object>();
			bindMap2.put("LOTNAME", lotName);
			
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
					histbindMap.put("LOTSTATE","Received");  
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
					histbindMap.put("OLDPROCESSFLOWNAME",(String)map.get("PROCESSFLOWNAME")); 
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
					histbindMap.put("RECEIVEFLAG","Y"); 
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
	
	private void updateOnlyArrayProductData(String lotName, String arrDBUserName) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Receive", getEventUser(), getEventComment(), null, null);
		
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		try
		{
			//Update Array Product
			String sql = "UPDATE "+arrDBUserName+".PRODUCT SET PRODUCTSTATE = 'Received', LASTEVENTNAME = 'Receive', LASTEVENTTIMEKEY = :LASTEVENTTIMEKEY, LASTEVENTTIME = TO_DATE('"+ TimeStampUtil.toTimeString((Timestamp)eventInfo.getEventTime()) +"', 'YYYY/MM/DD HH24:MI:SS') WHERE LOTNAME = :LOTNAME AND PRODUCTSTATE <> 'Scrapped' AND PRODUCTGRADE <> 'S' ";
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTNAME", lotName);
			bindMap.put("LASTEVENTTIMEKEY", eventInfo.getEventTimeKey());
			bindMap.put("LASTEVENTTIME", "TO_DATE("+ TimeStampUtil.toTimeString((Timestamp)eventInfo.getEventTime()) +"), 'YYYY/MM/DD HH24:MI:SS')");
			
			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
			
			//Select Array Product
			String sql_2 = "SELECT * FROM PRODUCT@ARRAYDB WHERE LOTNAME = :LOTNAME AND PRODUCTSTATE <> 'Scrapped' AND PRODUCTGRADE <> 'S' ";
			
			Map<String, Object> bindMap2 = new HashMap<String, Object>();
			bindMap2.put("LOTNAME", lotName);
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql_2, bindMap2);
			
			//Insert Product History
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
							"  NOTE,VCRPRODUCTNAME,ECCODE,SCRAPDEPARTMENTNAME,OLDSUBPRODUCTQUANTITY, OLDSUBPRODUCTQUANTITY1,OLDSUBPRODUCTQUANTITY2) " +
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
							"  :NOTE,:VCRPRODUCTNAME,:ECCODE,:SCRAPDEPARTMENTNAME,:OLDSUBPRODUCTQUANTITY, :OLDSUBPRODUCTQUANTITY1, :OLDSUBPRODUCTQUANTITY2 ) "; 

					Map<String, Object> histbindMap = new HashMap<String, Object>();
					
					histbindMap.put("PRODUCTNAME", (String) map.get("PRODUCTNAME"));
					histbindMap.put("TIMEKEY", eventInfo.getEventTimeKey());
					histbindMap.put("EVENTTIME", TimeStampUtil.toTimeString((Timestamp)eventInfo.getEventTime()));
					histbindMap.put("EVENTNAME", eventInfo.getEventName());
					histbindMap.put("OLDPRODUCTIONTYPE",(String)map.get("PRODUCTIONTYPE"));
					histbindMap.put("PRODUCTIONTYPE", (String) map.get("PRODUCTIONTYPE"));
					histbindMap.put("OLDPRODUCTSPECNAME",(String)map.get("PRODUCTSPECNAME"));
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
					histbindMap.put("OLDAREANAME", (String)map.get("AREANAME"));
					histbindMap.put("AREANAME", (String) map.get("AREANAME"));
					histbindMap.put("SERIALNO", (String) map.get("SERIALNO"));
					histbindMap.put("PRODUCTSTATE", "Received");
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
					histbindMap.put("OLDPROCESSFLOWVERSION",(String)map.get("PROCESSFLOWVERSION"));
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
	
	private void getHQGlassJudge(EventInfo eventInfo, String glassName, String arrDBUserName) throws CustomException
	{
		/*try
		{
			String sql = "SELECT * FROM "+arrDBUserName+".CT_HQGLASSJUDGE WHERE GLASSNAME = :GLASSNAME";
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("GLASSNAME", glassName);
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

			if ( sqlResult.size() > 0)
			{
				List<HQGlassJudge> hqGlassJudgeList = new ArrayList<HQGlassJudge>();
				
				for(Map<String, Object> map : sqlResult)
				{
					HQGlassJudge HQGlassJudgeData = new HQGlassJudge((String)map.get("HQGLASSNAME").toString());
					
					HQGlassJudgeData.setHQGlassJudge((String) map.get("HQGLASSJUDGE"));
					HQGlassJudgeData.setXAxis(Long.parseLong(map.get("XAXIS").toString()));
					HQGlassJudgeData.setYAxis(Long.parseLong(map.get("YAXIS").toString()));
					HQGlassJudgeData.setGlassName(glassName);
					
					HQGlassJudgeData.setLastEventName((String) map.get("LASTEVENTNAME"));
					HQGlassJudgeData.setLastEventUser((String) map.get("LASTEVENTUSER"));
					HQGlassJudgeData.setLastEventTime((Timestamp) map.get("LASTEVENTTIME"));
					HQGlassJudgeData.setLastEventComment((String) map.get("LASTEVENTCOMMENT"));
					
					hqGlassJudgeList.add(HQGlassJudgeData);
					
					//ExtendedObjectProxy.getHQGlassJudgeService().create(eventInfo, HQGlassJudgeData);
					
				}
				
				ExtendedObjectProxy.getHQGlassJudgeService().updateBatchInsert(hqGlassJudgeList);
				ExtendedObjectProxy.getHQGlassJudgeService().updateBatchInsertHistory(eventInfo, "CT_HQGlassJudgeHist", hqGlassJudgeList);
			}
			
		}
		catch(Exception ex)
		{
			eventLog.error("HQ Glass Data Create Fail ! ");
			throw new CustomException("LOT-0234","");
		}*/
	}
	
	private void getPanelJudge(EventInfo eventInfo, String glassName, String arrDBUserName) throws CustomException
	{
/*		try
		{
			String sql = "SELECT * FROM "+arrDBUserName+".CT_PANELJUDGE WHERE GLASSNAME = :GLASSNAME";
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("GLASSNAME", glassName);
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

			if ( sqlResult.size() > 0)
			{
				List<PanelJudge> panelJudgeList = new ArrayList<PanelJudge>();
				
				for(Map<String, Object> map : sqlResult)
				{
					PanelJudge panelJudgeData = new PanelJudge((String) map.get("PANELNAME"));
					
					panelJudgeData.setPanelJudge((String) map.get("PANELJUDGE"));
					panelJudgeData.setPanelGrade((String) map.get("PANELGRADE"));
					panelJudgeData.setxAxis1(Long.parseLong(map.get("XAXIS1").toString()));
					panelJudgeData.setyAxis1(Long.parseLong(map.get("YAXIS1").toString()));
					panelJudgeData.setxAxis2(Long.parseLong(map.get("XAXIS2").toString()));
					panelJudgeData.setyAxis2(Long.parseLong(map.get("YAXIS2").toString()));
					panelJudgeData.setGlassName(glassName);
					panelJudgeData.setHqGlassName((String) map.get("HQGLASSNAME"));
					panelJudgeData.setCutType((String) map.get("CUTTYPE"));
					panelJudgeData.setLastEventName((String) map.get("LASTEVENTNAME"));
					panelJudgeData.setLastEventUser((String) map.get("LASTEVENTUSER"));
					panelJudgeData.setLastEventTime((Timestamp) map.get("LASTEVENTTIME"));
					panelJudgeData.setLastEventComment((String) map.get("LASTEVENTCOMMENT"));
					
					// Added by smkang on 2018.12.11 - Four columns are added.
					panelJudgeData.setProcessOperationName((String) map.get("PROCESSOPERATIONNAME"));
					panelJudgeData.setMachineName((String) map.get("MACHINENAME"));
					panelJudgeData.setAssembledPanelName((String) map.get("ASSEMBLEDPANELNAME"));
					panelJudgeData.setProductSpecType((String) map.get("PRODUCTSPECTYPE"));
					
					panelJudgeList.add(panelJudgeData);
					
					//ExtendedObjectProxy.getPanelJudgeService().create(eventInfo, panelJudgeData);
					
				}
				
				ExtendedObjectProxy.getPanelJudgeService().updateBatchInsert(panelJudgeList);
				ExtendedObjectProxy.getPanelJudgeService().updateBatchInsertHistory(eventInfo, "CT_PanelJudgeHistory", panelJudgeList);
			}
			
		}
		catch(Exception ex)
		{
			eventLog.error("Panel Judge Data Create Fail ! ");
			throw new CustomException("LOT-0235","");
		}*/
	}
	
	private ArrayList<String> getProductList(String carrierName, String factoryName) throws CustomException
	{
		ArrayList<String> productList = new ArrayList<String>();
		
		String sql = "SELECT PRODUCTNAME FROM PRODUCT WHERE FACTORYNAME = :FACTORYNAME AND CARRIERNAME = :CARRIERNAME AND PRODUCTSTATE <> :PRODUCTSTATE AND PRODUCTGRADE <> :PRODUCTGRADE ";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("CARRIERNAME", carrierName);
		bindMap.put("PRODUCTSTATE", "Scrapped");
		bindMap.put("PRODUCTGRADE", "S");
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		if ( sqlResult.size() > 0)
		{
			for(Map<String, Object> map : sqlResult)
			{
				String productName = (String) map.get("PRODUCTNAME"); 
				
				productList.add(productName);
			}
		}
		
		return productList;
	}
}
