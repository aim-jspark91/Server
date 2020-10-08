package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.HQGlassJudge;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class ReviewStationUpdateHQglassJudge extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "UpdateHQglassJudgeReply");
		
		List<Element> dataList = SMessageUtil.getBodySequenceItemList(doc, "DATALIST", true);
		
		
		for (Element data : dataList)
		{
			String glassName = SMessageUtil.getChildText(data, "GLASSNAME", true);
			String hqGlassJudge = SMessageUtil.getChildText(data, "HQGLASSJUDGE", true);
			String hqGlassName = SMessageUtil.getChildText(data, "HQGLASSNAME", true);
			String lastEventComment = SMessageUtil.getChildText(data, "LASTEVENTCOMMENT", true);
			String lastEventName = SMessageUtil.getChildText(data, "LASTEVENTNAME", true);
			String lastEventTime = SMessageUtil.getChildText(data, "LASTEVENTTIME", true);
			String lastEventUser = SMessageUtil.getChildText(data, "LASTEVENTUSER", true);
			String xaxis = SMessageUtil.getChildText(data, "XAXIS", true);
			String yaxis = SMessageUtil.getChildText(data, "YAXIS", true);

			HQGlassJudge hqGlassJudgeData = null;
			
			try
			{
				hqGlassJudgeData = ExtendedObjectProxy.getHQGlassJudgeService().selectByKey(false, new Object[]{hqGlassName});
			}
			catch(Exception ex)
			{
				throw new CustomException("ReviewStation-0001", "");
			}
			
			hqGlassJudgeData.setHQGlassJudge(hqGlassJudge);
			hqGlassJudgeData.setLastEventComment(lastEventComment);
			hqGlassJudgeData.setLastEventName(lastEventName);
			hqGlassJudgeData.setLastEventTime(TimeStampUtil.getTimestamp(lastEventTime));
			hqGlassJudgeData.setLastEventUser(lastEventUser);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Update", this.getEventUser(), this.getEventComment(), "", "");
			ExtendedObjectProxy.getHQGlassJudgeService().modify(eventInfo, hqGlassJudgeData);

		}
		

		GenericServiceProxy.getMessageLogService().getLog().debug(new StringBuffer("jeongSuTest : ").append(JdomUtils.toString(doc)).toString());
		
		try {
			GenericServiceProxy.getESBServive().sendReplyBySender(getOriginalSourceSubjectName(), doc, "OICSender");
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}

}
