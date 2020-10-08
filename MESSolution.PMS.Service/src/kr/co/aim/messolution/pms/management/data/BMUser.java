package kr.co.aim.messolution.pms.management.data;
import java.util.Iterator;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "PMS", divider="_")
public class BMUser extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="bmUser", type="Key", dataType="String", initial="", history="")
	private String bmUser;

	@CTORMTemplate(seq = "2", name="bmID", type="Key", dataType="String", initial="", history="")
	private String bmID;
	
	@CTORMTemplate(seq = "3", name="userType", type="Key", dataType="String", initial="", history="")
	private String userType;
	
	@CTORMTemplate(seq = "4", name="timeKey", type="Key", dataType="String", initial="", history="")
	private String timeKey;
			
	public BMUser()
	{
		
	}
	
	public BMUser(String bmUser,String bmID,String userType )
	{
		setBmUser(bmUser);
		setBmID(bmID);
		setUserType(userType);
	}

	public String getBmUser() {
		return bmUser;
	}

	public void setBmUser(String bmUser) {
		this.bmUser = bmUser;
	}

	public String getBmID() {
		return bmID;
	}

	public void setBmID(String bmID) {
		this.bmID = bmID;
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

	public Iterator iterator() {
		return null;
	}
}
