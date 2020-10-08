package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class Recipe extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "2", name="recipeName", type="Key", dataType="String", initial="", history="")
	private String recipeName;
	
	@CTORMTemplate(seq = "3", name="description", type="Column", dataType="String", initial="", history="")
	private String description;
	
	@CTORMTemplate(seq = "4", name="recipeType", type="Column", dataType="String", initial="", history="")
	private String recipeType;
	
	@CTORMTemplate(seq = "5", name="compareFlag", type="Column", dataType="String", initial="", history="")
	private String compareFlag;
	
	@CTORMTemplate(seq = "6", name="recipeState", type="Column", dataType="String", initial="", history="")
	private String recipeState;
	
	@CTORMTemplate(seq = "7", name="checkState", type="Column", dataType="String", initial="", history="")
	private String checkState;
	
	@CTORMTemplate(seq = "8", name="checkOutTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp checkOutTime;
	
	@CTORMTemplate(seq = "9", name="checkOutUser", type="Column", dataType="String", initial="", history="")
	private String checkOutUser;
	
	@CTORMTemplate(seq = "10", name="approveTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp approveTime;
	
	@CTORMTemplate(seq = "11", name="compareResult", type="Column", dataType="String", initial="", history="")
	private String compareResult;
	
	@CTORMTemplate(seq = "12", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "13", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "14", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "15", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "16", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "17", name="sequence", type="Column", dataType="String", initial="", history="")
	private String sequence;
	
	@CTORMTemplate(seq = "18", name="relationFlag", type="Column", dataType="String", initial="", history="")
	private String relationFlag;
	
	@CTORMTemplate(seq = "19", name="activeResult", type="Column", dataType="String", initial="", history="")
	private String activeResult;
	
	@CTORMTemplate(seq = "20", name="lastActiveTimeKey", type="Column", dataType="String", initial="", history="")
	private String lastActiveTimeKey;

	//instantiation
	public Recipe()
	{
		
	}
	
	public Recipe(String machineName, String recipeName)
	{
		setMachineName(machineName);
		setRecipeName(recipeName);
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getRecipeName() {
		return recipeName;
	}

	public void setRecipeName(String recipeName) {
		this.recipeName = recipeName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRecipeType() {
		return recipeType;
	}

	public void setRecipeType(String recipeType) {
		this.recipeType = recipeType;
	}

	public String getCompareFlag() {
		return compareFlag;
	}

	public void setCompareFlag(String compareFlag) {
		this.compareFlag = compareFlag;
	}

	public String getRecipeState() {
		return recipeState;
	}

	public void setRecipeState(String recipeState) {
		this.recipeState = recipeState;
	}

	public String getCheckState() {
		return checkState;
	}

	public void setCheckState(String checkState) {
		this.checkState = checkState;
	}

	public Timestamp getCheckOutTime() {
		return checkOutTime;
	}

	public void setCheckOutTime(Timestamp checkOutTime) {
		this.checkOutTime = checkOutTime;
	}

	public String getCheckOutUser() {
		return checkOutUser;
	}

	public void setCheckOutUser(String checkOutUser) {
		this.checkOutUser = checkOutUser;
	}

	public Timestamp getApproveTime() {
		return approveTime;
	}

	public void setApproveTime(Timestamp approveTime) {
		this.approveTime = approveTime;
	}

	public String getCompareResult() {
		return compareResult;
	}

	public void setCompareResult(String compareResult) {
		this.compareResult = compareResult;
	}

	public Timestamp getLastEventTime() {
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}

	public String getLastEventTimeKey() {
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey) {
		this.lastEventTimeKey = lastEventTimeKey;
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

	public String getLastEventComment() {
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public String getRelationFlag() {
		return relationFlag;
	}

	public void setRelationFlag(String relationFlag) {
		this.relationFlag = relationFlag;
	}

	public String getActiveResult() {
		return activeResult;
	}

	public void setActiveResult(String activeResult) {
		this.activeResult = activeResult;
	}

	public String getLastActiveTimeKey() {
		return lastActiveTimeKey;
	}

	public void setLastActiveTimeKey(String lastActiveTimeKey) {
		this.lastActiveTimeKey = lastActiveTimeKey;
	}
	
	
	
}
