package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class BulletinBoard extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="factoryName", type="Key", dataType="String", initial="", history="")
	private String factoryName;
	
	@CTORMTemplate(seq = "2", name="no", type="Key", dataType="String", initial="", history="")
	private String no;
	
	@CTORMTemplate(seq = "3", name="title", type="Column", dataType="String", initial="", history="")
	private String title;
	
	@CTORMTemplate(seq = "4", name="comments", type="Column", dataType="String", initial="", history="")
	private String comments;
	
	@CTORMTemplate(seq = "5", name="priority", type="Column", dataType="String", initial="", history="")
	private String priority;
	
	@CTORMTemplate(seq = "6", name="createUser", type="Column", dataType="String", initial="", history="")
	private String createUser;
	
	@CTORMTemplate(seq = "7", name="createTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp createTime;
	
	@CTORMTemplate(seq = "8", name="createWorkstation", type="Column", dataType="String", initial="", history="")
	private String createWorkstation;
	
	@CTORMTemplate(seq = "9", name="createPassword", type="Column", dataType="String", initial="", history="")
	private String createPassword;
	
	
	public BulletinBoard()
	{
		
	}

	public BulletinBoard(String factoryName, String no)
	{
		setFactoryName(factoryName);
		setNo(no);
	}

	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	public String getNo() {
		return no;
	}

	public void setNo(String no) {
		this.no = no;
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

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
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

	public String getCreateWorkstation() {
		return createWorkstation;
	}

	public void setCreateWorkstation(String createWorkstation) {
		this.createWorkstation = createWorkstation;
	}

	public String getCreatePassword() {
		return createPassword;
	}

	public void setCreatePassword(String createPassword) {
		this.createPassword = createPassword;
	}

	
}
