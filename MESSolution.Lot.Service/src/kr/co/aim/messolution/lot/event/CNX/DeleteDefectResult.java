package kr.co.aim.messolution.lot.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DefectResult;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class DeleteDefectResult extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		Element defectResultList = SMessageUtil.getBodySequenceItem(doc, "DEFECTRESULTLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteDefectRuleSetting", this.getEventUser(), this.getEventComment(), "", "");
		
		if(defectResultList != null)
		{
			for(Object obj : defectResultList.getChildren())
			{
				Element element = (Element)obj;
//				String factoryName = SMessageUtil.getChildText(element, "FACTORYNAME", true);
//				String lotName = SMessageUtil.getChildText(element, "LOTNAME", true);
//				String carrierName = SMessageUtil.getChildText(element, "CARRIERNAME", true);
//				String productName = SMessageUtil.getChildText(element, "PRODUCTNAME", true);
//				String panelName = SMessageUtil.getChildText(element, "PANELNAME", true);
//				String cutNumber = SMessageUtil.getChildText(element, "CUTNUMBER", true);
//				String coordiNate_x = SMessageUtil.getChildText(element, "COORDINATE_X", true);
//				String coordiNate_y = SMessageUtil.getChildText(element, "COORDINATE_Y", true);
//				String productSpecName = SMessageUtil.getChildText(element, "PRODUCTSPECNAME", true);
//				String productSpecVersion = SMessageUtil.getChildText(element, "PRODUCTSPECVERSION", true);
//				String processOperationName = SMessageUtil.getChildText(element, "PROCESSOPERATIONNAME", true);
//				String processOperationVersion = SMessageUtil.getChildText(element, "PROCESSOPERATIONVERSION", true);
//				String machineName = SMessageUtil.getChildText(element, "MACHINENAME", true);
//				
//				String originLayer = SMessageUtil.getBodyItemValue(doc, "ORIGINLAYER", false);
//				String defectCode = SMessageUtil.getBodyItemValue(doc, "DEFECTCODE", true);
//				String defectSize = SMessageUtil.getBodyItemValue(doc, "DEFECTSIZE", false);
//				String autoJudgeFlag = SMessageUtil.getBodyItemValue(doc, "AUTOJUDGEFLAG", true);
//				String note = SMessageUtil.getBodyItemValue(doc, "NOTE", true);
//				String inspectionTime = SMessageUtil.getBodyItemValue(doc, "INSPECTIONTIME", true);
//
//				String holdFlag = SMessageUtil.getBodyItemValue(doc, "HOLDFLAG", true);
//				String mailFlag = SMessageUtil.getBodyItemValue(doc, "MAILFLAG", true);
//				String userGroupName = SMessageUtil.getBodyItemValue(doc, "USERGROUPNAME", true);
				
				String defectResultId = SMessageUtil.getChildText(element, "DEFECTRESULTID", true);
				
				DefectResult defectResult = null;

				
				try
				{
					defectResult = ExtendedObjectProxy.getDefectResultService().selectByKey(false, new Object[] {defectResultId});
				}
				catch (Exception ex)
				{
					defectResult = null;
				}
				
				if(defectResult == null)
				{
					throw new CustomException("COMMON-0001", "Not exist DefectResult");
				}
				
				
				ExtendedObjectProxy.getDefectResultService().remove(eventInfo, defectResult);
				
			}
		}
		
		return doc;
	}
}
