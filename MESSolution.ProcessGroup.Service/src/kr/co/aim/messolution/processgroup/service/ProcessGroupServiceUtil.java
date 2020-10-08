package kr.co.aim.messolution.processgroup.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.name.NameServiceProxy;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupKey;
import kr.co.aim.greentrack.processgroup.management.info.ext.MaterialU;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProcessGroupServiceUtil {

	private static Log log = LogFactory.getLog(ProcessGroupServiceUtil.class);

	/*
	* Name : setBatchUSequence
	* Desc : This function is setBatchUSequence
	* Author : AIM Systems, Inc
	* Date : 2011.01.11
	*/
	public List<MaterialU> setBatchUSequence(List<String> batchlotlist) throws FrameworkErrorSignal, NotFoundSignal{
		
		List<MaterialU> materialUList = new ArrayList<MaterialU>();
		
		MaterialU materialU = null;
		for(String lotName : batchlotlist){
			
			materialU = new MaterialU();
			materialU.setMaterialName(lotName);
			materialUList.add(materialU);
		}
		
		return materialUList;
	}
	
	/*
	* Name : checkExistProcessGroup
	* Desc : This function is NanaTrack API Call checkExistProcessGroup
	* Author : AIM Systems, Inc
	* Date : 2015.08.05
	*/
	public boolean checkExistProcessGroup(String processGroupName) throws CustomException{

		String condition = "PROCESSGROUPNAME = ?";
		
		Object[] bindSet = new Object[] {processGroupName};
		
		try
		{
			List <ProcessGroup> sqlResult = ProcessGroupServiceProxy.getProcessGroupService().select(condition, bindSet);
			if(sqlResult.size() > 0)
			{
				throw new CustomException("PROCESSGROUP-0001",processGroupName);
				
			}
			return false;
		}
		catch(Exception e)
		{
			 return false;
		}
	}
	
	/*
	* Name : getProcessGroupData
	* Desc : This function is NanaTrack API Call getProcessGroupData
	* Author : AIM Systems, Inc
	* Date : 2015.08.05
	*/
	public ProcessGroup getProcessGroupData(String processGroupName) throws FrameworkErrorSignal, NotFoundSignal, CustomException 
	{
		try 
		{
			ProcessGroupKey processGroupKey = new ProcessGroupKey();
			processGroupKey.setProcessGroupName(processGroupName);
			ProcessGroup processGroupData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(processGroupKey);

			return processGroupData;

		} catch (Exception e) {
			throw new CustomException("LOT-9000", processGroupName);
		} 
	}
	
	/*
	* Name : getPalletNamesFromInvoiceNo
	* Author : AIM Systems, Inc
	* Date : 2016.01.29
	*/
	public List<String> getPalletNamesFromInvoiceNo(String invoiceNo)
	{
		String query =
			"SELECT PROCESSGROUPNAME FROM PROCESSGROUP\n" +
			" WHERE PROCESSGROUPTYPE = 'Pallet' AND INVOICENO = ?\n" + 
			" ORDER BY PROCESSGROUPNAME";
		
		Object[] bindSet = new Object[] { invoiceNo };
		
		String[][] result = greenFrameServiceProxy.getSqlTemplate().queryForStringArray(query, bindSet);
		
		List<String> returnValue = new ArrayList<String>();
		
		for (String[] row : result)
			returnValue.add(row[0].toString());
		
		return returnValue;
	}
	public List<ProcessGroup> getPalletListFromInvoiceNo(String invoiceNo){
		String condition = " where InvoiceNo=? ";
		Object[] bindSet = {invoiceNo};
		List<ProcessGroup> select = ProcessGroupServiceProxy.getProcessGroupService().select(condition, bindSet);
		return select;
	}
	
	//-COMMENT-
	//2010.02.23 JUNG SUN KYU // 2016.02.22 LEE HYEON WOO
	public static String createEmptyBoxID(String ruleName, 
										  String productSpecName, 
										  String grade, 
										  String revisionCode, 
										  String quantity)
	{
		// 01. NameGeneratorRuleDef에 대한 쿼리문을 작성한다.		
		String sql = "SELECT SQL_TEXT " +
					 "FROM NAMEGENERATORRULEDEF " +
				     "WHERE RULENAME = :ruleName ";				
		
		// 02. bindMap값을 입력하여 쿼리를 수행한다.
		Map<String, String> bindMap = new HashMap<String, String>();
		
		bindMap.put("ruleName", ruleName);
		
		List<Map<String, Object>> sqlResult 
		  = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		
		// 03. SQL_Text 쿼리 수행 하고 결과값을 List에 담는다.
		String sSqlText = "";
		
        if( sqlResult.size()> 0 ) {
        	sSqlText = sqlResult.get(0).get("SQL_TEXT").toString();
        } else 
        {        	
        }
		
        // 04. bindMap값에  ProductSpecName 입력하여 쿼리를 수행한다.
		bindMap.clear();
		sqlResult.clear();
		bindMap = new HashMap<String, String>();
		bindMap.put("productSpecName", productSpecName);
		bindMap.put("grade", grade);
		bindMap.put("revisionCode", revisionCode);
		
		sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sSqlText, bindMap);
		
		List<String> argSeq = new ArrayList();
			
		for( int i = 0; i < sqlResult.size(); i++ ) {
			
			argSeq.add(sqlResult.get(i).get("ENUMVALUE").toString());
		}			
	
		// 05. generateName 함수를 호출 한다. String -> int 형 변환
		List<String> names = 
			NameServiceProxy.getNameGeneratorRuleDefService().generateName(ruleName, argSeq, Integer.valueOf(quantity).intValue());
		
		String[] receiveNames = new String[names.size()];
	    
		for(int i = 0 ;i < names.size(); i++) {
	    	receiveNames[i] = names.get(i).toString();
	    	log.info("EMPTY BOX ID LIST = " + i + " : " +  receiveNames[i]);			
	    }
		
		return receiveNames[0];
	}
	
	// -- COMMENT
	// -- 2016.02.23 LEE HYEOM WOO
	public static String oqaComplete(ProcessGroup processGroup)
	{
		String sql = "SELECT CO.OQACOMPLETE AS OQACOMPLETE FROM CT_OQALOTINFO CO, PROCESSGROUP PG " +
		"WHERE  CO.OQALOTNAME = PG.OQALOTNAME   " +
		"AND PROCESSGROUPNAME = :processGroupName " ;
		
		Map<String, String> bindMap = new HashMap<String, String>();
		
		bindMap.put("processGroupName", processGroup.getKey().getProcessGroupName());
		
		List<Map<String, Object>> sqlResult 
		  = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		
		// 03. SQL_Text 쿼리 수행 하고 결과값을 List에 담는다.
		String oqaComplete= "";
		
	    if( sqlResult.size()> 0 ) {
	    	oqaComplete = sqlResult.get(0).get("OQACOMPLETE").toString();
	    } 
	    
	    return oqaComplete;
	}
	
	// -- COMMENT
	// -- 2016.02.23 LEE HYEOM WOO
	public static String planBoxQty(ProcessGroup processGroup)
	{
		String sql = "SELECT (EMPTYBOXQTY+CREATEQTY) AS PLANBOXQTY FROM PROCESSGROUP "
				   + "WHERE PROCESSGROUPNAME = :processGroupName " ;
		
		Map<String, String> bindMap = new HashMap<String, String>();
		
		bindMap.put("processGroupName", processGroup.getKey().getProcessGroupName());
		
		List<Map<String, Object>> sqlResult 
		  = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		
		// 03. SQL_Text 쿼리 수행 하고 결과값을 List에 담는다.
		String planBoxQty= "";
		
	    if( sqlResult.size()> 0 ) {
	    	planBoxQty = sqlResult.get(0).get("PLANBOXQTY").toString();
	    } 
	    
	    return planBoxQty;
	}
}
