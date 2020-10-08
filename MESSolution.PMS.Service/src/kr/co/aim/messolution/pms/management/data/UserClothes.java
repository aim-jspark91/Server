package kr.co.aim.messolution.pms.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;


@CTORMHeader(tag = "PMS", divider="_")
public class UserClothes extends UdfAccessor
{
	
	@CTORMTemplate(seq = "1", name="clothesID", type="Key", dataType="String", initial="", history="")
	private String clothesID;
	
	@CTORMTemplate(seq = "2", name="userID", type="Column", dataType="String", initial="", history="")
	private String userID;
	
	@CTORMTemplate(seq = "3", name="lastCleanTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp lastCleanTime;
	
	@CTORMTemplate(seq = "4", name="currentCleanTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp currentCleanTime;
	
	@CTORMTemplate(seq = "5", name="cleanLimitedTime", type="Column", dataType="Number", initial="", history="")
	private Number cleanLimitedTime;
	
	@CTORMTemplate(seq = "6", name="cleanState", type="Column", dataType="String", initial="", history="")
	private String cleanState;
	
	@CTORMTemplate(seq = "7", name="description", type="Column", dataType="String", initial="", history="")
	private String description;
	
	@CTORMTemplate(seq = "8", name="lasteventName", type="Column", dataType="String", initial="", history="")
	private String lasteventName;
	
	@CTORMTemplate(seq = "9", name="lasteventTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp lasteventTime;

	@CTORMTemplate(seq = "10", name="lasteventTimeKey", type="Column", dataType="String", initial="", history="")
	private String lasteventTimeKey;
	
	@CTORMTemplate(seq = "11", name="clothesSpecName", type="Column", dataType="String", initial="", history="")
	private String clothesSpecName;
	
	@CTORMTemplate(seq = "12", name="lasteventComment", type="Column", dataType="String", initial="", history="")
	private String lasteventComment;
	
	@CTORMTemplate(seq = "13", name="createUserID", type="Column", dataType="String", initial="", history="")
	private String createUserID;
	
	public String getCreateUserID()
	{
		return createUserID;
	}
	public void setCreateUserID(String createUserID)
	{
		this.createUserID = createUserID;
	}
	public UserClothes()
	{
		
	}
	public String getUserID()
	{
		return userID;
	}

	public void setUserID(String userID)
	{
		this.userID = userID;
	}

	public String getClothesID()
	{
		return clothesID;
	}

	public void setClothesID(String clothesID)
	{
		this.clothesID = clothesID;
	}

	public Timestamp getLastCleanTime()
	{
		return lastCleanTime;
	}

	public void setLastCleanTime(Timestamp lastCleanTime)
	{
		this.lastCleanTime = lastCleanTime;
	}

	public Timestamp getCurrentCleanTime()
	{
		return currentCleanTime;
	}

	public void setCurrentCleanTime(Timestamp currentCleanTime)
	{
		this.currentCleanTime = currentCleanTime;
	}

	public Number getCleanLimitedTime()
	{
		return cleanLimitedTime;
	}

	public void setCleanLimitedTime(Number limitDay)
	{
		this.cleanLimitedTime = limitDay;
	}

	public String getCleanState()
	{
		return cleanState;
	}

	public void setCleanState(String cleanState)
	{
		this.cleanState = cleanState;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getLasteventName()
	{
		return lasteventName;
	}

	public void setLasteventName(String lasteventName)
	{
		this.lasteventName = lasteventName;
	}

	public Timestamp getLasteventTime()
	{
		return lasteventTime;
	}

	public void setLasteventTime(Timestamp lasteventTime)
	{
		this.lasteventTime = lasteventTime;
	}

	public String getLasteventTimeKey()
	{
		return lasteventTimeKey;
	}

	public void setLasteventTimeKey(String string)
	{
		this.lasteventTimeKey = string;
	}

	public String getClothesSpecName()
	{
		return clothesSpecName;
	}

	public void setClothesSpecName(String clothesSpecName)
	{
		this.clothesSpecName = clothesSpecName;
	}

	public String getLasteventComment()
	{
		return lasteventComment;
	}

	public void setLasteventComment(String lasteventComment)
	{
		this.lasteventComment = lasteventComment;
	}

	
}
