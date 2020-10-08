package kr.co.aim.messolution.fgms.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class Customer extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="customerNo", type="Key", dataType="String", initial="", history="")
	private String customerNo;

	@CTORMTemplate(seq = "2", name="customerName", type="Column", dataType="String", initial="", history="")
	private String customerName;
	
	@CTORMTemplate(seq = "3", name="destination", type="Column", dataType="String", initial="", history="")
	private String destination;
	
	@CTORMTemplate(seq = "4", name="telePhone", type="Column", dataType="String", initial="", history="")
	private String telePhone;
	
	@CTORMTemplate(seq = "5", name="fax", type="Column", dataType="String", initial="", history="")
	private String fax;
	
	@CTORMTemplate(seq = "6", name="address1", type="Column", dataType="String", initial="", history="")
	private String address1;
	
	@CTORMTemplate(seq = "7", name="address2", type="Column", dataType="String", initial="", history="")
	private String address2;
	
	@CTORMTemplate(seq = "8", name="address3", type="Column", dataType="String", initial="", history="")
	private String address3;
	
	@CTORMTemplate(seq = "9", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "10", name="lastEventUser", type="Column", dataType="String", initial="", history="")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "11", name="mail", type="Column", dataType="String", initial="", history="")
	private String mail;
	
	//instantiation
	public Customer()
	{
		
	}
	
	public Customer(String customerNo)
	{
		setCustomerNo(customerNo);
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getCustomerNo() {
		return customerNo;
	}

	public void setCustomerNo(String customerNo) {
		this.customerNo = customerNo;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getTelePhone() {
		return telePhone;
	}

	public void setTelePhone(String telePhone) {
		this.telePhone = telePhone;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getAddress1() {
		return address1;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public String getAddress3() {
		return address3;
	}

	public void setAddress3(String address3) {
		this.address3 = address3;
	}

	public Timestamp getLastEventTime() {
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}

	public String getLastEventUser() {
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}
}
