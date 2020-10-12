package kr.co.aim.messolution.fgms.event;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupKey;
import kr.co.aim.greentrack.processgroup.management.info.SetEventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class PausePallet extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {

		List<Element>palletList = SMessageUtil.getBodySequenceItemList(doc, "PALLET", true);
		
		for(Element pallet : palletList)
		{
			String PalnetName = SMessageUtil.getChildText(pallet, "PALLETNAME", true);
			
			ProcessGroupKey ProcessGroupkey = new ProcessGroupKey();
			ProcessGroupkey.setProcessGroupName(PalnetName);
			
			ProcessGroup processGroupDatainfo = null;
			processGroupDatainfo = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(ProcessGroupkey);
			
				SetEventInfo setEventInfo = new SetEventInfo();
				Map<String,String> processGroupUdfs = processGroupDatainfo.getUdfs();
				processGroupUdfs.put("PAUSESTATE", "OnPause");
				setEventInfo.setUdfs(processGroupUdfs);
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("Hold", getEventUser(), getEventComment(), "", "");
				ProcessGroupServiceProxy.getProcessGroupService().setEvent(ProcessGroupkey, eventInfo, setEventInfo);		
		}	
		return doc;
	}	
}
