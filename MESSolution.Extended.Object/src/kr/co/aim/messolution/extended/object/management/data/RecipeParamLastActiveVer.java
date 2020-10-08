package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class RecipeParamLastActiveVer extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "2", name="recipeName", type="Key", dataType="String", initial="", history="")
	private String recipeName;
	
	@CTORMTemplate(seq = "3", name="recipeParameterName", type="Key", dataType="String", initial="", history="")
	private String recipeParameterName;
	
	@CTORMTemplate(seq = "4", name="value", type="Column", dataType="String", initial="", history="")
	private String value;

	@CTORMTemplate(seq = "5", name="validationType", type="Column", dataType="String", initial="", history="")
	private String validationType;

	@CTORMTemplate(seq = "6", name="target", type="Column", dataType="String", initial="", history="")
	private String target;
	
	@CTORMTemplate(seq = "7", name="lowerLimit", type="Column", dataType="String", initial="", history="")
	private String lowerLimit;
	
	@CTORMTemplate(seq = "8", name="upperLimit", type="Column", dataType="String", initial="", history="")
	private String upperLimit;
	
	@CTORMTemplate(seq = "9", name="compareResult", type="Column", dataType="String", initial="", history="")
	private String compareResult;
	
	@CTORMTemplate(seq = "10", name="parameterState", type="Column", dataType="String", initial="", history="")
	private String parameterState;
	
	@CTORMTemplate(seq = "11", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "12", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "13", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "14", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "15", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "16", name="currentChangeValue", type="Column", dataType="String", initial="", history="")
	private String currentChangeValue;
	
	@CTORMTemplate(seq = "17", name="lastActiveTimeKey", type="Column", dataType="String", initial="", history="")
	private String lastActiveTimeKey;

	//instantiation
	public RecipeParamLastActiveVer()
	{
		
	}

	public RecipeParamLastActiveVer(String machineName, String recipeName,
			String recipeParameterName) {
		super();
		this.machineName = machineName;
		this.recipeName = recipeName;
		this.recipeParameterName = recipeParameterName;
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

	public String getRecipeParameterName() {
		return recipeParameterName;
	}

	public void setRecipeParameterName(String recipeParameterName) {
		this.recipeParameterName = recipeParameterName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValidationType() {
		return validationType;
	}

	public void setValidationType(String validationType) {
		this.validationType = validationType;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getLowerLimit() {
		return lowerLimit;
	}

	public void setLowerLimit(String lowerLimit) {
		this.lowerLimit = lowerLimit;
	}

	public String getUpperLimit() {
		return upperLimit;
	}

	public void setUpperLimit(String upperLimit) {
		this.upperLimit = upperLimit;
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

	public String getParameterState() {
		return parameterState;
	}

	public void setParameterState(String parameterState) {
		this.parameterState = parameterState;
	}

	public String getCurrentChangeValue() {
		return currentChangeValue;
	}

	public void setCurrentChangeValue(String currentChangeValue) {
		this.currentChangeValue = currentChangeValue;
	}

	public String getLastActiveTimeKey() {
		return lastActiveTimeKey;
	}

	public void setLastActiveTimeKey(String lastActiveTimeKey) {
		this.lastActiveTimeKey = lastActiveTimeKey;
	}
	
	
}
