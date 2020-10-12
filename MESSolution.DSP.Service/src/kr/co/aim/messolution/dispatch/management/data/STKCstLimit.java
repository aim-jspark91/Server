package kr.co.aim.messolution.dispatch.management.data;
import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "CT", divider="_")
public class STKCstLimit extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="FactoryName", type="Key", dataType="String", initial="", history="")
	private String FactoryName;

	@CTORMTemplate(seq = "2", name="MachineName", type="Key", dataType="String", initial="", history="")
	private String MachineName;
	
	@CTORMTemplate(seq = "3", name="DestMachineName", type="Key", dataType="String", initial="", history="")
	private String DestMachineName;
	
	@CTORMTemplate(seq = "4", name="RemainCSTQuantity", type="Number", dataType="String", initial="", history="")
	private Number RemainCSTQuantity;
	
	@CTORMTemplate(seq = "5", name="CommandQuantity", type="Number", dataType="String", initial="", history="")
	private Number CommandQuantity;
	
	@CTORMTemplate(seq = "6", name="TransferFlag", type="Column", dataType="String", initial="", history="")
	private String TransferFlag;
	
	@CTORMTemplate(seq = "7", name="Priority", type="Number", dataType="String", initial="", history="")
	private Number Priority;
	
	

	public STKCstLimit()
	{
		
	}
	
	public STKCstLimit(String FactoryName, String MachineName, String DestMachineName)
	{
		setFactoryName(FactoryName);
		setMachineName(MachineName);
		setDestMachineName(DestMachineName);
	}
	
	public String getFactoryName() {
		return FactoryName;
	}

	public void setFactoryName(String FactoryName) {
		this.FactoryName = FactoryName;
	}

	public String getMachineName() {
		return MachineName;
	}

	public void setMachineName(String MachineName) {
		this.MachineName = MachineName;
	}

	public String getDestMachineName() {
		return DestMachineName;
	}

	public void setDestMachineName(String DestMachineName) {
		this.DestMachineName = DestMachineName;
	}
	
	public Number getRemainCSTQuantity() {
		return RemainCSTQuantity;
	}

	public void setRemainCSTQuantity(Number RemainCSTQuantity) {
		this.RemainCSTQuantity = RemainCSTQuantity;
	}

	public String getTransferFlag() {
		return TransferFlag;
	}

	public void setTransferFlag(String TransferFlag) {
		this.TransferFlag = TransferFlag;
	}

	public Number getCommandQuantity() {
		return CommandQuantity;
	}

	public void setCommandQuantity(Number CommandQuantity) {
		this.CommandQuantity = CommandQuantity;
	}
 
	public Number getPriority() {
		return Priority;
	}

	public void setPriority(Number Priority) {
		this.Priority = Priority;
	}
	
}
