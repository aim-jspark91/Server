package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;


public class OperationMode extends UdfAccessor{

	@CTORMTemplate(seq = "1", name="MACHINENAME", type="Key", dataType="String", initial="", history="")
	private String MACHINENAME;

	@CTORMTemplate(seq = "2", name="OperationMode", type="Column", dataType="String", initial="", history="")
	private String OperationMode;
	
	@CTORMTemplate(seq = "3", name="Description", type="Column", dataType="String", initial="", history="")
	private String Description;
	
	public String getMACHINENAME() {
		return MACHINENAME;
	}
	public void setMACHINENAME(String MACHINENAME) {
		this.MACHINENAME = MACHINENAME;
	}
	public String getOperationMode() {
		return OperationMode;
	}
	public void setOperationMode(String OperationMode) {
		this.OperationMode = OperationMode;
	}
	public String getDescription() {
		return Description;
	}
	public void setDescription(String Description) {
		this.Description = Description;
	}	
}

