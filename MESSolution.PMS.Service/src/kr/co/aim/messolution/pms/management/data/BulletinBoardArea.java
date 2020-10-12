package kr.co.aim.messolution.pms.management.data;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "PMS", divider="_")
public class BulletinBoardArea extends UdfAccessor {
	@CTORMTemplate(seq = "1", name="userGroup", type="Key", dataType="String", initial="", history="")
	private String userGroup;	
	
	@CTORMTemplate(seq = "2", name="No", type="Key", dataType="String", initial="", history="")
	private String No;
	
	@CTORMTemplate(seq = "3", name="department", type="Column", dataType="String", initial="", history="")
	private String department;
	
	public BulletinBoardArea ()
	{
		
	}
	
	public BulletinBoardArea(String userGroup , String no)
	{   
		setUserGroup(userGroup);		
		setNo(no);
	}
	
	public String getUserGroup() {
		return userGroup;
	}

	public void setUserGroup(String userGroup) {
		this.userGroup = userGroup;
	}
	
	public String getNo() {
		return No;
	}

	public void setNo(String no) {
		No = no;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}
}
