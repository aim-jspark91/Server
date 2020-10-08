package kr.co.aim.messolution.dispatch.event;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import org.jdom.Document;
import org.jdom.Element;

public class UnloadRequest extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnloadRequest", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		//String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
		
		//1. Get Machine Spec Data
		MachineSpecKey msKey = new MachineSpecKey(machineName);
		MachineSpec msData = MachineServiceProxy.getMachineSpecService().selectByKey(msKey);
		
		String constructType = msData.getUdfs().get("CONSTRUCTTYPE").toString();
		
		//1.1 Check Construct Type 
		if(constructType.isEmpty()) throw new CustomException("MACHINE-9001", machineName + " [CONSTRUCTTYPE]");
		
		//1.2 Check is CST Cleaner
		// Modified by smkang on 2018.11.28 - ConstructType of cassette cleaner is changed to CCLN.
//		if(!constructType.equals("CCN")) throw new CustomException("MACHINE-9003", machineName);
		if(!constructType.equals(GenericServiceProxy.getConstantMap().ConstructType_CassetteClenaer)) throw new CustomException("MACHINE-9003", machineName);
		
		//2. Get Connected Stocker Need Clean CST
		String stockerName = getStocker(machineName, portType);
		
		if (stockerName.isEmpty()) throw new CustomException("MACHINE-9001", "");
		eventLog.debug("Transport Stocker is" + stockerName);
		
		//3. Make Transport Job Doc
		Document texDoc = this.writeTransportJob(doc, carrierName, stockerName);
		
		//4. Make Transport Job
		sendToTEM(texDoc);
	}
	
	private String getStocker(String machineName, String portType) throws CustomException
	{
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append(" SELECT P.STOCKERNAME ");
			sql.append("   FROM TMPOLICY T, ");
			sql.append("        POSCONNECTEDSTOCKER P, ");
			sql.append("        MACHINE M, ");
			sql.append("        MACHINESPEC MS ");
			sql.append("  WHERE     1 = 1 ");
			sql.append("        AND T.CONDITIONID = P.CONDITIONID ");
			sql.append("        AND M.MACHINENAME = :MACHINENAME ");
			sql.append("        AND P.PORTTYPE = :PORTTYPE ");
			sql.append("        AND MS.MACHINENAME = M.MACHINENAME ");
			sql.append("        AND T.FACTORYNAME = MS.FACTORYNAME ");
			sql.append("        AND T.MACHINENAME = MS.MACHINENAME ");
			sql.append("        AND ROWNUM = 1 ");
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("MACHINENAME", machineName);
			bindMap.put("PORTTYPE", portType);

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> machineList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
			
			String stockerName = machineList.get(0).get("STOCKERNAME").toString();
			
			return stockerName;
		}
		catch (Exception ex)
		{
			eventLog.debug("No Stocker");
			return "";
		}
	}
	
	private void sendToTEM(Document doc)
	{
		// send to TEMsvr
		try
		{
			eventLog.debug("Ready to Send to TEM");
			
			String replySubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEMsvr");
			eventLog.debug("TEM subject name is " + replySubject);

			GenericServiceProxy.getESBServive().sendReplyBySender(replySubject, doc, "TEMSender");
		}
		catch (Exception e)
		{
			eventLog.error("sending to TEMsvr is failed");
		}
	}
	
	private Document writeTransportJob(Document doc, String carrierName, String machineName)
			throws CustomException
		{
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "RequestTransportJobRequest");
			
			boolean result = doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
			
			Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);
			
			Element element1 = new Element("CARRIERNAME");
			element1.setText(carrierName);
			eleBodyTemp.addContent(element1);
			
			Element element2 = new Element("SOURCEMACHINENAME");
			element2.setText("");
			eleBodyTemp.addContent(element2);
			
			Element element3 = new Element("SOURCEZONENAME");
			element3.setText("");
			eleBodyTemp.addContent(element3);
			
			Element element4 = new Element("SOURCEPOSITIONTYPE");
			element4.setText("");
			eleBodyTemp.addContent(element4);
			
			Element element5 = new Element("SOURCEPOSITIONNAME");
			element5.setText("");
			eleBodyTemp.addContent(element5);
			
			Element element6 = new Element("DESTINATIONMACHINENAME");
			element6.setText(machineName);
			eleBodyTemp.addContent(element6);
			
			Element element7 = new Element("DESTINATIONZONENAME");
			element7.setText("");
			eleBodyTemp.addContent(element7);
			
			Element element8 = new Element("DESTINATIONPOSITIONTYPE");
			element8.setText("SHELF");
			eleBodyTemp.addContent(element8);
			
			Element element9 = new Element("DESTINATIONPOSITIONNAME");
			element9.setText("");
			eleBodyTemp.addContent(element9);
			
			Element element10 = new Element("LOTNAME");
			element10.setText("");
			eleBodyTemp.addContent(element10);
			
			Element element11 = new Element("PRODUCTQUANTITY");
			element11.setText("0");
			eleBodyTemp.addContent(element11);
			
			Element element12 = new Element("CARRIERSTATE");
			element12.setText("EMPTY");
			eleBodyTemp.addContent(element12);
			
			Element element13 = new Element("PRIORITY");
			element13.setText("50");
			eleBodyTemp.addContent(element13);
			
			eventLog.debug("Make Transport Job");
			
			//overwrite
			doc.getRootElement().addContent(eleBodyTemp);
			
			return doc;
		}
}