package kr.co.aim.messolution.dispatch.management.data;
import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "CT", divider="_")
public class STKOperationPriority extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="MachineName", type="Key", dataType="String", initial="", history="")
	private String MachineName;
	
	@CTORMTemplate(seq = "2", name="RequestName", type="Key", dataType="String", initial="", history="")
	private String RequestName;
	
	@CTORMTemplate(seq = "3", name="ProcessOperationName", type="Key", dataType="String", initial="", history="")
	private String ProcessOperationName;
	
	@CTORMTemplate(seq = "4", name="StockerName", type="Key", dataType="String", initial="", history="")
	private String StockerName;
	
	@CTORMTemplate(seq = "5", name="Priority", type="Column", dataType="Number", initial="", history="")
	private Number Priority;

 
	
	public STKOperationPriority()
	{
		
	}
	
	public STKOperationPriority(String MachineName )
	{
		setMachineName(MachineName);
	}


	public String getMachineName() {
		return MachineName;
	}

	public String getRequestName() {
		return RequestName;
	}

	public void setRequestName(String requestName) {
		RequestName = requestName;
	}

	public String getProcessOperationName() {
		return ProcessOperationName;
	}

	public void setProcessOperationName(String ProcessOperationName) {
		this.ProcessOperationName = ProcessOperationName;
	}
	public void setMachineName(String MachineName) {
		this.MachineName = MachineName;
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
