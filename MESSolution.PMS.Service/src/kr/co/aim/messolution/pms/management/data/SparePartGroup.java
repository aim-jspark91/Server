package kr.co.aim.messolution.pms.management.data;
import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "PMS", divider="_")
public class SparePartGroup extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="partGroup", type="Key", dataType="String", initial="", history="")
	private String partGroup;

	@CTORMTemplate(seq = "2", name="groupName", type="Column", dataType="String", initial="", history="")
	private String groupName;
	
	@CTORMTemplate(seq = "3", name="description", type="Column", dataType="String", initial="", history="")
	private String description;
		
	public SparePartGroup()
	{
		
	}
	
	public SparePartGroup(String partGroup)
	{
		setPartGroup(partGroup);
	}

	public String getPartGroup() {
		return partGroup;
	}

	public void setPartGroup(String partGroup) {
		this.partGroup = partGroup;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
