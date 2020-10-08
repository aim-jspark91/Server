package kr.co.aim.messolution.extended.object.management.data;


import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class UserGroupTableAccess extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="USERGROUPNAME", type="Key", dataType="String", initial="", history="")
	private String USERGROUPNAME;
	
	@CTORMTemplate(seq = "2", name="TABLENAME", type="Key", dataType="String", initial="", history="")
	private String TABLENAME;
	


	public UserGroupTableAccess()
	{
		
	}



	public String getUSERGROUPNAME() {
		return USERGROUPNAME;
	}



	public void setUSERGROUPNAME(String uSERGROUPNAME) {
		USERGROUPNAME = uSERGROUPNAME;
	}



	public String getTABLENAME() {
		return TABLENAME;
	}



	public void setTABLENAME(String tABLENAME) {
		TABLENAME = tABLENAME;
	}
	
	
}
