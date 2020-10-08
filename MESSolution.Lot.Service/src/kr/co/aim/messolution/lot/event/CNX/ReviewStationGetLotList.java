package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

import org.jdom.Document;
import org.jdom.Element;

public class ReviewStationGetLotList extends AsyncHandler { 
	
	@Override 
	public void doWorks(Document doc) throws CustomException {
		
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "GetLotListReply");
		
		String factoryName 			   = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String processOperationName    = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		
		try
		{
			this.generateBodyTemplate(doc, factoryName, processOperationName);
		}
		catch(Exception ex)
		{
			this.generateBodyTemplateNoLot(doc, factoryName, processOperationName);
			
		}
		//GenericServiceProxy.getMessageLogService().getLog().debug(new StringBuffer("jeongSuTest : ").append(JdomUtils.toString(doc)).toString());
		GenericServiceProxy.getESBServive().sendReplyBySender(getOriginalSourceSubjectName(), doc, "OICSender");
	}
	
	private Element generateBodyTemplate(Document doc, String factoryName, String processOperationName) throws CustomException
	{
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		
		Element bodyElement = null;
		bodyElement = new Element("Body");
		
		Element dataList = new Element("DATALIST");
		
		List<Lot> lotList = LotServiceProxy.getLotService().select("WHERE LOTSTATE = ? AND FACTORYNAME = ? AND BEFOREOPERATIONNAME = ? AND LOTNAME IN (SELECT LOTNAME FROM CT_LOTMULTIHOLD WHERE REASONCODE= ?)", new Object[]{GenericServiceProxy.getConstantMap().Lot_Released, factoryName, processOperationName,"GHLD"});
		for(Lot lotData : lotList)
		{
			Element data = new Element("DATA");
			
			Element lotNameElement = new Element("LOTNAME");
			lotNameElement.setText(lotData.getKey().getLotName());
			data.addContent(lotNameElement);
			
			Element processOperationNameElement = new Element("PROCESSOPERATIONNAME");
			processOperationNameElement.setText(lotData.getProcessOperationName());
			data.addContent(processOperationNameElement);
			
			Element carrierNameElement = new Element("CARRIERNAME");
			carrierNameElement.setText(lotData.getCarrierName());
			data.addContent(carrierNameElement);
			
			Element productSpecNameElement = new Element("PRODUCTSPECNAME");
			productSpecNameElement.setText(lotData.getProductSpecName());
			data.addContent(productSpecNameElement);
			
			Element machineNameElement = new Element("MACHINENAME");
			machineNameElement.setText(lotData.getUdfs().get("LASTLOGGEDOUTMACHINE"));
			//machineNameElement.setText(machineName);
			data.addContent(machineNameElement);
			try {
				MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(lotData.getUdfs().get("LASTLOGGEDOUTMACHINE"));
				Element machineTypeElement = new Element("MACHINETYPE");
				machineTypeElement.setText(machineSpecData.getMachineType());
				data.addContent(machineTypeElement);
			} catch (Exception e) {
				// TODO: handle exception
			}

			
			Element beforeProcessFlowNameElement = new Element("BEFOREPROCESSFLOWNAME");
			beforeProcessFlowNameElement.setText(lotData.getUdfs().get("BEFOREFLOWNAME"));
			data.addContent(beforeProcessFlowNameElement);
			
			Element beforeOperationNameElement = new Element("BEFOREOPERATIONNAME");
			beforeOperationNameElement.setText(lotData.getUdfs().get("BEFOREOPERATIONNAME"));
			data.addContent(beforeOperationNameElement);
			
			ProcessOperationSpec operationSpecData = CommonUtil.getProcessOperationSpec(factoryName, processOperationName);
			
			if(operationSpecData.getProcessOperationUnit().equals("Glass"))
			{
				Element productTypeElement = new Element("PRODUCTTYPE");
				productTypeElement.setText("GLASS");
				data.addContent(productTypeElement);
			}

			dataList.addContent(data);
		}

		bodyElement.addContent(dataList);
		doc.getRootElement().addContent(2, bodyElement);
		
		return bodyElement;
	}
	
	private Element generateBodyTemplateNoLot(Document doc, String factoryName, String processOperationName) throws CustomException
	{
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		
		Element bodyElement = null;
		bodyElement = new Element("Body");
		
		Element dataList = new Element("DATALIST");
		

		Element data = new Element("DATA");
		
		Element lotNameElement = new Element("LOTNAME");
		lotNameElement.setText("");
		data.addContent(lotNameElement);
		
		Element processOperationNameElement = new Element("PROCESSOPERATIONNAME");
		processOperationNameElement.setText("");
		data.addContent(processOperationNameElement);
		
		Element carrierNameElement = new Element("CARRIERNAME");
		carrierNameElement.setText("");
		data.addContent(carrierNameElement);
		
		Element productSpecNameElement = new Element("PRODUCTSPECNAME");
		productSpecNameElement.setText("");
		data.addContent(productSpecNameElement);
		
		Element machineNameElement = new Element("MACHINENAME");
		machineNameElement.setText("");
		data.addContent(machineNameElement);
		
		
		Element machineTypeElement = new Element("MACHINETYPE");
		machineTypeElement.setText("");
		data.addContent(machineTypeElement);
		
		Element beforeProcessFlowNameElement = new Element("BEFOREPROCESSFLOWNAME");
		beforeProcessFlowNameElement.setText("");
		data.addContent(beforeProcessFlowNameElement);
		
		Element beforeOperationNameElement = new Element("BEFOREOPERATIONNAME");
		beforeOperationNameElement.setText("");
		data.addContent(beforeOperationNameElement);
		
		Element productTypeElement = new Element("PRODUCTTYPE");
		productTypeElement.setText("");
		data.addContent(productTypeElement);

		dataList.addContent(data);
		

		bodyElement.addContent(dataList);
		doc.getRootElement().addContent(2, bodyElement);
		
		return bodyElement;
	}
	
}
