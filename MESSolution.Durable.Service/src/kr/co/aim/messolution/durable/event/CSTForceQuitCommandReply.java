/**
 * 
 */
package kr.co.aim.messolution.durable.event;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

import org.jdom.Document;

/**
 * @author Administrator
 *
 */
public class CSTForceQuitCommandReply extends AsyncHandler{
	@Override
	public void doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnitCommunicationStateReport", getEventUser(), getEventComment(), "", "");
		
		/* Validate Machine */
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		Machine mainMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		/* Validate Unit */
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		if(StringUtil.isNotEmpty(unitName))
		{
			Machine unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
		}
		/* Validate Cassette */
		String cassetteName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		if(StringUtil.isNotEmpty(cassetteName))
		{
		    Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(cassetteName);
	    }
		
		/* Validate Port */
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		if(StringUtil.isNotEmpty(portName))
		{
		    Port portData =  MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		}
		
		
		/* Result */
		String result = SMessageUtil.getBodyItemValue(doc, "RESULT", true);
		if(!StringUtil.equals(result, "OK"))
		{
			eventLog.warn("CSTForceQuitCommand Failed!");
		}
		
		
		String originalSourceSubjectName = getOriginalSourceSubjectName();
		if ( StringUtil.indexOf(StringUtil.upperCase(originalSourceSubjectName), "_INBOX") > -1 )
		{
			GenericServiceProxy.getESBServive().sendReplyBySender(originalSourceSubjectName, doc, "OICSender");
		}
	}
}
