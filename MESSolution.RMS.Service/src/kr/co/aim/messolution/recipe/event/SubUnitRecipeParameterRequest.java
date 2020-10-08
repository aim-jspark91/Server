package kr.co.aim.messolution.recipe.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.jdom.Document;

public class SubUnitRecipeParameterRequest extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
				
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME" , true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME" , true);
		String subunitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME" , true);
		Machine machineData = null;
		
		/*if(StringUtil.isEmpty(subunitName))
		{
			//get line machine
			machineData	= MESMachineServiceProxy.getMachineInfoUtil().getMachineData(subunitName);
		}
		else if(StringUtil.isEmpty(unitName))
		{
			//get line machine
			machineData	= MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
		}
		else
		{
			//get line machine
			machineData	= MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
		}*/
		
		machineData	= MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
				
		SMessageUtil.setBodyItemValue(doc, "MACHINENAME", machineName, true);
		SMessageUtil.setBodyItemValue(doc, "UNITNAME", unitName, true);
		SMessageUtil.setBodyItemValue(doc, "SUBUNITNAME", subunitName, true);
								
		String targetSubjectName = CommonUtil.getValue(machineData.getUdfs(), "MCSUBJECTNAME");
				
		GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "EISSender");
	}
}