package kr.co.aim.mes.product.policy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.orm.info.DataInfo;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.ExceptionKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.TransitionInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductMultiHold;
import kr.co.aim.greentrack.product.management.data.ProductMultiHoldKey;
import kr.co.aim.greentrack.product.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.product.management.info.MakeShippedInfo;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;


public class MakeOnHoldPolicy extends kr.co.aim.greentrack.product.management.policy.MakeOnHoldPolicy
{
	// Suffix 'M' of Method means Multi Hold.
		public void makeOnHoldM(DataInfo oldProduct, DataInfo newProduct, EventInfo eventInfo, TransitionInfo transitionInfo)
				throws NotFoundSignal, DuplicateNameSignal, FrameworkErrorSignal, InvalidStateTransitionSignal
		{
			Product newProductData = (Product) newProduct;
			Product oldProductData = (Product) oldProduct;
			MakeOnHoldInfo makeOnHoldInfo = (MakeOnHoldInfo) transitionInfo;
			boolean isDefined =
					getCommonValidateExecutor().isDefined(eventInfo.getBehaviorName(), this.getClass().getName(),
						"CanNotDoAtProductState");

			if (isDefined)
			{
				if (!oldProductData.getProductState().equals(getConstantMap().Prod_InProduction))
				{
					throw new FrameworkErrorSignal(ExceptionKey.CanNotDoAtProductState_Exception,
							ObjectUtil.getString(newProductData.getKey()), "makeOnHold", oldProductData.getProductState(),
							oldProductData.getProductProcessState(), oldProductData.getProductHoldState());
				}
			}
			
			Lot lotData = null;
			
			try {
				lotData = CommonUtil.getLotInfoByLotName( newProductData.getLotName() );
			} catch (CustomException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(checkProductMultiHold(newProductData.getLotName(),eventInfo)){
				lotData.setLotHoldState( "Y" );
				LotServiceProxy.getLotService().update( lotData );
				SetEventInfo setEventInfo = new SetEventInfo();
				LotServiceProxy.getLotService().setEvent( lotData.getKey(), eventInfo, setEventInfo );
			}

			if(makeOnHoldInfo.getMultiHoldUdfs() != null && makeOnHoldInfo.getMultiHoldUdfs().size() > 0){
				addMultiHoldProduct(newProductData.getKey().getProductName(), newProductData.getLotName(), eventInfo, makeOnHoldInfo.getMultiHoldUdfs()  );
				addMultiHoldProductHistory(newProductData.getKey().getProductName(), newProductData.getLotName(), eventInfo);
			}
			else{
				addMultiHoldProduct(newProductData.getKey().getProductName(), newProductData.getLotName(), eventInfo);
				addMultiHoldProductHistory(newProductData.getKey().getProductName(), newProductData.getLotName(), eventInfo);
			}

			if (!newProductData.getProductHoldState().equals(getConstantMap().Prod_OnHold))
				newProductData.setProductHoldState(getConstantMap().Prod_OnHold);						
		
			newProductData.setLastEventFlag("H");
		}
		
		private boolean checkLotHoldState( Lot lotData)
		{
			boolean lotHoldFlag = false;
			String sql = "select * from lot where lotname =:lotname and lotholdstate ='N' ";
			List<Map<String, Object>> sqlResult = new ArrayList<Map<String,Object>>();
			Map<String,String> bindMap = new HashMap<String,String>();
		  	bindMap.put("lotname",lotData.getKey().getLotName());
			
			sqlResult  = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
			
			if(sqlResult.size()>0){
				lotHoldFlag = true;
			}
			return lotHoldFlag;
			
		}
		
		private boolean checkProductMultiHold(String lotName,EventInfo eventInfo)
		{
			Object[] bindSet = new Object[] {eventInfo.getReasonCode(), lotName};
			try
			{
				ProductServiceProxy.getProductMultiHoldService().select(" WHERE REASONCODE = ? AND LOTNAME = ? ", bindSet);
				//Exist ProductMultiHold
				return false;
			}
			catch(Exception e)
			{
				//Not Exist ProductMultiHold
				return true;
			}
		}
		
		
		public void addMultiHoldProduct(String productName, String lotName, EventInfo eventInfo)
				throws FrameworkErrorSignal, DuplicateNameSignal
		{
			ProductMultiHoldKey key = new ProductMultiHoldKey();
			key.setProductName(productName);
			key.setReasonCode(eventInfo.getReasonCode());
			
			Map<String,String> udfs = new HashMap<String,String>();
			udfs.put( "LOTNAME", lotName );

			ProductMultiHold productMultiHold = new ProductMultiHold();
			productMultiHold.setKey(key);
			productMultiHold.setEventTime(eventInfo.getEventTime());
			productMultiHold.setEventName(eventInfo.getEventName());
			productMultiHold.setEventUser(eventInfo.getEventUser());
			productMultiHold.setEventComment(eventInfo.getEventComment());
			productMultiHold.setUdfs(udfs);

			ProductServiceProxy.getProductMultiHoldService().insert(productMultiHold);
		}

		public void addMultiHoldProduct(String productName, String lotName, EventInfo eventInfo, Map<String,String> udfs)
				throws FrameworkErrorSignal, DuplicateNameSignal
		{
			ProductMultiHoldKey key = new ProductMultiHoldKey();
			key.setProductName(productName);
			key.setReasonCode(eventInfo.getReasonCode());
			
			udfs.put( "LOTNAME", lotName );

			ProductMultiHold productMultiHold = new ProductMultiHold();
			productMultiHold.setKey(key);
			productMultiHold.setEventTime(eventInfo.getEventTime());
			productMultiHold.setEventName(eventInfo.getEventName());
			productMultiHold.setEventUser(eventInfo.getEventUser());
			productMultiHold.setEventComment(eventInfo.getEventComment());
			productMultiHold.setUdfs(udfs);

			ProductServiceProxy.getProductMultiHoldService().insert(productMultiHold);
		}
		
		public void addMultiHoldProductHistory(String productName, String lotName, EventInfo eventInfo)
				throws FrameworkErrorSignal, DuplicateNameSignal
		{

			String sql = "INSERT INTO PRODUCTMULTIHOLDHISTORY "
					+ " (PRODUCTNAME, REASONCODE, TIMEKEY, EVENTTIME, EVENTNAME, EVENTUSER, EVENTCOMMENT, LOTNAME) "
					+ "VALUES(" + ":PRODUCTNAME, " + ":REASONCODE, " + ":TIMEKEY, "
					+ ":EVENTTIME, " + ":EVENTNAME, " + ":EVENTUSER, " + ":EVENTCOMMENT, " + ":LOTNAME) ";

			Map<String, String> bindMap = new HashMap<String, String>();

			bindMap.put("PRODUCTNAME", productName);
			bindMap.put("LOTNAME", lotName);
			bindMap.put("REASONCODE", eventInfo.getReasonCode());
			bindMap.put("TIMEKEY",  eventInfo.getEventTimeKey() );
			bindMap.put("EVENTTIME", String.valueOf(eventInfo.getEventTime()));
			bindMap.put("EVENTNAME", eventInfo.getEventName());
			bindMap.put("EVENTUSER", eventInfo.getEventUser());
			bindMap.put("EVENTCOMMENT", eventInfo.getEventComment());
			
			greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql, bindMap);
		}
		
}
