package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

/**
 * @since 2018.08.11
 * @author smkang
 * @see For management of equipment idle time.
 */
public class ProductSpecIdleTime extends UdfAccessor{
	@CTORMTemplate(seq = "1", name="productspecname", type="Key", dataType="String", initial="", history="")
	private String productspecname;
	
	@CTORMTemplate(seq = "2", name="idletime", type="Column", dataType="String", initial="", history="")
	private String idletime;
	
	@CTORMTemplate(seq = "3", name="lastruntime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp lastruntime;
	
	@CTORMTemplate(seq = "4", name="validflag", type="Column", dataType="String", initial="", history="")
	private String validflag;
	
	@CTORMTemplate(seq = "5", name="lasteventuser", type="Column", dataType="String", initial="", history="N")
	private String lasteventuser;
	
	@CTORMTemplate(seq = "6", name="lasteventtime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lasteventtime;
	
	@CTORMTemplate(seq = "7", name="lasteventtimekey", type="Column", dataType="String", initial="", history="N")
	private String lasteventtimekey;
	
	@CTORMTemplate(seq = "8", name="lasteventname", type="Column", dataType="String", initial="", history="N")
	private String lasteventname;
	
	@CTORMTemplate(seq = "9", name="lasteventcomment", type="Column", dataType="String", initial="", history="N")
	private String lasteventcomment;
	
	public ProductSpecIdleTime() {
	}

	public String getProductspecname() {
		return productspecname;
	}

	public void setProductspecname(String productspecname) {
		this.productspecname = productspecname;
	}

	public String getIdletime() {
		return idletime;
	}

	public void setIdletime(String idletime) {
		this.idletime = idletime;
	}

	public Timestamp getLastruntime() {
		return lastruntime;
	}

	public void setLastruntime(Timestamp lastruntime) {
		this.lastruntime = lastruntime;
	}

	public String getValidflag() {
		return validflag;
	}

	public void setValidflag(String validflag) {
		this.validflag = validflag;
	}

	public String getLasteventuser() {
		return lasteventuser;
	}

	public void setLasteventuser(String lasteventuser) {
		this.lasteventuser = lasteventuser;
	}

	public Timestamp getLasteventtime() {
		return lasteventtime;
	}

	public void setLasteventtime(Timestamp lasteventtime) {
		this.lasteventtime = lasteventtime;
	}

	public String getLasteventtimekey() {
		return lasteventtimekey;
	}

	public void setLasteventtimekey(String lasteventtimekey) {
		this.lasteventtimekey = lasteventtimekey;
	}

	public String getLasteventname() {
		return lasteventname;
	}

	public void setLasteventname(String lasteventname) {
		this.lasteventname = lasteventname;
	}

	public String getLasteventcomment() {
		return lasteventcomment;
	}

	public void setLasteventcomment(String lasteventcomment) {
		this.lasteventcomment = lasteventcomment;
	}
	
	
	
	
	
}