package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class PanelJudgeTest extends UdfAccessor{
	@CTORMTemplate(seq = "1", name="GlassName", type="Key", dataType="String", initial="", history="")
	private String glassName;
	
	@CTORMTemplate(seq = "2", name="XAXIS", type="Key", dataType="int", initial="", history="")
	private int XAXIS;
	
	@CTORMTemplate(seq = "3", name="YAXIS", type="Key", dataType="int", initial="", history="")
	private int YAXIS;
	
	@CTORMTemplate(seq = "4", name="PanelJudge", type="Column", dataType="String", initial="", history="")
	private String panelJudge;
	
	@CTORMTemplate(seq = "5", name="LastEventName", type="Column", dataType="String", initial="", history="")
	private String LastEventName;
	
	@CTORMTemplate(seq = "6", name="LastEventUser", type="Column", dataType="String", initial="", history="")
	private String LastEventUser;
	
	@CTORMTemplate(seq = "7", name="LastEventTime", type="Column", dataType="TimeStamp", initial="", history="")
	private Timestamp LastEventTime;
	
	@CTORMTemplate(seq = "8", name="LastEventComment", type="Column", dataType="String", initial="", history="")
	private String LastEventComment;
	
	public String getGlassName() {
		return glassName;
	}

	public void setGlassName(String glassName) {
		this.glassName = glassName;
	}

	public int getXAXIS() {
		return XAXIS;
	}

	public void setXAXIS(int x) {
		this.XAXIS = x;
	}
	
	public int getYAXIS() {
		return YAXIS;
	}

	public void setYAXIS(int y) {
		this.YAXIS = y;
	}

	public String getPanelJudge() {
		return panelJudge;
	}

	public void setPanelJudge(String panelJudge) {
		this.panelJudge = panelJudge;
	}

	public String getLastEvenName() {
		return LastEventName;
	}

	public void setLastEvenName(String lastEvenName) {
		LastEventName = lastEvenName;
	}

	public String getLastEventUser() {
		return LastEventUser;
	}

	public void setLastEventUser(String lastEventUser) {
		LastEventUser = lastEventUser;
	}

	public Timestamp getLastEventTime() {
		return LastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime) {
		LastEventTime = lastEventTime;
	}

	public String getLastEventComment() {
		return LastEventComment;
	}

	public void setLastEventComment(String lastEventComment) {
		LastEventComment = lastEventComment;
	}
}
