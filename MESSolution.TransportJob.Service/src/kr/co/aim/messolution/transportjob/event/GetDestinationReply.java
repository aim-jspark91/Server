package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class GetDestinationReply extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		try {
			Element root = doc.getRootElement();
			
			String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);
			String carrierNameValue = SMessageUtil.getHeaderItemValue(doc, "CARRIERNAME", true);
			String transportJobNameValue = SMessageUtil.getHeaderItemValue(doc, "TRANSPORTJOBNAME",false);
			String destinationMachineNameValue =  SMessageUtil.getHeaderItemValue(doc,"DESTINATIONMACHINENAME", false);
			String destinationPositionTypeValue = SMessageUtil.getHeaderItemValue(doc,"DESTINATIONPOSITIONTYPE", false);
			String destinationPositionNameValue =  SMessageUtil.getHeaderItemValue(doc,"DESTINATIONPOSITIONNAME", false);
			String destinationZoneNameValue = SMessageUtil.getHeaderItemValue(doc,"DESTINATIONZONENAME", false);
			String priorityValue =  SMessageUtil.getHeaderItemValue(doc,"PRIORITY", false);
			String carrierStateValue =  SMessageUtil.getHeaderItemValue(doc,"CARRIERSTATE", false);
			String lotNameValue =  SMessageUtil.getHeaderItemValue(doc,"LOTNAME", false);
			String productQuantityValue =  SMessageUtil.getHeaderItemValue(doc,"PRODUCTQUANTITY", false);
			String returnMessage =  SMessageUtil.getHeaderItemValue(doc,"RETURN", false);
		
			// Body Element Recreate
			Element messageNameElement = root.getChild(SMessageUtil.Header_Tag).getChild("MESSAGENAME");
			messageNameElement.setText("GetDestinationReply");

			Element bodyElement = root.getChild(SMessageUtil.Body_Tag);
			bodyElement.detach();

			Element returnElement = root.getChild(SMessageUtil.Return_Tag);
			if(returnElement != null)
				returnElement.detach();
			
			bodyElement = new Element(SMessageUtil.Body_Tag);
			root.addContent(bodyElement);
			
			Element transportJobName = new Element("TRANSPORTJOBNAME");
			if(!StringUtils.isEmpty(transportJobNameValue))
			{
				transportJobName.setText(transportJobNameValue);
			}
			else
			{
				transportJobName.setText(CommonUtil.generateTransportJobId(carrierNameValue, ""));
			}
			bodyElement.addContent(transportJobName);	
			
			Element carrierNameElement = new Element("CARRIERNAME");
			if(StringUtils.isEmpty(carrierNameValue))
			{
				throw new CustomException("TRN-006",carrierNameValue);
			}
			carrierNameElement.setText(carrierNameValue);
			bodyElement.addContent(carrierNameElement);	
			
			Element destinationMachineName = new Element("DESTINATIONMACHINENAME");
			destinationMachineName.setText("");
			if(!StringUtils.isEmpty(destinationMachineNameValue))
			{
				destinationMachineName.setText(destinationMachineNameValue);
			}
			bodyElement.addContent(destinationMachineName);	
			
			Element destinationPositionType = new Element("DESTINATIONPOSITIONTYPE");
			destinationPositionType.setText("");
			if(!StringUtils.isEmpty(destinationPositionTypeValue))
			{
				destinationPositionType.setText(destinationPositionTypeValue);
			}
			bodyElement.addContent(destinationPositionType);			
			
			Element destinationPositionName = new Element("DESTINATIONPOSITIONNAME");
			destinationPositionName.setText("").toString();
			if(!StringUtils.isEmpty(destinationPositionNameValue))
			{
				destinationPositionName.setText(destinationPositionNameValue);
			}
			bodyElement.addContent(destinationPositionName);	
			
			Element destinationZoneName = new Element("DESTINATIONZONENAME");
			destinationZoneName.setText("");
			if(!StringUtils.isEmpty(destinationZoneNameValue))
			{
				destinationZoneName.setText(destinationZoneNameValue);
			}
			bodyElement.addContent(destinationZoneName);	

			Element priority = new Element("PRIORITY");
			priority.setText("");
			if(!StringUtils.isEmpty(priorityValue))
			{
				priority.setText(priorityValue);
			}
			bodyElement.addContent(priority);	

			Element carrierState = new Element("CARRIERSTATE");
			carrierState.setText("");
			if(!StringUtils.isEmpty(carrierStateValue))
			{
				carrierState.setText(carrierStateValue);
			}
			bodyElement.addContent(carrierState);	

			Element lotName = new Element("LOTNAME");
			lotName.setText("");
			if(!StringUtils.isEmpty(lotNameValue))
			{
				lotName.setText(lotNameValue);
			}
			bodyElement.addContent(lotName);	
			
			Element productQuantity = new Element("PRODUCTQUANTITY");
			productQuantity.setText("");
			if(!StringUtils.isEmpty(productQuantityValue))
			{
				productQuantity.setText(productQuantityValue);
			}
			bodyElement.addContent(productQuantity);	

			Element returnEle = new Element("RETURN");
			if(!StringUtils.isEmpty(returnMessage))
			{
				returnEle.setText(returnMessage);
				bodyElement.addContent(returnEle);	
			}
			returnElement = new Element("RETURN");
			root.addContent(returnElement);
			
			GenericServiceProxy.getESBServive().sendBySender(doc, "HIFSender");
			
		} catch (Exception e) {
			eventLog.error(e);
			
			if (e instanceof CustomException) {
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ((CustomException) e).errorDef.getErrorCode());
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ((CustomException) e).errorDef.getLoc_errorMessage());
			} else {
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, (e != null) ? e.getClass().getName() : "SYS-0000");
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, (e != null && StringUtils.isNotEmpty(e.getMessage())) ? e.getMessage() : "Unknown exception is occurred.");
			}
			
			GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");

			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
		}
	}
	
}