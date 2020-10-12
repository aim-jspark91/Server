package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.product.management.data.Product;

import org.jdom.Document;
import org.jdom.Element;

public class ReviewStationGetGlassID extends AsyncHandler {
	@Override
	public void doWorks(Document doc) throws CustomException {
		
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "GetGlassIDReply");
		
		String glassID 			   = SMessageUtil.getBodyItemValue(doc, "GLASSID", true);
		
		try
		{
			this.generateBodyTemplate(doc, glassID);
		}
		catch(Exception ex)
		{
			this.generateBodyTemplateNoData(doc, glassID);
		}
		GenericServiceProxy.getMessageLogService().getLog().debug(new StringBuffer("jeongSuTest : ").append(JdomUtils.toString(doc)).toString());
		GenericServiceProxy.getESBServive().sendReplyBySender(getOriginalSourceSubjectName(), doc, "OICSender");
	}

	private Element generateBodyTemplate(Document doc, String glassID) throws CustomException
	{
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		
		Element bodyElement = null;
		bodyElement = new Element("Body");
		
		Product product = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(glassID);
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(product.getLotName());
		
		Element glassIDElement = new Element("GLASSID");
		glassIDElement.setText(product.getKey().getProductName());
		bodyElement.addContent(glassIDElement);

		
		String lastAOIMachine = "";
		String lastAOIOper = "";
		
		String sql = "SELECT P.MACHINENAME, P.PROCESSOPERATIONNAME " +
				"FROM CT_PRODUCTINUNITORSUBUNIT P, MACHINESPEC MS " +
				"WHERE P.PRODUCTNAME = :PRODUCTNAME " +
				"      AND MS.CONSTRUCTTYPE = :CONSTRUCTTYPE " +
				"      AND P.MACHINENAME = MS.MACHINENAME " +
				"      AND P.PRODUCTNAME = :PRODUCTNAME " +
				"ORDER BY P.TIMEKEY DESC " ;


		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PRODUCTNAME", product.getKey().getProductName());	
		bindMap.put("CONSTRUCTTYPE", "AOI");
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		if(resultList != null && resultList.size() > 0)
		{	
			lastAOIMachine = (String)resultList.get(0).get("MACHINENAME");
			lastAOIOper = (String)resultList.get(0).get("PROCESSOPERATIONNAME");
		}
		
		Element processOperationNameElement = new Element("PROCESSOPERATIONNAME");
		processOperationNameElement.setText(lastAOIOper);
		bodyElement.addContent(processOperationNameElement);
		
		Element lotNameElement = new Element("LOTNAME");
		lotNameElement.setText(lotData.getKey().getLotName());
		bodyElement.addContent(lotNameElement);
		
		Element machineNameElement = new Element("MACHINENAME");
		machineNameElement.setText(lastAOIMachine);
		bodyElement.addContent(machineNameElement);
		
		doc.getRootElement().addContent(2, bodyElement);
		
		return bodyElement;
	}
	
	private Element generateBodyTemplateNoData(Document doc, String glassID) throws CustomException
	{
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		
		Element bodyElement = null;
		bodyElement = new Element("Body");
		
		Element glassIDElement = new Element("GLASSID");
		glassIDElement.setText("");
		bodyElement.addContent(glassIDElement);
		
		Element processOperationNameElement = new Element("PROCESSOPERATIONNAME");
		processOperationNameElement.setText("");
		bodyElement.addContent(processOperationNameElement);
		
		Element lotNameElement = new Element("LOTNAME");
		lotNameElement.setText("");
		bodyElement.addContent(lotNameElement);
		
		Element machineNameElement = new Element("MACHINENAME");
		machineNameElement.setText("");
		bodyElement.addContent(machineNameElement);
		
		doc.getRootElement().addContent(2, bodyElement);
		
		return bodyElement;
	}
}
