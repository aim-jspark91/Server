package kr.co.aim.messolution.lot.service;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MachineIdleTime;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SendServiceImpl implements ApplicationContextAware {

    /**
     * @uml.property name="applicationContext"
     * @uml.associationEnd
     */
    private ApplicationContext applicationContext;
    private static Log log = LogFactory.getLog(SendServiceImpl.class);

    /**
     * @param arg0
     * @throws BeansException
     * @uml.property name="applicationContext"
     */
    @Override
    public void setApplicationContext(ApplicationContext arg0)
            throws BeansException {
        applicationContext = arg0;
    }
    public void SendToFMCIsIdleTimeOver(String machineName){
    	try {
        	Document doc = this.CreateDocForFMCIsIdleTimeOver(machineName);
    		log.info(JdomUtils.toString(doc));
    		GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");
		} catch (Exception e) {
			log.warn("FMC Report Failed!");
		}

    }
	public void SendToFMCIsIdleTimeOver(Document doc)
	{
		try
		{
			Document copyDoc = this.CreateDocForFMCIsIdleTimeOver(doc);
			log.info(JdomUtils.toString(copyDoc));
			log.info(JdomUtils.toString(doc));
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), copyDoc, "FMCSender");
		}
		catch(Exception ex)
		{
			log.warn("FMC Report Failed!");
		}	
	}
	public void SendToFMCPortInInhibit(Document doc)
	{
		try
		{
			Document copyDoc = this.CreateDocForFMCPortInInhibit(doc);
			log.info(JdomUtils.toString(copyDoc));
			log.info(JdomUtils.toString(doc));
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), copyDoc, "FMCSender");	
		}
		catch(Exception ex)
		{
			log.warn("FMC Report Failed!");
		}	
	}
	private Document CreateDocForFMCIsIdleTimeOver(String machineName)throws CustomException{
		Document doc=null;
		Machine machineData  = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		String operationMode = CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE");
		try {
			// Body Tag �� ������ header Tag �� Return Tag ����
			//MODIFY BY JHIYING ON 20200225 BECAUSE FMC MACHINE IDLE TIME CAN'T SHOW MANTIS :5702
			//doc = SMessageUtil.CreateXmlDocumentForFMC("EQPIdleTimeChanged");
			doc = SMessageUtil.CreateXmlDocumentForFMC("A_EQPIdleTimeChanged");
		} catch (Exception e) {
			throw new CustomException();
		}
		// Body Tag ���� ��
		// MachineName�� isIdleTimeOver Setting
		Element elementBody = new Element(SMessageUtil.Body_Tag);
		
		Element element1 = new Element("MACHINENAME");
		element1.setText(machineName);
		elementBody.addContent(element1);
		
		// if machine.operationMode != INDP
		// �ش� isIdleTimeOver ���� �״�� ����
		
		// if machine.operationMode == INDP
		// machineIdleTime ���� UnitName�� * �� �ƴ� IdleTime ��ȸ
		// �ش� �� �� �ϳ��� Y���� �����Ѵٸ�, Y�� ����
		// �ش� ���� ��� ���� N�̶��, N���� ����
		if(StringUtils.equals(GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP, operationMode)){
			String condition = "MACHINENAME = ? AND UNITNAME != ?";
			Object[] bindSet = new Object[] {machineName, "*"};
			List<MachineIdleTime> machineIdleTimeList = ExtendedObjectProxy.getMachineIdleTimeService().select(condition, bindSet);
			boolean isExistY = false;
			for(MachineIdleTime machineIdleTime : machineIdleTimeList){
				if(StringUtils.equals("Y", machineIdleTime.getIsIdleTimeOver())){
					isExistY = true;
					break;
				}
			}
			if(isExistY){
				Element element2 = new Element("ISIDLETIMEOVER");
				element2.setText("Y");
				elementBody.addContent(element2);
			}else{
				Element element2 = new Element("ISIDLETIMEOVER");
				element2.setText("N");
				elementBody.addContent(element2);
			}

		}else{
			Object[] keySet = new Object[]{machineName, "*"};
			MachineIdleTime machineIdleTimeInfo = ExtendedObjectProxy.getMachineIdleTimeService().selectByKey(false, keySet);
			Element element2 = new Element("ISIDLETIMEOVER");
			element2.setText(machineIdleTimeInfo.getIsIdleTimeOver());
			elementBody.addContent(element2);
			
		}
		doc.getRootElement().addContent(elementBody);
		return doc;
	}
	private Document CreateDocForFMCIsIdleTimeOver(Document doc) throws CustomException{
		Document copyDoc =  (Document) doc.clone();
		
		String machineName = SMessageUtil.getBodyItemValue(copyDoc, "MACHINENAME", true);
		Machine machineData  = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		String operationMode = CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE");
		
		// ������ copyDoc�� Header Tag���� MessageName ����
		//MODIFY BY JHIYING ON20200225 MANTIS:5702
	//	SMessageUtil.setHeaderItemValue(copyDoc, "MESSAGENAME", "EQPIdleTimeChanged");
		SMessageUtil.setHeaderItemValue(copyDoc, "MESSAGENAME", "A_EQPIdleTimeChanged");
		// ������ copydoc�� Body Tag ����
		copyDoc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		// ������ copydoc�� Body Tag ���� �� ���Ӱ� body Tag ����
		Element elementBody = new Element(SMessageUtil.Body_Tag);
		
		Element element1 = new Element("MACHINENAME");
		element1.setText(machineName);
		elementBody.addContent(element1);

		if(StringUtils.equals(GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP, operationMode)){
			String condition = "MACHINENAME = ? AND UNITNAME != ?";
			Object[] bindSet = new Object[] {machineName, "*"};
			List<MachineIdleTime> machineIdleTimeList = ExtendedObjectProxy.getMachineIdleTimeService().select(condition, bindSet);
			boolean isExistY = false;
			for(MachineIdleTime machineIdleTime : machineIdleTimeList){
				if(StringUtils.equals("Y", machineIdleTime.getIsIdleTimeOver())){
					isExistY = true;
					break;
				}
			}
			if(isExistY){
				Element element2 = new Element("ISIDLETIMEOVER");
				element2.setText("Y");
				elementBody.addContent(element2);
			}else{
				Element element2 = new Element("ISIDLETIMEOVER");
				element2.setText("N");
				elementBody.addContent(element2);
			}

		}else{
			Object[] keySet = new Object[]{machineName, "*"};
			MachineIdleTime machineIdleTimeInfo = ExtendedObjectProxy.getMachineIdleTimeService().selectByKey(false, keySet);
			Element element2 = new Element("ISIDLETIMEOVER");
			element2.setText(machineIdleTimeInfo.getIsIdleTimeOver());
			elementBody.addContent(element2);
			
		}
		copyDoc.getRootElement().addContent(elementBody);
		
		return copyDoc;
	}
	private Document CreateDocForFMCPortInInhibit(Document doc) throws CustomException{
		Document copyDoc =  (Document) doc.clone();
		
		String machineName = SMessageUtil.getBodyItemValue(copyDoc, "MACHINENAME", true);
		List<Element> PortList = SMessageUtil.getBodySequenceItemList(doc, "PORTLIST", true);
		Port portData = null;

		//modify by jhiying on20200225 mantis :5702
	//	SMessageUtil.setHeaderItemValue(copyDoc, "MESSAGENAME", "PortInInhibitChanged");
		SMessageUtil.setHeaderItemValue(copyDoc, "MESSAGENAME", "A_PortInInhibitChanged");
		copyDoc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		Element elementBody = new Element(SMessageUtil.Body_Tag);
		
		Element element1 = new Element("MACHINENAME");
		element1.setText(machineName);
		elementBody.addContent(element1);
		
		for (Element elePort : PortList) 
		{
			String portName = SMessageUtil.getChildText(elePort, "PORTNAME", true);
			Element element2 = new Element("PORTNAME");
			element2.setText(portName);
			elementBody.addContent(element2);
			
			portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		}
		Element element3 = new Element("PORTININHIBITFLAG");
		element3.setText(portData.getUdfs().get("PORTININHIBITFLAG"));
		elementBody.addContent(element3);
		
		Element element4 = new Element("PORTACCESSMODE");
		element4.setText(portData.getAccessMode());
		elementBody.addContent(element4);
		
		copyDoc.getRootElement().addContent(elementBody);
		
		
		return copyDoc;
	}
    
}