package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class HelpDesk extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="department", type="Key", dataType="String", initial="", history="")
	private String department;
	
	@CTORMTemplate(seq = "2", name="workDate", type="Key", dataType="String", initial="", history="")
	private String workDate;
	
	@CTORMTemplate(seq = "3", name="dayNight", type="Key", dataType="Number", initial="", history="")
	private String dayNight;
	
	@CTORMTemplate(seq = "4", name="workUser", type="Key", dataType="String", initial="", history="")
	private String workUser;
	
	@CTORMTemplate(seq = "5", name="telePhone", type="Column", dataType="String", initial="", history="")
	private String telePhone;
	
	@CTORMTemplate(seq = "6", name="email", type="Column", dataType="String", initial="", history="")
	private String email;
	
	@CTORMTemplate(seq = "7", name="cellphone", type="Column", dataType="String", initial="", history="")
	private String cellphone;
	
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	
	public String getWorkDate() {
		return workDate;
	}
	public void setWorkDate(String workDate) {
		this.workDate = workDate;
	}
	
	public String getDayNight() {
		return dayNight;
	}
	public void setDayNight(String dayNight) {
		this.dayNight = dayNight;
	}
	
	public void setWorkUser(String workUser)
	{
		this.workUser = workUser;
	}
	public String getWorkUser()
	{
		return workUser;
	}
	
	public String getTelePhone() {
		return telePhone;
	}
	public void setTelePhone(String telePhone) {
		this.telePhone = telePhone;
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getCellphone() {
		return cellphone;
	}
	public void setCellphone(String cellphone) {
		this.cellphone = cellphone;
	}
	
	//instantiation
	public HelpDesk()
	{
		
	}
	
	public HelpDesk(String department, String workDate, String dayNight, String workUser)
	{
		setDepartment(department);
		setWorkDate(workDate);
		setDayNight(dayNight);
		setWorkUser(workUser);
	}
}
