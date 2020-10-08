package kr.co.aim.messolution.durable.event.CNX;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.CleanInfo;
import kr.co.aim.greentrack.durable.management.info.DirtyInfo;
import kr.co.aim.greentrack.durable.management.info.RepairInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class ChangeMaskCleanState extends SyncHandler {
	private static Log log = LogFactory.getLog(ChangeMaskCleanState.class);
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String messageName = SMessageUtil.getMessageName(doc);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, this.getEventUser(), this.getEventComment(), "", "");
		
		Element eleBody = SMessageUtil.getBodyElement(doc);
		
		if(eleBody!=null)
		{
				String sDurableName = SMessageUtil.getChildText(eleBody, "DURABLENAME", true);
				String sMachineName = SMessageUtil.getChildText(eleBody, "MACHINENAME", false);
				String sDurableCleanState = SMessageUtil.getChildText(eleBody, "DURABLECLEANSTATE",true );

				//getDurableData
				Durable durableData = CommonUtil.getDurableInfo(sDurableName);
						
				if(StringUtils.equals(sDurableCleanState, GenericServiceProxy.getConstantMap().Dur_Dirty))
				{
					DirtyInfo dirtyData = MESDurableServiceProxy.getDurableInfoUtil().dirtyInfo(durableData, sMachineName);
					
					MESDurableServiceProxy.getDurableServiceImpl().dirty(durableData, dirtyData, eventInfo);
					
					log.info("Excute  ChangeMaskCleanState : "+sDurableName+sDurableCleanState);
				}
				else if(StringUtils.equals(sDurableCleanState, GenericServiceProxy.getConstantMap().Dur_Clean))
				{
					CleanInfo cleanInfo = MESDurableServiceProxy.getDurableInfoUtil().cleanInfo(durableData, sMachineName);
					
					MESDurableServiceProxy.getDurableServiceImpl().clean(durableData, cleanInfo, eventInfo);
					
					log.info("Excute  ChangeMaskCleanState : "+sDurableName+sDurableCleanState);
				}else if(StringUtils.equals(sDurableCleanState, GenericServiceProxy.getConstantMap().Dur_Repairing))
				{
					RepairInfo repairInfo = MESDurableServiceProxy.getDurableInfoUtil().repairInfo(durableData, sMachineName);
					
					MESDurableServiceProxy.getDurableServiceImpl().repair(durableData, repairInfo, eventInfo);
					
					log.info("Excute  ChangeMaskCleanState : "+sDurableName+sDurableCleanState);
				}
		}	
		return doc;
	}

}
