package kr.co.aim.messolution.lot.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DefectRuleSetting;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class DeleteDefectRuleSetting extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		Element defectRuleSettingList = SMessageUtil.getBodySequenceItem(doc, "DEFECTRULESETTINGLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteDefectRuleSetting", this.getEventUser(), this.getEventComment(), "", "");
		
		if(defectRuleSettingList != null)
		{
			for(Object obj : defectRuleSettingList.getChildren())
			{
				Element element = (Element)obj;
				String factoryName = SMessageUtil.getChildText(element, "FACTORYNAME", true);
				String productSpecName =SMessageUtil.getChildText(element, "PRODUCTSPECNAME", true);
				String productSpecVersion =SMessageUtil.getChildText(element, "PRODUCTSPECVERSION", true);
				String ProcessOperationName = SMessageUtil.getChildText(element, "PROCESSOPERATIONNAME", true);
				String ProcessOperationVersion = SMessageUtil.getChildText(element, "PROCESSOPERATIONVERSION", true);
				String defectCode = SMessageUtil.getChildText(element, "DEFECTCODE", true);
		
				DefectRuleSetting fileJudgeSetting =null;
				
				try
				{
					fileJudgeSetting = ExtendedObjectProxy.getDefectRuleSettingService().selectByKey(false, new Object[] {factoryName,productSpecName, productSpecVersion, ProcessOperationName  , ProcessOperationVersion  , defectCode });
				}
				catch (Exception ex)
				{
					fileJudgeSetting = null;
				}
				
				if(fileJudgeSetting == null)
				{
					throw new CustomException("IDLE-0006", "");
				}
				
				ExtendedObjectProxy.getDefectRuleSettingService().remove(eventInfo, fileJudgeSetting);
			}
		}
		
		return doc;
	}
}
