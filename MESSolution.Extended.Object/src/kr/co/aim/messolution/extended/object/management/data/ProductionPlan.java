package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ProductionPlan  extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="No", type="Key", dataType="String", initial="", history="")
	private String no;
	
	@CTORMTemplate(seq = "2", name="factoryName", type="Column", dataType="String", initial="", history="")
	private String factoryName;
	
	@CTORMTemplate(seq = "3", name="Workstage", type="Column", dataType="String", initial="", history="")
	private String Workstage;
	
	@CTORMTemplate(seq = "4", name="OrderDate", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp OrderDate;
	
	@CTORMTemplate(seq = "5", name="OrderNo", type="Column", dataType="String", initial="", history="")
	private String OrderNo;
	
	@CTORMTemplate(seq = "6", name="WorkNo", type="Column", dataType="String", initial="", history="")
	private String WorkNo;
	
	@CTORMTemplate(seq = "7", name="ProductionSpec", type="Column", dataType="String", initial="", history="")
	private String ProductionSpec;
	
	@CTORMTemplate(seq = "8", name="Construction", type="Column", dataType="String", initial="", history="")
	private String Construction;
	
	@CTORMTemplate(seq = "9", name="Layer", type="Column", dataType="String", initial="", history="")
	private String Layer;
	
	@CTORMTemplate(seq = "10", name="LotType", type="Column", dataType="String", initial="", history="")
	private String LotType;
	
	@CTORMTemplate(seq = "11", name="Input", type="Column", dataType="String", initial="", history="")
	private String Input;
	
	@CTORMTemplate(seq = "12", name="Output", type="Column", dataType="String", initial="", history="")
	private String Output;
	
	@CTORMTemplate(seq = "13", name="Remark", type="Column", dataType="String", initial="", history="")
	private String Remark;
	
	@CTORMTemplate(seq = "14", name="CreateUser", type="Column", dataType="String", initial="", history="")
	private String createUser;
	
	@CTORMTemplate(seq = "15", name="CreateTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp createTime;
	
	@CTORMTemplate(seq = "16", name="CreateWorkstation", type="Column", dataType="String", initial="", history="")
	private String createWorkstation;
	
	@CTORMTemplate(seq = "17", name="CreatePassword", type="Column", dataType="String", initial="", history="")
	private String createPassword;
	
	public ProductionPlan()
	{
		
	}

	public ProductionPlan(String no)
	{
		setFactoryName(factoryName);
		setNo(no);
	}
	
	public String getProductionSpec() {
		return ProductionSpec;
	}

	public void setProductionSpec(String productionSpec) {
		ProductionSpec = productionSpec;
	}
	
	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	public String getNo() {
		return no;
	}

	public void setNo(String no) {
		this.no = no;
	}

	public String getWorkstage() {
		return Workstage;
	}

	public void setWorkstage(String workstage) {
		Workstage = workstage;
	}

	public Timestamp getOrderDate() {
		return OrderDate;
	}

	public void setOrderDate(Timestamp rderDate) {
		this.OrderDate = rderDate;
	}

	public String getOrderNo() {
		return OrderNo;
	}

	public void setOrderNo(String orderNo) {
		OrderNo = orderNo;
	}

	public String getWorkNo() {
		return WorkNo;
	}

	public void setWorkNo(String workNo) {
		WorkNo = workNo;
	}

	public String getConstruction() {
		return Construction;
	}

	public void setConstruction(String construction) {
		Construction = construction;
	}

	public String getLayer() {
		return Layer;
	}

	public void setLayer(String layer) {
		Layer = layer;
	}

	public String getLotType() {
		return LotType;
	}

	public void setLotType(String lotType) {
		LotType = lotType;
	}

	public String getInput() {
		return Input;
	}

	public void setInput(String input) {
		Input = input;
	}

	public String getOutput() {
		return Output;
	}

	public void setOutput(String output) {
		Output = output;
	}

	public String getRemark() {
		return Remark;
	}

	public void setRemark(String remark) {
		Remark = remark;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getCreateWorkstation() {
		return createWorkstation;
	}

	public void setCreateWorkstation(String createWorkstation) {
		this.createWorkstation = createWorkstation;
	}

	public String getCreatePassword() {
		return createPassword;
	}

	public void setCreatePassword(String createPassword) {
		this.createPassword = createPassword;
	}

	
}
