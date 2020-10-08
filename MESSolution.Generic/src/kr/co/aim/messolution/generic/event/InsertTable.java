package kr.co.aim.messolution.generic.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;
import org.jdom.Element;

public class InsertTable extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		
		String TableName = SMessageUtil.getBodyItemValue(doc, "TABLENAME", true);
		List<Element> columnNameList = SMessageUtil.getBodySequenceItemList(doc, "COLNAMELIST", true);
		List<Element> rowList = SMessageUtil.getBodySequenceItemList(doc, "ROWLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("RptExcelInsert", getEventUser(), getEventComment(), "", "");
	    
		StringBuilder DeleteSql = new StringBuilder();
		DeleteSql.append(" DELETE FROM "); 
		DeleteSql.append(TableName);
		
		Map args = null;
		int Delresult = GenericServiceProxy.getSqlMesTemplate().update(DeleteSql.toString(), args);
		
		for(Element eleRowData : rowList)
		{			
			StringBuilder InsertColumns = new StringBuilder();
			StringBuilder InsertValues = new StringBuilder();
			
			
			for(Element eleColName : columnNameList)
		    {
		    	String colName =  SMessageUtil.getChildText(eleColName, "COLUMNNAME", true);
		    	String rowValue = SMessageUtil.getChildText(eleRowData, colName , false);
		    	
		    	InsertColumns.append(colName);
		    	InsertColumns.append(" ,");
		    	
		    	InsertValues.append("'");
		    	InsertValues.append(rowValue);
		    	InsertValues.append("' ,");
		    	
		    }
			InsertColumns.setLength(InsertColumns.length() -1); //delete last "," 
			InsertValues.setLength(InsertValues.length() -1); //delete last ","
			
			

			
			StringBuilder InsertSql = new StringBuilder();
			InsertSql.append(" INSERT INTO ");
			InsertSql.append(TableName);
			InsertSql.append(" ( ");
			InsertSql.append(InsertColumns);
			InsertSql.append(" ) VALUES ( ");
			InsertSql.append(InsertValues);
			InsertSql.append(" )");

			
			int Insresult = GenericServiceProxy.getSqlMesTemplate().update(InsertSql.toString(), args);
		}
		StringBuilder InsertTimeSql = new StringBuilder();
		InsertTimeSql.append(" UPDATE ");
		InsertTimeSql.append(TableName);
		InsertTimeSql.append(" SET INSERTUSER = '"+eventInfo.getEventUser()+"' ,");
		InsertTimeSql.append(" INSERTTIMEKEY = '"+eventInfo.getEventTimeKey()+"'");
		int InsTimeresult = GenericServiceProxy.getSqlMesTemplate().update(InsertTimeSql.toString(), args);
		
		return doc;
	}

}
