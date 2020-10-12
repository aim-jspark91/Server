package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupKey;

import org.jdom.Document;
import org.jdom.Element;

public class CreatePPBoxNaming2 extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		String maker = SMessageUtil.getBodyItemValue(doc, "MAKER", true);
		//String description = SMessageUtil.getBodyItemValue(doc, "PRODUCTCATEGORY", true);
		String productCategory = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", true);
		String productModel = SMessageUtil.getBodyItemValue(doc, "PRODUCTMODEL", true);
		//String glassType = SMessageUtil.getBodyItemValue(doc, "GLASSTYPE", true);
		//String quantity = SMessageUtil.getBodyItemValue(doc, "QUANTITY", true);
		//String processGroupName = SMessageUtil.getBodyItemValue(doc, "PROCESSGROUPNAME", true);
		String rejectsCode =  SMessageUtil.getBodyItemValue(doc, "RejectsCode", true);
		String lineNo =  SMessageUtil.getBodyItemValue(doc, "LineNo", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventName("Create");
		
		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		
		List<String> BarCodeName = CommonUtil.generateNameByNamingRule("PPboxSequence2", nameRuleAttrMap, 1);
		
		//update cell print NamingRule--yanyan--20170424
		if(BarCodeName != null && BarCodeName.size() > 0)
		{	
			//String productCategory = "";
			if(productCategory.equals("TN")) {
				productCategory = "N";
			} else if(productCategory.equals("TWVA+")) {
				productCategory = "W";
			}
			else {
				throw new CustomException("SYS-9999", "productSpec", "productSpec is not TN or TWVA+,please choose again");
			}
			
			String BarCode = maker + productCategory + productModel + BarCodeName.get(0) +lineNo +"-" + rejectsCode ;
			
			ProcessGroupKey processGroupKey = new ProcessGroupKey();
			//processGroupKey.setProcessGroupName(processGroupName);
			//ProcessGroup boxData = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getProcessGroupData(processGroupName);
			
			//Map<String, String> udfs = boxData.getUdfs();
			//udfs.put("barCode", BarCode);			
			//boxData.setUdfs(udfs);
			
			//ProcessGroupServiceProxy.getProcessGroupService().update(boxData);
			
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
