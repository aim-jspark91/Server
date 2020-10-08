package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

/* hhlee, 2018.03.31, Add*/
public class MachineGroupMachine extends UdfAccessor{

	//MACHINEGROUPNAME	VARCHAR2(40)	Not Null
	//MACHINENAME	VARCHAR2(40)	Not Null
	
	@CTORMTemplate(seq = "1", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "2", name="machineGroupName", type="Column", dataType="String", initial="", history="")
	private String machineGroupName;
		
	public MachineGroupMachine()
	{		
	}
	
	public MachineGroupMachine(String machineName, String machineGroupName)
	{
		this.machineName = machineName;
		this.machineGroupName = machineGroupName;
	}

	public String getMachineGroupName() {
		return machineGroupName;
	}

	public void setMachineGroupName(String machineGroupName) {
		this.machineGroupName = machineGroupName;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}
}
