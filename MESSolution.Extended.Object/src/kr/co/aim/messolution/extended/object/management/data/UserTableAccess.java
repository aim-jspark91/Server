package kr.co.aim.messolution.extended.object.management.data;


import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class UserTableAccess extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="USERNAME", type="Key", dataType="String", initial="", history="")
	private String USERNAME;
	
	@CTORMTemplate(seq = "2", name="TABLENAME", type="Key", dataType="String", initial="", history="")
	private String TABLENAME;
	
	

	public UserTableAccess()
	{
		
	}



	public String getUSERNAME() {
		return USERNAME;
	}



	public void setUSERNAME(String uSERNAME) {
		USERNAME = uSERNAME;
	}



	public String getTABLENAME() {
		return TABLENAME;
	}



	public void setTABLENAME(String tABLENAME) {
		TABLENAME = tABLENAME;
	}
	
	
	
}
