package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class RecipeRelation extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="parentMachine", type="Key", dataType="String", initial="", history="")
	private String parentMachine;
	
	@CTORMTemplate(seq = "2", name="parentMachineRecipe", type="Key", dataType="String", initial="", history="")
	private String parentMachineRecipe;
	
	@CTORMTemplate(seq = "3", name="childMachine", type="Key", dataType="String", initial="", history="")
	private String childMachine;
	
	@CTORMTemplate(seq = "4", name="childMachineRecipe", type="Key", dataType="String", initial="", history="")
	private String childMachineRecipe;
	
	@CTORMTemplate(seq = "5", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "6", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "7", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "8", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "9", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;

	//instantiation
	public RecipeRelation()
	{
		
	}

	public RecipeRelation(String parentMachine, String parentMachineRecipe,
			String childMachine, String childMachineRecipe) {
		super();
		this.parentMachine = parentMachine;
		this.parentMachineRecipe = parentMachineRecipe;
		this.childMachine = childMachine;
		this.childMachineRecipe = childMachineRecipe;
	}

	public String getParentMachine() {
		return parentMachine;
	}

	public void setParentMachine(String parentMachine) {
		this.parentMachine = parentMachine;
	}

	public String getParentMachineRecipe() {
		return parentMachineRecipe;
	}

	public void setParentMachineRecipe(String parentMachineRecipe) {
		this.parentMachineRecipe = parentMachineRecipe;
	}

	public String getChildMachine() {
		return childMachine;
	}

	public void setChildMachine(String childMachine) {
		this.childMachine = childMachine;
	}

	public String getChildMachineRecipe() {
		return childMachineRecipe;
	}

	public void setChildMachineRecipe(String childMachineRecipe) {
		this.childMachineRecipe = childMachineRecipe;
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
	
}
