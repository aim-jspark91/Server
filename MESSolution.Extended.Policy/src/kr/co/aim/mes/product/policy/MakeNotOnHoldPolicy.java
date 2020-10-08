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
import kr.co.aim.greentrack.generic.state.LotStateModel;
import kr.co.aim.greentrack.generic.state.support.StateMachine;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.LotService;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.MakeNotOnHoldInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductMultiHold;
import kr.co.aim.greentrack.product.management.data.ProductMultiHoldKey;
import kr.co.aim.greentrack.product.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.product.management.info.MakeShippedInfo;
import kr.co.aim.greentrack.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;

public class MakeNotOnHoldPolicy extends kr.co.aim.greentrack.product.management.policy.MakeNotOnHoldPolicy
{
	
	public void makeNotOnHoldM(DataInfo oldProduct, DataInfo newProduct, EventInfo eventInfo,
								TransitionInfo transitionInfo)
			throws NotFoundSignal, DuplicateNameSignal, FrameworkErrorSignal, InvalidStateTransitionSignal
	{
		Product newProductData = (Product) newProduct;
		Product oldProductData = (Product) oldProduct;
		
		if(! checkProductHold( oldProductData, eventInfo )){
			return;
		}
			boolean isDefined =
				getCommonValidateExecutor().isDefined(eventInfo.getBehaviorName(), this.getClass().getName(),
					"CanNotDoAtProductState");

		if (isDefined)
		{
			if (!(oldProductData.getProductState().equals(getConstantMap().Prod_InProduction))
				|| !(oldProductData.getProductHoldState().equals(getConstantMap().Prod_OnHold)))
				throw new FrameworkErrorSignal(ExceptionKey.CanNotDoAtProductState_Exception,
						ObjectUtil.getString(newProductData.getKey()), "makeNotOnHold",
						oldProductData.getProductState(), oldProductData.getProductProcessState(),
						oldProductData.getProductHoldState());
		}

		boolean lotReleaseHoldFlag = checkProductHoldState(newProductData.getLotName(), newProductData.getKey().getProductName(), eventInfo);
		
		if (removeMultiHoldProduct(newProductData.getKey().getProductName(), eventInfo.getReasonCode()) == 0)
			newProductData.setProductHoldState(getConstantMap().Prod_NotOnHold);
		
		addMultiHoldProductHistory(newProductData.getKey().getProductName(),newProductData.getLotName(), eventInfo );
		
		Lot lotData = null;

		try
		{
			lotData = CommonUtil.getLotInfoByLotName( newProductData.getLotName() );
		}
		catch ( CustomException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		if(lotReleaseHoldFlag){
			lotData.setLotHoldState( "N" );
			LotServiceProxy.getLotService().update( lotData );
			SetEventInfo setEventInfo = new SetEventInfo();
			LotServiceProxy.getLotService().setEvent( lotData.getKey(), eventInfo, setEventInfo );
			return;
		}

		newProductData.setLastEventFlag("R");
	}

	private boolean checkProductHold( Product oldProduct, EventInfo eventInfo )
	{
		String sql = "SELECT * FROM PRODUCT "
				+ " WHERE PRODUCTNAME =:PRODUCTNAME AND PRODUCTHOLDSTATE ='NotOnHold' ";

		Map<String, String> bindMap = new HashMap<String, String>();

		bindMap.put("PRODUCTNAME", oldProduct.getKey().getProductName() );
		
		List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList( sql, bindMap );
		
		
		if(sqlResult.size()==1){
			return false;
		}
	
		return true;
	}

	public int removeMultiHoldProduct(String productName, String reasonCode)
			throws FrameworkErrorSignal, NotFoundSignal
	{
		
		ProductMultiHoldKey key = new ProductMultiHoldKey();
		key.setProductName(productName);
		key.setReasonCode(reasonCode);
		
		Map<String,String> bindMap = new HashMap<String,String>();
	  	bindMap.put("productname",productName);
	  	bindMap.put("reasoncode",reasonCode);
	  	
		String sql = "SELECT * FROM ProductMultiHold WHERE productName=:productname and reasoncode =:reasoncode";
		List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		
		if(sqlResult.size()>0){
			ProductServiceProxy.getProductMultiHoldService().delete(key);
		}
		
		String query = "SELECT COUNT(*) FROM ProductMultiHold WHERE productName=?";
		return GenericServiceProxy.getSqlMesTemplate().queryForInt(query, new Object[] { productName });
	}
	
	private boolean checkProductHoldState( String lotName, String productName, EventInfo eventInfo)
	{
		boolean lotReleaseHoldFlag = false;
		String sql = "SELECT * FROM PRODUCT WHERE LOTNAME = :LOTNAME AND  PRODUCTHOLDSTATE ='OnHold' and PRODUCTNAME <>:PRODUCTNAME";
		List<Map<String, Object>> sqlResult = new ArrayList<Map<String,Object>>();
		Map<String,String> bindMap = new HashMap<String,String>();
	  	bindMap.put("LOTNAME",lotName);
	  	bindMap.put("PRODUCTNAME",productName);
		
		sqlResult  = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		
		if(sqlResult.size()==0){
			lotReleaseHoldFlag = true;
		}
		return lotReleaseHoldFlag;
		
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
