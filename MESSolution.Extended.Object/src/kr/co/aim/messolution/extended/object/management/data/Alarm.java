package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class Alarm extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="alarmCode", type="Key", dataType="String", initial="", history="")
	private String alarmCode;

	@CTORMTemplate(seq = "2", name="timeKey", type="Key", dataType="String", initial="", history="")
	private String timeKey;
	
	@CTORMTemplate(seq = "3", name="factoryName", type="Column", dataType="String", initial="", history="")
	private String factoryName;
	
	@CTORMTemplate(seq = "4", name="alarmState", type="Column", dataType="String", initial="", history="")
	private String alarmState;
	
	@CTORMTemplate(seq = "5", name="alarmType", type="Column", dataType="String", initial="", history="")
	private String alarmType;
	
	@CTORMTemplate(seq = "6", name="alarmSeverity", type="Column", dataType="String", initial="", history="")
	private String alarmSeverity;
	
	@CTORMTemplate(seq = "7", name="description", type="Column", dataType="String", initial="", history="")
	private String description;
	
	@CTORMTemplate(seq = "8", name="createTimeKey", type="Column", dataType="String", initial="", history="")
	private String createTimeKey;
	
	@CTORMTemplate(seq = "9", name="createUser", type="Column", dataType="String", initial="", history="")
	private String createUser;
	
	@CTORMTemplate(seq = "10", name="resolveTimeKey", type="Column", dataType="String", initial="", history="")
	private String resolveTimeKey;
	
	@CTORMTemplate(seq = "11", name="resolveUser", type="Column", dataType="String", initial="", history="")
	private String resolveUser;
	
	@CTORMTemplate(seq = "12", name="lastEventName", type="Column", dataType="String", initial="", history="")
	private String lastEventName;
	
	@CTORMTemplate(seq = "13", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "14", name="lastEventUser", type="Column", dataType="String", initial="", history="")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "15", name="lastEventComment", type="Column", dataType="String", initial="", history="")
	private String lastEventComment;
	
	// -----------------------------------------------------------------------------------------------------------
	// Added by smkang on 2018.05.07 - MACHINENAME, UNITNAME and PRODUCTLIST are missed.
	@CTORMTemplate(seq = "16", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "17", name="unitName", type="Column", dataType="String", initial="", history="")
	private String unitName;
	
	@CTORMTemplate(seq = "18", name="productList", type="Column", dataType="String", initial="", history="")
	private String productList;
	// -----------------------------------------------------------------------------------------------------------
	
	@CTORMTemplate(seq = "19", name="subUnitName", type="Column", dataType="String", initial="", history="")
	private String subUnitName;
	
	//instantiation
	public Alarm()
	{
		
	}

	public Alarm(String alarmCode,  String timeKey)
	{
		setAlarmCode(alarmCode);
		setTimeKey(timeKey);
	}
	
	public String getAlarmCode() {
		return alarmCode;
	}

	public void setAlarmCode(String alarmCode) {
		this.alarmCode = alarmCode;
	}

	public String getTimeKey() {
		return timeKey;
	}

	public void setTimeKey(String timeKey) {
		this.timeKey = timeKey;
	}

	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	public String getAlarmState() {
		return alarmState;
	}

	public void setAlarmState(String alarmState) {
		this.alarmState = alarmState;
	}

	public String getAlarmType() {
		return alarmType;
	}

	public void setAlarmType(String alarmType) {
		this.alarmType = alarmType;
	}

	public String getAlarmSeverity() {
		return alarmSeverity;
	}

	public void setAlarmSeverity(String alarmSeverity) {
		this.alarmSeverity = alarmSeverity;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCreateTimeKey() {
		return createTimeKey;
	}

	public void setCreateTimeKey(String createTimeKey) {
		this.createTimeKey = createTimeKey;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getResolveTimeKey() {
		return resolveTimeKey;
	}

	public void setResolveTimeKey(String resolveTimeKey) {
		this.resolveTimeKey = resolveTimeKey;
	}

	public String getResolveUser() {
		return resolveUser;
	}

	public void setResolveUser(String resolveUser) {
		this.resolveUser = resolveUser;
	}

	public String getLastEventName() {
		return lastEventName;
	}

	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}

	public String getLastEventTimeKey() {
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey) {
		this.lastEventTimeKey = lastEventTimeKey;
	}

	public String getLastEventUser() {
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}

	public String getLastEventComment() {
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}
	
	// ----------------------------------------------------------------------------------
	// Added by smkang on 2018.05.05 - MACHINENAME, UNITNAME and PRODUCTLIST are missed.
	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}
	
	public String getUnitName() {
		return unitName;
	}
	
	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}
	
	public String getProductList() {
		return productList;
	}
	
	public void setProductList(String productList) {
		this.productList = productList;
	}
	// ----------------------------------------------------------------------------------

	public String getSubUnitName() {
		return subUnitName;
	}

	public void setSubUnitName(String subUnitName) {
		this.subUnitName = subUnitName;
	}
	
	
}
