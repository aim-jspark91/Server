package kr.co.aim.messolution.pms.management.data;
import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "PMS", divider="_")
public class PMUser extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="pmUser", type="Key", dataType="String", initial="", history="")
	private String pmUser;

	@CTORMTemplate(seq = "2", name="pmID", type="Key", dataType="String", initial="", history="")
	private String pmID;
	
	@CTORMTemplate(seq = "3", name="userType", type="Key", dataType="String", initial="", history="")
	private String userType;
	
	@CTORMTemplate(seq = "4", name="timeKey", type="Key", dataType="String", initial="", history="")
	private String timeKey;
			
	public PMUser()
	{
		
	}
	
	public PMUser(String pmUser,String pmID,String userType )
	{
		setPmUser(pmUser);
		setPmID(pmID);
		setUserType(userType);
	}

	public String getPmUser() {
		return pmUser;
	}

	public void setPmUser(String pmUser) {
		this.pmUser = pmUser;
	}

	public String getPmID() {
		return pmID;
	}

	public void setPmID(String pmID) {
		this.pmID = pmID;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getTimeKey() {
		return timeKey;
	}

	public void setTimeKey(String timeKey) {
		this.timeKey = timeKey;
	}

}
