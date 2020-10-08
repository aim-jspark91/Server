package kr.co.aim.messolution.pms.management.data;
import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "PMS", divider="_")
public class ESDTestSet extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="UserID", type="Key", dataType="String", initial="", history="")
	private String UserID;

	@CTORMTemplate(seq = "2", name="AlarmLimit", type="Column", dataType="String", initial="", history="")
	private String AlarmLimit;
	
	@CTORMTemplate(seq = "3", name="AlarmActiveFlag", type="Column", dataType="String", initial="", history="")
	private String AlarmActiveFlag;
	
	@CTORMTemplate(seq = "4", name="ControlLevel", type="Column", dataType="String", initial="", history="")
	private String ControlLevel;
	
	public ESDTestSet()
	{
		
	}
	
	public ESDTestSet(String UserID )
	{
		setUserID(UserID);
	}
	
	public String getUserID() {
		return UserID;
	}
	public void setUserID(String UserID) {
		this.UserID = UserID;
	}
	
	public String getAlarmLimit() {
		return AlarmLimit;
	}
	public void setAlarmLimit(String AlarmLimit) {
		this.AlarmLimit = AlarmLimit;
	}
	
	public String getAlarmActiveFlag() {
		return AlarmActiveFlag;
	}
	public void setAlarmActiveFlag(String AlarmActiveFlag) {
		this.AlarmActiveFlag = AlarmActiveFlag;
	}
	
	public String getControlLevel() {
		return ControlLevel;
	}
	public void setControlLevel(String ControlLevel) {
		this.ControlLevel = ControlLevel;
	}
	
}