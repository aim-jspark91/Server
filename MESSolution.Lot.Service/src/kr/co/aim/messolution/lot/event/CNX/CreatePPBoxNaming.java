package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupKey;

import org.jdom.Document;
import org.jdom.Element;

public class CreatePPBoxNaming extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		String maker = SMessageUtil.getBodyItemValue(doc, "MAKER", true);
		String productCategory = SMessageUtil.getBodyItemValue(doc, "PRODUCTCATEGORY", true);
		String productModel = SMessageUtil.getBodyItemValue(doc, "PRODUCTMODEL", true);
		String glassType = SMessageUtil.getBodyItemValue(doc, "GLASSTYPE", true);
		String quantity = SMessageUtil.getBodyItemValue(doc, "QUANTITY", true);
		String processGroupName = SMessageUtil.getBodyItemValue(doc, "PROCESSGROUPNAME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventName("Create");
		
		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		
		List<String> BarCodeName = CommonUtil.generateNameByNamingRule("PPboxSequence", nameRuleAttrMap, 1);
		
		if(BarCodeName != null && BarCodeName.size() > 0)
		{
			String BarCode = maker + productCategory + productModel + glassType + BarCodeName.get(0) + quantity;
			
			ProcessGroupKey processGroupKey = new ProcessGroupKey();
			processGroupKey.setProcessGroupName(processGroupName);
			ProcessGroup boxData = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getProcessGroupData(processGroupName);
			
			Map<String, String> udfs = boxData.getUdfs();
			udfs.put("barCode", BarCode);			
			boxData.setUdfs(udfs);
			
			ProcessGroupServiceProxy.getProcessGroupService().update(boxData);
			
			Element bodyElement = SMessageUtil.getBodyElement(doc);
			Element eleBarCodeName = new Element("BARCODENAME");
			eleBarCodeName.setText(BarCode);
			bodyElement.addContent(eleBarCodeName);
		}
		else
		{
			throw new CustomException("SYS-9999", "BarCode", "Check NamimgRlue");
		}
		
		return doc;
	}
}
