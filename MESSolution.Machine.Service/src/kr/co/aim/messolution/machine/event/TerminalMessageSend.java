package kr.co.aim.messolution.machine.event;

import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class TerminalMessageSend extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TMessageSend", getEventUser(), getEventComment(), "", "");
		Element eleBody = SMessageUtil.getBodyElement(doc);

		if(eleBody != null)
		{
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
			String subunitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);

			//get machineData
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			Machine unitData = null;
			Machine subunitData = null;
			String MachineName = StringUtil.EMPTY;

			/* Send TerminalMessage messages in SubUnit */
			String targetSubjectName = StringUtil.EMPTY;
			SMessageUtil.setHeaderItemValue(doc, "EVENTUSER", machineData.getKey().getMachineName());
            targetSubjectName = CommonUtil.getValue(machineData.getUdfs(), "MCSUBJECTNAME");
            
//			if(StringUtil.isNotEmpty(subunitName))
//			{
//				MachineName = subunitName;
//				subunitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(subunitName);
//				SMessageUtil.setHeaderItemValue(doc, "EVENTUSER", subunitData.getKey().getMachineName());
//				targetSubjectName = CommonUtil.getValue(subunitData.getUdfs(), "MCSUBJECTNAME");
//			}
//			/* Send TerminalMessage messages in Unit */
//			else if(StringUtil.isNotEmpty(unitName))
//			{
//				MachineName = unitName;
//				unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
//				SMessageUtil.setHeaderItemValue(doc, "EVENTUSER", unitData.getKey().getMachineName());
//				targetSubjectName = CommonUtil.getValue(unitData.getUdfs(), "MCSUBJECTNAME");
//			}
//			/* Send TerminalMessage messages in Machine */
//			else
//			{
//				MachineName = machineName;
//				SMessageUtil.setHeaderItemValue(doc, "EVENTUSER", machineData.getKey().getMachineName());
//				targetSubjectName = CommonUtil.getValue(machineData.getUdfs(), "MCSUBJECTNAME");
//			}

			SMessageUtil.setHeaderItemValue(doc, "EVENTCOMMENT", "TerminalMessageSend");

			 if(StringUtils.isEmpty(targetSubjectName))
			    	throw new CustomException("LOT-9006", MachineName);

			GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "EISSender");

		}

		//success then report to FMC
		try
		{
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");
		}
		catch(Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}
	}

	private Element generateBodyTemplate(Document doc, EventInfo eventInfo, String machineName, String unitName, List<Element>mList) throws CustomException
	{
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);

		Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);

		Element element1 = new Element ("MACHINENAME");
		element1.setText(machineName);
		eleBodyTemp.addContent(element1);

		Element element2 = new Element("UNITNAME");
		element2.setText(unitName);
		eleBodyTemp.addContent(element2);

		Element element3 = new Element("USERNAME");
		element3.setText(eventInfo.getEventUser());
		eleBodyTemp.addContent(element3);

		Element element4 = new Element("TERMINALMESSAGELIST");
		{
			for(Element e : mList)
			{
				Element element5 = new Element("TERMINALMESSAGE");
				element5.setText(SMessageUtil.getChildText(e, "TERMINALMESSAGE", true));
				element4.addContent(element5);
			}
		}

		eleBodyTemp.addContent(element4);

		doc.getRootElement().addContent(eleBodyTemp);

		return eleBodyTemp;
	}

	public List<ListOrderedMap>getPBTypeByTPEFOMPolicy()
	{
		String strSql = "SELECT DISTINCT PROBECARDTYPE " +
						"  FROM TPEFOMPOLICY " +
						" WHERE PROBECARDTYPE IS NOT NULL " ;

		Object [] bindSet = new Object[]{};

		List<ListOrderedMap> PBTypeList = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindSet);

		int count = PBTypeList.size();

		return PBTypeList ;
	}
}
