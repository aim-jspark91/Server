package kr.co.aim.messolution.fgms.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.name.NameServiceProxy;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupKey;
import kr.co.aim.greentrack.processgroup.management.info.CreateInfo;
import kr.co.aim.greentrack.processgroup.management.info.SetEventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class SplitPallet extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String palletName = SMessageUtil.getBodyItemValue(doc, "PALLETNAME", true);
		List<Element> boxList = SMessageUtil.getBodySequenceItemList(doc, "BOXLIST", true);
		int sBoxSize = 0;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Split", getEventUser(), getEventComment(), "", "");
		
		//Original Pallet
		ProcessGroupKey oPalletKey = new ProcessGroupKey(palletName);
		ProcessGroup oPalletData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(oPalletKey);
		
		//Check Invoice
		this.CheckInvoice(oPalletData);
		
		//CreatePalletName
		List<String> argSeq = new ArrayList<String>();
		argSeq.add(oPalletData.getUdfs().get("PRODUCTSPECNAME"));
				
		List<String> palletList = NameServiceProxy.getNameGeneratorRuleDefService().generateName("BoxNaming", argSeq, Integer.parseInt("1"));
		String newPalletName = palletList.get(0) + "_FGI";
		
		sBoxSize = (int)oPalletData.getMaterialQuantity()-boxList.size();
		List<String> materialList = new ArrayList<String>();
		
		//update Original Pallet
		oPalletData.setMaterialQuantity(sBoxSize);
				
		ProcessGroupServiceProxy.getProcessGroupService().update(oPalletData);
		SetEventInfo setEventInfo = new SetEventInfo();
		Map<String, String> udfs = oPalletData.getUdfs();
		setEventInfo.setUdfs(udfs);
						
		MESProcessGroupServiceProxy.getProcessGroupServiceImpl().setEvent(oPalletData, setEventInfo, eventInfo);
		
		//Make New Pallet
		ProcessGroup newPallet = this.CreateNewPallet(oPalletData, newPalletName, materialList);
		
		//Box Split, Assign
		for (Element eleBox : boxList) 
		{
			String boxName = SMessageUtil.getChildText(eleBox, "PROCESSGROUPNAME", true);
			materialList.add(boxName);
			
			ProcessGroup processGroupData = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getProcessGroupData(boxName);
			
			//Check Invoice
			this.CheckInvoice(processGroupData);
			
			this.SplitBox(processGroupData, newPalletName);
		}
		
		this.AssignPallet(newPallet, materialList, boxList.size());
		
		//call by value so that reply would be modified
		XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "NEWPALLETNAME", newPalletName);
		
		return doc;
	}
	
	private ProcessGroup CreateNewPallet(ProcessGroup palletData, String newPalletName,
			List<String> materialList)
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), "", "");
		
		HashMap<String, String> udfs = new HashMap<String, String>();
		
		CreateInfo createInfo = new CreateInfo();
		
		createInfo.setProcessGroupName(newPalletName);
		createInfo.setProcessGroupType(GenericServiceProxy.getConstantMap().TYPE_PALLET);
		createInfo.setMaterialType(GenericServiceProxy.getConstantMap().TYPE_PROCESSGROUP);
		createInfo.setDetailMaterialType(GenericServiceProxy.getConstantMap().TYPE_BOX);
		createInfo.setMaterialQuantity(0);
		createInfo.setMaterialNames(materialList);
		createInfo.setMaterialUdfs(udfs);
		udfs.put("PRODUCTSPECNAME", palletData.getUdfs().get("PRODUCTSPECNAME"));
		udfs.put("PRODUCTREQUESTNAME", palletData.getUdfs().get("PRODUCTREQUESTNAME"));
		udfs.put("STOCKSTATE", GenericServiceProxy.getConstantMap().FGMS_STOCKSTATE_STOCKED);
		udfs.put("DOMESTICEXPORT", palletData.getUdfs().get("DOMESTICEXPORT"));
		udfs.put("HOLDSTATE", "N");
		createInfo.setUdfs(udfs);
		
		ProcessGroup newPallet = ProcessGroupServiceProxy.getProcessGroupService().create(eventInfo, createInfo);
		
		return newPallet;
	}
	
	private void AssignPallet(ProcessGroup palletData, List<String> materialList, 
			int materialQuantity)
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Assign", getEventUser(), getEventComment(), "", "");
		
		palletData.setMaterialQuantity(materialQuantity);
		
		ProcessGroupServiceProxy.getProcessGroupService().update(palletData);
		SetEventInfo setEventInfo = new SetEventInfo();
		Map<String, String> udfs = palletData.getUdfs();
		setEventInfo.setUdfs(udfs);
						
		MESProcessGroupServiceProxy.getProcessGroupServiceImpl().setEvent(palletData, setEventInfo, eventInfo);
	}
	
	private void SplitBox(ProcessGroup boxData, String newPalletName)
	{
		boxData.setSuperProcessGroupName(newPalletName);
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
