package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class DailyCheck extends UdfAccessor{
	@CTORMTemplate(seq = "1", name="machinename", type="Key", dataType="String", initial="", history="")
	private String machinename;
	
	@CTORMTemplate(seq = "2", name="time", type="Column", dataType="Number", initial="", history="")
	private long time;

	@CTORMTemplate(seq = "3", name="areaName", type="Column", dataType="String", initial="", history="")
	private String areaName;
	
	@CTORMTemplate(seq = "4", name="supermachinename", type="Column", dataType="String", initial="", history="")
	private String supermachinename;
	
	@CTORMTemplate(seq = "5", name="detailmachinetype", type="Column", dataType="String", initial="", history="")
	private String detailmachinetype;
	
	@CTORMTemplate(seq = "6", name="factoryname", type="Column", dataType="String", initial="", history="")
	private String factoryname;
	
	@CTORMTemplate(seq = "7", name="createuser", type="Column", dataType="String", initial="", history="")
	private String createuser;
	
	@CTORMTemplate(seq = "8", name="createtime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp createtime;
	
	@CTORMTemplate(seq = "9", name="lasteventname", type="Column", dataType="String", initial="", history="N")
	private String lasteventname;
	
	@CTORMTemplate(seq = "10", name="lasteventtimekey", type="Column", dataType="String", initial="", history="N")
	private String lasteventtimekey;
	
	@CTORMTemplate(seq = "11", name="lasteventtime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lasteventtime;
	
	@CTORMTemplate(seq = "12", name="lasteventuser", type="Column", dataType="String", initial="", history="N")
	private String lasteventuser;
	
	@CTORMTemplate(seq = "13", name="lasteventcomment", type="Column", dataType="String", initial="", history="N")
	private String lasteventcomment;
	
	public DailyCheck()
	{
		
	}
	
	public DailyCheck(String machineName)
	{
		this.setmachinename(machineName);
	}
	
	public String getmachinename() {
		return machinename;
	}

	public void setmachinename(String machinename) {
		this.machinename = machinename;
	}
	
	public String getsupermachinename() {
		return supermachinename;
	}

	public void setsupermachinename(String supermachinename) {
		this.supermachinename = supermachinename;
	}
	
	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public long gettime() {
		return time;
	}

	public void settime(long time) {
		this.time = time;
	}
	
	public String getFactoryName() {
		return factoryname;
	}

	public void setFactoryName(String factoryname) {
		this.factoryname = factoryname;
	}
	
	public String getcreateuser() {
		return createuser;
	}

	public void setcreateuser(String createuser) {
		this.createuser = createuser;
	}
	
	public Timestamp getcreatetime() {
		return createtime;
	}

	public void setcreatetime(Timestamp createtime) {
		this.createtime = createtime;
	}
	
	public String getlasteventname() {
		return lasteventname;
	}

	public void setlasteventname(String lasteventname) {
		this.lasteventname = lasteventname;
	}
	
	public String getlasteventtimekey() {
		return lasteventtimekey;
	}

	public void setlasteventtimekey(String lasteventtimekey) {
		this.lasteventtimekey = lasteventtimekey;
	}
	
	public Timestamp getlasteventtime() {
		return lasteventtime;
	}

	public void setlasteventtime(Timestamp lasteventtime){
		this.lasteventtime = lasteventtime;
	}
	
	public String getlasteventuser() {
		return lasteventuser;
	}

	public void setlasteventuser(String lasteventuser) {
		this.lasteventuser = lasteventuser;
	}
	
	public String getlasteventcomment() {
		return lasteventcomment;
	}

	public void setlasteventcomment(String lasteventcomment) {
		this.lasteventcomment = lasteventcomment;
	}
	
	public String getdetailmachinetype() {
		return detailmachinetype;
	}

	public void setdetailmachinetype(String detailmachinetype) {
		this.detailmachinetype = detailmachinetype;
	}
	
}
