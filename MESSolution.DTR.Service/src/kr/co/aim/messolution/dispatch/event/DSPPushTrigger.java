package kr.co.aim.messolution.dispatch.event;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

public class DSPPushTrigger extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		// Port.TransferState = ReadyToLoad (Stock -> Push) 
		// Transport To Port¡¯s ConnectedStocker(LoadFrom)


		
		//3. Make Transport Job Doc
//		Document texDoc = this.writeTransportJob(doc, carrierName, sourceMachineName, sourcePositionType, sourcePositionName, sourceZoneName, 
//				destinationMachineName, "PORT", destinationPortName, "", "", "");
		

		//4. Make Transport Job
//		sendToTEM(texDoc);
	}

	private void sendToTEM(Document doc)
	{
		// send to TEMsvr
		try
		{
			String targetSubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEXsvr");

			GenericServiceProxy.getESBServive().sendBySender(targetSubject, doc, "TEXSender");
		}
		catch (Exception e)
		{
			eventLog.error("sending to TEMsvr is failed");
		}
	}

	private Document writeTransportJob(Document doc, String carrierName, String sourceMachineName, String sourcePositionType, String sourcePositionName, String sourceZoneName, 
			String destinationMachineName, String destinationPositionType, String destinationPositionName, String destinationZoneName, String region, String kanban)
			throws CustomException
	{
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "RequestTransportJobRequest");

		boolean result = doc.getRootElement().removeChild(SMessageUtil.Body_Tag);

		Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);

		Element element1 = new Element("CARRIERNAME");
		element1.setText(carrierName);
		eleBodyTemp.addContent(element1);

		Element element2 = new Element("SOURCEMACHINENAME");
		element2.setText(sourceMachineName);
		eleBodyTemp.addContent(element2);

		Element element3 = new Element("SOURCEZONENAME");
		element3.setText(sourceZoneName);
		eleBodyTemp.addContent(element3);

		Element element4 = new Element("SOURCEPOSITIONTYPE");
		element4.setText(sourcePositionType);
		eleBodyTemp.addContent(element4);

		Element element5 = new Element("SOURCEPOSITIONNAME");
		element5.setText(sourcePositionName);
		eleBodyTemp.addContent(element5);

		Element element6 = new Element("DESTINATIONMACHINENAME");
		element6.setText(destinationMachineName);
		eleBodyTemp.addContent(element6);

		Element element7 = new Element("DESTINATIONZONENAME");
		element7.setText(destinationZoneName);
		eleBodyTemp.addContent(element7);

		Element element8 = new Element("DESTINATIONPOSITIONTYPE");
		element8.setText(destinationPositionType);
		eleBodyTemp.addContent(element8);

		Element element9 = new Element("DESTINATIONPOSITIONNAME");
		element9.setText(destinationPositionName);
		eleBodyTemp.addContent(element9);

		Element element10 = new Element("LOTNAME");
		element10.setText("");
		eleBodyTemp.addContent(element10);

		Element element11 = new Element("PRODUCTQUANTITY");
		element11.setText("0");
		eleBodyTemp.addContent(element11);

		Element element12 = new Element("CARRIERSTATE");
		element12.setText("");
		eleBodyTemp.addContent(element12);

		Element element13 = new Element("PRIORITY");
		element13.setText("0");
		eleBodyTemp.addContent(element13);
		
		Element element14 = new Element("REGION");
		element14.setText(region);
		eleBodyTemp.addContent(element14);

		Element element15 = new Element("KANBAN");
		element15.setText(kanban);
		eleBodyTemp.addContent(element15);
		
		eventLog.debug("Make Transport Job");

		//overwrite
		doc.getRootElement().addContent(eleBodyTemp);

		return doc;
	}
}