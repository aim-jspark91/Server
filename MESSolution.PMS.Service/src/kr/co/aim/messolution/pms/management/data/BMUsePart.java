package kr.co.aim.messolution.pms.management.data;
import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "PMS", divider="_")
public class BMUsePart extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="bmID", type="Key", dataType="String", initial="", history="")
	private String bmID;

	@CTORMTemplate(seq = "2", name="partID", type="Key", dataType="String", initial="", history="")
	private String partID;
	
	@CTORMTemplate(seq = "3", name="useQuantity", type="Column", dataType="Number", initial="", history="")
	private Number useQuantity;
		
	@CTORMTemplate(seq = "4", name="eventTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp eventTime;
	
	public BMUsePart()
	{
		
	}
	
	public BMUsePart(String bmID,String partID)
	{
		setBmID(bmID);
		setPartID(partID);
	}
	
	public String getBmID() {
		return bmID;
	}

	public void setBmID(String bmID) {
		this.bmID = bmID;
	}

	public String getPartID() {
		return partID;
	}

	public void setPartID(String partID) {
		this.partID = partID;
	}

	public Number getUseQuantity() {
		return useQuantity;
	}

	public void setUseQuantity(Number useQuantity) {
		this.useQuantity = useQuantity;
	}
	
	public Timestamp getEventTime() {
		return eventTime;
	} 

	public void setEventTime(Timestamp eventTime) {
		this.eventTime = eventTime;
	}
}
