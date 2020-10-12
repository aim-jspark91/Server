package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Inhibit;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.name.NameServiceProxy;

import org.jdom.Document;

public class RegisterInhibit extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException
	{	
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", false);
		String ecCode = SMessageUtil.getBodyItemValue(doc, "ECCODE", false);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", false);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", false);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String machineGroupName = SMessageUtil.getBodyItemValue(doc, "MACHINEGROUP", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", false);
		String exceptionLotCount = SMessageUtil.getBodyItemValue(doc, "EXCEPTIONLOTCOUNT", false);
		String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
		String department = SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", true);
	
		// jjyoo on 2018.12.5 - Check the same inhibit data
		StringBuilder sqlStatement = new StringBuilder();
		Map<String, String> bindMap = new HashMap<String, String>();
		
		sqlStatement.append("SELECT * FROM CT_INHIBIT WHERE 1=1 ");
		if(productSpecName == "")
		{
			sqlStatement.append("AND PRODUCTSPECNAME IS NULL ");
		}
		else
		{
			sqlStatement.append("AND PRODUCTSPECNAME = :PRODUCTSPECNAME ");
			bindMap.put("PRODUCTSPECNAME", productSpecName);
		}
		if(ecCode == "")
		{
			sqlStatement.append("AND ECCODE IS NULL ");
		}
		else
		{
			sqlStatement.append("AND ECCODE = :ECCODE ");
			bindMap.put("ECCODE", ecCode);
		}
		if(processFlowName == "")
		{
			sqlStatement.append("AND PROCESSFLOWNAME IS NULL ");
		}
		else
		{
			sqlStatement.append("AND PROCESSFLOWNAME = :PROCESSFLOWNAME ");
			bindMap.put("PROCESSFLOWNAME", processFlowName);
		}
		if(processOperationName == "")
		{
			sqlStatement.append("AND PROCESSOPERATIONNAME IS NULL ");
		}
		else
		{
			sqlStatement.append("AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
			bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		}
		if(machineGroupName == "")
		{
			sqlStatement.append("AND MACHINEGROUPNAME IS NULL ");
		}
		else
		{
			sqlStatement.append("AND MACHINEGROUPNAME = :MACHINEGROUPNAME ");
			bindMap.put("MACHINEGROUPNAME", machineGroupName);
		}
		if(machineName == "")
		{
			sqlStatement.append("AND MACHINENAME IS NULL ");
		}
		else
		{
			sqlStatement.append("AND MACHINENAME = :MACHINENAME ");
			bindMap.put("MACHINENAME", machineName);
		}
		if(unitName == "")
		{
			sqlStatement.append("AND UNITNAME IS NULL ");
		}
		else
		{
			sqlStatement.append("AND UNITNAME = :UNITNAME ");
			bindMap.put("UNITNAME", unitName);
		}
		if(subUnitName == "")
		{
			sqlStatement.append("AND SUBUNITNAME IS NULL ");
		}
		else
		{
			sqlStatement.append("AND SUBUNITNAME = :SUBUNITNAME ");
			bindMap.put("SUBUNITNAME", subUnitName);
		}
		if(recipeName == "")
		{
			sqlStatement.append("AND RECIPENAME IS NULL ");
		}
		else
		{
			sqlStatement.append("AND RECIPENAME = :RECIPENAME ");
			bindMap.put("RECIPENAME", recipeName);
		}
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStatement.toString(), bindMap);
		
		if(sqlResult.size() > 0)
		{
			throw new CustomException("INHIBIT-0006", ""); 
		}
		else
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Register", this.getEventUser(), this.getEventComment(), "", "");
			
			List<String> argSeq = new ArrayList<String>();
			setTodayRuleNaming("InhibitNaming");
			List<String> inhibitIDList = NameServiceProxy.getNameGeneratorRuleDefService().generateName("InhibitNaming", argSeq, 1);
			String inhibitID = inhibitIDList.get(0);
			
			Inhibit inhibitData = new Inhibit(inhibitID);
			
			inhibitData.setProductSpecName(productSpecName); 
			inhibitData.setEcCode(ecCode);
			inhibitData.setProcessFlowName(processFlowName);
			inhibitData.setProcessFlowVersion(processFlowVersion);
			inhibitData.setProcessOperationName(processOperationName);
			inhibitData.setMachineGroupName(machineGroupName);
			inhibitData.setMachineName(machineName);
			inhibitData.setUnitName(unitName);
			inhibitData.setSubUnitName(subUnitName);
			inhibitData.setRecipeName(recipeName);
			if(exceptionLotCount != "")
			{
				inhibitData.setExceptionLotCount(Long.parseLong(exceptionLotCount));
			}
			else
			{
				inhibitData.setExceptionLotCount(0);
			}
			inhibitData.setProcessLotCount(0);
			inhibitData.setDescription(description);
			inhibitData.setDepartment(department);
			
			inhibitData.setLastEventName(eventInfo.getEventName());
			inhibitData.setLastEventTimekey(eventInfo.getEventTimeKey());
			inhibitData.setLastEventTime(eventInfo.getEventTime());
			inhibitData.setLastEventUser(eventInfo.getEventUser());
			inhibitData.setLastEventComment(eventInfo.getEventComment());
			inhibitData.setCreateTime(eventInfo.getEventTime());
			inhibitData.setCreateUser(eventInfo.getEventUser());
			
			ExtendedObjectProxy.getInhibitService().create(eventInfo, inhibitData);
		
			//SMessageUtil.addItemToBody(doc, "INHIBITID", inhibitID);
			setNextInfo(doc, inhibitID);
		}
		return doc;
	}
	private void setTodayRuleNaming(String ruleNaming){
		// Naming Rule 은 잘 안바뀌니 Hardcoding으로 작성
		StringBuilder prefixBuilder = new StringBuilder();
		prefixBuilder.append("IHB_");
		prefixBuilder.append(Calendar.getInstance().get(Calendar.YEAR));
		int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		if (month < 10)
		{
			prefixBuilder.append("0" + month);
		}
		else
		{
			prefixBuilder.append("" + month);
		}
		int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		if (day < 10)
		{
			prefixBuilder.append("0" + day);
		}
		else
		{
			prefixBuilder.append("" + day);
		}
		String prefix =prefixBuilder.toString();
		
	    Map<String, Object> bindMap = new HashMap<String, Object>();
	    bindMap.put("PRIFIX", prefix);
	    String selectSql = "SELECT * FROM CT_INHIBIT A WHERE A.INHIBITID LIKE :PRIFIX||'%'";
	    int result=0;
	    try {
	    	result = GenericServiceProxy.getSqlMesTemplate().update(selectSql, bindMap);
		} catch (Exception e) {

		}

	    if(result==0){
		    // 만일 결과가 0이면
			// 오늘 날짜에 해당하는 시퀀스가 존재하지 않는다.
			// 시퀀스를 000으로 수정한다.
	    	bindMap.put("LASTSERIALNO", "000");
	    	bindMap.put("RULENAME", ruleNaming);
			String modifySql ="UPDATE NAMEGENERATORSERIAL SET LASTSERIALNO=:LASTSERIALNO WHERE RULENAME=:RULENAME ";
			GenericServiceProxy.getSqlMesTemplate().update(modifySql, bindMap);
	    }

	}
	
	private void setNextInfo(Document doc, String inhibitID)
	{
		try
		{	
			StringBuilder strComment = new StringBuilder();
			strComment.append("InhibitID").append("[").append(inhibitID).append("]").append("\n");
			//setEventComment(strComment.toString());
			
			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, strComment.toString());
		}
		catch (Exception ex)
		{
			//eventLog.warn("Note after Crate is nothing");
		}
	}
}
