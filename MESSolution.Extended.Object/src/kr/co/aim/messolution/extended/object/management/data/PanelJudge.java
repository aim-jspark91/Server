package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class PanelJudge extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="panelName", type="Key", dataType="String", initial="", history="")
	private String panelName;
	
	@CTORMTemplate(seq = "2", name="panelJudge", type="Column", dataType="String", initial="", history="")
	private String panelJudge;
	
	@CTORMTemplate(seq = "3", name="panelGrade", type="Column", dataType="String", initial="", history="")
	private String panelGrade;
	
	@CTORMTemplate(seq = "4", name="xAxis1", type="Column", dataType="Number", initial="", history="")
	private long xAxis1;
	
	@CTORMTemplate(seq = "5", name="yAxis1", type="Column", dataType="Number", initial="", history="")
	private long yAxis1;
	
	@CTORMTemplate(seq = "6", name="xAxis2", type="Column", dataType="Number", initial="", history="")
	private long xAxis2;
	
	@CTORMTemplate(seq = "7", name="yAxis2", type="Column", dataType="Number", initial="", history="")
	private long yAxis2;
	
	@CTORMTemplate(seq = "8", name="glassName", type="Column", dataType="String", initial="", history="")
	private String glassName;
	
	@CTORMTemplate(seq = "9", name="hqGlassName", type="Column", dataType="String", initial="", history="")
	private String hqGlassName;
	
	@CTORMTemplate(seq = "10", name="cutType", type="Column", dataType="String", initial="", history="")
	private String cutType;
		
	@CTORMTemplate(seq = "11", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "12", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "13", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "14", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "15", name="processOperationName", type="Column", dataType="String", initial="", history="")
	private String processOperationName;
	
	@CTORMTemplate(seq = "16", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	
	// Added by smkang on 2018.11.20 - According to Wangli's request, assembled panel name is necessary to be updated.
	@CTORMTemplate(seq = "17", name="assembledPanelName", type="Column", dataType="String", initial="", history="")
	private String assembledPanelName;
	
	// Added by smkang on 2018.12.11 - ProductSpecType is added for report system.
	@CTORMTemplate(seq = "18", name="productSpecType", type="Column", dataType="String", initial="", history="")
	private String productSpecType;
	
	// Added by Park Jeong Su on 2019.01.25 - ProductSpecName is added for report system.
	@CTORMTemplate(seq = "19", name="productSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;
	
	// Added by Park Jeong Su on 2019.01.25 - ProductSpecType is added for report system.
	@CTORMTemplate(seq = "20", name="productRequestName", type="Column", dataType="String", initial="", history="")
	private String productRequestName;
	
	public PanelJudge() {		
	}
	
	public PanelJudge(String panelName) {
		this.panelName = panelName;
	}

	public String getPanelName() {
		return panelName;
	}

	public void setPanelName(String panelName) {
		this.panelName = panelName;
	}

	public String getPanelJudge() {
		return panelJudge;
	}

	public void setPanelJudge(String panelJudge) {
		this.panelJudge = panelJudge;
	}

	public String getPanelGrade() {
		return panelGrade;
	}

	public void setPanelGrade(String panelGrade) {
		this.panelGrade = panelGrade;
	}

	public long getxAxis1() {
		return xAxis1;
	}

	public void setxAxis1(long xAxis1) {
		this.xAxis1 = xAxis1;
	}

	public long getyAxis1() {
		return yAxis1;
	}

	public void setyAxis1(long yAxis1) {
		this.yAxis1 = yAxis1;
	}

	public long getxAxis2() {
		return xAxis2;
	}

	public void setxAxis2(long xAxis2) {
		this.xAxis2 = xAxis2;
	}

	public long getyAxis2() {
		return yAxis2;
	}

	public void setyAxis2(long yAxis2) {
		this.yAxis2 = yAxis2;
	}

	public String getGlassName() {
		return glassName;
	}

	public void setGlassName(String glassName) {
		this.glassName = glassName;
	}

	public String getHqGlassName() {
		return hqGlassName;
	}

	public void setHqGlassName(String hqGlassName) {
		this.hqGlassName = hqGlassName;
	}

	public String getCutType() {
		return cutType;
	}

	public void setCutType(String cutType) {
		this.cutType = cutType;
	}

	public String getLastEventName() {
		return lastEventName;
	}

	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}

	public String getLastEventUser() {
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}

	public Timestamp getLastEventTime() {
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}

	public String getLastEventComment() {
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}

	public String getProcessOperationName() {
		return processOperationName;
	}

	public void setProcessOperationName(String processOperationName) {
		this.processOperationName = processOperationName;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	// Added by smkang on 2018.11.20 - According to Wangli's request, assembled panel name is necessary to be updated.
	public String getAssembledPanelName() {
		return assembledPanelName;
	}

	// Added by smkang on 2018.11.20 - According to Wangli's request, assembled panel name is necessary to be updated.
	public void setAssembledPanelName(String assembledPanelName) {
		this.assembledPanelName = assembledPanelName;
	}
	
	// Added by smkang on 2018.12.11 - ProductSpecType is added for report system.
	public String getProductSpecType() {
		return productSpecType;
	}
	
	// Added by smkang on 2018.12.11 - ProductSpecType is added for report system.
	public void setProductSpecType(String productSpecType) {
		this.productSpecType = productSpecType;
	}

	public String getProductSpecName() {
		return productSpecName;
	}

	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}

	public String getProductRequestName() {
		return productRequestName;
	}

	public void setProductRequestName(String productRequestName) {
		this.productRequestName = productRequestName;
	}
	
	
}