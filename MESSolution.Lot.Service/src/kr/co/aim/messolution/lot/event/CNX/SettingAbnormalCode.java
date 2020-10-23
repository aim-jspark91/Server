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

public class SettingAbnormalCode extends SyncHandler {
	/**
	 * 151106 by xzquan : Create CreateLot
	 */
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("SettingAbnormalSheet", getEventUser(), getEventComment(), "", "");
		
		String factoryName = SMessageUtil.getBodyItemValue( doc, "FACTORYNAME", true );
		String AbnormalCode = SMessageUtil.getBodyItemValue(doc, "ABNORMALCODE", true);
		String department = SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", true);
		String subCode = SMessageUtil.getBodyItemValue(doc, "SUBCODE", true);
		String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", true);
		String aType = SMessageUtil.getBodyItemValue(doc, "ACTIONTYPE", true);
		
		if(aType.equals("Create"))
		{
			eventInfo.setEventName("Create AbnormalCode");
			eventInfo.setEventComment("Create AbnormalCode");
			
			CreateAbnormalCode(factoryName, AbnormalCode, department, subCode, description, eventInfo);
		}
		else if (aType.equals("Update"))
		{
			eventInfo.setEventName("Update AbnormalCode");
			eventInfo.setEventComment("Update AbnormalCode");

			UpdateAbnormalCode(factoryName, AbnormalCode, department, subCode, description, eventInfo);
		}
		else if (aType.equals("Delete"))
		{
			eventInfo.setEventName("Delete AbnormalCode");
			eventInfo.setEventComment("Delete AbnormalCode");

			DeleteAbnormalCode(factoryName, AbnormalCode);
		}

		return doc;

	}
	
	private void CreateAbnormalCode(String factoryName,  String AbnormalCode, String department, String subCode, String description,
			EventInfo eventInfo) throws CustomException
	{	
		try
		{
			String sql = "INSERT INTO CT_ABNORMALSHEETCODE(FACTORYNAME, ABNORMALCODE, DEPARTMENT, SUBCODE, DESCRIPTION) VALUES" +
			 " (:FACTORYNAME, :ABNORMALCODE, :DEPARTMENT, :SUBCODE, :DESCRIPTION )";
			
			Map<String,Object> bindMap = new HashMap<String,Object>();
			bindMap.put("FACTORYNAME", factoryName);
			bindMap.put("ABNORMALCODE", AbnormalCode);
			bindMap.put("DEPARTMENT", department);
			bindMap.put("SUBCODE", subCode);
			bindMap.put("DESCRIPTION", description);
			
			int result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql, bindMap);
		}
		catch(Exception e)
		{
			throw new CustomException("SYS-8001","for insert " + AbnormalCode + " into CT_ABNORMALSHEETCODE   Error : " + e.toString());
		}
	}
	
	private void UpdateAbnormalCode(String factoryName,  String AbnormalCode, String department, String subCode, String description,
			EventInfo eventInfo) throws CustomException
	{	
		try
		{
			String sql = "UPDATE CT_ABNORMALSHEETCODE SET DEPARTMENT =:DEPARTMENT, SUBCODE =:SUBCODE, DESCRIPTION=:DESCRIPTION "
					+ "WHERE FACTORYNAME =:FACTORYNAME AND ABNORMALCODE =:ABNORMALCODE";
			
			Map<String,Object> bindMap = new HashMap<String,Object>();
			bindMap.put("FACTORYNAME", factoryName);
			bindMap.put("ABNORMALCODE", AbnormalCode);
			bindMap.put("DEPARTMENT", department);
			bindMap.put("SUBCODE", subCode);
			bindMap.put("DESCRIPTION", description);
			
			int result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql, bindMap);
		}
		catch(Exception e)
		{
			throw new CustomException("SYS-8001","for update " + AbnormalCode + " set CT_ABNORMALSHEETCODE   Error : " + e.toString());
		}
	}


	private void DeleteAbnormalCode(String factoryName,  String AbnormalCode) throws CustomException
	{	
		try
		{
			String sql = "DELETE FROM CT_ABNORMALSHEETCODE "
					   + " WHERE FACTORYNAME = :FACTORYNAME AND ABNORMALCODE = :ABNORMALCODE";
			
			Map<String,Object> bindMap = new HashMap<String,Object>();
			bindMap.put("FACTORYNAME", factoryName);
			bindMap.put("ABNORMALCODE", AbnormalCode);
			
			int result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql, bindMap);
		}
		catch(Exception e)
		{
			throw new CustomException("SYS-8001","for delete " + AbnormalCode + " from the CT_ABNORMALSHEETCODE   Error : " + e.toString());
		}
	}
}
