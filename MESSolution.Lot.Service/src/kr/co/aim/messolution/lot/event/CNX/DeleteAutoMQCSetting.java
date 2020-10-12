package kr.co.aim.messolution.lot.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AutoMQCSetting;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class DeleteAutoMQCSetting extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		Element autoMQCSettingList = SMessageUtil.getBodySequenceItem(doc, "AUTOMQCSETTINGLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteAutoMQCSetting", this.getEventUser(), this.getEventComment(), "", "");
		
		if(autoMQCSettingList != null)
		{
			for(Object obj : autoMQCSettingList.getChildren())
			{
				Element element = (Element)obj;
				String productSpecName = SMessageUtil.getChildText(element, "PRODUCTSPECNAME", true);
				String ecCode = SMessageUtil.getChildText(element, "ECCODE", true);
				String processOperationName = SMessageUtil.getChildText(element, "PROCESSOPERATIONNAME", true);
				String machineName = SMessageUtil.getChildText(element, "MACHINENAME", true);

				
				String mqcTemplateName = SMessageUtil.getChildText(element, "MQCTEMPLATENAME", true);
				//String mqcType = SMessageUtil.getChildText(element, "MQCTYPE", true);
				String mqcType = "IDLETIME";
				
				AutoMQCSetting autoMQCSetting =null;
				
				try
				{
					autoMQCSetting = ExtendedObjectProxy.getAutoMQCSettingService().selectByKey(false, new Object[] {productSpecName,ecCode,processOperationName,machineName,mqcTemplateName,mqcType});
				}
				catch (Exception ex)
				{
					autoMQCSetting = null;
				}
				
				if(autoMQCSetting == null)
				{
					throw new CustomException("IDLE-0006", "");
				}
				
				ExtendedObjectProxy.getAutoMQCSettingService().remove(eventInfo, autoMQCSetting);
			}
		}
		
		return doc;
	}
}
