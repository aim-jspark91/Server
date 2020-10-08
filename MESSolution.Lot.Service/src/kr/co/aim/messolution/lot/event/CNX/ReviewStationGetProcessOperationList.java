package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.processoperationspec.ProcessOperationSpecServiceProxy;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

import org.jdom.Document;
import org.jdom.Element;

public class ReviewStationGetProcessOperationList extends AsyncHandler { 
	
	@Override 
	public void doWorks(Document doc) throws CustomException {
		
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "GetProcessOperationListReply");
		
		String factoryName 	= SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String processOperationGroup = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONGROUP", true);
		
		try
		{
			this.generateBodyTemplate(doc, factoryName, processOperationGroup);	
		}
		catch(Exception ex)
		{
			throw new CustomException("ReviewStation-0001", "");
		}
		GenericServiceProxy.getESBServive().sendReplyBySender(getOriginalSourceSubjectName(), doc, "OICSender");
	}
	
	private Element generateBodyTemplate(Document doc, String factoryName, String processOperationGroup) throws CustomException
	{
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		
		Element bodyElement = null;
		bodyElement = new Element("Body");
		
		Element dataList = new Element("DATALIST");
		
		List<ProcessOperationSpec> processOperationSpecList = null;
		if(processOperationGroup.equals("ALL"))
		{
			processOperationSpecList 
			= ProcessOperationSpecServiceProxy.getProcessOperationSpecService().select("WHERE FACTORYNAME = ? AND PROCESSOPERATIONTYPE = ? AND PROCESSOPERATIONUNIT = ? ORDER BY PROCESSOPERATIONNAME ASC", new Object[]{factoryName,"Production","Glass"});	
		}
		
		else if(processOperationGroup.equals("DUMMY"))
		{
			try {
				processOperationSpecList 
				= ProcessOperationSpecServiceProxy.getProcessOperationSpecService().select("WHERE FACTORYNAME = ? AND PROCESSOPERATIONNAME IN (SELECT PROCESSOPERATIONNAME FROM CT_FILEJUDGESETTING WHERE AUTOREVIEWFLAG = ?)  ORDER BY PROCESSOPERATIONNAME ASC", new Object[]{factoryName,"N"});
			} catch (Exception e) {

			}
			
		}
		if(processOperationSpecList!=null && processOperationSpecList.size()>0){
			for(ProcessOperationSpec processOperationSpec : processOperationSpecList)
			{
				Element data = new Element("DATA");
				
				Element factoryNameElement = new Element("FACTORYNAME");
				factoryNameElement.setText(factoryName);
				data.addContent(factoryNameElement);
				
				Element processOperationNameElement = new Element("PROCESSOPERATIONNAME");
				processOperationNameElement.setText(processOperationSpec.getKey().getProcessOperationName());
				data.addContent(processOperationNameElement);
				
				Element processOperationVerElement = new Element("PROCESSOPERATIONVERSION");
				processOperationVerElement.setText(processOperationSpec.getKey().getProcessOperationVersion());
				data.addContent(processOperationVerElement);
				
				Element processFlowVerElement = new Element("PROCESSFLOWVERSION");
				processFlowVerElement.setText("00001");
				data.addContent(processFlowVerElement);
				
				Element processOperationDescElement = new Element("PROCESSOPERATIONDESC");
				processOperationDescElement.setText(processOperationSpec.getDescription());
				data.addContent(processOperationDescElement);
				
				Element processOperationTypeElement = new Element("PROCESSOPERATIONTYPE");
				processOperationTypeElement.setText(processOperationSpec.getProcessOperationType());
				data.addContent(processOperationTypeElement);
				
				Element detailProcessOperationTypeElement = new Element("DETAILPROCESSOPERATIONTYPE");
				detailProcessOperationTypeElement.setText(processOperationSpec.getDetailProcessOperationType());
				data.addContent(detailProcessOperationTypeElement);
				
				Element layerNameElement = new Element("LAYERNAME");
				layerNameElement.setText(processOperationSpec.getUdfs().get("LAYERNAME"));
				data.addContent(layerNameElement);
				
				dataList.addContent(data);
			}
		}
		

		bodyElement.addContent(dataList);
		
		doc.getRootElement().addContent(2, bodyElement);
		
		return bodyElement;
	}
	
}
