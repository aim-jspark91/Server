package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.info.CreateInfo;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;

public class GenerateCSTGroupId extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), "CreateGroupID For STB", "", "");

		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		
		ProductRequest productRequestData = CommonUtil.getProductRequestData(productRequestName);
		ProductSpec productSpecData = CommonUtil.getProductSpecByProductSpecName("CELL", productRequestData.getProductSpecName(), "00001");
		HashMap<String, String> udfs = new HashMap<String, String>();
		
		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("PRODUCTSPECNAME", productSpecData.getKey().getProductSpecName());
		nameRuleAttrMap.put("OWNER", productSpecData.getProductionType());
				
		List<String> cstGroupId = CommonUtil.generateNameByNamingRule("CSTGroupIdNaming", nameRuleAttrMap, 1);
		
		if(StringUtils.isEmpty(cstGroupId.get(0).toString())) {
			throw new CustomException("");	// Fail generate GroupID!
		}
		
		CreateInfo createInfo = new CreateInfo();
		
		createInfo.setProcessGroupName(cstGroupId.get(0));
		createInfo.setProcessGroupType("GROUPID");
		createInfo.setMaterialType(GenericServiceProxy.getConstantMap().TYPE_LOT);
		
		createInfo.setMaterialUdfs(udfs);
		createInfo.setUdfs(udfs);		
		
		ProcessGroup newGroup = ProcessGroupServiceProxy.getProcessGroupService().create(eventInfo, createInfo);
		
		this.setReturnGroupInfo(doc, newGroup.getKey().getProcessGroupName());
		
		return doc;
	}
	
	private void setReturnGroupInfo(Document doc, String groupID)
	{
		try
		{
			StringBuilder strComment = new StringBuilder();
			
			strComment.append(groupID).append("\n");	
			
			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, strComment.toString());
		}
		catch (Exception ex)
		{
			eventLog.warn("Warning setReturnGroupInfo..");
		}
	}
}
