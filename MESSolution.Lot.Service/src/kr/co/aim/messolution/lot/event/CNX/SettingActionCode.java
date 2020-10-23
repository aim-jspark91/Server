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

public class SettingActionCode extends SyncHandler {
	/**
	 * 151106 by xzquan : Create CreateLot
	 */
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("SettingActionCode", getEventUser(), getEventComment(), "", "");
		
		String factoryName = SMessageUtil.getBodyItemValue( doc, "FACTORYNAME", true );
		String actionCode = SMessageUtil.getBodyItemValue(doc, "ACTIONCODE", true);
		String department = SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", true);
		String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", true);
		String aType = SMessageUtil.getBodyItemValue(doc, "ACTIONTYPE", true);
		
		if(aType.equals("Create"))
		{
			eventInfo.setEventName("Create ActionCode");
			eventInfo.setEventComment("Create ActionCode");
			
			CreateActionCode(factoryName, actionCode, department, description, eventInfo);
		}
		else if (aType.equals("Update"))
		{
			eventInfo.setEventName("Update ActionCode");
			eventInfo.setEventComment("Update ActionCode");

			UpdateActionCode(factoryName, actionCode, department, description, eventInfo);
		}
		else if (aType.equals("Delete"))
		{
			eventInfo.setEventName("Delete ActionCode");
			eventInfo.setEventComment("Delete ActionCode");

			DeleteActionCode(factoryName, actionCode);
		}

		return doc;

	}
	
	private void CreateActionCode(String factoryName,  String actionCode, String department, String description,
			EventInfo eventInfo) throws CustomException
	{	
		try
		{
			String sql = "INSERT INTO CT_ABNORMALSHEETACTIONCODE(FACTORYNAME, ACTIONCODE, DEPARTMENT, DESCRIPTION) VALUES" +
			 " (:FACTORYNAME, :ACTIONCODE, :DEPARTMENT, :DESCRIPTION )";
			
			Map<String,Object> bindMap = new HashMap<String,Object>();
			bindMap.put("FACTORYNAME", factoryName);
			bindMap.put("ACTIONCODE", actionCode);
			bindMap.put("DEPARTMENT", department);
			bindMap.put("DESCRIPTION", description);
			
			int result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql, bindMap);
		}
		catch(Exception e)
		{
			throw new CustomException("SYS-8001","for insert " + actionCode + " into CT_ABNORMALSHEETACTIONCODE   Error : " + e.toString());
		}
	}
	
	private void UpdateActionCode(String factoryName,  String actionCode, String department, String description,
			EventInfo eventInfo) throws CustomException
	{	
		try
		{
			String sql = "UPDATE CT_ABNORMALSHEETACTIONCODE SET DEPARTMENT =:DEPARTMENT, DESCRIPTION=:DESCRIPTION "
					+ "WHERE FACTORYNAME =:FACTORYNAME AND ACTIONCODE =:ACTIONCODE";
			
			Map<String,Object> bindMap = new HashMap<String,Object>();
			bindMap.put("FACTORYNAME", factoryName);
			bindMap.put("ACTIONCODE", actionCode);
			bindMap.put("DEPARTMENT", department);
			bindMap.put("DESCRIPTION", description);
			
			int result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql, bindMap);
		}
		catch(Exception e)
		{
			throw new CustomException("SYS-8001","for update " + actionCode + " set CT_ABNORMALSHEETACTIONCODE Error : " + e.toString());
		}
	}


	private void DeleteActionCode(String factoryName, String actionCode) throws CustomException
	{	
		try
		{
			String sql = "DELETE FROM CT_ABNORMALSHEETACTIONCODE "
					   + " WHERE FACTORYNAME = :FACTORYNAME AND ACTIONCODE = :ACTIONCODE";
			
			Map<String,Object> bindMap = new HashMap<String,Object>();
			bindMap.put("FACTORYNAME", factoryName);
			bindMap.put("ACTIONCODE", actionCode);
			
			int result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql, bindMap);
		}
		catch(Exception e)
		{
			throw new CustomException("SYS-8001","for delete " + actionCode + " from the CT_ABNORMALSHEETCODE   Error : " + e.toString());
		}
	}
}
