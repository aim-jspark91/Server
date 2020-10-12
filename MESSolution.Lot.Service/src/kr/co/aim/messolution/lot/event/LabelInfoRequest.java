package kr.co.aim.messolution.lot.event;

import java.text.SimpleDateFormat;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.product.management.data.ProductSpec;

import org.jdom.Document;
import org.jdom.Element;

public class LabelInfoRequest extends SyncHandler { 
	
	@Override 
	public Object doWorks(Document doc) throws CustomException {
		
		//pre-processing for sync
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "LabelInfoReply");
				
		String boxName = SMessageUtil.getBodyItemValue(doc, "BOXNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);		
		String zplCode = "";
			
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotInfoBydurableName(boxName);
		MachineSpec macSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);
		ProductSpec prdSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(macSpecData.getFactoryName(), lotData.getProductSpecName(), "00001");
						
		//yanyan		
		/*if (CommonUtil.getValue(macSpecData.getUdfs(), "CONSTRUCTTYPE").equals("BPK"))
		{
			CommonUtil.getZplCode("BOXLABEL", "00001");
			
			zplCode = CommonUtil.getZplCode("BOXLABEL", "00001");
			
			String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
				
			zplCode = StringUtil.replace(zplCode, "[S.O]", "TDCS1600009");
			zplCode = StringUtil.replace(zplCode, "[P.O]", "RDZC1600622");
			zplCode = StringUtil.replace(zplCode, "[P.M]", "HC001-050T2BZB");
			zplCode = StringUtil.replace(zplCode, "[CAT]", prdSpecData.getUdfs().get("PRODUCTSPECTYPE"));
			zplCode = StringUtil.replace(zplCode, "[TYPE]", lotData.getProductionType());						
			zplCode = StringUtil.replace(zplCode, "[G.C]", String.valueOf((int)lotData.getProductQuantity()));
			zplCode = StringUtil.replace(zplCode, "[P.C]", String.valueOf((int)lotData.getSubProductQuantity()));
			zplCode = StringUtil.replace(zplCode, "[S.P]", "0");
			zplCode = StringUtil.replace(zplCode, "[DATE]", currentDate);
			zplCode = StringUtil.replace(zplCode, "[ROHS]", "TEST");
			zplCode = StringUtil.replace(zplCode, "[QRCODE]", "1A001PA2100120;TDCS1600009;RDZC160022;HC001-050T2B2B;TFT;E;15;1500;0;2015-08-09;");
			zplCode = StringUtil.replace(zplCode, "BARCODE", lotData.getProcessGroupName());			
		} */
		
		// OLED Print--yanyan
        if (CommonUtil.getValue( macSpecData.getUdfs(), "CONSTRUCTTYPE").equals("BPK" ))
       {
              CommonUtil. getZplCode("BOXLABEL", "00010");
              
            zplCode = CommonUtil. getZplCode("BOXLABEL", "00010");
              
            String currentDate = new SimpleDateFormat("yyyy-MM-dd" ).format(new java.util.Date());
                    
            zplCode = StringUtil.replace(zplCode, "[L]",lotData.getKey().getLotName());
            zplCode = StringUtil.replace(zplCode ,"[PS]",lotData.getProductSpecName());
            zplCode = StringUtil.replace(zplCode, "[PQ]",String.valueOf((int)lotData.getProductQuantity()));
            zplCode = StringUtil.replace(zplCode, "[T]", lotData.getProductionType());
            zplCode = StringUtil.replace(zplCode, "[WO]", lotData.getProductRequestName());                                       
            zplCode = StringUtil.replace(zplCode, "[D]",currentDate);
            zplCode = StringUtil.replace(zplCode, "BARCODE", lotData.getProcessGroupName());
       }

				
		// Create Body Element
		this.generateBodyTemplate(doc, machineName, lotData.getProcessGroupName(), boxName, zplCode.trim());		
		
		return doc;
	}
	
	private Element generateBodyTemplate(Document doc, String machineName, String processGroupName, String boxName, 
			String zplCode) throws CustomException
	{
		Element bodyElement = null;
		bodyElement = new Element("Body");

		Element machineNameElement = new Element("MACHINENAME");
		machineNameElement.setText(machineName);
		bodyElement.addContent(machineNameElement);
		
		Element boxNameElement = new Element("BOXNAME");
		boxNameElement.setText(boxName);
		bodyElement.addContent(boxNameElement);
		
		Element processGroupElement = new Element("PROCESSGROUPNAME");
		processGroupElement.setText(processGroupName);
		bodyElement.addContent(processGroupElement);
		
		Element zplCodeElement = new Element("ZPLCODE");
		zplCodeElement.setText(zplCode);
		bodyElement.addContent(zplCodeElement);
		
		//first removal of existing node would be duplicated
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		//index of Body node is static
		doc.getRootElement().addContent(2, bodyElement);
		
		return bodyElement;
	}
}
