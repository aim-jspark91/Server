package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class PanelInspData extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="PanelName", type="Key", dataType="String", initial="", history="")
	private String PanelName;
	
	@CTORMTemplate(seq = "2", name="InspCount", type="Key", dataType="String", initial="", history="")
	private String InspCount;
	
	@CTORMTemplate(seq = "3", name="FileName", type="Key", dataType="String", initial="", history="")
	private String FileName;
	
	@CTORMTemplate(seq = "4", name="JigID", type="Column", dataType="String", initial="", history="")
	private String JigID;
	
	@CTORMTemplate(seq = "5", name="Color", type="Column", dataType="String", initial="", history="")
	private String Color;
	
	@CTORMTemplate(seq = "6", name="X", type="Column", dataType="String", initial="", history="")
	private String X;
	
	@CTORMTemplate(seq = "7", name="Y", type="Column", dataType="String", initial="", history="")
	private String Y;
	
	@CTORMTemplate(seq = "8", name="L", type="Column", dataType="String", initial="", history="")
	private String L;
	
	@CTORMTemplate(seq = "9", name="Eventtimekey", type="Column", dataType="String", initial="", history="")
	private String Eventtimekey;
	
	@CTORMTemplate(seq = "10", name="Eventtime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp Eventtime;
	
	@CTORMTemplate(seq = "11", name="Eventcomment", type="Column", dataType="String", initial="", history="")
	private String Eventcomment;
	
	@CTORMTemplate(seq = "12", name="Eventuser", type="Column", dataType="String", initial="", history="")
	private String Eventuser;
	
	@CTORMTemplate(seq = "13", name="ProcessOperationname", type="Column", dataType="String", initial="", history="")
	private String ProcessOperationname;
	
	@CTORMTemplate(seq = "14", name="LotName", type="Column", dataType="String", initial="", history="")
	private String LotName;
	
	public String getPanelName() {
		return PanelName;
	}
	public void setPanelName(String panelName) {
		this.PanelName = panelName;
	}
	
	public String getInspCount() {
		return InspCount;
	}
	public void setInspCount(String inspCount) {
		this.InspCount = inspCount;
	}
	
	public String getFileName() {
		return FileName;
	}
	public void setFileName(String fileName) {
		this.FileName = fileName;
	}
	
	public String getJigID() {
		return JigID;
	}
	public void setJigID(String jigID) {
		this.JigID = jigID;
	}
	
	public String getColor() {
		return Color;
	}
	public void setColor(String color) {
		this.Color = color;
	}
	
	public String getX() {
		return X;
	}
	public void setX(String x) {
		this.X = x;
	}
	
	public String getY() {
		return Y;
	}
	public void setY(String y) {
		this.Y = y;
	}
	
	public String getL() {
		return L;
	}
	public void setL(String l) {
		this.L = l;
	}
	
	public String getEventTimekey() {
		return Eventtimekey;
	}
	public void setEventTimekey(String eventtimekey) {
		this.Eventtimekey = eventtimekey;
	}
	
	public Timestamp getEventTime() {
		return Eventtime;
	}
	public void setEventTime(Timestamp eventtime) {
		this.Eventtime = eventtime;
	}
	
	public String getEventcomment() {
		return Eventcomment;
	}
	public void setEventcomment(String eventcomment) {
		this.Eventcomment = eventcomment;
	}
	
	public String getEventuser() {
		return Eventuser;
	}
	public void setEventuser(String eventuser) {
		this.Eventuser = eventuser;
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
	public PanelInspData()
	{
		
	}
	
	public PanelInspData(String panelName, String inspCount, String filename)
	{
		setPanelName(panelName);
		setInspCount(inspCount);
		setFileName(filename);

	}

}
