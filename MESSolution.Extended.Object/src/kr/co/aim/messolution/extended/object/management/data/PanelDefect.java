package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class PanelDefect extends UdfAccessor{

	@CTORMTemplate(seq = "1", name="PanelName", type="Key", dataType="String", initial="", history="")
	private String PanelName;
	
	@CTORMTemplate(seq = "2", name="TimeKey", type="Key", dataType="String", initial="", history="")
	private String TimeKey;
	
	@CTORMTemplate(seq = "3", name="DefectCode", type="Column", dataType="String", initial="", history="")
	private String DefectCode;
	
	@CTORMTemplate(seq = "4", name="PanelGrade", type="Column", dataType="String", initial="", history="")
	private String PanelGrade;
	
	@CTORMTemplate(seq = "5", name="EventComment", type="Column", dataType="String", initial="", history="")
	private String EventComment;
	
	@CTORMTemplate(seq = "6", name="EventUser", type="Column", dataType="String", initial="", history="")
	private String EventUser;
	
	@CTORMTemplate(seq = "7", name="Count", type="Column", dataType="String", initial="", history="")
	private String Count;
	
	@CTORMTemplate(seq = "9", name="ProcessOperationname", type="Column", dataType="String", initial="", history="")
	private String ProcessOperationname;
	
	@CTORMTemplate(seq = "10", name="LotName", type="Column", dataType="String", initial="", history="")
	private String LotName;
	
	public String getPanelName() {
		return PanelName;
	}
	public void setPanelName(String panelName) {
		this.PanelName = panelName;
	}
	
	public String getTimeKey() {
		return TimeKey;
	}
	public void setTimeKey(String timeKey) {
		this.TimeKey = timeKey;
	}
	
	public String getDefectCode() {
		return DefectCode;
	}
	public void setDefectCode(String defectCode) {
		this.DefectCode = defectCode;
	}
	
	public String getPanelGrade() {
		return PanelGrade;
	}
	public void setPanelGrade(String panelGrade) {
		this.PanelGrade = panelGrade;
	}
	
	public String getEventComment() {
		return EventComment;
	}
	public void setEventComment(String eventComment) {
		this.EventComment = eventComment;
	}
	
	public String getEventUser() {
		return EventUser;
	}
	public void setEventUser(String eventUser) {
		this.EventUser = eventUser;
	}
	
	public String getCount() {
		return EventUser;
	}
	public void setCount(String Count) {
		this.Count = Count;
	}
	
	public String getProcessOperationname() {
		return ProcessOperationname;
	}
	public void setProcessOperationname(String ProcessOperationname) {
		this.ProcessOperationname = ProcessOperationname;
	}
	
	public String getLotName() {
		return LotName;
	}
	public void setLotName(String LotName) {
		this.LotName = LotName;
	}
	//instantiation
	public PanelDefect()
	{
		
	}
	
	public PanelDefect(String panelName, String timeKey)
	{
		setPanelName(panelName);
		setTimeKey(timeKey);

	}
	
}
