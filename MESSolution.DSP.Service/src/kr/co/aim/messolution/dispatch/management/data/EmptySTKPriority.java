package kr.co.aim.messolution.dispatch.management.data;
import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "CT", divider="_")
public class EmptySTKPriority extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="MachineName", type="Key", dataType="String", initial="", history="")
	private String MachineName;
	
	@CTORMTemplate(seq = "2", name="RequestName", type="Key", dataType="String", initial="", history="")
	private String RequestName;
	
	@CTORMTemplate(seq = "3", name="StockerName", type="Key", dataType="String", initial="", history="")
	private String StockerName;
	
	@CTORMTemplate(seq = "4", name="Priority", type="Column", dataType="Number", initial="", history="")
	private Number Priority;

 
	
	public EmptySTKPriority()
	{
		
	}
	
	public EmptySTKPriority(String MachineName )
	{
		setMachineName(MachineName);
	}


	public String getMachineName() {
		return MachineName;
	}

	public void setMachineName(String ProductSpecName) {
		this.MachineName = ProductSpecName;
	}
	
	public String getRequestName() {
		return RequestName;
	}

	public void setRequestName(String requestName) {
		RequestName = requestName;
	}

	public String getStockerName() {
		return StockerName;
	}

	public void setStockerName(String StockerName) {
		this.StockerName = StockerName;
	}

 	public Number getPriority() {
		return Priority;
	}

	public void setPriority(Number Priority) {
		this.Priority = Priority;
	}
 
}
