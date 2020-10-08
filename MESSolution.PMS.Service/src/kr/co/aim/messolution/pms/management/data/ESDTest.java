package kr.co.aim.messolution.pms.management.data;
import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "PMS", divider="_")
public class ESDTest extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="ESDTestID", type="Key", dataType="String", initial="", history="")
	private String ESDTestID;

	@CTORMTemplate(seq = "2", name="UserID", type="Column", dataType="String", initial="", history="")
	private String UserID;
	
	@CTORMTemplate(seq = "3", name="LastEventName", type="Column", dataType="String", initial="", history="")
	private String LastEventName;
	
	@CTORMTemplate(seq = "4", name="LastEventTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp LastEventTime;
	
	@CTORMTemplate(seq = "5", name="LastEventTimeKey", type="Column", dataType="String", initial="", history="")
	private String LastEventTimeKey;

	@CTORMTemplate(seq = "6", name="ESDGrade", type="Column", dataType="String", initial="", history="")
	private String ESDGrade;
	
	@CTORMTemplate(seq = "7", name="ESDTestResult", type="Column", dataType="String", initial="", history="")
	private String ESDTestResult;
	
	@CTORMTemplate(seq = "8", name="TestArea", type="Column", dataType="String", initial="", history="")
	private String TestArea;
	
	public ESDTest()
	{
		
	}
	
	public ESDTest(String ESDTestID )
	{
		setESDTestID(ESDTestID);
	}
	
	public String getESDTestID() {
		return ESDTestID;
	}
	public void setESDTestID(String ESDTestID) {
		this.ESDTestID = ESDTestID;
	}
	
	public String getUserID() {
		return UserID;
	}
	public void setUserID(String UserID) {
		this.UserID = UserID;
	}
	
	public String getLastEventName() {
		return LastEventName;
	}
	public void setLastEventName(String LastEventName) {
		this.LastEventName = LastEventName;
	}
	
	public Timestamp getLastEventTime() {
		return LastEventTime;
	}
	public void setLastEventTime(Timestamp LastEventTime) {
		this.LastEventTime = LastEventTime;
	}
	
	public String getLastEventTimeKey() {
		return LastEventTimeKey;
	}
	public void setLastEventTimeKey(String LastEventTimeKey) {
		this.LastEventTimeKey = LastEventTimeKey;
	}
	
	public String getESDGrade() {
		return ESDGrade;
	}
	public void setESDGrade(String ESDGrade) {
		this.ESDGrade = ESDGrade;
	}
	
	public String getESDTestResult() {
		return ESDTestResult;
	}
	public void setESDTestResult(String ESDTestResult) {
		this.ESDTestResult = ESDTestResult;
	}
	
	public String getTestArea() {
		return TestArea;
	}
	public void setTestArea(String TestArea) {
		this.TestArea = TestArea;
	}
	
}