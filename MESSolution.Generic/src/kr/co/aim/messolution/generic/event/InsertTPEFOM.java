package kr.co.aim.messolution.generic.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import org.jdom.Element;

import org.jdom.Document;

public class InsertTPEFOM extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		Element eleBody = SMessageUtil.getBodyElement(doc);
		
		String selectSql ="SELECT * FROM TPEFOMPOLICY A WHERE 1=1 " +
				"AND A.FACTORYNAME=:FACTORYNAME " +
				"AND A.PRODUCTSPECNAME=:PRODUCTSPECNAME " +
				"AND A.PRODUCTSPECVERSION=:PRODUCTSPECVERSION " +
				"AND A.ECCODE=:ECCODE " +
				"AND A.PROCESSFLOWNAME=:PROCESSFLOWNAME " +
				"AND A.PROCESSFLOWVERSION=:PROCESSFLOWVERSION " +
				"AND A.PROCESSOPERATIONNAME=:PROCESSOPERATIONNAME " +
				"AND A.PROCESSOPERATIONVERSION=:PROCESSOPERATIONVERSION " +
				"AND A.MACHINENAME=:MACHINENAME " ;
		String insertSql ="INSERT INTO TPEFOMPOLICY (FACTORYNAME, " +
				"PRODUCTSPECNAME, " +
				"PRODUCTSPECVERSION, " +
				"ECCODE, " +
				"PROCESSFLOWNAME, " +
				"PROCESSFLOWVERSION, " +
				"PROCESSOPERATIONNAME, " +
				"PROCESSOPERATIONVERSION, " +
				"MACHINENAME, " +
				"MACHINERECIPENAME, " +
				"PHOTOMASK, " +
				"PROBECARD) VALUES (:FACTORYNAME, " +
				":PRODUCTSPECNAME, " +
				":PRODUCTSPECVERSION, " +
				":ECCODE, " +
				":PROCESSFLOWNAME, " +
				":PROCESSFLOWVERSION, " +
				":PROCESSOPERATIONNAME, " +
				":PROCESSOPERATIONVERSION, " +
				":MACHINENAME, " +
				":MACHINERECIPENAME, " +
				":PHOTOMASK, " +
				":PROBECARD " +
				") " ;
		
		if (eleBody != null) 
		{
			for (Element eleLot : SMessageUtil.getBodySequenceItemList(doc, "TPEFOMLIST", true))
			{
				// Key
				String factoryName = SMessageUtil.getChildText(eleLot, "FACTORYNAME", true);
				String productSpecName = SMessageUtil.getChildText(eleLot, "PRODUCTSPECNAME", true);
				String productSpecVersion = SMessageUtil.getChildText(eleLot, "PRODUCTSPECVERSION", true);
				String eccode = SMessageUtil.getChildText(eleLot, "ECCODE", true);
				String processFlowName = SMessageUtil.getChildText(eleLot, "PROCESSFLOWNAME", true);
				String processFlowVersion = SMessageUtil.getChildText(eleLot, "PROCESSFLOWVERSION", true);
				String processOperationName = SMessageUtil.getChildText(eleLot, "PROCESSOPERATIONNAME", true);
				String processOperationVersion = SMessageUtil.getChildText(eleLot, "PROCESSOPERATIONVERSION", true);
				String machineName = SMessageUtil.getChildText(eleLot, "MACHINENAME", true);
				// not Key
				String machineRecipeName = SMessageUtil.getChildText(eleLot, "MACHINERECIPENAME", false);
				String photoMask = SMessageUtil.getChildText(eleLot, "PHOTOMASK", false);
				String probeCard = SMessageUtil.getChildText(eleLot, "PROBECARD", false);
				
			    Map<String, Object> bindMap = new HashMap<String, Object>();
			    bindMap.put("FACTORYNAME", factoryName);
			    bindMap.put("PRODUCTSPECNAME", productSpecName);
			    bindMap.put("PRODUCTSPECVERSION", productSpecVersion);
			    bindMap.put("ECCODE", eccode);
			    bindMap.put("PROCESSFLOWNAME", processFlowName);
			    bindMap.put("PROCESSFLOWVERSION", processFlowVersion);
			    bindMap.put("PROCESSOPERATIONNAME", processOperationName);
			    bindMap.put("PROCESSOPERATIONVERSION", processOperationVersion);
			    bindMap.put("MACHINENAME", machineName);
			    bindMap.put("MACHINERECIPENAME", machineRecipeName);
			    bindMap.put("PHOTOMASK", photoMask);
			    bindMap.put("PROBECARD", probeCard);

			    int result = GenericServiceProxy.getSqlMesTemplate().update(selectSql, bindMap);
			    
			    if(result==0){
			    	// Create Case
			    	GenericServiceProxy.getSqlMesTemplate().update(insertSql, bindMap);
			    }else{
			    	// Exist Key
			    	// Create Error
			    	throw new CustomException("POLICY-0030",factoryName,productSpecName,productSpecVersion,eccode,processFlowName,processFlowVersion,processOperationName,processOperationVersion,machineName);
			    }
				
				
			}
		}
		
		return doc;

	}

}
