package kr.co.aim.messolution.consumable.event;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.info.MakeAvailableInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.jdom.Document;

public class CrateProcessEnd extends AsyncHandler {

	@Override
	public void doWorks(Document doc)
		throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String crateName   = SMessageUtil.getBodyItemValue(doc, "CRATENAME", true);
		
		/* Use as supplementary information. */
		String sLotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);	
		String sProductSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);	
		String sWorkorderName = SMessageUtil.getBodyItemValue(doc, "WORKORDERNAME", true);	
		String sPlanQuantity = SMessageUtil.getBodyItemValue(doc, "PLANQUANTITY", true);	
		String sProductSizeName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSIZE", true);	
		String sMachineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", true);	
		
		/*hhlee, 20180404, Not used ==>> 
		 * The quantity Crate is subtracted from the quantity at which it handles 
		 * the message "LotProcessEnd/LotProcessAbort/GlassScrap".
		 * */
		//String remainGlassQuantity = SMessageUtil.getBodyItemValue(doc, "REMAINGLASSQUANTITY", false);
		//String inputGlassQuantity = SMessageUtil.getBodyItemValue(doc, "INPUTGLASSQUANTITY", false);
		/*<<== hhlee, 20180404, Not used */
		
		/*Machine Validation */
		Machine machineData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Adjust", getEventUser(), getEventComment(), null, null);
				
		//1. Get Plan Data / Change W/O State
		//ProductRequestPlan pPlanData = CommonUtil.getStartPlanByMachine(machineName);
		//this.ChangeWorkOrder(pPlanData);
		
		//2. Get Crate Data
		ConsumableKey cKey = new ConsumableKey(crateName);
		Consumable crateData = ConsumableServiceProxy.getConsumableService().selectByKey(cKey);
		
		//3. Crate makeNotAvailable
		crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(crateData.getKey().getConsumableName());
		if(crateData.getQuantity() <= 0 && StringUtil.equals(crateData.getConsumableState(), "Available"))
		{
			eventInfo.setEventName("ChangeState");
			MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();
			makeNotAvailableInfo.setUdfs(crateData.getUdfs());
			MESConsumableServiceProxy.getConsumableServiceImpl().makeNotAvailable(crateData, makeNotAvailableInfo, eventInfo);
		}
		else if(crateData.getQuantity() > 0 && !StringUtil.equals(crateData.getConsumableState(), "Available"))
		{
			eventInfo.setEventName("ChangeState");
			MakeAvailableInfo makeAvailableInfo = new MakeAvailableInfo();
			makeAvailableInfo.setUdfs(crateData.getUdfs());
			MESConsumableServiceProxy.getConsumableServiceImpl().makeAvailable(crateData, makeAvailableInfo, eventInfo);
		}		
		
		/*hhlee, 20180404, Not Used(Original) ==>> */
		//String crateName   = SMessageUtil.getBodyItemValue(doc, "CRATENAME", true);
		//String remainGlassQuantity = SMessageUtil.getBodyItemValue(doc, "REMAINGLASSQUANTITY", false);
		//String inputGlassQuantity = SMessageUtil.getBodyItemValue(doc, "INPUTGLASSQUANTITY", false);
		
		//EventInfo eventInfo = EventInfoUtil.makeEventInfo("Adjust", getEventUser(), getEventComment(), null, null);
		/*<<== hhlee, 20180404, Not Used */
		
	}
}