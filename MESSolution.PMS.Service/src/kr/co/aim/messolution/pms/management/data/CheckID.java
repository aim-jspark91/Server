package kr.co.aim.messolution.pms.management.data;
import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "PMS", divider="_")
public class CheckID extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="checkID", type="Key", dataType="String", initial="", history="")
	private String checkID;

	@CTORMTemplate(seq = "2", name="machineGroupName", type="Column", dataType="String", initial="", history="")
	private String machineGroupName;
	
	@CTORMTemplate(seq = "3", name="createUser", type="Column", dataType="String", initial="", history="")
	private String createUser;
		
	@CTORMTemplate(seq = "4", name="createTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp createTime;
	
	@CTORMTemplate(seq = "5", name="departMent", type="Column", dataType="String", initial="", history="")
	private String departMent;
	
	public CheckID()
	{
		
	}
	
	public CheckID(String checkID)
	{
		setCheckID(checkID);
	}

	public String getCheckID() {
		return checkID;
	}

	public void setCheckID(String checkID) {
		this.checkID = checkID;
	}

	public String getMachineGroupName() {
		return machineGroupName;
	}

	public void setMachineGroupName(String machineGroupName) {
		this.machineGroupName = machineGroupName;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	
	public String getDepartMent() {
		return departMent;
	}
	public void setDepartMent(String departMent) {
		this.departMent = departMent;
	}
	
}
