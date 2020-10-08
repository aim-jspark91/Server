package kr.co.aim.messolution.machine.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MachineGroupMachine;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineGroup;

import org.jdom.Document;
import org.jdom.Element;

public class SubUnitGroupStateChanged extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("SubUnitGroupStateChanged", getEventUser(), getEventComment(), null, null);
		
		try
		{
		    String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		    String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		    
		    /* Machine Validation */
		    Machine machineData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		    Machine unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
		    Machine subunitData  = null;
		    
		    List<Element> subunitgrouplist = SMessageUtil.getBodySequenceItemList(doc, "SUBUNITGROUPLIST", true);		
			
			if (subunitgrouplist != null)
			{
				String subunitgroupName = StringUtil.EMPTY;
				String subunitname = StringUtil.EMPTY;
				
				MachineGroupMachine machinegroupmachine = null;
				MachineGroupMachine machinegroupmachinemodify = null;
				
				MachineGroup machinegroup = null;
				
				for(Element subunitgroupE : subunitgrouplist)
				{
					/* Unitname Validation */
					subunitgroupName = SMessageUtil.getChildText(subunitgroupE, "SUBUNITGROUPNAME", false);					
					//MachineGroup subunitgroupData =  MESMachineServiceProxy.getMachineInfoUtil(). .getMachineInfoUtil(). .getMachineData(unitName);
															
					List<Element> subunitList = SMessageUtil.getSubSequenceItemList(subunitgroupE, "SUBUNITLIST", true);
				
					if (subunitList != null)
					{
						for(Element subunitE : subunitList)
						{
							subunitname = SMessageUtil.getChildText(subunitE, "SUBUNITNAME", true);							
							
							/* SubUnit Validation */
							subunitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(subunitname);
							
							////get spec				
							//MachineGroupMachine machinegroupmachine = new MachineGroupMachine(subunitgroupName, subunitname);
							//machinegroupmachine.setMachineGroupName(subunitgroupName);
							//machinegroupmachine.setMachineName(subunitname);
							//machinegroupmachine = null;
							
							machinegroupmachine = new MachineGroupMachine(subunitname, subunitgroupName);
							machinegroupmachinemodify = null;
							try
							{	
								machinegroupmachinemodify = ExtendedObjectProxy.getMachineGroupMachineService().selectByKey(false, new Object[] {subunitname});
								machinegroupmachinemodify.setMachineGroupName(subunitgroupName);
								machinegroupmachinemodify.setMachineName(subunitname);
								
								ExtendedObjectProxy.getMachineGroupMachineService().modify(eventInfo, machinegroupmachinemodify);
								
							}
							catch(Exception ex)
							{
								ExtendedObjectProxy.getMachineGroupMachineService().create(eventInfo, machinegroupmachine);					
							}
					    }
			        }
				}
			}
		    
		}
		catch(Exception ex)
		{
			eventLog.warn(ex.getMessage());
		}		
	}
}