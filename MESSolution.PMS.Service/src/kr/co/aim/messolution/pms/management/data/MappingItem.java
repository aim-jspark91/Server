package kr.co.aim.messolution.pms.management.data;
import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "PMS", divider="_")
public class MappingItem extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="pmCode", type="Key", dataType="String", initial="", history="")
	private String pmCode;

	@CTORMTemplate(seq = "2", name="checkID", type="Key", dataType="String", initial="", history="")
	private String checkID;
	
	@CTORMTemplate(seq = "3", name="eventUser", type="Column", dataType="String", initial="", history="")
	private String eventUser;
	
	@CTORMTemplate(seq = "4", name="eventTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp eventTime;
	
	public MappingItem()
	{
		
	}
	
	public MappingItem(String pmCode, String checkID )
	{
		setPmCode(pmCode);
		setCheckID(checkID);
	}

	public String getPmCode() {
		return pmCode;
	}

	public void setPmCode(String pmCode) {
		this.pmCode = pmCode;
	}

	public String getCheckID() {
		return checkID;
	}

	public void setCheckID(String checkID) {
		this.checkID = checkID;
	}

	public String getEventUser() {
		return eventUser;
	}

	public void setEventUser(String eventUser) {
		this.eventUser = eventUser;
	}

	public Timestamp getEventTime() {
		return eventTime;
	}

	public void setEventTime(Timestamp eventTime) {
		this.eventTime = eventTime;
	}
}
