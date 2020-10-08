package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

import org.apache.commons.lang.StringUtils;

public class MachineAlarmList extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	
	// Modified by smkang on 2019.02.20 - UnitName is changed as a key of this table.
	@CTORMTemplate(seq = "2", name="unitName", type="Key", dataType="String", initial="", history="")
	private String unitName;

	// Modified by smkang on 2019.02.20 - SubUnitName is changed as a key of this table.
	@CTORMTemplate(seq = "3", name="subUnitName", type="Key", dataType="String", initial="", history="")
	private String subUnitName;
		
	@CTORMTemplate(seq = "4", name="alarmCode", type="Key", dataType="String", initial="", history="")
	private String alarmCode;
	
	@CTORMTemplate(seq = "5", name="alarmState", type="Column", dataType="String", initial="", history="")
	private String alarmState;
	
	@CTORMTemplate(seq = "6", name="alarmSeverity", type="Column", dataType="String", initial="", history="")
	private String alarmSeverity;
	
	@CTORMTemplate(seq = "7", name="alarmText", type="Column", dataType="String", initial="", history="")
	private String alarmText;
	
	@CTORMTemplate(seq = "8", name="productList", type="Column", dataType="String", initial="", history="")
	private String productList;
	
	@CTORMTemplate(seq = "9", name="createTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp createTime;
	
	@CTORMTemplate(seq = "10", name="createUser", type="Column", dataType="String", initial="", history="N")
	private String createUser;
	
	@CTORMTemplate(seq = "11", name="resolveTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp resolveTime;
	
	@CTORMTemplate(seq = "12", name="resolveUser", type="Column", dataType="String", initial="", history="N")
	private String resolveUser;
	
	@CTORMTemplate(seq = "13", name="issueTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp issueTime;
	
	@CTORMTemplate(seq = "14", name="issueUser", type="Column", dataType="String", initial="", history="N")
	private String issueUser;
	
	@CTORMTemplate(seq = "15", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;

	public MachineAlarmList() {
	}
	
	// Modified by smkang on 2019.02.20 - UnitName and SubUnitName are changed as a key of this table.
//	public MachineAlarmList(String machineName, String alarmCode) {
//		setMachineName(machineName);
//		setAlarmCode(alarmCode);
//	}
	public MachineAlarmList(String machineName, String unitName, String subUnitName, String alarmCode) {
		setMachineName(machineName);
		setAlarmCode(alarmCode);
		setUnitName(unitName);
		setSubUnitName(subUnitName);
	}

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
		// Modified by smkang on 2019.02.20 - Because UnitName is changed as a key of this table, null value is not permitted.
//		this.unitName = unitName;
		this.unitName = StringUtils.isNotEmpty(unitName) ? unitName : " ";
	}

	public String getSubUnitName() {
		return subUnitName;
	}

	public void setSubUnitName(String subUnitName) {
		// Modified by smkang on 2019.02.20 - Because SubUnitName is changed as a key of this table, null value is not permitted.
//		this.subUnitName = subUnitName;
		this.subUnitName = StringUtils.isNotEmpty(subUnitName) ? subUnitName : " ";
	}

	public String getAlarmCode() {
		return alarmCode;
	}

	public void setAlarmCode(String alarmCode) {
		this.alarmCode = alarmCode;
	}

	public String getAlarmState() {
		return alarmState;
	}

	public void setAlarmState(String alarmState) {
		this.alarmState = alarmState;
	}

	public String getAlarmSeverity() {
		return alarmSeverity;
	}

	public void setAlarmSeverity(String alarmSeverity) {
		this.alarmSeverity = alarmSeverity;
	}

	public String getAlarmText() {
		return alarmText;
	}

	public void setAlarmText(String alarmText) {
		this.alarmText = alarmText;
	}

	public String getProductList() {
		return productList;
	}

	public void setProductList(String productList) {
		this.productList = productList;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Timestamp getResolveTime() {
		return resolveTime;
	}

	public void setResolveTime(Timestamp resolveTime) {
		this.resolveTime = resolveTime;
	}

	public String getResolveUser() {
		return resolveUser;
	}

	public void setResolveUser(String resolveUser) {
		this.resolveUser = resolveUser;
	}

	public Timestamp getIssueTime() {
		return issueTime;
	}

	public void setIssueTime(Timestamp issueTime) {
		this.issueTime = issueTime;
	}

	public String getIssueUser() {
		return issueUser;
	}

	public void setIssueUser(String issueUser) {
		this.issueUser = issueUser;
	}

	public String getLastEventTimeKey() {
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey) {
		this.lastEventTimeKey = lastEventTimeKey;
	}
}