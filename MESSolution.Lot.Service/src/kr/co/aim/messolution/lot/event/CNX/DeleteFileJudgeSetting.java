package kr.co.aim.messolution.lot.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.FileJudgeSetting;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class DeleteFileJudgeSetting extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		Element fileJudgeSettingList = SMessageUtil.getBodySequenceItem(doc, "FILEJUDGESETTINGLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteFileJudgeSetting", this.getEventUser(), this.getEventComment(), "", "");
		
		if(fileJudgeSettingList != null)
		{
			for(Object obj : fileJudgeSettingList.getChildren())
			{
				Element element = (Element)obj;
				String ProcessFlowName = SMessageUtil.getChildText(element, "PROCESSFLOWNAME", true);
				String ProcessFlowVersion = SMessageUtil.getChildText(element, "PROCESSFLOWVERSION", true);
				String ProcessOperationName = SMessageUtil.getChildText(element, "PROCESSOPERATIONNAME", true);
				String ProcessOperationVersion = SMessageUtil.getChildText(element, "PROCESSOPERATIONVERSION", true);
				FileJudgeSetting fileJudgeSetting =null;
				
				try
				{
					fileJudgeSetting = ExtendedObjectProxy.getFileJudgeSettingService().selectByKey(false, new Object[] {ProcessFlowName, ProcessFlowVersion, ProcessOperationName  , ProcessOperationVersion   });
				}
				catch (Exception ex)
				{
					fileJudgeSetting = null;
				}
				
				if(fileJudgeSetting == null)
				{
					throw new CustomException("IDLE-0006", "");
				}
				
				ExtendedObjectProxy.getFileJudgeSettingService().remove(eventInfo, fileJudgeSetting);
			}
		}
		
		return doc;
	}
}
