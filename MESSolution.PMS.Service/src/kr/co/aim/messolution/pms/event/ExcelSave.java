package kr.co.aim.messolution.pms.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.transaction.PropagationBehavior;

import org.jdom.Document;
import org.jdom.Element;


public class ExcelSave extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
	
		
		String sTABLENAME   = SMessageUtil.getBodyItemValue(doc, "TABLENAME", true);
	 
		String sErrReturnList = "" ;
 
		List<Element> eleCrateList = SMessageUtil.getBodySequenceItemList(doc, "ARRLIST", true);
		
		GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
		
		for (Element eleCrate : eleCrateList)
		{
		    // Get DEFAULTFLAG
			StringBuilder sql = new StringBuilder();
			StringBuilder sqlColumnName = new StringBuilder();
			StringBuilder sqlsColumnValue = new StringBuilder();
			Map<String, String> bindMap = new HashMap<String, String>();
			String sPartNameList = "" ;		
			String sPartName = "" ;		
			try {
	
				sql.append( " INSERT INTO ") ;
				sql.append(sTABLENAME ) ;
		
				sPartName = "" ;
				
				for(int icol =0 ; icol < eleCrate.getChildren().size() ; icol++)
				{			
					Element e = (Element) eleCrate.getChildren().get(icol);
					
					String sColumnName = e.getName();
					String sColumnValue = SMessageUtil.getChildText(eleCrate, sColumnName, false);
									
					if(icol == 0)
					{
						//sPartName = sColumnValue ;
						sqlColumnName.append(" " ).append(sColumnName) ;				
						sqlsColumnValue.append(" '").append(sColumnValue).append("'") ;	
					}
					else if(icol == 1)
					{
						sPartName = sColumnValue ;
						sqlColumnName.append(" ," ).append(sColumnName) ;				
						sqlsColumnValue.append(",'").append(sColumnValue).append("'") ;		 
					}
					else
					{
						sqlColumnName.append(" ," ).append(sColumnName) ;				
						sqlsColumnValue.append(",'").append(sColumnValue).append("'") ;		 				
					}								
				}
				
				//add NotInQty part 
				sqlColumnName.append(" ," ).append("NOTINQUANTITY") ;				
				sqlsColumnValue.append(",'").append("0").append("'") ;	
				
				sql.append(" ( " ).append(sqlColumnName).append(")") ;
				sql.append(" VALUES ( " ).append(sqlsColumnValue).append(")") ;
				
				bindMap.clear();
	
//				bindMap.put("CNXsvr", "");
				
				int ircode = GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap); // Return 1 when ok
								
				GenericServiceProxy.getTxDataSourceManager().commitTransaction();
								
				if(ircode != 1)
				{
					sErrReturnList += sPartName + ",";		
				}
				
			} catch (Exception e) 
			{						
				sErrReturnList += sPartName + ",";		
			}				
		}

		if(sErrReturnList != "")
		{
			sErrReturnList = sErrReturnList.substring(0, (sErrReturnList.length() - 1));
			//SMessageUtil.addReturnToMessage(doc,"FAILPARTLIST",sErrReturnList) ;
		}
		
		//Return FailedPartList - SingReturnWay
		Document rtnDoc = new Document();
		rtnDoc = (Document)doc.clone();
		rtnDoc = SMessageUtil.addItemToBody(rtnDoc, "FAILPARTLIST", sErrReturnList);
		return rtnDoc;
	}
}
