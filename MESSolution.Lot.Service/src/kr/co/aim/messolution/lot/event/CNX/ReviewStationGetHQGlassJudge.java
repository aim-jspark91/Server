package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.HQGlassJudge;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.xml.JdomUtils;

import org.jdom.Document;
import org.jdom.Element;

public class ReviewStationGetHQGlassJudge extends AsyncHandler {
	@Override
	public void doWorks(Document doc) throws CustomException {
		
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "GetHQGlassJudgeReply");
		
		String glassName    			   = SMessageUtil.getBodyItemValue(doc, "GLASSNAME", true);
		
		try
		{
			this.generateBodyTemplate(doc, glassName);
		}
		catch(Exception ex)
		{
			this.generateBodyTemplateNoData(doc, glassName);
		}
		GenericServiceProxy.getMessageLogService().getLog().debug(new StringBuffer("jeongSuTest : ").append(JdomUtils.toString(doc)).toString());
		GenericServiceProxy.getESBServive().sendReplyBySender(getOriginalSourceSubjectName(), doc, "OICSender");
	}
	
	
	private Element generateBodyTemplate(Document doc, String glassName) throws CustomException
	{
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		
		Element bodyElement = null;
		bodyElement = new Element("Body");
		
		Element dataList = new Element("DATALIST");
		
		List<HQGlassJudge> hqGlassJudgeList 
			= ExtendedObjectProxy.getHQGlassJudgeService().select("WHERE GLASSNAME = ?", new Object[]{glassName});
		
		for(HQGlassJudge hqGlassJudgeData : hqGlassJudgeList)
		{
			Element data = new Element("DATA");
			
			Element glassNameElement = new Element("GLASSNAME");
			glassNameElement.setText(hqGlassJudgeData.getGlassName());
			data.addContent(glassNameElement);
			
			Element hqGlassJudgeElement = new Element("HQGLASSJUDGE");
			hqGlassJudgeElement.setText(hqGlassJudgeData.getHQGlassJudge());
			data.addContent(hqGlassJudgeElement);
			
			Element hqGlassNameElement = new Element("HQGLASSNAME");
			hqGlassNameElement.setText(hqGlassJudgeData.getHQGlassName());
			data.addContent(hqGlassNameElement);
			
			Element lastEventCommentElement = new Element("LASTEVENTCOMMENT");
			lastEventCommentElement.setText(hqGlassJudgeData.getLastEventComment());
			data.addContent(lastEventCommentElement);
			
			Element lastEventNameElement = new Element("LASTEVENTNAME");
			lastEventNameElement.setText(hqGlassJudgeData.getLastEventName());
			data.addContent(lastEventNameElement);
			
			Element lastEventTimeElement = new Element("LASTEVENTTIME");
			lastEventTimeElement.setText(String.valueOf(hqGlassJudgeData.getLastEventTime()));
			data.addContent(lastEventTimeElement);
			
			Element lastEventUserElement = new Element("LASTEVENTUSER");
			lastEventUserElement.setText(hqGlassJudgeData.getLastEventUser());
			data.addContent(lastEventUserElement);
			
			Element xaxisElement = new Element("XAXIS");
			xaxisElement.setText(String.valueOf(hqGlassJudgeData.getXAxis()));
			data.addContent(xaxisElement);
			
			Element yaxisElement = new Element("YAXIS");
			yaxisElement.setText(String.valueOf(hqGlassJudgeData.getYAxis()));
			data.addContent(yaxisElement);
			
			dataList.addContent(data);
		}
		
		bodyElement.addContent(dataList);
		
		doc.getRootElement().addContent(2, bodyElement);
		
		return bodyElement;
	}
	
	
	private Element generateBodyTemplateNoData(Document doc, String glassName) throws CustomException
	{
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		
		Element bodyElement = null;
		bodyElement = new Element("Body");
		
		Element dataList = new Element("DATALIST");
		
		Element data = new Element("DATA");
		
		Element glassNameElement = new Element("GLASSNAME");
		glassNameElement.setText("");
		data.addContent(glassNameElement);
		
		Element hqGlassJudgeElement = new Element("HQGLASSJUDGE");
		hqGlassJudgeElement.setText("");
		data.addContent(hqGlassJudgeElement);
		
		Element hqGlassNameElement = new Element("HQGLASSNAME");
		hqGlassNameElement.setText("");
		data.addContent(hqGlassNameElement);
		
		Element lastEventCommentElement = new Element("LASTEVENTCOMMENT");
		lastEventCommentElement.setText("");
		data.addContent(lastEventCommentElement);
		
		Element lastEventNameElement = new Element("LASTEVENTNAME");
		lastEventNameElement.setText("");
		data.addContent(lastEventNameElement);
		
		Element lastEventTimeElement = new Element("LASTEVENTTIME");
		lastEventTimeElement.setText("");
		data.addContent(lastEventTimeElement);
		
		Element lastEventUserElement = new Element("LASTEVENTUSER");
		lastEventUserElement.setText("");
		data.addContent(lastEventUserElement);
		
		Element xaxisElement = new Element("XAXIS");
		xaxisElement.setText("");
		data.addContent(xaxisElement);
		
		Element yaxisElement = new Element("YAXIS");
		yaxisElement.setText("");
		data.addContent(yaxisElement);
		
		dataList.addContent(data);
		
		
		bodyElement.addContent(dataList);
		
		doc.getRootElement().addContent(2, bodyElement);
		
		return bodyElement;
	}
}
