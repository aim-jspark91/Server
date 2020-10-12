package kr.co.aim.messolution.pms.management.data;
import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "PMS", divider="_")
public class CheckList extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="No", type="Key", dataType="String", initial="", history="")
	private String No;

	@CTORMTemplate(seq = "2", name="checkID", type="Key", dataType="String", initial="", history="")
	private String checkID;
	
	@CTORMTemplate(seq = "3", name="itemType", type="Column", dataType="String", initial="", history="")
	private String itemType;
	
	@CTORMTemplate(seq = "4", name="itemName", type="Column", dataType="String", initial="", history="")
	private String itemName;
	
	@CTORMTemplate(seq = "5", name="checkDesc", type="Column", dataType="String", initial="", history="")
	private String checkDesc;

	@CTORMTemplate(seq = "6", name="partID", type="Column", dataType="String", initial="", history="")
	private String partID;
	
	@CTORMTemplate(seq = "7", name="partName", type="Column", dataType="String", initial="", history="")
	private String partName;
	
	@CTORMTemplate(seq = "8", name="useQuantity", type="Column", dataType="String", initial="", history="")
	private String useQuantity;
	
	public CheckList()
	{
		
	}
	
	public CheckList(String No, String checkID )
	{
		setNo(No);
		setCheckID(checkID);
	}

	public String getNo() {
		return No;
	}

	public void setNo(String no) {
		No = no;
	}

	public String getCheckID() {
		return checkID;
	}

	public void setCheckID(String checkID) {
		this.checkID = checkID;
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
}
