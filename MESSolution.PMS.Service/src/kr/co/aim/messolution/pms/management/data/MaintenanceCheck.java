package kr.co.aim.messolution.pms.management.data;
import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "PMS", divider="_")
public class MaintenanceCheck extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="maintenanceID", type="Key", dataType="String", initial="", history="")
	private String maintenanceID;

	@CTORMTemplate(seq = "2", name="checkID", type="Key", dataType="String", initial="", history="")
	private String checkID;
	
	@CTORMTemplate(seq = "3", name="checkNo", type="Key", dataType="String", initial="", history="")
	private String checkNo;
	
	@CTORMTemplate(seq = "4", name="itemType", type="Column", dataType="String", initial="", history="")
	private String itemType;
	
	@CTORMTemplate(seq = "5", name="itemName", type="Column", dataType="String", initial="", history="")
	private String itemName;
	
	@CTORMTemplate(seq = "6", name="checkDesc", type="Column", dataType="String", initial="", history="")
	private String checkDesc;

	@CTORMTemplate(seq = "7", name="partID", type="Column", dataType="String", initial="", history="")
	private String partID;
	
	@CTORMTemplate(seq = "8", name="partName", type="Column", dataType="String", initial="", history="")
	private String partName;
	
	@CTORMTemplate(seq = "9", name="useQuantity", type="Column", dataType="String", initial="", history="")
	private String useQuantity;
	
	@CTORMTemplate(seq = "10", name="checkResult", type="Column", dataType="String", initial="", history="")
	private String checkResult;
	
	public MaintenanceCheck()
	{
		
	}
	
	public MaintenanceCheck(String maintenanceID, String checkID , String checkNo)
	{
		setMaintenanceID(maintenanceID);
		setCheckID(checkID);
		setCheckNo(checkNo);
	}

	public String getMaintenanceID() {
		return maintenanceID;
	}

	public void setMaintenanceID(String maintenanceID) {
		this.maintenanceID = maintenanceID;
	}

	public String getCheckID() {
		return checkID;
	}

	public void setCheckID(String checkID) {
		this.checkID = checkID;
	}

	public String getCheckNo() {
		return checkNo;
	}

	public void setCheckNo(String checkNo) {
		this.checkNo = checkNo;
	}

	public String getItemType() {
		return itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getCheckDesc() {
		return checkDesc;
	}

	public void setCheckDesc(String checkDesc) {
		this.checkDesc = checkDesc;
	}

	public String getPartID() {
		return partID;
	}

	public void setPartID(String partID) {
		this.partID = partID;
	}

	public String getPartName() {
		return partName;
	}

	public void setPartName(String partName) {
		this.partName = partName;
	}

	public String getUseQuantity() {
		return useQuantity;
	}

	public void setUseQuantity(String useQuantity) {
		this.useQuantity = useQuantity;
	}

	public String getCheckResult() {
		return checkResult;
	}

	public void setCheckResult(String checkResult) {
		this.checkResult = checkResult;
	}

}
