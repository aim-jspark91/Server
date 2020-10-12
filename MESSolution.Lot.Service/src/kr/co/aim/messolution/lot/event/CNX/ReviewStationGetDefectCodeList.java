package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;
import org.jdom.Element;

public class ReviewStationGetDefectCodeList extends AsyncHandler { 
	
	@Override 
	public void doWorks(Document doc) throws CustomException {
		
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "GetDefectCodeListReply");
		
		String factoryName 			   = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);

		try
		{
			this.generateBodyTemplate(doc, factoryName);	
		}
		catch(Exception ex)
		{
			this.generateBodyTemplateNoData(doc, factoryName);	
		}
		GenericServiceProxy.getESBServive().sendReplyBySender(getOriginalSourceSubjectName(), doc, "OICSender");
	}
	
	private Element generateBodyTemplate(Document doc, String factoryName) throws CustomException
	{
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		
		Element bodyElement = null;
		bodyElement = new Element("Body");
		
		Element dataList = new Element("DATALIST");
		
		String sql = " SELECT REASONCODE ,DESCRIPTION" +
					 " FROM REASONCODE " +
					 " WHERE FACTORYNAME = :FACTORYNAME AND REASONCODETYPE = :REASONCODETYPE ORDER BY REASONCODE " ;
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", "ARRAY");
		bindMap.put("REASONCODETYPE", "DefectCode");
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		for(int i = 0; i < sqlResult.size() ; i++)
		{
			Element data = new Element("DATA");
			
			Element defectCodeElement = new Element("DEFECTCODE");
			defectCodeElement.setText((String)sqlResult.get(i).get("REASONCODE"));
			data.addContent(defectCodeElement);
			
			Element defectdesElement = new Element("DESCRIPTION");
			defectdesElement.setText((String)sqlResult.get(i).get("DESCRIPTION"));
			data.addContent(defectdesElement);
			
			dataList.addContent(data);
		}
		
		bodyElement.addContent(dataList);
		doc.getRootElement().addContent(2, bodyElement);
		
		//GenericServiceProxy.getMessageLogService().getLog().debug(JdomUtils.toString(doc));
		
		return bodyElement;
	}
	
	private Element generateBodyTemplateNoData(Document doc, String factoryName) throws CustomException
	{
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		
		Element bodyElement = null;
		bodyElement = new Element("Body");
		
		Element dataList = new Element("DATALIST");

		Element data = new Element("DATA");
		Element defectCodeElement = new Element("DEFECTCODE");
		
		defectCodeElement.setText("");
		data.addContent(defectCodeElement);
		dataList.addContent(data);
		
		
		bodyElement.addContent(dataList);
		doc.getRootElement().addContent(2, bodyElement);
		
		return bodyElement;
	}
}
