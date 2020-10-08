package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MQCProductRelation extends UdfAccessor{
	@CTORMTemplate(seq = "1", name="FROMPRODUCTSPEC", type="Key", dataType="String", initial="", history="")
	private String FROMPRODUCTSPEC;
	
	@CTORMTemplate(seq = "2", name="TOPRODUCTSPEC", type="Key", dataType="String", initial="", history="")
	private String TOPRODUCTSPEC;
	
	@CTORMTemplate(seq = "3", name="FACTORYNAME", type="Column", dataType="String", initial="", history="")
	private String FACTORYNAME;
	
	@CTORMTemplate(seq = "4", name="CREATETIME", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp CREATETIME;
	
	@CTORMTemplate(seq = "5", name="LASTEVENTNAME", type="Column", dataType="String", initial="", history="N")
	private String LASTEVENTNAME;
	
	@CTORMTemplate(seq = "6", name="LASTEVENTTIMEKEY", type="Column", dataType="String", initial="", history="N")
	private String LASTEVENTTIMEKEY;
	
	@CTORMTemplate(seq = "7", name="LASTEVENTTIME", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp LASTEVENTTIME;
	
	@CTORMTemplate(seq = "8", name="LASTEVENTUSER", type="Column", dataType="String", initial="", history="N")
	private String LASTEVENTUSER;
	
	@CTORMTemplate(seq = "9", name="LASTEVENTCOMMENT", type="Column", dataType="String", initial="", history="N")
	private String LASTEVENTCOMMENT;
	
	//instantiation
	public MQCProductRelation()
	{
		
	}
	
	public MQCProductRelation(String FROMPRODUCTSPEC, String TOPRODUCTSPEC)
	{
		setFromProductSpec(FROMPRODUCTSPEC);
		setToProductSpec(TOPRODUCTSPEC);
	}
	
	public String getFromProductSpec() {
		return FROMPRODUCTSPEC;
	}

	public void setFromProductSpec(String FROMPRODUCTSPEC) {
		this.FROMPRODUCTSPEC = FROMPRODUCTSPEC;
	}
	
	public String getToProductSpec() {
		return TOPRODUCTSPEC;
	}

	public void setToProductSpec(String TOPRODUCTSPEC) {
		this.TOPRODUCTSPEC = TOPRODUCTSPEC;
	}
	
	public String getFACTORYNAME() {
		return FACTORYNAME;
	}

	public void setFACTORYNAME(String FACTORYNAME) {
		this.FACTORYNAME = FACTORYNAME;
	}
	
	public String getLASTEVENTNAME() {
		return LASTEVENTNAME;
	}

	public void setLASTEVENTNAME(String LASTEVENTNAME) {
		this.LASTEVENTNAME = LASTEVENTNAME;
	}
	
	public String getLASTEVENTTIMEKEY() {
		return LASTEVENTTIMEKEY;
	}

	public void setLASTEVENTTIMEKEY(String LASTEVENTTIMEKEY) {
		this.LASTEVENTTIMEKEY = LASTEVENTTIMEKEY;
	}
	
	public String getLASTEVENTUSER() {
		return LASTEVENTUSER;
	}

	public void setLASTEVENTUSER(String LASTEVENTUSER) {
		this.LASTEVENTUSER = LASTEVENTUSER;
	}
	
	public Timestamp getCREATETIME() {
		return CREATETIME;
	}

	public void setCREATETIME(Timestamp CREATETIME) {
		this.CREATETIME = CREATETIME;
	}
	
	public Timestamp LASTEVENTTIME() {
		return LASTEVENTTIME;
	}

	public void setLASTEVENTTIME(Timestamp LASTEVENTTIME) {
		this.LASTEVENTTIME = LASTEVENTTIME;
	}
}
