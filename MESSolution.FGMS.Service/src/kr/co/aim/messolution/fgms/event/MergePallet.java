package kr.co.aim.messolution.fgms.event;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupKey;
import kr.co.aim.greentrack.processgroup.management.info.SetEventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class MergePallet extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {

		String palletName = SMessageUtil.getBodyItemValue(doc, "PALLETNAME", true);
		String sourcePalletName = SMessageUtil.getBodyItemValue(doc, "SOURCEPALLETNAME", true);
		String sBoxSize = SMessageUtil.getBodyItemValue(doc, "SOURCEQUANTITY", true);
		List<Element> boxList = SMessageUtil.getBodySequenceItemList(doc, "BOXLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Split", getEventUser(), getEventComment(), "", "");
				
		//Original Pallet
		ProcessGroupKey oPalletKey = new ProcessGroupKey(sourcePalletName);
		ProcessGroup oPalletData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(oPalletKey);
		
		//Check Invoice
		this.CheckInvoice(oPalletData);
		
		//update Original Pallet
		oPalletData.setMaterialQuantity(oPalletData.getMaterialQuantity()-Integer.parseInt(sBoxSize));
						
		ProcessGroupServiceProxy.getProcessGroupService().update(oPalletData);
		SetEventInfo setEventInfo = new SetEventInfo();
		Map<String, String> udfs = oPalletData.getUdfs();
		setEventInfo.setUdfs(udfs);
		
		MESProcessGroupServiceProxy.getProcessGroupServiceImpl().setEvent(oPalletData, setEventInfo, eventInfo);
		
		//Box Split, Assign
		for (Element eleBox : boxList) 
		{
			String boxName = SMessageUtil.getChildText(eleBox, "PROCESSGROUPNAME", true);
					
			ProcessGroup processGroupData = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getProcessGroupData(boxName);
			
			//Check Invoice
			this.CheckInvoice(processGroupData);
			
			if(!StringUtils.equals(palletName, processGroupData.getSuperProcessGroupName()))
			{
				this.MergeBox(processGroupData, palletName);
			}
		}
		
		//Target Pallet
		ProcessGroupKey tPalletKey = new ProcessGroupKey(palletName);
		ProcessGroup tPalletData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(tPalletKey);
				
		//update Target Pallet
		tPalletData.setMaterialQuantity(boxList.size());
								
		ProcessGroupServiceProxy.getProcessGroupService().update(tPalletData);
		setEventInfo = new SetEventInfo();
		Map<String, String> tudfs = tPalletData.getUdfs();
		setEventInfo.setUdfs(tudfs);
		
		eventInfo = EventInfoUtil.makeEventInfo("Merge", getEventUser(), getEventComment(), "", "");
				
		MESProcessGroupServiceProxy.getProcessGroupServiceImpl().setEvent(tPalletData, setEventInfo, eventInfo);

		return doc;
	}
	
	private void MergeBox(ProcessGroup boxData, String palletName)
	{		
		boxData.setSuperProcessGroupName(palletName);
		ProcessGroupServiceProxy.getProcessGroupService().update(boxData);
	}
	
	private void CheckInvoice(ProcessGroup pData) throws CustomException
	{
		if(StringUtils.isNotEmpty(pData.getUdfs().get("INVOICENO")))
		{
			throw new CustomException("PROCESSGROUP-0005", pData.getKey().getProcessGroupName());
		}
	}
}
