package kr.co.aim.messolution.fgms.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class PackingSTDDef extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="productSpecName", type="Key", dataType="String", initial="", history="")
	private String productSpecName;

	@CTORMTemplate(seq = "2", name="description", type="Column", dataType="String", initial="", history="")
	private String description;
	
	@CTORMTemplate(seq = "3", name="panelWeight", type="Column", dataType="Number", initial="", history="")
	private long panelWeight;
	
	@CTORMTemplate(seq = "4", name="boxWeight", type="Column", dataType="Number", initial="", history="")
	private long boxWeight;
	
	@CTORMTemplate(seq = "5", name="emptyBoxWeight", type="Column", dataType="Number", initial="", history="")
	private long emptyBoxWeight;
	
	@CTORMTemplate(seq = "6", name="palletWeight", type="Column", dataType="Number", initial="", history="")
	private long palletWeight;
	
	@CTORMTemplate(seq = "7", name="width", type="Column", dataType="Number", initial="", history="")
	private long width;
	
	@CTORMTemplate(seq = "8", name="length", type="Column", dataType="Number", initial="", history="")
	private long length;
	
	@CTORMTemplate(seq = "9", name="height1", type="Column", dataType="Number", initial="", history="")
	private long height1;
	
	@CTORMTemplate(seq = "10", name="height2", type="Column", dataType="Number", initial="", history="")
	private long height2;
	
	@CTORMTemplate(seq = "11", name="height3", type="Column", dataType="Number", initial="", history="")
	private long height3;
	
	@CTORMTemplate(seq = "12", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "13", name="lastEventUser", type="Column", dataType="String", initial="", history="")
	private String lastEventUser;

	//instantiation
	public PackingSTDDef()
	{
		
	}
	
	public PackingSTDDef(String productSpecName)
	{
		setProductSpecName(productSpecName);
	}

	public String getProductSpecName() {
		return productSpecName;
	}

	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getPanelWeight() {
		return panelWeight;
	}

	public void setPanelWeight(long panelWeight) {
		this.panelWeight = panelWeight;
	}

	public long getBoxWeight() {
		return boxWeight;
	}

	public void setBoxWeight(long boxWeight) {
		this.boxWeight = boxWeight;
	}

	public long getEmptyBoxWeight() {
		return emptyBoxWeight;
	}

	public void setEmptyBoxWeight(long emptyBoxWeight) {
		this.emptyBoxWeight = emptyBoxWeight;
	}

	public long getPalletWeight() {
		return palletWeight;
	}

	public void setPalletWeight(long palletWeight) {
		this.palletWeight = palletWeight;
	}

	public long getWidth() {
		return width;
	}

	public void setWidth(long width) {
		this.width = width;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public long getHeight1() {
		return height1;
	}

	public void setHeight1(long height1) {
		this.height1 = height1;
	}

	public long getHeight2() {
		return height2;
	}

	public void setHeight2(long height2) {
		this.height2 = height2;
	}

	public long getHeight3() {
		return height3;
	}

	public void setHeight3(long height3) {
		this.height3 = height3;
	}

	public Timestamp getLastEventTime() {
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}

	public String getLastEventUser() {
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}
}
