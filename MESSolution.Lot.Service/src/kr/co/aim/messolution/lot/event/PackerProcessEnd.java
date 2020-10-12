package kr.co.aim.messolution.lot.event;

import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.port.management.data.Port;

import org.jdom.Document;
import org.jdom.Element;

public class PackerProcessEnd extends SyncHandler {

	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		//MACHINENAME
		//BOXNAME
		//PRODUCTSPECNAME
		//PRODUCTQUANTITY
		//PRODUCTLIST
		//  PRODUCT
		//     PRODUCTNAME
		//     LOTNAME
		//     PRODUCTJUDGE
		
		//pre-processing for sync
		Document jobEndDoc = (Document) doc.clone();
		Element bodyElement = SMessageUtil.getBodyElement(doc);
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "SendPackingBoxID");
		Element dateElement = new Element("DATE");
		dateElement.setText(TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_DAY));
		bodyElement.addContent(dateElement);
		bodyElement.removeContent(SMessageUtil.getBodySequenceItem(doc, "PRODUCTLIST", true));
		
		//only considering CPX case, port is fixed with 'V04'
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "BOXNAME", false);
		//List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);
		//String portName = "V04";
		
		//Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		//Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		
		//assign PP box
		if (StringUtil.isEmpty(carrierName))
		{
			//PP box standard is formal
			String durableSpecName = "GPPBOXU7V601";
			
			List<Durable> durableList = DurableServiceProxy.getDurableService().select("durableType = ? AND durableSpecName = ? AND durableState = ? ORDER BY durableName", new Object[] {"PPBox", durableSpecName, GenericServiceProxy.getConstantMap().Dur_Available});
			
			Durable durableData = durableList.get(0);
			
			SMessageUtil.setBodyItemValue(doc, "BOXNAME", durableData.getKey().getDurableName());
			
			carrierName = durableData.getKey().getDurableName();
		}
		else
		{
			//existing label identity
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		}
		
		try
		{
			sendLotProcessEnd(jobEndDoc, machineName, "V04", "", carrierName);
		}
		catch (Exception ex)
		{
			eventLog.error("failed to do semi LotProcessEnd");
		}
		
		//BOXNAME
		//PRODUCTSPECNAME
		//DATE
		//PRODUCTQUANTITY
		
		return doc;
	}
	
	/**
	 * semi-Job end
	 * @author swcho
	 * @since 2015/04/08
	 * @param doc
	 * @param machineName
	 * @param portName
	 * @param lotName
	 * @param carrierName
	 * @throws CustomException
	 */
	private void sendLotProcessEnd(Document doc, String machineName, String portName, String lotName, String carrierName)
		throws CustomException
	{
		//Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		//Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "LotProcessEnd");
		Element bodyElement = SMessageUtil.getBodyElement(doc);
		
		//Element machineElement = new Element("MACHINENAME");
		//machineElement.setText(machineName);
		//bodyElement.addContent(machineElement);
		
		Element lotElement = new Element("LOTNAME");
		lotElement.setText("");
		bodyElement.addContent(lotElement);
		
		Element durableElement = new Element("CARRIERNAME");
		durableElement.setText(carrierName);
		bodyElement.addContent(durableElement);
		
		Element lotGradeElement = new Element("LOTJUDGE");
		lotGradeElement.setText("");
		bodyElement.addContent(lotGradeElement);
		
		Element portElement = new Element("PORTNAME");
		portElement.setText(portName);
		bodyElement.addContent(portElement);
		
		Element portTypeElement = new Element("PORTTYPE");
		portTypeElement.setText(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"));
		bodyElement.addContent(portTypeElement);
		
		Element portUseTypeElement = new Element("PORTUSETYPE");
		portUseTypeElement.setText(CommonUtil.getValue(portData.getUdfs(), "PORTUSETYPE"));
		bodyElement.addContent(portUseTypeElement);
		
		//Element QTYElement = new Element("");
		//QTYElement.setText("");
		//bodyElement.addContent(QTYElement);
		
		List<Element> productElementList = SMessageUtil.getSubSequenceItemList(bodyElement, "PRODUCTLIST", false);
		
		int dPosition = 0;
		
		for (Element productElement : productElementList)
		{
			dPosition++;
			
			Element positionElement = new Element("POSITION");
			positionElement.setText(String.valueOf(dPosition));
			productElement.addContent(positionElement);
			
			//Element productGradeElement = new Element("PRODUCTJUDGE");
			//productGradeElement.setText("");
			//productElement.addContent(productGradeElement);
			
			Element processInfoElement = new Element("PROCESSINGINFO");
			processInfoElement.setText("N");
			productElement.addContent(processInfoElement);
		}
		
		GenericServiceProxy.getESBServive().send(GenericServiceProxy.getESBServive().getSendSubject("PEXsvr"), doc);
	}
}
