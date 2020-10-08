package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class HQGlassJudge extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="HQGlassName", type="Key", dataType="String", initial="", history="")
	private String HQGlassName;

	@CTORMTemplate(seq = "2", name="HQGlassJudge", type="Column", dataType="String", initial="", history="")
	private String HQGlassJudge;
	
	@CTORMTemplate(seq = "3", name="XAxis", type="Column", dataType="Long", initial="", history="")
	private long XAxis;
	
	@CTORMTemplate(seq = "4", name="YAxis", type="Column", dataType="Long", initial="", history="")
	private long YAxis;
	
	@CTORMTemplate(seq = "5", name="GlassName", type="Column", dataType="String", initial="", history="")
	private String GlassName;
	
	@CTORMTemplate(seq = "6", name="LastEventName", type="Column", dataType="String", initial="", history="N")
	private String LastEventName;
	
	@CTORMTemplate(seq = "7", name="LastEventUser", type="Column", dataType="String", initial="", history="N")
	private String LastEventUser;
	
	@CTORMTemplate(seq = "8", name="LastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp LastEventTime;
	
	@CTORMTemplate(seq = "9", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	

	public HQGlassJudge()
	{
		
	}


	public String getHQGlassName() {
		return HQGlassName;
	}


	public void setHQGlassName(String hQGlassName) {
		HQGlassName = hQGlassName;
	}


	public String getHQGlassJudge() {
		return HQGlassJudge;
	}


	public void setHQGlassJudge(String hQGlassJudge) {
		HQGlassJudge = hQGlassJudge;
	}


	public long getXAxis() {
		return XAxis;
	}


	public void setXAxis(long xAxis) {
		XAxis = xAxis;
	}


	public long getYAxis() {
		return YAxis;
	}


	public void setYAxis(long yAxis) {
		YAxis = yAxis;
	}


	public String getGlassName() {
		return GlassName;
	}


	public void setGlassName(String glassName) {
		GlassName = glassName;
	}

	public Timestamp getLastEventTime() {
		return LastEventTime;
	}


	public void setLastEventTime(Timestamp lastEventTime) {
		LastEventTime = lastEventTime;
	}


	public String getLastEventName() {
		return LastEventName;
	}


	public void setLastEventName(String lastEventName) {
		LastEventName = lastEventName;
	}


	public String getLastEventUser() {
		return LastEventUser;
	}


	public void setLastEventUser(String lastEventUser) {
		LastEventUser = lastEventUser;
	}


	public String getLastEventComment() {
		return lastEventComment;
	}


	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}



	

}
