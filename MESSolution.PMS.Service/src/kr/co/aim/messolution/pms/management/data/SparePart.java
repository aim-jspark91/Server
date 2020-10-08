package kr.co.aim.messolution.pms.management.data;
import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "PMS", divider="_")
public class SparePart extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="partID", type="Key", dataType="String", initial="", history="")
	private String partID;

	@CTORMTemplate(seq = "2", name="partName", type="Column", dataType="String", initial="", history="")
	private String partName;
	
	@CTORMTemplate(seq = "3", name="groupName", type="Column", dataType="String", initial="", history="")
	private String groupName;
	
	@CTORMTemplate(seq = "4", name="partSpec", type="Column", dataType="String", initial="", history="")
	private String partSpec;
	
	@CTORMTemplate(seq = "5", name="location", type="Column", dataType="String", initial="", history="")
	private String location;
	
	@CTORMTemplate(seq = "6", name="quantity", type="Column", dataType="Number", initial="", history="")
	private Number quantity;
	
	@CTORMTemplate(seq = "7", name="safeQuantity", type="Column", dataType="Number", initial="", history="")
	private Number safeQuantity;
	
	@CTORMTemplate(seq = "8", name="notInQuantity", type="Column", dataType="Number", initial="", history="")
	private Number notInQuantity;
	
	@CTORMTemplate(seq = "9", name="purchaseCompleteQty", type="Column", dataType="Number", initial="", history="")
	private Number purchaseCompleteQty;
	
	@CTORMTemplate(seq = "10", name="vendorID", type="Column", dataType="String", initial="", history="")
	private String vendorID;
	
	@CTORMTemplate(seq = "11", name="unit", type="Column", dataType="String", initial="", history="")
	private String unit;
	
	@CTORMTemplate(seq = "12", name="unitPrice", type="Column", dataType="Number", initial="", history="")
	private Number unitPrice;
	
	@CTORMTemplate(seq = "13", name="useFrequency", type="Column", dataType="Number", initial="", history="")
	private Number useFrequency;
	
	@CTORMTemplate(seq = "14", name="purchaseCycle", type="Column", dataType="String", initial="", history="")
	private String purchaseCycle;
	
	@CTORMTemplate(seq = "15", name="partDescription", type="Column", dataType="String", initial="", history="")
	private String partDescription;
	
	@CTORMTemplate(seq = "16", name="useDescription", type="Column", dataType="String", initial="", history="")
	private String useDescription;
	
	@CTORMTemplate(seq = "17", name="partType", type="Column", dataType="String", initial="", history="")
	private String partType;
	
	@CTORMTemplate(seq = "18", name="partAttribute", type="Column", dataType="String", initial="", history="")
	private String partAttribute;
	
	@CTORMTemplate(seq = "19", name="partGroup", type="Column", dataType="String", initial="", history="")
	private String partGroup;
	
	@CTORMTemplate(seq = "20", name="orderQuantity", type="Column", dataType="Number", initial="", history="")
	private Number orderQuantity;
	
	@CTORMTemplate(seq = "21", name="warningQuantity", type="Column", dataType="Number", initial="", history="")
	private Number warningQuantity;
	
	@CTORMTemplate(seq = "22", name="materialCode", type="Column", dataType="String", initial="", history="")
	private String materialCode;
	
	public SparePart()
	{
		
	}
	
	public SparePart(String partID)
	{
		setPartID(partID);
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

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getPartSpec() {
		return partSpec;
	}

	public void setPartSpec(String partSpec) {
		this.partSpec = partSpec;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Number getQuantity() {
		return quantity;
	}

	public void setQuantity(Number quantity) {
		this.quantity = quantity;
	}

	public Number getSafeQuantity() {
		return safeQuantity;
	}

	public void setSafeQuantity(Number safeQuantity) {
		this.safeQuantity = safeQuantity;
	}

	public Number getNotInQuantity() {
		return notInQuantity;
	}

	public void setNotInQuantity(Number notInQuantity) {
		this.notInQuantity = notInQuantity;
	}

	public Number getPurchaseCompleteQty() {
		return purchaseCompleteQty;
	}

	public void setPurchaseCompleteQty(Number purchaseCompleteQty) {
		this.purchaseCompleteQty = purchaseCompleteQty;
	}

	public String getVendorID() {
		return vendorID;
	}

	public void setVendorID(String vendorID) {
		this.vendorID = vendorID;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Number getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(Number unitPrice) {
		this.unitPrice = unitPrice;
	}

	public Number getUseFrequency() {
		return useFrequency;
	}

	public void setUseFrequency(Number useFrequency) {
		this.useFrequency = useFrequency;
	}

	public String getPurchaseCycle() {
		return purchaseCycle;
	}

	public void setPurchaseCycle(String purchaseCycle) {
		this.purchaseCycle = purchaseCycle;
	}

	public String getPartDescription() {
		return partDescription;
	}

	public void setPartDescription(String partDescription) {
		this.partDescription = partDescription;
	}

	public String getUseDescription() {
		return useDescription;
	}

	public void setUseDescription(String useDescription) {
		this.useDescription = useDescription;
	}

	public String getPartType() {
		return partType;
	}

	public void setPartType(String partType) {
		this.partType = partType;
	}

	public String getPartAttribute() {
		return partAttribute;
	}

	public void setPartAttribute(String partAttribute) {
		this.partAttribute = partAttribute;
	}

	public String getPartGroup() {
		return partGroup;
	}

	public void setPartGroup(String partGroup) {
		this.partGroup = partGroup;
	}
	
	public Number getOrderQuantity() {
		return orderQuantity;
	}

	public void setOrderQuantity(Number orderQuantity) {
		this.orderQuantity = orderQuantity;
	}

	public Number getWarningQuantity() {
		return warningQuantity;
	}

	public void setWarningQuantity(Number warningQuantity) {
		this.warningQuantity = warningQuantity;
	}

	public String getMaterialCode() {
		return materialCode;
	}

	public void setMaterialCode(String materialCode) {
		this.materialCode = materialCode;
	}
}
