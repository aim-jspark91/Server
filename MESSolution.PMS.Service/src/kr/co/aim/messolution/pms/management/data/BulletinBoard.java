package kr.co.aim.messolution.pms.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "PMS", divider="_")
public class BulletinBoard extends UdfAccessor {
	@CTORMTemplate(seq = "1", name="userGroup", type="Key", dataType="String", initial="", history="")
	private String userGroup;	
	
	@CTORMTemplate(seq = "2", name="No", type="Key", dataType="String", initial="", history="")
	private String No;
	
	@CTORMTemplate(seq = "3", name="department", type="Column", dataType="String", initial="", history="")
	private String department;
	
	@CTORMTemplate(seq = "4", name="title", type="Column", dataType="String", initial="", history="")
	private String title;

	@CTORMTemplate(seq = "5", name="comments", type="Column", dataType="String", initial="", history="")
	private String comments;
	
	@CTORMTemplate(seq = "6", name="createUser", type="Column", dataType="String", initial="", history="")
	private String createUser;
	
	@CTORMTemplate(seq = "7", name="createTime", type="Column", dataType="String", initial="", history="")
	private Timestamp createTime;
	
	@CTORMTemplate(seq = "8", name="priority", type="Column", dataType="String", initial="", history="")
	private String priority;
	
	public BulletinBoard ()
	{
		
	}
	
	public BulletinBoard(String userGroup , String no)
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
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

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}
}
