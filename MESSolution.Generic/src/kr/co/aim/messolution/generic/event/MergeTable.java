package kr.co.aim.messolution.generic.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;
import org.jdom.Element;

public class MergeTable extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		
		String TableName = SMessageUtil.getBodyItemValue(doc, "TABLENAME", true);
		List<Element> columnNameList = SMessageUtil.getBodySequenceItemList(doc, "COLNAMELIST", true);
		List<Element> rowList = SMessageUtil.getBodySequenceItemList(doc, "ROWLIST", true);
	    
		
		String getKeySql =	"  SELECT B.CNAME                                 "+
							"    FROM user_constraints A, COL B               "+
							"   WHERE     A.TABLE_NAME = :TABLENAME			  "+
							"         AND A.CONSTRAINT_TYPE = 'P'             "+
							"         AND A.TABLE_NAME = B.TNAME              "+
							"ORDER BY COLNO									  "	;
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("TABLENAME", TableName);
		
		List<Map<String, Object>> TableKeyList = GenericServiceProxy.getSqlMesTemplate().queryForList(getKeySql, bindMap);
		
		StringBuilder col_Key = new StringBuilder();
		for(Map<String, Object> map : TableKeyList)
		{
			col_Key.append(map.get("CNAME"));
			col_Key.append(",");
		}
		col_Key.setLength(col_Key.length()-1);// delete last","
		
		
		for(Element eleRowData : rowList)
		{			
			StringBuilder DeleteWhere = new StringBuilder();
			StringBuilder InsertColumns = new StringBuilder();
			StringBuilder InsertValues = new StringBuilder();
			
			for(Map<String, Object> map : TableKeyList)
			{
				String keyName = (String) map.get("CNAME");
				String rowValue = SMessageUtil.getChildText(eleRowData, keyName , true);
				
				DeleteWhere.append(keyName);
				DeleteWhere.append(" = '");
				DeleteWhere.append(rowValue);
				DeleteWhere.append("' AND ");
			}
			DeleteWhere.setLength(DeleteWhere.length() - 5); //delete last "AND "
			
			for(Element eleColName : columnNameList)
		    {
		    	String colName =  SMessageUtil.getChildText(eleColName, "COLUMNNAME", true);
		    	String rowValue = SMessageUtil.getChildText(eleRowData, colName , true);
		    	
		    	InsertColumns.append(colName);
		    	InsertColumns.append(" ,");
		    	
		    	InsertValues.append("'");
		    	InsertValues.append(rowValue);
		    	InsertValues.append("' ,");
		    	
		    }
			InsertColumns.setLength(InsertColumns.length() -1); //delete last "," 
			InsertValues.setLength(InsertValues.length() -1); //delete last ","
			
			StringBuilder DeleteSql = new StringBuilder();
			DeleteSql.append(" DELETE FROM "); 
			DeleteSql.append(TableName);
			DeleteSql.append(" WHERE ");
			DeleteSql.append(DeleteWhere);
			
			StringBuilder InsertSql = new StringBuilder();
			InsertSql.append(" INSERT INTO ");
			InsertSql.append(TableName);
			InsertSql.append(" ( ");
			InsertSql.append(InsertColumns);
			InsertSql.append(" ) VALUES ( ");
			InsertSql.append(InsertValues);
			InsertSql.append(" )");

			Map args = null;
			int Delresult = GenericServiceProxy.getSqlMesTemplate().update(DeleteSql.toString(), args);
			int Insresult = GenericServiceProxy.getSqlMesTemplate().update(InsertSql.toString(), args);
			
			
		}
		
	    
		return doc;
	}

}
